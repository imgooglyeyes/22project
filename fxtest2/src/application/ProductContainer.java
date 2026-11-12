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
		if (p == null) {
			productList.add(new Product(product.getId(), product.getName(), product.getDescription(), product.getPrice(), stocks)); // doesn't exist yet, just add it normally
			System.out.println("Added " + stocks + "x " + product.getName());
			return;
		}
		p.setStock(p.getStock() + stocks);
		System.out.println("Added "+ stocks + "x more " + p.getName());
	}
	
	public void remove(int i) {
		productList.remove(i);
	}
	
	public void remove(Product p) {
		productList.remove(p);
	}
	
	public void removeStock(Product product, int stocks) {
		Product p = getProductById(product.getId()); // find the product in this database
		if (p == null) return;
		p.setStock(p.getStock() - stocks);
	}
	
	public ObservableList<Product> getObservableList() {
		return FXCollections.observableArrayList(productList);
	}
	
	public ArrayList<Product> getList(){
		return productList;
	}
	
	public Product getProductById(int id) {
		for (Product p : productList) { // go through every product
			if (p.getId() == id) {return p;} // does its id match what the user is looking for?
		}
		return null; // if not, return null
	}
}
