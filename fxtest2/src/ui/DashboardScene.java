package ui;

import application.ProductContainer;
import application.SaleTransaction;
import application.TransactionContainer;
import application.User;
import application.UserContainer;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

/**
 * the home screen of a logged in user, it shows one tile for every screen the user may open
 *
 * <p>which tiles appear follows the can... methods of SceneManager, so the dashboard,
 * the header and the show... methods always use the same permission table.
 * every tile shows a short live number (for example how many requests are waiting)
 * and opens its screen when it is clicked
 *
 * <p>this screen only reads the data, it never changes anything.
 * the scene is created again every time it opens, so the numbers are always up to date
 *
 * @author Jian Carranza, Shawn Alfred Padilla
 * @version 1.0
 * @see SceneManager#showDashboard()
 * @see SceneManager#canViewDashboard()
 */
public class DashboardScene extends Scene {

    /** every sale transaction, read to count requests and to work out the cash balance */
    private TransactionContainer transactionData;

    /** the tiles, they wrap to the next line when the window is too narrow */
    private FlowPane tiles = new FlowPane(15, 15);

    /**
     * creates the dashboard with the tiles that fit the user
     *
     * @param stage the main window
     * @param manager the scene manager, used for the header, the role checks and to open the other scenes
     * @param database every product and its details
     * @param inventoryData the products and stock in the inventory
     * @param shelfData the products and stock on the shelf
     * @param userData every user and their details
     * @param currentUser the user who is logged in
     */
    public DashboardScene(
            Stage stage,
            SceneManager manager,
            ProductContainer database,
            ProductContainer inventoryData,
            ProductContainer shelfData,
            UserContainer userData,
            User currentUser) {

        super(new BorderPane(), 1000, 500);

        // loads the shared stylesheet
        Styles.apply(this);

        this.transactionData = manager.getTransactions();

        BorderPane root = (BorderPane) getRoot();
        root.setPadding(new Insets(12));

        Label welcome = new Label(
            "Welcome, " + currentUser.getFirstName() + " " + currentUser.getLastName() +
            " (" + currentUser.getUserType() + ")"
        );
        welcome.setStyle("-fx-font-size: 20;");

        tiles.setPadding(new Insets(10, 0, 0, 0));

        // each tile is added only if the permission table allows it for this user
        // the shelf is open to everyone
        addTile("Shelf",
            shelfData.getProducts().size() + " products on the shelf",
            manager::showShelf);

        if (manager.canViewMyRequests()) {
            int total = transactionData.getTransactionsByRequester(currentUser.getUsername()).size();
            int waiting = countWaitingRequests(currentUser.getUsername());
            addTile("My Requests",
                total + " requests, " + waiting + " waiting",
                manager::showMyRequests);
        }

        if (manager.canHandleTransactions()) {
            addTile("Transactions",
                countWaitingRequests(null) + " waiting for confirmation",
                manager::showTransactions);
        }

        if (manager.canViewInventory()) {
            addTile("Inventory",
                inventoryData.getProducts().size() + " products in stock",
                manager::showInventory);
        }

        if (manager.canViewCash()) {
            addTile("Cash",
                "Balance: " + formatMoney(calculateBalance()),
                manager::showCash);
        }

        if (manager.canViewHistoryOf(currentUser)) {
            int actions = transactionData.getTransactionsByActor(currentUser.getUsername()).size();
            addTile("History",
                actions + " requests taken by you",
                () -> manager.showHistory(currentUser));
        }

        if (manager.canViewStaffList()) {
            int staff = userData.getUsersByType(User.MANAGER).size() + userData.getUsersByType(User.CLERK).size();
            addTile("Staff List",
                staff + " staff members",
                manager::showStaffList);
        }

        if (manager.canApproveSignUps()) {
            addTile("Sign-Ups",
                countPendingSignUps(userData) + " waiting for approval",
                manager::showSignUpRequests);
        }

        if (manager.canManageDatabase()) {
            addTile("Database",
                database.getProducts().size() + " products",
                manager::showDatabase);
        }

        ScrollPane scrollPane = new ScrollPane(tiles);
        scrollPane.setFitToWidth(true); // lets the flow pane wrap instead of scrolling sideways

        VBox center = new VBox(10, welcome, scrollPane);

        Button logout = new Button("Log Out");
        Button quit = new Button("Quit");

        logout.setOnAction(event -> {
            currentUser.logOut();
            manager.showShelf();
        });
        quit.setOnAction(event -> Platform.exit());

        root.setTop(manager.createHeader("Dashboard"));
        root.setCenter(center);
        root.setBottom(new HBox(10, logout, quit));
    }

    /**
     * creates one tile and adds it to the dashboard
     * <p>a tile is a button so it uses the shared stylesheet, the title is on the first line and the live info under it
     *
     * @param title the name of the screen the tile opens
     * @param info a short live number or sentence shown under the title
     * @param action what happens when the tile is clicked, usually a show... method of SceneManager
     */
    private void addTile(String title, String info, Runnable action) {
        Button tile = new Button(title + "\n" + info);
        tile.setPrefSize(220, 100);
        tile.setWrapText(true);
        tile.setTextAlignment(TextAlignment.CENTER);
        tile.setOnAction(event -> action.run());

        tiles.getChildren().add(tile);
    }

    /**
     * counts the requests that still wait for a clerk
     *
     * @param requester only count the requests of this customer, null counts everyone's
     * @return how many transactions still have the requested status
     */
    private int countWaitingRequests(String requester) {
        int count = 0;

        for (SaleTransaction t : transactionData.getTransactions()) {
            boolean wanted = requester == null || t.getRequester().equals(requester);
            if (wanted && t.getStatus().equals(SaleTransaction.REQUESTED)) count++;
        }
        return count;
    }

    /**
     * counts the accounts that wait for a manager to approve them
     *
     * @param userData every user and their details
     * @return how many users have the pending status
     */
    private int countPendingSignUps(UserContainer userData) {
        int count = 0;

        for (User u : userData.getUsers()) {
            if (User.PENDING.equals(u.getStatus())) count++;
        }
        return count;
    }

    /**
     * works out the cash balance from the transactions
     * <p>a confirmed sale is money in and a voided confirmed sale cancels it out,
     * so the balance is the total of every transaction that is confirmed right now.
     * this gives the same number as the cash screen without building its entry list
     *
     * @return the current cash balance
     */
    private double calculateBalance() {
        double balance = 0;

        for (SaleTransaction t : transactionData.getTransactions()) {
            if (t.getStatus().equals(SaleTransaction.CONFIRMED)) balance += t.getTotal();
        }
        return balance;
    }

    /**
     * formats money as text, example: ₱150.00
     * <p>\u20B1 is the peso sign, written as an escape so the file encoding can't break it
     *
     * @param amount the amount to format
     * @return the amount with the peso sign and two decimals
     */
    private String formatMoney(double amount) {
        return String.format("\u20B1%.2f", amount);
    }
}