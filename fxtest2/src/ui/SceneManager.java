package ui;

import application.FarmSystem;
import application.ProductContainer;
import application.ProductsFileHandler;
import application.TransactionContainer;
import application.User;
import application.UserContainer;
import javafx.stage.Stage;

/**
 * creates and opens every scene, and holds the shared containers
 *
 * <p>scenes ask it for the header, the transactions and the farm system.
 * it also saves the product containers by hand and resets them to the template data
 *
 * <p>every role rule lives here in the can... methods, one method per row of the permission table.
 * the header and the scenes use them to decide which buttons to show, and the show... methods
 * use them again so a scene can't be opened by a user who should not see it.
 * FarmSystem does not check roles, it trusts the screens
 *
 * @author Jian Carranza, Shawn Alfred Padilla
 * @version 1.6
 * @see FarmSystem
 * @see ProductsFileHandler
 */
public class SceneManager {

    private Stage stage;

    private ProductContainer database;
    private ProductContainer inventoryData;
    private ProductContainer shelfData;
    private TransactionContainer transactionData;
    private UserContainer userData;

    private FarmSystem farmSystem;

    private User currentUser;

    /**
     * creates the scene manager and its shared data
     *
     * @param stage the main window
     * @param database every product and its details
     * @param inventoryData the products and stock in the inventory
     * @param shelfData the products and stock on the shelf
     * @param transactionData every sale transaction
     * @param userData every user and their details
     * @param currentUser the user who is logged in
     */
    public SceneManager(
            Stage stage,
            ProductContainer database,
            ProductContainer inventoryData,
            ProductContainer shelfData,
            TransactionContainer transactionData,
            UserContainer userData,
            User currentUser) {

        this.stage = stage;
        this.database = database;
        this.inventoryData = inventoryData;
        this.shelfData = shelfData;
        this.transactionData = transactionData;
        this.userData = userData;
        this.currentUser = currentUser;

        // the farm system gets the same container objects, so it and the scenes always see the same data
        this.farmSystem = new FarmSystem(database, inventoryData, shelfData, transactionData);
    }

    /** @return every sale transaction, scenes read and save from it */
    public TransactionContainer getTransactions() {
        return transactionData;
    }

    /** @return the shared farm system, scenes and windows call the product, stock and sales actions on it */
    public FarmSystem getFarmSystem() {
        return farmSystem;
    }

    // ---------- role checks ----------

    /** @return true if the current user is a guest, someone who is not logged in */
    private boolean isGuest() {
        return currentUser.getUserType().equals(User.GUEST);
    }

    /** @return true if the current user is a manager */
    private boolean isManager() {
        return currentUser.getUserType().equals(User.MANAGER);
    }

    /** @return true if the current user is a clerk */
    private boolean isClerk() {
        return currentUser.getUserType().equals(User.CLERK);
    }

    /** @return true if the current user is a customer */
    private boolean isCustomer() {
        return currentUser.getUserType().equals(User.CUSTOMER);
    }

    /** @return true if the current user is a clerk or a manager */
    private boolean isStaff() {
        return isManager() || isClerk();
    }

    /**
     * sign up, only guests can create an account, someone who is logged in has one already
     *
     * @return true if the current user can open the sign up screen
     * @since 1.6
     */
    public boolean canSignUp() {
        return isGuest();
    }

    /**
     * request to buy, only customers can send a request
     *
     * @return true if the current user can request to buy a shelf product
     * @since 1.5
     */
    public boolean canRequestToBuy() {
        return isCustomer();
    }

    /**
     * lets a customer open their own requests, the scene only shows what they requested
     *
     * @return true if the current user can open the my requests screen
     * @since 1.5
     */
    public boolean canViewMyRequests() {
        return isCustomer();
    }

    /**
     * add product to shelf, moves stock from the inventory to the shelf
     *
     * @return true if the current user can stock the shelf
     * @since 1.5
     */
    public boolean canAddToShelf() {
        return isStaff();
    }

    /**
     * move shelf stock back to inventory, only managers
     *
     * @return true if the current user can take stock off the shelf
     * @since 1.5
     */
    public boolean canMoveToInventory() {
        return isManager();
    }

    /**
     * the manual save button on the shelf, it is a backup for staff
     *
     * @return true if the current user can save by hand
     * @since 1.5
     */
    public boolean canManualSave() {
        return isStaff();
    }

    /**
     * view inventory, staff can look at it
     *
     * @return true if the current user can open the inventory screen
     * @since 1.5
     */
    public boolean canViewInventory() {
        return isStaff();
    }

    /**
     * add and remove inventory stock, only managers
     *
     * @return true if the current user can change the inventory stock
     * @since 1.5
     */
    public boolean canManageInventory() {
        return isManager();
    }

    /**
     * product database, create, edit and delete, only managers
     *
     * @return true if the current user can open and change the database
     * @since 1.5
     */
    public boolean canManageDatabase() {
        return isManager();
    }

    /**
     * confirm and void transactions
     *
     * @return true if the current user can open the transactions screen and act on them
     * @since 1.5
     */
    public boolean canHandleTransactions() {
        return isStaff();
    }

    /**
     * cash balance
     *
     * @return true if the current user can open the cash screen
     * @since 1.5
     */
    public boolean canViewCash() {
        return isStaff();
    }

    /**
     * sale history, a clerk can only see their own, a manager can see any manager or clerk
     * <p>customers and guests have no history
     *
     * @param target the staff member whose history is wanted
     * @return true if the current user can open the history of the target
     * @since 1.5
     */
    public boolean canViewHistoryOf(User target) {
        if (target == null) return false;

        if (isManager()) { // any staff member, the staff list only shows staff
            return target.getUserType().equals(User.MANAGER) ||
                   target.getUserType().equals(User.CLERK);
        }
        if (isClerk()) { // only their own history
            return target.getUsername().equals(currentUser.getUsername());
        }
        return false;
    }

    /**
     * the staff list is where a manager picks whose history to open
     *
     * @return true if the current user can open the staff list
     * @since 1.5
     */
    public boolean canViewStaffList() {
        return isManager();
    }

    /**
     * approve sign-ups, only managers
     * <p>the screen for this comes later in phase 3, nothing calls this yet
     *
     * @return true if the current user can approve new accounts
     * @since 1.5
     */
    public boolean canApproveSignUps() {
        return isManager();
    }

    // ---------- saving ----------

    /**
     * saves the database, inventory and shelf
     * <p>used by the manual save button, every other change is saved by FarmSystem
     */
    public void saveProductContainers() {
        ProductsFileHandler.saveAll(database, inventoryData, shelfData);
    }

    /**
     * puts the database, inventory and shelf back to the template data
     * <p>the files are copied first, then loaded again into the same container objects
     * <p>only managers can do this, transactions are not touched
     *
     * @return true if the reset worked, false if the user is not a manager or a file failed
     * @since 1.2
     */
    public boolean resetToDefaults() {
        if (!canManageDatabase()) return false;

        if (!ProductsFileHandler.resetToDefaults()) return false;

        ProductContainer[] fresh = ProductsFileHandler.loadAll();
        database.replaceWith(fresh[0]);
        inventoryData.replaceWith(fresh[1]);
        shelfData.replaceWith(fresh[2]);

        return true;
    }

    // ---------- scenes ----------

    /**
     * creates the shared header
     *
     * @param currentScene the name of the open scene, its button is highlighted
     * @return the header for the current user
     */
    public AppHeader createHeader(String currentScene) {
        return new AppHeader(this, currentScene, currentUser);
    }

    /** opens the shelf */
    public void showShelf() {
        ShelfScene scene = new ShelfScene(
            stage,
            this,
            inventoryData,
            shelfData,
            currentUser
        );

        stage.setScene(scene);
        stage.setTitle("The Shelf");
    }

    /** opens the inventory, doing nothing unless the current user can view it */
    public void showInventory() {
        if (!canViewInventory()) return;

        InventoryScene scene = new InventoryScene(
            stage,
            database,
            inventoryData,
            this,
            currentUser
        );

        stage.setScene(scene);
        stage.setTitle("Inventory");
    }

    /** opens the database, doing nothing unless the current user can manage it */
    public void showDatabase() {
        if (!canManageDatabase()) return;

        DatabaseScene scene = new DatabaseScene(
            stage,
            database,
            this,
            currentUser
        );

        stage.setScene(scene);
        stage.setTitle("Database");
    }

    /** opens the login scene */
    public void showLogin() {
        LoginScene scene = new LoginScene(stage, this, userData, currentUser);
        stage.setScene(scene);
        stage.setTitle("Log In");
    }

    /**
     * opens the sign up scene, doing nothing unless the current user is a guest
     *
     * @since 1.6
     */
    public void showSignUp() {
        if (!canSignUp()) return;

        SignUpScene scene = new SignUpScene(stage, this, userData);
        stage.setScene(scene);
        stage.setTitle("Sign Up");
    }

    /** opens the customer's own requests, doing nothing unless the user is a customer */
    public void showMyRequests() {
        if (!canViewMyRequests()) return;

        MyRequestsScene scene = new MyRequestsScene(stage, this, currentUser);
        stage.setScene(scene);
        stage.setTitle("My Requests");
    }

    /** opens all transactions, doing nothing unless the current user can handle them */
    public void showTransactions() {
        if (!canHandleTransactions()) return;

        TransactionsScene scene = new TransactionsScene(stage, this, currentUser);
        stage.setScene(scene);
        stage.setTitle("Transactions");
    }

    /**
     * opens the target user's transaction history
     * <p>does nothing if the current user is not allowed to see that history,
     * a clerk asking for another clerk's history is stopped here
     *
     * @param targetUser the staff member whose history to show
     */
    public void showHistory(User targetUser) {
        if (!canViewHistoryOf(targetUser)) return;

        HistoryScene scene = new HistoryScene(stage, this, currentUser, targetUser);
        stage.setScene(scene);
        stage.setTitle("Transactions History");
    }

    /** opens the staff list, doing nothing unless the current user can view it */
    public void showStaffList() {
        if (!canViewStaffList()) return;

        StaffListScene scene = new StaffListScene(stage, this, userData, currentUser);
        stage.setScene(scene);
        stage.setTitle("Staff List");
    }

    /** opens the cash balance, doing nothing unless the current user can view it */
    public void showCash() {
        if (!canViewCash()) return;

        CashScene scene = new CashScene(stage, this, currentUser);
        stage.setScene(scene);
        stage.setTitle("Cash");
    }
    /** this opens the sign-up requests screen, doing nothing unless the current user can approve sign-ups */
    public void showSignUpRequests() {
        if (canApproveSignUps() == false) {
            return;
        }

        SignUpRequestScene scene = new SignUpRequestScene(stage, this, userData, currentUser);
        stage.setScene(scene);
        stage.setTitle("Sign-Up Requests");
    }
}
