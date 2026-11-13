package ui;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class AppHeader extends HBox {

    public AppHeader(SceneManager manager, String currentScene) {

        setSpacing(10);
        setPadding(new Insets(10));

        Label title = new Label("Inventory System");

        Button shelf = new Button("Shelf");
        Button inventory = new Button("Inventory");
        Button database = new Button("Database");

        // connects the navigation buttons
        shelf.setOnAction(event -> manager.showShelf());
        inventory.setOnAction(event -> manager.showInventory());
        database.setOnAction(event -> manager.showDatabase());

        // highlights the current scene
        setActive(shelf, currentScene.equals("Shelf"));
        setActive(inventory, currentScene.equals("Inventory"));
        setActive(database, currentScene.equals("Database"));

        getChildren().addAll(title, shelf, inventory, database);
    }

    // highlights the active navigation button
    private void setActive(Button button, boolean active) {

        if (active) {
            button.setStyle(
                    "-fx-background-color: lightblue;" +
                    "-fx-font-weight: bold;"
            );
        }
    }
}