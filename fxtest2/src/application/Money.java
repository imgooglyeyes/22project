package application;

/**
 * formats money as text for the screens, so the peso text is written in one place
 *
 * <p>the tables, the cards, the dashboard tiles and the models all call format(),
 * so every amount of the app looks the same
 *
 * @author Shawn Alfred Padilla
 * @version 1.0
 * @see Product#getFormattedPrice()
 * @see SaleTransaction#getFormattedTotal()
 */
public class Money {

    /** no one creates this class, it only has a static method */
    private Money() {}

    /**
     * formats an amount with the peso sign and two decimals, a negative amount gets a minus sign in front of the peso sign
     * <p>\u20B1 is the peso sign, written as an escape so the file encoding can't break it
     *
     * @param amount the amount to format
     * @return the amount as text, example 150.00 with the peso sign in front
     */
    public static String format(double amount) {
        String text = String.format("\u20B1%.2f", Math.abs(amount));
        return amount < 0 ? "-" + text : text;
    }
}