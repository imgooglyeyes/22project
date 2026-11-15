package application;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// one purchase request made by a customer
// it stores a copy of the product name and price at request time,
// so the record stays correct even if the product is edited or deleted later
// this class only holds data and status, it never changes stock

public class SaleTransaction {

    // the three possible statuses
    public final static String REQUESTED = "Requested";
    public final static String CONFIRMED = "Confirmed";
    public final static String VOIDED = "Voided";

    public static int AVAILABLE_ID = 0; // makes sure no transaction shares an id

    private int id;
    private int productId;
    private String productName; 
    private double unitPrice;   // copy, price per piece at request time
    private int quantity;
    private String requester;   // username of the customer
    private String status;

    private String confirmedBy; // stays filled even if the transaction
    private String voidedBy;

    private LocalDateTime requestedAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime voidedAt;

    // creates a new request, it always starts as requested
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

    // rebuilds a saved transaction with all its data
    // confirmedBy, voidedBy and the times can be null
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

    // a request can only be confirmed once
    public boolean canConfirm() {
        return status.equals(REQUESTED);
    }

    // a request or a confirmed sale can be voided, but only once
    public boolean canVoid() {
        return !status.equals(VOIDED);
    }

    // true if this sale was confirmed before even if it is voided
    public boolean wasConfirmed() {
        return confirmedBy != null;
    }

    // marks the request as confirmed, returns false if it is not allowed
    public boolean confirm(String username) {
        if (!canConfirm()) return false;
        status = CONFIRMED;
        confirmedBy = username;
        confirmedAt = LocalDateTime.now();
        return true;
    }

    // marks the transaction as voided, returns false if it is not allowed
    public boolean voidTransaction(String username) {
        if (!canVoid()) return false;
        status = VOIDED;
        voidedBy = username;
        voidedAt = LocalDateTime.now();
        return true;
    }

    // total price of this transaction
    public double getTotal() {
        return unitPrice * quantity;
    }

    // total as text for the screens, example: ₱150.00
    public String getFormattedTotal() {
        return String.format("\u20B1%.2f", getTotal());
    }

    // time as text for the screens, empty if the time is not set yet
    public static String formatTime(LocalDateTime time) {
        if (time == null) return "";
        return time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    public int getId()                      { return id; }
    public int getProductId()               { return productId; }
    public String getProductName()          { return productName; }
    public double getUnitPrice()            { return unitPrice; }
    public int getQuantity()                { return quantity; }
    public String getRequester()            { return requester; }
    public String getStatus()               { return status; }
    public String getConfirmedBy()          { return confirmedBy; }
    public String getVoidedBy()             { return voidedBy; }
    public LocalDateTime getRequestedAt()   { return requestedAt; }
    public LocalDateTime getConfirmedAt()   { return confirmedAt; }
    public LocalDateTime getVoidedAt()      { return voidedAt; }
}