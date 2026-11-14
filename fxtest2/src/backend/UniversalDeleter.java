package backend;

import application.Product;
import application.ProductContainer;
// CHANGED: TABLEVIEW IMPORT REMOVED. THE NEW SCREENS USE ROWS, NOT A TABLEVIEW, SO THIS WINDOW NO LONGER NEEDS ONE.

public class UniversalDeleter {
	// CHANGED: THE TableView table FIELD IS REMOVED. THE SCREEN REFRESHES ITSELF AFTER CALLING delete().
	ProductContainer database;
	ProductContainer inventoryData;
	ProductContainer shelfData;


	public UniversalDeleter(ProductContainer database, ProductContainer inventoryData, ProductContainer shelfData) {
		this.database = database;
		this.inventoryData = inventoryData;
		this.shelfData = shelfData;
	}

	public void delete(Product targetProduct) { // deletes all instances of the selected item
		ProductContainer[] databases = new ProductContainer[3]; // 3 because that's how many databases we have
		databases[0] = database;
		databases[1] = inventoryData;
		databases[2] = shelfData;

		for (ProductContainer db: databases) {
			Product removeTarget = db.getProductById(targetProduct.getId()); // find the target product in that database
			if(removeTarget == null) continue; // can't remove null
			db.remove(removeTarget, removeTarget.getStock()); // remove all stocks
		}

		// CHANGED: table.setItems(...) AND table.refresh() REMOVED. WHY: THERE IS NO TABLE. THE SCREEN CALLS ITS OWN REFRESH AFTER delete().
	}
}
