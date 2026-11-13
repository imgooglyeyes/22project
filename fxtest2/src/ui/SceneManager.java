package ui;

import application.ProductContainer;
import application.User;
import javafx.stage.Stage;

public class SceneManager {

    private Stage stage;

    private ProductContainer database;
    private ProductContainer inventoryData;
    private ProductContainer shelfData;

    private User currentUser;

    private ShelfScene shelfScene;

    public SceneManager(
            Stage stage,
            ProductContainer database,
            ProductContainer inventoryData,
            ProductContainer shelfData,
            User currentUser) {

        this.stage = stage;
        this.database = database;
        this.inventoryData = inventoryData;
        this.shelfData = shelfData;
        this.currentUser = currentUser;

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

    // displays the shelf
    public void showShelf() {
        stage.setScene(shelfScene);
        stage.setTitle("The Shelf");
    }

    // displays the inventory
    public void showInventory() {

        InventoryScene1 inventoryScene = new InventoryScene1(
                stage,
                database,
                inventoryData,
                shelfData,
                shelfScene,
                currentUser
        );

        stage.setScene(inventoryScene);
        stage.setTitle("Inventory");
    }

    // displays the database
    public void showDatabase() {

        DatabaseScene1 databaseScene = new DatabaseScene1(
                stage,
                database,
                inventoryData,
                shelfData,
                shelfScene,
                currentUser
        );

        stage.setScene(databaseScene);
        stage.setTitle("Database");
    }

    // creates the header for the current scene
    public AppHeader createHeader(String currentScene) {
        return new AppHeader(this, currentScene);
    }
}