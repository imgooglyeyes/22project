package ui;

import application.ImportExportHandler;
import application.Product;
import application.ProductContainer;
import application.User;
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

public class InventoryScene extends Scene{
	// ======= TEMPLATE ========
	
	@SuppressWarnings({ "deprecation", "unchecked" })
	public InventoryScene(Stage stage, ProductContainer database, ProductContainer inventoryData, ProductContainer shelfData , Scene mainScene, User currentUser){
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

	        
	        table.setItems(inventoryData.getObservableList());
	        
			BorderPane root = new BorderPane(table);
			
			// ===== END OF TEMPLATE ====
			
			Button createProduct = new Button("Add Product From Database");
			Button viewShelf = new Button("View Shelf");
			Button quit = new Button("Quit");
			HBox moreButtons = new HBox(10); // creates an hbox with a 10px spacing and these 4 buttons as children
			moreButtons.setStyle("-fx-padding: 10;"); // this gives a nicer bit of space around the hbox
			
			createProduct.setOnMouseClicked(new EventHandler<MouseEvent>(){
				public void handle (MouseEvent event) {
					AddProductWindow addWindow = new AddProductWindow(stage, table, inventoryData, database, false); // adding to inventory based on info in database
					addWindow.show();
			}});
			
			viewShelf.setOnMouseClicked(new EventHandler<MouseEvent>(){
				public void handle (MouseEvent event) {
					stage.setScene(mainScene);
					stage.setTitle("proof of concept");
			}});
			
			moreButtons.getChildren().addAll(createProduct, viewShelf);
			
			// view database button
			if (currentUser.getUserType().equals(User.MANAGER)) {
				Button viewDatabase = new Button("View Database");
				
				viewDatabase.setOnMouseClicked(new EventHandler<MouseEvent>(){
					public void handle (MouseEvent event) {
						// temporarily uses database instead of inventoryData, inventory is not yet implemented
						DatabaseScene databaseScene = new DatabaseScene(stage, database, inventoryData, shelfData, mainScene, currentUser);
						stage.setScene(databaseScene);
						stage.setTitle("Database");
				}});
				
				Button removeStocks = new Button("Delete Stocks");
				
				BooleanBinding noItemSelected = Bindings.createBooleanBinding(() -> { // a boolean binding to check if the table has anything selected or not
			        if (table.getSelectionModel().getSelectedItem() == null) return true;
			        else return false;
			    }, table.getSelectionModel().selectedItemProperty());
				
				removeStocks.disableProperty().bind(noItemSelected);
				
				removeStocks.setOnMouseClicked(new EventHandler<MouseEvent>(){ // will be changed to affect stocks
					public void handle (MouseEvent event) {
						MoveProductWindow moveWindow = new MoveProductWindow(stage, table, inventoryData, null, table.getSelectionModel().getSelectedItem());
						// note: null is selected as the storage parameter for MoveProductWindow. This means, products removed from inventoryData will be moved nowhere.
						moveWindow.show();
				}});
				
				moreButtons.getChildren().addAll(viewDatabase, removeStocks);
			}
			
			quit.setOnMouseClicked(new EventHandler<MouseEvent>(){
				public void handle (MouseEvent event) {
//					dataHandler.quickSave(stage, tableData);
					Platform.exit();
				}
			});
			
			moreButtons.getChildren().add(quit);
			
			root.setTop(moreButtons);
			root.setStyle("-fx-padding: 12;");
			super(root, 900, 500);
	}
}
