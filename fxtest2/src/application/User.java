package application;

// currently not yet implemented

public class User {
    private String username; // may not need this one
    private String password;
    private String firstName;
    private String lastName;
    private String userType; // manager, clerk, or customer
    
    public final static String MANAGER = "Manager";
    public final static String CLERK = "Clerk";
    public final static String CUSTOMER = "Customer";
    public final static String GUEST = "Guest"; // no account
    
    public User(String username, String password, String firstName, String lastName, String userType) {
    	this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.userType = userType;
    }

    public String getUsername()		{ return username; }
    public String getPassword()		{ return password; }
    public String getFirstName()	{ return firstName; }
    public String getLastName()		{ return lastName; }
    public String getUserType()		{ return userType; }
    
    // overwrites user data
    public void replaceWith(User u) {
    	username = u.getUsername();
    	password = u.getPassword();
    	firstName = u.getFirstName();
    	lastName = u.getLastName();
    	userType = u.getUserType();
    }
    
    // overwrites user data with guest data
    public void logOut() {
    	username = "guest";
    	password = "";
    	firstName = "guest";
    	lastName = "";
    	userType = User.GUEST;
    }
}
