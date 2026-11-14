package backend;

import application.Product;
import application.ProductContainer;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

public class ProductCreationWindow extends ui.ProductCreationWindow {

    public ProductCreationWindow(
            Stage stage,
            ProductContainer database,
            TableView<Product> table) {
        super(stage, database, table);
    }
}
