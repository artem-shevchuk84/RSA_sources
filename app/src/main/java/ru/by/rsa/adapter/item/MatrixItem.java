package ru.by.rsa.adapter.item;

import java.util.HashMap;

import android.os.Parcel;
import android.os.Parcelable;

public class MatrixItem extends HashMap <String, String> implements Parcelable {
	private static final long serialVersionUID = 1L;
	public static final String ID		= "id";
	public static final String NAME		= "name";
	public static final String UN		= "un";
	public static final String COEFF	= "coeff";
	public static final String NDS		= "nds";
	public static final String QTY		= "qty";
	public static final String REST		= "rest";
	public static final String WEIGHT1	= "weight1";
	public static final String PRICE1	= "price1";
	public static final String MATRIX	= "matrix";
	public static final String AVG		= "avg";
	public static final String COEF		= "coef";
	public static final String DELIVERY = "delivery";
	public static final String SHARE 	= "share";
	
	public static final String VPERCENT	= "vpercent";
	public static final String CUSTOM1	= "custom1";
	public static final String DATE1 	= "date1";
	public static final String REST1 	= "rest1";
	public static final String RETURN1 	= "return1";
	public static final String ORDER1 	= "order1";
	public static final String DATE2 	= "date2";
	public static final String REST2 	= "rest2";
	public static final String RETURN2 	= "return2";
	public static final String ORDER2 	= "order2";
	public static final String DATE3 	= "date3";
	public static final String REST3 	= "rest3";
	public static final String RETURN3 	= "return3";
	public static final String ORDER3 	= "order3";
	public static final String DATE4 	= "date4";
	public static final String REST4 	= "rest4";
	public static final String RETURN4 	= "return4";
	public static final String ORDER4 	= "order4";
	public static final String DATE5 	= "date5";
	public static final String REST5 	= "rest5";
	public static final String RETURN5 	= "return5";
	public static final String ORDER5 	= "order5";
	public static final String DATE6 	= "date6";
	public static final String REST6 	= "rest6";
	public static final String RETURN6 	= "return6";
	public static final String ORDER6 	= "order6";
	public static final String DATE7 	= "date7";
	public static final String REST7 	= "rest7";
	public static final String RETURN7 	= "return7";
	public static final String ORDER7 	= "order7";
	public static final String DATE8 	= "date8";
	public static final String REST8 	= "rest8";
	public static final String RETURN8 	= "return8";
	public static final String ORDER8 	= "order8";
	public static final String DATE9 	= "date9";
	public static final String REST9 	= "rest9";
	public static final String RETURN9 	= "return9";
	public static final String ORDER9 	= "order9";
	
	public static final String BRAND_NAME = "brand_name";
	
	
	public MatrixItem(String id, String name, String un, String coeff, String nds, String qty, 
						String rest, String weight1, String price1, String matrix, String avg, 
						String coef, String delivery, String share,
						String vpercent, String date1, String rest1, String return1, String order1,
										 String date2, String rest2, String return2, String order2,
										 String date3, String rest3, String return3, String order3, 
										 String date4, String rest4, String return4, String order4,
										 String date5, String rest5, String return5, String order5,
										 String date6, String rest6, String return6, String order6,
										 String date7, String rest7, String return7, String order7,
										 String date8, String rest8, String return8, String order8,
										 String date9, String rest9, String return9, String order9,
										 String brand_name, String custom1) {
		super();
		super.put(ID, id);
		super.put(NAME, name);
		super.put(UN, un);
		super.put(COEFF, coeff);
		super.put(NDS, nds);
		super.put(QTY, qty);
		super.put(REST, rest);
		super.put(WEIGHT1, weight1);
		super.put(PRICE1, price1);
		super.put(MATRIX, matrix);
		super.put(AVG, avg);
		super.put(COEF, coef);
		super.put(DELIVERY, delivery);
		super.put(SHARE, share);
		
		super.put(VPERCENT, vpercent);
		super.put(CUSTOM1, custom1);

		super.put(DATE1, date1);
		super.put(REST1, rest1);
		super.put(RETURN1, return1);
		super.put(ORDER1, order1);
		super.put(DATE2, date2);
		super.put(REST2, rest2);
		super.put(RETURN2, return2);
		super.put(ORDER2, order2);
		super.put(DATE3, date3);
		super.put(REST3, rest3);
		super.put(RETURN3, return3);
		super.put(ORDER3, order3);
		
		super.put(DATE4, date4);
		super.put(REST4, rest4);
		super.put(RETURN4, return4);
		super.put(ORDER4, order4);
		super.put(DATE5, date5);
		super.put(REST5, rest5);
		super.put(RETURN5, return5);
		super.put(ORDER5, order5);
		super.put(DATE6, date6);
		super.put(REST6, rest6);
		super.put(RETURN6, return6);
		super.put(ORDER6, order6);
		super.put(DATE7, date7);
		super.put(REST7, rest7);
		super.put(RETURN7, return7);
		super.put(ORDER7, order7);
		super.put(DATE8, date8);
		super.put(REST8, rest8);
		super.put(RETURN8, return8);
		super.put(ORDER8, order8);
		super.put(DATE9, date9);
		super.put(REST9, rest9);
		super.put(RETURN9, return9);
		super.put(ORDER9, order9);
		
		super.put(BRAND_NAME, brand_name);
	}
	
	public int describeContents() {
        return 0;
    }
	
	public void writeToParcel(Parcel out, int flags) {
		out.writeMap(this);
    }
	
	public static final Parcelable.Creator<MatrixItem> CREATOR = new Parcelable.Creator<MatrixItem>() {
		    	public MatrixItem createFromParcel(Parcel in) 
		    	{
		    		return new MatrixItem(in);
		    	}
		    	public MatrixItem[] newArray(int size) 
		    	{
		    		return new MatrixItem[size];
		    	}
	};
	
	private MatrixItem(Parcel in) {
    	this("","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","" );
    	in.readMap(this, MatrixItem.class.getClassLoader());
    }
	
	
}
