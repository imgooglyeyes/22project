package application;

import java.util.ArrayList;

/**
 * holds a list of users, used for the user database, logging in, and signing up
 *
 * <p>
 *
 * @author Jian Carranza
 * @version 1.0
 * @see User
 */
public class UserContainer {
	/** every product in this container */
	private ArrayList<User> userList;

	/** creates an empty container */
	public UserContainer() {
		this.userList = new ArrayList<>();
	}

	/**
	 * creates a container with a starting list
	 *
	 * @param u the starting users, the list is copied
	 */
	public UserContainer(ArrayList<User> u) {
		this.userList = new ArrayList<>(u);
	}

	/**
	 * adds a new user to the list
	 *
	 * <p> other classes are responsible for not allowing duplicates.
	 * There is no way to remove users as we do not have account deletion in our scope.
	 *
	 * @param user the user to add
	 */
	public void add(User user) {
		userList.add(new User(user.getUsername(), user.getPassword(), user.getFirstName(), user.getLastName(), user.getUserType()));
		return;
	}


	/** @return the real list of users, changing it changes the container */
	public ArrayList<User> getUsers(){
		return userList;
	}

	/**
	 * finds a user by their username
	 *
	 * @param username the exact username to look for
	 * @return the user, or null if it is not here
	 */
	public User getUserByUsername(String username) {
		for (User u : userList) { // go through every user
			if (u.getUsername().equals(username)) {return u;} // does its username match what the parameter is looking for?
		}
		return null; // if not, return null
	}
}
