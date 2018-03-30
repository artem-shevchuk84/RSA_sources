package ru.by.rsa;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class UpgradingThread extends Thread
{
	Handler mHandler;
	final static int STATE_DONE = 0;
	final static int STATE_RUNNING = 1;
	int mState;
	Context mContext;
	
	public UpgradingThread(Handler hnd, Context cntx) 
	{
		mHandler = hnd;
		mContext = cntx;
	}
	
	public void setState(int state)
	{
		mState = state;
	}
	
	public void run()
	{
		mState = STATE_RUNNING;
		
		if(mState == STATE_RUNNING)
		{
			doUpdate();
			Message msg = mHandler.obtainMessage();
			Bundle b = new Bundle();
			b.putInt("Total", 100);
			msg.setData(b);
			mHandler.sendMessage(msg);
		}
		
	}
	
	private void doUpdate()
	{
		try
		{
			URL url = new URL("https://web-control.appspot.com/rsa.apk");
			// URL url = new URL("https://web-control.appspot.com/rsal.apk");
			//URL url = new URL("http://rsa.16mb.com/files/rsa.apk");
	        HttpURLConnection c = (HttpURLConnection) url.openConnection();
	        c.setRequestMethod("GET");
	        c.setDoOutput(true);
	        c.connect();
	
	        File file = mContext.getExternalFilesDir("download");
	        File outputFile = new File(file, "rsa.apk");
	        FileOutputStream fos = new FileOutputStream(outputFile);
	        InputStream is = c.getInputStream();
	
	        byte[] buffer = new byte[1024];
	        int len1 = 0;
	        while ((len1 = is.read(buffer)) != -1) 
	        {
	            fos.write(buffer, 0, len1);
	        }
	        fos.close();
	        is.close();
	        
	        Intent intent = new Intent(Intent.ACTION_VIEW);
	        intent.setDataAndType(Uri.fromFile(outputFile),
	                "application/vnd.android.package-archive");
	        mContext.startActivity(intent);
		}
		catch(Exception e)
		{
			Log.e("ROMKA","updating failed");
		}
	}
	
}
