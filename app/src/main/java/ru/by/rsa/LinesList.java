package ru.by.rsa;

import java.util.ArrayList;

import ru.by.rsa.adapter.item.LinesItem;

public class LinesList {
	private ArrayList<String> jsonLinesList;
	
	public LinesList() {
		this.jsonLinesList = new ArrayList<String>();
	}
	
	public void add(String numful, String goods_id, String goods_name, int qty, String packtype, int pricewnds, int pricewonds, int sumwnds, int sumwonds, int weight) {
		LinesItem line = new LinesItem(numful, goods_id, goods_name, qty, packtype, pricewnds, pricewonds, sumwnds, sumwonds, weight);
		if (jsonLinesList.isEmpty()) {
			this.jsonLinesList.add(line.toString());
		} else {
			this.jsonLinesList.add("," + line.toString());
		}
	}
	
	public String toString() {
		StringBuilder result = new StringBuilder("[");
		for (String s : this.jsonLinesList) {
			result.append(s);
		}
		result.append("]");
		return result.toString();
	}

}
