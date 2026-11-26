package application;

/**
 * a product sold in the app
 *
 * <p>attributes:
 * <ul>
 * <li>id (unique): int
 * <li>name: String
 * <li>desc: String
 * <li>price: double
 * <li>stock: int
 * <li>madeFrom: Integer, the id of the product this was made from, null for a harvest item
 * <li>AVAILABLE_ID: int, the next free id
 * </ul>
 *
 * @author Jian Carranza, Shawn Alfred Padilla
 * @version 1.3
 * @since 1.3 the price text comes from Money
 * @see ProductContainer
 * @see Money
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
    /** id of the product this one is made from, null means it is a harvest item */
    private Integer madeFrom;
    /** ensures that a product item has a unique id  */
    public static int AVAILABLE_ID = 0; 
    
    /**
     * creates a new harvest product with the next free id
     *
     * @param name what the product item is called
     * @param desc description of the product item for more details and specifications
     * @param price how much a unit of the product costs
     * @param stock remaining available amount of the product item
     */
    public Product(String name, String desc, double price, int stock) {
    	this.id = AVAILABLE_ID;
    	this.name = name;
        this.desc = desc;
        this.price = price;
        this.stock = stock;
        this.madeFrom = null;
        AVAILABLE_ID += 1; // the next product to be created will not share the id of the last product
    }
    
    /**
     * creates a harvest product with an existing id
     *
     * @param existing_id the id to keep
     * @param name what the product item is called
     * @param desc description of the product item
     * @param price how much a unit of the product costs
     * @param stock remaining available amount of the product item
     */
    public Product(int existing_id, String name, String desc, double price, int stock) {
    	this(existing_id, name, desc, price, stock, null);
    }
    
    /**
     * creates a product with an existing id and a madeFrom, used when loading and copying
     * <p>AVAILABLE_ID is moved past the id so a new product never reuses it
     *
     * @param existing_id the id to keep
     * @param name what the product item is called
     * @param desc description of the product item
     * @param price how much a unit of the product costs
     * @param stock remaining available amount of the product item
     * @param madeFrom id of the product this was made from, null for a harvest item
     * @since 1.1
     */
    public Product(int existing_id, String name, String desc, double price, int stock, Integer madeFrom) {
    	this.id = existing_id;
    	this.name = name;
        this.desc = desc;
        this.price = price;
        this.stock = stock;
        this.madeFrom = madeFrom;
        if (AVAILABLE_ID <= existing_id) { // make sure the AVAILABLE_ID won't eventually, accidentally reach an existing ID
        	AVAILABLE_ID = existing_id + 1;
        }
    }

    /** @return the unique id of this product */
    public int getId()					{ return id; }
    /** @return the name of this product */
    public String getName()				{ return name; }
    /** @return the description of this product */
    public String getDescription()		{ return desc; }
    /** @return the price of one unit */
    public double getPrice()			{ return price; }
    /** @return the remaining stock */
    public int getStock()				{ return stock; }
    
    /**
     * gets the source product
     *
     * @return the id of the source product, or null if this is a harvest item
     * @since 1.1
     */
    public Integer getMadeFrom()		{ return madeFrom; }
    
    /**
     * checks if this product was made from another product
     *
     * @return true if madeFrom is set
     * @since 1.1
     */
    public boolean isMade()				{ return madeFrom != null; }
    
    /**
     * gets the price as text for the screens, with the peso sign and two decimals
     *
     * <p>\u20B1 is the peso sign, written as an escape so the file encoding can't break it
     *
     * @return the price with the peso sign and two decimals
     */
    public String getFormattedPrice()	{ return String.format("\u20B1%.2f", price); }
    
    /** @param stock the new stock amount */
    public void setStock(int stock)		{ this.stock = stock;}
    /** @param name the new name */
    public void setName(String name)		{ this.name = name;}
    /** @param desc the new description */
    public void setDescription(String desc)		{ this.desc = desc;}
    /** @param price the new price of one unit */
    public void setPrice(double price)	{ this.price = price;}
    
    /**
     * sets the source product, used when the source is deleted or the made-from is edited
     *
     * @param madeFrom id of the source product, null to make this a harvest item
     * @since 1.2
     */
    public void setMadeFrom(Integer madeFrom)	{ this.madeFrom = madeFrom; }
}