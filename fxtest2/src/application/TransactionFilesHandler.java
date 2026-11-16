package application;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

// saves and loads transactions.txt
// one line per transaction, fields separated by |

public class TransactionFilesHandler {

    // same folder the shelf save button already uses
    private static final String FILE = "src/data/transactions.txt";

    // writes every transaction to the file, replacing the old content
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

    // reads the file into a new container, returns an empty one if the file doesn't exist yet
    public static TransactionContainer load() {
        TransactionContainer container = new TransactionContainer();
        Path path = Paths.get(FILE);

        if (!Files.exists(path)) return container; // first run, nothing saved yet

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue; // skip blank lines

                // -1 keeps empty fields at the end of the line
                String[] parts = line.split("\\|", -1);

                if (parts.length != 12) { // wrong number of fields, skip this line
                    System.out.println("Skipped a bad transaction line: " + line);
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

        return container;
    }

    // null becomes empty text so it can be saved
    private static String text(Object value) {
        return value == null ? "" : value.toString();
    }

    // empty text becomes null again when loading
    private static String nullIfEmpty(String value) {
        return value.isEmpty() ? null : value;
    }

    // empty text becomes null, anything else is read as a date and time
    private static LocalDateTime timeOrNull(String value) {
        return value.isEmpty() ? null : LocalDateTime.parse(value);
    }
}