package application;

import java.util.ArrayList;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ProductContainer {
	private ArrayList<Product> productList;
	
	public ProductContainer() {
		this.productList = new ArrayList<>();;
	}
	
	public ProductContainer(ArrayList<Product> p) {
		this.productList = new ArrayList<>(p);;
	}
	
	public void add(Product product, int stocks) {
		System.out.println("Attempting to add " + stocks + "x " + product.getName());
		Product p = getProductById(product.getId()); // find the product in this database
		
		if (p == null) { // doesn't exist yet, just add it normally
			productList.add(new Product(product.getId(), product.getName(), product.getDescription(), product.getPrice(), stocks)); 
			System.out.println("Added " + stocks + "x " + product.getName());
			return;
		}
		p.setStock(p.getStock() + stocks); // already exists, stack the stocks
		System.out.println("Added "+ stocks + "x more " + p.getName() + " (Total in stock: " + p.getStock() + ")");
	}
	
	public void remove(Product product, int stocks) {
		Product p = getProductById(product.getId()); // find the product in this database
		if (p == null) return; // doesn't exist
		p.setStock(p.getStock() - stocks);
		if (p.getStock() == 0) productList.remove(p); // nothing left, just remove it
	}
	
	public ObservableList<Product> getObservableList() {
		return FXCollections.observableArrayList(productList);
	}
	
	public ArrayList<Product> getProducts(){
		return productList;
	}
	
	public Product getProductById(int id) {
		for (Product p : productList) { // go through every product
			if (p.getId() == id) {return p;} // does its id match what the user is looking for?
		}
		return null; // if not, return null
	}
}
