package ui;

import application.User;
import application.UserContainer;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * shows the list of staff (managers first).
 *
 * <p>managers can click on any staff member in the list to view their transaction history.
 *
 * @author Jian Carranza
 * @version 1.0
 * @see SceneManager
 */
public class StaffListScene extends Scene {

    private UserContainer userData;
//    private User currentUser;

    private ObjectProperty<User> selectedStaff =
            new SimpleObjectProperty<>();

    private VBox staffRows = new VBox(5);

    /**
     * creates the shelf scene with the buttons that fit the user type
     *
     * @param stage the main window
     * @param manager the scene manager, used for the header
     * @param userData every user and their details
     * @param currentUser the user who is logged in
     */
    public StaffListScene(
            Stage stage,
            SceneManager manager,
            UserContainer userData,
            User currentUser) {

        super(new BorderPane(), 1000, 500);

        // loads the shared stylesheet
        Styles.apply(this);

        this.userData = userData;
//        this.currentUser = currentUser;

        BorderPane root = (BorderPane) getRoot();
        root.setPadding(new Insets(12));

        HBox header = new HBox(10);
        header.getChildren().addAll(
            createLabel("Username", 150),
            createLabel("First Name", 200),
            createLabel("Last Name", 200),
            createLabel("Role", 100)
        );

        VBox staffList = new VBox(10, header, staffRows);
        ScrollPane scrollPane = new ScrollPane(staffList);
        scrollPane.setFitToWidth(true);

        Button viewHistory = new Button("View History");
        Button logout = new Button("Log Out");
        Button quit = new Button("Quit");


        viewHistory.disableProperty().bind(Bindings.isNull(selectedStaff));

        viewHistory.setOnAction(event -> {
            User targetUser = selectedStaff.get();
            if (targetUser != null) {
                manager.showHistory(targetUser);
            }
        });


        logout.setOnAction(event -> {
        	currentUser.logOut();
        	manager.showShelf();
        });
        quit.setOnAction(event -> Platform.exit());

//        boolean isManager = currentUser.getUserType().equals(User.MANAGER);
//        boolean isClerk = currentUser.getUserType().equals(User.CLERK);
//        boolean isCustomer = currentUser.getUserType().equals(User.CUSTOMER);

        root.setTop(manager.createHeader("Staff List"));
        root.setCenter(scrollPane);
        root.setBottom(new HBox(10, viewHistory, logout, quit));

        refreshStaffList();
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
     * creates a row for one product, clicking the row selects the product
     *
     * @param product the shelf product to show
     */
    private void addStaffRow(User user) {
        HBox row = new HBox(10);
        Label username = createLabel(user.getUsername(), 150);
        Label firstName = createLabel(user.getFirstName(), 200);
        Label lastName = createLabel(user.getLastName(), 200);
        Label userType = createLabel(user.getUserType(), 100);

        row.getChildren().addAll(username, firstName, lastName, userType);
        row.setPadding(new Insets(8));
        row.setStyle("-fx-border-color: lightgray;");

        row.setOnMouseClicked(event -> {
            selectedStaff.set(user);
            for (Node node : staffRows.getChildren()) {
                node.setStyle("-fx-border-color: lightgray;");
            }
            row.setStyle(
                "-fx-background-color: lightblue;" +
                "-fx-border-color: gray;"
            );
        });

        staffRows.getChildren().add(row);
    }

    /** redraws the shelf from the shelf container and clears the selection */
    public void refreshStaffList() {
        staffRows.getChildren().clear();
        selectedStaff.set(null);
        for (User user : userData.getUsersByType(User.MANAGER)) { // add managers first
            addStaffRow(user);
        }
        for (User user : userData.getUsersByType(User.CLERK)) { // add clerks next
            addStaffRow(user);
        }
    }
}