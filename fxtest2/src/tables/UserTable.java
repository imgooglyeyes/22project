package tables;

import application.User;
import pages.StaffPage;

/**
 * the table of users, used by the members and the sign-ups tabs of the staff page
 *
 * @author Shawn Alfred Padilla
 * @version 1.0
 * @see DataTable
 * @see StaffPage
 */
public class UserTable extends DataTable<User> {

    /** creates the table with its columns */
    public UserTable() {
        addColumn("Username", User::getUsername, 150);
        addColumn("First Name", User::getFirstName, 180);
        addColumn("Last Name", User::getLastName, 180);
        addColumn("Role", User::getUserType, 100);
    }
}