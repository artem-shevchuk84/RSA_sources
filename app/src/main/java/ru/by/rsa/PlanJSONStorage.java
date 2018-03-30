package ru.by.rsa;

public class PlanJSONStorage {
	
	//public static final String JSON_GPS_LEXEM = "lns";

	private PlanList jsonPlanList;
	
	public PlanJSONStorage() {
		this.jsonPlanList = new PlanList();
	}
	
	public void addLine(String cust_id, String shop_id, String cust_text, String shop_text, long date) {
		this.jsonPlanList.add(cust_id, shop_id, cust_text, shop_text, date);
	}
	
	public String toString() {
		StringBuilder result = new StringBuilder("{\"pln\":");
		result.append(this.jsonPlanList.toString());
		result.append("}");
		return result.toString();
	}
	
}
