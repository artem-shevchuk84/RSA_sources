package ua.rsa.gd;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;


import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import ru.by.rsa.R;
import ua.rsa.gd.external.javadbf.DBFReader;
import ua.rsa.gd.external.javadbf.DBFWriter;
import ua.rsa.gd.external.javadbf.JDBFException;
import ua.rsa.gd.external.javadbf.JDBField;
import ua.rsa.gd.external.opencsv.CSVReader;
import ua.rsa.gd.org.apache.commons.net.ftp.FTPFile;
import ua.rsa.gd.utils.DataUtils;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Xml;
import android.view.Display;

/**
 * Some staff to work with this project (RSAActivity)
 * Includes:
 * 1. Methods for working with lzma archiver
 * 2. Methods for xml-parsing
 * 3. Classes for making special class holder of current order in program (mOrderH)
 *
 * @author Komarev Roman
 * Odessa, neo3da@mail.ru, +380503412392
 */
public class RsaDb
{

	private static int verOS = 0;

	public static final int DBF_CUST 	= 10;
	public static final int DBF_BRAND 	= 11;
	public static final int DBF_CHAR 	= 12;
	public static final int DBF_DEBIT 	= 13;
	public static final int DBF_GOODS 	= 14;
	public static final int DBF_GROUP 	= 15;
	public static final int DBF_SHOP 	= 16;
	public static final int DBF_SKLAD 	= 17;
	public static final int DBF_HEAD 	= 18;
	public static final int DBF_LINES 	= 19;
	public static final int DBF_WORKINF	= 20;
	public static final int DBF_PLAN 	= 21;
	public static final int DBF_PRODLOCK 	= 22;
	public static final int DBF_SKLADDET 	= 24;
	public static final String PREFS_NAME 		= "ru.by.rsa";
	public static final String USESSL 			= "ru.by.rsa.usessl";
	public static final String EMAILKEY 		= "ru.by.rsa.email";
	public static final String PASSWORDKEY 		= "ru.by.rsa.password";
	public static final String SMTPKEY 			= "ru.by.rsa.smtp";
	public static final String SMTPPORTKEY		= "ru.by.rsa.smtpport";
	public static final String POPKEY 			= "ru.by.rsa.pop";
	public static final String POPPORTKEY		= "ru.by.rsa.popport";
	public static final String SENDTOKEY		= "ru.by.rsa.sendto";
	public static final String FTPSERVER 		= "ru.by.rsa.ftpserver";
	public static final String FTPUSER	 		= "ru.by.rsa.ftpuser";
	public static final String FTPPASSWORD 		= "ru.by.rsa.ftppassword";
	public static final String FTPPORT	 		= "ru.by.rsa.ftpport";
	public static final String FTPINBOX 		= "ru.by.rsa.ftpinbox";
	public static final String FTPOUTBOX 		= "ru.by.rsa.ftpoutbox";
	public static final String ACTUALDBKEY		= "ru.by.rsa.actualdb";
	public static final String LASTDWNLDKEY		= "ru.by.rsa.lastdwnldkey";
	public static final String FTPLASTDWNLDKEY	= "ru.by.rsa.ftplastdwnldkey";
	public static final String LASTSENDKEY		= "ru.by.rsa.lastsendkey";
	public static final String FTPLASTSENDKEY	= "ru.by.rsa.ftplastsendkey";
	public static final String ACTIVESYNCKEY	= "ru.by.rsa.activesynckey";
	public static final String LASTUPDATECHECK  = "ru.by.rsa.lastupdatecheck";
	public static final String MARKETVERSION  	= "ru.by.rsa.marketversion";
	public static final String PRICESELECTED  	= "ru.by.rsa.priceselected";
	public static final String LASTCSVSENDDAY	= "ru.by.rsa.lastcsvsendday";
	public static final String CSVPORTION		= "ru.by.rsa.csvportion";
	public static final String BRANDGROUPSHOW	= "ru.by.rsa.brandgroupshow";
	public static final String LICENSED			= "ru.by.rsa.licensed";
	public static final String MAXPRICETYPE		= "ru.by.rsa.maxpricetype";
	public static final String SENDLINES		= "ru.by.rsa.sendlines";

	// Preferences screen
	public static final String PREFS_NAME_MAIN	= "ru.by.rsa_preferences";
	public static final String NAMEKEY			= "prefName";
	public static final String CODEKEY			= "prefCode";
	public static final String IMEIKEY			= "prefSerial";
	public static final String MONITORSERIAL	= "prefMonitorSerial";
	public static final String GPSKEY			= "chkGPS";
	public static final String COORDKEY			= "chkCoord";
	public static final String COORDKONTIKEY	= "chkCoordKONTI";
	public static final String RATEKEY			= "prefRate";
	public static final String DBPREFIXKEY		= "prefDbPrefixKey";
	public static final String LASTSDLOADKEY	= "lastsdloadkey";
	public static final String LASTOPTIMKEY		= "lastoptimkey";
	public static final String INTERFACEKEY		= "prefInterface";
	public static final String PROTOCOLKEY		= "prefProtocol";
	public static final String PRICETYPEKEY		= "prefPricetype";
	public static final String NDS			 	= "prefNDS";
	public static final String SENDRATEKEY	 	= "prefSendRate";
	public static final String HOSTKEY		 	= "prefHost";
	public static final String HOSTPORTKEY	 	= "prefHostPort";
	public static final String START_HOUR_KEY	= "prefStartHour";
	public static final String END_HOUR_KEY		= "prefEndHour";
	public static final String MAKESTARTKEY	 	= "prefMakeStart";
	public static final String LASTSKLADID	 	= "prefLastSkladId";
	public static final String LASTSKLADNAME 	= "prefLastSkladName";
	public static final String LIGHTTHEMEKEY	= "prefLightThemeKey";
	public static final String SHOWRECINLIST	= "prefShowRecInList";
	public static final String SHOWSHOPADDRESS	= "prefShowShopAddress";
	public static final String LONGATTRIBUTES   = "prefLongAttributes";
	public static final String VATRATE			= "prefVATRate";
	public static final String LASTVATKEY		= "prefLastVATStat";
	public static final String ORDERBYKEY		= "prefOrderBy";
	public static final String USINGPLAN		= "prefUsingPlan";
	public static final String ORDERHYST		= "prefOrderHyst";
	public static final String EKVATOR			= "prefIsEkvator";
	public static final String MATRIXKEY		= "prefUseMatrix";

	// save instance preferences
	public static final String PREFS_INSTANCE	= "ru.by.rsa_instance";

	// default preferences
	public static final String USEPACKS			= "prefUsingPacks";


	public static boolean checkScreenSize(Activity activity, double screen_size)
    {
		try {
	        Display display = activity.getWindowManager().getDefaultDisplay();
	        DisplayMetrics displayMetrics = new DisplayMetrics();
	        display.getMetrics(displayMetrics);

	        int width = displayMetrics.widthPixels / displayMetrics.densityDpi;
	        int height = displayMetrics.heightPixels / displayMetrics.densityDpi;

	        double screenDiagonal = Math.sqrt( width * width + height * height );
	        Log.d("ROMKA", Double.toString(screenDiagonal));
	        return (screenDiagonal >= screen_size );
		} catch (Exception e) {
			return false;
		}
    }


	public static String getDImeiOLD(Context context) {
		String tmp_imei = null;
		try {
			tmp_imei = ((TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
		} catch (Exception ei) {tmp_imei= null;}
		try {
			if (tmp_imei == null) {
				String s = android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
				s = s.substring(2, 16);
				long lFakeImei = Long.parseLong(s, 16);
				tmp_imei = Long.toString(lFakeImei);
			}
		} catch (Exception ed) {tmp_imei = "123456789123456";}
		return tmp_imei;
	}

	public static String getDImei(Context context) {
		String imei = null;
        String imei2 = null;
        try {
        	TelephonyInfo telephonyInfo = TelephonyInfo.getInstance(context);
        	imei	= telephonyInfo.getImeiSIM1();
        	imei2	= telephonyInfo.getImeiSIM2();
        	//Log.i("ROMKA", "IMEI1="+imei+"   IMEI2="+(imei2==null?"null":imei2));
        } catch (Exception dd) {
        	imei = RsaDb.getDImeiOLD(context);
        //	imei2 = null;
        }
        if (imei==null || imei.equals(""))
        	imei = RsaDb.getDImeiOLD(context);
		return imei;
	}

	public static class Au
	{
		private String s1;
		private String s2;
		private String s3;
		private String s4;
		private String s5;
		private String s6;
		private String s7;
		private String s8;
		private String s9;
		private String s10;
		private String s11;
		private String s12;
		private String s13;
		private String d1;
		private String d2;

		Au()
		{
			setS1("ht");
			setS2("tp:/");
			setS3("/rs");
			setS4("a.");
			setS5("16m");
			setS6("b.c");
			setS7("om/v");
			setS8("eri");
			setS9("fy.p");
			setS10("hp?");
			setS11("se");
			setS12("ri");
			setS13("al=");
			setD1("777777777");
			setD2("777777");
		}

		public String getS1()
		{
			return s1;
		}

		public void setS1(String s1)
		{
			this.s1 = s1;
		}

		public String getS2()
		{
			return s2;
		}

		public void setS2(String s2)
		{
			this.s2 = s2;
		}

		public String getS3()
		{
			return s3;
		}

		public void setS3(String s3)
		{
			this.s3 = s3;
		}

		public String getS4()
		{
			return s4;
		}

		public void setS4(String s4)
		{
			this.s4 = s4;
		}

		public String getS5()
		{
			return s5;
		}

		public void setS5(String s5)
		{
			this.s5 = s5;
		}

		public String getS6()
		{
			return s6;
		}

		public void setS6(String s6)
		{
			this.s6 = s6;
		}

		public String getS7()
		{
			return s7;
		}

		public void setS7(String s7)
		{
			this.s7 = s7;
		}

		public String getS8()
		{
			return s8;
		}

		public void setS8(String s8)
		{
			this.s8 = s8;
		}

		public String getS9()
		{
			return s9;
		}

		public void setS9(String s9)
		{
			this.s9 = s9;
		}

		public String getS10()
		{
			return s10;
		}

		public void setS10(String s10)
		{
			this.s10 = s10;
		}

		public String getS11()
		{
			return s11;
		}

		public void setS11(String s11)
		{
			this.s11 = s11;
		}

		public String getS12()
		{
			return s12;
		}

		public void setS12(String s12)
		{
			this.s12 = s12;
		}

		public String getS13()
		{
			return s13;
		}

		public void setS13(String s13)
		{
			this.s13 = s13;
		}

		public String getD1()
		{
			return d1;
		}

		public void setD1(String d1)
		{
			this.d1 = d1;
		}

		public String getD2()
		{
			return d2;
		}

		public void setD2(String d2)
		{
			this.d2 = d2;
		}
	}

	public static String normalizePrice(String OldString)
	{
		String newString = "0";

		if (OldString==null || OldString.equals("")) {
			return "0";
		} else {
			newString = OldString.replace("_", "");
			newString = newString.replace(",", ".");
		}

		return newString;
	}





	public static String normalizeRest(String OldString)
	{
		String newString = "0";

		if (OldString.equals("")||(OldString.contains("-")))
			return newString;

		newString = OldString.replace("_", "");

		return newString;
	}

	public static String normalizeFlash(String OldString)
	{
		String newString = "0";

		if (OldString.equals(""))
			return newString;

		return OldString;
	}

	public static String normalizeQty(String OldString, String def) {
		String newString = def;

		if (OldString==null||OldString.equals("")||OldString.equals("0"))
			return newString;

		return OldString;
	}

	public static String normalizeCoeff(String OldString)
	{
		String newString = "1";

		if (OldString.equals("0")||OldString.equals(""))
			return "1";

		if (OldString.equals("grm"))
			return "0.001";

		newString = OldString.replace("_", "");

		return newString;
	}

	public static String normalizematrix(String OldString) {
		String newString = "1";
		if (OldString.length()>0)
			newString = OldString;
		return newString;
	}
	public static String normalizeavg(String OldString) {
		String newString = "1";
		if (OldString.length()>0)
			newString = OldString.replace(",", ".");
		return newString;
	}
	public static String normalizecoef(String OldString) {
		String newString = "0";
		if (OldString.length()>0)
			newString = OldString.replace(",", ".");
		return newString;
	}
	public static String normalizedelivery(String OldString) {
		String newString = OldString;
		SimpleDateFormat fmt = new SimpleDateFormat("ddMMyyyy");
		SimpleDateFormat fmt_new = new SimpleDateFormat("yyyy-MM-dd");
		try {
			newString = fmt_new.format((fmt.parse(OldString)));
		} catch (Exception e) {
			Log.d("LOG","ERROR normalizrdelivery() ERROR");
		}
		return newString;
	}
	public static String normalizeshare(String OldString) {
		String newString = "0";
		if (OldString.length()>0)
			newString = OldString;
		return newString;
	}

	public static String dateCorrection(String OldString) {
		StringBuilder newString = new StringBuilder("20");

		try {
			newString.append(OldString.substring(6,8) + "-");
			newString.append(OldString.substring(3,5) + "-");
			newString.append(OldString.substring(0,2));
		} catch(Exception e) {
			newString = new StringBuilder("2009-01-01");
		};

		return newString.toString();
	}
	/**
	 * Get file from lzma archive
	 * Example of use:
	 *   	try
	 *		{
	 *			RsaDb.fromLzma(getApplicationContext(), "goods.dbf.lzma", "goods.dbf");
  	 *
	 *		} catch (Exception e)
	 *		{
	 *			e.printStackTrace();
	 *		}
	 *
	 * @param context Application context, used for getting correct files path
	 * @param inputFile Input file to decode
	 * @param outputFile Output file (after decoding)
	 * @throws Exception
	 */
	public static void fromLzma(Context context, String inputFile, String outputFile) throws Exception
	{
		// appPath = /data/data/ru.by.rsa/files
		String appPath = context.getFilesDir().getAbsolutePath();

		java.io.File inFile = new java.io.File(appPath + File.separator + inputFile);
		java.io.File outFile = new java.io.File(appPath + File.separator + outputFile);

		java.io.BufferedInputStream inStream  = new java.io.BufferedInputStream(new java.io.FileInputStream(inFile));
		java.io.BufferedOutputStream outStream = new java.io.BufferedOutputStream(new java.io.FileOutputStream(outFile));

		int propertiesSize = 5;
		byte[] properties = new byte[propertiesSize];
		if (inStream.read(properties, 0, propertiesSize) != propertiesSize)
			throw new Exception("input .lzma file is too short");

		ua.rsa.gd.external.sevenzip.compression.lzma.Decoder decoder = new ua.rsa.gd.external.sevenzip.compression.lzma.Decoder();
		if (!decoder.SetDecoderProperties(properties))
			throw new Exception("Incorrect stream properties");
		long outSize = 0;
		for (int i = 0; i < 8; i++)
		{
			int v = inStream.read();
			if (v < 0)
				throw new Exception("Can't read stream size");
			outSize |= ((long)v) << (8 * i);
		}
		if (!decoder.Code(inStream, outStream, outSize))
			throw new Exception("Error in data stream");
		outStream.flush();
		outStream.close();
		inStream.close();
	}

	/**
	 * Unzip files from archive
	 * @param context
	 * @param inputFile
	 * @param outputLocation
	 * @throws Exception
	 */
	public static void fromZip(Context context, String inputFile, String outputLocation) throws IOException
	{
		// appPath = /data/data/ru.by.rsa/files
		String appPath = context.getFilesDir().getAbsolutePath();
		String zipFile = appPath + File.separator + inputFile;
		String unzipLocation = appPath + File.separator + outputLocation + File.separator;

		Decompress d = new Decompress(zipFile, unzipLocation);
		d.unzip();
	}

	/**
	 * Zip files to archive
	 * @param context
	 * @param inputFile
	 * @param outputFile
	 * @throws IOException
	 */
	public static void toZip(Context context, String inputFile, String outputFile, String optionalPath) throws IOException
	{
		// appPath = /data/data/ru.by.rsa/files
		String appPath = context.getFilesDir().getAbsolutePath();
		String zipName = appPath + File.separator + outputFile;
		String[] files = new String[] { appPath + File.separator + inputFile };
		if (optionalPath != null) {
			files = new String[] { optionalPath + File.separator + inputFile };
		}

		Compress c = new Compress(files, zipName);
		c.zip();
	}

	public static void toZipArray(Context context, String inputDir, String outputFile) throws IOException
	{
		// appPath = /data/data/ru.by.rsa/files
		String appPath = context.getFilesDir().getAbsolutePath();
		String zipName = appPath + File.separator + outputFile;
		File outFolder = new File(appPath + File.separator + inputDir);

		FilenameFilter fFilter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				return filename.endsWith(".xls");
			}
		};

		File[] files = outFolder.listFiles(fFilter);
		String[] strFiles = outFolder.list(fFilter);

		if (strFiles.length<1) return;

		for (int i=0;i<strFiles.length;i++) {
			strFiles[i] = outFolder.getPath() + File.separator + strFiles[i];
		}

		Compress c = new Compress(strFiles, zipName);
		c.zip();

		// Delete all *.xls
		for (int j=0;j<files.length;j++) {
			files[j].delete();
		}

	}



	/**
	 * Put file into lzma archive
	 * Example of use:
	 *   	try
	 *		{
	 *			RsaDb.toLzma(getApplicationContext(), "goods.dbf.lzma", "goods.dbf");
  	 *
	 *		} catch (Exception e)
	 *		{
	 *			e.printStackTrace();
	 *		}
	 *
	 * @param context Application context, used for getting correct files path
	 * @param inputFile Input file to encode
	 * @param outputFile Output file (after encoding)
	 * @throws Exception
	 */
	public static void toLzma(Context context, String inputFile, String outputFile) throws Exception
	{
		// appPath = /data/data/ru.by.rsa/files
		String appPath = context.getFilesDir().getAbsolutePath();

		java.io.File inFile = new java.io.File(appPath + File.separator + inputFile);
		java.io.File outFile = new java.io.File(appPath + File.separator + outputFile);

		java.io.BufferedInputStream inStream  = new java.io.BufferedInputStream(new java.io.FileInputStream(inFile));
		java.io.BufferedOutputStream outStream = new java.io.BufferedOutputStream(new java.io.FileOutputStream(outFile));

		ua.rsa.gd.external.sevenzip.compression.lzma.Encoder encoder = new ua.rsa.gd.external.sevenzip.compression.lzma.Encoder();

		encoder.SetAlgorithm(1);
		encoder.SetDictionarySize(4);
		encoder.SetNumFastBytes(128);
		encoder.SetLcLpPb(3, 0, 2);
		encoder.WriteCoderProperties(outStream);
		long fileSize;
			fileSize = inFile.length();
		for (int i = 0; i < 8; i++)
			outStream.write((int)(fileSize >>> (8 * i)) & 0xFF);
		encoder.Code(inStream, outStream, -1, -1, null);
	}

	public static void insertEmptyShop(SQLiteDatabase db, String cust_id, String emptyShop, String emptyAdres)
	{
		ContentValues values = new ContentValues();

		values.put(RsaDbHelper.SHOP_ID,			"");
	    values.put(RsaDbHelper.SHOP_CUST_ID,	cust_id);
		values.put(RsaDbHelper.SHOP_NAME,		emptyShop);
		values.put(RsaDbHelper.SHOP_ADDRESS,	emptyAdres);
		db.insert(RsaDbHelper.TABLE_SHOP, RsaDbHelper.SHOP_NAME, values);
	}


	public static boolean hasNoVATgoods(ArrayList<OrderLines> mOrder)
	{
		if (mOrder.isEmpty()) return false;

		for (OrderLines CurLine : mOrder)
		{
			if (CurLine.get(OrderLines.DELAY).equals("")||CurLine.get(OrderLines.DELAY).equals("0"))
			{
				return true;
			}
		}

		return false;
	}

	public static boolean hasVATgoods(ArrayList<OrderLines> mOrder)
	{
		if (mOrder.isEmpty()) return false;

		for (OrderLines CurLine : mOrder)
		{
			if (!CurLine.get(OrderLines.DELAY).equals("")&&!CurLine.get(OrderLines.DELAY).equals("0"))
			{
				return true;
			}
		}

		return false;
	}


	/**
	 * Fill Tables in database with data from DBF
	 * @param context Application context, used for getting absolute path to application file store
	 * @param db Database used for filling
	 * @param inputFile Name of input DBF file for parsing
	 * @param table Ident of table to fill in DB
	 * @throws JDBFException
	 */
	public static void dbfToDb(Activity mAct, Context context, SQLiteDatabase db, String inputFile, int table, Handler mHandler, String fPath) throws JDBFException
	{
		/** appPath = /data/data/ru.by.rsa/files */
		String appPath = fPath;

		/** DBF file stream storage */
		DBFReader reader = null;
		/** Current row with excel data */
		Object[] rowObjects = null;
		/** Storage for data before writing it in database */
		ContentValues values = new ContentValues();
		/** Count of records in file */
		int recCount;
		int fieldCount;

		Bundle data = new Bundle();

		// Open DBF file
		reader = new DBFReader(appPath + File.separator + inputFile);
		recCount = (int) (reader.getFileSize() / reader.getRecordSize());
		fieldCount = reader.getFieldCount();

		switch (table)
		{
			case DBF_CUST:
			{
				// Delete table cust from database before inserting new
				db.execSQL("DROP TABLE IF EXISTS " + RsaDbHelper.TABLE_CUST);

				// Create new table in db (after deleting old one)
				RsaDbHelper.createCustDBTable(db);

				// Write message to Handler: this table begin to download:
	            android.os.Message hMess = mHandler.obtainMessage();
	            data.clear();
	            data.putString("LOG",   "TABLE");
	            data.putString("TABLE", context.getResources().getString(R.string.rsadb_clients));
	            data.putInt("COUNT", recCount-1);
	            hMess.setData(data);
	            mHandler.sendMessage(hMess);

	            int insertCounter = 0;
	            db.execSQL("BEGIN TRANSACTION");
				// Parsing DBF-file...
				while( (rowObjects = reader.nextRecord()) != null)
				{

			        if ((insertCounter%100) == 0)
			        {
			        	db.execSQL("COMMIT");
						db.execSQL("BEGIN TRANSACTION");
			        }

					values.put(RsaDbHelper.CUST_ID,			rowObjects[0].toString());
					values.put(RsaDbHelper.CUST_NAME,		rowObjects[1].toString());
					values.put(RsaDbHelper.CUST_TEL,		rowObjects[2].toString());
					values.put(RsaDbHelper.CUST_ADDRESS,	rowObjects[3].toString());
					values.put(RsaDbHelper.CUST_OKPO,		rowObjects[4].toString());
					values.put(RsaDbHelper.CUST_INN,		rowObjects[5].toString());
					values.put(RsaDbHelper.CUST_CONTACT,	rowObjects[6].toString());
					values.put(RsaDbHelper.CUST_DOGOVOR,	rowObjects[7].toString());
					db.insert(RsaDbHelper.TABLE_CUST, RsaDbHelper.CUST_NAME, values);
					values.clear();
					hMess = mHandler.obtainMessage();
					data.putString("LOG", "PROGRESS");
					hMess.setData(data);
					mHandler.sendMessage(hMess);

					insertCounter++;
			    }
				db.execSQL("COMMIT");
				break;
			}
			case DBF_BRAND:
			{
				// Delete table cust from database before inserting new
				db.execSQL("DROP TABLE IF EXISTS " + RsaDbHelper.TABLE_BRAND);

				// Create new table in db (after deleting old one)
				RsaDbHelper.createBrandDBTable(db);

				// Write message to Handler: this table begin to download:
	            android.os.Message hMess = mHandler.obtainMessage();
	            data.clear();
	            data.putString("LOG",   "TABLE");
	            data.putString("TABLE", context.getResources().getString(R.string.rsadb_brands));
	            data.putInt("COUNT", recCount-1);
	            hMess.setData(data);
	            mHandler.sendMessage(hMess);

	            int insertCounter = 0;
	            db.execSQL("BEGIN TRANSACTION");

				// Parsing DBF-file...
				while( (rowObjects = reader.nextRecord()) != null)
				{
					if ((insertCounter%100) == 0)
			        {
			        	db.execSQL("COMMIT");
						db.execSQL("BEGIN TRANSACTION");
			        }

			        values.put(RsaDbHelper.BRAND_ID,		rowObjects[0].toString());
					values.put(RsaDbHelper.BRAND_NAME,		rowObjects[1].toString());
					db.insert(RsaDbHelper.TABLE_BRAND, RsaDbHelper.BRAND_NAME, values);
					values.clear();
					hMess = mHandler.obtainMessage();
					data.putString("LOG", "PROGRESS");
					hMess.setData(data);
					mHandler.sendMessage(hMess);

					insertCounter++;
			    }
				db.execSQL("COMMIT");
				break;
			}
			case DBF_CHAR:
			{
				SharedPreferences prefs = mAct.getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);

				// Delete table cust from database before inserting new
				db.execSQL("DROP TABLE IF EXISTS " + RsaDbHelper.TABLE_CHAR);

				// Create new table in db (after deleting old one)
				RsaDbHelper.createCharDBTable(db);

				// Write message to Handler: this table begin to download:
	            android.os.Message hMess = mHandler.obtainMessage();
	            data.clear();
	            data.putString("LOG",   "TABLE");
	            data.putString("TABLE", context.getResources().getString(R.string.rsadb_conditions));
	            data.putInt("COUNT", recCount-1);
	            hMess.setData(data);
	            mHandler.sendMessage(hMess);

	            int insertCounter = 0;
	            int intMaxPriceType = 1;
	            int intTmp = 0;
	            db.execSQL("BEGIN TRANSACTION");

				// Parsing DBF-file...
				while( (rowObjects = reader.nextRecord()) != null)
				{
					if ((insertCounter%100) == 0)
			        {
			        	db.execSQL("COMMIT");
						db.execSQL("BEGIN TRANSACTION");
			        }

			        values.put(RsaDbHelper.CHAR_ID,			rowObjects[0].toString());
			        values.put(RsaDbHelper.CHAR_CUST_ID,	rowObjects[1].toString());
					values.put(RsaDbHelper.CHAR_BRAND_ID,	rowObjects[2].toString());
					values.put(RsaDbHelper.CHAR_DISCOUNT,	rowObjects[3].toString());
					values.put(RsaDbHelper.CHAR_DELAY,		rowObjects[4].toString());
					values.put(RsaDbHelper.CHAR_PRICE,		rowObjects[5].toString());

					db.insert(RsaDbHelper.TABLE_CHAR, RsaDbHelper.CHAR_ID, values);
					values.clear();
					hMess = mHandler.obtainMessage();
					data.putString("LOG", "PROGRESS");
					hMess.setData(data);
					mHandler.sendMessage(hMess);

					// find max pricetype
					intTmp = Integer.parseInt(rowObjects[5].toString());
					if (intTmp>intMaxPriceType)
						intMaxPriceType = intTmp;

					insertCounter++;
			    }
				db.execSQL("COMMIT");
				prefs.edit().putInt(RsaDb.MAXPRICETYPE, intMaxPriceType).commit();
				break;
			}
			case DBF_PLAN:
			{
				// Delete table cust from database before inserting new
				db.execSQL("DROP TABLE IF EXISTS " + RsaDbHelper.TABLE_PLAN);

				// Create new table in db (after deleting old one)
				RsaDbHelper.createPlanDBTable(db);

				// Write message to Handler: this table begin to download:
	            android.os.Message hMess = mHandler.obtainMessage();
	            data.clear();
	            data.putString("LOG",   "TABLE");
	            data.putString("TABLE", context.getResources().getString(R.string.rsadb_plan));
	            data.putInt("COUNT", recCount-1);
	            hMess.setData(data);
	            mHandler.sendMessage(hMess);

	            int insertCounter = 0;
	            db.execSQL("BEGIN TRANSACTION");

				// Parsing DBF-file...
				while( (rowObjects = reader.nextRecord()) != null)
				{
					if ((insertCounter%100) == 0)
			        {
			        	db.execSQL("COMMIT");
						db.execSQL("BEGIN TRANSACTION");
			        }

			        values.put(RsaDbHelper.PLAN_ID,			rowObjects[0].toString());
			        values.put(RsaDbHelper.PLAN_CUST_ID,	rowObjects[1].toString());
					values.put(RsaDbHelper.PLAN_SHOP_ID,	rowObjects[2].toString());
					values.put(RsaDbHelper.PLAN_CUST_TEXT,	rowObjects[3].toString());
					values.put(RsaDbHelper.PLAN_SHOP_TEXT,	rowObjects[4].toString());
					values.put(RsaDbHelper.PLAN_DATEV,		rowObjects[5].toString());
					values.put(RsaDbHelper.PLAN_STATE,		rowObjects[6].toString());
					db.insert(RsaDbHelper.TABLE_PLAN, RsaDbHelper.PLAN_ID, values);
					values.clear();
					hMess = mHandler.obtainMessage();
					data.putString("LOG", "PROGRESS");
					hMess.setData(data);
					mHandler.sendMessage(hMess);

					insertCounter++;
			    }
				db.execSQL("COMMIT");
				break;
			}
			case DBF_DEBIT:
			{
				// Delete table cust from database before inserting new
				db.execSQL("DROP TABLE IF EXISTS " + RsaDbHelper.TABLE_DEBIT);

				// Create new table in db (after deleting old one)
				RsaDbHelper.createDebitDBTable(db);

				// Write message to Handler: this table begin to download:
	            android.os.Message hMess = mHandler.obtainMessage();
	            data.clear();
	            data.putString("LOG",   "TABLE");
	            data.putString("TABLE", context.getResources().getString(R.string.rsadb_debit));
	            data.putInt("COUNT", recCount-1);
	            hMess.setData(data);
	            mHandler.sendMessage(hMess);

	            int insertCounter = 0;
	            db.execSQL("BEGIN TRANSACTION");

				// Parsing DBF-file...
				while( (rowObjects = reader.nextRecord()) != null)
				{
					if ((insertCounter%100) == 0)
			        {
			        	db.execSQL("COMMIT");
						db.execSQL("BEGIN TRANSACTION");
			        }

			        values.put(RsaDbHelper.DEBIT_ID,		rowObjects[0].toString());
			        values.put(RsaDbHelper.DEBIT_CUST_ID,	rowObjects[1].toString());
					values.put(RsaDbHelper.DEBIT_RN,		rowObjects[2].toString());
					values.put(RsaDbHelper.DEBIT_DATEDOC,	rowObjects[3].toString());
					values.put(RsaDbHelper.DEBIT_SUM,		normalizePrice(rowObjects[4].toString()));
					values.put(RsaDbHelper.DEBIT_DATEPP,	dateCorrection(rowObjects[5].toString()));
					values.put(RsaDbHelper.DEBIT_CLOSED,	rowObjects[6].toString());
					if (fieldCount>7)
						values.put(RsaDbHelper.DEBIT_COMMENT,	rowObjects[7].toString());

					if (fieldCount>8)
						values.put(RsaDbHelper.DEBIT_SHOP_ID,	rowObjects[8].toString());

					db.insert(RsaDbHelper.TABLE_DEBIT, 		RsaDbHelper.DEBIT_RN, values);
					values.clear();
					hMess = mHandler.obtainMessage();
					data.putString("LOG", "PROGRESS");
					hMess.setData(data);
					mHandler.sendMessage(hMess);

					insertCounter++;
			    }
				db.execSQL("COMMIT");
				break;
			}
			case DBF_GOODS:
			{
				// Delete table cust from database before inserting new
				db.execSQL("DROP TABLE IF EXISTS " + RsaDbHelper.TABLE_GOODS);

				// Create new table in db (after deleting old one)
				RsaDbHelper.createGoodsDBTable(db);

				// Write message to Handler: this table begin to download:
	            android.os.Message hMess = mHandler.obtainMessage();
	            data.clear();
	            data.putString("LOG",   "TABLE");
	            data.putString("TABLE", context.getResources().getString(R.string.rsadb_goods));
	            data.putInt("COUNT", recCount-1);
	            hMess.setData(data);
	            mHandler.sendMessage(hMess);

				// temporary variable to make if statement to detect situation when
				// rest is less then zero
				String strRest=null;
				int insertCounter = 0;
				db.execSQL("BEGIN TRANSACTION");

				// Parsing DBF-file...
				while( (rowObjects = reader.nextRecord()) != null)
				{

					if ((insertCounter%100) == 0)
			        {
			        	db.execSQL("COMMIT");
						db.execSQL("BEGIN TRANSACTION");
			        }

			        values.put(RsaDbHelper.GOODS_ID,		rowObjects[0].toString());
					values.put(RsaDbHelper.GOODS_NPP,		rowObjects[1].toString());
					values.put(RsaDbHelper.GOODS_NAME,		rowObjects[2].toString());
					values.put(RsaDbHelper.GOODS_BRAND_ID,	rowObjects[3].toString());
					values.put(RsaDbHelper.GOODS_QTY,		normalizeQty(rowObjects[4].toString(),"1"));
					values.put(RsaDbHelper.GOODS_RESTCUST,	rowObjects[5].toString());
					values.put(RsaDbHelper.GOODS_REST, 		normalizeRest(rowObjects[6].toString()));
					values.put(RsaDbHelper.GOODS_HIST1,		rowObjects[7].toString());
					values.put(RsaDbHelper.GOODS_RESTCUST1,	rowObjects[8].toString());
					values.put(RsaDbHelper.GOODS_HIST2,		rowObjects[9].toString());
					values.put(RsaDbHelper.GOODS_RESTCUST2,	rowObjects[10].toString());
					values.put(RsaDbHelper.GOODS_HIST3,		rowObjects[11].toString());
					values.put(RsaDbHelper.GOODS_GROUP_ID,	rowObjects[12].toString());
					values.put(RsaDbHelper.GOODS_PRICE1,	normalizePrice(rowObjects[13].toString()));
					values.put(RsaDbHelper.GOODS_PRICE2,	normalizePrice(rowObjects[14].toString()));
					values.put(RsaDbHelper.GOODS_PRICE3,	normalizePrice(rowObjects[15].toString()));
					values.put(RsaDbHelper.GOODS_PRICE4,	normalizePrice(rowObjects[16].toString()));
					values.put(RsaDbHelper.GOODS_PRICE5,	normalizePrice(rowObjects[17].toString()));
					values.put(RsaDbHelper.GOODS_PRICE6,	normalizePrice(rowObjects[18].toString()));
					values.put(RsaDbHelper.GOODS_PRICE7,	normalizePrice(rowObjects[19].toString()));
					values.put(RsaDbHelper.GOODS_PRICE8,	normalizePrice(rowObjects[20].toString()));
					values.put(RsaDbHelper.GOODS_PRICE9,	normalizePrice(rowObjects[21].toString()));
					values.put(RsaDbHelper.GOODS_PRICE10,	normalizePrice(rowObjects[22].toString()));
					values.put(RsaDbHelper.GOODS_PRICE11,	normalizePrice(rowObjects[23].toString()));
					values.put(RsaDbHelper.GOODS_PRICE12,	normalizePrice(rowObjects[24].toString()));
					values.put(RsaDbHelper.GOODS_PRICE13,	normalizePrice(rowObjects[25].toString()));
					values.put(RsaDbHelper.GOODS_PRICE14,	normalizePrice(rowObjects[26].toString()));
					values.put(RsaDbHelper.GOODS_PRICE15,	normalizePrice(rowObjects[27].toString()));
					values.put(RsaDbHelper.GOODS_PRICE16,	normalizePrice(rowObjects[28].toString()));
					values.put(RsaDbHelper.GOODS_PRICE17,	normalizePrice(rowObjects[29].toString()));
					values.put(RsaDbHelper.GOODS_PRICE18,	normalizePrice(rowObjects[30].toString()));
					values.put(RsaDbHelper.GOODS_PRICE19,	normalizePrice(rowObjects[31].toString()));
					values.put(RsaDbHelper.GOODS_PRICE20,	normalizePrice(rowObjects[32].toString()));
					values.put(RsaDbHelper.GOODS_DISCOUNT,	rowObjects[33].toString());
					values.put(RsaDbHelper.GOODS_PRICEWNDS,	!rowObjects[34].toString().equals("")?rowObjects[34].toString():"0");
					values.put(RsaDbHelper.GOODS_PRICEWONDS,!rowObjects[35].toString().equals("")?rowObjects[35].toString():"0");
					values.put(RsaDbHelper.GOODS_UN,		rowObjects[36].toString());
					values.put(RsaDbHelper.GOODS_COEFF,		normalizeCoeff(rowObjects[37].toString()));
					values.put(RsaDbHelper.GOODS_SUMWONDS,	!rowObjects[38].toString().equals("")?rowObjects[38].toString():"0");
					values.put(RsaDbHelper.GOODS_SUMWNDS,	!rowObjects[39].toString().equals("")?rowObjects[39].toString():"0");
					values.put(RsaDbHelper.GOODS_WEIGHT1,	normalizeRest(rowObjects[40].toString()));
					values.put(RsaDbHelper.GOODS_WEIGHT,	!rowObjects[41].toString().equals("")?rowObjects[41].toString():"0");
					values.put(RsaDbHelper.GOODS_VOLUME1,	!rowObjects[42].toString().equals("")?rowObjects[42].toString():"0");
					values.put(RsaDbHelper.GOODS_VOLUME,	!rowObjects[43].toString().equals("")?rowObjects[43].toString():"0");
					values.put(RsaDbHelper.GOODS_NDS,		!rowObjects[44].toString().equals("")?rowObjects[44].toString():"0");
					values.put(RsaDbHelper.GOODS_DATE,		rowObjects[45].toString());
					values.put(RsaDbHelper.GOODS_FLASH,		normalizeFlash(rowObjects[46].toString()));
					db.insert(RsaDbHelper.TABLE_GOODS, RsaDbHelper.GOODS_NAME, values);
					values.clear();
					hMess = mHandler.obtainMessage();
					data.putString("LOG", "PROGRESS");
					hMess.setData(data);
					mHandler.sendMessage(hMess);

					insertCounter++;
			    }
				db.execSQL("COMMIT");
				break;
			}
			case DBF_GROUP:
			{
				// Delete table cust from database before inserting new
				db.execSQL("DROP TABLE IF EXISTS " + RsaDbHelper.TABLE_GROUP);

				// Create new table in db (after deleting old one)
				RsaDbHelper.createGroupDBTable(db);

				// Write message to Handler: this table begin to download:
	            android.os.Message hMess = mHandler.obtainMessage();
	            data.clear();
	            data.putString("LOG",   "TABLE");
	            data.putString("TABLE", context.getResources().getString(R.string.rsadb_groups));
	            data.putInt("COUNT", recCount-1);
	            hMess.setData(data);
	            mHandler.sendMessage(hMess);

	            int insertCounter = 0;
	            db.execSQL("BEGIN TRANSACTION");

				// Parsing DBF-file...
				while( (rowObjects = reader.nextRecord()) != null)
				{
					if ((insertCounter%100) == 0)
			        {
			        	db.execSQL("COMMIT");
						db.execSQL("BEGIN TRANSACTION");
			        }

			        values.put(RsaDbHelper.GROUP_ID,	rowObjects[0].toString());
					values.put(RsaDbHelper.GROUP_NAME,	rowObjects[1].toString());
					if (fieldCount>2)
						values.put(RsaDbHelper.GROUP_BRAND_ID, rowObjects[2].toString());
					if (fieldCount>3)
						values.put(RsaDbHelper.GROUP_PARENT_NAME, rowObjects[3].toString());

					db.insert(RsaDbHelper.TABLE_GROUP, RsaDbHelper.GROUP_NAME, values);
					values.clear();
					hMess = mHandler.obtainMessage();
					data.putString("LOG", "PROGRESS");
					hMess.setData(data);
					mHandler.sendMessage(hMess);

					insertCounter++;
			    }
				db.execSQL("COMMIT");
				break;
			}
			case DBF_SHOP:
			{
				// Delete table cust from database before inserting new
				db.execSQL("DROP TABLE IF EXISTS " + RsaDbHelper.TABLE_SHOP);

				// Create new table in db (after deleting old one)
				RsaDbHelper.createShopDBTable(db);

				// Write message to Handler: this table begin to download:
	            android.os.Message hMess = mHandler.obtainMessage();
	            data.clear();
	            data.putString("LOG",   "TABLE");
	            data.putString("TABLE", context.getResources().getString(R.string.rsadb_tt));
	            data.putInt("COUNT", recCount-1);
	            hMess.setData(data);
	            mHandler.sendMessage(hMess);

	            int insertCounter = 0;
	            db.execSQL("BEGIN TRANSACTION");

				// Parsing DBF-file...
				while( (rowObjects = reader.nextRecord()) != null)
				{

					if ((insertCounter%100) == 0)
			        {
			        	db.execSQL("COMMIT");
						db.execSQL("BEGIN TRANSACTION");
			        }


			        values.put(RsaDbHelper.SHOP_ID,			rowObjects[0].toString());
			        values.put(RsaDbHelper.SHOP_CUST_ID,	rowObjects[1].toString());
					values.put(RsaDbHelper.SHOP_NAME,		rowObjects[2].toString());
					values.put(RsaDbHelper.SHOP_ADDRESS,	rowObjects[3].toString());
					db.insert(RsaDbHelper.TABLE_SHOP, RsaDbHelper.SHOP_NAME, values);
					values.clear();
					hMess = mHandler.obtainMessage();
					data.putString("LOG", "PROGRESS");
					hMess.setData(data);
					mHandler.sendMessage(hMess);

					insertCounter++;
			    }
				db.execSQL("COMMIT");
				break;
			}
			case DBF_PRODLOCK:
			{
				// Delete table cust from database before inserting new
				db.execSQL("DROP TABLE IF EXISTS " + RsaDbHelper.TABLE_PRODLOCK);

				// Create new table in db (after deleting old one)
				RsaDbHelper.createProdlockDBTable(db);

				// Write message to Handler: this table begin to download:
	            android.os.Message hMess = mHandler.obtainMessage();
	            data.clear();
	            data.putString("LOG",   "TABLE");
	            data.putString("TABLE", "Блок");
	            data.putInt("COUNT", recCount-1);
	            hMess.setData(data);
	            mHandler.sendMessage(hMess);

	            int insertCounter = 0;
	            db.execSQL("BEGIN TRANSACTION");

				// Parsing DBF-file...
				while( (rowObjects = reader.nextRecord()) != null)
				{
					if ((insertCounter%100) == 0)
			        {
			        	db.execSQL("COMMIT");
						db.execSQL("BEGIN TRANSACTION");
			        }

			        values.put(RsaDbHelper.PRODLOCK_CUST_ID,	rowObjects[0].toString());
			        values.put(RsaDbHelper.PRODLOCK_SHOP_ID,	rowObjects[1].toString());
					values.put(RsaDbHelper.PRODLOCK_GOODS_ID,	rowObjects[2].toString());
					values.put(RsaDbHelper.PRODLOCK_DATE,		rowObjects[3].toString());
					db.insert(RsaDbHelper.TABLE_PRODLOCK, RsaDbHelper.PRODLOCK_CUST_ID, values);
					values.clear();
					hMess = mHandler.obtainMessage();
					data.putString("LOG", "PROGRESS");
					hMess.setData(data);
					mHandler.sendMessage(hMess);

					insertCounter++;
			    }
				db.execSQL("COMMIT");
				break;
			}
			case DBF_SKLAD:
			{
				// Delete table cust from database before inserting new
				db.execSQL("DROP TABLE IF EXISTS " + RsaDbHelper.TABLE_SKLAD);

				// Create new table in db (after deleting old one)
				RsaDbHelper.createSkladDBTable(db);

				// Write message to Handler: this table begin to download:
	            android.os.Message hMess = mHandler.obtainMessage();
	            data.clear();
	            data.putString("LOG",   "TABLE");
	            data.putString("TABLE", context.getResources().getString(R.string.rsadb_wh));
	            data.putInt("COUNT", recCount-1);
	            hMess.setData(data);
	            mHandler.sendMessage(hMess);

	            int insertCounter = 0;
	            db.execSQL("BEGIN TRANSACTION");

				// Parsing DBF-file...
				while( (rowObjects = reader.nextRecord()) != null)
				{
					if ((insertCounter%100) == 0)
			        {
			        	db.execSQL("COMMIT");
						db.execSQL("BEGIN TRANSACTION");
			        }

			        values.put(RsaDbHelper.SKLAD_ID,	rowObjects[0].toString());
					values.put(RsaDbHelper.SKLAD_NAME,	rowObjects[1].toString());
					if (fieldCount>2)
						values.put(RsaDbHelper.SKLAD_DEFAULT,	rowObjects[2].toString());
					db.insert(RsaDbHelper.TABLE_SKLAD, RsaDbHelper.SKLAD_NAME, values);
					values.clear();
					hMess = mHandler.obtainMessage();
					data.putString("LOG", "PROGRESS");
					hMess.setData(data);
					mHandler.sendMessage(hMess);

					insertCounter++;
			    }
				db.execSQL("COMMIT");
				break;
			}
			case DBF_SKLADDET:
			{
				SharedPreferences prefs = mAct.getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);

				// Delete table cust from database before inserting new
				db.execSQL("DROP TABLE IF EXISTS " + RsaDbHelper.TABLE_SKLADDET);

				// Create new table in db (after deleting old one)
				RsaDbHelper.createSkladdetDBTable(db);

				// Write message to Handler: this table begin to download:
	            android.os.Message hMess = mHandler.obtainMessage();
	            data.clear();
	            data.putString("LOG",   "TABLE");
	            data.putString("TABLE", "ДетОст");
	            data.putInt("COUNT", recCount-1);
	            hMess.setData(data);
	            mHandler.sendMessage(hMess);

	            int insertCounter = 0;
	            db.execSQL("BEGIN TRANSACTION");

				// Parsing DBF-file...
				while( (rowObjects = reader.nextRecord()) != null)
				{
					if ((insertCounter%100) == 0)
			        {
			        	db.execSQL("COMMIT");
						db.execSQL("BEGIN TRANSACTION");
			        }

			        values.put(RsaDbHelper.SKLAD_SKLAD_ID,	rowObjects[0].toString());
			        values.put(RsaDbHelper.SKLAD_GOODS_ID,	rowObjects[1].toString());
					values.put(RsaDbHelper.SKLAD_FUNC_1,	rowObjects[2].toString());
					values.put(RsaDbHelper.SKLAD_FUNC_2,	rowObjects[3].toString());

					db.insert(RsaDbHelper.TABLE_SKLADDET, RsaDbHelper.SKLAD_SKLAD_ID, values);
					values.clear();
					hMess = mHandler.obtainMessage();
					data.putString("LOG", "PROGRESS");
					hMess.setData(data);
					mHandler.sendMessage(hMess);

					insertCounter++;
			    }
				db.execSQL("COMMIT");

				SharedPreferences def_prefs = PreferenceManager.getDefaultSharedPreferences(context);
				def_prefs.edit().putBoolean("prefSkladdet", true).commit();

				break;
			}
			case DBF_WORKINF:
			{
				// Write message to Handler: this table begin to download:
	            android.os.Message hMess = mHandler.obtainMessage();
	            data.clear();
	            data.putString("LOG",   "TABLE");
	            data.putString("TABLE", context.getResources().getString(R.string.rsadb_workinf));
	            data.putInt("COUNT", 1);
	            hMess.setData(data);
	            mHandler.sendMessage(hMess);

				// Parsing DBF-file...
				if ((rowObjects = reader.nextRecord()) != null)
				{
				    /** Get Shared Preferences */
					SharedPreferences prefs = mAct.getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);
					SharedPreferences def_prefs = PreferenceManager.getDefaultSharedPreferences(context);
					try {
						prefs.edit().putBoolean(RsaDb.SENDLINES,	rowObjects[0].toString().equals("1")).commit();
						def_prefs.edit().putBoolean("usingFactCash", rowObjects[0].toString().equals("10")).commit();
					} catch (Exception ee) {}

					try {
						int delta = Integer.parseInt(rowObjects[1].toString());
						if (delta>0) {
							prefs.edit().putInt("ru.by.rsa.resend_monitor", delta).commit();
						}
					} catch (Exception ee) {}

					prefs.edit().putString(RsaDb.EMAILKEY,		rowObjects[4].toString()).commit();
					prefs.edit().putString(RsaDb.PASSWORDKEY,	rowObjects[5].toString()).commit();
					prefs.edit().putString(RsaDb.SMTPKEY,		rowObjects[2].toString()).commit();
					prefs.edit().putString(RsaDb.SMTPPORTKEY,	rowObjects[3].toString()).commit();
					prefs.edit().putString(RsaDb.POPKEY,		rowObjects[8].toString()).commit();
					prefs.edit().putString(RsaDb.POPPORTKEY,	rowObjects[9].toString()).commit();
					prefs.edit().putString(RsaDb.SENDTOKEY,		rowObjects[7].toString()).commit();


					prefs.edit().putString(RsaDb.FTPUSER,		rowObjects[4].toString()).commit();
					prefs.edit().putString(RsaDb.FTPPASSWORD,	rowObjects[5].toString()).commit();
					prefs.edit().putString(RsaDb.FTPSERVER,		rowObjects[2].toString()).commit();
					prefs.edit().putString(RsaDb.FTPPORT,		rowObjects[3].toString()).commit();
					prefs.edit().putString(RsaDb.FTPINBOX,		rowObjects[6].toString()).commit();
					prefs.edit().putString(RsaDb.FTPOUTBOX,		rowObjects[7].toString()).commit();


					/** Get Screen Preferences */
					SharedPreferences screen_prefs = mAct.getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE);
					screen_prefs.edit().putString(RsaDb.CODEKEY, rowObjects[13].toString()).commit();
					screen_prefs.edit().putString(RsaDb.NAMEKEY, rowObjects[12].toString()).commit();

					hMess = mHandler.obtainMessage();
					data.putString("LOG", "PROGRESS");
					hMess.setData(data);
					mHandler.sendMessage(hMess);
			    }
				break;
			}
			default:
			// Do nothing if another kind of dialog is selected
				break;
		}
		// Closing opened DBF-file
		reader.close();
	}

	public static void copyTable(String db_source_name, SQLiteDatabase db_dest, int flag) throws Exception
	{
		db_dest.execSQL("ATTACH DATABASE '" + db_source_name + "' AS db_source");

		if ((flag & 2)==0) 		loadTable(db_dest, RsaDbHelper.TABLE_CUST);
		if ((flag & 128)==0) 	loadTable(db_dest, RsaDbHelper.TABLE_BRAND);
		if ((flag & 64)==0)		loadTable(db_dest, RsaDbHelper.TABLE_GROUP);
		if ((flag & 32)==0)		loadTable(db_dest, RsaDbHelper.TABLE_SKLAD);
		if ((flag & 16)==0) 	loadTable(db_dest, RsaDbHelper.TABLE_SHOP);
		if ((flag & 8)==0)		loadTable(db_dest, RsaDbHelper.TABLE_DEBIT);
		if ((flag & 4)==0)		loadTable(db_dest, RsaDbHelper.TABLE_CHAR);
		if ((flag & 1)==0) 		loadTable(db_dest, RsaDbHelper.TABLE_GOODS);
	}

	private static void loadTable(SQLiteDatabase db_dest, String tableName)
	{
		db_dest.execSQL("DROP TABLE IF EXISTS main." + tableName);

		if (tableName.equals(RsaDbHelper.TABLE_CUST))
			RsaDbHelper.createCustDBTable(db_dest);
		else if (tableName.equals(RsaDbHelper.TABLE_BRAND))
			RsaDbHelper.createBrandDBTable(db_dest);
		else if (tableName.equals(RsaDbHelper.TABLE_GROUP))
			RsaDbHelper.createGroupDBTable(db_dest);
		else if (tableName.equals(RsaDbHelper.TABLE_SKLAD))
			RsaDbHelper.createSkladDBTable(db_dest);
		else if (tableName.equals(RsaDbHelper.TABLE_SHOP))
			RsaDbHelper.createShopDBTable(db_dest);
		else if (tableName.equals(RsaDbHelper.TABLE_DEBIT))
			RsaDbHelper.createDebitDBTable(db_dest);
		else if (tableName.equals(RsaDbHelper.TABLE_CHAR))
			RsaDbHelper.createCharDBTable(db_dest);
		else if (tableName.equals(RsaDbHelper.TABLE_GOODS))
			RsaDbHelper.createGoodsDBTable(db_dest);

		db_dest.execSQL("INSERT INTO main." + tableName + " "
				+ "SELECT * FROM db_source." + tableName );

		Log.d("loadTable()", "Table: " + tableName + " loaded...");
	}


	public static String setStringLength(String s, int l)
	{
		if (s.length()>l)
		{
			return s.substring(s.length() - l);
		}

		return s;
	}

	/**
	 * Fill DBF file with data from database table with selected data
	 * @param context context Application context, used for getting absolute path to application file store
	 * @param db Database used for get data
	 * @param outputHead Name of output Head.dbf file
	 * @param outputLines Name of output Lines.dbf file
	 * @param date Selected day
	 * @throws JDBFException
	 * @return count of orders in HEAD of selected day
	 */
	public synchronized static int dbToDBF(Activity mAct, Context context, SQLiteDatabase db,
								String outputHead, String outputLines, String date, int stat_blocked, String fPath, boolean onlyNew) // Ticket 13: stat_blocked added
			throws JDBFException, IOException {
		SharedPreferences screen_prefs;
		if (mAct == null) {
			screen_prefs=context.getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE);
		} else {
			screen_prefs=mAct.getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE);
		}
		boolean isLongAttr = screen_prefs.getBoolean(RsaDb.LONGATTRIBUTES, false);
		SharedPreferences def_prefs = PreferenceManager.getDefaultSharedPreferences(context);
		boolean isLongComment = def_prefs.getBoolean("longComment", false);
		boolean usingDelivery  = def_prefs.getBoolean("prefUsedelivery", false);

		/** Count of orders */
		int countOrders = 0;
		/** Is set of data from DBtable HEAD that used for filling such dbf-file. To get value have to use mCurosr.getString("KEY") */
		Cursor		mCursorHead = null;
		/** Is set of data from DBtable LINES that used for filling such dbf-file. To get value have to use mCurosr.getString("KEY") */
		Cursor		mCursorLines = null;
		Cursor		mCursorRests = null;
		/** Arrays of columns that will be used to obtain data from DB-tables */
		String[] mContentHead = {"_id",	RsaDbHelper.HEAD_ID,		RsaDbHelper.HEAD_ZAKAZ_ID,	RsaDbHelper.HEAD_CUST_ID,
										RsaDbHelper.HEAD_SHOP_ID,	RsaDbHelper.HEAD_SKLAD_ID,	RsaDbHelper.HEAD_BLOCK,
										RsaDbHelper.HEAD_SENDED,	RsaDbHelper.HEAD_CUST_TEXT,	RsaDbHelper.HEAD_SHOP_TEXT,
										RsaDbHelper.HEAD_SKLAD_TEXT,RsaDbHelper.HEAD_DELAY,		RsaDbHelper.HEAD_PAYTYPE,
										RsaDbHelper.HEAD_HSUMO,		RsaDbHelper.HEAD_HWEIGHT,	RsaDbHelper.HEAD_HVOLUME,
										RsaDbHelper.HEAD_DATE,      RsaDbHelper.HEAD_TIME,		RsaDbHelper.HEAD_HNDS,
										RsaDbHelper.HEAD_HNDSRATE,	RsaDbHelper.HEAD_SUMWONDS,	RsaDbHelper.HEAD_NUMFULL,
										RsaDbHelper.HEAD_NUM1C,		RsaDbHelper.HEAD_GPSCOORD,	RsaDbHelper.HEAD_REMARK,
										RsaDbHelper.HEAD_ROUTECODE,	RsaDbHelper.HEAD_VISITID, RsaDbHelper.HEAD_DELIVERY };
		String[] mContentLines = {"_id",	RsaDbHelper.LINES_ID,			RsaDbHelper.LINES_ZAKAZ_ID,	RsaDbHelper.LINES_GOODS_ID,
											RsaDbHelper.LINES_TEXT_GOODS,	RsaDbHelper.LINES_RESTCUST, RsaDbHelper.LINES_QTY,
											RsaDbHelper.LINES_UN,			RsaDbHelper.LINES_COEFF,	RsaDbHelper.LINES_DISCOUNT,
											RsaDbHelper.LINES_PRICEWNDS, 	RsaDbHelper.LINES_SUMWNDS, 	RsaDbHelper.LINES_PRICEWONDS,
											RsaDbHelper.LINES_SUMWONDS, 	RsaDbHelper.LINES_NDS, 		RsaDbHelper.LINES_DELAY };
		String[] mContentRests = {"_id",	RsaDbHelper.RESTS_ID, RsaDbHelper.RESTS_ZAKAZ_ID, RsaDbHelper.RESTS_GOODS_ID,
											RsaDbHelper.RESTS_RESTQTY, RsaDbHelper.RESTS_RECQTY, RsaDbHelper.RESTS_QTY };

		/** appPath = /data/data/ru.by.rsa/files */
		String appPath = fPath;

		/** Storage for fields name in Head.dbf table */
		JDBField fieldsHead[] = new JDBField[26];

		/** Init fields names with data type -string, length -? */
		fieldsHead[0] = new JDBField("ID",			'C',	8,	0);
		fieldsHead[1] = new JDBField("ZAKAZ_ID",	'C',	4,  0);
		fieldsHead[2] = new JDBField("CUST_ID",		'C',	isLongAttr?12:8,	0);
		fieldsHead[3] = new JDBField("SHOP_ID",		'C',	isLongAttr?12:8,	0);
		fieldsHead[4] = new JDBField("SKLAD_ID",	'C',	isLongAttr?12:8,	0);
		fieldsHead[5] = new JDBField("BLOCK",		'C',	1,	0);
		fieldsHead[6] = new JDBField("SENDED",		'C',	1,	0);
		fieldsHead[7] = new JDBField("CUST_TEXT",	'C',	50,	0);
		fieldsHead[8] = new JDBField("SHOP_TEXT",	'C',	50, 0);
		fieldsHead[9] = new JDBField("SKLAD_TEXT",	'C',	50, 0);
		fieldsHead[10] = new JDBField("DELAY",		'C',	3,	0);
		fieldsHead[11] = new JDBField("PAYTYPE",	'C',	3,	0);
		fieldsHead[12] = new JDBField("HSUMO",		'C',	14, 0);
		fieldsHead[13] = new JDBField("HWEIGHT",	'C',	14, 0);
		fieldsHead[14] = new JDBField("HVOLUME",	'C',	14, 0);
		fieldsHead[15] = new JDBField("DATE",		'C',	10, 0);
		fieldsHead[16] = new JDBField("TIME",		'C',	5,  0);
		fieldsHead[17] = new JDBField("HNDS",		'C',	14, 0);
		fieldsHead[18] = new JDBField("HNDSRATE",	'C',	1,  0);
		fieldsHead[19] = new JDBField("SUMWONDS",	'C',	14, 0);
		fieldsHead[20] = new JDBField("NUMFULL",	'C',	40, 0);
		fieldsHead[21] = new JDBField("NUM1C",		'C',	20, 0);
		fieldsHead[22] = new JDBField("GPSCOORD",	'C',	30, 0);
		fieldsHead[23] = new JDBField("REMARK",		'C',	isLongComment?200:50, 0);
		fieldsHead[24] = new JDBField("ROUTECODE",	'C',	isLongAttr?12:10, 0);
		fieldsHead[25] = new JDBField("VISITID",	'C',	14, 0);

		int headLimits[] = { 8, 4, isLongAttr?12:8, isLongAttr?12:8, isLongAttr?12:8, 1, 1, 50, 50, 50, 3, 3, 14, 14, 14, 10, 5, 14, 1, 14, 40, 20, 30, isLongComment?200:50, isLongAttr?12:10, 14 };

		/** Storage for fields name in Lines.dbf table */
		JDBField fieldsLines[] = new JDBField[15];

		/** Init fields names with data type -string, length -? */
		fieldsLines[0] = new JDBField("ID",			'C',	8, 	0);
		fieldsLines[1] = new JDBField("ZAKAZ_ID",	'C',	4, 	0);
		fieldsLines[2] = new JDBField("GOODS_ID",	'C',	isLongAttr?12:8, 	0);
		fieldsLines[3] = new JDBField("TEXT_GOODS",	'C',	100,0);
		fieldsLines[4] = new JDBField("RESTCUST",	'C',	11, 0);
		fieldsLines[5] = new JDBField("QTY",		'C',	8, 	0);
		fieldsLines[6] = new JDBField("UN",			'C',	3, 	0);
		fieldsLines[7] = new JDBField("COEFF",		'C',	5, 	0);
		fieldsLines[8] = new JDBField("DISCOUNT",	'C',	3, 	0);
		fieldsLines[9] = new JDBField("PRICEWNDS",	'C',	15, 0);
		fieldsLines[10] = new JDBField("SUMWNDS",	'C',	15, 0);
		fieldsLines[11] = new JDBField("PRICEWONDS",'C',	15, 0);
		fieldsLines[12] = new JDBField("SUMWONDS",	'C',	15, 0);
		fieldsLines[13] = new JDBField("NDS",		'C',	15, 0);
		fieldsLines[14] = new JDBField("DELAY",		'C',	3, 	0);

		int linesLimits[] = { 8, 4, isLongAttr?12:8, 100, 11, 8, 3, 5, 3, 15, 15, 15, 15, 15, 3};

		/** Create and fill fields names in dbf files */
	    DBFWriter writerHead = new DBFWriter(appPath + File.separator + outputHead, fieldsHead, "US-ASCII");
	    DBFWriter writerLines = new DBFWriter(appPath + File.separator + outputLines, fieldsLines, "US-ASCII");

	    /** Variables for temporary storing data of one record before write it to dbf files */
	    Object rowHead[] = new Object[26];
	    Object rowLines[] = new Object[15];


	    String sended = "";
		if (onlyNew == true) {
			sended += " and (SENDED='0')";
		}

		// Get data of specified param date from database to mCursorHead by call query:
		// SELECT mContentLines FROM TABLE_HEAD WHERE DATE = date
		mCursorHead = db.query(RsaDbHelper.TABLE_HEAD, mContentHead, RsaDbHelper.HEAD_DATE + "='" + date + "'"+sended,
											null, null, null, null);
		int verOS = 2;
		try
		{
			verOS = Integer.parseInt(Build.VERSION.RELEASE.substring(0,1));
		}
		catch (Exception e) {};
		// init mCursorHead to work with it
		// Romka 19.12.2012
		if (verOS<3 && mAct!=null) mAct.startManagingCursor(mCursorHead);

		// Get count of orders
		countOrders = mCursorHead.getCount();


		// If found at last 1 order in this date
		if (countOrders > 0)
		{
			// Move to first record in mCursorHead
			mCursorHead.moveToFirst();

			// Parsing all orders in HEAD table
			for (int i=0;i<countOrders;i++)
			{
				// fill data for rows to temporary variables
				for (int ii=0;ii<5;ii++)
				{
					rowHead[ii] = setStringLength(mCursorHead.getString(ii+1), headLimits[ii]);
				}

				// Ticket 13: if order was blocked then ...
				if (mCursorHead.getInt(6) == stat_blocked)
					rowHead[5] = "1";
				else
					rowHead[5] = "0";

				for (int ii=6;ii<26;ii++)
				{
					rowHead[ii] = setStringLength(mCursorHead.getString(ii+1), headLimits[ii]);
				}

				try {
					// but in field ZAKAZ_ID we have to put data from _id
					if (mCursorHead.getString(22).length()>0 && !mCursorHead.getString(22).equals("0")) {
						rowHead[0] = "VZRT";
						// сюда вставить num1c
						String _s1 = rowHead[23].toString();
						String _s2 = mCursorHead.getString(22);
						if (_s2.equals("-") == false)
							_s2 = findNumById(db, mCursorHead.getString(22), screen_prefs.getString(RsaDb.IMEIKEY, "0"));
						String _s3 = "$"+_s2+"$ " + _s1;

						rowHead[23] = _s3.length()<=50?_s3:_s3.substring(0, 50);
					}
 				} catch (Exception e) {
 					rowHead[23] = "Ошибка";
 				}

				rowHead[1] = setStringLength( mCursorHead.getString(0), 4);
				rowHead[20] = setStringLength( screen_prefs.getString(RsaDb.IMEIKEY, "0") + "_" + rowHead[1] + mCursorHead.getString(21), 40);

				if (def_prefs.getBoolean("usingFactCash", false)) {
					rowHead[14] = mCursorHead.getString(1);
				}

				if (def_prefs.getBoolean("prefUsedelivery", false)) {
					rowHead[19] = mCursorHead.getString(27)==null?"":mCursorHead.getString(27);
				}


				// write data from temporary variables to HEAD.dbf file
				writerHead.addRecord(rowHead);


				// Get goods of current order from database table LINES to mCursorLines by call query:
				// SELECT mContentLines FROM TABLE_LINES WHERE ZAKAZ_ID(LINES) = _id(HEAD)
				mCursorLines = db.query(RsaDbHelper.TABLE_LINES, mContentLines,
											RsaDbHelper.LINES_ZAKAZ_ID + "='"
													+ mCursorHead.getString(0) +"'",
													null, RsaDbHelper.LINES_GOODS_ID, null, null);
				try {
					mCursorRests = db.query(RsaDbHelper.TABLE_RESTS, mContentRests,
												RsaDbHelper.RESTS_ZAKAZ_ID + "='"
													+ mCursorHead.getString(0) +"'",
													null, RsaDbHelper.RESTS_GOODS_ID, null, null);
				} catch (Exception e) {};

				verOS = 2;
				try
				{
					verOS = Integer.parseInt(Build.VERSION.RELEASE.substring(0,1));
				}
				catch (Exception e) {};

				// init mCursorHead to work with it
				// Romka 19.12.2012
				if (verOS<3 && mAct!=null) {
					mAct.startManagingCursor(mCursorLines);
					try {
						mAct.startManagingCursor(mCursorRests);
					} catch (Exception e) {};
				}

				// If found at last 1 line in this order
				if (mCursorLines.getCount() > 0)
				{
					// Before parsing
					mCursorLines.moveToFirst();

					// Parsing all lines of current order in LINES table
					for (int j=0;j<mCursorLines.getCount();j++)
					{
						// fill data for rows to temporary variables
						for (int jj=0;jj<15;jj++)
						{
							rowLines[jj] = setStringLength(mCursorLines.getString(jj+1), linesLimits[jj]);
						}

						try {
							rowLines[4] = findRestByGoodsID(mCursorRests, (String)rowLines[2], linesLimits);
						} catch (Exception e3) {};

						// write data from temporary variables to LINES.dbf file
						writerLines.addRecord(rowLines);
						///////////////////////////////////////////////////////////
						// Write rests if found and orders for this goods exists
						//
							try {
								rowLines[0] = (String)rowLines[1]; // move ZAKAZ_ID value to ID cell
								rowLines[1] = "REST";     // set ZAKAZ_ID = "REST" for identification
								for (int ii=3;ii<15;ii++) {
									rowLines[ii] = "";
								}

								setRestQtyRec(mCursorRests, rowLines, linesLimits);

								writerLines.addRecord(rowLines);
							} catch (Exception e) {}
						// end of write rests
						////////////////////////////////////////////////////////////

						// Go to next one
						mCursorLines.moveToNext();
					}
					///////////////////////////////////////////////////////////
					// Write rests if found and orders for this goods DOES NOT exists
					//
						try {
							mCursorRests =	db.query(	  RsaDbHelper.TABLE_RESTS,                                                 // table
														  mContentRests,                                                           // *
														  "("+RsaDbHelper.RESTS_ZAKAZ_ID + "='" + mCursorHead.getString(0) +"') AND " // where
														+ RsaDbHelper.RESTS_GOODS_ID + " NOT IN ("
														+ "SELECT GOODS_ID FROM _lines WHERE ZAKAZ_ID='" + mCursorHead.getString(0) + "'"
														+ ")",
														  null,                                                                    //
														  RsaDbHelper.RESTS_GOODS_ID,                                              //
														  null,                                                                    //
														  null);                                                                   //
							//mCursorRests = db.rawQuery( "select * from _rests where (ZAKAZ_ID='1') AND GOODS_ID group by GOODS_ID", null);
							if (mCursorRests.getCount()>0) {
								for (int jj=0;jj<15;jj++){
									rowLines[jj] = "";
								}
								mCursorRests.moveToFirst();
								for(int k=0;k<mCursorRests.getCount();k++) {

									try {
										// first of all write simple lines string with order = 0m but rest <> 0
										rowLines[0] = "0";
										rowLines[1] = setStringLength(mCursorHead.getString(0), linesLimits[0]);
										rowLines[2] = setStringLength(mCursorRests.getString(3), linesLimits[2]);
										rowLines[4] = setStringLength(mCursorRests.getString(4), linesLimits[4]);
										rowLines[5] = "0";
										rowLines[7] = setStringLength(mCursorRests.getString(5),  linesLimits[7]);
										writerLines.addRecord(rowLines);
									} catch (Exception e4) {}

									// then write detailed rest line
									rowLines[0] = setStringLength(mCursorHead.getString(0), linesLimits[0]);
									rowLines[1] = "REST";
									rowLines[2] = setStringLength(mCursorRests.getString(3), linesLimits[2]);
									rowLines[4] = setStringLength(mCursorRests.getString(4), linesLimits[4]);
									rowLines[5] = setStringLength(mCursorRests.getString(6),  linesLimits[5]);
									rowLines[7] = setStringLength(mCursorRests.getString(5),  linesLimits[7]);
									writerLines.addRecord(rowLines);
									mCursorRests.moveToNext();
									Log.i("RRR",Integer.toString(k));
								}

							}
						} catch(Exception e){Log.i("RRR","strange");}
						Log.i("RRR","urraaa");
					// end
					///////////////////////////////////////////////
				}

				// Go to next one
				mCursorHead.moveToNext();

			}

			try
	    	{
				// Close the Cursor
				if (mCursorLines != null)
				{
					mCursorLines.close();
				}
				if (mCursorRests != null)
				{
					mCursorRests.close();
				}
	    	}
			catch (Exception e)
			{
				// Do nothing
			}


		}


		try
    	{
			// Close the Cursor
			if (mCursorHead != null)
			{
				mCursorHead.close();
			}
    	}
		catch (Exception e)
		{
			// Do nothing
		}

	    // Close dbf-files
	    writerHead.close();

	    //////////////////////////////////////////////////
	    // Write Cash income to lines DBF
	    try
	    {

			Calendar c = Calendar.getInstance();
			String curDate =  String.format( "%04d-%02d-%02d", c.get(Calendar.YEAR), c.get(Calendar.MONTH)+1, c.get(Calendar.DAY_OF_MONTH) );
			String queryKassa =  "SELECT CUST_ID, CUST_TEXT, DATE, HSUMO "
					           + "FROM _kassa "
					           + "WHERE DATE='" + curDate + "'";
		    Cursor mCursorKassa = db.rawQuery(queryKassa, null);

		    verOS = 2;
			try
			{
				verOS = Integer.parseInt(Build.VERSION.RELEASE.substring(0,1));
			}
			catch (Exception e) {};

		    // Romka 19.12.2012
			if (verOS<3 && mAct!=null) mAct.startManagingCursor(mCursorKassa);
		    int countItem = mCursorKassa.getCount();

		    if (countItem>0)
		    {
		    	mCursorKassa.moveToFirst();
		    	for (int i=0;i<countItem;i++)
				{
		    		String s3="0";
		    		String s0="0";
		    		String s1="0";
		    		String s2="0";
		    		try {
			    		s3 = mCursorKassa.getString(3);
		    		} catch (Exception e4) {}
		    		try {
			    		s2 = mCursorKassa.getString(2);
					} catch (Exception e4) {}
		    		try {
			    		s0 = mCursorKassa.getString(0);
		    		} catch (Exception e4) {}
		    		try {
			    		s1 = mCursorKassa.getString(1);
		    		} catch (Exception e4) {}

		    		rowLines[0] = "";
		    		rowLines[1] = "KASA";   											//            -> ZAKAZ_ID
		    		rowLines[2] = setStringLength(s0, 8);        // CUST_ID    -> GOODS_ID
		    		rowLines[3] = setStringLength(s1, 50);       // CUST_TEXT  -> TEXT_GOODS
		    		rowLines[4] = setStringLength(s2, 10);		// DATE       -> RESTCUST
		    		rowLines[5] = "";
		    		rowLines[6] = "";
		    		rowLines[7] = "";
		    		rowLines[8] = "";
		    		rowLines[9] = "";
		    		rowLines[10] = setStringLength(s3, 14);      // HSUMO      -> SUMWNDS
		    		rowLines[11] = "";
		    		rowLines[12] = "";
		    		rowLines[13] = "";
		    		rowLines[14] = "";

		    		writerLines.addRecord(rowLines);
		    		mCursorKassa.moveToNext();
				}
		    }
		    if (mCursorKassa != null)
		        mCursorKassa.close();
	    }
	    catch (Exception e) {}

	    //////////////////////////////////////////////////
	    // Write Detailed Cash income to lines DBF
	    try
	    {
			Calendar c = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String curDate =  sdf.format(c.getTime());
			String queryDetKassa =  "SELECT CUST_ID, RN, SUM, DATE, FULL "
					           + "FROM _kassadet "
					           + "WHERE DATE='" + curDate + "' "
					           + "GROUP BY RN";
		    Cursor mCursorDetKassa = db.rawQuery(queryDetKassa, null);

		    verOS = 2;
			try
			{
				verOS = Integer.parseInt(Build.VERSION.RELEASE.substring(0,1));
			}
			catch (Exception e) {};

		    // Romka 19.12.2012
			if (verOS<3 && mAct!=null) mAct.startManagingCursor(mCursorDetKassa);
		    int countItem = mCursorDetKassa.getCount();

		    if (countItem>0)
		    {
		    	mCursorDetKassa.moveToFirst();
		    	for (int i=0;i<countItem;i++)
				{
		    		String cust_id = "0";
		    		String rn = "0";
		    		String sum = "0";
		    		String d = "2001-01-01";
		    		String full = "0";

		    		try {
			    		cust_id = mCursorDetKassa.getString(0);
		    		} catch (Exception e4) {}
		    		try {
			    		rn = mCursorDetKassa.getString(1);
					} catch (Exception e4) {}
		    		try {
			    		sum = mCursorDetKassa.getString(2);
		    		} catch (Exception e4) {}
		    		try {
			    		d = mCursorDetKassa.getString(3);
		    		} catch (Exception e4) {}
		    		try {
			    		full = mCursorDetKassa.getString(4);
		    		} catch (Exception e4) {}

		    		rowLines[0] = "";							//            -> ID
		    		rowLines[1] = "KASD";						//            -> ZAKAZ_ID
		    		rowLines[2] = setStringLength(cust_id, 8);  // CUST_ID    -> GOODS_ID
		    		rowLines[3] = setStringLength(rn, 50);      //  CUST_TEXT -> TEXT_GOODS
		    		rowLines[4] = setStringLength(d, 10);		// DATE       -> RESTCUST
		    		rowLines[5] = "";							//            -> QTY
		    		rowLines[6] = "";							//			  -> UN
		    		rowLines[7] = "";							//			  -> COEFF
		    		rowLines[8] = full;							//			  -> DISCOUNT
		    		rowLines[9] = "";							// 			  -> PRICEWNDS
		    		rowLines[10] = setStringLength(sum, 14);    // HSUMO      -> SUMWNDS
		    		rowLines[11] = "";							//			  -> PRICEWONDS
		    		rowLines[12] = "";							//			  -> SUMWONDS
		    		rowLines[13] = "";							//			  -> NDS
		    		rowLines[14] = "";							// 			  -> DELAY

		    		writerLines.addRecord(rowLines);
		    		mCursorDetKassa.moveToNext();
				}
		    }
		    if (mCursorDetKassa != null)
		        mCursorDetKassa.close();
	    }
	    catch (Exception e) {}

	    writerLines.close();


	    return countOrders;
	}


	public static void setRestQtyRec(Cursor cur, Object[] rows, int[] limits) {
		String rest = "-1";
		String qty = "-1";
		String rec = "-1";

		String goodsID = (String)rows[2];

		if (cur.getCount()>0) {
			cur.moveToFirst();
			for(int i=0;i<cur.getCount();i++) {
				if (goodsID.equals(cur.getString(3))) {
					rest = cur.getString(4);
					qty = cur.getString(6);
					rec = cur.getString(5);
					break;
				}
				cur.moveToNext();
			}
		}

		rows[4] = setStringLength(rest, limits[4]);
		rows[5] = setStringLength(qty,  limits[5]);
		rows[7] = setStringLength(rec,  limits[7]);
	}

	public static String findRestByGoodsID(Cursor cur, String goodsID, int[] limits) {
		String rest = "0";

		if (cur.getCount()>0) {
			cur.moveToFirst();
			for(int i=0;i<cur.getCount();i++) {
				if (goodsID.equals(cur.getString(3))) {
					rest = cur.getString(4);
					break;
				}
				cur.moveToNext();
			}
		}

		return setStringLength(rest, limits[4]);
	}

	public static String findNumById(SQLiteDatabase dbase, String _ord_id, String im) {
		String query = "select NUMFULL from _head where _id = " + _ord_id + " limit 1";
		Cursor curs = dbase.rawQuery(query, null);
		String res = "Undef";
		if (curs.getCount()>0) {
			curs.moveToFirst();
			res = curs.getString(0);
			res = im+"_"+_ord_id+res;
		} else {
			res = "0";
		}

		if (curs!=null && !curs.isClosed()) {
			curs.close();
		}
		return res;
	}

	public static int dbToCSV(Activity mAct, Context context, SQLiteDatabase db,
			String outputHead, String outputLines, String date, int stat_blocked, String fPath, boolean onlyNew) throws IOException {
		// order count
		int countOrders = 0;
		int countLines = 0;
		String code = "0";
		String qty = "0";
		Cursor mCursorHead = null;
		Cursor mCursorLines = null;
		File csvFile= null;
		OutputStream csvStream = null;
		BufferedWriter csvBufWriter = null;
		String appPath = fPath + File.separator;
		SharedPreferences prefs = mAct.getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);
		String portion = prefs.getString(RsaDb.CSVPORTION, "1");
		Calendar c = Calendar.getInstance();
		String lastSendDay = prefs.getString(RsaDb.LASTCSVSENDDAY, "00");
		String nowDay = Integer.toString(c.get(Calendar.DAY_OF_MONTH));
		if (lastSendDay.equals(nowDay) == false) {
			portion = "1";
		}

		String query =  "select _id, CUST_ID, CUST_TEXT, DATE, TIME, NUM1C, REMARK, HVOLUME, PAYTYPE " +
						"from _head " +
						"where (date='" + date +"')";
		if (onlyNew == true) {
			query += " and (SENDED='0')";
		}

		mCursorHead = db.rawQuery(query, new String[]{});

		verOS = 2;
		try {
			verOS = Integer.parseInt(Build.VERSION.RELEASE.substring(0,1));
		} catch (Exception e) {};
		if (verOS<3)  mAct.startManagingCursor(mCursorHead);

		countOrders = mCursorHead.getCount();

		if (countOrders>0) {
			mCursorHead.moveToFirst();
			for(int i=0;i<countOrders;i++) {
				query = "select _id, GOODS_ID, QTY " +
						"from _lines " +
						"where ZAKAZ_ID='"+ mCursorHead.getString(0)+"' " +
						"group by GOODS_ID "
						+ "order by TEXT_GOODS";
				mCursorLines = db.rawQuery(query, new String[]{});
				if (verOS<3)  mAct.startManagingCursor(mCursorHead);

				StringBuilder strDate= new StringBuilder(mCursorHead.getString(3).substring(0,2));
				strDate.append(mCursorHead.getString(4).substring(0,2));
				strDate.append(mCursorHead.getString(4).substring(3,5));
				// int sec = 10 + (int)(Math.random() * ((59 - 10) + 1));
				strDate.append(mCursorHead.getString(7));
				String pform = mCursorHead.getString(8).equals("Без")?"1":"2";

				StringBuilder filename = new StringBuilder(portion+"_");// Portion
					filename.append(strDate.toString()+"_");			// DDHHMMSS
					filename.append(mCursorHead.getString(1)+"_");		// CUST_ID
					filename.append(mCursorHead.getString(2)+"_");		// CUST_TEXT
					filename.append(pform+"_");		// Payform (1 or 2)
					filename.append(mCursorHead.getString(6));			// Remark
					filename.append(".xls");							// .xls
				csvFile = new File(appPath + outputHead + filename.toString());
				csvStream = new java.io.FileOutputStream(csvFile);
				csvBufWriter = new BufferedWriter(new OutputStreamWriter(csvStream,Charset.forName("Windows-1251")));

				countLines = mCursorLines.getCount();
				if (countLines>0)
					mCursorLines.moveToFirst();
				for (int j=0;j<mCursorLines.getCount();j++) {
					code = mCursorLines.getString(1);
					qty  = mCursorLines.getString(2);
					csvBufWriter.append(code + " 		" + qty + "\r\n");
					mCursorLines.moveToNext();
				}

				csvBufWriter.flush();
				csvBufWriter.close();
				mCursorHead.moveToNext();
			}
		}

		try {
			if (mCursorHead!=null)
				mCursorHead.close();
		} catch (Exception e2) {};

		portion = Integer.toString(Integer.parseInt(portion)+1);
		prefs.edit().putString(RsaDb.CSVPORTION, portion).commit();
		prefs.edit().putString(RsaDb.LASTCSVSENDDAY, nowDay).commit();

		return countOrders;
	}

	/**
	 * Fill XML file with data from database table with selected data
	 * @param context context Application context, used for getting absolute path to application file store
	 * @param db Database used for get data
	 * @param outputHead Name of output Head.xml file
	 * @param outputLines Name of output Lines.xml file
	 * @param date Selected day
	 * @throws IOException
	 * @return count of orders in HEAD of selected day
	 */
	public static int dbToXML(Activity mAct, Context context, SQLiteDatabase db, RsaDbHelper data_mdb,
								String outputHead, String outputLines, String date, int stat_blocked,
			String fPath, boolean onlyNew, String orderingId) throws IOException
	{
		Log.d("RRR", "dbToXML() called");
		/** Count of orders */
		int countOrders = 0;
		/** Is set of data from DBtable HEAD that used for filling such dbf-file. To get value have to use mCurosr.getString("KEY") */
		Cursor		mCursorHead = null;
		/** Is set of data from DBtable LINES that used for filling such dbf-file. To get value have to use mCurosr.getString("KEY") */
		Cursor		mCursorLines = null;
		Cursor		mCursorRests = null;
		/** Arrays of columns that will be used to obtain data from DB-tables */
		String[] mContentHead = {"_id",	RsaDbHelper.HEAD_ID,		RsaDbHelper.HEAD_ZAKAZ_ID,	RsaDbHelper.HEAD_CUST_ID,
										RsaDbHelper.HEAD_SHOP_ID,	RsaDbHelper.HEAD_SKLAD_ID,	RsaDbHelper.HEAD_BLOCK,
										RsaDbHelper.HEAD_SENDED,	RsaDbHelper.HEAD_CUST_TEXT,	RsaDbHelper.HEAD_SHOP_TEXT,
										RsaDbHelper.HEAD_SKLAD_TEXT,RsaDbHelper.HEAD_DELAY,		RsaDbHelper.HEAD_PAYTYPE,
										RsaDbHelper.HEAD_HSUMO,		RsaDbHelper.HEAD_HWEIGHT,	RsaDbHelper.HEAD_HVOLUME,
										RsaDbHelper.HEAD_DATE,      RsaDbHelper.HEAD_TIME,		RsaDbHelper.HEAD_HNDS,
										RsaDbHelper.HEAD_HNDSRATE,	RsaDbHelper.HEAD_SUMWONDS,	RsaDbHelper.HEAD_NUMFULL,
										RsaDbHelper.HEAD_NUM1C,		RsaDbHelper.HEAD_GPSCOORD,	RsaDbHelper.HEAD_REMARK,
										RsaDbHelper.HEAD_ROUTECODE,	RsaDbHelper.HEAD_VISITID,	RsaDbHelper.HEAD_DELIVERY }; //27
		String[] mContentLines = {"_id",	RsaDbHelper.LINES_ID,			RsaDbHelper.LINES_ZAKAZ_ID,	RsaDbHelper.LINES_GOODS_ID,
											RsaDbHelper.LINES_TEXT_GOODS,	RsaDbHelper.LINES_RESTCUST, RsaDbHelper.LINES_QTY,
											RsaDbHelper.LINES_UN,			RsaDbHelper.LINES_COEFF,	RsaDbHelper.LINES_DISCOUNT,
											RsaDbHelper.LINES_PRICEWNDS, 	RsaDbHelper.LINES_SUMWNDS, 	RsaDbHelper.LINES_PRICEWONDS,
											RsaDbHelper.LINES_SUMWONDS, 	RsaDbHelper.LINES_NDS, 		RsaDbHelper.LINES_DELAY,
											RsaDbHelper.LINES_COMMENT }; // [16]
		String[] mContentLinesSafe = {"_id",	RsaDbHelper.LINES_ID,			RsaDbHelper.LINES_ZAKAZ_ID,	RsaDbHelper.LINES_GOODS_ID,
										RsaDbHelper.LINES_TEXT_GOODS,	RsaDbHelper.LINES_RESTCUST, RsaDbHelper.LINES_QTY,
										RsaDbHelper.LINES_UN,			RsaDbHelper.LINES_COEFF,	RsaDbHelper.LINES_DISCOUNT,
										RsaDbHelper.LINES_PRICEWNDS, 	RsaDbHelper.LINES_SUMWNDS, 	RsaDbHelper.LINES_PRICEWONDS,
										RsaDbHelper.LINES_SUMWONDS, 	RsaDbHelper.LINES_NDS, 		RsaDbHelper.LINES_DELAY }; // [15]
		String[] mContentRests = {"_id",	RsaDbHelper.RESTS_ID, 		RsaDbHelper.RESTS_ZAKAZ_ID, RsaDbHelper.RESTS_GOODS_ID,
											RsaDbHelper.RESTS_RESTQTY, 	RsaDbHelper.RESTS_RECQTY, 	RsaDbHelper.RESTS_QTY };

		/** appPath = /data/data/ru.by.rsa/files/ */
		String appPath = fPath + File.separator;

		SharedPreferences def_prefs = PreferenceManager.getDefaultSharedPreferences(context);
		boolean usingShopTypes =  def_prefs.getBoolean("prefUsingTypes", false);
		boolean usingDelivery  = def_prefs.getBoolean("prefUsedelivery", false);
		boolean usingAgreed  = def_prefs.getBoolean("prefUseAgreed", false);
		boolean usingGeophoto  = def_prefs.getBoolean("prefGeoPhoto", false);

		/** Set xml files */
		XmlSerializer serializerH = Xml.newSerializer();
		XmlSerializer serializerL = Xml.newSerializer();

		File xmlFileH = new File(appPath + outputHead);
		File xmlFileL = new File(appPath + outputLines);

		OutputStream xmlStreamH = new java.io.FileOutputStream(xmlFileH);
		OutputStream xmlStreamL = new java.io.FileOutputStream(xmlFileL);

		serializerH.setOutput(xmlStreamH, "UTF-8");
		serializerH.startDocument("UTF-8", true);
		serializerH.startTag("", "head");
		serializerL.setOutput(xmlStreamL, "UTF-8");
		serializerL.startDocument("UTF-8", true);
		serializerL.startTag("", "lines");

	    /** Get Screen Preferences */
		SharedPreferences screen_prefs;
		if (mAct == null) {
			screen_prefs = context.getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE);
		} else {
			screen_prefs = mAct.getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE);
		}
		SQLiteDatabase data_db = data_mdb.getReadableDatabase();

		// Get data of specified param date from database to mCursorHead by call query:
		// SELECT mContentLines FROM TABLE_HEAD WHERE DATE = date
		String sended = "";
		if (onlyNew == true) {
			sended += " and (SENDED='0')";
		}
		String withoutCurrentOrder = "";
		if (!TextUtils.isEmpty(orderingId)) {
			withoutCurrentOrder = " and (_id!='"+orderingId+"')";
		}

		mCursorHead = db.query(RsaDbHelper.TABLE_HEAD, mContentHead, RsaDbHelper.HEAD_DATE + "='" + date + "'" + sended + withoutCurrentOrder,
											null, null, null, null);

		verOS = 2;
		try
		{
			verOS = Integer.parseInt(Build.VERSION.RELEASE.substring(0,1));
		}
		catch (Exception e) {};

		// init mCursorHead to work with it
		// Romka 19.12.2012
		if (verOS<3 && mAct!=null)  mAct.startManagingCursor(mCursorHead);

		// Get count of orders
		countOrders = mCursorHead.getCount();

		// If found at last 1 order in this date
		if (countOrders > 0)
		{
			// Move to first record in mCursorHead
			mCursorHead.moveToFirst();

			// Parsing all orders in HEAD table
			for (int i=0;i<countOrders;i++)
			{
				serializerH.startTag("", "item");

				if (usingGeophoto) {
					serializerH.attribute("", "MANUALGPS",	getManualGPS(db, date, mCursorHead.getString(3), mCursorHead.getString(4)));
				}

				String sklad_text = mCursorHead.getString(10);
				if (usingAgreed) {
					serializerH.attribute("", "AGREED",	sklad_text.contains("^^&&")?"1":"0");
				}

				if (usingDelivery) {
					String strVisitId = mCursorHead.getString(26);
					int state = Integer.parseInt(strVisitId.equals("")?"0":strVisitId.toString());

					serializerH.attribute("", "CB1", 	((state&1)==1)?"1":"0");
					serializerH.attribute("", "CB2", 	((state&2)==2)?"1":"0");
					serializerH.attribute("", "CB3", 	((state&4)==4)?"1":"0");
					serializerH.attribute("", "CB4", 	((state&8)==8)?"1":"0");
					serializerH.attribute("", "DELIVERY", 	mCursorHead.getString(27));
				}

				if (usingShopTypes == true) {
					serializerH.attribute("", "SHOPTYPE", 	getShopType(data_db, mCursorHead.getString(3), mCursorHead.getString(4)));
				}

				serializerH.attribute("", "VISITID", 	mCursorHead.getString(26)); // 25
				serializerH.attribute("", "ROUTECODE", 	mCursorHead.getString(25)); // 24

				if ( mCursorHead.getString(22).length()>0   // Добавлять в коментарий значек возврата если номер возврата больше 1 символа
						&& !mCursorHead.getString(22).toString().equals("0")) {  // и при этом этот символ не 0
					String _s1 = mCursorHead.getString(24);
					String _s2 = mCursorHead.getString(22);
					if (_s2.equals("-") == false)
						_s2 = findNumById(db, mCursorHead.getString(22),screen_prefs.getString(RsaDb.IMEIKEY, "0"));
					serializerH.attribute("", "REMARK", 	"$"+_s2+"$ " + _s1); // 23
				} else {
					serializerH.attribute("", "REMARK", 	mCursorHead.getString(24)); // 23
				}
				serializerH.attribute("", "GPSCOORD", 	mCursorHead.getString(23)); // 22
				serializerH.attribute("", "NUM1C", 		mCursorHead.getString(22)); // 21
				serializerH.attribute("", "NUMFULL",
						screen_prefs.getString(RsaDb.IMEIKEY, "0") + "_" + mCursorHead.getString(0) + mCursorHead.getString(21)); // 20
				serializerH.attribute("", "SUMWONDS", 	mCursorHead.getString(20)); // 19
				serializerH.attribute("", "HNDSRATE", 	mCursorHead.getString(19)); // 18
				serializerH.attribute("", "HNDS", 		mCursorHead.getString(18)); // 17
				serializerH.attribute("", "TIME", 		mCursorHead.getString(17)); // 16
				serializerH.attribute("", "DATE", 		mCursorHead.getString(16)); // 15
				serializerH.attribute("", "HVOLUME", 	mCursorHead.getString(15)); // 14
				serializerH.attribute("", "HWEIGHT", 	mCursorHead.getString(14)); // 13
				serializerH.attribute("", "HSUMO",		mCursorHead.getString(13)); // 12
				serializerH.attribute("", "PAYTYPE", 	mCursorHead.getString(12)); // 11
				serializerH.attribute("", "DELAY", 		mCursorHead.getString(11)); // 10
				serializerH.attribute("", "SKLAD_TEXT", sklad_text.replace("^^&&", "")); // 9
				serializerH.attribute("", "SHOP_TEXT", 	mCursorHead.getString(9)); // 8
				serializerH.attribute("", "CUST_TEXT", 	mCursorHead.getString(8)); // 7
				serializerH.attribute("", "SENDED", 	mCursorHead.getString(7)); // 6
				// Ticket 13: if order was blocked then ...
				if (mCursorHead.getInt(6) == stat_blocked)
					serializerH.attribute("", "BLOCK", 		"1"); // 5
				else
					serializerH.attribute("", "BLOCK", 		"0"); // 5
				serializerH.attribute("", "SKLAD_ID", 	mCursorHead.getString(5)); // 4
				serializerH.attribute("", "SHOP_ID", 	mCursorHead.getString(4)); // 3
				serializerH.attribute("", "CUST_ID", 	mCursorHead.getString(3)); // 2
				serializerH.attribute("", "ZAKAZ_ID", 	mCursorHead.getString(0)); // 1

				if (mCursorHead.getString(22).length()>0 && !mCursorHead.getString(22).equals("0")) {
					serializerH.attribute("", "ID", 		"VZRT"); // 0
				} else {
					serializerH.attribute("", "ID", 		mCursorHead.getString(1)); // 0
				}

				serializerH.endTag("", "item");

				// Get goods of current order from database table LINES to mCursorLines by call query:
				// SELECT mContentLines FROM TABLE_LINES WHERE ZAKAZ_ID(LINES) = _id(HEAD)
				Log.d("RRR", "dbToXML() try to select Lines");
				try {
					mCursorLines = db.query(RsaDbHelper.TABLE_LINES, mContentLines,
												RsaDbHelper.LINES_ZAKAZ_ID + "='"
														+ mCursorHead.getString(0) +"'",
														null, RsaDbHelper.LINES_GOODS_ID, null, "_id");
					Log.d("RRR", "dbToXML() success");
				} catch(Exception e) {
					Log.d("RRR", "dbToXML() ERROR! try to select another way");
					mCursorLines = db.query(RsaDbHelper.TABLE_LINES, mContentLinesSafe,
							RsaDbHelper.LINES_ZAKAZ_ID + "='"
									+ mCursorHead.getString(0) +"'",
									null, RsaDbHelper.LINES_GOODS_ID, null, "_id");
					Log.d("RRR", "dbToXML() now success");
				}

				try {
					mCursorRests = db.query(RsaDbHelper.TABLE_RESTS, mContentRests,
												RsaDbHelper.RESTS_ZAKAZ_ID + "='"
													+ mCursorHead.getString(0) +"'",
													null, RsaDbHelper.RESTS_GOODS_ID, null, "_id");
				} catch (Exception e) {};

				verOS = 2;
				try
				{
					verOS = Integer.parseInt(Build.VERSION.RELEASE.substring(0,1));
				}
				catch (Exception e) {};

				// init mCursorHead to work with it
				// // Romka 19.12.2012
				if (verOS<3 && mAct !=null) {
					mAct.startManagingCursor(mCursorLines);
					try {
						mAct.startManagingCursor(mCursorRests);
					} catch (Exception e) {};
				}

				// If found at last 1 line in this order
				if (mCursorLines.getCount() > 0)
				{
					// Before parsing
					mCursorLines.moveToFirst();

					// Parsing all lines of current order in LINES table
					for (int j=0;j<mCursorLines.getCount();j++)
					{
						String un = mCursorLines.getString(7);
						int qt = 1;
						int qty = Integer.parseInt(mCursorLines.getString(6));

						boolean ordInPCS = def_prefs.getBoolean("prefOrderInPCS", false);

						if (ordInPCS==false && un.equals("qq")) {
							un = "pk";
							qt = getQtyFromGoods(mCursorLines.getString(3), data_db);
						}

						qty = (int)(qty / qt);
						String lineComment = "";
						Log.d("RRR", "dbToXML() try getColumnCount?");
						if (mCursorLines.getColumnCount()>16) {
							Log.d("RRR", "dbToXML() more then 16 colums");
							try {
								lineComment = mCursorLines.getString(16);
							} catch(Exception e) {
								Log.d("RRR", "dbToXML() get 16 column error");
							}

							if (lineComment==null)
								lineComment="";
							Log.d("RRR", "dbToXML() check lineComment.length...");
							if (lineComment.length()>0) {
								Log.d("RRR", "dbToXML() lineComment>0");
								lineComment = "###" + lineComment;
							}
							Log.d("RRR", "dbToXML() check success");
						}

						float dprice = 0;
						float discount = 0;
						try {
							dprice = Float.parseFloat(mCursorLines.getString(10));
							discount = Float.parseFloat(mCursorLines.getString(9)) / 100f;
							dprice = dprice * (1 - discount);
						} catch (Exception e) {}
						Log.d("RRR", "dbToXML() good");

						serializerL.startTag("", "item");

						serializerL.attribute("", "DPRICE", DataUtils.Float.format("%.4f", dprice)); // price wnds with discount
						serializerL.attribute("", "DELAY", 		mCursorLines.getString(15)); // 14
						serializerL.attribute("", "NDS", 		mCursorLines.getString(14)); // 13
						serializerL.attribute("", "SUMWONDS", 	mCursorLines.getString(13)); // 12
						serializerL.attribute("", "PRICEWONDS", mCursorLines.getString(12)); // 11
						serializerL.attribute("", "SUMWNDS", 	mCursorLines.getString(11)); // 10
						serializerL.attribute("", "PRICEWNDS", 	mCursorLines.getString(10)); // 9
						serializerL.attribute("", "DISCOUNT", 	mCursorLines.getString(9));  // 8
						serializerL.attribute("", "COEFF", 		Integer.toString(qt));		  // 7
						serializerL.attribute("", "UN", 		un);						 // 6
						serializerL.attribute("", "QTY", 		Integer.toString(qty)); 	 // 5
						serializerL.attribute("", "RESTCUST", 	mCursorLines.getString(5));  // 4
						Log.d("RRR", "dbToXML() lineComment?");
						Log.d("RRR", lineComment==null?"fuck":"dbToXML() lineComment?");
						serializerL.attribute("", "TEXT_GOODS", mCursorLines.getString(4)+lineComment);  // 3
						Log.d("RRR", "dbToXML() lineComment is OK");
						serializerL.attribute("", "GOODS_ID", 	mCursorLines.getString(3));  // 2
						serializerL.attribute("", "ZAKAZ_ID", 	mCursorLines.getString(2));  // 1
						serializerL.attribute("", "ID", 		mCursorLines.getString(1));  // 0

						serializerL.endTag("", "item");

						///////////////////////////////////////////////////////////
						// Write rests if found and orders for this goods exists
						//
						try {
							serializerL.startTag("", "item");

							serializerL.attribute("", "DELAY", 		""); // 14
							serializerL.attribute("", "NDS", 		""); // 13
							serializerL.attribute("", "SUMWONDS", 	""); // 12
							serializerL.attribute("", "PRICEWONDS", ""); // 11
							serializerL.attribute("", "SUMWNDS", 	""); // 10
							serializerL.attribute("", "PRICEWNDS", 	""); // 9
							serializerL.attribute("", "DISCOUNT", 	"");  // 8
							serializerL.attribute("", "COEFF", 		getRecForXml(mCursorRests, mCursorLines.getString(3))); // 7 Рекомендовано
							serializerL.attribute("", "UN", 		"");  // 6
							serializerL.attribute("", "QTY", 		mCursorLines.getString(6));  // 5 Заказано
							serializerL.attribute("", "RESTCUST", 	getRestForXml(mCursorRests, mCursorLines.getString(3)));  // 4 Остаток
							serializerL.attribute("", "TEXT_GOODS", "");  						 // 3
							serializerL.attribute("", "GOODS_ID", 	mCursorLines.getString(3));  // 2
							serializerL.attribute("", "ZAKAZ_ID", 	"REST");				 	 // "REST"
							serializerL.attribute("", "ID", 		mCursorLines.getString(2));  // ZAKAZ_ID

							serializerL.endTag("", "item");

						} catch (Exception e) {}
						// end of write rests
						////////////////////////////////////////////////////////////

						// Go to next one
						mCursorLines.moveToNext();
					}
						///////////////////////////////////////////////////////////
						// Write rests if found and orders for this goods DOES NOT exists
						//
						try {
							mCursorRests =	db.query(	  RsaDbHelper.TABLE_RESTS,                                                 // table
												  mContentRests,                                                           // *
												  "("+RsaDbHelper.RESTS_ZAKAZ_ID + "='" + mCursorHead.getString(0) +"') AND " // where
												+ RsaDbHelper.RESTS_GOODS_ID + " NOT IN ("
												+ "SELECT GOODS_ID FROM _lines WHERE ZAKAZ_ID='" + mCursorHead.getString(0) + "'"
												+ ")",
												  null,                                                                    //
												  RsaDbHelper.RESTS_GOODS_ID,                                              //
												  null,                                                                    //
												  null);                                                                   //
							//mCursorRests = db.rawQuery( "select * from _rests where (ZAKAZ_ID='1') AND GOODS_ID group by GOODS_ID", null);
							if (mCursorRests.getCount()>0) {
								mCursorRests.moveToFirst();

								for(int k=0;k<mCursorRests.getCount();k++) {

									serializerL.startTag("", "item");

									serializerL.attribute("", "DELAY", 		""); // 14
									serializerL.attribute("", "NDS", 		""); // 13
									serializerL.attribute("", "SUMWONDS", 	""); // 12
									serializerL.attribute("", "PRICEWONDS", ""); // 11
									serializerL.attribute("", "SUMWNDS", 	""); // 10
									serializerL.attribute("", "PRICEWNDS", 	""); // 9
									serializerL.attribute("", "DISCOUNT", 	"");  // 8
									serializerL.attribute("", "COEFF", 		mCursorRests.getString(5)); // 7 Рекомендовано
									serializerL.attribute("", "UN", 		"");  // 6
									serializerL.attribute("", "QTY", 		mCursorRests.getString(6));  // 5 Заказано
									serializerL.attribute("", "RESTCUST", 	mCursorRests.getString(4));  // 4 Остаток
									serializerL.attribute("", "TEXT_GOODS", "");  						 // 3
									serializerL.attribute("", "GOODS_ID", 	mCursorRests.getString(3));  // 2
									serializerL.attribute("", "ZAKAZ_ID", 	"REST");				 	 // "REST"
									serializerL.attribute("", "ID", 		mCursorHead.getString(0));  // ZAKAZ_ID

									serializerL.endTag("", "item");

									mCursorRests.moveToNext();
									Log.i("RRR",Integer.toString(k));
								}

							}
						} catch(Exception e){Log.i("RRR","XMLstrange");}
						Log.i("RRR","XML urraaa");
						// end
						///////////////////////////////////////////////
				}
				// Go to next one
				mCursorHead.moveToNext();
			}
			try
	    	{
				// Close the Cursor
				if (mCursorLines != null)
				{
					mCursorLines.close();
				}
				if (mCursorRests != null)
				{
					mCursorRests.close();
				}
	    	}
			catch (Exception e)
			{
				// Do nothing
			}
		}

		try
    	{
			// Close the Cursor
			if (mCursorHead != null)
			{
				mCursorHead.close();
			}
    	}
		catch (Exception e)
		{
			// Do nothing
		}

		try  {
			if (data_db != null) {
				data_db.close();
			}
		} catch (Exception e)
		{}


	    // Close xml-files
        serializerH.endTag("", "head");
        serializerH.endDocument();

	    //////////////////////////////////////////////////
	    // Write Questionnaire
	    try {
	    	Cursor mCursorQuest = db.rawQuery("select ZAKAZ_ID, QUESTION_ID, QUESTION_TEXT, CORRECT, ANSWER, COMMENT from _quest where ZAKAZ_ID in " +
	    									 "(select _id from _head where DATE='"+date+"' "+sended+")", null);

		    if (mCursorQuest.getCount()>0) {
		    	while (mCursorQuest.moveToNext()) {
		    		serializerL.startTag("", "item");
		    		serializerL.attribute("", "DELAY", 		""); // 14
					serializerL.attribute("", "NDS", 		""); // 13
					serializerL.attribute("", "SUMWONDS", 	""); // 12
					serializerL.attribute("", "PRICEWONDS", ""); // 11
					serializerL.attribute("", "SUMWNDS", 	""); // 10
					serializerL.attribute("", "PRICEWNDS", 	""); // 9
					serializerL.attribute("", "DISCOUNT", 	"");  // 8
					serializerL.attribute("", "COEFF", 		"");  // 7
					serializerL.attribute("", "UN", 		mCursorQuest.getString(5));  // 6
					serializerL.attribute("", "QTY", 		mCursorQuest.getString(4));  // 5
					serializerL.attribute("", "RESTCUST", 	mCursorQuest.getString(3));
					serializerL.attribute("", "TEXT_GOODS", mCursorQuest.getString(2));
					serializerL.attribute("", "GOODS_ID", 	mCursorQuest.getString(1));
					serializerL.attribute("", "ZAKAZ_ID", 	"QUEST");					 // 1
					serializerL.attribute("", "ID", 		mCursorQuest.getString(0));	 // 0

		    		serializerL.endTag("", "item");
		    	}
		    }


		    if (mCursorQuest!=null && mCursorQuest.isClosed()==false)
		        mCursorQuest.close();
	    }
	    catch (Exception e) {
	    	Log.d("ZUZU", e.getMessage());
	    }



	    //////////////////////////////////////////////////
	    // Write Cash income to lines XML
	    try
	    {
			Calendar c = Calendar.getInstance();
			String curDate =  String.format( "%04d-%02d-%02d", c.get(Calendar.YEAR), c.get(Calendar.MONTH)+1, c.get(Calendar.DAY_OF_MONTH) );
			String queryKassa =  "SELECT CUST_ID, CUST_TEXT, DATE, HSUMO, TIME "
					           + "FROM _kassa "
					           + "WHERE DATE='" + curDate + "'";
		    Cursor mCursorKassa = db.rawQuery(queryKassa, null);
		    verOS = 2;
			try
			{
				verOS = Integer.parseInt(Build.VERSION.RELEASE.substring(0,1));
			}
			catch (Exception e) {};

		     //  Romka 19.12.2012
			if (verOS<3 && mAct!=null) mAct.startManagingCursor(mCursorKassa);
		    int countItem = mCursorKassa.getCount();

		    if (countItem>0)
		    {
		    	mCursorKassa.moveToFirst();
		    	for (int i=0;i<countItem;i++)
				{
		    		String s3="0";
		    		String s0="0";
		    		String s1="0";
		    		String s2="0";
		    		String time="";
		    		try {
			    		s3 = mCursorKassa.getString(3);
		    		} catch (Exception e4) {}
		    		try {
			    		s2 = mCursorKassa.getString(2);
					} catch (Exception e4) {}
		    		try {
			    		s0 = mCursorKassa.getString(0);
		    		} catch (Exception e4) {}
		    		try {
			    		s1 = mCursorKassa.getString(1);
		    		} catch (Exception e4) {}
		    		try {
			    		time = mCursorKassa.getString(4);
			    		if (time==null) time="";
		    		} catch (Exception e4) {}

		    		serializerL.startTag("", "item");
		    		serializerL.attribute("", "DELAY", 		""); // 14
					serializerL.attribute("", "NDS", 		time); // 13
					serializerL.attribute("", "SUMWONDS", 	""); // 12
					serializerL.attribute("", "PRICEWONDS", ""); // 11
					serializerL.attribute("", "SUMWNDS", 	s3); // 10
					serializerL.attribute("", "PRICEWNDS", 	""); // 9
					serializerL.attribute("", "DISCOUNT", 	"");  // 8
					serializerL.attribute("", "COEFF", 		"");  // 7
					serializerL.attribute("", "UN", 		"");  // 6
					serializerL.attribute("", "QTY", 		"");  // 5
					serializerL.attribute("", "RESTCUST", 	s2);  // 4
					serializerL.attribute("", "TEXT_GOODS", s1);  // 3
					serializerL.attribute("", "GOODS_ID", 	s0);  // 2
					serializerL.attribute("", "ZAKAZ_ID", 	"KASA");					 // 1
					serializerL.attribute("", "ID", 		"");						 // 0

		    		serializerL.endTag("", "item");


		    		mCursorKassa.moveToNext();
				}

		    }


		    if (mCursorKassa != null)
		        mCursorKassa.close();
	    }
	    catch (Exception e) {}

		//////////////////////////////////////////////////
		// Write Detailed Cash income to lines XML
		try  {
			Calendar c = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String curDate =  sdf.format(c.getTime());
			String queryDetKassa =    "SELECT CUST_ID, RN, SUM, DATE, FULL, TIME "
									+ "FROM _kassadet "
									+ "WHERE DATE='" + curDate + "' "
									+ "GROUP BY RN";
			Cursor mCursorDetKassa = db.rawQuery(queryDetKassa, null);

			verOS = 2;
			try {
				verOS = Integer.parseInt(Build.VERSION.RELEASE.substring(0,1));
			} catch (Exception e) {};

			// Romka 19.12.2012
			if (verOS<3&&mAct!=null) mAct.startManagingCursor(mCursorDetKassa);
			int countItem = mCursorDetKassa.getCount();

			if (countItem>0) {
				mCursorDetKassa.moveToFirst();
				for (int i=0;i<countItem;i++) {
					String cust_id = "0";
					String rn = "0";
					String sum = "0";
					String d = "2001-01-01";
					String full = "0";
					String time = "";

					try {
					cust_id = mCursorDetKassa.getString(0);
					} catch (Exception e4) {}
					try {
					rn = mCursorDetKassa.getString(1);
					} catch (Exception e4) {}
					try {
					sum = mCursorDetKassa.getString(2);
					} catch (Exception e4) {}
					try {
					d = mCursorDetKassa.getString(3);
					} catch (Exception e4) {}
					try {
					full = mCursorDetKassa.getString(4);
					} catch (Exception e4) {}
					try {
					time = mCursorDetKassa.getString(5);
					if (time == null) time = "";
					} catch (Exception e4) {}

					serializerL.startTag("", "item");

					serializerL.attribute("", "DELAY", 		""); // 14
					serializerL.attribute("", "NDS", 		time); // 13
					serializerL.attribute("", "SUMWONDS", 	""); // 12
					serializerL.attribute("", "PRICEWONDS", ""); // 11
					serializerL.attribute("", "SUMWNDS", 	sum); // 10
					serializerL.attribute("", "PRICEWNDS", 	""); // 9
					serializerL.attribute("", "DISCOUNT", 	full);  // 8
					serializerL.attribute("", "COEFF", 		"");  // 7
					serializerL.attribute("", "UN", 		"");  // 6
					serializerL.attribute("", "QTY", 		"");  // 5
					serializerL.attribute("", "RESTCUST", 	d);  // 4
					serializerL.attribute("", "TEXT_GOODS", rn);  // 3
					serializerL.attribute("", "GOODS_ID", 	cust_id);  // 2
					serializerL.attribute("", "ZAKAZ_ID", 	"KASD");					 // 1
					serializerL.attribute("", "ID", 		"");						 // 0

					serializerL.endTag("", "item");
					mCursorDetKassa.moveToNext();
				}
			}
			if (mCursorDetKassa != null)
				mCursorDetKassa.close();
			}
		catch (Exception e) {

			String g =  e.getMessage();
		}


	    serializerL.endTag("", "lines");
	    serializerL.endDocument();


        xmlStreamL.close();
        xmlStreamH.close();

	    return countOrders;
	}


	private static String getManualGPS(SQLiteDatabase db, String date, String cust_id, String shop_id) {
		String result = "0";
		Calendar 			c = Calendar.getInstance();
		SimpleDateFormat	inputfm = new SimpleDateFormat("dd.MM.yyyy");
		SimpleDateFormat	sfm = new SimpleDateFormat("yyyy-MM-dd");
		try {
			Date sendingDate = inputfm.parse(date);
			c.setTime(sendingDate);
		} catch (Exception e) {

		}
		String 				currentDay = sfm.format(c.getTime());
		c.add(Calendar.DATE, 1);
		String				nextDay	   = sfm.format(c.getTime());
		String	query  = "select GPS from _geophoto where TIMESTAMP BETWEEN '"+ currentDay +"' " +
						  "AND '" + nextDay + "' " +
						  "AND CUST_ID='"+cust_id+"' " +
						  "AND SHOP_ID='"+shop_id+"' order by TIMESTAMP desc limit 1";
		Cursor cursor = null;
		try {
			cursor = db.rawQuery(query, null);
			if (cursor.moveToFirst()) {
				String value = cursor.getString(0);
				result = value==null?"0":value;
			}
		} catch (Exception e) {}

		if (cursor!=null && !cursor.isClosed()) {
			cursor.close();
		}

		return result;
	}

	private static String getRecForXml(Cursor cur, String gID) {
		String result = "0";

		if (cur.getCount()>0) {
			cur.moveToFirst();
			for(int i=0;i<cur.getCount();i++) {
				if (gID.equals(cur.getString(3))) {
					return cur.getString(5);
				}
				cur.moveToNext();
			}
		}

		return result;
	}

	private static int getQtyFromGoods(String goods_id, SQLiteDatabase sdb) {
		int result = 1;
		String q = "1";
		try {
			Cursor cur = sdb.rawQuery("select QTY from _goods where ID='"+goods_id+"' limit 1", null);
			if (cur.getCount()>0) {
				cur.moveToFirst();
				q = cur.getString(0);
				result = Integer.parseInt(q);
			}
			if (cur!=null) cur.close();
		} catch (Exception e) {}

		return result;
	}

	private static String getShopType(SQLiteDatabase sdb, String cid, String sid) {
		String result = "Undef";
		try {
			Cursor cur = sdb.rawQuery("select TYPE from _shop where CUST_ID='"+cid+"' and ID='"+sid+"' limit 1", null);
			if (cur.getCount()>0) {
				cur.moveToFirst();
				result = cur.getString(0);
				if (result==null) result = "Undef";
			}
			if (cur!=null) cur.close();
		} catch (Exception e) {}

		return result;
	}

	private static String getRestForXml(Cursor cur, String gID) {
		String result = "0";

		if (cur.getCount()>0) {
			cur.moveToFirst();
			for(int i=0;i<cur.getCount();i++) {
				if (gID.equals(cur.getString(3))) {
					return cur.getString(4);
				}
				cur.moveToNext();
			}
		}

		return result;
	}

	private static int getCountXML(XmlPullParser parser) throws Exception
	{
		int i = 0;
		int eventType = parser.getEventType();

    	while (eventType != XmlPullParser.END_DOCUMENT)
		{
		    if ((eventType == XmlPullParser.START_TAG)
		    		&& parser.getName().toLowerCase().equals("item"))
		    {
				i++;
		    }
		    eventType = parser.next();
		}

		return i;
	}

	// ДЛЯ ДБФ надо передавать reccount-1
	private static void preparetable(SQLiteDatabase db, Context context, Handler mHandler, String tblName, int recCount)
	{
		Bundle data = new Bundle();
		String Name = null;


		if (tblName.equals("WORKINF"))
		{
			Name = context.getResources().getString(R.string.rsadb_workinf);

			// Write message to Handler: this table begin to download:
			android.os.Message hMess = mHandler.obtainMessage();
			data.clear();
			data.putString("LOG",   "TABLE");
			data.putString("TABLE", Name);
			data.putInt("COUNT", recCount);
			hMess.setData(data);
			mHandler.sendMessage(hMess);
			return;
		}

		// Delete table cust from database before inserting new
		db.execSQL("DROP TABLE IF EXISTS " + tblName);

		if (tblName.equals(RsaDbHelper.TABLE_CUST))
		{
			// Create new table in db (after deleting old one)
			RsaDbHelper.createCustDBTable(db);
			Name = context.getResources().getString(R.string.rsadb_clients);
		}
		else if (tblName.equals(RsaDbHelper.TABLE_BRAND))
		{
			// Create new table in db (after deleting old one)
			RsaDbHelper.createBrandDBTable(db);
			Name = context.getResources().getString(R.string.rsadb_brands);
		}
		else if (tblName.equals(RsaDbHelper.TABLE_CHAR))
		{
			// Create new table in db (after deleting old one)
			RsaDbHelper.createCharDBTable(db);
			Name = context.getResources().getString(R.string.rsadb_conditions);
		}
		else if (tblName.equals(RsaDbHelper.TABLE_PLAN))
		{
			// Create new table in db (after deleting old one)
			RsaDbHelper.createPlanDBTable(db);
			Name = context.getResources().getString(R.string.rsadb_plan);
		}
		else if (tblName.equals(RsaDbHelper.TABLE_SOLD))
		{
			// Create new table in db (after deleting old one)
			RsaDbHelper.createSoldDBTable(db);
			Name = context.getResources().getString(R.string.rsadb_sold);
		}
		else if (tblName.equals(RsaDbHelper.TABLE_DEBIT))
		{
			// Create new table in db (after deleting old one)
			RsaDbHelper.createDebitDBTable(db);
			Name = context.getResources().getString(R.string.rsadb_debit);
		}
		else if (tblName.equals(RsaDbHelper.TABLE_GOODS))
		{
			// Create new table in db (after deleting old one)
			RsaDbHelper.createGoodsDBTable(db);
			Name = context.getResources().getString(R.string.rsadb_goods);
		}
		else if (tblName.equals(RsaDbHelper.TABLE_GROUP))
		{
			// Create new table in db (after deleting old one)
			RsaDbHelper.createGroupDBTable(db);
			Name = context.getResources().getString(R.string.rsadb_groups);
		}
		else if (tblName.equals(RsaDbHelper.TABLE_SHOP))
		{
			// Create new table in db (after deleting old one)
			RsaDbHelper.createShopDBTable(db);
			Name = context.getResources().getString(R.string.rsadb_tt);
		}
		else if (tblName.equals(RsaDbHelper.TABLE_SKLAD))
		{
			// Create new table in db (after deleting old one)
			RsaDbHelper.createSkladDBTable(db);
			Name = context.getResources().getString(R.string.rsadb_wh);
		}
		else if (tblName.equals(RsaDbHelper.TABLE_MATRIX))
		{
			// Create new table in db (after deleting old one)
			RsaDbHelper.createMatrixDBTable(db);
			Name = "Матрица";
		}
		else if (tblName.equals(RsaDbHelper.TABLE_PRODLOCK))
		{
			// Create new table in db (after deleting old one)
			RsaDbHelper.createProdlockDBTable(db);
			Name = "Блокировки";
		}
		else if (tblName.equals(RsaDbHelper.TABLE_HIST)) {
			RsaDbHelper.createHistDBTable(db);
			Name = "История";
		} else if (tblName.equals(RsaDbHelper.TABLE_SALOUT)) {
			RsaDbHelper.createSaloutDBTable(db);
			Name = "Отгрузки";
		}
		else if (tblName.equals(RsaDbHelper.TABLE_SKLADDET))
		{
			// Create new table in db (after deleting old one)
			RsaDbHelper.createSkladdetDBTable(db);
			Name = "ДетСк";
		}
		else if (tblName.equals(RsaDbHelper.TABLE_STATIC_PLAN))
		{
			RsaDbHelper.createStaticPlanTable(db);
			Name = "СтатПлан";
		}

		// Write message to Handler: this table begin to download:
		android.os.Message hMess = mHandler.obtainMessage();
		data.clear();
		data.putString("LOG",   "TABLE");
		data.putString("TABLE", Name);
		data.putInt("COUNT", recCount);
		hMess.setData(data);
		mHandler.sendMessage(hMess);
	}

	public static int getCountCSV(InputStreamReader inStream) {
		int count = 0;
		BufferedReader inBuffer = new BufferedReader(inStream);

		try {
			while (inBuffer.readLine() != null) {
				count++;
			}
		} catch (Exception e) {
			return 0;
		}

		return count;
	}

	public static void CSVtoCust(Context context, SQLiteDatabase db, Handler mHandler, String fPath) throws Exception {
		ContentValues values = new ContentValues();
		Bundle data = new Bundle();
		// appPath = /data/data/ru.by.rsa/files
		String appPath = fPath + File.separator + "inbox/";
		File csvFile = new File(appPath + "cust.csv");
		// init file to stream
		InputStream csvStream = new java.io.FileInputStream(csvFile);
		// parse for elements count
		int CSVCount = getCountCSV(new InputStreamReader(csvStream));
		// close for files reseting
		csvStream.close();
		// open file again for parsing
		csvStream = new java.io.FileInputStream(csvFile);
		CSVReader reader = new CSVReader(new InputStreamReader(csvStream, Charset.forName("Windows-1251")));

		preparetable(db, context, mHandler, RsaDbHelper.TABLE_CUST, CSVCount);

		int insertCounter = 0;
        db.execSQL("BEGIN TRANSACTION");

		String next[] = {};
		next = reader.readNext();
		while (next != null) {
			if ((insertCounter%100) == 0) {
	        	db.execSQL("COMMIT");
				db.execSQL("BEGIN TRANSACTION");
	        }

			/////////////////////////////////////DB PASTE
			values.put(RsaDbHelper.CUST_ID,			next[0]);
			values.put(RsaDbHelper.CUST_NAME,		next[1]);
			values.put(RsaDbHelper.CUST_TEL,		next[2]);
			values.put(RsaDbHelper.CUST_ADDRESS,	next[3]);
			values.put(RsaDbHelper.CUST_OKPO,		next[4]);
			values.put(RsaDbHelper.CUST_INN,		next[5]);
			values.put(RsaDbHelper.CUST_CONTACT,	next[6]);
			values.put(RsaDbHelper.CUST_DOGOVOR,	next[7]);
			db.insert(RsaDbHelper.TABLE_CUST, RsaDbHelper.CUST_NAME, values);
			values.clear();
			//////////////////////////////////////

			// Write message to Handler: this table begin to download:
            android.os.Message hMess = mHandler.obtainMessage();
			hMess = mHandler.obtainMessage();
			data.putString("LOG", "PROGRESS");
			hMess.setData(data);
			mHandler.sendMessage(hMess);

			next = reader.readNext();
			insertCounter++;
		}
		db.execSQL("COMMIT");
		reader.close();
	}
	public static void CSVtoBrand(Context context, SQLiteDatabase db, Handler mHandler, String fPath) throws Exception {
		ContentValues values = new ContentValues();
		Bundle data = new Bundle();
		// appPath = /data/data/ru.by.rsa/files
		String appPath = fPath + File.separator + "inbox/";
		File csvFile = new File(appPath + "brand.csv");
		// init file to stream
		InputStream csvStream = new java.io.FileInputStream(csvFile);
		// parse for elements count
		int CSVCount = getCountCSV(new InputStreamReader(csvStream));
		// close for files reseting
		csvStream.close();
		// open file again for parsing
		csvStream = new java.io.FileInputStream(csvFile);
		CSVReader reader = new CSVReader(new InputStreamReader(csvStream, Charset.forName("Windows-1251")));

		preparetable(db, context, mHandler, RsaDbHelper.TABLE_BRAND, CSVCount);

		int insertCounter = 0;
        db.execSQL("BEGIN TRANSACTION");

		String next[] = {};
		next = reader.readNext();
		while (next != null) {
			if ((insertCounter%100) == 0) {
	        	db.execSQL("COMMIT");
				db.execSQL("BEGIN TRANSACTION");
	        }

			/////////////////////////////////////DB PASTE
			values.put(RsaDbHelper.BRAND_ID,	next[0]);
			values.put(RsaDbHelper.BRAND_NAME,	next[1]);
			db.insert(RsaDbHelper.TABLE_BRAND, RsaDbHelper.BRAND_NAME, values);
			values.clear();
			//////////////////////////////////////

			// Write message to Handler: this table begin to download:
            android.os.Message hMess = mHandler.obtainMessage();
			hMess = mHandler.obtainMessage();
			data.putString("LOG", "PROGRESS");
			hMess.setData(data);
			mHandler.sendMessage(hMess);

			next = reader.readNext();
			insertCounter++;
		}
		db.execSQL("COMMIT");
	}

	public static void CSVtoGroup(Context context, SQLiteDatabase db, Handler mHandler, String fPath) throws Exception {
		ContentValues values = new ContentValues();
		Bundle data = new Bundle();
		// appPath = /data/data/ru.by.rsa/files
		String appPath = fPath + File.separator + "inbox/";
		File csvFile = new File(appPath + "group.csv");
		// init file to stream
		InputStream csvStream = new java.io.FileInputStream(csvFile);
		// parse for elements count
		int CSVCount = getCountCSV(new InputStreamReader(csvStream));
		// close for files reseting
		csvStream.close();
		// open file again for parsing
		csvStream = new java.io.FileInputStream(csvFile);
		CSVReader reader = new CSVReader(new InputStreamReader(csvStream, Charset.forName("Windows-1251")));

		preparetable(db, context, mHandler, RsaDbHelper.TABLE_GROUP, CSVCount);

		int insertCounter = 0;
        db.execSQL("BEGIN TRANSACTION");

		String next[] = {};
		next = reader.readNext();
		while (next != null) {
			if ((insertCounter%100) == 0) {
	        	db.execSQL("COMMIT");
				db.execSQL("BEGIN TRANSACTION");
	        }

			/////////////////////////////////////DB PASTE
			values.put(RsaDbHelper.GROUP_ID,	next[0]);
			values.put(RsaDbHelper.GROUP_NAME,	next[1]);
			db.insert(RsaDbHelper.TABLE_GROUP, RsaDbHelper.GROUP_NAME, values);
			values.clear();
			//////////////////////////////////////

			// Write message to Handler: this table begin to download:
            android.os.Message hMess = mHandler.obtainMessage();
			hMess = mHandler.obtainMessage();
			data.putString("LOG", "PROGRESS");
			hMess.setData(data);
			mHandler.sendMessage(hMess);

			next = reader.readNext();
			insertCounter++;
		}
		db.execSQL("COMMIT");
	}

	public static void CSVtoSklad(Context context, SQLiteDatabase db, Handler mHandler, String fPath) throws Exception {
		ContentValues values = new ContentValues();
		Bundle data = new Bundle();
		// appPath = /data/data/ru.by.rsa/files
		String appPath = fPath + File.separator + "inbox/";
		File csvFile = new File(appPath + "sklad.csv");
		// init file to stream
		InputStream csvStream = new java.io.FileInputStream(csvFile);
		// parse for elements count
		int CSVCount = getCountCSV(new InputStreamReader(csvStream));
		// close for files reseting
		csvStream.close();
		// open file again for parsing
		csvStream = new java.io.FileInputStream(csvFile);
		CSVReader reader = new CSVReader(new InputStreamReader(csvStream, Charset.forName("Windows-1251")));

		preparetable(db, context, mHandler, RsaDbHelper.TABLE_SKLAD, CSVCount);

		int insertCounter = 0;
        db.execSQL("BEGIN TRANSACTION");

		String next[] = {};
		next = reader.readNext();
		while (next != null) {
			if ((insertCounter%100) == 0) {
	        	db.execSQL("COMMIT");
				db.execSQL("BEGIN TRANSACTION");
	        }

			/////////////////////////////////////DB PASTE
			values.put(RsaDbHelper.SKLAD_ID,	next[0]);
			values.put(RsaDbHelper.SKLAD_NAME,	next[1]);
			db.insert(RsaDbHelper.TABLE_SKLAD, RsaDbHelper.SKLAD_NAME, values);
			values.clear();
			//////////////////////////////////////

			// Write message to Handler: this table begin to download:
            android.os.Message hMess = mHandler.obtainMessage();
			hMess = mHandler.obtainMessage();
			data.putString("LOG", "PROGRESS");
			hMess.setData(data);
			mHandler.sendMessage(hMess);

			next = reader.readNext();
			insertCounter++;
		}
		db.execSQL("COMMIT");
	}

	public static void CSVtoShop(Context context, SQLiteDatabase db, Handler mHandler, String fPath) throws Exception {
		ContentValues values = new ContentValues();
		Bundle data = new Bundle();
		// appPath = /data/data/ru.by.rsa/files
		String appPath = fPath + File.separator + "inbox/";
		File csvFile = new File(appPath + "shop.csv");
		// init file to stream
		InputStream csvStream = new java.io.FileInputStream(csvFile);
		// parse for elements count
		int CSVCount = getCountCSV(new InputStreamReader(csvStream));
		// close for files reseting
		csvStream.close();
		// open file again for parsing
		csvStream = new java.io.FileInputStream(csvFile);
		CSVReader reader = new CSVReader(new InputStreamReader(csvStream, Charset.forName("Windows-1251")));

		preparetable(db, context, mHandler, RsaDbHelper.TABLE_SHOP, CSVCount);

		int insertCounter = 0;
        db.execSQL("BEGIN TRANSACTION");

		String next[] = {};
		next = reader.readNext();
		while (next != null) {
			if ((insertCounter%100) == 0) {
	        	db.execSQL("COMMIT");
				db.execSQL("BEGIN TRANSACTION");
	        }

			/////////////////////////////////////DB PASTE
			values.put(RsaDbHelper.SHOP_ID,			next[0]);
	        values.put(RsaDbHelper.SHOP_CUST_ID,	next[1]);
			values.put(RsaDbHelper.SHOP_NAME,		next[2]);
			values.put(RsaDbHelper.SHOP_ADDRESS,	next[3]);
			db.insert(RsaDbHelper.TABLE_SHOP, RsaDbHelper.SHOP_NAME, values);
			values.clear();
			//////////////////////////////////////

			// Write message to Handler: this table begin to download:
            android.os.Message hMess = mHandler.obtainMessage();
			hMess = mHandler.obtainMessage();
			data.putString("LOG", "PROGRESS");
			hMess.setData(data);
			mHandler.sendMessage(hMess);

			next = reader.readNext();
			insertCounter++;
		}
		db.execSQL("COMMIT");
	}

	public static void CSVtoDebit(Context context, SQLiteDatabase db, Handler mHandler, String fPath) throws Exception {
		ContentValues values = new ContentValues();
		Bundle data = new Bundle();
		// appPath = /data/data/ru.by.rsa/files
		String appPath = fPath + File.separator + "inbox/";
		File csvFile = new File(appPath + "debit.csv");
		// init file to stream
		InputStream csvStream = new java.io.FileInputStream(csvFile);
		// parse for elements count
		int CSVCount = getCountCSV(new InputStreamReader(csvStream));
		// close for files reseting
		csvStream.close();
		// open file again for parsing
		csvStream = new java.io.FileInputStream(csvFile);
		CSVReader reader = new CSVReader(new InputStreamReader(csvStream, Charset.forName("Windows-1251")));

		preparetable(db, context, mHandler, RsaDbHelper.TABLE_DEBIT, CSVCount);

		int insertCounter = 0;
        db.execSQL("BEGIN TRANSACTION");

		String next[] = {};
		next = reader.readNext();
		while (next != null) {
			if ((insertCounter%100) == 0) {
	        	db.execSQL("COMMIT");
				db.execSQL("BEGIN TRANSACTION");
	        }

			/////////////////////////////////////DB PASTE
			values.put(RsaDbHelper.DEBIT_ID,		next[0]);
	        values.put(RsaDbHelper.DEBIT_CUST_ID,	next[1]);
			values.put(RsaDbHelper.DEBIT_RN,		next[2]);
			values.put(RsaDbHelper.DEBIT_DATEDOC,	next[3]);
			values.put(RsaDbHelper.DEBIT_SUM,		normalizePrice(next[4]));

			values.put(RsaDbHelper.DEBIT_DATEPP,	dateCorrection(next[5]));
			values.put(RsaDbHelper.DEBIT_CLOSED,	next[6]);
			db.insert(RsaDbHelper.TABLE_DEBIT, RsaDbHelper.DEBIT_RN, values);
			values.clear();
			//////////////////////////////////////

			// Write message to Handler: this table begin to download:
            android.os.Message hMess = mHandler.obtainMessage();
			hMess = mHandler.obtainMessage();
			data.putString("LOG", "PROGRESS");
			hMess.setData(data);
			mHandler.sendMessage(hMess);

			next = reader.readNext();
			insertCounter++;
		}
		db.execSQL("COMMIT");
	}

	public static void CSVtoChar(Context context, SQLiteDatabase db, Handler mHandler, String fPath) throws Exception {
		ContentValues values = new ContentValues();
		Bundle data = new Bundle();
		// appPath = /data/data/ru.by.rsa/files
		String appPath = fPath + File.separator + "inbox/";
		File csvFile = new File(appPath + "char.csv");
		// init file to stream
		InputStream csvStream = new java.io.FileInputStream(csvFile);
		// parse for elements count
		int CSVCount = getCountCSV(new InputStreamReader(csvStream));
		// close for files reseting
		csvStream.close();
		// open file again for parsing
		csvStream = new java.io.FileInputStream(csvFile);
		CSVReader reader = new CSVReader(new InputStreamReader(csvStream, Charset.forName("Windows-1251")));

		preparetable(db, context, mHandler, RsaDbHelper.TABLE_CHAR, CSVCount);

		int insertCounter = 0;
        db.execSQL("BEGIN TRANSACTION");

		String next[] = {};
		next = reader.readNext();
		while (next != null) {
			if ((insertCounter%100) == 0) {
	        	db.execSQL("COMMIT");
				db.execSQL("BEGIN TRANSACTION");
	        }

			/////////////////////////////////////DB PASTE
			values.put(RsaDbHelper.CHAR_ID,			next[0]);
	        values.put(RsaDbHelper.CHAR_CUST_ID,	next[1]);
			values.put(RsaDbHelper.CHAR_BRAND_ID,	next[2]);
			values.put(RsaDbHelper.CHAR_DISCOUNT,	next[3]);
			values.put(RsaDbHelper.CHAR_DELAY,		next[4]);
			values.put(RsaDbHelper.CHAR_PRICE,		next[5]);
			db.insert(RsaDbHelper.TABLE_CHAR, RsaDbHelper.CHAR_ID, values);
			values.clear();
			//////////////////////////////////////

			// Write message to Handler: this table begin to download:
            android.os.Message hMess = mHandler.obtainMessage();
			hMess = mHandler.obtainMessage();
			data.putString("LOG", "PROGRESS");
			hMess.setData(data);
			mHandler.sendMessage(hMess);

			next = reader.readNext();
			insertCounter++;
		}
		db.execSQL("COMMIT");
	}

	public static void CSVtoPlan(Context context, SQLiteDatabase db, Handler mHandler, String fPath) throws Exception {
		ContentValues values = new ContentValues();
		Bundle data = new Bundle();
		// appPath = /data/data/ru.by.rsa/files
		String appPath = fPath + File.separator + "inbox/";
		File csvFile = new File(appPath + "plan.csv");
		// init file to stream
		InputStream csvStream = new java.io.FileInputStream(csvFile);
		// parse for elements count
		int CSVCount = getCountCSV(new InputStreamReader(csvStream));
		// close for files reseting
		csvStream.close();
		// open file again for parsing
		csvStream = new java.io.FileInputStream(csvFile);
		CSVReader reader = new CSVReader(new InputStreamReader(csvStream, Charset.forName("Windows-1251")));

		preparetable(db, context, mHandler, RsaDbHelper.TABLE_PLAN, CSVCount);

		int insertCounter = 0;
        db.execSQL("BEGIN TRANSACTION");

		String next[] = {};
		next = reader.readNext();
		while (next != null) {
			if ((insertCounter%100) == 0) {
	        	db.execSQL("COMMIT");
				db.execSQL("BEGIN TRANSACTION");
	        }

			/////////////////////////////////////DB PASTE
			values.put(RsaDbHelper.PLAN_ID,			next[0]);
	        values.put(RsaDbHelper.PLAN_CUST_ID,	next[1]);
			values.put(RsaDbHelper.PLAN_SHOP_ID,	next[2]);
			values.put(RsaDbHelper.PLAN_CUST_TEXT,	next[3]);
			values.put(RsaDbHelper.PLAN_SHOP_TEXT,	next[4]);
			values.put(RsaDbHelper.PLAN_DATEV,		next[5]);
			values.put(RsaDbHelper.PLAN_STATE,		next[6]);
			db.insert(RsaDbHelper.TABLE_PLAN, RsaDbHelper.PLAN_ID, values);
			values.clear();
			//////////////////////////////////////

			// Write message to Handler: this table begin to download:
            android.os.Message hMess = mHandler.obtainMessage();
			hMess = mHandler.obtainMessage();
			data.putString("LOG", "PROGRESS");
			hMess.setData(data);
			mHandler.sendMessage(hMess);

			next = reader.readNext();
			insertCounter++;
		}
		db.execSQL("COMMIT");
	}

	public static void CSVtoGoods(Context context, SQLiteDatabase db, Handler mHandler, String fPath) throws Exception {
		ContentValues values = new ContentValues();
		Bundle data = new Bundle();
		// appPath = /data/data/ru.by.rsa/files
		String appPath = fPath + File.separator + "inbox/";
		File csvFile = new File(appPath + "goods.csv");
		// init file to stream
		InputStream csvStream = new java.io.FileInputStream(csvFile);
		// parse for elements count
		int CSVCount = getCountCSV(new InputStreamReader(csvStream));
		// close for files reseting
		csvStream.close();
		// open file again for parsing
		csvStream = new java.io.FileInputStream(csvFile);
		CSVReader reader = new CSVReader(new InputStreamReader(csvStream, Charset.forName("Windows-1251")));

		preparetable(db, context, mHandler, RsaDbHelper.TABLE_GOODS, CSVCount);

		int insertCounter = 0;
        db.execSQL("BEGIN TRANSACTION");

		String next[] = {};
		next = reader.readNext();
		while (next != null) {
			if ((insertCounter%100) == 0) {
	        	db.execSQL("COMMIT");
				db.execSQL("BEGIN TRANSACTION");
	        }

			/////////////////////////////////////DB PASTE
			values.put(RsaDbHelper.GOODS_ID,		next[0]);
			values.put(RsaDbHelper.GOODS_NPP,		next[1]);
			values.put(RsaDbHelper.GOODS_NAME,		next[2]);
			values.put(RsaDbHelper.GOODS_BRAND_ID,	next[3]);
			values.put(RsaDbHelper.GOODS_QTY,		normalizeQty(next[4],"1"));
			values.put(RsaDbHelper.GOODS_RESTCUST,	next[5]);
			values.put(RsaDbHelper.GOODS_REST,		normalizeRest(next[6]));
			values.put(RsaDbHelper.GOODS_HIST1,		next[7]);
			values.put(RsaDbHelper.GOODS_RESTCUST1,	next[8]);
			values.put(RsaDbHelper.GOODS_HIST2,		next[9]);
			values.put(RsaDbHelper.GOODS_RESTCUST2,	next[10]);
			values.put(RsaDbHelper.GOODS_HIST3,		next[11]);
			values.put(RsaDbHelper.GOODS_GROUP_ID,	next[12]);
			values.put(RsaDbHelper.GOODS_PRICE1,	normalizePrice(next[13]));
			values.put(RsaDbHelper.GOODS_PRICE2,	normalizePrice(next[14]));
			values.put(RsaDbHelper.GOODS_PRICE3,	normalizePrice(next[15]));
			values.put(RsaDbHelper.GOODS_PRICE4,	normalizePrice(next[16]));
			values.put(RsaDbHelper.GOODS_PRICE5,	normalizePrice(next[17]));
			values.put(RsaDbHelper.GOODS_PRICE6,	normalizePrice(next[18]));
			values.put(RsaDbHelper.GOODS_PRICE7,	normalizePrice(next[19]));
			values.put(RsaDbHelper.GOODS_PRICE8,	normalizePrice(next[20]));
			values.put(RsaDbHelper.GOODS_PRICE9,	normalizePrice(next[21]));
			values.put(RsaDbHelper.GOODS_PRICE10,	normalizePrice(next[22]));
			values.put(RsaDbHelper.GOODS_PRICE11,	normalizePrice(next[23]));
			values.put(RsaDbHelper.GOODS_PRICE12,	normalizePrice(next[24]));
			values.put(RsaDbHelper.GOODS_PRICE13,	normalizePrice(next[25]));
			values.put(RsaDbHelper.GOODS_PRICE14,	normalizePrice(next[26]));
			values.put(RsaDbHelper.GOODS_PRICE15,	normalizePrice(next[27]));
			values.put(RsaDbHelper.GOODS_PRICE16,	normalizePrice(next[28]));
			values.put(RsaDbHelper.GOODS_PRICE17,	normalizePrice(next[29]));
			values.put(RsaDbHelper.GOODS_PRICE18,	normalizePrice(next[30]));
			values.put(RsaDbHelper.GOODS_PRICE19,	normalizePrice(next[31]));
			values.put(RsaDbHelper.GOODS_PRICE20,	normalizePrice(next[32]));
			values.put(RsaDbHelper.GOODS_DISCOUNT,	next[33]);
			values.put(RsaDbHelper.GOODS_PRICEWNDS,	next[34]);
			values.put(RsaDbHelper.GOODS_PRICEWONDS,next[35]);
			values.put(RsaDbHelper.GOODS_UN,		next[36]);
			values.put(RsaDbHelper.GOODS_COEFF,		next[37]);
			values.put(RsaDbHelper.GOODS_SUMWONDS,	next[38]);
			values.put(RsaDbHelper.GOODS_SUMWNDS,	next[39]);
			values.put(RsaDbHelper.GOODS_WEIGHT1,	normalizeRest(next[40]));
			values.put(RsaDbHelper.GOODS_WEIGHT,	next[41]);
			values.put(RsaDbHelper.GOODS_VOLUME1,	next[42]);
			values.put(RsaDbHelper.GOODS_VOLUME,	next[43]);
			values.put(RsaDbHelper.GOODS_NDS,		next[44]);
			values.put(RsaDbHelper.GOODS_DATE,		next[45]);
			values.put(RsaDbHelper.GOODS_FLASH,		normalizeFlash(next[46]));
			db.insert(RsaDbHelper.TABLE_GOODS, RsaDbHelper.GOODS_NAME, values);
			values.clear();
			//////////////////////////////////////

			// Write message to Handler: this table begin to download:
            android.os.Message hMess = mHandler.obtainMessage();
			hMess = mHandler.obtainMessage();
			data.putString("LOG", "PROGRESS");
			hMess.setData(data);
			mHandler.sendMessage(hMess);

			next = reader.readNext();
			insertCounter++;
		}
		db.execSQL("COMMIT");
	}

	public static void CSVtoFTP(Context context, SQLiteDatabase db, Handler mHandler, String fPath) throws Exception {
		ContentValues values = new ContentValues();
		Bundle data = new Bundle();
		// appPath = /data/data/ru.by.rsa/files
		String appPath = fPath + File.separator + "inbox/";
		File csvFile = new File(appPath + "workinf.csv");
		// parse for elements count
		int CSVCount = 1;
		// open file again for parsing
		InputStream csvStream = new java.io.FileInputStream(csvFile);
		CSVReader reader = new CSVReader(new InputStreamReader(csvStream, Charset.forName("Windows-1251")));

		preparetable(db, context, mHandler, "WORKINF", CSVCount);

		String next[] = {};
		next = reader.readNext();
		if (next != null) {

			/////////////////////////////////////DB PASTE
			// Get Shared Preferences
			SharedPreferences prefs = context.getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);

			prefs.edit().putString(RsaDb.EMAILKEY,		next[4]).commit();
			prefs.edit().putString(RsaDb.PASSWORDKEY,	next[5]).commit();
			prefs.edit().putString(RsaDb.SMTPKEY,		next[2]).commit();
			prefs.edit().putString(RsaDb.SMTPPORTKEY,	next[3]).commit();
			prefs.edit().putString(RsaDb.POPKEY,		next[8]).commit();
			prefs.edit().putString(RsaDb.POPPORTKEY,	next[9]).commit();
			prefs.edit().putString(RsaDb.SENDTOKEY,		next[7]).commit();

			prefs.edit().putString(RsaDb.FTPUSER,		next[4]).commit();
			prefs.edit().putString(RsaDb.FTPPASSWORD,	next[5]).commit();
			prefs.edit().putString(RsaDb.FTPSERVER,		next[2]).commit();
			prefs.edit().putString(RsaDb.FTPPORT,		next[3]).commit();
			prefs.edit().putString(RsaDb.FTPINBOX,		next[6]).commit();
			prefs.edit().putString(RsaDb.FTPOUTBOX,		next[7]).commit();

			SharedPreferences screen_prefs = context.getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE);
			screen_prefs.edit().putString(RsaDb.CODEKEY, next[13]).commit();
			screen_prefs.edit().putString(RsaDb.NAMEKEY, next[12]).commit();
			//////////////////////////////////////

			// Write message to Handler: this table begin to download:
            android.os.Message hMess = mHandler.obtainMessage();
			hMess = mHandler.obtainMessage();
			data.putString("LOG", "PROGRESS");
			hMess.setData(data);
			mHandler.sendMessage(hMess);

			next = reader.readNext();
		}
	}

 	public static void XMLtoCust(Context context, SQLiteDatabase db, Handler mHandler, String fPath) throws Exception
	{
			ContentValues values = new ContentValues();
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
	        factory.setNamespaceAware(true);
			XmlPullParser parser = factory.newPullParser();
			Bundle data = new Bundle();

			/** appPath = /data/data/ru.by.rsa/files */
			String appPath = fPath + File.separator + "inbox/";

			File xmlFile = new File(appPath + "cust.xml");
			InputStream xmlStream = new java.io.FileInputStream(xmlFile);

			parser.setInput(xmlStream, null); // Go to begin of file for item counting

        	preparetable(db, context, mHandler, RsaDbHelper.TABLE_CUST, getCountXML(parser));

        	xmlStream.close();
        	xmlStream = new java.io.FileInputStream(xmlFile);

        	parser.setInput(xmlStream, null);  // Go to begin of file for parsing

        	int eventType = parser.getEventType();
        	int insertCounter = 0;
            db.execSQL("BEGIN TRANSACTION");

        	while (eventType != XmlPullParser.END_DOCUMENT)
			{
        		if ((insertCounter%100) == 0)
		        {
		        	db.execSQL("COMMIT");
					db.execSQL("BEGIN TRANSACTION");
		        }

			    if ((eventType == XmlPullParser.START_TAG)
			    		&& parser.getName().toLowerCase().equals("item"))
			    {
			        values.put(RsaDbHelper.CUST_ID,			parser.getAttributeValue(0));
					values.put(RsaDbHelper.CUST_NAME,		parser.getAttributeValue(1));
					values.put(RsaDbHelper.CUST_TEL,		parser.getAttributeValue(2));
					values.put(RsaDbHelper.CUST_ADDRESS,	parser.getAttributeValue(3));
					values.put(RsaDbHelper.CUST_OKPO,		parser.getAttributeValue(4));
					values.put(RsaDbHelper.CUST_INN,		parser.getAttributeValue(5));
					values.put(RsaDbHelper.CUST_CONTACT,	parser.getAttributeValue(6));
					values.put(RsaDbHelper.CUST_DOGOVOR,	parser.getAttributeValue(7));

					if ((parser.getAttributeCount()>8)
							&& parser.getAttributeName(8).equals("COMMENT"))
						values.put(RsaDbHelper.CUST_COMMENT, parser.getAttributeValue(8));

					db.insert(RsaDbHelper.TABLE_CUST, RsaDbHelper.CUST_NAME, values);
					values.clear();

					// Write message to Handler: this table begin to download:
		            android.os.Message hMess = mHandler.obtainMessage();
					hMess = mHandler.obtainMessage();
					data.putString("LOG", "PROGRESS");
					hMess.setData(data);
					mHandler.sendMessage(hMess);
			    }
			    eventType = parser.next();

			    insertCounter++;
			}
        	db.execSQL("COMMIT");
	}


	public static void XMLtoBrand(Context context, SQLiteDatabase db, Handler mHandler, String fPath) throws Exception
	{
			ContentValues values = new ContentValues();
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
	        factory.setNamespaceAware(true);
			XmlPullParser parser = factory.newPullParser();
			Bundle data = new Bundle();

			/** appPath = /data/data/ru.by.rsa/files */
			String appPath = fPath + File.separator + "inbox/";

			File xmlFile = new File(appPath + "brand.xml");
			InputStream xmlStream = new java.io.FileInputStream(xmlFile);

			parser.setInput(xmlStream, null);//РАЗОБРАТЬ С ЗАГОЛОВКОМ ИКСЭМЭЛЯ
        	preparetable(db, context, mHandler, RsaDbHelper.TABLE_BRAND, getCountXML(parser));

        	xmlStream.close();
        	xmlStream = new java.io.FileInputStream(xmlFile);

			parser.setInput(xmlStream, null);//РАЗОБРАТЬ С ЗАГОЛОВКОМ ИКСЭМЭЛЯ

			int eventType = parser.getEventType();
			int insertCounter = 0;
            db.execSQL("BEGIN TRANSACTION");

        	while (eventType != XmlPullParser.END_DOCUMENT)
			{
        		if ((insertCounter%100) == 0)
		        {
		        	db.execSQL("COMMIT");
					db.execSQL("BEGIN TRANSACTION");
		        }

			    if ((eventType == XmlPullParser.START_TAG)
			    		&& parser.getName().toLowerCase().equals("item"))
			    {
			        values.put(RsaDbHelper.BRAND_ID,	parser.getAttributeValue(0));
					values.put(RsaDbHelper.BRAND_NAME,	parser.getAttributeValue(1));

					if (parser.getAttributeCount()>2) {
						if (parser.getAttributeName(2).equals("COMMENT"))
							values.put(RsaDbHelper.BRAND_COMMENT,	parser.getAttributeValue(2));
						if (parser.getAttributeName(2).equals("CPRICE"))
							values.put(RsaDbHelper.BRAND_CPRICE,	parser.getAttributeValue(2));
					}

					if (parser.getAttributeCount()>3) {
						if (parser.getAttributeName(3).equals("COMMENT"))
							values.put(RsaDbHelper.BRAND_COMMENT,	parser.getAttributeValue(3));
						if (parser.getAttributeName(3).equals("CPRICE"))
							values.put(RsaDbHelper.BRAND_CPRICE,	parser.getAttributeValue(3));
					}

					db.insert(RsaDbHelper.TABLE_BRAND, RsaDbHelper.BRAND_NAME, values);
					values.clear();

					// Write message to Handler: this table begin to download:
		            android.os.Message hMess = mHandler.obtainMessage();
					hMess = mHandler.obtainMessage();
					data.putString("LOG", "PROGRESS");
					hMess.setData(data);
					mHandler.sendMessage(hMess);
			    }
			    eventType = parser.next();
			    insertCounter++;
			}
        	db.execSQL("COMMIT");

	}


	public static void XMLtoGroup(Context context, SQLiteDatabase db, Handler mHandler, String fPath) throws Exception
	{
			ContentValues values = new ContentValues();
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
	        factory.setNamespaceAware(true);
			XmlPullParser parser = factory.newPullParser();
			Bundle data = new Bundle();

			/** appPath = /data/data/ru.by.rsa/files */
			String appPath = fPath + File.separator + "inbox/";

			File xmlFile = new File(appPath + "group.xml");
			InputStream xmlStream = new java.io.FileInputStream(xmlFile);

			parser.setInput(xmlStream, null);//РАЗОБРАТЬ С ЗАГОЛОВКОМ ИКСЭМЭЛЯ

        	preparetable(db, context, mHandler, RsaDbHelper.TABLE_GROUP, getCountXML(parser));

        	xmlStream.close();
        	xmlStream = new java.io.FileInputStream(xmlFile);

			parser.setInput(xmlStream, null);//РАЗОБРАТЬ С ЗАГОЛОВКОМ ИКСЭМЭЛЯ

			int eventType = parser.getEventType();
			int insertCounter = 0;
            db.execSQL("BEGIN TRANSACTION");

        	while (eventType != XmlPullParser.END_DOCUMENT)
			{
        		if ((insertCounter%100) == 0)
		        {
		        	db.execSQL("COMMIT");
					db.execSQL("BEGIN TRANSACTION");
		        }
			    if ((eventType == XmlPullParser.START_TAG)
			    		&& parser.getName().toLowerCase().equals("item"))
			    {
			        values.put(RsaDbHelper.GROUP_ID,		parser.getAttributeValue(0));
					values.put(RsaDbHelper.GROUP_NAME,		parser.getAttributeValue(1));

					if (parser.getAttributeCount()>2) {
						String _cc = parser.getAttributeName(2);
						if (_cc.equals("COMMENT")||_cc.equals("comment")) {
							values.put(RsaDbHelper.GROUP_COMMENT,	parser.getAttributeValue(2));
						} else {
							values.put(RsaDbHelper.GROUP_BRAND_ID,	parser.getAttributeValue(2));
						}
					}

					if (parser.getAttributeCount()>3) {
						String _cc = parser.getAttributeName(3);
						if (_cc.equals("BRAND_ID")||_cc.equals("brand_id")) {
							values.put(RsaDbHelper.GROUP_BRAND_ID,	parser.getAttributeValue(3));
						} else {
							values.put(RsaDbHelper.GROUP_PARENT_NAME,	parser.getAttributeValue(3));
						}
					}



					db.insert(RsaDbHelper.TABLE_GROUP, RsaDbHelper.GROUP_NAME, values);
					values.clear();

					// Write message to Handler: this table begin to download:
		            android.os.Message hMess = mHandler.obtainMessage();
					hMess = mHandler.obtainMessage();
					data.putString("LOG", "PROGRESS");
					hMess.setData(data);
					mHandler.sendMessage(hMess);
			    }
			    eventType = parser.next();
			    insertCounter++;
			}
        	db.execSQL("COMMIT");

	}


	public static void XMLtoSklad(Context context, SQLiteDatabase db, Handler mHandler, String fPath) throws Exception
	{
			ContentValues values = new ContentValues();
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
	        factory.setNamespaceAware(true);
			XmlPullParser parser = factory.newPullParser();
			Bundle data = new Bundle();

			/** appPath = /data/data/ru.by.rsa/files */
			String appPath = fPath + File.separator + "inbox/";

			File xmlFile = new File(appPath + "sklad.xml");
			InputStream xmlStream = new java.io.FileInputStream(xmlFile);

			parser.setInput(xmlStream, null);//РАЗОБРАТЬ С ЗАГОЛОВКОМ ИКСЭМЭЛЯ

        	preparetable(db, context, mHandler, RsaDbHelper.TABLE_SKLAD, getCountXML(parser));

        	xmlStream.close();
        	xmlStream = new java.io.FileInputStream(xmlFile);

			parser.setInput(xmlStream, null);//РАЗОБРАТЬ С ЗАГОЛОВКОМ ИКСЭМЭЛЯ

			int eventType = parser.getEventType();
			int insertCounter = 0;
            db.execSQL("BEGIN TRANSACTION");

        	while (eventType != XmlPullParser.END_DOCUMENT)
			{
        		if ((insertCounter%100) == 0)
		        {
		        	db.execSQL("COMMIT");
					db.execSQL("BEGIN TRANSACTION");
		        }
			    if ((eventType == XmlPullParser.START_TAG)
			    		&& parser.getName().toLowerCase().equals("item"))
			    {
			        values.put(RsaDbHelper.SKLAD_ID,	parser.getAttributeValue(0));
					values.put(RsaDbHelper.SKLAD_NAME,	parser.getAttributeValue(1));
					db.insert(RsaDbHelper.TABLE_SKLAD, RsaDbHelper.SKLAD_NAME, values);
					values.clear();

					// Write message to Handler: this table begin to download:
		            android.os.Message hMess = mHandler.obtainMessage();
					hMess = mHandler.obtainMessage();
					data.putString("LOG", "PROGRESS");
					hMess.setData(data);
					mHandler.sendMessage(hMess);
			    }
			    eventType = parser.next();
			    insertCounter++;
			}
        	db.execSQL("COMMIT");

	}

	public static void XMLtoSkladDet(Context context, SQLiteDatabase db, Handler mHandler, String fPath) throws Exception
	{
			ContentValues values = new ContentValues();
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
	        factory.setNamespaceAware(true);
			XmlPullParser parser = factory.newPullParser();
			Bundle data = new Bundle();

			/** appPath = /data/data/ru.by.rsa/files */
			String appPath = fPath + File.separator + "inbox/";

			File xmlFile = new File(appPath + "skladdet.xml");
			InputStream xmlStream = new java.io.FileInputStream(xmlFile);

			parser.setInput(xmlStream, null);//РАЗОБРАТЬ С ЗАГОЛОВКОМ ИКСЭМЭЛЯ

        	preparetable(db, context, mHandler, RsaDbHelper.TABLE_SKLADDET, getCountXML(parser));

        	xmlStream.close();
        	xmlStream = new java.io.FileInputStream(xmlFile);

			parser.setInput(xmlStream, null);//РАЗОБРАТЬ С ЗАГОЛОВКОМ ИКСЭМЭЛЯ

			int eventType = parser.getEventType();
			int insertCounter = 0;
            db.execSQL("BEGIN TRANSACTION");

        	while (eventType != XmlPullParser.END_DOCUMENT)
			{
        		if ((insertCounter%100) == 0)
		        {
		        	db.execSQL("COMMIT");
					db.execSQL("BEGIN TRANSACTION");
		        }
			    if ((eventType == XmlPullParser.START_TAG)
			    		&& parser.getName().toLowerCase().equals("item"))
			    {
			        values.put(RsaDbHelper.SKLAD_SKLAD_ID,	parser.getAttributeValue(0));
					values.put(RsaDbHelper.SKLAD_GOODS_ID,	parser.getAttributeValue(1));
					values.put(RsaDbHelper.SKLAD_FUNC_1,	parser.getAttributeValue(2));
					values.put(RsaDbHelper.SKLAD_FUNC_2,	parser.getAttributeValue(3));

					db.insert(RsaDbHelper.TABLE_SKLADDET, RsaDbHelper.SKLAD_SKLAD_ID, values);
					values.clear();

					// Write message to Handler: this table begin to download:
		            android.os.Message hMess = mHandler.obtainMessage();
					hMess = mHandler.obtainMessage();
					data.putString("LOG", "PROGRESS");
					hMess.setData(data);
					mHandler.sendMessage(hMess);
			    }
			    eventType = parser.next();
			    insertCounter++;
			}
        	db.execSQL("COMMIT");

        	SharedPreferences def_prefs = PreferenceManager.getDefaultSharedPreferences(context);
			def_prefs.edit().putBoolean("prefSkladdet", true).commit();

	}

	public static void XMLtoStaticPlan(Context context, SQLiteDatabase db, Handler mHandler, String fPath) throws Exception
	{
		ContentValues values = new ContentValues();
		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		factory.setNamespaceAware(true);
		XmlPullParser parser = factory.newPullParser();
		Bundle data = new Bundle();

		/** appPath = /data/data/ru.by.rsa/files */
		String appPath = fPath + File.separator + "inbox/";

		File xmlFile = new File(appPath + "statplan.xml");
		InputStream xmlStream = new java.io.FileInputStream(xmlFile);

		parser.setInput(xmlStream, null);//РАЗОБРАТЬ С ЗАГОЛОВКОМ ИКСЭМЭЛЯ

		preparetable(db, context, mHandler, RsaDbHelper.TABLE_STATIC_PLAN, getCountXML(parser));

		xmlStream.close();
		xmlStream = new java.io.FileInputStream(xmlFile);

		parser.setInput(xmlStream, null);//РАЗОБРАТЬ С ЗАГОЛОВКОМ ИКСЭМЭЛЯ

		int eventType = parser.getEventType();
		int insertCounter = 0;
		db.execSQL("BEGIN TRANSACTION");

		boolean isShowingPlanQty = false;
		String tempValue;

		while (eventType != XmlPullParser.END_DOCUMENT)
		{
			if ((insertCounter%100) == 0)
			{
				db.execSQL("COMMIT");
				db.execSQL("BEGIN TRANSACTION");
			}
			if ((eventType == XmlPullParser.START_TAG)
					&& parser.getName().toLowerCase().equals("item"))
			{
				values.put(RsaDbHelper.STATIC_DATETIME,	parser.getAttributeValue(0));
				values.put(RsaDbHelper.STATIC_CUST_ID,	parser.getAttributeValue(1));
				values.put(RsaDbHelper.STATIC_SHOP_ID,	parser.getAttributeValue(2));
				values.put(RsaDbHelper.STATIC_BRAND_ID,	parser.getAttributeValue(3));
				values.put(RsaDbHelper.STATIC_PLAN_SUM,	parser.getAttributeValue(4));
				values.put(RsaDbHelper.STATIC_PLAN_TOP_QTY,	parser.getAttributeValue(5));
				tempValue = parser.getAttributeValue(6);
				if (!TextUtils.isEmpty(tempValue)) {
					isShowingPlanQty = true;
				}
				values.put(RsaDbHelper.STATIC_PLAN_QTY,	tempValue);
				values.put(RsaDbHelper.STATIC_ACT_SUM,	parser.getAttributeValue(7));
				values.put(RsaDbHelper.STATIC_ACT_TOP_QTY,	parser.getAttributeValue(8));
				values.put(RsaDbHelper.STATIC_ACT_QTY,	parser.getAttributeValue(9));
				values.put(RsaDbHelper.STATIC_REST_SUM,	parser.getAttributeValue(10));
				values.put(RsaDbHelper.STATIC_REST_TOP_QTY,	parser.getAttributeValue(11));
				values.put(RsaDbHelper.STATIC_REST_QTY,	parser.getAttributeValue(12));
				values.put(RsaDbHelper.STATIC_TOTALS,	parser.getAttributeValue(13));

				db.insert(RsaDbHelper.TABLE_STATIC_PLAN, RsaDbHelper.STATIC_CUST_ID, values);
				values.clear();

				// Write message to Handler: this table begin to download:
				android.os.Message hMess = mHandler.obtainMessage();
				data.putString("LOG", "PROGRESS");
				hMess.setData(data);
				mHandler.sendMessage(hMess);
			}
			eventType = parser.next();
			insertCounter++;
		}
		db.execSQL("COMMIT");

		SharedPreferences def_prefs = PreferenceManager.getDefaultSharedPreferences(context);
		def_prefs.edit().putBoolean("prefStatPlan", true).commit();
		def_prefs.edit().putBoolean("prefStatPlanShowQty", isShowingPlanQty).commit();
	}


	public static void XMLtoShop(Context context, SQLiteDatabase db, Handler mHandler, String fPath) throws Exception
	{
			ContentValues values = new ContentValues();
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
	        factory.setNamespaceAware(true);
			XmlPullParser parser = factory.newPullParser();
			Bundle data = new Bundle();

			/** appPath = /data/data/ru.by.rsa/files */
			String appPath = fPath + File.separator + "inbox/";

			File xmlFile = new File(appPath + "shop.xml");
			InputStream xmlStream = new java.io.FileInputStream(xmlFile);

			parser.setInput(xmlStream, null);//РАЗОБРАТЬ С ЗАГОЛОВКОМ ИКСЭМЭЛЯ

        	preparetable(db, context, mHandler, RsaDbHelper.TABLE_SHOP, getCountXML(parser));

        	xmlStream.close();
        	xmlStream = new java.io.FileInputStream(xmlFile);

			parser.setInput(xmlStream, null);//РАЗОБРАТЬ С ЗАГОЛОВКОМ ИКСЭМЭЛЯ

			int eventType = parser.getEventType();
			int insertCounter = 0;
            db.execSQL("BEGIN TRANSACTION");

        	while (eventType != XmlPullParser.END_DOCUMENT)
			{
        		if ((insertCounter%100) == 0)
		        {
		        	db.execSQL("COMMIT");
					db.execSQL("BEGIN TRANSACTION");
		        }
			    if ((eventType == XmlPullParser.START_TAG)
			    		&& parser.getName().toLowerCase().equals("item"))
			    {
			        values.put(RsaDbHelper.SHOP_ID,			parser.getAttributeValue(0));
			        values.put(RsaDbHelper.SHOP_CUST_ID,	parser.getAttributeValue(1));
					values.put(RsaDbHelper.SHOP_NAME,		parser.getAttributeValue(2));
					values.put(RsaDbHelper.SHOP_ADDRESS,	parser.getAttributeValue(3));
					if (parser.getAttributeCount()>4) {
						values.put(RsaDbHelper.SHOP_TYPE,	parser.getAttributeValue(4));
					}
					if (parser.getAttributeCount()>4) {
						for (int i=4;i<parser.getAttributeCount();i++) {
							if (parser.getAttributeName(i).toUpperCase().equals("TYPE")) {
								values.put(RsaDbHelper.SHOP_TYPE,	parser.getAttributeValue(i));
							} else if (parser.getAttributeName(i).toUpperCase().equals("CONTACT")) {
								values.put(RsaDbHelper.SHOP_CONTACT,	parser.getAttributeValue(i));
							} else if (parser.getAttributeName(i).toUpperCase().equals("TEL")) {
								values.put(RsaDbHelper.SHOP_TEL,	parser.getAttributeValue(i));
							}
						}
					}

					String gps = parser.getAttributeValue(null, "GPS");
					String photo = parser.getAttributeValue(null, "PHOTO");

					values.put(RsaDbHelper.SHOP_GPS, gps==null?"":gps);
					values.put(RsaDbHelper.SHOP_PHOTO, photo==null?"0":photo);


					db.insert(RsaDbHelper.TABLE_SHOP, RsaDbHelper.SHOP_NAME, values);
					values.clear();

					// Write message to Handler: this table begin to download:
		            android.os.Message hMess = mHandler.obtainMessage();
					hMess = mHandler.obtainMessage();
					data.putString("LOG", "PROGRESS");
					hMess.setData(data);
					mHandler.sendMessage(hMess);
			    }
			    eventType = parser.next();
			    insertCounter++;
			}
        	db.execSQL("COMMIT");

	}


	public static void XMLtoDebit(Context context, SQLiteDatabase db, Handler mHandler, String fPath) throws Exception
	{
			ContentValues values = new ContentValues();
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
	        factory.setNamespaceAware(true);
			XmlPullParser parser = factory.newPullParser();
			Bundle data = new Bundle();

			/** appPath = /data/data/ru.by.rsa/files */
			String appPath = fPath + File.separator + "inbox/";

			File xmlFile = new File(appPath + "debit.xml");
			InputStream xmlStream = new java.io.FileInputStream(xmlFile);

			parser.setInput(xmlStream, null);//РАЗОБРАТЬ С ЗАГОЛОВКОМ ИКСЭМЭЛЯ

        	preparetable(db, context, mHandler, RsaDbHelper.TABLE_DEBIT, getCountXML(parser));

        	xmlStream.close();
        	xmlStream = new java.io.FileInputStream(xmlFile);

			parser.setInput(xmlStream, null);//РАЗОБРАТЬ С ЗАГОЛОВКОМ ИКСЭМЭЛЯ

			int eventType = parser.getEventType();
			int insertCounter = 0;
            db.execSQL("BEGIN TRANSACTION");

        	while (eventType != XmlPullParser.END_DOCUMENT)
			{
        		if ((insertCounter%100) == 0)
		        {
		        	db.execSQL("COMMIT");
					db.execSQL("BEGIN TRANSACTION");
		        }
			    if ((eventType == XmlPullParser.START_TAG)
			    		&& parser.getName().toLowerCase().equals("item"))
			    {
			        values.put(RsaDbHelper.DEBIT_ID,		parser.getAttributeValue(0));
			        values.put(RsaDbHelper.DEBIT_CUST_ID,	parser.getAttributeValue(1));
					values.put(RsaDbHelper.DEBIT_RN,		parser.getAttributeValue(2));
					values.put(RsaDbHelper.DEBIT_DATEDOC,	parser.getAttributeValue(3));
					values.put(RsaDbHelper.DEBIT_SUM,		normalizePrice(
							parser.getAttributeValue(4)));
					values.put(RsaDbHelper.DEBIT_DATEPP,	dateCorrection(
							parser.getAttributeValue(5)));
					values.put(RsaDbHelper.DEBIT_CLOSED,	parser.getAttributeValue(6));
					if (parser.getAttributeCount()>7) {
						for (int i=7;i<parser.getAttributeCount();i++) {
							if (parser.getAttributeName(i).toUpperCase().equals("SHOP_ID")) {
								values.put(RsaDbHelper.DEBIT_SHOP_ID,	parser.getAttributeValue(i));
							} else if (parser.getAttributeName(i).toUpperCase().equals("PAYMENT")) {
								values.put(RsaDbHelper.DEBIT_PAYMENT,	parser.getAttributeValue(i));
							} else {
								values.put(RsaDbHelper.DEBIT_COMMENT,	parser.getAttributeValue(i));
							}
						}
					}
					db.insert(RsaDbHelper.TABLE_DEBIT, RsaDbHelper.DEBIT_RN, values);
					values.clear();

					// Write message to Handler: this table begin to download:
		            android.os.Message hMess = mHandler.obtainMessage();
					hMess = mHandler.obtainMessage();
					data.putString("LOG", "PROGRESS");
					hMess.setData(data);
					mHandler.sendMessage(hMess);
			    }
			    eventType = parser.next();
			    insertCounter++;
			}
        	db.execSQL("COMMIT");

	}


	public static void XMLtoChar(Context context, SQLiteDatabase db, Handler mHandler, String fPath) throws Exception
	{
			ContentValues values = new ContentValues();
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
	        factory.setNamespaceAware(true);
			XmlPullParser parser = factory.newPullParser();
			Bundle data = new Bundle();

			/** appPath = /data/data/ru.by.rsa/files */
			String appPath = fPath + File.separator + "inbox/";

			File xmlFile = new File(appPath + "char.xml");
			InputStream xmlStream = new java.io.FileInputStream(xmlFile);

			parser.setInput(xmlStream, null);//РАЗОБРАТЬ С ЗАГОЛОВКОМ ИКСЭМЭЛЯ

        	preparetable(db, context, mHandler, RsaDbHelper.TABLE_CHAR, getCountXML(parser));

        	xmlStream.close();
        	xmlStream = new java.io.FileInputStream(xmlFile);

			parser.setInput(xmlStream, null);//РАЗОБРАТЬ С ЗАГОЛОВКОМ ИКСЭМЭЛЯ

			int eventType = parser.getEventType();
			int insertCounter = 0;
            db.execSQL("BEGIN TRANSACTION");

        	while (eventType != XmlPullParser.END_DOCUMENT)
			{
        		if ((insertCounter%100) == 0)
		        {
		        	db.execSQL("COMMIT");
					db.execSQL("BEGIN TRANSACTION");
		        }
			    if ((eventType == XmlPullParser.START_TAG)
			    		&& parser.getName().toLowerCase().equals("item"))
			    {
			        values.put(RsaDbHelper.CHAR_ID,			parser.getAttributeValue(0));
			        values.put(RsaDbHelper.CHAR_CUST_ID,	parser.getAttributeValue(1));
					values.put(RsaDbHelper.CHAR_BRAND_ID,	parser.getAttributeValue(2));
					values.put(RsaDbHelper.CHAR_DISCOUNT,	parser.getAttributeValue(3));
					values.put(RsaDbHelper.CHAR_DELAY,		parser.getAttributeValue(4));
					values.put(RsaDbHelper.CHAR_PRICE,		parser.getAttributeValue(5));
					db.insert(RsaDbHelper.TABLE_CHAR, RsaDbHelper.CHAR_ID, values);
					values.clear();

					// Write message to Handler: this table begin to download:
		            android.os.Message hMess = mHandler.obtainMessage();
					hMess = mHandler.obtainMessage();
					data.putString("LOG", "PROGRESS");
					hMess.setData(data);
					mHandler.sendMessage(hMess);
			    }
			    eventType = parser.next();
			    insertCounter++;
			}
        	db.execSQL("COMMIT");

	}

	public static void XMLtoHist(Context context, SQLiteDatabase db, Handler mHandler, String fPath) throws Exception
	{
			ContentValues values = new ContentValues();
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
	        factory.setNamespaceAware(true);
			XmlPullParser parser = factory.newPullParser();
			Bundle data = new Bundle();

			/** appPath = /data/data/ru.by.rsa/files */
			String appPath = fPath + File.separator + "inbox/";

			File xmlFile = new File(appPath + "hist.xml");
			InputStream xmlStream = new java.io.FileInputStream(xmlFile);

			parser.setInput(xmlStream, null);//РАЗОБРАТЬ С ЗАГОЛОВКОМ ИКСЭМЭЛЯ

        	preparetable(db, context, mHandler, RsaDbHelper.TABLE_HIST, getCountXML(parser));

        	xmlStream.close();
        	xmlStream = new java.io.FileInputStream(xmlFile);

			parser.setInput(xmlStream, null);//РАЗОБРАТЬ С ЗАГОЛОВКОМ ИКСЭМЭЛЯ

			int eventType = parser.getEventType();
			int insertCounter = 0;
            db.execSQL("BEGIN TRANSACTION");

        	while (eventType != XmlPullParser.END_DOCUMENT)
			{
        		if ((insertCounter%100) == 0)
		        {
		        	db.execSQL("COMMIT");
					db.execSQL("BEGIN TRANSACTION");
		        }
			    if ((eventType == XmlPullParser.START_TAG)
			    		&& parser.getName().toLowerCase().equals("item"))
			    {
			        values.put(RsaDbHelper.HIST_CUST_ID, parser.getAttributeValue(0));
			        values.put(RsaDbHelper.HIST_SHOP_ID, parser.getAttributeValue(1));
			        values.put(RsaDbHelper.HIST_GOODS_ID, parser.getAttributeValue(2));
			        values.put(RsaDbHelper.HIST_QTY, parser.getAttributeValue(3));
			        values.put(RsaDbHelper.HIST_PRICE, normalizePrice(parser.getAttributeValue(4)));
			        values.put(RsaDbHelper.HIST_FLASH, parser.getAttributeValue(5));
			        values.put(RsaDbHelper.HIST_DATE, dateCorrection(parser.getAttributeValue(6)));


			        if (parser.getAttributeCount()>7) {
			        	values.put(RsaDbHelper.HIST_COMMENT, parser.getAttributeValue(7));
			        }

					db.insert(RsaDbHelper.TABLE_HIST, RsaDbHelper.HIST_CUST_ID, values);
					values.clear();

					// Write message to Handler: this table begin to download:
		            android.os.Message hMess = mHandler.obtainMessage();
					hMess = mHandler.obtainMessage();
					data.putString("LOG", "PROGRESS");
					hMess.setData(data);
					mHandler.sendMessage(hMess);
			    }
			    eventType = parser.next();
			    insertCounter++;
			}
        	db.execSQL("COMMIT");

	}

	public static void XMLtoSales(Context context, SQLiteDatabase db, Handler mHandler, String fPath) throws Exception
	{
			ContentValues values = new ContentValues();
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
	        factory.setNamespaceAware(true);
			XmlPullParser parser = factory.newPullParser();
			Bundle data = new Bundle();

			/** appPath = /data/data/ru.by.rsa/files */
			String appPath = fPath + File.separator + "inbox/";

			File xmlFile = new File(appPath + "salout.xml");
			InputStream xmlStream = new java.io.FileInputStream(xmlFile);

			parser.setInput(xmlStream, null);//РАЗОБРАТЬ С ЗАГОЛОВКОМ ИКСЭМЭЛЯ

        	preparetable(db, context, mHandler, RsaDbHelper.TABLE_SALOUT, getCountXML(parser));

        	xmlStream.close();
        	xmlStream = new java.io.FileInputStream(xmlFile);

			parser.setInput(xmlStream, null);//РАЗОБРАТЬ С ЗАГОЛОВКОМ ИКСЭМЭЛЯ

			int eventType = parser.getEventType();
			int insertCounter = 0;
            db.execSQL("BEGIN TRANSACTION");

        	while (eventType != XmlPullParser.END_DOCUMENT)
			{
        		if ((insertCounter%100) == 0)
		        {
		        	db.execSQL("COMMIT");
					db.execSQL("BEGIN TRANSACTION");
		        }
			    if ((eventType == XmlPullParser.START_TAG)
			    		&& parser.getName().toLowerCase().equals("item"))
			    {

			        String invoice_no 	= parser.getAttributeValue(null, "invoice_no");
			        String datetime 	= parser.getAttributeValue(null, "datetime");
			        String cust_id 		= parser.getAttributeValue(null, "cust_id");
			        String shop_id 		= parser.getAttributeValue(null, "shop_id");
			        String wh_id 		= parser.getAttributeValue(null, "wh_id");
			        String cust_name 	= parser.getAttributeValue(null, "cust_name");
			        String shop_name 	= parser.getAttributeValue(null, "shop_name");
			        String wh_name 		= parser.getAttributeValue(null, "wh_name");
			        String goods_id 	= parser.getAttributeValue(null, "goods_id");
			        String goods_name 	= parser.getAttributeValue(null, "goods_name");
			        String qty 			= parser.getAttributeValue(null, "qty");
			        String price 		= parser.getAttributeValue(null, "price");
			        String sum 			= parser.getAttributeValue(null, "sum");

			        if ( invoice_no==null
			        		|| datetime==null
			        		|| goods_id==null
			        		|| goods_name==null )
			        	continue;

			    	values.put(RsaDbHelper.SALOUT_INVOICE_NO, 	invoice_no);
			    	values.put(RsaDbHelper.SALOUT_DATETIME, 	datetime);
			    	values.put(RsaDbHelper.SALOUT_CUST_ID, 		cust_id==null?"Undef":cust_id);
			    	values.put(RsaDbHelper.SALOUT_SHOP_ID, 		shop_id==null?"Undef":shop_id);
			    	values.put(RsaDbHelper.SALOUT_WH_ID, 		wh_id==null?"Undef":wh_id);
			    	values.put(RsaDbHelper.SALOUT_CUST_NAME,  	cust_name==null?"Undef":cust_name);
			    	values.put(RsaDbHelper.SALOUT_SHOP_NAME,  	shop_name==null?"Undef":shop_name);
			    	values.put(RsaDbHelper.SALOUT_WH_NAME,  	wh_name==null?"Undef":wh_name);
			    	values.put(RsaDbHelper.SALOUT_GOODS_ID, 	goods_id);
			    	values.put(RsaDbHelper.SALOUT_GOODS_NAME, 	goods_name);
			    	values.put(RsaDbHelper.SALOUT_QTY, 			normalizeQty(qty, "0"));
			    	values.put(RsaDbHelper.SALOUT_PRICE, 		normalizePrice(price));
			    	values.put(RsaDbHelper.SALOUT_SUM, 			normalizePrice(sum));

			    	db.insert(RsaDbHelper.TABLE_SALOUT, RsaDbHelper.SALOUT_SHOP_NAME, values);
					values.clear();

					// Write message to Handler: this table begin to download:
		            android.os.Message hMess = mHandler.obtainMessage();
					hMess = mHandler.obtainMessage();
					data.putString("LOG", "PROGRESS");
					hMess.setData(data);
					mHandler.sendMessage(hMess);
			    }
			    eventType = parser.next();
			    insertCounter++;
			}
        	db.execSQL("COMMIT");

	}

	public static void XMLtoPlan(Context context, SQLiteDatabase db, Handler mHandler, String fPath) throws Exception
	{
			ContentValues values = new ContentValues();
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
	        factory.setNamespaceAware(true);
			XmlPullParser parser = factory.newPullParser();
			Bundle data = new Bundle();

			/** appPath = /data/data/ru.by.rsa/files */
			String appPath = fPath + File.separator + "inbox/";

			File xmlFile = new File(appPath + "plan.xml");
			InputStream xmlStream = new java.io.FileInputStream(xmlFile);

			parser.setInput(xmlStream, null);//РАЗОБРАТЬ С ЗАГОЛОВКОМ ИКСЭМЭЛЯ

        	preparetable(db, context, mHandler, RsaDbHelper.TABLE_PLAN, getCountXML(parser));

        	xmlStream.close();
        	xmlStream = new java.io.FileInputStream(xmlFile);

			parser.setInput(xmlStream, null);//РАЗОБРАТЬ С ЗАГОЛОВКОМ ИКСЭМЭЛЯ

			int eventType = parser.getEventType();
			int insertCounter = 0;
            db.execSQL("BEGIN TRANSACTION");

        	while (eventType != XmlPullParser.END_DOCUMENT)
			{
        		if ((insertCounter%100) == 0)
		        {
		        	db.execSQL("COMMIT");
					db.execSQL("BEGIN TRANSACTION");
		        }
			    if ((eventType == XmlPullParser.START_TAG)
			    		&& parser.getName().toLowerCase().equals("item"))
			    {
			        values.put(RsaDbHelper.PLAN_ID,			parser.getAttributeValue(0).equals("")?"0":parser.getAttributeValue(0));
			        values.put(RsaDbHelper.PLAN_CUST_ID,	parser.getAttributeValue(1));
					values.put(RsaDbHelper.PLAN_SHOP_ID,	parser.getAttributeValue(2));
					values.put(RsaDbHelper.PLAN_CUST_TEXT,	parser.getAttributeValue(3));
					values.put(RsaDbHelper.PLAN_SHOP_TEXT,	parser.getAttributeValue(4));
					values.put(RsaDbHelper.PLAN_DATEV,		parser.getAttributeValue(5));
					values.put(RsaDbHelper.PLAN_STATE,		parser.getAttributeValue(6));
					db.insert(RsaDbHelper.TABLE_PLAN, RsaDbHelper.PLAN_ID, values);
					values.clear();

					// Write message to Handler: this table begin to download:
		            android.os.Message hMess = mHandler.obtainMessage();
					hMess = mHandler.obtainMessage();
					data.putString("LOG", "PROGRESS");
					hMess.setData(data);
					mHandler.sendMessage(hMess);
			    }
			    eventType = parser.next();
			    insertCounter++;
			}
        	db.execSQL("COMMIT");

	}

	public static void XMLtoSold(Context context, SQLiteDatabase db, Handler mHandler, String fPath) throws Exception
	{
			ContentValues values = new ContentValues();
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
	        factory.setNamespaceAware(true);
			XmlPullParser parser = factory.newPullParser();
			Bundle data = new Bundle();

			/** appPath = /data/data/ru.by.rsa/files */
			String appPath = fPath + File.separator + "inbox/";

			File xmlFile = new File(appPath + "sold.xml");
			InputStream xmlStream = new java.io.FileInputStream(xmlFile);

			parser.setInput(xmlStream, null);//РАЗОБРАТЬ С ЗАГОЛОВКОМ ИКСЭМЭЛЯ

        	preparetable(db, context, mHandler, RsaDbHelper.TABLE_SOLD, getCountXML(parser));

        	xmlStream.close();
        	xmlStream = new java.io.FileInputStream(xmlFile);

			parser.setInput(xmlStream, null);//РАЗОБРАТЬ С ЗАГОЛОВКОМ ИКСЭМЭЛЯ

			int eventType = parser.getEventType();
			int insertCounter = 0;
            db.execSQL("BEGIN TRANSACTION");

        	while (eventType != XmlPullParser.END_DOCUMENT)
			{
        		if ((insertCounter%100) == 0)
		        {
		        	db.execSQL("COMMIT");
					db.execSQL("BEGIN TRANSACTION");
		        }
			    if ((eventType == XmlPullParser.START_TAG)
			    		&& parser.getName().toLowerCase().equals("item"))
			    {
			        values.put(RsaDbHelper.SOLD_BRAND_ID,	parser.getAttributeValue(0));
			        values.put(RsaDbHelper.SOLD_GROUP_ID,	parser.getAttributeValue(1));
					values.put(RsaDbHelper.SOLD_CUST_ID,	parser.getAttributeValue(2));
					values.put(RsaDbHelper.SOLD_SHOP_ID,	parser.getAttributeValue(3));
					values.put(RsaDbHelper.SOLD_COMMENT,	parser.getAttributeValue(4));

					db.insert(RsaDbHelper.TABLE_SOLD, RsaDbHelper.SOLD_CUST_ID, values);
					values.clear();

					// Write message to Handler: this table begin to download:
		            android.os.Message hMess = mHandler.obtainMessage();
					hMess = mHandler.obtainMessage();
					data.putString("LOG", "PROGRESS");
					hMess.setData(data);
					mHandler.sendMessage(hMess);
			    }
			    eventType = parser.next();
			    insertCounter++;
			    Log.d("ROMKA","SOLD item added... ");
			}
        	db.execSQL("COMMIT");

	}

	public static void XMLtoMatrix(Context context, SQLiteDatabase db, Handler mHandler, String fPath) throws Exception
	{
			ContentValues values = new ContentValues();
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
	        factory.setNamespaceAware(true);
			XmlPullParser parser = factory.newPullParser();
			Bundle data = new Bundle();

			/** appPath = /data/data/ru.by.rsa/files */
			String appPath = fPath + File.separator + "inbox/";

			File xmlFile = new File(appPath + "matrix.xml");
			InputStream xmlStream = new java.io.FileInputStream(xmlFile);

			parser.setInput(xmlStream, null);//РАЗОБРАТЬ С ЗАГОЛОВКОМ ИКСЭМЭЛЯ

        	preparetable(db, context, mHandler, RsaDbHelper.TABLE_MATRIX, getCountXML(parser));

        	xmlStream.close();
        	xmlStream = new java.io.FileInputStream(xmlFile);

			parser.setInput(xmlStream, null);//РАЗОБРАТЬ С ЗАГОЛОВКОМ ИКСЭМЭЛЯ

			int eventType = parser.getEventType();
			int insertCounter = 0;
            db.execSQL("BEGIN TRANSACTION");

        	while (eventType != XmlPullParser.END_DOCUMENT)
			{
        		if ((insertCounter%100) == 0)
		        {
		        	db.execSQL("COMMIT");
					db.execSQL("BEGIN TRANSACTION");
		        }
			    if ((eventType == XmlPullParser.START_TAG)
			    		&& parser.getName().toLowerCase().equals("item"))
			    {
			        values.put(RsaDbHelper.MATRIX_CUST_ID,	parser.getAttributeValue(0));
			        values.put(RsaDbHelper.MATRIX_SHOP_ID,	parser.getAttributeValue(1));
					values.put(RsaDbHelper.MATRIX_GOODS_ID,	parser.getAttributeValue(2));
					values.put(RsaDbHelper.MATRIX_MATRIX,	normalizematrix(parser.getAttributeValue(3)));
					values.put(RsaDbHelper.MATRIX_AVG,		normalizeavg(parser.getAttributeValue(4)));
					values.put(RsaDbHelper.MATRIX_COEF,		normalizecoef(parser.getAttributeValue(5)));
					values.put(RsaDbHelper.MATRIX_DELIVERY,	normalizedelivery(parser.getAttributeValue(6)));
					values.put(RsaDbHelper.MATRIX_SHARE,	normalizeshare(parser.getAttributeValue(7)));

					values.put(RsaDbHelper.MATRIX_VPERCENT,	parser.getAttributeValue(8));

					try {
						String custom1 = parser.getAttributeValue(null, "CUSTOM1");
						values.put(RsaDbHelper.MATRIX_CUSTOM1,
								TextUtils.isEmpty(custom1) ? "" : custom1);

					} catch (Exception e) {
						values.put(RsaDbHelper.MATRIX_CUSTOM1,"ошибка загрузки");
					}

					int attributeCount = parser.getAttributeCount();
					for(int i=9;i<attributeCount;i++) {
						values.put(parser.getAttributeName(i), parser.getAttributeValue(i));
					}
					/*
					values.put(RsaDbHelper.MATRIX_DATE1,	parser.getAttributeValue(9));
					values.put(RsaDbHelper.MATRIX_REST1,	parser.getAttributeValue(10));
					values.put(RsaDbHelper.MATRIX_RETURN1,	parser.getAttributeValue(11));
					values.put(RsaDbHelper.MATRIX_ORDER1,	parser.getAttributeValue(12));
					values.put(RsaDbHelper.MATRIX_DATE2,	parser.getAttributeValue(13));
					values.put(RsaDbHelper.MATRIX_REST2,	parser.getAttributeValue(14));
					values.put(RsaDbHelper.MATRIX_RETURN2,	parser.getAttributeValue(15));
					values.put(RsaDbHelper.MATRIX_ORDER2,	parser.getAttributeValue(16));
					values.put(RsaDbHelper.MATRIX_DATE3,	parser.getAttributeValue(17));
					values.put(RsaDbHelper.MATRIX_REST3,	parser.getAttributeValue(18));
					values.put(RsaDbHelper.MATRIX_RETURN3,	parser.getAttributeValue(19));
					values.put(RsaDbHelper.MATRIX_ORDER3,	parser.getAttributeValue(20));

					if (parser.getAttributeCount()>21) {
						values.put(RsaDbHelper.MATRIX_DATE4,	parser.getAttributeValue(21));
						values.put(RsaDbHelper.MATRIX_REST4,	parser.getAttributeValue(22));
						values.put(RsaDbHelper.MATRIX_RETURN4,	parser.getAttributeValue(23));
						values.put(RsaDbHelper.MATRIX_ORDER4,	parser.getAttributeValue(24));
						values.put(RsaDbHelper.MATRIX_DATE5,	parser.getAttributeValue(25));
						values.put(RsaDbHelper.MATRIX_REST5,	parser.getAttributeValue(26));
						values.put(RsaDbHelper.MATRIX_RETURN5,	parser.getAttributeValue(27));
						values.put(RsaDbHelper.MATRIX_ORDER5,	parser.getAttributeValue(28));
						values.put(RsaDbHelper.MATRIX_DATE6,	parser.getAttributeValue(29));
						values.put(RsaDbHelper.MATRIX_REST6,	parser.getAttributeValue(30));
						values.put(RsaDbHelper.MATRIX_RETURN6,	parser.getAttributeValue(31));
						values.put(RsaDbHelper.MATRIX_ORDER6,	parser.getAttributeValue(32));
						values.put(RsaDbHelper.MATRIX_DATE7,	parser.getAttributeValue(33));
						values.put(RsaDbHelper.MATRIX_REST7,	parser.getAttributeValue(34));
						values.put(RsaDbHelper.MATRIX_RETURN7,	parser.getAttributeValue(35));
						values.put(RsaDbHelper.MATRIX_ORDER7,	parser.getAttributeValue(36));
						values.put(RsaDbHelper.MATRIX_DATE8,	parser.getAttributeValue(37));
						values.put(RsaDbHelper.MATRIX_REST8,	parser.getAttributeValue(38));
						values.put(RsaDbHelper.MATRIX_RETURN8,	parser.getAttributeValue(39));
						values.put(RsaDbHelper.MATRIX_ORDER8,	parser.getAttributeValue(40));
						values.put(RsaDbHelper.MATRIX_DATE9,	parser.getAttributeValue(41));
						values.put(RsaDbHelper.MATRIX_REST9,	parser.getAttributeValue(42));
						values.put(RsaDbHelper.MATRIX_RETURN9,	parser.getAttributeValue(43));
						values.put(RsaDbHelper.MATRIX_ORDER9,	parser.getAttributeValue(44));
					}
					*/
					db.insert(RsaDbHelper.TABLE_MATRIX, RsaDbHelper.MATRIX_CUST_ID, values);
					values.clear();

					// Write message to Handler: this table begin to download:
		            android.os.Message hMess = mHandler.obtainMessage();
					hMess = mHandler.obtainMessage();
					data.putString("LOG", "PROGRESS");
					hMess.setData(data);
					mHandler.sendMessage(hMess);
			    }
			    eventType = parser.next();
			    insertCounter++;
			   // Log.d("ROMKA","MATRIX item added... ");
			}
        	db.execSQL("COMMIT");

        	SharedPreferences prefs = context.getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE);
        	prefs.edit().putBoolean(RsaDb.MATRIXKEY, true).commit();
	}

	public static void XMLtoProdlock(Context context, SQLiteDatabase db, Handler mHandler, String fPath) throws Exception
	{
			ContentValues values = new ContentValues();
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
	        factory.setNamespaceAware(true);
			XmlPullParser parser = factory.newPullParser();
			Bundle data = new Bundle();

			/** appPath = /data/data/ru.by.rsa/files */
			String appPath = fPath + File.separator + "inbox/";

			File xmlFile = new File(appPath + "prodlock.xml");
			InputStream xmlStream = new java.io.FileInputStream(xmlFile);

			parser.setInput(xmlStream, null);//РАЗОБРАТЬ С ЗАГОЛОВКОМ ИКСЭМЭЛЯ

        	preparetable(db, context, mHandler, RsaDbHelper.TABLE_PRODLOCK, getCountXML(parser));

        	xmlStream.close();
        	xmlStream = new java.io.FileInputStream(xmlFile);

			parser.setInput(xmlStream, null);//РАЗОБРАТЬ С ЗАГОЛОВКОМ ИКСЭМЭЛЯ

			int eventType = parser.getEventType();
			int insertCounter = 0;
            db.execSQL("BEGIN TRANSACTION");

        	while (eventType != XmlPullParser.END_DOCUMENT)
			{
        		if ((insertCounter%100) == 0)
		        {
		        	db.execSQL("COMMIT");
					db.execSQL("BEGIN TRANSACTION");
		        }
			    if ((eventType == XmlPullParser.START_TAG)
			    		&& parser.getName().toLowerCase().equals("item"))
			    {
			        values.put(RsaDbHelper.PRODLOCK_CUST_ID,	parser.getAttributeValue(0));
			        values.put(RsaDbHelper.PRODLOCK_SHOP_ID,	parser.getAttributeValue(1));
					values.put(RsaDbHelper.PRODLOCK_GOODS_ID,	parser.getAttributeValue(2));
					values.put(RsaDbHelper.PRODLOCK_DATE,		parser.getAttributeValue(3));

					db.insert(RsaDbHelper.TABLE_PRODLOCK, RsaDbHelper.PRODLOCK_CUST_ID, values);
					values.clear();

					// Write message to Handler: this table begin to download:
		            android.os.Message hMess = mHandler.obtainMessage();
					hMess = mHandler.obtainMessage();
					data.putString("LOG", "PROGRESS");
					hMess.setData(data);
					mHandler.sendMessage(hMess);
			    }
			    eventType = parser.next();
			    insertCounter++;
			   // Log.d("ROMKA","MATRIX item added... ");
			}
        	db.execSQL("COMMIT");

        	SharedPreferences prefs = context.getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE);
        	prefs.edit().putBoolean(RsaDb.MATRIXKEY, true).commit();
	}

	public static void XMLtoFTP(Context context, SQLiteDatabase db, Handler mHandler, String fPath,
			boolean doFtpUpdate) throws Exception
	{
			ContentValues values = new ContentValues();
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
	        factory.setNamespaceAware(true);
			XmlPullParser parser = factory.newPullParser();
			Bundle data = new Bundle();

			/** appPath = /data/data/ru.by.rsa/files */
			String appPath = fPath + File.separator + "inbox/";

			File xmlFile = new File(appPath + "workinf.xml");
			InputStream xmlStream = new java.io.FileInputStream(xmlFile);

			parser.setInput(xmlStream, null);//РАЗОБРАТЬ С ЗАГОЛОВКОМ ИКСЭМЭЛЯ

        	preparetable(db, context, mHandler, "WORKINF", getCountXML(parser));

        	xmlStream.close();
        	xmlStream = new java.io.FileInputStream(xmlFile);

			parser.setInput(xmlStream, null);//РАЗОБРАТЬ С ЗАГОЛОВКОМ ИКСЭМЭЛЯ

			int eventType = parser.getEventType();

        	while (eventType != XmlPullParser.END_DOCUMENT)
			{

			    if ((eventType == XmlPullParser.START_TAG)
			    		&& parser.getName().toLowerCase().equals("item")) {

					/** Get Shared Preferences */
					SharedPreferences prefs = context.getSharedPreferences(RsaDb.PREFS_NAME,
							Context.MODE_PRIVATE);
					SharedPreferences screen_prefs = context.getSharedPreferences(
							RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE);

					try {
						String[] itemsOrderBy = context.getResources()
								.getStringArray(R.array.prefOrderBy);
						String strXmlSort = parser.getAttributeValue(1);
						if (TextUtils.isEmpty(strXmlSort) == false) {
							int iXmlSort = Integer.parseInt(strXmlSort) - 1;
							screen_prefs.edit()
									.putString(RsaDb.ORDERBYKEY, itemsOrderBy[iXmlSort])
									.commit();
						}
					} catch (Exception e2) {
					}

					try {
						prefs.edit()
								.putBoolean(RsaDb.SENDLINES,
										parser.getAttributeValue(0).equals("1"))
								.commit();
					} catch (Exception ee) {
					}

					try {
						int delta = Integer.parseInt(parser.getAttributeValue(1));
						if (delta > 0) {
							prefs.edit().putInt("ru.by.rsa.resend_monitor", delta).commit();
						}
					} catch (Exception ee) {
					}
					if (doFtpUpdate) {
						prefs.edit().putString(RsaDb.EMAILKEY, parser.getAttributeValue(4)).commit();
						prefs.edit().putString(RsaDb.PASSWORDKEY, parser.getAttributeValue(5)).commit();
						prefs.edit().putString(RsaDb.SMTPKEY, parser.getAttributeValue(2)).commit();
						prefs.edit().putString(RsaDb.SMTPPORTKEY, parser.getAttributeValue(3)).commit();
						prefs.edit().putString(RsaDb.POPKEY, parser.getAttributeValue(8)).commit();
						prefs.edit().putString(RsaDb.POPPORTKEY, parser.getAttributeValue(9)).commit();
						prefs.edit().putString(RsaDb.SENDTOKEY, parser.getAttributeValue(7)).commit();

						prefs.edit().putString(RsaDb.FTPUSER, parser.getAttributeValue(4)).commit();
						prefs.edit().putString(RsaDb.FTPPASSWORD, parser.getAttributeValue(5)).commit();
						prefs.edit().putString(RsaDb.FTPSERVER, parser.getAttributeValue(2)).commit();
						prefs.edit().putString(RsaDb.FTPPORT, parser.getAttributeValue(3)).commit();
						prefs.edit().putString(RsaDb.FTPINBOX, parser.getAttributeValue(6)).commit();
						prefs.edit().putString(RsaDb.FTPOUTBOX, parser.getAttributeValue(7)).commit();
					}
					screen_prefs.edit().putString(RsaDb.CODEKEY, parser.getAttributeValue(13)).commit();
					screen_prefs.edit().putString(RsaDb.NAMEKEY, parser.getAttributeValue(12)).commit();

					SharedPreferences def_prefs = PreferenceManager.getDefaultSharedPreferences(context);

					// first element have index = 0, last = count-1
					int count = parser.getAttributeCount();
					if (count>14) {
						for (int i=14;i<count;i++) {
							try {
								setPrefFromXml(prefs, screen_prefs, def_prefs, parser.getAttributeName(i), parser.getAttributeValue(i), db);
							} catch (Exception e) {}
						}
					}

					// Write message to Handler: this table begin to download:
		            android.os.Message hMess = mHandler.obtainMessage();
					hMess = mHandler.obtainMessage();
					data.putString("LOG", "PROGRESS");
					hMess.setData(data);
					mHandler.sendMessage(hMess);
			    }
			    eventType = parser.next();
			}

	}

	private static void setPrefFromXml(SharedPreferences prefs, SharedPreferences screen_prefs,
										SharedPreferences def_prefs, String name, String value, SQLiteDatabase ddb) {
		// prefs - email and so on
		// screen_prefs - TP name, code and so on
		// def_prefs default prefs

		if (name.equals("priceselected")) {
			// default price: "Цена 1", "Цена 2", ..., "Цена 20"
			screen_prefs.edit().putString(PRICESELECTED, value).commit();
		} else if (name.equals("brandgroupshow")) {
			// "0" showAll, "1" showgroups, "2" showbrands
			prefs.edit().putInt(BRANDGROUPSHOW, Integer.parseInt(value)).commit();
		} else if (name.equals("gps")) {
			// "0" - not use monitoring. "1" - use it
			screen_prefs.edit().putBoolean(GPSKEY, value.equals("1")).commit();
		} else if (name.equals("coord")) {
			// "0" - do not send coords. "1" - use it
			screen_prefs.edit().putBoolean(COORDKEY, value.equals("1")).commit();
		} else if (name.equals("rate")) {
			// "20" or else
			screen_prefs.edit().putString(RATEKEY, value).commit();
		} else if (name.equals("interface")) {
			// "DBF", "XML", "CSV"
			screen_prefs.edit().putString(INTERFACEKEY, value).commit();
		} else if (name.equals("protocol")) {
			// "E-mail", "Ftp"
			screen_prefs.edit().putString(PROTOCOLKEY, value).commit();
		} else if (name.equals("pricetype")) {
			// "0" - not use autoprice. "1" - use it
			screen_prefs.edit().putBoolean(PRICETYPEKEY, value.equals("1")).commit();
		} else if (name.equals("sendrate")) {
			// "1600" or else
			screen_prefs.edit().putString(SENDRATEKEY, value).commit();
		} else if (name.equals("host")) {
			// "82.254.34.10" or else
			screen_prefs.edit().putString(HOSTKEY, value).commit();
		} else if (name.equals("hostport")) {
			// "7777" or else
			screen_prefs.edit().putString(HOSTPORTKEY, value.equals("")?"6666":value).commit();
		} else if (name.equals("starthour")) {
			// "8" or "9" and so on
			screen_prefs.edit().putString(START_HOUR_KEY, value).commit();
		} else if (name.equals("endhour")) {
			// "18" or "19" and so on
			screen_prefs.edit().putString(END_HOUR_KEY, value).commit();
		} else if (name.equals("lighttheme")) {
			// "0" - not use recomend order. "1" - use it
			screen_prefs.edit().putBoolean(LIGHTTHEMEKEY, value.equals("1")).commit();
		} else if (name.equals("showrecinlist")) {
			// "0" - not use recomend order. "1" - use it
			screen_prefs.edit().putBoolean(SHOWRECINLIST, value.equals("1")).commit();
		} else if (name.equals("vatrate")) {
			// "20", "18" - VAT
			screen_prefs.edit().putString(VATRATE, value).commit();
		} else if (name.equals("lastvat")) {
			// "0" - VAT button is off, "1" - VAT button is on
			screen_prefs.edit().putString(LASTVATKEY, value).commit();
		} else if (name.equals("orderby")) {
			// "Без сортировки", "По алфавиту", "По порядк. №"
			screen_prefs.edit().putString(ORDERBYKEY, value).commit();
		} else if (name.equals("usingplan")) {
			// "0" - not use plan. "1" - use plan
			def_prefs.edit().putBoolean(USINGPLAN, value.equals("1")).commit();
		} else if (name.equals("currency")) {
			// "грн." or "руб."
			def_prefs.edit().putString("prefCurrency", value).commit();
		} else if (name.equals("usepacks")) {
			// "0" or "1"
			def_prefs.edit().putBoolean("prefUsingPacks", value.equals("1")).commit();
		} else if (name.equals("usevozvrat")) {
			// "0" or "1"
			def_prefs.edit().putBoolean("prefUsingVozvrat", value.equals("1")).commit();
		} else if (name.equals("prefOrderHyst")) {
			// "0" or "1"
			def_prefs.edit().putString("prefOrderHyst", value).commit();
		} else if (name.equals("extfname")) {
			// "0" or "1"
			def_prefs.edit().putBoolean("prefExtendedFilename", value.equals("1")).commit();
		} else if (name.equals("autosync")) {
			// "0" or "1"
			def_prefs.edit().putBoolean("prefAutosync", value.equals("1")).commit();
		} else if (name.equals("as_start")) {
			// "0" or "1"
			def_prefs.edit().putString("prefAutosyncStartAt", value).commit();
		} else if (name.equals("as_finish")) {
			// "0" or "1"
			def_prefs.edit().putString("prefAutosyncStopAt", value).commit();
		} else if (name.equals("as_interval")) {
			// "0" or "1"
			def_prefs.edit().putString("prefAutosyncInterval", value).commit();
		} else if (name.equals("pr1")) {
			// "pricename"
			def_prefs.edit().putString("prefPrice1", value).commit();
		} else if (name.equals("pr2")) {
			// "pricename"
			def_prefs.edit().putString("prefPrice2", value).commit();
		} else if (name.equals("pr3")) {
			// "pricename"
			def_prefs.edit().putString("prefPrice3", value).commit();
		} else if (name.equals("pr4")) {
			// "pricename"
			def_prefs.edit().putString("prefPrice4", value).commit();
		} else if (name.equals("pr5")) {
			// "pricename"
			def_prefs.edit().putString("prefPrice5", value).commit();
		} else if (name.equals("pr6")) {
			// "pricename"
			def_prefs.edit().putString("prefPrice6", value).commit();
		} else if (name.equals("pr7")) {
			// "pricename"
			def_prefs.edit().putString("prefPrice7", value).commit();
		} else if (name.equals("pr8")) {
			// "pricename"
			def_prefs.edit().putString("prefPrice8", value).commit();
		} else if (name.equals("pr9")) {
			// "pricename"
			def_prefs.edit().putString("prefPrice9", value).commit();
		} else if (name.equals("pr10")) {
			// "pricename"
			def_prefs.edit().putString("prefPrice10", value).commit();
		} else if (name.equals("pr11")) {
			// "pricename"
			def_prefs.edit().putString("prefPrice11", value).commit();
		} else if (name.equals("pr12")) {
			// "pricename"
			def_prefs.edit().putString("prefPrice12", value).commit();
		} else if (name.equals("pr13")) {
			// "pricename"
			def_prefs.edit().putString("prefPrice13", value).commit();
		} else if (name.equals("pr14")) {
			// "pricename"
			def_prefs.edit().putString("prefPrice14", value).commit();
		} else if (name.equals("pr15")) {
			// "pricename"
			def_prefs.edit().putString("prefPrice15", value).commit();
		} else if (name.equals("pr16")) {
			// "pricename"
			def_prefs.edit().putString("prefPrice16", value).commit();
		} else if (name.equals("pr17")) {
			// "pricename"
			def_prefs.edit().putString("prefPrice17", value).commit();
		} else if (name.equals("pr18")) {
			// "pricename"
			def_prefs.edit().putString("prefPrice18", value).commit();
		} else if (name.equals("pr19")) {
			// "pricename"
			def_prefs.edit().putString("prefPrice19", value).commit();
		} else if (name.equals("pr20")) {
			// "pricename"
			def_prefs.edit().putString("prefPrice20", value).commit();
		} else if (name.equals("stypes")) {
			// "pricename"
			if (value.length()>3) {
				ddb.execSQL("DROP TABLE IF EXISTS _stype");
				RsaDbHelper.createShoptypeDBTable(ddb);

				String[] arr = value.split(",");
				ContentValues values = new ContentValues();
				for ( String ss : arr) {
					values.put(RsaDbHelper.SHOPTYPE_NAME,	ss);
					ddb.insert(RsaDbHelper.TABLE_SHOPTYPE, RsaDbHelper.SHOPTYPE_NAME, values);
					values.clear();
				}
			}
		} else if (name.equals("usequest")) {
			// "0" or "1"
			def_prefs.edit().putBoolean("prefUsingQuest", value.equals("1")).commit();
		} else if (name.equals("usetypes")) {
			// "0" or "1"
			def_prefs.edit().putBoolean("prefUsingTypes", value.equals("1")).commit();
		}  else if (name.equals("deepincome")) {
			// "0" or "1"
			def_prefs.edit().putBoolean("prefDeepIncome", value.equals("1")).commit();
		}	else if (name.equals("debautocorrect")) {
			// "0" or "1"
			def_prefs.edit().putBoolean("prefDebitAutocorrection", value.equals("1")).commit();
		}	else if (name.equals("usedelivery")) {
			// "0" or "1"
			def_prefs.edit().putBoolean("prefUsedelivery", value.equals("1")).commit();
		} else if (name.equals("orderinpcs")) {
			// "0" or "1"
			def_prefs.edit().putBoolean("prefOrderInPCS", value.equals("1")).commit();
		} else if (name.equals("useagreement")) {
			// "0" or "1"
			def_prefs.edit().putBoolean("prefUseAgreed", value.equals("1")).commit();
		} else if (name.equals("geophotorequired")) {
			// "0" or "1"
			def_prefs.edit().putBoolean("prefGeoPhoto", value.equals("1")).commit();
		} else if (name.equals("but1_active")) {
			// "0" or "1"
			def_prefs.edit().putBoolean("prefButton1", value.equals("1")).commit();
		} else if (name.equals("but2_active")) {
			// "0" or "1"
			def_prefs.edit().putBoolean("prefButton2", value.equals("1")).commit();
		} else if (name.equals("but3_active")) {
			// "0" or "1"
			def_prefs.edit().putBoolean("prefButton3", value.equals("1")).commit();
		} else if (name.equals("but4_active")) {
			// "0" or "1"
			def_prefs.edit().putBoolean("prefButton4", value.equals("1")).commit();
		} else if (name.equals("but5_active")) {
			// "0" or "1"
			def_prefs.edit().putBoolean("prefButton5", value.equals("1")).commit();
		} else if (name.equals("but6_active")) {
			// "0" or "1"
			def_prefs.edit().putBoolean("prefButton6", value.equals("1")).commit();
		} else if (name.equals("but1_name")) {
			// "0" or "1"
			def_prefs.edit().putString("prefButton1_name", value).commit();
		} else if (name.equals("but2_name")) {
			// "0" or "1"
			def_prefs.edit().putString("prefButton2_name", value).commit();
		} else if (name.equals("but3_name")) {
			// "0" or "1"
			def_prefs.edit().putString("prefButton3_name", value).commit();
		} else if (name.equals("but4_name")) {
			// "0" or "1"
			def_prefs.edit().putString("prefButton4_name", value).commit();
		} else if (name.equals("but5_name")) {
			// "0" or "1"
			def_prefs.edit().putString("prefButton5_name", value).commit();
		} else if (name.equals("but6_name")) {
			// "0" or "1"
			def_prefs.edit().putString("prefButton6_name", value).commit();
		} else if (name.equals("prtype_block")) {
				// "0" or "1"
				def_prefs.edit().putBoolean("prefPrtypeBlock", value.equals("1")).commit();
		} else if (name.equals("cb1")) {
			// "0" or "1"
			def_prefs.edit().putString("cb1", value).commit();
		} else if (name.equals("cb2")) {
			// "0" or "1"
			def_prefs.edit().putString("cb2", value).commit();
		} else if (name.equals("cb3")) {
			// "0" or "1"
			def_prefs.edit().putString("cb3", value).commit();
		} else if (name.equals("cb4")) {
			// "0" or "1"
			def_prefs.edit().putString("cb4", value).commit();
		} else if (name.equals("kass_hyst")) {
			// "0" or "1"
			def_prefs.edit().putString("ordclean", value).commit();
		}  else if (name.equals("useModalCalc")) {
			// "0" or "1"
			def_prefs.edit().putBoolean("useModalCalc", value.equals("1")).commit();
		}  else if (name.equals("useExtdebit")) {
			// "0" or "1"
			def_prefs.edit().putBoolean("useExtdebit", value.equals("1")).commit();
		}  else if (name.equals("shrinkAfter")) {
			// "0" or "1"
			def_prefs.edit().putBoolean("shrinkAfter", value.equals("1")).commit();
		}  else if (name.equals("prefFixCustomer")) {
			// "0" or "1"
			def_prefs.edit().putBoolean("prefFixCustomer", value.equals("1")).commit();
		}  else if (name.equals("altOrd")) {
			// "0" or "1"
			def_prefs.edit().putBoolean("altOrd", value.equals("1")).commit();
		}  else if (name.equals("extVim")) {
			// "0" or "1"
			def_prefs.edit().putBoolean("extVim", value.equals("1")).commit();
		}  else if (name.equals("encoded")) {
			// "0" or "1"
			def_prefs.edit().putBoolean("encoded", value.equals("1")).commit();
		}  else if (name.equals("extgps")) {
			// "0" or "1"
			def_prefs.edit().putBoolean("extgps", value.equals("1")).commit();
		}  else if (name.equals("useConfirmDefault")) {
			// "0" or "1"
			def_prefs.edit().putBoolean("useConfirmDefault", value.equals("1")).commit();
		}  else if (name.equals("unlimErarh")) {
			// "0" or "1"
			def_prefs.edit().putBoolean("unlimErarh", value.equals("1")).commit();
		}  else if (name.equals("dscperline")) {
			// "0" or "1"
			screen_prefs.edit().putBoolean("dscperline", value.equals("1")).commit();
		}

	}

	private static String fixRest(HashMap<String, String> hm, String f, String ost) {
		String res			= ost;
		String strOrdered	= null;
		double d_ost		= 0;
		double ord_ost		= 0;

		strOrdered = hm.get(f);

		if (strOrdered!=null) {
			try {
				d_ost	= Double.parseDouble(ost);
				ord_ost = Double.parseDouble(strOrdered);
				int iRes = (int)(d_ost-ord_ost);
				res = Integer.toString(iRes);
			} catch(Exception ddee) {}
		}

		return res;
	}

	private static void fillRestsMap(Context _c, HashMap<String, String> hm) {
		RsaDbHelper		mDb			= null;
		SQLiteDatabase	db_orders	= null;
		Cursor			cur			= null;
		String			q			= "select GOODS_ID, SUM(QTY) from _lines where ZAKAZ_ID in ( " +
									  			"select _id from _head where SENDED='0') group by GOODS_ID";
		mDb			=	new RsaDbHelper(_c, RsaDbHelper.DB_ORDERS);
		db_orders	=	mDb.getReadableDatabase();
		try {
			cur		=	db_orders.rawQuery(q, null);
			if (cur.getCount()>0) {
				while (cur.moveToNext()) {
					hm.put(cur.getString(0), cur.getString(1));
				}
			}
		} catch(Exception ze) {}

		if (cur!=null && !cur.isClosed()) {
			cur.close();
		}
		if (db_orders!=null && db_orders.isOpen()) {
			db_orders.close();
		}
	}

	public static void XMLtoGoods(Context context, SQLiteDatabase db, Handler mHandler, String fPath)  throws Exception
	{
			ContentValues values = new ContentValues();
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
	        factory.setNamespaceAware(true);
			XmlPullParser parser = factory.newPullParser();
			Bundle data = new Bundle();

			/** appPath = /data/data/ru.by.rsa/files */
			String appPath = fPath + File.separator + "inbox/";

			File xmlFile = new File(appPath + "goods.xml");
			InputStream xmlStream = new java.io.FileInputStream(xmlFile);

			parser.setInput(xmlStream, null);//РАЗОБРАТЬ С ЗАГОЛОВКОМ ИКСЭМЭЛЯ

        	preparetable(db, context, mHandler, RsaDbHelper.TABLE_GOODS, getCountXML(parser));

        	xmlStream.close();
        	xmlStream = new java.io.FileInputStream(xmlFile);

			parser.setInput(xmlStream, null);//РАЗОБРАТЬ С ЗАГОЛОВКОМ ИКСЭМЭЛЯ

			String fixed_rest = "0";
			HashMap<String, String> arrCorrectRest = new HashMap<String, String>();
			try {
				fillRestsMap(context, arrCorrectRest);
			} catch (Exception fe) {}

			int eventType = parser.getEventType();
			int insertCounter = 0;
            db.execSQL("BEGIN TRANSACTION");

        	while (eventType != XmlPullParser.END_DOCUMENT)
			{
        		if ((insertCounter%100) == 0)
		        {
		        	db.execSQL("COMMIT");
					db.execSQL("BEGIN TRANSACTION");
		        }
			    if ((eventType == XmlPullParser.START_TAG)
			    		&& parser.getName().toLowerCase().equals("item"))
			    {
			    	String _gid = parser.getAttributeValue(0);
			        values.put(RsaDbHelper.GOODS_ID,		_gid);
					values.put(RsaDbHelper.GOODS_NPP,		parser.getAttributeValue(1));
					values.put(RsaDbHelper.GOODS_NAME,		parser.getAttributeValue(2));
					values.put(RsaDbHelper.GOODS_BRAND_ID,	parser.getAttributeValue(3));
					values.put(RsaDbHelper.GOODS_QTY,		normalizeQty(parser.getAttributeValue(4),"1"));
					values.put(RsaDbHelper.GOODS_RESTCUST,	parser.getAttributeValue(5));

					try {
						fixed_rest = fixRest(arrCorrectRest, _gid, normalizeRest(parser.getAttributeValue(6)));

					} catch (Exception eee) {
						fixed_rest = normalizeRest(parser.getAttributeValue(6));
					}
					values.put(RsaDbHelper.GOODS_REST,		fixed_rest);
					values.put(RsaDbHelper.GOODS_HIST1,		parser.getAttributeValue(7));
					values.put(RsaDbHelper.GOODS_RESTCUST1,	parser.getAttributeValue(8));
					values.put(RsaDbHelper.GOODS_HIST2,		parser.getAttributeValue(9));
					values.put(RsaDbHelper.GOODS_RESTCUST2,	parser.getAttributeValue(10));
					values.put(RsaDbHelper.GOODS_HIST3,		parser.getAttributeValue(11));
					values.put(RsaDbHelper.GOODS_GROUP_ID,	parser.getAttributeValue(12));
					values.put(RsaDbHelper.GOODS_PRICE1,	normalizePrice(parser.getAttributeValue(13)));
					values.put(RsaDbHelper.GOODS_PRICE2,	normalizePrice(parser.getAttributeValue(14)));
					values.put(RsaDbHelper.GOODS_PRICE3,	normalizePrice(parser.getAttributeValue(15)));
					values.put(RsaDbHelper.GOODS_PRICE4,	normalizePrice(parser.getAttributeValue(16)));
					values.put(RsaDbHelper.GOODS_PRICE5,	normalizePrice(parser.getAttributeValue(17)));
					values.put(RsaDbHelper.GOODS_PRICE6,	normalizePrice(parser.getAttributeValue(18)));
					values.put(RsaDbHelper.GOODS_PRICE7,	normalizePrice(parser.getAttributeValue(19)));
					values.put(RsaDbHelper.GOODS_PRICE8,	normalizePrice(parser.getAttributeValue(20)));
					values.put(RsaDbHelper.GOODS_PRICE9,	normalizePrice(parser.getAttributeValue(21)));
					values.put(RsaDbHelper.GOODS_PRICE10,	normalizePrice(parser.getAttributeValue(22)));
					values.put(RsaDbHelper.GOODS_PRICE11,	normalizePrice(parser.getAttributeValue(23)));
					values.put(RsaDbHelper.GOODS_PRICE12,	normalizePrice(parser.getAttributeValue(24)));
					values.put(RsaDbHelper.GOODS_PRICE13,	normalizePrice(parser.getAttributeValue(25)));
					values.put(RsaDbHelper.GOODS_PRICE14,	normalizePrice(parser.getAttributeValue(26)));
					values.put(RsaDbHelper.GOODS_PRICE15,	normalizePrice(parser.getAttributeValue(27)));
					values.put(RsaDbHelper.GOODS_PRICE16,	normalizePrice(parser.getAttributeValue(28)));
					values.put(RsaDbHelper.GOODS_PRICE17,	normalizePrice(parser.getAttributeValue(29)));
					values.put(RsaDbHelper.GOODS_PRICE18,	normalizePrice(parser.getAttributeValue(30)));
					values.put(RsaDbHelper.GOODS_PRICE19,	normalizePrice(parser.getAttributeValue(31)));
					values.put(RsaDbHelper.GOODS_PRICE20,	normalizePrice(parser.getAttributeValue(32)));
					values.put(RsaDbHelper.GOODS_DISCOUNT,	parser.getAttributeValue(33));
					values.put(RsaDbHelper.GOODS_PRICEWNDS,	parser.getAttributeValue(34));
					values.put(RsaDbHelper.GOODS_PRICEWONDS,parser.getAttributeValue(35));
					values.put(RsaDbHelper.GOODS_UN,		parser.getAttributeValue(36));
					values.put(RsaDbHelper.GOODS_COEFF,		parser.getAttributeValue(37));
					values.put(RsaDbHelper.GOODS_SUMWONDS,	parser.getAttributeValue(38));
					values.put(RsaDbHelper.GOODS_SUMWNDS,	parser.getAttributeValue(39));
					try {
						values.put(RsaDbHelper.GOODS_WEIGHT1,	normalizeRest(parser.getAttributeValue(40)));
					} catch (Exception e) {
						// TODO Auto-generated catch block
						Log.d("ZUZUZU",e.getMessage());

					}
					values.put(RsaDbHelper.GOODS_WEIGHT,	parser.getAttributeValue(41));
					values.put(RsaDbHelper.GOODS_VOLUME1,	parser.getAttributeValue(42));
					values.put(RsaDbHelper.GOODS_VOLUME,	parser.getAttributeValue(43));
					values.put(RsaDbHelper.GOODS_NDS,		parser.getAttributeValue(44));
					values.put(RsaDbHelper.GOODS_DATE,		parser.getAttributeValue(45));
					values.put(RsaDbHelper.GOODS_FLASH,		normalizeFlash(parser.getAttributeValue(46)));

					db.insert(RsaDbHelper.TABLE_GOODS, RsaDbHelper.GOODS_NAME, values);
					values.clear();

					// Write message to Handler: this table begin to download:
		            android.os.Message hMess = mHandler.obtainMessage();
					hMess = mHandler.obtainMessage();
					data.putString("LOG", "PROGRESS");
					hMess.setData(data);
					mHandler.sendMessage(hMess);
			    }
			    eventType = parser.next();
			    insertCounter++;
			}
        	db.execSQL("COMMIT");

	}

	/**
	 * Parse FTP File list to find witch files are is
	 * @param mFiles - List of files of ftp dirrectory to chek
	 */
	public static int parseFTPFiles(FTPFile[] mFiles, SharedPreferences mPref)
	{
		// Result variable
		int bMain = 0;

		if (mPref.getString(RsaDb.INTERFACEKEY, "DBF").equals("DBF"))
		{
			for (int i=0; i<mFiles.length; i++)
			{
		        if (mFiles[i].getName().toLowerCase().equals("goods.dbf.lzma")) 		// 0000 0001
		        {
		        	bMain |= 1;
		        }
		        else if (mFiles[i].getName().toLowerCase().equals("cust.dbf.lzma"))  // 0000 0010
		        {
		        	bMain |= 2;
		        }
		        else if (mFiles[i].getName().toLowerCase().equals("char.dbf.lzma"))  // 0000 0100
		        {
		        	bMain |= 4;
		        }
		        else if (mFiles[i].getName().toLowerCase().equals("debit.dbf.lzma"))	// 0000 1000
		        {
		        	bMain |= 8;
		        }
		        else if (mFiles[i].getName().toLowerCase().equals("shop.dbf.lzma"))  // 0001 0000
		        {
		        	bMain |= 16;
		        }
		        else if (mFiles[i].getName().toLowerCase().equals("sklad.dbf.lzma"))	// 0010 0000
		        {
		        	bMain |= 32;
		        }
		        else if (mFiles[i].getName().toLowerCase().equals("group.dbf.lzma")) // 0100 0000
		        {
		        	bMain |= 64;
		        }
		        else if (mFiles[i].getName().toLowerCase().equals("brand.dbf.lzma")) // 1000 0000
		        {
		        	bMain |= 128;
		        }
		        else if (mFiles[i].getName().toLowerCase().equals("workinf.dbf.lzma")) // 1 0000 0000
		        {
		        	bMain |= 256;
		        }
		        else if (mFiles[i].getName().toLowerCase().equals("plan.dbf.lzma")) // 10 0000 0000
		        {
		        	bMain |= 512;
		        }
		        else if (mFiles[i].getName().toLowerCase().equals("sold.dbf.lzma")) // 100 0000 0000
		        {
		        	bMain |= 1024;
		        }
		        else if (mFiles[i].getName().toLowerCase().equals("matrix.dbf.lzma")) // 1000 0000 0000
		        {
		        	bMain |= 2048;
		        }
		        else if (mFiles[i].getName().toLowerCase().equals("prodlock.dbf.lzma")) // 1 0000 0000 0000
		        {
		        	bMain |= 4096;
		        }

			}
		}
		else if (mPref.getString(RsaDb.INTERFACEKEY, "DBF").equals("XML"))
		{
			for (int i=0; i<mFiles.length; i++)
			{
		        if (mFiles[i].getName().toLowerCase().equals("goods.xml.zip")) 		// 0000 0001
		        {
		        	bMain |= 1;
		        }
		        else if (mFiles[i].getName().toLowerCase().equals("cust.xml.zip"))  // 0000 0010
		        {
		        	bMain |= 2;
		        }
		        else if (mFiles[i].getName().toLowerCase().equals("char.xml.zip"))  // 0000 0100
		        {
		        	bMain |= 4;
		        }
		        else if (mFiles[i].getName().toLowerCase().equals("debit.xml.zip"))	// 0000 1000
		        {
		        	bMain |= 8;
		        }
		        else if (mFiles[i].getName().toLowerCase().equals("shop.xml.zip"))  // 0001 0000
		        {
		        	bMain |= 16;
		        }
		        else if (mFiles[i].getName().toLowerCase().equals("sklad.xml.zip"))	// 0010 0000
		        {
		        	bMain |= 32;
		        }
		        else if (mFiles[i].getName().toLowerCase().equals("group.xml.zip")) // 0100 0000
		        {
		        	bMain |= 64;
		        }
		        else if (mFiles[i].getName().toLowerCase().equals("brand.xml.zip")) // 1000 0000
		        {
		        	bMain |= 128;
		        }
		        else if (mFiles[i].getName().toLowerCase().equals("workinf.xml.zip")) // 1 0000 0000
		        {
		        	bMain |= 256;
		        }
		        else if (mFiles[i].getName().toLowerCase().equals("plan.xml.zip")) // 10 0000 0000
		        {
		        	bMain |= 512;
		        }
		        else if (mFiles[i].getName().toLowerCase().equals("sold.xml.zip")) // 100 0000 0000
		        {
		        	bMain |= 1024;
		        }
		        else if (mFiles[i].getName().toLowerCase().equals("matrix.xml.zip")) // 1000 0000 0000
		        {
		        	bMain |= 2048;
		        }
		        else if (mFiles[i].getName().toLowerCase().equals("prodlock.xml.zip")) // 1 0000 0000 0000
		        {
		        	bMain |= 4096;
		        }
		        else if (mFiles[i].getName().toLowerCase().equals("hist.xml.zip")) // 10 0000 0000 0000
		        {
		        	bMain |= 8192;
		        }
		        else if (mFiles[i].getName().toLowerCase().equals("skladdet.xml.zip")) // 100 0000 0000 0000
		        {
		        	bMain |= 16384;
		        }
		        else if (mFiles[i].getName().toLowerCase().equals("salout.xml.zip")) {// 1000 0000 0000 0000
		        	bMain |= 32768;
		        }
				else if (mFiles[i].getName().toLowerCase().equals("statplan.xml.zip")) {// 1 0000 0000 0000 0000
					bMain |= 65536;
				}

			}
		}
		else if (mPref.getString(RsaDb.INTERFACEKEY, "DBF").equals("CSV"))
		{
			for (int i=0; i<mFiles.length; i++)
			{
		        if (mFiles[i].getName().toLowerCase().equals("goods.csv.zip")) 		// 0000 0001
		        {
		        	bMain |= 1;
		        }
		        else if (mFiles[i].getName().toLowerCase().equals("cust.csv.zip"))  // 0000 0010
		        {
		        	bMain |= 2;
		        }
		        else if (mFiles[i].getName().toLowerCase().equals("char.csv.zip"))  // 0000 0100
		        {
		        	bMain |= 4;
		        }
		        else if (mFiles[i].getName().toLowerCase().equals("debit.csv.zip"))	// 0000 1000
		        {
		        	bMain |= 8;
		        }
		        else if (mFiles[i].getName().toLowerCase().equals("shop.csv.zip"))  // 0001 0000
		        {
		        	bMain |= 16;
		        }
		        else if (mFiles[i].getName().toLowerCase().equals("sklad.csv.zip"))	// 0010 0000
		        {
		        	bMain |= 32;
		        }
		        else if (mFiles[i].getName().toLowerCase().equals("group.csv.zip")) // 0100 0000
		        {
		        	bMain |= 64;
		        }
		        else if (mFiles[i].getName().toLowerCase().equals("brand.csv.zip")) // 1000 0000
		        {
		        	bMain |= 128;
		        }
		        else if (mFiles[i].getName().toLowerCase().equals("workinf.csv.zip")) // 1 0000 0000
		        {
		        	bMain |= 256;
		        }
		        else if (mFiles[i].getName().toLowerCase().equals("plan.csv.zip")) // 10 0000 0000
		        {
		        	bMain |= 512;
		        }
			}
		}
		return bMain;
	}

	public static int parseSDFiles(String iFace)
	{
		String SD_CARD_PATH = Environment.getExternalStorageDirectory().toString();
		String inboxPath = SD_CARD_PATH + File.separator + "rsa" + File.separator + "inbox";
		// Result variable
		int bMain = 0;

		File folder = new File(inboxPath);
		File[] listOfFiles = folder.listFiles();
		if (iFace.equals("XML"))
		{
		  for (int i = 0; i < listOfFiles.length; i++)
		  {
			  if (listOfFiles[i].isFile())
			  {
				  if (listOfFiles[i].getName().toLowerCase().equals("goods.xml")) 		// 0000 0001
			        {
			        	bMain |= 1;
			        }
			        else if (listOfFiles[i].getName().toLowerCase().equals("cust.xml"))  // 0000 0010
			        {
			        	bMain |= 2;
			        }
			        else if (listOfFiles[i].getName().toLowerCase().equals("char.xml"))  // 0000 0100
			        {
			        	bMain |= 4;
			        }
			        else if (listOfFiles[i].getName().toLowerCase().equals("debit.xml"))	// 0000 1000
			        {
			        	bMain |= 8;
			        }
			        else if (listOfFiles[i].getName().toLowerCase().equals("shop.xml"))  // 0001 0000
			        {
			        	bMain |= 16;
			        }
			        else if (listOfFiles[i].getName().toLowerCase().equals("sklad.xml"))	// 0010 0000
			        {
			        	bMain |= 32;
			        }
			        else if (listOfFiles[i].getName().toLowerCase().equals("group.xml")) // 0100 0000
			        {
			        	bMain |= 64;
			        }
			        else if (listOfFiles[i].getName().toLowerCase().equals("brand.xml")) // 1000 0000
			        {
			        	bMain |= 128;
			        }
			        else if (listOfFiles[i].getName().toLowerCase().equals("workinf.xml")) // 1 0000 0000
			        {
			        	bMain |= 256;
			        }
			        else if (listOfFiles[i].getName().toLowerCase().equals("plan.xml")) // 10 0000 0000
			        {
			        	bMain |= 512;
			        }
			        else if (listOfFiles[i].getName().toLowerCase().equals("sold.xml")) // 100 0000 0000
			        {
			        	bMain |= 1024;
			        }
			        else if (listOfFiles[i].getName().toLowerCase().equals("matrix.xml")) // 1000 0000 0000
			        {
			        	bMain |= 2048;
			        }
			        else if (listOfFiles[i].getName().toLowerCase().equals("prodlock.xml")) // 1 0000 0000 0000
			        {
			        	bMain |= 4096;
			        }
			        else if (listOfFiles[i].getName().toLowerCase().equals("hist.xml")) // 10 0000 0000 0000
			        {
			        	bMain |= 8192;
			        }
			        else if (listOfFiles[i].getName().toLowerCase().equals("skladdet.xml")) // 100 0000 0000 0000
			        {
			        	bMain |= 16384;
			        }
			        else if (listOfFiles[i].getName().toLowerCase().equals("salout.xml")) // 1000 0000 0000 0000
			        {
			        	bMain |= 32768;
			        }
				  	else if (listOfFiles[i].getName().toLowerCase().equals("statplan.xml")) // 1 0000 0000 0000 0000
				  	{
						bMain |= 65536;
				  	}

		      }
		  }
		}
		else if (iFace.equals("DBF"))
		{
			for (int i = 0; i < listOfFiles.length; i++)
			  {
				  if (listOfFiles[i].isFile())
				  {
					  if (listOfFiles[i].getName().toLowerCase().equals("goods.dbf")) 		// 0000 0001
				        {
				        	bMain |= 1;
				        }
				        else if (listOfFiles[i].getName().toLowerCase().equals("cust.dbf"))  // 0000 0010
				        {
				        	bMain |= 2;
				        }
				        else if (listOfFiles[i].getName().toLowerCase().equals("char.dbf"))  // 0000 0100
				        {
				        	bMain |= 4;
				        }
				        else if (listOfFiles[i].getName().toLowerCase().equals("debit.dbf"))	// 0000 1000
				        {
				        	bMain |= 8;
				        }
				        else if (listOfFiles[i].getName().toLowerCase().equals("shop.dbf"))  // 0001 0000
				        {
				        	bMain |= 16;
				        }
				        else if (listOfFiles[i].getName().toLowerCase().equals("sklad.dbf"))	// 0010 0000
				        {
				        	bMain |= 32;
				        }
				        else if (listOfFiles[i].getName().toLowerCase().equals("group.dbf")) // 0100 0000
				        {
				        	bMain |= 64;
				        }
				        else if (listOfFiles[i].getName().toLowerCase().equals("brand.dbf")) // 1000 0000
				        {
				        	bMain |= 128;
				        }
				        else if (listOfFiles[i].getName().toLowerCase().equals("workinf.dbf")) // 1 0000 0000
				        {
				        	bMain |= 256;
				        }
				        else if (listOfFiles[i].getName().toLowerCase().equals("plan.dbf")) // 10 0000 0000
				        {
				        	bMain |= 512;
				        }
				        else if (listOfFiles[i].getName().toLowerCase().equals("sold.dbf")) // 100 0000 0000
				        {
				        	bMain |= 1024;
				        }
				        else if (listOfFiles[i].getName().toLowerCase().equals("matrix.dbf")) // 1000 0000 0000
				        {
				        	bMain |= 2048;
				        }
				        else if (listOfFiles[i].getName().toLowerCase().equals("prodlock.dbf")) // 1 0000 0000 0000
				        {
				        	bMain |= 4096;
				        }
				        else if (listOfFiles[i].getName().toLowerCase().equals("skladdet.dbf")) // 100 0000 0000 0000
				        {
				        	bMain |= 16384;
				        }
			      }
			  }

		}
		else if (iFace.equals("CSV"))
		{
			for (int i = 0; i < listOfFiles.length; i++)
			  {
				  if (listOfFiles[i].isFile())
				  {
					  if (listOfFiles[i].getName().toLowerCase().equals("goods.csv")) 		// 0000 0001
				        {
				        	bMain |= 1;
				        }
				        else if (listOfFiles[i].getName().toLowerCase().equals("cust.csv"))  // 0000 0010
				        {
				        	bMain |= 2;
				        }
				        else if (listOfFiles[i].getName().toLowerCase().equals("char.csv"))  // 0000 0100
				        {
				        	bMain |= 4;
				        }
				        else if (listOfFiles[i].getName().toLowerCase().equals("debit.csv"))	// 0000 1000
				        {
				        	bMain |= 8;
				        }
				        else if (listOfFiles[i].getName().toLowerCase().equals("shop.csv"))  // 0001 0000
				        {
				        	bMain |= 16;
				        }
				        else if (listOfFiles[i].getName().toLowerCase().equals("sklad.csv"))	// 0010 0000
				        {
				        	bMain |= 32;
				        }
				        else if (listOfFiles[i].getName().toLowerCase().equals("group.csv")) // 0100 0000
				        {
				        	bMain |= 64;
				        }
				        else if (listOfFiles[i].getName().toLowerCase().equals("brand.csv")) // 1000 0000
				        {
				        	bMain |= 128;
				        }
				        else if (listOfFiles[i].getName().toLowerCase().equals("workinf.csv")) // 1 0000 0000
				        {
				        	bMain |= 256;
				        }
				        else if (listOfFiles[i].getName().toLowerCase().equals("plan.csv")) // 10 0000 0000
				        {
				        	bMain |= 512;
				        }
			      }
			  }

		}
		return bMain;
	}

}



class OrderLines extends HashMap<String, String> implements Parcelable
{
	private static final long serialVersionUID = 1L;

	/////////////////////////////////////////////////////////////////////
	/////////// Parcelable interface methods implementation
	public int describeContents()
	{
        return 0;
    }
	public void writeToParcel(Parcel out, int flags)
	{
		out.writeMap(this);
    }
    public static final Parcelable.Creator<OrderLines> CREATOR = new Parcelable.Creator<OrderLines>()
    {
    	public OrderLines createFromParcel(Parcel in)
    	{
    		return new OrderLines(in);
    	}
    	public OrderLines[] newArray(int size)
    	{
    		return new OrderLines[size];
    	}
    };
    private OrderLines(Parcel in)
    {
    	this("","","","","","","","","","","","","","","","", "", "");		// calling OrderLines() to init values in lines, another way it will be null and NullPointer exeption
    	in.readMap(this, OrderLines.class.getClassLoader());
    }
	/////////// End of Parcelable interface methods implementation
	/////////////////////////////////////////////////////////////

	public OrderLines(	String _id,				String _zakaz_id,
						String _goods_id,		String _text_goods,
						String _restcust,		String _qty,
						String _un,				String _coeff,
						String _discount,		String _pricewnds,
						String _sumwnds,		String _pricewonds,
						String _sumwonds,		String _nds,
						String _delay,			String _weight, String _comment, String _brand_name)
	{
		super();
		super.put(ID, 			_id);
		super.put(ZAKAZ_ID,		_zakaz_id); //zak
		super.put(GOODS_ID, 	_goods_id);
		super.put(TEXT_GOODS, 	_text_goods);
		super.put(RESTCUST, 	_restcust); //res
		super.put(QTY, 			_qty);
		super.put(UN, 			_un); 		//un
		super.put(COEFF, 		_coeff); 	//coef
		super.put(DISCOUNT,		_discount); //dis
		super.put(PRICEWNDS, 	_pricewnds);
		super.put(SUMWNDS, 		_sumwnds);
		super.put(PRICEWONDS, 	_pricewonds);
		super.put(SUMWONDS, 	_sumwonds);
		super.put(NDS,		 	_nds); 		//nds
		super.put(DELAY,	 	_delay); 	//delay
		super.put(WEIGHT,	 	_weight); 	//weight
		super.put(COMMENT,	 	_comment); 	//weight
		super.put(BRAND_NAME, 	_brand_name); 	//brand_name

	}

	public static final String ID 			= RsaDbHelper.LINES_ID;					// Номер заказанного товара по порядку за текущий день (не используется)
	public static final String ZAKAZ_ID   	= RsaDbHelper.LINES_ZAKAZ_ID;     		// ID соответствующего заказа из HEAD
	public static final String GOODS_ID   	= RsaDbHelper.LINES_GOODS_ID;			// ID Товара в 1С
	public static final String TEXT_GOODS 	= RsaDbHelper.LINES_TEXT_GOODS;			// Наименование товара
	public static final String RESTCUST 	= RsaDbHelper.LINES_RESTCUST;			// Не используется
	public static final String QTY 			= RsaDbHelper.LINES_QTY;				// Кол-во заказанного товара
	public static final String UN			= RsaDbHelper.LINES_UN;					// Единица измерения ящ. шт. и т.п.
	public static final String COEFF		= RsaDbHelper.LINES_COEFF;				// Коэф. например заказали 1ящ.(заказ) * 2кг(коэф) = 2 кг (цена умножается на 2)
	public static final String DISCOUNT		= RsaDbHelper.LINES_DISCOUNT;			// Скидка. Не используется всегда = 0
	public static final String PRICEWNDS 	= RsaDbHelper.LINES_PRICEWNDS;			// Цена за единицу с НДС
	public static final String SUMWNDS 		= RsaDbHelper.LINES_SUMWNDS;			// Сумма с НДС = ЦЕНАсНДС * Кол-во * Коэф.
	public static final String PRICEWONDS 	= RsaDbHelper.LINES_PRICEWONDS;			// Цена за единицу без НДС
	public static final String SUMWONDS 	= RsaDbHelper.LINES_SUMWONDS;			// Сумма без НДС = ЦЕНАбезНДС * Кол-во * Коэф.
	public static final String NDS			= RsaDbHelper.LINES_NDS;				// Всего НДС = СуммасНДС - СуммабезНДС
	public static final String DELAY		= RsaDbHelper.LINES_DELAY;				// Отсрочка не используется всегда пусто
	public static final String WEIGHT		= "WEIGHT";								// Вес товара
	public static final String COMMENT		= "COMMENT";							// Коментарий по товару
	public static final String BRAND_NAME	= "BRAND_NAME";							// Бренд товара

}

class OrderRests extends HashMap<String, String> implements Parcelable
{
	private static final long serialVersionUID = 1L;

	/////////////////////////////////////////////////////////////////////
	/////////// Parcelable interface methods implementation
	public int describeContents()
	{
        return 0;
    }
	public void writeToParcel(Parcel out, int flags)
	{
		out.writeMap(this);
    }
    public static final Parcelable.Creator<OrderRests> CREATOR = new Parcelable.Creator<OrderRests>()
    {
    	public OrderRests createFromParcel(Parcel in)
    	{
    		return new OrderRests(in);
    	}
    	public OrderRests[] newArray(int size)
    	{
    		return new OrderRests[size];
    	}
    };
    private OrderRests(Parcel in)
    {
    	this("","","","","","");		// calling OrderLines() to init values in lines, another way it will be null and NullPointer exeption
    	in.readMap(this, OrderRests.class.getClassLoader());
    }
	/////////// End of Parcelable interface methods implementation
	/////////////////////////////////////////////////////////////

	public OrderRests(	String _id,				String _zakaz_id,
						String _goods_id,		String _restqty,
						String _recqty,			String _qty )
	{
		super();
		super.put(ID, 			_id);
		super.put(ZAKAZ_ID,		_zakaz_id); //zak
		super.put(GOODS_ID, 	_goods_id);
		super.put(RESTQTY, 		_restqty);
		super.put(RECQTY,	 	_recqty);
		super.put(QTY, 			_qty);
	}

	public static final String ID 			= RsaDbHelper.RESTS_ID;					// не используется
	public static final String ZAKAZ_ID   	= RsaDbHelper.RESTS_ZAKAZ_ID;     		// ID соответствующего заказа из HEAD
	public static final String GOODS_ID   	= RsaDbHelper.RESTS_GOODS_ID;			// ID Товара в 1С
	public static final String RESTQTY	 	= RsaDbHelper.RESTS_RESTQTY;			// Кол-во остатков в текущего товара при сохр. данного заказа
	public static final String RECQTY	 	= RsaDbHelper.RESTS_RECQTY;				// Кол-во рекомендованого для заказа товара при сохр. данного заказа
	public static final String QTY 			= RsaDbHelper.RESTS_QTY;				// Кол-во заказанного товара в данном заказе
}

class OrderHead implements Parcelable
{
	public int mode;						// MODE of current order (MODIFYING=RSAActivity.IDM_MODIFY or ADDING NEW=IDM_ADD)
	public boolean filled;					// Used for information in HeadActivity... If true then OrderH already filled
	public boolean restored;					// // Used for information in HeadActivity... If true then HeadActivity restored
	public String _id;						// if user choosed order modifying then _id = order's id in database, allows programmers to work with it
	public CharSequence id;
	public CharSequence zakaz_id;
	public CharSequence cust_id;			//ID Клиента
	public CharSequence shop_id;			//ID Торговой точки
	public CharSequence sklad_id;			//ID Склада
	public CharSequence block;
	public CharSequence sended;
	public CharSequence cust_text;
	public CharSequence shop_text;
	public CharSequence sklad_text;
	public CharSequence delay;
	public CharSequence paytype;			//Тип оплаты Нал/Безнал
	public CharSequence hsumo;
	public double debit_total;
	public double debit_actual;
	public CharSequence hweight;			//Вес по заказу
	public CharSequence hvolume;			//Объем по заказу
	public CharSequence date;				//Дата заказа
	public CharSequence time;				//Время заказа
	public CharSequence hnds;
	public CharSequence hndsrate;
	public CharSequence sumwonds;
	public CharSequence numfull;
	public CharSequence num1c;
	public CharSequence gpscoord;
	public CharSequence remark;				//Примечание к заказу
	public CharSequence routecode;			//Маршрут
	public CharSequence visitid;
	public CharSequence delivery;           // Дата планируемой доставки
	ArrayList<OrderLines> lines;
	ArrayList<OrderRests> restslines;

	/////////////////////////////////////////////////////////////////////
	/////////// Parcelable interface methods implementation
	public int describeContents() {
        return 0;
    }
	public void writeToParcel(Parcel out, int flags) {
        out.writeInt(mode);
        out.writeInt(filled?0:1);  // need use next statement on another side: filled = in.readInt() == 0;
        out.writeInt(restored?0:1);  // need use next statement on another side: restored = in.readInt() == 0;
        out.writeString(_id);
        out.writeString(this.id.toString());
        out.writeString(this.zakaz_id.toString());
        out.writeString(this.cust_id.toString());
        out.writeString(this.shop_id.toString());
        out.writeString(this.sklad_id.toString());
        out.writeString(this.block.toString());
        out.writeString(this.sended.toString());
        out.writeString(this.cust_text.toString());
        out.writeString(this.shop_text.toString());
        out.writeString(this.sklad_text.toString());
        out.writeString(this.delay.toString());
        out.writeString(this.paytype.toString());
        out.writeString(this.hsumo.toString());
        out.writeDouble(debit_total);
        out.writeDouble(debit_actual);
        out.writeString(this.hweight.toString());
        out.writeString(this.hvolume.toString());
        out.writeString(this.date.toString());
        out.writeString(this.time.toString());
        out.writeString(this.hnds.toString());
        out.writeString(this.hndsrate.toString());
        out.writeString(this.sumwonds.toString());
        out.writeString(this.numfull.toString());
        out.writeString(this.num1c.toString());
        out.writeString(this.gpscoord.toString());
        out.writeString(this.remark.toString());
        out.writeString(this.routecode.toString());
        out.writeString(this.visitid.toString());
        out.writeString(this.delivery.toString());

        out.writeList(lines);
        out.writeList(restslines);
    }

    public static final Parcelable.Creator<OrderHead> CREATOR = new Parcelable.Creator<OrderHead>()
    {
    	public OrderHead createFromParcel(Parcel in)
    	{
    		return new OrderHead(in);
    	}
    	public OrderHead[] newArray(int size)
    	{
    		return new OrderHead[size];
    	}
    };
    private OrderHead(Parcel in) {
    	this(); // calling OrderHead() to init lines another way it will be null and NullPointer exeption
        mode 		= in.readInt();
        // dual used variable:
        // first point - if mode = MODIFY then filles meens "is data from modifying order is loaded to OrderH(current order)"
        // second point - if mode = ADD then filles meens "is user already selected customer, shop and pressed add(goods) button
        filled 		= in.readInt() == 0;
        restored	= in.readInt() == 0;
        _id 		= in.readString();
        this.id		= in.readString();
        zakaz_id	= in.readString();
        cust_id		= in.readString();
        shop_id		= in.readString();
        sklad_id	= in.readString();
        block		= in.readString();
        sended		= in.readString();
        cust_text	= in.readString();
        shop_text	= in.readString();
        sklad_text	= in.readString();
        delay		= in.readString();
        paytype		= in.readString();
        hsumo		= in.readString();
        debit_total = in.readDouble();
		debit_actual = in.readDouble();
        hweight		= in.readString();
        hvolume		= in.readString();
        date		= in.readString();
        time		= in.readString();
        hnds		= in.readString();
        hndsrate	= in.readString();
        sumwonds	= in.readString();
        numfull		= in.readString();
        num1c		= in.readString();
        gpscoord	= in.readString();
        remark		= in.readString();
        routecode	= in.readString();
        visitid		= in.readString();
        delivery	= in.readString();
        in.readTypedList(lines, OrderLines.CREATOR);
        in.readTypedList(restslines, OrderRests.CREATOR);
    }
	/////////// End of Parcelable interface methods implementation
	/////////////////////////////////////////////////////////////

	public OrderHead()
	{
		lines = new ArrayList<OrderLines>();
		restslines = new ArrayList<OrderRests>();
		this.clear();
	}

	public int isInLines(String id)		//Содержится ли товар с указанным кодом в списке
	{
		for (int i=0;i<lines.size();i++)
		{
			if ( lines.get(i).get(OrderLines.GOODS_ID).equals(id) )
			{
				return i;
			}
		}
		return -1;
	}

	public int isInRests(String id)		//Содержится ли товар с указанным кодом в списке
	{
		for (int i=0;i<restslines.size();i++)
		{
			if ( restslines.get(i).get(OrderRests.GOODS_ID).equals(id) )
			{
				return i;
			}
		}
		return -1;
	}

	public String getQTYbyGoodsIDfromLines(String goodsID)
	{
		for (int i=0;i<lines.size();i++)
		{
			if ( lines.get(i).get(OrderLines.GOODS_ID).equals(goodsID) )
			{
				return lines.get(i).get(OrderLines.QTY);
			}
		}
		return "0";
	}

	public String getRestByGoodsIDfromRests(String goodsID)
	{
		for (int i=0;i<restslines.size();i++)
		{
			if ( restslines.get(i).get(OrderRests.GOODS_ID).equals(goodsID) )
			{
				return restslines.get(i).get(OrderRests.RESTQTY);
			}
		}
		return "0";
	}

	// will return 3 or less rows LIKE THAT:
	//   DATE(3)    SUM(QTY)    SUM(RESTQTY)    SUM(RECQTY)
	// 14.12.2012     5               34             44
	// 13.12.2012  	  6               33             33
	// 12.12.2012     4               45             45
	//
	public String[][] prepareHistoryTable(SQLiteDatabase db_orders, String goodsID)
	{
		String[][] tblHistory = new String[4][3];
		Cursor histCursor = null;

		tblHistory[0][0] = "01.01.2001";
		tblHistory[0][1] = "01.01.2001";
		tblHistory[0][2] = "01.01.2001";

		tblHistory[1][0] = "0";
		tblHistory[1][1] = "0";
		tblHistory[1][2] = "0";

		tblHistory[2][0] = "0";
		tblHistory[2][1] = "0";
		tblHistory[2][2] = "0";

		tblHistory[3][0] = "0";
		tblHistory[3][1] = "0";
		tblHistory[3][2] = "0";

		try
		{
			Calendar c = Calendar.getInstance();
			String today = String.format( "%02d.%02d.%02d", c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.MONTH)+1, c.get(Calendar.YEAR) );

			histCursor = null;
			String qry =     "select DATE, SUM(RESTQTY), SUM(QTY), SUM(RECQTY)"
						+	" from _head"
						+	" inner join _rests"
						+	" on _head._id=_rests.ZAKAZ_ID"
						+	" where (_rests.ZAKAZ_ID IN (select _id from _head where " +
						                                         "CUST_ID='"+cust_id+"' " +
						                                         "and SHOP_ID='"+shop_id+"'))"
						+	" and GOODS_ID='"+goodsID+"'"
						+	" group by DATE"
						+	" order by DATE DESC"
						+	" limit 4";

			histCursor = db_orders.rawQuery(qry, new String[]{});

			int verOS = 2;
			try
			{
				verOS = Integer.parseInt(Build.VERSION.RELEASE.substring(0,1));
			}
			catch (Exception e) {};

			// init mCursor to work with it
	        // startManagingCursor(histCursor);
	        // move to first record in mCursor
			int ordersCount = histCursor.getCount();
			if (ordersCount>0)
			{
				histCursor.moveToFirst();
				if (histCursor.getString(0).equals(today))
				{
					histCursor.moveToNext();
					ordersCount--;
				}

				for (int i=0;i<ordersCount;i++)
				{
					tblHistory[0][i] = histCursor.getString(0);
					tblHistory[1][i] = histCursor.getString(1);
					tblHistory[2][i] = histCursor.getString(2);
					tblHistory[3][i] = histCursor.getString(3);
					histCursor.moveToNext();
				}
			}


			if (histCursor != null)
		    {
		        histCursor.close();
		    }
		}
		catch (Exception e)
		{
			tblHistory[0][0] = "01.01.2001";
			tblHistory[0][1] = "01.01.2001";
			tblHistory[0][2] = "01.01.2001";

			tblHistory[1][0] = "0";
			tblHistory[1][1] = "0";
			tblHistory[1][2] = "0";

			tblHistory[2][0] = "0";
			tblHistory[2][1] = "0";
			tblHistory[2][2] = "0";

			tblHistory[3][0] = "0";
			tblHistory[3][1] = "0";
			tblHistory[3][2] = "0";

			if (histCursor != null)
		    {
		        histCursor.close();
		    }
		}

		return tblHistory;
	}

	public String calculateRecomendOrder(String goodsID, String[][] tblHistory, String strRestCount)
	{

		float restCount = Float.parseFloat(strRestCount); // Текущий остаток
		float lastRestCount = 0;						  // Остаток прошлого визита
		float lastOrderCount = 0;                         // Последний заказ
		double recomend = 0;                              // Рекомендация

		for (int i=0;i<3;i++)
		{
			if (!tblHistory[0][i].equals("-.-.-"))
			{
				lastRestCount  = Float.parseFloat(tblHistory[1][i]);
				lastOrderCount = Float.parseFloat(tblHistory[2][i]);

				recomend = (lastRestCount+lastOrderCount-restCount)*1.5-restCount;

				return Long.toString((long)recomend);
			}
		}


		return "0";
	}

	public void clear()
	{
		id = "";
		zakaz_id = "";
		cust_id = "";
		shop_id = "";
		sklad_id = "";
		block = "";
		sended = "";
		cust_text = "";
		shop_text = "";
		sklad_text = "";
		delay = "0";
		paytype = "Нал";
		hsumo = "";
		debit_total = 0;
		debit_actual = 0;
		hweight = "";
		hvolume = "";
		date = "";
		time = "";
		hnds = "";
		hndsrate ="";
		sumwonds = "";
		numfull = "";
		num1c = "";
		gpscoord = "";
		routecode = "";
		visitid = "";
		delivery = "";
		remark = "";
		filled = false;
		restored = false;
		mode = 101;
		_id="";

		lines.clear();
		restslines.clear();
	}


}


