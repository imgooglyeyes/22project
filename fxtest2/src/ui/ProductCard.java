package ui;

import application.FarmSystem;
import application.Product;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * one product of the shelf as a card: a placeholder picture, the name, where it comes from,
 * the description, the price, a stock badge and an optional button
 *
 * <p>there is no photo field yet, so the picture is a soft gradient tile with the first letter of the name.
 * the made-from chip says "Harvest" for a harvest item or "Made from" and the source name for a made product.
 * the stock badge is amber when the stock is low (see FarmSystem#LOW_STOCK_THRESHOLD)
 *
 * <p>the card only shows data, the page decides which button it gets (or none) and what the button does.
 * the name and the description have a fixed height, so every card of a grid has the same size,
 * the full text is in the tooltip. the look comes from css classes, there are no inline colors
 *
 * @author Shawn Alfred Padilla
 * @version 1.0
 * @see MarketplacePage
 * @see FarmSystem#LOW_STOCK_THRESHOLD
 */
public class ProductCard extends VBox {

    /** the width of every card */
    public static final double WIDTH = 220;

    /** the height of the placeholder picture */
    private static final double IMAGE_HEIGHT = 110;
    /** room for two lines of the name */
    private static final double NAME_HEIGHT = 44;
    /** room for three lines of the description */
    private static final double DESCRIPTION_HEIGHT = 48;
    /** the space inside the card, it matches the padding in css */
    private static final double INNER_WIDTH = WIDTH - 32;

    /**
     * creates the card
     *
     * @param product the shelf product to show
     * @param madeFromName the name of the source product, null if it is a harvest item or the source is missing
     * @param action the button at the bottom of the card, null for no button
     */
    public ProductCard(Product product, String madeFromName, Button action) {
        super(8); // sets the spacing to 8px
        getStyleClass().addAll("card", "product-card");
        setPrefWidth(WIDTH);
        setMinWidth(WIDTH);
        setMaxWidth(WIDTH);

        // the placeholder picture
        Label initial = new Label(initialOf(product.getName()));
        initial.getStyleClass().add("product-initial");
        StackPane image = new StackPane(initial);
        image.getStyleClass().add("product-image");
        fixHeight(image, IMAGE_HEIGHT);

        Label name = new Label(product.getName());
        name.getStyleClass().add("product-name");
        name.setWrapText(true);
        name.setAlignment(Pos.TOP_LEFT);
        fixHeight(name, NAME_HEIGHT);

        // a made product shows its source, a long name is cut with dots instead of widening the card
        Label source = new Label(product.isMade()
            ? "Made from " + (madeFromName == null ? "unknown" : madeFromName)
            : "Harvest"
        );
        source.getStyleClass().addAll("chip", "chip-muted");
        source.setMaxWidth(INNER_WIDTH);
        HBox sourceRow = new HBox(source);

        Label description = new Label(product.getDescription());
        description.getStyleClass().add("caption");
        description.setWrapText(true);
        description.setAlignment(Pos.TOP_LEFT);
        fixHeight(description, DESCRIPTION_HEIGHT);

        // the price on the left and the stock badge on the right
        Label price = new Label(product.getFormattedPrice());
        price.getStyleClass().add("product-price");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox priceRow = new HBox(8, price, spacer, createStockBadge(product.getStock()));
        priceRow.setAlignment(Pos.CENTER_LEFT);

        Tooltip.install(this, new Tooltip(product.getName() + "\n" + product.getDescription()));

        getChildren().addAll(image, name, sourceRow, description, priceRow);

        if (action != null) {
            action.setMaxWidth(Double.MAX_VALUE); // the button fills the width of the card
            getChildren().add(action);
        }
    }

    /**
     * creates the stock badge: out of stock, low stock (amber) or in stock
     * <p>the shelf removes a product at 0 stock, so the out of stock case only guards a hand edited file
     *
     * @param stock the stock on the shelf
     * @return the badge
     */
    private Label createStockBadge(int stock) {
        Label badge = new Label();
        badge.getStyleClass().add("chip");

        if (stock <= 0) {
            badge.setText("Out of stock");
            badge.getStyleClass().add("chip-danger");
        } else if (stock <= FarmSystem.LOW_STOCK_THRESHOLD) {
            badge.setText("Only " + stock + " left");
            badge.getStyleClass().add("chip-amber");
        } else {
            badge.setText(stock + " in stock");
        }
        return badge;
    }

    /**
     * gets the first letter of a name in capitals, shown on the placeholder picture
     *
     * @param name the product name
     * @return one letter, or ? for an empty name
     */
    private String initialOf(String name) {
        String trimmed = name.trim();
        return trimmed.isEmpty() ? "?" : trimmed.substring(0, 1).toUpperCase();
    }

    /**
     * gives a region one fixed height, so the cards of a grid line up
     *
     * @param region the region to fix
     * @param height the height in pixels
     */
    private void fixHeight(Region region, double height) {
        region.setMinHeight(height);
        region.setPrefHeight(height);
        region.setMaxHeight(height);
    }
}