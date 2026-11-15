package ui;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;

import application.SaleTransaction;

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

// screen for clerks and managers to see the cash balance and every cash entry
// there is no cash file or model, the entries are worked out from the transactions
// a confirmed sale is money in, a voided confirmed sale is money out
// a request voided before it was confirmed never moved any money, so it has no entry
// the scene is created again every time it opens, so it is always up to date

public class CashScene extends Scene {

    // one cash movement, only used inside this screen
    private static class CashEntry {
        LocalDateTime time;
        String type; // "Sale" or "Void"
        SaleTransaction transaction;
        String staff; // who confirmed or voided it
        double amount; // positive for money in, negative for money out

        CashEntry(LocalDateTime time, String type, SaleTransaction transaction, String staff, double amount) {
            this.time = time;
            this.type = type;
            this.transaction = transaction;
            this.staff = staff;
            this.amount = amount;
        }
    }

    private VBox entryRows = new VBox(5);

    public CashScene(Stage stage, SceneManager manager) {

        super(new BorderPane(), 900, 500);

        // loads the shared stylesheet
        Styles.apply(this);

        BorderPane root = (BorderPane) getRoot();
        root.setPadding(new Insets(12));

        ArrayList<CashEntry> entries = buildEntries(manager);

        // the balance is the total of all entries
        double balance = 0;
        for (CashEntry entry : entries) {
            balance += entry.amount;
        }

        Label balanceLabel = new Label("Cash balance: " + formatMoney(balance));
        balanceLabel.setStyle("-fx-font-size: 20;");

        // creates the column headers
        HBox header = new HBox(10);
        header.setPadding(new Insets(0, 8, 0, 8)); // lines up with the row padding
        header.getChildren().addAll(
            createLabel("Time", 140),
            createLabel("Type", 60),
            createLabel("Product", 230),
            createLabel("Qty", 50),
            createLabel("Txn", 50),
            createLabel("By", 100),
            createLabel("Amount", 120)
        );

        // shows the entries, newest first
        if (entries.isEmpty()) {
            entryRows.getChildren().add(new Label("There are no cash entries yet."));
        }
        for (CashEntry entry : entries) {
            addEntryRow(entry);
        }

        VBox entryList = new VBox(10, header, entryRows);
        ScrollPane scrollPane = new ScrollPane(entryList);
        scrollPane.setFitToWidth(true);

        // balance on top of the list
        VBox center = new VBox(10, balanceLabel, scrollPane);

        Button quit = new Button("Quit");
        quit.setOnAction(event -> Platform.exit());

        root.setTop(manager.createHeader("Cash"));
        root.setCenter(center);
        root.setBottom(new HBox(10, quit));
    }

    // works out the cash entries from the transactions, newest first
    private ArrayList<CashEntry> buildEntries(SceneManager manager) {

        ArrayList<CashEntry> entries = new ArrayList<>();

        for (SaleTransaction t : manager.getTransactions().getTransactions()) {

            if (!t.wasConfirmed()) continue; // never confirmed, no money moved

            // money in when it was confirmed
            entries.add(new CashEntry(
                timeOrFallback(t.getConfirmedAt(), t),
                "Sale", t, t.getConfirmedBy(), t.getTotal()
            ));

            // money out when a confirmed sale was voided
            if (t.getStatus().equals(SaleTransaction.VOIDED)) {
                entries.add(new CashEntry(
                    timeOrFallback(t.getVoidedAt(), t),
                    "Void", t, t.getVoidedBy(), -t.getTotal()
                ));
            }
        }

        entries.sort(Comparator.comparing((CashEntry e) -> e.time).reversed());
        return entries;
    }

    // uses the request time if a saved time is missing (for example a hand edited file)
    private LocalDateTime timeOrFallback(LocalDateTime time, SaleTransaction t) {
        return time != null ? time : t.getRequestedAt();
    }

    // money as text, example: ₱150.00 or -₱150.00
    private String formatMoney(double amount) {
        String text = String.format("\u20B1%.2f", Math.abs(amount));
        return amount < 0 ? "-" + text : text;
    }

    // creates a label with a fixed width
    private Label createLabel(String text, double width) {
        Label label = new Label(text);
        label.setPrefWidth(width);
        return label;
    }

    // creates a row for one cash entry
    private void addEntryRow(CashEntry entry) {

        HBox row = new HBox(10);
        row.getChildren().addAll(
            createLabel(SaleTransaction.formatTime(entry.time), 140),
            createLabel(entry.type, 60),
            createLabel(entry.transaction.getProductName(), 230),
            createLabel(String.valueOf(entry.transaction.getQuantity()), 50),
            createLabel(String.valueOf(entry.transaction.getId()), 50),
            createLabel(entry.staff == null ? "" : entry.staff, 100),
            createLabel(formatMoney(entry.amount), 120)
        );
        row.setPadding(new Insets(8));
        row.setStyle("-fx-border-color: lightgray;");

        entryRows.getChildren().add(row);
    }
}