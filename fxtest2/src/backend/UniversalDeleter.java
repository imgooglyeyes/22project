package backend;

import application.Product;
import application.ProductContainer;
import javafx.scene.control.TableView;

public class UniversalDeleter {
	TableView<Product> table;
	ProductContainer database;
	ProductContainer inventoryData;
	ProductContainer shelfData;
	
	
	public UniversalDeleter(TableView<Product> table, ProductContainer database, ProductContainer inventoryData, ProductContainer shelfData) {
		this.table = table;
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
		
		table.setItems(database.getObservableList()); // update table
		table.refresh();
	}
}
