package ru.by.rsa.adapter.item;

import java.util.HashMap;

import android.os.Parcel;
import android.os.Parcelable;

public class ComboSkladItem extends HashMap <String, String> implements Parcelable {
	private static final long serialVersionUID = 1L;
	public static final String ID		= "id";
	public static final String NAME		= "name";
	
	
	
	public ComboSkladItem(String id, String name) {
		super();
		super.put(ID,		id);
		super.put(NAME,		name);
	}
	
	public int describeContents() {
        return 0;
    }
	
	public void writeToParcel(Parcel out, int flags) {
		out.writeMap(this);
    }
	
	public static final Parcelable.Creator<ComboSkladItem> CREATOR = new Parcelable.Creator<ComboSkladItem>() {
		    	public ComboSkladItem createFromParcel(Parcel in) 
		    	{
		    		return new ComboSkladItem(in);
		    	}
		    	public ComboSkladItem[] newArray(int size) 
		    	{
		    		return new ComboSkladItem[size];
		    	}
	};
	
	private ComboSkladItem(Parcel in) {
    	this("","");		
    	in.readMap(this, ComboSkladItem.class.getClassLoader());
    }
	
	
}
