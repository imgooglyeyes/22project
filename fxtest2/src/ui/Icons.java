package ui;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.transform.Scale;

/**
 * the svg paths of the icons used by the app shell and the pages
 *
 * <p>every path is drawn on a 24 x 24 grid. most come from the material icons set (apache 2.0),
 * the tractor is referenced online because the set has none. the nodes are made with of(),
 * which returns a group around the svg path so the layout size is always the size asked for
 *
 * <p>the svg path node has the style class "icon", so css can recolor it with -fx-fill,
 * for example .icon { -fx-fill: -sf-ink; }
 *
 * @author Shawn Alfred Padilla
 * @version 1.2
 * @since 1.1 the icons of the side nav and the user chip were added
 * @since 1.2 the unused chart, gear and person add icons were removed
 * @see Fonts
 */
public class Icons {

    /** the side of the grid every path is drawn on */
    private static final double GRID = 24;

    /** house shape, for the home page */
    public static final String HOME =
        "M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z";

    /** storefront, for the marketplace page */
    public static final String STORE =
        "M20 4H4v2h16V4zm1 10v-2l-1-5H4l-1 5v2h1v6h10v-6h4v6h2v-6h1zm-9 4H6v-4h6v4z";

    /** hand drawn tractor, for the inventory page */
    public static final String TRACTOR =
        "M3.5 5h1.5v5H3.5z "                                        // exhaust pipe
        + "M2 10h7v4H2z "                                           // hood
        + "M9 5h6.5l1.5 5v2H9z "                                    // cab
        + "M17 12a4 4 0 1 1 0 8a4 4 0 1 1 0-8z "                    // big rear wheel
        + "M17 14.5a1.5 1.5 0 1 0 0 3a1.5 1.5 0 1 0 0-3z "          // rear wheel hole
        + "M6 14a3 3 0 1 1 0 6a3 3 0 1 1 0-6z "                     // small front wheel
        + "M6 16a1 1 0 1 0 0 2a1 1 0 1 0 0-2z";                     // front wheel hole

    /** padlock, for the locked nav items */
    public static final String LOCK =
        "M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zm-6 9c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zm3.1-9H8.9V6c0-1.71 1.39-3.1 3.1-3.1 1.71 0 3.1 1.39 3.1 3.1v2z";

    /** bell, for the notification button */
    public static final String BELL =
        "M12 22c1.1 0 2-.9 2-2h-4c0 1.1.89 2 2 2zm6-6v-5c0-3.07-1.64-5.64-4.5-6.32V4c0-.83-.67-1.5-1.5-1.5s-1.5.67-1.5 1.5v.68C7.63 5.36 6 7.92 6 11v5l-2 2v1h16v-1l-2-2z";

    /** magnifying glass, for the search box */
    public static final String SEARCH =
        "M15.5 14h-.79l-.28-.27A6.471 6.471 0 0 0 16 9.5 6.5 6.5 0 1 0 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z";

    /** eye, for the show / hide password toggle */
    public static final String EYE =
        "M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z";

    /** receipt, for the my requests page */
    public static final String RECEIPT =
        "M18 17H6v-2h12v2zm0-4H6v-2h12v2zm0-4H6V7h12v2zM3 22l1.5-1.5L6 22l1.5-1.5L9 22l1.5-1.5L12 22l1.5-1.5L15 22l1.5-1.5L18 22l1.5-1.5L21 22V2l-1.5 1.5L18 2l-1.5 1.5L15 2l-1.5 1.5L12 2l-1.5 1.5L9 2 7.5 3.5 6 2 4.5 3.5 3 2v20z";

    /** two arrows going back and forth, for the transactions page */
    public static final String SWAP =
        "M6.99 11L3 15l3.99 4v-3H14v-2H6.99v-3zM21 9l-3.99-4v3H10v2h7.01v3L21 9z";

    /** clock with a back arrow, for the history page */
    public static final String HISTORY =
        "M13 3c-4.97 0-9 4.03-9 9H1l3.89 3.89.07.14L9 12H6c0-3.87 3.13-7 7-7s7 3.13 7 7-3.13 7-7 7c-1.93 0-3.68-.79-4.94-2.06l-1.42 1.42C8.27 19.99 10.51 21 13 21c4.97 0 9-4.03 9-9s-4.03-9-9-9zm-1 5v5l4.28 2.54.72-1.21-3.5-2.08V8H12z";

    /** wallet, for the cash page */
    public static final String WALLET =
        "M21 18v1c0 1.1-.9 2-2 2H5c-1.11 0-2-.9-2-2V5c0-1.1.89-2 2-2h14c1.1 0 2 .9 2 2v1h-9c-1.11 0-2 .9-2 2v8c0 1.1.89 2 2 2h9zm-9-2h10V8H12v8zm4-2.5c-.83 0-1.5-.67-1.5-1.5s.67-1.5 1.5-1.5 1.5.67 1.5 1.5-.67 1.5-1.5 1.5z";

    /** two people, for the staff list page */
    public static final String PEOPLE =
        "M16 11c1.66 0 2.99-1.34 2.99-3S17.66 5 16 5c-1.66 0-3 1.34-3 3s1.34 3 3 3zm-8 0c1.66 0 2.99-1.34 2.99-3S9.66 5 8 5C6.34 5 5 6.34 5 8s1.34 3 3 3zm0 2c-2.33 0-7 1.17-7 3.5V19h14v-2.5c0-2.33-4.67-3.5-7-3.5zm8 0c-.29 0-.62.02-.97.05 1.16.84 1.97 1.97 1.97 3.45V19h6v-2.5c0-2.33-4.67-3.5-7-3.5z";

    /** stacked discs, for the products page */
    public static final String DATABASE =
        "M2 20h20v-4H2v4zm2-3h2v2H4v-2zM2 4v4h20V4H2zm4 3H4V5h2v2zm-4 7h20v-4H2v4zm2-3h2v2H4v-2z";

    /** small down arrow, for the user chip */
    public static final String CHEVRON =
        "M7 10l5 5 5-5z";

    /**
     * creates an icon node of the given size
     * <p>the path is scaled from the 24 x 24 grid with a scale transform, and an invisible square
     * keeps the bounds at the full grid, so every icon takes the same space in a layout
     * even when its shape does not fill the grid
     *
     * @param path one of the path constants of this class
     * @param size the side of the icon in pixels
     * @return a group with the icon, recolor its child with the css rule .icon { -fx-fill: ... }
     */
    public static Group of(String path, double size) {
        SVGPath shape = new SVGPath();
        shape.setContent(path);
        shape.getStyleClass().add("icon");

        Rectangle box = new Rectangle(GRID, GRID, Color.TRANSPARENT); // fixes the bounds to the grid
        box.setMouseTransparent(true);

        Group inner = new Group(box, shape);
        double scale = size / GRID;
        inner.getTransforms().add(new Scale(scale, scale, 0, 0)); // pivot at the corner, so the bounds start at 0

        return new Group(inner); // the outer group reports the scaled bounds to the layout
    }
}