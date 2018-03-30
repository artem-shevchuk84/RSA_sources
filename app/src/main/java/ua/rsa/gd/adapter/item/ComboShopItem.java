package ua.rsa.gd.adapter.item;

import java.util.HashMap;

import android.os.Parcel;
import android.os.Parcelable;

public class ComboShopItem extends HashMap <String, String> implements Parcelable {
	private static final long serialVersionUID = 1L;
	public static final String ID		= "id";
	public static final String CUST_ID	= "cust_id";
	public static final String NAME		= "name";
	public static final String ADRES	= "adres";
	public static final String TYPE		= "type";
	
	
	public ComboShopItem(String id, String cust_id, String name, String adres, String type) {
		super();
		super.put(ID,		id);
		super.put(CUST_ID,	cust_id);
		super.put(NAME,		name);
		super.put(ADRES,	adres);
		super.put(TYPE,		type);
	}
	
	public int describeContents() {
        return 0;
    }
	
	public void writeToParcel(Parcel out, int flags) {
		out.writeMap(this);
    }
	
	public static final Parcelable.Creator<ComboShopItem> CREATOR = new Parcelable.Creator<ComboShopItem>() {
		    	public ComboShopItem createFromParcel(Parcel in) 
		    	{
		    		return new ComboShopItem(in);
		    	}
		    	public ComboShopItem[] newArray(int size) 
		    	{
		    		return new ComboShopItem[size];
		    	}
	};
	
	private ComboShopItem(Parcel in) {
    	this("","","","","");		
    	in.readMap(this, ComboShopItem.class.getClassLoader());
    }
	
	
}
