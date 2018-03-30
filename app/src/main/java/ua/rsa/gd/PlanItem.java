package ua.rsa.gd;

import java.util.ArrayList;

public class PlanItem {
	
	private ArrayList<String> jsonPlan;
	
	public PlanItem(String cust_id, String shop_id, String cust_text, String shop_text, long date) {
		this.jsonPlan = new ArrayList<String>();
		this.set(cust_id, shop_id, cust_text, shop_text, date);
	}
	
	public void set(String cust_id, String shop_id, String cust_text, String shop_text, long date) {
		this.jsonPlan.add("'"+cust_id+"',");		              	// cust_id 
		this.jsonPlan.add("'"+shop_id+"',");					  	// shop_id
		this.jsonPlan.add("'"+cust_text.replaceAll("'", "")+"',");						// cust_text
		this.jsonPlan.add("'"+shop_text.replaceAll("'", "")+"',");						// shop_text
		this.jsonPlan.add(Long.toString(date));						// date
	}
	
	public String toString() {
		StringBuilder result = new StringBuilder("[");
		for (String s : this.jsonPlan) {
			result.append(s);
		}
		result.append("]");
		return result.toString();
	}
	
}
