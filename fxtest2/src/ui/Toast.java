package ui;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import shell.AppShell;
import shell.SceneManager;

/**
 * a short success message that fades in at the bottom of the page and fades out by itself
 *
 * <p>it is the feedback of every action that worked (request sent, sale confirmed, product created),
 * alerts are only kept for errors and for confirmations of things that can't be undone.
 * the toast does not take mouse clicks, so it never blocks the page under it.
 * the dark green look comes from the toast css class
 *
 * <p>AppShell owns the toast layer and shows one toast at a time, a new one replaces the old one
 *
 * @author Shawn Alfred Padilla
 * @version 1.0
 * @see AppShell#toast(String)
 * @see SceneManager#toast(String)
 */
public class Toast extends HBox {

    /** how long the toast fades in, stays and fades out, in milliseconds */
    private static final double FADE_IN = 150;
    private static final double STAY = 2200;
    private static final double FADE_OUT = 400;

    /** the whole fade in, stay and fade out */
    private final SequentialTransition animation;

    /** the layer the toast is shown on, null until show() */
    private StackPane host;

    /**
     * creates the toast, call show() to display it
     *
     * @param message the text of the toast, keep it short
     */
    public Toast(String message) {
        super(8); // sets the spacing to 8px
        getStyleClass().add("toast");
        setAlignment(Pos.CENTER_LEFT);
        setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE); // keeps the toast small, a stack pane would stretch it
        setMouseTransparent(true);
        setOpacity(0);
        setAccessibleText(message);

        getChildren().add(new Label(message));

        // bottom center of the page, a little above the edge
        StackPane.setAlignment(this, Pos.BOTTOM_CENTER);
        StackPane.setMargin(this, new Insets(0, 0, 24, 0));

        FadeTransition in = new FadeTransition(Duration.millis(FADE_IN), this);
        in.setFromValue(0);
        in.setToValue(1);

        PauseTransition stay = new PauseTransition(Duration.millis(STAY));

        FadeTransition out = new FadeTransition(Duration.millis(FADE_OUT), this);
        out.setFromValue(1);
        out.setToValue(0);

        animation = new SequentialTransition(in, stay, out);
        animation.setOnFinished(event -> dismiss());
    }

    /**
     * puts the toast on a layer and starts the animation
     *
     * @param layer the stack pane the toast is added on top of
     */
    public void show(StackPane layer) {
        this.host = layer;
        layer.getChildren().add(this);
        animation.play();
    }

    /** stops the animation and removes the toast right away, used when a newer toast takes its place */
    public void dismiss() {
        animation.stop();
        if (host != null) host.getChildren().remove(this);
    }
}