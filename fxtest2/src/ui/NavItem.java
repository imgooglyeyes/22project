package ui;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

/**
 * one entry of the side nav: an icon, a label and, for a locked entry, a small lock
 *
 * <p>it is an hbox and not a button so it does not pick up the button styles.
 * it can be clicked, and it can be reached with tab and opened with enter or space.
 * the open page is marked with the nav-item-active class, a locked entry has the
 * nav-item-locked class and shows a lock icon, all of it is styled in application.css
 *
 * @author Shawn Alfred Padilla
 * @version 1.0
 * @see SideNav
 */
public class NavItem extends HBox {

    /** the name of the page this entry opens, SideNav compares it with the open page */
    private final String key;

    /**
     * creates the entry
     *
     * @param key the name of the page this entry opens, the same name that is given to SceneManager#setPage
     * @param label the text shown next to the icon
     * @param iconPath one of the path constants of Icons
     * @param locked true to show a lock, the action then usually explains why the page is closed
     * @param action what happens when the entry is clicked or activated with the keyboard
     */
    public NavItem(String key, String label, String iconPath, boolean locked, Runnable action) {
        super(12); // sets the spacing to 12px
        this.key = key;

        getStyleClass().add("nav-item");
        if (locked) getStyleClass().add("nav-item-locked");

        setAlignment(Pos.CENTER_LEFT);
        setFocusTraversable(true); // reachable with tab
        setAccessibleText(locked ? label + ", locked" : label);

        Label text = new Label(label);
        text.getStyleClass().add("nav-label");

        // pushes the lock to the right edge
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        getChildren().addAll(Icons.of(iconPath, 20), text, spacer);
        if (locked) getChildren().add(Icons.of(Icons.LOCK, 14));

        setOnMouseClicked(event -> action.run());

        // enter and space work like a click, so the nav can be used without a mouse
        setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.SPACE) {
                action.run();
                event.consume();
            }
        });
    }

    /** @return the name of the page this entry opens */
    public String getKey() {
        return key;
    }

    /**
     * marks this entry as the open page, or clears the mark
     *
     * @param active true if this entry is the open page
     */
    public void setActive(boolean active) {
        getStyleClass().remove("nav-item-active"); // does nothing if it is not there, so it never doubles
        if (active) getStyleClass().add("nav-item-active");
    }
}