package ru.by.rsa;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import ru.by.rsa.external.javadbf.JDBFException;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Activity used for sending orders for selected day
 * @author Komarev Roman
 * Odessa, neo3da@mail.ru, +380503412392
 */
public class SendActivity extends Activity implements Runnable
{
	/** Used for day selection. All orders from that day will be sended by email  */
	DatePicker mPicker;
	/** Send email by button press */
	Button btnSend;
	/** TextView. used like console to show status of sending mail */
	TextView mText;
	/** SQLite database that stores all data */
	static SQLiteDatabase db;
	/** For store pointer to this activity */
	Runnable mAct;
	Activity mAct2;
	/** Thread for background downloading */
	static public Thread background;
	/** Used to send messages from Thread to main Thread */
	static Handler mHandler;
    /** Get Shared Preferences and fill special field of message with it */
	SharedPreferences prefs;
	/** JavaMail variable */
	Mail mMessage;
	/** Context for some methods */
    Context context;
	/** Path to application file storage */
	String appPath;
	/** Init database with architecture that designed in RsaDbHelper.class */
	static RsaDbHelper mDb;
	/** Current date */
	String curDate;
	CheckBox chkPhoto;
	CheckBox chkNew;
	boolean useGPS;
	
    /** Orders count */
    int countOrders;
    /** Current theme */
	private boolean lightTheme;
    
    public void onCreate(Bundle savedInstanceState) 
    {
    	lightTheme = getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE).getBoolean(RsaDb.LIGHTTHEMEKEY, false);
    	useGPS = getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE).getBoolean(RsaDb.GPSKEY, false);
    //	if (lightTheme) {
	//		setTheme(R.style.Theme_Custom);
	//	} else {
	//		setTheme(R.style.Theme_CustomBlack);
	//	}
    	
    	if (lightTheme) {
    		setTheme(R.style.Theme_Custom);
    	   	super.onCreate(savedInstanceState);
    	   	setContentView(R.layout.send);
    	} else {
    		setTheme(R.style.Theme_CustomBlack2);
    	   	super.onCreate(savedInstanceState);
    	   	setContentView(R.layout.send);
    	}
    	
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        
    	mAct = this;
    	mAct2 = this;

    	// Bind variables with xml elements
    	mPicker 				= (DatePicker)findViewById(R.id.datePicker_send);
    	btnSend 				= (Button)findViewById(R.id.btnSend_send);
    	final TextView txtLog	= (TextView)findViewById(R.id.txtLog_send);
    	chkPhoto 				= (CheckBox)findViewById(R.id.chkPhoto);
    	chkNew 					= (CheckBox)findViewById(R.id.chkNew);
    	
    	String _iface = getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE)
    						.getString(RsaDb.INTERFACEKEY, "DBF");
    	
   		chkNew.setChecked(_iface.equals("CSV"));
    	
    	if (lightTheme) {
    		txtLog.setTextColor(0xFF0000BB);
    		chkPhoto.setTextColor(Color.BLACK);
    		chkNew.setTextColor(Color.BLACK);
    	}
    	
		// if activity was destroyed (for exmp. by display rotation)
		// then get data of LOG from previous state 
		if (savedInstanceState != null)
        {
        	txtLog.setText(savedInstanceState.getString("LOG"));
        }
		else
		{
		    // Get Shared Preferences and fill special field of message with it
			prefs = getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);
			txtLog.setText(prefs.getString(RsaDb.LASTSENDKEY, ""));
		}
		
        // Init new thread with function run() in that class
        background = new Thread(mAct);
        
        mHandler = new Handler()
        {
        	@Override
        	public void handleMessage(android.os.Message msg)
        	{
        		String s = msg.getData().getString("LOG");
        		txtLog.append(s);
        	}
        };
    	
        
        
        /** Get the current date */
        final Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);
    	
        // Set datapicker to current date
        mPicker.updateDate(mYear, mMonth, mDay);   
        
     //   Log.d("RRRR", Integer.toString(mYear));
        
        btnSend.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0)
			{
				prefs = getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);
				if (prefs.getBoolean(RsaDb.ACTIVESYNCKEY, true)) 
				{
					Toast.makeText(getApplicationContext(),R.string.send_already,Toast.LENGTH_SHORT).show(); 
					return;
				}
		        
				// Clear "console" TextView
				txtLog.setText(R.string.send_connecting);
				
				// Get current date from DataPicker to String curDate in format like 23.02.2011
				curDate = String.format( "%02d.%02d.%02d", mPicker.getDayOfMonth(),
														   mPicker.getMonth()+1,
														   mPicker.getYear());
				
				// Start downloading in thread
				background.start();
				
				btnSend.setClickable(false);
			}});
    }

	/**
	 *  Saving activity positions on destroy .. or if display rotation
	 */
	@Override
	protected void onSaveInstanceState (Bundle outState)
	{
		super.onSaveInstanceState(outState);
		
        /** Binding xml elements of view to variables */
        final TextView txtLog 	= (TextView)findViewById(R.id.txtLog_send);
		// save LOG state
		outState.putString("LOG", txtLog.getText().toString());
	}
	
	/**
	 * If Back-button on device pressed then do...
	 */
	@Override
	public void onBackPressed()
	{
		prefs = getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);
		if (prefs.getBoolean(RsaDb.ACTIVESYNCKEY, true)) 
		{
			Toast.makeText(getApplicationContext(),R.string.send_wait,Toast.LENGTH_SHORT).show(); 
			return;
		}
		
        /** Binding xml elements of view to variables */
        final TextView txtLog 	= (TextView)findViewById(R.id.txtLog_send);
        
		prefs.edit().putString(RsaDb.LASTSENDKEY, txtLog.getText().toString()).commit();
		
	    if (db != null) 
	    {
	    	db.close();
	    }
	    
    	finish();
	}
    
	/**
     * Thread body
     */
	@Override
	public void run()
	{
		/** Handler message */
		android.os.Message hMess;
		Bundle data = new Bundle();
		
		/** Device has registration */
		boolean isLicensed = true;
		
		/** Is License checking is active? */
		boolean isLicenseCheck = false;
		
	    // Get Shared Preferences and fill special field of message with it
		prefs = getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);
	
	    prefs.edit().putBoolean(RsaDb.ACTIVESYNCKEY, true).commit();
		
		/** Context for some methods */
        context = getApplicationContext();
		
        /** Path to application file storage */
		appPath = context.getFilesDir().getAbsolutePath();
		
		RsaApplication app = (RsaApplication) this.getApplication();
		
		if (app.getSyncState() == RsaApplication.STATE_AUTOSYNC) {
        	hMess = mHandler.obtainMessage();
        	data.putString("LOG", "Происходит автовыгрузка данных...");
        	hMess.setData(data); 
        	mHandler.sendMessage(hMess);
        	
    		// Set status of sync process to false
    		prefs.edit().putBoolean(RsaDb.ACTIVESYNCKEY, false).commit();
    	    if (db != null)	db.close();
        	return;
        } 
		app.setSyncState(RsaApplication.STATE_MANUALSYNC);
		
		// Init database with architecture that designed in RsaDbHelper.class
		mDb = new RsaDbHelper(context, RsaDbHelper.DB_ORDERS);
        db = mDb.getWritableDatabase();
  
        
        // Added 05 Jan 2013 by Romka
        String imei = null;
        String imei2 = null;
        try {
        	TelephonyInfo telephonyInfo = TelephonyInfo.getInstance(getApplicationContext());
        	imei	= telephonyInfo.getImeiSIM1();
        	imei2	= telephonyInfo.getImeiSIM2();
        } catch (Exception dd) {
        	imei = RsaDb.getDImei(getApplicationContext());
        	imei2 = null;
        }
        
        if (imei==null) {
        	imei = RsaDb.getDImei(getApplicationContext());
        	imei2 = null;
        }
        
        SharedPreferences ___prefs = getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);
        RsaDbHelper main__mDb = new RsaDbHelper(this, ___prefs.getString(RsaDb.ACTUALDBKEY, RsaDbHelper.DB_NAME1));
        SharedPreferences def_pref = PreferenceManager.getDefaultSharedPreferences(this);
        
     	CheckVersionThread getverThread = new CheckVersionThread(	imei, imei2, 
     																___prefs, db,  
     																main__mDb, 
     																useGPS, 
     																___prefs.getBoolean(RsaDb.SENDLINES, false),
     																def_pref.getBoolean("chkRestSend", false)); 
        getverThread.start();
        
        
        isLicensed = true;
        isLicenseCheck = true;
        
        try {
        	isLicensed = ___prefs.getBoolean(RsaDb.LICENSED, true);
        } catch(Exception e){}
        
        // if license checking is active, but device has no license...
        if ((isLicenseCheck==true) && (isLicensed==false))
        {
        	// Show message that current device is not active
        	hMess = mHandler.obtainMessage();
        	data.putString("LOG", "Устройство не зарегистрировано!");
        	hMess.setData(data); 
        	mHandler.sendMessage(hMess);
        	
    		// Set status of sync process to false
    		prefs.edit().putBoolean(RsaDb.ACTIVESYNCKEY, false).commit();
    		app.setSyncState(RsaApplication.STATE_STANDBY);
    	    if (db != null) 
    	    {
    	    	db.close();
    	    }    		
        	return;
        }
        else
        { 
			////////////////////////////////////////////////////////////////////////////////////////////////////
			// Try to create files with orders at selected day
			try
			{
				String Pth = getApplicationContext().getFilesDir().getAbsolutePath();
				if (getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE)
	      				.getString(RsaDb.INTERFACEKEY, "DBF").equals("DBF"))
				{
					countOrders = RsaDb.dbToDBF(mAct2, getApplicationContext(), db, "outbox/Head.dbf", "outbox/Lines.dbf", curDate, R.drawable.ic_locked_, Pth, chkNew.isChecked());
				}
				else if (getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE)
	      				.getString(RsaDb.INTERFACEKEY, "DBF").equals("XML"))
				{
					
					SharedPreferences __prefs = getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);
					RsaDbHelper __mDb = new RsaDbHelper(this, __prefs.getString(RsaDb.ACTUALDBKEY, RsaDbHelper.DB_NAME1));
					countOrders = RsaDb.dbToXML(mAct2, getApplicationContext(), db, __mDb, "outbox/Head.xml", "outbox/Lines.xml", curDate, R.drawable.ic_locked_, Pth, chkNew.isChecked(), null);
				}
				else if (getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE)
	      				.getString(RsaDb.INTERFACEKEY, "DBF").equals("CSV"))
				{
					countOrders = RsaDb.dbToCSV(mAct2, getApplicationContext(), db, "outbox/", "not used", curDate, R.drawable.ic_locked_, Pth, chkNew.isChecked());
				}
			} 
	    	catch (JDBFException e)
			{
	    		prefs.edit().putBoolean(RsaDb.ACTIVESYNCKEY, false).commit();
	    		app.setSyncState(RsaApplication.STATE_STANDBY);
	            hMess = mHandler.obtainMessage();
	            data.putString("LOG", getResources().getString(R.string.send_err_makingdbf));
	            hMess.setData(data); 
	            mHandler.sendMessage(hMess);try {Thread.sleep(500);} catch (InterruptedException e2){return;}
	    	    if (db != null) 
	    	    {
	    	    	db.close();
	    	    }
				return;
			} 
	    	catch (IOException e)
			{
	    		writeErrLog("Create DBF-file:", e.getMessage());
	    		prefs.edit().putBoolean(RsaDb.ACTIVESYNCKEY, false).commit();
	    		app.setSyncState(RsaApplication.STATE_STANDBY);
	            hMess = mHandler.obtainMessage();
	            data.putString("LOG", getResources().getString(R.string.send_err_writedbf)+"\n"+e.getMessage());
	            hMess.setData(data); 
	            mHandler.sendMessage(hMess);try {Thread.sleep(500);} catch (InterruptedException e2){return;}
	    	    if (db != null) 
	    	    {
	    	    	db.close();
	    	    }
				return;
			}
			catch (Exception e)
			{
	    		prefs.edit().putBoolean(RsaDb.ACTIVESYNCKEY, false).commit();
	    		app.setSyncState(RsaApplication.STATE_STANDBY);
	            hMess = mHandler.obtainMessage();
	            data.putString("LOG", "\nError 144: Попробуйте еще раз");
	            hMess.setData(data); 
	            mHandler.sendMessage(hMess);try {Thread.sleep(500);} catch (InterruptedException e2){return;}
	    	    if (db != null) 
	    	    {
	    	    	db.close();
	    	    }
				return;
			}
			// if files was created successfully then write about it...
	        hMess = mHandler.obtainMessage();
	        data.putString("LOG", getResources().getString(R.string.send_dbfcreated) 
	        		+ getResources().getString(R.string.send_orders) 
	        		+ countOrders 
	        		+ getResources().getString(R.string.send_qty));
	        hMess.setData(data); 
	        mHandler.sendMessage(hMess);try {Thread.sleep(500);} catch (InterruptedException e2){return;}
			
			////////////////////////////////////////////////////////////////////////////////////////////////////
			// Try to make archives
			try
			{
				if (getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE)
	      				.getString(RsaDb.INTERFACEKEY, "DBF").equals("DBF"))
				{
					RsaDb.toLzma(context, "outbox" + File.separator + "Head.dbf", "outbox" + File.separator + "HeadTS.dbf.lzma");
					RsaDb.toLzma(context, "outbox" + File.separator + "Lines.dbf", "outbox" + File.separator + "LinesTS.dbf.lzma");
				}
				else if (getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE)
	      				.getString(RsaDb.INTERFACEKEY, "DBF").equals("XML"))
				{
					RsaDb.toZip(context, "outbox" + File.separator + "Head.xml", "outbox" + File.separator + "HeadTS.xml.zip", null);
					RsaDb.toZip(context, "outbox" + File.separator + "Lines.xml", "outbox" + File.separator + "LinesTS.xml.zip", null);
				}
				if (getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE)
	      				.getString(RsaDb.INTERFACEKEY, "DBF").equals("CSV"))
				{
					//клиент захотел не архивировать файлы
					//RsaDb.toZipArray(context, "outbox", "outbox" + File.separator + "HeadTS.csv.zip");
				}
			} 
			catch (Exception e)
			{
				prefs.edit().putBoolean(RsaDb.ACTIVESYNCKEY, false).commit();
				app.setSyncState(RsaApplication.STATE_STANDBY);
	            hMess = mHandler.obtainMessage();
	            data.putString("LOG", getResources().getString(R.string.send_err_makingarch));
	            hMess.setData(data); 
	            mHandler.sendMessage(hMess);try {Thread.sleep(500);} catch (InterruptedException e2){return;}
	    	    if (db != null) 
	    	    {
	    	    	db.close();
	    	    }
				return;
			}
			// if archives was created successfully then write about it...
	        hMess = mHandler.obtainMessage();
	        data.putString("LOG", getResources().getString(R.string.send_archcreated));
	        hMess.setData(data); 
	        mHandler.sendMessage(hMess);try {Thread.sleep(500);} catch (InterruptedException e2){return;}
			
			////////////////////////////////////////////////////////////////////////////////////////////////////
			// Try to send email
		    // Get Shared Preferences and fill special field of message with it
			prefs = getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);
		
			String _user = prefs.getString(RsaDb.EMAILKEY, ""); 		
			String _pass = prefs.getString(RsaDb.PASSWORDKEY, ""); 	
			String _smtphost = prefs.getString(RsaDb.SMTPKEY, ""); 		
			String _smtpport = prefs.getString(RsaDb.SMTPPORTKEY, ""); 	
			String _sendto = prefs.getString(RsaDb.SENDTOKEY, "");
			
			// Init Mail instance for sending
			mMessage = new Mail(	_user, 
									_pass,
									_smtphost,
									_smtpport,
									 true,
									 prefs.getBoolean(RsaDb.USESSL, false));
			
		    String[] toArr = {_sendto}; 
		    
		    // Init another params
		    mMessage.setTo(toArr); 
		    mMessage.setFrom(_user); 
		    String strSubject = getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE)
      				.getString(RsaDb.NAMEKEY, "") + " ";
		    strSubject += getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE)
      				.getString(RsaDb.CODEKEY, "");
		    mMessage.setSubject(strSubject); 
		    StringBuilder strBodyText = new StringBuilder("");
		    //mMessage.setBody(""); 
		    File[] files = null;
		    String detectError = "";
		    
		    try 
		    {
		    	detectError = "\n Ошибка прикрепления вложения";
				if (getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE)
	      				.getString(RsaDb.INTERFACEKEY, "DBF").equals("DBF"))
				{
					// Add attachment to message
					mMessage.addAttachment(appPath + File.separator + "outbox" + File.separator + "HeadTS.dbf.lzma", 
												"HeadTS.dbf.lzma"); 
					mMessage.addAttachment(appPath + File.separator + "outbox" + File.separator + "LinesTS.dbf.lzma", 
		    									"LinesTS.dbf.lzma");
				}
				else if (getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE)
	      				.getString(RsaDb.INTERFACEKEY, "DBF").equals("XML"))
				{
					// Add attachment to message
					mMessage.addAttachment(appPath + File.separator + "outbox" + File.separator + "HeadTS.xml.zip", 
												"HeadTS.xml.zip"); 
					mMessage.addAttachment(appPath + File.separator + "outbox" + File.separator + "LinesTS.xml.zip", 
		    									"LinesTS.xml.zip");
				}
				else if (getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE)
	      				.getString(RsaDb.INTERFACEKEY, "DBF").equals("CSV"))
				{
					// Add all *.csv to mail attach
					detectError = "\n Ошибка доступа к файлу выгрузки";
					File outFolder = new File(appPath + File.separator + "outbox");
					detectError = "\n Ошибка создания фильтра";
					FilenameFilter fFilter = new FilenameFilter() {
						@Override
						public boolean accept(File dir, String filename) {
							return filename.endsWith(".xls");
						}
					};
					detectError = "\n Ошибка получения списка файлов для отправки";
					files = outFolder.listFiles(fFilter);
					detectError = "\n Ошибка формирования списка имен файлов для отправки";
					String[] strFiles = outFolder.list(fFilter);
					detectError = "\n Ошибка попытки вложить файлы, колво: " + Integer.toString(strFiles.length);
					if (strFiles.length>0) {
						strBodyText.append("Кол-во прикрепленных файлов: "+Integer.toString(strFiles.length)+"\n");
						for (int i=0;i<strFiles.length;i++) {
							mMessage.addAttachment(outFolder.getPath() + File.separator + strFiles[i], 
									strFiles[i]);
							strBodyText.append(strFiles[i]+"\n");
						}
						
						
					}
				}
				mMessage.setBody(strBodyText.toString());
				detectError = "\n Ошибка проверки на фото";
				if (chkPhoto.isChecked()) {
					detectError = "\n Ошибка прикрепления фоток";
					StringBuilder d = new StringBuilder(String.format("%04d", mPicker.getYear()));
					d.append(String.format("%02d",(mPicker.getMonth()+1)));
					d.append(String.format("%02d", mPicker.getDayOfMonth()));
					attachPhoto(mMessage, d.toString());
				}
		        
		    	// Perform sending message and if OK then...
				detectError = "\n Низкоуровневая ошибка отправки письма";
		        if(mMessage.send()) 
		        {	
		        	detectError = "\n Ошибка удалениня файлов";
		        	if (getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE)
		      				.getString(RsaDb.INTERFACEKEY, "DBF").equals("CSV")) {
		        		// Delete all *.xls
						for (int j=0;j<files.length;j++) {
							files[j].delete();
						}	
		        	}
		        	
		        	detectError = "\n Ошибка обновления данных в БД";
					/** Ticket 13: Used as data storage before save it to database */
					ContentValues val = new ContentValues();
					// Ticket 13: Put to special storage "1" for SENDED
	    			val.put(RsaDbHelper.HEAD_SENDED, "1");
	    			// Ticket 13: Put to special storage ic_locked for BLOCK 
	    			val.put(RsaDbHelper.HEAD_BLOCK,	R.drawable.ic_locked_);
		        	// Ticket 13: if orders successfully sended then set BLOCK and SENDED status in TABLE_HEAD
		        	db.update(RsaDbHelper.TABLE_HEAD, val, 
		        						RsaDbHelper.HEAD_DATE + "='"
		        						+ curDate 
		        						+ "'", null);
		        	// Ticket 13: Not needed
		        	val.clear();
		        	detectError = "";
		        	// if OK then show successfully message on display
		            hMess = mHandler.obtainMessage();
		            data.putString("LOG", getResources().getString(R.string.send_orderson) 
		            		+ curDate 
		            		+ getResources().getString(R.string.send_sended));
		            hMess.setData(data); 
		            mHandler.sendMessage(hMess);try {Thread.sleep(500);} catch (InterruptedException e2){return;}
		        } 
		        else 
		        { 
		        	try {
		        	if (getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE)
		      				.getString(RsaDb.INTERFACEKEY, "DBF").equals("CSV")) {
		        		// Delete all *.xls
						for (int j=0;j<files.length;j++) {
							files[j].delete();
						}	
		        	}} catch (Exception eee) {}
		        	detectError = "";
		        	// if NOT OK then show error message on display
		            hMess = mHandler.obtainMessage();
		            data.putString("LOG", getResources().getString(R.string.send_err_sendmessage));
		            hMess.setData(data); 
		            mHandler.sendMessage(hMess);try {Thread.sleep(500);} catch (InterruptedException e2){return;}
		        } 
		    } 
		    catch(Exception e) 
		    { 
		    	e.printStackTrace();
		    	try {
		    	if (getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE)
	      				.getString(RsaDb.INTERFACEKEY, "DBF").equals("CSV")) {
	        		// Delete all *.xls
					for (int j=0;j<files.length;j++) {
						files[j].delete();
					}	
	        	}} catch (Exception eee) {}
		    	prefs.edit().putBoolean(RsaDb.ACTIVESYNCKEY, false).commit();
		    	app.setSyncState(RsaApplication.STATE_STANDBY);
	            hMess = mHandler.obtainMessage();
	            writeErrLog("msg.send() error", e.getMessage());
	            data.putString("LOG", getResources().getString(R.string.send_err_sendmessage_jmail)+detectError);
	            hMess.setData(data); 
	            mHandler.sendMessage(hMess);try {Thread.sleep(500);} catch (InterruptedException e2){return;}
	    	    if (db != null) 
	    	    {
	    	    	db.close();
	    	    }
				return;
		    }
        }
	    
	    if (db != null) 
	    {
	    	db.close();
	    }
	    
	    app.setSyncState(RsaApplication.STATE_STANDBY);
	    
        /** Binding xml elements of view to variables */
        final TextView txtLog 	= (TextView)findViewById(R.id.txtLog_send);
        
	    prefs.edit().putString(RsaDb.LASTSENDKEY, txtLog.getText().toString()).commit();
	    prefs.edit().putBoolean(RsaDb.ACTIVESYNCKEY, false).commit();
	}
	
    /**
     * Method that starts if system trying to destroy activity
     */
    @Override
    protected void onDestroy() 
    {
    	super.onDestroy();
    	
	    if (db != null) 
	    {
	    	db.close();
	    }
    	
	    prefs = getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);
	    
	    prefs.edit().putBoolean(RsaDb.ACTIVESYNCKEY, false).commit();
	    ((RsaApplication)this.getApplication()).setSyncState(RsaApplication.STATE_STANDBY);
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {        
        super.onConfigurationChanged(newConfig);
    }
    
    /**
     * Method for check site version
     */
    public String readVersionFromSite() 
    {
        StringBuilder builder = new StringBuilder();
        HttpClient client = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet("http://rsa.16mb.com/chkver.php");
        try 
        {
	          HttpResponse response = client.execute(httpGet);
	          StatusLine statusLine = response.getStatusLine();
	          int statusCode = statusLine.getStatusCode();
	          
	          if (statusCode == 200) 
	          {
		            HttpEntity entity = response.getEntity();
		            InputStream content = entity.getContent();
		            BufferedReader reader = new BufferedReader(new InputStreamReader(content));
		            String line;
		            
		            while ((line = reader.readLine()) != null) 
		            {
		            	builder.append(line);
		            }
	          }
        } 
        catch (Exception e) {} 
        
        return builder.toString();
    }
    
    void attachPhoto(Mail me, String d) {
    	String merchPath = Environment.getExternalStorageDirectory().toString() + File.separator + "rsa" + File.separator + "outbox";
    	try {
    		File dir = new File(merchPath);
    		FilenameFilter imFilter = new FilenameFilter() {
    			@Override
    			public boolean accept(File dir, String filename) {
    				return filename.endsWith(".jpg");
    			}
    		};
        	File[] fileList = dir.listFiles(imFilter);
        	for(int i=0;i<fileList.length;i++){
        		Log.i("RRR",d + "=" + fileList[i].getName());
        		if (d.equals(fileList[i].getName().substring(0, 8))) {
        			me.addAttachment(merchPath + File.separator + fileList[i].getName(), fileList[i].getName());
        		}
        	}
    	
    	} catch (Exception e5) {}
    }
    
    private void writeErrLog(String mess, String ex) {
    	String SD_CARD_PATH = Environment.getExternalStorageDirectory().toString() + File.separator + "rsa";
    	SD_CARD_PATH = SD_CARD_PATH + File.separator + "error_log.txt";
    	// get current time
    	Calendar c = Calendar.getInstance();
    	SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    	// init error msg
    	StringBuilder msgError = new StringBuilder("\r\n---"+ fmt.format(c.getTime()) +"----------------------\r\n");
    	msgError.append(mess);
    	msgError.append("\r\nException text:\r\n");
    	msgError.append(ex + "\r\n");
    	
    	try {
	    	BufferedWriter out = new BufferedWriter(new FileWriter(SD_CARD_PATH, true));
	    	out.write(msgError.toString());
	    	out.close();
    	} catch (Exception e) {}
    }		
}
