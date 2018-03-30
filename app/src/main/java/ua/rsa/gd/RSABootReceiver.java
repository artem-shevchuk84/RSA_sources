package ua.rsa.gd;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class RSABootReceiver extends BroadcastReceiver
{

	@Override
	public void onReceive(Context context, Intent intent)
	{
		SharedPreferences prefs = context.getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE);
		if (prefs.getBoolean(RsaDb.GPSKEY, true))
		{
			context.startService(new Intent(context, RsaGpsService.class));
			Alarm alarm = new Alarm();
			alarm.SetAlarm(context);
		}
	}

}
