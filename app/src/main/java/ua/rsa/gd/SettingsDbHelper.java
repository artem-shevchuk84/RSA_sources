package ua.rsa.gd;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import ua.rsa.gd.R;

/**
 * Class with database architecture, used
 * for working with database (creation, initialisation, etc.)
 * @author Komarev Roman
 * Odessa, neo3da@mail.ru, +380503412392
 */
public class SettingsDbHelper extends SQLiteOpenHelper 
		implements BaseColumns
{
	// Tables architecture
	public static final String		DB_NAME							= "settings.db";
	public static final String		TABLESID						= "_id";
	public static final String			TABLE_SETTINGS				= "_settings";
	public static final String				SETTINGS_ID				= "ID";
	public static final String				SETTINGS_FIO			= "FIO";
	public static final String				SETTINGS_CODE			= "CODE";
	public static final String				SETTINGS_ORDERBY		= "ORDERBY";
	public static final String				SETTINGS_LASTSKLAD		= "LASTSKLAD";
	public static final String				SETTINGS_LASTSKLADID	= "LASTSKLADID";
	public static final String				SETTINGS_LASTVAT		= "LASTVAT";
	public static final String				SETTINGS_LASTOPTIM		= "LASTOPTIM";
	public static final String				SETTINGS_LOGIN			= "LOGIN";
	public static final String				SETTINGS_PASSWORD		= "PASSWORD";
	public static final String				SETTINGS_SMTP			= "SMTP";
	public static final String				SETTINGS_SMTPPORT		= "SMTPPORT";
	public static final String				SETTINGS_POP			= "POP";
	public static final String				SETTINGS_POPPORT		= "POPPORT";
	public static final String				SETTINGS_SENDTO			= "SENDTO";
	
	public static final String				FTPSERVER				= "FTPSERVER";
	public static final String				FTPUSER					= "FTPUSER";
	public static final String				FTPPASSWORD				= "FTPPASSWORD";
	public static final String				FTPPORT					= "FTPPORT";
	public static final String				FTPINBOX				= "FTPINBOX";
	public static final String				FTPOUTBOX				= "FTPOUTBOX";
	public static final String				ACTUALDBKEY				= "ACTUALDBKEY";
	
	private String currentDbName;
	private static Context c;
	
	
	public SettingsDbHelper(Context context, String db_name) {
		super(context, db_name, null, 1);
		currentDbName = db_name;
		c = context;
	}
	
	/**
	 *  If database is not created then creates it
	 *  @param db database
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		createSettingsTable(db);
		fillSettings(db);
	}
	
	/**
	 * Creates table TABLE_CUST in database 
	 * @param db Database where we have to create table
	 */
	public static void createSettingsTable(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " 		+ TABLE_SETTINGS + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ SETTINGS_ID			+ " TEXT, "
				+ SETTINGS_FIO			+ " TEXT, "
				+ SETTINGS_CODE			+ " TEXT, "
				+ SETTINGS_ORDERBY		+ " TEXT, "
				+ SETTINGS_LASTSKLAD	+ " TEXT, "
				+ SETTINGS_LASTSKLADID	+ " TEXT, "
				+ SETTINGS_LASTVAT		+ " TEXT, "
				+ SETTINGS_LASTOPTIM	+ " TEXT, "
				+ SETTINGS_LOGIN		+ " TEXT, "
				+ SETTINGS_PASSWORD		+ " TEXT, "
				+ SETTINGS_SMTP			+ " TEXT, "	
				+ SETTINGS_SMTPPORT		+ " TEXT, "	
				+ SETTINGS_POP			+ " TEXT, "	
				+ SETTINGS_POPPORT		+ " TEXT, "
		        + SETTINGS_SENDTO		+ " TEXT, "
		        + FTPSERVER				+ " TEXT, "
		        + FTPUSER				+ " TEXT, "
		        + FTPPASSWORD			+ " TEXT, "
		        + FTPPORT				+ " TEXT, "
		        + FTPINBOX				+ " TEXT, "
		        + FTPOUTBOX				+ " TEXT, "
		        + ACTUALDBKEY			+ " TEXT)"
		);
	}
	
	private static void fillSettings(SQLiteDatabase db)
	{
		String[] itemsOrderBy = c.getResources().getStringArray(R.array.prefOrderBy);
		SharedPreferences prefs = c.getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);
		SharedPreferences prefs_main = c.getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE);
		
		String fio 			= prefs_main.getString(RsaDb.NAMEKEY, "Не выбрано");
		String code 		= prefs_main.getString(RsaDb.CODEKEY, "Не выбрано");
		String orderby 		= prefs_main.getString(RsaDb.ORDERBYKEY, itemsOrderBy[0]);
		String skladlast 	= prefs_main.getString(RsaDb.LASTSKLADNAME, "x");
		String skladid 		= prefs_main.getString(RsaDb.LASTSKLADID, "x");
		String lastvat 		= prefs_main.getString(RsaDb.VATRATE, "1");
		String lastoptim 	= prefs_main.getString(RsaDb.LASTOPTIMKEY, "Не выполнялось");
		String login 		= prefs.getString(RsaDb.EMAILKEY, "");
		String password 	= prefs.getString(RsaDb.PASSWORDKEY, "");
		String smtp 		= prefs.getString(RsaDb.SMTPKEY, "");
		String smtpport		= prefs.getString(RsaDb.SMTPPORTKEY, "25"); 
		String pop 			= prefs.getString(RsaDb.POPKEY, "");
		String popport 		= prefs.getString(RsaDb.POPPORTKEY, "110");
		String sendto 		= prefs.getString(RsaDb.SENDTOKEY, "110");
				
		String ftpserver	= prefs.getString(RsaDb.FTPSERVER, "");
		String ftpuser 		= prefs.getString(RsaDb.FTPUSER, "");
		String ftppassword	= prefs.getString(RsaDb.FTPPASSWORD, "");
		String ftpport 		= prefs.getString(RsaDb.FTPPORT, "");
		String ftpinbox		= prefs.getString(RsaDb.FTPINBOX, "");
		String ftpoutbox	= prefs.getString(RsaDb.FTPOUTBOX, "");	
		String actualdb		= prefs.getString(RsaDb.ACTUALDBKEY, "new");
		
		
		db.execSQL("INSERT INTO _settings VALUES ( 	  null, " +						// _id
													"'1', " +						// id
													"'"+fio+"', " +					// fio
													"'"+code+"', " +				// code
													"'"+orderby+"', " +				// orderby
													"'"+skladlast+"', " +			// skladlast
													"'"+skladid+"', " +				// skladid
													"'"+lastvat+"', " +				// lastvat
													"'"+lastoptim+"', " +			// lastoptim
													"'"+login+"', " +				// login
													"'"+password+"', " +			// password
													"'"+smtp+"', " +				// smtp
													"'"+smtpport+"', " +			// smtpport
													"'"+pop+"', " +					// pop
													"'"+popport+"', " +				// popport
													"'"+sendto+"', " +				// popport
													
													"'"+ftpserver+"', " +			
													"'"+ftpuser+"', " +				
													"'"+ftppassword+"', " +			
													"'"+ftpport+"', " +				
													"'"+ftpinbox+"', " +			
													"'"+ftpoutbox+"', " +
													"'"+actualdb+"' )");			
		
		for (int i = 0; i < 3; i++) {
			db.execSQL("INSERT INTO _settings VALUES ( 	  null, " +						// _id
														"'"+ Integer.toString(i+2) +"', " + // ID
														"'Не указано', " +				// fio
														"'Не указано', " +				// code
														"'"+itemsOrderBy[0]+"', " +		// orderby
														"'x', " +						// skladlast
														"'x', " +						// skladid
														"'1', " +						// lastvat
														"'Не выполнялось', " +			// lastoptim
														"'', " +						// login
														"'', " +						// password
														"'', " +						// smtp
														"'25', " +						// smtpport
														"'', " +						// pop
														"'110', " +						// popport
														"'', " +						// sendto
														"'', " +						
														"'', " +						
														"'', " +						
														"'', " +						
														"'', " +
														"'', " +
														"'new' )");							
		}
	}
	
	/**
	 * Runs if structure of database has been changed
	 * destroy all tables and then creates them with 
	 * new architecture by constructor onCreate()
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}
		
	
	/**
	 * Runs when database becomes opened
	 * used for extra SQLite parameters to work with it
	 * in our case turning on integrity monitoring
	 */
	@Override
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
	}

}
