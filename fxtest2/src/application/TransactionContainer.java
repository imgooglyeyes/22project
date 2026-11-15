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
}