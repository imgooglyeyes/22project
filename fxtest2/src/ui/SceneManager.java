package ui;

import application.FarmSystem;
import application.ProductContainer;
import application.ProductsFileHandler;
import application.TransactionContainer;
import application.User;
import application.UserContainer;
import javafx.stage.Stage;

/**
 * creates and opens every scene, and holds the shared containers
 *
 * <p>scenes ask it for the header, the transactions and the farm system.
 * it also saves the product containers by hand and resets them to the template data
 *
 * @author Jian Carranza, Shawn Alfred Padilla
 * @version 1.4
 * @see FarmSystem
 * @see ProductsFileHandler
 */
public class SceneManager {

    private Stage stage;

    private ProductContainer database;
    private ProductContainer inventoryData;
    private ProductContainer shelfData;
    private TransactionContainer transactionData;
    private UserContainer userData;

    private FarmSystem farmSystem;

    private User currentUser;

    /**
     * creates the scene manager and its shared data
     *
     * @param stage the main window
     * @param database every product and its details
     * @param inventoryData the products and stock in the inventory
     * @param shelfData the products and stock on the shelf
     * @param transactionData every sale transaction
     * @param userData every user and their details
     * @param currentUser the user who is logged in
     */
    public SceneManager(
            Stage stage,
            ProductContainer database,
            ProductContainer inventoryData,
            ProductContainer shelfData,
            TransactionContainer transactionData,
            UserContainer userData,
            User currentUser) {

        this.stage = stage;
        this.database = database;
        this.inventoryData = inventoryData;
        this.shelfData = shelfData;
        this.transactionData = transactionData;
        this.userData = userData;
        this.currentUser = currentUser;

        // the farm system gets the same container objects, so it and the scenes always see the same data
        this.farmSystem = new FarmSystem(database, inventoryData, shelfData, transactionData);
    }

    /** @return every sale transaction, scenes read and save from it */
    public TransactionContainer getTransactions() {
        return transactionData;
    }

    /** @return the shared farm system, scenes and windows call the product, stock and sales actions on it */
    public FarmSystem getFarmSystem() {
        return farmSystem;
    }

    /**
     * saves the database, inventory and shelf
     * <p>used by the manual save button, every other change is saved by FarmSystem
     */
    public void saveProductContainers() {
        ProductsFileHandler.saveAll(database, inventoryData, shelfData);
    }

    /**
     * puts the database, inventory and shelf back to the template data
     * <p>the files are copied first, then loaded again into the same container objects
     * <p>only managers can do this, transactions are not touched
     *
     * @return true if the reset worked, false if the user is not a manager or a file failed
     * @since 1.2
     */
    public boolean resetToDefaults() {
        if (!currentUser.getUserType().equals(User.MANAGER)) return false;

        if (!ProductsFileHandler.resetToDefaults()) return false;

        ProductContainer[] fresh = ProductsFileHandler.loadAll();
        database.replaceWith(fresh[0]);
        inventoryData.replaceWith(fresh[1]);
        shelfData.replaceWith(fresh[2]);

        return true;
    }

    /**
     * creates the shared header
     *
     * @param currentScene the name of the open scene, its button is highlighted
     * @return the header for the current user
     */
    public AppHeader createHeader(String currentScene) {
        return new AppHeader(this, currentScene, currentUser);
    }

    /** opens the shelf */
    public void showShelf() {
        ShelfScene scene = new ShelfScene(
            stage,
            this,
            inventoryData,
            shelfData,
            currentUser
        );

        stage.setScene(scene);
        stage.setTitle("The Shelf");
    }

    /** opens the inventory, doing nothing unless the current user is staff */
    public void showInventory() {
        if (!isStaff()) return;

        InventoryScene scene = new InventoryScene(
            stage,
            database,
            inventoryData,
            this,
            currentUser
        );

        stage.setScene(scene);
        stage.setTitle("Inventory");
    }

    /** opens the database, doing nothing unless the current user is a manager */
    public void showDatabase() {
        if (!currentUser.getUserType().equals(User.MANAGER)) return;

        DatabaseScene scene = new DatabaseScene(
            stage,
            database,
            this,
            currentUser
        );

        stage.setScene(scene);
        stage.setTitle("Database");
    }

    /** opens the login scene */
    public void showLogin() {
        LoginScene scene = new LoginScene(stage, this, userData, currentUser);
        stage.setScene(scene);
        stage.setTitle("Log In");
    }

    /** opens the customer's own requests, doing nothing unless the user is a customer */
    public void showMyRequests() {
        if (!currentUser.getUserType().equals(User.CUSTOMER)) return;

        MyRequestsScene scene = new MyRequestsScene(stage, this, currentUser);
        stage.setScene(scene);
        stage.setTitle("My Requests");
    }

    /** opens all transactions, doing nothing unless the current user is staff */
    public void showTransactions() {
        if (!isStaff()) return;

        TransactionsScene scene = new TransactionsScene(stage, this, currentUser);
        stage.setScene(scene);
        stage.setTitle("Transactions");
    }

    /** opens the target user's transaction history, doing nothing unless the current user is staff */
    public void showHistory(User targetUser) {
        if (!isStaff()) return;

        HistoryScene scene = new HistoryScene(stage, this, currentUser, targetUser);
        stage.setScene(scene);
        stage.setTitle("Transactions History");
    }

    /** opens the staff list, doing nothing unless the current user is a manager */
    public void showStaffList() {
        if (!currentUser.getUserType().equals(User.MANAGER)) return;

        StaffListScene scene = new StaffListScene(stage, this, userData, currentUser);
        stage.setScene(scene);
        stage.setTitle("Staff List");
    }

    /** opens the cash balance, doing nothing unless the current user is staff */
    public void showCash() {
        if (!isStaff()) return;

        CashScene scene = new CashScene(stage, this, currentUser);
        stage.setScene(scene);
        stage.setTitle("Cash");
    }

    /** @return true if the current user is a clerk or a manager */
    private boolean isStaff() {
        return currentUser.getUserType().equals(User.MANAGER) ||
               currentUser.getUserType().equals(User.CLERK);
    }
}
