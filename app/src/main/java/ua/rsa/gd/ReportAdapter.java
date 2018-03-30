package ua.rsa.gd;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ReportAdapter extends ArrayAdapter<String> {
	
	private String[] items;
	Context mContext;
	int headlines;
	int textColor;
	boolean isDebtRep;
	
	
	public ReportAdapter(Context context, int resource, String[] elements, int head, int color, boolean rep_debt) {
		super(context, resource, elements);
		items = elements;
		headlines = head;
		this.mContext = context;
		this.textColor = color;
		this.isDebtRep = rep_debt;
	}
	
	public View getView(int position, View converView, ViewGroup parent) {
		TextView label = (TextView)converView;
		if (converView == null) {
			converView = new TextView(mContext);
			label = (TextView)converView;
		}
		label.setTextColor(textColor);
		label.setText(items[position]);
		myFnt fnt = new myFnt();
		checkForFont(position, fnt);
		if (isDebtRep) {
			checkForGrav(position, fnt);
			checkForColr(position, fnt);
		}
		label.setTypeface(null, fnt.get());
		if (isDebtRep) {
			label.setGravity(fnt.getgrav());
			label.setBackgroundColor(fnt.getcolr());
		}
		return (converView);
	}
	
	public String getItem(int position) {
		return items[position];
	}
	
	private class myFnt {
		int style;
		int grav;
		int bg;
		
		public myFnt() {
			style = Typeface.NORMAL;
			grav  = Gravity.LEFT;
			bg	  = Color.TRANSPARENT;
		}
		
		public int get() {
			return style;
		}
		
		public int getgrav() {
			return grav;
		}
		
		public int getcolr() {
			return bg;
		}
		
		public void set(int s) {
			style = s;
		}
		
		public void setgrav(int s) {
			grav = s;
		}
		
		public void setcolr(int s) {
			bg = s;
		}
		
	}
	
	private void checkForFont(int pos, myFnt ff) {
		if (pos<headlines) {
			ff.set(Typeface.BOLD);
		}
	}
	
	private void checkForGrav(int pos, myFnt ff) {
		if ((pos+1)%2==0) {
			ff.setgrav(Gravity.RIGHT);
		}
	}
	
	private void checkForColr(int pos, myFnt ff) {
		if ((pos/2)%2==0) {
			ff.setcolr(textColor!=Color.WHITE?Color.LTGRAY:Color.parseColor("#282be160"));
		}
	}

}
