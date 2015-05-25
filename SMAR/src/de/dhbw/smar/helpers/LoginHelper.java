package de.dhbw.smar.helpers;

import java.util.ArrayList;
import java.util.List;

// @author Sebastian Kowalski
public class LoginHelper {
	// Make this class a singleton
	private static final LoginHelper lh = new LoginHelper();
	private String username = null;
	private String jwt = null;
	private String password = null;
	private String hwaddress = null;
	
	public String getHwaddress() {
		return hwaddress;
	}

	public void setHwaddress(String hwaddress) {
		this.hwaddress = hwaddress;
	}
	
	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	private boolean loggedIn = false;
	
	private LoginHelper() {
		// No constructor because of singleton
	}
	
	public static LoginHelper getInstance() {
		return lh;
	}
	
	public void setLogout() {
		this.username = null;
		this.password = null;
		this.loggedIn = false;
	}
	
	public boolean isLoggedIn() {
		return this.loggedIn;
	}
	
	public void setLoggedIn(boolean loggedIn) {
		this.loggedIn = loggedIn;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public List<String> getUserList() {
		List<String> userList = new ArrayList<String>();
		userList.add("Sebastian");
		userList.add("Stephan");
		userList.add("Raffael");
		return userList;
	}

	public String getJwt() {
		return jwt;
	}

	public void setJwt(String jwt) {
		this.jwt = jwt;
	}
}
