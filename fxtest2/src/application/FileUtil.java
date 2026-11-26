package application;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * the small file helpers that the save file handlers share: read lines, write lines and copy a file
 *
 * <p>ProductsFileHandler and UsersFileHandler both used to have their own copy of these three methods.
 * the folder of a file is created when it is missing, and a path without a folder is allowed
 *
 * @author Shawn Alfred Padilla
 * @version 1.0
 * @see ProductsFileHandler
 * @see UsersFileHandler
 */
public class FileUtil {

    /** no one creates this class, it only has static methods */
    private FileUtil() {}

    /**
     * reads every non-blank line of a file, uses the template if the file does not exist
     * <p>an io error is printed and the lines read so far are returned
     *
     * @param file path of the save file
     * @param templateFile path of the template used if the save file is missing
     * @return the non-blank lines, empty if nothing could be read
     */
    public static List<String> readLines(String file, String templateFile) {
        List<String> lines = new ArrayList<>();

        Path path = Paths.get(file);
        if (!Files.exists(path)) path = Paths.get(templateFile); // the template SHOULD EXIST

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue; // skip blank lines
                lines.add(line);
            }
        } catch (IOException e) {
            System.out.println("Something went wrong while reading " + path);
            e.printStackTrace();
        }

        return lines;
    }

    /**
     * writes the lines to a file, replacing the old content
     *
     * @param file path of the file to write, its folder is created if missing
     * @param lines the lines to write
     * @throws IOException if the file can't be written
     */
    public static void writeLines(String file, List<String> lines) throws IOException {
        Path path = Paths.get(file);
        createFolderOf(path);

        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        }
    }

    /**
     * copies one file and replaces the target if it already exists
     *
     * @param from path of the file to copy
     * @param to path of the copy, its folder is created if missing
     * @throws IOException if the source can't be read or the target can't be written
     */
    public static void copyFile(String from, String to) throws IOException {
        Path target = Paths.get(to);
        createFolderOf(target);
        Files.copy(Paths.get(from), target, StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * makes sure the folder of a file exists
     *
     * @param path the path of the file
     * @throws IOException if the folder can't be created
     */
    private static void createFolderOf(Path path) throws IOException {
        Path parent = path.getParent();
        if (parent != null) Files.createDirectories(parent); // a path without a folder needs nothing
    }
}