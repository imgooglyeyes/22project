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
 * it also saves the product containers and resets them to the template data
 *
 * @author Jian Carranza, Shawn Alfred Padilla
 * @version 1.2
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
     * creates the scene manager and the shelf scene
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
        this.transactionData = transactionData; // must be set before the shelf scene is created
        this.userData = userData;
        this.currentUser = currentUser;

        // one shared farm system for all the sales actions
        this.farmSystem = new FarmSystem(database, shelfData, transactionData);

        showShelf(); // initial screen
    }

    /** @return every sale transaction, scenes read and save from it */
    public TransactionContainer getTransactions() {
        return transactionData;
    }

    /** @return the shared farm system, scenes call the sales actions on it */
    public FarmSystem getFarmSystem() {
        return farmSystem;
    }
    
    /**
     * saves the database, inventory and shelf
     * <p>used by AddProductWindow and MoveProductWindow because they do not know what containers they work with
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
        if (!currentUser.getUserType().equals(User.MANAGER)) return false; // managers only

        if (!ProductsFileHandler.resetToDefaults()) return false; // copying the files failed

        // load the fresh files, then copy them into the containers the scenes already use
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

    /** opens the shelf, a new scene us created each time*/
    public void showShelf() {
    	
        ShelfScene shelfScene = new ShelfScene(
            stage,
            this,
            database,
            inventoryData,
            shelfData,
            currentUser
        );
    	
    	//shelfScene.refreshShelf();
        stage.setScene(shelfScene);
        stage.setTitle("The Shelf");
    }

    /** opens the inventory, a new scene is created each time */
    public void showInventory() {

        InventoryScene scene = new InventoryScene(
            stage,
            database,
            inventoryData,
            shelfData,
            this,
            currentUser
        );

        stage.setScene(scene);
        stage.setTitle("Inventory");
    }

    /** opens the database, a new scene is created each time */
    public void showDatabase() {

        DatabaseScene scene = new DatabaseScene(
            stage,
            database,
            inventoryData,
            shelfData,
            this,
            currentUser
        );

        stage.setScene(scene);
        stage.setTitle("Database");
    }
    
    /** opens the log in, a new scene is created each time */
    public void showLogin() {

        LoginScene scene = new LoginScene(
            stage,
            this,
            userData,
            currentUser
        );

        stage.setScene(scene);
        stage.setTitle("Log In");
    }

    /** opens the customer's own requests, does nothing if the user is not a customer */
    public void showMyRequests() {

        if (!currentUser.getUserType().equals(User.CUSTOMER)) return;

        MyRequestsScene scene = new MyRequestsScene(stage, this, currentUser);

        stage.setScene(scene);
        stage.setTitle("My Requests");
    }

    /** opens all transactions, does nothing if the user is not a clerk or manager */
    public void showTransactions() {

        if (!isStaff()) return;

        TransactionsScene scene = new TransactionsScene(stage, this, currentUser);

        stage.setScene(scene);
        stage.setTitle("Transactions");
    }
    
    /** opens all transactions that the target user has interacted with, does nothing if the user is not a clerk or manager */
    public void showHistory(User targetUser) {

        if (!isStaff()) return;

        HistoryScene scene = new HistoryScene(stage, this, currentUser, targetUser);

        stage.setScene(scene);
        stage.setTitle("Transactions History");
    }
    
    /** opens the staff list, does nothing if the user is not a manager */
    public void showStaffList() {

        if (!currentUser.getUserType().equals(User.MANAGER)) return;

        StaffListScene scene = new StaffListScene(stage, this, userData, currentUser);

        stage.setScene(scene);
        stage.setTitle("Transactions History");
    }

    /** opens the cash balance, does nothing if the user is not a clerk or manager */
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