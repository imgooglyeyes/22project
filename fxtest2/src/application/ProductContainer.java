package application;

import java.util.ArrayList;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * holds a list of products, used for the database, the inventory and the shelf
 *
 * <p>adding a product makes a copy, so each container has its own stock.
 * the copy keeps madeFrom so it is not lost in the inventory and shelf.
 * replaceWith swaps the whole content, so screens keep using the same container object
 *
 * @author Jian Carranza, Shawn Alfred Padilla
 * @version 1.1
 * @see Product
 */
public class ProductContainer {
	/** every product in this container */
	private ArrayList<Product> productList;

	/** creates an empty container */
	public ProductContainer() {
		this.productList = new ArrayList<>();
	}

	/**
	 * creates a container with a starting list
	 *
	 * @param p the starting products, the list is copied
	 */
	public ProductContainer(ArrayList<Product> p) {
		this.productList = new ArrayList<>(p);
	}

	/**
	 * adds stock of a product, if the product is already here the stock is stacked
	 * <p>a new product is added as a copy so containers don't share products
	 *
	 * @param product the product to add
	 * @param stocks how many to add
	 */
	public void add(Product product, int stocks) {
		System.out.println("Attempting to add " + stocks + "x " + product.getName());
		Product p = getProductById(product.getId()); // find the product in this database

		if (p == null) { // doesn't exist yet, just add it normally
			// the copy keeps madeFrom so it is not lost in the inventory and shelf
			productList.add(new Product(product.getId(), product.getName(), product.getDescription(), product.getPrice(), stocks, product.getMadeFrom()));
			System.out.println("Added " + stocks + "x " + product.getName());
			return;
		}
		p.setStock(p.getStock() + stocks); // already exists, stack the stocks
		System.out.println("Added "+ stocks + "x more " + p.getName() + " (Total in stock: " + p.getStock() + ")");
	}

	/**
	 * removes stock of a product, the product is removed when its stock reaches 0
	 * <p>nothing happens if the product is not in this container
	 *
	 * @param product the product to remove stock from
	 * @param stocks how many to remove
	 */
	public void remove(Product product, int stocks) {
		Product p = getProductById(product.getId()); // find the product in this database
		if (p == null) return; // doesn't exist
		p.setStock(p.getStock() - stocks);
		if (p.getStock() == 0) productList.remove(p); // nothing left, just remove it
	}

	/**
	 * throws away everything in this container and copies everything from the other one
	 * <p>the container object stays the same, so anything holding it sees the new content
	 *
	 * @param other the container to copy from, it is not changed
	 * @since 1.1
	 */
	public void replaceWith(ProductContainer other) {
		productList.clear();
		for (Product p : other.getProducts()) {
			add(p, p.getStock()); // add makes a copy, so the two containers don't share products
		}
	}

	/** @return a new observable list with the same products */
	public ObservableList<Product> getObservableList() {
		return FXCollections.observableArrayList(productList);
	}

	/** @return the real list of products, changing it changes the container */
	public ArrayList<Product> getProducts(){
		return productList;
	}

	/**
	 * finds a product by its id
	 *
	 * @param id the id to look for
	 * @return the product, or null if it is not here
	 */
	public Product getProductById(int id) {
		for (Product p : productList) { // go through every product
			if (p.getId() == id) {return p;} // does its id match what the parameter is looking for?
		}
		return null; // if not, return null
	}
	
	/**
	 * finds a product by its name
	 *
	 * @param name the exact name to look for
	 * @return the product, or null if it is not here
	 */
	public Product getProductByName(String name) {
		for (Product p : productList) { // go through every product
			if (p.getName().equals(name)) {return p;} // does its name match what the parameter is looking for?
		}
		return null; // if not, return null
	}
}