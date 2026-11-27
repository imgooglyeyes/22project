package pages;

import java.util.ArrayList;

import application.FarmSystem;
import application.FarmSystem.TrendingProduct;
import application.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import ui.Styles;
import shell.SceneManager;

/**
 * the home page of a guest and a customer containinga welcome card, the trending products and a short note about the farm
 *
 * <p>the welcome card has the buttons that fit the user: a guest gets log in and sign up,
 * a customer gets a shortcut to their requests and a note when some of them still wait for a clerk.
 * staff have their own home, see StaffHomePage
 *
 * <p>the trending list shows the most sold products from the confirmed sales,
 * a click on a row opens the marketplace, and so do enter and space because a row can be reached with tab.
 * when nothing was sold yet an empty state explains why the list is empty.
 * the about card says what the farm sells and where, it sets the agricultural setting of the app
 *
 * <p>this page only reads the data, it never changes anything.
 * the page is created again every time it opens, so the numbers are always up to date.
 * the look comes from css classes (card, row, chip), there are no inline colors
 *
 * @author Shawn Alfred Padilla
 * @version 1.3
 * @since 1.1 the about the farm card was added
 * @since 1.2 the trending rows can be reached with tab and opened with the keyboard
 * @since 1.3 the name comes from User.getDisplayName
 * @see StaffHomePage
 * @see SceneManager#showHome()
 * @see FarmSystem#getTrendingProducts(int)
 */
public class HomePage extends ScrollPane {

    /** how many products the trending list shows */
    private static final int TRENDING_LIMIT = 5;

    /** the widest the content can get, so the cards do not stretch across a big window */
    private static final double MAX_WIDTH = 960;

    /** what the farm sells and where, change it to fit the farm of the team */
    private static final String ABOUT_TEXT =
        "ShopeeFarm sells coconut, rice, banana, mango, calamansi, cacao, coffee, sugarcane and ube "
        + "from Philippine farms, and the products made from them, like virgin coconut oil, "
        + "coconut sugar, cacao tablea, barako coffee and ube halaya.";

    /** explains the harvest and made from labels that every product card shows */
    private static final String ABOUT_NOTE =
        "A harvest item is sold as it is picked, a made product is processed from one harvest item.";

    /**
     * creates the home page
     *
     * @param manager the scene manager, opens the other pages and answers the role checks
     * @param user the user who is logged in, or the guest
     */
    public HomePage(SceneManager manager, User user) {
        FarmSystem farmSystem = manager.getFarmSystem();
        boolean guest = user.getUserType().equals(User.GUEST);

        VBox content = new VBox(16,
            buildWelcome(manager, farmSystem, user, guest),
            buildTrending(manager, farmSystem),
            buildAbout()
        );
        content.setPadding(new Insets(24));
        content.setMaxWidth(MAX_WIDTH);

        // the wrapper keeps the content centered at the top when the window is wide
        StackPane wrapper = new StackPane(content);
        wrapper.setAlignment(Pos.TOP_CENTER);

        setContent(wrapper);
        setFitToWidth(true); // lets the content shrink with the window instead of scrolling sideways
        setHbarPolicy(ScrollBarPolicy.NEVER);
    }

    // ---------- welcome ----------

    /**
     * builds the welcome card
     *
     * @param manager the scene manager, opens the pages the buttons lead to
     * @param farmSystem used to count the waiting requests of a customer
     * @param user the user who is logged in, or the guest
     * @param guest true if the user has no account
     * @return the card with the title, the text and the buttons
     */
    private VBox buildWelcome(SceneManager manager, FarmSystem farmSystem, User user, boolean guest) {
        Label title = new Label(guest ? "Welcome to ShopeeFarm" : "Welcome back, " + user.getDisplayName());
        title.getStyleClass().add("page-title");
        title.setWrapText(true);

        Label subtitle = new Label(guest
            ? "Fresh harvests and the products made from them, straight from Philippine farms. Log in to send a request to buy."
            : "Browse what the farm has on the shelf and send a request to buy. A clerk confirms it."
        );
        subtitle.getStyleClass().add("muted");
        subtitle.setWrapText(true);

        VBox card = new VBox(12, title, subtitle);
        card.getStyleClass().add("card");

        // a customer is told when requests still wait for a clerk
        if (manager.canViewMyRequests()) {
            int waiting = farmSystem.countPendingRequests(user.getUsername());

            if (waiting > 0) {
                Label note = new Label(waiting + (waiting == 1 ? " request is" : " requests are") + " waiting for a clerk to confirm");
                note.getStyleClass().add("caption");
                note.setWrapText(true);
                card.getChildren().add(note);
            }
        }

        Button browse = new Button("Browse the marketplace");
        browse.setOnAction(event -> manager.showShelf());

        HBox actions = new HBox(8, browse);

        if (guest) {
            Button logIn = new Button("Log In");
            Button signUp = new Button("Sign Up");
            logIn.getStyleClass().add("secondary");
            signUp.getStyleClass().add("secondary");

            logIn.setOnAction(event -> manager.showLogin());
            signUp.setOnAction(event -> manager.showSignUp());

            actions.getChildren().addAll(logIn, signUp);
        } else if (manager.canViewMyRequests()) {
            Button myRequests = new Button("My Requests");
            myRequests.getStyleClass().add("secondary");
            myRequests.setOnAction(event -> manager.showMyRequests());

            actions.getChildren().add(myRequests);
        }

        card.getChildren().add(actions);
        return card;
    }

    // ---------- trending ----------

    /**
     * builds the trending card
     *
     * @param manager the scene manager, a click on a row opens the marketplace
     * @param farmSystem works out the best selling products
     * @return the card with the list, or an empty state if nothing was sold yet
     */
    private VBox buildTrending(SceneManager manager, FarmSystem farmSystem) {
        Label heading = new Label("Trending now");
        heading.getStyleClass().add("section-title");

        Label hint = new Label("The most sold products, confirmed sales only");
        hint.getStyleClass().add("caption");

        VBox card = new VBox(12, heading, hint);
        card.getStyleClass().add("card");

        ArrayList<TrendingProduct> trending = farmSystem.getTrendingProducts(TRENDING_LIMIT);

        if (trending.isEmpty()) {
            card.getChildren().add(buildEmptyState(manager));
            return card;
        }

        VBox rows = new VBox(8);
        int rank = 1;
        for (TrendingProduct entry : trending) {
            rows.getChildren().add(buildTrendingRow(manager, rank, entry));
            rank++;
        }

        card.getChildren().add(rows);
        return card;
    }

    /**
     * builds one row of the trending list: the rank, the name and a lime chip with the amount sold
     * <p>the chip has dark text, white text on lime is too low in contrast.
     * the row is reachable with tab and opens the marketplace with enter or space, like a click
     *
     * @param manager the scene manager, a click on the row opens the marketplace
     * @param rank the place in the list, starting at 1
     * @param entry the product and how many were sold
     * @return the row
     */
    private HBox buildTrendingRow(SceneManager manager, int rank, TrendingProduct entry) {
        Label rankLabel = new Label("#" + rank);
        rankLabel.getStyleClass().add("section-title");
        rankLabel.setPrefWidth(36);

        Label name = new Label(entry.getName());
        name.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(name, Priority.ALWAYS); // pushes the chip to the right edge

        Label sold = new Label(entry.getSold() + " sold");
        sold.getStyleClass().addAll("chip", "chip-lime");

        HBox row = new HBox(12, rankLabel, name, sold);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12));
        row.getStyleClass().add(Styles.ROW);
        row.setCursor(Cursor.HAND);

        // keyboard access, the row works like a button
        row.setFocusTraversable(true);
        row.setAccessibleText("Number " + rank + ", " + entry.getName() + ", " + entry.getSold() + " sold, open the marketplace");

        row.setOnMouseClicked(event -> manager.showShelf());
        row.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.SPACE) {
                manager.showShelf();
                event.consume();
            }
        });

        return row;
    }

    /**
     * builds the message shown when nothing was sold yet, with a button to the marketplace
     *
     * @param manager the scene manager, opens the marketplace
     * @return the empty state
     */
    private VBox buildEmptyState(SceneManager manager) {
        Label message = new Label("No sales yet");
        message.getStyleClass().add("section-title");

        Label detail = new Label("Products show up here once a clerk confirms a sale.");
        detail.getStyleClass().add("muted");
        detail.setWrapText(true);

        Button shelf = new Button("Go to the marketplace");
        shelf.getStyleClass().add("secondary");
        shelf.setOnAction(event -> manager.showShelf());

        return new VBox(8, message, detail, shelf);
    }

    // ---------- about ----------

    /**
     * builds the about card, a short note about what the farm sells and what harvest and made products are
     *
     * @return the card with the heading and the two texts
     * @since 1.1
     */
    private VBox buildAbout() {
        Label heading = new Label("About the farm");
        heading.getStyleClass().add("section-title");

        Label text = new Label(ABOUT_TEXT);
        text.setWrapText(true);

        Label note = new Label(ABOUT_NOTE);
        note.getStyleClass().add("caption");
        note.setWrapText(true);

        VBox card = new VBox(8, heading, text, note);
        card.getStyleClass().add("card");
        return card;
    }
}