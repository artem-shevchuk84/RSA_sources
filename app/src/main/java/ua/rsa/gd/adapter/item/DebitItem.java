package ua.rsa.gd.adapter.item;

import java.util.HashMap;

import android.os.Parcel;
import android.os.Parcelable;

public class DebitItem extends HashMap <String, String> implements Parcelable {
	private static final long serialVersionUID = 1L;
	public static final String DATEDOC = "datedoc";
	public static final String SUM = "sumdoc";
	public static final String EXPDATE = "expdate";
	public static final String DOCNUMBER = "docnumber";
	public static final String EXPIRED = "expired";
	public static final String CASH = "cash";
	public static final String CURRENCY = "currency";
	public static final String COMMENT = "comment";
	public static final String ADDRESS = "address";
	public static final String STARTDEBT = "start_debt";
	
	public DebitItem(String datedoc, String sumdoc, String expdate, 
									String docnumber, String expired, String cash, String currency,
									String comment, String address, String start_debt) {
		super();
		super.put(DATEDOC, datedoc);
		super.put(SUM, sumdoc);
		super.put(EXPDATE, expdate);
		super.put(DOCNUMBER, docnumber);
		super.put(EXPIRED, expired);
		super.put(CASH, cash);
		super.put(CURRENCY, currency);
		super.put(COMMENT, comment);
		super.put(ADDRESS, address);
		super.put(STARTDEBT, start_debt);
		
		
	}
	
	public int describeContents() {
        return 0;
    }
	
	public void writeToParcel(Parcel out, int flags) {
		out.writeMap(this);
    }
	
	public static final Parcelable.Creator<DebitItem> CREATOR = new Parcelable.Creator<DebitItem>() {
		    	public DebitItem createFromParcel(Parcel in) 
		    	{
		    		return new DebitItem(in);
		    	}
		    	public DebitItem[] newArray(int size) 
		    	{
		    		return new DebitItem[size];
		    	}
	};
	
	private DebitItem(Parcel in) {
    	this("","","","","","","","","","");		
    	in.readMap(this, DebitItem.class.getClassLoader());
    }
	
	
}
