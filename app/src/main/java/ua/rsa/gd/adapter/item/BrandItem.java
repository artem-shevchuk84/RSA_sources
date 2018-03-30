package ua.rsa.gd.adapter.item;

public class BrandItem {
	String ID;
	String NAME;
	float VALUE;
	
	public BrandItem(String _id, String _name) {
		ID = _id;
		NAME = _name;
		VALUE = 0;
	}
	
	public void incValue(float increment) {
		VALUE += increment;
	}
	
	public String getValue() {
		String result = "0";
		
		try {
			result = Float.toString(VALUE);
		} catch (Exception e) {
			// do nothing
		}
		
		return result;
	}
	
	public String getId() {
		return ID;
	}
	
	public String getName() {
		return NAME;
	}
}
