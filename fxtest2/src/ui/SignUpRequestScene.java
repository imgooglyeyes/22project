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
 * screen where managers approve or reject pending clerk and manager sign-ups
 *
 * <p>only accounts with the Pending status are listed here. approving sets the
 * status to Active so the user can log in; rejecting sets it to Rejected.
 * changes are saved immediately through UsersFileHandler
 *
 * @author Matthew Prado
 * @version 1.0
 * @see SceneManager#canApproveSignUps()
 * @see UsersFileHandler#save(UserContainer)
 */
public class SignUpRequestsScene extends Scene {

    private UserContainer userData;

    private ObjectProperty<User> selectedRequest = new SimpleObjectProperty<>();

    private VBox requestRows = new VBox(5);

    /**
     * creates the sign-up requests scene
     *
     * @param stage the main window
     * @param manager the scene manager, used for the header and navigation
     * @param userData every user and their details
     * @param currentUser the manager who is logged in
     */
    public SignUpRequestsScene(
            Stage stage,
            SceneManager manager,
            UserContainer userData,
            User currentUser) {

        super(new BorderPane(), 1000, 500);

        // loads the shared stylesheet
        Styles.apply(this);

        this.userData = userData;

        BorderPane root = (BorderPane) getRoot();
        root.setPadding(new Insets(12));

        // creates the column headers
        HBox header = new HBox(10);
        header.setPadding(new Insets(0, 8, 0, 8)); // lines up with the row padding
        header.getChildren().addAll(
            createLabel("Username", 160),
            createLabel("First Name", 200),
            createLabel("Last Name", 200),
            createLabel("Requested Role", 140),
            createLabel("Status", 100)
        );

        VBox requestList = new VBox(10, header, requestRows);
        ScrollPane scrollPane = new ScrollPane(requestList);
        scrollPane.setFitToWidth(true);

        // creates the buttons
        Button approveButton = new Button("Approve");
        Button rejectButton = new Button("Reject");
        Button logout = new Button("Log Out");
        Button quit = new Button("Quit");

        // approve and reject require a selected pending request
        approveButton.disableProperty().bind(Bindings.isNull(selectedRequest));
        rejectButton.disableProperty().bind(Bindings.isNull(selectedRequest));

        approveButton.setOnAction(event -> handleDecision(true));
        rejectButton.setOnAction(event -> handleDecision(false));

        logout.setOnAction(event -> {
            currentUser.logOut();
            manager.showShelf();
        });
        quit.setOnAction(event -> Platform.exit());

        root.setTop(manager.createHeader("Sign-Up Requests"));
        root.setCenter(scrollPane);
        root.setBottom(new HBox(10, approveButton, rejectButton, logout, quit));

        // loads the pending sign-up requests
        refreshRequests();
    }

    // creates a label with a fixed width
    private Label createLabel(String text, double width) {
        Label label = new Label(text);
        label.setPrefWidth(width);
        return label;
    }

    // asks the manager to confirm an action, true if they pressed ok
    private boolean askToProceed(String message) {
        Alert alert = new Alert(AlertType.CONFIRMATION, message);
        alert.setHeaderText(null);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    // approves or rejects the selected pending account and saves users.txt
    private void handleDecision(boolean approve) {
        User target = selectedRequest.get();
        if (target == null) return;

        String actionText = approve ? "Approve" : "Reject";
        String message = actionText + " " + target.getUserType() +
                " account for " + target.getFirstName() + " " + target.getLastName() +
                " (" + target.getUsername() + ")?";

        if (!askToProceed(message)) return;

        target.setStatus(approve ? User.ACTIVE : User.REJECTED);
        UsersFileHandler.save(userData);

        refreshRequests();
    }

    // creates a row for one pending user
    private void addRequestRow(User user) {
        HBox row = new HBox(10);
        row.getChildren().addAll(
            createLabel(user.getUsername(), 160),
            createLabel(user.getFirstName(), 200),
            createLabel(user.getLastName(), 200),
            createLabel(user.getUserType(), 140),
            createLabel(user.getStatus(), 100)
        );
        row.setPadding(new Insets(8));
        row.setStyle("-fx-border-color: lightgray;");

        // selects the user when its row is clicked
        row.setOnMouseClicked(event -> {
            selectedRequest.set(user);

            for (Node node : requestRows.getChildren()) {
                node.setStyle("-fx-border-color: lightgray;");
            }

            row.setStyle(
                "-fx-background-color: lightblue;" +
                "-fx-border-color: gray;"
            );
        });

        requestRows.getChildren().add(row);
    }

    // refreshes the list of pending users
    public void refreshRequests() {
        requestRows.getChildren().clear();
        selectedRequest.set(null);

        for (User user : userData.getUsers()) {
            if (User.PENDING.equals(user.getStatus())) {
                addRequestRow(user);
            }
        }

        if (requestRows.getChildren().isEmpty()) {
            requestRows.getChildren().add(new Label("There are no pending sign-up requests."));
        }
    }
}
