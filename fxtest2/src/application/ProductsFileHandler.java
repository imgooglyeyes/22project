package application;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * saves and loads the three product containers (database, inventory, shelf)
 *
 * <p>this class replaces ProductFilesHandler. files live in src/data, and if a
 * file is missing the matching one in src/templatedata is used instead.
 * reading, writing and copying files is done by FileUtil
 *
 * <p>file formats, one product per line, fields separated by |
 * <ul>
 * <li>productsDB.txt: id|name|description|price|madeFrom
 * <li>inventory.txt and shelf.txt: id|stock
 * </ul>
 * madeFrom is a product id, it is empty for a harvest item
 *
 * <p>a null container is never written, so its file is left alone.
 * only problems are printed, a save or a reset that worked is silent
 *
 * @author Jian Carranza, Shawn Alfred Padilla
 * @version 2.3
 * @since 2.0
 * @since 2.1 resetToDefaults was added
 * @since 2.2 the prints of a save and a reset that worked were removed
 * @since 2.3 the file helpers moved to FileUtil
 * @see ProductContainer
 * @see Product
 * @see FileUtil
 */
public class ProductsFileHandler {

	/** save file for the database container */
	public static final String DATABASE		= "fxtest2/src/data/productsDB.txt";
	/** save file for the inventory container */
	public static final String INVENTORY	= "fxtest2/src/data/inventory.txt";
	/** save file for the shelf container */
	public static final String SHELF		= "fxtest2/src/data/shelf.txt";

	/** starting data for the database, used when the save file does not exist */
	public static final String TEMPLATE_DATABASE	= "fxtest2/src/templatedata/productsDB.txt";
	/** starting data for the inventory, used when the save file does not exist */
	public static final String TEMPLATE_INVENTORY	= "fxtest2/src/templatedata/inventory.txt";
	/** starting data for the shelf, used when the save file does not exist */
	public static final String TEMPLATE_SHELF		= "fxtest2/src/templatedata/shelf.txt";

	/**
	 * saves the given containers, a null container is skipped and its file is left alone
	 * <p>this fixes the old save, which emptied every file even when its container was null
	 * <p>an io error is printed and not thrown, so a failed save never crashes the app
	 *
	 * @param database every product and its details, can be null
	 * @param inventoryData products and stock in the inventory, can be null
	 * @param shelfData products and stock on the shelf, can be null
	 */
	public static void saveAll(ProductContainer database, ProductContainer inventoryData, ProductContainer shelfData) {
		try {
			// id|name|description|price|madeFrom
			if (database != null) {
				List<String> lines = new ArrayList<>();
				for (Product p : database.getProducts()) {
					lines.add(
						p.getId() + "|" +
						p.getName() + "|" +
						p.getDescription() + "|" +
						p.getPrice() + "|" +
						(p.getMadeFrom() == null ? "" : p.getMadeFrom()) // empty for a harvest item
					);
				}
				FileUtil.writeLines(DATABASE, lines);
			}

			if (inventoryData != null) FileUtil.writeLines(INVENTORY, stockLines(inventoryData));
			if (shelfData != null) FileUtil.writeLines(SHELF, stockLines(shelfData));
		} catch (IOException e) {
			System.out.println("Something went wrong while saving all product containers.");
			e.printStackTrace();
		}
	}

	/**
	 * loads the database, inventory and shelf
	 * <p>the database is loaded first because the other two look their products up in it
	 *
	 * @return an array of three containers: [0] database, [1] inventory, [2] shelf
	 */
	public static ProductContainer[] loadAll() {
		ProductContainer[] containers = new ProductContainer[3];
		containers[0] = new ProductContainer(); // database
		containers[1] = new ProductContainer(); // inventory
		containers[2] = new ProductContainer(); // shelf

		loadDatabase(containers[0]);
		loadStock(containers[1], containers[0], INVENTORY, TEMPLATE_INVENTORY);
		loadStock(containers[2], containers[0], SHELF, TEMPLATE_SHELF);

		return containers;
	}

	/**
	 * copies the three template files over the three save files
	 * <p>this only changes the files, the caller has to load them again to update the app
	 * <p>transactions.txt is not touched because there is no template for it
	 *
	 * @return true if all three files were copied, false if something went wrong
	 * @since 2.1
	 */
	public static boolean resetToDefaults() {
		try {
			FileUtil.copyFile(TEMPLATE_DATABASE, DATABASE);
			FileUtil.copyFile(TEMPLATE_INVENTORY, INVENTORY);
			FileUtil.copyFile(TEMPLATE_SHELF, SHELF);
			return true;
		} catch (IOException e) {
			System.out.println("Something went wrong while resetting to the template data.");
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * reads productsDB.txt into the database container
	 * <p>old files with 4 fields still load, a missing or empty madeFrom means a harvest item
	 * <p>a bad line is skipped and printed, it does not stop the load
	 *
	 * @param database the empty container to fill
	 */
	private static void loadDatabase(ProductContainer database) {
		for (String line : FileUtil.readLines(DATABASE, TEMPLATE_DATABASE)) {
			// -1 keeps empty fields at the end of the line
			String[] parts = line.split("\\|", -1);

			try {
				Integer madeFrom = null;
				if (parts.length >= 5 && !parts[4].trim().isEmpty()) {
					madeFrom = Integer.parseInt(parts[4].trim());
				}

				// the existing-id constructor keeps ids from changing
				database.add(new Product(
					Integer.parseInt(parts[0]),		// existing_id
					parts[1],						// name
					parts[2],						// desc
					Double.parseDouble(parts[3]),	// price
					0,								// stock (doesn't matter in database)
					madeFrom						// madeFrom
				), 0);
			} catch (RuntimeException e) { // bad number or missing field
				System.out.println("Skipped a bad product line: " + line);
			}
		}
	}

	/**
	 * reads an id|stock file into a container, product details come from the database
	 * <p>a line with an unknown product id is skipped, so a deleted product can't crash the load
	 *
	 * @param target the empty container to fill
	 * @param database the loaded database, used to find each product by id
	 * @param file path of the save file
	 * @param templateFile path of the template used if the save file is missing
	 */
	private static void loadStock(ProductContainer target, ProductContainer database, String file, String templateFile) {
		for (String line : FileUtil.readLines(file, templateFile)) {
			String[] parts = line.split("\\|", -1);

			try {
				Product product = database.getProductById(Integer.parseInt(parts[0]));

				if (product == null) { // the product was deleted from the database
					System.out.println("Skipped a line with an unknown product id: " + line);
					continue;
				}

				target.add(product, Integer.parseInt(parts[1]));
			} catch (RuntimeException e) { // bad number or missing field
				System.out.println("Skipped a bad product line: " + line);
			}
		}
	}

	/**
	 * turns a container into id|stock lines
	 *
	 * @param container the container to read from
	 * @return one id|stock line per product
	 */
	private static List<String> stockLines(ProductContainer container) {
		List<String> lines = new ArrayList<>();
		for (Product p : container.getProducts()) {
			lines.add(p.getId() + "|" + p.getStock());
		}
		return lines;
	}
}