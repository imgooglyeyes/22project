package ui;

import shell.AppShell;
import shell.TopBar;


/**
 * a page that the search box of the top bar can filter
 *
 * <p>the app shell connects the search box to the open page when it implements this interface,
 * and disables the box on every other page. the box is cleared each time a page opens,
 * so a search never carries over to another page
 *
 * @author Shawn Alfred Padilla
 * @version 1.0
 * @see TopBar#setSearchTarget(Searchable)
 * @see AppShell#setPage(String, javafx.scene.Node)
 */
public interface Searchable {

    /**
     * called every time the text of the search box changes
     *
     * @param text what is typed in the search box, empty when it is cleared
     */
    void onSearch(String text);

    /**
     * gets the faint text shown in the empty search box
     *
     * @return the prompt for this page, "Search" by default
     */
    default String searchPrompt() {
        return "Search";
    }
}