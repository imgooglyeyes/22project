package application;

import java.util.ArrayList;

// holds every sale transaction in the app
// works like ProductContainer but for transactions
// screens read from here

public class TransactionContainer {
    private ArrayList<SaleTransaction> transactionList;

    public TransactionContainer() { // empty list
        this.transactionList = new ArrayList<>();
    }

    public void add(SaleTransaction transaction) {
        transactionList.add(transaction);
    }

    // returns every transaction, oldest first
    public ArrayList<SaleTransaction> getTransactions() {
        return transactionList;
    }

    // finds one transaction by its id, null if it doesn't exist
    public SaleTransaction getTransactionById(int id) {
        for (SaleTransaction t : transactionList) {
            if (t.getId() == id) return t;
        }
        return null;
    }

    // returns only the requests made by one customer 
    public ArrayList<SaleTransaction> getTransactionsByRequester(String username) {
        ArrayList<SaleTransaction> result = new ArrayList<>();
        for (SaleTransaction t : transactionList) {
            if (t.getRequester().equals(username)) result.add(t);
        }
        return result;
    }
    
    // returns only the requests than have been acted upon by the actor
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