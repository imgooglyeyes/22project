package application;

/**
 * holds the business rules for sales and stock moves, screens call these instead of changing stock themselves
 *
 * <p>each method returns null when it worked, or a short error message when it didn't.
 * role checks are not done here, the screens decide who sees which button.
 * every method saves only the files it changed, so the other product files stay untouched
 *
 * <p>stock flows one way down the chain: database -> inventory -> shelf.
 * addToInventory and addToShelf move stock forward, moveToInventory moves it back
 *
 * @author Shawn Alfred Padilla
 * @version 1.2
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