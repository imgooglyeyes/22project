package application;

// holds the business rules for sales, screens call these instead of changing stock themselves
// each method returns null when it worked, or a short error message when it didn't
// role checks are not done here, the screens decide who sees which button
// stock moves (add to shelf, move to inventory) will be added here in phase 1

public class FarmSystem {

    private ProductContainer database;
    private ProductContainer shelfData;
    private TransactionContainer transactionData;

    public FarmSystem(ProductContainer database, ProductContainer shelfData, TransactionContainer transactionData) {
        this.database = database;
        this.shelfData = shelfData;
        this.transactionData = transactionData;
    }

    // a customer asks to buy a product, a request never changes stock
    public String requestToBuy(Product product, int quantity, String customer) {
        Product shelfProduct = shelfData.getProductById(product.getId());

        if (shelfProduct == null) return "This product is not on the shelf";
        if (quantity < 1) return "Quantity must be at least 1";
        if (quantity > shelfProduct.getStock()) return "Only " + shelfProduct.getStock() + " left on the shelf";

        transactionData.add(new SaleTransaction(shelfProduct, quantity, customer));
        TransactionStore.save(transactionData);
        return null;
    }

    // staff confirms a request, this is when the stock leaves the shelf
    public String confirm(SaleTransaction transaction, String staff) {
        if (!transaction.canConfirm()) return "This request was already confirmed or voided";

        // check the stock again, it may have changed since the request was made
        Product shelfProduct = shelfData.getProductById(transaction.getProductId());
        if (shelfProduct == null || shelfProduct.getStock() < transaction.getQuantity()) {
            return "Not enough stock on the shelf";
        }

        shelfData.remove(shelfProduct, transaction.getQuantity());
        transaction.confirm(staff);
        TransactionStore.save(transactionData);
        return null;
    }

    // staff voids a request or a confirmed sale
    // a confirmed sale gets its stock back on the shelf
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
        TransactionStore.save(transactionData);
        return null;
    }
}