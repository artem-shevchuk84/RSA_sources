package ru.by.rsa;

public class CoordinateJSONStorage {
	
	public static final String JSON_GPS_LEXEM = "gps";

	private CoordinateList jsonCoordinateList;
	
	public CoordinateJSONStorage() {
		this.jsonCoordinateList = new CoordinateList();
	}
	
	public void addCoordinates(long id, long lat, long lon, long date, int cash, String name, String adres, int weight, String cust_id, String shop_id, String numful) {
		this.jsonCoordinateList.add(id, lat, lon, date, cash, name, adres, weight, cust_id, shop_id, numful);
	}
	
	public String toString() {
		StringBuilder result = new StringBuilder("{\"gps\":");
		result.append(this.jsonCoordinateList.toString());
		result.append("}");
		return result.toString();
	}
	
}
