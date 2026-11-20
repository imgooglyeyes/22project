package ui;

import java.util.ArrayList;
import java.util.Collections;

import application.SaleTransaction;
import application.User;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

// screen where a customer sees only their own purchase requests and their status
// it is read only, customers can't confirm or void anything
// the scene is created again every time it opens, so the list is always up to date

public class MyRequestsScene extends Scene {

    private VBox requestRows = new VBox(5);

    public MyRequestsScene(Stage stage, SceneManager manager, User currentUser) {

        super(new BorderPane(), 900, 500);

        // loads the shared stylesheet
        Styles.apply(this);

        BorderPane root = (BorderPane) getRoot();
        root.setPadding(new Insets(12));

        // creates the column headers
        HBox header = new HBox(10);
        header.getChildren().addAll(
            createLabel("Product", 250),
            createLabel("Qty", 60),
            createLabel("Total", 120),
            createLabel("Status", 110),
            createLabel("Requested", 170)
        );

        VBox requestList = new VBox(10, header, requestRows);
        ScrollPane scrollPane = new ScrollPane(requestList);
        scrollPane.setFitToWidth(true);

        Button quit = new Button("Quit");
        Button logout = new Button("Log Out");
        
        logout.setOnAction(event -> {
        	currentUser.logOut();
        	manager.showShelf();
        });
        quit.setOnAction(event -> Platform.exit());

        root.setTop(manager.createHeader("My Requests"));
        root.setCenter(scrollPane);
        root.setBottom(new HBox(10, logout, quit));

        // loads this customer's requests
        loadRequests(manager, currentUser);
    }

    // creates a label with a fixed width
    private Label createLabel(String text, double width) {
        Label label = new Label(text);
        label.setPrefWidth(width);
        return label;
    }

    // fills the list with the customer's own requests, newest first
    private void loadRequests(SceneManager manager, User currentUser) {

        ArrayList<SaleTransaction> requests =
            manager.getTransactions().getTransactionsByRequester(currentUser.getUsername());

        if (requests.isEmpty()) {
            requestRows.getChildren().add(new Label("You have no requests yet."));
            return;
        }

        Collections.reverse(requests); // the container gives oldest first, this is a copy so it is safe to reverse

        for (SaleTransaction t : requests) {
            addRequestRow(t);
        }
    }

    // creates a row for one request
    private void addRequestRow(SaleTransaction t) {

        HBox row = new HBox(10);
        row.getChildren().addAll(
            createLabel(t.getProductName(), 250),
            createLabel(String.valueOf(t.getQuantity()), 60),
            createLabel(t.getFormattedTotal(), 120),
            createLabel(t.getStatus(), 110),
            createLabel(SaleTransaction.formatTime(t.getRequestedAt()), 170)
        );
        row.setPadding(new Insets(8));
        row.setStyle("-fx-border-color: lightgray;");

        requestRows.getChildren().add(row);
    }
}