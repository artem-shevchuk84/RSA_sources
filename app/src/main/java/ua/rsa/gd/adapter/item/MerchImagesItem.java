package ua.rsa.gd.adapter.item;

import java.io.File;

public class MerchImagesItem {
	String date;
	String id;
	File file;
	
	public MerchImagesItem(File newFile) {
		file = newFile;
		String name = file.getName();
		String[] parts = name.split("#");
		if (parts.length==3) {
			date = parts[0].substring(0, 8);
			id = parts[1];
		} else if (parts.length==4) {
			date = parts[0].substring(0, 8);
			id = parts[2];
		} else {
			date = "010101";
			id = "010101";
		}
	}
	
	public String getDate(){
		return date;
	}
	public String getID(){
		return id;
	}
	
	
}
