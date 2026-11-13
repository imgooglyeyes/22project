package ui;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import application.Product;
import application.ProductContainer;
import application.User;
import backend.AddProductWindow;
import backend.MoveProductWindow;
import backend.ProductCreationWindow;
import backend.ProductEditingWindow;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class ShelfScene extends Scene{
	// ======= TEMPLATE ========
	
	@SuppressWarnings({ "deprecation", "unchecked" })
	public ShelfScene(Stage stage, ProductContainer database, ProductContainer inventoryData, ProductContainer shelfData, User currentUser){
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
	        
	    table.setItems(shelfData.getObservableList());
	        
	    BorderPane root = new BorderPane(table);
			
		// ===== END OF TEMPLATE ====
		
	    HBox moreButtons = new HBox(10);
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
	 				InventoryScene inventoryScene = new InventoryScene(stage, database, inventoryData, shelfData, currentUser);
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
 					DatabaseScene databaseScene = new DatabaseScene(stage, database, inventoryData, shelfData, currentUser);
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
// 					dataHandler.quickSave(stage, tableData);
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
// 					TestLoginWindow loginWindow = new TestLoginWindow(stage, database);
//					loginWindow.show();
 			}});
 			
 			moreButtons.getChildren().add(login);
 		}
 		
 		
 		moreButtons.setStyle("-fx-padding: 10;"); // this gives a nicer bit of space around the hbox
 		root.setTop(moreButtons);
 		root.setStyle("-fx-padding: 12;");
 		//ImportExportHandler dataHandler = new ImportExportHandler(); // will be reused soon. currently not used anywhere in the code.
		super(root, 900, 500);
	}
}
