package ua.rsa.gd;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Calendar;

import ua.rsa.gd.R;
import ua.rsa.gd.external.javadbf.JDBFException;
import ua.rsa.gd.org.apache.commons.net.ftp.FTP;
import ua.rsa.gd.org.apache.commons.net.ftp.FTPClient;
import ua.rsa.gd.org.apache.commons.net.ftp.FTPReply;


import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * @author Komarev Roman
 * Odessa, neo3da@mail.ru, +380503412392
 */
public class SyncAuto_FTP implements Runnable
{
	static SQLiteDatabase db;
	static public Thread background;
	static Handler mHandler;
	SharedPreferences prefs;
	Mail mMessage;
    Context context;
	String appPath;
	static RsaDbHelper mDb;
	String curDate;
	String ordDate;
	
    int countOrders;
    boolean useGPS;
	String mOrderingId;

    SyncAuto_FTP (Context cxt, String orderingId) {
		mOrderingId = orderingId;
    	context = cxt;
    	useGPS = context.getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE).getBoolean(RsaDb.GPSKEY, false);

        background = new Thread(this);
    	
        final Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);
    	  	
		curDate = String.format( "%02d.%02d.%02d", mDay, mMonth+1, mYear);
		ordDate = String.format( "%02d_%02d_%02d", mDay, mMonth+1, mYear);
		background.start();		
    }
    
	@Override
	public void run()
	{
		boolean isLicensed = true;
		boolean isLicenseCheck = false;
		prefs = context.getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);
	    prefs.edit().putBoolean(RsaDb.ACTIVESYNCKEY, true).commit();
		appPath = context.getFilesDir().getAbsolutePath();
		RsaApplication app = (RsaApplication) context.getApplicationContext();
		
		mDb = new RsaDbHelper(context, RsaDbHelper.DB_ORDERS);
        db = mDb.getWritableDatabase();
        
        try  {
        	String imei = null;
            String imei2 = null;
            try {
            	TelephonyInfo telephonyInfo = TelephonyInfo.getInstance(context.getApplicationContext());
            	imei	= telephonyInfo.getImeiSIM1();
            	imei2	= telephonyInfo.getImeiSIM2();
            } catch (Exception dd) {
            	imei = RsaDb.getDImei(context.getApplicationContext());
            	imei2 = null;
            }
            
            if (imei==null) {
            	imei = RsaDb.getDImei(context.getApplicationContext());
            	imei2 = null;
            }
            
	        SharedPreferences ___prefs = context.getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);
	        SharedPreferences def_pref = PreferenceManager.getDefaultSharedPreferences(context);
	        
	        RsaDbHelper main__mDb = new RsaDbHelper(context, ___prefs.getString(RsaDb.ACTUALDBKEY, RsaDbHelper.DB_NAME1));
	     	CheckVersionThread getverThread = new CheckVersionThread(	imei, imei2, 
	     																___prefs, db, main__mDb, 
	     																useGPS, 
	     																___prefs.getBoolean(RsaDb.SENDLINES, false),
	     																def_pref.getBoolean("chkRestSend", false)); 
	        getverThread.start();
        } catch(Exception e) {}
        
        
        isLicensed = true;
        isLicenseCheck = false;
        
        if ((isLicenseCheck==true) && (isLicensed==false)) {
    		prefs.edit().putBoolean(RsaDb.ACTIVESYNCKEY, false).commit();
    	    if (db != null)  {
    	    	db.close();
    	    }    		
        	return;
        } else {
        	FTPClient ftp = new FTPClient();
            boolean error = false;

            try  {
        		////////////////////////////////////////////////////////////////////////////////////////////////////
        		// Try to create files with orders at selected day
        		try {
        			Log.d("FtpSendActivity", "run() - try to get files dir");
        			String Pth = context.getApplicationContext().getFilesDir().getAbsolutePath();
        			Log.d("FtpSendActivity", "run() - OK. files dir is: " + Pth);
                	Log.d("FtpSendActivity", "run() - DBF or XML?");
					if (context.getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE)
              				.getString(RsaDb.INTERFACEKEY, "DBF").equals("DBF"))
					{
						Log.d("FtpSendActivity", "run() - DBF, so try to create DBF-files");
						
						Activity aaa = null;
						countOrders = RsaDb.dbToDBF(aaa, context.getApplicationContext(), db, "outbox/Head.dbf", "outbox/Lines.dbf", curDate, R.drawable.ic_locked_, Pth, false);
						Log.d("FtpSendActivity", "run() - OK. Files was created =)");
					}
					else if (context.getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE)
              				.getString(RsaDb.INTERFACEKEY, "DBF").equals("XML"))
					{
						Log.d("FtpSendActivity", "run() - XML, so try to create XML-files");
		                try {  
		                	SharedPreferences __prefs = context.getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);
		            		RsaDbHelper __mDb = new RsaDbHelper(context, __prefs.getString(RsaDb.ACTUALDBKEY, RsaDbHelper.DB_NAME1));
		            		
		            		String headName = "outbox/Head.xml";
		            		String linesName = "outbox/Lines.xml";
		            		
							if ((PreferenceManager.getDefaultSharedPreferences(context)).getBoolean("prefExtendedFilename", false)) {
								headName= "outbox/"+context.getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE)
										.getString(RsaDb.CODEKEY, "") + "_HeadTS_" +ordDate+".xml";
								linesName= "outbox/"+context.getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE)
										.getString(RsaDb.CODEKEY, "") + "_LinesTS_" +ordDate+".xml";
							}
		            		
							Activity aaa = null;
		                	countOrders = RsaDb.dbToXML(aaa, context.getApplicationContext(), db, __mDb, headName, linesName, curDate, R.drawable.ic_locked_, Pth, false, mOrderingId);
		                	Log.d("FtpSendActivity", "run() - OK. Files was created =)");	
		                } catch (Exception e) { 
		                	throw new IOException();
		                }
					}
        		} 
            	catch (JDBFException e)
        		{
            		Log.d("FtpSendActivity", "run() - ERROR. JDBF");
            		prefs.edit().putBoolean(RsaDb.ACTIVESYNCKEY, false).commit();
                   
            	    if (db != null)  {
            	    	db.close();
            	    }
        			return;
        		} 
            	catch (IOException e)
        		{
            		Log.d("FtpSendActivity", "run() - ERROR. IO");
            		prefs.edit().putBoolean(RsaDb.ACTIVESYNCKEY, false).commit();
                    
            	    if (db != null)  {
            	    	db.close();
            	    }
        			return;
        		}
        		catch (Exception e5)
        		{
        			Log.d("FtpSendActivity", "run() - ERROR. Unknown");
        		}
        		
        		
        		// if files was created successfully then write about it...
            	
                Log.d("FtpSendActivity", "run() - Try to make archives");
        		////////////////////////////////////////////////////////////////////////////////////////////////////
        		// Try to make archives
        		try
        		{
					if (context.getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE)
              				.getString(RsaDb.INTERFACEKEY, "DBF").equals("DBF"))
        			{
						RsaDb.toLzma(context, "outbox" + File.separator + "Head.dbf", "outbox" + File.separator + "HeadTS.dbf.lzma");
						RsaDb.toLzma(context, "outbox" + File.separator + "Lines.dbf", "outbox" + File.separator + "LinesTS.dbf.lzma");
        			}
					else if (context.getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE)
              				.getString(RsaDb.INTERFACEKEY, "DBF").equals("XML"))
					{
						if ((PreferenceManager.getDefaultSharedPreferences(context)).getBoolean("prefExtendedFilename", false)) {
							String agentCode= context.getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE).getString(RsaDb.CODEKEY, "");
							RsaDb.toZip(context, "outbox" + File.separator + agentCode+"_HeadTS_"+ordDate+".xml", "outbox" + File.separator + "HeadTS.xml.zip", null);
							RsaDb.toZip(context, "outbox" + File.separator + agentCode+"_LinesTS_"+ordDate+".xml", "outbox" + File.separator + "LinesTS.xml.zip", null);
						} else {
							RsaDb.toZip(context, "outbox" + File.separator + "Head.xml", "outbox" + File.separator + "HeadTS.xml.zip", null);
							RsaDb.toZip(context, "outbox" + File.separator + "Lines.xml", "outbox" + File.separator + "LinesTS.xml.zip", null);
						}
						
					}
					else if (context.getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE)
              				.getString(RsaDb.INTERFACEKEY, "DBF").equals("CSV"))
					{
						///// Надо вставить архивацию всех *.XLS файлов в папке outbox
						// затем удалить все XLS
						RsaDb.toZipArray(context, "outbox", "outbox" + File.separator + "HeadTS.csv.zip");
					}
					
        		} 
        		catch (Exception e)
        		{
        			Log.d("FtpSendActivity", "run() - ERROR.");
        			
            	    if (db != null) {
            	    	db.close();
            	    }
        			return;
        		}
        		Log.d("FtpSendActivity", "run() - success!");
                
                Log.d("FtpSendActivity", "run() - try to send archives");
            	/////////////// SEND ARCHIVES VIA FTP
            	// Reply code shows us if connection successed
            	int reply;
            	// Estabilish connection 
            	String serv = prefs.getString(RsaDb.FTPSERVER, "");
            	String p = prefs.getString(RsaDb.FTPPORT, "21");
            	ftp.setConnectTimeout(5000);
            	ftp.connect(prefs.getString(RsaDb.FTPSERVER, ""), Integer.parseInt(p));
            	Log.d("FtpSendActivity", "server=" +serv);
            	Log.d("FtpSendActivity", "port" +p);
            	Log.d("FtpSendActivity", "run() - connected");
            	// Show message about server status
            	System.out.print(ftp.getReplyString());
            	// After connection attempt, we should check the reply code to verify success.
            	reply = ftp.getReplyCode();

            	// if server status bad then disconnect and show message 
            	if (!FTPReply.isPositiveCompletion(reply)) 
            	{
            		Log.d("FtpSendActivity", "run() - ftp not ready EXIT");
            		// Disconnect
            		ftp.disconnect();
            		// Set status of sync process to false
            		prefs.edit().putBoolean(RsaDb.ACTIVESYNCKEY, false).commit();
            		// end run() process
            	    if (db != null) 
            	    {
            	    	db.close();
            	    }
            		return;
            	}
            	
            	Log.d("FtpSendActivity", "run() - ftp OK");
            	// Try to login on ftp
            	if (ftp.login(prefs.getString(RsaDb.FTPUSER, ""), prefs.getString(RsaDb.FTPPASSWORD, "")))
            	{
            		Log.d("FtpSendActivity", "run() - login OK");
            	}
            	else
            	{
            		Log.d("FtpSendActivity", "run() - login error");
                	// Set status of sync process to false
            		prefs.edit().putBoolean(RsaDb.ACTIVESYNCKEY, false).commit();
            		// end run() process
            	    if (db != null) 
            	    {
            	    	db.close();
            	    }
                	return;
            	}

            	// Activate passive mode
            	ftp.enterLocalPassiveMode();
            	// Go to outbox dir - root dir if not specified
            	if (ftp.changeWorkingDirectory(prefs.getString(RsaDb.FTPOUTBOX, "/")))
            	{
            		Log.d("FtpSendActivity", "run() - open dir OK");
            	}
            	else
            	{
            		Log.d("FtpSendActivity", "run() - open dir ERROR");
                	// Set status of sync process to false
            		prefs.edit().putBoolean(RsaDb.ACTIVESYNCKEY, false).commit();
            		// end run() process
            	    if (db != null) 
            	    {
            	    	db.close();
            	    }
                	return;
            	}
            	
            	// Set files type
            	if (ftp.setFileType(FTP.BINARY_FILE_TYPE))
            	{
            	}
            	else
            	{
                	// Set status of sync process to false
            		prefs.edit().putBoolean(RsaDb.ACTIVESYNCKEY, false).commit();
            		// end run() process
            	    if (db != null) 
            	    {
            	    	db.close();
            	    }
                	return;
            	}
            	
            	Log.d("FtpSendActivity", "run() - process ftp sending");
            	// Set input stream for copying files from device to ftp folder
            	BufferedInputStream buffInH = null;
            	BufferedInputStream buffInL = null;
            	File fileH = null;
            	File fileL = null;
            	
            	// Set variable that assigned to file on device
				if (context.getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE)
          				.getString(RsaDb.INTERFACEKEY, "DBF").equals("DBF"))
				{
					fileH = new File(appPath + File.separator + "outbox" + File.separator + "HeadTS.dbf.lzma");
					fileL = new File(appPath + File.separator + "outbox" + File.separator + "LinesTS.dbf.lzma");
				}
				else if (context.getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE)
          				.getString(RsaDb.INTERFACEKEY, "DBF").equals("XML"))
				{
						fileH = new File(appPath + File.separator + "outbox" + File.separator + "HeadTS.xml.zip");
						fileL = new File(appPath + File.separator + "outbox" + File.separator + "LinesTS.xml.zip");
				}
				else if (context.getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE)
          				.getString(RsaDb.INTERFACEKEY, "DBF").equals("CSV"))
				{
					// Указать файл для записи на ФТП
					fileH = new File(appPath + File.separator + "outbox" + File.separator + "HeadTS.csv.zip");
				}
				
				if (context.getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE)
          				.getString(RsaDb.INTERFACEKEY, "DBF").equals("CSV")) {
	            	// Assign file with inputstream
					buffInH = new BufferedInputStream(new FileInputStream(fileH));
				} else {
					buffInH = new BufferedInputStream(new FileInputStream(fileH));
					buffInL = new BufferedInputStream(new FileInputStream(fileL));
				}
				
				// Enter passive mode on ftp
				ftp.enterLocalPassiveMode();
            	
				/** Key that gives to know if transfered was successful */
				boolean transferedH = false;
				boolean transferedL = false;
				if (context.getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE)
          				.getString(RsaDb.INTERFACEKEY, "DBF").equals("DBF"))
				{
					transferedH = ftp.storeFile("HeadTS_" + ordDate + ".dbf.lzma", buffInH);
					transferedL = ftp.storeFile("LinesTS_" + ordDate + ".dbf.lzma", buffInL);
				}
				else if (context.getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE)
          				.getString(RsaDb.INTERFACEKEY, "DBF").equals("XML"))
				{
					String agentCode = "";
					if ((PreferenceManager.getDefaultSharedPreferences(context)).getBoolean("prefExtendedFilename", false)) {
						agentCode= context.getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE)
								.getString(RsaDb.CODEKEY, "") + "_";
					}
						
					
					transferedH = ftp.storeFile(agentCode+"HeadTS_" + ordDate + ".xml.zip", buffInH);
					transferedL = ftp.storeFile(agentCode+"LinesTS_" + ordDate + ".xml.zip", buffInL);	
				}

				
            	// if transfer complite then ...
            	if (transferedH && transferedL) 
            	{
            		
            	}
            	else
            	{
                	// Set status of sync process to false
            		prefs.edit().putBoolean(RsaDb.ACTIVESYNCKEY, false).commit();
            		// end run() process
            	    if (db != null) 
            	    {
            	    	db.close();
            	    }
                	return;
            	}
            	
            	// close input stream, not needed
            	buffInH.close();
            	if (context.getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE)
          				.getString(RsaDb.INTERFACEKEY, "DBF").equals("CSV") == false) {
            		buffInL.close();
            	}	
            	// End session and logout
            	if (ftp.logout())
            	{
            	}
            	else
            	{
                	// Set status of sync process to false
            		prefs.edit().putBoolean(RsaDb.ACTIVESYNCKEY, false).commit();
            		// end run() process
            	    if (db != null) 
            	    {
            	    	db.close();
            	    }
            	}
            } 
            catch(IOException e) 
            {
            	// Set status of sync process to false
        		prefs.edit().putBoolean(RsaDb.ACTIVESYNCKEY, false).commit();
        		// Error key switch to true
            	error = true;
            	// end run() process
        	    if (db != null) 
        	    {
        	    	db.close();
        	    }
        		return;
            } 
            finally 
            {
            	if (ftp.isConnected()) 
            	{
            		try 
            		{
            			ftp.disconnect();
            		} 
            		catch(IOException ioe) 
            		{
            			// do nothing
            		}
            	}
            	if (error)
				{
            		// Set status of sync process to false
            		prefs.edit().putBoolean(RsaDb.ACTIVESYNCKEY, false).commit();
            	    if (db != null) 
            	    {
            	    	db.close();
            	    }
            		return;
				}
            }
        }
        
        
        
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
        
        
        
        // Close db not needed
	    if (db != null) 
	    {
	    	db.close();
	    }
	    
	    
	    // Set status of sync process to false before end thread
	    prefs.edit().putBoolean(RsaDb.ACTIVESYNCKEY, false).commit();
	}
	

}
