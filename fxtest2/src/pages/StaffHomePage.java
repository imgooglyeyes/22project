package pages;

import application.FarmSystem;
import application.Money;
import application.User;
import application.UserContainer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import shell.SceneManager;

/**
 * the dashboard of a clerk and a manager: a welcome and tiles with what needs attention
 *
 * <p>the tiles are pending requests, low stock on the shelf, the cash balance, the requests the staff member handled,
 * the sales they confirmed and, for managers on their own dashboard, the sign ups that wait for approval.
 * every tile shows a live number and opens the page where the staff member can act on it.
 * which tiles appear follows the can... methods of SceneManager
 *
 * <p>the page has a target, the staff member the numbers belong to. on the home page the target is the
 * logged in user. a manager can open the dashboard of another staff member from the staff page,
 * then the page has the name of that person in the title and a button back to the staff page
 *
 * <p>this page only reads the data, it never changes anything.
 * the page is created again every time it opens, so the numbers are always up to date.
 * a tile is a button with the tile css class (a white card look), so it works with the keyboard
 *
 * @author Shawn Alfred Padilla
 * @version 1.3
 * @since 1.1 the page has a target user, the requests handled and the sales confirmed tiles were added
 * @since 1.2 the garbled peso sign in the comments was replaced with plain words
 * @since 1.3 the name comes from User.getDisplayName and the money text from Money
 * @see HomePage
 * @see SceneManager#showHome()
 * @see SceneManager#showStaffDashboard(User)
 * @see FarmSystem#countHandledBy(String)
 * @see FarmSystem#getSalesTotalBy(String)
 */
public class StaffHomePage extends ScrollPane {

    /** the widest the content can get, so the tiles do not stretch across a big window */
    private static final double MAX_WIDTH = 960;

    /** the tiles, they wrap to the next line when the window is too narrow */
    private final FlowPane tiles = new FlowPane(16, 16);

    /**
     * creates the home dashboard of the logged in staff member
     *
     * @param manager the scene manager, opens the pages and answers the role checks
     * @param userData every user, read to count the pending sign ups
     * @param user the user who is logged in
     */
    public StaffHomePage(SceneManager manager, UserContainer userData, User user) {
        this(manager, userData, user, user);
    }

    /**
     * creates the dashboard of a staff member, the viewer can be someone else
     *
     * @param manager the scene manager, opens the pages and answers the role checks
     * @param userData every user, read to count the pending sign ups
     * @param viewer the user who is logged in and looks at the page
     * @param target the staff member the personal numbers belong to, the same as the viewer on the home page
     * @since 1.1
     */
    public StaffHomePage(SceneManager manager, UserContainer userData, User viewer, User target) {
        FarmSystem farmSystem = manager.getFarmSystem();

        // compared by username, the logged in user is a copy of the account in the user list
        boolean own = viewer.getUsername().equals(target.getUsername());
        String staff = target.getUsername();
        String name = target.getDisplayName();

        Label title = new Label(own ? "Welcome back, " + name : name + "'s dashboard");
        title.getStyleClass().add("page-title");
        title.setWrapText(true);

        Label subtitle = new Label(own
            ? "Here is what needs attention on the farm today"
            : "What " + staff + " has handled, seen as a manager"
        );
        subtitle.getStyleClass().add("muted");

        // pushes the back button to the right edge
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox head = new HBox(12, new VBox(4, title, subtitle), spacer);
        head.setAlignment(Pos.CENTER_LEFT);

        if (!own) { // a manager came from the staff page, so there is a way back
            Button back = new Button("Back to Staff");
            back.getStyleClass().add("secondary");
            back.setOnAction(event -> manager.showStaffList());
            head.getChildren().add(back);
        }

        // each tile is added only if the permission table allows it for this user
        if (manager.canHandleTransactions()) {
            addTile("Pending requests",
                String.valueOf(farmSystem.countPendingRequests()),
                "waiting for confirmation",
                manager::showTransactions);
        }

        // the shelf is open to everyone
        addTile("Low stock",
            String.valueOf(farmSystem.getLowStock(FarmSystem.LOW_STOCK_THRESHOLD).size()),
            "products running out on the shelf",
            manager::showShelf);

        if (manager.canViewCash()) {
            addTile("Cash balance",
                Money.format(farmSystem.getCashBalance()),
                "from confirmed sales",
                manager::showCash);
        }

        // the personal numbers of the target, both open the history of the target
        addTile("Requests handled",
            String.valueOf(farmSystem.countHandledBy(staff)),
            "confirmed or voided by " + (own ? "you" : staff),
            () -> manager.showHistory(target));

        addTile("Sales confirmed",
            Money.format(farmSystem.getSalesTotalBy(staff)),
            "confirmed by " + (own ? "you" : staff) + ", voids not counted",
            () -> manager.showHistory(target));

        // the sign ups are not a personal number, so they only show on the own dashboard
        if (own && manager.canApproveSignUps()) {
            addTile("Sign-up requests",
                String.valueOf(userData.countByStatus(User.PENDING)),
                "waiting for approval",
                manager::showSignUpRequests);
        }

        VBox content = new VBox(16, head, tiles);
        content.setPadding(new Insets(24));
        content.setMaxWidth(MAX_WIDTH);

        // the wrapper keeps the content centered at the top when the window is wide
        StackPane wrapper = new StackPane(content);
        wrapper.setAlignment(Pos.TOP_CENTER);

        setContent(wrapper);
        setFitToWidth(true); // lets the flow pane wrap instead of scrolling sideways
        setHbarPolicy(ScrollBarPolicy.NEVER);
    }

    /**
     * creates one tile and adds it to the page
     * <p>the title is a small caption, the live number is big and a short note explains it.
     * the tile is a button with only a graphic, so the whole card is clickable
     *
     * @param title the small caption on top
     * @param value the live number or amount
     * @param note a short sentence under the number
     * @param action what happens when the tile is clicked, usually a show... method of SceneManager
     */
    private void addTile(String title, String value, String note, Runnable action) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("caption");

        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("page-title");

        Label noteLabel = new Label(note);
        noteLabel.getStyleClass().add("caption");
        noteLabel.setWrapText(true);

        VBox inside = new VBox(4, titleLabel, valueLabel, noteLabel);
        inside.setAlignment(Pos.CENTER_LEFT);

        Button tile = new Button();
        tile.getStyleClass().add("tile");
        tile.setGraphic(inside);
        tile.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        tile.setAlignment(Pos.CENTER_LEFT);
        tile.setPadding(new Insets(16));
        tile.setPrefSize(250, 120);
        tile.setAccessibleText(title + ": " + value + ", " + note);
        tile.setOnAction(event -> action.run());

        tiles.getChildren().add(tile);
    }
}