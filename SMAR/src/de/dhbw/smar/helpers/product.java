package de.dhbw.smar.helpers;

public class product {
	
	private int id;
	private String name;
	private String unit;
	private String amount;
	private String receiving_name;
	private String receiving_date;
	private int amount_to_add;
	private String unit_to_add;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getUnit() {
		return unit;
	}
	public void setUnit(String unit) {
		this.unit = unit;
	}
	public String getAmount() {
		return amount;
	}
	public void setAmount(String amount) {
		this.amount = amount;
	}
	public String getReceiving_name() {
		return receiving_name;
	}
	public void setReceiving_name(String receiving_name) {
		this.receiving_name = receiving_name;
	}
	public String getReceiving_date() {
		return receiving_date;
	}
	public void setReceiving_date(String receiving_date) {
		this.receiving_date = receiving_date;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getUnit_to_add() {
		return unit_to_add;
	}
	public void setUnit_to_add(String unit_to_add) {
		this.unit_to_add = unit_to_add;
	}
	public int getAmount_to_add() {
		return amount_to_add;
	}
	public void setAmount_to_add(int amount_to_add) {
		this.amount_to_add = amount_to_add;
	}
	
	

}
