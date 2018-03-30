package ua.rsa.gd;

import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;

public class CoordProvider extends ContentProvider
{

	public static final String DB_COORD			= "coord.db";
	public static final Uri CONTENT_URI			= Uri.parse(
											"content://ua.rsa.gd.CoordProvider/_coord");
	public static final int URI_CODE			= 1;
	public static final int URI_CODE_ID			= 2;
	
	private static final UriMatcher mUriMatcher;
	private static HashMap<String, String> mCoordMap;
	private SQLiteDatabase db;
	
	static {
		mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		mUriMatcher.addURI("ua.rsa.gd.CoordProvider", CoordDbHelper.TABLE_NAME, URI_CODE);
		mUriMatcher.addURI("ua.rsa.gd.CoordProvider", CoordDbHelper.TABLE_NAME + "/#", URI_CODE_ID);
		mCoordMap = new HashMap<String, String>();
		mCoordMap.put(CoordDbHelper._ID, CoordDbHelper._ID);
		mCoordMap.put(CoordDbHelper.DATE, CoordDbHelper.DATE);
		mCoordMap.put(CoordDbHelper.TIME, CoordDbHelper.TIME);
		mCoordMap.put(CoordDbHelper.COORD, CoordDbHelper.COORD);
		
		mCoordMap.put(CoordDbHelper.SENT, CoordDbHelper.SENT);
		mCoordMap.put(CoordDbHelper.AVER, CoordDbHelper.AVER);
		mCoordMap.put(CoordDbHelper.AFLG, CoordDbHelper.AFLG);
		mCoordMap.put(CoordDbHelper.ADATE, CoordDbHelper.ADATE);
		mCoordMap.put(CoordDbHelper.AUTC, CoordDbHelper.AUTC);
		mCoordMap.put(CoordDbHelper.ALAT, CoordDbHelper.ALAT);
		mCoordMap.put(CoordDbHelper.ASIND, CoordDbHelper.ASIND);
		mCoordMap.put(CoordDbHelper.ALONG, CoordDbHelper.ALONG);
		mCoordMap.put(CoordDbHelper.AWIND, CoordDbHelper.AWIND);
		mCoordMap.put(CoordDbHelper.AALT, CoordDbHelper.AALT);
		mCoordMap.put(CoordDbHelper.ASPEED, CoordDbHelper.ASPEED);
		mCoordMap.put(CoordDbHelper.ACOURSE, CoordDbHelper.ACOURSE);
		mCoordMap.put(CoordDbHelper.ABAT, CoordDbHelper.ABAT);
		mCoordMap.put(CoordDbHelper.ASTART, CoordDbHelper.ASTART);
		mCoordMap.put(CoordDbHelper.SENTKONTI, CoordDbHelper.SENTKONTI);
	}
	
	public String getDbName()
	{
		return(DB_COORD);
	}
	
	@Override
	public int delete(Uri url, String where, String[] whereArgs)
	{
		int retVal = db.delete(CoordDbHelper.TABLE_NAME, where, whereArgs);
		
		getContext().getContentResolver().notifyChange(url, null);
		return retVal;
	}

	@Override
	public String getType(Uri uri)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri url, ContentValues inValues)
	{
		ContentValues values = new ContentValues(inValues);
		
		long rowId = db.insert(CoordDbHelper.TABLE_NAME, CoordDbHelper.DATE, values);
		
		if (rowId > 0)
		{
			Uri uri = ContentUris.withAppendedId(CONTENT_URI, rowId);
			getContext().getContentResolver().notifyChange(uri, null);
			return uri;
		}
		else
		{
			throw new SQLException("Failed to insert row into " + url);
		}
		
	}

	@Override
	public boolean onCreate()
	{
		db = (new CoordDbHelper(getContext())).getWritableDatabase();
		return (db == null)?false:true;
	}

	@Override
	public Cursor query(Uri url, String[] projection, String selection,
			String[] selectionArgs, String sort)
	{
		String orderBy;
		if (TextUtils.isEmpty(sort))
		{
			orderBy = CoordDbHelper._ID;
		}
		else
		{
			orderBy = sort;
		}
		
		Cursor c = db.query(CoordDbHelper.TABLE_NAME, projection, selection, selectionArgs, null, null, orderBy);
		c.setNotificationUri(getContext().getContentResolver(), url);
		return c;
	}

	@Override
	public int update(Uri url, ContentValues values, String where,
			String[] whereArgs)
	{
		int retVal = db.update(CoordDbHelper.TABLE_NAME, values, where, whereArgs);
		
		getContext().getContentResolver().notifyChange(url, null);
		return retVal;
	}

}
