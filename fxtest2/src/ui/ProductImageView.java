package ui;

import javafx.geometry.Rectangle2D;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

/**
 * a fixed size tile that shows a product picture, or a placeholder when there is none
 *
 * @author Shawn Alfred Padilla
 * @version 1.0
 * @see ProductCard
 * @see ImagePicker
 * @see application.ProductImages
 */
public class ProductImageView extends StackPane {

    private final double width;
    private final double height;

    public ProductImageView(double width, double height) {
        this.width = width;
        this.height = height;

        getStyleClass().add("product-image");
        setMinSize(width, height);
        setPrefSize(width, height);
        setMaxSize(width, height);

        Rectangle clip = new Rectangle(width, height);
        clip.setArcWidth(16);
        clip.setArcHeight(16);
        setClip(clip);
    }

    public void show(Image image, String placeholder) {
        if (image == null) {
            Label letter = new Label(placeholder);
            letter.getStyleClass().add("product-initial");
            getChildren().setAll(letter);
            return;
        }

        ImageView view = new ImageView(image);

        double imageRatio = image.getWidth() / image.getHeight();
        double tileRatio = width / height;

        if (imageRatio > tileRatio) {
            double cropWidth = image.getHeight() * tileRatio;
            view.setViewport(new Rectangle2D((image.getWidth() - cropWidth) / 2, 0, cropWidth, image.getHeight()));
        } else {
            double cropHeight = image.getWidth() / tileRatio;
            view.setViewport(new Rectangle2D(0, (image.getHeight() - cropHeight) / 2, image.getWidth(), cropHeight));
        }

        view.setFitWidth(width);
        view.setFitHeight(height);
        view.setSmooth(true);

        getChildren().setAll(view);
    }
}
