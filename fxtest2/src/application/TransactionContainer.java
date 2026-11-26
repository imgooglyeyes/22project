package application;

import java.util.ArrayList;

/**
 * holds every sale transaction in the app
 *
 * <p>it works like ProductContainer but for transactions, screens and FarmSystem read from here.
 * transactions are kept in the order they were made, oldest first, and are never removed
 *
 * @author Shawn Alfred Padilla
 * @version 1.1
 * @since 1.1 the comments were turned into javadoc, getTransactionsByActor no longer uses try and catch
 * @see SaleTransaction
 * @see TransactionFilesHandler
 */
public class TransactionContainer {

    /** every transaction, oldest first */
    private ArrayList<SaleTransaction> transactionList;

    /** creates an empty container */
    public TransactionContainer() {
        this.transactionList = new ArrayList<>();
    }

    /**
     * adds a transaction at the end of the list
     *
     * @param transaction the transaction to add
     */
    public void add(SaleTransaction transaction) {
        transactionList.add(transaction);
    }

    /** @return the real list of transactions, oldest first, changing it changes the container */
    public ArrayList<SaleTransaction> getTransactions() {
        return transactionList;
    }

    /**
     * finds one transaction by its id
     *
     * @param id the id to look for
     * @return the transaction, or null if it does not exist
     */
    public SaleTransaction getTransactionById(int id) {
        for (SaleTransaction t : transactionList) {
            if (t.getId() == id) return t;
        }
        return null;
    }

    /**
     * lists only the requests made by one customer
     *
     * @param username the username of the customer
     * @return a new list with the requests of the customer, oldest first
     */
    public ArrayList<SaleTransaction> getTransactionsByRequester(String username) {
        ArrayList<SaleTransaction> result = new ArrayList<>();
        for (SaleTransaction t : transactionList) {
            if (t.getRequester().equals(username)) result.add(t);
        }
        return result;
    }

    /**
     * returns only the requests that have been acted upon by the actor
     * <p>confirmedBy and voidedBy can be null, which makes equals throw, so each check has its own try and catch.
     * it is the same rule as FarmSystem#countHandledBy, so the numbers match the rows of the history page
     *
     * @param username the username of the clerk or manager
     * @return a new list with the transactions they confirmed or voided, oldest first
     */
    public ArrayList<SaleTransaction> getTransactionsByActor(String username) {
        ArrayList<SaleTransaction> result = new ArrayList<>();
        for (SaleTransaction t : transactionList) {
            try { // causes an error if confirmed by is null
            	if (t.getConfirmedBy().equals(username)) {
            		result.add(t);
            		continue; // already added, no need to check more
            	}
            } catch (Exception e) {}
            try { // causes an error if voided by is null
            	if (t.getVoidedBy().equals(username)) { // there's the chance that it was not confirmed by but instead voided by the actor
            		result.add(t);
            		continue; // already added, no need to check more
            	}
            } catch (Exception e) {}
        }
        return result;
    }
}