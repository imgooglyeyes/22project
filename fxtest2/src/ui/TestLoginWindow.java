package ui;

import application.Product;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

// THIS IS NOT USED ANYWHERE FOR THE SHOPPEEFARM APP
// THIS IS A LEFTOVER FROM MY EXER 9
// IT MIGHT BE RECYCLED
// WILL BE DELETED EVENTUALLY

public class TestLoginWindow {
	
	private LabelledTextField usernameField;
	private LabelledPasswordField passwordField;
	
	private Button login;
	private Button exit;
	private VBox loginArea;
	private Label welcomeText;
	private Label userTypeText;
	private VBox successArea;
	private HBox buttonsHBox;
	private Scene loginScene;
	private Scene successScene;
	private Stage loginWindow;
	
	public TestLoginWindow(Stage stage, ObservableList<Product> data) {
		this.usernameField = new LabelledTextField("Username:", true);
		this.passwordField = new LabelledPasswordField("Password: ", true);
        this.login = new Button("Log In");
        this.exit = new Button("Cancel");
        this.userTypeText = new Label();
        this.welcomeText = new Label();
        welcomeText.setStyle("-fx-font-size: 20;");
        
        login.disableProperty().bind(usernameField.empty.or(passwordField.empty));
        
        this.buttonsHBox = new HBox(10, exit, login);
        this.loginArea = new VBox(10, usernameField, passwordField, buttonsHBox); // creates a vbox with a 10px spacing with these as children
        loginArea.setStyle("-fx-padding: 20;");
        
        this.successArea = new VBox(10, welcomeText, userTypeText);
        successArea.setStyle("-fx-padding: 20;");
        successArea.setAlignment(Pos.CENTER);
        

        this.loginScene = new Scene(loginArea, 300, 300);
        this.successScene = new Scene(successArea, 400, 200);

        this.loginWindow = new Stage(); // creates a new window for the user creation 
        loginWindow.setTitle("Test Log In");
        loginWindow.setScene(loginScene);
        
        // Handle submission / closing
        login.setOnMouseClicked(new EventHandler<MouseEvent>(){
			public void handle (MouseEvent event) {
				for (Product u : data) { // check every known user
					if (u.getUsername().equals(usernameField.getText()) && u.getPassword().equals(passwordField.getText())) { // if username and password matches anyone within the data
						welcomeText.setText("Welcome, " + u.getFirstName() + " " + u.getLastName() + "!");
						userTypeText.setText(u.getUserType());
						loginWindow.setTitle("Log In Success");
						loginWindow.setScene(successScene);
						return;
					}
				}
				Alert alert = new Alert(AlertType.ERROR); // create an alert window

				alert.setTitle("Invalid Log In");
				alert.setHeaderText("Something Went Wrong");
				alert.setContentText("Invalid username or password.");

				alert.showAndWait();
				return;
			}
		});
        
        exit.setOnMouseClicked(new EventHandler<MouseEvent>(){
			public void handle (MouseEvent event) {
				loginWindow.close();
			}
		});

        loginWindow.initModality(Modality.WINDOW_MODAL); // blocks the user from interacting with the window below it
        loginWindow.initOwner(stage); // the window below it being the main stage

        
	}
	
	public void show() { // just to make things simpler
		// make the new window actually appear
        loginWindow.show();		
	}
}
