package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import pages.MarketplacePage;

/**
 * the message shown in place of a list that has nothing to show
 *
 * <p>it is a title, a short explanation and a button that leads the person somewhere useful,
 * so an empty page is never a dead end. the look comes from css classes, there are no inline colors
 *
 * @author Shawn Alfred Padilla
 * @version 1.0
 * @see MarketplacePage
 */
public class EmptyState extends VBox {

    /**
     * creates the empty state
     *
     * @param title the short headline
     * @param detail one sentence that explains why it is empty and what to do
     * @param buttonText the text of the button, null for no button
     * @param action what happens when the button is clicked, null for no button
     */
    public EmptyState(String title, String detail, String buttonText, Runnable action) {
        super(8); // sets the spacing to 8px
        setAlignment(Pos.CENTER);
        setPadding(new Insets(48, 16, 48, 16));

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("section-title");

        Label detailLabel = new Label(detail);
        detailLabel.getStyleClass().add("muted");
        detailLabel.setWrapText(true);
        detailLabel.setTextAlignment(TextAlignment.CENTER);
        detailLabel.setMaxWidth(420);

        getChildren().addAll(titleLabel, detailLabel);

        if (buttonText != null && action != null) {
            Button button = new Button(buttonText);
            button.getStyleClass().add("secondary");
            button.setOnAction(event -> action.run());
            getChildren().add(button);
        }
    }
}