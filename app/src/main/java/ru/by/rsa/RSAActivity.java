package ru.by.rsa;


import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SimpleCursorAdapter.ViewBinder;

import ru.by.rsa.org.apache.commons.net.util.Base64;
import ru.by.rsa.ui.activity.AboutActivity;
import ru.by.rsa.utils.GpsUtils;

/**
 * Remote Sales Agent
 * Main Activity that shows list of orders stored in database
 * with some details (Date, Client, Sum etc.).
 * In addition, from that Activity user can get Program Menu
 * by pressing MENU-button on device.
 * Menu functions allows user to add or modify his orders, make
 * synchronization and get program settings.
 * @version 1.0, November 2011
 * @author Komarev Roman
 * Odessa, neo3da@mail.ru, +380503412392
 */
public class RSAActivity extends ListActivity
{
	/** Is set of data from DB to be shown in ListActivity. To get value have to use mCurosr.getString("KEY") */
	private Cursor		mCursor;
	/** Special adapter that must be used between mCursor and ListActivity */
	private SimpleCursorAdapter mAdapter;
	/** Ticket 12: to get data about rest of goods to change it with current order */
	private Cursor mCursorGoods;
	/** Ticket 12: lines from DB, used when deleting order to return stocks amount */
	private Cursor mCursorLines;
	/** SQLite database that stores orders data */
	private static SQLiteDatabase db_orders;
	/** SQLite database that stores 1C data */
	private static SQLiteDatabase db;
	private static SQLiteDatabase db_settings;
	private static Cursor settingsCursor;
	/** If first run identif */
	boolean newBase;
	/** Variables to store current date */
	private int mYear, mYear2;
	private int mYearTo, mYear2To;
	private int mMonth, mMonth2;
	private int mMonthTo, mMonth2To;
	private int mDay, mDay2;
	private int mDayTo, mDay2To;
	/** Ticket 26: Variable to get version of application on smartphone */
	private String versionLocal;
	private String versionSite;
	/** Index of selected position in orders list, when it's equals -1 that means is nothing selected */
	private int listPosition;
	/** Array of columns that will be used to obtain data from DB-tables in columns with the same name */
	private String[] 	mContent = {"_id", RsaDbHelper.HEAD_CUST_ID,	RsaDbHelper.HEAD_SHOP_ID,	RsaDbHelper.HEAD_SKLAD_ID,
										   RsaDbHelper.HEAD_CUST_TEXT,	RsaDbHelper.HEAD_SHOP_TEXT,	RsaDbHelper.HEAD_SKLAD_TEXT,
										   RsaDbHelper.HEAD_DATE, RsaDbHelper.HEAD_TIME, RsaDbHelper.HEAD_PAYTYPE,
										   RsaDbHelper.HEAD_HSUMO, RsaDbHelper.HEAD_BLOCK, RsaDbHelper.HEAD_SENDED,
										   RsaDbHelper.HEAD_HWEIGHT, RsaDbHelper.HEAD_NUM1C, RsaDbHelper.HEAD_VISITID }; // Ticket 14: HEAD_BLOCKED added


	/** Ticket 12: to get data about rest of goods to change it with current order */
	private String[] mContentGoods = {"_id", RsaDbHelper.GOODS_ID, RsaDbHelper.GOODS_REST};
	/** Ticket 12: to get data about rest of goods in deleting lines to change stocks in DB in TABLE_GOODS */
	private String[] mContentLines = {"_id", RsaDbHelper.LINES_ZAKAZ_ID, RsaDbHelper.LINES_GOODS_ID, RsaDbHelper.LINES_QTY};
	/** Constant identifier for ADD-button in program menu or context menu */
	public static final int IDM_ADD				= 101;
	/** Constant identifier for MODIFY-button in program menu or context menu  */
	public static final int IDM_MODIFY			= 102;
	/** Constant identifier for DELETE-button in program menu or context menu  */
	public static final int IDM_DELETE			= 103;
	/** Constant identifier for SYNC-button in program menu or context menu  */
	public static final int IDM_SYNC			= 104;
	/** Constant identifier for SETTINGS-button in program menu or context menu  */
	public static final int IDM_SETTINGS		= 105;
	/** Constant identifier for PERIOD-button in program menu or context menu  */
	public static final int IDM_PERIOD			= 106;
	/** Constant identifier for DEBIT-button in program menu or context menu  */
	public static final int IDM_DEBIT			= 107;
	/** Constant identifier for ABOUT-button in program menu or context menu  */
	public static final int IDM_ABOUT			= 108;
	/** Constant identifier for EXIT-button in program menu or context menu  */
	public static final int IDM_EXIT			= 109;
	/** Constant identifier for MAP-button in program menu or context menu  */
	public static final int IDM_MAP				= 110;
	/** Constant identifier for GPS  */
	public static final int IDM_GPS				= 111;
	/** Constant identifier for KASSA  */
	public static final int IDM_KASSA			= 112;
	/** Constant identifier for NEWVERSION  */
	public static final int IDM_NEWVERSION		= 113;
	/** Constant identifier for UPGRADING  */
	public static final int IDM_UPGRADING		= 114;
	/** Constant identifier for UPGRADING  */
	public static final int IDM_REPORT			= 115;
	public static final int IDM_PERIODTO		= 116;
	public static final int IDM_USER			= 117;
	public static final int IDM_COPY			= 118;
	//public static final int IDM_SALES			= 119;
	/** Current theme */
	private boolean lightTheme;
	private String currency;

	ProgressDialog updProgressDialog;
	UpgradingThread updThread;
	private String totalWeight;
	private boolean mAltPaytype;

	SharedPreferences def_prefs;

	private static int verOS = 0;


	final Handler updHandler = new Handler()
	{
		public void handleMessage(Message msg)
		{
			int total = msg.getData().getInt("Total");
			if (total==100)
			{
				dismissDialog(IDM_UPGRADING);
				updThread.setState(UpgradingThread.STATE_DONE);
			}
		}
	};


	/**
	 * Method that starts on Activity creation
	 * @param savedInstanceState previous saved state of activity
	 */
	@Override
    public void onCreate(Bundle savedInstanceState) {
		lightTheme = getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE).getBoolean(RsaDb.LIGHTTHEMEKEY, false);

		def_prefs = PreferenceManager.getDefaultSharedPreferences(this);
		currency = " "+def_prefs.getString("prefCurrency", getResources().getString(R.string.preferences_currency_summary));

		mAltPaytype = def_prefs.getBoolean("altOrd", false);
		//def_prefs.edit().putBoolean("extVim", true).commit();
		
		verOS = 2;
		try {
			verOS = Integer.parseInt(Build.VERSION.RELEASE.substring(0,1));
		}
		catch (Exception e) {};


		if (lightTheme) {
			setTheme(R.style.Theme_Custom);
			super.onCreate(savedInstanceState);
			setContentView(R.layout.l_orders);
		} else {
			setTheme(R.style.Theme_CustomBlack2);
			super.onCreate(savedInstanceState);
			setContentView(R.layout.orders);
		}

		try
		{
	        // On Application start do shrink DB with coords by 7 days from current
	        shrinkCoordDb(8);
	        SharedPreferences def_prefs = PreferenceManager.getDefaultSharedPreferences(this);
			if (!def_prefs.getBoolean("shrinkAfter",false)) {
				shrinkOrders(this, Integer.parseInt(def_prefs.getString(RsaDb.ORDERHYST, "30")),
						Integer.parseInt(def_prefs.getString("ordclean", "7")));
			}
		} catch (Exception e)	{}


    	// Get current date
    	Calendar c = Calendar.getInstance();

    	// Ticket 26: Get application current version name and saved version from site */
    	SharedPreferences _prefs = getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);
    	versionSite = _prefs.getString(RsaDb.MARKETVERSION, "0");
    	versionLocal = "0";
    	try
		{
			versionLocal = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
		}
    	catch (NameNotFoundException e1)
    	{
    		Toast.makeText(getApplicationContext(),"!!! Can't read current version!",Toast.LENGTH_LONG).show();
    	}
    	Log.v("ROMKA", "Current version(string): " + versionLocal);
    	Log.v("ROMKA", "Site version(string): "    + versionSite);

    	float fVersionLocal = 0;
    	float fVersionSite 	= 0;

    	try
    	{
    		fVersionLocal = Float.parseFloat(versionLocal);
    	}
    	catch(Exception e) {};

    	try
    	{
    		fVersionSite = Float.parseFloat(versionSite);
    	}
    	catch(Exception e) {};


    	// if BOTH versionLocal != 0 AND versionSite != 0 then TRUE
    	boolean versionsInitiated = (fVersionLocal!=0)&&(fVersionSite!=0);
    	boolean newVersionDetected = fVersionSite > fVersionLocal;

    	Log.v("ROMKA", "Both of versions initiated: " + (versionsInitiated?"TRUE":"FALSE"));
    	Log.v("ROMKA", "SiteVersion > LocalVersion: " + (newVersionDetected?"TRUE":"FALSE"));

    	if (versionsInitiated && newVersionDetected)
    	{
    		// Do upgrade
    		Log.v("ROMKA", "Do Upgrade");
    		//showDialog(IDM_NEWVERSION); //TODO: auto update disabled
    	}


		if (savedInstanceState != null)
        {
        	// get date
        	mYear = savedInstanceState.getInt("YEAR");
        	mMonth = savedInstanceState.getInt("MONTH");
        	mDay = savedInstanceState.getInt("DAY");
        	mYear2 = savedInstanceState.getInt("YEAR2");
        	mMonth2 = savedInstanceState.getInt("MONTH2");
        	mDay2 = savedInstanceState.getInt("DAY2");

        	mYearTo = savedInstanceState.getInt("YEARTO");
        	mMonthTo = savedInstanceState.getInt("MONTHTO");
        	mDayTo = savedInstanceState.getInt("DAYTO");
        	mYear2To = savedInstanceState.getInt("YEAR2TO");
        	mMonth2To = savedInstanceState.getInt("MONTH2TO");
        	mDay2To = savedInstanceState.getInt("DAY2TO");

        }
        else
        {
        	mYear = c.get(Calendar.YEAR);
        	mMonth = c.get(Calendar.MONTH);
        	mDay = c.get(Calendar.DAY_OF_MONTH);
        	mYear2 = mYear;
        	mMonth2 = mMonth;
        	mDay2 = mDay;

        	mYearTo = c.get(Calendar.YEAR);
        	mMonthTo = c.get(Calendar.MONTH);
        	mDayTo = c.get(Calendar.DAY_OF_MONTH);
        	mYear2To = mYearTo;
        	mMonth2To = mMonthTo;
        	mDay2To = mDayTo;

        }

        // Get path to program files storage (of this application) */
        String appPath = getApplicationContext().getFilesDir().getAbsolutePath();
	    // Get Screen Preferences */
		SharedPreferences screen_prefs = getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE);

		String tmp_imei = RsaDb.getDImei(getApplicationContext());
	//	try {
	//		tmp_imei = ((TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
	//	} catch (Exception ei) {tmp_imei= null;}
	//	try {
	//		if (tmp_imei == null) {
	//			String s = android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
	//			s = s.substring(2, 16);
	//			long lFakeImei = Long.parseLong(s, 16);
	//			tmp_imei = Long.toString(lFakeImei);
	//		}
	//	} catch (Exception ed) {tmp_imei = "123456789123456";}

		String ser = Integer.toString( Math.abs((int)Long.parseLong(tmp_imei)) );
		// Every time when Application starts, it must to set correct IMEI
		screen_prefs.edit().putString(RsaDb.IMEIKEY, tmp_imei).commit();
		screen_prefs.edit().putString(RsaDb.MONITORSERIAL, ser).commit();

        // Check for directory "inbox" existense, if not then create it 
        boolean exists = (new File(appPath + File.separator + "inbox")).exists();
        if (!exists)
        {
            File f = new File(appPath + File.separator + "inbox");
            f.mkdir();
        }

        // Check for directory "outbox" existense, if not then create it
        exists = (new File(appPath + File.separator + "outbox")).exists();
        if (!exists)
        {
            File f = new File(appPath + File.separator + "outbox");
            f.mkdir();
        }

        // Check for directory "Data" existense, if not then create it
        exists = (new File(appPath + File.separator + "Data")).exists();
        if (!exists)
        {
            File f = new File(appPath + File.separator + "Data");
            f.mkdir();
        }

        try
        {
	        // Check for directory "/sdcard/rsa" existense, if not then create it
	        String SD_CARD_PATH = Environment.getExternalStorageDirectory().toString();
	        exists = (new File(SD_CARD_PATH + File.separator + "rsa")).exists();
	        if (!exists)
	        {
	            File f = new File(SD_CARD_PATH + File.separator + "rsa");
	            f.mkdir();
	        }

	        exists = (new File(SD_CARD_PATH + File.separator + "rsa" + File.separator + "inbox")).exists();
	        if (!exists)
	        {
	            File f = new File(SD_CARD_PATH + File.separator + "rsa" + File.separator + "inbox");
	            f.mkdir();
	        }
	        exists = (new File(SD_CARD_PATH + File.separator + "rsa" + File.separator + "outbox")).exists();
	        if (!exists)
	        {
	            File f = new File(SD_CARD_PATH + File.separator + "rsa" + File.separator + "outbox");
	            f.mkdir();
	        }
        } catch (Exception e)
        {
        	Toast.makeText(getApplicationContext(),"!!! Can't create folders on SDCARD !!!",Toast.LENGTH_SHORT).show();
        }


        if (screen_prefs.getInt(RsaDb.DBPREFIXKEY, 99) == 99) {
        	screen_prefs.edit().putInt(RsaDb.DBPREFIXKEY, 0).commit();
		}

        // if AutoPriceType = null then make it true by default
        if (screen_prefs.getString(RsaDb.PRICESELECTED, "x").equals("x"))
		{
        	// Get array from res 0 - "Price 1"; 1 - "Price 2" */
			String[] arrayPrices = getResources().getStringArray(R.array.Prices);

        	screen_prefs.edit().putBoolean(RsaDb.PRICETYPEKEY, false).commit();
        	screen_prefs.edit().putString(RsaDb.PRICESELECTED, arrayPrices[0]).commit();
		}

        if (screen_prefs.getString(RsaDb.RATEKEY, "x").equals("x"))
		{
        	screen_prefs.edit().putString(RsaDb.RATEKEY, "20").commit();
		}

        if (screen_prefs.getString(RsaDb.VATRATE, "77").equals("77"))
		{
        	screen_prefs.edit().putString(RsaDb.VATRATE, "20").commit();
		}

        String orderString = getResources().getString(R.string.preferences_orderby_default);
        if (screen_prefs.getString(RsaDb.ORDERBYKEY, orderString).equals(orderString))
		{
        	screen_prefs.edit().putString(RsaDb.ORDERBYKEY, orderString).commit();
		}

        // Sets index of selected position to -1
        // means is nothing selected
        listPosition = -1;

        // Register Activity to work with LongClicks with ListView items
        // and empty ListView
        registerForContextMenu(getListView());
        registerForContextMenu(getListView().getEmptyView());

        LinearLayout layout = (LinearLayout)findViewById(R.id.main_blank_area);
		registerForContextMenu(layout);

    }

	private void turnGPSOn(){
	    String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

	    if(!provider.contains("gps")){ //if gps is disabled
	        final Intent poke = new Intent();
	        poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
	        poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
	        poke.setData(Uri.parse("3"));
	        sendBroadcast(poke);
	    }
	}

	private boolean canToggleGPS() {
	    PackageManager pacman = getPackageManager();
	    PackageInfo pacInfo = null;

	    try {
	        pacInfo = pacman.getPackageInfo("com.android.settings", PackageManager.GET_RECEIVERS);
	    } catch (NameNotFoundException e) {
	        return false; //package not found
	    }

	    if(pacInfo != null){
	        for(ActivityInfo actInfo : pacInfo.receivers){
	            //test if recevier is exported. if so, we can toggle GPS.
	            if(actInfo.name.equals("com.android.settings.widget.SettingsAppWidgetProvider") && actInfo.exported){
	                return true;
	            }
	        }
	    }

	    return false; //default
	}



	/**
	 * Metgod that starts every time when Activity is shown on display
	 */
    @Override
    public void onStart()
    {
    	super.onStart();

    //	Context ct = getApplicationContext();
    //	try {
	//		RsaDb.toZipArray(ct, "outbox", "dffgsdfg");
	//	} catch (IOException e1) {
	//		// TODO Auto-generated catch block
	//		e1.printStackTrace();
	//	}
    	totalWeight = "";
	    // Get Shared Preferences
		SharedPreferences prefs = getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);
	    // Get Screen Preferences
		final SharedPreferences screen_prefs = getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE);

		// if have no license show message
		if (prefs.getBoolean(RsaDb.LICENSED, true) == false) {
			Toast.makeText(getApplicationContext(),getResources().getString(R.string.list_order_noregistration),Toast.LENGTH_LONG).show();
			Toast.makeText(getApplicationContext(),getResources().getString(R.string.list_order_noregistration),Toast.LENGTH_LONG).show();
		}

		// Start GPS tracking service if GPS option enabled
		if (screen_prefs.getBoolean(RsaDb.GPSKEY, true))
		{
			// Turn on GPS
	    	try {
	    		if (canToggleGPS()) {
	    			turnGPSOn();
	    		} else {
	    			String provider =
							Settings.Secure.getString(getContentResolver(),
									Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
					LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
					if(!provider.contains("gps") && lm.getAllProviders().contains(LocationManager.GPS_PROVIDER)) {
	    		    	showDialog(IDM_GPS);
	    		    }
	    		}
			} catch (Exception e) {}

			startService(new Intent(RSAActivity.this, RsaGpsService.class));
			Alarm alarm = new Alarm();
			alarm.SetAlarm(this);
		}
		else
		{
			Alarm alarm = new Alarm();
			alarm.CancelAlarm(this);
			stopService(new Intent(RSAActivity.this, RsaGpsService.class));
		}

		boolean isXML = screen_prefs.getString(RsaDb.INTERFACEKEY, "DBF").equals("XML");
		if ((isXML==true)&&(lightTheme==true)) {
			((View)findViewById(R.id.dropShadowdown)).setVisibility(View.VISIBLE);
			((View)findViewById(R.id.downPanel_layout)).setVisibility(View.VISIBLE);

			String[] dbArray = getResources().getStringArray(R.array.Databases);
			int indexSelectedDb = screen_prefs.getInt(RsaDb.DBPREFIXKEY, 0);
			String strCurrentDB = dbArray[indexSelectedDb];
			strCurrentDB += " | ФИО: " + screen_prefs.getString(RsaDb.NAMEKEY, "Не выбрано");
			((TextView)findViewById(R.id.main_currentDB)).setText(strCurrentDB);
		} else if (isXML == true) {
			String[] dbArray = getResources().getStringArray(R.array.Databases);
			int indexSelectedDb = screen_prefs.getInt(RsaDb.DBPREFIXKEY, 0);
			String strCurrentDB = dbArray[indexSelectedDb];
			strCurrentDB += "\n" + screen_prefs.getString(RsaDb.NAMEKEY, "Не выбрано");
			((TextView)findViewById(R.id.main_update)).setText(strCurrentDB);
		}

        // Sync button
        Button btnSync = (Button)findViewById(R.id.main_pbtn_prev);

        // Listener for Sync-button click, calls when Sync-button clicked
        btnSync.setOnClickListener(new OnClickListener()
        {
			@Override
			public void onClick(View v) {
				//  Used to start another activities and send data for them
				Intent intent = new Intent();
				// Get array from res 0 - Email; 1 - DBF;
				String[] protArray = getResources().getStringArray(R.array.prefProtocol);

				// if protocol = "E-mail" then...
				if (screen_prefs.getString(RsaDb.PROTOCOLKEY, protArray[0]).equals(protArray[0]))
				{
					// Preparing intent to start SyncActivity with E-mail protocol
					intent.setClass(getApplicationContext(), SyncActivity.class);
				}
				else // if protocol = "FTP" then...
				{
					// Preparing intent to start SyncActivity with FTP protocol
					intent.setClass(getApplicationContext(), FtpSyncActivity.class);
				}

				// Set selection index to -1 (Nothing selected), because we going to leave the activity
				listPosition = -1;
				// Close database if it is opened, to allow use it in another activities
				if (db_orders != null)
				{
					db_orders.close();
				}
				if (db != null)
				{
					db.close();
				}
				startActivity(intent);
			}
		});

        // New order button
        Button btnNew = (Button)findViewById(R.id.main_pbtn_next);

        // Listener for Sync-button click, calls when Sync-button clicked
        btnNew.setOnClickListener(new OnClickListener()
        {
			@Override
			public void onClick(View v) {
	        	//  Used to start another activities and send data for them
	        	Intent intent = new Intent();
				if (newBase) {
					Toast.makeText(getApplicationContext(),R.string.list_order_warning_noBD,Toast.LENGTH_LONG).show();
					return;
				}
				Cursor mCursorCust = null;
				try {
					String[] mContentCust = {"_id", RsaDbHelper.CUST_ID};
					mCursorCust = db.query(  RsaDbHelper.TABLE_CUST, mContentCust,
													RsaDbHelper.CUST_ID + "<>''",
													null, null, null, null);
					if (verOS<3) startManagingCursor(mCursorCust);
					if (mCursorCust.getCount() < 1){
						Toast.makeText(getApplicationContext(),R.string.list_order_warning_noCust,Toast.LENGTH_LONG).show();
						return;
					}
				} catch (Exception e) {
					if (mCursorCust != null)  {
		    	        mCursorCust.close();
		    	    }
					Toast.makeText(getApplicationContext(),"Exception: Check for customers",Toast.LENGTH_LONG).show();
				}

				if (def_prefs.getBoolean("prefUseExtPlan", false)) {
					intent.setClass(getApplicationContext(), DaysActivity.class);
				} else if (def_prefs.getBoolean(RsaDb.USINGPLAN, false)) {
					intent.setClass(getApplicationContext(), PlanActivity.class);
				} else {
					intent.setClass(getApplicationContext(), NewHeadActivity.class);
				}
				listPosition = -1;
				intent.putExtra("MODE", IDM_ADD);
				if (db_orders != null) {
					db_orders.close();
	    	    }
				if (db != null) {
					db.close();
	    	    }
				startActivity(intent);
			}
		});

		// Get actual db name from SharedPrefs
		String tmpName = prefs.getString(RsaDb.ACTUALDBKEY, "new");

		newBase = false;

		// If not db name is not specified in Shared Preferences then make it DB_NAME1
		// for first creation
		if (tmpName.equals("new"))
		{
			newBase = true;
			if (screen_prefs.getString(RsaDb.NAMEKEY, "x").equals("x"))
			{
				screen_prefs.edit().putString(RsaDb.NAMEKEY, getResources().getString(R.string.prefs_namecode_default)).commit();
			}
			if (screen_prefs.getString(RsaDb.CODEKEY, "x").equals("x"))
			{
				screen_prefs.edit().putString(RsaDb.CODEKEY, getResources().getString(R.string.prefs_namecode_default)).commit();
			}
			screen_prefs.edit().putString(RsaDb.IMEIKEY, RsaDb.getDImei(getApplicationContext())).commit();
			if (screen_prefs.getString(RsaDb.LASTSDLOADKEY, "x").equals("x"))
			{
				screen_prefs.edit().putString(RsaDb.LASTSDLOADKEY, getResources().getString(R.string.prefs_lastdwnld_default)).commit();
			}
			if (screen_prefs.getString(RsaDb.LASTOPTIMKEY, "x").equals("x"))
			{
				screen_prefs.edit().putString(RsaDb.LASTOPTIMKEY,  getResources().getString(R.string.prefs_lastdwnld_default)).commit();
			}
			if (screen_prefs.getBoolean(RsaDb.GPSKEY, false) == false)
			{
				screen_prefs.edit().putBoolean(RsaDb.GPSKEY, false).commit();
			}
			if (screen_prefs.getBoolean(RsaDb.LONGATTRIBUTES, false) == false)
			{
				screen_prefs.edit().putBoolean(RsaDb.LONGATTRIBUTES, false).commit();
			}
			if (screen_prefs.getString(RsaDb.INTERFACEKEY, "x").equals("x"))
			{
				screen_prefs.edit().putString(RsaDb.INTERFACEKEY, getResources().getString(R.string.prefs_interface_default)).commit();
			}
			if (!screen_prefs.getString(RsaDb.PROTOCOLKEY, "").equals("Ftp"))
			{
				screen_prefs.edit().putString(RsaDb.PROTOCOLKEY, getResources().getString(R.string.prefs_protocol_default)).commit();
			}

			// Ticket 27: if e-mail params is empty then set them with default values
			if ( (prefs.getString(RsaDb.EMAILKEY,"0").length()<3)
					||(prefs.getString(RsaDb.SENDTOKEY,"0").length()<3))
			{
				// Default params for sync... gives user chance to test application
				//TODO: data for test
/*				prefs.edit().putString(RsaDb.EMAILKEY, 		"rsatest0@oseledko.ukrwest.net").commit();
				prefs.edit().putString(RsaDb.PASSWORDKEY, 	"Qwerty00").commit();
				prefs.edit().putString(RsaDb.SMTPKEY, 		"mx.oseledko.com").commit();
				prefs.edit().putString(RsaDb.SMTPPORTKEY, 	"2525").commit();
				prefs.edit().putString(RsaDb.POPKEY, 		"mx.oseledko.com").commit();
				prefs.edit().putString(RsaDb.POPPORTKEY, 	"110").commit();
				prefs.edit().putString(RsaDb.SENDTOKEY, 	"").commit();
				prefs.edit().putBoolean(RsaDb.USESSL, 		true).commit();*/
				prefs.edit().putString(RsaDb.EMAILKEY, 		"").commit();
				prefs.edit().putString(RsaDb.PASSWORDKEY, 	"").commit();
				prefs.edit().putString(RsaDb.SMTPKEY, 		"").commit();
				prefs.edit().putString(RsaDb.SMTPPORTKEY, 	"").commit();
				prefs.edit().putString(RsaDb.POPKEY, 		"").commit();
				prefs.edit().putString(RsaDb.POPPORTKEY, 	"").commit();
				prefs.edit().putString(RsaDb.SENDTOKEY, 	"").commit();
				prefs.edit().putBoolean(RsaDb.USESSL, 		true).commit();
			}
		}
		else
		{
			// Init database with 1C data with architecture that designed in RsaDbHelper.class
			RsaDbHelper mDb = new RsaDbHelper(this, tmpName);
			// Open database
			db = mDb.getWritableDatabase();
			// Close database with 1C data
		//	db.close();  rrrrrr

			// Init database with orders data
			mDb = new RsaDbHelper(this, RsaDbHelper.DB_ORDERS);
			// Open database
			db_orders = mDb.getWritableDatabase();

			try {
				int delta_days = prefs.getInt("ru.by.rsa.resend_monitor", 0);

				if (delta_days>0) {
					Calendar c = Calendar.getInstance();
			    	SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
			    	String current_date = fmt.format(c.getTime());
			    	c.add(Calendar.DATE, -delta_days);
			    	String begin_date = fmt.format(c.getTime());

			    	db_orders.execSQL("update _head set MONITORED=0 where SDATE between '"+begin_date+"' AND '"+current_date+"'");
			    	Toast.makeText(getApplicationContext(),"Обнулили мониторинг с "+begin_date,Toast.LENGTH_LONG).show();
			    	prefs.edit().putInt("ru.by.rsa.resend_monitor", 0).commit();
				}


				//if (prefs.getBoolean("ru.by.rsa.runonce_eleven", false)==false) {
				//		db_orders.execSQL("update _head set MONITORED=0 where SDATE between '2014-01-13' AND '2014-01-17'");
				//		prefs.edit().putBoolean("ru.by.rsa.runonce_eleven", true).commit();
				//}
				//if (prefs.getBoolean("ru.by.rsa.runonce_six", false)==false && prefs.getBoolean(RsaDb.SENDLINES, false)==true) {
				//	db_orders.execSQL("update _head set MONITORED=1 where SDATE between '2013-11-01' AND '2013-12-10'");
				//	db_orders.execSQL("update _head set MONITORED=0 where SDATE between '2013-12-11' AND '2013-12-13'");
				//	prefs.edit().putBoolean("ru.by.rsa.runonce_six", true).commit();
				//}
				//	if (prefs.getBoolean("ru.by.rsa.runonce_nine", false)==false && prefs.getBoolean(RsaDb.SENDLINES, false)==true) {
				//		db_orders.execSQL("update _head set MONITORED=1 where SDATE between '2013-12-01' AND '2013-12-15'");
				//		//db_orders.execSQL("update _head set MONITORED=0 where SDATE between '2013-12-01' AND '2013-12-11'");
				//		prefs.edit().putBoolean("ru.by.rsa.runonce_nine", true).commit();
				//		Toast.makeText(getApplicationContext(),"Пометили ВСЕ",Toast.LENGTH_LONG).show();
				//	}
			} catch (Exception e) {}
			/////////////////




			//////////////////

			// Fill list with orders from database
			updateList();

			// Method onStart() can be called when another activity was just been closed
        	// so we need reinit value listPosition to -1 (Nothing selected)
			listPosition = -1;
		}



    }

    /**
    * Used to update list of orders on device display
    * with data from database
    */
    private void updateList()
	{
    	/** Set string with current date */
    	// String sdate = String.format( "%02d.%02d.%02d", mDay, mMonth+1, mYear );
    	String sdate = String.format( "%04d-%02d-%02d", mYear, mMonth+1, mDay );
    	String sdateTo = String.format( "%04d-%02d-%02d", mYearTo, mMonthTo+1, mDayTo );
    	SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
    	Date date1 = null;
    	Date date2 = null;
    	try {
	    	date1 = fmt.parse(sdate);
	    	date2 = fmt.parse(sdateTo);
    	} catch (Exception e) {}
    	if (date1.getTime()>date2.getTime()) {
    		mYear = mYearTo = mYear2;
    		mMonth = mMonthTo = mMonth2;
    		mDay = mDayTo = mDay2;
    		sdate = String.format( "%04d-%02d-%02d", mYear, mMonth+1, mDay );
        	sdateTo = String.format( "%04d-%02d-%02d", mYearTo, mMonthTo+1, mDayTo );
    		Toast.makeText(getApplicationContext(),"Дата начала не может быть больше конечной даты!",Toast.LENGTH_LONG).show();
    	}

    	final boolean isCSV = getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE)
				.getString(RsaDb.INTERFACEKEY, "DBF").equals("CSV");

    	String s;
    	float sum = 0;
        // get data of table HEAD, from columns with same names as in mContent array 
    	mCursor = db_orders.query(RsaDbHelper.TABLE_HEAD, mContent,
    							RsaDbHelper.HEAD_SDATE + " between '"
    							+ sdate + "' AND '"+ sdateTo + "'",
    							null, null, null, null);
        // init mCursor to work with it
    	////19.12.2012 Romka
    	if (verOS<3) startManagingCursor(mCursor);
        // move to first record in mCursor
        mCursor.moveToFirst();

        // Init mAdapter with binding data in mCursor with values on list_order.xml       
        mAdapter = new SimpleCursorAdapter(
        					this, lightTheme?R.layout.l_list_order:R.layout.list_order, mCursor,
        					new String[] {RsaDbHelper.HEAD_DATE, RsaDbHelper.HEAD_TIME, RsaDbHelper.HEAD_HSUMO,
        									RsaDbHelper.HEAD_PAYTYPE, RsaDbHelper.HEAD_CUST_TEXT, RsaDbHelper.HEAD_SHOP_TEXT,
        									RsaDbHelper.HEAD_BLOCK, RsaDbHelper.HEAD_HWEIGHT, RsaDbHelper.HEAD_CUST_ID},
        					new int[] {R.id.txtDate_main, R.id.txtTime_main, R.id.txtTotal_main, R.id.txtCash_main,
        							    R.id.txtName_main, R.id.txtShop_main, R.id.list_order_block, R.id.txtW_main, R.id.txt_orders_currency}); // Ticket 13: R.id.block added

        mAdapter.setViewBinder(new ViewBinder()
        {
            public boolean setViewValue(View aView, Cursor aCursor, int aColumnIndex)
            {
            	if (aColumnIndex == 1)
                {
            			((TextView) aView).setText(currency);
                        return true;
                }
            	if (aColumnIndex == 9)
                {
            			TextView strTxt = (TextView) aView;
            			View prnt = (View)(aView.getParent().getParent().getParent());

            			myInt mi = new myInt();

						String paytype = aCursor.getString(9);

						if (mAltPaytype) {
							int customButtonsState = 0;
							String visitId=aCursor.getString(15);
							try {
								customButtonsState = Integer.parseInt(TextUtils.isEmpty(visitId)?"0":visitId);
							} catch (Exception e) {}
							if ((customButtonsState&1)==1) {
								visitId = "Ф";
							} else{
								visitId = "К";
							}
							if (paytype.equals("Без")) {
								visitId += "Б";
							} else {
								visitId += "Н";
							}
							paytype = visitId;
						}

            			String t = checkForReturnInvoice(aCursor.getString(14), paytype, mi);

            			prnt.setBackgroundColor(mi.get());
            			strTxt.setText(t);

            			return true;
                }
                if (aColumnIndex == 11)
                {
                		ImageView imgView = (ImageView) aView;
                		if (aCursor.getString(12).equals("1"))
                			imgView.setImageDrawable(getResources().getDrawable(R.drawable.ic_locked_));
                		else
                			imgView.setImageDrawable(getResources().getDrawable(R.drawable.ic_unlocked_));
                        return true;
                }
                if (aColumnIndex==13)
                {
                		TextView txtView = (TextView) aView;
                		String s = aCursor.getString(13);
                		if ((isCSV==false)||s.equals("0.000")||s.equals("")||s.equals("0")) {
                			txtView.setText("");
                		} else {
                			txtView.setText(s+ " кг.");
                		}
                        return true;
                }
                return false;
            }
        });


        // Set mAdapter to ListActivty data adapter to show data from mCursor on device display 
        setListAdapter(mAdapter);

        float hwght = 0;
        int orderCount = 0;
        totalWeight = "";
        // searching for customer of current order in customers-combobox
        for (int i=0;i<mCursor.getCount();i++) {
        	String n1c = mCursor.getString(14);
        	if ((n1c.length()==0) || n1c.equals("0")) {
        		orderCount++;
	       		sum+= Float.parseFloat(mCursor.getString(10));
	       		try {
	       			hwght += Float.parseFloat(mCursor.getString(13));
	       		} catch(Exception e) {}
        	}
        	mCursor.moveToNext();
        }

        s = String.format("%.2f",sum).replace(',', '.');

        String totalWeightWhite = "";
        if ((hwght>0)&&(isCSV==true)) {
        	totalWeight = "(" + String.format("%.3f", hwght).replace(',', '.') + " кг.)";
        	totalWeightWhite = "\n"+totalWeight;
        }

        /** Bind Variable with xml element of empty listview */
        TextView txtEmpty = (TextView)findViewById(R.id.txtEmpty);

        if (lightTheme)
        {
        	TextView txtActionBar = (TextView)findViewById(R.id.main_update);
        	txtActionBar.setText(getResources().getString(R.string.list_order_titleord) + " " + Integer.toString(orderCount)
        			+ " " + getResources().getString(R.string.list_order_titlesum) + " " + s + totalWeightWhite);
        }
        else
        {
        	this.setTitle(getResources().getString(R.string.list_order_titleord) + " " + Integer.toString(orderCount)
        			+ " " + getResources().getString(R.string.list_order_titlesum) + " " + s + " " + totalWeight);
        }

        // if ListView is empty and base already was downloaded
        if ((mCursor.getCount()==0)&&(!newBase))
        {
        	// Show warning message
        	String strEmptyPeriod = String.format( "%02d.%02d.%04d - ", mDay, mMonth+1, mYear )
        						  + String.format( "%02d.%02d.%04d", mDayTo, mMonthTo+1, mYearTo )
        						  + "\n";
        	txtEmpty.setText(strEmptyPeriod + getResources().getString(R.string.list_order_empty));
        }

        // if DB is empty
        if (newBase)
        {
        	// Show warning message
        	txtEmpty.setText(getResources().getString(R.string.list_order_noBD));
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
    	      if (db_orders != null)
    	      {
    	        db_orders.close();
    	      }
    	      if (db != null)
			  {
				  db.close();
			  }
    	}
    	catch (Exception error)
    	{
    	      /** Error Handler Code */
    	}
        // Method onStop() can be called when another activity was just been started
    	// or this Activity interrupted, so we need reinit value listPosition to -1 (Nothing selected)
    	listPosition = -1;
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
	    if (db_orders != null)
	    {
	    	db_orders.close();
	    }
	    if (db != null)
		{
			db.close();
		}
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
    	// Program menu creation with 5 buttons
    	// its called by pressing MENU-button on device
    //	menu.add(Menu.NONE, IDM_ADD, Menu.NONE, R.string.list_order_new)
    //		.setIcon(R.drawable.ic_menu_add)
    //		.setAlphabeticShortcut('a');
    	boolean isXML = getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE)
				.getString(RsaDb.INTERFACEKEY, "DBF").equals("XML");
    	if (isXML == true) {
	    	menu.add(Menu.NONE, IDM_USER, Menu.NONE, R.string.list_order_user)
				.setIcon(R.drawable.ic_menu_cc)
				.setAlphabeticShortcut('u');
    	}
    	menu.add(Menu.NONE, IDM_MAP, Menu.NONE, R.string.list_order_map)
			.setIcon(R.drawable.ic_menu_map)
			.setEnabled(false)
    		.setAlphabeticShortcut('m');
    	menu.add(Menu.NONE, IDM_ABOUT, Menu.NONE, R.string.list_order_about)
    		.setIcon(R.drawable.ic_menu_edit)
			.setAlphabeticShortcut('h');
    	menu.add(Menu.NONE, IDM_SETTINGS, Menu.NONE, R.string.list_order_settings)
    		.setIcon(R.drawable.ic_menu_param)
			.setAlphabeticShortcut('n');
    //	menu.add(Menu.NONE, IDM_EXIT, Menu.NONE, R.string.list_order_exit)
	//		.setIcon(R.drawable.ic_menu_exit)
	//		.setAlphabeticShortcut('e');
    	return (super.onCreateOptionsMenu(menu));
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,ContextMenu.ContextMenuInfo menuInfo)
    {
    	boolean isXML = getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE)
									.getString(RsaDb.INTERFACEKEY, "DBF").equals("XML");
		boolean isEDebit = PreferenceManager.getDefaultSharedPreferences(this)
				.getBoolean("useExtdebit",false);
    	if (v == findViewById(R.id.main_blank_area)) {
    		menu.add(Menu.NONE, IDM_PERIOD, Menu.NONE, R.string.list_order_datefilter);
    		menu.add(Menu.NONE, IDM_DEBIT,  Menu.NONE, R.string.list_order_debit);
    		if (isXML==true && !isEDebit) menu.add(Menu.NONE, IDM_KASSA,  Menu.NONE, R.string.list_order_kassa);
    		menu.add(Menu.NONE, IDM_REPORT,  Menu.NONE, R.string.list_order_report);
    		menu.add(Menu.NONE, IDM_SETTINGS,  Menu.NONE, R.string.list_order_settings);
    		menu.add(Menu.NONE, IDM_USER,  	   Menu.NONE, R.string.list_order_user);
    		//if (isXML==true) menu.add(Menu.NONE, IDM_SALES,     Menu.NONE, R.string.list_order_sales);
    		menu.add(Menu.NONE, IDM_ABOUT,     Menu.NONE, R.string.list_order_about);
    		return;
    	}

    	if (v.getId() != getListView().getEmptyView().getId())
    	{
    	//	menu.add(Menu.NONE, IDM_ADD, 	Menu.NONE, R.string.list_order_neworder);
    	//	menu.add(Menu.NONE, IDM_MODIFY, Menu.NONE, R.string.list_order_modify);
    		menu.add(Menu.NONE, IDM_DELETE, Menu.NONE, R.string.list_order_delete);
    		menu.add(Menu.NONE, IDM_PERIOD, Menu.NONE, R.string.list_order_datefilter);
    		menu.add(Menu.NONE, IDM_DEBIT,  Menu.NONE, R.string.list_order_debit);
    		if (isXML==true && !isEDebit) menu.add(Menu.NONE, IDM_KASSA,  Menu.NONE, R.string.list_order_kassa);
    		menu.add(Menu.NONE, IDM_REPORT,  Menu.NONE, R.string.list_order_report);
    		menu.add(Menu.NONE, IDM_SETTINGS,  Menu.NONE, R.string.list_order_settings);
    		if (isXML==true) menu.add(Menu.NONE, IDM_COPY,  Menu.NONE, R.string.list_order_copy);
    		menu.add(Menu.NONE, IDM_USER,  	   Menu.NONE, R.string.list_order_user);
    		//if (isXML==true) menu.add(Menu.NONE, IDM_SALES,     Menu.NONE, R.string.list_order_sales);
    		menu.add(Menu.NONE, IDM_ABOUT,     Menu.NONE, R.string.list_order_about);
    	}
    	else if (!newBase)
    	{
    	//	menu.add(Menu.NONE, IDM_ADD, 	   Menu.NONE, R.string.list_order_neworder);
    		if (isXML==true && !isEDebit) menu.add(Menu.NONE, IDM_KASSA,     Menu.NONE, R.string.list_order_kassa);
    		menu.add(Menu.NONE, IDM_DEBIT,     Menu.NONE, R.string.list_order_debit);
    		menu.add(Menu.NONE, IDM_PERIOD,    Menu.NONE, R.string.list_order_datefilter);
    		menu.add(Menu.NONE, IDM_REPORT,    Menu.NONE, R.string.list_order_report);
    	//	menu.add(Menu.NONE, IDM_SYNC,      Menu.NONE, R.string.list_order_synchronization);
    		menu.add(Menu.NONE, IDM_SETTINGS,  Menu.NONE, R.string.list_order_settings);
    		menu.add(Menu.NONE, IDM_USER,  	   Menu.NONE, R.string.list_order_user);
    	//	if (isXML==true) menu.add(Menu.NONE, IDM_SALES,     Menu.NONE, R.string.list_order_sales);
    		menu.add(Menu.NONE, IDM_ABOUT,     Menu.NONE, R.string.list_order_about);
    	}
    	else
    	{
    		menu.add(Menu.NONE, IDM_SYNC,      Menu.NONE, R.string.list_order_sync);
    		menu.add(Menu.NONE, IDM_SETTINGS,  Menu.NONE, R.string.list_order_settings);
    		menu.add(Menu.NONE, IDM_USER,  	   Menu.NONE, R.string.list_order_user);
    		menu.add(Menu.NONE, IDM_ABOUT,     Menu.NONE, R.string.list_order_about);
    	}
    }

    /**
     *  Starts actions according to user choise in context menu
     *  @param item - Selected MenuItem, used to switch
     */
    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
    	AdapterView.AdapterContextMenuInfo info= (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

    	/**  Used to start another activities and send data for them */
    	final Intent intent = new Intent();
    	// Set index of selected position (listPosition) to position of just been selected item
    	// but if long click was in empty ListView then index = -1
    	listPosition = (info==null)?-1:info.position;

    	switch (item.getItemId())
    	{
       		case IDM_ADD:
       		{
				if (newBase)
				{
					Toast.makeText(getApplicationContext(),R.string.list_order_warning_noBD,Toast.LENGTH_LONG).show();
					break;
				}
			    // Preparing intent to start HeadActivity
			    intent.setClass(this, NewHeadActivity.class);

				// Set selection index to -1 (Nothing selected), because we going to leave the activity
				listPosition = -1;

				// Preparing data to transfer in new Activity (HeadActivity)
				// We just send information that user selected ADD-button
				// from program menu. This value can be get by MODE-key
				intent.putExtra("MODE", IDM_ADD);

				// Close database if it is opened, to allow use it in another activities
				if (db_orders != null)
	    	    {
					db_orders.close();
	    	    }
				if (db != null)
				{
					db.close();
				}

				startActivity(intent);
       			return(true);
       		}
       		case IDM_MODIFY:
       		{
				if (newBase)
				{
					Toast.makeText(getApplicationContext(),R.string.list_order_warning_noBD,Toast.LENGTH_LONG).show();
					break;
				}
				if (listPosition!=-1)
				{
			    	// Set mCursor position to selected item
			    	mCursor.moveToPosition(listPosition);

    				// Preparing intent to start HeadActivity
    				intent.setClass(this, NewHeadActivity.class);

    				// Set mCursor position to selected item
    				mCursor.moveToPosition(listPosition);

    				// Preparing data to transfer in new Activity (HeadActivity)
    				// We just send information that user selected MODIFY-button
    				// from program menu. This value can be get by MODE-key
    				intent.putExtra("MODE", IDM_MODIFY);
    				// also we sends _id of selected order from database table "HEAD"
    				intent.putExtra("_id", mCursor.getString(0));

    				// Close database if it is opened, to allow use it in another activities
    				if (db_orders != null)
   	    	        {
   	    	         	db_orders.close();
   	    	        }
    				if (db != null)
    				{
    					db.close();
    				}

    				// Set selection index to -1 (Nothing selected), because we going to leave the activity
    				listPosition = -1;

    				// Start HeadActivity.class to modify selected order
    				startActivity(intent);
				}
				else
				{
					// if listPosition = -1 then there is nothing selected and we shows pop-up message
			    	// and do nothing
					Toast.makeText(getApplicationContext(),R.string.list_order_warning_notselected,Toast.LENGTH_SHORT).show();
				}
       			return(true);
       		}
       		case IDM_DELETE:
       		{
				if (newBase)
				{
					Toast.makeText(getApplicationContext(),R.string.list_order_warning_noBD,Toast.LENGTH_LONG).show();
					break;
				}
			    if (listPosition!=-1) // if some order is selected then do:
			    {
			    	// Set mCursor position to selected item
			    	mCursor.moveToPosition(listPosition);

			    	// Ticket 13: if order is locked then don't delete and show message
			    	if (mCursor.getInt(11) == R.drawable.ic_locked_)
			    	{
			    		Toast.makeText(getApplicationContext(),R.string.list_order_warning_locked,Toast.LENGTH_LONG).show();
			    		break;
			    	}

				    /** Ticket 12: Get Shared Preferences to find actual DB name */
					SharedPreferences prefs = getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);
					// Ticket 12: Get actual db name from SharedPrefs
					String tmpName;
					// Ticket 12: Get 1C Data ACTUALDB name
			        tmpName = prefs.getString(RsaDb.ACTUALDBKEY, "new");
			        // Ticket 12: if DB is not empty do
			        if (!tmpName.equals("new"))
			        {
						/** Ticket 12: Used as stock data storage before save it to database */
						ContentValues stockval = new ContentValues();
						/** Ticket 12: Used to store calculated REST */
						float newRest;
						/** Ticket 12: Init database with 1C data with architecture that designed in RsaDbHelper.class */
						RsaDbHelper mDb = new RsaDbHelper(this, tmpName);
						// Ticket 12: Open 1C data Database
						db = mDb.getWritableDatabase();
			        	// Ticket 12: Get goods of current order from database table LINES to mCursorLines by call query:
			        	// Ticket 12: SELECT TABLE_LINES FROM mContent WHERE ZAKAZ_ID(LINES) = _id(HEAD)
			        	mCursorLines = db_orders.query(RsaDbHelper.TABLE_LINES, mContentLines,
												RsaDbHelper.LINES_ZAKAZ_ID + "='"
												+ mCursor.getString(0) +"'",
												null, null, null, null);

			        	if (mCursorLines.getCount()>0) {
				        	// Ticket 12: Init mCursorLines to work with it
				        	//19.12.2012 Romka
				        	if (verOS<3) startManagingCursor(mCursorLines);
				        	// Ticket 12: Before parsing
				        	mCursorLines.moveToFirst();
				        	// Ticket 12: Parsing table LINES in database for stock increase (TABLE GOODS)
				        	for (int i=0;i<mCursorLines.getCount();i++)
				        	{
				        		// Ticket 12: Select record with current GOOD to take stock rest of it
				        		mCursorGoods = db.query(RsaDbHelper.TABLE_GOODS, mContentGoods,
														RsaDbHelper.GOODS_ID + "='"
														+ mCursorLines.getString(2) + "'",  // Ticket 12: mCursorLines.getString(2) = LINES_GOODS_ID
														null, null, null, null);
				        		// Ticket 12: Init mCursorGoods to work with it
				        		//19.12.2012 Romka
				        		if (verOS<3) startManagingCursor(mCursorGoods);
				        		// Ticket 12: Move to first record in mCursorGoods if empty then don't increase stocks
				        		if (mCursorGoods.moveToFirst() == true)
				        		{	// Ticket 12: get "STOCK" and increase it with "QTY" of deleting orderm (CursorLines.getString(3) = LINES_QTY)
				        			newRest = Float.parseFloat(mCursorGoods.getString(2)) + Float.parseFloat(mCursorLines.getString(3));
				        			// Ticket 12: Stocks can't be less than 0
				        			if (newRest<0) newRest = 0;
				        			// Ticket 12: Put to special stock storage number of rest stock in deleting order
				        			stockval.put(RsaDbHelper.GOODS_REST,	Float.toString(newRest));
				        			// Ticket 12: Increase Database stocks with data from deleting order
				        			db.update(RsaDbHelper.TABLE_GOODS, stockval,
												RsaDbHelper.GOODS_ID + "='"+ mCursorLines.getString(2) +"'", null);
				        			// Ticket 12: Clear stock storage variable because we need it for next line
				        			stockval.clear();
				        		}
				        		// TICKET 12: Go to next record in database
				        		mCursorLines.moveToNext();
				        	}
				        	// Ticket 12: Cursor not needed now
				        	mCursorGoods.close();
			        	}
			        	// Ticket 12: Cursor not needed now
			        	mCursorLines.close();


			        	// 27.01.2013
				    	//if (db != null)
				    	//{
				    	//   db.close();
				    	//}
			        }

			    	// Delete order from table HEAD with same _id as in selected item of order list
			    	// DELETE TABLE_HEAD WHERE _id(TABLE_HEAD) = _id(mCursor)
			    	db_orders.delete(RsaDbHelper.TABLE_HEAD, "_id='"+mCursor.getString(0) +"'", null);
					// Delete all goods of selected order from database (LINES)
					db_orders.delete(RsaDbHelper.TABLE_LINES, RsaDbHelper.LINES_ZAKAZ_ID + "='"+ mCursor.getString(0) +"'", null);
					db_orders.delete(RsaDbHelper.TABLE_RESTS, RsaDbHelper.RESTS_ZAKAZ_ID + "='"+ mCursor.getString(0) +"'", null);

					// Set selection index to -1 (Nothing selected), because we just deleted
			    	// selected order
			    	listPosition = -1;

			    	// After deleting selected order we have to update displayed information
			    	// by call to updateList() method
			    	updateList();
			    }
			    else
			    {
			    	// if listPosition = -1 then there is nothing selected and we shows pop-up message
			    	// and do nothing
			    	Toast.makeText(getApplicationContext(),R.string.list_order_warning_notselected,Toast.LENGTH_SHORT).show();
			    }
       			return(true);
       		}
       		case IDM_PERIOD:
       		{
            	// set current date
            	Calendar c = Calendar.getInstance();
            	mYear2 = c.get(Calendar.YEAR);
            	mMonth2 = c.get(Calendar.MONTH);
            	mDay2 = c.get(Calendar.DAY_OF_MONTH);
            	mYear2To = c.get(Calendar.YEAR);
            	mMonth2To = c.get(Calendar.MONTH);
            	mDay2To = c.get(Calendar.DAY_OF_MONTH);
       			showDialog(IDM_PERIOD);
       			return(true);
       		}
       		case IDM_COPY:
       		{
       			if (newBase) {
					Toast.makeText(getApplicationContext(),R.string.list_order_warning_noBD,Toast.LENGTH_LONG).show();
					break;
				}
			    if (listPosition!=-1) {
			    	mCursor.moveToPosition(listPosition);
			    	showDialog(IDM_COPY);
	       			return(true);
			    }
			    return(true);
       		}
       		case IDM_REPORT:
       		{
       			// Preparing intent to start AboutActivity
				intent.setClass(this, ReportActivity.class);

				// Set selection index to -1 (Nothing selected), because we going to leave the activity
				listPosition = -1;

				// Close database if it is opened, to allow use it in another activities
				if (db_orders != null)
    	        {
    	         	db_orders.close();
    	        }
				if (db != null)
				{
					db.close();
				}

				startActivity(intent);
       			return(true);
       		}
       		case IDM_DEBIT:
       		{
				if (newBase)
				{
					Toast.makeText(getApplicationContext(),R.string.list_order_warning_noBD,Toast.LENGTH_LONG).show();
					break;
				}

				SharedPreferences screen_prefs = getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE);
				boolean isEDebit = PreferenceManager.getDefaultSharedPreferences(this)
						.getBoolean("useExtdebit",false);
				boolean isXML = screen_prefs.getString(RsaDb.INTERFACEKEY, "DBF").equals("XML");
			    intent.setClass(this, (isEDebit&&isXML)?DebitExtActivity.class:DebitActivity.class);

				// Set selection index to -1 (Nothing selected), because we going to leave the activity
				listPosition = -1;

				// Preparing data to transfer in new Activity (DebitActivity)
				// We just send information that user selected DEBIT-button
				// from program menu. This value can be get by MODE-key
				intent.putExtra("MODE", IDM_DEBIT);

				// Close database if it is opened, to allow use it in another activities
				if (db_orders != null)
	    	    {
					db_orders.close();
	    	    }
				if (db != null)
				{
					db.close();
				}

				startActivity(intent);
       			return(true);
       		}
       		case IDM_KASSA:
       		{
				if (newBase)
				{
					Toast.makeText(getApplicationContext(),R.string.list_order_warning_noBD,Toast.LENGTH_LONG).show();
					break;
				}
			    // Preparing intent to start DebitActivity
			    intent.setClass(this, KassaActivity.class);

				// Set selection index to -1 (Nothing selected), because we going to leave the activity
				listPosition = -1;

				// Preparing data to transfer in new Activity (DebitActivity)
				// We just send information that user selected DEBIT-button
				// from program menu. This value can be get by MODE-key
				intent.putExtra("MODE", IDM_KASSA);

				// Close database if it is opened, to allow use it in another activities
				if (db_orders != null)
	    	    {
					db_orders.close();
	    	    }
				if (db != null)
				{
					db.close();
				}

				startActivity(intent);
       			return(true);
       		}
       		case IDM_SYNC:
       		{
				/** Get array from res 0 - Email; 1 - DBF;*/
				String[] protArray = getResources().getStringArray(R.array.prefProtocol);
				/** Get preferences */
				SharedPreferences screen_prefs = getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE);
				// if protocol = "E-mail" then...
				if (screen_prefs.getString(RsaDb.PROTOCOLKEY, protArray[0]).equals(protArray[0]))
				{
					// Preparing intent to start SyncActivity with E-mail protocol
					intent.setClass(getApplicationContext(), SyncActivity.class);
				}
				else // if protocol = "FTP" then...
				{
					// Preparing intent to start SyncActivity with FTP protocol
					intent.setClass(getApplicationContext(), FtpSyncActivity.class);
				}

				// Set selection index to -1 (Nothing selected), because we going to leave the activity
				listPosition = -1;

				// Close database if it is opened, to allow use it in another activities
				if (db_orders != null)
				{
					db_orders.close();
				}

				if (db != null)
				{
					db.close();
				}

				startActivity(intent);
       			return(true);
       		}
       		case IDM_SETTINGS:
       		{
			    // Preparing intent to start SettingsActivity
				intent.setClass(this, Preferences.class);

				// Set selection index to -1 (Nothing selected), because we going to leave the activity
				listPosition = -1;

				// Close database if it is opened, to allow use it in another activities
				if (db_orders != null)
	    	    {
	    	       	db_orders.close();
	    	    }
				if (db != null)
				{
					db.close();
				}

				startActivity(intent);
       			return(true);
       		}
       		case IDM_USER:{
    			showDialog(IDM_USER);
    			return(true);
    		}
       		case IDM_ABOUT:{
					// Preparing intent to start AboutActivity
				intent.setClass(this, AboutActivity.class);

				// Set selection index to -1 (Nothing selected), because we going to leave the activity
				listPosition = -1;

				// Close database if it is opened, to allow use it in another activities
				if (db_orders != null)
    	        {
    	         	db_orders.close();
    	        }
				if (db != null)
				{
					db.close();
				}

				startActivity(intent);
				return(true);
			}
    	}
    	return(super.onOptionsItemSelected(item));
   }

    /**
     *  Starts actions according to user choise in program menu
     *  @param item - Selected MenuItem, used to switch
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
    	/**  Used to start another activities and send data for them */
    	final Intent intent = new Intent();

    	switch (item.getItemId())
    	{
    		case IDM_ADD:{
    				if (newBase) {
    					Toast.makeText(getApplicationContext(),R.string.list_order_warning_noBD,Toast.LENGTH_LONG).show();
    					break;
    				}
    			    intent.setClass(this, NewHeadActivity.class);
    				listPosition = -1;
    				intent.putExtra("MODE", IDM_ADD);

    				if (db_orders != null) {
   	    	         	db_orders.close();
   	    	        }
    				if (db != null) {
    					db.close();
    				}

    				startActivity(intent);
    				break;
    			}
    		case IDM_ABOUT:{
   					// Preparing intent to start AboutActivity
					intent.setClass(this, AboutActivity.class);

					// Set selection index to -1 (Nothing selected), because we going to leave the activity
					listPosition = -1;

					// Close database if it is opened, to allow use it in another activities
					if (db_orders != null)
	    	        {
	    	         	db_orders.close();
	    	        }
					if (db != null)
					{
						db.close();
					}

					startActivity(intent);
					break;
				}
    		case IDM_DELETE:{
					if (newBase)
					{
						Toast.makeText(getApplicationContext(),R.string.list_order_warning_noBD,Toast.LENGTH_LONG).show();
						break;
					}
    			    if (listPosition!=-1) // if some order is selected then do:
    			    {
    			    	// Set mCursor position to selected item
    			    	mCursor.moveToPosition(listPosition);

    			    	// Ticket 13: if order is locked then don't delete and show message
    			    	if (mCursor.getInt(11) == R.drawable.ic_locked_)
    			    	{
    			    		Toast.makeText(getApplicationContext(),R.string.list_order_warning_locked,Toast.LENGTH_LONG).show();
    			    		break;
    			    	}

    				    /** Ticket 12: Get Shared Preferences to find actual DB name */
    					SharedPreferences prefs = getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);
    					// Ticket 12: Get actual db name from SharedPrefs
    					String tmpName;
    					// Ticket 12: Get 1C Data ACTUALDB name
    			        tmpName = prefs.getString(RsaDb.ACTUALDBKEY, "new");
    			        // Ticket 12: if DB is not empty do
    			        if (!tmpName.equals("new"))
    			        {
    						/** Ticket 12: Used as stock data storage before save it to database */
    						ContentValues stockval = new ContentValues();
    						/** Ticket 12: Used to store calculated REST */
    						float newRest;
    						/** Ticket 12: Init database with 1C data with architecture that designed in RsaDbHelper.class */
    						RsaDbHelper mDb = new RsaDbHelper(this, tmpName);
    						// Ticket 12: Open 1C data Database
    						db = mDb.getWritableDatabase();
    			        	// Ticket 12: Get goods of current order from database table LINES to mCursorLines by call query:
    			        	// Ticket 12: SELECT TABLE_LINES FROM mContent WHERE ZAKAZ_ID(LINES) = _id(HEAD)
    			        	mCursorLines = db_orders.query(RsaDbHelper.TABLE_LINES, mContentLines,
    												RsaDbHelper.LINES_ZAKAZ_ID + "='"
    												+ mCursor.getString(0) +"'",
    												null, null, null, null);
    			        	// Ticket 12: Init mCursorLines to work with it
    			        	//19.12.2012 Romka
    			        	if (verOS<3) startManagingCursor(mCursorLines);
    			        	// Ticket 12: Before parsing
    			        	mCursorLines.moveToFirst();
    			        	// Ticket 12: Parsing table LINES in database for stock increase (TABLE GOODS)
    			        	for (int i=0;i<mCursorLines.getCount();i++)
    			        	{
    			        		// Ticket 12: Select record with current GOOD to take stock rest of it
    			        		mCursorGoods = db.query(RsaDbHelper.TABLE_GOODS, mContentGoods,
    													RsaDbHelper.GOODS_ID + "='"
    													+ mCursorLines.getString(2) + "'",  // Ticket 12: mCursorLines.getString(2) = LINES_GOODS_ID
    													null, null, null, null);
    			        		// Ticket 12: Init mCursorGoods to work with it
    			        		//19.12.2012 Romka
    			        		if (verOS<3) startManagingCursor(mCursorGoods);
    			        		// Ticket 12: Move to first record in mCursorGoods if empty then don't increase stocks
    			        		if (mCursorGoods.moveToFirst() == true)
    			        		{	// Ticket 12: get "STOCK" and increase it with "QTY" of deleting orderm (CursorLines.getString(3) = LINES_QTY)
    			        			newRest = Float.parseFloat(mCursorGoods.getString(2)) + Float.parseFloat(mCursorLines.getString(3));
    			        			// Ticket 12: Stocks can't be less than 0
    			        			if (newRest<0) newRest = 0;
    			        			// Ticket 12: Put to special stock storage number of rest stock in deleting order
    			        			stockval.put(RsaDbHelper.GOODS_REST,	Float.toString(newRest));
    			        			// Ticket 12: Increase Database stocks with data from deleting order
    			        			db.update(RsaDbHelper.TABLE_GOODS, stockval,
    											RsaDbHelper.GOODS_ID + "='"+ mCursorLines.getString(2) +"'", null);
    			        			// Ticket 12: Clear stock storage variable because we need it for next line
    			        			stockval.clear();
    			        		}
    			        		// TICKET 12: Go to next record in database
    			        		mCursorLines.moveToNext();
    			        	}
    			        	// Ticket 12: Cursor not needed now
    			        	mCursorLines.close();
    			        	// Ticket 12: Cursor not needed now
    			        	mCursorGoods.close();
    			        	// Ticket 12: Close 1c data Database because not needed
    			    	    if (db != null)
    			    	    {
    			    	        db.close();
    			    	    }
    			        }

    			    	// Delete order from table HEAD with same _id as in selected item of order list
    			    	// DELETE TABLE_HEAD WHERE _id(TABLE_HEAD) = _id(mCursor)
    			    	db_orders.delete(RsaDbHelper.TABLE_HEAD, "_id='"+mCursor.getString(0) +"'", null);

    			    	// Delete all goods of selected order from database (LINES)
    					db_orders.delete(RsaDbHelper.TABLE_LINES, RsaDbHelper.LINES_ZAKAZ_ID + "='"+ mCursor.getString(0) +"'", null);

    			    	// Set selection index to -1 (Nothing selected), because we just deleted
    			    	// selected order
    			    	listPosition = -1;

    			    	// After deleting selected order we have to update displayed information
    			    	// by call to updateList() method
    			    	updateList();
    			    }
    			    else
    			    {
    			    	// if listPosition = -1 then there is nothing selected and we shows pop-up message
    			    	// and do nothing
    			    	Toast.makeText(getApplicationContext(),R.string.list_order_warning_notselected,Toast.LENGTH_SHORT).show();
    			    }
    				break;
				}
    		case IDM_SYNC:{
					/** Get array from res 0 - Email; 1 - DBF;*/
					String[] protArray = getResources().getStringArray(R.array.prefProtocol);
					/** Get preferences */
					SharedPreferences screen_prefs = getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE);
					// if protocol = "E-mail" then...
					if (screen_prefs.getString(RsaDb.PROTOCOLKEY, protArray[0]).equals(protArray[0]))
					{
						// Preparing intent to start SyncActivity with E-mail protocol
						intent.setClass(getApplicationContext(), SyncActivity.class);
					}
					else // if protocol = "FTP" then...
					{
						// Preparing intent to start SyncActivity with FTP protocol
						intent.setClass(getApplicationContext(), FtpSyncActivity.class);
					}

					// Set selection index to -1 (Nothing selected), because we going to leave the activity
					listPosition = -1;

					// Close database if it is opened, to allow use it in another activities
					if (db_orders != null)
					{
						db_orders.close();
					}

					if (db != null)
					{
						db.close();
					}

					startActivity(intent);
    				break;
			}
    		case IDM_USER:{
    			showDialog(IDM_USER);
    			break;
    		}
    		case IDM_MAP:{
				break;
			}
    		case IDM_EXIT:{
		    	// Just quit from programm
				finish();
				break;
			}
    		case IDM_SETTINGS:{
    			    // Preparing intent to start PreferencesActivity
    				intent.setClass(this, Preferences.class);

    				// Set selection index to -1 (Nothing selected), because we going to leave the activity
    				listPosition = -1;

    				// Close database if it is opened, to allow use it in another activities
    				if (db_orders != null)
   	    	        {
   	    	         	db_orders.close();
   	    	        }
    				if (db != null)
    				{
    					db.close();
    				}

    				startActivity(intent);
    				break;
    			}
    		default:
    			// If nothing selected then nothing to do...
    			return false;
    	}
    	return true;
    }

    /**
     * Makes item selection, and shows pop-up message with Client Name of selected order
     * @param parent - In this param system puts pointer to ListView of Activity
     * @param v - In this param system puts pointer to selected View, used to apply selection
     * @param position - In this param system puts index of selected order, used to set {@value} listPosition
     * @param id - In this param system puts id of selected View
     */
    public void onListItemClick(ListView parent, View v, int position, long id) {
    	if (newBase) {
			return;
		}
    	listPosition = position;
    	mCursor.moveToPosition(listPosition);

    	final Intent intent = new Intent();

		if (listPosition!=-1) {
	    	mCursor.moveToPosition(listPosition);
			intent.setClass(this, NewHeadActivity.class);
			mCursor.moveToPosition(listPosition);
			intent.putExtra("MODE", IDM_MODIFY);
			intent.putExtra("_id", mCursor.getString(0));
			if (db_orders != null) {
   	         	db_orders.close();
   	        }
			if (db != null) {
				db.close();
			}
			listPosition = -1;
			startActivity(intent);
		}
	}

    /**
     * Build DataPicker dialog..
     * @param id Provide info about witch kind of dialogs it must build
     */
    @Override
    protected Dialog onCreateDialog(int id)
    {
    	switch(id)
    	{
    		case IDM_PERIOD:
    		{
    				return new DatePickerDialog(this, mDateSetListener,
    										mYear2, mMonth2, mDay2);
    		}
    		case IDM_PERIODTO:
    		{
    				return new DatePickerDialog(this, mDateToSetListener,
							mYear2To, mMonth2To, mDay2To);
    		}
    		case IDM_COPY:
    		{
    			Calendar c = Calendar.getInstance();
    			return new DatePickerDialog(this, mDateCopySetListener,
    					c.get(Calendar.YEAR),
    					c.get(Calendar.MONTH),
    					c.get(Calendar.DAY_OF_MONTH));
    		}
    		case IDM_UPGRADING:
    		{
    			updProgressDialog = new ProgressDialog(RSAActivity.this);
    			updProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    			updProgressDialog.setMessage(getResources().getString(R.string.list_order_update_process));
    			updProgressDialog.setCancelable(false);

    			updThread = new UpgradingThread(updHandler, this);
    			updThread.start();
    			//doUpdate();
    			return updProgressDialog;
    		}
    		case IDM_USER: {
    			SettingsDbHelper sDb = new SettingsDbHelper(this, SettingsDbHelper.DB_NAME);
    			db_settings = sDb.getWritableDatabase();
    			String q = "select FIO, CODE, ORDERBY, LASTSKLAD, LASTSKLADID, LASTVAT, LASTOPTIM, LOGIN, PASSWORD, SMTP, SMTPPORT, POP, POPPORT, SENDTO, " +
    					   "FTPSERVER, FTPUSER, FTPPASSWORD, FTPPORT, FTPINBOX, FTPOUTBOX, ACTUALDBKEY " +
    					   "from _settings limit 4";
    			settingsCursor = db_settings.rawQuery(q, null);

    			settingsCursor.moveToFirst();

    			List<Map<String, ?>> items = new ArrayList<Map<String, ?>>();

    			String[] arrayBases = getResources().getStringArray(R.array.Databases);
    			for (String s : arrayBases) {
    				Map<String, Object> map = new HashMap<String, Object>();
	                map.put("NAME_TEXT", s + " " + settingsCursor.getString(0));
	                items.add(map);
	                settingsCursor.moveToNext();
    			}
    			final SimpleAdapter custAdapter = new SimpleAdapter(this, items, R.layout.list_report_cust,
    					new String[]  {"NAME_TEXT"},
						new int[] {android.R.id.text1});

    			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			    builder.setTitle(R.string.list_order_dbselect)
			    	   .setAdapter(custAdapter, new DialogInterface.OnClickListener() {
			               public void onClick(DialogInterface dialog, int which) {
			            	   SharedPreferences screen_prefs = getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE);
			            	   int index = screen_prefs.getInt(RsaDb.DBPREFIXKEY, 0);
			            	   if (index == which)
			            		   return;
			            	   savePreferencesToDb(db_settings, index+1, getApplicationContext());
			            	   screen_prefs.edit().putInt(RsaDb.DBPREFIXKEY, which).commit();
			            	   loadPreferencesFromDb(db_settings, which+1, getApplicationContext());
			            	   onStop();
			            	   onStart();
			           }
			    });
			    return builder.create();
    		}
    		case IDM_NEWVERSION:
    		{
    			AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(getResources().getString(R.string.list_order_update_title) + " " + versionSite);
				builder.setMessage(getResources().getString(R.string.list_order_update_message));

				builder.setPositiveButton(getResources().getString(R.string.list_order_update_yes), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						showDialog(IDM_UPGRADING);
					}
				});
				builder.setNegativeButton(getResources().getString(R.string.list_order_update_cancel), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
					}
				});

				return builder.create();
    		}
    		case IDM_GPS:
    		{
    			final AlertDialog.Builder builder = new AlertDialog.Builder(this);
    		    builder.setMessage("GPS ДОЛЖЕН БЫТЬ АКТИВЕН")
    		            .setCancelable(false).setPositiveButton("ОК",
    		                    new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which)
									{
										String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
    		                        	if(!provider.contains("gps"))
    		                        	{
    		                            	Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
    		                            	startActivity(intent);
    		                        	}
									}
    		                    });
    		    final AlertDialog alert = builder.create();
    		    alert.show();
    		}
    	}
		return null;
    }

	/**
	 * Runs everytime when Dialog called to show()
	 * @param id constant identifier of kind of dialog
	 * @param dialog pointer to Dialog object
	 */
	@Override
	protected void onPrepareDialog(int id, Dialog dialog)
	{
    	switch(id)
    	{
    		case IDM_PERIOD:
    		{
    				((DatePickerDialog) dialog).updateDate(mYear2, mMonth2, mDay2);
    				dialog.setTitle("Выберите начало периода");
    				break;
    		}
    		case IDM_PERIODTO:
    		{
    				((DatePickerDialog) dialog).updateDate(mYear2To, mMonth2To, mDay2To);
    				dialog.setTitle("Выберите конец периода");
    				break;
    		}
    		case IDM_COPY:
    		{
    				Calendar c = Calendar.getInstance();
    				((DatePickerDialog) dialog).updateDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
    				dialog.setTitle("Копировать на дату");
    				break;
    		}
    		case IDM_NEWVERSION:
    		{
    			dialog.setTitle(getResources().getString(R.string.list_order_update_title) + " " + versionSite);
                break;
    		}
    		case IDM_USER: {
    			SettingsDbHelper sDb = new SettingsDbHelper(this, SettingsDbHelper.DB_NAME);
    			db_settings = sDb.getWritableDatabase();
    			String q = "select FIO, CODE, ORDERBY, LASTSKLAD, LASTSKLADID, LASTVAT, LASTOPTIM, LOGIN, PASSWORD, SMTP, SMTPPORT, POP, POPPORT, SENDTO, " +
    					   "FTPSERVER, FTPUSER, FTPPASSWORD, FTPPORT, FTPINBOX, FTPOUTBOX, ACTUALDBKEY " +
    					   "from _settings limit 4";
    			settingsCursor = db_settings.rawQuery(q, null);

    			settingsCursor.moveToFirst();

    			List<Map<String, ?>> items = new ArrayList<Map<String, ?>>();

    			String[] arrayBases = getResources().getStringArray(R.array.Databases);
    			for (String s : arrayBases) {
    				Map<String, Object> map = new HashMap<String, Object>();
	                map.put("NAME_TEXT", s + " " + settingsCursor.getString(0));
	                items.add(map);
	                settingsCursor.moveToNext();
    			}
    			final SimpleAdapter custAdapter = new SimpleAdapter(this, items, R.layout.list_report_cust,
    					new String[]  {"NAME_TEXT"},
						new int[] {android.R.id.text1});

    			((AlertDialog)dialog).getListView().setAdapter(custAdapter);
    			break;
    		}
    	}
	}

	/**
	 *  Saving activity positions on destroy .. or if display rotation
	 */
	@Override
	protected void onSaveInstanceState (Bundle outState)
	{
		super.onSaveInstanceState(outState);

		// put date
		outState.putInt("YEAR", mYear);
		outState.putInt("MONTH", mMonth);
		outState.putInt("DAY", mDay);
		outState.putInt("YEAR2", mYear2);
		outState.putInt("MONTH2", mMonth2);
		outState.putInt("DAY2", mDay2);

		outState.putInt("YEARTO", mYearTo);
		outState.putInt("MONTHTO", mMonthTo);
		outState.putInt("DAYTO", mDayTo);
		outState.putInt("YEAR2TO", mYear2To);
		outState.putInt("MONTH2TO", mMonth2To);
		outState.putInt("DAY2TO", mDay2To);

	}

	/**
	 * Shrinking Coord Database
	 * @param days Actual info
	 */
	private void shrinkCoordDb(int days)
	{
		Calendar sC = Calendar.getInstance();
		sC.add(Calendar.DAY_OF_YEAR, -days);
		String sDate = String.format( "%04d-%02d-%02d", sC.get(Calendar.YEAR), sC.get(Calendar.MONTH)+1, sC.get(Calendar.DAY_OF_MONTH) );
		getContentResolver().delete(CoordProvider.CONTENT_URI,
										   CoordDbHelper.DATE + " BETWEEN date('2000-01-01') AND date('"
										+  sDate + "')", null);
	}

	@SuppressLint("SimpleDateFormat")
	public static void shrinkOrders(Context context, int days, int kas_days) {
		try {
			RsaDbHelper 		mDb_ord	  = new RsaDbHelper(context, RsaDbHelper.DB_ORDERS);
			SQLiteDatabase 		db_orders = mDb_ord.getWritableDatabase();

			Calendar 			sC	= Calendar.getInstance();
			SimpleDateFormat	fmt	= new SimpleDateFormat("yyyy-MM-dd");
			if (days == -1) {
                db_orders.beginTransaction();
                try {
                    db_orders.execSQL("delete from _lines");
                    db_orders.execSQL("delete from _rests");
                    db_orders.execSQL("delete from _head");
                    db_orders.setTransactionSuccessful();
                } catch(Exception d) {}
                finally {
                    db_orders.endTransaction();
                }
				days=1;
			}
			sC.add(Calendar.DAY_OF_YEAR, -days);
			String sDate = fmt.format(sC.getTime());
			sC = Calendar.getInstance();
            if (kas_days == -1) {
                db_orders.beginTransaction();
                try {
                    db_orders.execSQL("delete from _kassa");
                    db_orders.execSQL("delete from _kassadet");
                    db_orders.setTransactionSuccessful();
                } catch(Exception d) {}
                finally {
                    db_orders.endTransaction();
                }
                kas_days = 1;
            }

            sC.add(Calendar.DAY_OF_YEAR, -kas_days);
			String kasSDate = fmt.format(sC.getTime());
			db_orders.beginTransaction();
			try {
				db_orders.execSQL("delete from _kassa where DATE < '"+kasSDate+"'");
				db_orders.execSQL("delete from _kassadet where DATE < '"+kasSDate+"'");

				db_orders.execSQL("delete from _lines where ZAKAZ_ID in (select _id from _head where SDATE < '"+sDate+"')");
				db_orders.execSQL("delete from _rests where ZAKAZ_ID in (select _id from _head where SDATE < '"+sDate+"')");
				db_orders.execSQL("delete from _head where SDATE < '"+sDate+"'");

				db_orders.setTransactionSuccessful();
			} catch(Exception d) {
				Log.d("RRR",d.getMessage());
			}
			finally {
				db_orders.endTransaction();
			}

			if (db_orders!=null && db_orders.isOpen()) {
    	      	db_orders.close();
    	    }
		} catch (Exception e) {
			Log.i("ROMKA","Error Shrinking Orders");
		}
	}


    /** The callback received when the user "sets" the date in the dialog */
    private DatePickerDialog.OnDateSetListener mDateSetListener =
            new DatePickerDialog.OnDateSetListener()
    {
		@Override
		public void onDateSet(DatePicker view, int year,
								int monthOfYear, int dayOfMonth)
		{
				mYear = year;
	            mMonth = monthOfYear;
	            mDay = dayOfMonth;
	            showDialog(IDM_PERIODTO);
		}
	};

	private DatePickerDialog.OnDateSetListener mDateToSetListener =
            new DatePickerDialog.OnDateSetListener()
    {
		@Override
		public void onDateSet(DatePicker view, int year,
								int monthOfYear, int dayOfMonth)
		{
        		mYearTo = year;
	            mMonthTo = monthOfYear;
	            mDayTo = dayOfMonth;

	            updateList();
		}
	};

	private DatePickerDialog.OnDateSetListener mDateCopySetListener =
            new DatePickerDialog.OnDateSetListener()
    {
		@Override
		public void onDateSet(DatePicker view, int year,
								int monthOfYear, int dayOfMonth)
		{
	            Calendar c = Calendar.getInstance();
				String date = String.format( "%02d.%02d.%02d", dayOfMonth, monthOfYear+1, year );
				String sdate = String.format( "%04d-%02d-%02d", year, monthOfYear+1, dayOfMonth ) + "'";
				String time = String.format( "%02d:%02d", c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE) );



		    	try {
			    	if (year<=c.get(Calendar.YEAR)) {
			    		if (monthOfYear<=c.get(Calendar.MONTH)) {
			    			if (dayOfMonth<c.get(Calendar.DAY_OF_MONTH)) {
					    		Toast.makeText(getApplicationContext(), "Нельзя копировать в прошлое!", Toast.LENGTH_LONG).show();
					    		return;
			    			}
			    		}
			    	}
		    	} catch (Exception e) {}

		    	sdate = ", '" + sdate;
	            String id = mCursor.getString(0);
	            String fields = "ID, ZAKAZ_ID, CUST_ID, SHOP_ID, SKLAD_ID, CUST_TEXT, SHOP_TEXT, SKLAD_TEXT, " +
	            				"DELAY, PAYTYPE, HSUMO, HWEIGHT, HVOLUME, HNDS, HNDSRATE, SUMWONDS, REMARK, ROUTECODE, " +
	            				"VISITID, NUM1C, DELIVERY";
	            String aditional_fields = ", BLOCK, SENDED, MONITORED, DATE, SDATE, TIME, NUMFULL, GPSCOORD";

	            String gpsStatus = "";
	            String gpscoord = "";
	            if (getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE).getBoolean(RsaDb.GPSKEY, false)==true) {
		            try {
		            	LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
						long[] l  = GpsUtils.getGPS(lm);
						if (l != null) {
							gpscoord = Long.toString(l[0]) + " " + Long.toString(l[1]);
						} else {
							gpscoord = "1";
						}
		            } catch (Exception e) {
		            	gpscoord = "2";
		            }
	            } else {
	            	gpscoord = "0";
	            }


	            try  {
					String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
	    		    if((!provider.contains("gps"))&&(getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE).getBoolean(RsaDb.GPSKEY, false)==true))
	    		    		gpsStatus = "_G";
				} catch (Exception e) {
					Toast.makeText(getApplicationContext(), "Error: 4> Не смог определить координаты", Toast.LENGTH_LONG).show();
				}
	            String block = ", " + Integer.toString(R.drawable.ic_unlocked_);
	            String sended = ", '0'";
	            String monitored = ", 0";
	            //String num1c = ", '0'";
	            String numfull = ", '_" + date + "_" + time + gpsStatus + "'";
	            gpscoord = ", '" + gpscoord + "'";

	            String changed_aditional_fields = block + sended + monitored + ", '" + date + "'"+ sdate + ", '" + time + "'" + numfull + gpscoord;
	          //  String qu = "create table if not exists _tmphead as select * from _head where _id='" + id + "'; " +
	           // 			"insert into _head("+ fields + aditional_fields +") " +
	           // 			"select " + fields + changed_aditional_fields + " from _tmphead; " +
	           // 			"drop table if exists _tmphead;";
	            String q1 = "create temp table if not exists _tmphead as select * from _head where _id='" + id + "'";
	            String q2 = "insert into _head("+ fields + aditional_fields +") " +
            					"select " + fields + changed_aditional_fields + " from _tmphead ";
	            String q3 = "drop table if exists _tmphead";

	            try {
	            	db_orders.execSQL(q1);
	            	db_orders.execSQL(q2);
	            	db_orders.execSQL(q3);

	            	Cursor cur = db_orders.rawQuery("SELECT last_insert_rowid()", null);
	            	if (cur.getCount()>0) {
	            		cur.moveToFirst();
	            		copyLines(db_orders, Integer.toString(cur.getInt(0)), id);
	            		if (cur!= null) cur.close();
	            	}

	            } catch(Exception e) {
	            	e.printStackTrace();
	            	Toast.makeText(getApplicationContext(),R.string.list_order_error_copy,Toast.LENGTH_SHORT).show();
	            }

	            Toast.makeText(getApplicationContext(),R.string.list_order_succ_copy,Toast.LENGTH_SHORT).show();
	            updateList();
		}
	};

	private void copyLines(SQLiteDatabase dbo, String id, String old_id) {
		String fields = "ID, GOODS_ID, TEXT_GOODS, RESTCUST, QTY, UN, COEFF, DISCOUNT, PRICEWNDS, " +
						"SUMWNDS, PRICEWONDS, SUMWONDS, NDS, DELAY, WEIGHT";
		String aditional_fields = ", ZAKAZ_ID";

		String q1 = "create temp table if not exists _tmplines as select * from _lines where ZAKAZ_ID='" + old_id + "'";
        String q2 = "insert into _lines("+ fields + aditional_fields +") " +
    					"select " + fields + ", '" + id + "' from _tmplines ";
        String q3 = "drop table if exists _tmplines";

        Log.d("RRR", q1+q2+q3);

        dbo.execSQL(q1);
        dbo.execSQL(q2);
        dbo.execSQL(q3);

	}

	private void savePreferencesToDb(SQLiteDatabase sdb, int index, Context c) {
		SharedPreferences pr 	  = c.getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);
		SharedPreferences pr_main = c.getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE);

		ContentValues values = new ContentValues();
		values.put(SettingsDbHelper.SETTINGS_FIO,			pr_main.getString(RsaDb.NAMEKEY, 		"Не указано"));
		values.put(SettingsDbHelper.SETTINGS_CODE,			pr_main.getString(RsaDb.CODEKEY, 		"Не указано"));
		values.put(SettingsDbHelper.SETTINGS_ORDERBY,		pr_main.getString(RsaDb.ORDERBYKEY, 	"Без сортировки"));
		values.put(SettingsDbHelper.SETTINGS_LASTSKLAD,		pr_main.getString(RsaDb.LASTSKLADNAME, 	"x"));
		values.put(SettingsDbHelper.SETTINGS_LASTSKLADID,	pr_main.getString(RsaDb.LASTSKLADID, 	"x"));
		values.put(SettingsDbHelper.SETTINGS_LASTVAT,		pr_main.getString(RsaDb.LASTVATKEY, 	"1"));
		values.put(SettingsDbHelper.SETTINGS_LASTOPTIM,		pr_main.getString(RsaDb.LASTOPTIMKEY, 	"Не выполнялось"));
		values.put(SettingsDbHelper.SETTINGS_LOGIN,			pr.getString(RsaDb.EMAILKEY, 			""));
		values.put(SettingsDbHelper.SETTINGS_PASSWORD,		pr.getString(RsaDb.PASSWORDKEY,			""));
		values.put(SettingsDbHelper.SETTINGS_SMTP,			pr.getString(RsaDb.SMTPKEY,				""));
		values.put(SettingsDbHelper.SETTINGS_SMTPPORT,		pr.getString(RsaDb.SMTPPORTKEY,			""));
		values.put(SettingsDbHelper.SETTINGS_POP,			pr.getString(RsaDb.POPKEY,				""));
		values.put(SettingsDbHelper.SETTINGS_POPPORT,		pr.getString(RsaDb.POPPORTKEY,			""));
		values.put(SettingsDbHelper.SETTINGS_SENDTO,		pr.getString(RsaDb.SENDTOKEY,			""));

		values.put(SettingsDbHelper.FTPSERVER,		pr.getString(RsaDb.FTPSERVER,			""));
		values.put(SettingsDbHelper.FTPUSER,		pr.getString(RsaDb.FTPUSER,			""));
		values.put(SettingsDbHelper.FTPPASSWORD,	pr.getString(RsaDb.FTPPASSWORD,		""));
		values.put(SettingsDbHelper.FTPPORT,		pr.getString(RsaDb.FTPPORT,			""));
		values.put(SettingsDbHelper.FTPINBOX,		pr.getString(RsaDb.FTPINBOX,			""));
		values.put(SettingsDbHelper.FTPOUTBOX,		pr.getString(RsaDb.FTPOUTBOX,			""));
		values.put(SettingsDbHelper.ACTUALDBKEY,	pr.getString(RsaDb.ACTUALDBKEY,			""));

		sdb.update(SettingsDbHelper.TABLE_SETTINGS, values, "ID='" + Integer.toString(index) + "'", null);
	}

	private void loadPreferencesFromDb(SQLiteDatabase sdb, int index, Context c) {
		SharedPreferences pr 	  = c.getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);
		SharedPreferences pr_main = c.getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE);

		settingsCursor.moveToPosition(index-1);
		pr_main.edit().putString(RsaDb.NAMEKEY, 		settingsCursor.getString(0)).commit();
		pr_main.edit().putString(RsaDb.CODEKEY, 		settingsCursor.getString(1)).commit();
		pr_main.edit().putString(RsaDb.ORDERBYKEY, 		settingsCursor.getString(2)).commit();
		pr_main.edit().putString(RsaDb.LASTSKLADNAME, 	settingsCursor.getString(3)).commit();
		pr_main.edit().putString(RsaDb.LASTSKLADID, 	settingsCursor.getString(4)).commit();
		pr_main.edit().putString(RsaDb.LASTVATKEY, 		settingsCursor.getString(5)).commit();
		pr_main.edit().putString(RsaDb.LASTOPTIMKEY, 	settingsCursor.getString(6)).commit();
		pr.edit().putString(RsaDb.EMAILKEY, 			settingsCursor.getString(7)).commit();
		pr.edit().putString(RsaDb.PASSWORDKEY,			settingsCursor.getString(8)).commit();
		pr.edit().putString(RsaDb.SMTPKEY,				settingsCursor.getString(9)).commit();
		pr.edit().putString(RsaDb.SMTPPORTKEY,			settingsCursor.getString(10)).commit();
		pr.edit().putString(RsaDb.POPKEY,				settingsCursor.getString(11)).commit();
		pr.edit().putString(RsaDb.POPPORTKEY,			settingsCursor.getString(12)).commit();
		pr.edit().putString(RsaDb.SENDTOKEY,			settingsCursor.getString(13)).commit();

		pr.edit().putString(RsaDb.FTPSERVER,		settingsCursor.getString(14)).commit();
		pr.edit().putString(RsaDb.FTPUSER,			settingsCursor.getString(15)).commit();
		pr.edit().putString(RsaDb.FTPPASSWORD,		settingsCursor.getString(16)).commit();
		pr.edit().putString(RsaDb.FTPPORT,			settingsCursor.getString(17)).commit();
		pr.edit().putString(RsaDb.FTPINBOX,			settingsCursor.getString(18)).commit();
		pr.edit().putString(RsaDb.FTPOUTBOX,		settingsCursor.getString(19)).commit();
		pr.edit().putString(RsaDb.ACTUALDBKEY,		settingsCursor.getString(20)).commit();

		try {
			if (this.settingsCursor != null) {
				this.settingsCursor.close();
			}
			if (this.db_settings != null) {
				this.db_settings.close();
			}
		} catch (Exception e) {}

	}

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

	private String checkForReturnInvoice(String s_num1c, String s_pt, myInt ii)
	{
		if ((s_num1c==null) || (s_num1c.equals("0")) || (s_num1c.equals("")))
			return s_pt;
		if (s_num1c.contains("1")) {
			ii.set(Color.parseColor(lightTheme?"#30000000":"#30FFFFFF"));
			return "ВЗРТ";
		}

		ii.set(Color.parseColor(lightTheme?"#30000000":"#30FFFFFF"));
		return "ВЗРТ";
		// return s_pt;
	}

}