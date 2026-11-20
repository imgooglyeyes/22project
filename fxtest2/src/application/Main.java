
package application;

import java.util.ArrayList;

import javafx.application.Application;
import javafx.stage.Stage;

import ui.SceneManager;

/**
 * starts the shopeefarm app
 *
 * <p>it loads the products and transactions from src/data (missing files fall back
 * to src/templatedata), creates the scene manager and opens the shelf
 *
 * @author Jian Carranza, Shawn Alfred Padilla
 * @version 1.1
 * @see ProductsFileHandler
 * @see SceneManager
 */
public class Main extends Application {

    /**
     * loads the data, creates the scene manager and shows the shelf
     *
     * @param stage the main window given by javafx
     */
    @Override
    public void start(Stage stage) {

        // creates the product containers, missing files fall back to src/templatedata
    	ProductContainer[] containers = ProductsFileHandler.loadAll();
        ProductContainer database = containers[0];
        ProductContainer inventoryData = containers[1];
        ProductContainer shelfData = containers[2];

        // loads the saved transactions (empty if the file doesn't exist yet)
        TransactionContainer transactionData = TransactionFilesHandler.load();

        // temporary for testing
        ArrayList<User> hardcodedusers = new ArrayList<>();
        hardcodedusers.add(new User(
                "MainManager",
                "maxgalactica",
                "Billy Bob",
                "Jones",
                User.MANAGER
            ));
        hardcodedusers.add(new User(
                "Clerk1",
                "coughup",
                "Angel",
                "Starr",
                User.CLERK
            ));
        hardcodedusers.add(new User(
                "Clerk2",
                "sl9",
                "Jake",
                "Marshall",
                User.CLERK
            ));
        hardcodedusers.add(new User(
                "Customer1",
                "yourhonor",
                "Beard",
                "Gentleman",
                User.CUSTOMER
            ));
        hardcodedusers.add(new User(
                "Customer2",
                "7777777",
                "Damon",
                "Gant",
                User.CUSTOMER
            ));
        hardcodedusers.add(new User(
                "MainGuest",
                "",
                "guest",
                "",
                User.GUEST
            ));
        
        UserContainer userData = new UserContainer(hardcodedusers);
        
        User currentUser = userData.getUserByUsername("MainManager");

        // creates the scene manager
        SceneManager manager = new SceneManager(
            stage,
            database,
            inventoryData,
            shelfData,
            transactionData,
            userData,
            currentUser
        );

        // opens the shelf scene
        manager.showShelf();

        // displays the application window
        stage.show();
    }

    /**
     * launches the javafx app
     *
     * @param args command line arguments, not used
     */
    public static void main(String[] args) {
        launch(args);
    }
}