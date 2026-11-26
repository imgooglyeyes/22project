package ui;

import java.io.File;
import java.io.IOException;

import application.ProductImages;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

/**
 * a row to pick, replace or remove the picture of a product
 *
 * @author Shawn Alfred Padilla
 * @version 1.0
 * @see ProductImageView
 * @see ProductImages
 */
public class ImagePicker extends HBox {

    private static final double PREVIEW_SIZE = 72;

    private final ProductImageView preview = new ProductImageView(PREVIEW_SIZE, PREVIEW_SIZE);
    private final Button removeButton = new Button("Remove");

    private String savedName;
    private File chosenFile;

    public ImagePicker(String savedName) {
        super(10);
        this.savedName = savedName;

        Label label = new Label("Image:");
        label.getStyleClass().add("field-label");
        label.setMinWidth(LabelledTextField.LABEL_WIDTH);

        Button chooseButton = new Button("Choose...");
        chooseButton.getStyleClass().add("secondary");
        removeButton.getStyleClass().add("secondary");

        chooseButton.setOnAction(event -> choose());
        removeButton.setOnAction(event -> {
            this.savedName = null;
            chosenFile = null;
            refreshPreview();
        });

        VBox buttons = new VBox(8, chooseButton, removeButton);
        buttons.setAlignment(Pos.CENTER_LEFT);

        setAlignment(Pos.CENTER_LEFT);
        getChildren().addAll(label, preview, buttons);

        refreshPreview();
    }

    public String commit() throws IOException {
        if (chosenFile == null) return savedName;
        return ProductImages.importFile(chosenFile);
    }

    private void choose() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose a product image");

        String[] patterns = new String[ProductImages.EXTENSIONS.length];
        for (int i = 0; i < patterns.length; i++) {
            patterns[i] = "*." + ProductImages.EXTENSIONS[i];
        }
        chooser.getExtensionFilters().add(new ExtensionFilter("Images", patterns));

        File file = chooser.showOpenDialog(getScene().getWindow());
        if (file == null) return;

        chosenFile = file;
        refreshPreview();
    }

    private void refreshPreview() {
        Image image;

        if (chosenFile != null) {
            image = new Image(chosenFile.toURI().toString(), PREVIEW_SIZE * 2, PREVIEW_SIZE * 2, true, true);
        } else {
            image = ProductImages.load(savedName);
        }

        if (image != null && image.isError()) image = null;

        preview.show(image, "+");
        removeButton.setDisable(chosenFile == null && savedName == null);
    }
}
