package ua.rsa.gd;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

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
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputFilter;
import android.text.Spanned;
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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import ua.rsa.gd.R;
import ua.rsa.gd.adapter.item.DebitItem;

/**
 * Activity that allows user view debit
 * @author Komarev Roman
 * Odessa, neo3da@mail.ru, +380503412392 
 */
public class DebitActivity extends ListActivity implements OnItemSelectedListener
{
	/** Is set of data from DB to be shown in ClientName-ComboBox. To get value have to use mCurosr.getString("KEY") */
	private Cursor mCursor;
	
	private Dialog dg;

	/** Is set of data from DB to be shown in ListView. To get value have to use mCurosr.getString("KEY") */
	private Cursor mCursorList;
	
	/** Arrays of columns that will be used to obtain data from DB-tables in columns with the same name */
	private String[] mContent = {"_id", RsaDbHelper.CUST_ID, RsaDbHelper.CUST_NAME};
	//private String[] mContentList2 = {"_id", RsaDbHelper.DEBIT_CUST_ID, RsaDbHelper.DEBIT_DATEDOC, 
	//										RsaDbHelper.DEBIT_RN, RsaDbHelper.DEBIT_SUM, RsaDbHelper.DEBIT_DATEPP,
	//										RsaDbHelper.DEBIT_CLOSED, RsaDbHelper.DEBIT_COMMENT, RsaDbHelper.DEBIT_SHOP_ID };
	
	/** Special adapter that must be used between mCursor and combobox of customer selection */
	private SimpleCursorAdapter mAdapter;
	
	private final static int IDD_INCOME = 1;
	
	/** Special adapter that must be used between mCursorList and ListView */
	private SimpleAdapter mAdapterList;
	
	EditText edtDsc;
	
	
	/** Open SQLite database that stores all data */
	SQLiteDatabase db;
	SQLiteDatabase db_orders;
	
	Spinner cmbCust;
	TextView txtTotal;
	TextView txtCashTotal;
	String currentCustID;
	int currentPosition;
	
	/** Designed class to store data of current order before save it to database */
	private OrderHead mOrderH = null;
	
	ArrayList<DebitItem> list;
	DebitItem currentItem;
	
	/** Current theme */
	private boolean lightTheme;
	private String currency;
	
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
			setContentView(R.layout.l_debit);
		}
		else
		{
			setTheme(R.style.Theme_CustomBlack2);
			super.onCreate(savedInstanceState);
			setContentView(R.layout.debit);
		}
		
		
		if (savedInstanceState != null) {
			Bundle extras	= savedInstanceState;
        	currentCustID	= extras.getString("CUST_ID");
        	currentPosition = extras.getInt("POSITION");
        	currentItem		= extras.getParcelable("CURITEM");
        	
        	String sklad_id = extras.getString("SKLADID");
        	if (sklad_id!=null) {
        		mOrderH = extras.getParcelable("ORDERH");
        	}
        	
        } else {
        	currentCustID = "";
        	currentPosition = 0;
        }
        /** Data from previous activity (HeadActivity.class) to extras variable */
		Bundle extras;
		// Get data from previous activity (HeadActivity.class) to extras variable */
		extras = getIntent().getExtras();
		if (extras != null)
		{
			// Init class to store data of current order before save it to database
			mOrderH = new OrderHead();
        
			// Get data of current order from back activity(HeadActivity.class) to this */
			mOrderH = extras.getParcelable("ORDERH");
		}
		
    }
    
    @Override
    public void onStart()
    {
    	super.onStart();
    	
    	isPad = RsaDb.checkScreenSize(this, 5);
    	
    	txtCashTotal = (TextView)findViewById(R.id.txtCash_debit);
    	txtCashTotal.setText("0.00");
    	
	    // Button when user wants to go back to orders list, by pressing
        // it current activity going down, and previous activity
        // starts (RSAActivity)
        Button btnBack = (Button)findViewById(R.id.debit_pbtn_prev);
        
        // Listener for OK-button click, calls when OK-button clicked
        btnBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
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
		});
    	
    	updateList();
    }
    
    /**
     * Fills combobox and display with data
     */
    private void updateList()
    {
    	/** Get Shared Preferences */
		SharedPreferences prefs = getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);
		
		/** Init database with architecture that designed in RsaDbHelper.class */
		RsaDbHelper mDb = new RsaDbHelper(this, prefs.getString(RsaDb.ACTUALDBKEY, RsaDbHelper.DB_NAME1));
		RsaDbHelper mDb_orders = new RsaDbHelper(this, RsaDbHelper.DB_ORDERS);
		
		// Open SQLite database that stores all data
		db = mDb.getWritableDatabase();
		db_orders = mDb_orders.getWritableDatabase();
		
        /** Binding xml elements of view to variables */
        cmbCust = (Spinner)findViewById(R.id.cmbDebit_debit);

        
		// Get data from database to mCursorList by call query:
		// SELECT TABLE_DEBIT FROM mContentList
	//	mCursorList = db.query(RsaDbHelper.TABLE_DEBIT, mContentList, null, null, null, null, null);

		// if debit is empty exit from function
	//	if (mCursorList.getCount()<1)
	//		return;
		
		// Init mCursor to work with it
		// //19.12.2012 Romka 
	//	if (verOS<3) startManagingCursor(mCursorList); 
		
		// Move to first record in mCursor
   //     mCursorList.moveToFirst();
        
        /*
        String MY_QUERY = 	"SELECT _cust._id, _cust.ID, _cust.NAME " +
        					"FROM _cust " +
        					"WHERE _cust.ID IN (SELECT _debit.CUST_ID FROM _debit) " +
        					"ORDER BY _cust.NAME";*/
        
        if (mOrderH!=null && mOrderH.cust_id!=null && mOrderH.shop_id!=null
				&& !mOrderH.cust_id.equals("-XXX") && !mOrderH.shop_id.equals("ZZZ")) {
	        String testQuery  = "select CUST_ID from _debit where CLOSED!='2' and CUST_ID='"+mOrderH.cust_id+"' limit 1";
	        Cursor testCursor = db.rawQuery(testQuery, null);
	        if (testCursor.getCount()<1) {
	        	ContentValues insertValues = new ContentValues();
	        	SimpleDateFormat formatNew = new SimpleDateFormat("yyyy-MM-dd");
	        	SimpleDateFormat formatOld = new SimpleDateFormat("dd.MM.yy");
				Calendar c = Calendar.getInstance();
				
				//insertValues.put("_id", null);
	        	insertValues.put(RsaDbHelper.DEBIT_ID, "new");
	        	insertValues.put(RsaDbHelper.DEBIT_CUST_ID, mOrderH.cust_id.toString());
	        	insertValues.put(RsaDbHelper.DEBIT_RN, "new");
	        	insertValues.put(RsaDbHelper.DEBIT_DATEDOC, formatOld.format(c.getTime()));
	        	insertValues.put(RsaDbHelper.DEBIT_SUM, "0");
	        	insertValues.put(RsaDbHelper.DEBIT_DATEPP, formatNew.format(c.getTime()));
	        	insertValues.put(RsaDbHelper.DEBIT_CLOSED, "0");
	        	insertValues.put(RsaDbHelper.DEBIT_COMMENT, "");
	        	insertValues.put(RsaDbHelper.DEBIT_SHOP_ID, mOrderH.shop_id.toString());
	        	insertValues.put(RsaDbHelper.DEBIT_PAYMENT, "");
	
	    		db.insert(RsaDbHelper.TABLE_DEBIT, RsaDbHelper.DEBIT_CUST_ID, insertValues);
	        }
        }
        
        String MY_QUERY = 	"SELECT _debit._id, _debit.CUST_ID as ID, _cust.NAME " +
							"FROM _debit " +
							"INNER JOIN _cust " +
							"ON _debit.CUST_ID = _cust.ID " +
							"WHERE CLOSED!='2' " +
							"GROUP BY _debit.CUST_ID " +
							"ORDER BY _cust.NAME";
        		
		// Get data from database to mCursor by call query:
		// SELECT TABLE_CUST FROM mContent ORDERBY CUST_NAME
		//mCursor = db.query(RsaDbHelper.TABLE_CUST, mContent, null, null, null, null, RsaDbHelper.CUST_NAME);
          mCursor = db.rawQuery(MY_QUERY, null);

          // if no cutomers to show then exit from function
         if (mCursor.getCount()<1)
        	 return;
         
         // Init mCursor to work with it
         // //19.12.2012 Romka 
         if (verOS<3) startManagingCursor(mCursor); 
		
		// Move to first record in mCursor
        mCursor.moveToFirst();
        
        // Init mAdapter with binding data in mCursor with values customer combobox
        mAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, 
        									mCursor, mContent, 
        									new int [] {0, 0, android.R.id.text1});
        
        // Set standart DropDown view
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        
        // Set mAdapter to customers combobox data adapter to show data from mCursor on combobox
        cmbCust.setAdapter(mAdapter);

        // Set Listener for Customer combobox - this activity (DebitActivity.class)
        // so we have to Override special listening methods in this class
        cmbCust.setOnItemSelectedListener(this);
        
        if (mOrderH != null)
        {
        	mCursor.moveToFirst();
        	for (int i=0;i<mCursor.getCount();i++)
        	{
        		if (mCursor.getString(1).equals(mOrderH.cust_id))
        		{
        			cmbCust.setSelection(i);
        			this.onItemSelected(null, null, i, 0);
        			break;
        		}
        		mCursor.moveToNext();
        	}
        	
        } else {      	
        	// Select first record in combobox
        	this.onItemSelected(null, null, 0, 0);
        }
        Log.d("RRR","R");
    }
    
    private void updateRNList(int index, String cash) {
    	SharedPreferences def_prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		boolean isAutocorrect = def_prefs.getBoolean("prefDebitAutocorrection", false);
    	
    	String sum = currentItem.get(DebitItem.SUM);
    	String startsum = currentItem.get(DebitItem.STARTDEBT);
    	String mon = cash;
		float ss = 0;
    	
    	if (isAutocorrect) {
	    	float s = Float.parseFloat(sum.equals("")?"0":sum);
	    	float m = Float.parseFloat(mon.equals("")?"0":mon);
	    	ss = Float.parseFloat(startsum.equals("")?"0":startsum);
	    	s = ss - m;
	    	sum = Float.toString(s);
	    	//Log.d("RRR","sum="+sum);
	    	//Log.d("RRR","ss="+startsum);
    	}
    	DebitItem newItem = new DebitItem( currentItem.get(DebitItem.DATEDOC), 
    									   sum, 
    									   currentItem.get(DebitItem.EXPDATE), 
    									   currentItem.get(DebitItem.DOCNUMBER), 
    									   currentItem.get(DebitItem.EXPIRED), 
    									   cash,
    									   currency,
    									   currentItem.get(DebitItem.COMMENT),
    									   currentItem.get(DebitItem.ADDRESS),
    									   currentItem.get(DebitItem.STARTDEBT));
    	list.set(index, newItem);
    	setListAdapter(mAdapterList);
    	try {
    		txtCashTotal.setText(calcCashSum(list));
	    } catch (Exception e) {
	    	txtCashTotal.setText("0");
	    	Toast.makeText(this, "Sum calculation error", Toast.LENGTH_SHORT).show();
	    }
    	
    	
    	
    	Log.d("RR","D");
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

    	      // We have to release mCursorList
    	      // before Activity will close
    	      if (this.mCursorList != null) 
    	      {
    	        this.mCursorList.close();
    	      }
    	      
    	      // We have to release mCursor
    	      // before Activity will close
    	      if (this.mCursor != null) 
    	      {
    	        this.mCursor.close();
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
		if (db_orders != null) 
		{
		   	db_orders.close();
		}
	}

	/**
     * Selects item from combobox with customers list. When user do this 
     * program make query to database to get list of debit that Customer have
     * and binds that list to listview
     * @param arg0 - In this param system puts pointer to parent
     * @param arg1 - In this param system puts pointer to selected View, used to apply selection
     * @param arg3 - In this param system puts id of selected View
     */
	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		SharedPreferences def_prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		boolean isAutocorrect = def_prefs.getBoolean("prefDebitAutocorrection", false);
		txtTotal = (TextView)findViewById(R.id.debit_txtTotal);
		
		// Move cursor to selected position
		mCursor.moveToPosition(arg2);
		
		currentCustID = mCursor.getString(1);

		def_prefs.edit().putString("prevSCust", currentCustID).commit();
		
		// Get data from database to mCursorList by call query:
		// SELECT TABLE_DEBIT FROM mContentList
		//mCursorList = db.query(RsaDbHelper.TABLE_DEBIT, mContentList2, 
		//							RsaDbHelper.DEBIT_CUST_ID + "='"
		//							+ mCursor.getString(1) + "'", 
		//							null, null, null, null);

		String query =  "select d._id, d.CUST_ID, d.DATEDOC, d.RN, d.SUM, " +
						"d.DATEPP, d.CLOSED, d.COMMENT, s.NAME from _debit as d " +
				        "left join _shop as s on s.CUST_ID=d.CUST_ID and s.ID = d.SHOP_ID " +
				        "where CLOSED!='2' and d.CUST_ID='"+ mCursor.getString(1) + "' order by s.NAME";
		mCursorList = db.rawQuery(query,  null);
		
		if (mCursorList.getCount()<1)
			return;
		
		// Init mCursor to work with it
		//19.12.2012 Romka 
		if (verOS<3) startManagingCursor(mCursorList); 
		
		// Move to first record in mCursorList
        mCursorList.moveToFirst();
        
        float fltTotalDebit = 0;
        float fltTotalExpired = 0;
        Calendar c = Calendar.getInstance();
        Date currentDate = c.getTime();
        Date expireDate = c.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        long diff = 0;
        float deb = 0;
        float cs = 0;
        long lngExpiredDays = 0;
        StringBuilder strExpiration;
        list = new ArrayList<DebitItem>();
        
        for (int i=0;i<mCursorList.getCount();i++) {
        	strExpiration = new StringBuilder(getResources().getString(R.string.debit_txtExpired)); // Expired.......->
        	try {
        		expireDate = sdf.parse(mCursorList.getString(5)); 					// invoice expiration date
	        	diff = currentDate.getTime() - expireDate.getTime();				// difference between exp and current date
        		deb = Float.parseFloat(mCursorList.getString(4).replace(',', '.')); // invoice debit
        		if (isAutocorrect) {
        			cs = getKassaDet(mCursorList.getString(1), mCursorList.getString(3));
        		}
        		// Expired days, if <= 0 then NOT EXPIRED
	        	lngExpiredDays =  diff / (24 * 60 * 60 * 1000);
        		// Total Debit for current Customer
        		fltTotalDebit += deb - cs;
        		if (lngExpiredDays > 0) {
	        		// Total Expired debit for current Customer
		        	fltTotalExpired += deb - cs;
        		} else {
        			// Expired days, if <= 0 then NOT EXPIRED and make it = 0
        			lngExpiredDays = 0;
        			// Expired debit for current invoice make it 0 because NOT EXPIRED
        			deb = 0;
        		}
        	} catch (Exception e) {}
        	strExpiration.append(String.format("%.2f",deb).replace(',', '.'));
    		strExpiration.append(getResources().getString(R.string.debit_txtFor));
    		strExpiration.append(Long.toString(lngExpiredDays));
    		strExpiration.append(getResources().getString(R.string.debit_txtDays));
    		
    		String cash = "";
    		String d = sdf.format(currentDate); // current date
    		String id = currentCustID;
    		String rn = mCursorList.getString(3);
    		String comment = mCursorList.getString(7);
    		if(comment==null||comment.length()==0) {
    			comment = "(без комментария)";
    		}
    		
    		try {
    			String q = "select SUM from _kassadet where (DATE='"+d+"') and (CUST_ID='"+id+"') and (RN='"+rn+"') limit 1";
    			Cursor cashCursor = db_orders.rawQuery(q, null);
    			if (cashCursor.getCount()>0) {
    				cashCursor.moveToFirst();
    				cash = cashCursor.getString(0);
    			}
    			if (cashCursor != null) {
    				cashCursor.close();
    			}
    		} catch (Exception e3) {}
    		
    		String adr = "Неизвестный адрес";
    		try {
    			if (mCursorList.getString(8)!=null) {
    				adr = mCursorList.getString(8);
    			}
    		} catch (Exception e) {}
    		
    		
			isAutocorrect = def_prefs.getBoolean("prefDebitAutocorrection", false);
    		
    		String ss = mCursorList.getString(4);
    		String startsum = mCursorList.getString(4);
    		if (isAutocorrect) {
    			float m = 0;
    			try {
    				m = Float.parseFloat(cash.equals("")?"0":cash);
    			} catch (Exception e) {}
    				float s = Float.parseFloat(ss.equals("")?"0":ss);
    			s = s - m;
    			ss = Float.toString(s);
    		}
    		
    		list.add(new DebitItem(		mCursorList.getString(2), // datedoc
    									ss, // sum
    									mCursorList.getString(5), // expdate
    									mCursorList.getString(3), // docnumber
    									strExpiration.toString(), // expired
    									cash,
    									currency,                
    									comment,
    									adr,
    									startsum));
        	mCursorList.moveToNext();
        }
        
        String strExpired = String.format("%.2f",fltTotalExpired).replace(',', '.');
        String strDebit   = String.format("%.2f",fltTotalDebit).replace(',', '.');
        
        txtTotal.setText(strExpired + " (" + strDebit + ")");
        try {
        	txtCashTotal.setText(calcCashSum(list));
        } catch (Exception e) {
        	txtCashTotal.setText("0");
        	Toast.makeText(this, "Ошибка расчета суммы", Toast.LENGTH_SHORT).show();
        }
        
        mAdapterList = new SimpleAdapter(	this, list, lightTheme?R.layout.l_list_debit:R.layout.list_debit, 
        									new String[] {DebitItem.DATEDOC, DebitItem.SUM, DebitItem.EXPDATE, 
        													DebitItem.DOCNUMBER, DebitItem.EXPIRED, DebitItem.CASH, 
        													DebitItem.CURRENCY, DebitItem.COMMENT, DebitItem.ADDRESS}, 
        									new int[] {R.id.txtDate_debit, R.id.txtSum_debit, R.id.txtDateAp_debit, 
        												R.id.txtRN_debit, R.id.txtExpiration_debit, R.id.txtCash_debit, 
        												R.id.txtCurrency_debit, R.id.txtComment_debit, R.id.txtAD_debit});
        
    
        
        setListAdapter(mAdapterList);
        Log.d("RRR","z");
        
	}
	
	private float getKassaDet(String c, String r) {
		float result = 0;
		try {
			Cursor curs = db_orders.rawQuery("select SUM from _kassadet where CUST_ID='"+c+"' and RN='"+r+"' limit 1", null);
			if (curs.moveToFirst()) {
				result = Float.parseFloat(curs.getString(0).replace(",","."));
			}
			if (curs!=null && !curs.isClosed() ) {
				curs.close();
			}
		} catch (Exception e) {}
		
		return result;
	}
	
	private void calculateTotal(String c) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Calendar cal = Calendar.getInstance();
		Date currentDate = cal.getTime();
		txtTotal = (TextView)findViewById(R.id.debit_txtTotal);

		float fltTotalExpired = 0;
		float fltTotalDebit = 0;
		
		
		try {
			for (DebitItem item:list) {
				String cash = item.get(DebitItem.CASH).equals("")?"0":item.get(DebitItem.CASH);
				String st_debt = item.get(DebitItem.STARTDEBT).equals("")?"0":item.get(DebitItem.STARTDEBT);
				fltTotalDebit += Float.parseFloat(st_debt) - Float.parseFloat(cash);
				
				Date expireDate = sdf.parse(item.get(DebitItem.EXPDATE));				
				if (expireDate.getTime() < currentDate.getTime()) { 
					fltTotalExpired += Float.parseFloat(st_debt) - Float.parseFloat(cash);
				}
				
				Log.d("zaza", "total="+Float.toString(fltTotalDebit) + " | Expired="+Float.toString(fltTotalExpired));
			}
		}catch(Exception e) {
			Log.d("zulu",e.getMessage());
		}
		
		
		String strExpired = String.format("%.2f",fltTotalExpired).replace(',', '.');
	    String strDebit   = String.format("%.2f",fltTotalDebit).replace(',', '.');
	        
	    txtTotal.setText(strExpired + " (" + strDebit + ")");
		
	}
	
	private String calcCashSum(ArrayList<DebitItem> l) {
		String result = "0.00";
		float s = 0;
		
		for (DebitItem item : l ) {
			try {
				s += Float.parseFloat(item.get(DebitItem.CASH));
			} catch (Exception e3) {}
		}
		
		result = String.format("%.2f", s).replace(',', '.');
		
		return result;
	}
	
	/**
	 * If Back-button on device pressed then do...
	 */
	@Override
	public void onBackPressed() {
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
	

	@Override
	public void onNothingSelected(AdapterView<?> arg0)
	{
		// TODO Auto-generated method stub
		
	}
	
	public void onListItemClick(ListView parent, View v, int position, long id)
	{	
		currentItem = list.get(position);
		currentPosition = position;
		showDialog(IDD_INCOME);
	}
	
	private boolean alreadyTaken(String date, String cust_id, String rn) {
		boolean result = false;
		Cursor cashCursor = null;
		
		try {
			String q = "select SUM from _kassadet where (DATE='"+date+"') and (CUST_ID='"+cust_id+"') and (RN='"+rn+"') limit 1";
			cashCursor = db_orders.rawQuery(q, null);
			if (cashCursor.getCount()>0) {
				result = true;
			}
		} catch (Exception e3) {}

		try {
			if (cashCursor != null) {
				cashCursor.close();
			}
		} catch (Exception e4) {}
		
		return result;
	}
	
	@Override
	protected Dialog onCreateDialog(int id, Bundle args)
	{
		switch(id)
		{	
			case IDD_INCOME:
			{
				/** Set my own view of dialog to display to layout variable. Uses dlg_head.xml */
				LayoutInflater inflater = getLayoutInflater();
				View layout = inflater.inflate(R.layout.dlg_head, 
						(ViewGroup)findViewById(R.id.linear_dlg_head));
				
				/** Binding xml element of my own layout to final variable */ 
				edtDsc = (EditText)layout.findViewById(R.id.edtRemark_head);
				edtDsc.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
			
				int maxLength = 9;
				InputFilter[] FilterArray = new InputFilter[1];
				FilterArray[0] = new InputFilter.LengthFilter(maxLength);
				edtDsc.setFilters(FilterArray);
				
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setView(layout);
				
				builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Calendar c = Calendar.getInstance();
						SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
						SimpleDateFormat fmtTime = new SimpleDateFormat("HH:mm:ss");
						String time = fmtTime.format(c.getTime());
						String date = fmt.format(c.getTime());
						String cust_id = currentCustID;
						String rn = currentItem.get(DebitItem.DOCNUMBER);
						String sum = edtDsc.getText().toString();
						sum = sum.equals("")?"0":sum;
						String full = "0";
						String cust_name = ((TextView)cmbCust.getSelectedView()).getText().toString();
						SharedPreferences def_prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
						boolean isAutocorrect = def_prefs.getBoolean("prefDebitAutocorrection", false);
						
						try {
							float f_sum		= Float.parseFloat(sum);
							float prev_sum	= Float.parseFloat(currentItem.get(DebitItem.STARTDEBT));
							if (f_sum>prev_sum) {
								if (isAutocorrect==true && prev_sum>0) {
									sum = currentItem.get(DebitItem.STARTDEBT);
									Toast.makeText(getApplicationContext(),"Автокоррекция! Денег больше чем долга!!",Toast.LENGTH_LONG).show();
								} else {
									Toast.makeText(getApplicationContext(),"Денег больше чем долга!!",Toast.LENGTH_LONG).show();
								}
							}
						} catch (Exception e) {}
						
						
						ContentValues values = new ContentValues();
							values.put(RsaDbHelper.KASSADET_SUM, sum);
							values.put(RsaDbHelper.KASSADET_DATE, date);
							
						if (alreadyTaken(date, cust_id, rn)) {
							db_orders.update(RsaDbHelper.TABLE_KASSADET, values, "(DATE='"+date+"') and (CUST_ID='"+cust_id+"') and (RN='"+rn+"')", null);
						} else {
							values.put(RsaDbHelper.KASSADET_CUST_ID, cust_id);
							values.put(RsaDbHelper.KASSADET_RN, rn);
							values.put(RsaDbHelper.KASSADET_CUSTNAME, cust_name);
							values.put(RsaDbHelper.KASSADET_FULL, full);
							values.put(RsaDbHelper.KASSADET_TIME, time);
							db_orders.insert(RsaDbHelper.TABLE_KASSADET, RsaDbHelper.KASSADET_CUST_ID, values);
						}
						
						InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				        imm.hideSoftInputFromWindow(edtDsc.getWindowToken(), 0);
				        updateRNList(currentPosition, sum);
				        
				        if (isAutocorrect) {
				        	calculateTotal(cust_id);
				        }
					}
				});
				
				builder.setNegativeButton(R.string.head_dlg_cancel, new DialogInterface.OnClickListener() {					
					@Override
					public void onClick(DialogInterface dialog, int which) {
				        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				        imm.hideSoftInputFromWindow(edtDsc.getWindowToken(), 0);
					}
				});
				
				
				Dialog md = builder.create();
				md.requestWindowFeature(Window.FEATURE_NO_TITLE);
				dg = md;
				return md;
			}	
			default:
				return null;
		}
	}
		
	@Override
	protected void onPrepareDialog(int id, Dialog dialog)
	{	
		super.onPrepareDialog(id, dialog);
		// Determining the type of dialog to show, in this Activity only one dialog
		switch(id)
		{	
			case IDD_INCOME:
			{
				/** Binding xml element of remark edit */
				edtDsc = (EditText)dialog.findViewById(R.id.edtRemark_head);
			    edtDsc.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
				edtDsc.setFilters(new InputFilter[] {});
			    edtDsc.setText(currentItem.get(DebitItem.STARTDEBT));
			    
			    
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
		                          //  if (temp.length() > beforeDecimal) {
		                          //      return "";
		                          //  }
		                        } else {
		                         //   temp = temp.substring(temp.indexOf(".") + 1);
		                         //   if (temp.length() > afterDecimal) {
		                         //       return "";
		                         //  }
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
				    	edtDsc.setSelection(0, edtDsc.getText().toString().length()); 
				        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				        imm.showSoftInput(edtDsc, InputMethodManager.SHOW_FORCED);
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
		dg = dialog;
		
		
	}
	
	@Override
	protected void onSaveInstanceState (Bundle outState) {
		super.onSaveInstanceState(outState);
		
		outState.putString("CUST_ID", currentCustID);
		outState.putInt("POSITION", currentPosition);
		outState.putParcelable("CURITEM", currentItem);
		
		if (mOrderH!=null) {
			outState.putInt("MODE", mOrderH.mode);
			outState.putString("_id", mOrderH._id);
	    	outState.putString("SKLADID", mOrderH.sklad_id.toString());
	    	outState.putString("REMARK", mOrderH.remark.toString());
	    	outState.putString("DELAY", mOrderH.delay.toString());
	    	outState.putString("DISCOUNT", mOrderH.id.toString());
	    	outState.putParcelable("ORDERH", mOrderH);
		}
		
		try {
			if (dg != null) dg.dismiss();
		} catch (Exception e) {} 
	}
	
}