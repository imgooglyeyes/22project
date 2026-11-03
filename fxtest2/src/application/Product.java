package application;

public class Product {
	private int id;
    private String name;
    private String desc;
    private double price;
    private int stock;
    
    // Login fields kept for the old test window.
    private String username = "";
    private String password = "";
    private String firstName = "";
    private String lastName = "";
    private String userType = "";
    
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
    public String getUsername()		{ return username; }
    public String getPassword()		{ return password; }
    public String getFirstName()		{ return firstName; }
    public String getLastName()		{ return lastName; }
    public String getUserType()		{ return userType; }
    
    public void setStock(int stock)		{ this.stock = stock;}
}