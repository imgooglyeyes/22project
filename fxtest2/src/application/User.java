package application;

/**
 * an account in the app, a guest is a user without an account
 *
 * <p>attributes:
 * <ul>
 * <li>username: String
 * <li>password: String
 * <li>firstName: String
 * <li>lastName: String
 * <li>userType: String, manager, clerk, customer or guest
 * <li>status: String, active, pending or rejected
 * </ul>
 *
 * <p>only active users can log in, customers are active at once and
 * clerks and managers start as pending until a manager approves them (phase 3)
 *
 * @author Jian Carranza, Shawn Alfred Padilla
 * @version 1.1
 * @see UserContainer
 * @see UsersFileHandler
 */
public class User {
	/** the name used to log in */
	private String username;
	/** the password used to log in */
	private String password;
	/** first name of the user */
	private String firstName;
	/** last name of the user */
	private String lastName;
	/** manager, clerk, customer or guest */
	private String userType;
	/** active, pending or rejected */
	private String status;

	public final static String MANAGER = "Manager";
	public final static String CLERK = "Clerk";
	public final static String CUSTOMER = "Customer";
	public final static String GUEST = "Guest"; // no account

	/** the account can log in */
	public final static String ACTIVE = "Active";
	/** the account waits for a manager to approve it */
	public final static String PENDING = "Pending";
	/** a manager turned the account down, it can't log in */
	public final static String REJECTED = "Rejected";

	/**
	 * creates an active user, used for guests and for old save lines without a status
	 *
	 * @param username the name used to log in
	 * @param password the password used to log in
	 * @param firstName first name of the user
	 * @param lastName last name of the user
	 * @param userType manager, clerk, customer or guest
	 */
	public User(String username, String password, String firstName, String lastName, String userType) {
		this(username, password, firstName, lastName, userType, ACTIVE);
	}

	/**
	 * creates a user with a status
	 *
	 * @param username the name used to log in
	 * @param password the password used to log in
	 * @param firstName first name of the user
	 * @param lastName last name of the user
	 * @param userType manager, clerk, customer or guest
	 * @param status active, pending or rejected
	 * @since 1.1
	 */
	public User(String username, String password, String firstName, String lastName, String userType, String status) {
		this.username = username;
		this.password = password;
		this.firstName = firstName;
		this.lastName = lastName;
		this.userType = userType;
		this.status = status;
	}

	/** @return the name used to log in */
	public String getUsername()		{ return username; }
	/** @return the password used to log in */
	public String getPassword()		{ return password; }
	/** @return the first name */
	public String getFirstName()	{ return firstName; }
	/** @return the last name */
	public String getLastName()		{ return lastName; }
	/** @return manager, clerk, customer or guest */
	public String getUserType()		{ return userType; }

	/**
	 * gets the account status
	 *
	 * @return active, pending or rejected
	 * @since 1.1
	 */
	public String getStatus()		{ return status; }

	/**
	 * checks if the account can log in
	 *
	 * @return true if the status is active
	 * @since 1.1
	 */
	public boolean isActive()		{ return status.equals(ACTIVE); }

	/**
	 * sets the account status, used when a manager approves or rejects a sign up
	 *
	 * @param status active, pending or rejected
	 * @since 1.1
	 */
	public void setStatus(String status)	{ this.status = status; }

	/**
	 * checks if a text is one of the three statuses, used when loading the save file
	 *
	 * @param status the text to check
	 * @return true if it is active, pending or rejected
	 * @since 1.1
	 */
	public static boolean isValidStatus(String status) {
		return ACTIVE.equals(status) || PENDING.equals(status) || REJECTED.equals(status);
	}

	/** 
	 * overwrites this user with the data of another user
	 * <p>the current user object is shared by every scene, so login copies into it instead of replacing it
	 *
	 * @param u the user to copy from
	 */
	public void replaceWith(User u) {
		username = u.getUsername();
		password = u.getPassword();
		firstName = u.getFirstName();
		lastName = u.getLastName();
		userType = u.getUserType();
		status = u.getStatus();
	}

	/** overwrites this user with guest data */
	public void logOut() {
		username = "guest";
		password = "";
		firstName = "guest";
		lastName = "";
		userType = User.GUEST;
		status = ACTIVE;
	}
}