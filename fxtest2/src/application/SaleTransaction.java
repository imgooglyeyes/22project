package application;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * one purchase request made by a customer, it can be confirmed or voided by staff
 *
 * <p>it stores a copy of the product name and the unit price at request time,
 * so the record stays correct even if the product is edited or deleted later
 * this class only holds data and status, it NEVER CHANGES STOCK, FarmSystem does that
 *
 * <p>the status goes one way: requested, then confirmed, and either of them can be voided once
 * confirmedBy and voidedBy stay filled after a void, so a voided sale still shows who confirmed it
 * AVAILABLE_ID is moved past every loaded id, so a new transaction NEVER REUSES AN ID
 *
 * @author Shawn Alfred Padilla
 * @version 1.1
 * @since 1.1 the total text comes from Money
 * @see TransactionContainer
 * @see TransactionFilesHandler
 * @see FarmSystem
 * @see Money
 */
public class SaleTransaction {

    /** the request waits for a clerk or a manager */
    public final static String REQUESTED = "Requested";
    /** staff confirmed the request, the stock left the shelf */
    public final static String CONFIRMED = "Confirmed";
    /** staff voided the request or the sale */
    public final static String VOIDED = "Voided";

    /** the next free id, makes sure no transaction shares an id */
    public static int AVAILABLE_ID = 0;

    /** unique id of this transaction */
    private int id;
    /** id of the product that was requested */
    private int productId;
    /** copy of the product name at request time */
    private String productName;
    /** copy of the price per piece at request time */
    private double unitPrice;
    /** how many pieces were requested */
    private int quantity;
    /** username of the customer */
    private String requester;
    /** requested, confirmed or voided */
    private String status;

    /** username of the staff member who confirmed it, null if never confirmed, stays filled after a void */
    private String confirmedBy;
    /** username of the staff member who voided it, null if not voided */
    private String voidedBy;

    /** when the request was made */
    private LocalDateTime requestedAt;
    /** when it was confirmed, null if never confirmed */
    private LocalDateTime confirmedAt;
    /** when it was voided, null if not voided */
    private LocalDateTime voidedAt;

    /**
     * creates a new request, it always starts as requested
     * <p>the name and the price are copied from the product, so later edits do not change this record
     *
     * @param product the product the customer wants
     * @param quantity how many the customer wants
     * @param requester the username of the customer
     */
    public SaleTransaction(Product product, int quantity, String requester) {
        this.id = AVAILABLE_ID;
        AVAILABLE_ID += 1; // next transaction gets a new id

        this.productId = product.getId();
        this.productName = product.getName();
        this.unitPrice = product.getPrice();
        this.quantity = quantity;
        this.requester = requester;
        this.status = REQUESTED;
        this.requestedAt = LocalDateTime.now();
    }

    /**
     * rebuilds a saved transaction with all its data, used when loading the save file
     * <p>confirmedBy, voidedBy and the three times can be null.
     * AVAILABLE_ID is moved past the id so a new transaction never reuses it
     *
     * @param id the saved id to keep
     * @param productId id of the product
     * @param productName name of the product at request time
     * @param unitPrice price per piece at request time
     * @param quantity how many pieces
     * @param requester username of the customer
     * @param status requested, confirmed or voided
     * @param confirmedBy username of the staff who confirmed it, or null
     * @param voidedBy username of the staff who voided it, or null
     * @param requestedAt when it was requested
     * @param confirmedAt when it was confirmed, or null
     * @param voidedAt when it was voided, or null
     */
    public SaleTransaction(int id, int productId, String productName, double unitPrice, int quantity,
                           String requester, String status, String confirmedBy, String voidedBy,
                           LocalDateTime requestedAt, LocalDateTime confirmedAt, LocalDateTime voidedAt) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.requester = requester;
        this.status = status;
        this.confirmedBy = confirmedBy;
        this.voidedBy = voidedBy;
        this.requestedAt = requestedAt;
        this.confirmedAt = confirmedAt;
        this.voidedAt = voidedAt;

        // make sure new transactions never reuse a loaded id
        if (AVAILABLE_ID <= id) {
            AVAILABLE_ID = id + 1;
        }
    }

    /** @return true if the request can still be confirmed, a request can only be confirmed once */
    public boolean canConfirm() {
        return status.equals(REQUESTED);
    }

    /** @return true if the transaction can be voided, a request or a confirmed sale can be voided but only once */
    public boolean canVoid() {
        return !status.equals(VOIDED);
    }

    /** @return true if this sale was confirmed before, even if it was voided later */
    public boolean wasConfirmed() {
        return confirmedBy != null;
    }

    /**
     * marks the request as confirmed
     *
     * @param username the staff member who confirms it
     * @return true if it was confirmed, false if it is not allowed
     */
    public boolean confirm(String username) {
        if (!canConfirm()) return false;
        status = CONFIRMED;
        confirmedBy = username;
        confirmedAt = LocalDateTime.now();
        return true;
    }

    /**
     * marks the transaction as voided
     *
     * @param username the staff member who voids it
     * @return true if it was voided, false if it is not allowed
     */
    public boolean voidTransaction(String username) {
        if (!canVoid()) return false;
        status = VOIDED;
        voidedBy = username;
        voidedAt = LocalDateTime.now();
        return true;
    }

    /** @return the total price of this transaction, the unit price times the quantity */
    public double getTotal() {
        return unitPrice * quantity;
    }

    /**
     * gets the total as text for the screens, with the peso sign and two decimals
     *
     * @return the formatted total, see Money#format(double)
     */
    public String getFormattedTotal() {
        return Money.format(getTotal());
    }

    /**
     * gets a time as text for the screens
     *
     * @param time the time to format, can be null
     * @return the time as yyyy-MM-dd HH:mm, or an empty text if the time is not set yet
     */
    public static String formatTime(LocalDateTime time) {
        if (time == null) return "";
        return time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    /** @return the unique id of this transaction */
    public int getId()                      { return id; }
    /** @return the id of the product that was requested */
    public int getProductId()               { return productId; }
    /** @return the product name saved at request time */
    public String getProductName()          { return productName; }
    /** @return the price per piece saved at request time */
    public double getUnitPrice()            { return unitPrice; }
    /** @return how many pieces were requested */
    public int getQuantity()                { return quantity; }
    /** @return the username of the customer */
    public String getRequester()            { return requester; }
    /** @return requested, confirmed or voided */
    public String getStatus()               { return status; }
    /** @return the staff member who confirmed it, or null */
    public String getConfirmedBy()          { return confirmedBy; }
    /** @return the staff member who voided it, or null */
    public String getVoidedBy()             { return voidedBy; }
    /** @return when the request was made */
    public LocalDateTime getRequestedAt()   { return requestedAt; }
    /** @return when it was confirmed, or null */
    public LocalDateTime getConfirmedAt()   { return confirmedAt; }
    /** @return when it was voided, or null */
    public LocalDateTime getVoidedAt()      { return voidedAt; }
}