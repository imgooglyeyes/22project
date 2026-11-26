package ui;

import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;

/**
 * a pill shaped toggle where exactly one option is selected at a time
 *
 * <p>the options are toggle buttons in one group, the segmented css class draws the pill and the selected option.
 * clicking the selected option again does nothing, so something is always selected.
 * the first option starts selected
 *
 * @author Shawn Alfred Padilla
 * @version 1.0
 * @see MarketplacePage
 */
public class SegmentedToggle extends HBox {

    /** the group that keeps one option selected */
    private final ToggleGroup group = new ToggleGroup();

    /** what happens when the selected option changes, can be null */
    private Runnable onChange;

    /**
     * creates the toggle, the first option starts selected
     *
     * @param options the text of each option, in order
     */
    public SegmentedToggle(String... options) {
        super(4); // sets the spacing to 4px
        getStyleClass().add("segmented");

        for (String option : options) {
            ToggleButton button = new ToggleButton(option);
            button.setToggleGroup(group);
            getChildren().add(button);
        }

        group.selectedToggleProperty().addListener((obs, old, now) -> {
            if (now == null) { // clicking the open option would unselect it, so it is selected again
                if (old != null) old.setSelected(true);
                return;
            }
            if (onChange != null) onChange.run();
        });

        if (!group.getToggles().isEmpty()) group.getToggles().get(0).setSelected(true);
    }

    /** @return the text of the selected option */
    public String getSelected() {
        Toggle selected = group.getSelectedToggle();
        return selected == null ? null : ((ToggleButton) selected).getText();
    }

    /**
     * selects an option by its text, nothing happens if there is no such option
     *
     * @param option the text of the option to select
     */
    public void select(String option) {
        for (Toggle toggle : group.getToggles()) {
            if (((ToggleButton) toggle).getText().equals(option)) toggle.setSelected(true);
        }
    }

    /**
     * sets what happens when another option is selected
     *
     * @param onChange the code to run, null for nothing
     */
    public void setOnChange(Runnable onChange) {
        this.onChange = onChange;
    }
}