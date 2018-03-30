package ua.rsa.gd;

public class Outlet {
	private String cust_id;
	private String shop_id;
	
	Outlet (String c, String s) {
		cust_id = c;
		shop_id = s;
	}
	
	public String getCust_id() {
		return cust_id;
	}
	public String getShop_id() {
		return shop_id;
	}
}