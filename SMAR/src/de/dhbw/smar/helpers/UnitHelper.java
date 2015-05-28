package de.dhbw.smar.helpers;

public class UnitHelper {

	private String name;
	private String capacity;
	
	public UnitHelper() {
		
	}
	
	public UnitHelper(String name, String capacity) {
		this.name = name;
		this.capacity = capacity;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCapacity() {
		return capacity;
	}

	public void setCapacity(String capacity) {
		this.capacity = capacity;
	}
	
}
