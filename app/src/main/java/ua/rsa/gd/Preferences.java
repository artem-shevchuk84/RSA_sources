package ua.rsa.gd;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Calendar;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import ru.by.rsa.R;
import ua.rsa.gd.utils.GpsUtils;

public class Preferences extends PreferenceActivity
        implements OnSharedPreferenceChangeListener, Runnable {

    /**
     * Get Shared Preferences
     */
    private SharedPreferences prefs;

    public static final String DEF_RATEKEY = "20";
    public static final String DEF_SENDRATEKEY = "600";
    public static final Boolean DEF_COORDKEY = false;
    public static final Boolean DEF_COORDKONTIKEY = false;
    public static final String DEF_HOSTKEY = "8.8.8.8";
    public static final String DEF_HOSTPORTKEY = "7777";
    public static final String DEF_START_HOUR_KEY = "8";
    public static final String DEF_END_HOUR_KEY = "8";

    /**
     * Bind all xml elements with variables
     */
    private Preference prefName;
    private EditTextPreference prefCode;
    private static Preference prefRate;
    private Preference prefVATRate;
    private static Preference prefSendRate;
    private static Preference prefHost;
    private static Preference prefHostPort;
    private static Preference prefStartHour;
    private static Preference prefEndHour;
    private Preference prefSerial;
    private static Preference prefMonitorSerial;
    private Preference pushSDLoad;
    private Preference pushSDLoadNoUpdate;
    private Preference pushMBDOut;
    private static Preference pushCoord;
    private Preference pushSDOut;
    private Preference pushOptimization;
    private Preference pushInfo;
    private static CheckBoxPreference chkGPS;
    private CheckBoxPreference chkLongAttributes;
    private static CheckBoxPreference chkCoord;
    private static CheckBoxPreference chkSundayWork;
    private static CheckBoxPreference chkCoordKONTI;
    private static CheckBoxPreference chkAutoSync;
    private Preference prefInterface;
    private Preference prefOrderBy;
    private Preference prefProtocol;
    private Preference prefPricetype;
    private EditTextPreference prefCurrency;

    private static Preference prefAutosyncStartAt;
    private static Preference prefAutosyncStopAt;
    private static Preference prefAutosyncInterval;

    private EditTextPreference prefMonPassword;

    private static int mYear;
    private static int mMonth;
    private static int mDay;

    static boolean ClosePermission;
    static AlertDialog d;
    static TextView txtLog;
    static TextView txtTableName;
    static TextView txtCount;
    static TextView txtPos;
    static String curTable;

    static ProgressBar mProgress;
    static int iProgress;
    static int maxProgress;
    static boolean mDoFtpUpdate = true;
    /**
     * Activity to send in functions
     */
    Activity mAct;
    /**
     * Current date
     */
    String curDate;
    /**
     * Ord date
     */
    String ordDate;

    /**
     * Init database with architecture that designed in RsaDbHelper.class
     */
    RsaDbHelper mDb;
    /**
     * SQLite database that stores all data
     */
    static SQLiteDatabase db;
    /**
     * Context for some methods
     */
    Context context;

    /**
     * Used to receive binary result from function that cheking attachment
     */
    int binFlag = 0;
    /**
     * Thread for background downloading
     */
    static Thread background;
    /**
     * Used to send messages from Thread to main Thread
     */
    Handler mHandler;

    /**
     * Constant to identify kind of dialog when dialog window will be called = Loading data from
     * SDCARD
     */
    private final static int IDD_SDLOAD = 0;
    private final static int IDD_SDOUT = 1;
    private final static int IDD_CONFIRMATION = 2;


    // the callback received when the user "sets" the date in the dialog
    private DatePickerDialog.OnDateSetListener mDateSetListener =
            new DatePickerDialog.OnDateSetListener() {
                public void onDateSet(DatePicker view, int year,
                        int monthOfYear, int dayOfMonth) {
                    mYear = year;
                    mMonth = monthOfYear + 1;
                    mDay = dayOfMonth;

                    try {
                        // Get current date from DataPicker to String curDate in format like 23
                        // .02.2011
                        curDate = String.format("%02d.%02d.%02d", mDay,
                                mMonth,
                                mYear);

                        // Get current date from DataPicker to String ordDate in format like
						// 23_02_2011
                        ordDate = String.format("%02d_%02d_%02d", mDay,
                                mMonth,
                                mYear);
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), "Error: date formating", 5000)
                                .show();
                    }

                    if (writeToSD()) {
                        Toast.makeText(getApplicationContext(), R.string.prefs_sdout_done, 5000)
                                .show();
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.prefs_sdout_error, 5000)
                                .show();
                    }

                }
            };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        boolean lightTheme = getSharedPreferences(RsaDb.PREFS_NAME_MAIN,
                Context.MODE_PRIVATE).getBoolean(RsaDb.LIGHTTHEMEKEY, false);

        if (lightTheme)
            setTheme(android.R.style.Theme_Light);

        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        mAct = this;

        // Get Shared Preferences
        prefs = getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE);

        // Bind all xml elements with variables
        prefName = (Preference) findPreference("prefName");
        prefCode = (EditTextPreference) findPreference("prefCode");
        prefCurrency = (EditTextPreference) findPreference("prefCurrency");
        prefSerial = (Preference) findPreference("prefSerial");
        prefMonitorSerial = (Preference) findPreference("prefMonitorSerial");
        pushMBDOut = (Preference) findPreference("pushMBDOut");
        pushSDLoad = (Preference) findPreference("pushSDLoad");
        pushSDLoadNoUpdate = (Preference) findPreference("pushSDLoadNoUpdate");
        pushCoord = (Preference) findPreference("pushCoord");
        pushSDOut = (Preference) findPreference("pushSDOut");
        pushOptimization = (Preference) findPreference("pushOptimization");
        pushInfo = (Preference) findPreference("pushInfo");
        chkGPS = (CheckBoxPreference) findPreference("chkGPS");
        chkLongAttributes = (CheckBoxPreference) findPreference("prefLongAttributes");
        chkCoord = (CheckBoxPreference) findPreference("chkCoord");
        chkAutoSync = (CheckBoxPreference) findPreference("prefAutosync");
        prefAutosyncStartAt = (Preference) findPreference("prefAutosyncStartAt");
        prefAutosyncStopAt = (Preference) findPreference("prefAutosyncStopAt");
        prefAutosyncInterval = (Preference) findPreference("prefAutosyncInterval");
        chkCoordKONTI = (CheckBoxPreference) findPreference("chkCoordKONTI");
        chkSundayWork = (CheckBoxPreference) findPreference("chkSundayWork");
        prefRate = (Preference) findPreference("prefRate");
        prefVATRate = (Preference) findPreference("prefVATRate");
        prefInterface = (Preference) findPreference("prefInterface");
        prefOrderBy = (Preference) findPreference("prefOrderBy");
        prefProtocol = (Preference) findPreference("prefProtocol");
        prefPricetype = (Preference) findPreference("prefPricetype");
        prefSendRate = (Preference) findPreference("prefSendRate");
        prefHost = (Preference) findPreference("prefHost");
        prefHostPort = (Preference) findPreference("prefHostPort");
        prefStartHour = (Preference) findPreference("prefStartHour");
        prefEndHour = (Preference) findPreference("prefEndHour");
        prefMonPassword = (EditTextPreference) findPreference("prefMonPassword");

        prefMonPassword.setText("");

        prefMonPassword.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (checkPassword((String) newValue)) {
                    allowAll();
                } else {
                    denyAll();
                }

                return true;
            }
        });

        chkAutoSync.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if ((Boolean) newValue) {
                    AlarmAutoSync autosync_alarm = new AlarmAutoSync();
                    autosync_alarm.SetAlarm(getApplicationContext());
                } else {
                    AlarmAutoSync autosync_alarm = new AlarmAutoSync();
                    autosync_alarm.CancelAlarm(getApplicationContext());
                }
                return true;
            }
        });

        prefAutosyncStartAt.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (chkAutoSync.isChecked() == false)
                    return true;
                AlarmAutoSync autosync_alarm = new AlarmAutoSync();
                autosync_alarm.CancelAlarm(getApplicationContext());
                autosync_alarm.SetAlarm(getApplicationContext());
                return true;
            }
        });
        prefAutosyncStopAt.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (chkAutoSync.isChecked() == false)
                    return true;
                AlarmAutoSync autosync_alarm = new AlarmAutoSync();
                autosync_alarm.CancelAlarm(getApplicationContext());
                autosync_alarm.SetAlarm(getApplicationContext());
                return true;
            }
        });
        prefAutosyncInterval.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (chkAutoSync.isChecked() == false)
                    return true;
                AlarmAutoSync autosync_alarm = new AlarmAutoSync();
                autosync_alarm.CancelAlarm(getApplicationContext());
                autosync_alarm.SetAlarm(getApplicationContext());
                return true;
            }
        });

        pushMBDOut.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                String SD_CARD_PATH = Environment.getExternalStorageDirectory().toString()
                        + File.separator + "rsa";
                File dbFile = getDatabasePath(RsaDbHelper.DB_ORDERS);
                File target = new File(SD_CARD_PATH + File.separator + "base");

                try {
                    copy(dbFile, target);
                } catch (IOException e) {
                }

                Toast.makeText(getApplicationContext(), "Выгрузили", Toast.LENGTH_SHORT).show();

                return false;
            }
        });

        // If Load data from SD clicked then start activity that will download
        // data from sdcard/rsa/inbox to database
        pushSDLoad.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                // show dialog to set qty
                mDoFtpUpdate = true;
                showDialog(IDD_SDLOAD);
                return false;
            }
        });

        pushSDLoadNoUpdate.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                // show dialog to set qty
                mDoFtpUpdate = false;
                showDialog(IDD_SDLOAD);
                return false;
            }
        });

        pushSDOut.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                // show dialog to set qty
                showDialog(IDD_SDOUT);
                return false;
            }
        });

        pushOptimization.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Calendar mcal = Calendar.getInstance();
                String mdate1 = String.format("%02d.%02d.%02d", mcal.get(Calendar.DAY_OF_MONTH),
                        mcal.get(Calendar.MONTH) + 1, mcal.get(Calendar.YEAR));
                mcal.add(Calendar.DAY_OF_YEAR, -120);
                String sDate = String.format("%04d-%02d-%02d", mcal.get(Calendar.YEAR),
                        mcal.get(Calendar.MONTH) + 1, mcal.get(Calendar.DAY_OF_MONTH));
                String mdate2 = String.format("%02d.%02d.%02d", mcal.get(Calendar.DAY_OF_MONTH),
                        mcal.get(Calendar.MONTH) + 1, mcal.get(Calendar.YEAR));
                prefs.edit().putString(RsaDb.LASTOPTIMKEY, mdate2 + "-" + mdate1).commit();

                // Init database with architecture that designed in RsaDbHelper.class
                mDb = new RsaDbHelper(getApplicationContext(), RsaDbHelper.DB_ORDERS);
                // Open database with orders
                db = mDb.getWritableDatabase();

                // Delete lines from lines
                db.delete(RsaDbHelper.TABLE_LINES,
                        RsaDbHelper.LINES_ZAKAZ_ID + " IN (SELECT " + RsaDbHelper.HEAD_ZAKAZ_ID
                                + " FROM " + RsaDbHelper.TABLE_HEAD + " WHERE ( "
                                + RsaDbHelper.HEAD_SDATE + " BETWEEN date('2000-01-01') AND date('"
                                + sDate + "')"
                                + " ))", null);

                // Delete orders from head
                db.delete(RsaDbHelper.TABLE_HEAD,
                        RsaDbHelper.HEAD_SDATE + " BETWEEN date('2000-01-01') AND date('"
                                + sDate + "')", null);

                if (db != null) {
                    db.close();
                }

                updateData();
                return false;
            }
        });

        pushCoord.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                GpsUtils.exportGPSTrack(Preferences.this, true, null);
                return false;
            }
        });

        denyAll();
        prefs.registerOnSharedPreferenceChangeListener(this);
    }


    public static void copy(File source, File dest) throws IOException {
        FileChannel sourceChannel = new FileInputStream(source).getChannel();
        try {
            FileChannel destChannel = new FileOutputStream(dest).getChannel();
            try {
                destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
            } finally {
                destChannel.close();
            }
        } finally {
            sourceChannel.close();
        }
    }


    /**
     * Method that starts every time when Activity is shown on display
     */
    @Override
    public void onStart() {
        super.onStart();

        updateData();
    }

    private void updateData() {
        SharedPreferences dbprefs = getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);

        try {
            File file = getDatabasePath(RsaDbHelper.DB_ORDERS);
            float ord = file.length() / 1024;
            file = getDatabasePath(CoordProvider.DB_COORD);
            float gps = file.length() / 1024;
            file = getDatabasePath(dbprefs.getString(RsaDb.ACTUALDBKEY, "new"));
            float dbl = file.length() / 1024;
            pushInfo.setSummary(String.format("DB=%.1fKb ORD=%.1fKb GPS=%.1fKb", dbl, ord, gps));
        } catch (Exception e) {
        }

        prefCurrency.setSummary(prefCurrency.getText());

        prefName.setSummary(prefs.getString(RsaDb.NAMEKEY,
                getResources().getString(R.string.preferences_empty)));

        prefCode.setSummary(prefs.getString(RsaDb.CODEKEY,
                getResources().getString(R.string.preferences_empty)));
        prefRate.setSummary(prefs.getString(RsaDb.RATEKEY,
                getResources().getString(R.string.preferences_empty)));
        prefVATRate.setSummary(prefs.getString(RsaDb.VATRATE, "20"));
        prefSendRate.setSummary(prefs.getString(RsaDb.SENDRATEKEY, DEF_SENDRATEKEY));
        prefHost.setSummary(prefs.getString(RsaDb.HOSTKEY, DEF_HOSTKEY));
        prefHostPort.setSummary(prefs.getString(RsaDb.HOSTPORTKEY, DEF_HOSTPORTKEY));
        prefStartHour.setSummary(prefs.getString(RsaDb.START_HOUR_KEY, DEF_START_HOUR_KEY));
        prefEndHour.setSummary(prefs.getString(RsaDb.END_HOUR_KEY, DEF_END_HOUR_KEY));
        prefSerial.setSummary(prefs.getString(RsaDb.IMEIKEY,
                getResources().getString(R.string.preferences_empty)));
        prefMonitorSerial.setSummary(prefs.getString(RsaDb.MONITORSERIAL,
                getResources().getString(R.string.preferences_empty)));

        // pushSDLoad.setSummary(prefs.getString(RsaDb.LASTSDLOADKEY, getResources().getString(R
		// .string.preferences_not)));
        pushOptimization.setSummary(prefs.getString(RsaDb.LASTOPTIMKEY,
                getResources().getString(R.string.preferences_not)));
        if (prefs.getBoolean(RsaDb.GPSKEY, true)) {
            if (checkPassword(prefMonPassword.getText().toString())) {
                chkCoord.setEnabled(true);
                chkCoord.setEnabled(true);
                chkCoordKONTI.setEnabled(true);
                chkSundayWork.setEnabled(true);
                prefRate.setEnabled(false);
                prefSendRate.setEnabled(true);
                prefHost.setEnabled(true);
                prefHostPort.setEnabled(true);
                prefStartHour.setEnabled(true);
                prefEndHour.setEnabled(true);
            }
            chkGPS.setSummary(getResources().getString(R.string.preferences_active));

            startService(new Intent(Preferences.this, RsaGpsService.class));
            Alarm alarm = new Alarm();
            alarm.SetAlarm(this);

        } else {
            chkCoord.setChecked(false);

            if (checkPassword(prefMonPassword.getText().toString())) {
                chkCoord.setEnabled(false);
                chkCoordKONTI.setEnabled(false);
                chkSundayWork.setEnabled(false);
                prefRate.setEnabled(true);
                prefSendRate.setEnabled(false);
                prefHost.setEnabled(false);
                prefHostPort.setEnabled(false);
                prefStartHour.setEnabled(false);
                prefEndHour.setEnabled(false);
            }
            //prefs.edit().putBoolean(RsaDb.COORDKEY, false).commit();
            chkCoord.setSummary(getResources().getString(R.string.preferences_notactive));
            chkCoordKONTI.setSummary(getResources().getString(R.string.preferences_notactive));
            chkGPS.setSummary(getResources().getString(R.string.preferences_notactive));
            Alarm alarm = new Alarm();
            alarm.CancelAlarm(this);
            stopService(new Intent(Preferences.this, RsaGpsService.class));
        }

        if (prefs.getBoolean(RsaDb.COORDKONTIKEY, DEF_COORDKONTIKEY)) {
            chkCoordKONTI.setSummary(getResources().getString(R.string.preferences_active));
        } else {
            chkCoordKONTI.setSummary(getResources().getString(R.string.preferences_notactive));
        }

        if (prefs.getBoolean(RsaDb.COORDKEY, DEF_COORDKEY)) {
            chkCoord.setSummary(getResources().getString(R.string.preferences_active));
            if (checkPassword(prefMonPassword.getText().toString())) {
                prefSendRate.setEnabled(false);
                prefHost.setEnabled(false);
                chkCoordKONTI.setEnabled(false);
                chkSundayWork.setEnabled(false);
                prefHostPort.setEnabled(false);
                prefStartHour.setEnabled(false);
                prefEndHour.setEnabled(false);
            }
        } else if (chkGPS.isChecked()) {
            chkCoord.setSummary(getResources().getString(R.string.preferences_notactive));
            if (checkPassword(prefMonPassword.getText().toString())) {
                prefSendRate.setEnabled(true);
                prefHost.setEnabled(true);
                chkCoordKONTI.setEnabled(true);
                chkSundayWork.setEnabled(true);
                prefHostPort.setEnabled(true);
                prefStartHour.setEnabled(true);
                prefEndHour.setEnabled(true);
            }
        } else {
            if (checkPassword(prefMonPassword.getText().toString())) {
                prefSendRate.setEnabled(false);
                prefHost.setEnabled(false);
                prefHostPort.setEnabled(false);
                prefStartHour.setEnabled(false);
                prefEndHour.setEnabled(false);
                chkCoordKONTI.setEnabled(false);
                chkSundayWork.setEnabled(false);
            }
        }

        prefInterface.setSummary(prefs.getString(RsaDb.INTERFACEKEY,
                getResources().getString(R.string.preferences_dbf)));
        prefOrderBy.setSummary(prefs.getString(RsaDb.ORDERBYKEY,
                getResources().getString(R.string.preferences_orderby_default)));
        prefProtocol.setSummary(prefs.getString(RsaDb.PROTOCOLKEY,
                getResources().getString(R.string.preferences_email)));
        prefPricetype.setSummary(prefs.getBoolean(RsaDb.PRICETYPEKEY, false)
                ? getResources().getString(R.string.preferences_active)
                : getResources().getString(R.string.preferences_notactive));
        SharedPreferences def_prefs = PreferenceManager.getDefaultSharedPreferences(
                getApplicationContext());

        prefPricetype.setEnabled(!def_prefs.getBoolean("prefPrtypeBlock", false));

        chkLongAttributes.setSummary(prefs.getBoolean(RsaDb.LONGATTRIBUTES, false)
                ? getResources().getString(R.string.preferences_active)
                : getResources().getString(R.string.preferences_notactive));

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences arg0, String arg1) {
        updateData();
    }

    /**
     * Runs when dialog called to show first time
     *
     * @param id constant identifier of kind of dialog
     *
     * @return pointer to Dialog that has been created
     */
    @Override
    protected Dialog onCreateDialog(int id) {
        // Determining the type of dialog to show, in this Activity only one dialog
        switch (id) {
            case IDD_SDLOAD: {
                /** Set my own view of dialog to display to layout variable. Using .xml */
                LayoutInflater inflater = getLayoutInflater();
                View layout = inflater.inflate(R.layout.dlg_sdload,
                        (ViewGroup) findViewById(R.id.dlg_sdload));

                /** Building dialog view with my own xml view */
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setView(layout);
                builder.setMessage(R.string.dlg_sdload_title);

                /**
                 * Listener for OK button
                 */
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing here. We'll override the onclick in onPrepareDialog()
                    }
                });

                // Sets Dialog is not cancelable by pressing Back-button on device
                builder.setCancelable(false);

                // return with dialog creation
                return builder.create();
            }
            case IDD_SDOUT: {
                // get the current date
                final Calendar c = Calendar.getInstance();
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);

                return new DatePickerDialog(this,
                        mDateSetListener,
                        mYear, mMonth, mDay);
            }
            case IDD_CONFIRMATION: {
                AlertDialog.Builder adb = new AlertDialog.Builder(this);
                adb.setTitle("Внимание");
                adb.setMessage(
                        "Очистить историю заказов и кассы согласно установленным значениям?");
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
            default:
                // Do nothing if another kind of dialog is selected
                return null;
        }
    }

    /**
     * Runs everytime when Dialog called to show()
     *
     * @param id constant identifier of kind of dialog
     * @param dialog pointer to Dialog object
     */
    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        // Determining the type of dialog to show, in this Activity only one dialog
        switch (id) {
            case IDD_SDLOAD: {
                String iFace = getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE)
                        .getString(RsaDb.INTERFACEKEY, "DBF");
                d = (AlertDialog) dialog;
                txtLog = (TextView) d.findViewById(R.id.txtLog_sdload);
                txtTableName = (TextView) d.findViewById(R.id.txtTableName_sdload);
                txtCount = (TextView) d.findViewById(R.id.txtRecordsCount_sdload);
                txtPos = (TextView) d.findViewById(R.id.txtPosition_sdload);
                mProgress = (ProgressBar) d.findViewById(R.id.Progress_sdload);

                d.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        Button b = d.getButton(AlertDialog.BUTTON_POSITIVE);
                        b.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                // Dismiss once everything is OK.
                                if (ClosePermission)
                                    d.dismiss();
                            }
                        });
                    }
                });

                txtLog.setTextColor(Color.parseColor("#FFFFFF"));
                txtLog.setText(
                        getResources().getString(R.string.prefs_sdload_via) + " " + iFace + "\n");
                ClosePermission = false;

                if (!sdFilesOK(iFace))
                    break;

                // Init new thread with function run() in that class
                background = new Thread(this);
                mHandler = new Handler() {
                    @Override
                    public void handleMessage(android.os.Message msg) {
                        String s = msg.getData().getString("LOG");
                        if (s.equals("TABLE")) {
                            curTable = msg.getData().getString("TABLE");
                            txtTableName.setText(curTable + ":");
                            iProgress = 0;
                            mProgress.setProgress(iProgress);
                            txtPos.setText(Integer.toString(iProgress));
                            maxProgress = msg.getData().getInt("COUNT");
                            mProgress.setMax(maxProgress);
                            txtCount.setText(Integer.toString(maxProgress));
                        } else if (s.equals("PROGRESS")) {
                            txtTableName.setText(curTable + ":");
                            txtCount.setText(Integer.toString(maxProgress));
                            mProgress.setMax(maxProgress);
                            iProgress++;
                            mProgress.setProgress(iProgress);
                            txtPos.setText(Integer.toString(iProgress));
                        } else if (s.equals("ERR")) {
                            txtLog.setTextColor(Color.parseColor("#FF0000"));
                            txtLog.append(getResources().getString(R.string.prefs_sdload_error));
                            ClosePermission = true;
                        } else if (s.equals("SUC")) {
                            txtLog.setTextColor(Color.parseColor("#00FF00"));
                            txtLog.setText(getResources().getString(R.string.prefs_sdload_done));
                            ClosePermission = true;
                            SharedPreferences def_prefs
                                    = PreferenceManager.getDefaultSharedPreferences(
                                    getApplicationContext());
                            if (def_prefs.getBoolean("shrinkAfter", false)) {
                                showDialog(IDD_CONFIRMATION);
                            }

                        }
                    }
                };

                // Start downloading in thread
                background.start();
                break;
            }
            default:
                // Do nothing if another kind of dialog is selected
        }
    }

    private boolean sdFilesOK(String iFace) {
        String SD_CARD_PATH = Environment.getExternalStorageDirectory().toString() + File.separator
                + "rsa";

        SharedPreferences scr_prefs = getSharedPreferences(RsaDb.PREFS_NAME_MAIN,
                Context.MODE_PRIVATE);
        int whichInterface = detectWorkinf(
                SD_CARD_PATH + File.separator + "inbox" + File.separator);

        if (whichInterface == 0) {
            // if workinf was not found
        } else if (whichInterface == 1) {
            // if DBF only
            scr_prefs.edit().putString(RsaDb.INTERFACEKEY, "DBF").commit();
        } else if (whichInterface == 2) {
            // if XML only
            scr_prefs.edit().putString(RsaDb.INTERFACEKEY, "XML").commit();
        } else if (whichInterface == 3) {
            // if csv only
            scr_prefs.edit().putString(RsaDb.INTERFACEKEY, "CSV").commit();
        }

        iFace = scr_prefs.getString(RsaDb.INTERFACEKEY, "DBF");

        binFlag = RsaDb.parseSDFiles(iFace);
        if ((binFlag & 0x1F) == 0x1F) // (x & 00011111) == 00011111?
        {
            txtLog.append(getResources().getString(R.string.prefs_sdload_el) + " ");

            if ((binFlag & 0x20) == 0x20) {
                txtLog.append("Sk, ");
            }
            if ((binFlag & 0x40) == 0x40) {
                txtLog.append("Gr, ");
            }
            if ((binFlag & 0x80) == 0x80) {
                txtLog.append("Br, ");
            }
            if ((binFlag & 0x100) == 0x100) {
                txtLog.append("If.");
            }

            return true;
        } else {
            txtLog.setTextColor(Color.parseColor("#FF0000"));
            txtLog.append(getResources().getString(R.string.prefs_sdload_nofiles));
            ClosePermission = true;
            return false;
        }
    }

    private static int detectWorkinf(String pth) {
        int result = 0;
        // 0 - not found, 1 - dbf, 2 - xml, 3 - csv, 4 - more

        File f = new File(pth + "Workinf.DBF");
        if (f.exists()) {
            result = 1;
        }

        f = new File(pth + "workinf.xml");
        if (f.exists() && (result == 0)) {
            result = 2;
        } else {
            result = 4;
        }

        f = new File(pth + "workinf.csv");
        if (f.exists() && (result == 0)) {
            result = 3;
        } else if (result == 0) {
            result = 4;
        }

        return result;
    }

    @Override
    public void run() {
        /** Handler message */
        android.os.Message hMess;
        Bundle data = new Bundle();
        String iFace = null;

        // Get Shared Preferences and fill special field of message with it
        prefs = getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);

        /** Context for some methods */
        context = getApplicationContext();

        if (!prefs.getString(RsaDb.ACTUALDBKEY, "").equals(RsaDbHelper.DB_NAME1)) {
            mDb = new RsaDbHelper(context, RsaDbHelper.DB_NAME1);
        } else {
            mDb = new RsaDbHelper(context, RsaDbHelper.DB_NAME2);
        }

        db = mDb.getWritableDatabase();
        db.execSQL("PRAGMA foreign_keys=OFF;");

        String SD_CARD_PATH = Environment.getExternalStorageDirectory().toString()
                + File.separator + "rsa";

        SharedPreferences scr_prefs = getSharedPreferences(RsaDb.PREFS_NAME_MAIN,
                Context.MODE_PRIVATE);

        iFace = scr_prefs.getString(RsaDb.INTERFACEKEY, "DBF");

        try {
            if (iFace.equals("DBF")) {
                RsaDb.dbfToDb(mAct, getApplicationContext(), db,
                        "inbox" + File.separator + "Cust.DBF", RsaDb.DBF_CUST, mHandler,
                        SD_CARD_PATH);

                if ((binFlag & 0x80) == 0x80)
                    RsaDb.dbfToDb(mAct, getApplicationContext(), db,
                            "inbox" + File.separator + "Brand.DBF", RsaDb.DBF_BRAND, mHandler,
                            SD_CARD_PATH);
                if ((binFlag & 0x40) == 0x40)
                    RsaDb.dbfToDb(mAct, getApplicationContext(), db,
                            "inbox" + File.separator + "Group.DBF", RsaDb.DBF_GROUP, mHandler,
                            SD_CARD_PATH);
                if ((binFlag & 0x20) == 0x20)
                    RsaDb.dbfToDb(mAct, getApplicationContext(), db,
                            "inbox" + File.separator + "Sklad.DBF", RsaDb.DBF_SKLAD, mHandler,
                            SD_CARD_PATH);
                RsaDb.dbfToDb(mAct, getApplicationContext(), db,
                        "inbox" + File.separator + "Debit.DBF", RsaDb.DBF_DEBIT, mHandler,
                        SD_CARD_PATH);
                RsaDb.dbfToDb(mAct, getApplicationContext(), db,
                        "inbox" + File.separator + "Shop.DBF", RsaDb.DBF_SHOP, mHandler,
                        SD_CARD_PATH);
                RsaDb.dbfToDb(mAct, getApplicationContext(), db,
                        "inbox" + File.separator + "Char.DBF", RsaDb.DBF_CHAR, mHandler,
                        SD_CARD_PATH);
                RsaDb.dbfToDb(mAct, getApplicationContext(), db,
                        "inbox" + File.separator + "Goods.DBF", RsaDb.DBF_GOODS, mHandler,
                        SD_CARD_PATH);
                if ((binFlag & 0x100) == 0x100)
                    RsaDb.dbfToDb(mAct, getApplicationContext(), db,
                            "inbox" + File.separator + "Workinf.DBF", RsaDb.DBF_WORKINF, mHandler,
                            SD_CARD_PATH);
                if ((binFlag & 0x200) == 0x200)
                    RsaDb.dbfToDb(mAct, getApplicationContext(), db,
                            "inbox" + File.separator + "Plan.DBF", RsaDb.DBF_PLAN, mHandler,
                            SD_CARD_PATH);
                if ((binFlag & 0x4000) == 0x4000)
                    RsaDb.dbfToDb(mAct, getApplicationContext(), db,
                            "inbox" + File.separator + "Skladdet.DBF", RsaDb.DBF_SKLADDET, mHandler,
                            SD_CARD_PATH);
            } else if (iFace.equals("XML")) {
                RsaDb.XMLtoCust(context, db, mHandler, SD_CARD_PATH);
                RsaDb.XMLtoBrand(context, db, mHandler, SD_CARD_PATH);
                RsaDb.XMLtoGroup(context, db, mHandler, SD_CARD_PATH);
                RsaDb.XMLtoSklad(context, db, mHandler, SD_CARD_PATH);
                RsaDb.XMLtoDebit(context, db, mHandler, SD_CARD_PATH);
                RsaDb.XMLtoShop(context, db, mHandler, SD_CARD_PATH);
                RsaDb.XMLtoChar(context, db, mHandler, SD_CARD_PATH);
                RsaDb.XMLtoGoods(context, db, mHandler, SD_CARD_PATH);
                if ((binFlag & 0x100) == 0x100)
                    RsaDb.XMLtoFTP(context, db, mHandler, SD_CARD_PATH, mDoFtpUpdate);
                if ((binFlag & 0x200) == 0x200)
                    RsaDb.XMLtoPlan(context, db, mHandler, SD_CARD_PATH);
                if ((binFlag & 0x400) == 0x400)
                    RsaDb.XMLtoSold(context, db, mHandler, SD_CARD_PATH);
                if ((binFlag & 0x800) == 0x800)
                    RsaDb.XMLtoMatrix(context, db, mHandler, SD_CARD_PATH);
                if ((binFlag & 0x2000) == 0x2000)
                    RsaDb.XMLtoHist(context, db, mHandler, SD_CARD_PATH);
                if ((binFlag & 0x4000) == 0x4000)
                    RsaDb.XMLtoSkladDet(context, db, mHandler, SD_CARD_PATH);
                if ((binFlag & 0x8000) == 0x8000)
                    RsaDb.XMLtoSales(context, db, mHandler, SD_CARD_PATH);
                if ((binFlag & 0x10000) == 0x10000)
                    RsaDb.XMLtoStaticPlan(context, db, mHandler, SD_CARD_PATH);

            } else if (iFace.equals("CSV")) {
                RsaDb.CSVtoCust(context, db, mHandler, SD_CARD_PATH);
                RsaDb.CSVtoBrand(context, db, mHandler, SD_CARD_PATH);
                RsaDb.CSVtoGroup(context, db, mHandler, SD_CARD_PATH);
                RsaDb.CSVtoSklad(context, db, mHandler, SD_CARD_PATH);
                RsaDb.CSVtoDebit(context, db, mHandler, SD_CARD_PATH);
                RsaDb.CSVtoShop(context, db, mHandler, SD_CARD_PATH);
                RsaDb.CSVtoChar(context, db, mHandler, SD_CARD_PATH);
                RsaDb.CSVtoGoods(context, db, mHandler, SD_CARD_PATH);
                if ((binFlag & 0x100) == 0x100)
                    RsaDb.CSVtoFTP(context, db, mHandler, SD_CARD_PATH);
                if ((binFlag & 0x200) == 0x200)
                    RsaDb.CSVtoPlan(context, db, mHandler, SD_CARD_PATH);
            }
        } catch (Exception e) {
            hMess = mHandler.obtainMessage();
            data.putString("LOG", "ERR");
            hMess.setData(data);
            mHandler.sendMessage(hMess);
            if (db != null) {
                db.close();
            }
            return;
        }

        try {
            RsaDb.copyTable(getDatabasePath(
                    prefs.getString(RsaDb.ACTUALDBKEY, RsaDbHelper.DB_NAME1)).toString(), db,
                    binFlag);
        } catch (Exception e) {
            Log.d("copyTable", "EXCEPTION in method");
        }

        db.execSQL("PRAGMA foreign_keys=ON;");
        if (db != null) {
            db.close();
        }

        hMess = mHandler.obtainMessage();
        data.putString("LOG", "SUC");
        hMess.setData(data);
        mHandler.sendMessage(hMess);

        // Set new DB to actual state
        if (!prefs.getString(RsaDb.ACTUALDBKEY, "").equals(RsaDbHelper.DB_NAME1))
            prefs.edit().putString(RsaDb.ACTUALDBKEY, RsaDbHelper.DB_NAME1).commit();
        else
            prefs.edit().putString(RsaDb.ACTUALDBKEY, RsaDbHelper.DB_NAME2).commit();
    }

    private boolean writeToSD() {
        String SD_CARD_PATH = "";
        try {
            // Init database with architecture that designed in RsaDbHelper.class
            mDb = new RsaDbHelper(getApplicationContext(), RsaDbHelper.DB_ORDERS);
            // Open database with orders
            db = mDb.getWritableDatabase();
            SD_CARD_PATH = Environment.getExternalStorageDirectory().toString() + File.separator
                    + "rsa";
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Error: opening db and init SD_CARD_PATH", 5000)
                    .show();
        }

        SharedPreferences __prefs = getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);
        RsaDbHelper __mDb = new RsaDbHelper(this,
                __prefs.getString(RsaDb.ACTUALDBKEY, RsaDbHelper.DB_NAME1));

        try {
            if (getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE)
                    .getString(RsaDb.INTERFACEKEY, "DBF").equals("DBF")) {
                RsaDb.dbToDBF(mAct, getApplicationContext(), db, "outbox/Head.dbf",
                        "outbox/Lines.dbf", curDate, R.drawable.ic_locked_, SD_CARD_PATH, false);
            } else if (getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE)
                    .getString(RsaDb.INTERFACEKEY, "DBF").equals("XML")) {
                Log.d("RRR", "before dbToXML()...");
                RsaDb.dbToXML(mAct, getApplicationContext(), db, __mDb, "outbox/Head.xml",
                        "outbox/Lines.xml", curDate, R.drawable.ic_locked_, SD_CARD_PATH, false, null);
                Log.d("RRR", "after dbToXML()!");
            } else if (getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE)
                    .getString(RsaDb.INTERFACEKEY, "DBF").equals("CSV")) {
                RsaDb.dbToCSV(mAct, getApplicationContext(), db, "outbox/", "not used", curDate,
                        R.drawable.ic_locked_, SD_CARD_PATH, false);
            }

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

        } catch (Exception e) {
            if (db != null) {
                db.close();
            }
            return false;
        }

        try {
            if (db != null) {
                db.close();
            }
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Error: closing db", 5000).show();
        }
        return true;
    }

    private static boolean checkPassword(String pass) {
        Calendar c = Calendar.getInstance();
        String currentDay = Integer.toString(7 + c.get(Calendar.DAY_OF_MONTH)); // Current day + 7

        if (pass.equals(currentDay)) {
            return true;
        }

        return false;
    }

    private static void denyAll() {
        prefMonitorSerial.setEnabled(false);
        chkCoord.setEnabled(false);
        prefRate.setEnabled(false);
        chkGPS.setEnabled(false);
        prefSendRate.setEnabled(false);
        prefHost.setEnabled(false);
        prefHostPort.setEnabled(false);
        prefStartHour.setEnabled(false);
        prefEndHour.setEnabled(false);
        chkCoordKONTI.setEnabled(false);
        chkSundayWork.setEnabled(false);
        pushCoord.setEnabled(false);
    }

    private void allowAll() {
        prefMonitorSerial.setEnabled(true);
        //chkCoord.setEnabled(false);
        prefRate.setEnabled(true);
        chkGPS.setEnabled(true);
        //prefSendRate.setEnabled(false);
        //prefHost.setEnabled(false);
        //prefHostPort.setEnabled(false);
        //prefStartHour.setEnabled(false);
        //prefEndHour.setEnabled(false);
        //chkCoordKONTI.setEnabled(false);
        pushCoord.setEnabled(true);
        updateData();
    }

}