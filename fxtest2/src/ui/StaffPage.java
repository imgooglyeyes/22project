package ui;

import java.util.ArrayList;

import application.User;
import application.UserContainer;
import application.UsersFileHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;

/**
 * the page where a manager sees the staff and decides on the accounts that wait for approval
 *
 * <p>it has two tabs in one segmented toggle. members lists the active managers first and then the active clerks,
 * and the manager picks one to open their dashboard or their transaction history. sign-ups lists the pending accounts
 * (the clerk and manager accounts made in the sign up form), an approved account becomes active and can log in,
 * a rejected account stays in the save file as rejected, so its username can't be taken by someone else
 *
 * <p>every decision is saved to users.txt right away and answered with a toast.
 * the search box of the top bar filters the open tab.
 * the page is created again every time it opens, so the lists are always up to date
 *
 * @author Jian Carranza, Shawn Alfred Padilla
 * @version 2.2
 * @since 2.0 the page replaces the old staff list scene and the old sign up requests scene
 * @since 2.1 the open dashboard button and the toasts were added
 * @since 2.2 show and owner moved to Ui
 * @see UserTable
 * @see Ui
 * @see SceneManager#showStaffList()
 * @see SceneManager#showSignUpRequests()
 * @see SceneManager#showStaffDashboard(User)
 * @see UsersFileHandler
 */
public class StaffPage extends BorderPane implements Searchable {

    /** the two tabs */
    private static final String TAB_MEMBERS = "Members";
    private static final String TAB_SIGN_UPS = "Sign-Ups";

    private final SceneManager manager;
    private final UserContainer userData;

    private final PageToolbar toolbar = new PageToolbar("Staff");
    private final SegmentedToggle tabToggle = new SegmentedToggle(TAB_MEMBERS, TAB_SIGN_UPS);

    private final UserTable membersTable = new UserTable();
    private final UserTable signUpTable = new UserTable();
    /** holds the table of the open tab */
    private final StackPane body = new StackPane();

    /** the buttons of the members tab and of the sign-ups tab, only the ones of the open tab are shown */
    private final Button dashboardButton = new Button("Open Dashboard");
    private final Button historyButton = new Button("View History");
    private final Button approveButton = new Button("Approve");
    private final Button rejectButton = new Button("Reject");

    /** the typed search text in lowercase, empty shows everything */
    private String query = "";

    /**
     * creates the page
     *
     * @param manager the scene manager, answers the role checks, opens the dashboard and the history, and shows toasts
     * @param userData every user, the staff are listed and the status of pending accounts is changed here
     * @param startOnSignUps true to open on the sign-ups tab, false to open on the members tab
     */
    public StaffPage(SceneManager manager, UserContainer userData, boolean startOnSignUps) {
        this.manager = manager;
        this.userData = userData;

        setPadding(new Insets(24));

        rejectButton.getStyleClass().add("danger");
        historyButton.getStyleClass().add("secondary");

        membersTable.setEmptyText("There are no staff members yet");
        signUpTable.setEmptyText("There are no sign-ups waiting");

        // each button needs a selected row in its own table
        dashboardButton.disableProperty().bind(membersTable.getSelectionModel().selectedItemProperty().isNull());
        historyButton.disableProperty().bind(membersTable.getSelectionModel().selectedItemProperty().isNull());
        approveButton.disableProperty().bind(signUpTable.getSelectionModel().selectedItemProperty().isNull());
        rejectButton.disableProperty().bind(signUpTable.getSelectionModel().selectedItemProperty().isNull());

        dashboardButton.setOnAction(event -> {
            User target = membersTable.getSelected();
            if (target != null) manager.showStaffDashboard(target); // does nothing unless the manager can see that dashboard
        });
        historyButton.setOnAction(event -> {
            User target = membersTable.getSelected();
            if (target != null) manager.showHistory(target); // does nothing unless the manager can see that history
        });
        approveButton.setOnAction(event -> decide(User.ACTIVE));

        // a rejected account can't log in, so ask before doing it
        rejectButton.setOnAction(event -> {
            User user = signUpTable.getSelected();
            if (user == null) return;

            if (Dialogs.confirm(Ui.windowOf(this), "Reject the account " + user.getUsername() + "?\nThey will not be able to log in.")) {
                decide(User.REJECTED);
            }
        });

        toolbar.addFilter(tabToggle);
        toolbar.addAction(dashboardButton, historyButton, approveButton, rejectButton);

        setTop(toolbar);
        setCenter(body);
        BorderPane.setMargin(body, new Insets(16, 0, 0, 0));

        if (startOnSignUps) tabToggle.select(TAB_SIGN_UPS); // the change listener is set after, so it does not draw twice
        tabToggle.setOnChange(this::showTab);

        refresh();
    }

    // ---------- search ----------

    /**
     * filters the rows of the open tab by the text of the search box
     *
     * @param text what is typed in the search box
     */
    @Override
    public void onSearch(String text) {
        query = text == null ? "" : text.trim().toLowerCase();
        showTab();
    }

    /** @return the faint text of the search box on this page */
    @Override
    public String searchPrompt() {
        return "Search by name";
    }

    // ---------- drawing ----------

    /** loads both lists again from the user container */
    private void refresh() {
        // only active accounts are staff, a pending or rejected account can't log in so it has no history
        ArrayList<User> members = new ArrayList<>();
        for (User user : userData.getUsersByType(User.MANAGER)) { // managers first
            if (user.isActive()) members.add(user);
        }
        for (User user : userData.getUsersByType(User.CLERK)) { // clerks next
            if (user.isActive()) members.add(user);
        }
        membersTable.setRows(members);

        ArrayList<User> pending = new ArrayList<>();
        for (User user : userData.getUsers()) {
            if (User.PENDING.equals(user.getStatus())) pending.add(user);
        }
        signUpTable.setRows(pending);

        showTab();
    }

    /** shows the table and the buttons of the open tab, and updates the caption under the title */
    private void showTab() {
        boolean signUps = TAB_SIGN_UPS.equals(tabToggle.getSelected());

        body.getChildren().setAll(signUps ? signUpTable : membersTable);
        Ui.show(dashboardButton, !signUps);
        Ui.show(historyButton, !signUps);
        Ui.show(approveButton, signUps);
        Ui.show(rejectButton, signUps);

        membersTable.setFilter(this::matches);
        signUpTable.setFilter(this::matches);

        if (signUps) {
            toolbar.setSubtitle(signUpTable.getShownCount() + " waiting for approval");
        } else {
            int count = membersTable.getShownCount();
            toolbar.setSubtitle(count + (count == 1 ? " staff member" : " staff members"));
        }
    }

    /**
     * checks the search text against the username and the names
     *
     * @param user the user to check
     * @return true if the search is empty or matches
     */
    private boolean matches(User user) {
        if (query.isEmpty()) return true;

        return user.getUsername().toLowerCase().contains(query)
            || user.getFirstName().toLowerCase().contains(query)
            || user.getLastName().toLowerCase().contains(query);
    }

    // ---------- actions ----------

    /**
     * sets the status of the selected sign-up, saves the users and shows a toast
     * <p>nothing happens unless the current user can approve sign ups and the account is still pending,
     * so a stale selection can't change an account that was already decided
     *
     * @param newStatus the new status, active to approve or rejected to reject
     */
    private void decide(String newStatus) {
        if (!manager.canApproveSignUps()) return;

        User user = signUpTable.getSelected();
        if (user == null || !User.PENDING.equals(user.getStatus())) return;

        String name = user.getUsername(); // read before the refresh clears the selection

        user.setStatus(newStatus);
        UsersFileHandler.save(userData); // saved right away so the decision survives a restart

        refresh();
        manager.toast(newStatus.equals(User.ACTIVE) ? name + " was approved" : name + " was rejected");
    }
}