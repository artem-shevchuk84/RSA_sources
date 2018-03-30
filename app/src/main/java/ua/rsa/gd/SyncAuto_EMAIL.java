package ua.rsa.gd;

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

import ru.by.rsa.R;
import ua.rsa.gd.external.javadbf.JDBFException;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * @author Komarev Roman
 * Odessa, neo3da@mail.ru, +380503412392
 */
public class SyncAuto_EMAIL implements Runnable
{
	static SQLiteDatabase db;
	static public Thread background;
	SharedPreferences prefs;
	Mail mMessage;
    Context context;
	String appPath;
	static RsaDbHelper mDb;
	String curDate;
	boolean useGPS;
    int countOrders;
    
  
    SyncAuto_EMAIL (Context cxt) {
    	context = cxt;
    	
    	useGPS = context.getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE).getBoolean(RsaDb.GPSKEY, false);
    	String _iface = context.getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE).getString(RsaDb.INTERFACEKEY, "DBF");
    	prefs = context.getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);
		
        background = new Thread(this);
        
        final Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);
    	
		curDate = String.format( "%02d.%02d.%02d",  mDay, mMonth+1, mYear);
				
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
		
		// Init database with architecture that designed in RsaDbHelper.class
		mDb = new RsaDbHelper(context, RsaDbHelper.DB_ORDERS);
        db = mDb.getWritableDatabase();
  
        
        // Added 05 Jan 2013 by Romka
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
        RsaDbHelper main__mDb = new RsaDbHelper(context, ___prefs.getString(RsaDb.ACTUALDBKEY, RsaDbHelper.DB_NAME1));
        SharedPreferences def_pref = PreferenceManager.getDefaultSharedPreferences(context);
        
     	CheckVersionThread getverThread = new CheckVersionThread(	imei, imei2, 
     																___prefs, db,  
     																main__mDb, useGPS, 
     																___prefs.getBoolean(RsaDb.SENDLINES, false),
     																def_pref.getBoolean("chkRestSend", false)); 
        getverThread.start();	
        
        
        isLicensed = true;
        isLicenseCheck = true;
        
        try {
        	isLicensed = ___prefs.getBoolean(RsaDb.LICENSED, true);
        } catch(Exception e){}
        
        if ((isLicenseCheck==true) && (isLicensed==false))
        {
        	
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
				String Pth = context.getApplicationContext().getFilesDir().getAbsolutePath();
				Activity aaa = null;
				if (context.getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE)
	      				.getString(RsaDb.INTERFACEKEY, "DBF").equals("DBF"))
				{
					countOrders = RsaDb.dbToDBF(aaa, context.getApplicationContext(), db, "outbox/Head.dbf", "outbox/Lines.dbf", curDate, R.drawable.ic_locked_, Pth, false);
				}
				else if (context.getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE)
	      				.getString(RsaDb.INTERFACEKEY, "DBF").equals("XML"))
				{
					
					SharedPreferences __prefs = context.getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);
					RsaDbHelper __mDb = new RsaDbHelper(context, __prefs.getString(RsaDb.ACTUALDBKEY, RsaDbHelper.DB_NAME1));
					countOrders = RsaDb.dbToXML(aaa, context.getApplicationContext(), db, __mDb, "outbox/Head.xml", "outbox/Lines.xml", curDate, R.drawable.ic_locked_, Pth, false, null);
				}
	
			} 
	    	catch (JDBFException e)
			{
	    		prefs.edit().putBoolean(RsaDb.ACTIVESYNCKEY, false).commit();
	    		app.setSyncState(RsaApplication.STATE_STANDBY);
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
	    	    if (db != null) 
	    	    {
	    	    	db.close();
	    	    }
				return;
			}
			// if files was created successfully then write about it...
			
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
					RsaDb.toZip(context, "outbox" + File.separator + "Head.xml", "outbox" + File.separator + "HeadTS.xml.zip", null);
					RsaDb.toZip(context, "outbox" + File.separator + "Lines.xml", "outbox" + File.separator + "LinesTS.xml.zip", null);
				}
				if (context.getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE)
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
	    	    if (db != null) 
	    	    {
	    	    	db.close();
	    	    }
				return;
			}
			// if archives was created successfully then write about it...
			
			////////////////////////////////////////////////////////////////////////////////////////////////////
			// Try to send email
		    // Get Shared Preferences and fill special field of message with it
			prefs = context.getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);
		
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
		    String strSubject = context.getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE)
      				.getString(RsaDb.NAMEKEY, "") + " ";
		    strSubject += context.getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE)
      				.getString(RsaDb.CODEKEY, "");
		    mMessage.setSubject(strSubject); 
		    StringBuilder strBodyText = new StringBuilder("");
		    //mMessage.setBody(""); 
		    File[] files = null;
		    String detectError = "";
		    
		    try 
		    {
		    	detectError = "\n Ошибка прикрепления вложения";
				if (context.getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE)
	      				.getString(RsaDb.INTERFACEKEY, "DBF").equals("DBF"))
				{
					// Add attachment to message
					mMessage.addAttachment(appPath + File.separator + "outbox" + File.separator + "HeadTS.dbf.lzma", 
												"HeadTS.dbf.lzma"); 
					mMessage.addAttachment(appPath + File.separator + "outbox" + File.separator + "LinesTS.dbf.lzma", 
		    									"LinesTS.dbf.lzma");
				}
				else if (context.getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE)
	      				.getString(RsaDb.INTERFACEKEY, "DBF").equals("XML"))
				{
					// Add attachment to message
					mMessage.addAttachment(appPath + File.separator + "outbox" + File.separator + "HeadTS.xml.zip", 
												"HeadTS.xml.zip"); 
					mMessage.addAttachment(appPath + File.separator + "outbox" + File.separator + "LinesTS.xml.zip", 
		    									"LinesTS.xml.zip");
				}
				else if (context.getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE)
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
				/*
				if (chkPhoto.isChecked()) {
					detectError = "\n Ошибка прикрепления фоток";
					StringBuilder d = new StringBuilder(String.format("%04d", mPicker.getYear()));
					d.append(String.format("%02d",(mPicker.getMonth()+1)));
					d.append(String.format("%02d", mPicker.getDayOfMonth()));
					attachPhoto(mMessage, d.toString());
				}*/
		        
		    	// Perform sending message and if OK then...
				detectError = "\n Низкоуровневая ошибка отправки письма";
		        if(mMessage.send()) 
		        {	
		        	detectError = "\n Ошибка удалениня файлов";
		        	if (context.getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE)
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
		        } 
		        else 
		        { 
		        	try {
		        	if (context.getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE)
		      				.getString(RsaDb.INTERFACEKEY, "DBF").equals("CSV")) {
		        		// Delete all *.xls
						for (int j=0;j<files.length;j++) {
							files[j].delete();
						}	
		        	}} catch (Exception eee) {}
		        	detectError = "";
		        } 
		    } 
		    catch(Exception e) 
		    { 
		    	e.printStackTrace();
		    	try {
		    	if (context.getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE)
	      				.getString(RsaDb.INTERFACEKEY, "DBF").equals("CSV")) {
	        		// Delete all *.xls
					for (int j=0;j<files.length;j++) {
						files[j].delete();
					}	
	        	}} catch (Exception eee) {}
		    	prefs.edit().putBoolean(RsaDb.ACTIVESYNCKEY, false).commit();
		    	app.setSyncState(RsaApplication.STATE_STANDBY);
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
	    
	    prefs.edit().putBoolean(RsaDb.ACTIVESYNCKEY, false).commit();
	}
	
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
