package ui;

import java.io.IOException;
import java.io.InputStream;

import javafx.scene.text.Font;

/**
 * loads the plus jakarta sans font files so css can use them
 *
 * <p>only the four static weights from the ui plan are loaded: regular (400), medium (500),
 * semibold (600) and bold (700). javafx does not handle variable fonts well, so the
 * static files are used and the variable files in the same folder are ignored
 *
 * <p>javafx css only understands normal and bold for -fx-font-weight, so the medium and
 * semibold weights are picked by their own family names in css, for example
 * -fx-font-family: "Plus Jakarta Sans SemiBold". the names printed by load() show
 * the exact names javafx registered
 *
 * <p>a missing font never crashes the app, the css fallback (segoe ui) is used instead
 *
 * @author Shawn Alfred Padilla
 * @version 1.0
 * @see Main
 */
public class Fonts {

    /** the family name used in css */
    public static final String FAMILY = "Plus Jakarta Sans";

    /** folder of the static font files, relative to the class path root (bin) */
    private static final String FOLDER = "/application/fonts/Plus_Jakarta_Sans/static/";

    /** the four weights the ui uses: 400, 500, 600 and 700 */
    private static final String[] FILES = {
        "PlusJakartaSans-Regular.ttf",
        "PlusJakartaSans-Medium.ttf",
        "PlusJakartaSans-SemiBold.ttf",
        "PlusJakartaSans-Bold.ttf"
    };

    /** true once the fonts were loaded, so calling load() twice does nothing */
    private static boolean loaded = false;

    /**
     * loads the four font files and prints the registered font names once to verify them
     * <p>call this from Main before the first scene is created
     *
     * @return true if all four files were loaded, false if any file is missing or broken
     */
    public static boolean load() {
        if (loaded) return true; // already done, the names are printed only once

        int count = 0;

        for (String file : FILES) {
            // the size is only a default, css sets the real size
            try (InputStream stream = Fonts.class.getResourceAsStream(FOLDER + file)) {
                if (stream == null) { // eclipse did not copy the file to bin, or the path is wrong
                    System.out.println("Font file not found: " + FOLDER + file);
                    continue;
                }

                Font font = Font.loadFont(stream, 14);
                if (font == null) { // the file was found but javafx could not read it
                    System.out.println("Font could not be loaded: " + file);
                    continue;
                }
                count++;
            } catch (IOException e) { // only the stream closing can fail here
                System.out.println("Something went wrong while loading the font " + file);
                e.printStackTrace();
            }
        }

        // prints the names javafx registered, use these exact names in css
        System.out.println("Loaded " + count + " of " + FILES.length + " " + FAMILY + " fonts: "
            + Font.getFontNames(FAMILY));

        loaded = count == FILES.length;
        return loaded;
    }
}