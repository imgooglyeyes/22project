package ui;

import java.util.ArrayList;
import java.util.Collections;

import application.ProductsFileHandler;
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


/**
 * screen where a clerk sees only transaction requests that they took action with.
 * <p>a manager can also use this screen to look into the history of other clerks.
 * 
 * <p>it is read only, managers and clerks can't confirm or void anything from here.
 * <p>the scene is created again every time it opens, so the list is always up to date.
 *
 * @author Jian Carranza
 * @version 1.0
 * @see SceneManager
 */
public class HistoryScene extends Scene {

    private VBox transactionRows = new VBox(5);

    public HistoryScene(Stage stage, SceneManager manager, User currentUser, User targetUser) {

        super(new BorderPane(), 1000, 500);

        // loads the shared stylesheet
        Styles.apply(this);

        BorderPane root = (BorderPane) getRoot();
        root.setPadding(new Insets(12));

     // creates the column headers
        HBox header = new HBox(10);
        header.setPadding(new Insets(0, 8, 0, 8)); // lines up with the row padding
        header.getChildren().addAll(
            createLabel("ID", 35),
            createLabel("Product", 170),
            createLabel("Qty", 40),
            createLabel("Total", 90),
            createLabel("Requester", 90),
            createLabel("Status", 90),
            createLabel("Requested", 140),
            createLabel("Confirmed by", 90),
            createLabel("Voided by", 90)
        );

        VBox requestList = new VBox(10, header, transactionRows);
        ScrollPane scrollPane = new ScrollPane(requestList);
        scrollPane.setFitToWidth(true);

        Button quit = new Button("Quit");
        Button logout = new Button("Log Out");
        
        logout.setOnAction(event -> {
        	currentUser.logOut();
        	manager.showShelf();
        });
        quit.setOnAction(event -> Platform.exit());

        root.setTop(manager.createHeader("History"));
        root.setCenter(scrollPane);
        root.setBottom(new HBox(10, logout, quit));

        // loads this customer's requests
        loadRequests(manager, currentUser, targetUser);
    }

    // creates a label with a fixed width
    private Label createLabel(String text, double width) {
        Label label = new Label(text);
        label.setPrefWidth(width);
        return label;
    }

    // fills the list with the customer's own requests, newest first
    private void loadRequests(SceneManager manager, User currentUser, User targetUser) {

        ArrayList<SaleTransaction> requests =
            manager.getTransactions().getTransactionsByActor(targetUser.getUsername());

        if (requests.isEmpty()) { // change the display if the current and target user are the same
            if (currentUser.equals(targetUser)) transactionRows.getChildren().add(new Label("You have taken no requests yet."));
            else transactionRows.getChildren().add(new Label(targetUser.getUsername() + " has taken no requests yet."));
            return;
        }

        Collections.reverse(requests); // the container gives oldest first, this is a copy so it is safe to reverse

        for (SaleTransaction t : requests) {
            addTransactionRow(t);
        }
    }

    // empty text instead of null, for confirmer and voider names
    private String textOrEmpty(String text) {
        return text == null ? "" : text;
    }
    
    // creates a row for one request
    private void addTransactionRow(SaleTransaction t) {

        HBox row = new HBox(10);
        row.getChildren().addAll(
            createLabel(String.valueOf(t.getId()), 35),
            createLabel(t.getProductName(), 170),
            createLabel(String.valueOf(t.getQuantity()), 40),
            createLabel(t.getFormattedTotal(), 90),
            createLabel(t.getRequester(), 90),
            createLabel(t.getStatus(), 90),
            createLabel(SaleTransaction.formatTime(t.getRequestedAt()), 140),
            createLabel(textOrEmpty(t.getConfirmedBy()), 90),
            createLabel(textOrEmpty(t.getVoidedBy()), 90)
        );
        row.setPadding(new Insets(8));
        row.setStyle("-fx-border-color: lightgray;");

        transactionRows.getChildren().add(row);
    }
}