package ua.rsa.gd;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.facebook.stetho.Stetho;

import ua.rsa.gd.BuildConfig;

public class RsaApplication extends Application {

    public static final int STATE_STANDBY = 0; // агент не работает с активити и синхра не запущена
    public static final int STATE_ORDERNIG = 1; // агент создает заявку
    public static final int STATE_MANUALSYNC = 2; // агент запустил ручную синхру
    public static final int STATE_AUTOSYNC = 3; // происходит автосинхра

    private static RsaApplication singleton;
    public int currentSyncState;
    public String orderingId;

    public static RsaApplication getInstance() {
        return singleton;
    }

    public void setSyncState(int state) {
        currentSyncState = state;
        Log.d("RRRSingle", Integer.toString(state));
    }

    public int getSyncState() {
        return currentSyncState;
    }

    @Override
    public final void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            Stetho.initializeWithDefaults(this);
        }
        singleton = this;
        setSyncState(STATE_STANDBY);
        SharedPreferences def_prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (def_prefs.getBoolean("prefAutosync", false)) {
            AlarmAutoSync autosync_alarm = new AlarmAutoSync();
            autosync_alarm.SetAlarm(this);
        }
    }

}
