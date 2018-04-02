package ua.rsa.gd;

import java.util.ArrayList;



import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.TextView;

import ua.rsa.gd.R;

public class ShowmutualreportActivity extends Activity {

	private boolean lightTheme;
	private Bundle extras;
	private ListView grid;
	private TextView title;
	private ArrayList<MutualItem> list;
	private SimpleAdapter adapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {

		
		lightTheme = getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE).getBoolean(RsaDb.LIGHTTHEMEKEY, false);
		if (lightTheme) {
			setTheme(R.style.Theme_Custom);
			super.onCreate(savedInstanceState);
			setContentView(R.layout.l_showreport_debt);
		} else {
			setTheme(R.style.Theme_CustomBlackNoTitleBar);
			super.onCreate(savedInstanceState);
			setContentView(R.layout.showreport_debt);
		}
		
		if (savedInstanceState != null) {
        	extras = savedInstanceState;
        	Log.i("ShowpreportActivity", "import from saved instance");
        } else {
    		extras = getIntent().getExtras();
        }
		
		title = (TextView)findViewById(R.id.showreport_title);
		title.setText(extras.getString(ReportActivity.NAME));
		
		grid = (ListView)findViewById(R.id.showreport_list);
		
		list = new ArrayList<MutualItem>();
		
		String elem[] = extras.getStringArray(ReportActivity.ELEMENTS);
		int count = elem.length / 3;
		
		for (int i=0;i<count;i++) {
			
			list.add(new MutualItem(elem[i*3], elem[i*3+1], elem[i*3+2]));
		}
		
		adapter = new SimpleAdapter(getApplicationContext(), list, lightTheme?R.layout.l_list_mutual:R.layout.list_mutual,
											new String[] {MutualItem.DOCUMENT, MutualItem.SALE, MutualItem.PAYMENT},
											new int[] {R.id.txtDocument, R.id.txtSale, R.id.txtPayment});
		
		adapter.setViewBinder(new ViewBinder() {
			
			@Override
			public boolean setViewValue(View view, Object data,
					String textRepresentation) {
				
				switch (view.getId()) {
					case R.id.txtDocument: {
						TextView tv = (TextView) view;
		                
						fontBold ff = new fontBold();
						String t = checkForBold(textRepresentation,ff);
		                
	                    tv.setText(t);
		                tv.setTypeface(ff.getTypeface());
						
						return true;
					}
				};
				
				return false;
			}
		});
		
		grid.setAdapter(adapter);

	
	}
	
	
	private String checkForBold(String txt, fontBold ff)
	{
		if (txt==null) 
			return "";
		
		if (txt.contains("##")) {
			ff.setTypeFace(Typeface.DEFAULT_BOLD);
			return txt.replace("##", "");
		}
		
		return txt;
	}
	
	private class fontBold {
	
		Typeface typeFace;
		
		public fontBold() {
				typeFace = Typeface.DEFAULT;
		}
		
		public Typeface getTypeface() {
			return typeFace;
		}

		public void setTypeFace(Typeface tf) {
			typeFace = tf;
		}
		
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		Log.i("ShowpreportActivity", extras.getString("NAME"));
	}
	
	@Override
	public void onResume() {
		super.onResume();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		outState.putString(ReportActivity.NAME, extras.getString(ReportActivity.NAME));
		outState.putStringArray(ReportActivity.ELEMENTS, extras.getStringArray(ReportActivity.ELEMENTS));
		outState.putInt(ReportActivity.COLUMNS, extras.getInt(ReportActivity.COLUMNS));
		outState.putParcelable("ORDERH", extras.getParcelable("ORDERH"));
	}
	
	@Override
	public void onBackPressed()
	{
		OrderHead mOrderH = extras.getParcelable("ORDERH");
		
		Intent intent = new Intent();
		
		if (mOrderH != null) {
			Bundle b = new Bundle();
	    	b.putInt("MODE", mOrderH.mode);
	    	b.putString("_id", mOrderH._id);
	    	b.putString("SKLADID", mOrderH.sklad_id.toString());
	    	b.putString("REMARK", mOrderH.remark.toString());
	    	b.putString("DELAY", mOrderH.delay.toString());
	    	b.putString("DISCOUNT", mOrderH.id.toString());
	    	intent.putExtra("ORDERH", mOrderH);
	    	intent.putExtras(b);
	    }
    	setResult(RESULT_OK, intent);
    	
    	finish();
	}
}
