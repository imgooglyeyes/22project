package ui;

import application.User;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

/**
 * the shared header with the navigation buttons shown at the top of every scene
 *
 * <p>which buttons appear is decided by the can... methods of SceneManager,
 * so the header and the scenes always follow the same permission table.
 * the button of the open scene is highlighted and disabled
 *
 * <p>a logged in user gets a dashboard button where the title label would be,
 * this keeps the manager header from growing wider than the window.
 * a guest still sees the title label because a guest has no dashboard
 *
 * @author Jian Carranza, Shawn Alfred Padilla
 * @version 1.4
 * @see SceneManager
 */
public class AppHeader extends HBox {

    /**
     * creates the header with the buttons that fit the user
     *
     * @param manager the scene manager, opens the scenes and answers the role checks
     * @param currentScene the name of the open scene, its button is highlighted
     * @param user the user who is logged in
     */
    public AppHeader(SceneManager manager, String currentScene, User user) {

        setSpacing(10);
        setPadding(new Insets(10));

        Label title = new Label("Inventory System");

        Button dashboardButton = new Button("Dashboard");
        Button shelfButton = new Button("Shelf");
        Button inventoryButton = new Button("Inventory");
        Button transactionsButton = new Button("Transactions");
        Button historyButton = new Button("History");
        Button cashButton = new Button("Cash");
        Button staffListButton = new Button("Staff List");
        Button signUpRequestsButton = new Button("Sign-Ups"); // short label so the manager header still fits
        Button databaseButton = new Button("Database");
        Button loginButton = new Button("Log In");
        Button signUpButton = new Button("Sign Up");
        Button myRequestsButton = new Button("My Requests");

        // opens the selected scene
        dashboardButton.setOnAction(e -> manager.showDashboard());
        shelfButton.setOnAction(e -> manager.showShelf());
        loginButton.setOnAction(e -> manager.showLogin());
        signUpButton.setOnAction(e -> manager.showSignUp());
        inventoryButton.setOnAction(e -> manager.showInventory());
        transactionsButton.setOnAction(e -> manager.showTransactions());
        historyButton.setOnAction(e -> manager.showHistory(user)); // by default, it should show the currentUser's history
        cashButton.setOnAction(e -> manager.showCash());
        databaseButton.setOnAction(e -> manager.showDatabase());
        staffListButton.setOnAction(e -> manager.showStaffList());
        signUpRequestsButton.setOnAction(e -> manager.showSignUpRequests());
        myRequestsButton.setOnAction(e -> manager.showMyRequests());

        // highlights the current scene
        setActive(dashboardButton, currentScene.equals("Dashboard"));
        setActive(shelfButton, currentScene.equals("Shelf"));
        setActive(loginButton, currentScene.equals("Log In"));
        setActive(signUpButton, currentScene.equals("Sign Up"));
        setActive(inventoryButton, currentScene.equals("Inventory"));
        setActive(transactionsButton, currentScene.equals("Transactions"));
        setActive(historyButton, currentScene.equals("History"));
        setActive(cashButton, currentScene.equals("Cash"));
        setActive(databaseButton, currentScene.equals("Database"));
        setActive(staffListButton, currentScene.equals("Staff List"));
        setActive(signUpRequestsButton, currentScene.equals("Sign Up Requests"));
        setActive(myRequestsButton, currentScene.equals("My Requests"));

        // only guests have no account yet, so only they see log in and sign up
        boolean isGuest = user.getUserType().equals(User.GUEST);

        // the dashboard button takes the place of the title for logged in users
        if (manager.canViewDashboard()) {
            getChildren().add(dashboardButton);
        } else {
            getChildren().add(title);
        }
        getChildren().add(shelfButton);

        if (isGuest) {
            getChildren().add(loginButton);
            getChildren().add(signUpButton);
        }

        // every other button follows the permission table in SceneManager
        if (manager.canViewMyRequests()) {
            getChildren().add(myRequestsButton);
        }

        if (manager.canViewInventory()) {
            getChildren().add(inventoryButton);
        }

        if (manager.canHandleTransactions()) {
            getChildren().add(transactionsButton);
        }

        // the history button opens the user's own history, which every staff member may see
        if (manager.canViewHistoryOf(user)) {
            getChildren().add(historyButton);
        }

        if (manager.canViewCash()) {
            getChildren().add(cashButton);
        }

        if (manager.canViewStaffList()) {
            getChildren().add(staffListButton);
        }

        if (manager.canApproveSignUps()) {
            getChildren().add(signUpRequestsButton);
        }

        if (manager.canManageDatabase()) {
            getChildren().add(databaseButton);
        }
    }

    /**
     * highlights the active button and disables it
     *
     * @param button the button to check
     * @param active true if its scene is the open one
     */
    private void setActive(Button button, boolean active) {

        if (active) {
            button.setStyle(
                "-fx-background-color: lightblue;" +
                "-fx-font-weight: bold;"
            );
            button.setDisable(true);
        }
    }
}