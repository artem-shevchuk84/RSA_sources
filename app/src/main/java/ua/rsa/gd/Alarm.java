package ua.rsa.gd;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;

/**
 * Alarm class that used to periodic wakeup gpsservice
 * @author Komarev Roman
 * Odessa, neo3da@mail.ru, +380503412392
 */
public class Alarm extends BroadcastReceiver
{
	 /**
	  * Called by system every 15 seconds, and performs StartService 
	  * @param context
	  * @param intent
	  */
	 @Override
	 public void onReceive(Context context, Intent intent) 
     {   
         PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
         PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "YOUR TAG");
         wl.acquire();
         
         
         Log.d("Alarm", "onReceive()");
         context.startService(new Intent(context, RsaGpsService.class));
         
         wl.release();
     }

	 /**
	  * Set alarm to perform every 15 seconds
	  * @param context
	  */
	 public void SetAlarm(Context context)
	 {
		 Log.d("Alarm", "SetAlarm()");
	     AlarmManager am=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
	     Intent i = new Intent(context, Alarm.class);
	     PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
	     am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000*60*15, pi); // Millisec * Second * Minute
	 }

	 /**
	  * Cancel alarm
	  * @param context
	  */
	 public void CancelAlarm(Context context)
	 {
		 Log.d("Alarm", "CancelAlarm()");
	     Intent intent = new Intent(context, Alarm.class);
	     PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
	     AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
	     alarmManager.cancel(sender);
	 }
}
