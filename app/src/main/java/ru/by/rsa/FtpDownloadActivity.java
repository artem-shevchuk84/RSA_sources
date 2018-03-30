package ru.by.rsa;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.mail.Message;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;


import ru.by.rsa.external.javadbf.JDBFException;
import ru.by.rsa.org.apache.commons.net.ftp.FTP;
import ru.by.rsa.org.apache.commons.net.ftp.FTPClient;
import ru.by.rsa.org.apache.commons.net.ftp.FTPFile;
import ru.by.rsa.org.apache.commons.net.ftp.FTPReply;

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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Used for receiving data for smartfon database
 *
 * @author Komarev Roman
 *         Odessa, neo3da@mail.ru, +380503412392
 */
public class FtpDownloadActivity extends Activity implements Runnable {

    /**
     * Used to send messages from Thread to main Thread
     */
    Handler mHandler;
    /**
     * Activity to send in functions
     */
    Activity mAct;
    /**
     * JavaMail variable
     */
    Mail mPop;
    /**
     * Variable that stores array of incoming messages
     */
    Message[] mMessages;
    /**
     * Stores number of total messages in mailbox
     */
    int totalMessages;
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
    RsaDbHelper mDb;
    /**
     * SQLite database that stores all data
     */
    static SQLiteDatabase db;
    /**
     * Indicates that downloading files comlited
     */
    boolean Downloaded;
    /**
     * Get Shared Preferences and fill special field of message with it
     */
    SharedPreferences prefs;
    /**
     * Used to receive binary result from function that cheking attachment
     */
    int binFlag = 0;
    /**
     * Used for information that main messages was successfuly saved on device
     */
    boolean key;
    /**
     * Thread for background downloading
     */
    static Thread background;
    /**
     * Conf changed
     */
    boolean confChanged;
    /**
     * xml elements of view to variables
     */
    static Button btnDLoad;
    static TextView txtTableName;
    static TextView txtCount;
    static TextView txtLog;
    static TextView txtPos;
    static CheckBox mChkFtp;
    static ProgressBar mProgress;
    static int iProgress;
    static int maxProgress;
    static String curTable;
    boolean useGPS;
    static boolean mDoSettingsUpdate;

    /**
     * Current theme
     */
    private boolean lightTheme;


    public void onCreate(Bundle savedInstanceState) {

        lightTheme = getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE).getBoolean(
                RsaDb.LIGHTTHEMEKEY, false);
        useGPS = getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE).getBoolean(
                RsaDb.GPSKEY, false);

        if (lightTheme) {
            setTheme(R.style.Theme_Custom);
        } else {
            setTheme(R.style.Theme_CustomBlack2);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.download);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        confChanged = false;

        mAct = this;

        /** Binding xml elements of view to variables */
        btnDLoad = (Button) findViewById(R.id.btnDownload_download);
        txtTableName = (TextView) findViewById(R.id.txtTableName_download);
        txtCount = (TextView) findViewById(R.id.txtRecordsCount_download);
        txtLog = (TextView) findViewById(R.id.txtLog_download);
        txtPos = (TextView) findViewById(R.id.txtPosition_download);
        mProgress = (ProgressBar) findViewById(R.id.Progress_download);
        mChkFtp = (CheckBox) findViewById(R.id.chkFtp);

        SharedPreferences def_prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mDoSettingsUpdate = def_prefs.getBoolean("useConfirmDefault", true);
        mChkFtp.setChecked(mDoSettingsUpdate);
        mChkFtp.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mDoSettingsUpdate = isChecked;
            }
        });

        if (lightTheme)
            txtLog.setTextColor(0xFF0000BB);

        // if activity was destroyed (for exmp. by display rotation)
        // then get data of LOG from previous state
        if (savedInstanceState != null) {
            txtLog.setText(savedInstanceState.getString("LOG"));
            iProgress = savedInstanceState.getInt("IPROGRESS");
            maxProgress = savedInstanceState.getInt("MAXPROGRESS");
            curTable = savedInstanceState.getString("CURTABLE");
        } else {
            // Get Shared Preferences and fill special field of message with it
            prefs = getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);
            txtLog.setText(prefs.getString(RsaDb.FTPLASTDWNLDKEY, ""));
            iProgress = 0;
            curTable = "Таблица";
            maxProgress = 0;
        }

        // Init new thread with function run() in that class
        background = new Thread(this);
        mHandler = new Handler() {
            @Override
            public void handleMessage(android.os.Message msg) {
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
                        SharedPreferences def_prefs = PreferenceManager.getDefaultSharedPreferences(
                                getApplicationContext());
                        if (def_prefs.getBoolean("shrinkAfter", false)) {
                            showDialog(0);
                        }
                    }
                }
            }
        };

        // On button click start to download files from mailbox
        btnDLoad.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                prefs = getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);
                if (prefs.getBoolean(RsaDb.ACTIVESYNCKEY, true)) {
                    Toast.makeText(getApplicationContext(), R.string.download_background,
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                // Clear "console" TextView
                txtLog.setText(R.string.download_connecting);

                // Start downloading in thread
                background.start();

                btnDLoad.setClickable(false);
            }
        });
    }

    /**
     * Thread body
     */
    @Override
    public void run() {
        /** Handler message */
        android.os.Message hMess;
        Bundle data = new Bundle();
        /** Device has registration */
        boolean parseError = true;

        // Get Shared Preferences and fill special field of message with it
        prefs = getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);

        // Set key that stores status of syncronization (in progress or no) to true
        prefs.edit().putBoolean(RsaDb.ACTIVESYNCKEY, true).commit();

        /** Context for some methods */
        context = getApplicationContext();

        // Path to application file storage
        appPath = context.getFilesDir().getAbsolutePath();

        if (!prefs.getString(RsaDb.ACTUALDBKEY, "").equals(RsaDbHelper.DB_NAME1)) {
            mDb = new RsaDbHelper(context, RsaDbHelper.DB_NAME1);
        } else {
            mDb = new RsaDbHelper(context, RsaDbHelper.DB_NAME2);
        }

        // Open database with orders 
        db = mDb.getWritableDatabase();

        // Remarked 05 Jan 2013 by Romka
        // Get version from site
        // try
        //{
        //	JSONArray jsonArray = new JSONArray(readVersionFromSite());
        //	String siteVersion = jsonArray.getString(0);
        //	Float.parseFloat(siteVersion);
        //	SharedPreferences _prefs = getSharedPreferences(RsaDb.PREFS_NAME, Context
        // .MODE_PRIVATE);
        //	_prefs.edit().putString(RsaDb.MARKETVERSION, siteVersion).commit();
        //	Log.v("ROMKA","Version from site checked!! :)");
        //}
        //catch (Exception e)
        //{
        //	Log.e("ROMKA", "JSON Exception");
        //}

        // Added 05 Jan 2013 by Romka
        // And moved to FtpsendActivity 01.02.2013
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

        RsaDbHelper orders_mDb = new RsaDbHelper(context, RsaDbHelper.DB_ORDERS);

        CheckVersionThread getverThread = new CheckVersionThread(imei, imei2, ___prefs,
                orders_mDb.getWritableDatabase(),
                mDb, useGPS,
                ___prefs.getBoolean(RsaDb.SENDLINES, false),
                def_pref.getBoolean("chkRestSend", false));
        getverThread.start();

        ////////////////// Begin to FTP- downloading

        /** FTP connection instance */
        FTPClient ftp = new FTPClient();
        /** Key that gives to know about errors while process */
        boolean error = false;

        try {
            /////MAIN FTP PROCCESS
            /////////////// SEND ARCHIVES VIA FTP
            // Reply code shows us if connection successed
            int reply;
            // Estabilish connection
            ftp.setConnectTimeout(5000);
            ftp.connect(prefs.getString(RsaDb.FTPSERVER, ""),
                    Integer.parseInt(prefs.getString(RsaDb.FTPPORT, "21")));
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
                // Disconnect
                try {
                    hMess = mHandler.obtainMessage();
                    data.putString("LOG", "> Отключаемся от FTP...\n");
                    hMess.setData(data);
                    mHandler.sendMessage(hMess);
                    ftp.logout();
                } catch (Exception dr) {
                }
                try {
                    ftp.disconnect();
                } catch (Exception dz) {
                }

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

            // Try to login on ftp
            String login = prefs.getString(RsaDb.FTPUSER, "");
            String password = prefs.getString(RsaDb.FTPPASSWORD, "");
            if (def_pref.getBoolean("encoded", false)) {
                login = Utils.decode(login);
                password = Utils.decode(password);
            }

            if (ftp.login(login, password)) {
                // Show message about login and password OK
                hMess = mHandler.obtainMessage();
                data.putString("LOG", getResources().getString(R.string.send_ftp_login));
                hMess.setData(data);
                mHandler.sendMessage(hMess);
            } else {
                // Show message about login and password error
                hMess = mHandler.obtainMessage();
                data.putString("LOG", getResources().getString(R.string.send_ftp_badlogin));
                hMess.setData(data);
                mHandler.sendMessage(hMess);
                // Set status of sync process to false
                prefs.edit().putBoolean(RsaDb.ACTIVESYNCKEY, false).commit();
                try {
                    hMess = mHandler.obtainMessage();
                    data.putString("LOG", "> Отключаемся от FTP...\n");
                    hMess.setData(data);
                    mHandler.sendMessage(hMess);
                    ftp.logout();
                } catch (Exception dr) {
                }
                try {
                    ftp.disconnect();
                } catch (Exception dz) {
                }
                // end run() process
                if (db != null) {
                    db.close();
                }
                return;
            }

            // Activate passive mode
            ftp.enterLocalPassiveMode();
            // Go to outbox dir - root dir if not specified
            if (ftp.changeWorkingDirectory(prefs.getString(RsaDb.FTPINBOX, "/"))) {
                // if dir changed successfull then show message
                hMess = mHandler.obtainMessage();
                data.putString("LOG",
                        getResources().getString(R.string.download_ftp_chdir) + prefs.getString(
                                RsaDb.FTPINBOX, "/") + "\n");
                hMess.setData(data);
                mHandler.sendMessage(hMess);
            } else {
                // Show message if dir not changed
                hMess = mHandler.obtainMessage();
                data.putString("LOG",
                        getResources().getString(R.string.send_ftp_chdir_error) + prefs.getString(
                                RsaDb.FTPINBOX, "/") + "\n");
                hMess.setData(data);
                mHandler.sendMessage(hMess);
                // Set status of sync process to false
                prefs.edit().putBoolean(RsaDb.ACTIVESYNCKEY, false).commit();
                try {
                    hMess = mHandler.obtainMessage();
                    data.putString("LOG", "> Отключаемся от FTP...\n");
                    hMess.setData(data);
                    mHandler.sendMessage(hMess);
                    ftp.logout();
                } catch (Exception dr) {
                }
                try {
                    ftp.disconnect();
                } catch (Exception dz) {
                }
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
                try {
                    hMess = mHandler.obtainMessage();
                    data.putString("LOG", "> Отключаемся от FTP...\n");
                    hMess.setData(data);
                    mHandler.sendMessage(hMess);
                    ftp.logout();
                } catch (Exception dr) {
                }
                try {
                    ftp.disconnect();
                } catch (Exception dz) {
                }
                // end run() process
                if (db != null) {
                    db.close();
                }
                return;
            }

            // // // // // /// =)
            // // // // /// =)
            // // // // /// =)
            /** Get List of files, storring in current directory */
            FTPFile[] mFiles = ftp.listFiles();
            /** If count of files stored in current directory */
            int filesCount = mFiles.length;
            /** If needed files was not found then = true */
            boolean filesNotFound = true;

            // if folder is not empty then count of files mast be more then 2
            // i dont know why 3
            if (filesCount < 3) {
                // Show message if found some files
                hMess = mHandler.obtainMessage();
                data.putString("LOG", getResources().getString(R.string.download_ftpfilesnotfound));
                hMess.setData(data);
                mHandler.sendMessage(hMess);
                try {
                    hMess = mHandler.obtainMessage();
                    data.putString("LOG", "> Отключаемся от FTP...\n");
                    hMess.setData(data);
                    mHandler.sendMessage(hMess);
                    ftp.logout();
                } catch (Exception dr) {
                }
                try {
                    ftp.disconnect();
                } catch (Exception dz) {
                }
            } else {
                // Show message if found some files
                hMess = mHandler.obtainMessage();
                data.putString("LOG", getResources().getString(R.string.download_ftpfilesfound));
                hMess.setData(data);
                mHandler.sendMessage(hMess);

                // Check file list for needed files
                binFlag = RsaDb.parseFTPFiles(mFiles,
                        getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE));

                if ((binFlag & 0x1F) == 0x1F) // (x & 00011111) == 00011111?
                {
                    hMess = mHandler.obtainMessage();
                    data.putString("LOG", getResources().getString(R.string.download_correctfound));
                    hMess.setData(data);
                    mHandler.sendMessage(hMess);
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e2) {
                        return;
                    }

                    // set key to know that needed files found
                    filesNotFound = false;

                    if ((binFlag & 0x20) == 0x20) {
                        hMess = mHandler.obtainMessage();
                        data.putString("LOG", getResources().getString(R.string.download_found_WH));
                        hMess.setData(data);
                        mHandler.sendMessage(hMess);
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e2) {
                            return;
                        }
                    }
                    if ((binFlag & 0x40) == 0x40) {
                        hMess = mHandler.obtainMessage();
                        data.putString("LOG", getResources().getString(R.string.download_found_GR));
                        hMess.setData(data);
                        mHandler.sendMessage(hMess);
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e2) {
                            return;
                        }
                    }
                    if ((binFlag & 0x80) == 0x80) {
                        hMess = mHandler.obtainMessage();
                        data.putString("LOG", getResources().getString(R.string.download_found_BR));
                        hMess.setData(data);
                        mHandler.sendMessage(hMess);
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e2) {
                            return;
                        }
                    }
                    if ((binFlag & 0x100) == 0x100) {
                        hMess = mHandler.obtainMessage();
                        data.putString("LOG",
                                getResources().getString(R.string.download_found_Work));
                        hMess.setData(data);
                        mHandler.sendMessage(hMess);
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e2) {
                            return;
                        }
                    }
                    if ((binFlag & 0x200) == 0x200) {
                        hMess = mHandler.obtainMessage();
                        data.putString("LOG",
                                getResources().getString(R.string.download_found_Plan));
                        hMess.setData(data);
                        mHandler.sendMessage(hMess);
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e2) {
                            return;
                        }
                    }
                    if ((binFlag & 0x400) == 0x400) {
                        hMess = mHandler.obtainMessage();
                        data.putString("LOG",
                                getResources().getString(R.string.download_found_Sold));
                        hMess.setData(data);
                        mHandler.sendMessage(hMess);
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e2) {
                            return;
                        }
                    }
                    if ((binFlag & 0x800) == 0x800) {
                        hMess = mHandler.obtainMessage();
                        data.putString("LOG", "M");
                        hMess.setData(data);
                        mHandler.sendMessage(hMess);
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e2) {
                            return;
                        }
                    }
                    if ((binFlag & 0x2000) == 0x2000) {
                        hMess = mHandler.obtainMessage();
                        data.putString("LOG", "H");
                        hMess.setData(data);
                        mHandler.sendMessage(hMess);
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e2) {
                            return;
                        }
                    }
                    if ((binFlag & 0x4000) == 0x4000) {
                        hMess = mHandler.obtainMessage();
                        data.putString("LOG", "D");
                        hMess.setData(data);
                        mHandler.sendMessage(hMess);
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e2) {
                            return;
                        }
                    }
                } else {
                    hMess = mHandler.obtainMessage();
                    data.putString("LOG",
                            getResources().getString(R.string.download_ftpfilesnotfound));
                    hMess.setData(data);
                    mHandler.sendMessage(hMess);
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e2) {
                        return;
                    }
                    filesNotFound = true;
                }
            }

            // if found required files then download them one by one
            if (filesNotFound == false) {
                // Not 0 if error occured while download occured
                int dnError = 0;
                int arcError = 0;
                ftp.enterLocalPassiveMode();

                if (getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE)
                        .getString(RsaDb.INTERFACEKEY, "DBF").equals("DBF")) {
                    // Download files from ftp to inbox on device
                    dnError += ftpDownload(ftp,
                            appPath + File.separator + "inbox" + File.separator + "goods.dbf.lzma",
                            "goods.dbf.lzma");
                    dnError += ftpDownload(ftp,
                            appPath + File.separator + "inbox" + File.separator + "cust.dbf.lzma",
                            "cust.dbf.lzma");
                    dnError += ftpDownload(ftp,
                            appPath + File.separator + "inbox" + File.separator + "char.dbf.lzma",
                            "char.dbf.lzma");
                    dnError += ftpDownload(ftp,
                            appPath + File.separator + "inbox" + File.separator + "debit.dbf.lzma",
                            "debit.dbf.lzma");
                    dnError += ftpDownload(ftp,
                            appPath + File.separator + "inbox" + File.separator + "shop.dbf.lzma",
                            "shop.dbf.lzma");
                    if ((binFlag & 0x20) == 0x20)
                        dnError += ftpDownload(ftp,
                                appPath + File.separator + "inbox" + File.separator
                                        + "sklad.dbf.lzma", "sklad.dbf.lzma");
                    if ((binFlag & 0x40) == 0x40)
                        dnError += ftpDownload(ftp,
                                appPath + File.separator + "inbox" + File.separator
                                        + "group.dbf.lzma", "group.dbf.lzma");
                    if ((binFlag & 0x80) == 0x80)
                        dnError += ftpDownload(ftp,
                                appPath + File.separator + "inbox" + File.separator
                                        + "brand.dbf.lzma", "brand.dbf.lzma");
                    if ((binFlag & 0x100) == 0x100)
                        dnError += ftpDownload(ftp,
                                appPath + File.separator + "inbox" + File.separator
                                        + "workinf.dbf.lzma", "workinf.dbf.lzma");
                    if ((binFlag & 0x200) == 0x200)
                        dnError += ftpDownload(ftp,
                                appPath + File.separator + "inbox" + File.separator
                                        + "plan.dbf.lzma", "plan.dbf.lzma");
                    if ((binFlag & 0x1000) == 0x1000)
                        dnError += ftpDownload(ftp,
                                appPath + File.separator + "inbox" + File.separator
                                        + "prodlock.dbf.lzma", "prodlock.dbf.lzma");
                } else if (getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE)
                        .getString(RsaDb.INTERFACEKEY, "DBF").equals("XML")) {
                    // Download files from ftp to inbox on device
                    dnError += ftpDownload(ftp,
                            appPath + File.separator + "inbox" + File.separator + "goods.xml.zip",
                            "goods.xml.zip");
                    dnError += ftpDownload(ftp,
                            appPath + File.separator + "inbox" + File.separator + "cust.xml.zip",
                            "cust.xml.zip");
                    dnError += ftpDownload(ftp,
                            appPath + File.separator + "inbox" + File.separator + "char.xml.zip",
                            "char.xml.zip");
                    dnError += ftpDownload(ftp,
                            appPath + File.separator + "inbox" + File.separator + "debit.xml.zip",
                            "debit.xml.zip");
                    dnError += ftpDownload(ftp,
                            appPath + File.separator + "inbox" + File.separator + "shop.xml.zip",
                            "shop.xml.zip");
                    if ((binFlag & 0x20) == 0x20)
                        dnError += ftpDownload(ftp,
                                appPath + File.separator + "inbox" + File.separator
                                        + "sklad.xml.zip", "sklad.xml.zip");
                    if ((binFlag & 0x40) == 0x40)
                        dnError += ftpDownload(ftp,
                                appPath + File.separator + "inbox" + File.separator
                                        + "group.xml.zip", "group.xml.zip");
                    if ((binFlag & 0x80) == 0x80)
                        dnError += ftpDownload(ftp,
                                appPath + File.separator + "inbox" + File.separator
                                        + "brand.xml.zip", "brand.xml.zip");
                    if ((binFlag & 0x100) == 0x100)
                        dnError += ftpDownload(ftp,
                                appPath + File.separator + "inbox" + File.separator
                                        + "workinf.xml.zip", "workinf.xml.zip");
                    if ((binFlag & 0x200) == 0x200)
                        dnError += ftpDownload(ftp,
                                appPath + File.separator + "inbox" + File.separator
                                        + "plan.xml.zip", "plan.xml.zip");
                    if ((binFlag & 0x400) == 0x400)
                        dnError += ftpDownload(ftp,
                                appPath + File.separator + "inbox" + File.separator
                                        + "sold.xml.zip", "sold.xml.zip");
                    if ((binFlag & 0x800) == 0x800)
                        dnError += ftpDownload(ftp,
                                appPath + File.separator + "inbox" + File.separator
                                        + "matrix.xml.zip", "matrix.xml.zip");
                    if ((binFlag & 0x2000) == 0x2000)
                        dnError += ftpDownload(ftp,
                                appPath + File.separator + "inbox" + File.separator
                                        + "hist.xml.zip", "hist.xml.zip");
                    if ((binFlag & 0x4000) == 0x4000)
                        dnError += ftpDownload(ftp,
                                appPath + File.separator + "inbox" + File.separator
                                        + "skladdet.xml.zip", "skladdet.xml.zip");
                    if ((binFlag & 0x8000) == 0x8000)
                        dnError += ftpDownload(ftp,
                                appPath + File.separator + "inbox" + File.separator
                                        + "salout.xml.zip", "salout.xml.zip");
                    if ((binFlag & 0x10000) == 0x10000)
                        dnError += ftpDownload(ftp,
                                appPath + File.separator + "inbox" + File.separator
                                        + "statplan.xml.zip", "statplan.xml.zip");
                } else if (getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE)
                        .getString(RsaDb.INTERFACEKEY, "DBF").equals("CSV")) {
                    // Download files from ftp to inbox on device
                    dnError += ftpDownload(ftp,
                            appPath + File.separator + "inbox" + File.separator + "goods.csv.zip",
                            "goods.csv.zip");
                    dnError += ftpDownload(ftp,
                            appPath + File.separator + "inbox" + File.separator + "cust.csv.zip",
                            "cust.csv.zip");
                    dnError += ftpDownload(ftp,
                            appPath + File.separator + "inbox" + File.separator + "char.csv.zip",
                            "char.csv.zip");
                    dnError += ftpDownload(ftp,
                            appPath + File.separator + "inbox" + File.separator + "debit.csv.zip",
                            "debit.csv.zip");
                    dnError += ftpDownload(ftp,
                            appPath + File.separator + "inbox" + File.separator + "shop.csv.zip",
                            "shop.csv.zip");
                    if ((binFlag & 0x20) == 0x20)
                        dnError += ftpDownload(ftp,
                                appPath + File.separator + "inbox" + File.separator
                                        + "sklad.csv.zip", "sklad.csv.zip");
                    if ((binFlag & 0x40) == 0x40)
                        dnError += ftpDownload(ftp,
                                appPath + File.separator + "inbox" + File.separator
                                        + "group.csv.zip", "group.csv.zip");
                    if ((binFlag & 0x80) == 0x80)
                        dnError += ftpDownload(ftp,
                                appPath + File.separator + "inbox" + File.separator
                                        + "brand.csv.zip", "brand.csv.zip");
                    if ((binFlag & 0x100) == 0x100)
                        dnError += ftpDownload(ftp,
                                appPath + File.separator + "inbox" + File.separator
                                        + "workinf.csv.zip", "workinf.csv.zip");
                    if ((binFlag & 0x200) == 0x200)
                        dnError += ftpDownload(ftp,
                                appPath + File.separator + "inbox" + File.separator
                                        + "plan.csv.zip", "plan.csv.zip");
                }

                if (dnError == 0) {
                    hMess = mHandler.obtainMessage();
                    data.putString("LOG",
                            getResources().getString(R.string.download_ftpfilesdownloaded));
                    hMess.setData(data);
                    mHandler.sendMessage(hMess);
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e2) {
                        return;
                    }
                } else {
                    hMess = mHandler.obtainMessage();
                    data.putString("LOG",
                            getResources().getString(R.string.download_ftpdownload_error));
                    hMess.setData(data);
                    mHandler.sendMessage(hMess);
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e2) {
                        return;
                    }
                }

                try {
                    hMess = mHandler.obtainMessage();
                    data.putString("LOG", "> Отключаемся от FTP...\n");
                    hMess.setData(data);
                    mHandler.sendMessage(hMess);
                    ftp.logout();
                    ftp.disconnect();
                } catch (Exception dr) {
                }

                ////// Extract archives
                ///////////////////////////////////////////////////////////////////////////////////
                // Get files from archive if was not errors thru download process
                if (dnError == 0) {
                    try {
                        if (getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE)
                                .getString(RsaDb.INTERFACEKEY, "DBF").equals("DBF")) {
                            RsaDb.fromLzma(context, "inbox/goods.dbf.lzma", "inbox/Goods.DBF");
                            RsaDb.fromLzma(context, "inbox/cust.dbf.lzma", "inbox/Cust.DBF");
                            RsaDb.fromLzma(context, "inbox/char.dbf.lzma", "inbox/Char.DBF");
                            RsaDb.fromLzma(context, "inbox/debit.dbf.lzma", "inbox/Debit.DBF");
                            RsaDb.fromLzma(context, "inbox/shop.dbf.lzma", "inbox/Shop.DBF");
                            if ((binFlag & 0x20) == 0x20)
                                RsaDb.fromLzma(context, "inbox/sklad.dbf.lzma", "inbox/Sklad.DBF");
                            if ((binFlag & 0x40) == 0x40)
                                RsaDb.fromLzma(context, "inbox/group.dbf.lzma", "inbox/Group.DBF");
                            if ((binFlag & 0x80) == 0x80)
                                RsaDb.fromLzma(context, "inbox/brand.dbf.lzma", "inbox/Brand.DBF");
                            if ((binFlag & 0x100) == 0x100)
                                RsaDb.fromLzma(context, "inbox/workinf.dbf.lzma",
                                        "inbox/Workinf.DBF");
                            if ((binFlag & 0x200) == 0x200)
                                RsaDb.fromLzma(context, "inbox/plan.dbf.lzma", "inbox/Plan.DBF");
                            if ((binFlag & 0x1000) == 0x1000)
                                RsaDb.fromLzma(context, "inbox/prodlock.dbf.lzma",
                                        "inbox/Prodlock.DBF");
                        } else if (getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE)
                                .getString(RsaDb.INTERFACEKEY, "DBF").equals("XML")) {
                            RsaDb.fromZip(context, "inbox/goods.xml.zip", "inbox");
                            RsaDb.fromZip(context, "inbox/cust.xml.zip", "inbox");
                            RsaDb.fromZip(context, "inbox/char.xml.zip", "inbox");
                            RsaDb.fromZip(context, "inbox/debit.xml.zip", "inbox");
                            RsaDb.fromZip(context, "inbox/shop.xml.zip", "inbox");
                            if ((binFlag & 0x20) == 0x20)
                                RsaDb.fromZip(context, "inbox/sklad.xml.zip", "inbox");
                            if ((binFlag & 0x40) == 0x40)
                                RsaDb.fromZip(context, "inbox/group.xml.zip", "inbox");
                            if ((binFlag & 0x80) == 0x80)
                                RsaDb.fromZip(context, "inbox/brand.xml.zip", "inbox");
                            if ((binFlag & 0x100) == 0x100)
                                RsaDb.fromZip(context, "inbox/workinf.xml.zip", "inbox");
                            if ((binFlag & 0x200) == 0x200)
                                RsaDb.fromZip(context, "inbox/plan.xml.zip", "inbox");
                            if ((binFlag & 0x400) == 0x400)
                                RsaDb.fromZip(context, "inbox/sold.xml.zip", "inbox");
                            if ((binFlag & 0x800) == 0x800)
                                RsaDb.fromZip(context, "inbox/matrix.xml.zip", "inbox");
                            if ((binFlag & 0x2000) == 0x2000)
                                RsaDb.fromZip(context, "inbox/hist.xml.zip", "inbox");
                            if ((binFlag & 0x4000) == 0x4000)
                                RsaDb.fromZip(context, "inbox/skladdet.xml.zip", "inbox");
                            if ((binFlag & 0x8000) == 0x8000)
                                RsaDb.fromZip(context, "inbox/salout.xml.zip", "inbox");
                            if ((binFlag & 0x10000) == 0x10000)
                                RsaDb.fromZip(context, "inbox/statplan.xml.zip", "inbox");
                        } else if (getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE)
                                .getString(RsaDb.INTERFACEKEY, "DBF").equals("CSV")) {
                            RsaDb.fromZip(context, "inbox/goods.csv.zip", "inbox");
                            RsaDb.fromZip(context, "inbox/cust.csv.zip", "inbox");
                            RsaDb.fromZip(context, "inbox/char.csv.zip", "inbox");
                            RsaDb.fromZip(context, "inbox/debit.csv.zip", "inbox");
                            RsaDb.fromZip(context, "inbox/shop.csv.zip", "inbox");
                            if ((binFlag & 0x20) == 0x20)
                                RsaDb.fromZip(context, "inbox/sklad.csv.zip", "inbox");
                            if ((binFlag & 0x40) == 0x40)
                                RsaDb.fromZip(context, "inbox/group.csv.zip", "inbox");
                            if ((binFlag & 0x80) == 0x80)
                                RsaDb.fromZip(context, "inbox/brand.csv.zip", "inbox");
                            if ((binFlag & 0x100) == 0x100)
                                RsaDb.fromZip(context, "inbox/workinf.csv.zip", "inbox");
                            if ((binFlag & 0x200) == 0x200)
                                RsaDb.fromZip(context, "inbox/plan.csv.zip", "inbox");
                        }

                        hMess = mHandler.obtainMessage();
                        data.putString("LOG",
                                getResources().getString(R.string.download_all_unpack));
                        hMess.setData(data);
                        mHandler.sendMessage(hMess);
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e2) {
                            return;
                        }
                        // No errors
                        arcError = 0;
                    } catch (Exception e) {
                        hMess = mHandler.obtainMessage();
                        data.putString("LOG",
                                getResources().getString(R.string.download_err_unpack));
                        hMess.setData(data);
                        mHandler.sendMessage(hMess);
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e2) {
                            return;
                        }
                        // Error of unpack
                        arcError = 1;
                    }
                }

                // if download complite and unpack complite then parse files data to Database
                if ((dnError == 0) && (arcError == 0)) {
                    // Write about begining of downloading!
                    hMess = mHandler.obtainMessage();
                    data.putString("LOG", getResources().getString(R.string.download_loading));
                    hMess.setData(data);
                    mHandler.sendMessage(hMess);
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e2) {
                        return;
                    }

                    db = mDb.getWritableDatabase();
                    db.execSQL("PRAGMA foreign_keys=OFF;");
                    String pth = this.getFilesDir().getAbsolutePath();
                    try {
                        if (getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE)
                                .getString(RsaDb.INTERFACEKEY, "DBF").equals("DBF")) {
                            RsaDb.dbfToDb(mAct, getApplicationContext(), db,
                                    "inbox" + File.separator + "Cust.DBF", RsaDb.DBF_CUST, mHandler,
                                    pth);

                            if ((binFlag & 0x80) == 0x80)
                                RsaDb.dbfToDb(mAct, getApplicationContext(), db,
                                        "inbox" + File.separator + "Brand.DBF", RsaDb.DBF_BRAND,
                                        mHandler, pth);
                            if ((binFlag & 0x40) == 0x40)
                                RsaDb.dbfToDb(mAct, getApplicationContext(), db,
                                        "inbox" + File.separator + "Group.DBF", RsaDb.DBF_GROUP,
                                        mHandler, pth);
                            if ((binFlag & 0x20) == 0x20)
                                RsaDb.dbfToDb(mAct, getApplicationContext(), db,
                                        "inbox" + File.separator + "Sklad.DBF", RsaDb.DBF_SKLAD,
                                        mHandler, pth);
                            RsaDb.dbfToDb(mAct, getApplicationContext(), db,
                                    "inbox" + File.separator + "Debit.DBF", RsaDb.DBF_DEBIT,
                                    mHandler, pth);
                            RsaDb.dbfToDb(mAct, getApplicationContext(), db,
                                    "inbox" + File.separator + "Shop.DBF", RsaDb.DBF_SHOP, mHandler,
                                    pth);
                            RsaDb.dbfToDb(mAct, getApplicationContext(), db,
                                    "inbox" + File.separator + "Char.DBF", RsaDb.DBF_CHAR, mHandler,
                                    pth);
                            RsaDb.dbfToDb(mAct, getApplicationContext(), db,
                                    "inbox" + File.separator + "Goods.DBF", RsaDb.DBF_GOODS,
                                    mHandler, pth);
                            if ((binFlag & 0x100) == 0x100)
                                RsaDb.dbfToDb(mAct, getApplicationContext(), db,
                                        "inbox" + File.separator + "Workinf.DBF", RsaDb.DBF_WORKINF,
                                        mHandler, pth);
                            if ((binFlag & 0x200) == 0x200)
                                RsaDb.dbfToDb(mAct, getApplicationContext(), db,
                                        "inbox" + File.separator + "Plan.DBF", RsaDb.DBF_PLAN,
                                        mHandler, pth);
                            if ((binFlag & 0x1000) == 0x1000)
                                RsaDb.dbfToDb(mAct, getApplicationContext(), db,
                                        "inbox" + File.separator + "Prodlock.DBF",
                                        RsaDb.DBF_PRODLOCK, mHandler, pth);
                        } else if (getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE)
                                .getString(RsaDb.INTERFACEKEY, "DBF").equals("XML")) {
                            RsaDb.XMLtoCust(this, db, mHandler,
                                    this.getFilesDir().getAbsolutePath());
                            RsaDb.XMLtoBrand(this, db, mHandler,
                                    this.getFilesDir().getAbsolutePath());
                            RsaDb.XMLtoGroup(this, db, mHandler,
                                    this.getFilesDir().getAbsolutePath());
                            RsaDb.XMLtoSklad(this, db, mHandler,
                                    this.getFilesDir().getAbsolutePath());
                            RsaDb.XMLtoDebit(this, db, mHandler,
                                    this.getFilesDir().getAbsolutePath());
                            RsaDb.XMLtoShop(this, db, mHandler,
                                    this.getFilesDir().getAbsolutePath());
                            RsaDb.XMLtoChar(this, db, mHandler,
                                    this.getFilesDir().getAbsolutePath());
                            RsaDb.XMLtoGoods(this, db, mHandler,
                                    this.getFilesDir().getAbsolutePath());
                            if ((binFlag & 0x100) == 0x100)
                                RsaDb.XMLtoFTP(this, db, mHandler,
                                        this.getFilesDir().getAbsolutePath(), mDoSettingsUpdate);
                            if ((binFlag & 0x200) == 0x200)
                                RsaDb.XMLtoPlan(this, db, mHandler,
                                        this.getFilesDir().getAbsolutePath());
                            if ((binFlag & 0x400) == 0x400)
                                RsaDb.XMLtoSold(this, db, mHandler,
                                        this.getFilesDir().getAbsolutePath());
                            if ((binFlag & 0x800) == 0x800)
                                RsaDb.XMLtoMatrix(this, db, mHandler,
                                        this.getFilesDir().getAbsolutePath());
                            if ((binFlag & 0x2000) == 0x2000)
                                RsaDb.XMLtoHist(this, db, mHandler,
                                        this.getFilesDir().getAbsolutePath());
                            if ((binFlag & 0x4000) == 0x4000)
                                RsaDb.XMLtoSkladDet(this, db, mHandler,
                                        this.getFilesDir().getAbsolutePath());
                            if ((binFlag & 0x8000) == 0x8000)
                                RsaDb.XMLtoSales(this, db, mHandler,
                                        this.getFilesDir().getAbsolutePath());
                            if ((binFlag & 0x10000) == 0x10000)
                                RsaDb.XMLtoStaticPlan(this, db, mHandler,
                                        this.getFilesDir().getAbsolutePath());

                        } else if (getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE)
                                .getString(RsaDb.INTERFACEKEY, "DBF").equals("CSV")) {
                            RsaDb.CSVtoCust(this, db, mHandler,
                                    this.getFilesDir().getAbsolutePath());
                            RsaDb.CSVtoBrand(this, db, mHandler,
                                    this.getFilesDir().getAbsolutePath());
                            RsaDb.CSVtoGroup(this, db, mHandler,
                                    this.getFilesDir().getAbsolutePath());
                            RsaDb.CSVtoSklad(this, db, mHandler,
                                    this.getFilesDir().getAbsolutePath());
                            RsaDb.CSVtoDebit(this, db, mHandler,
                                    this.getFilesDir().getAbsolutePath());
                            RsaDb.CSVtoShop(this, db, mHandler,
                                    this.getFilesDir().getAbsolutePath());
                            RsaDb.CSVtoChar(this, db, mHandler,
                                    this.getFilesDir().getAbsolutePath());
                            RsaDb.CSVtoGoods(this, db, mHandler,
                                    this.getFilesDir().getAbsolutePath());
                            if ((binFlag & 0x100) == 0x100)
                                RsaDb.CSVtoFTP(this, db, mHandler,
                                        this.getFilesDir().getAbsolutePath());
                            if ((binFlag & 0x200) == 0x200)
                                RsaDb.CSVtoPlan(this, db, mHandler,
                                        this.getFilesDir().getAbsolutePath());
                        }

                        try {
                            RsaDb.copyTable(getDatabasePath(prefs.getString(RsaDb.ACTUALDBKEY,
                                    RsaDbHelper.DB_NAME1)).toString(), db, binFlag);
                        } catch (Exception e) {
                            Log.d("copyTable", "EXCEPTION in method");
                        }

                        parseError = false;
                    } catch (JDBFException e) {
                        prefs.edit().putBoolean(RsaDb.ACTIVESYNCKEY, false).commit();
                        hMess = mHandler.obtainMessage();
                        data.putString("LOG", getResources().getString(R.string.download_err_dbf));
                        hMess.setData(data);
                        mHandler.sendMessage(hMess);
                        parseError = true;
                    } catch (Exception e2) {
                        prefs.edit().putBoolean(RsaDb.ACTIVESYNCKEY, false).commit();
                        hMess = mHandler.obtainMessage();
                        data.putString("LOG", getResources().getString(R.string.download_err_xml));
                        hMess.setData(data);
                        mHandler.sendMessage(hMess);
                        parseError = true;
                    }
                    db.execSQL("PRAGMA foreign_keys=ON;");

                }
            }

            //////////////////////////
            ///////////////////////
            ///////////////////////
            // End session and logout
            try {
                if (ftp.logout()) {
                    // Show message that logout successfull
                    hMess = mHandler.obtainMessage();
                    data.putString("LOG", getResources().getString(R.string.send_ftp_logout));
                    hMess.setData(data);
                    mHandler.sendMessage(hMess);
                } else {
                    // Show message if logout error
                /*	hMess = mHandler.obtainMessage();
                    data.putString("LOG", getResources().getString(R.string.send_ftp_logout_error));
                    hMess.setData(data); 
                    mHandler.sendMessage(hMess);*/
                    // Set status of sync process to false
                    prefs.edit().putBoolean(RsaDb.ACTIVESYNCKEY, false).commit();
                    // end run() process
                    if (db != null) {
                        db.close();
                    }
                }
            } catch (Exception ef) {
            }
        } catch (IOException e) {
            // Show message about connection error
            hMess = mHandler.obtainMessage();
            data.putString("LOG", "\n" + getResources().getString(R.string.send_ftp_connect_error));
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

        ////////////////// end of downloading process

        // Close db - not needed
        if (db != null) {
            db.close();
        }

        if (parseError == false) {
            // Show successfully message on display
            hMess = mHandler.obtainMessage();
            data.putString("LOG", getResources().getString(R.string.download_loading_succ));
            hMess.setData(data);
            mHandler.sendMessage(hMess);
            // Set new DB to actual state
            if (!prefs.getString(RsaDb.ACTUALDBKEY, "").equals(RsaDbHelper.DB_NAME1)) {
                prefs.edit().putString(RsaDb.ACTUALDBKEY, RsaDbHelper.DB_NAME1).commit();
            } else {
                prefs.edit().putString(RsaDb.ACTUALDBKEY, RsaDbHelper.DB_NAME2).commit();
            }
        } else {
            // Show error message on display
            hMess = mHandler.obtainMessage();
            data.putString("LOG", getResources().getString(R.string.download_ftpBD));
            hMess.setData(data);
            mHandler.sendMessage(hMess);
        }

        prefs.edit().putString(RsaDb.FTPLASTDWNLDKEY, txtLog.getText().toString()).commit();
        prefs.edit().putBoolean(RsaDb.ACTIVESYNCKEY, false).commit();
    }

    private int ftpDownload(FTPClient mFtp, String DevicePath, String FTPPath) {
        try {
            // Set Output stream for copying files from ftp to device
            BufferedOutputStream buffOut = null;
            // Set variable that assigned to file on device
            File file = new File(DevicePath);
            // Assign file with outputstream
            buffOut = new BufferedOutputStream(new FileOutputStream(file));
            // Get file and set result of operation to variable
            int res = mFtp.retrieveFile(FTPPath, buffOut) ? 0 : 1;
            // Close buffer
            buffOut.close();

            return res;
        } catch (Exception e) {
            return 1;
        }
    }

    /**
     * Saving activity positions on destroy .. or if display rotation
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
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
    public void onBackPressed() {
        prefs = getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);
        if (prefs.getBoolean(RsaDb.ACTIVESYNCKEY, true)) {
            Toast.makeText(getApplicationContext(), R.string.download_wait, Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        prefs.edit().putString(RsaDb.FTPLASTDWNLDKEY, txtLog.getText().toString()).commit();
        if (db != null) {
            db.close();
        }

        finish();
    }

    /**
     * Method that starts if system trying to destroy activity
     */
    @Override
    protected void onDestroy() {
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
    public String readVersionFromSite() {
        StringBuilder builder = new StringBuilder();
        HttpClient client = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet("http://rsa.16mb.com/chkver.php");
        try {
            HttpResponse response = client.execute(httpGet);
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();

            if (statusCode == 200) {
                HttpEntity entity = response.getEntity();
                InputStream content = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                String line;

                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
            }
        } catch (Exception e) {
        }

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
                SharedPreferences def_prefs = PreferenceManager.getDefaultSharedPreferences(
                        getApplicationContext());
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
