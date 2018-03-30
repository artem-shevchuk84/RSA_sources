package ua.rsa.gd;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import ru.by.rsa.R;

/**
 * Activity for setting up Email-transport parameters
 * and save them in SharedPreferences of app
 * @author Komarev Roman
 * Odessa, neo3da@mail.ru, +380503412392
 */
public class FtpSyncparamActivity extends Activity {

	SharedPreferences def_pref;

	private TextWatcher mWatcher = new TextWatcher() {
		@Override
		public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

		}

		@Override
		public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

		}

		@Override
		public void afterTextChanged(Editable editable) {
			if (def_pref.getBoolean("encoded", false)) {
				def_pref.edit().putBoolean("encoded", false).commit();
				Toast.makeText(getApplicationContext(), "Вы отключили шифрование",
						Toast.LENGTH_SHORT).show();
			}
		}
	};

    public void onCreate(Bundle savedInstanceState) {
    	
    	boolean lightTheme = getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE).getBoolean(RsaDb.LIGHTTHEMEKEY, false);

    	if (lightTheme) {
			setTheme(R.style.Theme_Custom);
		} else {
			setTheme(R.style.Theme_CustomBlack2);
		}
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ftpsendprm);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		def_pref = PreferenceManager.getDefaultSharedPreferences(this);

        /** Bind Applay button on display with variable */
        Button btnApplay 	 = (Button)findViewById(R.id.btnApply_ftpSendprm);
		EditText edtLogin 	 = (EditText)findViewById(R.id.edtLogin_ftpSendprm);
		EditText edtPass 	 = (EditText)findViewById(R.id.edtPass_ftpSendprm);
		EditText edtServer	 = (EditText)findViewById(R.id.edtServer_ftpSendprm);
		EditText edtPort	 = (EditText)findViewById(R.id.edtPort_ftpSendprm);
		EditText edtInbox 	 = (EditText)findViewById(R.id.edtInbox_ftpSendprm);
		EditText edtOutbox   = (EditText)findViewById(R.id.edtOutbox_ftpSendprm);
        
	    /** Get Shared Preferences and fill EditText boxes with it */
		SharedPreferences prefs = getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);
	
		edtLogin.setText(prefs.getString(RsaDb.FTPUSER, 		"")  ); 		
		edtPass.setText(prefs.getString(RsaDb.FTPPASSWORD, 		"")  ); 	
		edtServer.setText(prefs.getString(RsaDb.FTPSERVER, 		"")  ); 		
		edtPort.setText(prefs.getString(RsaDb.FTPPORT, 			"")  ); 	
		edtInbox.setText(prefs.getString(RsaDb.FTPINBOX, 		"")  ); 		
		edtOutbox.setText(prefs.getString(RsaDb.FTPOUTBOX, "")  );

		edtLogin.addTextChangedListener(mWatcher);
		edtPass.addTextChangedListener(mWatcher);

        // Set Listener when button click performed
        btnApplay.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0)
			{
				EditText edtLogin 	 = (EditText)findViewById(R.id.edtLogin_ftpSendprm);
				EditText edtPass 	 = (EditText)findViewById(R.id.edtPass_ftpSendprm);
				EditText edtServer	 = (EditText)findViewById(R.id.edtServer_ftpSendprm);
				EditText edtPort	 = (EditText)findViewById(R.id.edtPort_ftpSendprm);
				EditText edtInbox 	 = (EditText)findViewById(R.id.edtInbox_ftpSendprm);
				EditText edtOutbox   = (EditText)findViewById(R.id.edtOutbox_ftpSendprm);

				String login = edtLogin.getText().toString().replace(" ", "");
				edtServer.setText(edtServer.getText().toString().replace(" ", ""));
				
				String port = edtPort.getText().toString().replace(" ", "");
				if (port.length()<1) {
					port = "21";
				}
				edtPort.setText(port);
				
			    /** Set Shared Preferences with data from EditText boxes*/
				SharedPreferences prefs = getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);
				prefs.edit().putString(RsaDb.FTPUSER, 		login).commit();
				prefs.edit().putString(RsaDb.FTPPASSWORD, 	edtPass.getText().toString()).commit();
				prefs.edit().putString(RsaDb.FTPSERVER,		edtServer.getText().toString()).commit();
				prefs.edit().putString(RsaDb.FTPPORT, 		edtPort.getText().toString()).commit();
				prefs.edit().putString(RsaDb.FTPINBOX, 		edtInbox.getText().toString()).commit();
				prefs.edit().putString(RsaDb.FTPOUTBOX, 	edtOutbox.getText().toString()).commit();

				Toast.makeText(getApplicationContext(),R.string.sendprm_saved,Toast.LENGTH_SHORT).show(); 				
			}});
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
    
	/**
	 * If Back-button on device pressed then do...
	 */
	@Override
	public void onBackPressed()
	{
	    /** Get Shared Preferences and fill special field of message with it */
		SharedPreferences prefs;
		
		prefs = getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);
		if (prefs.getBoolean(RsaDb.ACTIVESYNCKEY, true)) 
		{
			Toast.makeText(getApplicationContext(),R.string.sendprm_wait,Toast.LENGTH_SHORT).show(); 
			return;
		}
		
    	finish();
	}
	
    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {        
        super.onConfigurationChanged(newConfig);
    }
}
