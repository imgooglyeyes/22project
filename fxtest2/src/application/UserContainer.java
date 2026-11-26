package application;

import java.util.ArrayList;

/**
 * holds a list of users, used for the user database, logging in, and signing up
 *
 * <p>other classes are responsible for checking a username with isUsernameTaken before adding a user
 *
 * @author Jian Carranza, Shawn Alfred Padilla
 * @version 1.3
 * @since 1.2 countByStatus was added for the staff home tiles
 * @since 1.3 the pointless return at the end of add was removed
 * @see User
 */
public class UserContainer {
	/** every user in this container */
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
	 * adds a new user to the list (does not make a copy of the parameter)
	 *
	 * <p> other classes are responsible for not allowing duplicates.
	 * There is no way to remove users as we do not have account deletion in our scope.
	 *
	 * @param user the user to add
	 */
	public void add(User user) {
		userList.add(user);
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

	/**
	 * checks if a username can't be used for a new account
	 * <p>spaces at the ends and letter case are ignored, so Clerk1 and clerk1 count as the same name.
	 * guest is always taken because logging out uses it as the placeholder name
	 *
	 * @param username the username to check
	 * @return true if another user has this name or it is the reserved guest name
	 * @since 1.1
	 */
	public boolean isUsernameTaken(String username) {
		String wanted = username.trim();

		if (wanted.equalsIgnoreCase("guest")) return true; // reserved, see User#logOut()

		for (User u : userList) {
			if (u.getUsername().equalsIgnoreCase(wanted)) return true;
		}
		return false;
	}

	/**
	 * creates a list of user by their role/type
	 *
	 * @param type the user type to look for
	 * @return the a list of users that have the type
	 */
    public ArrayList<User> getUsersByType(String type) {
        ArrayList<User> result = new ArrayList<>();
        for (User u : userList) {
            if (u.getUserType().equals(type)) result.add(u);
        }
        return result;
    }

	/**
	 * counts the users that have a status
	 * <p>the staff home uses this to show how many sign ups wait for a manager
	 *
	 * @param status active, pending or rejected
	 * @return how many users have the status
	 * @since 1.2
	 */
	public int countByStatus(String status) {
		int count = 0;
		for (User u : userList) {
			if (status.equals(u.getStatus())) count++;
		}
		return count;
	}
}