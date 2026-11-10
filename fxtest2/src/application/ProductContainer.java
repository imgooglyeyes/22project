package application;

import java.util.ArrayList;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ProductContainer {
	private ArrayList<Product> productList;
	
	public ProductContainer(ArrayList<Product> p) {
		this.productList = p;
	}
	
	public void add(Product p) {
		productList.add(p);
	}
	
	public void remove(int i) {
		productList.remove(i);
	}
	
	public void remove(Product p) {
		productList.remove(p);
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
