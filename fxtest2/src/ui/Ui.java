package ui;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.stage.Window;

/**
 * small helpers that more than one page and popup used to copy
 *
 * <p>show() hides a node without leaving a gap, collapseWhenEmpty() does the same for a label that is empty most of the time,
 * windowOf() gives the window a page is in, and afterAction() creates the code a popup runs after an action worked.
 * pages call these instead of keeping their own copy
 *
 * @author Shawn Alfred Padilla
 * @version 1.0
 * @see MarketplacePage
 * @see StaffPage
 * @see AuthPage
 */
public class Ui {

    /** no one creates this class, it only has static methods */
    private Ui() {}

    /**
     * shows or hides a node, a hidden node takes no space
     *
     * @param node the node to change
     * @param visible true to show it
     */
    public static void show(Node node, boolean visible) {
        node.setVisible(visible);
        node.setManaged(visible);
    }

    /**
     * makes a label take no space while its text is empty
     * <p>messages are empty most of the time, so they must not leave a gap.
     * the label's visible and managed properties are bound, so do not call show() on it
     *
     * @param label the label to set up
     */
    public static void collapseWhenEmpty(Label label) {
        label.managedProperty().bind(label.textProperty().isNotEmpty());
        label.visibleProperty().bind(label.textProperty().isNotEmpty());
    }

    /**
     * gets the window a node is in, the dialogs of a page belong to it
     * <p>the node has to be in a scene already, so call it from an action and not from a constructor
     *
     * @param node the node, usually the page itself
     * @return the window of the node
     */
    public static Window windowOf(Node node) {
        return node.getScene().getWindow();
    }

    /**
     * creates what the popup windows run after an action worked: redraw, count the bell again and show a toast
     *
     * @param manager the scene manager, counts the bell and shows the toast
     * @param refresh the code that redraws the page
     * @param message the text of the toast
     * @return the code to give to the popup window as its refresh
     */
    public static Runnable afterAction(SceneManager manager, Runnable refresh, String message) {
        return () -> {
            refresh.run();
            manager.refreshBell(); // a request can change the badge, counting again is cheap
            manager.toast(message);
        };
    }
}