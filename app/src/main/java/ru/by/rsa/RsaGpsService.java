package ru.by.rsa;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Calendar;


import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.location.GpsStatus.NmeaListener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import ru.by.rsa.utils.GpsUtils;

/**
 * Service class used like GPS tracking system
 * @author Komarev Roman
 * Odessa, neo3da@mail.ru, +380503412392
 */
public class RsaGpsService extends Service implements OnSharedPreferenceChangeListener
{
	// WORK VARIABLES
		/** Current NMEA only $..MC stored */
		private static RsaNMEA CurrentNMEA;
		/** Define variable that will point to GPS location service */
		private static LocationManager locationManager;
		/** Used to hold smartphone CPU in wake state until service is active */
		private static PowerManager.WakeLock WakeLock;
		/** Location in spec radius (deltaN|deltaE) for about [limitTimeInRange] minutes*/
		private static int timesInRange;
		private static long lastN;
		private static long lastE;
	//	private static Context myCont;// to remove
		//private static Context cont;
		
		
	// HANDLERS AND THREADs
		/** Define Handler to call periodic task that writes coords to DB */
		private Handler coordHandler = new Handler();
		/** Define Handler to call periodic task that check if now working hours */
		private Handler timeHandler = new Handler();
		/** Define Handler to call periodic task that sends coords to ANTOR */
		private Handler sendcoordsHandler = new Handler();
		/** Thread for background downloading */
		public static Thread background;
		public static Thread backgroundKONTI;
		
	// PREFERENCES
		/** Start HOUR of work */
		private static int start_HOUR; 	//8
		/** End HOUR of work */ 		// 19
		private static int end_HOUR;
		private static boolean work_on_sunday;
		/** Write coords Task will start every xx seconds */
		private static long COORD_TASK;
		/** Send coords every 20 seconds = 20000 ms */
		private static long SENDCOORD_TASK;
		/** send coord preference if active */
		private static boolean prefSendCoord;
		private static boolean prefSendCoordKONTI;
		/** Server address */
		private static String HostName;
		/** TCP-port number */
		private static int Port;
		private static String HostNameKONTI;
		private static int PortKONTI;
	
	// GLOBAL INDICATORS
		/** Current battery level */
		private static int BATTERY_LEVEL;
		/** if locationlistener ais active */
		private static boolean isLocationListener;
		/** if coord writing is active */
		private static boolean isWritecoordTask;
		/** if coord sending is active */
		private static boolean isCoordSending;
		/** if sendcoordthread in process */
		private static boolean isCoordSendingThread;

	// SYSTEM CONSTANTS
		/** Application version */
		private static String APP_VERSION;
		/** IMEI Number */
		private static String DEV_IMEI;
		/** Android version */
		private static String ANDROID_VER;
		
	// SERVICE CONSTANTS	
		/** Check time Task will start every 1 minute = 60000 ms */
		private final static long CHKTIME_TASK   	= 60000;
		/** Listen sensor every 2 seconds = 2000 ms */
		private final static long LOCATION_LISTEN	= 5000;
		/** AUTHORIZATION OK MESSAGE */
		private final static String AUTH_OK = "$3SACK*29"; 
		/** QTY of messages per block */
		private final static int BLOCK_QTY = 20;
		/** TIMEOUT while sending coords, count of trys */
		private final static int TIMEOUT = 10;
		/** New order broadcast message */
		public static final String NEW_ORDER = "NEW ORDER";
		/** Main application trying to sync, broadcast message */
		public static final String APP_SYNC = "NEW SYNC";
		/** Distance 5 meters by N degrees */
		public static final long deltaN = 6000L;
		/** Distance 5 meters by E degrees */
		public static final long deltaE = 8000L;
		/** Count of GPS fixes for time range = 5 minutes */
		private static int limitTimesInRange; 
		/** Time in radius that used for start|stop point 1 minutes */
		private static final int limitTime = 1;   // not used since 28 Oct 2012 (28.10.2012)
	
	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}
	@Override 
    public void onCreate() 
	{
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		WakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "My Tag");
		
		SharedPreferences prefs = getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE);
		initVariables(prefs);
		prefs.registerOnSharedPreferenceChangeListener(this);
		
        registerReceiver(batteryLevelReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        registerReceiver(newOrderReceiver, new IntentFilter(RsaGpsService.NEW_ORDER));
        registerReceiver(newSyncReceiver, new IntentFilter(RsaGpsService.APP_SYNC));
        registerReceiver(lightsOnReceiver, new IntentFilter(Intent.ACTION_SCREEN_ON));
    } 
	@Override 
    public void onStart(Intent intent, int startid) 
	{
		// start periodic task in 1 second, that do main job
		timeHandler.postDelayed(checktimeTask, 1000);
		//myCont = getApplicationContext();  // to remove
    } 
    @Override 
    public void onDestroy() 
	{ 
    	super.onDestroy();
    	
    	if (batteryLevelReceiver != null)
    		unregisterReceiver(batteryLevelReceiver);
    	if (newOrderReceiver != null)
    		unregisterReceiver(newOrderReceiver);
    	if (newSyncReceiver != null)
    		unregisterReceiver(newSyncReceiver);
    	if (lightsOnReceiver != null)
    		unregisterReceiver(lightsOnReceiver);
    	
    	stopWriteCoordTask();
    	stopCheckTimeTask();
    	stopLocationListener();    	
    	stopNmeaListener();    	
    	stopNetStateReceiver();
    	stopWakeLock();
    }
    
	/** First initialize of variables for first use */
    private void initVariables(SharedPreferences prefs)
    {
    	BATTERY_LEVEL = 100;
    	
		try 
			{ APP_VERSION = getPackageManager().getPackageInfo(getPackageName(), 0).versionName; } 
		catch (NameNotFoundException e)
			{ APP_VERSION = "1.00";	}
		DEV_IMEI = RsaDb.getDImei(getApplicationContext());
		ANDROID_VER = android.os.Build.VERSION.RELEASE;
		
    	isLocationListener 		= false;
		isWritecoordTask 		= false;
		isCoordSending 			= false;
		isCoordSendingThread 	= false;
		CurrentNMEA 			= new RsaNMEA();
		
		SharedPreferences def_prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		work_on_sunday = def_prefs.getBoolean("chkSundayWork", false);
		
		start_HOUR = Integer.parseInt(getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE).getString(RsaDb.START_HOUR_KEY, Preferences.DEF_START_HOUR_KEY));
		end_HOUR = Integer.parseInt(getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE).getString(RsaDb.END_HOUR_KEY, Preferences.DEF_END_HOUR_KEY));
		
		COORD_TASK = 1000 * Integer.parseInt(getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE).getString(RsaDb.RATEKEY, "15"));
		SENDCOORD_TASK = 1000 * Integer.parseInt(getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE).getString(RsaDb.SENDRATEKEY, "20"));
		prefSendCoord = getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE).getBoolean(RsaDb.COORDKEY, false);
		prefSendCoordKONTI = getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE).getBoolean(RsaDb.COORDKONTIKEY, false);
		HostName = getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE).getString(RsaDb.HOSTKEY, "8.8.8.8");
		try {
		Port = Integer.parseInt(getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE).getString(RsaDb.HOSTPORTKEY, "7777"));
		} catch (Exception e) {
			Port=6666;
		}
		HostNameKONTI = "195.184.205.214";
		PortKONTI = 5555;
		
		
		// Romka 1.90 (28 Oct 2012  28.10.2012)
		// limitTimesInRange = (int)(limitTime*60*1000/COORD_TASK);
		limitTimesInRange = 2;
		
		timesInRange = 0;
		lastN = 0;
		lastE = 0;
		
		updatePreferences(prefs);
    }
	@Override
	public void onSharedPreferenceChanged(SharedPreferences prefs, String key)
	{
		updatePreferences(prefs);
	}
	private void updatePreferences(SharedPreferences prefs)
	{
		start_HOUR = Integer.parseInt(getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE).getString(RsaDb.START_HOUR_KEY, Preferences.DEF_START_HOUR_KEY));
		end_HOUR = Integer.parseInt(getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE).getString(RsaDb.END_HOUR_KEY, Preferences.DEF_END_HOUR_KEY));
		
		SharedPreferences def_prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		work_on_sunday = def_prefs.getBoolean("chkSundayWork", false);
		
		COORD_TASK = 1000 * Integer.parseInt(prefs.getString(RsaDb.RATEKEY, Preferences.DEF_RATEKEY));
		SENDCOORD_TASK = 1000 * Integer.parseInt(prefs.getString(RsaDb.SENDRATEKEY, Preferences.DEF_SENDRATEKEY));
		prefSendCoord = prefs.getBoolean(RsaDb.COORDKEY, Preferences.DEF_COORDKEY);
		prefSendCoordKONTI = prefs.getBoolean(RsaDb.COORDKONTIKEY, Preferences.DEF_COORDKONTIKEY);
		HostName = prefs.getString(RsaDb.HOSTKEY, Preferences.DEF_HOSTKEY);
		Port = Integer.parseInt(prefs.getString(RsaDb.HOSTPORTKEY, Preferences.DEF_HOSTPORTKEY));	
		HostNameKONTI = "195.184.205.214";
		PortKONTI = 5555;
	}
	public static boolean inRangeLongTime()
	{
		long CurN;
		long CurE;
		
		try 
		{
			CurN = Long.parseLong( CurrentNMEA.get(RsaNMEA.LONGTITUDE).replace(".", "") );
			CurE = Long.parseLong( CurrentNMEA.get(RsaNMEA.LATITUDE).replace(".", "") );
		}
		catch (Exception e) 
		{
			return false;
		}
		
		if ((Math.abs(CurN-lastN)<deltaN) && (Math.abs(CurE-lastE)<deltaE))
		{
			timesInRange++;
		}
		else
		{
			timesInRange = 0;
			lastN = CurN;
			lastE = CurE;
		}
		
		if (timesInRange<limitTimesInRange)
		{
			return false;
		}
		else
		{
			timesInRange = 0;
			return true;
		}
	}
	
/////////////////////////////////////////////////////////////    
//TASKS//// ///////////////////////////////////////////////// 
	/** 
	 * Define task that will start every 5 minutes and check time 
	 * for working hours. And if it find so than do main job
	 */
	private Runnable checktimeTask = new Runnable() 
	{
        public void run() 
        {
        	try 
        	{
	        	// Get current TIME to "c" variable, HOUR to "h", DAY to "d" 
	        	Calendar c = Calendar.getInstance();
	        	int h = c.get(Calendar.HOUR_OF_DAY);
	        	int d = c.get(Calendar.DAY_OF_WEEK);
	        	// is now is working time
	        	boolean isTime = false;
	        	
	        	// if today is not SUNDAY AND current HOUR is in workhours range (8:00 - 18:00) then do
	     //   	if (true)
	        	if ( ((d!=Calendar.SUNDAY)||(work_on_sunday==true))
	        			&& ((h>=start_HOUR)&&(h<end_HOUR)))
	        	{
	        		
	        		if (WakeLock.isHeld()==false)
	        		{
	        			// WakeLock.acquire();
	        		}
	        		// if not previously activated listener for GPS location sensor
	        		// then we should do all from very beginning
	        		if (isLocationListener == false)
	        		{
	        			// Stop periodic task that writing coords every 20 seconds to DB
	        			// if it is active now
	        			stopWriteCoordTask();        			
	        			stopNetStateReceiver();
	        				
	        			// Acquire a reference to the system Location Manager
	        			locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	        			// Register the listener with the Location Manager to receive location updates
	        			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, LOCATION_LISTEN, 0, locationListener);
	        			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_LISTEN, 0, locationListener);
	        			locationManager.addNmeaListener(nmealistener);
	        			isLocationListener = true;
	        			
	        			// Start write coord periodic task
	        			coordHandler.postDelayed(writecoordTask, COORD_TASK);
	        			isWritecoordTask = true;
	        			
	        			if (prefSendCoord)
	        			{
	        				registerReceiver(mConnReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
	        				isCoordSending = true;
	        			}
	        		}
	        		else 
	        		{	// if locationlistener active, but writecoord periodic task is not active
	        			if (isWritecoordTask == false)
	        			{	// kill old if exist and start new one
	        				stopWriteCoordTask();
	        				// start new one
	        				coordHandler.postDelayed(writecoordTask, COORD_TASK);
	        				isWritecoordTask = true;
	        			}
	        				
	        			// if locationlistener active, but mConnReceiver for coord sending not active
	        			if (isCoordSending == false)
	        			{	// kill old if exist and start new one
	        				stopNetStateReceiver();
	        				// start new one
	        				if (prefSendCoord)
	            			{
	            				registerReceiver(mConnReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
	            				isCoordSending = true;
	            			}
	        			}
	        		}
	        		isTime = true;
	        	}            
	        	
	        	// if now is not working time then deactivate all listeners
	        	if (isTime == false)
	        	{
	            	stopWriteCoordTask();
	        		stopLocationListener();
	        		stopNmeaListener();
	        		stopNetStateReceiver();
	        		stopWakeLock();
	        	}
        	}
        	catch (Exception e) { }
        	timeHandler.postDelayed(checktimeTask, CHKTIME_TASK);
        }
    };
    private Runnable sendcoordsTask = new Runnable() 
	{
    	@Override
        public void run() 
        {
        	if (isCoordSendingThread == false)
        	{
        		background = new Thread(sendcoordThread);
        		background.start();
        		
        		if (prefSendCoordKONTI)
        		{
	        		backgroundKONTI = new Thread(sendcoordThreadKONTI);
	        		backgroundKONTI.start();
        		}
        	}
        	
        	if (prefSendCoord)
        		sendcoordsHandler.postDelayed(sendcoordsTask, SENDCOORD_TASK);
        }
	};
  
	private Runnable sendcoordThread = new Runnable()
	{
		@Override
		public void run()
		{
			if (isLicensed())
			{
				isCoordSendingThread = true;
				sendAllCoords();
				isCoordSendingThread = false;
			}
		}
	};
	private Runnable sendcoordThreadKONTI = new Runnable()
	{
		@Override
		public void run()
		{
			if (isLicensed())
			{
				isCoordSendingThread = true;
				sendAllCoordsKONTI();
				isCoordSendingThread = false;
			}
		}
	};
	private Runnable writecoordTask = new Runnable() 
	{
        public void run()
        {
        	ContentValues values = new ContentValues(5);
        	Calendar c = Calendar.getInstance();
        	String curDate = String.format( "%04d-%02d-%02d", c.get(Calendar.YEAR), c.get(Calendar.MONTH)+1, c.get(Calendar.DAY_OF_MONTH) );
        	String curTime = String.format( "%02d:%02d:%02d", c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), c.get(Calendar.SECOND) );
        	
            /** Get current coords and write it to DB */
        	LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			long[] l  = GpsUtils.getGPS(lm);
			
			String sStatus = CurrentNMEA.get(RsaNMEA.STATUS);
			boolean bStatus = sStatus.equals("A");
			if ( (l!=null)&&(bStatus) )
			{
				if (inRangeLongTime()) CurrentNMEA.setStartFlag(true);
				values.put(CoordDbHelper.DATE,   curDate);
				values.put(CoordDbHelper.TIME,   curTime);
				values.put(CoordDbHelper.COORD,  Long.toString(l[0]) + " " + Long.toString(l[1]));
				values.put(CoordDbHelper.SENT,   "0"); // Sent or not+
				values.put(CoordDbHelper.AVER,   "1"); // Protocol version+
				values.put(CoordDbHelper.AFLG,   "0"); // Flag 0,1,2,3+
				values.put(CoordDbHelper.ADATE,  CurrentNMEA.get(RsaNMEA.DATE));
				values.put(CoordDbHelper.AUTC,   CurrentNMEA.get(RsaNMEA.UTCTIME));
				values.put(CoordDbHelper.ALAT,   CurrentNMEA.get(RsaNMEA.LATITUDE));
				values.put(CoordDbHelper.ASIND,  CurrentNMEA.get(RsaNMEA.NINDICATOR));
				values.put(CoordDbHelper.ALONG,  CurrentNMEA.get(RsaNMEA.LONGTITUDE));
				values.put(CoordDbHelper.AWIND,  CurrentNMEA.get(RsaNMEA.WINDICATOR));
				values.put(CoordDbHelper.AALT,   "50"); // altitude
				values.put(CoordDbHelper.ASPEED, CurrentNMEA.get(RsaNMEA.SPEED));
				values.put(CoordDbHelper.ACOURSE,CurrentNMEA.get(RsaNMEA.COURSE));
				values.put(CoordDbHelper.ABAT,   Integer.toString(BATTERY_LEVEL)); // battery level
				values.put(CoordDbHelper.ASTART, CurrentNMEA.getStartFlag()); // first point after stop or no
				CurrentNMEA.setStartFlag(false);
				values.put(CoordDbHelper.AFIX, sStatus);  // Actual data or not A/V
				values.put(CoordDbHelper.FNMEA, CurrentNMEA.getNMEA());  // Actual data or not A/V
				values.put(CoordDbHelper.SENTKONTI,  "0"); // Sent or not+
				
				getContentResolver().insert(CoordProvider.CONTENT_URI, values);
			}
			else // Added 28.10.2012 Romka   if fix was lost then make last coord startflag = 1 (true)
			{
				setPreviousStartflagON();
			}
            coordHandler.postDelayed(writecoordTask, COORD_TASK);
        }
    };
    
/////////////////////////////////////////////////////////////    
//RECEIVERS /////////////////////////////////////////////////     
    private BroadcastReceiver mConnReceiver = new BroadcastReceiver() 
    {
        @Override
        public void onReceive(Context context, Intent intent) 
        {
            boolean noConnectivity = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
                        
            if (noConnectivity == false)
            {	// Network ON
            	if (sendcoordsTask!=null)
            		sendcoordsHandler.removeCallbacks(sendcoordsTask);
            	sendcoordsHandler.postDelayed(sendcoordsTask, 3000);
            }
            else
            {	// Network OFF
            	if (sendcoordsTask!=null)
            		sendcoordsHandler.removeCallbacks(sendcoordsTask);
            	isCoordSendingThread = false;
            }
        }
    };
    private BroadcastReceiver batteryLevelReceiver = new BroadcastReceiver() 
    {
    	@Override
        public void onReceive(Context context, Intent intent) 
        {
            // context.unregisterReceiver(this);
            int rawlevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            int level = 100;
            
            if (rawlevel >= 0 && scale > 0) 
            {
                level = (rawlevel * 100) / scale;
            }
            BATTERY_LEVEL =  level;
        }
    };
    
    private BroadcastReceiver newOrderReceiver = new BroadcastReceiver() 
    {
    	@Override
        public void onReceive(Context context, Intent intent) 
        {
    		if (intent.getAction().equalsIgnoreCase(NEW_ORDER))
    		{
    			boolean flag = intent.getBooleanExtra(RsaDb.MAKESTARTKEY, false);
    			CurrentNMEA.setStartFlag(flag);
    			//Toast.makeText(getApplicationContext(),"Something changed",Toast.LENGTH_LONG).show();
    		}
        }
    };
    
    private BroadcastReceiver lightsOnReceiver = new BroadcastReceiver() 
    {
    	@Override
        public void onReceive(Context context, Intent intent) 
        {
    			CurrentNMEA.setStartFlag(true);
        }
    };
    
    private BroadcastReceiver newSyncReceiver = new BroadcastReceiver() 
    {
    	@Override
        public void onReceive(Context context, Intent intent) 
        {
    		if (intent.getAction().equalsIgnoreCase(APP_SYNC))
    		{
    			if (sendcoordsTask!=null)
            		sendcoordsHandler.removeCallbacks(sendcoordsTask);
            	sendcoordsHandler.postDelayed(sendcoordsTask, 3000);
    		}
        }
    };
    
/////////////////////////////////////////////////////////////    
//LISTENERS ///////////////////////////////////////////////// 
    private static LocationListener locationListener = new LocationListener() 
	{
		public void onLocationChanged(Location location) {}
		public void onProviderEnabled(String provider) {}
		public void onProviderDisabled(String provider) {}
		@Override
		public void onStatusChanged(String arg0, int arg1, Bundle arg2) {}
	};
	private static NmeaListener nmealistener = new NmeaListener()
	{
		@Override
		public void onNmeaReceived(long timestamp, String nmea)
		{
			CurrentNMEA.setNMEA(nmea);   // myCont -> to remove
			//CurrentNMEA.setNMEA("$GPRMC,150259.000,A,4832.246,N,03501.000,E,,,110612,,*06");
		}
	};	
	
	// 28.10.2012 if fix was lost then update last record startflag to 1
	void setPreviousStartflagON()
	{
		Cursor mCursor;
		String idPosition = "";
    	final String[] mContent = new String[]	{ CoordDbHelper._ID,		// [0] -  idPosition 
    											  CoordDbHelper.SENT, 		// [1] -  "0" if not sent
				  								  CoordDbHelper.ASTART }; 	// [2] -  Start point
    	mCursor = getContentResolver().query(CoordProvider.CONTENT_URI, mContent, 
    													CoordDbHelper.SENT + "='0'", 
    													null, null);
    	ContentValues val = new ContentValues();
    	
    	if ((mCursor!=null)&&(mCursor.getCount()>0))
    	{
    		idPosition = getIdPositionofLast(mCursor);
    		val.put(CoordDbHelper.ASTART, "1");
    		getContentResolver().update(CoordProvider.CONTENT_URI, val, CoordDbHelper._ID + "='" 
    										+ idPosition + "'", null);
    	}

    	if (mCursor != null) 
	    {
    		mCursor.close();
	    }
	}
	
	
/////////////////////////////////////////////////////////////    
//SEND METHODS //////////////////////////////////////////////    
    private void sendAllCoords()
    {
    	Cursor mCursor;
    	final String[] mContent = new String[]	{ CoordDbHelper._ID,		// [0] -  idPosition 
    											  CoordDbHelper.SENT, 		// [1] -  "0" if not sent
    											  CoordDbHelper.AVER,		// [2] -  Protocol version
				  								  CoordDbHelper.AFLG, 		// [3] -  Message flag 0,1,2,3
				  								  CoordDbHelper.ADATE, 		// [4] -  Date
				  								  CoordDbHelper.AUTC,		// [5] -  Time UTC
				  								  CoordDbHelper.ALAT, 		// [6] -  Lattitude
				  								  CoordDbHelper.ASIND, 		// [7] -  S indicator
				  								  CoordDbHelper.ALONG, 		// [8] -  Longtitude
				  								  CoordDbHelper.AWIND, 		// [9] -  W indicator
				  								  CoordDbHelper.AALT, 		// [10] - Altitude
				  								  CoordDbHelper.ASPEED, 	// [11] - Speed
				  								  CoordDbHelper.ACOURSE, 	// [12] - Course
				  								  CoordDbHelper.ABAT, 		// [13] - Battery level
				  								  CoordDbHelper.ASTART, 	// [14] - Start point
				  								  CoordDbHelper.AFIX };		// [15] - "A" - good data, "V" - bad data
    	mCursor = getContentResolver().query(CoordProvider.CONTENT_URI, mContent, 
    													CoordDbHelper.SENT + "='0'", 
    													null, null);
    	int blockCount = getBlockCount(mCursor, BLOCK_QTY);
    	int deltaCount = getDeltaCount(mCursor, BLOCK_QTY);
    	long idPosition = 0;
    	if ((blockCount != 0)||(deltaCount != 0))
    	{	// if there are some thing to send then take "_id" of first element in db
    		idPosition = getIdPosition(mCursor);
    	}
    	else
    	{	// if nothing to send then exit
    		if (mCursor != null) 
    	    {
        		mCursor.close();
    	    }
    		return;
    	}
    	int i = 0;
    	int timeout = 0;
    	boolean auth = false;
    	Socket s = new Socket();
    	OutputStream out = null;
    	BufferedReader in = null;
    	for(int j=0;j<3;j++)
    	{
    		try { s.connect(new InetSocketAddress(HostName, Port)); } catch (Exception e) {}
    		if (s.isConnected()) 
    		{
    			try
				{   
    				s.setSoTimeout(3000);
    				out = s.getOutputStream();
					in = new BufferedReader(new InputStreamReader(s.getInputStream())); // String st = in.readLine();
					auth = authorization(s, out, in);
				} 
    			catch (Exception e)
				{
    				timeout = TIMEOUT + 1;
				}
    			break;
    		}
    	}
    	if ((s.isConnected()==false)||(auth==false)) 
    		timeout = TIMEOUT + 1;
    	while ((i<blockCount)&&(timeout<TIMEOUT))
    	{
    		if (sendBlock(s, HostName, Port, out, in, mCursor, idPosition, i*BLOCK_QTY, i*BLOCK_QTY+BLOCK_QTY))
    		{
    			i++;
    			timeout = 0;
    		}
    		else
    		{
    			timeout++;
    		}
    	}
    	while ((i==blockCount)&&(timeout<TIMEOUT)&&(deltaCount>0))
    	{
    		
    		if (sendBlock(s, HostName, Port, out, in, mCursor, idPosition, i*BLOCK_QTY, i*BLOCK_QTY + deltaCount))
    		{
    			i++;
    			timeout = 0;
    		}
    		else
    		{
    			timeout++;
    		}
    	}
    	try 
    	{ 
    		s.close(); 
    	} catch (IOException e) {}
    	if (mCursor != null) 
	    {
    		mCursor.close();
	    }
    } 	
    private void sendAllCoordsKONTI()
    {
    	Cursor mCursor;
    	final String[] mContent = new String[]	{ CoordDbHelper._ID,		// [0] -  idPosition 
    											  CoordDbHelper.SENTKONTI,	// [1] -  "0" if not sentkonti
    											  CoordDbHelper.AVER,		// [2] -  Protocol version
				  								  CoordDbHelper.AFLG, 		// [3] -  Message flag 0,1,2,3
				  								  CoordDbHelper.ADATE, 		// [4] -  Date
				  								  CoordDbHelper.AUTC,		// [5] -  Time UTC
				  								  CoordDbHelper.ALAT, 		// [6] -  Lattitude
				  								  CoordDbHelper.ASIND, 		// [7] -  S indicator
				  								  CoordDbHelper.ALONG, 		// [8] -  Longtitude
				  								  CoordDbHelper.AWIND, 		// [9] -  W indicator
				  								  CoordDbHelper.AALT, 		// [10] - Altitude
				  								  CoordDbHelper.ASPEED, 	// [11] - Speed
				  								  CoordDbHelper.ACOURSE, 	// [12] - Course
				  								  CoordDbHelper.ABAT, 		// [13] - Battery level
				  								  CoordDbHelper.ASTART, 	// [14] - Start point
				  								  CoordDbHelper.AFIX };		// [15] - "A" - good data, "V" - bad data
    	mCursor = getContentResolver().query(CoordProvider.CONTENT_URI, mContent, 
    													CoordDbHelper.SENTKONTI + "='0'", 
    													null, null);
    	int blockCount = getBlockCount(mCursor, BLOCK_QTY);
    	int deltaCount = getDeltaCount(mCursor, BLOCK_QTY);
    	long idPosition = 0;
    	if ((blockCount != 0)||(deltaCount != 0))
    	{	// if there are some thing to send then take "_id" of first element in db
    		idPosition = getIdPosition(mCursor);
    	}
    	else
    	{	// if nothing to send then exit
    		if (mCursor != null) 
    	    {
        		mCursor.close();
    	    }
    		return;
    	}
    	int i = 0;
    	int timeout = 0;
    	boolean auth = false;
    	Socket s = new Socket();
    	OutputStream out = null;
    	BufferedReader in = null;
    	for(int j=0;j<3;j++)
    	{
    		try { s.connect(new InetSocketAddress(HostNameKONTI, PortKONTI)); } catch (Exception e) {}
    		if (s.isConnected()) 
    		{
    			try
				{   
    				s.setSoTimeout(3000);
    				out = s.getOutputStream();
					in = new BufferedReader(new InputStreamReader(s.getInputStream())); // String st = in.readLine();
					auth = authorization(s, out, in);
				} 
    			catch (Exception e)
				{
    				timeout = TIMEOUT + 1;
				}
    			break;
    		}
    	}
    	if ((s.isConnected()==false)||(auth==false)) 
    		timeout = TIMEOUT + 1;
    	while ((i<blockCount)&&(timeout<TIMEOUT))
    	{
    		if (sendBlockKONTI(s, HostNameKONTI, PortKONTI, out, in, mCursor, idPosition, i*BLOCK_QTY, i*BLOCK_QTY+BLOCK_QTY))
    		{
    			i++;
    			timeout = 0;
    		}
    		else
    		{
    			timeout++;
    		}
    	}
    	while ((i==blockCount)&&(timeout<TIMEOUT)&&(deltaCount>0))
    	{
    		
    		if (sendBlockKONTI(s, HostNameKONTI, PortKONTI, out, in, mCursor, idPosition, i*BLOCK_QTY, i*BLOCK_QTY + deltaCount))
    		{
    			i++;
    			timeout = 0;
    		}
    		else
    		{
    			timeout++;
    		}
    	}
    	try 
    	{ 
    		s.close(); 
    	} catch (IOException e) {}
    	if (mCursor != null) 
	    {
    		mCursor.close();
	    }
    }
    private boolean sendBlock(Socket s, String host, int port, OutputStream out, BufferedReader in, Cursor cur, long idPos, int start, int end)
    {
    	boolean sent = false;
    	byte[] bline = null;
    	StringBuilder line = null;
    	// TRY SEND GPS DATA VIA INTERNET
    	try
		{
    		if ((s.isConnected()==false)||(s.isClosed()))
    		{	
    			s = new Socket();
    			s.setSoTimeout(3000);
    			s.connect(new InetSocketAddress(host, port));
    			out = s.getOutputStream();											// String st = in.readLine();
    			in = new BufferedReader(new InputStreamReader(s.getInputStream())); // String st = in.readLine();
    		}
			cur.moveToPosition(start);
			for(int i=start;i<end;i++)
			{
				line = new StringBuilder("$3SGPS"); 			// Header
				line.append("," + cur.getString(2));			// Protocol Version
				line.append(",");								// ,
				if ((start+1)==end)
				{
					line.append("3");							// Flag 3 - if there is one message in block
				}
				else
				{
					if (i==start)
					{
						line.append("1");							// Flag 1 - if there is first message in block
					}
					else if (i==end-1)
					{
						line.append("2");							// Flag 2 - if there is last message in block
					}
					else
					{
						line.append("0");							// Flag 0 - if message inside block
					}
				}
				line.append("," + cur.getString(4));			// Date
				line.append("," + cur.getString(5));			// Time
				line.append("," + cur.getString(6));			// Lattitude
				line.append("," + cur.getString(7));			// S indicator
				line.append("," + cur.getString(8));			// Longtitude
				line.append("," + cur.getString(9));			// W indicator
				line.append("," + cur.getString(10));			// Altitude
				line.append("," + cur.getString(11));			// Speed
				line.append("," + cur.getString(12));			// Course
				line.append("," + cur.getString(13));			// Battery level
				line.append("," + cur.getString(14));			// Start 0,1
				line.append("*");								// end of block
				line.append(RsaNMEA.calcCRC(line.toString()));	// CRC
				line.append("\r\n");
				
				bline = line.toString().getBytes("ASCII");
			
				// System.out.println("RRR send> " + line.toString());
				out.write(bline);
				cur.moveToNext();
			}			
			sent = verifySentBlock(s, in);
		} 
    	catch (Exception e)
		{
			sent = false;
			try 
	    	{ 
	    		in.close();
				out.close();
	    		s.close(); 
	    	} catch (IOException e2) {}
		} 
    	// IF GPS DATA WAS SENT THEN MARK BLOCK AS SENT
    	if (sent)
    	{
    		ContentValues val = new ContentValues();
    		val.put(CoordDbHelper.SENT, "1");
    		getContentResolver().update(CoordProvider.CONTENT_URI, val, CoordDbHelper._ID + " BETWEEN '" 
    										+ Long.toString(idPos+start) + "' AND '" 
    										+ Long.toString(idPos+end-1) + "'", null);
    	}
    	return sent;
    }
    private boolean sendBlockKONTI(Socket s, String host, int port, OutputStream out, BufferedReader in, Cursor cur, long idPos, int start, int end)
    {
    	boolean sent = false;
    	byte[] bline = null;
    	StringBuilder line = null;
    	// TRY SEND GPS DATA VIA INTERNET
    	try
		{
    		if ((s.isConnected()==false)||(s.isClosed()))
    		{	
    			s = new Socket();
    			s.setSoTimeout(3000);
    			s.connect(new InetSocketAddress(host, port));
    			out = s.getOutputStream();											// String st = in.readLine();
    			in = new BufferedReader(new InputStreamReader(s.getInputStream())); // String st = in.readLine();
    		}
			cur.moveToPosition(start);
			for(int i=start;i<end;i++)
			{
				line = new StringBuilder("$3SGPS"); 			// Header
				line.append("," + cur.getString(2));			// Protocol Version
				line.append(",");								// ,
				if ((start+1)==end)
				{
					line.append("3");							// Flag 3 - if there is one message in block
				}
				else
				{
					if (i==start)
					{
						line.append("1");							// Flag 1 - if there is first message in block
					}
					else if (i==end-1)
					{
						line.append("2");							// Flag 2 - if there is last message in block
					}
					else
					{
						line.append("0");							// Flag 0 - if message inside block
					}
				}
				line.append("," + cur.getString(4));			// Date
				line.append("," + cur.getString(5));			// Time
				line.append("," + cur.getString(6));			// Lattitude
				line.append("," + cur.getString(7));			// S indicator
				line.append("," + cur.getString(8));			// Longtitude
				line.append("," + cur.getString(9));			// W indicator
				line.append("," + cur.getString(10));			// Altitude
				line.append("," + cur.getString(11));			// Speed
				line.append("," + cur.getString(12));			// Course
				line.append("," + cur.getString(13));			// Battery level
				line.append("," + cur.getString(14));			// Start 0,1
				line.append("*");								// end of block
				line.append(RsaNMEA.calcCRC(line.toString()));	// CRC
				line.append("\r\n");
				
				bline = line.toString().getBytes("ASCII");
			
				// System.out.println("RRR send> " + line.toString());
				out.write(bline);
				cur.moveToNext();
			}			
			sent = verifySentBlock(s, in);
		} 
    	catch (Exception e)
		{
			sent = false;
			try 
	    	{ 
	    		in.close();
				out.close();
	    		s.close(); 
	    	} catch (IOException e2) {}
		} 
    	// IF GPS DATA WAS SENT THEN MARK BLOCK AS SENT
    	if (sent)
    	{
    		ContentValues val = new ContentValues();
    		val.put(CoordDbHelper.SENTKONTI, "1");
    		getContentResolver().update(CoordProvider.CONTENT_URI, val, CoordDbHelper._ID + " BETWEEN '" 
    										+ Long.toString(idPos+start) + "' AND '" 
    										+ Long.toString(idPos+end-1) + "'", null);
    	}
    	return sent;
    }
    private static int getBlockCount(Cursor cur, int qty)
    {
    	return (int)(cur.getCount() / qty);
    }
    private static int getDeltaCount(Cursor cur, int qty)
    {
    	return (cur.getCount() - qty*getBlockCount(cur, qty));
    }
    private static long getIdPosition(Cursor cur)
    {
    	cur.moveToFirst();
    	return Long.parseLong(cur.getString(0));
    }
    private static String getIdPositionofLast(Cursor cur)
    {
    	cur.moveToLast();
    	return cur.getString(0);
    }
    private static boolean verifySentBlock(Socket s, BufferedReader in) 
    {
    	String st;
		try
		{
			st = in.readLine();
		} 
		catch (IOException e)
		{
			return false;
		}
		
    	return st.equals(AUTH_OK);
    }
    private static boolean authorization(Socket s, OutputStream out, BufferedReader in) 
    {
    	String ser = Integer.toString( Math.abs((int)Long.parseLong(DEV_IMEI)) );
    	
		StringBuilder mess = new StringBuilder("$3SAUTH");			// $3SAUTH
		mess.append("," + DEV_IMEI);                           		// ,<IMEI>
		mess.append("," + ser);										// ,<SN>
		mess.append("," + APP_VERSION);								// ,<SW>
		mess.append("," + ANDROID_VER);								// ,<FW>
		mess.append("*");											//*
		mess.append(RsaNMEA.calcCRC(mess.toString()));				//<Checksum>
		mess.append("\r\n");										//\r\n
		
		String st = null;
		try
		{
		//	System.out.println("RRR> AU=" + mess.toString());
			out.write(mess.toString().getBytes("ASCII"));
			st = in.readLine();
		//	System.out.println("RRR> AUTH=" + st);
		} 
		catch (Exception e)
		{
			return false;
		}
		
    	return st.equals(AUTH_OK);
    }   
    private boolean isLicensed()
    {
    	return true;
    /*	try
    	{
    	///////////begin cheking	
    		RsaDb.Au a = new RsaDb.Au();
    		URL url = new URL(    a.getS1() + a.getS2() + a.getS3() + a.getS4() + a.getS5()
    	        				+ a.getS6() + a.getS7() + a.getS8() + a.getS9() + a.getS10()
    	        				+ a.getS11() + a.getS12() + a.getS13()
    					+ ((TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId());
    		URLConnection conn = url.openConnection();
    		InputStreamReader rd = new InputStreamReader(conn.getInputStream());
    		BufferedReader reader = new BufferedReader(rd);
    		StringBuilder allpage = new StringBuilder();
    		String line = null;
    		while ((line = reader.readLine()) != null) 
    			allpage.append(line + System.getProperty("line.separator"));
    		String pagetext = allpage.toString();
    		if (pagetext.contains("YES") == true)
    		{
    			try { reader.close(); rd.close(); } catch (Exception e) { }
    			return true;
    		}
    		
    		url = new URL(a.getS1() + a.getS2() + a.getS3() + a.getS4() + a.getS5()
    				+ a.getS6() + a.getS7() + a.getS8() + a.getS9() + a.getS10()
    				+ a.getS11() + a.getS12() + a.getS13() 
    				+ a.getD1() + a.getD2());
    		conn = url.openConnection();
    		rd = new InputStreamReader(conn.getInputStream());
    		reader = new BufferedReader(rd);
    		allpage = new StringBuilder();
    		line = null;
    		while ((line = reader.readLine()) != null) 
    			allpage.append(line + System.getProperty("line.separator"));
    		pagetext = allpage.toString();
    		try { reader.close(); rd.close(); } catch (Exception e) { }
    		if (pagetext.contains("YES") == true)
    			return false;
    		else if (pagetext.contains("NO") == true)
    			return true;
    	//////////end cheking	
    	}
    	catch (Exception e) 
    	{
			return false;
		}
    	
    	return false;*/
    }
/////////////////////////////////////////////////////////////    
//  STOP METHODS ////////////////////////////////////////////
    private void stopWakeLock()
    {
    	if (WakeLock.isHeld())
		{
			 WakeLock.release();
		}
    }
    private void stopCheckTimeTask()
    {
    	if (checktimeTask != null)
    		timeHandler.removeCallbacks(checktimeTask);
    }
    private void stopWriteCoordTask()
    {
    	if ((writecoordTask != null)&&(isWritecoordTask==true))
    		coordHandler.removeCallbacks(writecoordTask);
    	isWritecoordTask = false;
    }
    private void stopLocationListener()
    {
    	if ((locationListener != null)&&(isLocationListener==true))
    		locationManager.removeUpdates(locationListener);
    	isLocationListener = false;
    }
    private void stopNmeaListener()
    {
    	if ((nmealistener != null)&&(isLocationListener==true))
    		locationManager.removeNmeaListener(nmealistener);
    }
    private void stopNetStateReceiver()
    {
    	if ((mConnReceiver != null)&&(isCoordSending==true))
    		unregisterReceiver(mConnReceiver);
    	if (sendcoordsTask!=null)
    		sendcoordsHandler.removeCallbacks(sendcoordsTask);
    	isCoordSending = false;
    }
    
}
