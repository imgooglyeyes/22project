package backend;

import application.Product;
import application.ProductContainer;
import javafx.stage.Stage;

public class ProductEditingWindow extends ui.ProductEditingWindow1 {

    public ProductEditingWindow(
            Stage stage,
            ProductContainer database,
            ProductContainer inventoryData,
            ProductContainer shelfData,
            Product targetProduct,
            Runnable refreshDatabase) {
        super(stage, database, inventoryData, shelfData, targetProduct, refreshDatabase);
    }
}
