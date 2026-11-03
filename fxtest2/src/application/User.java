package application;

// currently not yet implemented

public class User {
    private final String username; // may not need this one
    private final String password;
    private final String firstName;
    private final String lastName;
    private final String userType; // manager, clerk, or customer
    
    public final static String MANAGER = "Manager";
    public final static String CLERK = "Clerk";
    public final static String CUSTOMER = "Customer";
    
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
}
