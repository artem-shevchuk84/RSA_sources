package ru.by.rsa;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.ExecutionException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.UserTokenHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

public class CheckVersionThread extends Thread
{
	private static final String HTTP_GETVER_PATH = "http://web-control.appspot.com/getver?imei=rsa_";
	final static int STATE_START   = 0;
	final static int STATE_FINISH  = 1;
	final static int STATE_RUNNING = 2;
	
	int mState;
	SharedPreferences _pref;
	String _imei;
	String _imei_2;
	SQLiteDatabase db_orders;
	SQLiteDatabase db_plan;
	boolean useGPS;
	boolean _sendLines;
	boolean _sendRest;
	
	public CheckVersionThread(String imei, String imei_2, SharedPreferences pref, SQLiteDatabase db, RsaDbHelper mDb, boolean gps, boolean sendLines, boolean sendRest) 
	{
		_imei  		= imei;
		_imei_2		= (imei_2==null)?"":imei_2;
		_pref  		= pref;
		db_orders 	= db;
		db_plan		= mDb.getReadableDatabase();
		mState = STATE_START;
		useGPS = gps;
		_sendLines = sendLines;
		_sendRest = sendRest;
	}
	
	public void setState(int state)
	{
		mState = state;
	}
	
	public void run()
	{
		doCheck();
		
		if (useGPS == true) {
			Calendar cc = Calendar.getInstance();
			DateFormat cc_fmt = new SimpleDateFormat("yyyy-MM-dd");
			String cc_date = null;
			try {
				for (int i=0;i<3;i++) {
					cc_date = cc_fmt.format(cc.getTime());
					doSendCoords(cc_date);
					cc.add(Calendar.DATE, -1);
				}
			} catch (Exception e) {
				writeErrLog("\n>>>unknown error", e.getMessage());
			}
		}
	}
	
	private synchronized void doSendCoords(String send_time){
		Log.i("ROMKA",">>> doSendCoords()");
		CoordinateJSONStorage 	jsonCoords = new CoordinateJSONStorage();
		LinesJSONStorage 		jsonLines = new LinesJSONStorage();
		PlanJSONStorage			jsonPlan  = new PlanJSONStorage();
		RestJSONStorage			jsonRest = new RestJSONStorage();
		String 					linesEntity = ""; // if sending lines then it must be setup
		String 					planEntity = "";  // if sending lines then it must be setup
		String 					restEntity = "";
		String query =  "select SDATE, TIME, GPSCOORD, HSUMO, CUST_TEXT, SHOP_TEXT, HWEIGHT, CUST_ID, SHOP_ID, NUMFULL, _id, NUM1C from _head " +
						"where (MONITORED<>1) " +
					//	"and (GPSCOORD<>'1') " +
					//	"and (GPSCOORD<>'0') " +
					//	"and (NUM1C = '' OR NUM1C = '0') " +
						"and (SDATE = '"+send_time+"') " +  
						"order by SDATE desc";
		
		Cursor curOrders = db_orders.rawQuery(query, null);
		int count = curOrders.getCount();
		Log.i("ROMKA",">>> OrdersCount="+Integer.toString(count));
		if (count < 1) {
			return;
		}
		curOrders.moveToFirst();
		long _id = 0;
		long _lat = 0;
		long _lon = 0;
		int _cash = 0;
		String _name = "";
		String[] strCoord = null;
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		DateFormat idDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		
		String	_adres = "Undef";
		int	 	_weight = 0; 
		String	_cust_id = "Undef";
		String	_shop_id = "Undef";
		String	_numful	 = "Undef";
		boolean isReturn = false; // это возврат?
		
		java.util.Date _date = null;
		
			for (int i=0;i<count;i++) {
				try {
					if (	(curOrders.getString(11).length()<1) || curOrders.getString(11).equals("0")  )
						isReturn = false;
					else 
						isReturn = true;
				} catch (Exception e) {
					isReturn = false;
				}
				try {
					if (curOrders.getString(2).equals("0")) {
						_lat = 0;
						_lon = 0;
					} else if (curOrders.getString(2).equals("1")) {
						_lat = 1;
						_lon = 1;
					} else {
						strCoord = curOrders.getString(2).split(" ");
						_lat = Long.parseLong(strCoord[0]);
						_lon = Long.parseLong(strCoord[1]);
					}
					_date = dateFormat.parse(curOrders.getString(0)+" "+curOrders.getString(1)+":00");
					_id = Long.parseLong(idDateFormat.format(_date));
					_cash = (int)(Float.parseFloat(curOrders.getString(3))*100);
					_cash = isReturn?_cash*(-1):_cash;
					_name = curOrders.getString(4);
					
					_adres = curOrders.getString(5);
					_weight = (int)curOrders.getFloat(6); 
					_cust_id = curOrders.getString(7);
					_shop_id = curOrders.getString(8);
					_numful	 = _imei+"i"+curOrders.getString(10)+curOrders.getString(9);
					
					if (_cust_id==null || _cust_id.equals(""))
						continue;
					if (_name==null || _name.equals(""))
						continue;
					
					jsonCoords.addCoordinates(_id, _lat, _lon, _date.getTime(), _cash, _name, _adres, _weight, _cust_id, _shop_id, _numful);
				} catch (Exception e) {}
				curOrders.moveToNext();
			}
		
		if (curOrders != null) {
			curOrders.close();
		}
		
		/////////////// Lines///////////////////////////	
		//if(_sendLines == true) {
		if(true) {
			Log.i("ROMKA",">>> sendLines=true");
			query =  "select _lines.GOODS_ID, _lines.TEXT_GOODS, _lines.QTY, _lines.UN, " +
					 "_lines.PRICEWNDS, _lines.PRICEWONDS, _lines.SUMWNDS, _lines.SUMWONDS, _head.NUMFULL, _lines.ZAKAZ_ID, _lines.WEIGHT, _head.NUM1C " +
					 "from _lines " +
					 "inner join _head on _lines.ZAKAZ_ID = _head._id " +
					 "where _lines.ZAKAZ_ID in ( " +
					 "select _head._id from _head " +
					 "where (_head.MONITORED<>1) " +
					//	"and (_head.GPSCOORD<>'1') " +
					//	"and (_head.GPSCOORD<>'0') " +
						"and (_head.SDATE = '"+send_time+"') " +
					//	"and (_head.NUM1C = '' OR _head.NUM1C = '0') " +
					 ") " +
					 "GROUP BY _head.NUMFULL, _lines.GOODS_ID";
			Cursor curLines = null;
			curLines = db_orders.rawQuery(query, null);
			
			count = curLines.getCount();
			Log.i("ROMKA",">>> LinesCount="+Integer.toString(count));
			if (count > 0) {
				curLines.moveToFirst();
				
				String		lns_numful			= "Undef";
				String		lns_goods_id		= "Undef";
				String		lns_goods_name		= "Undef";
				int			lns_qty				= 0;
				String		lns_packtype		= "Undef";
				int			lns_pricewnds		= 0;
				int			lns_pricewonds		= 0;
				int			lns_sumwnds			= 0; 
				int			lns_sumwonds		= 0;
				int			lns_weight			= 0;
				
				for (int i=0;i<count;i++) {
					try {
						if (	(curLines.getString(11).length()<1) || curLines.getString(11).equals("0")  )
							isReturn = false;
						else 
							isReturn = true;
					} catch (Exception e) {
						isReturn = false;
					}
					try {
						lns_numful			= _imei+"i"+ curLines.getString(9)+curLines.getString(8);
						lns_goods_id		= curLines.getString(0);
						lns_goods_name		= curLines.getString(1);
						lns_qty				= curLines.getInt(2);
						lns_qty				= isReturn?lns_qty*(-1):lns_qty;
						lns_packtype		= curLines.getString(3);
						lns_pricewnds		= (int)(Float.parseFloat(curLines.getString(4))*100);
						lns_pricewonds		= (int)(Float.parseFloat(curLines.getString(5))*100);
						lns_sumwnds			= (int)(Float.parseFloat(curLines.getString(6))*100); 
						lns_sumwonds		= (int)(Float.parseFloat(curLines.getString(7))*100);
						lns_weight			= (int)(Float.parseFloat(curLines.getString(10)));
						
						if (lns_goods_id==null || lns_goods_id.equals(""))
							continue;
						if (lns_goods_name==null || lns_goods_name.equals(""))
							continue;
						
						jsonLines.addLine(lns_numful, lns_goods_id, lns_goods_name, lns_qty, lns_packtype, lns_pricewnds, lns_pricewonds, lns_sumwnds, lns_sumwonds, lns_weight);
					} catch (Exception e) {
					}
					curLines.moveToNext();
				}
				
			} else {
				_sendLines = false;	
			}
			if (curLines != null) {
				curLines.close();
			}
		}
		//////////////End of lines	
        //////////////Plan///////////////////////////
		if(_sendLines == true) {
			DateFormat dateFormatPlan 		= new SimpleDateFormat("ddMMyyyy");
			DateFormat dateFormatPlanToSend = new SimpleDateFormat("ddMMyyyy  HH:mm:ss");
			Calendar c 			= Calendar.getInstance();
			String d_current 	= dateFormatPlan.format(c.getTime());
			c.add(Calendar.DATE, -1);
			String d_before 	= dateFormatPlan.format(c.getTime());
			c.add(Calendar.DATE, 2);
			String d_after 		= dateFormatPlan.format(c.getTime());
			query = "select CUST_ID, SHOP_ID, CUST_TEXT, SHOP_TEXT, DATEV from _plan " +
					"where DATEV='"+d_before+"' OR DATEV='"+d_current+"' OR DATEV='"+d_after+"'";
			Cursor curPlan = null;
			curPlan = db_plan.rawQuery(query, null);
			int plan_count = curPlan.getCount();
			if (plan_count>0) {
				curPlan.moveToFirst();
				for (int i=0;i<plan_count;i++) {
					try {
						String cust_id 		= curPlan.getString(0);
						String shop_id 		= curPlan.getString(1);
						String cust_text 	= curPlan.getString(2);
						String shop_text 	= curPlan.getString(3);
						java.util.Date date	= dateFormatPlanToSend.parse(curPlan.getString(4) + " 00:00:00");
						
						if (cust_id==null || shop_id==null || cust_text==null || shop_text==null || date==null)
							continue;
						if (cust_id.equals("") || shop_id.equals("") || cust_text.equals(""))
							continue;
						
						jsonPlan.addLine(cust_id, shop_id, cust_text, shop_text, date.getTime());
					} catch (Exception ddd) {}
					curPlan.moveToNext();
				}
			}
			try {
				if (curPlan != null) {
					curPlan.close();
				}
			} catch (Exception eee) {}
		}
        ////////////End of plan	
		////////////Rests//////////////
		if(_sendRest == true) {
			query = "select ID, NAME, REST from _goods";
			Cursor curRest = null;
			curRest = db_plan.rawQuery(query, null);
			int rest_count = curRest.getCount();
			if (rest_count>0) {
				curRest.moveToFirst();
				for (int i=0;i<rest_count;i++) {
					try {
						String goods_id 		= curRest.getString(0);
						String goods_name 		= curRest.getString(1);
						int    rest		 		= curRest.getInt(2);
						
						if (goods_id==null || goods_name==null)
							continue;
						if (goods_id.equals("") || goods_name.equals(""))
							continue;
						
						jsonRest.addLine(goods_id, goods_name, "1", "1", rest);
					} catch (Exception ddd) {}
					curRest.moveToNext();
				}
				//jsonRest.addLine(goods_id, goods_name, sklad_id, sklad_name, rest);
			}
			try {
				if (curRest != null) {
					curRest.close();
				}
			} catch (Exception ez) {}
		}
		////////////End of rests
		try {
			if (db_plan != null) {
				db_plan.close();
			}
		} catch(Exception ed) {}
		
		DefaultHttpClient hc=new DefaultHttpClient();
		ResponseHandler <String> res=new BasicResponseHandler();
		String strPost;
		if (_imei_2!=null && _imei_2.length()>4) {
			strPost = "http://web-control.appspot.com/getver?imei=" + _imei + "i" + _imei_2;
		} else {
			strPost = "http://web-control.appspot.com/getver?imei=" + _imei;
		}
		
		//writeErrLog("\n>>>TESTTEST>", strPost);
		
		
		HttpPost postMethod = new HttpPost(strPost);
		postMethod.addHeader("accept", "application/json");
		postMethod.addHeader("content-type", "application/x-www-form-urlencoded; charset=utf-8");
		String response = "error";
		
		
		linesEntity = "&lines=" + jsonLines.toString().replaceAll("%", "o/o");

		planEntity = "&plan=" + jsonPlan.toString();
		
		restEntity = "&rest=" + jsonRest.toString();
		
		try {
			postMethod.setEntity(new StringEntity("coords="+jsonCoords.toString()+linesEntity+planEntity+restEntity,"UTF-8"));
		} catch (Exception e) {
			response = "post error";
		} 
		
		try {
			Log.i("ROMKA","0");
			response = hc.execute(postMethod, res);
			Log.i("ROMKA","1");
			JSONObject jsonAnsw = new JSONObject(response);
			Log.i("ROMKA","2");
			String answer = jsonAnsw.getString("res");
			Log.i("ROMKA","3");
			String stmt = "(MONITORED<>1) AND (SDATE='"+send_time+"')";
			Log.i("ROMKA","4");
			if (answer.equals("0") == true) {
				Log.i("ROMKA","5");
				ContentValues values = new ContentValues();
				Log.i("ROMKA","6");
				values.put(RsaDbHelper.HEAD_MONITORED,	1);
				Log.i("ROMKA","7");
				db_orders.update(RsaDbHelper.TABLE_HEAD, values, stmt, null);
				Log.i("ROMKA","8");
			}
		} catch (Exception e) {
			response = "execute error";
			//writeErrLog("\n>>>execute error HEAD", jsonCoords.toString());
			//writeErrLog("\n>>>execute error LINES", linesEntity);
			//writeErrLog("\n>>>execute error Plan", planEntity);
			if (response!=null) writeErrLog("\n>>>execute error Response", response);
			//writeErrLog("\n>>>execute error", e.getMessage());
			//writeErrLog("\n>>>execute error", e.getMessage());
		}
		//Log.i("ROMKA",">>>rrr" + jsonCoords.toString());
		//Log.i("ROMKA",">>>" + jsonLines.toString());
		//Log.i("ROMKA",">>>" + linesEntity);
		//writeErrLog("\n>>>", jsonCoords.toString());
		//writeErrLog("\n>>>", jsonLines.toString());
		//Log.i("ROMKA",">>>" + response);
	}
	
	
	private synchronized boolean doCheck()
	{
		try 
		{
			JSONObject jsonObject = new JSONObject(readVersionFromSite());
			String siteVersion = jsonObject.getString("rsa");
			String strLicensed = jsonObject.getString("lic");
			
			boolean bLicensed = true;
			if (strLicensed.equals("1")) {
				bLicensed = true;
			} else if (strLicensed.equals("0")) {
				bLicensed = false;
			}
			
			Float.parseFloat(siteVersion);
			//Log.i("ROMKA", ">>>" + siteVersion);
			_pref.edit().putString(RsaDb.MARKETVERSION, siteVersion).commit();
			_pref.edit().putBoolean(RsaDb.LICENSED, bLicensed).commit();
			return true;
		} 
		catch (Exception e) 
		{
			Log.e("ROMKA", "JSON Exception");
		}
		return false;
	}
	
	/**
     * Method for check site version
     */
    public String readVersionFromSite() 
    {
        StringBuilder builder = new StringBuilder();
        HttpClient client = new DefaultHttpClient();
        HttpGet httpGet;
        if (_imei_2!=null && _imei_2.length()>4) {
        	httpGet = new HttpGet(HTTP_GETVER_PATH + _imei + "i" + _imei_2);
        	//writeErrLog("\n>>>TESTTEST2>", HTTP_GETVER_PATH + _imei + "i" + _imei_2);
        } else {
        	httpGet = new HttpGet(HTTP_GETVER_PATH + _imei);
        	//writeErrLog("\n>>>TESTTEST2>", HTTP_GETVER_PATH + _imei);
        }
        
        
        
        
        try 
        {
	          HttpResponse response = client.execute(httpGet);
	          StatusLine statusLine = response.getStatusLine();
	          int statusCode = statusLine.getStatusCode();
	          if (statusCode == 200) 
	          {
		            HttpEntity entity = response.getEntity();
		            InputStream content = entity.getContent();
		            BufferedReader reader = new BufferedReader(new InputStreamReader(content));
		            String line;
		            
		            while ((line = reader.readLine()) != null) 
		            {
		            	builder.append(line);
		            }
	          }
        } 
        catch (Exception e) {} 
        
        //Log.i("readFromSite:", builder.toString());
        return builder.toString();
    }
    
    @SuppressLint("SimpleDateFormat")
	private static void writeErrLog(String mess, String ex) {
		try {
			String path = Environment.getExternalStorageDirectory().toString() + File.separator + "rsa";
			path = path + File.separator + "error_log.txt";
	    	// get current time
	    	Calendar c = Calendar.getInstance();
	    	SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	    	// init error msg
	    	StringBuilder msgError = new StringBuilder("\r\n---"+ fmt.format(c.getTime()) +"----------------------\r\n");
	    	msgError.append(mess);
	    	msgError.append("\r\nException text:\r\n");
	    	msgError.append(ex + "\r\n");
	    	
	    	BufferedWriter out = new BufferedWriter(new FileWriter(path, true));
	    	out.write(msgError.toString());
	    	out.close();
    	} catch (Exception e) {}
    }
    
    private static void test() {
    	Log.i("ROMKA",">>>1");
    	ResponseHandler <String> res=new BasicResponseHandler();
    	Log.i("ROMKA",">>>2");
		DefaultHttpClient hc=new DefaultHttpClient();
		Log.i("ROMKA",">>>3");
		String strPost = "http://web-control.appspot.com/teststring?imei=1";
		HttpPost postMethod = new HttpPost(strPost);
		Log.i("ROMKA",">>>4");
		postMethod.addHeader("accept", "application/json");
		postMethod.addHeader("content-type", "application/x-www-form-urlencoded; charset=utf-8");
		Log.i("ROMKA",">>>5");
		String response = "";
		String mess = "текст";
		try {
			Log.i("ROMKA",">>>6");
			//postMethod.setEntity(new StringEntity("a="+mess,"UTF-8"));
			Log.i("ROMKA",">>>7");
			Log.i("ROMKA",">>>req="+postMethod.getURI());
			response = hc.execute(postMethod, res);
			Log.i("ROMKA",">>>8");
			Log.i("ROMKA",">>>"+ response.toString());
		} catch (Exception e) {
			Log.i("ROMKA",">>>9");
			e.printStackTrace();
		}
		Log.i("ROMKA",">>>10");
    }
    
	
}
