package ua.rsa.gd.adapter.item;

public class GoodItem {
	String ID;
	String BRAND_ID;
	
	public GoodItem(String _id, String _brandid) {
		ID = _id;
		BRAND_ID = _brandid;
	}
	
	public String getId() {
		return ID;
	}
	
	public String getBrandId() {
		return BRAND_ID;
	}
	
	
}
