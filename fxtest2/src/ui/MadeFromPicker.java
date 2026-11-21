package ui;

import java.util.HashMap;
import java.util.List;

import application.Product;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

/**
 * a label and a combo box to pick which harvest product another product is made from
 *
 * <p>the first choice is "None (harvest item)", which means the product has no source.
 * the create and edit windows both use this, so the picker looks and works the same in both.
 * it only shows the products it is given, the rules about what is allowed are in FarmSystem
 *
 * @author Jian Carranza, Shawn Alfred Padilla
 * @version 1.0
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