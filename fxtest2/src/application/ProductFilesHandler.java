package application;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ProductFilesHandler {
	
	// different save files for the three different, main ProductContainers
	public static final String DATABASE		= "src/data/productsDB.txt";
	public static final String INVENTORY	= "src/data/inventory.txt";
	public static final String SHELF		= "src/data/shelf.txt";
	
	// these are save files that were originally hardcoded as starting data
	public static final String TEMPLATE_DATABASE		= "src/templatedata/productsDB.txt";
	public static final String TEMPLATE_INVENTORY		= "src/templatedata/inventory.txt";
	public static final String TEMPLATE_SHELF			= "src/templatedata/shelf.txt";

	
	public static void save(ProductContainer database, ProductContainer inventoryData, ProductContainer shelfData) {
		try (
	            BufferedWriter databaseWriter =
	                Files.newBufferedWriter(Paths.get(DATABASE));
				BufferedWriter inventoryWriter =
		                Files.newBufferedWriter(Paths.get(INVENTORY));
				BufferedWriter shelfWriter =
	                Files.newBufferedWriter(Paths.get(SHELF));
	            
	        ) {
				// id|name|description|price
	            for (Product p : database.getProducts()) {
	                databaseWriter.write(
	                    p.getId() + "|" +
	                    p.getName() + "|" +
	                    p.getDescription() + "|" +
	                    p.getPrice()
	                );
	                databaseWriter.newLine();
	            }
	
	            // id|stock
	            for (Product p : inventoryData.getProducts()) {
	                inventoryWriter.write(p.getId() + "|" + p.getStock());
	                inventoryWriter.newLine();
	            }
	            
	            // id|stock
	            for (Product p : shelfData.getProducts()) {
	                shelfWriter.write(p.getId() + "|" + p.getStock());
	                shelfWriter.newLine();
	            }
	
	            System.out.println("Saving complete.");
	        } catch (IOException e) {
	            System.out.println("Something went wrong while saving.");
	            e.printStackTrace();
	        }
	}
	
	public static ProductContainer[] loadAll() {
        ProductContainer[] containers = new ProductContainer[3];
        
        containers[0] = new ProductContainer(); // database
        containers[1] = new ProductContainer(); // inventory
        containers[2] = new ProductContainer(); // shelf
        
        Path dataPath = Paths.get(DATABASE);
        Path inventoryPath = Paths.get(INVENTORY);
        Path shelfPath = Paths.get(SHELF);

        // if they don't exist, just grab the templates which SHOULD EXIST
        if (!Files.exists(dataPath)) dataPath = Paths.get(TEMPLATE_DATABASE);
        if (!Files.exists(inventoryPath)) inventoryPath = Paths.get(TEMPLATE_INVENTORY);
        if (!Files.exists(shelfPath)) shelfPath = Paths.get(TEMPLATE_SHELF);

        
        // start by loading the main database (containers[0])
        try (BufferedReader reader = Files.newBufferedReader(dataPath)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue; // skip blank lines

                // -1 keeps empty fields at the end of the line
                String[] parts = line.split("\\|", -1);

                // id|name|description|price
                try {
                    containers[0].add(new Product(
                        Integer.parseInt(parts[0]),		// existing_id
                        parts[1],						// name
                        parts[2],						// desc
                        Double.parseDouble(parts[3]),	// price
                        0								// stock (doesn't matter in database)
                    ), 0);
                } catch (RuntimeException e) {
                    System.out.println("Skipped a bad product line: " + line);
                }
            }
        } catch (IOException e) {
            System.out.println("Something went wrong while loading all product containers.");
            e.printStackTrace();
        }
        
        // THIS COULD BE REFINED INTO A FOR LOOP
        // RIGHT NOW, I'M FEELING LAZY
        // - JIAN
        
        // load inventory (containers[1])
        try (BufferedReader reader = Files.newBufferedReader(inventoryPath)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue; // skip blank lines

                // -1 keeps empty fields at the end of the line
                String[] parts = line.split("\\|", -1);

                // id|stock
                try {
                    containers[1].add(
                        containers[0].getProductById(Integer.parseInt(parts[0])) // get product from the database based on id
                    ,Integer.parseInt(parts[1])); // add specified stocks
                } catch (RuntimeException e) {
                    System.out.println("Skipped a bad product line: " + line);
                }
            }
        } catch (IOException e) {
            System.out.println("Something went wrong while loading all product containers.");
            e.printStackTrace();
        }
        
        // load shelf (containers[2])
        try (BufferedReader reader = Files.newBufferedReader(shelfPath)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue; // skip blank lines

                // -1 keeps empty fields at the end of the line
                String[] parts = line.split("\\|", -1);

                // id|stock
                try {
                    containers[2].add(
                        containers[0].getProductById(Integer.parseInt(parts[0])) // get product from the database based on id
                    ,Integer.parseInt(parts[1])); // add specified stocks
                } catch (RuntimeException e) {
                    System.out.println("Skipped a bad product line: " + line);
                }
            }
        } catch (IOException e) {
            System.out.println("Something went wrong while loading all product containers.");
            e.printStackTrace();
        }

        return containers;
    }
}
