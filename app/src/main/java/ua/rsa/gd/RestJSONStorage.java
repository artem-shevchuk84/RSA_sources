package ua.rsa.gd;

public class RestJSONStorage {
	
	//public static final String JSON_GPS_LEXEM = "rst";

	private RestList jsonRestList;
	
	public RestJSONStorage() {
		this.jsonRestList = new RestList();
	}
	
	public void addLine(String goods_id, String goods_name, String sklad_id, String sklad_name, int rest) {
		this.jsonRestList.add(goods_id, goods_name, sklad_name, sklad_id, rest);
	}
	
	public String toString() {
		StringBuilder result = new StringBuilder("{\"rst\":");
		result.append(this.jsonRestList.toString());
		result.append("}");
		return result.toString();
	}
	
}
