package ui;

import application.User;
import application.UserContainer;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * changes the current user if login credentials match an existing account
 *
 *
 * @author Jian Carranza
 * @version 1.0
 * @see SceneManager
 */
public class LoginScene extends Scene {
	
	/**
     * creates the login scene
     *
     * @param stage the main window
     * @param manager the scene manager, used for the header
     * @param userData every user and their details
     * @param currentUser the user who is not yet logged in
     */
	public LoginScene(
            Stage stage,
            SceneManager manager,
            UserContainer userData,
            User currentUser) {

        super(new BorderPane(), 1000, 500);
        
        // loads the shared stylesheet
        Styles.apply(this);

        
        LabelledTextField usernameField = new LabelledTextField("Username:", true);
		LabelledPasswordField passwordField = new LabelledPasswordField("Password: ", true);
        Label errorText = new Label("Invalid username or password.");
        errorText.setVisible(false); // starts as invisible
		Button confirm = new Button("Confirm");
        
        // disabled if the username or password is invalid
        confirm.disableProperty().bind(usernameField.invalid.or(passwordField.invalid));
        
        // puts the important buttons together
        VBox loginArea = new VBox(10, usernameField, passwordField, errorText, confirm); // creates a vbox with a 10px spacing with these as children
        loginArea.setStyle("-fx-padding: 20;");
        
        BorderPane root = (BorderPane) getRoot();
        root.setPadding(new Insets(12));

        
        Button quit = new Button("Quit");

        HBox buttons = new HBox(10,
            quit
        );
        
        // exits the application
        quit.setOnAction(event -> Platform.exit());
        
        // validates login and replaces current user if valid
        confirm.setOnAction(event -> {
        	for (User u : userData.getUsers()) { // check every known user
				if (u.getUsername().equals(usernameField.getText()) && u.getPassword().equals(passwordField.getText())) { // if username and password matches anyone within the data
					currentUser.replaceWith(u); // change current user with new log in
					manager.showShelf();
					return;
				}
			}
			errorText.setVisible(true); // no user matches name and password
			return;
        });
        
        // sets up shared header, bottom buttons, and shows the login scene
        root.setTop(manager.createHeader("Log In"));
        root.setBottom(buttons);
        root.setCenter(loginArea);
	}
}
