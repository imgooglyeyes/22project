package application;

/**
 * holds the business rules for sales, screens call these instead of changing stock themselves
 *
 * <p>each method returns null when it worked, or a short error message when it didn't.
 * role checks are not done here, the screens decide who sees which button.
 * stock moves (add to shelf, move to inventory) will be added here in phase 1
 *
 * @author Shawn Alfred Padilla
 * @version 1.1
 * @see SaleTransaction
 * @see ProductsFileHandler
 */
public class FarmSystem {

    /** every product, used to find a product that was sold */
    private ProductContainer database;
    /** the products on the shelf, sales take stock from here */
    private ProductContainer shelfData;
    /** every sale transaction */
    private TransactionContainer transactionData;

    /**
     * creates the farm system
     *
     * @param database every product and its details
     * @param shelfData the products on the shelf
     * @param transactionData every sale transaction
     */
    public FarmSystem(ProductContainer database, ProductContainer shelfData, TransactionContainer transactionData) {
        this.database = database;
        this.shelfData = shelfData;
        this.transactionData = transactionData;
    }

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