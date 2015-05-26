package de.dhbw.smar.helpers;

/**
 * 
 * Class that holds the login data.
 * 
 * @author Sebastian Kowalski
 *
 */
public class LoginHelper {
	// Make this class a singleton
	private static final LoginHelper lh = new LoginHelper();
	
	private LoginHelper() {
		// No constructor because of singleton
	}
	
	// Get the instance
	public static LoginHelper getInstance() {
		return lh;
	}
	
	// Variables	
	private String username = null;
	private String password = null;
	private String hwaddress = null;
	private String jwt = null;
	private boolean loggedIn = false;
	
	// Getter
	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}
	
	public String getHwaddress() {
		return hwaddress;
	}
	
	public String getJwt() {
		return jwt;
	}
	
	public boolean isLoggedIn() {
		return this.loggedIn;
	}
	
	// Setter
	public void setUsername(String username) {
		this.username = username;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public void setHwaddress(String hwaddress) {
		this.hwaddress = hwaddress;
	}
	
	public void setJwt(String jwt) {
		this.jwt = jwt;
	}
	
	public void setLoggedIn(boolean loggedIn) {
		this.loggedIn = loggedIn;
	}
	
	// Easy Method to log out.
	public void setLogout() {
		this.username = null;
		this.password = null;
		this.jwt = null;
		this.loggedIn = false;
	}
}