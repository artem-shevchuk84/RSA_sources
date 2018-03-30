package ua.rsa.gd.adapter.item;

import java.util.HashMap;

public class ReportItem extends HashMap <String, String> {
	private static final long serialVersionUID = 1L;
	public static final String NAME = "name";
	public static final String DETAILS = "details";
	
	public ReportItem(String name, String details) {
		super();
		super.put(NAME, name);
		super.put(DETAILS, details);
	}
}
