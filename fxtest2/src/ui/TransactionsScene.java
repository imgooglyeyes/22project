package ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import application.FarmSystem;
import application.SaleTransaction;
import application.User;

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

// screen for clerks and managers to see every transaction and confirm or void it
// the rules (stock checks, saving) are in FarmSystem, this screen only asks and shows the result
// FarmSystem returns null when it worked, or an error message that we show in an alert

public class TransactionsScene extends Scene {

    private SceneManager manager;
    private User currentUser;

    private ObjectProperty<SaleTransaction> selectedTransaction =
            new SimpleObjectProperty<>();

    private VBox transactionRows = new VBox(5);

    public TransactionsScene(Stage stage, SceneManager manager, User currentUser) {

        super(new BorderPane(), 1000, 500);

        // loads the shared stylesheet
        Styles.apply(this);

        this.manager = manager;
        this.currentUser = currentUser;

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

        VBox transactionList = new VBox(10, header, transactionRows);
        ScrollPane scrollPane = new ScrollPane(transactionList);
        scrollPane.setFitToWidth(true);

        // creates the buttons
        Button confirmButton = new Button("Confirm");
        Button voidButton = new Button("Void");
        Button quit = new Button("Quit");

        // confirm only works on requested transactions
        confirmButton.disableProperty().bind(Bindings.createBooleanBinding(
            () -> selectedTransaction.get() == null || !selectedTransaction.get().canConfirm(),
            selectedTransaction
        ));

        // void works on anything that is not voided yet
        voidButton.disableProperty().bind(Bindings.createBooleanBinding(
            () -> selectedTransaction.get() == null || !selectedTransaction.get().canVoid(),
            selectedTransaction
        ));

        confirmButton.setOnAction(event -> confirmSelected());
        voidButton.setOnAction(event -> voidSelected());
        quit.setOnAction(event -> Platform.exit());

        root.setTop(manager.createHeader("Transactions"));
        root.setCenter(scrollPane);
        root.setBottom(new HBox(10, confirmButton, voidButton, quit));

        // loads the transactions
        refreshTransactions();
    }

    // creates a label with a fixed width
    private Label createLabel(String text, double width) {
        Label label = new Label(text);
        label.setPrefWidth(width);
        return label;
    }

    // empty text instead of null, for confirmer and voider names
    private String textOrEmpty(String text) {
        return text == null ? "" : text;
    }

    // asks the staff to confirm an action, true if they pressed ok
    private boolean askToProceed(String message) {
        Alert alert = new Alert(AlertType.CONFIRMATION, message);
        alert.setHeaderText(null);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    // shows an error from FarmSystem
    private void showError(String message) {
        Alert alert = new Alert(AlertType.ERROR, message);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    // confirms the selected request, this is when stock leaves the shelf
    private void confirmSelected() {
        SaleTransaction t = selectedTransaction.get();
        if (t == null) return;

        boolean proceed = askToProceed(
            "Confirm " + t.getQuantity() + "x " + t.getProductName() +
            " for " + t.getFormattedTotal() + "?\nThe stock will be taken from the shelf."
        );
        if (!proceed) return;

        FarmSystem farmSystem = manager.getFarmSystem();
        String error = farmSystem.confirm(t, currentUser.getUsername());

        if (error != null) showError(error);
        refreshTransactions();
    }

    // voids the selected transaction
    private void voidSelected() {
        SaleTransaction t = selectedTransaction.get();
        if (t == null) return;

        String message = "Void " + t.getQuantity() + "x " + t.getProductName() + "?";
        if (t.wasConfirmed()) { // tell the staff the stock will go back
            message += "\nThe stock will be returned to the shelf.";
        }
        if (!askToProceed(message)) return;

        FarmSystem farmSystem = manager.getFarmSystem();
        String error = farmSystem.voidTransaction(t, currentUser.getUsername());

        if (error != null) showError(error);
        refreshTransactions();
    }

    // creates a row for one transaction
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

        // selects the transaction when its row is clicked
        row.setOnMouseClicked(event -> {

            selectedTransaction.set(t);

            // resets all row styles
            for (Node node : transactionRows.getChildren()) {
                node.setStyle("-fx-border-color: lightgray;");
            }

            // highlights the selected row
            row.setStyle(
                "-fx-background-color: lightblue;" +
                "-fx-border-color: gray;"
            );
        });

        transactionRows.getChildren().add(row);
    }

    // refreshes the list, newest first
    private void refreshTransactions() {

        transactionRows.getChildren().clear();
        selectedTransaction.set(null);

        // copy the list first, reversing the original would change the saved order
        ArrayList<SaleTransaction> transactions =
            new ArrayList<>(manager.getTransactions().getTransactions());

        if (transactions.isEmpty()) {
            transactionRows.getChildren().add(new Label("There are no transactions yet."));
            return;
        }

        Collections.reverse(transactions);

        for (SaleTransaction t : transactions) {
            addTransactionRow(t);
        }
    }
}