package ua.rsa.gd;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;

import ua.rsa.gd.R;

public class ShowDebtReportActivity extends Activity {

	private boolean lightTheme;
	private Bundle extras;
	private ListView list;
	private TextView title;
	private ReportAdapter adapter;
	
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
		list = (ListView)findViewById(R.id.showreport_list);
		adapter = new ReportAdapter(	getApplicationContext(), 
										android.R.layout.simple_list_item_1, 
										extras.getStringArray(ReportActivity.ELEMENTS),
										extras.getInt(ReportActivity.COLUMNS),
										lightTheme?Color.BLACK:Color.WHITE, true);
		list.setAdapter(adapter);
		
	}
	
	@Override
	protected void onStart() {
		super.onStart();
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
