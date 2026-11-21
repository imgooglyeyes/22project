package ui;

import application.User;
import application.UserContainer;
import application.UsersFileHandler;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * screen where a guest creates a new account
 *
 * <p>a customer account is active at once, a clerk or manager account is saved as pending
 * until a manager approves it (see SignUpRequestsScene in phase 3).
 * the sign up button stays disabled while a field is empty, contains |, the username is
 * already used or the two passwords are different
 *
 * <p>the new user is added to the shared user container and saved right away.
 * the scene is created again every time it opens, so the fields always start empty
 *
 * @author Shawn Alfred Padilla
 * @version 1.0
 * @see SceneManager#showSignUp()
 * @see UserContainer#isUsernameTaken(String)
 * @see UsersFileHandler
 */
public class SignUpScene extends Scene {

	/** the account types a person can pick when signing up */
	private static final String[] ROLES = { User.CUSTOMER, User.CLERK, User.MANAGER };

	/**
	 * creates the sign up scene
	 *
	 * @param stage the main window
	 * @param manager the scene manager, used for the header and to open the login scene afterwards
	 * @param userData every user, the new account is added here
	 */
	public SignUpScene(Stage stage, SceneManager manager, UserContainer userData) {

		super(new BorderPane(), 1000, 500);

		// loads the shared stylesheet
		Styles.apply(this);

		BorderPane root = (BorderPane) getRoot();
		root.setPadding(new Insets(12));

		LabelledTextField usernameField = new LabelledTextField("Username:", true);
		LabelledPasswordField passwordField = new LabelledPasswordField("Password:", true);
		LabelledPasswordField confirmField = new LabelledPasswordField("Confirm password:", true);
		LabelledTextField firstNameField = new LabelledTextField("First name:", true);
		LabelledTextField lastNameField = new LabelledTextField("Last name:", true);

		// customer is the default, it is the only type that needs no approval
		ComboBox<String> roleBox = new ComboBox<>(FXCollections.observableArrayList(ROLES));
		roleBox.setValue(User.CUSTOMER);
		HBox roleRow = new HBox(10, new Label("Account type:"), roleBox);

		// a note that only shows for the types that need a manager to approve them
		Label roleNote = new Label();
		roleNote.textProperty().bind(Bindings.createStringBinding(
			() -> User.CUSTOMER.equals(roleBox.getValue())
				? ""
				: "A manager has to approve this account before you can log in",
			roleBox.valueProperty()
		));

		// true while the username belongs to someone else (or is the reserved guest name)
		BooleanBinding usernameTaken = Bindings.createBooleanBinding(
			() -> userData.isUsernameTaken(usernameField.getText()),
			usernameField.getTextField().textProperty()
		);

		// true while the two password fields are not the same
		BooleanBinding passwordsDiffer = Bindings.createBooleanBinding(
			() -> !passwordField.getText().equals(confirmField.getText()),
			passwordField.getPasswordField().textProperty(),
			confirmField.getPasswordField().textProperty()
		);

		// tells the person why the button is disabled, only after they started typing the field
		Label problemText = new Label();
		problemText.textProperty().bind(Bindings.createStringBinding(
			() -> {
				if (!usernameField.getText().trim().isEmpty() && usernameTaken.get()) return "This username can't be used";
				if (!confirmField.getText().isEmpty() && passwordsDiffer.get()) return "The passwords don't match";
				return "";
			},
			usernameTaken, passwordsDiffer, confirmField.getPasswordField().textProperty()
		));

		Button signUp = new Button("Sign Up");

		// the button is disabled while any of the checks fail
		signUp.disableProperty().bind(
			usernameField.invalid
				.or(passwordField.invalid)
				.or(confirmField.invalid)
				.or(firstNameField.invalid)
				.or(lastNameField.invalid)
				.or(usernameTaken)
				.or(passwordsDiffer)
				.or(roleBox.valueProperty().isNull())
		);

		// creates the account, saves it and goes to the login scene
		signUp.setOnAction(event -> {
			String username = usernameField.getText().trim();

			// the button is already disabled for this, checked again in case the list changed
			if (userData.isUsernameTaken(username)) {
				new Alert(AlertType.ERROR, "This username can't be used").showAndWait();
				return;
			}

			String role = roleBox.getValue();
			boolean needsApproval = !role.equals(User.CUSTOMER);

			userData.add(new User(
				username,
				passwordField.getText(), // kept as typed, login compares it the same way
				firstNameField.getText().trim(),
				lastNameField.getText().trim(),
				role,
				needsApproval ? User.PENDING : User.ACTIVE
			));
			UsersFileHandler.save(userData);

			new Alert(AlertType.INFORMATION, needsApproval
				? "Account created. A manager has to approve it before you can log in."
				: "Account created. You can log in now."
			).showAndWait();

			manager.showLogin();
		});

		VBox form = new VBox(10,
			usernameField, passwordField, confirmField,
			firstNameField, lastNameField,
			roleRow, roleNote, problemText, signUp
		);
		form.setStyle("-fx-padding: 20;");

		Button quit = new Button("Quit");
		quit.setOnAction(event -> Platform.exit());

		// sets up shared header, bottom buttons, and the form
		root.setTop(manager.createHeader("Sign Up"));
		root.setCenter(form);
		root.setBottom(new HBox(10, quit));
	}
}