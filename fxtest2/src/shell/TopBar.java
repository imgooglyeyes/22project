package shell;

import java.net.URL;

import application.FarmSystem;
import application.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import ui.BellButton;
import ui.Icons;
import ui.Searchable;

/**
 * the dark green bar at the top of the app with the logo, the search box, the bell and the user chip
 *
 * <p>the logo goes to the home page when it is clicked, and the app name is shown instead if the logo file is missing.
 * the search box works on pages that implement Searchable, it is disabled on every other page.
 * the bell is hidden for guests, a click opens the transactions for staff and the my requests page for customers.
 * its badge counts the requests that wait for a clerk: all of them for staff, only their own for a customer
 *
 * <p>refresh() is called every time a page opens, so a login or a logout changes the chip and the bell right away.
 * refreshBell() updates only the badge, pages call it after an action that changes the requests
 *
 * @author Shawn Alfred Padilla
 * @version 1.4
 * @since 1.1 the logo opens the home page for everyone, guests included
 * @since 1.2 the search box is connected to the open page with setSearchTarget
 * @since 1.3 the bell badge shows the waiting requests of the role
 * @see UserChip
 * @see BellButton
 * @see Searchable
 */
public class TopBar extends HBox {

    /** the logo file, relative to the class path root (bin) */
    public static final String LOGO = "/application/images/logo.png";

    private final SceneManager manager;
    private final User user;

    /** holds the user chip, so refresh() can swap it without touching the other nodes */
    private final StackPane chipSlot = new StackPane();
    private final BellButton bell;
    private final TextField searchField = new TextField();

    /** the page that gets the typed text, null while the search box is disabled */
    private Searchable searchTarget;

    /**
     * creates the bar, call refresh() to fill the user chip
     *
     * @param manager the scene manager, answers the role checks and opens the pages
     * @param user the user who is logged in
     */
    public TopBar(SceneManager manager, User user) {
        super(16); // sets the spacing to 16px
        this.manager = manager;
        this.user = user;

        getStyleClass().add("top-bar");
        setAlignment(Pos.CENTER_LEFT);
        setPadding(new Insets(12, 16, 12, 16));

        // the search box, disabled until a searchable page opens
        searchField.setPromptText("Search");
        searchField.setDisable(true);
        searchField.textProperty().addListener((obs, old, now) -> {
            if (searchTarget != null) searchTarget.onSearch(now);
        });
        HBox.setHgrow(searchField, Priority.ALWAYS);
        HBox search = new HBox(8, Icons.of(Icons.SEARCH, 18), searchField);
        search.getStyleClass().add("top-search");
        search.setAlignment(Pos.CENTER_LEFT);
        search.setPrefWidth(380);

        // two spacers keep the search box in the middle
        Region leftSpace = new Region();
        Region rightSpace = new Region();
        HBox.setHgrow(leftSpace, Priority.ALWAYS);
        HBox.setHgrow(rightSpace, Priority.ALWAYS);

        this.bell = new BellButton(this::openRequests);

        getChildren().addAll(createBrand(), leftSpace, search, rightSpace, bell, chipSlot);
    }

    /**
     * builds the logo, or the app name if the logo file is missing
     * <p>a missing file never crashes the app, only a message is printed
     *
     * @return the clickable brand node
     */
    private Node createBrand() {
        Node content;
        URL logo = TopBar.class.getResource(LOGO);

        if (logo != null) {
            // 0 width with preserve ratio keeps the shape, the height is 36
            ImageView view = new ImageView(new Image(logo.toExternalForm(), 0, 36, true, true));
            view.setPreserveRatio(true);
            content = view;
        } else {
            System.out.println("Logo not found: " + LOGO);
            Label name = new Label("ShopeeFarm");
            name.getStyleClass().add("brand");
            content = name;
        }

        HBox brand = new HBox(content);
        brand.getStyleClass().add("brand-box");
        brand.setAlignment(Pos.CENTER_LEFT);
        brand.setFocusTraversable(true);
        brand.setAccessibleText("Go to the home page");

        // every role has a home page, guests included
        brand.setOnMouseClicked(event -> manager.showHome());

        return brand;
    }

    /** opens the screen where the user follows requests, staff handle them and customers wait for them */
    private void openRequests() {
        if (manager.canHandleTransactions()) manager.showTransactions();
        else manager.showMyRequests(); // does nothing unless the user is a customer
    }

    /**
     * shows the user chip and the bell that fit the current user
     * <p>the user object is shared and changed by login and logout, so this reads it again every time
     */
    public void refresh() {
        boolean guest = user.getUserType().equals(User.GUEST);

        chipSlot.getChildren().setAll(new UserChip(manager, user));

        // a guest has no requests to follow
        bell.setVisible(!guest);
        bell.setManaged(!guest);

        refreshBell();
    }

    /**
     * counts the waiting requests again and shows the number on the bell
     * <p>staff see every request that waits for a clerk, a customer only sees their own.
     * a guest has no bell, so the count is 0
     *
     * @since 1.3
     */
    public void refreshBell() {
        if (user.getUserType().equals(User.GUEST)) {
            bell.setCount(0);
            return;
        }

        FarmSystem farmSystem = manager.getFarmSystem();
        int waiting = manager.canHandleTransactions()
            ? farmSystem.countPendingRequests()
            : farmSystem.countPendingRequests(user.getUsername());

        bell.setCount(waiting);
    }

    /**
     * connects the search box to a page, or disables it
     * <p>the old text is cleared first without telling the old page, a search never carries over to another page
     *
     * @param target the page that gets the typed text, null to disable the search box
     * @since 1.2
     */
    public void setSearchTarget(Searchable target) {
        searchTarget = null; // clearing the text must not search the old page
        searchField.clear();
        searchField.setDisable(target == null);
        searchField.setPromptText(target == null ? "Search" : target.searchPrompt());
        searchTarget = target;
    }

    /**
     * gets the search box, SceneManager uses it to clear the search
     *
     * @return the search text field
     */
    public TextField getSearchField() {
        return searchField;
    }
}