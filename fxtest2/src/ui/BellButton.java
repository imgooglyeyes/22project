package ui;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

/**
 * the bell of the top bar, a click opens the screen with the requests of the user
 *
 * <p>a small lime badge with a number sits on the corner of the bell, it is hidden while the count is 0.
 * the count is set with setCount(), TopBar fills it with the pending requests of the role.
 * the accessible text follows the count, so a screen reader says how many requests wait.
 * it can be reached with tab and opened with enter or space
 *
 * @author Shawn Alfred Padilla
 * @version 1.1
 * @since 1.1 the accessible text includes the number of waiting requests
 * @see TopBar
 */
public class BellButton extends StackPane {

    /** the accessible text when no request is waiting */
    private static final String BASE_TEXT = "Requests";

    /** the text inside the badge */
    private final Label badgeText = new Label();
    /** the round badge on the corner of the bell */
    private final StackPane badge = new StackPane(badgeText);

    /**
     * creates the bell with no badge
     *
     * @param action what happens when the bell is clicked or activated with the keyboard
     */
    public BellButton(Runnable action) {
        getStyleClass().add("bell");
        setFocusTraversable(true);
        setAccessibleText(BASE_TEXT);
        Tooltip.install(this, new Tooltip(BASE_TEXT));

        badge.getStyleClass().add("bell-badge");
        badgeText.getStyleClass().add("bell-badge-text");
        badge.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE); // keeps it small instead of filling the bell
        badge.setVisible(false);
        StackPane.setAlignment(badge, Pos.TOP_RIGHT);

        getChildren().addAll(Icons.of(Icons.BELL, 22), badge);

        setOnMouseClicked(event -> action.run());
        setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.SPACE) {
                action.run();
                event.consume();
            }
        });
    }

    /**
     * shows how many requests are waiting
     * <p>the accessible text is updated too, so the number is not only visual
     *
     * @param count the number to show, 0 or less hides the badge, more than 99 shows 99+
     */
    public void setCount(int count) {
        badgeText.setText(count > 99 ? "99+" : String.valueOf(count));
        badge.setVisible(count > 0);
        setAccessibleText(count > 0 ? BASE_TEXT + ", " + count + " waiting" : BASE_TEXT);
    }
}