package ru.by.rsa;

import java.util.ArrayList;

import ru.by.rsa.adapter.item.RestItem;

public class RestList {
	private ArrayList<String> jsonRestList;
	
	public RestList() {
		this.jsonRestList = new ArrayList<String>();
	}
	
	public void add(String goods_id, String goods_name, String sklad_id, String sklad_name, int rest) {
		RestItem line = new RestItem(goods_id, goods_name, sklad_name, sklad_id, rest);
		if (jsonRestList.isEmpty()) {
			this.jsonRestList.add(line.toString());
		} else {
			this.jsonRestList.add("," + line.toString());
		}
	}
	
	public String toString() {
		StringBuilder result = new StringBuilder("[");
		for (String s : this.jsonRestList) {
			result.append(s);
		}
		result.append("]");
		return result.toString();
	}

}
