
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

        InventoryScene1 scene = new InventoryScene1(
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

        DatabaseScene1 scene = new DatabaseScene1(
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
}
