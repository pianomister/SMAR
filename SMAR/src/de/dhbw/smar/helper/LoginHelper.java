package de.dhbw.smar.helper;

public class LoginHelper {
	// Make this class a singleton
	private static final LoginHelper lh = new LoginHelper();
	private String username = null;
	private String password = null;
	private boolean loggedIn = false;
	
	private LoginHelper() {
		// No constructor because of singleton
	}
	
	public static LoginHelper getInstance() {
		return lh;
	}
	
	public void setLogin(String username, String password) {
		this.username = username;
		this.password = password;
		this.loggedIn = true;
	}
	
	public void setLogout() {
		this.username = null;
		this.password = null;
		this.loggedIn = false;
	}
	
	public boolean isLoggedIn() {
		return this.loggedIn;
	}
}
