package ui;

import application.FarmSystem;
import application.ProductContainer;
import application.TransactionContainer;
import application.User;
import javafx.stage.Stage;

public class SceneManager {

    private Stage stage;

    private ProductContainer database;
    private ProductContainer inventoryData;
    private ProductContainer shelfData;
    private TransactionContainer transactionData;

    private FarmSystem farmSystem;

    private User currentUser;

    private ShelfScene shelfScene;

    public SceneManager(
            Stage stage,
            ProductContainer database,
            ProductContainer inventoryData,
            ProductContainer shelfData,
            TransactionContainer transactionData,
            User currentUser) {

        this.stage = stage;
        this.database = database;
        this.inventoryData = inventoryData;
        this.shelfData = shelfData;
        this.transactionData = transactionData; // must be set before the shelf scene is created
        this.currentUser = currentUser;

        // one shared farm system for all the sales actions
        this.farmSystem = new FarmSystem(database, shelfData, transactionData);

        // creates the shelf scene
        shelfScene = new ShelfScene(
            stage,
            this,
            database,
            inventoryData,
            shelfData,
            currentUser
        );
    }

    // lets scenes read and save the transactions
    public TransactionContainer getTransactions() {
        return transactionData;
    }

    // lets scenes call the sales actions
    public FarmSystem getFarmSystem() {
        return farmSystem;
    }

    // creates the shared header
    public AppHeader createHeader(String currentScene) {
        return new AppHeader(this, currentScene, currentUser);
    }

    // opens the shelf
    public void showShelf() {
        shelfScene.refreshShelf();
        stage.setScene(shelfScene);
        stage.setTitle("The Shelf");
    }

    // opens the inventory
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

    // opens the database
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

    // opens the customer's own requests, only customers can open it
    public void showMyRequests() {

        if (!currentUser.getUserType().equals(User.CUSTOMER)) return;

        MyRequestsScene scene = new MyRequestsScene(stage, this, currentUser);

        stage.setScene(scene);
        stage.setTitle("My Requests");
    }

    // opens all transactions, only clerks and managers can open it
    public void showTransactions() {

        if (!isStaff()) return;

        TransactionsScene scene = new TransactionsScene(stage, this, currentUser);

        stage.setScene(scene);
        stage.setTitle("Transactions");
    }

    // opens the cash balance, only clerks and managers can open it
    public void showCash() {

        if (!isStaff()) return;

        CashScene scene = new CashScene(stage, this);

        stage.setScene(scene);
        stage.setTitle("Cash");
    }

    // true for clerks and managers
    private boolean isStaff() {
        return currentUser.getUserType().equals(User.MANAGER) ||
               currentUser.getUserType().equals(User.CLERK);
    }
}