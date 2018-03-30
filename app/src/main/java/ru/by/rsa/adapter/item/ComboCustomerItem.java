package ru.by.rsa.adapter.item;

import java.util.HashMap;

import android.os.Parcel;
import android.os.Parcelable;

public class ComboCustomerItem extends HashMap <String, String> implements Parcelable {
	private static final long serialVersionUID = 1L;
	public static final String ID		= "id";
	public static final String NAME		= "name";
	public static final String TEL		= "tel";
	public static final String ADRES	= "adres";
	public static final String OKPO		= "okpo";
	public static final String INN		= "inn";
	public static final String CONTACT	= "contact";
	public static final String DOGOVOR	= "dogovor";
	public static final String COMMENT	= "comment";
	
	
	
	public ComboCustomerItem(String id, String name, String tel, String adres, String okpo, String inn, String contact, String dogovor, String comment) {
		super();
		super.put(ID,		id);
		super.put(NAME,		name);
		super.put(TEL,		tel);
		super.put(ADRES,	adres);
		super.put(OKPO,		okpo);
		super.put(INN,		inn);
		super.put(CONTACT,	contact);
		super.put(DOGOVOR,	dogovor);
		super.put(COMMENT,	comment);
	}
	
	public int describeContents() {
        return 0;
    }
	
	public void writeToParcel(Parcel out, int flags) {
		out.writeMap(this);
    }
	
	public static final Parcelable.Creator<ComboCustomerItem> CREATOR = new Parcelable.Creator<ComboCustomerItem>() {
		    	public ComboCustomerItem createFromParcel(Parcel in) 
		    	{
		    		return new ComboCustomerItem(in);
		    	}
		    	public ComboCustomerItem[] newArray(int size) 
		    	{
		    		return new ComboCustomerItem[size];
		    	}
	};
	
	private ComboCustomerItem(Parcel in) {
    	this("","","","","","","","","");		
    	in.readMap(this, ComboCustomerItem.class.getClassLoader());
    }
	
}
