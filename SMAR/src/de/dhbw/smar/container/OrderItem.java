package de.dhbw.smar.container;

public class OrderItem {
	private int product_id;
	private String product_name;
	
	private int unit_id;
	private String unit_name;
	private int unit_capacity;
	
	private int amount;
	
	private int delivered_amount;
	
	public OrderItem(int product_id, String product_name, int unit_id, String unit_name, int unit_capacity, int amount) {
		this.product_id = product_id;
		this.product_name = product_name;
		this.unit_id = unit_id;
		this.unit_name = unit_name;
		this.unit_capacity = unit_capacity;
		this.amount = amount;
		this.delivered_amount = 0;
	}

	public int getDelivered_amount() {
		return delivered_amount;
	}

	public void setDelivered_amount(int delivered_amount) {
		this.delivered_amount = delivered_amount;
	}

	public int getProduct_id() {
		return product_id;
	}

	public String getProduct_name() {
		return product_name;
	}

	public int getUnit_id() {
		return unit_id;
	}

	public String getUnit_name() {
		return unit_name;
	}

	public int getUnit_capacity() {
		return unit_capacity;
	}

	public int getAmount() {
		return amount;
	}
}
