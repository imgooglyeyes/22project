package ui;

import application.User;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

/**
 * the user part of the top bar
 *
 * <p>a guest gets a log in and a sign up button. a logged in user gets a chip with an avatar
 * (the first letter of the name in a circle), the name and the role, and a click opens the user menu
 * with the role, log out and quit. this replaces the log out and quit buttons that every old scene had
 *
 * @author Shawn Alfred Padilla
 * @version 1.0
 * @see TopBar
 * @see SceneManager#logOut()
 */
public class UserChip extends HBox {

    /**
     * creates the chip that fits the user
     *
     * @param manager the scene manager, opens the log in and sign up pages and logs out
     * @param user the user who is logged in, or the guest
     */
    public UserChip(SceneManager manager, User user) {
        super(8); // sets the spacing to 8px
        setAlignment(Pos.CENTER_RIGHT);

        if (user.getUserType().equals(User.GUEST)) { // a guest has no menu, only the way to get an account
            Button logIn = new Button("Log In");
            Button signUp = new Button("Sign Up");
            logIn.getStyleClass().add("on-dark");
            signUp.getStyleClass().add("outline-dark");

            logIn.setOnAction(event -> manager.showLogin());
            signUp.setOnAction(event -> manager.showSignUp());

            getChildren().addAll(logIn, signUp);
            return;
        }

        getStyleClass().add("user-chip");
        setFocusTraversable(true);

        String name = (user.getFirstName() + " " + user.getLastName()).trim();
        if (name.isEmpty()) name = user.getUsername(); // an account without a name still shows something
        setAccessibleText(name + ", " + user.getUserType() + ", open the user menu");

        // the avatar is the first letter of the name in a circle
        Circle circle = new Circle(16);
        circle.getStyleClass().add("avatar");
        Label letter = new Label(name.substring(0, 1).toUpperCase());
        letter.getStyleClass().add("avatar-letter");
        StackPane avatar = new StackPane(circle, letter);

        Label nameLabel = new Label(name);
        nameLabel.getStyleClass().add("user-name");
        Label roleLabel = new Label(user.getUserType());
        roleLabel.getStyleClass().add("user-role");
        VBox texts = new VBox(0, nameLabel, roleLabel);
        texts.setAlignment(Pos.CENTER_LEFT);

        getChildren().addAll(avatar, texts, Icons.of(Icons.CHEVRON, 18));

        // the user menu, the role line is only a title so it is disabled
        MenuItem roleItem = new MenuItem("Signed in as " + user.getUserType());
        roleItem.setDisable(true);
        MenuItem logOut = new MenuItem("Log Out");
        MenuItem quit = new MenuItem("Quit");
        logOut.setOnAction(event -> manager.logOut());
        quit.setOnAction(event -> manager.quit());

        ContextMenu menu = new ContextMenu(roleItem, new SeparatorMenuItem(), logOut, quit);

        // a click or enter opens the menu below the chip, a second one closes it
        setOnMouseClicked(event -> toggle(menu));
        setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.SPACE) {
                toggle(menu);
                event.consume();
            }
        });
    }

    /**
     * opens the menu below the chip, or closes it if it is already open
     *
     * @param menu the user menu
     */
    private void toggle(ContextMenu menu) {
        if (menu.isShowing()) {
            menu.hide();
        } else {
            menu.show(this, Side.BOTTOM, 0, 8);
        }
    }
}