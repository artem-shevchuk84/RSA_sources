package ru.by.rsa;

import java.util.ArrayList;

public class CoordinateList {
	private ArrayList<String> jsonCoordinateList;
	
	public CoordinateList() {
		this.jsonCoordinateList = new ArrayList<String>();
	}
	
	public void add(long id, long lat, long lon, long date, int cash, String name, String adres, int weight, String cust_id, String shop_id, String numful) {
		Coordinate coordinate = new Coordinate(id, lat, lon, date, cash, name, adres, weight, cust_id, shop_id, numful);
		if (jsonCoordinateList.isEmpty()) {
			this.jsonCoordinateList.add(coordinate.toString());
		} else {
			this.jsonCoordinateList.add("," + coordinate.toString());
		}
	}
	
	public String toString() {
		StringBuilder result = new StringBuilder("[");
		for (String s : this.jsonCoordinateList) {
			result.append(s);
		}
		result.append("]");
		return result.toString();
	}

}
