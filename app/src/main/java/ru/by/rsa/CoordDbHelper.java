package ru.by.rsa;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

/**
 * Class with database architecture, used
 * for working with database (creation, initialisation, etc.)
 * @author Komarev Roman
 * Odessa, neo3da@mail.ru, +380503412392
 */
public class CoordDbHelper extends SQLiteOpenHelper 
		implements BaseColumns
{
	// Table architecture
	public static final String		TABLE_NAME					= "_coord";
	public static final String		  DATE						= "DATE"; 
	public static final String		  TIME						= "TIME";
	public static final String		  COORD						= "COORD";
	// for ANTOR
	public static final String		  SENT						= "SENT";		// Flag 1 - message sent, 0 - message not sent
	public static final String		  AVER						= "AVER";		// Protocol version
	public static final String		  AFLG						= "AFLG";		// Message flag (0,1,2,3)
	public static final String		  ADATE						= "ADATE";		// Date = "ddmmyy"
	public static final String		  AUTC						= "AUTC";		// Time = "hhmmss"
	public static final String		  ALAT						= "ALAT";		// Latitude = "ddmm.mmmm"
	public static final String		  ASIND						= "ASIND";		// S Indicator (N=north or S=south)
	public static final String		  ALONG						= "ALONG";		// Longtitude = dddmm.mmmm
	public static final String		  AWIND						= "AWIND";		// W Indicator (E=east or W=west)
	public static final String		  AALT						= "AALT";		// Altitude meters x.x
	public static final String		  ASPEED					= "ASPEED";		// Speed x.xx (km/h)
	public static final String		  ACOURSE					= "ACOURSE";	// Course xxx.x
	public static final String		  ABAT						= "ABAT";		// Batary level % (0-100%)
	public static final String		  ASTART					= "ASTART";		// Start Flag (1-first point, 0-next point)
	public static final String		  AFIX   					= "AFIX";		// Actual data or not A|V
	public static final String		  FNMEA   					= "NMEAFULL";	// receiced nmea full
	public static final String		  SENTKONTI					= "SENTKONTI";	// Flag 1 - message sent, 0 - message not sent
	
	/**
	 * Database constructor, creates database on device
	 * @param context Activity context that binds database on it
	 */
	public CoordDbHelper(Context context)
	{
		super(context, CoordProvider.DB_COORD, null, 8); // 8 - SENTKONTI ADDED
	}
	
	/**
	 *  If database is not created then creates it
	 *  @param db database
	 */
	@Override
	public void onCreate(SQLiteDatabase db)
	{
		db.execSQL("CREATE TABLE " 		+ TABLE_NAME + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ DATE			+ " TEXT, "
				+ TIME			+ " TEXT, "
				+ COORD			+ " TEXT, "
				+ SENT			+ " TEXT, "
				+ AVER			+ " TEXT, "
				+ AFLG			+ " TEXT, "
				+ ADATE			+ " TEXT, "
				+ AUTC			+ " TEXT, "
				+ ALAT			+ " TEXT, "
				+ ASIND			+ " TEXT, "
				+ ALONG			+ " TEXT, "
				+ AWIND			+ " TEXT, "
				+ AALT			+ " TEXT, "
				+ ASPEED		+ " TEXT, "
				+ ACOURSE		+ " TEXT, "
				+ ABAT			+ " TEXT, "
				+ ASTART		+ " TEXT, "
				+ AFIX			+ " TEXT, "
				+ FNMEA			+ " TEXT, "
				+ SENTKONTI		+ " TEXT)"
		);
	}
	
	/**
	 * Runs if structure of database has been changed
	 * destroy all tables and then creates them with 
	 * new architecture by constructor onCreate()
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
			// db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
			// onCreate(db);
		Log.i("RRR",">UPGRADED !!!!!!!!!!!!!!!!!!!");
		db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD " + SENTKONTI);
	}
	
	/**
	 * Runs when database becomes opened
	 * used for extra SQLite parameters to work with it
	 * in our case turning on integrity monitoring
	 */
	@Override
	public void onOpen(SQLiteDatabase db)
	{
		super.onOpen(db);
//		if (!db.isReadOnly())
//		{
			// SQLite Integrity monitoring turning ON
			// after that incoming data have no 
			// chances to break database architecture
//			db.execSQL("PRAGMA foreign_keys=ON;");
//		}
	}

}
