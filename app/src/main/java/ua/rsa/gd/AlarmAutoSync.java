package ua.rsa.gd;


import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;

import ua.rsa.gd.R;

/**
 * Alarm class that used to periodic wakeup gpsservice
 * @author Komarev Roman
 * Odessa, neo3da@mail.ru, +380503412392
 */
public class AlarmAutoSync extends BroadcastReceiver
{
	 /**
	  * Called by system every 15 seconds, and performs StartService 
	  * @param context
	  * @param intent
	  */
	 @Override
	 public void onReceive(Context context, Intent intent) 
     {
		 SharedPreferences def_prefs = PreferenceManager.getDefaultSharedPreferences(context);
		 int minH = 12;
		 int maxH = 19;
		 int hour = 10;
		 
		 try {
			 minH = Integer.parseInt(def_prefs.getString("prefAutosyncStartAt", "12"));
			 maxH = Integer.parseInt(def_prefs.getString("prefAutosyncStopAt", "19"));
			 Calendar c = Calendar.getInstance();
			 hour = c.get(Calendar.HOUR_OF_DAY);
			 if (hour<minH || hour>maxH) return;
		 } catch (Exception e) {
			 return;
		 }
		 
         PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
         PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "YOUR TAG");
         wl.acquire();

         Log.d("AlarmAutoSync", "onReceive() - start of sync");
         RsaApplication app = (RsaApplication) context.getApplicationContext();
         if (app.getSyncState() == RsaApplication.STATE_STANDBY) {
        	 Log.d("AlarmAutoSync", "onReceive() - trying sync..."); 
        	 app.setSyncState(RsaApplication.STATE_AUTOSYNC);
        	 
        	 try {
        		 String[] protArray = context.getResources().getStringArray(R.array.prefProtocol);
        		 SharedPreferences screen_prefs = context.getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE);
 					// if protocol = "E-mail" then...
 				 if (screen_prefs.getString(RsaDb.PROTOCOLKEY, protArray[0]).equals(protArray[0])) {
 					 makeSyncViaEMAIL(context);
 				 } else {
 					 makeSyncViaFTP(context, app.orderingId);
 				 }
        	 } catch(Exception e) {}
        	 
        	 if (app.getSyncState()==RsaApplication.STATE_AUTOSYNC) 
        		 app.setSyncState(RsaApplication.STATE_STANDBY);
        	 
         } else {
        	 Log.d("AlarmAutoSync", "onReceive() - is not standby..."); 
         }
         Log.d("AlarmAutoSync", "onReceive() - end of sync");
         
         wl.release();
     }

	 /**
	  * Set alarm to perform every 15 seconds
	  * @param context
	  */
	 public void SetAlarm(Context context)
	 {
		 Log.d("AlarmAutoSync", "SetAlarm()");
	     AlarmManager am=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
	     Intent i = new Intent(context, AlarmAutoSync.class);
	     PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
	     SharedPreferences def_prefs = PreferenceManager.getDefaultSharedPreferences(context);
	     int period = Integer.parseInt(def_prefs.getString("prefAutosyncInterval", "60"));
	     am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000*60*period, pi); // Millisec * Second * Minute
	 }

	 /**
	  * Cancel alarm
	  * @param context
	  */
	 public void CancelAlarm(Context context)
	 {
		 Log.d("AlarmAutoSync", "CancelAlarm()");
	     Intent intent = new Intent(context, AlarmAutoSync.class);
	     PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
	     AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
	     alarmManager.cancel(sender);
	 }
	 
	 void makeSyncViaFTP(Context ctx, String orderingId) {
		 Log.d("AlarmAutoSync", "onReceive() -  makeSyncviaFTP()...");
		 SyncAuto_FTP sm = new SyncAuto_FTP(ctx, orderingId);
	 }
	 
	 void makeSyncViaEMAIL(Context ctx) {
		 Log.d("AlarmAutoSync", "onReceive() -  makeSyncviaFTP()...");
		 SyncAuto_EMAIL sm = new SyncAuto_EMAIL(ctx);
	 }
	 
}
