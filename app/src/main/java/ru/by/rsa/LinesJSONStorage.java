package ru.by.rsa;

public class LinesJSONStorage {
	
	//public static final String JSON_GPS_LEXEM = "lns";

	private LinesList jsonLinesList;
	
	public LinesJSONStorage() {
		this.jsonLinesList = new LinesList();
	}
	
	public void addLine(String numful, String goods_id, String goods_name, int qty, String packtype, int pricewnds, int pricewonds, int sumwnds, int sumwonds, int weight) {
		this.jsonLinesList.add(numful, goods_id, goods_name, qty, packtype, pricewnds, pricewonds, sumwnds, sumwonds, weight);
	}
	
	public String toString() {
		StringBuilder result = new StringBuilder("{\"lns\":");
		result.append(this.jsonLinesList.toString());
		result.append("}");
		return result.toString();
	}
	
}
