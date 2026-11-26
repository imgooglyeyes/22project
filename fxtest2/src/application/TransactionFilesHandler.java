package application;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

/**
 * saves and loads the transaction container
 *
 * <p>the file lives in src/data, there is no template file, so a missing file just means no transactions yet.
 * the reset to template data does not touch it
 *
 * <p>file format, one transaction per line, fields separated by |
 * <ul>
 * <li>transactions.txt: id|productId|productName|unitPrice|quantity|requester|status|confirmedBy|voidedBy|requestedAt|confirmedAt|voidedAt
 * </ul>
 * an empty field means null, a line with the wrong number of fields is skipped and printed
 *
 * <p>load prints the full path it reads and how many lines it loaded, so a wrong path or a rejected file is easy to spot
 *
 * @author Shawn Alfred Padilla
 * @version 1.2
 * @since 1.1 the comments were turned into javadoc
 * @since 1.2 load prints the path, a missing file and the number of loaded transactions
 * @see TransactionContainer
 * @see SaleTransaction
 */
public class TransactionFilesHandler {

    /** save file for the transaction container, in the same folder as the product files */
    private static final String FILE = "fxtest2/src/data/transactions.txt";

    /** how many fields one saved line has */
    private static final int FIELD_COUNT = 12;

    /**
     * saves every transaction, replacing the old content of the file
     * <p>an io error is printed and not thrown, so a failed save never crashes the app
     *
     * @param container the transactions to save
     */
    public static void save(TransactionContainer container) {
        Path path = Paths.get(FILE);

        try {
            Files.createDirectories(path.getParent()); // makes sure the folder exists

            try (BufferedWriter writer = Files.newBufferedWriter(path)) {
                for (SaleTransaction t : container.getTransactions()) {
                    // format: id|productId|productName|unitPrice|quantity|requester|status|confirmedBy|voidedBy|requestedAt|confirmedAt|voidedAt
                    writer.write(
                        t.getId() + "|" +
                        t.getProductId() + "|" +
                        t.getProductName() + "|" +
                        t.getUnitPrice() + "|" +
                        t.getQuantity() + "|" +
                        t.getRequester() + "|" +
                        t.getStatus() + "|" +
                        text(t.getConfirmedBy()) + "|" +
                        text(t.getVoidedBy()) + "|" +
                        text(t.getRequestedAt()) + "|" +
                        text(t.getConfirmedAt()) + "|" +
                        text(t.getVoidedAt())
                    );
                    writer.newLine();
                }
            }

            System.out.println("Saved transactions.");
        } catch (IOException e) {
            System.out.println("Something went wrong while saving transactions.");
            e.printStackTrace();
        }
    }

    /**
     * loads the transactions from the file
     * <p>a bad line is skipped and printed, it does not stop the load
     *
     * @return a new container with the saved transactions, an empty one if the file does not exist yet
     */
    public static TransactionContainer load() {
        TransactionContainer container = new TransactionContainer();
        Path path = Paths.get(FILE);

        // the full path shows which folder the app really reads from
        System.out.println("Loading transactions from " + path.toAbsolutePath());

        if (!Files.exists(path)) { // first run, nothing saved yet
            System.out.println("Transactions file not found, starting with no transactions.");
            return container;
        }

        int readLines = 0; // every non blank line we tried to read

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue; // skip blank lines
                readLines++;

                // -1 keeps empty fields at the end of the line
                String[] parts = line.split("\\|", -1);

                if (parts.length != FIELD_COUNT) { // wrong number of fields, skip this line
                    System.out.println("Skipped a bad transaction line (" + parts.length + " fields): " + line);
                    continue;
                }

                try {
                    container.add(new SaleTransaction(
                        Integer.parseInt(parts[0]),
                        Integer.parseInt(parts[1]),
                        parts[2],
                        Double.parseDouble(parts[3]),
                        Integer.parseInt(parts[4]),
                        parts[5],
                        parts[6],
                        nullIfEmpty(parts[7]),
                        nullIfEmpty(parts[8]),
                        timeOrNull(parts[9]),
                        timeOrNull(parts[10]),
                        timeOrNull(parts[11])
                    ));
                } catch (RuntimeException e) { // bad number or bad time, skip this line
                    System.out.println("Skipped a bad transaction line: " + line);
                }
            }
        } catch (IOException e) {
            System.out.println("Something went wrong while loading transactions.");
            e.printStackTrace();
        }

        System.out.println("Loaded " + container.getTransactions().size() + " of " + readLines + " transaction lines.");
        return container;
    }

    /**
     * turns a value into text for the save file
     *
     * @param value the value to write, can be null
     * @return the value as text, or an empty text for null
     */
    private static String text(Object value) {
        return value == null ? "" : value.toString();
    }

    /**
     * turns an empty text back into null when loading
     *
     * @param value the text read from the file
     * @return the text, or null if it is empty
     */
    private static String nullIfEmpty(String value) {
        return value.isEmpty() ? null : value;
    }

    /**
     * reads a saved time
     *
     * @param value the text read from the file
     * @return null if the text is empty, otherwise the date and time
     */
    private static LocalDateTime timeOrNull(String value) {
        return value.isEmpty() ? null : LocalDateTime.parse(value);
    }
}