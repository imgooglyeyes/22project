package ui;

import application.User;
import application.UserContainer;
import application.UsersFileHandler;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * the log in and sign up page, a white card on the forest green gradient with a tab toggle
 *
 * <p>log in works like the old login scene: only active accounts can log in, a pending or
 * rejected account gets its own message so the person knows the password was right,
 * and a successful login opens the home page of the role
 *
 * <p>sign up works like the old sign up scene: a customer account is active at once, a clerk or
 * manager account is saved as pending until a manager approves it. the button stays disabled while
 * a field is empty, contains |, the username is taken or the two passwords are different.
 * after a sign up the page switches to the log in tab and shows a note, so no popup is needed
 *
 * <p>every message is shown inside the card and goes away when the person types again.
 * enter submits from any field, the show / hide eye is in RevealPasswordField.
 * the page is created again every time it opens, so the fields always start empty
 *
 * @author Jian Carranza, Shawn Alfred Padilla
 * @version 1.2
 * @since 1.1 a login opens the home page of the role
 * @since 1.2 the empty message binding comes from Ui.collapseWhenEmpty
 * @see SceneManager#showLogin()
 * @see SceneManager#showSignUp()
 * @see RevealPasswordField
 * @see UserContainer#isUsernameTaken(String)
 */
public class AuthPage extends StackPane {

    /** shown when no account matches the username and password */
    private static final String INVALID_LOGIN = "Invalid username or password.";

    /** the account types a person can pick when signing up */
    private static final String[] ROLES = { User.CUSTOMER, User.CLERK, User.MANAGER };

    private final SceneManager manager;
    private final UserContainer userData;
    /** the shared user object, login copies the account into it */
    private final User currentUser;

    private final ToggleButton logInTab = new ToggleButton("Log In");
    private final ToggleButton signUpTab = new ToggleButton("Sign Up");
    private final Label subtitle = new Label();

    /** holds the form of the open tab */
    private final StackPane formHost = new StackPane();

    // log in form
    private final TextField logInUsername = new TextField();
    private final RevealPasswordField logInPassword = new RevealPasswordField("Password");
    private final Label logInSuccess = new Label();
    private final Label logInError = new Label();
    private final Button logInButton = new Button("Log In");
    private final VBox logInForm;

    // sign up form
    private final TextField signUpUsername = new TextField();
    private final TextField firstName = new TextField();
    private final TextField lastName = new TextField();
    private final RevealPasswordField signUpPassword = new RevealPasswordField("Password");
    private final RevealPasswordField signUpConfirm = new RevealPasswordField("Confirm password");
    private final ComboBox<String> roleBox = new ComboBox<>(FXCollections.observableArrayList(ROLES));
    private final Label roleNote = new Label();
    private final Label signUpProblem = new Label();
    private final Button signUpButton = new Button("Sign Up");
    private final VBox signUpForm;

    /**
     * creates the page
     *
     * @param manager the scene manager, opens the home page and answers the role checks
     * @param userData every user, log in reads it and sign up adds to it
     * @param currentUser the user who is not yet logged in
     * @param startOnSignUp true to open on the sign up tab, false to open on the log in tab
     */
    public AuthPage(SceneManager manager, UserContainer userData, User currentUser, boolean startOnSignUp) {
        this.manager = manager;
        this.userData = userData;
        this.currentUser = currentUser;

        getStyleClass().add("auth-page");

        this.logInForm = buildLogInForm();
        this.signUpForm = buildSignUpForm();

        // the two tabs are in one group, so only one is selected at a time
        ToggleGroup tabs = new ToggleGroup();
        logInTab.setToggleGroup(tabs);
        signUpTab.setToggleGroup(tabs);
        logInTab.setMaxWidth(Double.MAX_VALUE);
        signUpTab.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(logInTab, Priority.ALWAYS);
        HBox.setHgrow(signUpTab, Priority.ALWAYS);

        HBox segmented = new HBox(4, logInTab, signUpTab);
        segmented.getStyleClass().add("segmented");

        // clicking the open tab again would unselect it, so it is selected again
        tabs.selectedToggleProperty().addListener((obs, old, now) -> {
            if (now == null) {
                if (old != null) old.setSelected(true);
                return;
            }
            showTab(now == signUpTab);
        });

        Label title = new Label("Welcome to ShopeeFarm");
        title.getStyleClass().add("page-title");
        title.setWrapText(true);

        subtitle.getStyleClass().add("muted");
        subtitle.setWrapText(true);

        VBox card = new VBox(16, title, subtitle, segmented, formHost);
        card.getStyleClass().addAll("card", "auth-card");
        card.setMaxWidth(440);
        card.setMaxHeight(Region.USE_PREF_SIZE); // keeps the card at its own height instead of stretching it

        (startOnSignUp ? signUpTab : logInTab).setSelected(true);

        // the card is centered, and the page scrolls if the sign up form is taller than the window
        StackPane center = new StackPane(card);
        center.setPadding(new Insets(24));

        ScrollPane scrollPane = new ScrollPane(center);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setHbarPolicy(ScrollBarPolicy.NEVER);

        getChildren().add(scrollPane);
    }

    // ---------- tabs ----------

    /**
     * shows the form of a tab and puts the cursor in its first field
     * <p>the focus is requested later because the page may not be in a scene yet when it is created
     *
     * @param signUp true for the sign up form, false for the log in form
     */
    private void showTab(boolean signUp) {
        subtitle.setText(signUp
            ? "Customers can log in right away, staff accounts need a manager to approve them."
            : "Log in to request products, or to manage the farm if you are staff."
        );

        formHost.getChildren().setAll(signUp ? signUpForm : logInForm);

        Node first = signUp ? signUpUsername : logInUsername;
        Platform.runLater(first::requestFocus);
    }

    // ---------- log in ----------

    /**
     * builds the log in form
     *
     * @return the form with its fields, messages and button
     */
    private VBox buildLogInForm() {
        logInUsername.setPromptText("Username");

        showOnlyWhenFilled(logInSuccess, "success-text");
        showOnlyWhenFilled(logInError, "error-text");

        logInButton.setMaxWidth(Double.MAX_VALUE);

        // disabled while the username or the password is empty or contains |
        logInButton.disableProperty().bind(
            invalid(logInUsername.textProperty()).or(invalid(logInPassword.textProperty()))
        );

        logInButton.setOnAction(event -> submitLogIn());
        logInUsername.setOnAction(event -> submitLogIn());
        logInPassword.setOnSubmit(this::submitLogIn);

        // a message about the last try is old once the person types again
        logInUsername.textProperty().addListener((obs, old, now) -> logInError.setText(""));
        logInPassword.textProperty().addListener((obs, old, now) -> logInError.setText(""));

        return new VBox(12,
            field("Username", logInUsername),
            field("Password", logInPassword),
            logInSuccess,
            logInError,
            logInButton
        );
    }

    /**
     * checks the typed username and password, and logs in if they match an active account
     * <p>nothing happens while the button is disabled, so enter can't skip the checks
     */
    private void submitLogIn() {
        if (logInButton.isDisable()) return;

        String username = logInUsername.getText();
        String password = logInPassword.getText();

        for (User u : userData.getUsers()) { // check every known user
            if (u.getUsername().equals(username) && u.getPassword().equals(password)) {

                if (!u.isActive()) { // right password, but the account can't be used yet
                    logInSuccess.setText("");
                    logInError.setText(u.getStatus().equals(User.PENDING)
                        ? "This account is waiting for a manager to approve it."
                        : "This account was rejected by a manager."
                    );
                    return;
                }

                currentUser.replaceWith(u); // change current user with new log in
                manager.showHome(); // lands on the home page of the role (staff tiles or the customer welcome)
                return;
            }
        }

        logInSuccess.setText(""); // the sign up note is old now
        logInError.setText(INVALID_LOGIN);
    }

    // ---------- sign up ----------

    /**
     * builds the sign up form
     * <p>the account type is customer by default, it is the only type that needs no approval
     *
     * @return the form with its fields, notes and button
     */
    private VBox buildSignUpForm() {
        signUpUsername.setPromptText("Username");
        firstName.setPromptText("First name");
        lastName.setPromptText("Last name");

        roleBox.setValue(User.CUSTOMER);
        roleBox.setMaxWidth(Double.MAX_VALUE);

        // a note that only shows for the types that need a manager to approve them
        roleNote.textProperty().bind(Bindings.createStringBinding(
            () -> User.CUSTOMER.equals(roleBox.getValue())
                ? ""
                : "A manager has to approve this account before you can log in.",
            roleBox.valueProperty()
        ));
        showOnlyWhenFilled(roleNote, "caption");
        showOnlyWhenFilled(signUpProblem, "error-text");

        // true while the username belongs to someone else (or is the reserved guest name)
        BooleanBinding usernameTaken = Bindings.createBooleanBinding(
            () -> userData.isUsernameTaken(signUpUsername.getText()),
            signUpUsername.textProperty()
        );

        // true while the two password fields are not the same
        BooleanBinding passwordsDiffer = Bindings.createBooleanBinding(
            () -> !signUpPassword.getText().equals(signUpConfirm.getText()),
            signUpPassword.textProperty(), signUpConfirm.textProperty()
        );

        // true while any field contains the separator of the save file
        BooleanBinding hasBar = Bindings.createBooleanBinding(
            () -> containsBar(signUpUsername.getText())
                || containsBar(firstName.getText())
                || containsBar(lastName.getText())
                || containsBar(signUpPassword.getText())
                || containsBar(signUpConfirm.getText()),
            signUpUsername.textProperty(), firstName.textProperty(), lastName.textProperty(),
            signUpPassword.textProperty(), signUpConfirm.textProperty()
        );

        // tells the person why the button is disabled, only after they started typing the field
        signUpProblem.textProperty().bind(Bindings.createStringBinding(
            () -> {
                if (hasBar.get()) return "Fields can't contain the | character";
                if (!signUpUsername.getText().trim().isEmpty() && usernameTaken.get()) return "This username can't be used";
                if (!signUpConfirm.getText().isEmpty() && passwordsDiffer.get()) return "The passwords don't match";
                return "";
            },
            hasBar, usernameTaken, passwordsDiffer, signUpUsername.textProperty(), signUpConfirm.textProperty()
        ));

        signUpButton.setMaxWidth(Double.MAX_VALUE);

        // the button is disabled while any of the checks fail
        signUpButton.disableProperty().bind(
            invalid(signUpUsername.textProperty())
                .or(invalid(signUpPassword.textProperty()))
                .or(invalid(signUpConfirm.textProperty()))
                .or(invalid(firstName.textProperty()))
                .or(invalid(lastName.textProperty()))
                .or(usernameTaken)
                .or(passwordsDiffer)
                .or(roleBox.valueProperty().isNull())
        );

        signUpButton.setOnAction(event -> submitSignUp());
        signUpUsername.setOnAction(event -> submitSignUp());
        firstName.setOnAction(event -> submitSignUp());
        lastName.setOnAction(event -> submitSignUp());
        signUpPassword.setOnSubmit(this::submitSignUp);
        signUpConfirm.setOnSubmit(this::submitSignUp);

        // first and last name side by side
        HBox names = new HBox(12, field("First name", firstName), field("Last name", lastName));
        for (Node child : names.getChildren()) {
            HBox.setHgrow(child, Priority.ALWAYS);
        }

        return new VBox(12,
            field("Username", signUpUsername),
            names,
            field("Password", signUpPassword),
            field("Confirm password", signUpConfirm),
            field("Account type", roleBox),
            roleNote,
            signUpProblem,
            signUpButton
        );
    }

    /**
     * creates the account, saves it and switches to the log in tab
     * <p>nothing happens while the button is disabled, so enter can't skip the checks.
     * a customer is active at once, a clerk or manager is pending until a manager approves them
     */
    private void submitSignUp() {
        if (signUpButton.isDisable() || !manager.canSignUp()) return;

        String username = signUpUsername.getText().trim();

        // the button is already disabled for this, checked again in case the list changed
        if (userData.isUsernameTaken(username)) return;

        String role = roleBox.getValue();
        boolean needsApproval = !role.equals(User.CUSTOMER);

        userData.add(new User(
            username,
            signUpPassword.getText(), // kept as typed, login compares it the same way
            firstName.getText().trim(),
            lastName.getText().trim(),
            role,
            needsApproval ? User.PENDING : User.ACTIVE
        ));
        UsersFileHandler.save(userData);

        clearSignUpForm();

        // moves to the log in tab with the username filled in and a note instead of a popup
        logInUsername.setText(username);
        logInPassword.clear();
        logInError.setText("");
        logInSuccess.setText(needsApproval
            ? "Account created. A manager has to approve it before you can log in."
            : "Account created. You can log in now."
        );
        logInTab.setSelected(true);

        Platform.runLater(logInPassword::requestFocus); // the username is already there
    }

    /** empties the sign up fields, so the tab is clean if the person comes back to it */
    private void clearSignUpForm() {
        signUpUsername.clear();
        firstName.clear();
        lastName.clear();
        signUpPassword.clear();
        signUpConfirm.clear();
        roleBox.setValue(User.CUSTOMER);
    }

    // ---------- helpers ----------

    /**
     * puts a small label above an input
     *
     * @param labelText the text of the label
     * @param input the input that goes under the label
     * @return the label and the input in one box
     */
    private VBox field(String labelText, Node input) {
        Label label = new Label(labelText);
        label.getStyleClass().add("field-label");
        return new VBox(4, label, input);
    }

    /**
     * makes a label take no space while it is empty, and gives it a style class
     * <p>the messages of the cards are empty most of the time, so they must not leave a gap.
     * the binding itself is shared, see Ui#collapseWhenEmpty(Label)
     *
     * @param label the label to set up
     * @param styleClass the css class of the label
     */
    private static void showOnlyWhenFilled(Label label, String styleClass) {
        label.getStyleClass().add(styleClass);
        label.setWrapText(true);
        Ui.collapseWhenEmpty(label);
    }

    /**
     * checks if a text is empty or contains | (the separator in the save files)
     * <p>this is the same rule as the old labelled fields, spaces at the ends are ignored
     *
     * @param text the text property of the field to check
     * @return a binding that is true while the text is invalid
     */
    private static BooleanBinding invalid(StringProperty text) {
        return Bindings.createBooleanBinding(() -> {
            String value = text.get().trim();
            return value.isEmpty() || value.contains("|");
        }, text);
    }

    /**
     * checks if a text contains | (the separator in the save files)
     *
     * @param text the text to check
     * @return true if it contains the character
     */
    private static boolean containsBar(String text) {
        return text.contains("|");
    }
}