package ui;

import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * the bar at the top of a page: the title on the left, the actions on the right and the filters under them
 *
 * <p>it replaces the button bars that every old scene had at the bottom. the filter row wraps
 * when the window is narrow and takes no space while it has nothing in it.
 * a small caption under the title can show a live count, set it with setSubtitle()
 *
 * @author Shawn Alfred Padilla
 * @version 1.1
 * @since 1.1 the caption hides itself with Ui.collapseWhenEmpty
 * @see MarketplacePage
 * @see Ui
 */
public class PageToolbar extends VBox {

    private final Label subtitle = new Label();
    /** the buttons on the right of the title */
    private final HBox actions = new HBox(8);
    /** the filters and the sort under the title, they wrap to the next line when needed */
    private final FlowPane filters = new FlowPane(12, 8);

    /**
     * creates the toolbar
     *
     * @param titleText the title of the page
     */
    public PageToolbar(String titleText) {
        super(12); // sets the spacing to 12px

        Label title = new Label(titleText);
        title.getStyleClass().add("page-title");

        subtitle.getStyleClass().add("caption");
        Ui.collapseWhenEmpty(subtitle); // an empty caption must not leave a gap

        // pushes the actions to the right edge
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        actions.setAlignment(Pos.CENTER_RIGHT);

        HBox top = new HBox(12, new VBox(2, title, subtitle), spacer, actions);
        top.setAlignment(Pos.CENTER_LEFT);

        filters.setAlignment(Pos.CENTER_LEFT);
        // an empty row must not leave a gap
        filters.managedProperty().bind(Bindings.isNotEmpty(filters.getChildren()));
        filters.visibleProperty().bind(Bindings.isNotEmpty(filters.getChildren()));

        getChildren().addAll(top, filters);
    }

    /**
     * sets the small caption under the title
     *
     * @param text the caption, empty to hide it
     */
    public void setSubtitle(String text) {
        subtitle.setText(text);
    }

    /**
     * adds nodes to the filter row
     *
     * @param nodes the filters, toggles and sort boxes to add
     */
    public void addFilter(Node... nodes) {
        filters.getChildren().addAll(nodes);
    }

    /**
     * adds buttons to the right of the title
     *
     * @param nodes the buttons to add
     */
    public void addAction(Node... nodes) {
        actions.getChildren().addAll(nodes);
    }
}