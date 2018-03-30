package ru.by.rsa.adapter.item;

import java.util.ArrayList;

public class LinesItem {
	
	private ArrayList<String> jsonCoord;
	
	public LinesItem(String numful, String goods_id, String goods_name, int qty, String packtype, int pricewnds, int pricewonds, int sumwnds, int sumwonds, int weight) {
		this.jsonCoord = new ArrayList<String>();
		this.set(numful, goods_id, goods_name, qty, packtype, pricewnds, pricewonds, sumwnds, sumwonds, weight);
	}
	
	public void set(String numful, String goods_id, String goods_name, int qty, String packtype, int pricewnds, int pricewonds, int sumwnds, int sumwonds, int weight) {
		this.jsonCoord.add("'"+numful+"',");		              	// numful  String 
		this.jsonCoord.add("'"+goods_id+"',");					  	// goods_id String
		this.jsonCoord.add("'"+goods_name.replaceAll("'", "")+"',");					// goods_name String
		this.jsonCoord.add(Integer.toString(qty) + ",");			// qty Integer
		this.jsonCoord.add("'"+packtype.replaceAll("'", "")+"',");						// packtype String
		this.jsonCoord.add(Integer.toString(pricewnds) + ",");		// pricewnds Integer
		this.jsonCoord.add(Integer.toString(pricewonds) + ",");		// pricewonds Integer
		this.jsonCoord.add(Integer.toString(sumwnds) + ",");		// sumwnds Integer
		this.jsonCoord.add(Integer.toString(sumwonds) + ",");		// sumwonds Integer
		this.jsonCoord.add(Integer.toString(weight));				// weight Integer
	}
	
	public String toString() {
		StringBuilder result = new StringBuilder("[");
		for (String s : this.jsonCoord) {
			result.append(s);
		}
		result.append("]");
		return result.toString();
	}
	
}
