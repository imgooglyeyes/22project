
package application;

import javafx.application.Application;
import javafx.stage.Stage;

import ui.SceneManager;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        // creates the product containers
    	ProductContainer[] containers = ProductFilesHandler.loadAll();
        ProductContainer database = containers[0];
        ProductContainer inventoryData = containers[1];
        ProductContainer shelfData = containers[2];

        // loads the saved transactions (empty if the file doesn't exist yet)
        TransactionContainer transactionData = TransactionFilesHandler.load();

        // temporary for testing
        User currentUser = new User(
            "customer1",
            "password",
            "Billy Bob",
            "Jones",
            User.MANAGER
        );

        // creates the scene manager
        SceneManager manager = new SceneManager(
            stage,
            database,
            inventoryData,
            shelfData,
            transactionData,
            currentUser
        );

        // opens the shelf scene
        manager.showShelf();

        // displays the application window
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}