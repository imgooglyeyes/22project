package application;

import java.util.ArrayList;

/**
 * holds the business rules for products, stock moves and sales, screens call these instead of changing data themselves
 *
 * <p>each method returns null when it worked, or a short error message when it didn't.
 * role checks are not done here, the screens decide who sees which button.
 * every method saves only the files it changed, so the other product files stay untouched
 *
 * <p>stock flows one way down the chain: database -> inventory -> shelf.
 * addToInventory and addToShelf move stock forward, moveToInventory moves it back
 *
 * <p>a product is either a harvest item (madeFrom is null) or made from one harvest item.
 * a made product can't be the source of another product, so the chain is never deeper than one level
 *
 * @author Shawn Alfred Padilla
 * @version 1.4
 * @see SaleTransaction
 * @see ProductsFileHandler
 */
public class FarmSystem {

    /** every product, used to find a product that was sold or added */
    private ProductContainer database;
    /** the products in the inventory, the shelf is stocked from here */
    private ProductContainer inventoryData;
    /** the products on the shelf, sales take stock from here */
    private ProductContainer shelfData;
    /** every sale transaction */
    private TransactionContainer transactionData;

    /**
     * creates the farm system
     *
     * @param database every product and its details
     * @param inventoryData the products and stock in the inventory
     * @param shelfData the products on the shelf
     * @param transactionData every sale transaction
     * @since 1.2 inventoryData was added as the second parameter
     */
    public FarmSystem(ProductContainer database, ProductContainer inventoryData, ProductContainer shelfData, TransactionContainer transactionData) {
        this.database = database;
        this.inventoryData = inventoryData;
        this.shelfData = shelfData;
        this.transactionData = transactionData;
    }

    // ---------- product details ----------

    /**
     * checks if a product name is already used, spaces at the ends are ignored
     * <p>windows use this to disable their confirm button while the name is a duplicate
     *
     * @param name the name to look for
     * @param exceptId the id of a product that is allowed to keep this name (the one being edited), null for none
     * @return true if another product already has this name
     * @since 1.3
     */
    public boolean isNameTaken(String name, Integer exceptId) {
        String wanted = name.trim();

        for (Product p : database.getProducts()) {
            if (exceptId != null && p.getId() == exceptId) continue; // a product can keep its own name
            if (p.getName().equals(wanted)) return true;
        }
        return false;
    }

    /**
     * lists the harvest products, these are the only products another product can be made from
     * <p>windows use this to fill their made-from picker
     *
     * @param exceptId the id of a product to leave out (the one being edited, it can't be made from itself), null for none
     * @return the harvest products of the database, these are the real products so they must only be read
     * @since 1.4
     */
    public ArrayList<Product> getHarvestProducts(Integer exceptId) {
        ArrayList<Product> result = new ArrayList<>();

        for (Product p : database.getProducts()) {
            if (p.isMade()) continue; // a made product can't be a source
            if (exceptId != null && p.getId() == exceptId) continue;
            result.add(p);
        }
        return result;
    }

    /**
     * checks if any product is made from the given product
     * <p>the edit window uses this to lock the made-from picker, because such a product has to stay a harvest item
     *
     * @param id the id of the possible source product
     * @return true if at least one product has this id as its madeFrom
     * @since 1.4
     */
    public boolean isSourceOfOthers(int id) {
        for (Product p : database.getProducts()) {
            if (p.getMadeFrom() != null && p.getMadeFrom() == id) return true;
        }
        return false;
    }

    /**
     * creates a new product in the database, it starts with no stock anywhere
     * <p>only the database file is saved
     *
     * @param name the name of the product, must be unique
     * @param desc the description of the product
     * @param price the price of one unit, 0 or higher
     * @param madeFrom the id of the harvest product it is made from, null for a harvest item
     * @return null if the product was created, or an error message
     * @since 1.3
     */
    public String createProduct(String name, String desc, double price, Integer madeFrom) {
        String error = checkDetails(name, desc, price);
        if (error != null) return error;

        if (isNameTaken(name, null)) return "A product with this name already exists";

        // checked before the product is created, because creating a product uses up an id
        error = checkMadeFrom(madeFrom, null);
        if (error != null) return error;

        Product created = new Product(name.trim(), desc.trim(), price, 0);
        created.setMadeFrom(madeFrom);

        // the database keeps its own copy, so the stock here does not matter
        database.add(created, 0);
        ProductsFileHandler.saveAll(database, null, null); // only the database file is written
        return null;
    }

    /**
     * changes the name, description, price and madeFrom of a product everywhere it exists
     * <p>the database, inventory and shelf copies are all updated, then all three files are saved.
     * the stock of the copies is not touched
     *
     * @param product the product to edit, looked up in the database by its id
     * @param name the new name, must not belong to another product
     * @param desc the new description
     * @param price the new price of one unit, 0 or higher
     * @param madeFrom the id of the harvest product it is made from, null for a harvest item
     * @return null if the product was edited, or an error message
     * @since 1.3
     */
    public String editProduct(Product product, String name, String desc, double price, Integer madeFrom) {
        if (product == null) return "No product selected";

        String error = checkDetails(name, desc, price);
        if (error != null) return error;

        Product target = database.getProductById(product.getId());
        if (target == null) return "This product no longer exists";

        if (isNameTaken(name, target.getId())) return "Another product already has this name";

        error = checkMadeFrom(madeFrom, target.getId());
        if (error != null) return error;

        // a source can't become a made product, or the chain would be two levels deep
        if (madeFrom != null && isSourceOfOthers(target.getId())) {
            return "Other products are made from this one, so it must stay a harvest item";
        }

        ProductContainer[] containers = { database, inventoryData, shelfData };
        for (ProductContainer container : containers) {
            Product copy = container.getProductById(target.getId());
            if (copy == null) continue; // not stocked in this container

            copy.setName(name.trim());
            copy.setDescription(desc.trim());
            copy.setPrice(price);
            copy.setMadeFrom(madeFrom); // every copy keeps the same madeFrom
        }

        ProductsFileHandler.saveAll(database, inventoryData, shelfData);
        return null;
    }

    /**
     * checks the details of a product before it is created or edited
     * <p>a | is not allowed because it separates the fields in the save file
     *
     * @param name the name to check
     * @param desc the description to check
     * @param price the price to check
     * @return null if everything is fine, or an error message
     */
    private String checkDetails(String name, String desc, double price) {
        if (name == null || name.trim().isEmpty()) return "Name can't be empty";
        if (desc == null || desc.trim().isEmpty()) return "Description can't be empty";
        if (name.contains("|") || desc.contains("|")) return "Name and description can't contain |";
        if (Double.isNaN(price) || Double.isInfinite(price) || price < 0) return "Price must be a number, 0 or higher";
        return null;
    }

    /**
     * checks the source of a product before it is created or edited
     * <p>the source has to exist, has to be a harvest item and can't be the product itself
     *
     * @param madeFrom the id of the source product, null for a harvest item (always fine)
     * @param selfId the id of the product being edited, null for a product that is being created
     * @return null if everything is fine, or an error message
     * @since 1.4
     */
    private String checkMadeFrom(Integer madeFrom, Integer selfId) {
        if (madeFrom == null) return null; // a harvest item has no source to check

        if (selfId != null && madeFrom.equals(selfId)) return "A product can't be made from itself";

        Product source = database.getProductById(madeFrom);
        if (source == null) return "The source product no longer exists";
        if (source.isMade()) return "A product can only be made from a harvest item";

        return null;
    }

    // ---------- stock moves ----------

    /**
     * adds new stock to the inventory, the database has no stock limit so nothing is taken from it
     * <p>only the inventory file is saved
     *
     * @param product the product to add, looked up in the database by its id
     * @param quantity how many to add, at least 1
     * @return null if the stock was added, or an error message
     * @since 1.2
     */
    public String addToInventory(Product product, int quantity) {
        if (product == null) return "No product selected";
        if (quantity < 1) return "Quantity must be at least 1";

        // use the database copy so the inventory always gets the real product details
        Product source = database.getProductById(product.getId());
        if (source == null) return "This product no longer exists";

        inventoryData.add(source, quantity);
        ProductsFileHandler.saveAll(null, inventoryData, null); // only the inventory file is written
        return null;
    }

    /**
     * throws away stock from the inventory, nothing goes to the shelf
     * <p>only the inventory file is saved
     *
     * @param product the product to remove stock from, looked up in the inventory by its id
     * @param quantity how many to remove, at least 1 and at most the inventory stock
     * @return null if the stock was removed, or an error message
     * @since 1.3
     */
    public String removeFromInventory(Product product, int quantity) {
        if (product == null) return "No product selected";
        if (quantity < 1) return "Quantity must be at least 1";

        Product source = inventoryData.getProductById(product.getId());
        if (source == null) return "This product is not in the inventory";
        if (quantity > source.getStock()) return "Only " + source.getStock() + " left in the inventory";

        inventoryData.remove(source, quantity);
        ProductsFileHandler.saveAll(null, inventoryData, null); // only the inventory file is written
        return null;
    }

    /**
     * moves stock from the inventory to the shelf
     *
     * @param product the product to move, looked up in the inventory by its id
     * @param quantity how many to move, at least 1 and at most the inventory stock
     * @return null if the stock was moved, or an error message
     * @since 1.2
     */
    public String addToShelf(Product product, int quantity) {
        return transfer(inventoryData, shelfData, product, quantity, "inventory");
    }

    /**
     * moves stock from the shelf back to the inventory
     *
     * @param product the product to move, looked up on the shelf by its id
     * @param quantity how many to move, at least 1 and at most the shelf stock
     * @return null if the stock was moved, or an error message
     * @since 1.2
     */
    public String moveToInventory(Product product, int quantity) {
        return transfer(shelfData, inventoryData, product, quantity, "shelf");
    }

    /**
     * deletes a product from the database, the inventory and the shelf
     * <p>any product that was made from the deleted one loses its madeFrom,
     * so no saved id points to a product that no longer exists
     * <p>old transactions are not touched because they keep their own copy of the name and price
     * <p>all three product files are saved
     *
     * @param product the product to delete, looked up in the database by its id
     * @return null if it was deleted, or an error message
     * @since 1.2
     */
    public String deleteEverywhere(Product product) {
        if (product == null) return "No product selected";

        Product target = database.getProductById(product.getId());
        if (target == null) return "This product no longer exists";

        int id = target.getId();
        ProductContainer[] containers = { database, inventoryData, shelfData };

        for (ProductContainer container : containers) {
            Product copy = container.getProductById(id);
            // removed from the list directly, so it also works for the database where stock is always 0
            if (copy != null) container.getProducts().remove(copy);

            // products made from the deleted one become harvest items
            for (Product other : container.getProducts()) {
                if (other.getMadeFrom() != null && other.getMadeFrom() == id) {
                    other.setMadeFrom(null);
                }
            }
        }

        ProductsFileHandler.saveAll(database, inventoryData, shelfData);
        return null;
    }

    /**
     * takes stock out of one container and puts it in another, then saves both files
     * <p>the target gets its copy first, the source product is still valid afterwards
     * <p>the source container removes the product by itself when its stock reaches 0
     *
     * @param from the container the stock is taken from
     * @param to the container that receives the stock
     * @param product the product to move, looked up in the source by its id
     * @param quantity how many to move
     * @param fromName the name of the source container, used in error messages
     * @return null if the stock was moved, or an error message
     */
    private String transfer(ProductContainer from, ProductContainer to, Product product, int quantity, String fromName) {
        if (product == null) return "No product selected";
        if (quantity < 1) return "Quantity must be at least 1";

        Product source = from.getProductById(product.getId());
        if (source == null) return "This product is not in the " + fromName;
        if (quantity > source.getStock()) return "Only " + source.getStock() + " left in the " + fromName;

        to.add(source, quantity);
        from.remove(source, quantity);

        ProductsFileHandler.saveAll(null, inventoryData, shelfData); // the database file did not change
        return null;
    }

    // ---------- sales ----------

    /**
     * a customer asks to buy a product, a request never changes stock
     *
     * @param product the product the customer wants
     * @param quantity how many the customer wants, at least 1
     * @param customer the username of the customer
     * @return null if the request was saved, or an error message
     */
    public String requestToBuy(Product product, int quantity, String customer) {
        Product shelfProduct = shelfData.getProductById(product.getId());

        if (shelfProduct == null) return "This product is not on the shelf";
        if (quantity < 1) return "Quantity must be at least 1";
        if (quantity > shelfProduct.getStock()) return "Only " + shelfProduct.getStock() + " left on the shelf";

        transactionData.add(new SaleTransaction(shelfProduct, quantity, customer));
        TransactionFilesHandler.save(transactionData);
        return null;
    }

    /**
     * staff confirms a request, this is when the stock leaves the shelf
     * <p>the stock is checked again because it may have changed since the request was made
     * <p>only the shelf file is saved, so the other product files stay untouched
     *
     * @param transaction the request to confirm
     * @param staff the username of the clerk or manager
     * @return null if it was confirmed, or an error message
     */
    public String confirm(SaleTransaction transaction, String staff) {
        if (!transaction.canConfirm()) return "This request was already confirmed or voided";

        // check the stock again, it may have changed since the request was made
        Product shelfProduct = shelfData.getProductById(transaction.getProductId());
        if (shelfProduct == null || shelfProduct.getStock() < transaction.getQuantity()) {
            return "Not enough stock on the shelf";
        }

        shelfData.remove(shelfProduct, transaction.getQuantity());
        transaction.confirm(staff);
        TransactionFilesHandler.save(transactionData);
        ProductsFileHandler.saveAll(null, null, shelfData); // only the shelf file is written
        return null;
    }

    /**
     * staff voids a request or a confirmed sale
     * <p>a confirmed sale gets its stock back on the shelf, if the product still exists
     *
     * @param transaction the transaction to void
     * @param staff the username of the clerk or manager
     * @return null if it was voided, or an error message
     */
    public String voidTransaction(SaleTransaction transaction, String staff) {
        if (!transaction.canVoid()) return "This transaction was already voided";

        if (transaction.wasConfirmed()) { // stock left the shelf before, so return it
            Product product = database.getProductById(transaction.getProductId());

            if (product != null) {
                shelfData.add(product, transaction.getQuantity());
            } else { // product was deleted, nothing to return the stock to
                System.out.println("Product no longer exists, stock was not returned");
            }
        }

        transaction.voidTransaction(staff);
        TransactionFilesHandler.save(transactionData);
        ProductsFileHandler.saveAll(null, null, shelfData); // only the shelf file is written
        return null;
    }
}