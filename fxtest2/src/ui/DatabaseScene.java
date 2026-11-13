package ui;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import application.Product;
import application.ProductContainer;
import application.User;
import backend.ImportExportHandler;
import backend.ProductCreationWindow;
import backend.ProductEditingWindow;
import backend.UniversalDeleter;
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
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class DatabaseScene extends Scene{
	// ======= TEMPLATE ========
	
	@SuppressWarnings({ "deprecation", "unchecked" })
	public DatabaseScene(Stage stage, ProductContainer database, ProductContainer inventoryData, ProductContainer shelfData, User currentUser){
			TableView<Product> table = new TableView<>();
	        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

	        TableColumn<Product, String> colID			= new TableColumn<>("Internal ID");
	        TableColumn<Product, String> colName		= new TableColumn<>("Name");
	        TableColumn<Product, String> colDesc		= new TableColumn<>("Description");
	        TableColumn<Product, String> colPrice		= new TableColumn<>("Price");

	        colID.setCellValueFactory(new PropertyValueFactory<>("id"));
	        colName.setCellValueFactory(new PropertyValueFactory<>("name")); // I have no idea how these work but they seem to be calling getter functions somehow
	        colDesc.setCellValueFactory(new PropertyValueFactory<>("description"));
	        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));

	        table.getColumns().addAll( // add these columns to the table
	            colID, colName, colDesc, colPrice
	        );

	        
	        table.setItems(database.getObservableList());
	        
			BorderPane root = new BorderPane(table);
			
			// ===== END OF TEMPLATE ====
			
			Button createProduct = new Button("Create New Product");
			Button viewShelf = new Button("View Shelf");
			Button viewInventory = new Button("View Inventory");
			Button deleteSelected = new Button("Delete All Instances"); // not yet implemented
			Button editSelected = new Button("Edit Product Data");
			BooleanBinding noItemSelected = Bindings.createBooleanBinding(() -> { // a boolean binding to check if the table has anything selected or not
		        if (table.getSelectionModel().getSelectedItem() == null) return true;
		        else return false;
		    }, table.getSelectionModel().selectedItemProperty());
			
			deleteSelected.disableProperty().bind(noItemSelected);
			editSelected.disableProperty().bind(noItemSelected);
			
			Button quit = new Button("Quit");
			
			UniversalDeleter deleter = new UniversalDeleter(table, database, inventoryData, shelfData); // this is used to delete all instances of a product

			HBox moreButtons = new HBox(10, createProduct, viewShelf, viewInventory, editSelected, deleteSelected, quit); // creates an hbox with a 10px spacing and these 4 buttons as children
			moreButtons.setStyle("-fx-padding: 10;"); // this gives a nicer bit of space around the hbox
			//ImportExportHandler dataHandler = new ImportExportHandler(); // will be reused soon. currently not used anywhere in the code.
			
			createProduct.setOnMouseClicked(new EventHandler<MouseEvent>(){
				public void handle (MouseEvent event) {
					ProductCreationWindow creationWindow = new ProductCreationWindow(stage, database, table); // I had to make it a new class to shorten the code, also more modular that way
					creationWindow.show();
			}});
			
			viewShelf.setOnMouseClicked(new EventHandler<MouseEvent>(){
				public void handle (MouseEvent event) {
					// temporarily uses database instead of inventoryData, inventory is not yet implemented
					ShelfScene shelfScene = new ShelfScene(stage, database, inventoryData, shelfData, currentUser);
					stage.setScene(shelfScene);
					stage.setTitle("The Shelf");
			}});
			
			viewInventory.setOnMouseClicked(new EventHandler<MouseEvent>(){
				public void handle (MouseEvent event) {
					InventoryScene inventoryScene = new InventoryScene(stage, database, inventoryData, shelfData, currentUser);
					stage.setScene(inventoryScene);
					stage.setTitle("Inventory");
			}});
			
			editSelected.setOnMouseClicked(new EventHandler<MouseEvent>(){
				public void handle (MouseEvent event) {
					ProductEditingWindow editWindow = new ProductEditingWindow(stage, table, database, inventoryData, shelfData, table.getSelectionModel().getSelectedItem()); // I had to make it a new class to shorten the code, also more modular that way
					editWindow.show();
			}});
			
			deleteSelected.setOnMouseClicked(new EventHandler<MouseEvent>(){
				public void handle (MouseEvent event) {
					deleter.delete(table.getSelectionModel().getSelectedItem());
			}});
			
			quit.setOnMouseClicked(new EventHandler<MouseEvent>(){
				public void handle (MouseEvent event) {
//					dataHandler.quickSave(stage, tableData);
					Platform.exit();
				}
			});
			
			root.setTop(moreButtons);
			root.setStyle("-fx-padding: 12;");
			super(root, 900, 500);
	}
}
