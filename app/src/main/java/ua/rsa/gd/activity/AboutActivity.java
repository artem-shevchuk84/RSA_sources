package ua.rsa.gd.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import ua.rsa.gd.R;
import ua.rsa.gd.RsaDb;

/**
 * About Activity gives info about Application and
 * License
 * @author Komarev Roman
 * Odessa, neo3da@mail.ru, +380503412392
 */
public class AboutActivity extends Activity
{
	
	/** Current theme */
	private boolean lightTheme;
	private static int verOS = 0;
	/**
	 * Creates the activity
	 * @param savedInstanceState previous saved state of activity
	 */	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		
		verOS = 2;
		try
		{
			verOS = Integer.parseInt(Build.VERSION.RELEASE.substring(0,1));
		}
		catch (Exception e) {};
		
		lightTheme = getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE).getBoolean(RsaDb.LIGHTTHEMEKEY, false);
		if (lightTheme)
		{
			setTheme(R.style.Theme_Custom);
			super.onCreate(savedInstanceState);
			setContentView(R.layout.l_about);
		}
		else
		{
			setTheme(R.style.Theme_CustomBlack2);
			super.onCreate(savedInstanceState);
			setContentView(R.layout.about);
		}
		
	}
	
	/**
	 * Method that starts every time when Activity is shown on display
	 */
	@Override
	public void onStart()
	{
		super.onStart();
		
	    // Button when user wants to go back to orders list, by pressing
        // it current activity going down, and previous activity
        // starts (RSAActivity)
        Button btnBack = (Button)findViewById(R.id.about_pbtn_prev);
        
        // Listener for OK-button click, calls when OK-button clicked
        btnBack.setOnClickListener(new OnClickListener() 
        {
			@Override
			public void onClick(View v) {
		    	
		    	finish();
				
			}				
		});
		
	}
	
}
