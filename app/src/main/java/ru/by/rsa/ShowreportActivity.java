package ru.by.rsa;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.OnHierarchyChangeListener;
import android.widget.GridView;
import android.widget.TextView;

public class ShowreportActivity extends Activity {

	private static int verOS = 0;
	private boolean lightTheme;
	private Bundle extras;
	private GridView grid;
	private TextView title;
	private ReportAdapter adapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		verOS = 2;
		try {
			verOS = Integer.parseInt(Build.VERSION.RELEASE.substring(0,1));
		}
		catch (Exception e) {};
		
		lightTheme = getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE).getBoolean(RsaDb.LIGHTTHEMEKEY, false);
		if (lightTheme) {
			setTheme(R.style.Theme_Custom);
			super.onCreate(savedInstanceState);
			setContentView(R.layout.l_showreport);
		} else {
			setTheme(R.style.Theme_CustomBlackNoTitleBar);
			super.onCreate(savedInstanceState);
			setContentView(R.layout.showreport);
		}
		
		if (savedInstanceState != null) {
        	extras = savedInstanceState;
        	Log.i("ShowpreportActivity", "import from saved instance");
        } else {
    		extras = getIntent().getExtras();
        }
		
		title = (TextView)findViewById(R.id.showreport_title);
		title.setText(extras.getString(ReportActivity.NAME));
		grid = (GridView)findViewById(R.id.showreport_grid);
		grid.setNumColumns(extras.getInt(ReportActivity.COLUMNS));
		adapter = new ReportAdapter(	getApplicationContext(), 
										android.R.layout.simple_list_item_1, 
										extras.getStringArray(ReportActivity.ELEMENTS),
										extras.getInt(ReportActivity.COLUMNS),
										lightTheme?Color.BLACK:Color.WHITE,
										extras.getInt("REP", 0)==ReportActivity.IDM_DEBTS);
		grid.setAdapter(adapter);
		if (extras.getInt("REP", 0) == ReportActivity.IDM_TOPSKU) {
			grid.setVerticalSpacing(75);
		} else if (extras.getInt("REP", 0) == ReportActivity.IDM_MUTUALDEBTS) {
			grid.setVerticalSpacing(75);
		} else if (extras.getInt("REP", 0) == ReportActivity.IDM_SALOUT_SKU) {
			grid.setVerticalSpacing(50);
		}
		/*
		if (extras.getInt("REP", 0) == ReportActivity.IDM_DEBTS) {
			final int ccc = lightTheme?Color.LTGRAY:Color.parseColor("#282be160");
			grid.setOnHierarchyChangeListener(new OnHierarchyChangeListener() {
				@Override
				public void onChildViewRemoved(View parent, View child) {
					// TODO Auto-generated method stub
				}
				
				@Override
				public void onChildViewAdded(View parent, View child) {
					// TODO Auto-generated method stub
					TextView t = (TextView)child;
					
					t.setBackgroundColor(ccc);
				}
			});
			//grid.setHorizontalSpacing(75);
			
			
			//v.setBackgroundColor(Color.RED);
			//System.out.println(grid.getItemAtPosition(2) );
		}*/
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
	    	intent.putExtra("ORDERH", mOrderH);
	    	intent.putExtras(b);
	    }
    	setResult(RESULT_OK, intent);
    	
    	finish();
	}
}
