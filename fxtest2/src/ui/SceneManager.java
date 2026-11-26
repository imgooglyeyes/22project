package ui;

import java.net.URL;
import java.util.Optional;

import application.FarmSystem;
import application.ProductContainer;
import application.ProductsFileHandler;
import application.TransactionContainer;
import application.User;
import application.UserContainer;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * creates and opens every page, and holds the shared containers
 *
 * <p>the app has one scene with an AppShell (top bar, side nav, page area), pages are swapped
 * in the middle with setPage(). every screen is a page, the old scenes are gone.
 * the log in and sign up pages are one AuthPage, the home is a HomePage or a StaffHomePage,
 * the shelf is a MarketplacePage, and the staff pages are tables built on DataTable
 *
 * <p>pages ask it for the transactions and the farm system, and tell it to show a toast
 * or to count the bell again after an action.
 * it also saves the product containers by hand and resets them to the template data
 *
 * <p>every role rule lives here in the can... methods, one method per row of the permission table.
 * the side nav and the pages use them to decide what to show, and the show... methods
 * use them again so a page can't be opened by a user who should not see it.
 * FarmSystem does not check roles, it trusts the screens
 *
 * @author Jian Carranza, Shawn Alfred Padilla
 * @version 1.14
 * @since 1.9 one scene with an app shell, setPage, showLockedPrompt, logOut and quit
 * @since 1.10 showLogin and showSignUp open the AuthPage
 * @since 1.11 showHome replaces showDashboard, log out lands on the home page
 * @since 1.12 showShelf opens the MarketplacePage, clearSearch empties the search box
 * @since 1.13 every staff screen is a page, showScene and createHeader were removed, showDatabase became showProducts
 * @since 1.14 toast, refreshBell and the dashboard of a staff member (canViewDashboardOf, showStaffDashboard)
 * @see FarmSystem
 * @see ProductsFileHandler
 * @see AppShell
 * @see AuthPage
 * @see MarketplacePage
 */
public class SceneManager {

    /** the starting size of the window */
    private static final double WINDOW_WIDTH = 1180;
    private static final double WINDOW_HEIGHT = 720;

    /** the smallest size the window can be resized to */
    private static final double MIN_WIDTH = 960;
    private static final double MIN_HEIGHT = 600;

    private Stage stage;

    private ProductContainer database;
    private ProductContainer inventoryData;
    private ProductContainer shelfData;
    private TransactionContainer transactionData;
    private UserContainer userData;

    private FarmSystem farmSystem;

    private User currentUser;

    /** the frame around every page */
    private AppShell shell;

    /**
     * creates the scene manager, its shared data and the one scene of the app
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

        // the farm system gets the same container objects, so it and the pages always see the same data
        this.farmSystem = new FarmSystem(database, inventoryData, shelfData, transactionData);

        // the shell is the root of the only scene, it is created after the user so it can read it
        this.shell = new AppShell(this, currentUser);
        Scene scene = new Scene(shell, WINDOW_WIDTH, WINDOW_HEIGHT);
        Styles.apply(scene);

        stage.setScene(scene);
        stage.setMinWidth(MIN_WIDTH);
        stage.setMinHeight(MIN_HEIGHT);

        // the logo is also the window icon, a missing file only prints a message
        URL logo = SceneManager.class.getResource(TopBar.LOGO);
        if (logo != null) stage.getIcons().add(new Image(logo.toExternalForm()));
    }

    /** @return every sale transaction, pages read and save from it */
    public TransactionContainer getTransactions() {
        return transactionData;
    }

    /** @return the shared farm system, pages and windows call the product, stock and sales actions on it */
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
     * staff home, the tiles with pending requests, low stock, cash and sign ups
     * <p>the home page itself is open to everyone, guests and customers get the HomePage instead
     *
     * @return true if the current user gets the staff home
     * @since 1.11
     */
    public boolean canViewStaffHome() {
        return isStaff();
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
     * lets a customer open their own requests, the page only shows what they requested
     *
     * @return true if the current user can open the my requests page
     * @since 1.5
     */
    public boolean canViewMyRequests() {
        return isCustomer();
    }

    /**
     * add product to shelf, moves stock from the inventory to the shelf
     * <p>the marketplace also gives these users the manage and preview toggle
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
     * the manual save button, it is a backup for staff
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
     * @return true if the current user can open the inventory page
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
     * @return true if the current user can open and change the products page
     * @since 1.5
     */
    public boolean canManageDatabase() {
        return isManager();
    }

    /**
     * confirm and void transactions
     *
     * @return true if the current user can open the transactions page and act on them
     * @since 1.5
     */
    public boolean canHandleTransactions() {
        return isStaff();
    }

    /**
     * cash balance
     *
     * @return true if the current user can open the cash page
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

        if (isManager()) { // any staff member, the staff page only shows staff
            return target.getUserType().equals(User.MANAGER) ||
                   target.getUserType().equals(User.CLERK);
        }
        if (isClerk()) { // only their own history
            return target.getUsername().equals(currentUser.getUsername());
        }
        return false;
    }

    /**
     * the dashboard of a staff member, only managers can open the dashboard of someone else
     * <p>a clerk only has their own dashboard, which is their home page
     *
     * @param target the staff member whose dashboard is wanted
     * @return true if the current user can open the dashboard of the target
     * @since 1.14
     */
    public boolean canViewDashboardOf(User target) {
        if (target == null || !isManager()) return false;

        return target.getUserType().equals(User.MANAGER) ||
               target.getUserType().equals(User.CLERK);
    }

    /**
     * the history item of the side nav, only clerks get it, a manager opens the history from the staff page
     *
     * @return true if the current user gets a history item in the nav
     * @since 1.13
     */
    public boolean canViewOwnHistory() {
        return isClerk();
    }

    /**
     * the staff page is where a manager picks whose history to open
     *
     * @return true if the current user can open the staff page
     * @since 1.5
     */
    public boolean canViewStaffList() {
        return isManager();
    }

    /**
     * approve or reject sign ups, only managers
     * <p>the sign-ups tab of the staff page and the home tile use this
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

    // ---------- account and window ----------

    /**
     * logs the current user out and opens the home page, used by the user menu
     * <p>the home page is open to guests, so the person lands on the guest welcome
     *
     * @since 1.9
     */
    public void logOut() {
        currentUser.logOut();
        showHome();
    }

    /**
     * closes the app, used by the user menu
     *
     * @since 1.9
     */
    public void quit() {
        Platform.exit();
    }

    /**
     * empties the search box of the top bar
     * <p>the open page is told by the box itself, so it redraws without the search
     *
     * @since 1.12
     */
    public void clearSearch() {
        shell.getTopBar().getSearchField().clear();
    }

    /**
     * shows a short success message at the bottom of the page
     * <p>pages call this after an action that worked, errors and confirmations use Dialogs instead
     *
     * @param message the text of the toast, keep it short
     * @since 1.14
     */
    public void toast(String message) {
        shell.toast(message);
    }

    /**
     * counts the waiting requests again and shows the number on the bell
     * <p>pages call this after an action that adds, confirms or voids a request
     *
     * @since 1.14
     */
    public void refreshBell() {
        shell.getTopBar().refreshBell();
    }

    /**
     * explains why a locked nav item can't be opened
     * <p>a guest is asked to log in and can go straight to the log in page, a customer
     * is told that the page is for staff. the dialog uses the app theme
     *
     * @param itemName the name of the locked page, shown in the message
     * @since 1.9
     */
    public void showLockedPrompt(String itemName) {
        if (isGuest()) {
            ButtonType logIn = new ButtonType("Log In", ButtonData.OK_DONE);
            ButtonType notNow = new ButtonType("Not Now", ButtonData.CANCEL_CLOSE);

            Alert alert = new Alert(
                AlertType.INFORMATION,
                "Log in with a clerk or manager account to unlock " + itemName + ".",
                logIn,
                notNow
            );
            Dialogs.theme(alert, stage, "Log in to unlock");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == logIn) showLogin();
        } else {
            Alert alert = new Alert(
                AlertType.INFORMATION,
                itemName + " is only for clerks and managers.",
                ButtonType.OK
            );
            Dialogs.theme(alert, stage, "Locked");
            alert.showAndWait();
        }
    }

    // ---------- pages ----------

    /**
     * shows a page in the middle of the shell and refreshes the top bar and the side nav
     *
     * @param name the name of the page, the nav item with the same name is marked
     * @param page the page to show
     * @since 1.9
     */
    public void setPage(String name, Parent page) {
        shell.setPage(name, page);
    }

    /**
     * opens the home page of the current user
     * <p>clerks and managers get the staff home with tiles, guests and customers
     * get the welcome with the trending products. everyone can open it
     *
     * @since 1.11 replaces showDashboard
     */
    public void showHome() {
        Parent page = canViewStaffHome()
            ? new StaffHomePage(this, userData, currentUser)
            : new HomePage(this, currentUser);

        setPage("Home", page);
        stage.setTitle("Home");
    }

    /**
     * opens the dashboard of a staff member, doing nothing unless the current user can view it
     * <p>it is the same page as the home of a clerk, so the manager sees what the clerk sees.
     * the staff item of the nav stays marked, because the page is opened from the staff page
     *
     * @param target the clerk or manager whose dashboard to show
     * @since 1.14
     */
    public void showStaffDashboard(User target) {
        if (!canViewDashboardOf(target)) return;

        setPage("Staff", new StaffHomePage(this, userData, currentUser, target));
        stage.setTitle(target.getUsername() + "'s Dashboard");
    }

    /**
     * opens the marketplace, the shelf as a grid of product cards, everyone can open it
     *
     * @since 1.12 the page replaces the old shelf scene
     */
    public void showShelf() {
        setPage("Shelf", new MarketplacePage(stage, this, inventoryData, shelfData, currentUser));
        stage.setTitle("Marketplace");
    }

    /**
     * opens the inventory, doing nothing unless the current user can view it
     *
     * @since 1.13 the page replaces the old inventory scene
     */
    public void showInventory() {
        if (!canViewInventory()) return;

        setPage("Inventory", new InventoryPage(stage, this, database, inventoryData));
        stage.setTitle("Inventory");
    }

    /**
     * opens the products page, doing nothing unless the current user can manage it
     *
     * @since 1.13 the page replaces the old database scene, the method was called showDatabase
     */
    public void showProducts() {
        if (!canManageDatabase()) return;

        setPage("Products", new ProductsPage(stage, this, database));
        stage.setTitle("Products");
    }

    /**
     * opens the auth page on the log in tab
     *
     * @since 1.10 the page replaces the old login scene
     */
    public void showLogin() {
        setPage("Log In", new AuthPage(this, userData, currentUser, false));
        stage.setTitle("Log In");
    }

    /**
     * opens the auth page on the sign up tab, doing nothing unless the current user is a guest
     *
     * @since 1.10 the page replaces the old sign up scene
     */
    public void showSignUp() {
        if (!canSignUp()) return;

        setPage("Sign Up", new AuthPage(this, userData, currentUser, true));
        stage.setTitle("Sign Up");
    }

    /**
     * opens the staff page on the sign-ups tab, doing nothing unless the current user can approve sign ups
     *
     * @since 1.7
     */
    public void showSignUpRequests() {
        if (!canApproveSignUps()) return;

        setPage("Staff", new StaffPage(this, userData, true));
        stage.setTitle("Staff");
    }

    /**
     * opens the customer's own requests, doing nothing unless the user is a customer
     *
     * @since 1.13 the page replaces the old my requests scene
     */
    public void showMyRequests() {
        if (!canViewMyRequests()) return;

        setPage("My Requests", new MyRequestsPage(this, currentUser));
        stage.setTitle("My Requests");
    }

    /**
     * opens all transactions, doing nothing unless the current user can handle them
     *
     * @since 1.13 the page replaces the old transactions scene
     */
    public void showTransactions() {
        if (!canHandleTransactions()) return;

        setPage("Transactions", new TransactionsPage(this, currentUser));
        stage.setTitle("Transactions");
    }

    /**
     * opens the target user's transaction history
     * <p>does nothing if the current user is not allowed to see that history,
     * a clerk asking for another clerk's history is stopped here.
     * a manager has no history item in the nav, so the staff item stays marked
     *
     * @param targetUser the staff member whose history to show
     * @since 1.13 the page replaces the old history scene
     */
    public void showHistory(User targetUser) {
        if (!canViewHistoryOf(targetUser)) return;

        setPage(isManager() ? "Staff" : "History", new HistoryPage(this, currentUser, targetUser));
        stage.setTitle("Transactions History");
    }

    /**
     * opens the staff page on the members tab, doing nothing unless the current user can view it
     *
     * @since 1.13 the page replaces the old staff list scene
     */
    public void showStaffList() {
        if (!canViewStaffList()) return;

        setPage("Staff", new StaffPage(this, userData, false));
        stage.setTitle("Staff");
    }

    /**
     * opens the cash balance, doing nothing unless the current user can view it
     *
     * @since 1.13 the page replaces the old cash scene
     */
    public void showCash() {
        if (!canViewCash()) return;

        setPage("Cash", new CashPage(this));
        stage.setTitle("Cash");
    }
}