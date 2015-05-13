package de.dhbw.smar.helpers;

import java.util.ArrayList;
import java.util.List;

// @author Sebastian Kowalski
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
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public List<String> getUserList() {
		List<String> userList = new ArrayList<String>();
		userList.add("Sebastian");
		userList.add("Stephan");
		userList.add("Raffael");
		return userList;
	}
}
