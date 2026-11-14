package backend;

import application.ProductContainer;

public class UniversalDeleter extends ui.UniversalDeleter {

    public UniversalDeleter(
            ProductContainer database,
            ProductContainer inventoryData,
            ProductContainer shelfData) {
        super(database, inventoryData, shelfData);
    }
}
