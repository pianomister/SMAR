package de.dhbw.smar.container;

import java.util.ArrayList;

public class OrderContainer {
	
	private int order_id;
	private String name;
	private String date;
	private String barcode;
	
	private ArrayList<OrderItem> items = new ArrayList<OrderItem>();

	public OrderContainer(int order_id, String name, String date, String barcode) {
		this.order_id = order_id;
		this.name = name;
		this.date = date;
		this.barcode = barcode;
	}

	public ArrayList<OrderItem> getItems() {
		return items;
	}
	
	public boolean isInOrder(int product_id, int unit_id) {
		for(OrderItem item: items) {
			if(item.getProduct_id() == product_id && item.getUnit_id() == unit_id) {
				return true;
			}
		}
		return false;
	}
	
	public void addItem(OrderItem oi) {
		items.add(oi);
	}
	
	public boolean setDeliveredAmount(int delivered_amount, int product_id, int unit_id) {
		for(OrderItem item: items) {
			if(item.getProduct_id() == product_id && item.getUnit_id() == unit_id) {
				item.setDelivered_amount(delivered_amount);
				return true;
			}
		}
		return false;
	}
	
	public int getDeliveredAmount(int product_id, int unit_id) {
		for(OrderItem item: items) {
			if(item.getProduct_id() == product_id && item.getUnit_id() == unit_id) {
				return item.getDelivered_amount();
			}
		}
		return 0;
	}

	public void setItems(ArrayList<OrderItem> items) {
		this.items = items;
	}

	public int getOrder_id() {
		return order_id;
	}

	public String getName() {
		return name;
	}

	public String getDate() {
		return date;
	}
	
	public String getBarcode() {
		return barcode;
	}
}
