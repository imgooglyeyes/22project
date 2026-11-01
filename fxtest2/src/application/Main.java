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

        ArrayList<Product> sampleList = new ArrayList<>();
        sampleList.add(new Product("Example",  "This is an example to show off what the finale product could possibly look like. If I make the text long enough, will it wrap around? I don't know yet. Let's find out together, shall we?",  100.0, 20));
        sampleList.add(new Product("Another product", "here's a decription", 200, 100));
        
        ObservableList<Product> tableData = FXCollections.observableArrayList(sampleList);
        ObservableList<Product> database = FXCollections.observableArrayList(sampleList);
        table.setItems(tableData);
        
		BorderPane root = new BorderPane(table);
		
		// ===== END OF TEMPLATE ====
		
		Button createProduct = new Button("Create New Product In Database");
		Button addProduct = new Button("Add Product From Inventory");
		Button deleteSelected = new Button("Delete Selected");
		Button exportBtn = new Button("Save Data");
		Button quit = new Button("Save and Quit");
		Button login = new Button("Test Login");
		HBox moreButtons = new HBox(10, createProduct, addProduct, deleteSelected, exportBtn, quit, login); // creates an hbox with a 10px spacing and these 4 buttons as children
		moreButtons.setStyle("-fx-padding: 10;"); // this gives a nicer bit of space around the hbox
		ImportExportHandler dataHandler = new ImportExportHandler(); // will be reused soon. currently not used anywhere in the code.
		
		createProduct.setOnMouseClicked(new EventHandler<MouseEvent>(){
			public void handle (MouseEvent event) {
				ProductCreationWindow creationWindow = new ProductCreationWindow(stage, database); // I had to make it a new class to shorten the code, also more modular that way
				creationWindow.show();
		}});
		
		addProduct.setOnMouseClicked(new EventHandler<MouseEvent>(){
			public void handle (MouseEvent event) {
				AddProductWindow creationWindow = new AddProductWindow(stage, table, tableData, database); // I had to make it a new class to shorten the code, also more modular that way
				creationWindow.show();
		}});
		
		deleteSelected.setOnMouseClicked(new EventHandler<MouseEvent>(){
			public void handle (MouseEvent event) {
				tableData.remove(table.getSelectionModel().getSelectedItem()); // takes the currently selected object (DON'T TAKE THE INDEX, IT'S NOT CONSTANT)
				table.setItems(tableData); // update items
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
						writer.write(p.getID() + "|" + p.getName() + "|" + p.getDescription() + "|" + p.getPrice());
						writer.newLine();
					}
					// data is formatted by id|name|description|price
					System.out.println("Saving complete.");
					writer.close();
				} catch (IOException e) {
					System.out.println("Something went wrong.");
					e.printStackTrace();
				}
				
				// this is saving the products on sale
				Path tableSavePath = Paths.get("src/data/shelf.txt");
				System.out.println("Saving database...");
				try {
					BufferedWriter writer = Files.newBufferedWriter(tableSavePath);
					for(Product p : tableData) { // go through every product in the table
						writer.write(p.getID() + "|" + p.getStock());
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
		
		root.setTop(moreButtons);
		root.setStyle("-fx-padding: 12;");
		Scene mainScene = new Scene(root, 900, 500);
		stage.setScene(mainScene);
		stage.setTitle("proof of concept");
		stage.show();
	}

	public static void main(String[] args) { 
		launch(); 
	}

}

