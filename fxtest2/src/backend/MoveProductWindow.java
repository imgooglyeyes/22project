package backend;

import application.Product;
import application.ProductContainer;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

public class MoveProductWindow extends ui.MoveProductWindow {

    public MoveProductWindow(
            Stage stage,
            TableView<Product> table,
            ProductContainer currentDatabase,
            ProductContainer storageDatabase,
            Product targetProduct) {
        super(stage, table, currentDatabase, storageDatabase, targetProduct);
    }
}
