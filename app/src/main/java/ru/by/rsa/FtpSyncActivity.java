package ru.by.rsa;

import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.TabHost;

/**
 * Allows user to do synchronization with server
 * using different methods
 * @author Komarev Roman
 * Odessa, neo3da@mail.ru, +380503412392
 */
public class FtpSyncActivity extends TabActivity
{
	
	/** Current theme */
	private boolean lightTheme;
	
	public void onCreate(Bundle savedInstanceState) 
	{
		lightTheme = getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE).getBoolean(RsaDb.LIGHTTHEMEKEY, false);
		
		if (lightTheme)
		{
			setTheme(R.style.Theme_Custom);
			super.onCreate(savedInstanceState);
			setContentView(R.layout.l_sync);
		}
		else
		{
			setTheme(R.style.Theme_CustomBlack2);
			super.onCreate(savedInstanceState);
			setContentView(R.layout.sync);
		}

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        
	    SharedPreferences prefs;
	    prefs = getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);
	    
	    prefs.edit().putBoolean(RsaDb.ACTIVESYNCKEY, false).commit();
	    
	    TabHost tabHost = getTabHost();  // The activity TabHost
	    TabHost.TabSpec spec;  // Resusable TabSpec for each tab
	    Intent intent;  // Reusable Intent for each tab

	    // Create an Intent to launch an Activity for the tab (to be reused)
	    intent = new Intent().setClass(this, FtpSendActivity.class);

	    // Initialize a TabSpec for each tab and add it to the TabHost
	    spec = tabHost.newTabSpec("send")
	    			.setIndicator(getResources().getString(R.string.sync_send), getResources().getDrawable(R.drawable.ic_tab_send))
	                .setContent(intent);
	    tabHost.addTab(spec);

	    // Do the same for the other tabs
	    intent = new Intent().setClass(this, FtpDownloadActivity.class);
	    spec = tabHost.newTabSpec("download")
	    			  .setIndicator(getResources().getString(R.string.sync_download), getResources().getDrawable(R.drawable.ic_tab_download))
	                  .setContent(intent);
	    tabHost.addTab(spec);

	    intent = new Intent().setClass(this, FtpSyncparamActivity.class);
	    spec = tabHost.newTabSpec("param")
	    		      .setIndicator(getResources().getString(R.string.sync_param), getResources().getDrawable(R.drawable.ic_tab_param))
	                  .setContent(intent);
	    tabHost.addTab(spec);

	    tabHost.setCurrentTab(0);
	}
	
    /**
     * Method that starts if system trying to destroy activity
     */
    @Override
    protected void onDestroy() 
    {
    	super.onDestroy();
    	
	    SharedPreferences prefs;
	    prefs = getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);
	    
	    prefs.edit().putBoolean(RsaDb.ACTIVESYNCKEY, false).commit();
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {        
        super.onConfigurationChanged(newConfig);
    }
	
}
