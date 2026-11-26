package ui;

import application.FarmSystem;
import application.Product;

/**
 * the table of products, used by the inventory page and the products page
 *
 * <p>the inventory view shows the stock, the database view shows the id and what the product is made from.
 * a product with no source is shown as harvest
 *
 * @author Shawn Alfred Padilla
 * @version 1.0
 * @see DataTable
 * @see InventoryPage
 * @see ProductsPage
 */
public class ProductTable extends DataTable<Product> {

    /**
     * creates the table with its columns
     *
     * @param farmSystem looks up the name of the source product
     * @param databaseView true for the database columns (id and made from), false for the inventory columns (stock)
     */
    public ProductTable(FarmSystem farmSystem, boolean databaseView) {
        if (databaseView) addColumn("ID", Product::getId, 50);

        addColumn("Name", Product::getName, 200);
        addColumn("Description", Product::getDescription, 380);
        addMoneyColumn("Price", Product::getPrice, 90);

        if (databaseView) {
            addColumn("Made From", p -> {
                String source = farmSystem.getMadeFromName(p);
                if (!p.isMade()) return "Harvest";
                return source == null ? "Unknown" : source;
            }, 160);
        } else {
            addColumn("Stock", Product::getStock, 70);
        }
    }
}