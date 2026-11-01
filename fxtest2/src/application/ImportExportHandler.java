package application;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

// ignore all of this for now, these are all left over from my exercise
// I'll repurpose it once I have to

public class ImportExportHandler {
	private File saveFile;
	
	public void importData(Stage stage, TableView<Product> table, ObservableList<Product> data) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Import Save File");
		fileChooser.getExtensionFilters().add(
			    new FileChooser.ExtensionFilter("Text Files", "*.txt")
			); // this filters the allowed extensions
		
		saveFile = fileChooser.showOpenDialog(stage);
		
		if (saveFile != null) {
			System.out.println("Loading database...");
			try {
				BufferedReader reader = Files.newBufferedReader(saveFile.toPath());
				int ignored = 0, added = 0; // tracked for the success window
				Boolean duplicate; // stops the code from adding a duplicate user
				String line;
				while ((line = reader.readLine()) != null) { // read through every line
					String[] userData = line.split(","); // split the line by ,
					duplicate = false;
					for(Product u : data) { // check every user
//						if (Arrays.equals(userData, u.getDataArray())) { // does this set of data already exist?
//							ignored += 1;
//							duplicate = true; // then it's a duplicate
//							break; // end checking
//						}
					}
					if (duplicate) continue; // end this iteration so it won't add
					// if no duplicate is found
//					data.add(new Product(userData[0], userData[1], userData[2], userData[3], userData[4], userData[5], userData[6]));
					added += 1;
					// data is formatted by username,email,firstn,middlen,lastn,usertype,password
				}
				System.out.println("Loading complete.");
				table.setItems(data);
				reader.close();
				createSuccessWindow(stage, ignored, added);
			} catch (IOException e) {
				System.out.println("Something went wrong.");
				e.printStackTrace();
			}
		} else {
			System.out.println("Save file is null. Most likely cancelled by user.");
			return;
		}
		
		return;
	}
	
	public void exportData(Stage stage, ObservableList<Product> data) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Export Save File");
		fileChooser.setInitialFileName("exported-users.txt"); // Default name
		fileChooser.getExtensionFilters().add(
		    new FileChooser.ExtensionFilter("Text Files", "*.txt")
		); // this filters the allowed extensions

		saveFile = fileChooser.showSaveDialog(stage);

		if (saveFile != null) {
			System.out.println("Saving database...");
			try {
				BufferedWriter writer = Files.newBufferedWriter(saveFile.toPath());
				for(Product u : data) { // go through every user
//					writer.write(u.getUsername() + "," + u.getEmailAddress() + "," + u.getFirstName() + "," + u.getMiddleName() + "," + u.getLastName() + "," + u.getUserType() + "," + u.getPassword());
					writer.newLine();
				}
				// data is formatted by username,email,firstn,middlen,lastn,usertype,password
				System.out.println("Saving complete.");
				writer.close();
			} catch (IOException e) {
				System.out.println("Something went wrong.");
				e.printStackTrace();
			}
		    System.out.println("Saving to: " + saveFile.getAbsolutePath()); // just for double checking through the console
		} else {
			System.out.println("Save file is null. Most likely cancelled by user.");
			return;
		}
	}
	
	public void quickSave(Stage stage,ObservableList<Product> data) { // used during save and quit, will save to most recent save file accessed or none of the user has not exported/imported anything this session
		if (saveFile != null) {
			System.out.println("Saving database...");
			try {
				BufferedWriter writer = Files.newBufferedWriter(saveFile.toPath());
				for(Product u : data) { // go through every user
//					writer.write(u.getUsername() + "," + u.getEmailAddress() + "," + u.getFirstName() + "," + u.getMiddleName() + "," + u.getLastName() + "," + u.getUserType() + "," + u.getPassword());
					writer.newLine();
				}
				// data is formatted by username,email,firstn,middlen,lastn,usertype,password
				System.out.println("Saving complete.");
				writer.close();
			} catch (IOException e) {
				System.out.println("Something went wrong.");
				e.printStackTrace();
			}
		    System.out.println("Saving to: " + saveFile.getAbsolutePath()); // just for double checking through the console
		} else {
			System.out.println("Attempted to save and quit but user has not made/opened a save this session.");
			return;
		}
	}
	
	private void createSuccessWindow(Stage stage, int ignored, int added) { // success screen for importing
		Label lowerText = new Label(added + " users added and " + ignored+ " duplicates ignored.");
        Label upperText = new Label("Import successful!");
        upperText.setStyle("-fx-font-size: 20;");
        
        VBox successArea = new VBox(10, upperText, lowerText);
        successArea.setStyle("-fx-padding: 20;");
        successArea.setAlignment(Pos.CENTER);
        Scene successScene = new Scene(successArea, 400, 200);
        
        Stage successWindow = new Stage(); // creates a new window for the success screen
        successWindow.setTitle("Import Successful");
        successWindow.setScene(successScene);
        successWindow.show(); // don't forget to make the screen actually appear
        
		return;
	}
}
