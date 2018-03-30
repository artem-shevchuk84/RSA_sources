package ua.rsa.gd;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import ru.by.rsa.R;

/**
 * Activity for setting up Email-transport parameters
 * and save them in SharedPreferences of app
 * @author Komarev Roman
 * Odessa, neo3da@mail.ru, +380503412392
 */
public class SyncparamActivity extends Activity
{
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sendprm);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        
        /** Bind Applay button on display with variable */
        Button btnApplay 	 = (Button)findViewById(R.id.btnApply_Sendprm);
		EditText edtUser 	 = (EditText)findViewById(R.id.edtUser_Sendprm);
		EditText edtPass 	 = (EditText)findViewById(R.id.edtPass_Sendprm);
		EditText edtSMTP	 = (EditText)findViewById(R.id.edtSMTP_Sendprm);
		EditText edtSMTPport = (EditText)findViewById(R.id.edtSMTPport_Sendprm);
		EditText edtPOP 	 = (EditText)findViewById(R.id.edtPOP_Sendprm);
		EditText edtPOPport  = (EditText)findViewById(R.id.edtPOPport_Sendprm);   
		EditText edtTo   	 = (EditText)findViewById(R.id.edtSendTo_Sendprm);
		CheckBox chkSSL		 = (CheckBox)findViewById(R.id.isSSL);
        
	    /** Get Shared Preferences and fill EditText boxes with it */
		SharedPreferences prefs = getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);
	
		edtUser.setText(prefs.getString(RsaDb.EMAILKEY, 		"")  ); 		
		edtPass.setText(prefs.getString(RsaDb.PASSWORDKEY, 		"")  ); 	
		edtSMTP.setText(prefs.getString(RsaDb.SMTPKEY, 			"")  ); 		
		edtSMTPport.setText(prefs.getString(RsaDb.SMTPPORTKEY, 	"")  ); 	
		edtPOP.setText(prefs.getString(RsaDb.POPKEY, 			"")  ); 		
		edtPOPport.setText(prefs.getString(RsaDb.POPPORTKEY, 	"")  ); 	
		edtTo.setText(prefs.getString(RsaDb.SENDTOKEY, 			"")  ); 
		chkSSL.setChecked(prefs.getBoolean(RsaDb.USESSL, 		false));
        
        // Set Listener when button click performed
        btnApplay.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0)
			{
				EditText edtUser 	 = (EditText)findViewById(R.id.edtUser_Sendprm);
				EditText edtPass 	 = (EditText)findViewById(R.id.edtPass_Sendprm);
				EditText edtSMTP	 = (EditText)findViewById(R.id.edtSMTP_Sendprm);
				EditText edtSMTPport = (EditText)findViewById(R.id.edtSMTPport_Sendprm);
				EditText edtPOP 	 = (EditText)findViewById(R.id.edtPOP_Sendprm);
				EditText edtPOPport  = (EditText)findViewById(R.id.edtPOPport_Sendprm);
				EditText edtTo   	 = (EditText)findViewById(R.id.edtSendTo_Sendprm);
				CheckBox chkSSL		 = (CheckBox)findViewById(R.id.isSSL);
				
				edtUser.setText(edtUser.getText().toString().replace(" ", ""));		
				edtSMTP.setText(edtSMTP.getText().toString().replace(" ", ""));
				edtSMTPport.setText(edtSMTPport.getText().toString().replace(" ", ""));
				edtPOP.setText(edtPOP.getText().toString().replace(" ", ""));
				edtPOPport.setText(edtPOPport.getText().toString().replace(" ", ""));
				edtTo.setText(edtTo.getText().toString().replace(" ", ""));
				
				
			    /** Set Shared Preferences with data from EditText boxes*/
				SharedPreferences prefs = getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);
				prefs.edit().putString(RsaDb.EMAILKEY, 		edtUser.getText().toString()).commit();
				prefs.edit().putString(RsaDb.PASSWORDKEY, 	edtPass.getText().toString()).commit();
				prefs.edit().putString(RsaDb.SMTPKEY, 		edtSMTP.getText().toString()).commit();
				prefs.edit().putString(RsaDb.SMTPPORTKEY, 	edtSMTPport.getText().toString()).commit();
				prefs.edit().putString(RsaDb.POPKEY, 		edtPOP.getText().toString()).commit();
				prefs.edit().putString(RsaDb.POPPORTKEY, 	edtPOPport.getText().toString()).commit();
				prefs.edit().putString(RsaDb.SENDTOKEY, 	edtTo.getText().toString()).commit();
				
				prefs.edit().putBoolean(RsaDb.USESSL, 	    chkSSL.isChecked()).commit();
				
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
