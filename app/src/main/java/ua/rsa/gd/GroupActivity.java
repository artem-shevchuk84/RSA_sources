package ua.rsa.gd;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import ua.rsa.gd.R;
import ua.rsa.gd.utils.DataUtils;

/**
 * Activity that allows user to view list goods groups
 * and user can select one them to add some products in
 * next activity (GoodsActivity) 
 * @author Komarev Roman
 * Odessa, neo3da@mail.ru, +380503412392
 */
public class GroupActivity extends ListActivity 
{
	/** Is set of data from DB to be shown in ListActivity. To get value have to use mCurosr.getString("KEY") */
	private Cursor mCursor;
	/** Special adapter that must be used between mCursor and ListActivity */
	private SimpleAdapter mAdapter;
	/** SQLite database that stores all data */
	private static SQLiteDatabase db;
	/** Used for identify sending data of current order to new activity(GoodsActivity.class) */
	static final int PICK_GOODS_REQUEST = 0;
	/** Designed class to store data of current order before save it to database */
	private OrderHead mOrderH;
	private ArrayList<String> arrayPricetypes;
	private EditText filterText = null;
	private Button btnClear = null;
	/** Current position in ListView */
	int iidx;
	int ttop;
	/** Count of items in TABLE_GROUP */
	int countOfGroupItems;
	
	public static final int SHOW_ALL				= 0;
	public static final int SHOW_ONLY_GROUPS		= 1;
	public static final int SHOW_ONLY_BRANDS		= 2;
	
	
	/** Current theme */
	private boolean lightTheme;
	private boolean isEkvator;
	private boolean cleared = false;
	private String idGroupBeforeClear;
	
	private static int verOS = 0;
	
	/**
	 * Creates the activity
	 * @param savedInstanceState previous saved state of activity
	 */
	public void onCreate(Bundle savedInstanceState) 
	{
		Bundle extras;

		verOS = 2;
		try {
			verOS = Integer.parseInt(Build.VERSION.RELEASE.substring(0,1));
		} catch (Exception e) {};
		lightTheme = getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE).getBoolean(RsaDb.LIGHTTHEMEKEY, false);
		isEkvator = getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE).getBoolean(RsaDb.EKVATOR, false);
		
		
		if (lightTheme) {
			setTheme(R.style.Theme_Custom);
			super.onCreate(savedInstanceState);
			setContentView(R.layout.l_group);
		} else {
			setTheme(R.style.Theme_CustomBlack2);
			super.onCreate(savedInstanceState);
			setContentView(R.layout.group);
		}
		
		iidx = 0;
		ttop = 0;
		
		if (savedInstanceState != null) {
        	extras = savedInstanceState;
        } else {
    		extras = getIntent().getExtras();
        }
		
		// Init class to store data of current order before save it to database
        mOrderH = new OrderHead();
		// Get data of current order from back activity(LinesActivity.class) to this */
		mOrderH = extras.getParcelable("ORDERH");
		
		filterText = (EditText) findViewById(R.id.group_edit);
		filterText.addTextChangedListener(filterTextWatcher);
		
		btnClear = (Button) findViewById(R.id.groups_pbtn_clear);
		btnClear.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (filterText.getText().length()<1)
					return;
				ListView lv = getListView();
				int pos = lv.getFirstVisiblePosition();
				
				mCursor.moveToPosition(pos);
				idGroupBeforeClear = mCursor.getString(2);
				cleared = true;
				
				filterText.setText("");
				updateList();
			}
		});
		
		registerForContextMenu(getListView());
        registerForContextMenu(getListView().getEmptyView());
	}
	
	/**
	 * Filter
	 */
	private TextWatcher filterTextWatcher = new TextWatcher() {
	    public void afterTextChanged(Editable s) {
	    }
	    public void beforeTextChanged(CharSequence s, int start, int count,
	            int after) {
	    }
	    public void onTextChanged(CharSequence s, int start, int before,
	            int count) {
	    	updateList();
	    }

	};
	
	/**
	 * Metgod that starts every time when Activity is shown on display
	 */	
	@Override
	public void onStart()
	{
		super.onStart();
		filterText.removeTextChangedListener(filterTextWatcher);
		filterText.setText("");
		filterText.addTextChangedListener(filterTextWatcher);
		
	    /** Get Shared Preferences */
		SharedPreferences prefs = getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);
		
		// Init database with architecture that designed in RsaDbHelper.class
		RsaDbHelper mDb = new RsaDbHelper(this, prefs.getString(RsaDb.ACTUALDBKEY, RsaDbHelper.DB_NAME1));
		
		// Open database
        db = mDb.getReadableDatabase();
        
        arrayPricetypes = new ArrayList<String>();
		if (getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE).getBoolean(RsaDb.PRICETYPEKEY, false)) {
			try {
				loadPriceTypes(db, arrayPricetypes, mOrderH.cust_id.toString());
			}catch (Exception e) {
				Toast.makeText(getApplicationContext(), "Не заполнены типы цен!", Toast.LENGTH_LONG).show();
			}
		}
        
		updateList();
        
        // Button when wants to go back to lines, by pressing
        // it current activity going down, and previous activity
        // starts (LinesActivity)
        Button btnBack = (Button)findViewById(R.id.group_pbtn_prev);
        
        // Listener for OK-button click, calls when OK-button clicked
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
	}
	
	
	/**
	* Used to update list of groups on device display
	* with data from database
	*/
	private void updateList()
	{
				// Get data from database to mCursor by call query:
				// SELECT mContent FROM TABLE_GROUP        
				//mCursor = db.query(RsaDbHelper.TABLE_GROUP, mContent, 
				//		RsaDbHelper.GROUP_NAME + " LIKE '%" + filterText.getText() + "%'", 
				//		null, null, null, null);
		        
				String mq =   "SELECT _id, NAME, ID "
							+ "FROM ( SELECT _id, NAME, ID "
							+		 "FROM _brand "
							+		 "WHERE NAME LIKE '%" +  filterText.getText()  + "%' "
							+		 "ORDER BY NAME ) "
				            + "UNION ALL "
				            + "SELECT _id, NAME, ID "
				            + "FROM ( SELECT _id, NAME, ID "
				            + 		 "FROM _group "
				            + 		 "WHERE NAME LIKE '%" +  filterText.getText()  + "%' "
				            +		 "ORDER BY NAME )";
		
				String safe_mq = 	  "SELECT _id, NAME, ID " 
				        			+ "FROM _group " 
				        			+ "WHERE NAME LIKE '%" +  filterText.getText()  + "%' "
				        			+ "ORDER BY NAME";
				
				String brand_mq = 	  "SELECT _id, NAME, ID " 
				        			+ "FROM _brand " 
				        			+ "WHERE NAME LIKE '%" + filterText.getText()  + "%' "
				        			+ "ORDER BY NAME";
		
				countOfGroupItems = 0;
				
				SharedPreferences prefs = getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);
				
				int key = prefs.getInt(RsaDb.BRANDGROUPSHOW, SHOW_ALL);
				Cursor tmpCursor = null;
				try	
				{
					tmpCursor = db.rawQuery(brand_mq, null);
					if (key==SHOW_ALL) {
						countOfGroupItems = tmpCursor.getCount();
						mCursor = db.rawQuery(mq, null);
					} else if (key==SHOW_ONLY_GROUPS) {
						countOfGroupItems = 0;
						mCursor = db.rawQuery(safe_mq, null);
					} else if (key==SHOW_ONLY_BRANDS) {
						countOfGroupItems = tmpCursor.getCount();
						mCursor = tmpCursor;
					}
				}
				catch (Exception e)
				{
					mCursor = db.rawQuery(safe_mq, null);
				}
				
				// init mCursor to work with it
				// Romka 19.12.2012
				if (verOS<3) startManagingCursor(mCursor);
		        // move to first record in mCursor
		        mCursor.moveToFirst();
		        
		        ////////////////////////////////////////
		        ///////////////////////////////////////
		        Map<String, String> m;
				ArrayList<Map<String, String>> listData;
				listData = new ArrayList<Map<String, String>>();
				m = new HashMap<String, String>();
	        	m.put("groupName", "TOP SKU"); 
	        	m.put("groupComment", "Выборка топовых товаров");
	        	listData.add(m);
				for (int i=0;i<mCursor.getCount();i++) {
					m = new HashMap<String, String>();
		        	m.put("groupName", mCursor.getString(1));
		        	m.put("groupComment", getComment(db, i>=countOfGroupItems, mCursor.getString(2), mOrderH.cust_id.toString(), mOrderH.shop_id.toString()));
		        	listData.add(m);
		        	mCursor.moveToNext();
				}
				String listFrom[] = new String[] {"groupName", "groupComment"};
		        int listTo[] = new int[] {R.id.txtName_group, R.id.txtComment_group};
				//Collections.sort(listData, groupsComparator);
		        // Init mAdapter with binding data in mCursor with values on group.xml
		        mAdapter = new SimpleAdapter(
		        					this,
									listData,
		        					R.layout.list_group, 
		        					listFrom, 
		        					listTo); 
		        
		        // Set mAdapter to ListActivty data adapter to show data from mCursor on device display
		        setListAdapter(mAdapter);
		        ListView lv = this.getListView();
		        if (cleared) {
					cleared = false;
					ttop = 0;
					iidx = 0;
					
					int c = mCursor.getCount();
					if (c>0) {
						mCursor.moveToFirst();
						for (int i=0;i<c;i++) {
							if (mCursor.getString(2).equals(idGroupBeforeClear)) { 
								iidx = i;
								break;
							}
							mCursor.moveToNext();
						}
					}
				}
				lv.setSelectionFromTop(iidx, ttop);
		        
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
				txtTotalSum.setText(DataUtils.Float.format("%.2f",hsumo) + " $");
	}

	Comparator<Map<String, String>> groupsComparator = new Comparator<Map<String,String>>() {

		@Override
		public int compare(Map<String, String> o1, Map<String, String> o2) {
			// Get the distance and compare the distance.
			String groupName1 = o1.get("groupName");
			String groupName2 = o2.get("groupName");

			if(groupName1.equals("TOP SKU")){
				return 1;
			}
			return groupName1.compareTo(groupName2);
		}
	};

	private static String getComment( SQLiteDatabase __db, boolean __isGroup, String __id, String _custid, String _shopid) {
		String result = "";
		Cursor __cur = null;
		String q = null; 
		
		if (__isGroup == true) { 
			q = "select COMMENT from _sold where CUST_ID='"+_custid+"' and GROUP_ID='"+__id+"' and SHOP_ID='"+_shopid+"' limit 1";
		//	System.out.println("ROMKA=" + q);
		} else {
			q = "select COMMENT from _sold where CUST_ID='"+_custid+"' and BRAND_ID='"+__id+"' and SHOP_ID='"+_shopid+"' limit 1";
		//	System.out.println("ROMKA=" + q);
		}
		
		try {
			__cur = __db.rawQuery(q, null);
			
			if (__cur.getCount()>0) {
				__cur.moveToFirst();
				result = __cur.getString(0);
			}			
		} catch (Exception __e) {}
		
		try {
			if (__cur != null) 
				__cur.close();
		} catch (Exception __e) {}
		return result;
	}
	
	/**
     * Metohd that starts when another activity becomes on display
     */	
	@Override
	public void onStop()
	{
	    	try 
	    	{
	    	      super.onStop();
	    	      
	    	      // We have to release mAdapter with that Cursor
	    	      // before Activity will close
	    	      if (this.mAdapter !=null)
	    	      {
	    	    	 
	    	        this.mAdapter= null;
	    	      }
	    	      
	    	      // We have to release mAdapter with that Cursor
	    	      // before Activity will close
	    	      if (this.mCursor != null) 
	    	      {
	    	        this.mCursor.close();
	    	      }
	    	      
	    	      // Before Activity will be closed we have to close database
	    	      // to allow use it in another activities
	    	      if (db != null) 
	    	      {
	    	        db.close();
	    	      }
	    	} 
	    	catch (Exception error) 
	    	{
	    	      /** Error Handler Code **/
	    	}
	}
	
	
    /**
     * Method that starts if system trying to destroy activity
     */	
	@Override
	protected void onDestroy() 
	{
	   super.onDestroy();
	   
	   // Filter
   	   filterText.removeTextChangedListener(filterTextWatcher);
   	   // If some unexpected situation has occurred and
   	   // this Activity going to Destroy we have to 
   	   // close database to allow use it in another activities	   
	   db.close();  
	}
	
	/**
	 *  Saving activity positions on destroy .. or if display rotation
	 */
	@Override
	protected void onSaveInstanceState (Bundle outState)
	{
		super.onSaveInstanceState(outState);
		
		outState.putParcelable("ORDERH", mOrderH);
	}
	
	
	/**
     * Selects item and shows pop-up dialog to choose quantity of selected goods
     * @param parent - In this param system puts pointer to ListView of Activity
     * @param v - In this param system puts pointer to selected View, used to apply selection
     * @param position - In this param system puts index of selected order, used to set {@value} listPosition
     * @param id - In this param system puts id of selected View
     */	
	public void onListItemClick(ListView parent, View v, int position, long id)
	{
		/** Used for prepearing to start another activity and send some data to it */
		Intent intent = new Intent();
		position = position-1;
		
		if (position<0) {
			intent.putExtra("isTOPSKU", true);
			intent.putExtra("isGroup", false);
		} else {
			try 
			{
				intent.putExtra("isTOPSKU", false);
				// Move to selected position in list of group
				mCursor.moveToPosition(position);
				// Put value GROUP_ID of selected group into intent variable
				// for sending to another activity (GoodsActivity.class)
				if (position >= countOfGroupItems) {
					intent.putExtra(RsaDbHelper.GROUP_ID, mCursor.getString(2));
					intent.putExtra(RsaDbHelper.GROUP_NAME, mCursor.getString(1).toUpperCase());
					intent.putExtra("isGroup", true);
				} else {
					intent.putExtra(RsaDbHelper.BRAND_ID, mCursor.getString(2));
					intent.putExtra(RsaDbHelper.BRAND_NAME, mCursor.getString(1).toUpperCase());
					intent.putExtra("isGroup", false);
				}
			}
			catch (Exception e)
			{
				Toast.makeText(getApplicationContext(),  "1> Error: mCursor.moveToPosition(" + Integer.toString(position) + ")\n"
														+"2> countOfGroupItems = " + Integer.toString(countOfGroupItems),Toast.LENGTH_LONG).show();
				return;
			}
		}
		
		try 
		{
			// Put pricetypes array of current customer in mOrderH
			intent.putStringArrayListExtra("PR_TYPES", arrayPricetypes);
			
			// Put data of current order to intent for using in next activity (GoodsActivity.class)
			intent.putExtra("ORDERH", mOrderH);		
			
			// Put current listview position
			ListView lv = this.getListView();
			intent.putExtra("INDEX", lv.getFirstVisiblePosition());
			View mv = lv.getChildAt(0);
			int top = (mv == null) ? 0 : mv.getTop();
			intent.putExtra("TOP", top);
			
			boolean usingMatrix 	= getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE).getBoolean(RsaDb.MATRIXKEY, false);
			
			// Set class for calling activity as GoodsActivity.class
			if (isEkvator == true) {
				intent.setClass(this, EkvatorGoodsActivity.class);
			} else if (usingMatrix) {
				intent.setClass(this, MatrixGoodsActivity.class);
			} else {
				intent.setClass(this, GoodsActivity.class);
			}
		}
		catch (Exception e)
		{
			Toast.makeText(getApplicationContext(),  "1> Error: saving list position\n"
													+"2> countOfGroupItems = " + Integer.toString(countOfGroupItems) + "\n"
													+"3> position = " +Integer.toString(position),Toast.LENGTH_LONG).show();
		}
			
		try
		{
			// Close database to allow use it in another activities	 
			if (db != null) 
		    {
		       db.close();
		    }
		}
		catch (Exception e)
		{
			Toast.makeText(getApplicationContext(),  "1> Error: db.close()\n"
					+"2> countOfGroupItems = " + Integer.toString(countOfGroupItems)+"\n"
					+"3> position = " +Integer.toString(position),Toast.LENGTH_LONG).show();
		}
		
		
		try
		{
			// Starts GoodsActivity.class (with sending GROUP_ID by intent extra)
			startActivityForResult(intent, PICK_GOODS_REQUEST);		
		}
		catch (Exception e)
		{
			Toast.makeText(getApplicationContext(),  "1> Error: startActivity()\n"
					+"2> countOfGroupItems = " + Integer.toString(countOfGroupItems)+"\n"
					+"3> position = " +Integer.toString(position),Toast.LENGTH_LONG).show();
		}
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
	protected void onActivityResult(int requestCode, int resultCode, Intent data) 
	{
		/** Data from previous activity (GoodsActivity.class) to extras variable */
		Bundle extras;
		
		// if arrived data from GoodsActivity then...
        if (requestCode == PICK_GOODS_REQUEST) 
        {
        	if (resultCode == RESULT_OK)
        	{
                // Get data from previous activity (GoodsActivity.class) to extras variable */
        		extras = data.getExtras();
        		
            	// Init class to store data of current order before save it to database 
                mOrderH = new OrderHead();
                
        		// Get data of current order from GoodsActivity.class to this  */
        		mOrderH = extras.getParcelable("ORDERH");
        		
        		// Set previous listview position
        		iidx = extras.getInt("INDEX");
        		ttop = extras.getInt("TOP");
        	}
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
			txtTotalSum.setText(DataUtils.Float.format("%.2f",hsumo) + " $");
        } catch (Exception e) {
        	Toast.makeText(getApplicationContext(),  "Некритичный сбой",Toast.LENGTH_LONG).show();
        }
    }
	
	@Override
    public void onCreateContextMenu(ContextMenu menu, View v,ContextMenu.ContextMenuInfo menuInfo) 
    {
    	menu.add(Menu.NONE, SHOW_ALL,  		   Menu.NONE, R.string.group_showall);
    	menu.add(Menu.NONE, SHOW_ONLY_GROUPS,  Menu.NONE, R.string.group_showgroups);
    	menu.add(Menu.NONE, SHOW_ONLY_BRANDS,  Menu.NONE, R.string.group_showbrands);
    }
	
	@Override
    public boolean onContextItemSelected(MenuItem item) 
    {
		SharedPreferences prefs = getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);
		
    	switch (item.getItemId()) 
    	{
       		case SHOW_ALL:
       		{
       			prefs.edit().putInt(RsaDb.BRANDGROUPSHOW, SHOW_ALL).commit();
       			updateList();
       			return(true);
       		}
       		case SHOW_ONLY_GROUPS:
       		{
       			prefs.edit().putInt(RsaDb.BRANDGROUPSHOW, SHOW_ONLY_GROUPS).commit();
       			updateList();
       			return(true);
       		}
       		case SHOW_ONLY_BRANDS:
       		{
       			prefs.edit().putInt(RsaDb.BRANDGROUPSHOW, SHOW_ONLY_BRANDS).commit();
       			updateList();
       			return(true);
       		}
    	}
    	return(super.onOptionsItemSelected(item));
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
