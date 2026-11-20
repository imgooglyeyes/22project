package ui;

import application.User;
import application.UserContainer;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * changes the current user if login credentials match an existing account
 *
 * <p>also has the Sign Up button where new accounts can be made
 *
 * @author Jian Carranza
 * @version 1.0
 * @see SceneManager
 */
public class LoginScene extends Scene {

	UserContainer userData;
	User currentUser;
	
	public LoginScene(
            Stage stage,
            SceneManager manager,
            UserContainer userData,
            User currentUser) {

        super(new BorderPane(), 900, 500);
        
        // loads the shared stylesheet
        Styles.apply(this);

        this.userData = userData;
        this.currentUser = currentUser;
        
        BorderPane root = (BorderPane) getRoot();
        root.setPadding(new Insets(12));

        Button quit = new Button("Quit");

        HBox buttons = new HBox(10,
            quit
        );
        
        quit.setOnAction(event -> Platform.exit());
        
        root.setTop(manager.createHeader("Log In"));
        root.setBottom(buttons);
	}
}
