package ui;

import application.User;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;

/**
 * the one persistent frame of the app: the top bar, the side nav and the page area
 *
 * <p>it is the root of the only scene of the app. SceneManager swaps the page in the
 * middle with setPage(), the top bar and the side nav stay and are only refreshed.
 * the page area is a stack pane, the open page is its bottom layer and the toast sits on top of it
 *
 * <p>a page that implements Searchable gets the search box of the top bar, every other page gets it disabled
 *
 * @author Shawn Alfred Padilla
 * @version 1.3
 * @since 1.1 the search box is connected to pages that are searchable
 * @since 1.2 the toast layer was added, setPage only replaces the page so an open toast stays
 * @see TopBar
 * @see SideNav
 * @see Searchable
 * @see Toast
 * @see SceneManager#setPage(String, javafx.scene.Parent)
 */
public class AppShell extends BorderPane {

    private final TopBar topBar;
    private final SideNav sideNav;

    /** holds the open page, and the toast on top of it */
    private final StackPane pageHost = new StackPane();

    /** the page that is open now, null before the first page */
    private Node currentPage;

    /** the toast that is showing now, null if none */
    private Toast currentToast;

    /**
     * creates the shell, the page area starts empty
     *
     * @param manager the scene manager, answers the role checks and opens the pages
     * @param user the user who is logged in, the bar and the nav read it every time they refresh
     */
    public AppShell(SceneManager manager, User user) {
        this.topBar = new TopBar(manager, user);
        this.sideNav = new SideNav(manager, user);

        setTop(topBar);
        setLeft(sideNav);
        setCenter(pageHost);
    }

    /**
     * shows a page in the middle and refreshes the bar and the nav for the current user
     * <p>the search box is cleared and connected to the page if it is searchable, or disabled if it is not.
     * only the page layer is replaced, so a toast that is showing is not removed
     *
     * @param name the name of the page, the nav item with the same name is marked
     * @param page the page to show, it replaces the one before
     */
    public void setPage(String name, Node page) {
        topBar.refresh();
        topBar.setSearchTarget(page instanceof Searchable ? (Searchable) page : null);
        sideNav.refresh(name);

        if (currentPage != null) pageHost.getChildren().remove(currentPage);
        pageHost.getChildren().add(0, page); // index 0 keeps the page under the toast
        currentPage = page;
    }

    /**
     * shows a short message at the bottom of the page, a toast that is already showing is replaced
     *
     * @param message the text of the toast
     * @since 1.2
     */
    public void toast(String message) {
        if (currentToast != null) currentToast.dismiss();

        currentToast = new Toast(message);
        currentToast.show(pageHost);
    }

    /** @return the top bar, SceneManager uses it for the search box and the bell */
    public TopBar getTopBar() {
        return topBar;
    }
}