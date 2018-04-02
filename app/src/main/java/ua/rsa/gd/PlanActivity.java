package ua.rsa.gd;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SimpleCursorAdapter.ViewBinder;

import ua.rsa.gd.R;

public class PlanActivity extends ListActivity {
	public static final String EXTRA_CALL_FROM_DAYS_ACTIVITY = "call_from_days";
	private Cursor mCursor;
	private ListAdapter mAdapter;
	private String[]  mContent = {"_id", "cast(" + RsaDbHelper.PLAN_ID + " as number) as sid",
										 RsaDbHelper.PLAN_CUST_ID, RsaDbHelper.PLAN_SHOP_ID,
										 RsaDbHelper.PLAN_CUST_TEXT, RsaDbHelper.PLAN_SHOP_TEXT}; 
	private String[]  mContent2 = {"_id", "sid", 
										 RsaDbHelper.PLAN_CUST_ID, RsaDbHelper.PLAN_SHOP_ID,
										 RsaDbHelper.PLAN_CUST_TEXT, RsaDbHelper.PLAN_SHOP_TEXT}; 
	public static final int MAKEORDER_FROM_PLAN_REQUEST = 400;
	private static SQLiteDatabase db;
	private boolean lightTheme;
	private static int verOS = 0;
	private Outlet firstOutletInPlan = null;
	private Map<String, String> mapIndex;
	private SharedPreferences mPrefs;
	private int mShowByDay = -1;
	
	public void onCreate(Bundle savedInstanceState) {
		Bundle extras;
		mapIndex = new HashMap<String, String>();
		verOS = 2;
		try {
			verOS = Integer.parseInt(Build.VERSION.RELEASE.substring(0,1));
		} catch (Exception e) {}
		mPrefs = getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE);
		lightTheme = mPrefs.getBoolean(RsaDb.LIGHTTHEMEKEY, false);
		if (lightTheme) {
			setTheme(R.style.Theme_Custom);
			super.onCreate(savedInstanceState);
			setContentView(R.layout.l_plan);
		} else {
			setTheme(R.style.Theme_CustomBlack2);
			super.onCreate(savedInstanceState);
			setContentView(R.layout.plan);
		}
		if (savedInstanceState != null) {
        	extras = savedInstanceState;
        } else {
    		extras = getIntent().getExtras();
			if (extras!=null) {
				mShowByDay = extras.getInt(EXTRA_CALL_FROM_DAYS_ACTIVITY, -1);
			}
        }
		
		String[] Days = getResources().getStringArray(R.array.Days);
		String[] Months = getResources().getStringArray(R.array.Months);
		Calendar c = Calendar.getInstance();
		StringBuilder curDate = new StringBuilder(Days[c.get(Calendar.DAY_OF_WEEK)-1] + ", ");
		curDate.append(c.get(Calendar.DAY_OF_MONTH) + " ");
		curDate.append(Months[c.get(Calendar.MONTH)]);
		((TextView)findViewById(R.id.plan_title)).setText(curDate);
	}

	@Override
	public void onStart() {
		super.onStart();
		RsaDbHelper mDb = new RsaDbHelper(this, mPrefs.getString(RsaDb.ACTUALDBKEY, RsaDbHelper.DB_NAME1));
        db = mDb.getReadableDatabase();
		updateList();
        Button btnBack = (Button)findViewById(R.id.plan_pbtn_prev);
        btnBack.setOnClickListener(new OnClickListener()
        {
			@Override
			public void onClick(View v) 
			{ 	
		    	finish();
			}				
		});
	}

	synchronized public Outlet getLastOutlet() {
		Outlet o = null;
		RsaDbHelper 	mDb_ord	  = new RsaDbHelper(this, RsaDbHelper.DB_ORDERS);
		SQLiteDatabase db_orders  = mDb_ord.getReadableDatabase();
		Cursor			cursor = null;
		Calendar		c = Calendar.getInstance();
		SimpleDateFormat	fmt	= new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
		String 			today = fmt.format(c.getTime());
		
		try {
			cursor = db_orders.rawQuery("select CUST_ID, SHOP_ID from _head where SDATE='"+today+"' order by _id desc limit 1", null);
			if (cursor.moveToFirst()) {
				o = new Outlet(cursor.getString(0), cursor.getString(1));
			}
		} catch (Exception e) {}
		
		if (cursor!=null && !cursor.isClosed()) {
			cursor.close();
		}
		if (db_orders!=null && db_orders.isOpen()) {
			db_orders.close();
		}
		return o;
	}
	
	private void updateList() {
				// Get data from database to mCursor by call query:
				// SELECT mContent FROM TABLE_PLAN        
				//mCursor = db.query(RsaDbHelper.TABLE_GROUP, mContent, 
				//		RsaDbHelper.GROUP_NAME + " LIKE '%" + filterText.getText() + "%'", 
				//		null, null, null, null);
		        Calendar c = Calendar.getInstance();
		        int d = c.get(Calendar.DAY_OF_MONTH);
		        int m = c.get(Calendar.MONTH)+1;
		        int y = c.get(Calendar.YEAR);
		        String curDate = String.format(Locale.getDefault(), "%02d%02d%04d", d, m, y);

				try	{
					if (mShowByDay == -1) {
						mCursor = db.query(RsaDbHelper.TABLE_PLAN, mContent,
								RsaDbHelper.PLAN_DATEV + "='" + curDate + "'",
								null, null, null,
								"sid");
					} else {
						String curDay = Integer.toString(mShowByDay - 1);
						mCursor = db.query(RsaDbHelper.TABLE_PLAN, mContent,
								"CAST(strftime('%w', DATE(substr(DATEV,5,4)||'-'||substr(DATEV,3,2)"
										+ "||'-'||substr(DATEV,1,2))) as integer)="+curDay,
								null, null, null,"sid");
					}
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}

				if (verOS<3) startManagingCursor(mCursor);
		        mCursor.moveToFirst();
		        if (mCursor.getCount()>0) {
		        	firstOutletInPlan = new Outlet(mCursor.getString(2), mCursor.getString(3));
		        }
		        
		        for (int i=0;i<mCursor.getCount();i++) {
		        	mapIndex.put(mCursor.getString(1), Integer.toString(i+1)+".");
		        	mCursor.moveToNext();
		        }
		        
		        mCursor.moveToFirst();
		        mAdapter = new SimpleCursorAdapter(
		        					this, lightTheme?R.layout.l_list_plan:R.layout.list_plan, mCursor, 
		        					mContent2, 
		        					new int[] {0, R.id.txtOrder_plan, 0, 0, R.id.txtCust_plan, R.id.txtShop_plan}); 

		        ((SimpleCursorAdapter) mAdapter).setViewBinder(new ViewBinder()  {
		            public boolean setViewValue(View aView, Cursor aCursor, int aColumnIndex)   {
		                if (aColumnIndex == 1)  {
		                        TextView textView = (TextView) aView;
		                        textView.setText(mShowByDay!=-1?null:mapIndex.get(aCursor.getString(1)));
		                        return true;
		                }
		                if (aColumnIndex == 4 && mShowByDay == -1)  {

	                        TextView textView = (TextView) aView;
	                        Outlet o = getLastOutlet();
	                        String currentCustID = mCursor.getString(2);
	                        String currentShopID = mCursor.getString(3);
	                        
	                        View prnt = (View)(aView.getParent().getParent().getParent());
	                        
	                        if (o==null && currentCustID.equals(firstOutletInPlan.getCust_id())
	                        		&& currentShopID.equals(firstOutletInPlan.getShop_id())) {
	                        	prnt.setBackgroundColor(Color.parseColor("#3300FF00"));
	                        } else if (o!=null && currentCustID.equals(o.getCust_id())
	                        		&& currentShopID.equals(o.getShop_id())) {
	                        	prnt.setBackgroundColor(Color.parseColor("#3300FF00"));
	                        } else {
	                        	prnt.setBackgroundColor(Color.TRANSPARENT);
	                        }

	                        return false;
		                }
		                return false;
		            }
		        });
		        setListAdapter(mAdapter);
	}

	@Override
	public void onStop() {
		try {
	    	      super.onStop();
	    	      if (this.mAdapter !=null) {
	    	        ((CursorAdapter) this.mAdapter).getCursor().close();
	    	        this.mAdapter= null;
	    	      }
	    	      if (this.mCursor != null)  {
	    	        this.mCursor.close();
	    	      }
	    	      if (db != null)  {
	    	        db.close();
	    	      }
	    	}  catch (Exception error)  { }
	}

	@Override
	protected void onDestroy() {
	   super.onDestroy();
	   db.close();  
	}

	@Override
	protected void onSaveInstanceState (Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	public void onListItemClick(ListView parent, View v, int position, long id) {
		Intent intent = new Intent();
		try  {
			mCursor.moveToPosition(position);
			intent.putExtra("_CUSTID", mCursor.getString(2));
			intent.putExtra("_SHOPID", mCursor.getString(3));
			intent.putExtra("MODE", RSAActivity.IDM_ADD);
			intent.setClass(this, NewHeadActivity.class);
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(),  "1> Error: mCursor.moveToPosition(" + Integer.toString(position)
													,Toast.LENGTH_LONG).show();
			return;
		}
		try {
			if (db != null)  {
		       db.close();
		    }
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(),  "1> Error: db.close()\n"
					+"3> position = " +Integer.toString(position),Toast.LENGTH_LONG).show();
		}
		
		try {
			startActivityForResult(intent, MAKEORDER_FROM_PLAN_REQUEST);
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(),  "1> Error: startActivity()\n"
					+"3> position = " +Integer.toString(position),Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onBackPressed() {
    	finish();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MAKEORDER_FROM_PLAN_REQUEST)  {
        	if (resultCode == RESULT_OK) {
        		finish();
        	}
        }
    }

}
