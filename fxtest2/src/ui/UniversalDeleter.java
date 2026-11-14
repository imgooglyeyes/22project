package ui;

import application.Product;
import application.ProductContainer;

public class UniversalDeleter {

    private ProductContainer database;
    private ProductContainer inventoryData;
    private ProductContainer shelfData;

    public UniversalDeleter(
            ProductContainer database,
            ProductContainer inventoryData,
            ProductContainer shelfData) {

        this.database = database;
        this.inventoryData = inventoryData;
        this.shelfData = shelfData;
    }

    // deletes every instance of the product from the database, inventory, and shelf
    public void delete(Product target) {

        ProductContainer[] containers = { database, inventoryData, shelfData };

        for (ProductContainer container : containers) {

            // finds the product in this container
            Product found = container.getProductById(target.getId());

            // skips containers that do not have the product
            if (found == null) continue;

            // removes all of its stock, which also removes it from the list
            container.remove(found, found.getStock());
        }
    }
}   