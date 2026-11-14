
package application;

import java.util.ArrayList;

import javafx.application.Application;
import javafx.stage.Stage;

import ui.SceneManager;

public class Main1 extends Application {

    @Override
    public void start(Stage stage) {

        // creates the initial product database
        ArrayList<Product> hardcodedData = new ArrayList<>();

        hardcodedData.add(new Product("Mature Coconut", "Whole mature coconut, fresh from the tree.", 30.0, 0));
        hardcodedData.add(new Product("Virgin Coconut Oil (250ml)", "Cold-pressed, unrefined oil for cooking, baking and skin and hair care.", 230.0, 0));
        hardcodedData.add(new Product("Virgin Coconut Oil (500ml)", "Cold-pressed, unrefined oil for cooking, baking and skin and hair care..", 365.0, 0));
        hardcodedData.add(new Product("Coconut Milk (400ml)", "Unsweetened milk pressed from ripe coconut meat, for sweet and savory dishes.", 68.0, 0));
        hardcodedData.add(new Product("Coconut Cream (250ml)", "Thick first-press cream for desserts and rich sauces.", 65.0, 0));
        hardcodedData.add(new Product("Desiccated Coconut (250g)", "Dried, finely shredded coconut meat for baking and desserts.", 85.0, 0));
        hardcodedData.add(new Product("Young Coconut", "Fresh whole young coconut for drinking and scraping the soft meat.", 35.0, 0));
        hardcodedData.add(new Product("Fresh Coconut Sap (500ml)", "Unfermented sap tapped from the coconut flower stalk.", 80.0, 0));
        hardcodedData.add(new Product("Coconut Sugar (250g)", "Unrefined sugar made by boiling down coconut sap.", 120.0, 0));
        hardcodedData.add(new Product("Coconut Sap Vinegar (500ml)", "Traditional vinegar fermented from coconut sap.", 75.0, 0));
        hardcodedData.add(new Product("Palay (25kg)", "Freshly threshed rice grain with the husk on.", 450.0, 0));
        hardcodedData.add(new Product("Well-Milled White Rice (5kg)", "Everyday polished white rice.", 280.0, 0));
        hardcodedData.add(new Product("Brown Rice (1kg)", "Whole-grain rice with the bran layer kept.", 85.0, 0));
        hardcodedData.add(new Product("Saba Banana (1kg)", "Cooking banana for turon, banana cue and nilaga.", 45.0, 0));
        hardcodedData.add(new Product("Banana Chips (200g)", "Crisp fried saba banana slices.", 70.0, 0));
        hardcodedData.add(new Product("Carabao Mango (1kg)", "Fresh ripe sweet mangoes.", 120.0, 0));
        hardcodedData.add(new Product("Dried Mango (100g)", "Soft sun-dried mango slices.", 95.0, 0));
        hardcodedData.add(new Product("Calamansi (500g)", "Fresh calamansi for juice, marinades and dipping sauces.", 60.0, 0));
        hardcodedData.add(new Product("Calamansi Juice Concentrate (250ml)", "Concentrated juice to dilute and drink.", 110.0, 0));
        hardcodedData.add(new Product("Dried Cacao Beans (500g)", "Fermented, sun-dried cacao beans.", 160.0, 0));
        hardcodedData.add(new Product("Cacao Tablea (250g)", "Pressed roasted cacao tablets for tsokolate.", 180.0, 0));
        hardcodedData.add(new Product("Green Coffee Beans (Barako) (500g)", "Dried, unroasted liberica coffee beans.", 240.0, 0));
        hardcodedData.add(new Product("Barako Coffee Beans (Roasted) (250g)", "Roasted whole beans of strong Philippine liberica coffee.", 220.0, 0));
        hardcodedData.add(new Product("Sugarcane Stalks (1 bundle)", "Freshly cut sugarcane.", 60.0, 0));
        hardcodedData.add(new Product("Muscovado Sugar (500g)", "Unrefined brown sugar from sugarcane juice.", 85.0, 0));
        hardcodedData.add(new Product("Ube (1kg)", "Fresh purple yam roots.", 110.0, 0));
        hardcodedData.add(new Product("Ube Halaya (350g)", "Sweet purple yam jam, sold in a jar.", 140.0, 0));
        hardcodedData.add(new Product("Secret Sauce", "This sauce is a secret.", 100.0, 0));

        // creates the product containers
        ProductContainer database = new ProductContainer(hardcodedData);
        ProductContainer inventoryData = new ProductContainer();
        ProductContainer shelfData = new ProductContainer();

        // adds initial stocks to inventory
        for (int i = 0; i < 6; i++) {
            inventoryData.add(database.getProductById(i), 20);
        }

        // adds initial stocks to the shelf
        for (int i = 0; i < 3; i++) {
            shelfData.add(inventoryData.getProductById(i), 5);
        }

        // creates the current user
        User currentUser = new User(
            "MainManager",
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
