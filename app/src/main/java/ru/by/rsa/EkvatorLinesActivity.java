package ru.by.rsa;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SimpleCursorAdapter.ViewBinder;


/**
 * Activity that allows user to view, and change quantity of goods
 * already ordered in current order 
 * @author Komarev Roman
 * Odessa, neo3da@mail.ru, +380503412392
 */
public class EkvatorLinesActivity extends ListActivity 
{
	/** Constant to identify kind of dialog when dialog window will be called */
	private final static int IDD_COUNT = 0;
	private final static int IDD_HIST = 1;
	
	EditText edtRest = null;
	/** Index shows restsline position in previous order with the same goods or -1 if such goods was not found */
	private int idx_rest;
	
	/** Special adapter that must be used between mCursor and ListActivity */
	private ListAdapter mAdapter;
	/** Is set of data from DB to be shown in ListActivity. To get value have to use mCurosr.getString("KEY") */ 
	private Cursor mCursor;
	/** Temporary variable used if selected item already has been added */
	private OrderLines CurrentLine;
	private OrderRests CurrentRestsLine;	
	/** SQLite database that stores all data */
	private static SQLiteDatabase db;
	/** Selected position of selected goods */
	private int selectedPosition;
	/** Used for identify sending data of current order to new activity(GroupActivity.class) */
	static final int PICK_LINES_REQUEST = 1;
	/** Designed class to store data of current order before save it to database */
	private OrderHead mOrderH;
	/** Max value of current item */
	// private long maxQty;
	/** Key to find out: "does this active shown because of groups was closed (true) */
	private boolean isFromGroups;
	/** Array of columns that will be used to obtain data from DB-tables in columns with the same name */
	private String[]  mContent = {"_id",	RsaDbHelper.GOODS_ID,
											RsaDbHelper.GOODS_QTY, // [2]
											RsaDbHelper.GOODS_NAME,
											RsaDbHelper.GOODS_PRICE1,
											RsaDbHelper.GOODS_WEIGHT1,																						
											RsaDbHelper.GOODS_REST,
											RsaDbHelper.GOODS_COEFF,
											RsaDbHelper.GOODS_NDS, // [8]
											RsaDbHelper.GOODS_UN, // [9]
											RsaDbHelper.GOODS_PRICE20}; // [10]
	/** Current theme */
	private boolean lightTheme;
	private String currency;
	private boolean showRecomend;
	
	private static int verOS = 0;
	
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
	
	/** SQLite database that stores orders data */
	private static SQLiteDatabase db_orders_REC;
	
	SharedPreferences merch_prefs;
	boolean merch_only;
	private boolean isPad;
	
	/**
	 * Creates the activity
	 * @param savedInstanceState previous saved state of activity
	 */	
	public void onCreate(Bundle savedInstanceState) 
	{
		/** Data from previous activity (LinesActivity.class) to extras variable */
		Bundle extras;
		
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
		}
		else
		{
			setTheme(R.style.Theme_CustomBlack2);
		}
		super.onCreate(savedInstanceState);
		
		// init -1 that meens: by default is nothing selected
		selectedPosition = -1;
		
		// if activity was destroyed (for exmp. by display rotation)
		// then get data of current order (mOrderH) from saved 
		if (savedInstanceState != null)
        {
        	extras = savedInstanceState;
        	
        	// get selected goods position in list, used for dialog resuming
        	selectedPosition = extras.getInt("POSITION");
        }
        else
        {
        	// if not then
    		// Get data from previous activity (HeadActivity.class) to extras variable */
    		extras = getIntent().getExtras();
        }
		
		// Init class to store data of current order before save it to database
        mOrderH = new OrderHead();
        
		// Get data of current order from back activity(HeadActivity.class) to this */
		mOrderH = extras.getParcelable("ORDERH");
		
		// Get data of previous activity that was hided (does it was groupsactivity?)
		isFromGroups = extras.getBoolean("ISFROMGROUP");
		
		if (lightTheme)
			setContentView(R.layout.l_lines);
		else
			setContentView(R.layout.lines);
		
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
	}	
	
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
		
        // Button when user ends the adding goods into order, by pressing
        // it current activity going down, and previous activity
        // starts (HeadActivity)
        Button btnOK = (Button)findViewById(R.id.lines_pbtn_prev);
        
        // Listener for OK-button click, calls when OK-button clicked
        btnOK.setOnClickListener(new OnClickListener() 
        {
			@Override
			public void onClick(View v) {
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
				
				/** Used for prepearing data for another activity and send some data to it */
				Intent intent = new Intent();

		    	// Put data of current order to intent for using in next activity(LinesActivity.class)
		    	intent.putExtra("ORDERH", mOrderH);
		    	
		    	// Set result of lines additting is OK and send it's data to LinesActivity.class
		    	setResult(RESULT_OK, intent);
				
				// Close activity
				finish();
			}				
		});
        
        /** Used for preparing to start activity to add some goods in order  */
        final Intent intent = new Intent();
        
        // Define what exactly activity have to start
        intent.setClass(this, GroupActivity.class);
        
        // Button when user wants to add some goods into order, by pressing
        // it activity with goods group have to start (GroupActivity)
        Button btnADD = (Button)findViewById(R.id.lines_pbtn_next);
        
        // Listener for ADD-button, calls when ADD-button clicked
        // and starts it activity with goods group have to start (GroupActivity.class)
        btnADD.setOnClickListener(new OnClickListener() 
        {
			@Override
			public void onClick(View v) {
				 // Before Activity will be paused we have to close database
	    	     // to allow use it in another activities
				 if (db != null) 
	    	     {
	    	        db.close();
	    	     }
				 if (db_orders_REC != null) 
				 {
				    db_orders_REC.close();
				 }
				// Put data of current order to intent for using in next activity (GoodsActivity.class)
				intent.putExtra("ORDERH", mOrderH);
				
				// Starts GroupActivity.class
				startActivityForResult(intent, PICK_LINES_REQUEST);
			}				
		});
        
        /** Init database with orders  */
		RsaDbHelper mDb_REC = new RsaDbHelper(this, RsaDbHelper.DB_ORDERS);
		// Open database 
		db_orders_REC = mDb_REC.getReadableDatabase();
        
        // Updates list of ordered goods on display from static OrderH class
        updateList();

        // if there are no goods in current order then...
		if (mOrderH.lines.isEmpty())
		{   // if we came from GroupsActivity then...
			if (isFromGroups==true)
			{	// set to default = false
				isFromGroups=false;
				// back to head activity because of empty list of goods
				this.onBackPressed();
			}
			else // if we can from another activity (HeadActivity) then goto GroupsActivity 
			{
			    // Before Activity will be paused we have to close database
	    	    // to allow use it in another activities
			    if (db != null) 
	    	    {
	    	        db.close();
	    	    }
			    
			    if (db_orders_REC != null) 
			    {
			        db_orders_REC.close();
			    }
			    
				// Put data of current order to intent for using in next activity (GoodsActivity.class)
				intent.putExtra("ORDERH", mOrderH);
				// Starts GroupActivity.class
				startActivityForResult(intent, PICK_LINES_REQUEST);
			}
		}
	}
	
	/** 
	 * When user select some goods and go to this activity, he gets data of current order
	 *  by this method
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) 
	{
		/** Data from previous activity (GroupActivity.class) to extras variable */
		Bundle extras;
		
		// if arrived data from GoodsActivity then...
        if (requestCode == PICK_LINES_REQUEST) 
        {
        	if (resultCode == RESULT_OK)
        	{
                // Get data from previous activity (GroupActivity.class) to extras variable */
        		extras = data.getExtras();
        		
            	// Init class to store data of current order before save it to database (=null in that Activity)
                mOrderH = new OrderHead();
                
        		// Get data of current order from GroupActivity.class to this */
        		mOrderH = extras.getParcelable("ORDERH");
        		
        		// Set data of previous activity that it was groupsactivity
        		isFromGroups = true;
        	}
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
     * Method that starts if system trying to destroy activity
     */
    @Override
    protected void onDestroy() 
    {
    	super.onDestroy();
    	
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
		
		// put selected position
		outState.putInt("POSITION", selectedPosition);
	}
    
	/**
	* Used to update list of goods on device display
	* with data from static storage class mOrderH
	*/	
	private void updateList()
	{
		// Binds data from static storage class mOrderH
		// by adapter to elements in list_lines.xml
		mAdapter = new SimpleAdapter(
        		this, mOrderH.lines, lightTheme?R.layout.l_list_lines:R.layout.list_lines,
        		new String[] {OrderLines.TEXT_GOODS, OrderLines.PRICEWNDS, 
        				      OrderLines.SUMWNDS, OrderLines.QTY, OrderLines.GOODS_ID, OrderLines.GOODS_ID},
        		new int[] {R.id.txtName_lines, R.id.txtPrice_lines, 
        				   R.id.txtTotal_lines, R.id.txtQty_lines, R.id.txt_rest_lines, R.id.txt_rec_lines}
        );
		
		SimpleAdapter smplAdapter = (SimpleAdapter)mAdapter;
		
		smplAdapter.setViewBinder(new SimpleAdapter.ViewBinder() {
			@Override
			public boolean setViewValue(View view, Object data,
					String textRepresentation) {
				if (view.getId() == R.id.txt_rest_lines) {
                        
						// ORDERED COUNT ADDON
                        TextView textView = (TextView) view;
                        
                        if (showRecomend==false) {
                        	textView.setText("");
                        	return true;
                        }
                        textView.setText(findFixedRest(data.toString()));
                        
                        return true;
                }
				if (view.getId() == R.id.txt_rec_lines){
						
						
						
	            		// ORDERED COUNT ADDON
	                    TextView textView2 = (TextView) view;
	                    
	                    if (showRecomend==false) {
                        	textView2.setText("");
                        	return true;
                        }
	                    
	                    tblHistory_REC = mOrderH.prepareHistoryTable(db_orders_REC, data.toString());
	                    textView2.setText(mOrderH.calculateRecomendOrder(data.toString(), 
	                    									tblHistory_REC, 
	                    									mOrderH.getRestByGoodsIDfromRests(data.toString())));
                        
                        return true;
                }
				
				return false;
			}
		});
		
		// Set mAdapter to ListView in lines.xml
        setListAdapter(mAdapter); 
        
		/** Ticket 33: Used to get Sum from lines in special class holder OrderH */
		ArrayList<OrderLines> mOrder = mOrderH.lines;
		/** Ticket 33: Used to calculate "Sum with NDS" of all goods in current order */
		float hsumo = 0;
		// Ticket 33: FOR statement for every item in ArrayList
		for (OrderLines CurLine : mOrder) {
			//  Ticket 33: SUM all "sum with nds" from ordered goods one by one
			hsumo += Float.parseFloat(CurLine.get(OrderLines.SUMWNDS));
		} // Ticket 33: At the end of cycle hsumo stores total sum with nds of current order
		/** Ticket 33: Binding xml element of textview to variable */
		TextView txtTotalSum = (TextView)findViewById(R.id.lines_txtTotalSum_text);
		// Ticket 33: Set xml text view of current order sum with calculated sum
		txtTotalSum.setText(String.format("%.2f",hsumo).replace(',', '.') + " $");
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
		
		if (merch_only==false){
			// Starts dialog that allows user to modify already ordered goods in list on display  
			showDialog(IDD_COUNT);
		} else {
			showDialog(IDD_HIST);
		}
		
	}
	
	/**
     * Selects item and shows pop-up dialog to choose quantity of selected goods
     * @param v - In this param system puts pointer to selected View, used to apply selection
     * @param position - In this param system puts index of selected order, used to set {@value} listPosition
     * @param id - In this param system puts id of selected View
     */	
	public void onLongListItemClick(View v, int position, long id)
	{	
		// set selected position used to save and restore state
		selectedPosition = position;
		
		
		// Starts dialog that allows user to modify already ordered goods in list on display  
		showDialog(IDD_HIST);
	}
	
    /**
     * Method that starts after activity was paused
     */
	@Override
	public void onResume()
	{   
		super.onResume();
		
		// If activity was interrupted, then we have to 
		// update displaying data on resume
		updateList();
	}
	
	/**
	 * Runs when dialog called to show first time
	 * @param id constant identifier of kind of dialog
	 * @return pointer to Dialog that has been created
	 */	
	@Override
	protected Dialog onCreateDialog(int id)
	{
		// Set temporary variable CurrentLine to selected line (goods item)
		// for future modifying
		CurrentLine = mOrderH.lines.get(selectedPosition);
		
		idx_rest = mOrderH.isInRests(CurrentLine.get(OrderLines.GOODS_ID));
		
		CurrentRestsLine = null;
		
		if (idx_rest >= 0)           
		{
			CurrentRestsLine = mOrderH.restslines.get(idx_rest);											
		}
		
		// Determining the type of dialog to show, in this Activity only one dialog
		switch(id)
		{	
			case IDD_COUNT:
			{
				/** Set my own view of dialog to display to layout variable. Uses dlg_goods.xml */
				LayoutInflater inflater = getLayoutInflater();
				View layout = inflater.inflate(R.layout.ekvator_dlg_goods, 
						(ViewGroup)findViewById(R.id.dlg_goods));
				
				/** Binding xml elements of my own layout to final variables */
				final TextView txtName 		= (TextView)layout.findViewById(R.id.txtName_dlgGoods);
				final TextView txtId 		= (TextView)layout.findViewById(R.id.txtId_dlgGoods);
				final TextView txtPrice 	= (TextView)layout.findViewById(R.id.txtPrice_dlgGoods);
				final TextView txtStopPrice = (TextView)layout.findViewById(R.id.txtStopPrice_dlgGoods);
				final TextView txtWeight 	= (TextView)layout.findViewById(R.id.txtWeight_dlgGoods);
				final TextView txtRest 		= (TextView)layout.findViewById(R.id.txtRest_dlgGoods);
				final TextView txtCount 	= (TextView)layout.findViewById(R.id.txtCount_dlgGoods);
				final TextView txtTotal 	= (TextView)layout.findViewById(R.id.txtTotal_dlgGoods);
				final TextView txtCurrency 	= (TextView)layout.findViewById(R.id.txtCurrency_dlgGoods);
				final EditText edtQty 		= (EditText)layout.findViewById(R.id.edtQty);
				final EditText edtCustPrice	= (EditText)layout.findViewById(R.id.edtCustomPrice);
				final CheckBox chkPack		= (CheckBox)layout.findViewById(R.id.chkPack);
						
				SharedPreferences screen_prefs = getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE);
				/** Get array from res 0 - "Price 1"; 1 - "Price 2" */
				String[] arrayPrices = getResources().getStringArray(R.array.Prices);
				String price = screen_prefs.getString(RsaDb.PRICESELECTED, "xxx");
				// find price index
				int i=0;
				for (i=0;i<arrayPrices.length;i++)
				{
					if (arrayPrices[i].equals(price))
						break;
				}
				mContent[4] = "PRICE" + Integer.toString(i+1);
				
				// Get data of selected item from database to mCursor by call query:
				// SELECT TABLE_GOODS FROM mContent WHERE GOODS_ID = GOODS_ID(from CurrentLine)
				mCursor = db.query(RsaDbHelper.TABLE_GOODS, mContent, 
						RsaDbHelper.GOODS_ID + "='"
						+ CurrentLine.get(OrderLines.GOODS_ID)+"'", 
						null, null, null, null);
				
				// init mCursor to work with it
				// Romka 19.12.2012
				if (verOS<3) startManagingCursor(mCursor);
				
				// move to first record in mCursor
		        mCursor.moveToFirst();
				
		        txtCurrency.setText(currency);
		        // Sets GOODS_NAME of selected item to special element on dialog view
				txtName.setText(mCursor.getString(3));
				// Sets GOODS_ID of selected item to special element on dialog view
				txtId.setText(getResources().getString(R.string.goods_code) + mCursor.getString(1));
				// Sets LINES_PRICEWNDS of selected item to special element on dialog view
				float pr = Float.parseFloat(mCursor.getString(4));
				float nd = Float.parseFloat(mCursor.getString(8)) + 1;
				float tpr = pr * nd;
				txtPrice.setText(getResources().getString(R.string.goods_writeprice) 
						+ String.format("%.2f", tpr).replace(',', '.') 
						+ currency);
				
				float s_pr = 0;
				try {
					s_pr = Float.parseFloat(mCursor.getString(10));
				} catch (Exception f) {
					s_pr = 0;
				}
				txtStopPrice.setText(String.format("%.2f", s_pr).replace(',', '.'));
				
				
				edtCustPrice.setText(CurrentLine.get(OrderLines.PRICEWNDS));
				
				// Sets GOODS_WEIGHT1 of selected item to special element on dialog view
				txtWeight.setText(getResources().getString(R.string.goods_writeweight) 
						+ mCursor.getString(5) 
						+ getResources().getString(R.string.goods_weight_curency));
				// Sets GOODS_REST of selected item to special element on dialog view
				txtRest.setText(getResources().getString(R.string.goods_writerest) 
						+ mCursor.getString(6) 
						+ getResources().getString(R.string.goods_qty));
				
				String un = CurrentLine.get(OrderLines.UN);
				if (un.equals("qq")) {
					chkPack.setChecked(true);
				} else {
					chkPack.setChecked(false);
				}
				
				// Sets LINES_QTY of selected item to special element on dialog view
				txtCount.setText(CurrentLine.get(OrderLines.QTY));
				// Sets LINES_SUMWNDS of selected item to special element on dialog view
				txtTotal.setText(CurrentLine.get(OrderLines.SUMWNDS));
				// maxQty = (int)Float.parseFloat(mCursor.getString(6));
				edtQty.setText(CurrentLine.get(OrderLines.QTY));
				
				/** Listener for trying to change QTY of selected item in Order */
				edtQty.addTextChangedListener(new TextWatcher(){
					@Override
					public void afterTextChanged(Editable arg0)
					{
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
							// Do nothing
					}
	
					@Override
					public void onTextChanged(CharSequence arg0, int arg1,
								int arg2, int arg3)
					{
						long tmpQTY=0;
						
						// If EditText = "" then QTY = 0
						if (edtQty.getText().toString().equals("")) {
							tmpQTY = 0;
						} else {
							tmpQTY = Long.parseLong(edtQty.getText().toString());
						}
						
						int qt = 1;
						if (chkPack.isChecked() == true) {
							try {
								qt = Integer.parseInt(mCursor.getString(2));
							} catch (Exception e) {
								qt = 1;
							}
						}
						
						// Set special element QTY on dialog view same as edtQty 
						txtCount.setText(Long.toString(tmpQTY*qt));	
						// Set special element SUM on dialog view same as edtQty
						// SUM = QTY * GOODS_COEFF * GOODS_PRICE1
						String strPr = edtCustPrice.getText().toString();
						float tpr = 0;
						try {
							tpr = (strPr.length()>0)?Float.parseFloat(strPr):0;
						} catch (Exception d) {
							tpr = 0;
						}
						txtTotal.setText(String.format("%.2f", qt*tmpQTY * Float.parseFloat(mCursor.getString(7)) * tpr ).replace(',', '.'));

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
						if (edtQty.getText().toString().equals("")) {
							tmpQTY = 0;
						} else {
							tmpQTY = Long.parseLong(edtQty.getText().toString());
						}
						int qt = 1;
						if (chkPack.isChecked() == true) {
							try {
								qt = Integer.parseInt(mCursor.getString(2));
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
						txtTotal.setText(String.format("%.2f", qt*tmpQTY * Float.parseFloat(mCursor.getString(7)) * tpr ).replace(',', '.'));
					}
				});
				
				edtQty.setOnLongClickListener(new OnLongClickListener() {
					@Override
					public boolean onLongClick(View arg0)
					{
						// Make return TRUE to perform select all operation
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
						
						// If EditText = "" then QTY = 0
						if (edtQty.getText().toString().equals("")) {
							tmpQTY = 0;
						} else {
							tmpQTY = Long.parseLong(edtQty.getText().toString());
						}
						
						int qt = 1;
						if (chkPack.isChecked() == true) {
							try {
								qt = Integer.parseInt(mCursor.getString(2));
							} catch (Exception e) {
								qt = 1;
							}
						}
						
						// Set special element QTY on dialog view same as edtQty 
						txtCount.setText(Long.toString(tmpQTY*qt));	
						// Set special element SUM on dialog view same as edtQty
						// SUM = QTY * GOODS_COEFF * GOODS_PRICE1
						String strPr = edtCustPrice.getText().toString();
						float tpr = 0;
						try {
							tpr = (strPr.length()>0)?Float.parseFloat(strPr):0;
						} catch (Exception d) {
							tpr = 0;
						}
						txtTotal.setText(String.format("%.2f", qt*tmpQTY * Float.parseFloat(mCursor.getString(7)) * tpr ).replace(',', '.'));
					}
				});
				
				
				/** Building dialog view with my own xml view */
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setView(layout);
				// builder.setMessage(R.string.goods_count);
				
				/** 
				 * Listener for OK button, that makes order of selected item with 
				 * user-defined QTY and SUM. All data saving to special class holder
				 * mOrderH
				 */
				builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// If user changing QTY not equals 0 then...
						if ((!edtQty.getText().toString().equals("0"))&&(!edtQty.getText().toString().equals("")))
						{
							int qt = 1;
							String un = mCursor.getString(9);
							if (chkPack.isChecked() == true) {
								un = "qq";
								try {
									qt = Integer.parseInt(mCursor.getString(2));
								} catch (Exception e) {
									qt = 1;
								}
							}
							
							qt = qt * Integer.parseInt(edtQty.getText().toString());
							
							/** Temporary variable of SUM with NDS to store some data before saving it */
							float stopP = 0;
							try {
								stopP = Float.parseFloat(txtStopPrice.getText().toString());
							} catch(Exception g) {
								stopP = 0;
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
							
							float tmpSumwnds = qt * Float.parseFloat(mCursor.getString(7)) * tpr;
							float nd = tmpSumwnds/6;
							float prnd = tpr/6;
							
							// Save data of QTY, SUMWNDS, SUMWONDS of selected item to special class holder mOrderH
							CurrentLine.put(OrderLines.UN,			un);
							CurrentLine.put(OrderLines.QTY,			Integer.toString(qt));			
							CurrentLine.put(OrderLines.SUMWNDS,		String.format("%.2f",tmpSumwnds).replace(',', '.'));
							CurrentLine.put(OrderLines.SUMWONDS,	String.format("%.2f",tmpSumwnds - nd).replace(',', '.'));
							CurrentLine.put(OrderLines.PRICEWNDS,	String.format("%.2f",tpr).replace(',', '.'));
							CurrentLine.put(OrderLines.PRICEWONDS,	String.format("%.2f",tpr - prnd).replace(',', '.'));
							CurrentLine.put(OrderLines.NDS,			String.format("%.2f",nd).replace(',', '.'));
							
							
							
							// Refresh displayed goods with new data saved in class holder mOrderH 
							updateList();
						}
						// If user set QTY of selected item to 0 then show him popup-message
						// with warning about wrong QTY
						else
						{
							Toast.makeText(getApplicationContext(), R.string.goods_qty_notcorrect, Toast.LENGTH_LONG).show();
						}
						
						// Ticket 2: Hide soft keyboard because Button CANCEL was pressed
				        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				        // Ticket 2: Hide soft keyboard because Button CANCEL was pressed
				        imm.hideSoftInputFromWindow(edtQty.getWindowToken(), 0);
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
				 
				// Return with dialog creationg
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
				}});
				
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
						if (edtRest.getText().toString().equals(""))
						{
							edtRest.setText("0");
						}
						else
						{
							long j = Long.parseLong(edtRest.getText().toString());
							edtRest.setText(Long.toString(j));
						}						
						
						// if not ordered earlier and != ""
						if (idx_rest<0)   
						{
							// Adding selected item to restslist
							mOrderH.restslines.add(new OrderRests(			"0", 											// *id not used
																			"",												// *zakaz_id used in headactivity
																			CurrentLine.get(OrderLines.GOODS_ID),			// goods_id
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
						
						/////////////////////////////////////////
						// if MERCH ONLY activated
						if (merch_only==true) {
							CurrentLine.put(OrderLines.QTY,			edtRest.getText().toString());			
							CurrentLine.put(OrderLines.RESTCUST,	edtRest.getText().toString());
						}
						//end of MERCH Addon
						//////////////////////////////////
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
		// Set temporary variable CurrentLine to selected line (goods item)
		// for future modifying
		CurrentLine = mOrderH.lines.get(selectedPosition);
		
		idx_rest = mOrderH.isInRests(CurrentLine.get(OrderLines.GOODS_ID));
		
		CurrentRestsLine = null;
		
		if (idx_rest >= 0)           
		{
			CurrentRestsLine = mOrderH.restslines.get(idx_rest);											
		}
		
		// Determining the type of dialog to show, in this Activity only one dialog
		switch(id)
		{	
			case IDD_COUNT:
			{
				/** Binding xml elements of my own layout to final variables */
				final TextView txtName 		= (TextView)dialog.findViewById(R.id.txtName_dlgGoods);
				final TextView txtId 		= (TextView)dialog.findViewById(R.id.txtId_dlgGoods);
				final TextView txtPrice 	= (TextView)dialog.findViewById(R.id.txtPrice_dlgGoods);
				final TextView txtStopPrice = (TextView)dialog.findViewById(R.id.txtStopPrice_dlgGoods);
				final TextView txtWeight 	= (TextView)dialog.findViewById(R.id.txtWeight_dlgGoods);
				final TextView txtRest 		= (TextView)dialog.findViewById(R.id.txtRest_dlgGoods);
				final TextView txtCount 	= (TextView)dialog.findViewById(R.id.txtCount_dlgGoods);
				final TextView txtTotal 	= (TextView)dialog.findViewById(R.id.txtTotal_dlgGoods);
			//	final SeekBar skbCount 		= (SeekBar)dialog.findViewById(R.id.skbCount_dlgGoods2);
				final EditText edtQty 		= (EditText)dialog.findViewById(R.id.edtQty);
				final EditText edtCustPrice	= (EditText)dialog.findViewById(R.id.edtCustomPrice);
				final CheckBox chkPack		= (CheckBox)dialog.findViewById(R.id.chkPack);
				
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
				
				SharedPreferences screen_prefs = getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE);
				/** Get array from res 0 - "Price 1"; 1 - "Price 2" */
				String[] arrayPrices = getResources().getStringArray(R.array.Prices);
				String price = screen_prefs.getString(RsaDb.PRICESELECTED, "xxx");
				// find price index
				int i=0;
				for (i=0;i<arrayPrices.length;i++)
				{
					if (arrayPrices[i].equals(price))
						break;
				}
				mContent[4] = "PRICE" + Integer.toString(i+1);
				
				// Get data of selected item from database to mCursor by call query:
				// SELECT TABLE_GOODS FROM mContent WHERE GOODS_ID = GOODS_ID(from CurrentLine)
				mCursor = db.query(RsaDbHelper.TABLE_GOODS, mContent, 
						RsaDbHelper.GOODS_ID + "='"
						+ CurrentLine.get(OrderLines.GOODS_ID)+"'", 
						null, null, null, null);
				
				// init mCursor to work with it
				// Romka 19.12.2012
				if (verOS<3) startManagingCursor(mCursor);
				
				// move to first record in mCursor
		        mCursor.moveToFirst();
				
		        // Sets GOODS_NAME of selected item to special element on dialog view
				txtName.setText(mCursor.getString(3));
				// Sets GOODS_ID of selected item to special element on dialog view
				txtId.setText(getResources().getString(R.string.goods_code) + mCursor.getString(1));
				// Sets LINES_PRICEWNDS of selected item to special element on dialog view
				float pr = Float.parseFloat(mCursor.getString(4));
				float nd = Float.parseFloat(mCursor.getString(8)) + 1;
				float tpr = pr * nd;
				txtPrice.setText(getResources().getString(R.string.goods_writeprice) 
						+ String.format("%.2f", tpr).replace(',', '.') 
						+ currency);
				float s_pr = 0;
				try {
					s_pr = Float.parseFloat(mCursor.getString(10));
				} catch (Exception f) {
					s_pr = 0;
				}
				txtStopPrice.setText(String.format("%.2f", s_pr).replace(',', '.'));
				
				edtCustPrice.setText(CurrentLine.get(OrderLines.PRICEWNDS));
				
				// Sets GOODS_WEIGHT1 of selected item to special element on dialog view
				txtWeight.setText(getResources().getString(R.string.goods_writeweight) 
						+ mCursor.getString(5) 
						+ getResources().getString(R.string.goods_weight_curency));
				// Sets GOODS_REST of selected item to special element on dialog view
				txtRest.setText(getResources().getString(R.string.goods_writerest) 
						+ mCursor.getString(6) 
						+ getResources().getString(R.string.goods_qty));
				// Sets LINES_QTY of selected item to special element on dialog view
				
				String un = CurrentLine.get(OrderLines.UN);
				int qt = 1;
				if (un.equals("qq")) {
					chkPack.setChecked(true);
					try {
						qt = Integer.parseInt(mCursor.getString(2));
					} catch (Exception e) {
						qt = 1;
					}
				} else {
					chkPack.setChecked(false);
				}
				qt = Integer.parseInt(CurrentLine.get(OrderLines.QTY)) / qt;
				
				txtCount.setText(CurrentLine.get(OrderLines.QTY));
				// Sets LINES_SUMWNDS of selected item to special element on dialog view
				txtTotal.setText(CurrentLine.get(OrderLines.SUMWNDS));
				// maxQty = (int)Float.parseFloat(mCursor.getString(6));
				edtQty.setText(Integer.toString(qt));
			}
			break;
			
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
				tblHistory = mOrderH.prepareHistoryTable(db_orders, CurrentLine.get(OrderLines.GOODS_ID));
				
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
							curRecomend.setText(mOrderH.calculateRecomendOrder(CurrentLine.get(OrderLines.GOODS_ID), 
																					tblHistory, 
																					edtRest.getText().toString()));
						}
						else
						{
							curRecomend.setText(mOrderH.calculateRecomendOrder(CurrentLine.get(OrderLines.GOODS_ID), 
																					tblHistory, 
																					"0"));
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
				curRecomend.setText(mOrderH.calculateRecomendOrder(	CurrentLine.get(OrderLines.GOODS_ID), 
																		tblHistory, 
																		edtRest.getText().toString()));
				
				
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
		//if (isPad == false) {
		//	dialog.getWindow().setGravity(Gravity.TOP);
		//} else {
			dialog.getWindow().setGravity(Gravity.BOTTOM);
		//}
			
	} 
	
	/**
	 * If Back-button on device pressed then do...
	 */
	@Override
	public void onBackPressed()
	{
		/** Used for prepearing data for another activity and send some data to it */
		Intent intent = new Intent();

    	// Put data of current order to intent for using in next activity(LinesActivity.class)
    	intent.putExtra("ORDERH", mOrderH);
    	
    	// Set result of lines additting is OK and send it's data to LinesActivity.class
    	setResult(RESULT_OK, intent);
    	
    	finish();
	}
	
}
