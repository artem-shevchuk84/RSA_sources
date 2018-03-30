package ru.by.rsa;

import java.util.ArrayList;

public class Coordinate {
	
	private ArrayList<String> jsonCoord;
	
	public Coordinate(long id, long lat, long lon, long date, int cash, String name, String adres, int weight, String cust_id, String shop_id, String numful) {
		this.jsonCoord = new ArrayList<String>();
		this.set(id, lat, lon, date, cash, name, adres, weight, cust_id, shop_id, numful);
	}
	
	public void set(long id, long lat, long lon, long date, int cash, String name, String adres, int weight, String cust_id, String shop_id, String numful) {
		this.jsonCoord.add(Long.toString(id) + ",");
		this.jsonCoord.add(Long.toString(lat) + ",");
		this.jsonCoord.add(Long.toString(lon) + ",");
		this.jsonCoord.add(Long.toString(date) + ",");
		this.jsonCoord.add(Integer.toString(cash) + ",");
		this.jsonCoord.add("'"+name.replaceAll("'", "")+"',");
		this.jsonCoord.add("'"+adres.replaceAll("'", "")+"',");
		this.jsonCoord.add(Integer.toString(weight) + ",");
		this.jsonCoord.add("'"+cust_id+"',");
		this.jsonCoord.add("'"+shop_id+"',");
		this.jsonCoord.add("'"+numful+"'");
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
