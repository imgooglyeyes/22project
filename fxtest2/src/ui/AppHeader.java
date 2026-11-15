
package ui;

import application.User;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class AppHeader extends HBox {

    public AppHeader(SceneManager manager, String currentScene, User user) {

        setSpacing(10);
        setPadding(new Insets(10));

        Label title = new Label("Inventory System");

        Button shelfButton = new Button("Shelf");
        Button inventoryButton = new Button("Inventory");
        Button transactionsButton = new Button("Transactions");
        Button databaseButton = new Button("Database");
        Button myRequestsButton = new Button("My Requests");

        // opens the selected scene
        shelfButton.setOnAction(e -> manager.showShelf());
        inventoryButton.setOnAction(e -> manager.showInventory());
        transactionsButton.setOnAction(e -> manager.showTransactions());
        databaseButton.setOnAction(e -> manager.showDatabase());
        myRequestsButton.setOnAction(e -> manager.showMyRequests());

        // highlights the current scene
        setActive(shelfButton, currentScene.equals("Shelf"));
        setActive(inventoryButton, currentScene.equals("Inventory"));
        setActive(transactionsButton, currentScene.equals("Transactions"));
        setActive(databaseButton, currentScene.equals("Database"));
        setActive(myRequestsButton, currentScene.equals("My Requests"));

        // checks which buttons the user can access
        boolean isManager = user.getUserType().equals(User.MANAGER);
        boolean isClerk = user.getUserType().equals(User.CLERK);
        boolean isCustomer = user.getUserType().equals(User.CUSTOMER);

        getChildren().add(title);
        getChildren().add(shelfButton);

        // customers can see their own requests
        if (isCustomer) {
            getChildren().add(myRequestsButton);
        }

        if (isManager || isClerk) {
            getChildren().add(inventoryButton);
            getChildren().add(transactionsButton);
        }

        if (isManager) {
            getChildren().add(databaseButton);
        }
    }

    // highlights the active button
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