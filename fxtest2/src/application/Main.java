package application;

import javafx.application.Application;
import javafx.application.Platform;
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
import ui.DatabaseScene;
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
        hardcodedData.add(new Product("Corn",  "A staple food in many regions of the world. Can be used for starch, syrup, oil, and more.",  25.0, 20));
        hardcodedData.add(new Product("Coconut", "From the tree of life, this fruit comes with multiple culinary and nonculinary use.", 40.0, 15));
        
        ObservableList<Product> shelfData = FXCollections.observableArrayList(hardcodedData); // products on sale (must exist in inventory)
        ObservableList<Product> inventoryData = FXCollections.observableArrayList(hardcodedData); // products not on sale (must exist in database)
       
        // Currently not implemented:
        //ObservableList<Product> inventoryData = FXCollections.observableArrayList(hardcodedData); // products in storage (must exist in database)
        
        hardcodedData.add(new Product("1kg Rice",  "Perfect alongside any meal. It can even become a meal itself.",  100.0, 20));
        
        ObservableList<Product> database = FXCollections.observableArrayList(hardcodedData); // universal set of existing products
        table.setItems(shelfData);
        
		BorderPane root = new BorderPane(table);
		
		// ===== END OF TEMPLATE ====
		
		Button addProduct = new Button("Add Product From Inventory"); // currently adds from the database while the inventory is not yet implemented
		Button viewDatabase = new Button("View Database");
		Button deleteSelected = new Button("Delete Selected"); // temp
		Button exportBtn = new Button("Manual Save");
		Button quit = new Button("Quit");
		Button login = new Button("Login");
		HBox moreButtons = new HBox(10, viewDatabase, addProduct, deleteSelected, exportBtn, quit, login); // creates an hbox with a 10px spacing and these 4 buttons as children
		moreButtons.setStyle("-fx-padding: 10;"); // this gives a nicer bit of space around the hbox
		//ImportExportHandler dataHandler = new ImportExportHandler(); // will be reused soon. currently not used anywhere in the code.
		
		root.setTop(moreButtons);
		root.setStyle("-fx-padding: 12;");
		Scene mainScene = new Scene(root, 900, 500);
		stage.setScene(mainScene);
		stage.setTitle("proof of concept");
		stage.show();

		
		addProduct.setOnMouseClicked(new EventHandler<MouseEvent>(){
			public void handle (MouseEvent event) {
				// temporarily uses database instead of inventoryData, inventory is not yet implemented
				AddProductWindow addWindow = new AddProductWindow(stage, table, shelfData, database); // I had to make it a new class to shorten the code, also more modular that way
				addWindow.show();
		}});
		
		viewDatabase.setOnMouseClicked(new EventHandler<MouseEvent>(){
			public void handle (MouseEvent event) {
				// temporarily uses database instead of inventoryData, inventory is not yet implemented
				DatabaseScene databaseScene = new DatabaseScene(stage, database, mainScene);
				stage.setScene(databaseScene);
				stage.setTitle("Database");
		}});
		
		login.setOnMouseClicked(new EventHandler<MouseEvent>(){
			public void handle (MouseEvent event) {
				System.out.println("This button isn't working yet. There still needs to be a user database. TestLoginWindow is a leftover from my last exercise.");
//				TestLoginWindow loginWindow = new TestLoginWindow(stage, database);
//				loginWindow.show();
		}});
		
		deleteSelected.setOnMouseClicked(new EventHandler<MouseEvent>(){ // will be changed to affect stocks
			public void handle (MouseEvent event) {
				shelfData.remove(table.getSelectionModel().getSelectedItem()); // takes the currently selected object (DON'T TAKE THE INDEX, IT'S NOT CONSTANT)
				table.setItems(shelfData); // update items
		}});
		
		
		exportBtn.setOnMouseClicked(new EventHandler<MouseEvent>(){
			public void handle (MouseEvent event) {
				// THESE WILL BE MOVED TO A NEW CLASS ONCE I ADD FUNCTIONALITY FOR LOADING THEM
				
				// this is saving the universal set of products
				Path dbSavePath = Paths.get("src/data/productsDB.txt");
				System.out.println("Saving database...");
				try {
					BufferedWriter writer = Files.newBufferedWriter(dbSavePath);
					for(Product p : database) { // go through every product in the database (not to be confused by the data in the table)
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
					for(Product p : shelfData) { // go through every product in the table
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
					for(Product p : inventoryData) { // go through every product in the table
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
		
		quit.setOnMouseClicked(new EventHandler<MouseEvent>(){
			public void handle (MouseEvent event) {
//				dataHandler.quickSave(stage, tableData);
				Platform.exit();
			}
		});
	}

	public static void main(String[] args) { 
		launch(); 
	}

}

