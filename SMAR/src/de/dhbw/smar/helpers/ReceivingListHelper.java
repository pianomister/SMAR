package de.dhbw.smar.helpers;

import java.util.ArrayList;

public class ReceivingListHelper {

	private static final ReceivingListHelper rlh = new ReceivingListHelper();
	
	public void ReceivingListHelper() {
		// No constructur becaus of singleton
	}
	
	// Get the instance
		public static ReceivingListHelper getInstance() {
			return rlh;
		}
	
		
	// Variables
	private ArrayList<product> list = new ArrayList<product>();
	
	public void addProduct(product p) {
		list.add(p);
	}
	
	public void clear() {
		list.clear();
	}
	
	public ArrayList<product> get() {
		return list;
	}
	
	
}
