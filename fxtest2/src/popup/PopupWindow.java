package popup;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ui.Styles;
import ui.Ui;
import ui.WarningNote;

/**
 * the base of every popup window: a modal window with a title, a form and a row of buttons
 *
 * <p>it builds what all popups repeated: the stage that blocks the main window, the shared stylesheet,
 * the heading, the cancel button and the button row. a subclass adds its fields to the form,
 * and calls finish() with its main button as the last step of its constructor.
 * the popup only picks the values, FarmSystem does the checks and the saving
 *
 * <p>it also holds the parts the amount popups share: the amount spinner, a binding for the number
 * that is typed in it, and the warning note that explains a wrong amount
 *
 * @author Shawn Alfred Padilla
 * @version 1.2
 * @since 1.1 the problem label hides itself with Ui.collapseWhenEmpty
 * @since 1.2 createAmountSpinner, typedAmount, parseAmount and createWarningNote were added
 * @see AddProductWindow
 * @see MoveProductWindow
 * @see ProductCreationWindow
 * @see ProductEditingWindow
 * @see RequestToBuyWindow
 * @see WarningNote
 */
public abstract class PopupWindow {

	/** the popup's own window, also the owner of the dialogs the popup opens */
	protected final Stage window = new Stage();
	/** holds the heading and the fields, the subclass adds its fields here */
	protected final VBox form = new VBox(12);
	/** the row at the bottom with the cancel button and the main button */
	protected final HBox buttons = new HBox(8);
	/** closes the window without doing anything */
	protected final Button cancel = new Button("Cancel");

	/**
	 * creates the window, call show() to display it
	 *
	 * @param owner the main window, this window blocks it while open
	 * @param windowTitle the title of the window
	 * @param heading the headline shown on top of the form
	 * @param width the width of the window
	 * @param height the height of the window
	 */
	protected PopupWindow(Stage owner, String windowTitle, String heading, double width, double height) {
		Label title = new Label(heading);
		title.getStyleClass().add("section-title");
		title.setWrapText(true);

		form.getStyleClass().add("popup");
		form.getChildren().add(title);

		cancel.getStyleClass().add("secondary");
		cancel.setOnAction(event -> window.close());
		buttons.setAlignment(Pos.CENTER_RIGHT);

		Scene scene = new Scene(form, width, height);
		Styles.apply(scene); // loads the shared stylesheet

		window.setTitle(windowTitle);
		window.setScene(scene);
		window.initModality(Modality.WINDOW_MODAL); // blocks the user from interacting with the window below it
		window.initOwner(owner); // the window below it being the main stage
	}

	/**
	 * puts the main button next to cancel and the button row at the bottom of the form
	 * <p>call it last, after every field was added
	 *
	 * @param main the button that does the action of the popup
	 */
	protected void finish(Button main) {
		buttons.getChildren().addAll(cancel, main);
		form.getChildren().add(buttons);
	}

	/**
	 * creates a small label to put above an input
	 *
	 * @param text the text of the label
	 * @return the label
	 */
	protected Label fieldLabel(String text) {
		Label label = new Label(text);
		label.getStyleClass().add("field-label");
		return label;
	}

	/**
	 * creates a red message that takes no space while it is empty
	 * <p>the forms use it to tell why the main button is disabled
	 *
	 * @param text the message, an empty text hides the label
	 * @return the label
	 */
	protected Label createProblemLabel(ObservableValue<String> text) {
		Label label = new Label();
		label.getStyleClass().add("error-text");
		label.setWrapText(true);
		label.textProperty().bind(text);
		Ui.collapseWhenEmpty(label);
		return label;
	}

	/**
	 * creates an amber warning that says what is wrong and what to do next, it takes no space while the message is empty
	 * <p>the amount popups use it so the person sees the problem before pressing the main button
	 *
	 * @param message what is wrong, an empty text hides the warning
	 * @param nextStep what the person can do about it
	 * @return the warning note
	 * @since 1.2
	 */
	protected WarningNote createWarningNote(ObservableValue<String> message, ObservableValue<String> nextStep) {
		return new WarningNote(message, nextStep);
	}

	/**
	 * creates the editable amount spinner of the amount popups, it starts at 1
	 * <p>there is no upper limit on purpose, a number that is too big is explained by the warning note
	 * and blocked by the main button, FarmSystem still checks it again.
	 * the editor text is set by hand so the typed amount binding has a number from the start
	 *
	 * @return the spinner
	 * @since 1.2
	 */
	protected Spinner<Integer> createAmountSpinner() {
		Spinner<Integer> spinner = new Spinner<>();
		spinner.setValueFactory(new IntegerSpinnerValueFactory(1, Integer.MAX_VALUE, 1, 1)); // min, max, initial value, step
		spinner.setEditable(true); // allows the user to put their own number
		spinner.setMaxWidth(Double.MAX_VALUE);
		spinner.getEditor().setText("1");
		return spinner;
	}

	/**
	 * follows the number that is typed in a spinner, even before it is committed
	 * <p>the arrows change the editor text too, so this covers them as well
	 *
	 * @param spinner the spinner to follow
	 * @return a binding with the typed number, or -1 while the text is not a whole number
	 * @since 1.2
	 */
	protected IntegerBinding typedAmount(Spinner<Integer> spinner) {
		return Bindings.createIntegerBinding(
			() -> parseAmount(spinner.getEditor().getText()),
			spinner.getEditor().textProperty()
		);
	}

	/**
	 * reads a whole number from a text
	 *
	 * @param text the text to read, can be null
	 * @return the number, or -1 if the text is not a whole number
	 * @since 1.2
	 */
	protected static int parseAmount(String text) {
		if (text == null) return -1;

		try {
			return Integer.parseInt(text.trim());
		} catch (NumberFormatException e) {
			return -1;
		}
	}

	/** shows the window */
	public void show() { // just to make things simpler
		// make the new window actually appear
		window.show();
	}
}