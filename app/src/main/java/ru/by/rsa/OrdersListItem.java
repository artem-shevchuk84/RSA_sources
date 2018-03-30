package ru.by.rsa;

import java.util.HashMap;

import android.os.Parcel;
import android.os.Parcelable;

public class OrdersListItem extends HashMap <String, String> implements Parcelable {
	private static final long serialVersionUID = 1L;
	public static final String DATE		= "date";
	public static final String NUM		= "num";
	public static final String SUM		= "sum";
	
	
	
	public OrdersListItem(String date, String num, String sum) {
		super();
		super.put(DATE,		date);
		super.put(NUM,		num);
		super.put(SUM,		sum);
	}
	
	public int describeContents() {
        return 0;
    }
	
	public void writeToParcel(Parcel out, int flags) {
		out.writeMap(this);
    }
	
	public static final Parcelable.Creator<OrdersListItem> CREATOR = new Parcelable.Creator<OrdersListItem>() {
		    	public OrdersListItem createFromParcel(Parcel in) 
		    	{
		    		return new OrdersListItem(in);
		    	}
		    	public OrdersListItem[] newArray(int size) 
		    	{
		    		return new OrdersListItem[size];
		    	}
	};
	
	private OrdersListItem(Parcel in) {
    	this("","","");		
    	in.readMap(this, OrdersListItem.class.getClassLoader());
    }
	
	
}
