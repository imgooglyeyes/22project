package application;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ui.TestLoginWindow;
import ui.AddProductWindow;
import ui.DatabaseScene1;
import ui.InventoryScene1;
import ui.MoveProductWindow;
import ui.ProductCreationWindow;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Main extends Application {

	@SuppressWarnings({ "deprecation", "unchecked" }) // these warnings were part of the template, let's just ignore them
	@Override
	public void start(Stage stage) {
		System.out.println("test2");
		// ======= TEMPLATE ========
		TableView<Product> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Product, String> colName		= new TableColumn<>("Name");
        TableColumn<Product, String> colDesc		= new TableColumn<>("Description");
        TableColumn<Product, String> colPrice		= new TableColumn<>("Price");
        TableColumn<Product, String> colStock		= new TableColumn<>("Stock");

        colName.setCellValueFactory(new PropertyValueFactory<>("name")); // I have no idea how these work but they seem to be calling getter functions somehow
        colDesc.setCellValueFactory(new PropertyValueFactory<>("description"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));

        table.getColumns().addAll( // add these columns to the table
            colName, colDesc, colPrice, colStock
        );

        ArrayList<Product> hardcodedData = new ArrayList<>();
        hardcodedData.add(new Product("Mature Coconut",  "Whole mature coconut, fresh from the tree.",  30.0, 0));
        hardcodedData.add(new Product("Virgin Coconut Oil (250ml)",  "Cold-pressed, unrefined oil for cooking, baking and skin and hair care.",  230.0, 0));
        hardcodedData.add(new Product("Virgin Coconut Oil (500ml)",  "Cold-pressed, unrefined oil for cooking, baking and skin and hair care..",  365.0, 0));
        hardcodedData.add(new Product("Coconut Milk (400ml)",  "Unsweetened milk pressed from ripe coconut meat, for sweet and savory dishes.",  68.0, 0));
        hardcodedData.add(new Product("Coconut Cream (250ml)",  "Thick first-press cream for desserts and rich sauces.",  65.0, 0));
        hardcodedData.add(new Product("Desiccated Coconut (250g)",  "Dried, finely shredded coconut meat for baking and desserts.",  85.0, 0));
        hardcodedData.add(new Product("Young Coconut",  "Fresh whole young coconut for drinking and scraping the soft meat.",  35.0, 0));
        hardcodedData.add(new Product("Fresh Coconut Sap (500ml)",  "Unfermented sap tapped from the coconut flower stalk.",  80.0, 0));
        hardcodedData.add(new Product("Coconut Sugar (250g)",  "Unrefined sugar made by boiling down coconut sap.",  120.0, 0));
        hardcodedData.add(new Product("Coconut Sap Vinegar (500ml)",  "Traditional vinegar fermented from coconut sap.",  75.0, 0));
        hardcodedData.add(new Product("Palay (25kg)",  "Freshly threshed rice grain with the husk on.",  450.0, 0));
        hardcodedData.add(new Product("Well-Milled White Rice (5kg)",  "Everyday polished white rice.",  280.0, 0));
        hardcodedData.add(new Product("Brown Rice (1kg)",  "Whole-grain rice with the bran layer kept.",  85.0, 0));
        hardcodedData.add(new Product("Saba Banana (1kg)",  "Cooking banana for turon, banana cue and nilaga.",  45.0, 0));
        hardcodedData.add(new Product("Banana Chips (200g)",  "Crisp fried saba banana slices.",  70.0, 0));
        hardcodedData.add(new Product("Carabao Mango (1kg)",  "Fresh ripe sweet mangoes.",  120.0, 0));
        hardcodedData.add(new Product("Dried Mango (100g)",  "Soft sun-dried mango slices.",  95.0, 0));
        hardcodedData.add(new Product("Calamansi (500g)",  "Fresh calamansi for juice, marinades and dipping sauces.",  60.0, 0));
        hardcodedData.add(new Product("Calamansi Juice Concentrate (250ml)",  "Concentrated juice to dilute and drink.",  110.0, 0));
        hardcodedData.add(new Product("Dried Cacao Beans (500g)",  "Fermented, sun-dried cacao beans.",  160.0, 0));
        hardcodedData.add(new Product("Cacao Tablea (250g)",  "Pressed roasted cacao tablets for tsokolate.",  180.0, 0));
        hardcodedData.add(new Product("Green Coffee Beans (Barako) (500g)",  "Dried, unroasted liberica coffee beans.",  240.0, 0));
        hardcodedData.add(new Product("Barako Coffee Beans (Roasted) (250g)",  "Roasted whole beans of strong Philippine liberica coffee.",  220.0, 0));
        hardcodedData.add(new Product("Sugarcane Stalks (1 bundle)",  "Freshly cut sugarcane.",  60.0, 0));
        hardcodedData.add(new Product("Muscovado Sugar (500g)",  "Unrefined brown sugar from sugarcane juice.",  85.0, 0));
        hardcodedData.add(new Product("Ube (1kg)",  "Fresh purple yam roots.",  110.0, 0));
        hardcodedData.add(new Product("Ube Halaya (350g)",  "Sweet purple yam jam, sold in a jar.",  140.0, 0));
        hardcodedData.add(new Product("Secret Sauce",  "This sauce is a secret.",  100.0, 0));
        
        ProductContainer database = new ProductContainer(hardcodedData); // universal set of existing products
        ProductContainer inventoryData = new ProductContainer(); // products not on sale / products in storage (must exist in database)
        
        
        inventoryData.add(database.getProductById(0), 20);
        inventoryData.add(database.getProductById(1), 20);
        inventoryData.add(database.getProductById(2), 20);
        inventoryData.add(database.getProductById(3), 20);
        inventoryData.add(database.getProductById(4), 20);
        inventoryData.add(database.getProductById(5), 20);
        
        ProductContainer shelfData = new ProductContainer(); // products on sale (must exist in inventory)
        
        shelfData.add(inventoryData.getProductById(0), 5);
        shelfData.add(inventoryData.getProductById(1), 5);
        shelfData.add(inventoryData.getProductById(2), 5);
        
        table.setItems(shelfData.getObservableList());
        
        User currentUser = new User("MainManager", "password", "Billy Bob", "Jones", User.MANAGER);
        
		BorderPane root = new BorderPane(table);
		
		// ===== END OF TEMPLATE ====
		
		HBox moreButtons = new HBox(10);
		root.setTop(moreButtons);
		root.setStyle("-fx-padding: 12;");
		Scene mainScene = new Scene(root, 900, 500);
		stage.setScene(mainScene);
		stage.setTitle("The Shelf");
		stage.show();

		
		// add product and view inventory button
		if (currentUser.getUserType().equals(User.MANAGER) || currentUser.getUserType().equals(User.CLERK)) {
			Button addProduct = new Button("Add Product From Inventory"); // currently adds from the database while the inventory is not yet implemented
			
			addProduct.setOnMouseClicked(new EventHandler<MouseEvent>(){
				public void handle (MouseEvent event) {
					// temporarily uses database instead of inventoryData, inventory is not yet implemented
					AddProductWindow addWindow = new AddProductWindow(stage, table, shelfData, inventoryData, true); // a window that lets the user add something to the shelfData based on what's in inventoryData with a stock limit (true)
					addWindow.show();
			}});
			
			Button viewInventory = new Button("View Inventory");
			
			viewInventory.setOnMouseClicked(new EventHandler<MouseEvent>(){
				public void handle (MouseEvent event) {
					InventoryScene1 inventoryScene = new InventoryScene1(stage, database, inventoryData, shelfData, mainScene, currentUser);
					stage.setScene(inventoryScene);
					stage.setTitle("Inventory");
			}});
			
			moreButtons.getChildren().addAll(addProduct, viewInventory);
		}

		// view database button
		if (currentUser.getUserType().equals(User.MANAGER)) {
			Button viewDatabase = new Button("View Database");
			
			viewDatabase.setOnMouseClicked(new EventHandler<MouseEvent>(){
				public void handle (MouseEvent event) {
					// temporarily uses database instead of inventoryData, inventory is not yet implemented
					DatabaseScene1 databaseScene = new DatabaseScene1(stage, database, inventoryData, shelfData, mainScene, currentUser);
					stage.setScene(databaseScene);
					stage.setTitle("Database");
			}});
			
			moreButtons.getChildren().add(viewDatabase);
		}
		
		// delete (to be changed), manual save (to be changed), and quit button
		if (currentUser.getUserType().equals(User.MANAGER) || currentUser.getUserType().equals(User.CLERK)) {
			Button removeStocks = new Button("Move Stocks To Inventory");
			
			BooleanBinding noItemSelected = Bindings.createBooleanBinding(() -> { // a boolean binding to check if the table has anything selected or not
		        if (table.getSelectionModel().getSelectedItem() == null) return true;
		        else return false;
		    }, table.getSelectionModel().selectedItemProperty());
			
			removeStocks.disableProperty().bind(noItemSelected);
			
			removeStocks.setOnMouseClicked(new EventHandler<MouseEvent>(){ // will be changed to affect stocks
				public void handle (MouseEvent event) {
					MoveProductWindow moveWindow = new MoveProductWindow(stage, table, shelfData, inventoryData, table.getSelectionModel().getSelectedItem());
					// note: inventoryData is selected as the storage parameter for MoveProductWindow. This means, products removed from shelfData will be moved there.
					moveWindow.show();
			}});
			
			Button exportBtn = new Button("Manual Save");
			
			exportBtn.setOnMouseClicked(new EventHandler<MouseEvent>(){
				public void handle (MouseEvent event) {
					// THESE WILL BE MOVED TO A NEW CLASS ONCE I ADD FUNCTIONALITY FOR LOADING THEM
					
					// this is saving the universal set of products
					Path dbSavePath = Paths.get("src/data/productsDB.txt");
					System.out.println("Saving database...");
					try {
						BufferedWriter writer = Files.newBufferedWriter(dbSavePath);
						for(Product p : database.getObservableList()) { // go through every product in the database (not to be confused by the data in the table)
							writer.write(p.getId() + "|" + p.getName() + "|" + p.getDescription() + "|" + p.getPrice());
							writer.newLine();
						}
						// data is formatted by id|name|description|price
						System.out.println("Saving universal database.");
						writer.close();
					} catch (IOException e) {
						System.out.println("Something went wrong.");
						e.printStackTrace();
					}
					
					// this is saving the products on sale
					Path shelfSavePath = Paths.get("src/data/shelf.txt");
					System.out.println("Saving marketplace data...");
					try {
						BufferedWriter writer = Files.newBufferedWriter(shelfSavePath);
						for(Product p : shelfData.getObservableList()) { // go through every product in the table
							writer.write(p.getId() + "|" + p.getStock());
							writer.newLine();
						}
						// data is formatted by id|stock
						System.out.println("Saving complete.");
						writer.close();
					} catch (IOException e) {
						System.out.println("Something went wrong.");
						e.printStackTrace();
					}
					
					// this is saving the products not on sale
					Path inventorySavePath = Paths.get("src/data/inventory.txt");
					System.out.println("Saving inventory data...");
					try {
						BufferedWriter writer = Files.newBufferedWriter(inventorySavePath);
						for(Product p : inventoryData.getObservableList()) { // go through every product in the table
							writer.write(p.getId() + "|" + p.getStock());
							writer.newLine();
						}
						// data is formatted by id|stock
						System.out.println("Saving complete.");
						writer.close();
					} catch (IOException e) {
						System.out.println("Something went wrong.");
						e.printStackTrace();
					}
				}
			});
			
			Button quit = new Button("Quit");
			
			quit.setOnMouseClicked(new EventHandler<MouseEvent>(){
				public void handle (MouseEvent event) {
//					dataHandler.quickSave(stage, tableData);
					Platform.exit();
				}
			});
			
			moreButtons.getChildren().addAll(removeStocks, exportBtn, quit);
		}
		
		
		// log in button (to be implemented)
		if (currentUser.getUserType().equals(User.GUEST)) {
			Button login = new Button("Login"); //temp, does not work at all yet
			
			login.setOnMouseClicked(new EventHandler<MouseEvent>(){
				public void handle (MouseEvent event) {
					System.out.println("This button isn't working yet. There still needs to be a user database. TestLoginWindow is a leftover from my last exercise.");
//					TestLoginWindow loginWindow = new TestLoginWindow(stage, database);
//					loginWindow.show();
			}});
			
			moreButtons.getChildren().add(login);
		}
		
		
		moreButtons.setStyle("-fx-padding: 10;"); // this gives a nicer bit of space around the hbox
		//ImportExportHandler dataHandler = new ImportExportHandler(); // will be reused soon. currently not used anywhere in the code.
	}

	public static void main(String[] args) { 
		launch(); 
	}

}

