package ui;

import java.util.HashMap;
import java.util.List;

import application.Product;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/**
 * a label and a combo box to pick which harvest product another product is made from
 *
 * <p>the first choice is "None (harvest item)", which means the product has no source.
 * the create and edit windows both use this, so the picker looks and works the same in both.
 * it only shows the products it is given, the rules about what is allowed are in FarmSystem.
 * the label has the same width as the labelled text fields, so the rows of a form line up
 *
 * @author Jian Carranza, Shawn Alfred Padilla
 * @version 1.1
 * @since 1.1 the label has the field-label style and the width of LabelledTextField
 * @see application.FarmSystem#getHarvestProducts(Integer)
 */
public class MadeFromPicker extends HBox {

	/** the choice for a harvest item, it has no source product */
	public static final String NONE = "None (harvest item)";

	private ComboBox<String> comboBox;

	/** maps a product name to its id, names are unique so they can be used as keys */
	private HashMap<String, Integer> ids = new HashMap<>();

	/**
	 * creates the picker
	 *
	 * @param harvestProducts the products that can be picked as the source
	 * @param selectedId the id of the source that starts selected, null (or an unknown id) selects none
	 */
	public MadeFromPicker(List<Product> harvestProducts, Integer selectedId) {
		super(10); // sets the spacing to 10px
		Label label = new Label("Made from:");
		label.getStyleClass().add("field-label");
		label.setMinWidth(LabelledTextField.LABEL_WIDTH);

		ObservableList<String> names = FXCollections.observableArrayList();
		names.add(NONE); // always the first choice

		String selectedName = NONE;
		for (Product p : harvestProducts) {
			names.add(p.getName());
			ids.put(p.getName(), p.getId());
			if (selectedId != null && p.getId() == selectedId) selectedName = p.getName();
		}

		this.comboBox = new ComboBox<>(names);
		comboBox.setValue(selectedName);
		comboBox.setMaxWidth(Double.MAX_VALUE); // fills the row like the text fields
		HBox.setHgrow(comboBox, Priority.ALWAYS);

		setAlignment(Pos.CENTER_LEFT);
		this.getChildren().addAll(label, comboBox);
	}

	/**
	 * gets the id of the picked source product
	 *
	 * @return the id of the source, or null if none is picked
	 */
	public Integer getSelectedId() {
		String picked = comboBox.getValue();
		if (picked == null || picked.equals(NONE)) return null;
		return ids.get(picked);
	}
}