package application;

/**
 * products sold in the app with the following attributes:
 * <p> - id (unique) :int
 * <p> - name : String 
 * <p> - desc : String
 * <p> - price : double
 * <p> - stock : int 
 * <p> - AVAILABLE_ID (constant): int 
 * 
 * @author Jian Carranza, Shawn Alfred Padilla
 * @version 1.0
 */
public class Product {
    /** auto-generated id to keep track of a product item even after modification */
	private int id; 
    /** what is the product item called */
    private String name;
    /** description of the product item for more details and specifications */
    private String desc;
    /** how much is a unit of the product in decimals (double) */
    private double price;
    /** remaining available amount or number of a product item */
    private int stock;
    /** ensures that a product item has a unique id  */
    public static int AVAILABLE_ID = 0; 
    
    /**
     * creates a Product instance
     * 
     * @param name : what is the product item called
     * @param desc : description of the product item for more details and specifications
     * @param price : how much is a unit of the product in decimals
     * @param stock : remaining available amount or number of a product item
     */
    public Product(String name, String desc, double price, int stock) {
    	this.id = AVAILABLE_ID;
    	this.name = name;
        this.desc = desc;
        this.price = price;
        this.stock = stock;
        AVAILABLE_ID += 1; // the next product to be created will not share the id of the last product
    }
    
    public Product(int existing_id, String name, String desc, double price, int stock) { // version that adds a product with an existing id
    	this.id = existing_id;
    	this.name = name;
        this.desc = desc;
        this.price = price;
        this.stock = stock;
        if (AVAILABLE_ID <= existing_id) { // make sure the AVAILABLE_ID won't eventually, accidentally reach an existing ID
        	AVAILABLE_ID = existing_id + 1;
        }
    }

    public int getId()					{ return id; }
    public String getName()				{ return name; }
    public String getDescription()		{ return desc; }
    public double getPrice()			{ return price; }
    public int getStock()				{ return stock; }
    
    // price as text for the screens, example: ₱30.00
    // \u20B1 is the peso sign, written as an escape so the file encoding can't break it
    public String getFormattedPrice()	{ return String.format("\u20B1%.2f", price); }
    
    public void setStock(int stock)		{ this.stock = stock;}
    public void setName(String name)		{ this.name = name;}
    public void setDescription(String desc)		{ this.desc = desc;}
    public void setPrice(double price)	{ this.price = price;}
}