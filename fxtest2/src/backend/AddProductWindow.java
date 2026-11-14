package backend;

import application.Product;
import application.ProductContainer;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

public class AddProductWindow extends ui.AddProductWindow {

    public AddProductWindow(
            Stage stage,
            TableView<Product> table,
            ProductContainer targetDatabase,
            ProductContainer sourceDatabase,
            Boolean limited) {
        super(stage, table, targetDatabase, sourceDatabase, limited);
    }
}
