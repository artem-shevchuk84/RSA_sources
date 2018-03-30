package ua.rsa.gd;

import java.util.HashMap;

import android.os.Parcel;
import android.os.Parcelable;

public class MutualItem extends HashMap <String, String> implements Parcelable {
	private static final long serialVersionUID = 1L;
	public static final String DOCUMENT = "document";
	public static final String SALE = "sale";
	public static final String PAYMENT = "payment";

	
	public MutualItem(String doc, String s, String p) {
		super();
		super.put(DOCUMENT, doc);
		super.put(SALE , s);
		super.put(PAYMENT, p);

		
	}
	
	public int describeContents() {
        return 0;
    }
	
	public void writeToParcel(Parcel out, int flags) {
		out.writeMap(this);
    }
	
	public static final Parcelable.Creator<MutualItem> CREATOR = new Parcelable.Creator<MutualItem>() {
		    	public MutualItem createFromParcel(Parcel in) 
		    	{
		    		return new MutualItem(in);
		    	}
		    	public MutualItem[] newArray(int size) 
		    	{
		    		return new MutualItem[size];
		    	}
	};
	
	private MutualItem(Parcel in) {
    	this("","","");		
    	in.readMap(this, MutualItem.class.getClassLoader());
    }
	
	
}
