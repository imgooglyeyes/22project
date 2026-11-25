package ui;

import java.util.Optional;

import application.User;
import application.UserContainer;
import application.UsersFileHandler;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * screen where a manager approves or rejects the accounts that are waiting for approval
 *
 * <p>only pending accounts are listed, these are the clerk and manager accounts made in the sign up screen.
 * an approved account becomes active and can log in, a rejected account stays in the save file
 * as rejected, so its username can't be taken by someone else
 *
 * <p>every decision is saved to users.txt right away.
 * the scene is created again every time it opens, so the list is always up to date
 *
 * @author Shawn Alfred Padilla
 * @version 1.0
 * @see SceneManager#showSignUpRequests()
 * @see SceneManager#canApproveSignUps()
 * @see SignUpScene
 * @see UsersFileHandler
 */
public class SignUpRequestsScene extends Scene {

    /** the shared scene manager, used for the role check and to open other scenes */
    private SceneManager manager;
    /** every user, the pending ones are listed and their status is changed here */
    private UserContainer userData;

    /** the row that was clicked, null when nothing is selected */
    private ObjectProperty<User> selectedUser =
            new SimpleObjectProperty<>();

    private VBox userRows = new VBox(5);

    /**
     * creates the sign up requests scene with the approve and reject buttons
     *
     * @param stage the main window
     * @param manager the scene manager, used for the header and the role check
     * @param userData every user and their details
     * @param currentUser the user who is logged in
     */
    public SignUpRequestsScene(
            Stage stage,
            SceneManager manager,
            UserContainer userData,
            User currentUser) {

        super(new BorderPane(), 1000, 500);

        // loads the shared stylesheet
        Styles.apply(this);

        this.manager = manager;
        this.userData = userData;

        BorderPane root = (BorderPane) getRoot();
        root.setPadding(new Insets(12));

        // creates the column headers
        HBox header = new HBox(10);
        header.getChildren().addAll(
            createLabel("Username", 150),
            createLabel("First Name", 200),
            createLabel("Last Name", 200),
            createLabel("Role", 100)
        );

        VBox requestList = new VBox(10, header, userRows);
        ScrollPane scrollPane = new ScrollPane(requestList);
        scrollPane.setFitToWidth(true);

        // creates the buttons
        Button approve = new Button("Approve");
        Button reject = new Button("Reject");
        Button logout = new Button("Log Out");
        Button quit = new Button("Quit");

        // approve and reject need a selected account
        approve.disableProperty().bind(Bindings.isNull(selectedUser));
        reject.disableProperty().bind(Bindings.isNull(selectedUser));

        approve.setOnAction(event -> decide(User.ACTIVE));

        // a rejected account can't log in, so ask before doing it
        reject.setOnAction(event -> {
            User user = selectedUser.get();
            if (user == null) return;

            if (askToProceed("Reject the account " + user.getUsername() + "?\nThey will not be able to log in.")) {
                decide(User.REJECTED);
            }
        });

        logout.setOnAction(event -> {
            currentUser.logOut();
            manager.showShelf();
        });
        quit.setOnAction(event -> Platform.exit());

        root.setTop(manager.createHeader("Sign Up Requests"));
        root.setCenter(scrollPane);
        root.setBottom(new HBox(10, approve, reject, logout, quit));

        refreshRequests();
    }

    /**
     * creates a label with a fixed width
     *
     * @param text the text of the label
     * @param width the preferred width in pixels
     * @return the label
     */
    private Label createLabel(String text, double width) {
        Label label = new Label(text);
        label.setPrefWidth(width);
        return label;
    }

    /**
     * asks the manager to confirm an action
     *
     * @param message what is about to happen
     * @return true if they pressed ok
     */
    private boolean askToProceed(String message) {
        Alert alert = new Alert(AlertType.CONFIRMATION, message);
        alert.setHeaderText(null);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /**
     * sets the status of the selected account and saves the users
     * <p>nothing happens unless the current user can approve sign ups and the account is still pending,
     * so a stale selection can't change an account that was already decided
     *
     * @param newStatus the new status, active to approve or rejected to reject
     */
    private void decide(String newStatus) {
        if (!manager.canApproveSignUps()) return;

        User user = selectedUser.get();
        if (user == null || !User.PENDING.equals(user.getStatus())) return;

        user.setStatus(newStatus);
        UsersFileHandler.save(userData); // saved right away so the decision survives a restart

        refreshRequests();
    }

    /**
     * creates a row for one pending account, clicking the row selects the account
     *
     * @param user the pending account to show
     */
    private void addRequestRow(User user) {
        HBox row = new HBox(10);
        Label username = createLabel(user.getUsername(), 150);
        Label firstName = createLabel(user.getFirstName(), 200);
        Label lastName = createLabel(user.getLastName(), 200);
        Label userType = createLabel(user.getUserType(), 100);

        row.getChildren().addAll(username, firstName, lastName, userType);
        row.setPadding(new Insets(8));
        row.setStyle("-fx-border-color: lightgray;");

        // selects the account when its row is clicked
        row.setOnMouseClicked(event -> {
            selectedUser.set(user);

            // resets all row styles
            for (Node node : userRows.getChildren()) {
                node.setStyle("-fx-border-color: lightgray;");
            }

            // highlights the selected row
            row.setStyle(
                "-fx-background-color: lightblue;" +
                "-fx-border-color: gray;"
            );
        });

        userRows.getChildren().add(row);
    }

    /**
     * redraws the list from the user container and clears the selection
     * <p>only pending accounts get a row, a note is shown when there are none
     */
    public void refreshRequests() {
        userRows.getChildren().clear();
        selectedUser.set(null);

        for (User user : userData.getUsers()) {
            if (User.PENDING.equals(user.getStatus())) addRequestRow(user);
        }

        if (userRows.getChildren().isEmpty()) {
            userRows.getChildren().add(new Label("There are no sign up requests waiting."));
        }
    }
}