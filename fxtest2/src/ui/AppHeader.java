
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
        Button databaseButton = new Button("Database");

        // opens the selected scene
        shelfButton.setOnAction(e -> manager.showShelf());
        inventoryButton.setOnAction(e -> manager.showInventory());
        databaseButton.setOnAction(e -> manager.showDatabase());

        // highlights the current scene
        setActive(shelfButton, currentScene.equals("Shelf"));
        setActive(inventoryButton, currentScene.equals("Inventory"));
        setActive(databaseButton, currentScene.equals("Database"));

        // checks which buttons the user can access
        boolean isManager = user.getUserType().equals(User.MANAGER);
        boolean isClerk = user.getUserType().equals(User.CLERK);

        getChildren().add(title);
        getChildren().add(shelfButton);

        if (isManager || isClerk) {
            getChildren().add(inventoryButton);
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
