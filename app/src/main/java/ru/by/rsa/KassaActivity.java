package ru.by.rsa;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Currency;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnShowListener;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;

/**
 * Activity that allows user view debit
 * @author Komarev Roman
 * Odessa, neo3da@mail.ru, +380503412392 
 */
public class KassaActivity extends ListActivity
{
	/** Is set of data from DB to be shown in ListView. To get value have to use mCurosr.getString("KEY") */
	private Cursor mCursor;

	/** To get value have to use mCurosrKassa.getString("KEY") */
	private Cursor mCursorKassa;
	
	/** Arrays of columns that will be used to obtain data from DB-tables in columns with the same name */
	private String[] mContent = {"_id", RsaDbHelper.DEBIT_CUST_ID, RsaDbHelper.CUST_NAME, "S", RsaDbHelper.CUST_OKPO};
	
	/** Arrays of columns that will be used to obtain data from DB-tables in columns with the same name */
	private String[] mContentKassa = {"_id", RsaDbHelper.KASSA_CUST_ID, RsaDbHelper.KASSA_DATE, RsaDbHelper.KASSA_HSUMO};
	
	/** Special adapter that must be used between mCursor and combobox of customer selection */
	private SimpleCursorAdapter mAdapter;
	
	/** Open SQLite database that stores all data */
	private static SQLiteDatabase db;
	
	/** Open SQLite database that stores all data */
	private static SQLiteDatabase db_kassa;
	
	/** Current theme */
	private boolean lightTheme;
	private String currency;
	
	/** Constant to identify kind of dialog when dialog window will be called, dialog for remark typing */
	private final static int IDD_INCOME = 1;
	
	private EditText filterText = null;
	private float strTotalDebts = 0;
	
	private  String curCUST_ID;
    private  String curCUST_TEXT;
    private  String curDebit;
    
    private static int verOS = 0;
    
    private boolean isPad;
    
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
    	verOS = 2;
		try
		{
			verOS = Integer.parseInt(Build.VERSION.RELEASE.substring(0,1));
		}
		catch (Exception e) {};
		
		isPad = false;
		lightTheme = getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE).getBoolean(RsaDb.LIGHTTHEMEKEY, false);
		SharedPreferences def_prefs = PreferenceManager.getDefaultSharedPreferences(this);
		currency = " "+def_prefs.getString("prefCurrency", getResources().getString(R.string.preferences_currency_summary));
		
		if (lightTheme)
		{
			setTheme(R.style.Theme_Custom);
			super.onCreate(savedInstanceState);
			setContentView(R.layout.l_kassa);
		}
		else
		{
			setTheme(R.style.Theme_CustomBlack2);
			super.onCreate(savedInstanceState);
			setContentView(R.layout.kassa);
		}
		
		// Filter
		filterText = (EditText) findViewById(R.id.kassa_edit);
		// Filter
		filterText.addTextChangedListener(filterTextWatcher);
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
    
    @Override
    public void onStart()
    {
    	super.onStart();
    	
    	
    	isPad = RsaDb.checkScreenSize(this, 5);
    	
	    // Button when user wants to go back to orders list, by pressing
        // it current activity going down, and previous activity
        // starts (RSAActivity)
        Button btnBack = (Button)findViewById(R.id.kassa_pbtn_prev);
        
        // Listener for OK-button click, calls when OK-button clicked
        btnBack.setOnClickListener(new OnClickListener() 
        {
			@Override
			public void onClick(View v) {
		    	
				Intent intent = new Intent();
				
				setResult(RESULT_OK, intent);
				
		    	finish();
			}				
		});
        
        /** Init database with architecture that designed in RsaDbHelper.class */
		RsaDbHelper mDb_kassa = new RsaDbHelper(this, RsaDbHelper.DB_ORDERS);
		
		/** Get Shared Preferences */
		SharedPreferences prefs = getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);
		/** Init database with architecture that designed in RsaDbHelper.class */
		RsaDbHelper mDb = new RsaDbHelper(this, prefs.getString(RsaDb.ACTUALDBKEY, RsaDbHelper.DB_NAME1));
		
		
		// Open SQLite database that stores all data
		db_kassa = mDb_kassa.getWritableDatabase();
		// Open SQLite database that stores all data
		db = mDb.getReadableDatabase();
    	
    	updateList();
    }
    
    /**
     * Fills combobox and display with data
     */
    private void updateList()
    {
    	/** Get Shared Preferences */
	//	SharedPreferences prefs = getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);
		
	//	/** Init database with architecture that designed in RsaDbHelper.class */
	//	RsaDbHelper mDb = new RsaDbHelper(this, prefs.getString(RsaDb.ACTUALDBKEY, RsaDbHelper.DB_NAME1));
		try {
			String q = "SELECT SUM(REPLACE(_debit.SUM,' ', '')) FROM _debit";
			Cursor tc = db.rawQuery(q, null);
			if (tc.getCount()>0) {
				tc.moveToFirst();
				strTotalDebts = tc.getFloat(0);
			}
			tc.close();
		} catch (Exception e) {}
    	
    	
		
    //    String MY_QUERY = 	"SELECT _cust._id, _cust.NAME, _cust.ID " +
    //    					"FROM _cust " +
    //   					"WHERE _cust.NAME like '%" + filterText.getText() + "%' " +
    //    					"ORDER BY _cust.NAME";
		String MY_QUERY = 	"SELECT _debit._id, _debit.CUST_ID, _cust.NAME, SUM(REPLACE(_debit.SUM,' ', '')) AS S, _cust.OKPO " +
			    			"FROM _debit " +
			    			"INNER JOIN _cust " +
			    			"ON _debit.CUST_ID=_cust.ID " +
			    			"WHERE (_cust.NAME like '%" + filterText.getText() + "%') " +
			    			"GROUP BY _debit.CUST_ID " +
			    			"ORDER BY _cust.NAME";
        
        
		if (mCursor != null) 
	    {
	        mCursor.close();
	    }
		
		// Get data from database to mCursor by call query:
		// SELECT TABLE_CUST FROM mContent ORDERBY CUST_NAME
        mCursor = db.rawQuery(MY_QUERY, null);
		// Init mCursor to work with it
        //	19.12.2012 Romka 
        if (verOS<3) startManagingCursor(mCursor); 
		
		// Move to first record in mCursor
        mCursor.moveToFirst();
        
        // Init mAdapter with binding data in mCursor with values customer combobox
        mAdapter = new SimpleCursorAdapter(this, lightTheme?R.layout.l_list_kassa:R.layout.list_kassa, 
        									mCursor, mContent, 
        									new int [] {R.id.txtCurrency_kassa, 0, R.id.txtName_kassa, R.id.txtDebit_kassa, R.id.txtCash_kassa});
        final Calendar c = Calendar.getInstance();
        final String curDate =  String.format( "%04d-%02d-%02d", c.get(Calendar.YEAR), c.get(Calendar.MONTH)+1, c.get(Calendar.DAY_OF_MONTH) );
        SharedPreferences def_prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		final boolean isAutocorrect = def_prefs.getBoolean("prefDebitAutocorrection", false);
		
        mAdapter.setViewBinder(new ViewBinder() 
        {
            public boolean setViewValue(View aView, Cursor aCursor, int aColumnIndex) 
            {
            	if (aColumnIndex == 0) 
                {
            		((TextView) aView).setText(currency);
            		return true;
                }
            	if (aColumnIndex == 3) 
                {
            		String result = aCursor.getString(3);
            		float deb = Float.parseFloat(result);
            		float m = 0;
            		
            		if (isAutocorrect) {
	            		String chkQuery =	 "SELECT " + RsaDbHelper.KASSA_HSUMO 
			      				   		  + " FROM " + RsaDbHelper.TABLE_KASSA
			      				   		  + " WHERE (" + RsaDbHelper.KASSA_CUST_ID
			      				   		  + "='" + aCursor.getString(1) + "')"
			      				   		  + " AND (_kassa.DATE='" + curDate + "')";
	            		
	            		Cursor cc = db_kassa.rawQuery(chkQuery, null);
	            		if (cc.moveToFirst()) {
	            			String money = cc.getString(0);
	            			m = Float.parseFloat(money.equals("")?"0":money);
	            		}
	            		if (cc!=null && !cc.isClosed()) {
	            			cc.close();
	            		}
	            		deb = deb - m;
	            		result = Float.toString(deb);
            		}    		
            		           		
            		((TextView) aView).setText(result);
            		return true;
                }
                if (aColumnIndex == 4) 
                {		
                	  TextView txtCash = (TextView) aView; 
                	  String chkQuery = 		 "SELECT " + RsaDbHelper.KASSA_HSUMO + ", " + RsaDbHelper.KASSA_DATE
								      				   + " FROM " + RsaDbHelper.TABLE_KASSA
								      				   + " WHERE (" + RsaDbHelper.KASSA_CUST_ID
								      				   + "='" + aCursor.getString(1) + "')"
								      				   + " AND (_kassa.DATE='" + curDate + "')";
                	  
				      mCursorKassa = db_kassa.rawQuery(chkQuery, null);
				      // init mCursorKassa to work with it
				      //	19.12.2012 Romka 
				      if (verOS<3) startManagingCursor(mCursorKassa);
				      
				      if (mCursorKassa.getCount()>0)
				      {
				    	  mCursorKassa.moveToFirst();
				    	  
				    	  if(mCursorKassa.getString(0).equals("")||mCursorKassa.getString(0).equals("0"))
				    	  {
				    		  txtCash.setText("");
				    		  return true;
				          }
				    		  
				    	  
				    	  txtCash.setText(getResources().getString(R.string.kassa_income) 
				    			  				+ mCursorKassa.getString(0)
				    			  				+ currency);
				    	  txtCash.setTextSize(14);
				    	  txtCash.setTypeface(null,Typeface.BOLD);
				    	  return true;
				      }
				      
				      txtCash.setText("");
				      if (mCursorKassa != null) 
		    	      {
		    	        mCursorKassa.close();
		    	      }
                      return true;
                }
                return false;
            }
        });
        
        // Set mAdapter to KassaActivty data adapter to show data from mCursor on device display
        setListAdapter(mAdapter);    
        
        TextView txtTotalSum = (TextView)findViewById(R.id.kassa_txtTotalSum_text);
        float incomeSum = 0;
        Cursor mCursorSum = null;
        
        try 
        {
	        String sumQuery =    "SELECT " + RsaDbHelper.KASSA_HSUMO + ", " + RsaDbHelper.KASSA_DATE
							   + " FROM " + RsaDbHelper.TABLE_KASSA
							   + " WHERE (_kassa.DATE='" + curDate + "')" 
							   + "AND (_kassa.HSUMO <> '')";
	        mCursorSum = db_kassa.rawQuery(sumQuery, null);
	
	        if (mCursorSum.getCount() > 0)
	        {
	        	//19.12.2012 Romka 
	        	if (verOS<3) startManagingCursor(mCursorSum);
	        	mCursorSum.moveToFirst();
	        	for (int i=0;i<mCursorSum.getCount();i++)
	        	{
	        		incomeSum += Float.parseFloat(mCursorSum.getString(0));
	        		mCursorSum.moveToNext();
	        	}
	        }
        }
        catch (Exception e) {Toast.makeText(getApplicationContext(),"Exception: Sum calculating",Toast.LENGTH_LONG).show();}
		txtTotalSum.setText(String.format("%.2f",incomeSum).replace(',', '.') +"("+String.format("%.2f",strTotalDebts).replace(',', '.')+")");
		if (mCursorSum != null) 
	    {
	        mCursorSum.close();
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
    	      
    	      if (this.mAdapter !=null)
    	      {
    	        ((CursorAdapter) this.mAdapter).getCursor().close();
    	        this.mAdapter= null;
    	      }

    	      if (this.mCursor != null) 
    	      {
    	        this.mCursor.close();
    	      }

    	      if (this.mCursorKassa != null) 
    	      {
    	        this.mCursorKassa.close();
    	      }
    	} 
    	catch (Exception error) 
    	{
    	      /** Error Handler Code **/
    	}
		
	    // Close database after reading
		if (db != null) 
		{
		   	db.close();
		}
		// Close database after reading
		if (db_kassa != null) 
		{
		   	db_kassa.close();
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

    	// Set result of lines additting is OK and send it's data to LinesActivity.class
    	setResult(RESULT_OK, intent);
    	
    	finish();
	}
	
	/**
	 * Runs when dialog called to show first time
	 * @param id constant identifier of kind of dialog
	 * @return pointer to Dialog that has been created
	 */	
	@Override
	protected Dialog onCreateDialog(int id)
	{
		// Determining the type of dialog to show, in this Activity only one dialog
		switch(id)
		{	
			case IDD_INCOME:
			{
				/** Set my own view of dialog to display to layout variable. Uses dlg_head.xml */
				LayoutInflater inflater = getLayoutInflater();
				View layout = inflater.inflate(R.layout.dlg_head, 
						(ViewGroup)findViewById(R.id.linear_dlg_head));
				
				/** Binding xml element of my own layout to final variable */ 
				final EditText edtDsc = (EditText)layout.findViewById(R.id.edtRemark_head);
				edtDsc.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
				
				int maxLength = 9;
				InputFilter[] FilterArray = new InputFilter[1];
				FilterArray[0] = new InputFilter.LengthFilter(maxLength);
				edtDsc.setFilters(FilterArray);
				
				edtDsc.setText("");
				
				edtDsc.setOnLongClickListener(new OnLongClickListener() {
					@Override
					public boolean onLongClick(View arg0)
					{
						return false;
					}});
				edtDsc.setOnTouchListener(new OnTouchListener(){
					@Override
					public boolean onTouch(View arg0, MotionEvent arg1)
					{
						return false;
					}});
				
				 /** Building dialog view with my own xml view */
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setView(layout);
				/** 
				 * Listener for OK button, that makes orders remark same with dialog edit-field 
				 */
				builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						SharedPreferences def_prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
						boolean isAutocorrect = def_prefs.getBoolean("prefDebitAutocorrection", false);
						/** Used as data storage before save it to database */
						ContentValues values = new ContentValues();
						/** Get the current date */
						final Calendar c = Calendar.getInstance();
				        String curDate =  String.format( "%04d-%02d-%02d", c.get(Calendar.YEAR), c.get(Calendar.MONTH)+1, c.get(Calendar.DAY_OF_MONTH) );
				        SimpleDateFormat fmtTime = new SimpleDateFormat("HH:mm:ss");
				        String time = fmtTime.format(c.getTime());
				        String cash = edtDsc.getText().toString();
				        
				        if (isAutocorrect) { 
				        	float money = Float.parseFloat(cash.equals("")?"0":cash);
				        	float debt = Float.parseFloat(curDebit.equals("")?"0":curDebit);
				        	
				        	if (money>debt && debt>0) {
				        		cash = curDebit;
				        		Toast.makeText(getApplicationContext(), "Автокоррекция суммы", Toast.LENGTH_SHORT).show();
				        	}
				        	
				        }
				        
				        values.put(RsaDbHelper.KASSA_ZAKAZ_ID, curCUST_ID + "@" + curDate);
				        values.put(RsaDbHelper.KASSA_CUST_ID, curCUST_ID);
				        values.put(RsaDbHelper.KASSA_CUST_TEXT, curCUST_TEXT);
				        values.put(RsaDbHelper.KASSA_DATE, curDate);
				        values.put(RsaDbHelper.KASSA_HSUMO, cash);
				        values.put(RsaDbHelper.KASSA_TIME, time);
				        
				        String chkQuery = 		 "SELECT " + RsaDbHelper.KASSA_CUST_ID
				        					  + " FROM " + RsaDbHelper.TABLE_KASSA
				        					  + " WHERE " + RsaDbHelper.KASSA_ZAKAZ_ID
				        					  + "='" + curCUST_ID + "@" + curDate + "'";
				        
				        mCursorKassa = db_kassa.rawQuery(chkQuery, null);
				        // init mCursorKassa to work with it
				        //19.12.2012 Romka 
				        if (verOS<3) startManagingCursor(mCursorKassa);
				        
				        if (mCursorKassa.getCount()>0)
				        {
				        	db_kassa.update( RsaDbHelper.TABLE_KASSA, 
				        					 values, 
				        					 RsaDbHelper.KASSA_ZAKAZ_ID + "='" + curCUST_ID + "@" + curDate +"'", 
				        					 null);
				        }
				        else
				        {
				        	db_kassa.insert(RsaDbHelper.TABLE_KASSA, null, values);
				        }
				        
				        values.clear();
				        if (mCursorKassa != null) 
			    	    {
			    	        mCursorKassa.close();
			    	    }
				        
				        updateList();
				        // Toast.makeText(getApplicationContext(), mCursor.getString(1), 5000).show();
						// Ticket 2: Hide soft keyboard because Button CANCEL was pressed
				        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				        // Ticket 2: Hide soft keyboard because Button CANCEL was pressed
				        imm.hideSoftInputFromWindow(edtDsc.getWindowToken(), 0);
					}
				});
				
				/** Listener for Cancel button, that means make no changes with remark and button stat */
				builder.setNegativeButton(R.string.head_dlg_cancel, new DialogInterface.OnClickListener() {					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// Ticket 2: Hide soft keyboard because Button CANCEL was pressed
				        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				        // Ticket 2: Hide soft keyboard because Button CANCEL was pressed
				        imm.hideSoftInputFromWindow(edtDsc.getWindowToken(), 0);
					}
				});
				
				// Sets Dialog is not cancelable by pressing Back-button on device
				builder.setCancelable(false);
				
				// Return with dialog creationg
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
		// Determining the type of dialog to show, in this Activity only one dialog
		switch(id)
		{	
			case IDD_INCOME:
			{
				/** Binding xml element of remark edit */
				final EditText edtDsc = (EditText)dialog.findViewById(R.id.edtRemark_head);
			    edtDsc.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
				
				// Set text in edit on dialog view to order's current remark 
			    edtDsc.setText("");
			    
			    edtDsc.setFilters(new InputFilter[] {
		                new DigitsKeyListener(Boolean.FALSE, Boolean.TRUE) {
		                    int beforeDecimal = 6, afterDecimal = 2;

		                    @Override
		                    public CharSequence filter(CharSequence source, int start, int end,
		                            Spanned dest, int dstart, int dend) {
		                        String temp = edtDsc.getText() + source.toString();

		                        if (temp.equals(".")) {
		                            return "0.";
		                        }
		                        else if (temp.toString().indexOf(".") == -1) {
		                            // no decimal point placed yet
		                            if (temp.length() > beforeDecimal) {
		                                return "";
		                            }
		                        } else {
		                            temp = temp.substring(temp.indexOf(".") + 1);
		                            if (temp.length() > afterDecimal) {
		                                return "";
		                            }
		                        }

		                        return super.filter(source, start, end, dest, dstart, dend);
		                    }
		                }
		        });
			    
			    edtDsc.setOnLongClickListener(new OnLongClickListener() {
					@Override
					public boolean onLongClick(View arg0)
					{
						return false;
					}});
				edtDsc.setOnTouchListener(new OnTouchListener(){
					@Override
					public boolean onTouch(View arg0, MotionEvent arg1)
					{
						return false;
					}});
				dialog.setOnShowListener(new OnShowListener() {
				    @Override
				    public void onShow(DialogInterface dialog) {
				        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				        
				        // Ticket 2: 
				        imm.showSoftInput(edtDsc, InputMethodManager.SHOW_FORCED);
				        
				       // Ticket 2: Removed:
				       // imm.showSoftInput(edtRemark, InputMethodManager.SHOW_IMPLICIT);
				       // imm.toggleSoftInput(0, 1);
				        edtDsc.setSelection(0, edtDsc.getText().toString().length());
				    }
				});
			    
			} break;
			default:
				// Do nothing if another kind of dialog is selected
		}
		if (isPad == false) {
			dialog.getWindow().setGravity(Gravity.TOP);
		} else {
			dialog.getWindow().setGravity(Gravity.CENTER);
		}
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
		// Move to selected position in list of group
		mCursor.moveToPosition(position);
		curCUST_ID = mCursor.getString(1);
        curCUST_TEXT = mCursor.getString(2);
        curDebit = mCursor.getString(3);
        
		showDialog(IDD_INCOME);
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
   	   // Close database after reading
   		if (db != null) 
   		{
   		   	db.close();
   		}
   		// Close database after reading
   		if (db_kassa != null) 
   		{
   		   	db_kassa.close();
   		}
	   
	}
}