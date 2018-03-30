package ua.rsa.gd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ExpandableListView.OnChildClickListener;

import ru.by.rsa.R;

/**
 * Activity that allows user to view list goods groups
 * and user can select one them to add some products in
 * next activity (GoodsActivity) 
 * @author Komarev Roman
 * Odessa, neo3da@mail.ru, +380503412392
 */
public class GroupErarhActivity1 extends Activity {

	static final int PICK_GOODS_REQUEST = 0;
	
	ArrayList<Map<String, String>> groupData;
	ArrayList<ArrayList<Map<String, String>>> childData;
	ExpandableListView elvPriceList;
	
	private EditText filterText = null;
	
	/** Current position in ListView */
	int iidx;
	int ttop;
	int groupPos;
	
	private OrderHead mOrderH;
	private ArrayList<String> arrayPricetypes;
	private boolean lightTheme;
	private static int verOS = 0;
	
	/**
	 * Creates the activity
	 * @param savedInstanceState previous saved state of activity
	 */
	public void onCreate(Bundle savedInstanceState) {
		Bundle extras;

		verOS = 2;
		try {
			verOS = Integer.parseInt(Build.VERSION.RELEASE.substring(0,1));
		} catch (Exception e) {};
		lightTheme = getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE).getBoolean(RsaDb.LIGHTTHEMEKEY, false);
		
		if (lightTheme) {
			setTheme(R.style.Theme_Custom);
			super.onCreate(savedInstanceState);
			setContentView(R.layout.l_igroup);
		} else {
			setTheme(R.style.Theme_CustomBlack2);
			super.onCreate(savedInstanceState);
			setContentView(R.layout.igroup);
		}
		
		iidx = 0;
		ttop = 0;
		groupPos= -1;
		
		if (savedInstanceState != null) {
        	extras = savedInstanceState;
        } else {
    		extras = getIntent().getExtras();
        }
		
		filterText = (EditText) findViewById(R.id.group_edit);
		filterText.addTextChangedListener(filterTextWatcher);
		
		// Init class to store data of current order before save it to database
        mOrderH = new OrderHead();
		// Get data of current order from back activity(LinesActivity.class) to this */
		mOrderH = extras.getParcelable("ORDERH");
		
		updateList();
	}
	
	private TextWatcher filterTextWatcher = new TextWatcher() {
	    public void afterTextChanged(Editable s) {
	    }
	    public void beforeTextChanged(CharSequence s, int start, int count,
	            int after) {
	    }
	    public void onTextChanged(CharSequence s, int start, int before,
	            int count) {
	    	if (count==3) {
				Intent intent = new Intent();
				intent.putExtra("isTOPSKU", false);
				intent.putExtra(RsaDbHelper.GROUP_ID, "0");
				intent.putExtra(RsaDbHelper.GROUP_NAME, "0");
				intent.putExtra("isGroup", true);
				
				try {
					intent.putStringArrayListExtra("PR_TYPES", arrayPricetypes);
					
					intent.putExtra("ORDERH", mOrderH);		
					intent.putExtra("INDEX", 0);
					intent.putExtra("TOP", 0);
					intent.putExtra("GPOSITION", 0);
					intent.putExtra("STEXT", s.toString());
				} catch (Exception e) {}
				
				filterText.setText("");
				intent.setClass(getApplicationContext(), GoodsActivity.class);
				startActivityForResult(intent, PICK_GOODS_REQUEST);
	    	}
	    }
	};
	
	/**
	 * Metgod that starts every time when Activity is shown on display
	 */	
	@Override
	public void onStart()
	{
		super.onStart();
		
	    /** Get Shared Preferences */
		SharedPreferences prefs = getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);
        
        arrayPricetypes = new ArrayList<String>();
		if (getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE).getBoolean(RsaDb.PRICETYPEKEY, false)) {
			try {
				RsaDbHelper mDb = new RsaDbHelper(this, prefs.getString(RsaDb.ACTUALDBKEY, RsaDbHelper.DB_NAME1));
		        SQLiteDatabase db = mDb.getReadableDatabase();
				loadPriceTypes(db, arrayPricetypes, mOrderH.cust_id.toString());
				db.close();
			}catch (Exception e) {
				Toast.makeText(getApplicationContext(), "Не заполнены типы цен!", Toast.LENGTH_LONG).show();
			}
		}
		
        Button btnBack = (Button)findViewById(R.id.group_pbtn_prev);
        btnBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				Bundle b = new Bundle();
				b.putBoolean("ISFROMGROUP",true);
		    	intent.putExtra("ORDERH", mOrderH);
		    	intent.putExtras(b);
		    	setResult(RESULT_OK, intent);
		    	finish();
			}				
		});
        updateList();
	}
	
	private void updateList() {
		if (getDataFromDatabase() == false) {
			Toast.makeText(getApplicationContext(), "Нет данных в БД!", Toast.LENGTH_LONG).show();
			return;
		}
		
		if (showList() == false) {
			Toast.makeText(getApplicationContext(), "Невозможно отобразить!", Toast.LENGTH_LONG).show();
			return;
		}
		
		try {
        	/** Ticket 33: Used to get Sum from lines in special class holder OrderH */
			ArrayList<OrderLines> mOrder = mOrderH.lines;
			/** Ticket 33: Used to calculate "Sum with NDS" of all goods in current order */
			float hsumo = 0;
			// Ticket 33: FOR statement for every item in ArrayList
			for (OrderLines CurLine : mOrder) 
			{
				//  Ticket 33: SUM all "sum with nds" from ordered goods one by one
				hsumo += Float.parseFloat(CurLine.get(OrderLines.SUMWNDS));
			} // Ticket 33: At the end of cycle hsumo stores total sum with nds of current order
			/** Ticket 33: Binding xml element of textview to variable */
			TextView txtTotalSum = (TextView)findViewById(R.id.group_txtTotalSum_text);
			// Ticket 33: Set xml text view of current order sum with calculated sum
			txtTotalSum.setText(String.format("%.2f",hsumo).replace(',', '.') + " $");
        } catch (Exception e) {
        	Toast.makeText(getApplicationContext(),  "Некритичный сбой",Toast.LENGTH_LONG).show();
        }
		
	}
	
	private boolean showList() {
		
		String groupFrom[] = new String[] {"groupName"};
        int groupTo[] = new int[] {android.R.id.text1};
        String childFrom[] = new String[] {"childName", "childId"};
        int childTo[] = new int[] {R.id.txt_item_name, 0};
        
		SimpleExpandableListAdapter adapter = new SimpleExpandableListAdapter(
	            this,
	            groupData,
	            lightTheme?R.layout.l_iexpandable_list_item:R.layout.iexpandable_list_item,
	            groupFrom,
	            groupTo,
	            childData,
	            R.layout.isimple_list_item,
	            childFrom,
	            childTo);
	            
		
	    elvPriceList = (ExpandableListView) findViewById(R.id.igroup_list);
	    elvPriceList.setAdapter(adapter);
	    
	    elvPriceList.setSelectionFromTop(iidx, ttop);
	    if (groupPos>=0) {
	    	elvPriceList.expandGroup(groupPos);
	    }
	    elvPriceList.setOnChildClickListener(new OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				
				
				// Put current listview position
				ListView lv = elvPriceList;
				View mv = lv.getChildAt(0);
				int top = (mv == null) ? 0 : mv.getTop();
				
				String cId = childData.get(groupPosition).get(childPosition).get("childId");
				String cName = childData.get(groupPosition).get(childPosition).get("childName");
				Intent intent = new Intent();
				intent.putExtra("isTOPSKU", false);
				intent.putExtra(RsaDbHelper.GROUP_ID, cId);
				intent.putExtra(RsaDbHelper.GROUP_NAME, cName.toUpperCase());
				intent.putExtra("isGroup", true);
				
				try {
					intent.putStringArrayListExtra("PR_TYPES", arrayPricetypes);
					
					intent.putExtra("ORDERH", mOrderH);		
					intent.putExtra("INDEX", lv.getFirstVisiblePosition());
					intent.putExtra("TOP", top);
					intent.putExtra("GPOSITION", groupPosition);
				} catch (Exception e) {}
				
				intent.setClass(getApplicationContext(), GoodsActivity.class);
				startActivityForResult(intent, PICK_GOODS_REQUEST);
				
				return false;
			}
		});
	    registerForContextMenu(elvPriceList);
		
		return true;
	}
	
	private boolean getDataFromDatabase() {
		Map<String, String> m;
		ArrayList<Map<String, String>> childDataItem;
		
		Cursor cGroups = null;
		String qGroups = "select _group.ID, _group.NAME, _group.BRAND_ID, _brand.NAME from _group " +
						 "inner join _brand on _group.BRAND_ID = _brand.ID " +
						 "order by _brand.NAME";
		int countGroups = 0;
		
		SharedPreferences prefs = getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);
		RsaDbHelper mDb = new RsaDbHelper(this, prefs.getString(RsaDb.ACTUALDBKEY, RsaDbHelper.DB_NAME1));
		SQLiteDatabase db = mDb.getReadableDatabase();
		
		cGroups = db.rawQuery(qGroups, null);
		countGroups = cGroups.getCount();
		
		if (countGroups>0) {
			groupData = new ArrayList<Map<String, String>>();
			childData = new ArrayList<ArrayList<Map<String, String>>>();
			childDataItem = new ArrayList<Map<String, String>>();
			String currentBrandName = "s1s";

			while (cGroups.moveToNext()) {
				if (currentBrandName.equals(cGroups.getString(3)) == false) { // if new brand detected
					if (currentBrandName.equals("s1s") == false) 
						childData.add(childDataItem); // then save previous child data but first time should not work
					currentBrandName = cGroups.getString(3);
					m = new HashMap<String, String>();
			        m.put("groupName", currentBrandName); 
			        groupData.add(m);
			        
			        childDataItem = new ArrayList<Map<String, String>>();
			        m = new HashMap<String, String>();
			        m.put("childName", cGroups.getString(1));
			    	m.put("childId", cGroups.getString(0));
			    	childDataItem.add(m);
				} else {
			        m = new HashMap<String, String>();
			    	m.put("childName", cGroups.getString(1));
			    	m.put("childId", cGroups.getString(0));
			    	childDataItem.add(m);
				}
			}
			childData.add(childDataItem);
		} 
		
		if (cGroups!=null && !cGroups.isClosed()) {
			cGroups.close();
		}
		if (db!=null && db.isOpen()) {
			db.close();
		}
		
		return countGroups>0;
	}
	/**
     * Metohd that starts when another activity becomes on display
     */	
	@Override
	public void onStop() {
		super.onStop();
	}
	
	
    /**
     * Method that starts if system trying to destroy activity
     */	
	@Override
	protected void onDestroy() {
	   super.onDestroy();
	}
	
	@Override
	protected void onSaveInstanceState (Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putParcelable("ORDERH", mOrderH);
	}
	
	/**
	 * If Back-button on device pressed then do...
	 */
	@Override
	public void onBackPressed()
	{
		Intent intent = new Intent();
		Bundle b = new Bundle();
		b.putBoolean("ISFROMGROUP",true);
    	intent.putExtra("ORDERH", mOrderH);
    	intent.putExtras(b);
    	
    	setResult(RESULT_OK, intent);
    	
    	finish();
	}
	
	/** 
	 * When user select some goods and go to this activity, he gets data of current order
	 *  by this method
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		/** Data from previous activity (GoodsActivity.class) to extras variable */
		Bundle extras;
		
		// if arrived data from GoodsActivity then...
        if (requestCode == PICK_GOODS_REQUEST)  {
        	if (resultCode == RESULT_OK)
        	{
        		extras = data.getExtras();
                mOrderH = new OrderHead();
        		mOrderH = extras.getParcelable("ORDERH");
        		
        		iidx = extras.getInt("INDEX");
        		ttop = extras.getInt("TOP");
        		groupPos = extras.getInt("GPOSITION");
        	}
        }
        updateList();
    }
	
	private void loadPriceTypes(SQLiteDatabase __db, ArrayList<String> __arr, String __id) {
		Cursor __cur;
		String __q = "select PRICE from _char where CUST_ID='" + __id + "' group by PRICE";
		int __count = 0;
		__cur = __db.rawQuery(__q, null);
		__count = __cur.getCount();
		
		if (__count>0) {
			__cur.moveToFirst();
			for (int i=0; i<__count; i++) {
				__arr.add(__cur.getString(0));
				__cur.moveToNext();
			}
		}
		if (__cur != null) {
			__cur.close();
		}
	}
}
