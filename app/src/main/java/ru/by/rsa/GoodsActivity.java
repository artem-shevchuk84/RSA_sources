package ru.by.rsa;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

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
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
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
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.SimpleCursorAdapter.ViewBinder;

import ru.by.rsa.utils.DataUtils;

/**
 * Activity that allows user to view, add and change quantity of goods
 * to current order
 * @author Komarev Roman
 * Odessa, neo3da@mail.ru, +380503412392
 */
public class GoodsActivity extends ListActivity
{
	public static final int IDM_ZERO_STOCKS			= 100;
	public static final int IDM_ZERO_STOCKS_SHOW	= 101;
	/** Final variables used in dialog */
	TextView txtName = null;
	TextView txtId = null;
	TextView txtPrice = null;
	TextView txtDiscount = null;
	TextView txtPriceWithDiscount = null;
	TextView txtWeight = null;
	TextView txtRest = null;
	TextView txtCount = null;
	TextView txtTotal = null;
	EditText edtQty = null;
	EditText edtPrc = null;
	EditText edtSum = null;
	EditText edtRest = null;
	TextView txtComment = null;
	CheckBox chkPack = null;
	CheckBox chkPrc = null;
	RelativeLayout rltPrc = null;
	Button   btnOptions = null;
	Button   btnPrice = null;
	Button   btnClear = null;
	String[][] tblHistory;
	String[][] tblHistory_REC = new String[4][3];
	Context ctx = null;
	private String goods_comments = "";

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
	int groupPos;
	/** Current position in ListView */
	int goods_iidx;
	int goods_ttop;

	private static boolean showCustomPrice = false;

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

	private static float customPrice = 0;
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
	private String strGlobalSearch;
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
											RsaDbHelper.GOODS_FLASH, 	// [13]
											RsaDbHelper.GOODS_BRAND_ID, // [14]
											RsaDbHelper.GOODS_DISCOUNT }; // [15]

	SharedPreferences merch_prefs;
	boolean merch_only;

	/** Current theme */
	private boolean lightTheme;
	private boolean cleared = false;
	private String idGoodsBeforeClear;
	private boolean mIsVim;
	private boolean mShowZeroStocks = true;

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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, IDM_ZERO_STOCKS_SHOW, Menu.NONE, "ПОКАЗАТЬ НУЛЕВЫЕ ОСТАТКИ")
				.setAlphabeticShortcut('s');
		menu.add(Menu.NONE, IDM_ZERO_STOCKS, Menu.NONE, "СКРЫТЬ НУЛЕВЫЕ ОСТАТКИ")
				.setAlphabeticShortcut('h');
		return (super.onCreateOptionsMenu(menu));
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case IDM_ZERO_STOCKS:
				mShowZeroStocks = false;
				updateList();
				return true;
			case IDM_ZERO_STOCKS_SHOW:
				mShowZeroStocks = true;
				updateList();
				return true;
		}
		return true;
	}

	TextWatcher prcWatcher = new TextWatcher(){
		@Override
		public void afterTextChanged(Editable arg0)
		{
			mCursor.moveToPosition(selectedPosition);
			if (edtPrc.getText().toString().length()==0
					|| edtPrc.getText().toString().equals("."))
				customPrice=0;
			else {
                customPrice = Float.parseFloat(edtPrc.getText().toString());
			}
			float tmpQTY = DataUtils.Float.parse(edtQty.getText().toString());

			int qt = 1;
			if (chkPack.isChecked() == true) {
				try {
					qt = Integer.parseInt(mCursor.getString(3));
				} catch (Exception e) {
					qt = 1;
				}
			}
			txtCount.setText(DataUtils.Float.format("%.3f", tmpQTY*qt));
			txtTotal.setText(DataUtils.Float.format("%.2f", qt*tmpQTY * Float.parseFloat(mCursor.getString(9)) * customPrice ));
			edtSum.removeTextChangedListener(sumWatcher);
			edtSum.setText(txtTotal.getText());
			edtSum.addTextChangedListener(sumWatcher);
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

		}};
	TextWatcher sumWatcher = new TextWatcher(){
	@Override
	public void afterTextChanged(Editable arg0) {
		mCursor.moveToPosition(selectedPosition);
		if (edtSum.getText().toString().length()==0
				|| edtSum.getText().toString().equals(".")) {
			customPrice = 0;
			edtPrc.removeTextChangedListener(prcWatcher);
			edtPrc.setText("0");//
			edtPrc.addTextChangedListener(prcWatcher);
		} else {
			float sum = DataUtils.Float.parse(edtSum.getText().toString());
			float count = DataUtils.Float.parse(edtQty.getText().toString());
			customPrice = sum/count;
			String newpr= DataUtils.Float.format("%.8f", customPrice);
			edtPrc.removeTextChangedListener(prcWatcher);
			edtPrc.setText(newpr);
			edtPrc.addTextChangedListener(prcWatcher);
		}
		float tmpQTY = DataUtils.Float.parse(edtQty.getText().toString());

		int qt = 1;
		if (chkPack.isChecked() == true) {
			try {
				qt = Integer.parseInt(mCursor.getString(3));
			} catch (Exception e) {
				qt = 1;
			}
		}
		txtCount.setText(DataUtils.Float.format("%.3f", tmpQTY*qt));
		txtTotal.setText(DataUtils.Float.format("%.2f", qt*tmpQTY * Float.parseFloat(mCursor.getString(9)) * customPrice ));
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

	}};

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
		ctx = this;
		lightTheme = getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE).getBoolean(RsaDb.LIGHTTHEMEKEY, false);
		showRecomend = getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE).getBoolean(RsaDb.SHOWRECINLIST, false);

		SharedPreferences def_prefs = PreferenceManager.getDefaultSharedPreferences(this);
		currency = " "+def_prefs.getString("prefCurrency", getResources().getString(R.string.preferences_currency_summary));
		mIsVim = def_prefs.getBoolean("extVim", false);


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
		groupPos = 0;
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
		strGlobalSearch = extras.getString("STEXT");
		arrayPricetypes = extras.getStringArrayList("PR_TYPES");

		if (isTOPSKU==true || strGlobalSearch!=null) {
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
    	groupPos = extras.getInt("GPOSITION");
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
		filterText.setText(strGlobalSearch);
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

		btnOptions = (Button) findViewById(R.id.options);
		btnOptions.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				openOptionsMenu();
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
		    	intent.putExtra("GPOSITION", groupPos);

		    	// Set result of goods additting is OK and send it's data to GroupActivity.class
		    	setResult(RESULT_OK, intent);

		    	finish();
			}
		});
	}

	private void checkForFlash(String sflash, fontFlash ff, String sku)
	{
		if (sflash==null)
			return;

		if (sflash.equals("1")) {
			ff.setTypeFace(Typeface.DEFAULT_BOLD);

			if (findInHist(sku)) {
				ff.setColor(lightTheme?Color.BLUE:Color.parseColor("#00FFFF"));
			} else {
				ff.setColor(Color.RED);
			}
		}
	}

	private boolean findInHist(String sku) {
		boolean res = false;

		String q = "select GOODS_ID " +
     		   "from _hist " +
     		   "where CUST_ID = '" + mOrderH.cust_id + "' " +
     		   "and SHOP_ID = '" + mOrderH.shop_id +"' " +
     		   "and FLASH = '1' " +
     		   "and GOODS_ID = '" + sku +"' limit 1";

		Cursor c = null;
		try {
			c = db.rawQuery(q, null);
			res = c.getCount()>0;
			c.close();
		} catch (Exception e) {}

		return res;
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
		boolean isSkladDetalization = false;
		SharedPreferences def_prefs = PreferenceManager.getDefaultSharedPreferences(this);
		try {
			isSkladDetalization = def_prefs.getBoolean("prefSkladdet", false);
		} catch(Exception e) {
			Log.d("RRRA", "11111");
		}

		/** Ticket 28: Get Shared Screen Preferences */
		SharedPreferences screen_prefs = getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE);
		SharedPreferences prefs = getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);
		int intMaxPriceType = prefs.getInt(RsaDb.MAXPRICETYPE, 20);
		// Ticket 28: Modified by ticked. If boolean value pricetype in setting not checked then
		if (!screen_prefs.getBoolean(RsaDb.PRICETYPEKEY, false))
		{
			String strCheckNDS = "";
			String orderString = "";
			int index=0;
			try
			{
				/** Get array from res 0 - "Price 1"; 1 - "Price 2" */
				String[] arrayPrices = getResources().getStringArray(R.array.Prices);

				String price = screen_prefs.getString(RsaDb.PRICESELECTED, "xxx");
				//btnPrice.setText(price);
				//currentPrice = price;

				// find price index
				index = 0;
				for (int i=0;i<arrayPrices.length;i++)
				{
					if (arrayPrices[i].equals(price))
					{
						index = i;
						break;
					}
				}
				String strPriceName = def_prefs.getString("prefPrice"+Integer.toString(index+1), "Цена "+Integer.toString(index+1));
				btnPrice.setText(strPriceName);
				currentPrice = strPriceName;
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
		        if (strGlobalSearch != null) {
		        	whereGroupOrBrand = RsaDbHelper.GOODS_FLASH + "!='7";
		        } else if (isTOPSKU==true) {
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
		        if (mOrderH.hndsrate.equals("0")||mOrderH.hndsrate.equals("")) {
		        	strCheckNDS = "(" + RsaDbHelper.GOODS_NDS + "<>'0') AND (" + RsaDbHelper.GOODS_NDS + "<>'0.07') AND ("
		        			          + RsaDbHelper.GOODS_NDS + "<>'')";
		        } else if (mOrderH.hndsrate.equals("2")) {
		        	strCheckNDS = "(" + RsaDbHelper.GOODS_NDS + "='0.07')";
		        } else {
		        	strCheckNDS = "(" + RsaDbHelper.GOODS_NDS + "='0') OR ("
	  			          + RsaDbHelper.GOODS_NDS + "='')";
		        }
			}
			catch(Exception e)
			{
				Log.d("RRRA", "22222");
				Toast.makeText(getApplicationContext(), "Error: 1> updatelist()\n"
					       								+"2> mContent[5]= " + mContent[5], Toast.LENGTH_LONG).show();



			}

			// Using PRICE1 column only
			// Get data from database to mCursor by call query:
			// SELECT TABLE_GOODS FROM mContent WHERE (GOODS_GROUP_ID = GROUP_ID) AND (GOODS_NAME LIKE '%string%')
	        // AND () ORDER BY GOODS_NPP
	        String qq = "<empty>";
	        Log.d("RRRA", "333333");
			String zeroStocks = "";
	        try
	        {
	        		qq = "( g." + whereGroupOrBrand + "') AND ("
							+ RsaDbHelper.GOODS_NAME + " LIKE '%" + filterText.getText()
							+ "%') AND (" + strCheckNDS + ") " + orderString;

				if (!mShowZeroStocks) {
					zeroStocks = "REST > 0 AND ";
				}

	        		//mCursor = db.query(RsaDbHelper.TABLE_GOODS, mContent,
					//									qq,
					//									null, null, null, null);
	        		String myQuery =	"SELECT distinct g._id as _id, g.ID as ID, GROUP_ID, QTY, NAME, PRICE"+Integer.toString(index+1)+", WEIGHT1, "
	        							+ "UN, REST, COEFF, NDS, NPP, WEIGHT, FLASH, g.BRAND_ID as BRAND_ID, ifnull(c.DISCOUNT, '0') as DISCOUNT "
	        							+ "FROM _goods as g "
										+ "LEFT JOIN _char as c on g.BRAND_ID = c.BRAND_ID AND c.CUST_ID = '" + mOrderH.cust_id + "' "
										+ "WHERE " + zeroStocks + qq
										+ "ORDER BY NAME";

	        			if (isSkladDetalization) {
	        				myQuery =	"SELECT distinct g._id as _id, g.ID as ID, GROUP_ID, QTY, NAME, PRICE"+Integer.toString(index+1)+", WEIGHT1, "
        								+ "UN, ifnull(s.FUNC_1,'0') as REST, COEFF, NDS, NPP, WEIGHT, FLASH, g.BRAND_ID as BRAND_ID, ifnull(c.DISCOUNT, '0') as DISCOUNT "
        								+ "FROM _goods as g "
        								+ "LEFT JOIN _skladdet as s on g.ID = s.GOODS_ID "
										+ "LEFT JOIN _char as c on g.BRAND_ID = c.BRAND_ID AND c.CUST_ID = '" + mOrderH.cust_id + "' "
        								+ "WHERE s.SKLAD_ID = '" + mOrderH.sklad_id.toString() + "' "
        								+ "AND " + zeroStocks + qq;
	        			}

	        			Log.d("RRRA", "555555");
	        		mCursor = db.rawQuery(myQuery.toString(), new String[]{});
	        		Log.d("RRRA", "666666");
	        		if (mCursor==null || mCursor.getCount()<1) {
	        			Log.d("RRRA", "777777 ggg");
	        			Toast.makeText(getApplicationContext(), "Пусто! Проверьте, выбрали ли вы правильный СКЛАД!", Toast.LENGTH_LONG).show();
	        		}

	        }
	        catch (Exception e)
	        {
	        	Log.d("RRRA", "8888 except");
	        	Toast.makeText(getApplicationContext(), "Error: 1> db.query()\n"
	        									       +"2> " + qq, Toast.LENGTH_LONG).show();
	        }
		}
		else
		{	Log.d("RRRA", "pricetype");
			if (strGlobalSearch != null) {
				whereGroupOrBrand = RsaDbHelper.GOODS_FLASH + "!='7";
			} else if (isTOPSKU==true) {
	        	whereGroupOrBrand = RsaDbHelper.GOODS_FLASH + "='1";
	        } else {
		        if (isGroup == true) {
		        	whereGroupOrBrand = RsaDbHelper.GOODS_GROUP_ID + "='" + curGroupID;
		        } else {
		        	// if brand selected then select goods by brand
		        	whereGroupOrBrand = RsaDbHelper.GOODS_BRAND_ID + "='" + curBrandID;
		        }
	        }
			String zeroStocks = " AND ";
			if (!mShowZeroStocks) {
				zeroStocks = " AND REST > 0 AND ";
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
				myQuery.append(
							(isSkladDetalization==false?"SELECT _id, ":"SELECT g._id, ")
										   + RsaDbHelper.GOODS_ID 	+ ", " + RsaDbHelper.GOODS_GROUP_ID + ", " + RsaDbHelper.GOODS_QTY 		+ ", "
										   + RsaDbHelper.GOODS_NAME + ", " + RsaDbHelper.CHAR_PRICE + arrayPricetypes.get(i) + ", " + RsaDbHelper.GOODS_WEIGHT1 	+ ", "
										   + RsaDbHelper.GOODS_UN 	+ ", "
										   + (isSkladDetalization==false?"REST":"ifnull(s.FUNC_1,'0') as REST")
										   + ", " + RsaDbHelper.GOODS_COEFF + ", "
										   + RsaDbHelper.GOODS_NDS  + ", " + RsaDbHelper.GOODS_NPP + ", " + RsaDbHelper.GOODS_WEIGHT + ", "
										   + RsaDbHelper.GOODS_FLASH + ", " + RsaDbHelper.GOODS_BRAND_ID + ", " + RsaDbHelper.GOODS_DISCOUNT
						+ 	" FROM " + RsaDbHelper.TABLE_GOODS +
						(isSkladDetalization==false?"":" LEFT JOIN _skladdet as s on g.ID = s.GOODS_ID") +
							" WHERE (" + whereGroupOrBrand + "')" +
						(isSkladDetalization==false?"":" AND s.SKLAD_ID = '" + mOrderH.sklad_id.toString() + "'")
						+	zeroStocks
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
			if (mCursor==null || mCursor.getCount()<1) {
    			Toast.makeText(getApplicationContext(), "Пусто! Проверьте, выбрали ли вы правильный СКЛАД!", Toast.LENGTH_LONG).show();
    		}
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
	        								    0, //[13] flash
												R.id.txtDiscount_goods,
												R.id.txtPriceWithDiscount_goods
	        								});
		}
		catch (Exception e)
		{
			Log.d("RRR", e.getMessage());
			Toast.makeText(getApplicationContext(), "Error: 1> mCursor.moveToFirst()", Toast.LENGTH_LONG).show();
		}


        mAdapter.setViewBinder(new ViewBinder()
        {
            public boolean setViewValue(View aView, Cursor aCursor, int aColumnIndex)
            {
            	if (aColumnIndex == 4)
                {
	                    TextView textView2 = (TextView) aView;

	                    fontFlash ff = new fontFlash(Typeface.DEFAULT, lightTheme?Color.BLACK:Color.LTGRAY);
	                    checkForFlash(aCursor.getString(13), ff, mCursor.getString(1));

	                    textView2.setTextColor(ff.getColor());
		                textView2.setTypeface(ff.getTypeface());
                        return false;
                }
                if (aColumnIndex == 5)
                {
                        String newPrice = getPriceWithTax(aCursor, aColumnIndex);
                        TextView textView = (TextView) aView;
                        textView.setText(newPrice);
                        return true;
                }

                if (aColumnIndex == 9)
                {

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

	                        TextView textView2 = (TextView) aView;

	                        textView2.setText(findFixedRest(aCursor.getString(1)));
                        }
                        catch(Exception e)
                        {
                        	Toast.makeText(getApplicationContext(), "Error: 1> aColumnIndex == 10", Toast.LENGTH_LONG).show();
                        }

                        return true;
                }

				if (aColumnIndex == 14)
				{
					float discount = Float.parseFloat(aCursor.getString(15));
					TextView textView = (TextView) aView;
					textView.setText(DataUtils.Float.format("%.0f",discount) + "%");
					return true;
				}

				if (aColumnIndex == 15)
				{
					float price = Float.parseFloat(getPriceWithTax(aCursor, 5));
					float discount = Float.parseFloat(aCursor.getString(15));
					String newPrice;
					if(discount > 0){
						newPrice = DataUtils.Float.format("%.2f", price - (price * discount) / 100);
					}else{
						newPrice = Float.toString(price);
					}
					TextView textView = (TextView) aView;
					textView.setText(newPrice);
					return true;
				}

                if (aColumnIndex == 1)
                {

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
	                        												   "4> ", Toast.LENGTH_LONG).show();
	                        }
                		}
                        return true;
                }



                return false;
            }
        });

        Log.d("RRRA", "adapter");
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
			Log.d("RRRA", "afterwork");
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
			txtTotalSum.setText(DataUtils.Float.format("%.2f",hsumo) + " $");
			Log.d("RRRA", "hz");
			TextView txtGroup = (TextView)findViewById(R.id.goods_txtGroup);
			if (strGlobalSearch!=null) {
				txtGroup.setText("ГЛОБАЛЬНЫЙ ПОИСК");
			} else if (isTOPSKU==true) {
				txtGroup.setText("TOP SKU");
			} else {
				txtGroup.setText(isGroup?curGroupName:curBrandName);
			}
			Log.d("RRRA", "end");
        }
        catch (Exception e)
        {
        	Log.d("RRRA", "why??");
        	Toast.makeText(getApplicationContext(), "Error: 1> setListAdapter(mAdapter)", Toast.LENGTH_LONG).show();
        }
	}

	private String getPriceWithTax(Cursor cursor, int priceIdx){
		float oldPrice = Float.parseFloat(cursor.getString(priceIdx));
		float NDS = Float.parseFloat(cursor.getString(10));
		return DataUtils.Float.format("%.2f", oldPrice*(1+NDS));
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
    	intent.putExtra("GPOSITION", groupPos);

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
		outState.putString("STEXT", strGlobalSearch);

		outState.putStringArrayList("PR_TYPES", arrayPricetypes);


		// Send previous position of listview of groupsactivity
    	outState.putInt("TOP", ttop);
    	outState.putInt("INDEX", iidx);
    	outState.putInt("GPOSITION", groupPos);


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
			if (merch_only == false) {
				if (checkForLock(selectedPosition, mCursor, mOrderH.cust_id.toString(), mOrderH.shop_id.toString())==false) {
					showDialog(IDD_COUNT);
				} else {
					Toast.makeText(getApplicationContext(), "Данное SKU заблокировано для этой ТТ!", Toast.LENGTH_SHORT).show();
				}
			} else {
				showDialog(IDD_HIST);
			}
	}

	private static boolean checkForLock(int pos, Cursor cur, String cId, String sId) {
		boolean res = false;
		Calendar c = Calendar.getInstance();
		SimpleDateFormat sfmt = new SimpleDateFormat("yyyy-MM-dd");
		String q = null;
		String gId = "0";
		String dt  = sfmt.format(c.getTime());

		if (pos>=0) {
			try {
				cur.moveToPosition(pos);
				gId = cur.getString(1);
				q = "select DATE from _prodlock where GOODS_ID='"+gId+"' and CUST_ID='"+cId+"' and SHOP_ID='"+sId+"' and DATE='"+dt+"'";
				Cursor cc = db.rawQuery(q, null);
				if (cc.getCount()>0)
					res = true;
				cc.close();
			} catch(Exception e) {}
		}
		return res;
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
		SharedPreferences def_prefs = PreferenceManager.getDefaultSharedPreferences(this);
		switch(id)
		{
			case IDD_PRICE:
			{
				/** Get array from res 0 - "Price 1"; 1 - "Price 2" */
				//final String[] items = getResources().getStringArray(R.array.Prices);
				final String[] items = new String[20];
				for (int j=1;j<=20;j++) {
					items[j-1] = def_prefs.getString("prefPrice"+Integer.toString(j),"Цена "+Integer.toString(j));
				}
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(getResources().getString(R.string.goods_select));

				builder.setItems(items, new DialogInterface.OnClickListener() {
				    public void onClick(DialogInterface dialog, int item)
				    {
				    	SharedPreferences screen_prefs = getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE);

				        screen_prefs.edit().putString(RsaDb.PRICESELECTED, "Цена "+Integer.toString(item+1)).commit();
				        updateList();
				    }
				});

				return builder.create();
			}
			case IDD_COUNT:
			{
				/** Set my own view of dialog to display to layout variable. Uses dlg_goods.xml */
				LayoutInflater inflater = getLayoutInflater();
				View layout = inflater.inflate(R.layout.dlg_goods,
						(ViewGroup)findViewById(R.id.dlg_goods));

				/** Binding xml elements of my own layout to final variables */
				txtName 	= (TextView)layout.findViewById(R.id.txtName_dlgGoods);
				txtId 		= (TextView)layout.findViewById(R.id.txtId_dlgGoods);
				txtPrice 	= (TextView)layout.findViewById(R.id.txtPrice_dlgGoods);
				txtDiscount = (TextView)layout.findViewById(R.id.txtDiscount_dlgGoods);
				txtPriceWithDiscount = (TextView)layout.findViewById(R.id.txtPriceWithDiscount_dlgGoods);
				txtWeight 	= (TextView)layout.findViewById(R.id.txtWeight_dlgGoods);
				txtRest		= (TextView)layout.findViewById(R.id.txtRest_dlgGoods);
				txtCount 	= (TextView)layout.findViewById(R.id.txtCount_dlgGoods);
				txtTotal 	= (TextView)layout.findViewById(R.id.txtTotal_dlgGoods);
				TextView txtCurrency 	= (TextView)layout.findViewById(R.id.txtCurrency_dlgGoods);
				edtQty 		= (EditText)layout.findViewById(R.id.edtQty);
				chkPack		= (CheckBox)layout.findViewById(R.id.chkPack);
				edtPrc		= (EditText) layout.findViewById(R.id.edtPrc);
				edtSum		= (EditText)layout.findViewById(R.id.edtSum);
				chkPrc		= (CheckBox)layout.findViewById(R.id.chkPrc);
				rltPrc 		= (RelativeLayout)layout.findViewById(R.id.rltPrc);

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
						+ DataUtils.Float.format("%.2f", tpr)
						+ currency);
				float discount = Float.parseFloat(mCursor.getString(15));
				txtDiscount.setText(getResources().getString(R.string.lines_dlg_discount)
						+ DataUtils.Float.format("%.0f",discount)
						+ "%");
				float priceWithDiscount = tpr - tpr * discount / 100;
				txtPriceWithDiscount.setText(getResources().getString(R.string.lines_dlg_price_with_discount)
						+ DataUtils.Float.format("%.2f", priceWithDiscount)
						+ currency);
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
					edtQty.setText("1.000");
					////
					mCursor.moveToPosition(selectedPosition);
					txtCount.setText("1");
					// Set special element SUM on dialog view same as edtQty
					// SUM = QTY * GOODS_COEFF * GOODS_PRICE1
					txtTotal.setText(DataUtils.Float.format("%.2f", Float.parseFloat(mCursor.getString(9)) * priceWithDiscount ));
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
					if (un.equals("qq")) {
						chkPack.setChecked(true);
						float tmpQTY = DataUtils.Float.parse(edtQty.getText().toString());
						mCursor.moveToPosition(selectedPosition);

						int qt = 1;
						if (chkPack.isChecked() == true) {
							try {
								qt = Integer.parseInt(mCursor.getString(3));
							} catch (Exception e) {
								qt = 1;
							}
						}
						txtCount.setText(DataUtils.Float.format("%.3f", tmpQTY*qt));
						txtTotal.setText(DataUtils.Float.format("%.2f", qt*tmpQTY * Float.parseFloat(mCursor.getString(9)) * priceWithDiscount ));

					} else {
						chkPack.setChecked(false);
					}

					txtCount.setText(CurrentLine.get(OrderLines.QTY));
					txtTotal.setText(CurrentLine.get(OrderLines.SUMWNDS));
					edtQty.setText(CurrentLine.get(OrderLines.QTY));
				}

				/** Listener for trying to change QTY of selected item in Order */
				edtQty.addTextChangedListener(new TextWatcher(){

					int startChanged, countChanged;

					@Override
					public void afterTextChanged(Editable arg0)
					{
						// Move Cursor to selected position before save data
						mCursor.moveToPosition(selectedPosition);

						String count = txtCount.getText().toString();
						String formattedValue = DataUtils.Float.format("%.3f",Float.parseFloat(count));
						if ((!formattedValue.equals(edtQty.getText().toString())) && (!chkPack.isChecked())) {
							//edtQty.setText(formattedValue);
							int position = startChanged + countChanged;
							edtQty.setSelection(position, position);
						}
					}

					@Override
					public void beforeTextChanged(CharSequence arg0, int arg1,
							int arg2, int arg3)
					{
						// do nothing
					}

					@Override
					public void onTextChanged(CharSequence arg0, int start,
							int before, int count)
					{
						startChanged = start;
						countChanged = count;
						float tmpQTY = DataUtils.Float.parse(edtQty.getText().toString());
						mCursor.moveToPosition(selectedPosition);

						int qt = 1;
						if (chkPack.isChecked() == true) {
							try {
								qt = Integer.parseInt(mCursor.getString(3));
							} catch (Exception e) {
								qt = 1;
							}
						}
						txtCount.setText(DataUtils.Float.format("%.3f", tmpQTY*qt));
						float pr = Float.parseFloat(mCursor.getString(5));
						float nd = Float.parseFloat(mCursor.getString(10)) + 1;
						float discount = Float.parseFloat(mCursor.getString(15));
						float tpr = pr * nd;
						customPrice = tpr - tpr * discount / 100;
						txtTotal.setText(DataUtils.Float.format("%.2f", qt*tmpQTY * Float.parseFloat(mCursor.getString(9)) * customPrice ));
						edtSum.removeTextChangedListener(sumWatcher);
						edtSum.setText(txtTotal.getText());
						edtSum.addTextChangedListener(sumWatcher);
				}});
				edtPrc.addTextChangedListener(prcWatcher);

				edtSum.addTextChangedListener(sumWatcher);



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

						float tmpQTY = DataUtils.Float.parse(edtQty.getText().toString());
						mCursor.moveToPosition(selectedPosition);

						int qt = 1;
						if (chkPack.isChecked() == true) {
							try {
								qt = Integer.parseInt(mCursor.getString(3));
							} catch (Exception e) {
								qt = 1;
							}
						}
						txtCount.setText(DataUtils.Float.format("%.3f", tmpQTY*qt));
						float pr = Float.parseFloat(mCursor.getString(5));
						float nd = Float.parseFloat(mCursor.getString(10)) + 1;
						float tpr = pr * nd;
						txtTotal.setText(DataUtils.Float.format("%.2f", qt*tmpQTY * Float.parseFloat(mCursor.getString(9)) * customPrice ));
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
						// If such goods are not ordered earlier and user wants to
						// order now not 0 of it...
						String quantity = edtQty.getText().toString();
						if ((idx<0)&&(!quantity.isEmpty())&&(!quantity.equals("0")))
						{
							/** Temporary variable of SUM with NDS to store some data before saving it */
							float tmpSumwnds = Float.parseFloat(txtTotal.getText().toString());
							/** Temporary variable of PRICE without NDS to store some data before saving it */
							float tmpPricewonds = Float.parseFloat(mCursor.getString(5));
							/** Temporary variable of SUM without NDS to store some data before saving it */
							float pr = Float.parseFloat(mCursor.getString(5));
							float nd = Float.parseFloat(mCursor.getString(10)) + 1F;
							float tpr = pr * nd;
							float tmpSumwonds = tmpSumwnds / nd;
							String un = mCursor.getString(7);
							float qt = 1;
							if (chkPack.isChecked()==true) {
								try {
									qt = Float.parseFloat(mCursor.getString(3));
								} catch (Exception e) {
									qt = 1;
								}
								qt = qt * Float.parseFloat(quantity);
								un = "qq";
							} else {
								qt = Float.parseFloat(quantity);
							}

							String current_Brandname = "brand_err1";
							try {
								current_Brandname = findBrandNameById(mCursor.getString(14), db);
							} catch (Exception e) {}



							// Adding selected item to order
							mOrderH.lines.add(new OrderLines(				"0", 											// *id used for REST QTY in Shop (Merchandising)
																			"",												// *zakaz_id used in headactivity
																			mCursor.getString(1),							// goods_id
																			mCursor.getString(4), 							// text_goods
																			currentPrice,    								// PRICETYPE (Price1,Price2...20)
																			DataUtils.Float.format("%.3f", qt),							// qty
																			un,												// un
																			mCursor.getString(9),							// coeff
																			mCursor.getString(15),											// discount
																			DataUtils.Float.format("%.8f",tpr),	// pricewnds
																			DataUtils.Float.format("%.8f",tmpSumwnds),	// sumwnds
																			DataUtils.Float.format("%.8f",tmpPricewonds), 	// pricewonds=pricewnds/1.2
																			DataUtils.Float.format("%.8f",tmpSumwonds),	// sumwonds=sumwnds/1.2
																			DataUtils.Float.format("%.8f",tmpSumwnds-tmpSumwonds),	// nds=sumwnds-sumwonds
																			mCursor.getString(10),							// must be delay, but overloaded to NDS flag
																			mCursor.getString(6),							//weight
																			goods_comments,										// comment
																			current_Brandname));			    				// brand
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
																						DataUtils.Float.format("%.3f", quantity)));					// qty
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
						else if ((idx>=0)&&(!quantity.equals(""))&&(!quantity.equals("0")))
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
							float qty = qt * Float.parseFloat(quantity);

							// Just change QTY of previously ordered item
							CurrentLine.put(OrderLines.QTY, 	Float.toString(qty));
							// Just change SUM with NDS of previously ordered item
							CurrentLine.put(OrderLines.SUMWNDS, txtTotal.getText().toString());
							// Just change SUM without NDS of previously ordered item
							CurrentLine.put(OrderLines.SUMWONDS,
									Float.toString(Float.parseFloat(txtTotal.getText().toString())/1.2F));
						}
						// But if user choose QTY = 0 then show pop-up message that it is forbiden to order 0 QTY
						else
						{
							Toast.makeText(getApplicationContext(), R.string.goods_qty_notcorrect, Toast.LENGTH_LONG).show();
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
																		mCursor.getString(15),						// discount
																		"0.00",											// pricewnds
																		"0.00",											// sumwnds
																		"0.00", 										// pricewonds=pricewnds/1.2
																		"0.00",											// sumwonds=sumwnds/1.2
																		"0.00",											// nds=sumwnds-sumwonds
																		"0.0",											// must be delay, but overloaded to NDS flag
																		"0", "", "brand_merch"));
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


			if (isCustomPricing(db, mCursor.getString(14))) {
				showCustomPrice = true;
			} else {
				showCustomPrice = false;
			}


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
			if (idx >= 0) {
				CurrentLine = mOrderH.lines.get(idx);
			}

			// If item was previously added to current Order, then
			// set CurrentLine to line with this previously added item
			if (idx_rest >= 0) {
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
				txtWeight 	= (TextView)dialog.findViewById(R.id.txtWeight_dlgGoods);
				txtRest 	= (TextView)dialog.findViewById(R.id.txtRest_dlgGoods);
				txtCount 	= (TextView)dialog.findViewById(R.id.txtCount_dlgGoods);
				txtTotal 	= (TextView)dialog.findViewById(R.id.txtTotal_dlgGoods);
				edtQty 		= (EditText)dialog.findViewById(R.id.edtQty);
				chkPack 	= (CheckBox)dialog.findViewById(R.id.chkPack);
				chkPrc		= (CheckBox)dialog.findViewById(R.id.chkPrc);
				edtPrc		= (EditText)dialog.findViewById(R.id.edtPrc);
				edtSum		= (EditText)dialog.findViewById(R.id.edtSum);

				if (showCustomPrice && !mIsVim) {
					rltPrc.setVisibility(View.VISIBLE);
					edtSum.setVisibility(View.GONE);
				} else if (showCustomPrice && mIsVim && mOrderH.num1c!=null
						&& mOrderH.num1c.toString().length()>0
						&& !mOrderH.num1c.toString().equals("0")) {
					edtSum		= (EditText)dialog.findViewById(R.id.edtSum);
					rltPrc.setVisibility(View.VISIBLE);
					edtSum.setVisibility(View.VISIBLE);
				} else {
					rltPrc.setVisibility(View.GONE);
				}

				chkPrc.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						if (isChecked) chkPrc.setChecked(false);
						mCursor.moveToPosition(selectedPosition);
						float pr = Float.parseFloat(mCursor.getString(5));
						float nd = Float.parseFloat(mCursor.getString(10)) + 1;
						float tpr = pr * nd;
						customPrice = tpr;
						edtPrc.setText(DataUtils.Float.format("%.8f", customPrice));
					}
				});
				/*
				edtPrc.addTextChangedListener(new TextWatcher() {
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
						if (edtPrc.getText().toString().length()==0 
								|| edtPrc.getText().toString().equals(".")) 
							customPrice=0;
						else
							customPrice= Float.parseFloat(edtPrc.getText().toString());
					}
				});*/

				txtComment  = (TextView)dialog.findViewById(R.id.txtComment_dlgGoods);

				if (CurrentLine!=null)
					txtComment.setText(CurrentLine.get(OrderLines.COMMENT));
				else
					txtComment.setText("ИСТОРИЯ");
				if (txtComment.getText().length()<1) {
					txtComment.setText("ИСТОРИЯ");
				}

				txtComment.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent();

						ArrayList<String> elements = new ArrayList<String>();
						elements.add("Дата:");
						elements.add("Кол-во:");
						elements.add("Цена:");

						String q = "select DATE, QTY, PRICE " +
				        		   "from _hist " +
				        		   "where CUST_ID = '" + mOrderH.cust_id + "' " +
				        		   "and SHOP_ID = '" + mOrderH.shop_id +"' " +
				        		   "and GOODS_ID = '" + mCursor.getString(1) +"' ";
						Cursor mCursorHist = null;
						try {
							mCursorHist = db.rawQuery(q, null);
						} catch (Exception e) {
							return;
						}

						if (mCursorHist.getCount()>0) {
							mCursorHist.moveToFirst();
							for(int i=0;i<mCursorHist.getCount();i++) {
								elements.add(mCursorHist.getString(0));
								elements.add(mCursorHist.getString(1));
								elements.add(mCursorHist.getString(2));
								mCursorHist.moveToNext();
							}
						}

						String[] array_elements = new String[elements.size()];
						elements.toArray(array_elements);

						intent.setClass(getApplicationContext(), ShowreportActivity.class);
						intent.putExtra("NAME", "Движения по SKU");
						intent.putExtra("ELEMENTS", array_elements);
						intent.putExtra("COLUMNS", 3);
						startActivity(intent);

						/// comment functionality
						/*
							LayoutInflater		inflater	= getLayoutInflater();
							View 				layout		= inflater.inflate(R.layout.dlg_head, (ViewGroup)findViewById(R.id.linear_dlg_head));
							AlertDialog.Builder builder		= new AlertDialog.Builder(ctx);
							final EditText 		edtRemark	= (EditText)layout.findViewById(R.id.edtRemark_head);
							edtRemark.setFilters(new InputFilter[] {new InputFilter.LengthFilter(30)});
							builder.setView(layout);
							
							String _com = "";
							if (CurrentLine != null) {
								_com = CurrentLine.get(OrderLines.COMMENT);
								edtRemark.setText(_com);
							} else {
								edtRemark.setText("");
							}
							if (edtRemark.getText().length()<1) {
								edtRemark.setText("");
							}
							
							builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {					
								@Override
								public void onClick(DialogInterface dialog, int which) {
									goods_comments = edtRemark.getText().toString();
									if (goods_comments.equals("ИСТОРИЯ"))
										goods_comments = "";
									if (CurrentLine!=null)
										CurrentLine.put(OrderLines.COMMENT, goods_comments);
									txtComment.setText(goods_comments);
								}
							});
							
							builder.setCancelable(false);
							builder.show();
						*/
						/////////
					}
				});

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
				customPrice = tpr;
				txtPrice.setText(getResources().getString(R.string.goods_writeprice)
						+ DataUtils.Float.format("%.4f", tpr)
						+ currency);
				edtPrc.setText(DataUtils.Float.format("%.8f", customPrice));
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

					edtQty.setText("1.000");


					////
							mCursor.moveToPosition(selectedPosition);
							txtCount.setText("1");
							// Set special element SUM on dialog view same as edtQty
							// SUM = QTY * GOODS_COEFF * GOODS_PRICE1
							float pr2 = Float.parseFloat(mCursor.getString(5));
							float nd2 = Float.parseFloat(mCursor.getString(10)) + 1;
							float tpr2 = pr2 * nd;
							txtTotal.setText(DataUtils.Float.format("%.2f", Float.parseFloat(mCursor.getString(9)) * customPrice ));
					////

					SharedPreferences screen_prefs = PreferenceManager.getDefaultSharedPreferences(this);
					if (screen_prefs.getBoolean(RsaDb.USEPACKS, false) == true) {
						chkPack.setChecked(true);
						mCursor.moveToPosition(selectedPosition);

						float tmpQTY = DataUtils.Float.parse(edtQty.getText().toString());

						int qt = 1;
						if (chkPack.isChecked() == true) {
							try {
								qt = Integer.parseInt(mCursor.getString(3));
							} catch (Exception e) {
								qt = 1;
							}
						}
						txtCount.setText(Float.toString(tmpQTY*qt));
						pr = Float.parseFloat(mCursor.getString(5));
						nd = Float.parseFloat(mCursor.getString(10)) + 1;
						tpr = pr * nd;
						txtTotal.setText(DataUtils.Float.format("%.2f", qt*tmpQTY * Float.parseFloat(mCursor.getString(9)) * customPrice ));
					} else {
						chkPack.setChecked(false);
					}
				}
				// But if selected item was previously added to current Order, then
				// set special elements on dialog view same as in previous time (QTY, SUM)
				else
				{
					String un = CurrentLine.get(OrderLines.UN);
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

					float qty = Float.parseFloat(CurrentLine.get(OrderLines.QTY)) / qt;

					//edtQty.setText(Integer.toString(qt));
					txtCount.setText(CurrentLine.get(OrderLines.QTY));
					txtTotal.setText(CurrentLine.get(OrderLines.SUMWNDS));
					customPrice = Float.parseFloat(CurrentLine.get(OrderLines.SUMWNDS))/Float.parseFloat(CurrentLine.get(OrderLines.QTY));
					edtQty.setText(DataUtils.Float.format("%.3f", qty));
					edtPrc.setText(DataUtils.Float.format("%.8f", customPrice));

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

	private static String findBrandNameById(String id, SQLiteDatabase d) {
		String res = "Undef";
		String q = "select NAME from _brand where ID='"+id+"'";
		Cursor brName = db.rawQuery(q, null);

		if (brName.getCount()>0) {
			brName.moveToFirst();
			res = brName.getString(0);
		}

		return res;
	}

	private static boolean isCustomPricing(SQLiteDatabase d, String id) {
		String q = "select CPRICE from _brand where ID='"+id+"'";
		boolean res = false;
		Cursor cpr = db.rawQuery(q, null);
		try {
			if (cpr.getCount()>0) {
				cpr.moveToFirst();
				res = cpr.getString(0).equals("1");
			}
		} catch(Exception e) {
			res = false;
		}

		return res;
	}

}

