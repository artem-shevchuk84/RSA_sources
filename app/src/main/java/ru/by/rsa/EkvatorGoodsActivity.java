package ru.by.rsa;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnShowListener;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.SimpleCursorAdapter.ViewBinder;

/**
 * Activity that allows user to view, add and change quantity of goods
 * to current order 
 * @author Komarev Roman
 * Odessa, neo3da@mail.ru, +380503412392
 */
public class EkvatorGoodsActivity extends ListActivity 
{    
	/** Final variables used in dialog */ 
	TextView txtName = null; 
	TextView txtId = null; 		
	TextView txtPrice = null; 
	TextView txtStopPrice = null;
	TextView txtWeight = null; 
	TextView txtRest = null; 		
	TextView txtCount = null; 	
	TextView txtTotal = null; 	
	EditText edtQty = null;
	EditText edtCustPrice = null; 
	EditText edtRest = null;
	CheckBox chkPack = null;
	Button   btnPrice = null;
	Button   btnClear = null;
	String[][] tblHistory;
	String[][] tblHistory_REC = new String[4][3];
	
	
	TextView curRecomend = null;
	TextView txtHistoryDate1 = null;
	TextView txtHistoryDate2 = null;
	TextView txtHistoryDate3 = null;
	
	TextView txtHistoryQTY1 = null;
	TextView txtHistoryQTY2 = null;
	TextView txtHistoryQTY3 = null;
	
	TextView txtHistoryRec1 = null;
	TextView txtHistoryRec2 = null;
	TextView txtHistoryRec3 = null;
	
	TextView txtHistoryRest1 = null;
	TextView txtHistoryRest2 = null;
	TextView txtHistoryRest3 = null;
	
	
	
	// Current Price
	String currentPrice = "";
	
	// 
	private boolean showRecomend;
	
	
	// Filter
	private EditText filterText = null;
		
	/** Store info of current position of list view of Group avtivity */
	int ttop;
	int iidx;
	/** Current position in ListView */
	int goods_iidx;
	int goods_ttop;
	
	/** SQLite database that stores orders data */
	private static SQLiteDatabase db_orders_REC;
	
	/** Designed class to store data of current order before save it to database */
	private OrderHead mOrderH;
	/** Is set of data from DB to be shown in ListActivity. To get value have to use mCurosr.getString("KEY") */ 
	private Cursor mCursor;
	/** Special adapter that must be used between mCursor and ListActivity */
	// private ListAdapter mAdapter;
	// Filter
	private SimpleCursorAdapter mAdapter;
	/** Constant to identify kind of dialog when dialog window will be called = Select qty of goods */
	private final static int IDD_COUNT = 0;
	/** Constant to identify kind of dialog when dialog window will be called = Show history of goods */
	private final static int IDD_HIST = 1;
	/** Constant to identify kind of dialog when dialog window will be called = Show pricelist */
	private final static int IDD_PRICE = 2;
	/** Temporary variable used if selected item already has been added */
	private OrderLines CurrentLine;
	/** Temporary variable used if selected item already has been added */
	private OrderRests CurrentRestsLine;
	/** Index shows line position in previous order with the same goods or -1 if such goods was not found */
	private int idx;
	/** Index shows restsline position in previous order with the same goods or -1 if such goods was not found */
	private int idx_rest;
	/** Data from previous activity (GroupActivity.class) to extras variable */
	private Bundle extras;
	/** SQLite database that stores all data */
	private static SQLiteDatabase db;
	/** Max value of current item */
	// private long maxQty;
	/** Selected position of selected goods */
	private int selectedPosition;
	/** Current group id of selected group (in previous Activity) */
	private String curGroupID;
	/** Current group name of selected group (in previous Activity) */
	private String curGroupName;
	/** Current brand id of selected brand (in previous Activity) */
	private String curBrandID;
	/** Current brand name of selected brand (in previous Activity) */
	private String curBrandName;
	/** if was selected BRAND (not GROUP) then false */
	private Boolean isGroup;
	private Boolean isTOPSKU;
	/** Array of columns that will be used to obtain data from DB-tables in columns with the same name */
	private String[]  mContent = {"_id", 	RsaDbHelper.GOODS_ID,										 
											RsaDbHelper.GOODS_GROUP_ID,
											RsaDbHelper.GOODS_QTY,
											RsaDbHelper.GOODS_NAME,
											RsaDbHelper.GOODS_PRICE1,
											RsaDbHelper.GOODS_WEIGHT1,
											RsaDbHelper.GOODS_UN,										
											RsaDbHelper.GOODS_REST,
											RsaDbHelper.GOODS_COEFF,
											RsaDbHelper.GOODS_NDS,   // [10]
											RsaDbHelper.GOODS_NPP,    // [11]
											RsaDbHelper.GOODS_WEIGHT, // [12]
											RsaDbHelper.GOODS_FLASH,
											RsaDbHelper.GOODS_PRICE20}; // [14]
	
	SharedPreferences merch_prefs;
	boolean merch_only;
	
	/** Current theme */
	private boolean lightTheme;
	private boolean cleared = false;
	private String idGoodsBeforeClear;
	
	private String currency;
	
	private ArrayList<String> arrayPricetypes;
	
	private boolean isPad;
	
	private static int verOS = 0;
	
	private class myInt {
		int color;
		public myInt() {
			color = Color.TRANSPARENT;
		}
		public int get() {
			return color;
		}
		public void set(int c) {
			color = c;
		}
	}
	
	private class fontFlash {
		int color;
		Typeface typeFace;
		
		public fontFlash(Typeface tf, int c) {
			color = c;
			typeFace = tf;
		}
		
		public int getColor() {
			return color;
		}
		public Typeface getTypeface() {
			return typeFace;
		}
		public void setColor(int c) {
			color = c;
		}
		public void setTypeFace(Typeface tf) {
			typeFace = tf;
		}
		
	}
	
	/**
	 * Creates the activity
	 * @param savedInstanceState previous saved state of activity
	 */
	public void onCreate(Bundle savedInstanceState) 
	{
		merch_prefs = PreferenceManager.getDefaultSharedPreferences(this);
		merch_only = merch_prefs.getBoolean("prefMerchOnly", false);
		
		isPad = false;
		verOS = 2;
		try
		{
			verOS = Integer.parseInt(Build.VERSION.RELEASE.substring(0,1));
		}
		catch (Exception e) {};
		
		lightTheme = getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE).getBoolean(RsaDb.LIGHTTHEMEKEY, false);
		showRecomend = getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE).getBoolean(RsaDb.SHOWRECINLIST, false);
		
		SharedPreferences def_prefs = PreferenceManager.getDefaultSharedPreferences(this);
		currency = " "+def_prefs.getString("prefCurrency", getResources().getString(R.string.preferences_currency_summary));
		
		if (lightTheme)
		{
			setTheme(R.style.Theme_Custom);
			super.onCreate(savedInstanceState);
			setContentView(R.layout.l_goods);
		}
		else
		{
			setTheme(R.style.Theme_CustomBlack2);
			super.onCreate(savedInstanceState);
			setContentView(R.layout.goods);
		}
		
		// init -1 that meens: by default is nothing selected
		selectedPosition = -1;
		ttop = 0;
		iidx = 0;
		goods_iidx = 0;
		goods_ttop = 0;
		
		// if activity was destroyed (for exmp. by display rotation)
		// then get data of current order (mOrderH) from saved 
		if (savedInstanceState != null)
        {
        	extras = savedInstanceState;
        	
        	// get selected goods position in list, used for dialog resuming
        	selectedPosition = extras.getInt("POSITION");
        	
        	goods_ttop = extras.getInt("GTOP");
        	goods_iidx = extras.getInt("GINDEX");
        }
        else
        {
        	// if not then
    		// Get data from previous activity (GroupActivity.class) to extras variable */
    		extras = getIntent().getExtras();
        }
		
		isGroup = extras.getBoolean("isGroup");
		isTOPSKU = extras.getBoolean("isTOPSKU");
		arrayPricetypes = extras.getStringArrayList("PR_TYPES");
		
		if (isTOPSKU==true) {
			// nothing
			
		} else {
			if (isGroup == true) {
				// take group id from selected group
				curGroupID = extras.getString(RsaDbHelper.GROUP_ID);
				curGroupName = extras.getString(RsaDbHelper.GROUP_NAME);
			} else {
				// take brand id from selected brand
				curBrandID = extras.getString(RsaDbHelper.BRAND_ID);
				curBrandName = extras.getString(RsaDbHelper.BRAND_NAME);
			}
		}
		
		
    	// get groupsactivity listview position
    	ttop = extras.getInt("TOP");
    	iidx = extras.getInt("INDEX");
		// Init class to store data of current order before save it to database
        mOrderH = new OrderHead();
        
		// Get data of current order from back activity(GroupActivity.class) to this */
		mOrderH = extras.getParcelable("ORDERH");
		
		// Get listview 
		ListView lv = getListView(); 
		// Create a longclick listener for items 
		lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){ 
		    @Override 
		    public boolean onItemLongClick(AdapterView<?> av, View v, int pos, long id) 
		    { 
		        onLongListItemClick(v,pos,id); 
		        return true; 
		    } 
		}); 
		
		// Filter
		filterText = (EditText) findViewById(R.id.goods_edit);
		// Filter
		filterText.addTextChangedListener(filterTextWatcher);
		
		btnPrice = (Button) findViewById(R.id.goods_pbtn_price);
		btnPrice.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				showDialog(IDD_PRICE);
			}
		});
		
		btnClear = (Button) findViewById(R.id.goods_pbtn_clear);
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
				idGoodsBeforeClear = mCursor.getString(1);
				cleared = true;
				filterText.setText("");
				updateList();
			}
		});
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
	 * Method that starts every time when Activity is shown on display
	 */
	@Override
	public void onStart()
	{
		
		super.onStart();
		
		isPad = RsaDb.checkScreenSize(this, 5);
		
	    /** Get Shared Preferences */
		SharedPreferences prefs = getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);
		
		// Init database with architecture that designed in RsaDbHelper.class
		RsaDbHelper mDb = new RsaDbHelper(this, prefs.getString(RsaDb.ACTUALDBKEY, RsaDbHelper.DB_NAME1));
		
		// Open database
        db = mDb.getReadableDatabase();  
       
        /** Init database with orders  */
		RsaDbHelper mDb_REC = new RsaDbHelper(this, RsaDbHelper.DB_ORDERS);
		// Open database 
		db_orders_REC = mDb_REC.getReadableDatabase();
        
        // Fill list with goods from database
		updateList();  
		
        // Button when wants to go back to groups, by pressing
        // it current activity going down, and previous activity
        // starts (GroupActivity)
        Button btnBack = (Button)findViewById(R.id.goods_pbtn_prev);
        
        // Listener for OK-button click, calls when OK-button clicked
        btnBack.setOnClickListener(new OnClickListener() 
        {
			@Override
			public void onClick(View v) {
				/** Used for prepearing data for another activity and send some data to it */
				Intent intent = new Intent();

		    	// Put data of current order to intent for using in next activity(GroupActivity.class)
		    	intent.putExtra("ORDERH", mOrderH);
		    	// Send previous position of listview of groupsactivity
		    	intent.putExtra("TOP", ttop);
		    	intent.putExtra("INDEX", iidx);
		    	// Set result of goods additting is OK and send it's data to GroupActivity.class
		    	setResult(RESULT_OK, intent);
		    	
		    	finish();
			}				
		});
	}
	
	private void checkForFlash(String sflash, fontFlash ff)
	{
		if (sflash==null) 
			return;
		
		if (sflash.equals("1")) {
			ff.setColor(Color.RED);
			ff.setTypeFace(Typeface.DEFAULT_BOLD);
		}
	}
	
	private String findOrderedGoods(String goodsId, myInt ii)
	{
		if ((goodsId==null) || (goodsId.equals(""))) 
			return "";
		
		for (OrderLines CurLine : mOrderH.lines) 
		{
			if (CurLine.get(OrderLines.GOODS_ID).equals(goodsId)) {
				
				ii.set(Color.parseColor(lightTheme?"#30000000":"#30FFFFFF"));
				return CurLine.get(OrderLines.QTY); 
			}
		}
		
		return "";
	}
	
	private String findFixedRest(String goodsId)
	{
		if ((goodsId==null) || (goodsId.equals("")))
			return "0";
		
		for (OrderRests CurLine : mOrderH.restslines) 
		{
			if (CurLine.get(OrderRests.GOODS_ID).equals(goodsId))
				return CurLine.get(OrderRests.RESTQTY); 
		}
		
		return "0";
	}
	
	/**
	* Used to update list of goods on device display
	* with data from database
	*/
	private void updateList()
	{
		// statement for selecting goods by brand or group
		String whereGroupOrBrand = "";
		
		/** Ticket 28: Get Shared Screen Preferences */
		SharedPreferences screen_prefs = getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE);
		SharedPreferences prefs = getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);
		int intMaxPriceType = prefs.getInt(RsaDb.MAXPRICETYPE, 20);
		// Ticket 28: Modified by ticked. If boolean value pricetype in setting not checked then
		if (screen_prefs.getBoolean(RsaDb.PRICETYPEKEY, false) == false)
		{	
			String strCheckNDS = "";
			String orderString = "";
			try
			{
				/** Get array from res 0 - "Price 1"; 1 - "Price 2" */
				String[] arrayPrices = getResources().getStringArray(R.array.Prices);
				
				String price = screen_prefs.getString(RsaDb.PRICESELECTED, "xxx");
				btnPrice.setText(price);
				currentPrice = price;
				
				// find price index
				int index = 0;
				for (int i=0;i<arrayPrices.length;i++)
				{
					if (arrayPrices[i].equals(price))
					{
						index = i;
						break;
					}
				}
				mContent[5] = "PRICE" + Integer.toString(index+1);
				
				// Get array from res 0 - "No sort"; 1 - "By alphabet"; 2 - by NPP 
				final String[] itemsOrderBy = getResources().getStringArray(R.array.prefOrderBy);
				orderString = itemsOrderBy[0];
				String currentOrderBy =  screen_prefs.getString(RsaDb.ORDERBYKEY, orderString);
				
		        if (currentOrderBy.equals(itemsOrderBy[1]))
		        	orderString = "ORDER BY " + RsaDbHelper.GOODS_NAME;
		        else if (currentOrderBy.equals(itemsOrderBy[2]))
		        	orderString = "ORDER BY " + RsaDbHelper.GOODS_NPP;
		        else if (currentOrderBy.equals(itemsOrderBy[0]))
		        	orderString = "";
		        else
		        {
		        	orderString = "";
		        	screen_prefs.edit().putString(RsaDb.ORDERBYKEY, itemsOrderBy[0]).commit();
		        }
		        
		        
		        // if group selected then select goods by group
		        if (isTOPSKU==true) {
		        	whereGroupOrBrand = RsaDbHelper.GOODS_FLASH + "='1";
		        } else {
			        if (isGroup == true) {
			        	whereGroupOrBrand = RsaDbHelper.GOODS_GROUP_ID + "='" + curGroupID;
			        } else {
			        	// if brand selected then select goods by brand
			        	whereGroupOrBrand = RsaDbHelper.GOODS_BRAND_ID + "='" + curBrandID;
			        }
		        }
				
		        strCheckNDS = "";
		        if (mOrderH.hndsrate.equals("0")||mOrderH.hndsrate.equals(""))
		        {
		        	strCheckNDS = "(" + RsaDbHelper.GOODS_NDS + "<>'0') AND (" 
		        			          + RsaDbHelper.GOODS_NDS + "<>'')";
		        }
		        else
		        {
		        	strCheckNDS = "(" + RsaDbHelper.GOODS_NDS + "='0') OR (" 
	  			          + RsaDbHelper.GOODS_NDS + "='')";
		        }
			}
			catch(Exception e)
			{
				Toast.makeText(getApplicationContext(), "Error: 1> updatelist()\n"
					       								+"2> mContent[5]= " + mContent[5], 5000).show();
			}
			
			// Using PRICE1 column only
			// Get data from database to mCursor by call query:
			// SELECT TABLE_GOODS FROM mContent WHERE (GOODS_GROUP_ID = GROUP_ID) AND (GOODS_NAME LIKE '%string%') 
	        // AND () ORDER BY GOODS_NPP
	        String qq = "<empty>";
	        try
	        {
	        		qq = "(" + whereGroupOrBrand + "') AND ("
							+ RsaDbHelper.GOODS_NAME + " LIKE '%" + filterText.getText()
							+ "%') AND (" + strCheckNDS + ") " + orderString;
	        		
	        		mCursor = db.query(RsaDbHelper.TABLE_GOODS, mContent, 
														qq, 
														null, null, null, null);
	        }
	        catch (Exception e)
	        {
	        	Toast.makeText(getApplicationContext(), "Error: 1> db.query()\n"
	        									       +"2> " + qq, 5000).show();
	        }
		}
		else
		{	
			if (isTOPSKU==true) {
	        	whereGroupOrBrand = RsaDbHelper.GOODS_FLASH + "='1";
	        } else {
		        if (isGroup == true) {
		        	whereGroupOrBrand = RsaDbHelper.GOODS_GROUP_ID + "='" + curGroupID;
		        } else {
		        	// if brand selected then select goods by brand
		        	whereGroupOrBrand = RsaDbHelper.GOODS_BRAND_ID + "='" + curBrandID;
		        }
	        }
	        
			/** Get array from res 0 - "No sort"; 1 - "By alphabet"; 2 - by NPP */
			final String[] itemsOrderBy = getResources().getStringArray(R.array.prefOrderBy);
			String orderString = itemsOrderBy[0];
			String currentOrderBy =  screen_prefs.getString(RsaDb.ORDERBYKEY, orderString);
			
	        	if (currentOrderBy.equals(itemsOrderBy[1]))
	        		orderString = "ORDER BY " + RsaDbHelper.GOODS_NAME;
	        	else if (currentOrderBy.equals(itemsOrderBy[2]))
	        		orderString = "ORDER BY " + RsaDbHelper.GOODS_NPP;
	        	else if (currentOrderBy.equals(itemsOrderBy[0]))
	        		orderString = "";
	        	else
	        	{
	        		orderString = "";
	        		screen_prefs.edit().putString(RsaDb.ORDERBYKEY, itemsOrderBy[0]).commit();
	        	}
			
	        	String strFilter = "";
	        	if (filterText.length()>2) {
	        		strFilter = " AND (" + RsaDbHelper.GOODS_NAME + " LIKE '%" + filterText.getText() + "%')";
	        	}
						
			if (arrayPricetypes.size()==0) {
				arrayPricetypes.add("1");
			}
			mContent[5] = "PRICE" + arrayPricetypes.get(0);
			// if pricetype setting is ON then use price special for client and brand
			/** Ticket 28: Variable for sql query string build */
			StringBuilder myQuery = new StringBuilder();
			//  Ticket 28: Create mega sql-query with 20 prices
			//  Ticket 28: SELECT ID, NAME, PRICE1.....
			//  Ticket 28: FROM _goods
			//  Ticket 28: WHERE (GROUP_ID='601') 
			//  Ticket 28: AND
			//             (NAME LIKE '%filter%')
			//             AND
			//  Ticket 28: (BRAND_ID IN 
			//  Ticket 28: 		(SELECT BRAND_ID
			//  Ticket 28: 		 FROM _char
			//  Ticket 28: 		 WHERE (CUST_ID='402')
			//  Ticket 28: 		 AND
			//  Ticket 28: 		 (PRICE='1')
			//  Ticket 28: 		)
			//  Ticket 28: )
			//  Ticket 28: UNION
			// 	Ticket 28:   .......................
			int _count = arrayPricetypes.size();
			for (int i=0;i<_count;i++)
			{
				myQuery.append("SELECT _id, " + RsaDbHelper.GOODS_ID 	+ ", " + RsaDbHelper.GOODS_GROUP_ID + ", " + RsaDbHelper.GOODS_QTY 		+ ", "
										   + RsaDbHelper.GOODS_NAME + ", " + RsaDbHelper.CHAR_PRICE + arrayPricetypes.get(i) + ", " + RsaDbHelper.GOODS_WEIGHT1 	+ ", "
										   + RsaDbHelper.GOODS_UN 	+ ", " + RsaDbHelper.GOODS_REST 	+ ", " + RsaDbHelper.GOODS_COEFF + ", "  
										   + RsaDbHelper.GOODS_NDS  + ", " + RsaDbHelper.GOODS_NPP + ", " + RsaDbHelper.GOODS_WEIGHT + ", " 
										   + RsaDbHelper.GOODS_FLASH
						+ 	" FROM " + RsaDbHelper.TABLE_GOODS
						+	" WHERE (" + whereGroupOrBrand + "')"
						+	" AND"
						+	" (" + RsaDbHelper.GOODS_BRAND_ID + " IN" 
								 		+	" (SELECT " + RsaDbHelper.CHAR_BRAND_ID
								 		+	" FROM " + RsaDbHelper.TABLE_CHAR
								 		+	" WHERE (" + RsaDbHelper.CHAR_CUST_ID + "='" + mOrderH.cust_id + "')"
								 		+	" AND"
								 		+	" (" + RsaDbHelper.CHAR_PRICE + "='" + arrayPricetypes.get(i) + "')"
								 		+	")"
						 +	")" + strFilter
				);
				if (i<_count-1) myQuery.append(" UNION ");
			}
			
			// Ticket 28: Qwery that calculates price with TABLE_CHAR data
			mCursor = db.rawQuery(myQuery.toString() + orderString, new String[]{});
			btnPrice.setText("");
			btnPrice.setEnabled(false);
		}
		
		try 
		{
			// Init mCursor to work with it
			// Romka 19.12.2012
			if (verOS<3) startManagingCursor(mCursor);
	        
	        // Move to first record in mCursor
	        mCursor.moveToFirst();
	        
	        // Init mAdapter with binding data in mCursor with values on goods.xml
	        mAdapter = new SimpleCursorAdapter(
	        					this, lightTheme?R.layout.l_list_goods:R.layout.list_goods, mCursor, 
	        					mContent, 
	        					new int[]	{	0, 
	        									R.id.txt_rec_goods,   // [1] goods_id but used for REC count forming 
	        									0, 
	        									0, 
	        									R.id.txtName_goods, // [4]
	        									R.id.txtPrice_goods, // [5]
	        									R.id.txtWeight_goods,
	        									R.id.txtUn_goods,
	        									R.id.txtRest_goods, 
	        								    R.id.txtOrderedCount_goods, // [9]
	        								    R.id.txt_rest_goods, // [10] NDS but used for REST count forming
	        								    0,  // [11] NPP
	        								    R.id.txtQ_goods, // [12] WEIGHT used for pack qty - special for lugansk
	        								    0 //[13] flash
	        								});
		}
		catch (Exception e)
		{
			Toast.makeText(getApplicationContext(), "Error: 1> mCursor.moveToFirst()", 5000).show();
		}
        
        mAdapter.setViewBinder(new ViewBinder() 
        {
            public boolean setViewValue(View aView, Cursor aCursor, int aColumnIndex) 
            {
            	if (aColumnIndex == 4) 
                {
	                    TextView textView2 = (TextView) aView;
	                        
	                    fontFlash ff = new fontFlash(Typeface.DEFAULT, lightTheme?Color.BLACK:Color.LTGRAY);
	                    checkForFlash(aCursor.getString(13), ff);
	                        
	                    textView2.setTextColor(ff.getColor());
		                textView2.setTypeface(ff.getTypeface());
                        
                        return false;
                }
                if (aColumnIndex == 5) 
                {
                	    // NDS ADDON
                        float oldPrice = Float.parseFloat(aCursor.getString(aColumnIndex));
                        float NDS = Float.parseFloat(aCursor.getString(10));
                        String newPrice = String.format("%.2f", oldPrice*(1+NDS));
                        TextView textView = (TextView) aView;
                        textView.setText(newPrice.replace(',', '.'));
                        
                        return true;
                }
                
                if (aColumnIndex == 9) 
                {
                        // ORDERED COUNT ADDON
                        TextView textView2 = (TextView) aView;
                        View prnt = (View)(aView.getParent().getParent());
                        
                        myInt mi = new myInt();
                        String qt = findOrderedGoods(aCursor.getString(1), mi);
                        
                        textView2.setText(qt);
                        prnt.setBackgroundColor(mi.get());
                        
                    
                        
                        return true;
                }
                
                if (aColumnIndex == 10) 
                {
                        try
                        {
	                		// ORDERED COUNT ADDON
	                        TextView textView2 = (TextView) aView;
	                        
	                        textView2.setText(findFixedRest(aCursor.getString(1)));
                        }
                        catch(Exception e)
                        {
                        	Toast.makeText(getApplicationContext(), "Error: 1> aColumnIndex == 10", 5000).show();
                        }
                        
                        return true;
                }
                
                if (aColumnIndex == 1) 
                {
                	 	// Calculate and show recommended
                		TextView textView2 = (TextView) aView;
                		
                		if (!showRecomend) {
                			textView2.setText("");
                			return true;
                		} else {
	                		String etap="1";
	                		String curStr="empty";
	                		try
	                		{
	                			curStr = mCursor.getString(1);
		    					tblHistory_REC = mOrderH.prepareHistoryTable(db_orders_REC, curStr);
		    					
		    					etap = "2";
		                		
		                        etap = "3";
		                        textView2.setText(mOrderH.calculateRecomendOrder(curStr, 
		                        									tblHistory_REC, 
		                        									mOrderH.getRestByGoodsIDfromRests(curStr)));
		                        etap = "4";
	                		}
	                		catch(Exception e)
	                        {
	                        	Toast.makeText(getApplicationContext(), "Error: 1> aColumnIndex == 1\n" +
	                        												   "2> etap=" + etap + "\n" +
	                        												   "3> mCursor.getString(1)=" + curStr + "\n" +
	                        												   "4> ", 5000).show();
	                        }
                		}
                        return true;
                }
                
                
                
                return false;
            }
        });
        
        
        try 
        {
	        // Set mAdapter to ListActivty data adapter to show data from mCursor on device display
	        setListAdapter(mAdapter);
	        
	        ListView lv = this.getListView();
			if (cleared) {
				cleared = false;
				goods_ttop = 0;
				goods_iidx = 0;
				
				int c = mCursor.getCount();
				if (c>0) {
					mCursor.moveToFirst();
					for (int i=0;i<c;i++) {
						if (mCursor.getString(1).equals(idGoodsBeforeClear)) { 
							goods_iidx = i;
							break;
						}
						mCursor.moveToNext();
					}
				}
			}
			
			lv.setSelectionFromTop(goods_iidx, goods_ttop);
			
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
			TextView txtTotalSum = (TextView)findViewById(R.id.goods_txtTotalSum_text);
			// Ticket 33: Set xml text view of current order sum with calculated sum
			txtTotalSum.setText(String.format("%.2f",hsumo).replace(',', '.') + " $");
			
			TextView txtGroup = (TextView)findViewById(R.id.goods_txtGroup);
			if (isTOPSKU==true) {
				txtGroup.setText("TOP SKU");
			} else {
				txtGroup.setText(isGroup?curGroupName:curBrandName);
			}
        }
        catch (Exception e)
        {
        	Toast.makeText(getApplicationContext(), "Error: 1> setListAdapter(mAdapter)", 5000).show();
        }
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
	    	        ((CursorAdapter) this.mAdapter).getCursor().close();
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
	    	      
	    	      if (db_orders_REC != null) 
  	    	      {
  	    	        db_orders_REC.close();
  	    	      }
	    	} 
	    	catch (Exception error) 
	    	{
	    	      /** Error Handler Code **/
	    	}
	}
	
	/**
	 * If Back-button on device pressed then do...
	 */
	@Override
	public void onBackPressed()
	{
		/** Used for prepearing data for another activity and send some data to it */
		Intent intent = new Intent();

    	// Put data of current order to intent for using in next activity(GroupActivity.class)
    	intent.putExtra("ORDERH", mOrderH);
    	// Send previous position of listview of groupsactivity
    	intent.putExtra("TOP", ttop);
    	intent.putExtra("INDEX", iidx);
    	
    	// Set result of goods additting is OK and send it's data to GroupActivity.class
    	setResult(RESULT_OK, intent);
    	
    	
    	
    	finish();
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
    	if (db_orders_REC != null) 
	    {
	        db_orders_REC.close();
	    }
    }
	
	/**
	 *  Saving activity positions on destroy .. or if display rotation
	 */
	@Override
	protected void onSaveInstanceState (Bundle outState)
	{
		
		super.onSaveInstanceState(outState);
		
		// put current order
		outState.putParcelable("ORDERH", mOrderH);
		
		// put group id from selected group
		outState.putString(RsaDbHelper.GROUP_ID, curGroupID);
		outState.putString(RsaDbHelper.GROUP_NAME, curGroupName);
		outState.putString(RsaDbHelper.BRAND_ID, curBrandID);
		outState.putString(RsaDbHelper.BRAND_NAME, curBrandName);
		outState.putBoolean("isGroup", isGroup);
		outState.putBoolean("isTOPSKU", isTOPSKU);
		
		outState.putStringArrayList("PR_TYPES", arrayPricetypes);
		
		
		// Send previous position of listview of groupsactivity
    	outState.putInt("TOP", ttop);
    	outState.putInt("INDEX", iidx);
    	
    	// Put current listview position
    	ListView lv = this.getListView();
    	outState.putInt("GINDEX", lv.getFirstVisiblePosition());
    	View mv = lv.getChildAt(0);
    	int top = (mv == null) ? 0 : mv.getTop();
    	outState.putInt("GTOP", top);
    			
		// put selected position
		outState.putInt("POSITION", selectedPosition);
		
		
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
			// set selected position used to save and restore state
			selectedPosition = position;
			
			
			// Put current listview position
	    	ListView lv = this.getListView();
	    	goods_iidx = lv.getFirstVisiblePosition(); 
	    	View mv = lv.getChildAt(0);
	    	goods_ttop = (mv == null) ? 0 : mv.getTop();
			
			// show dialog to set qty
			if (merch_only == false) 
				showDialog(IDD_COUNT);
			else
				showDialog(IDD_HIST);
	}
	
    /**
     * Initializing context menu, that activating by long click
     * @param v View which has been clicked
     * @param pos Item position in ListView
     * @param id id of item
     */
	protected void onLongListItemClick(View v, int pos, long id) 
	{ 
		// set selected position used to save and restore state
		selectedPosition = pos;
		
		// Put current listview position
    	ListView lv = this.getListView();
    	goods_iidx = lv.getFirstVisiblePosition(); 
    	View mv = lv.getChildAt(0);
    	goods_ttop = (mv == null) ? 0 : mv.getTop();
    	
		// show dialog to see history
	    showDialog(IDD_HIST);
	}
	
	/**
	 * Runs when dialog called to show first time
	 * @param id constant identifier of kind of dialog
	 * @return pointer to Dialog that has been created
	 */
	@Override
	protected Dialog onCreateDialog(int id)
	{
		if (selectedPosition >= 0)
		{
			// Move to selected item in query (mCursor)
			mCursor.moveToPosition(selectedPosition);
			
			/** 
			 * If selected item was previously added to current Order, then idx = index of item in list (OrderH.lines) 
			 *  or idx = -1 if it is not
			 */
			idx = mOrderH.isInLines(mCursor.getString(1)); 
			idx_rest = mOrderH.isInRests(mCursor.getString(1));
			
	        // Init temporary variable used if selected item already has been added
	        // with 0, before start to work with it
	        CurrentLine = null;
	        CurrentRestsLine = null;
	        
			// If item was previously added to current Order, then
			// set CurrentLine to line with this previously added item
			if (idx >= 0)           
			{
				CurrentLine = mOrderH.lines.get(idx);											
			}
			if (idx_rest >= 0)           
			{
				CurrentRestsLine = mOrderH.restslines.get(idx_rest);											
			}
			
		}
		
		// Determining the type of dialog to show, in this Activity only one dialog
		switch(id)
		{	
			case IDD_PRICE:
			{
				/** Get array from res 0 - "Price 1"; 1 - "Price 2" */
				final String[] items = getResources().getStringArray(R.array.Prices);
				
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(getResources().getString(R.string.goods_select));
				
				builder.setItems(items, new DialogInterface.OnClickListener() {
				    public void onClick(DialogInterface dialog, int item) 
				    {
				    	SharedPreferences screen_prefs = getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE);
				        
				        screen_prefs.edit().putString(RsaDb.PRICESELECTED, items[item]).commit();
				        updateList();
				    }
				});
				
				return builder.create();
			}
			case IDD_COUNT:
			{
				/** Set my own view of dialog to display to layout variable. Uses dlg_goods.xml */
				LayoutInflater inflater = getLayoutInflater();
				View layout = inflater.inflate(R.layout.ekvator_dlg_goods, 
						(ViewGroup)findViewById(R.id.dlg_goods));	
				
				/** Binding xml elements of my own layout to final variables */ 
				txtName 	= (TextView)layout.findViewById(R.id.txtName_dlgGoods);
				txtId 		= (TextView)layout.findViewById(R.id.txtId_dlgGoods);
				txtPrice 	= (TextView)layout.findViewById(R.id.txtPrice_dlgGoods);
				txtStopPrice = (TextView)layout.findViewById(R.id.txtStopPrice_dlgGoods);
				txtWeight 	= (TextView)layout.findViewById(R.id.txtWeight_dlgGoods);
				txtRest		= (TextView)layout.findViewById(R.id.txtRest_dlgGoods);
				txtCount 	= (TextView)layout.findViewById(R.id.txtCount_dlgGoods);
				txtTotal 	= (TextView)layout.findViewById(R.id.txtTotal_dlgGoods);
				TextView txtCurrency 	= (TextView)layout.findViewById(R.id.txtCurrency_dlgGoods);
				edtQty 		= (EditText)layout.findViewById(R.id.edtQty);
				edtCustPrice	= (EditText)layout.findViewById(R.id.edtCustomPrice);
				chkPack		= (CheckBox)layout.findViewById(R.id.chkPack);
				
				txtCurrency.setText(currency);
				// Sets GOODS_NAME of selected item to special element on dialog view 
				txtName.setText(mCursor.getString(4));
				// Sets GOODS_ID of selected item to special element on dialog view
				txtId.setText(getResources().getString(R.string.goods_code) 
						+ mCursor.getString(1));
				// Sets GOODS_PRICE1 of selected item to special element on dialog view
				float pr = Float.parseFloat(mCursor.getString(5));
				float nd = Float.parseFloat(mCursor.getString(10)) + 1F;
				float tpr = pr * nd;
				txtPrice.setText(getResources().getString(R.string.goods_writeprice) 
						+ String.format("%.2f", tpr).replace(',', '.') 
						+ currency);
				
				float s_pr = 0;
				try {
					s_pr = Float.parseFloat(mCursor.getString(14));
				} catch (Exception f) {
					s_pr = 0;
				}
				txtStopPrice.setText(String.format("%.2f", s_pr).replace(',', '.'));
				edtCustPrice.setText(String.format("%.2f", tpr).replace(',', '.'));
				
				// Sets GOODS_WEIGHT1 of selected item to special element on dialog view
				txtWeight.setText(getResources().getString(R.string.goods_writeweight) 
						+ mCursor.getString(6) 
						+ getResources().getString(R.string.goods_weight_curency));
				// Sets GOODS_REST of selected item to special element on dialog view
				txtRest.setText(getResources().getString(R.string.goods_writerest) 
						+ mCursor.getString(8) 
						+ getResources().getString(R.string.goods_qty));
				// Sets maximum value of selected item = GOODS_REST of selected item
				// maxQty = (int)Float.parseFloat(mCursor.getString(8));
				
				// If selected item was NOT previously added to current Order, then
				// set special elements on dialog view to 0 (QTY, SUM)
				if (idx < 0)
				{
					txtCount.setText("0");
					txtTotal.setText("0.00");
					edtQty.setText("1");
					////
							long tmpQTY=0;
							mCursor.moveToPosition(selectedPosition);
							tmpQTY = 1;
							txtCount.setText("1");	
							// Set special element SUM on dialog view same as edtQty
							// SUM = QTY * GOODS_COEFF * GOODS_PRICE1
							float pr2 = Float.parseFloat(mCursor.getString(5));
							float nd2 = Float.parseFloat(mCursor.getString(10)) + 1;
							float tpr2 = pr2 * nd;
							txtTotal.setText(String.format("%.2f", Float.parseFloat(mCursor.getString(9)) * tpr2 ).replace(',', '.'));
					////
							
					SharedPreferences screen_prefs = PreferenceManager.getDefaultSharedPreferences(this);
					if (screen_prefs.getBoolean(RsaDb.USEPACKS, false) == true) {
						chkPack.setChecked(true);
					} else {
						chkPack.setChecked(false);
					}
							
				}
				else
				{
					// But if selected item was previously added to current Order, then
					// set special elements on dialog view same as in previous time (QTY, SUM)
					String un = CurrentLine.get(OrderLines.UN);
					edtCustPrice.setText(CurrentLine.get(OrderLines.PRICEWNDS));
					if (un.equals("qq")) {
						chkPack.setChecked(true);
						long tmpQTY=0;
						mCursor.moveToPosition(selectedPosition);
						
						// If EditText = "" then QTY = 0
						if (edtQty.getText().toString().equals("")) {
							tmpQTY = 0;
						} else {
							tmpQTY = Long.parseLong(edtQty.getText().toString());
						}
						int qt = 1;
						if (chkPack.isChecked() == true) {
							try {
								qt = Integer.parseInt(mCursor.getString(3));
							} catch (Exception e) {
								qt = 1;
							}
						}
						txtCount.setText(Long.toString(tmpQTY*qt));
						String strPr = edtCustPrice.getText().toString();
						tpr = 0;
						try {
							tpr = (strPr.length()>0)?Float.parseFloat(strPr):0;
						} catch (Exception d) {
							tpr = 0;
						}
						txtTotal.setText(String.format("%.2f", qt*tmpQTY * Float.parseFloat(mCursor.getString(9)) * tpr ).replace(',', '.'));
						
					} else {
						chkPack.setChecked(false);
					}
					
					txtCount.setText(CurrentLine.get(OrderLines.QTY));
					txtTotal.setText(CurrentLine.get(OrderLines.SUMWNDS));
					edtQty.setText(CurrentLine.get(OrderLines.QTY));
				}
				
				/** Listener for trying to change QTY of selected item in Order */
				edtQty.addTextChangedListener(new TextWatcher(){
					@Override
					public void afterTextChanged(Editable arg0)
					{
						// Move Cursor to selected position before save data
						mCursor.moveToPosition(selectedPosition);

						String count = txtCount.getText().toString();
						if ((!count.equals(edtQty.getText().toString())) && (!chkPack.isChecked())) {
							edtQty.setText(String.format("%.2f",count));
							int length = edtQty.getText().toString().length();
							edtQty.setSelection(length, length);
						}
					}

					@Override
					public void beforeTextChanged(CharSequence arg0, int arg1,
							int arg2, int arg3)
					{
						// do nothing
					}

					@Override
					public void onTextChanged(CharSequence arg0, int arg1,
							int arg2, int arg3)
					{
						long tmpQTY=0;
						mCursor.moveToPosition(selectedPosition);
						
						// If EditText = "" then QTY = 0
						if (edtQty.getText().toString().equals("")) {
							tmpQTY = 0;
						} else {
							tmpQTY = Long.parseLong(edtQty.getText().toString());
						}
						int qt = 1;
						if (chkPack.isChecked() == true) {
							try {
								qt = Integer.parseInt(mCursor.getString(3));
							} catch (Exception e) {
								qt = 1;
							}
						}
						txtCount.setText(Long.toString(tmpQTY*qt));
						String strPr = edtCustPrice.getText().toString();
						float tpr = 0;
						try {
							tpr = (strPr.length()>0)?Float.parseFloat(strPr):0;
						} catch (Exception d) {
							tpr = 0;
						}
						txtTotal.setText(String.format("%.2f", qt*tmpQTY * Float.parseFloat(mCursor.getString(9)) * tpr ).replace(',', '.'));
				}});
				
				edtCustPrice.addTextChangedListener(new TextWatcher() {
					@Override
					public void onTextChanged(CharSequence s, int start, int before, int count) {
						// TODO Auto-generated method stub
						
					}
					@Override
					public void beforeTextChanged(CharSequence s, int start, int count,
							int after) {
						// TODO Auto-generated method stub
						
					}
					@Override
					public void afterTextChanged(Editable s) {
						long tmpQTY=0;
						mCursor.moveToPosition(selectedPosition);
						
						// If EditText = "" then QTY = 0
						if (edtQty.getText().toString().equals("")) {
							tmpQTY = 0;
						} else {
							tmpQTY = Long.parseLong(edtQty.getText().toString());
						}
						int qt = 1;
						if (chkPack.isChecked() == true) {
							try {
								qt = Integer.parseInt(mCursor.getString(3));
							} catch (Exception e) {
								qt = 1;
							}
						}
						txtCount.setText(Long.toString(tmpQTY*qt));
						String strPr = edtCustPrice.getText().toString();
						float tpr = 0;
						try {
							tpr = (strPr.length()>0)?Float.parseFloat(strPr):0;
						} catch (Exception d) {
							tpr = 0;
						}
						txtTotal.setText(String.format("%.2f", qt*tmpQTY * Float.parseFloat(mCursor.getString(9)) * tpr ).replace(',', '.'));
					}
				});
				
				edtQty.setOnLongClickListener(new OnLongClickListener() {
					@Override
					public boolean onLongClick(View arg0)
					{
						// make return tu TRUE for select all operation 
						return true;
					}});
				
				edtQty.setOnTouchListener(new OnTouchListener(){
					@Override
					public boolean onTouch(View arg0, MotionEvent arg1)
					{
						edtQty.setSelection(0, edtQty.getText().toString().length());
						return false;
					}});
				
				chkPack.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						long tmpQTY=0;
						mCursor.moveToPosition(selectedPosition);
						
						// If EditText = "" then QTY = 0
						if (edtQty.getText().toString().equals("")) {
							tmpQTY = 0;
						} else {
							tmpQTY = Long.parseLong(edtQty.getText().toString());
						}
						int qt = 1;
						if (chkPack.isChecked() == true) {
							try {
								qt = Integer.parseInt(mCursor.getString(3));
							} catch (Exception e) {
								qt = 1;
							}
						}
						txtCount.setText(Long.toString(tmpQTY*qt));
						String strPr = edtCustPrice.getText().toString();
						float tpr = 0;
						try {
							tpr = (strPr.length()>0)?Float.parseFloat(strPr):0;
						} catch (Exception d) {
							tpr = 0;
						}
						txtTotal.setText(String.format("%.2f", qt*tmpQTY * Float.parseFloat(mCursor.getString(9)) * tpr ).replace(',', '.'));
					}
				});
				
				/** Building dialog view with my own xml view */
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setView(layout);
				//builder.setMessage(R.string.goods_count);
				
				/** 
				 * Listener for OK button, that makes order of selected item with 
				 * user-defined QTY and SUM. All data saving to special class holder
				 * mOrderH
				 */
				builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						
						// Move Cursor to selected position before save data
						mCursor.moveToPosition(selectedPosition);
						
						float stopP = 0;
						try {
							stopP = Float.parseFloat(txtStopPrice.getText().toString());
						} catch(Exception g) {
							stopP = 0;
						}
						
						// If such goods are not ordered earlier and user wants to
						// order now not 0 of it...
						if ((idx<0)&&(!edtQty.getText().toString().equals(""))&&(!edtQty.getText().toString().equals("0")))   
						{
							String un = mCursor.getString(7);
							int qt = 1;
							if (chkPack.isChecked()==true) {
								try {
									qt = Integer.parseInt(mCursor.getString(3));
								} catch (Exception e) {
									qt = 1;
								}
								qt = qt * Integer.parseInt(edtQty.getText().toString());
								un = "qq";
							} else {
								qt = Integer.parseInt(edtQty.getText().toString());
							}
							
							String strPr = edtCustPrice.getText().toString();
							float tpr = 0;
							try {
								tpr = (strPr.length()>0)?Float.parseFloat(strPr):0;
							} catch (Exception d) {
								tpr = 0;
							}
							if (tpr<stopP) {
								tpr = stopP;
							}
							float tmpSumwnds = qt * Float.parseFloat(mCursor.getString(9)) * tpr;
							float nd = tmpSumwnds/6;
							float prnd = tpr/6;
							// Adding selected item to order
							mOrderH.lines.add(new OrderLines(				"0", 											// *id used for REST QTY in Shop (Merchandising)
																			"",												// *zakaz_id used in headactivity
																			mCursor.getString(1),							// goods_id
																			mCursor.getString(4), 							// text_goods
																			currentPrice,    								// PRICETYPE (Price1,Price2...20)
																			Integer.toString(qt),							// qty
																			un,												// un
																			mCursor.getString(9),							// coeff
																			"0",											// *discount !not used
																			String.format("%.2f",tpr).replace(',', '.'),	// pricewnds 
																			String.format("%.2f",tmpSumwnds).replace(',', '.'),	// sumwnds
																			String.format("%.2f",tpr-prnd).replace(',', '.'), 	// pricewonds=pricewnds/1.2	
																			String.format("%.2f",tmpSumwnds-nd).replace(',', '.'),	// sumwonds=sumwnds/1.2
																			String.format("%.2f",nd).replace(',', '.'),	// nds=sumwnds-sumwonds
																			mCursor.getString(10),							// must be delay, but overloaded to NDS flag
																			mCursor.getString(6), "", "brand"));						//weight
							///////////////////////////
							// Add current shop REST of this good if not added yet (TABLE_RESTS)
								try
								{
									if (mOrderH.isInRests(mCursor.getString(1)) < 0)
									{
										String[][] tblH = mOrderH.prepareHistoryTable(db_orders_REC, mCursor.getString(1));
										
										// recomended qty
										String rQty = mOrderH.calculateRecomendOrder( mCursor.getString(1), 
																						tblH, "0");
										
										// Adding selected item to restslist
										mOrderH.restslines.add(new OrderRests(			"0", 											// *id not used
																						"",												// *zakaz_id used in headactivity
																						mCursor.getString(1),							// goods_id
																						"0",											// restqty
																						rQty,											// recqty
																						edtQty.getText().toString()));					// qty
									}
								}
								catch(Exception e)
								{
									Toast.makeText(getApplicationContext(), "Error 1> via inserting rest", Toast.LENGTH_SHORT).show();
								}
							////////////////////////////							
						}
						// If such goods are ordered earlier and user wants to
						// change QTY of it ...
						else if ((idx>=0)&&(!edtQty.getText().toString().equals(""))&&(!edtQty.getText().toString().equals("0")))
						{	
							int qt = 1;
							if (chkPack.isChecked()==true) {
								CurrentLine.put(OrderLines.UN, 	"qq");
								try {
									qt = Integer.parseInt(mCursor.getString(3));
								} catch (Exception e) {
									qt = 1;
								}
							} else {
								CurrentLine.put(OrderLines.UN, 	mCursor.getString(7));
							}
							qt = qt * Integer.parseInt(edtQty.getText().toString());
							
							String strPr = edtCustPrice.getText().toString();
							float tpr = 0;
							try {
								tpr = (strPr.length()>0)?Float.parseFloat(strPr):0;
							} catch (Exception d) {
								tpr = 0;
							}
							if (tpr<stopP) {
								tpr = stopP;
							}
							float tmpSumwnds = qt * Float.parseFloat(mCursor.getString(9)) * tpr;
							float nd = tmpSumwnds/6;
							float prnd = tpr/6;
							
							
							CurrentLine.put(OrderLines.QTY, 		Integer.toString(qt));
							CurrentLine.put(OrderLines.SUMWNDS,		String.format("%.2f",tmpSumwnds).replace(',', '.'));
							CurrentLine.put(OrderLines.SUMWONDS,	String.format("%.2f",tmpSumwnds - nd).replace(',', '.'));
							CurrentLine.put(OrderLines.PRICEWNDS,	String.format("%.2f",tpr).replace(',', '.'));
							CurrentLine.put(OrderLines.PRICEWONDS,	String.format("%.2f",tpr - prnd).replace(',', '.'));
							CurrentLine.put(OrderLines.NDS,			String.format("%.2f",nd).replace(',', '.'));
						} 
						// But if user choose QTY = 0 then show pop-up message that it is forbiden to order 0 QTY
						else
						{
							Toast.makeText(getApplicationContext(), R.string.goods_qty_notcorrect, 5000).show();
						}
						
						// Ticket 2: Hide soft keyboard because Button OK was pressed
				        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				        // Ticket 2: Hide soft keyboard because Button OK was pressed
				        imm.hideSoftInputFromWindow(edtQty.getWindowToken(), 0);
				        // Update info in ListView and Total Sum in HeadPanel
				        updateList();
					}
				});
				
				/** Listener for Cancel button, that means do nothing and just close the dialog */
				builder.setNegativeButton(R.string.goods_cancel, new DialogInterface.OnClickListener() {					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// Ticket 2: Hide soft keyboard because Button CANCEL was pressed
				        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				        // Ticket 2: Hide soft keyboard because Button CANCEL was pressed
				        imm.hideSoftInputFromWindow(edtQty.getWindowToken(), 0);
					}
				});
				
				/** Listener for Delete button, deletes selected item from special class holder RSAAcitivy.OrderH */				
				builder.setNeutralButton(R.string.goods_delete, new DialogInterface.OnClickListener() {					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// Remove selected line from class holder mOrderH
						mOrderH.lines.remove(CurrentLine);
						
						// Refresh displayed goods after removal selected item
						updateList();
						
						// Ticket 2: Hide soft keyboard because Button CANCEL was pressed
				        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				        // Ticket 2: Hide soft keyboard because Button CANCEL was pressed
				        imm.hideSoftInputFromWindow(edtQty.getWindowToken(), 0);
					}
				});
				
				// Sets Dialog is not cancelable by pressing Back-button on device
				builder.setCancelable(false);
				
				// return with dialog creation
				Dialog md = builder.create();
				md.requestWindowFeature(Window.FEATURE_NO_TITLE);
				return md;
			}
			case IDD_HIST:
			{
				/** Set my own view of dialog to display to layout variable. Uses dlg_goods.xml */
				LayoutInflater inflater = getLayoutInflater();
				View layout = inflater.inflate(R.layout.dlg_goods_history, 
						(ViewGroup)findViewById(R.id.dlg_goods));
				
				// Bind xml elements
				edtRest = (EditText)layout.findViewById(R.id.edtRec);
				curRecomend = (TextView)layout.findViewById(R.id.txt_dlg_hist_recomend);
				
				
				/** Listener for trying to change QTY of selected item in Order */
				edtRest.addTextChangedListener(new TextWatcher(){
					@Override
					public void afterTextChanged(Editable arg0)
					{
						
					}
	
					@Override
					public void beforeTextChanged(CharSequence arg0, int arg1,
								int arg2, int arg3)
					{
							// Do nothing
					}
	
					@Override
					public void onTextChanged(CharSequence arg0, int arg1,
								int arg2, int arg3)
					{
						
						
					}
				});
				
				edtRest.setOnLongClickListener(new OnLongClickListener() {
							@Override
							public boolean onLongClick(View arg0)
							{
								// Make return TRUE to perform select all operation
								return true;
							}});
						
						edtRest.setOnTouchListener(new OnTouchListener(){
						@Override
						public boolean onTouch(View arg0, MotionEvent arg1)
						{
							edtRest.setSelection(0, edtRest.getText().toString().length());
							return false;
						}
				});
				
				/** Building dialog view with my own xml view */
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setView(layout);
				//builder.setMessage(R.string.goods_hist);
				  
				/** 
				 * Listener for OK button in dialog (just close dialog) 
				 */
				builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {					
					@Override
					public void onClick(DialogInterface dialog, int which) 
					{
						mCursor.moveToPosition(selectedPosition);
						// if not ordered earlier and != ""
						
						if (edtRest.getText().toString().equals(""))
						{
							edtRest.setText("0");
						}
						else
						{
							long j = Long.parseLong(edtRest.getText().toString());
							edtRest.setText(Long.toString(j));
						}
						
						if (idx_rest<0)   
						{
							// Adding selected item to restslist
							mOrderH.restslines.add(new OrderRests(			"0", 											// *id not used
																			"",												// *zakaz_id used in headactivity
																			mCursor.getString(1),							// goods_id
																			edtRest.getText().toString(),					// restqty
																			curRecomend.getText().toString(),				// recqty
																			"0"));											// qty
						
						} // if ordered earlier (so edtRest not empty)
						else 
						{
							// Just change REST of previously countered item
							CurrentRestsLine.put(OrderRests.RESTQTY, edtRest.getText().toString());
							CurrentRestsLine.put(OrderRests.RECQTY,  curRecomend.getText().toString());
						}
						
						////////////////
						//if Merch ONLY ACTIVATED
						if (merch_only == true) {
							if (idx<0) {
								mOrderH.lines.add(new OrderLines(		"0", 											// *id used for REST QTY in Shop (Merchandising)
																		"",												// *zakaz_id used in headactivity
																		mCursor.getString(1),							// goods_id
																		mCursor.getString(4), 							// text_goods
																		edtRest.getText().toString(),					// restcust
																		edtRest.getText().toString(),											// qty
																		mCursor.getString(7),							// un
																		mCursor.getString(9),							// coeff
																		"0",											// *discount !not used
																		"0.00",											// pricewnds 
																		"0.00",											// sumwnds
																		"0.00", 										// pricewonds=pricewnds/1.2	
																		"0.00",											// sumwonds=sumwnds/1.2
																		"0.00",											// nds=sumwnds-sumwonds
																		"0.0",										// must be delay, but overloaded to NDS flag
																		"0", "", "brand"));
							} else {
								CurrentLine.put(OrderLines.RESTCUST, 	edtRest.getText().toString());
								CurrentLine.put(OrderLines.QTY, 	edtRest.getText().toString());
							}
						}
						//End of Merch addon
						////////////////
						updateList();
						
						// Ticket 2: Hide soft keyboard because Button CANCEL was pressed
				        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				        // Ticket 2: Hide soft keyboard because Button CANCEL was pressed
				        imm.hideSoftInputFromWindow(edtRest.getWindowToken(), 0);
					}
				});
				
				// Sets Dialog cancelable by pressing Back-button on device
				builder.setCancelable(false);
				
				// return with dialog creation
				Dialog md = builder.create();
				md.requestWindowFeature(Window.FEATURE_NO_TITLE);
				return md;
			}
			default:
				// Do nothing if another kind of dialog is selected 
				return null;
		}
		
	}

	/**
	 * Runs everytime when Dialog called to show()
	 * @param id constant identifier of kind of dialog
	 * @param dialog pointer to Dialog object 
	 */
	@Override
	protected void onPrepareDialog(int id, Dialog dialog)
	{
		if (selectedPosition >= 0)
		{
			// Move to selected item in query (mCursor)
			mCursor.moveToPosition(selectedPosition);
			
			// 
			// If selected item was previously added to current Order, then idx = index of item in list (OrderH.lines) 
			// or idx = -1 if it is not
			//
			idx = mOrderH.isInLines(mCursor.getString(1)); 
			idx_rest = mOrderH.isInRests(mCursor.getString(1));
			
	        // Init temporary variable used if selected item already has been added
	        // with 0, before start to work with it
	        CurrentLine = null;
	        CurrentRestsLine = null;
	        
			// If item was previously added to current Order, then
			// set CurrentLine to line with this previously added item
			if (idx >= 0)           
			{
				CurrentLine = mOrderH.lines.get(idx);											
			}
			
			// If item was previously added to current Order, then
			// set CurrentLine to line with this previously added item
			if (idx_rest >= 0)           
			{
				CurrentRestsLine = mOrderH.restslines.get(idx_rest);											
			}
		}
		
		// Determining the type of dialog to show, in this Activity only one dialog
		switch(id)
		{	
			case IDD_COUNT:
			{
				/** Binding xml elements of my own layout to final variables */
				txtName 	= (TextView)dialog.findViewById(R.id.txtName_dlgGoods);
				txtId 		= (TextView)dialog.findViewById(R.id.txtId_dlgGoods);
				txtPrice 	= (TextView)dialog.findViewById(R.id.txtPrice_dlgGoods);
				txtStopPrice = (TextView)dialog.findViewById(R.id.txtStopPrice_dlgGoods);
				txtWeight 	= (TextView)dialog.findViewById(R.id.txtWeight_dlgGoods);
				txtRest 	= (TextView)dialog.findViewById(R.id.txtRest_dlgGoods);
				txtCount 	= (TextView)dialog.findViewById(R.id.txtCount_dlgGoods);
				txtTotal 	= (TextView)dialog.findViewById(R.id.txtTotal_dlgGoods);
				edtQty 		= (EditText)dialog.findViewById(R.id.edtQty);
				edtCustPrice	= (EditText)dialog.findViewById(R.id.edtCustomPrice);
				chkPack 	= (CheckBox)dialog.findViewById(R.id.chkPack);
				
				dialog.setOnShowListener(new OnShowListener() {
				    @Override
				    public void onShow(DialogInterface dialog) {
				        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				       // Ticket 2: 
				        imm.showSoftInput(edtQty, InputMethodManager.SHOW_FORCED);
				        
				       // Ticket 2: Removed:
				       // imm.showSoftInput(edtQty, InputMethodManager.SHOW_IMPLICIT);
				       // imm.toggleSoftInput(0, 1);
				        edtQty.setSelection(0, edtQty.getText().toString().length()); 
				    }
				});
				
				// Sets GOODS_NAME of selected item to special element on dialog view
				String tmpStr = mCursor.getString(4);
				if (tmpStr.length()>35)
					txtName.setText(tmpStr.substring(0, 35));
				else
					txtName.setText(tmpStr);
				
				// Sets GOODS_ID of selected item to special element on dialog view
				txtId.setText(getResources().getString(R.string.goods_code) 
						+ mCursor.getString(1));
				// Sets GOODS_PRICE1 of selected item to special element on dialog view
				float pr = Float.parseFloat(mCursor.getString(5));
				float nd = Float.parseFloat(mCursor.getString(10)) + 1;
				float tpr = pr * nd;
				txtPrice.setText(getResources().getString(R.string.goods_writeprice) 
						+ String.format("%.2f", tpr).replace(',', '.') 
						+ currency);
				
				float s_pr = 0;
				try {
					s_pr = Float.parseFloat(mCursor.getString(14));
				} catch (Exception f) {
					s_pr = 0;
				}
				txtStopPrice.setText(String.format("%.2f", s_pr).replace(',', '.'));
				edtCustPrice.setText(String.format("%.2f", tpr).replace(',', '.'));
				
				// Sets GOODS_WEIGHT1 of selected item to special element on dialog view
				txtWeight.setText(getResources().getString(R.string.goods_writeweight) 
						+ mCursor.getString(6) 
						+ getResources().getString(R.string.goods_weight_curency));
				// Sets GOODS_REST of selected item to special element on dialog view
				txtRest.setText(getResources().getString(R.string.goods_writerest) 
						+ mCursor.getString(8) 
						+ getResources().getString(R.string.goods_qty));			
				// set maximum value of selected item
				// maxQty = (int)Float.parseFloat(mCursor.getString(8));
						
				
				// If selected item was NOT previously added to current Order, then
				// set special elements on dialog view to 0 (QTY, SUM)
				if (idx < 0)
				{
					txtCount.setText("0");
					txtTotal.setText("0.00");
					edtQty.setText("1");
					////
							long tmpQTY=0;
							mCursor.moveToPosition(selectedPosition);
							tmpQTY = 1;
							txtCount.setText("1");	
							// Set special element SUM on dialog view same as edtQty
							// SUM = QTY * GOODS_COEFF * GOODS_PRICE1
							String strPr = edtCustPrice.getText().toString();
							float tpr2 = 0;
							try {
								tpr2 = (strPr.length()>0)?Float.parseFloat(strPr):0;
							} catch (Exception d) {
								tpr2 = 0;
							}
							txtTotal.setText(String.format("%.2f", Float.parseFloat(mCursor.getString(9)) * tpr2 ).replace(',', '.'));
					////
							
					SharedPreferences screen_prefs = PreferenceManager.getDefaultSharedPreferences(this);
					if (screen_prefs.getBoolean(RsaDb.USEPACKS, false) == true) {
						chkPack.setChecked(true);
						tmpQTY=0;
						mCursor.moveToPosition(selectedPosition);
						
						// If EditText = "" then QTY = 0
						if (edtQty.getText().toString().equals("")) {
							tmpQTY = 0;
						} else {
							tmpQTY = Long.parseLong(edtQty.getText().toString());
						}
						int qt = 1;
						if (chkPack.isChecked() == true) {
							try {
								qt = Integer.parseInt(mCursor.getString(3));
							} catch (Exception e) {
								qt = 1;
							}
						}
						txtCount.setText(Long.toString(tmpQTY*qt));
						strPr = edtCustPrice.getText().toString();
						tpr = 0;
						try {
							tpr = (strPr.length()>0)?Float.parseFloat(strPr):0;
						} catch (Exception d) {
							tpr = 0;
						}
						txtTotal.setText(String.format("%.2f", qt*tmpQTY * Float.parseFloat(mCursor.getString(9)) * tpr ).replace(',', '.'));
					} else {
						chkPack.setChecked(false);
					}
				}
				// But if selected item was previously added to current Order, then
				// set special elements on dialog view same as in previous time (QTY, SUM)
				else
				{
					String un = CurrentLine.get(OrderLines.UN);
					edtCustPrice.setText(CurrentLine.get(OrderLines.PRICEWNDS));
					int qt = 1;
					if (un.equals("qq")) {
						chkPack.setChecked(true);
						try {
							qt = Integer.parseInt(mCursor.getString(3));
						} catch (Exception e) {
							qt = 1;
						}
					} else {
						chkPack.setChecked(false);
					}
					
					qt = Integer.parseInt(CurrentLine.get(OrderLines.QTY)) / qt;
					
					txtCount.setText(CurrentLine.get(OrderLines.QTY));
					txtTotal.setText(CurrentLine.get(OrderLines.SUMWNDS));
					edtQty.setText(Integer.toString(qt));
					
				}		
				break;
			}
			case IDD_HIST:
			{
				// Bind xml elements
				edtRest = (EditText)dialog.findViewById(R.id.edtRec);
				curRecomend = (TextView)dialog.findViewById(R.id.txt_dlg_hist_recomend);
				txtHistoryDate1 = (TextView)dialog.findViewById(R.id.txt_dlg_hist1_date);
				txtHistoryDate2 = (TextView)dialog.findViewById(R.id.txt_dlg_hist2_date);
				txtHistoryDate3 = (TextView)dialog.findViewById(R.id.txt_dlg_hist3_date);
				txtHistoryQTY1 = (TextView)dialog.findViewById(R.id.txt_dlg_hist1_ord);
				txtHistoryQTY2 = (TextView)dialog.findViewById(R.id.txt_dlg_hist2_ord);
				txtHistoryQTY3 = (TextView)dialog.findViewById(R.id.txt_dlg_hist3_ord);
				txtHistoryRec1 = (TextView)dialog.findViewById(R.id.txt_dlg_hist1_rec);
				txtHistoryRec2 = (TextView)dialog.findViewById(R.id.txt_dlg_hist2_rec);
				txtHistoryRec3 = (TextView)dialog.findViewById(R.id.txt_dlg_hist3_rec);
				txtHistoryRest1 = (TextView)dialog.findViewById(R.id.txt_dlg_hist1_rest);
				txtHistoryRest2 = (TextView)dialog.findViewById(R.id.txt_dlg_hist2_rest);
				txtHistoryRest3 = (TextView)dialog.findViewById(R.id.txt_dlg_hist3_rest);
				
				/** SQLite database that stores orders data */
				SQLiteDatabase db_orders;
				/** Init database with orders  */
				RsaDbHelper mDb = new RsaDbHelper(this, RsaDbHelper.DB_ORDERS);
				// Open database 
				db_orders = mDb.getReadableDatabase();
				/** Array of columns that will be used to obtain data from DB-tables in columns with the same name */
				
				tblHistory = new String[4][3];
				tblHistory = mOrderH.prepareHistoryTable(db_orders, mCursor.getString(1));
				
				dialog.setOnShowListener(new OnShowListener() {
				    @Override
				    public void onShow(DialogInterface dialog) {
				        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				       // Ticket 2: 
				        imm.showSoftInput(edtRest, InputMethodManager.SHOW_FORCED);
				        
				       // Ticket 2: Removed:
				       // imm.showSoftInput(edtQty, InputMethodManager.SHOW_IMPLICIT);
				       // imm.toggleSoftInput(0, 1);
				        edtRest.setSelection(0, edtRest.getText().toString().length()); 
				    }
				});
				
				// Listener for trying to change QTY of selected item in Order 
				edtRest.addTextChangedListener(new TextWatcher(){
					@Override
					public void afterTextChanged(Editable arg0)
					{
						edtRest.setSelection(edtRest.getText().toString().length(), edtRest.getText().toString().length());
					}

					@Override
					public void beforeTextChanged(CharSequence arg0, int arg1,
							int arg2, int arg3)
					{
						// do nothing
					}

					@Override
					public void onTextChanged(CharSequence arg0, int arg1,
							int arg2, int arg3)
					{
						if (!edtRest.getText().toString().equals(""))
						{
							curRecomend.setText(mOrderH.calculateRecomendOrder(mCursor.getString(1), tblHistory, edtRest.getText().toString()));
						}
						else
						{
							curRecomend.setText(mOrderH.calculateRecomendOrder(mCursor.getString(1), tblHistory, "0"));
						}
					}				
				});
				
				if (idx_rest < 0)
				{
					edtRest.setText("0");
				}
				else
				{
					edtRest.setText(CurrentRestsLine.get(OrderRests.RESTQTY));
				}
				
				
				//tblHistory = mOrderH.prepareHistoryTable(db_orders, mCursor.getString(1));
				curRecomend.setText(mOrderH.calculateRecomendOrder(mCursor.getString(1), tblHistory, edtRest.getText().toString()));
				
				
				txtHistoryDate1.setText(tblHistory[0][0]);
				txtHistoryDate2.setText(tblHistory[0][1]);
				txtHistoryDate3.setText(tblHistory[0][2]);
				
				txtHistoryQTY1.setText(tblHistory[1][0]);
				txtHistoryQTY2.setText(tblHistory[1][1]);
				txtHistoryQTY3.setText(tblHistory[1][2]);

				txtHistoryRest1.setText(tblHistory[2][0]);
				txtHistoryRest2.setText(tblHistory[2][1]);
				txtHistoryRest3.setText(tblHistory[2][2]);
				
				txtHistoryRec1.setText(tblHistory[3][0]);
				txtHistoryRec2.setText(tblHistory[3][1]);
				txtHistoryRec3.setText(tblHistory[3][2]);
				
	    	    // Not needed
	    	    if (db_orders != null) 
	    	    {
	    	        db_orders.close();
	    	    }
				
				break;	
			}
			default:
				// Do nothing if another kind of dialog is selected
		}
		//if (isPad==false) {
		//	dialog.getWindow().setGravity(Gravity.TOP);
		//} else {
			dialog.getWindow().setGravity(Gravity.BOTTOM);
		//}
		
	}

}

