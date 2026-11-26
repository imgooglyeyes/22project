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
 * saves and loads the user container
 *
 * <p>files live in src/data, and if a file is missing the matching one in src/templatedata is used instead
 *
 * <p>file formats, one user per line, fields separated by |
 * <ul>
 * <li>users.txt: username|password|firstName|lastName|userType|status
 * </ul>
 * status is active, pending or rejected, old lines with only 5 fields still load as active.
 *
 * @author Jian Carranza, Matthew Prado
 * @version 1.4
 * @since 1.2 the status field was added
 * @since 1.2 the status field was added
 * @see UserContainer
 * @see User
 */

public class UsersFileHandler {

	/** save file for the user container */
	public static final String USERDATA		= "fxtest2/src/data/users.txt";

	/** starting data for the user container, used when the save file does not exist */
	public static final String TEMPLATE_USERDATA	= "fxtest2/src/templatedata/users.txt";

	/**
	 * saves the user container, a null container is ignored to avoid errors
	 * 
	 * <p>an io error is printed and not thrown, so a failed save never crashes the app
	 *
	 * @param userData the user container to save
	 */
	public static void save(UserContainer userData) {
		try {
			// username|password|firstName|lastName|userType|status
			if (userData != null) {
				List<String> lines = new ArrayList<>();
				for (User u : userData.getUsers()) {
					if (u==null){
						System.out.println("Skipped a null user while saving.");
						continue;
					}
					lines.add(
						u.getUsername() + "|" +
						u.getPassword() + "|" +
						u.getFirstName() + "|" +
						u.getLastName() + "|" +
						u.getUserType() + "|" +
						u.getStatus()
					);
				}
				writeLines(USERDATA, lines);
				System.out.println("Saved user data.");
			}
		} catch (IOException e) {
			System.out.println("Something went wrong while saving user data.");
			e.printStackTrace();
		}
	}

	/**
	 * loads user data
	 * <p>a line with 5 fields is an old line and gets the active status
	 * <p>a bad line is skipped and printed, it does not stop the load
	 *
	 * @return user data as a UserContainer
	 */
	public static UserContainer load() {
		UserContainer load = new UserContainer();

		for (String line : readLines(USERDATA, TEMPLATE_USERDATA)) {
			// -1 keeps empty fields at the end of the line
			String[] parts = line.split("\\|", -1);

			try {
				if (parts.length != 5 && parts.length != 6) {
					System.out.println("Skipped a bad user data line: " + line);
					continue;
				}

				// old lines have no status, those accounts already worked so they stay active
				String status = parts.length == 6 ? parts[5].trim() : User.ACTIVE;

				if (!User.isValidStatus(status)) { // a typo in the file would lock the account out silently
					System.out.println("Skipped a user data line with an unknown status: " + line);
					continue;
				}

				load.add(new User(
					parts[0],						// username
					parts[1],						// password
					parts[2],						// firstName
					parts[3],						// lastName
					parts[4],						// userType
					status							// status
				));
			} catch (RuntimeException e) { // missing field
				System.out.println("Skipped a bad user data line: " + line);
			}
		}

		return load;
	}

	/**
	 * copies the template file over the save file
	 * <p>this only changes the file, the caller has to load it again to update the app
	 *
	 * @return true if the file was copied, false if something went wrong
	 * @since 1.0
	 */
	public static boolean resetToDefaults() {
		try {
			copyFile(TEMPLATE_USERDATA, USERDATA);
			System.out.println("Reset user data file to the template data.");
			return true;
		} catch (IOException e) {
			System.out.println("Something went wrong while resetting to the template data.");
			e.printStackTrace();
			return false;
		}
	}

	private static void copyFile(String from, String to) throws IOException {
		Path target = Paths.get(to);
		Path parent = target.getParent();
		if (parent != null) {
			Files.createDirectories(parent);
		}
		Files.copy(Paths.get(from), target, StandardCopyOption.REPLACE_EXISTING);
	}

	private static void writeLines(String file, List<String> lines) throws IOException {
		Path path = Paths.get(file);
		Path parent = path.getParent();

		if (parent != null) {
			Files.createDirectories(parent);
		}

		try (BufferedWriter writer = Files.newBufferedWriter(path)) {
			for (String line : lines) {
				writer.write(line);
				writer.newLine();
			}
		}
	}

	private static List<String> readLines(String file, String templateFile) {
		List<String> lines = new ArrayList<>();

		Path path = Paths.get(file);
		if (!Files.exists(path)) path = Paths.get(templateFile);

		try (BufferedReader reader = Files.newBufferedReader(path)) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.trim().isEmpty()) continue;
				lines.add(line);
			}
		} catch (IOException e) {
			System.out.println("Something went wrong while loading user data.");
			e.printStackTrace();
		}

		return lines;
	}
}