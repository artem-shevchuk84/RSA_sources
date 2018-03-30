package ru.by.rsa.adapter.item;

import java.util.ArrayList;

public class RestItem {
	
	private ArrayList<String> jsonRest;
	
	public RestItem(String goods_id, String goods_name, String sklad_id, String sklad_name, int rest) {
		this.jsonRest = new ArrayList<String>();
		this.set(goods_id, goods_name, sklad_name, sklad_id, rest);
	}
	
	public void set(String goods_id, String goods_name, String sklad_id, String sklad_name, int rest) {
		this.jsonRest.add("'"+goods_id+"',");		              	// goods_id 
		this.jsonRest.add("'"+goods_name.replaceAll("'", "")+"',");	// goods_name
		this.jsonRest.add("'"+sklad_id+"',");  						// sklad_id
		this.jsonRest.add("'"+sklad_name.replaceAll("'", "")+"',");	// sklad_name
		this.jsonRest.add(Integer.toString(rest));					// rest
	}
	
	public String toString() {
		StringBuilder result = new StringBuilder("[");
		for (String s : this.jsonRest) {
			result.append(s);
		}
		result.append("]");
		return result.toString();
	}
	
}
