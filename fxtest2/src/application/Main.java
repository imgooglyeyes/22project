package application;

import javafx.application.Application;
import javafx.stage.Stage;

import ui.Fonts;
import ui.SceneManager;

/**
 * starts the shopeefarm app
 *
 * <p>it loads the fonts, the products and the transactions from src/data (missing files fall back
 * to src/templatedata), creates the scene manager and opens the home page
 *
 * @author Jian Carranza, Shawn Alfred Padilla
 * @version 1.4
 * @since 1.2 the fonts are loaded before the first scene
 * @since 1.3 the app opens on the home page instead of the shelf
 * @see Fonts
 * @see ProductsFileHandler
 * @see SceneManager
 */
public class Main extends Application {

    /**
     * loads the fonts and the data, creates the scene manager and shows the home page
     *
     * @param stage the main window given by javafx
     */
    @Override
    public void start(Stage stage) {
        // the fonts must be loaded before the first scene so css can use them
        // a missing font only prints a message, the css fallback font is used
        Fonts.load();

        // creates the product containers, missing files fall back to src/templatedata
    	ProductContainer[] containers = ProductsFileHandler.loadAll();
        ProductContainer database = containers[0];
        ProductContainer inventoryData = containers[1];
        ProductContainer shelfData = containers[2];

        // loads the saved transactions (empty if the file doesn't exist yet)
        TransactionContainer transactionData = TransactionFilesHandler.load();
       
        // loads the saved user data
        UserContainer userData = UsersFileHandler.load();
        
        // always start as a guest
        User currentUser = new User(
        		"guest",
        		"",
        		"guest",
        		"",
        		User.GUEST
        	);
        
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

        // opens the home page, a guest sees the welcome and the trending list first
        manager.showHome();

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