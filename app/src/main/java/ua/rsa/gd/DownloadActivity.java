package ua.rsa.gd;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.mail.Message;
import javax.mail.MessagingException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import ru.by.rsa.R;
import ua.rsa.gd.external.javadbf.JDBFException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Used for receiving data for smartfon database
 * @author Komarev Roman
 * Odessa, neo3da@mail.ru, +380503412392
 */
public class DownloadActivity extends Activity implements Runnable
{
	/** Used to send messages from Thread to main Thread */
	Handler mHandler;
	/** Activity to send in functions */
	Activity mAct;
	/** JavaMail variable */
	Mail mPop;
	/** Variable that stores array of incoming messages */
	Message[] mMessages;
	/** Stores number of total messages in mailbox */
	int totalMessages;
	/** Context for some methods */
    Context context;
	/** Init database with architecture that designed in RsaDbHelper.class */
    RsaDbHelper mDb; 
	/** SQLite database that stores all data */
	static SQLiteDatabase db;    
    /** Indicates that downloading files comlited */
    boolean Downloaded;
    /** Get Shared Preferences and fill special field of message with it */
	SharedPreferences prefs;
	/** Used to receive binary result from function that cheking attachment */
	int binFlag = 0;
	/** Used for information that main messages was successfuly saved on device */
	boolean key;
	/** Thread for background downloading */
	static Thread background;
	/** Conf changed */
	boolean confChanged;
    /** xml elements of view to variables */
	static Button btnDLoad;
    static TextView txtTableName;
    static TextView txtCount;
    static TextView txtLog;
    static TextView txtPos;
    static ProgressBar mProgress;	
    static int iProgress;
    static int maxProgress;
    static String curTable;
    
    /** Current theme */
	private boolean lightTheme;
	
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.download);
        
        lightTheme = getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE).getBoolean(RsaDb.LIGHTTHEMEKEY, false);
        
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        
        confChanged = false;

        mAct = this;
        
        /** Binding xml elements of view to variables */
        btnDLoad = (Button)findViewById(R.id.btnDownload_download);
        txtTableName	= (TextView)findViewById(R.id.txtTableName_download);
        txtCount 	= (TextView)findViewById(R.id.txtRecordsCount_download);
        txtLog 	= (TextView)findViewById(R.id.txtLog_download);
        txtPos 	= (TextView)findViewById(R.id.txtPosition_download);
        mProgress = (ProgressBar)findViewById(R.id.Progress_download);
        
        if (lightTheme) txtLog.setTextColor(0xFF0000BB);
        
		// if activity was destroyed (for exmp. by display rotation)
		// then get data of LOG from previous state 
		if (savedInstanceState != null)
        {
        	txtLog.setText(savedInstanceState.getString("LOG"));
        	iProgress = savedInstanceState.getInt("IPROGRESS");
        	maxProgress = savedInstanceState.getInt("MAXPROGRESS");
        	curTable = savedInstanceState.getString("CURTABLE");
        }
		else
		{
		    // Get Shared Preferences and fill special field of message with it
			prefs = getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);
			txtLog.setText(prefs.getString(RsaDb.LASTDWNLDKEY, ""));
			iProgress = 0;
			curTable = "Таблица";
			maxProgress = 0;
		}
		
        // Init new thread with function run() in that class
        background = new Thread(this);
        mHandler = new Handler()
        {
        	@Override
        	public void handleMessage(android.os.Message msg)
        	{
        		String s = msg.getData().getString("LOG");
        		if (s.equals("TABLE")) {
        			curTable = msg.getData().getString("TABLE");
        			txtTableName.setText(curTable + ":");
        			s = msg.getData().getString("TABLE") + ", ";
        			iProgress = 0;
        			mProgress.setProgress(iProgress);
        			txtPos.setText(Integer.toString(iProgress));
        			maxProgress = msg.getData().getInt("COUNT");
        			mProgress.setMax(maxProgress);
        			txtCount.setText(Integer.toString(maxProgress));
        			txtLog.append(s);
        		} else if (s.equals("PROGRESS")) {
        			txtTableName.setText(curTable + ":");
        			txtCount.setText(Integer.toString(maxProgress));
        			mProgress.setMax(maxProgress);
        			iProgress++;
        			mProgress.setProgress(iProgress);
        			txtPos.setText(Integer.toString(iProgress));
        		} else {
        			txtLog.append(s);
					if (s.equals(getResources().getString(R.string.download_loading_succ))) {
						SharedPreferences def_prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
						if (def_prefs.getBoolean("shrinkAfter",false)) {
							showDialog(0);
						}
					}
        		}
        	}
        };
        
        // On button click start to download files from mailbox
        btnDLoad.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0)
			{
				// Get version from site
			//	try 
			//	{
			//		JSONArray jsonArray = new JSONArray(readVersionFromSite());
			//		String siteVersion = jsonArray.getString(0);
			//		Float.parseFloat(siteVersion);
			//		SharedPreferences _prefs = getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);
			//		_prefs.edit().putString(RsaDb.MARKETVERSION, siteVersion).commit();
			//	} 
			//	catch (JSONException e) 
			//	{
			//		Log.e("ROMKA", "JSON Exception");
			//	}
				
				
				prefs = getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);
				if (prefs.getBoolean(RsaDb.ACTIVESYNCKEY, true)) 
				{
					Toast.makeText(getApplicationContext(),R.string.download_background,Toast.LENGTH_SHORT).show(); 
					return;
				}
				
				// Clear "console" TextView
				txtLog.setText(R.string.download_connecting);
				
				// Start downloading in thread
				background.start();
				
				btnDLoad.setClickable(false);
			}});
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
		
		// init Variables
		mMessages = null;
		totalMessages = 0;
        context = getApplicationContext();
		mDb = null; 
    	db = null;
    	Downloaded = false;
        
	    // Get Shared Preferences and fill special field of message with it
		prefs = getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);
	
	    prefs.edit().putBoolean(RsaDb.ACTIVESYNCKEY, true).commit();
		
		if (!prefs.getString(RsaDb.ACTUALDBKEY, "").equals(RsaDbHelper.DB_NAME1))
		{
			mDb = new RsaDbHelper(context, RsaDbHelper.DB_NAME1);
		}
		else
		{
			mDb = new RsaDbHelper(context, RsaDbHelper.DB_NAME2);
		}
			
		String _user = prefs.getString(RsaDb.EMAILKEY, ""); 		
		String _pass = prefs.getString(RsaDb.PASSWORDKEY, ""); 	
		String _smtphost = prefs.getString(RsaDb.POPKEY, ""); 		
		String _smtpport = prefs.getString(RsaDb.POPPORTKEY, ""); 	
		
		// Init Mail instance for sending
		mPop = new Mail(		_user, 
								_pass,
								_smtphost,
								_smtpport,
								 true, 
								 prefs.getBoolean(RsaDb.USESSL, false));
		
		////////////////////////////////////////////////////////////////////////////////////////////////
		// Try connect to mailbox 
		try 
		{
			// Connect to service
            mPop.connect();
            
            // Open folder with messages
            mPop.openFolder("INBOX");
            
            // Get message count on server
            totalMessages = mPop.getMessageCount();
            
            // Write info about messages in console
            hMess = mHandler.obtainMessage();
            data.putString("LOG", getResources().getString(R.string.download_connected) + totalMessages + "\n");
            hMess.setData(data); Thread.sleep(100);
            mHandler.sendMessage(hMess);
        } 
		catch(Exception e) 
		{
			prefs.edit().putBoolean(RsaDb.ACTIVESYNCKEY, false).commit();
			// Write info about fail in console
            hMess = mHandler.obtainMessage();
            data.putString("LOG", getResources().getString(R.string.download_err_connect));
            hMess.setData(data); 
            mHandler.sendMessage(hMess);try {Thread.sleep(500);} catch (InterruptedException e2){return;}
			/////////////////////////////////////////////////////////////////////////////////////////////////
			// Try to close everithing before return
				try
				{
					mPop.disconnect();
				} 
				catch (Exception e1)
				{
					// Who cares
				}
            return;
        }
		
		// Used for information that main messages was successfuly saved on device
		key = false;

		///////////////////////////////////////////////////////////////////////////////////////////////////
		// Try to get all messages 
		if (totalMessages != 0)
		{
			try
			{
				mMessages = mPop.getMessages();
	            hMess = mHandler.obtainMessage();
	            data.putString("LOG", getResources().getString(R.string.download_received));
	            hMess.setData(data); 
	            mHandler.sendMessage(hMess);try {Thread.sleep(500);} catch (InterruptedException e2){return;}
			}
			catch (Exception e)
			{
				prefs.edit().putBoolean(RsaDb.ACTIVESYNCKEY, false).commit();
	            hMess = mHandler.obtainMessage();
	            data.putString("LOG", getResources().getString(R.string.download_err_receiving));
	            hMess.setData(data); 
	            mHandler.sendMessage(hMess); try {Thread.sleep(500);} catch (InterruptedException e2){return;}
				/////////////////////////////////////////////////////////////////////////////////////////////////
				// Try to close everithing before return
					try
					{
						mPop.closeFolder();
						mPop.disconnect();
					} 
					catch (Exception e1)
					{
						// Who cares
					}
				return;
			}
			
			///////////////////////////////////////////////////////////////////////////////////////////////
			// Parse all messages from last to first for attachment
			key = false;
			binFlag = 0; 
			for (int i=1;i<=totalMessages;i++)
			{
	            hMess = mHandler.obtainMessage();
	            data.putString("LOG", getResources().getString(R.string.download_scan) + (totalMessages-i+1) + "\n");
	            hMess.setData(data); 
	            mHandler.sendMessage(hMess); try {Thread.sleep(1000);} catch (InterruptedException e2){return;}
				try
				{
					if ( (mPop.isFrom(mMessages[totalMessages-i], prefs.getString(RsaDb.SENDTOKEY, "")))
							&& mPop.hasAttachment(mMessages[totalMessages-i])   )
					{
			            hMess = mHandler.obtainMessage();
			            data.putString("LOG", getResources().getString(R.string.download_filesfound));
			            hMess.setData(data);  try {Thread.sleep(500);} catch (InterruptedException e2){return;}
			            mHandler.sendMessage(hMess); try {Thread.sleep(500);} catch (InterruptedException e2){return;}
			            binFlag = mPop.hasMainAttachment(mMessages[totalMessages-i], getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE));
						if ( (binFlag & 0x1F) == 0x1F) // (x & 00011111) == 00011111?
						{
				            hMess = mHandler.obtainMessage();
				            data.putString("LOG", getResources().getString(R.string.download_correctfound));
				            hMess.setData(data); 
				            mHandler.sendMessage(hMess); try {Thread.sleep(500);} catch (InterruptedException e2){return;}
							if ( (binFlag & 0x20) == 0x20)
							{
					            hMess = mHandler.obtainMessage();
					            data.putString("LOG", getResources().getString(R.string.download_found_WH));
					            hMess.setData(data); 
					            mHandler.sendMessage(hMess);try {Thread.sleep(500);} catch (InterruptedException e2){return;}
							}
							if ( (binFlag & 0x40) == 0x40)
							{
					            hMess = mHandler.obtainMessage();
					            data.putString("LOG", getResources().getString(R.string.download_found_GR));
					            hMess.setData(data); 
					            mHandler.sendMessage(hMess);try {Thread.sleep(500);} catch (InterruptedException e2){return;}
							}
							if ( (binFlag & 0x80) == 0x80)
							{
					            hMess = mHandler.obtainMessage();
					            data.putString("LOG", getResources().getString(R.string.download_found_BR));
					            hMess.setData(data); 
					            mHandler.sendMessage(hMess);try {Thread.sleep(500);} catch (InterruptedException e2){return;}
							}		
							if ( (binFlag & 0x100) == 0x100)
							{
					            hMess = mHandler.obtainMessage();
					            data.putString("LOG", getResources().getString(R.string.download_found_Work));
					            hMess.setData(data); 
					            mHandler.sendMessage(hMess);try {Thread.sleep(500);} catch (InterruptedException e2){return;}
							}
							if ( (binFlag & 0x200) == 0x200)
							{
					            hMess = mHandler.obtainMessage();
					            data.putString("LOG", getResources().getString(R.string.download_found_Plan));
					            hMess.setData(data); 
					            mHandler.sendMessage(hMess);try {Thread.sleep(500);} catch (InterruptedException e2){return;}
							}
							if ( (binFlag & 0x400) == 0x400)
							{
					            hMess = mHandler.obtainMessage();
					            data.putString("LOG", getResources().getString(R.string.download_found_Sold));
					            hMess.setData(data); 
					            mHandler.sendMessage(hMess);try {Thread.sleep(500);} catch (InterruptedException e2){return;}
							}
							if ( (binFlag & 0x800) == 0x800)
							{
					            hMess = mHandler.obtainMessage();
					            data.putString("LOG", "M");
					            hMess.setData(data); 
					            mHandler.sendMessage(hMess);try {Thread.sleep(500);} catch (InterruptedException e2){return;}
							}
							if ( (binFlag & 0x1000) == 0x1000)
							{
					            hMess = mHandler.obtainMessage();
					            data.putString("LOG", "L");
					            hMess.setData(data); 
					            mHandler.sendMessage(hMess);try {Thread.sleep(500);} catch (InterruptedException e2){return;}
							}
								
							mPop.getArchives(mMessages[totalMessages-i], File.separator + "inbox", context);
				            hMess = mHandler.obtainMessage();
				            data.putString("LOG", getResources().getString(R.string.download_unpacking));
				            hMess.setData(data); 
				            mHandler.sendMessage(hMess);try {Thread.sleep(500);} catch (InterruptedException e2){return;}
							key = true;
							break;
						}
						else
						{
				            hMess = mHandler.obtainMessage();
				            data.putString("LOG", getResources().getString(R.string.download_err_msg));
				            hMess.setData(data); 
				            mHandler.sendMessage(hMess); try {Thread.sleep(500);} catch (InterruptedException e2){return;}
						}
					}
				}
				catch(Exception e)
				{
					// Write info about fail and go to next message 
		            hMess = mHandler.obtainMessage();
		            data.putString("LOG", getResources().getString(R.string.download_err_scan) + (totalMessages-i+1) + " (JMAIL)\n");
		            hMess.setData(data); 
		            mHandler.sendMessage(hMess); try {Thread.sleep(500);} catch (InterruptedException e2){return;}
		            continue;
				}
			}
			
			if (key)
			{
				///////////////////////////////////////////////////////////////////////////////////
				// Get files from archive
				try
				{
              		if (getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE)
              				.getString(RsaDb.INTERFACEKEY, "DBF").equals("DBF"))
              		{
              			RsaDb.fromLzma(context, "inbox/goods.dbf.lzma", "inbox/Goods.DBF");
              			RsaDb.fromLzma(context, "inbox/cust.dbf.lzma", "inbox/Cust.DBF");
              			RsaDb.fromLzma(context, "inbox/char.dbf.lzma", "inbox/Char.DBF");
              			RsaDb.fromLzma(context, "inbox/debit.dbf.lzma", "inbox/Debit.DBF");
              			RsaDb.fromLzma(context, "inbox/shop.dbf.lzma", "inbox/Shop.DBF");
              			if ( (binFlag & 0x20) == 0x20)
              				RsaDb.fromLzma(context, "inbox/sklad.dbf.lzma", "inbox/Sklad.DBF");
              			if ( (binFlag & 0x40) == 0x40)
              				RsaDb.fromLzma(context, "inbox/group.dbf.lzma", "inbox/Group.DBF");
              			if ( (binFlag & 0x80) == 0x80)
              				RsaDb.fromLzma(context, "inbox/brand.dbf.lzma", "inbox/Brand.DBF");
              			if ( (binFlag & 0x100) == 0x100)
              				RsaDb.fromLzma(context, "inbox/workinf.dbf.lzma", "inbox/Workinf.DBF");
              			if ( (binFlag & 0x200) == 0x200)
              				RsaDb.fromLzma(context, "inbox/plan.dbf.lzma", "inbox/Plan.DBF");
              			if ( (binFlag & 0x1000) == 0x1000)
              				RsaDb.fromLzma(context, "inbox/prodlock.dbf.lzma", "inbox/Prodlock.DBF");
              			if ( (binFlag & 0x4000) == 0x4000)
              				RsaDb.fromLzma(context, "inbox/skladdet.dbf.lzma", "inbox/Skladdet.DBF");
              			hMess = mHandler.obtainMessage();
              			data.putString("LOG", getResources().getString(R.string.download_all_unpack));
              			hMess.setData(data); 
              			mHandler.sendMessage(hMess); try {Thread.sleep(500);} catch (InterruptedException e2){return;}
              		}
              		else if (getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE)
              				.getString(RsaDb.INTERFACEKEY, "DBF").equals("XML"))
              		{
						RsaDb.fromZip(context, "inbox/goods.xml.zip", "inbox");
						RsaDb.fromZip(context, "inbox/cust.xml.zip", "inbox");
						RsaDb.fromZip(context, "inbox/char.xml.zip", "inbox");
						RsaDb.fromZip(context, "inbox/debit.xml.zip", "inbox");
						RsaDb.fromZip(context, "inbox/shop.xml.zip", "inbox");
						if ( (binFlag & 0x20) == 0x20)
							RsaDb.fromZip(context, "inbox/sklad.xml.zip", "inbox");
						if ( (binFlag & 0x40) == 0x40)
							RsaDb.fromZip(context, "inbox/group.xml.zip", "inbox");
						if ( (binFlag & 0x80) == 0x80)
							RsaDb.fromZip(context, "inbox/brand.xml.zip", "inbox");
						if ( (binFlag & 0x100) == 0x100)
							RsaDb.fromZip(context, "inbox/workinf.xml.zip", "inbox");
						if ( (binFlag & 0x200) == 0x200)
							RsaDb.fromZip(context, "inbox/plan.xml.zip", "inbox");
						if ( (binFlag & 0x400) == 0x400)
							RsaDb.fromZip(context, "inbox/sold.xml.zip", "inbox");
						if ( (binFlag & 0x800) == 0x800)
							RsaDb.fromZip(context, "inbox/matrix.xml.zip", "inbox");
						if ( (binFlag & 0x1000) == 0x1000)
							RsaDb.fromZip(context, "inbox/prodlock.xml.zip", "inbox");
              		}
              		else if (getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE)
              				.getString(RsaDb.INTERFACEKEY, "DBF").equals("CSV"))
              		{
						RsaDb.fromZip(context, "inbox/goods.csv.zip", "inbox");
						RsaDb.fromZip(context, "inbox/cust.csv.zip", "inbox");
						RsaDb.fromZip(context, "inbox/char.csv.zip", "inbox");
						RsaDb.fromZip(context, "inbox/debit.csv.zip", "inbox");
						RsaDb.fromZip(context, "inbox/shop.csv.zip", "inbox");
						if ( (binFlag & 0x20) == 0x20)
							RsaDb.fromZip(context, "inbox/sklad.csv.zip", "inbox");
						if ( (binFlag & 0x40) == 0x40)
							RsaDb.fromZip(context, "inbox/group.csv.zip", "inbox");
						if ( (binFlag & 0x80) == 0x80)
							RsaDb.fromZip(context, "inbox/brand.csv.zip", "inbox");
						if ( (binFlag & 0x100) == 0x100)
							RsaDb.fromZip(context, "inbox/workinf.csv.zip", "inbox");
						if ( (binFlag & 0x200) == 0x200)
							RsaDb.fromZip(context, "inbox/plan.csv.zip", "inbox");
              		}
				} 
				catch (Exception e)
				{
					prefs.edit().putBoolean(RsaDb.ACTIVESYNCKEY, false).commit();
					// Write info about fail
		            hMess = mHandler.obtainMessage();
		            data.putString("LOG", getResources().getString(R.string.download_err_unpack));
		            hMess.setData(data);
		            mHandler.sendMessage(hMess); try {Thread.sleep(500);} catch (InterruptedException e2){return;}
					/////////////////////////////////////////////////////////////////////////////////////////////////
					// Try to close everithing before return
						try
						{
							mPop.closeFolder();
							mPop.disconnect();
						} 
						catch (Exception e1)
						{
							// Who cares
						}
					return;
				}
			
				/////////////////////////////////////////////////////////////////////////////////////
				// Download data from files to Database
				// Open database 
				
				Downloaded = true;
			}
		}
		
		
		if (!key)
		{
			// if FOR statement finished and not correct messages fount then
			// exit and write about it
            hMess = mHandler.obtainMessage();
            data.putString("LOG", getResources().getString(R.string.download_msg_notfound));
            hMess.setData(data); 
            mHandler.sendMessage(hMess); try {Thread.sleep(500);} catch (InterruptedException e2){return;}
		}
		
		
		/////////////////////////////////////////////////////////////////////////////////////////////////
		// Delete all messages
		if (mMessages != null)
		{
			try
			{
				mPop.deleteAllMessages(mMessages);
				hMess = mHandler.obtainMessage();
				data.putString("LOG", getResources().getString(R.string.download_clearbox));
				hMess.setData(data); 
				mHandler.sendMessage(hMess); try {Thread.sleep(500);} catch (InterruptedException e2){return;}
			} 
			catch (MessagingException e1)
			{
				hMess = mHandler.obtainMessage();
				data.putString("LOG", getResources().getString(R.string.download_err_delete));
				hMess.setData(data); 
				mHandler.sendMessage(hMess); try {Thread.sleep(500);} catch (InterruptedException e2){return;}
			}
		}
		
		/////////////////////////////////////////////////////////////////////////////////////////////////
		// Try to close opened email folder
		// and disconnect from server
		try
		{
    	    if (db != null) 
    	    {
    	    	db.close();
    	    }
			mPop.closeFolder();
			mPop.disconnect();
			// Write about success!
            hMess = mHandler.obtainMessage();
            data.putString("LOG", getResources().getString(R.string.download_disconnect));
            hMess.setData(data); 
            mHandler.sendMessage(hMess); try {Thread.sleep(500);} catch (InterruptedException e2){return;}
		} 
		catch (Exception e)
		{
			// Write about fail!
            hMess = mHandler.obtainMessage();
            data.putString("LOG", getResources().getString(R.string.download_err_connect_jmail));
            hMess.setData(data); 
            mHandler.sendMessage(hMess);
		}
		
		if (Downloaded && key)
		{
			// Write about begining of downloading!
            hMess = mHandler.obtainMessage();
            data.putString("LOG", getResources().getString(R.string.download_loading));
            hMess.setData(data); 
            mHandler.sendMessage(hMess); try {Thread.sleep(500);} catch (InterruptedException e2){return;}
            
	        db = mDb.getWritableDatabase();
			db.execSQL("PRAGMA foreign_keys=OFF;");
			String pth = this.getFilesDir().getAbsolutePath();
			try
			{
					
					
					if (getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE)
              				.getString(RsaDb.INTERFACEKEY, "DBF").equals("DBF"))
					{
						RsaDb.dbfToDb(mAct, getApplicationContext(),  db, "inbox" + File.separator + "Cust.DBF",  RsaDb.DBF_CUST, mHandler, pth);
						
						if ( (binFlag & 0x80) == 0x80)
							RsaDb.dbfToDb(mAct, getApplicationContext(),  db, "inbox" + File.separator + "Brand.DBF",  RsaDb.DBF_BRAND, mHandler, pth);
						if ( (binFlag & 0x40) == 0x40)
							RsaDb.dbfToDb(mAct, getApplicationContext(),  db, "inbox" + File.separator + "Group.DBF",  RsaDb.DBF_GROUP, mHandler, pth);
						if ( (binFlag & 0x20) == 0x20)
							RsaDb.dbfToDb(mAct, getApplicationContext(),  db, "inbox" + File.separator + "Sklad.DBF",  RsaDb.DBF_SKLAD, mHandler, pth);
						RsaDb.dbfToDb(mAct, getApplicationContext(),  db, "inbox" + File.separator + "Debit.DBF", RsaDb.DBF_DEBIT, mHandler, pth);
						RsaDb.dbfToDb(mAct, getApplicationContext(),  db, "inbox" + File.separator + "Shop.DBF",  RsaDb.DBF_SHOP, mHandler, pth);
						RsaDb.dbfToDb(mAct, getApplicationContext(),  db, "inbox" + File.separator + "Char.DBF",  RsaDb.DBF_CHAR, mHandler, pth);
						RsaDb.dbfToDb(mAct, getApplicationContext(),  db, "inbox" + File.separator + "Goods.DBF", RsaDb.DBF_GOODS, mHandler, pth);
						if ( (binFlag & 0x100) == 0x100)
							RsaDb.dbfToDb(mAct, getApplicationContext(),  db, "inbox" + File.separator + "Workinf.DBF",  RsaDb.DBF_WORKINF, mHandler, pth);
						if ( (binFlag & 0x200) == 0x200)
							RsaDb.dbfToDb(mAct, getApplicationContext(),  db, "inbox" + File.separator + "Plan.DBF",  RsaDb.DBF_PLAN, mHandler, pth);
						if ( (binFlag & 0x1000) == 0x1000)
							RsaDb.dbfToDb(mAct, getApplicationContext(),  db, "inbox" + File.separator + "Prodlock.DBF",  RsaDb.DBF_PRODLOCK, mHandler, pth);
						if ( (binFlag & 0x4000) == 0x4000)
							RsaDb.dbfToDb(mAct, getApplicationContext(),  db, "inbox" + File.separator + "Skladdet.DBF",  RsaDb.DBF_SKLADDET, mHandler, pth);
					}
					else if (getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE)
              				.getString(RsaDb.INTERFACEKEY, "DBF").equals("XML"))
					{
					      RsaDb.XMLtoCust(this, db, mHandler, this.getFilesDir().getAbsolutePath());
				          RsaDb.XMLtoBrand(this, db, mHandler, this.getFilesDir().getAbsolutePath());
				          RsaDb.XMLtoGroup(this, db, mHandler, this.getFilesDir().getAbsolutePath());
				          RsaDb.XMLtoSklad(this, db, mHandler, this.getFilesDir().getAbsolutePath());
				          RsaDb.XMLtoDebit(this, db, mHandler, this.getFilesDir().getAbsolutePath());
				          RsaDb.XMLtoShop(this, db, mHandler, this.getFilesDir().getAbsolutePath());
				          RsaDb.XMLtoChar(this, db, mHandler, this.getFilesDir().getAbsolutePath());
				          RsaDb.XMLtoGoods(this, db, mHandler, this.getFilesDir().getAbsolutePath());
				          if ( (binFlag & 0x100) == 0x100)
				        	  RsaDb.XMLtoFTP(this, db, mHandler, this.getFilesDir().getAbsolutePath(), true);
				          if ( (binFlag & 0x200) == 0x200)
				        	  RsaDb.XMLtoPlan(this, db, mHandler, this.getFilesDir().getAbsolutePath());
				          if ( (binFlag & 0x400) == 0x400)
				        	  RsaDb.XMLtoSold(this, db, mHandler, this.getFilesDir().getAbsolutePath());
				          if ( (binFlag & 0x800) == 0x800)
				        	  RsaDb.XMLtoMatrix(this, db, mHandler, this.getFilesDir().getAbsolutePath());
				          if ( (binFlag & 0x1000) == 0x1000)
				        	  RsaDb.XMLtoProdlock(this, db, mHandler, this.getFilesDir().getAbsolutePath());
					}
					else if (getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE)
              				.getString(RsaDb.INTERFACEKEY, "DBF").equals("CSV"))
					{
					      RsaDb.CSVtoCust(this, db, mHandler, this.getFilesDir().getAbsolutePath());
				          RsaDb.CSVtoBrand(this, db, mHandler, this.getFilesDir().getAbsolutePath());
				          RsaDb.CSVtoGroup(this, db, mHandler, this.getFilesDir().getAbsolutePath());
				          RsaDb.CSVtoSklad(this, db, mHandler, this.getFilesDir().getAbsolutePath());
				          RsaDb.CSVtoDebit(this, db, mHandler, this.getFilesDir().getAbsolutePath());
				          RsaDb.CSVtoShop(this, db, mHandler, this.getFilesDir().getAbsolutePath());
				          RsaDb.CSVtoChar(this, db, mHandler, this.getFilesDir().getAbsolutePath());
				          RsaDb.CSVtoGoods(this, db, mHandler, this.getFilesDir().getAbsolutePath());
				          if ( (binFlag & 0x100) == 0x100)
				        	  RsaDb.CSVtoFTP(this, db, mHandler, this.getFilesDir().getAbsolutePath());
				          if ( (binFlag & 0x200) == 0x200)
				        	  RsaDb.CSVtoPlan(this, db, mHandler, this.getFilesDir().getAbsolutePath());
					}
					
					try
					{
						RsaDb.copyTable(getDatabasePath(prefs.getString(RsaDb.ACTUALDBKEY, RsaDbHelper.DB_NAME1)).toString(), db, binFlag);
					} 
					catch (Exception e)
					{ 
						Log.d("copyTable","EXCEPTION in method");
					}
					
		            hMess = mHandler.obtainMessage();
		            data.putString("LOG", getResources().getString(R.string.download_loading_succ));
		            hMess.setData(data); 
		            mHandler.sendMessage(hMess);
					if (!prefs.getString(RsaDb.ACTUALDBKEY, "").equals(RsaDbHelper.DB_NAME1))
					{
						prefs.edit().putString(RsaDb.ACTUALDBKEY, RsaDbHelper.DB_NAME1).commit();
					}
					else
					{
						prefs.edit().putString(RsaDb.ACTUALDBKEY, RsaDbHelper.DB_NAME2).commit();
					}
			} 
			catch (JDBFException e)
			{
				prefs.edit().putBoolean(RsaDb.ACTIVESYNCKEY, false).commit();
	            hMess = mHandler.obtainMessage();
	            data.putString("LOG", getResources().getString(R.string.download_err_dbf));
	            hMess.setData(data); 
	            mHandler.sendMessage(hMess);
				/////////////////////////////////////////////////////////////////////////////////////////////////
				// Try to close everithing before return
		    	    if (db != null) 
		    	    {
		    	    	db.close();
		    	    }
				return;
			}
			catch (Exception e2)
			{
				prefs.edit().putBoolean(RsaDb.ACTIVESYNCKEY, false).commit();
	            hMess = mHandler.obtainMessage();
	            data.putString("LOG", getResources().getString(R.string.download_err_xml));
	            hMess.setData(data); 
	            mHandler.sendMessage(hMess);
				/////////////////////////////////////////////////////////////////////////////////////////////////
				// Try to close everithing before return
		    	    if (db != null) 
		    	    {
		    	    	db.close();
		    	    }
				return;
			}
			db.execSQL("PRAGMA foreign_keys=ON;");
		}
		
		prefs.edit().putString(RsaDb.LASTDWNLDKEY, txtLog.getText().toString()).commit();
		prefs.edit().putBoolean(RsaDb.ACTIVESYNCKEY, false).commit();
	}
	
	/**
	 *  Saving activity positions on destroy .. or if display rotation
	 */
	@Override
	protected void onSaveInstanceState (Bundle outState)
	{
		super.onSaveInstanceState(outState);
		
		// save LOG state
		outState.putString("LOG", txtLog.getText().toString());
		outState.putInt("IPROGRESS", iProgress);
		outState.putInt("MAXPROGRESS", maxProgress);
		outState.putString("CURTABLE", curTable);
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
			Toast.makeText(getApplicationContext(), R.string.download_wait, Toast.LENGTH_SHORT).show(); 
			return;
		}
		
		prefs.edit().putString(RsaDb.LASTDWNLDKEY, txtLog.getText().toString()).commit();
	    if (db != null) 
	    {
	    	db.close();
	    }
	    
    	finish();
	}
	
    /**
     * Method that starts if system trying to destroy activity
     */
    @Override
    protected void onDestroy() 
    {
    	super.onDestroy();
    	
    	/*   if ((db!=null)&&(!confChanged)) 
	    {
	    	db.close();
	    }
    	 */ 
	    prefs = getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);
	    
	    prefs.edit().putBoolean(RsaDb.ACTIVESYNCKEY, false).commit();
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

	@Override
	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder adb = new AlertDialog.Builder(this);
		adb.setTitle("Внимание");
		adb.setMessage("Очистить историю заказов и кассы согласно установленным значениям?");
		adb.setPositiveButton("Да", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				SharedPreferences def_prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
					RSAActivity.shrinkOrders(getApplicationContext(),
							Integer.parseInt(def_prefs.getString(RsaDb.ORDERHYST, "30")),
							Integer.parseInt(def_prefs.getString("ordclean", "7")));
			}
		});
		adb.setNegativeButton("Нет", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {

			}
		});
		return adb.create();
	}
}
