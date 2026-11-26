package application;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Locale;

import javafx.scene.image.Image;

/**
 * copies, finds and deletes the picture files of the products
 *
 * <p>a picture is saved in src/data/images under a generated name, and only that name is stored in the product.
 * a name is looked up in src/data/images first, then in src/templatedata/images, so the pictures of the
 * template products work the same way the template save files do
 *
 * <p>loaded pictures are cached by name, a new import always gets a new name so the cache can't go stale.
 * only problems are printed, nothing here crashes the app
 *
 * @author Shawn Alfred Padilla
 * @version 1.0
 * @see Product#getImage()
 * @see FarmSystem
 */
public class ProductImages {

    public static final String FOLDER = "fxtest2/src/data/images/";
    public static final String TEMPLATE_FOLDER = "fxtest2/src/templatedata/images/";
    public static final String[] EXTENSIONS = { "png", "jpg", "jpeg", "gif" };

    private static final double MAX_WIDTH = 440;
    private static final double MAX_HEIGHT = 240;
    private static final HashMap<String, Image> cache = new HashMap<>();

    private ProductImages() {}

    public static String importFile(File source) throws IOException {
        String name = source.getName().toLowerCase(Locale.ROOT);
        int dot = name.lastIndexOf('.');
        String extension = dot < 0 ? "" : name.substring(dot + 1);

        if (!isSupported(extension)) throw new IOException("Pick a png, jpg or gif image");

        Image check = new Image(source.toURI().toString(), 64, 64, true, false);
        if (check.isError()) throw new IOException("This file is not a valid image");

        Path folder = Paths.get(FOLDER);
        Files.createDirectories(folder);

        Path target = Files.createTempFile(folder, "img-", "." + extension);
        Files.copy(source.toPath(), target, StandardCopyOption.REPLACE_EXISTING);

        return target.getFileName().toString();
    }

    public static Image load(String fileName) {
        if (!isSafe(fileName)) return null;

        Image cached = cache.get(fileName);
        if (cached != null) return cached;

        Path path = find(fileName);
        if (path == null) return null;

        Image image = new Image(path.toUri().toString(), MAX_WIDTH, MAX_HEIGHT, true, true);
        if (image.isError()) return null;

        cache.put(fileName, image);
        return image;
    }

    public static void delete(String fileName) {
        if (!isSafe(fileName)) return;

        cache.remove(fileName);

        try {
            Files.deleteIfExists(Paths.get(FOLDER + fileName));
        } catch (IOException e) {
            System.out.println("Something went wrong while deleting the image " + fileName);
        }
    }

    private static Path find(String fileName) {
        Path saved = Paths.get(FOLDER + fileName);
        if (Files.exists(saved)) return saved;

        Path template = Paths.get(TEMPLATE_FOLDER + fileName);
        return Files.exists(template) ? template : null;
    }

    private static boolean isSafe(String fileName) {
        return fileName != null
            && !fileName.isEmpty()
            && !fileName.contains("/")
            && !fileName.contains("\\")
            && !fileName.contains("..");
    }

    private static boolean isSupported(String extension) {
        for (String supported : EXTENSIONS) {
            if (supported.equals(extension)) return true;
        }
        return false;
    }
}
