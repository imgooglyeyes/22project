package tables;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import application.Money;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 * the base of every table of the app, a table view with a filter, click to sort and an empty message
 *
 * <p>the pages give it rows with setRows() and a filter with setFilter(), the table keeps its own copy of the rows,
 * so the real containers are never sorted or changed by it. subclasses add their columns with addColumn() and
 * addMoneyColumn(), so every table of the app looks and behaves the same
 *
 * <p>a column is not tied to a property, so the table can't see a change inside a row by itself,
 * setRows() calls refresh() to redraw the cells
 *
 * @author Shawn Alfred Padilla
 * @version 1.2
 * @since 1.1 the garbled peso sign in the comments was replaced with plain words
 * @since 1.2 the money text comes from Money
 * @param <T> the type of the rows
 * @see TransactionTable
 * @see ProductTable
 * @see UserTable
 * @see Money
 */
public class DataTable<T> extends TableView<T> {

    /** the rows given by the page, in the order they were given */
    private final ObservableList<T> rows = FXCollections.observableArrayList();
    /** the rows that pass the filter */
    private final FilteredList<T> filtered = new FilteredList<>(rows, row -> true);

    /** creates an empty table, the columns are added by the subclass */
    public DataTable() {
        // the sorted list follows the column the person clicked, with no click it keeps the given order
        SortedList<T> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(comparatorProperty());
        setItems(sorted);

        setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY); // the columns share the width, no sideways scroll
        setEmptyText("Nothing to show");
    }

    /**
     * replaces every row, the selection is cleared
     *
     * @param newRows the rows to show, the list is copied
     */
    public void setRows(List<T> newRows) {
        getSelectionModel().clearSelection(); // the old selected row may not exist anymore
        rows.setAll(newRows);
        refresh(); // the rows can be the same objects with new values, so the cells are redrawn
    }

    /**
     * hides the rows that do not pass the filter
     *
     * @param filter the rule for the rows to keep, null keeps every row
     */
    public void setFilter(Predicate<T> filter) {
        // a new lambda every time, so the list filters again even if the page passes the same method reference
        filtered.setPredicate(row -> filter == null || filter.test(row));
    }

    /**
     * sets the message shown when there are no rows to show
     *
     * @param text the message
     */
    public void setEmptyText(String text) {
        setPlaceholder(new Label(text));
    }

    /** @return the selected row, or null if nothing is selected */
    public T getSelected() {
        return getSelectionModel().getSelectedItem();
    }

    /** @return how many rows pass the filter */
    public int getShownCount() {
        return filtered.size();
    }

    /** @return how many rows the table holds, filtered or not */
    public int getTotalCount() {
        return rows.size();
    }

    /**
     * adds a column
     *
     * @param title the text of the column header
     * @param getter reads the value of the column from a row
     * @param width the preferred width, the columns share the real width in this ratio
     * @param <V> the type of the value, numbers keep their own sort order
     * @return the new column, so a subclass can change how its cells look
     */
    protected <V> TableColumn<T, V> addColumn(String title, Function<T, V> getter, double width) {
        TableColumn<T, V> column = new TableColumn<>(title);
        column.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<V>(getter.apply(cell.getValue())));
        column.setPrefWidth(width);
        getColumns().add(column);
        return column;
    }

    /**
     * adds a column of money, shown with the peso sign and two decimals, and sorted by the number
     * <p>a negative amount gets a minus sign in front of the peso sign, Money does the formatting
     *
     * @param title the text of the column header
     * @param getter reads the amount from a row
     * @param width the preferred width
     * @return the new column
     */
    protected TableColumn<T, Double> addMoneyColumn(String title, Function<T, Double> getter, double width) {
        TableColumn<T, Double> column = addColumn(title, getter, width);

        column.setCellFactory(col -> new TableCell<T, Double>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);

                if (empty || amount == null) {
                    setText(null);
                    return;
                }

                setText(Money.format(amount));
            }
        });
        return column;
    }
}