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
 * <p>only active accounts can log in, a pending or rejected account gets its own message
 * so the user knows the password was right
 *
 * <p>a successful login opens the dashboard, which shows the tiles that fit the role of the new user
 *
 * @author Jian Carranza
 * @version 1.2
 * @see SceneManager
 * @see DashboardScene
 * @see User#isActive()
 */
public class LoginScene extends Scene {

	/** shown when no account matches the username and password */
	private static final String INVALID_LOGIN = "Invalid username or password.";

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
		Label errorText = new Label(INVALID_LOGIN);
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

					if (!u.isActive()) { // right password, but the account can't be used yet
						errorText.setText(u.getStatus().equals(User.PENDING)
							? "This account is waiting for a manager to approve it."
							: "This account was rejected by a manager.");
						errorText.setVisible(true);
						return;
					}

					currentUser.replaceWith(u); // change current user with new log in
					manager.showDashboard(); // the user is logged in now, so the dashboard is allowed
					return;
				}
			}
			errorText.setText(INVALID_LOGIN); // the label may still show the pending message from before
			errorText.setVisible(true); // no user matches name and password
		});

		// sets up shared header, bottom buttons, and shows the login scene
		root.setTop(manager.createHeader("Log In"));
		root.setBottom(buttons);
		root.setCenter(loginArea);
	}
}