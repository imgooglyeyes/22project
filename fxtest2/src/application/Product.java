package application;

public class Product {
	private int id; // this will be used to make sure we don't lose track of an item after the user edits its name
    private String name;
    private String desc;
    private double price;
    private int stock;
    
    public static int AVAILABLE_ID = 0; // makes sure no item has the same ID

    public Product(String name, String desc, double price, int stock) {
    	this.id = AVAILABLE_ID;
    	this.name = name;
        this.desc = desc;
        this.price = price;
        this.stock = stock;
        AVAILABLE_ID += 1; // the next product to be created will not share the id of the last product
    }

    public int getId()					{ return id; }
    public String getName()				{ return name; }
    public String getDescription()		{ return desc; }
    public double getPrice()			{ return price; }
    public int getStock()				{ return stock; }
    
    public void setStock(int stock)		{ this.stock = stock;}
}