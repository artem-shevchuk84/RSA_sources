package ua.rsa.gd;

import java.util.ArrayList;

public class PlanList {
	private ArrayList<String> jsonPlanList;
	
	public PlanList() {
		this.jsonPlanList = new ArrayList<String>();
	}
	
	public void add(String cust_id, String shop_id, String cust_text, String shop_text, long date) {
		PlanItem line = new PlanItem(cust_id, shop_id, cust_text, shop_text, date);
		if (jsonPlanList.isEmpty()) {
			this.jsonPlanList.add(line.toString());
		} else {
			this.jsonPlanList.add("," + line.toString());
		}
	}
	
	public String toString() {
		StringBuilder result = new StringBuilder("[");
		for (String s : this.jsonPlanList) {
			result.append(s);
		}
		result.append("]");
		return result.toString();
	}

}
