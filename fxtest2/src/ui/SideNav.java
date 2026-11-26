package ui;

import application.User;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * the white sidebar with one nav item for every page the user can open
 *
 * <p>which items appear follows the can... methods of SceneManager, so the nav, the screens
 * and the show... methods always use the same permission table.
 * the home and the marketplace are open to everyone, including guests.
 * inventory and cash are shown locked for guests and customers, so they can see that
 * more exists after logging in. a click on a locked item asks SceneManager to explain why it is closed
 *
 * <p>the items per role: a customer has my requests, a clerk has transactions, inventory, cash and history,
 * a manager has transactions, inventory, cash, staff and products. the manager has no history item,
 * the history of a staff member is opened from the staff page
 *
 * <p>the nav is built again by refresh() every time a page opens, so a login or a logout
 * is shown right away
 *
 * @author Shawn Alfred Padilla
 * @version 1.2
 * @since 1.1 the home item is shown to everyone, guests included
 * @since 1.2 staff and products replace the staff list, sign-ups and database items
 * @see NavItem
 * @see SceneManager
 */
public class SideNav extends VBox {

    /** the fixed width of the sidebar */
    private static final double WIDTH = 220;

    /** the scene manager, answers the role checks and opens the pages */
    private final SceneManager manager;
    /** the user who is logged in, the history item needs it */
    private final User user;

    /**
     * creates an empty nav, call refresh() to fill it
     *
     * @param manager the scene manager, answers the role checks and opens the pages
     * @param user the user who is logged in
     */
    public SideNav(SceneManager manager, User user) {
        super(4); // sets the spacing to 4px
        this.manager = manager;
        this.user = user;

        getStyleClass().add("side-nav");
        setPadding(new Insets(16, 12, 16, 12));
        setMinWidth(WIDTH);
        setPrefWidth(WIDTH);
        setMaxWidth(WIDTH);
    }

    /**
     * builds the items again for the current user and marks the open page
     *
     * @param activeKey the name of the open page, no item is marked if none matches (for example the log in page)
     */
    public void refresh(String activeKey) {
        getChildren().clear();

        // the home and the marketplace are open to everyone
        addItem("Home", "Home", Icons.HOME, manager::showHome);
        addItem("Shelf", "Marketplace", Icons.STORE, manager::showShelf);

        // every other item is added only if the permission table allows it for this user
        if (manager.canViewMyRequests()) {
            addItem("My Requests", "My Requests", Icons.RECEIPT, manager::showMyRequests);
        }

        if (manager.canHandleTransactions()) {
            addItem("Transactions", "Transactions", Icons.SWAP, manager::showTransactions);
        }

        // inventory and cash are shown locked to the users who can't open them
        if (manager.canViewInventory()) {
            addItem("Inventory", "Inventory", Icons.TRACTOR, manager::showInventory);
        } else {
            addLockedItem("Inventory", "Inventory", Icons.TRACTOR);
        }

        if (manager.canViewCash()) {
            addItem("Cash", "Cash", Icons.WALLET, manager::showCash);
        } else {
            addLockedItem("Cash", "Cash", Icons.WALLET);
        }

        // a clerk opens their own history here, a manager opens any history from the staff page
        if (manager.canViewOwnHistory()) {
            addItem("History", "History", Icons.HISTORY, () -> manager.showHistory(user));
        }

        // the staff page holds the members and the sign-ups
        if (manager.canViewStaffList()) {
            addItem("Staff", "Staff", Icons.PEOPLE, manager::showStaffList);
        }

        if (manager.canManageDatabase()) {
            addItem("Products", "Products", Icons.DATABASE, manager::showProducts);
        }

        // pushes anything added below it to the bottom of the sidebar
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        getChildren().add(spacer);

        // marks the open page
        for (Node node : getChildren()) {
            if (node instanceof NavItem) {
                NavItem item = (NavItem) node;
                item.setActive(item.getKey().equals(activeKey));
            }
        }
    }

    /**
     * adds an item that opens a page
     *
     * @param key the name of the page, used to mark the open page
     * @param label the text shown in the nav
     * @param iconPath one of the path constants of Icons
     * @param action what happens when the item is clicked
     */
    private void addItem(String key, String label, String iconPath, Runnable action) {
        getChildren().add(new NavItem(key, label, iconPath, false, action));
    }

    /**
     * adds a locked item, a click explains why the page is closed instead of opening it
     *
     * @param key the name of the page
     * @param label the text shown in the nav
     * @param iconPath one of the path constants of Icons
     */
    private void addLockedItem(String key, String label, String iconPath) {
        getChildren().add(new NavItem(key, label, iconPath, true, () -> manager.showLockedPrompt(label)));
    }
}