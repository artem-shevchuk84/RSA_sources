package ua.rsa.gd;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Calendar;

import ru.by.rsa.R;
import ua.rsa.gd.external.javadbf.JDBFException;
import ua.rsa.gd.org.apache.commons.net.ftp.FTP;
import ua.rsa.gd.org.apache.commons.net.ftp.FTPClient;
import ua.rsa.gd.org.apache.commons.net.ftp.FTPReply;
import ua.rsa.gd.utils.FileUtils;
import ua.rsa.gd.utils.GpsUtils;


import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
 *
 * @author Komarev Roman
 *         Odessa, neo3da@mail.ru, +380503412392
 */
public class FtpSendActivity extends Activity implements Runnable {
    /**
     * Used for day selection. All orders from that day will be sended by email
     */
    DatePicker mPicker;
    /**
     * Send email by button press
     */
    Button btnSend;
    /**
     * TextView. used like console to show status of sending mail
     */
    TextView mText;
    /**
     * SQLite database that stores all data
     */
    static SQLiteDatabase db;
    /**
     * For store pointer to this activity
     */
    Runnable mAct;
    Activity mAct2;
    /**
     * Thread for background downloading
     */
    static public Thread background;
    /**
     * Used to send messages from Thread to main Thread
     */
    static Handler mHandler;
    /**
     * Get Shared Preferences and fill special field of message with it
     */
    SharedPreferences prefs;
    /**
     * JavaMail variable
     */
    Mail mMessage;
    /**
     * Context for some methods
     */
    Context context;
    /**
     * Path to application file storage
     */
    String appPath;
    /**
     * Init database with architecture that designed in RsaDbHelper.class
     */
    static RsaDbHelper mDb;
    /**
     * Current date
     */
    String curDate;
    String curDateAmer;
    /**
     * Current date for orders in format
     */
    String ordDate;

    /**
     * Orders count
     */
    int countOrders;

    boolean useGPS;

    /**
     * Current theme
     */
    private boolean lightTheme;

    CheckBox chkNew;

    public void onCreate(Bundle savedInstanceState) {
        lightTheme = getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE).getBoolean(RsaDb.LIGHTTHEMEKEY, false);
        useGPS = getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE).getBoolean(RsaDb.GPSKEY, false);

        if (lightTheme) {
            setTheme(R.style.Theme_Custom);
        } else {
            setTheme(R.style.Theme_CustomBlack2);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.send);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mAct = this;
        mAct2 = this;

        // Bind variables with xml elements
        mPicker = (DatePicker) findViewById(R.id.datePicker_send);
        btnSend = (Button) findViewById(R.id.btnSend_send);
        final TextView txtLog = (TextView) findViewById(R.id.txtLog_send);
        chkNew = (CheckBox) findViewById(R.id.chkNew);

        if (lightTheme) txtLog.setTextColor(0xFF0000BB);

        // if activity was destroyed (for exmp. by display rotation)
        // then get data of LOG from previous state
        if (savedInstanceState != null) {
            txtLog.setText(savedInstanceState.getString("LOG"));
        } else {
            // Get Shared Preferences and fill special field of message with it
            prefs = getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);
            txtLog.setText(prefs.getString(RsaDb.FTPLASTSENDKEY, ""));
        }

        // Init new thread with function run() in that class
        background = new Thread(mAct);

        mHandler = new Handler() {
            @Override
            public void handleMessage(android.os.Message msg) {
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

        btnSend.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Log.d("FtpSendActivity", "onClick() - just pressed");
                prefs = getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);
                if (prefs.getBoolean(RsaDb.ACTIVESYNCKEY, true)) {
                    Log.d("FtpSendActivity", "onClick() - cant do that, beacuse of sync in progress");
                    Toast.makeText(getApplicationContext(), R.string.send_already, Toast.LENGTH_SHORT).show();
                    return;
                }

                // Clear "console" TextView
                txtLog.setText("");

                // Get current date from DataPicker to String curDate in format like 23.02.2011
                curDate = String.format("%02d.%02d.%02d", mPicker.getDayOfMonth(),
                        mPicker.getMonth() + 1,
                        mPicker.getYear());

                curDateAmer = String.format("%04d-%02d-%02d", mPicker.getYear(), mPicker.getMonth() + 1,
                        mPicker.getDayOfMonth());

                // Get current date from DataPicker to String ordDate in format like 23_02_2011
                ordDate = String.format("%02d_%02d_%02d", mPicker.getDayOfMonth(),
                        mPicker.getMonth() + 1,
                        mPicker.getYear());
                Log.d("FtpSendActivity", "onClick() - starting background Thread (to send data via internet)");
                // Start downloading in thread
                background.start();

                btnSend.setClickable(false);
            }
        });
    }

    /**
     * Saving activity positions on destroy .. or if display rotation
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        /** Binding xml elements of view to variables */
        final TextView txtLog = (TextView) findViewById(R.id.txtLog_send);
        // save LOG state
        outState.putString("LOG", txtLog.getText().toString());
    }

    /**
     * If Back-button on device pressed then do...
     */
    @Override
    public void onBackPressed() {
        prefs = getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);
        if (prefs.getBoolean(RsaDb.ACTIVESYNCKEY, true)) {
            Toast.makeText(getApplicationContext(), R.string.send_wait, Toast.LENGTH_SHORT).show();
            return;
        }

        /** Binding xml elements of view to variables */
        final TextView txtLog = (TextView) findViewById(R.id.txtLog_send);

        prefs.edit().putString(RsaDb.FTPLASTSENDKEY, txtLog.getText().toString()).commit();

        if (db != null) {
            db.close();
        }

        finish();
    }

    /**
     * Thread body
     */
    @Override
    public void run() {
        Log.d("FtpSendActivity", "run() - Thread just started 2.11");
        /** Handler message */
        android.os.Message hMess;
        Bundle data = new Bundle();

        /** Device has registration */
        boolean isLicensed = true;

        /** Is License checking is active? */
        boolean isLicenseCheck = false;

        // Get Shared Preferences and fill special field of message with it
        prefs = getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);

        // Set key that stores status of syncronization (in progress or no) to true
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
            if (db != null) db.close();
            return;
        }
        app.setSyncState(RsaApplication.STATE_MANUALSYNC);

        // Init database with architecture that designed in RsaDbHelper.class
        mDb = new RsaDbHelper(context, RsaDbHelper.DB_ORDERS);

        // Open database with orders 
        db = mDb.getWritableDatabase();

        Log.d("FtpSendActivity", "run() - trying to get IMEI");
        try {
            String imei = null;
            String imei2 = null;
            try {
                TelephonyInfo telephonyInfo = TelephonyInfo.getInstance(getApplicationContext());
                imei = telephonyInfo.getImeiSIM1();
                imei2 = telephonyInfo.getImeiSIM2();
            } catch (Exception dd) {
                imei = RsaDb.getDImei(getApplicationContext());
                imei2 = null;
            }

            if (imei == null) {
                imei = RsaDb.getDImei(getApplicationContext());
                imei2 = null;
            }

            SharedPreferences ___prefs = getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences def_pref = PreferenceManager.getDefaultSharedPreferences(this);

            RsaDbHelper main__mDb = new RsaDbHelper(this, ___prefs.getString(RsaDb.ACTUALDBKEY, RsaDbHelper.DB_NAME1));
            CheckVersionThread getverThread = new CheckVersionThread(imei, imei2,
                    ___prefs, db,
                    main__mDb, useGPS,
                    ___prefs.getBoolean(RsaDb.SENDLINES, false),
                    def_pref.getBoolean("chkRestSend", false));
            getverThread.start();
        } catch (Exception e) {
        }

        Log.d("FtpSendActivity", "run() - done");
        /*
        // License verification 
        try 
    	{
        	RsaDb.Au a = new RsaDb.Au();
    		// Ticket 37: Set URL for IMEI checking 
    		URL url = new URL(    a.getS1() + a.getS2() + a.getS3() + a.getS4() + a.getS5()
    	        				+ a.getS6() + a.getS7() + a.getS8() + a.getS9() + a.getS10()
    	        				+ a.getS11() + a.getS12() + a.getS13()
    					+ ((TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId());

    		// Ticket 37: Up connection 
    		URLConnection conn = url.openConnection();
    		// Ticket 37: Init reader for HTML text 
    		InputStreamReader rd = new InputStreamReader(conn.getInputStream());
    		// Ticket 37: Init buffer for parsing HTML text 
    		BufferedReader reader = new BufferedReader(rd);
    		// Ticket 37: Init StringBuilder for building HTML text to variable 
    		StringBuilder allpage = new StringBuilder();
    		// Ticket 37: Temp variable to store lines 
    		String line = null;
    		// Ticket 37: Read lines from URL while ends 
    		while ((line = reader.readLine()) != null) 
    		{
    			// Ticket 37: Incremental write lines of HTML text to string builder variable 
    			allpage.append(line + System.getProperty("line.separator"));
    		}
    		// Ticket 37: Convert HTML to simple string 
    		String pagetext = allpage.toString();

    		// Ticket 37: Check if HTML text has word YES from mySQL Server 
    		if ((pagetext.length()>4)||(pagetext.contains("YES")==true)||(pagetext.length()<1))
    		{
    			isLicensed = true;
    		}

    		//shit
    		// Ticket 37: Set URL to check Licensed function is active or no 
    		url = new URL(a.getS1() + a.getS2() + a.getS3() + a.getS4() + a.getS5()
    				+ a.getS6() + a.getS7() + a.getS8() + a.getS9() + a.getS10()
    				+ a.getS11() + a.getS12() + a.getS13() 
    				+ a.getD1() + a.getD2());
    		// Ticket 37: Up connection 
    		conn = url.openConnection();
    		// Ticket 37: Init reader for HTML text 
    		rd = new InputStreamReader(conn.getInputStream());
    		// Ticket 37: Init buffer for parsing HTML text 
    		reader = new BufferedReader(rd);
    		// Ticket 37: Init StringBuilder for building HTML text to variable 
    		allpage = new StringBuilder();
    		// Ticket 37: Temp variable to store lines 
    		line = null;
    		// Ticket 37: Read lines from URL while ends 
    		while ((line = reader.readLine()) != null) 
    		{
    			// Ticket 37: Incremental write lines of HTML text to string builder variable 
    			allpage.append(line + System.getProperty("line.separator"));
    		}
    		// Ticket 37: Convert HTML to simple string 
    		pagetext = allpage.toString();

    		// Ticket 37: Check if HTML text has word YES from mySQL Server 
    		if ((pagetext.length()>2)&&(pagetext.contains("YES")==true)&&(pagetext.length()<4))
    		{
    			isLicenseCheck = true;
    		}
    		else 
    		{
    			isLicenseCheck = false;
    		}
    		// Ticket 37: Close buffer 
    		reader.close();
    		// Ticket 37: Close Stream 
    		rd.close();
    	}
    	catch(Exception e)
    	{
    		//  Ticket 37: If exception then not licensed
    		// but write that is no internet
            hMess = mHandler.obtainMessage();
            data.putString("LOG", getResources().getString(R.string.send_nointernet));
            hMess.setData(data); 
            mHandler.sendMessage(hMess);
            
            isLicensed = true;
            isLicenseCheck = true;
    	}
        */

        isLicensed = true;
        isLicenseCheck = false;

        Log.d("FtpSendActivity", "run() - licensed?");
        // if license checking is active, but device has no license...
        if ((isLicenseCheck == true) && (isLicensed == false)) {
            Log.d("FtpSendActivity", "run() - NO! EXIT!");
            // Show message that current device is not active
            hMess = mHandler.obtainMessage();
            data.putString("LOG", getResources().getString(R.string.send_nointernet) + "ИЛИ\n"
                    + getResources().getString(R.string.send_license));
            hMess.setData(data);
            mHandler.sendMessage(hMess);

            // Set status of sync process to false
            prefs.edit().putBoolean(RsaDb.ACTIVESYNCKEY, false).commit();
            if (db != null) {
                db.close();
            }
            return;
        } else {
            Log.d("FtpSendActivity", "run() - YES. Begin to connection");
            /** FTP connection instance */
            FTPClient ftp = new FTPClient();
            /** Key that gives to know about errors while process */
            boolean error = false;

            // Create files, Estabilishing connection, login, password, changedir
            try {
                ////////////////////////////////////////////////////////////////////////////////////////////////////
                // Try to create files with orders at selected day
                try {
                    Log.d("FtpSendActivity", "run() - try to get files dir");
                    String Pth = getApplicationContext().getFilesDir().getAbsolutePath();
                    Log.d("FtpSendActivity", "run() - OK. files dir is: " + Pth);

                    hMess = mHandler.obtainMessage();
                    data.putString("LOG", "> Выгрузка в " + Pth + "\n");
                    hMess.setData(data);
                    mHandler.sendMessage(hMess);
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e2) {
                        return;
                    }

                    Log.d("FtpSendActivity", "run() - DBF or XML?");
                    if (getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE)
                            .getString(RsaDb.INTERFACEKEY, "DBF").equals("DBF")) {
                        Log.d("FtpSendActivity", "run() - DBF, so try to create DBF-files");

                        countOrders = RsaDb.dbToDBF(mAct2, getApplicationContext(), db, "outbox/Head.dbf", "outbox/Lines.dbf", curDate, R.drawable.ic_locked_, Pth, chkNew.isChecked());

                        Log.d("FtpSendActivity", "run() - OK. Files was created =)");
                    } else if (getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE)
                            .getString(RsaDb.INTERFACEKEY, "DBF").equals("XML")) {
                        Log.d("FtpSendActivity", "run() - XML, so try to create XML-files");
                        try {
                            SharedPreferences __prefs = getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);
                            RsaDbHelper __mDb = new RsaDbHelper(this, __prefs.getString(RsaDb.ACTUALDBKEY, RsaDbHelper.DB_NAME1));

                            String headName = "outbox/Head.xml";
                            String linesName = "outbox/Lines.xml";

                            if ((PreferenceManager.getDefaultSharedPreferences(this)).getBoolean("prefExtendedFilename", false)) {
                                headName = "outbox/" + getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE)
                                        .getString(RsaDb.CODEKEY, "") + "_HeadTS_" + ordDate + ".xml";
                                linesName = "outbox/" + getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE)
                                        .getString(RsaDb.CODEKEY, "") + "_LinesTS_" + ordDate + ".xml";
                            }


                            countOrders = RsaDb.dbToXML(mAct2, getApplicationContext(), db, __mDb, headName, linesName, curDate, R.drawable.ic_locked_, Pth, chkNew.isChecked(), null);
                            Log.d("FtpSendActivity", "run() - OK. Files was created =)");
                        } catch (Exception e) {
                            hMess = mHandler.obtainMessage();
                            data.putString("LOG", "Error: dbToXML " + curDate + " - " + Pth + "\n" + e.getMessage());
                            hMess.setData(data);
                            mHandler.sendMessage(hMess);
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e2) {
                                return;
                            }
                            throw new IOException();
                        }
                    } else if (getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE)
                            .getString(RsaDb.INTERFACEKEY, "DBF").equals("CSV")) {
                        Log.d("FtpSendActivity", "run() - CSV, so try to create CSV-files");
                        try {
                            countOrders = RsaDb.dbToCSV(mAct2, getApplicationContext(), db, "outbox/", "not used", curDate, R.drawable.ic_locked_, Pth, chkNew.isChecked());
                            Log.d("FtpSendActivity", "run() - OK. Files was created =)");
                        } catch (Exception e) {
                            hMess = mHandler.obtainMessage();
                            data.putString("LOG", "Error: dbToCSV " + curDate + " - " + Pth);
                            hMess.setData(data);
                            mHandler.sendMessage(hMess);
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e2) {
                                return;
                            }
                            throw new IOException();
                        }
                    }
                } catch (JDBFException e) {
                    Log.d("FtpSendActivity", "run() - ERROR. JDBF");
                    prefs.edit().putBoolean(RsaDb.ACTIVESYNCKEY, false).commit();
                    hMess = mHandler.obtainMessage();
                    data.putString("LOG", getResources().getString(R.string.send_err_makingdbf));
                    hMess.setData(data);
                    mHandler.sendMessage(hMess);
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e2) {
                        return;
                    }
                    if (db != null) {
                        db.close();
                    }
                    return;
                } catch (IOException e) {
                    Log.d("FtpSendActivity", "run() - ERROR. IO");
                    prefs.edit().putBoolean(RsaDb.ACTIVESYNCKEY, false).commit();
                    hMess = mHandler.obtainMessage();
                    data.putString("LOG", getResources().getString(R.string.send_err_writedbf));
                    hMess.setData(data);
                    mHandler.sendMessage(hMess);
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e2) {
                        return;
                    }
                    if (db != null) {
                        db.close();
                    }
                    return;
                } catch (Exception e5) {
                    Log.d("FtpSendActivity", "run() - ERROR. Unknown");
                    hMess = mHandler.obtainMessage();
                    data.putString("LOG", "Не могу создать файлы. Нет места?\n");
                    hMess.setData(data);
                    mHandler.sendMessage(hMess);
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e2) {
                        return;
                    }
                }


                // if files was created successfully then write about it...
                hMess = mHandler.obtainMessage();
                data.putString("LOG", getResources().getString(R.string.send_dbfcreated)
                        + getResources().getString(R.string.send_orders)
                        + countOrders
                        + getResources().getString(R.string.send_qty));
                hMess.setData(data);
                mHandler.sendMessage(hMess);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e2) {
                    return;
                }

                Log.d("FtpSendActivity", "run() - Try to make archives");
                ////////////////////////////////////////////////////////////////////////////////////////////////////
                // Try to make archives
                try {
                    if (getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE)
                            .getString(RsaDb.INTERFACEKEY, "DBF").equals("DBF")) {
                        RsaDb.toLzma(context, "outbox" + File.separator + "Head.dbf", "outbox" + File.separator + "HeadTS.dbf.lzma");
                        RsaDb.toLzma(context, "outbox" + File.separator + "Lines.dbf", "outbox" + File.separator + "LinesTS.dbf.lzma");
                    } else if (getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE)
                            .getString(RsaDb.INTERFACEKEY, "DBF").equals("XML")) {
                        if ((PreferenceManager.getDefaultSharedPreferences(this)).getBoolean("prefExtendedFilename", false)) {
                            String agentCode = getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE).getString(RsaDb.CODEKEY, "");
                            RsaDb.toZip(context, "outbox" + File.separator + agentCode + "_HeadTS_" + ordDate + ".xml", "outbox" + File.separator + "HeadTS.xml.zip", null);
                            RsaDb.toZip(context, "outbox" + File.separator + agentCode + "_LinesTS_" + ordDate + ".xml", "outbox" + File.separator + "LinesTS.xml.zip", null);
                        } else {
                            RsaDb.toZip(context, "outbox" + File.separator + "Head.xml", "outbox" + File.separator + "HeadTS.xml.zip", null);
                            RsaDb.toZip(context, "outbox" + File.separator + "Lines.xml", "outbox" + File.separator + "LinesTS.xml.zip", null);
                        }

                    } else if (getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE)
                            .getString(RsaDb.INTERFACEKEY, "DBF").equals("CSV")) {
                        ///// Надо вставить архивацию всех *.XLS файлов в папке outbox
                        // затем удалить все XLS
                        RsaDb.toZipArray(context, "outbox", "outbox" + File.separator + "HeadTS.csv.zip");
                    }

                } catch (Exception e) {
                    Log.d("FtpSendActivity", "run() - ERROR.");
                    prefs.edit().putBoolean(RsaDb.ACTIVESYNCKEY, false).commit();
                    hMess = mHandler.obtainMessage();
                    data.putString("LOG", getResources().getString(R.string.send_err_makingarch));
                    hMess.setData(data);
                    mHandler.sendMessage(hMess);
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e2) {
                        return;
                    }
                    if (db != null) {
                        db.close();
                    }
                    return;
                }
                Log.d("FtpSendActivity", "run() - success!");
                // if archives was created successfully then write about it...
                hMess = mHandler.obtainMessage();
                data.putString("LOG", getResources().getString(R.string.send_archcreated));
                hMess.setData(data);
                mHandler.sendMessage(hMess);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e2) {
                    return;
                }

                Log.d("FtpSendActivity", "run() - try to send archives");
                /////////////// SEND ARCHIVES VIA FTP
                // Reply code shows us if connection successed
                int reply;
                // Estabilish connection
                String serv = prefs.getString(RsaDb.FTPSERVER, "");
                String p = prefs.getString(RsaDb.FTPPORT, "21");
                ftp.setAutodetectUTF8(true);
                ftp.setControlEncoding("UTF-8");
                ftp.setConnectTimeout(5000);
                ftp.connect(prefs.getString(RsaDb.FTPSERVER, ""), Integer.parseInt(p));
                Log.d("FtpSendActivity", "server=" + serv);
                Log.d("FtpSendActivity", "port" + p);
                Log.d("FtpSendActivity", "run() - connected");
                // Show message about connection
                hMess = mHandler.obtainMessage();
                data.putString("LOG", getResources().getString(R.string.send_ftp_connected));
                hMess.setData(data);
                mHandler.sendMessage(hMess);
                // Show message about server status
                System.out.print(ftp.getReplyString());
                // After connection attempt, we should check the reply code to verify success.
                reply = ftp.getReplyCode();

                // if server status bad then disconnect and show message
                if (!FTPReply.isPositiveCompletion(reply)) {
                    Log.d("FtpSendActivity", "run() - ftp not ready EXIT");
                    // Disconnect
                    ftp.disconnect();
                    // Show message about refused connection
                    hMess = mHandler.obtainMessage();
                    data.putString("LOG", getResources().getString(R.string.send_ftp_refused));
                    hMess.setData(data);
                    mHandler.sendMessage(hMess);
                    // Set status of sync process to false
                    prefs.edit().putBoolean(RsaDb.ACTIVESYNCKEY, false).commit();
                    // end run() process
                    if (db != null) {
                        db.close();
                    }
                    return;
                }

                Log.d("FtpSendActivity", "run() - ftp OK");
                // Try to login on ftp
                SharedPreferences def_pref = PreferenceManager.getDefaultSharedPreferences(this);
                String login = prefs.getString(RsaDb.FTPUSER, "");
                String password = prefs.getString(RsaDb.FTPPASSWORD, "");
                if (def_pref.getBoolean("encoded", false)) {
                    login = Utils.decode(login);
                    password = Utils.decode(password);
                }

                if (ftp.login(login, password, "")) {
                    Log.d("FtpSendActivity", "run() - login OK");
                    // Show message about login and password OK
                    hMess = mHandler.obtainMessage();
                    data.putString("LOG", getResources().getString(R.string.send_ftp_login));
                    hMess.setData(data);
                    mHandler.sendMessage(hMess);
                } else {
                    Log.d("FtpSendActivity", "run() - login error");
                    // Show message about login and password error
                    hMess = mHandler.obtainMessage();
                    data.putString("LOG", getResources().getString(R.string.send_ftp_badlogin));
                    hMess.setData(data);
                    mHandler.sendMessage(hMess);
                    // Set status of sync process to false
                    prefs.edit().putBoolean(RsaDb.ACTIVESYNCKEY, false).commit();
                    // end run() process
                    if (db != null) {
                        db.close();
                    }
                    return;
                }

                // Activate passive mode
                ftp.enterLocalPassiveMode();
                // Go to outbox dir - root dir if not specified
                if (ftp.changeWorkingDirectory(prefs.getString(RsaDb.FTPOUTBOX, "/"))) {
                    Log.d("FtpSendActivity", "run() - open dir OK");
                    // if dir changed successfull then show message
                    hMess = mHandler.obtainMessage();
                    data.putString("LOG", getResources().getString(R.string.send_ftp_chdir) + prefs.getString(RsaDb.FTPOUTBOX, "/") + "\n");
                    hMess.setData(data);
                    mHandler.sendMessage(hMess);
                } else {
                    Log.d("FtpSendActivity", "run() - open dir ERROR");
                    // Show message if dir not changed
                    hMess = mHandler.obtainMessage();
                    data.putString("LOG", getResources().getString(R.string.send_ftp_chdir_error) + prefs.getString(RsaDb.FTPOUTBOX, "/") + "\n");
                    hMess.setData(data);
                    mHandler.sendMessage(hMess);
                    // Set status of sync process to false
                    prefs.edit().putBoolean(RsaDb.ACTIVESYNCKEY, false).commit();
                    // end run() process
                    if (db != null) {
                        db.close();
                    }
                    return;
                }

                // Set files type
                if (ftp.setFileType(FTP.BINARY_FILE_TYPE)) {
                    hMess = mHandler.obtainMessage();
                    data.putString("LOG", getResources().getString(R.string.send_ftp_binary));
                    hMess.setData(data);
                    mHandler.sendMessage(hMess);
                } else {
                    // Show message if filetype not binary
                    hMess = mHandler.obtainMessage();
                    data.putString("LOG", getResources().getString(R.string.send_ftp_binary_error));
                    hMess.setData(data);
                    mHandler.sendMessage(hMess);
                    // Set status of sync process to false
                    prefs.edit().putBoolean(RsaDb.ACTIVESYNCKEY, false).commit();
                    // end run() process
                    if (db != null) {
                        db.close();
                    }
                    return;
                }

                Log.d("FtpSendActivity", "run() - process ftp sending");
                // Set input stream for copying files from device to ftp folder
                BufferedInputStream buffInH = null;
                BufferedInputStream buffInL = null;
                BufferedInputStream buffInT = null;
                File fileH = null;
                File fileL = null;
                File fileT = null;

                boolean isGpsTrackingSend = def_pref.getBoolean("extgps", false);

                if (isGpsTrackingSend) {
                    String folderPath = Environment.getExternalStorageDirectory().toString()
                            + File.separator + "rsa";
                    GpsUtils.exportGPSTrack(FtpSendActivity.this, false, curDateAmer);
                    RsaDb.toZip(context, "track.log", "Track.zip", folderPath);
                }

                // Set variable that assigned to file on device
                if (getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE)
                        .getString(RsaDb.INTERFACEKEY, "DBF").equals("DBF")) {
                    fileH = new File(appPath + File.separator + "outbox" + File.separator + "HeadTS.dbf.lzma");
                    fileL = new File(appPath + File.separator + "outbox" + File.separator + "LinesTS.dbf.lzma");
                } else if (getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE)
                        .getString(RsaDb.INTERFACEKEY, "DBF").equals("XML")) {
                    fileH = new File(appPath + File.separator + "outbox" + File.separator + "HeadTS.xml.zip");
                    fileL = new File(appPath + File.separator + "outbox" + File.separator + "LinesTS.xml.zip");
                    if (isGpsTrackingSend) {
                        fileT = new File(appPath + File.separator + "Track.zip");
                    }
                } else if (getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE)
                        .getString(RsaDb.INTERFACEKEY, "DBF").equals("CSV")) {
                    // Указать файл для записи на ФТП
                    fileH = new File(appPath + File.separator + "outbox" + File.separator + "HeadTS.csv.zip");
                }

                if (getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE)
                        .getString(RsaDb.INTERFACEKEY, "DBF").equals("CSV")) {
                    // Assign file with inputstream
                    buffInH = new BufferedInputStream(new FileInputStream(fileH));
                } else {
                    buffInH = new BufferedInputStream(new FileInputStream(fileH));
                    buffInL = new BufferedInputStream(new FileInputStream(fileL));
                    if (isGpsTrackingSend) {
                        buffInT = new BufferedInputStream(new FileInputStream(fileT));
                    }
                }

                // Enter passive mode on ftp
                ftp.enterLocalPassiveMode();

                /** Key that gives to know if transfered was successful */
                boolean transferedH = false;
                boolean transferedL = false;

                int photoCounter = 0;
                if (getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE)
                        .getString(RsaDb.INTERFACEKEY, "DBF").equals("DBF")) {
                    transferedH = ftp.storeFile("HeadTS_" + ordDate + ".dbf.lzma", buffInH);
                    transferedL = ftp.storeFile("LinesTS_" + ordDate + ".dbf.lzma", buffInL);
                } else if (getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE)
                        .getString(RsaDb.INTERFACEKEY, "DBF").equals("XML")) {
                    String agentCode = "";
                    if ((PreferenceManager.getDefaultSharedPreferences(this)).getBoolean("prefExtendedFilename", false)) {
                        agentCode = getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE)
                                .getString(RsaDb.CODEKEY, "") + "_";
                    }


                    transferedH = ftp.storeFile(agentCode + "HeadTS_" + ordDate + ".xml.zip", buffInH);
                    transferedL = ftp.storeFile(agentCode + "LinesTS_" + ordDate + ".xml.zip", buffInL);
                    if (isGpsTrackingSend) {
                        ftp.storeFile(agentCode + "Track_" + ordDate + ".zip", buffInT);
                    }

                    /// send photos if exist
                    FilenameFilter fFilter = new FilenameFilter() {
                        @Override
                        public boolean accept(File dir, String filename) {
                            return filename.endsWith(".jpg");
                        }
                    };
                    String imagesFolderPath = Environment.getExternalStorageDirectory().toString() + File.separator + "rsa" + File.separator + "outbox";
                    File outFolder = new File(imagesFolderPath);
                    File[] photoFiles = outFolder.listFiles(fFilter);
                    String[] strFiles = outFolder.list(fFilter);
                    File photo;
                    if (strFiles != null && strFiles.length > 0) {
                        BufferedInputStream buffImage;
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inPreferredConfig = Bitmap.Config.RGB_565;
                        Bitmap bitmap;
                        FileOutputStream out = null;
                        for (int i = 0; i < strFiles.length; i++) {
                            try {
                                bitmap = BitmapFactory.decodeFile(imagesFolderPath + File.separator + strFiles[i], options);
                                bitmap = FileUtils.getResizedBitmap(bitmap, 800, 600);
                                try {
                                    out = new FileOutputStream(imagesFolderPath + File.separator + strFiles[i]);
                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 75, out);
                                } catch (Exception e) {
                                } finally {
                                    bitmap.recycle();
                                    out.close();
                                }
                            } catch (Exception e) {
                            }
                            try {
                                photo = new File(imagesFolderPath + File.separator + strFiles[i]);
                                buffImage = new BufferedInputStream(new FileInputStream(photo));
                                ftp.storeFile(strFiles[i], buffImage);
                                photoCounter++;
                                photoFiles[i].delete();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                    }
                    // end of sending photo

                } else if (getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE)
                        .getString(RsaDb.INTERFACEKEY, "DBF").equals("CSV")) {
                    // Передать файлик на ФТП сервер
                    transferedH = ftp.storeFile("HeadTS_" + ordDate + ".csv.zip", buffInH);
                    transferedL = true;
                }

                // if transfer complite then ...
                if (transferedH && transferedL) {
                    // Show message that files transfered
                    hMess = mHandler.obtainMessage();
                    data.putString("LOG", getResources().getString(R.string.send_ftp_sended) + (photoCounter == 0 ? "" : "> (+" + Integer.toString(photoCounter) + " фото)"));
                    hMess.setData(data);
                    mHandler.sendMessage(hMess);
                } else {
                    // Show message if filetype not binary
                    hMess = mHandler.obtainMessage();
                    data.putString("LOG", getResources().getString(R.string.send_ftp_send_error));
                    hMess.setData(data);
                    mHandler.sendMessage(hMess);
                    // Set status of sync process to false
                    prefs.edit().putBoolean(RsaDb.ACTIVESYNCKEY, false).commit();
                    // end run() process
                    if (db != null) {
                        db.close();
                    }
                    return;
                }

                // close input stream, not needed
                buffInH.close();
                if (getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE)
                        .getString(RsaDb.INTERFACEKEY, "DBF").equals("CSV") == false) {
                    buffInL.close();
                }
                // End session and logout
                if (ftp.logout()) {
                    // Show message that logout successfull
                    hMess = mHandler.obtainMessage();
                    data.putString("LOG", getResources().getString(R.string.send_ftp_logout));
                    hMess.setData(data);
                    mHandler.sendMessage(hMess);
                } else {
                    // Show message if logout error
                    hMess = mHandler.obtainMessage();
                    data.putString("LOG", getResources().getString(R.string.send_ftp_logout_error));
                    hMess.setData(data);
                    mHandler.sendMessage(hMess);
                    // Set status of sync process to false
                    prefs.edit().putBoolean(RsaDb.ACTIVESYNCKEY, false).commit();
                    // end run() process
                    if (db != null) {
                        db.close();
                    }
                }
            } catch (IOException e) {
                // Show message about connection error
                hMess = mHandler.obtainMessage();
                data.putString("LOG", getResources().getString(R.string.send_ftp_connect_error));
                hMess.setData(data);
                mHandler.sendMessage(hMess);
                // Set status of sync process to false
                prefs.edit().putBoolean(RsaDb.ACTIVESYNCKEY, false).commit();
                // Error key switch to true
                error = true;
                // end run() process
                if (db != null) {
                    db.close();
                }
                return;
            } finally {
                if (ftp.isConnected()) {
                    try {
                        ftp.disconnect();
                    } catch (IOException ioe) {
                        // do nothing
                    }
                }
                if (error) {
                    hMess = mHandler.obtainMessage();
                    data.putString("LOG", getResources().getString(R.string.send_ftp_disconnect_error));
                    hMess.setData(data);
                    mHandler.sendMessage(hMess);
                    // Set status of sync process to false
                    prefs.edit().putBoolean(RsaDb.ACTIVESYNCKEY, false).commit();
                    if (db != null) {
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
        val.put(RsaDbHelper.HEAD_BLOCK, R.drawable.ic_locked_);
        // Ticket 13: if orders successfully sended then set BLOCK and SENDED status in TABLE_HEAD
        db.update(RsaDbHelper.TABLE_HEAD, val,
                RsaDbHelper.HEAD_DATE + "='"
                        + curDate
                        + "'", null);
        // Ticket 13: Not needed
        val.clear();


        // Close db not needed
        if (db != null) {
            db.close();
        }


        // Show successfully message on display
        hMess = mHandler.obtainMessage();
        data.putString("LOG", getResources().getString(R.string.send_orderson)
                + curDate
                + getResources().getString(R.string.send_sended));
        hMess.setData(data);
        mHandler.sendMessage(hMess);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e2) {
            return;
        }


        // Set status of sync process to false before end thread
        prefs.edit().putBoolean(RsaDb.ACTIVESYNCKEY, false).commit();

        /** Binding xml elements of view to variables */
        final TextView txtLog = (TextView) findViewById(R.id.txtLog_send);

        // Save log
        prefs.edit().putString(RsaDb.FTPLASTSENDKEY, txtLog.getText().toString()).commit();
    }

    /**
     * Method that starts if system trying to destroy activity
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (db != null) {
            db.close();
        }

        prefs = getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);

        prefs.edit().putBoolean(RsaDb.ACTIVESYNCKEY, false).commit();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }


}
