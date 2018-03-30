package ru.by.rsa;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Class with database architecture, used
 * for working with database (creation, initialisation, etc.)
 * @author Komarev Roman
 * Odessa, neo3da@mail.ru, +380503412392
 */
public class RsaDbHelper extends SQLiteOpenHelper 
		implements BaseColumns
{
	// Tables architecture
	public static final String		DB_NAME1					= "rsa.db";
	public static final String		DB_NAME2					= "rsa2.db";
	public static final String		DB_ORDERS					= "rsa_ord.db";
	public static final String[]	DB_PREFIX					= {"", "a", "b", "c"};
	public static final String		TABLESID					= "_id";
	public static final String			TABLE_CUST				= "_cust";
	public static final String				CUST_ID				= "ID";
	public static final String				CUST_NAME			= "NAME";
	public static final String				CUST_TEL			= "TEL";
	public static final String				CUST_ADDRESS		= "ADDRESS";
	public static final String				CUST_OKPO			= "OKPO";
	public static final String				CUST_INN			= "INN";
	public static final String				CUST_CONTACT		= "CONTACT";
	public static final String				CUST_DOGOVOR		= "DOGOVOR";
	public static final String				CUST_COMMENT		= "COMMENT";
	public static final String			TABLE_BRAND				= "_brand";
	public static final String				BRAND_ID			= "ID";
	public static final String				BRAND_NAME			= "NAME";
	public static final String				BRAND_COMMENT		= "COMMENT";
	public static final String				BRAND_CPRICE		= "CPRICE";
	public static final String			TABLE_GROUP				= "_group";
	public static final String				GROUP_ID			= "ID";
	public static final String				GROUP_NAME			= "NAME";
	public static final String				GROUP_COMMENT		= "COMMENT";
	public static final String				GROUP_BRAND_ID		= "BRAND_ID";
	public static final String				GROUP_PARENT_NAME	= "PARENT_NAME";
	public static final String			TABLE_SKLAD				= "_sklad";
	public static final String				SKLAD_ID			= "ID";
	public static final String				SKLAD_NAME			= "NAME";
	public static final String				SKLAD_DEFAULT		= "DFLT";
	public static final String			TABLE_SKLADDET			= "_skladdet";
	public static final String				SKLAD_SKLAD_ID		= "SKLAD_ID";
	public static final String				SKLAD_GOODS_ID		= "GOODS_ID";
	public static final String				SKLAD_FUNC_1		= "FUNC_1";
	public static final String				SKLAD_FUNC_2		= "FUNC_2";
	public static final String			TABLE_SHOP				= "_shop";
	public static final String				SHOP_ID				= "ID";
	public static final String				SHOP_CUST_ID		= "CUST_ID";
	public static final String				SHOP_NAME			= "NAME";
	public static final String				SHOP_ADDRESS		= "ADDRESS";
	public static final String				SHOP_TYPE			= "TYPE";
	public static final String				SHOP_CONTACT		= "CONTACT";
	public static final String				SHOP_TEL			= "TEL";
	public static final String				SHOP_GPS			= "GPS";
	public static final String				SHOP_PHOTO			= "PHOTO";
	public static final String			TABLE_SHOPTYPE			= "_stype";
	public static final String				SHOPTYPE_NAME		= "NAME";
	public static final String			TABLE_DEBIT				= "_debit";
	public static final String				DEBIT_ID			= "ID";
	public static final String				DEBIT_CUST_ID		= "CUST_ID";
	public static final String				DEBIT_RN			= "RN";
	public static final String				DEBIT_DATEDOC		= "DATEDOC";
	public static final String				DEBIT_SUM			= "SUM";
	public static final String				DEBIT_DATEPP		= "DATEPP";
	public static final String				DEBIT_CLOSED		= "CLOSED";
	public static final String				DEBIT_COMMENT		= "COMMENT";
	public static final String				DEBIT_SHOP_ID		= "SHOP_ID";
	public static final String				DEBIT_PAYMENT		= "PAYMENT";
	public static final String			TABLE_CHAR				= "_char";
	public static final String				CHAR_ID				= "ID";
	public static final String				CHAR_CUST_ID		= "CUST_ID";
	public static final String				CHAR_BRAND_ID		= "BRAND_ID";
	public static final String				CHAR_DISCOUNT		= "DISCOUNT";
	public static final String				CHAR_DELAY			= "DELAY";
	public static final String				CHAR_PRICE			= "PRICE";
	public static final String			TABLE_PLAN				= "_plan";
	public static final String				PLAN_ID				= "ID";
	public static final String				PLAN_CUST_ID		= "CUST_ID";
	public static final String				PLAN_SHOP_ID		= "SHOP_ID";
	public static final String				PLAN_CUST_TEXT		= "CUST_TEXT";
	public static final String				PLAN_SHOP_TEXT		= "SHOP_TEXT";
	public static final String				PLAN_DATEV			= "DATEV";
	public static final String				PLAN_STATE			= "STATE";
	public static final String			TABLE_PRICETYPE			= "_pricetype";
	public static final String				PRICETYPE_ID		= "ID";
	public static final String				PRICETYPE_NAME		= "NAME";
	public static final String			TABLE_GOODS				= "_goods";
	public static final String				GOODS_ID			= "ID";
	public static final String				GOODS_NPP			= "NPP";
	public static final String				GOODS_NAME			= "NAME";
	public static final String				GOODS_BRAND_ID		= "BRAND_ID";
	public static final String				GOODS_QTY			= "QTY";
	public static final String				GOODS_RESTCUST		= "RESTCUST";
	public static final String				GOODS_REST			= "REST";
	public static final String				GOODS_HIST1			= "HIST1";
	public static final String				GOODS_RESTCUST1		= "RESTCUST1";
	public static final String				GOODS_HIST2			= "HIST2";
	public static final String				GOODS_RESTCUST2		= "RESTCUST2";
	public static final String				GOODS_HIST3			= "HIST3";
	public static final String				GOODS_GROUP_ID		= "GROUP_ID";
	public static final String				GOODS_PRICE1		= "PRICE1";
	public static final String				GOODS_PRICE2		= "PRICE2";
	public static final String				GOODS_PRICE3		= "PRICE3";
	public static final String				GOODS_PRICE4		= "PRICE4";
	public static final String				GOODS_PRICE5		= "PRICE5";
	public static final String				GOODS_PRICE6		= "PRICE6";
	public static final String				GOODS_PRICE7		= "PRICE7";
	public static final String				GOODS_PRICE8		= "PRICE8";
	public static final String				GOODS_PRICE9		= "PRICE9";
	public static final String				GOODS_PRICE10		= "PRICE10";
	public static final String				GOODS_PRICE11		= "PRICE11";
	public static final String				GOODS_PRICE12		= "PRICE12";
	public static final String				GOODS_PRICE13		= "PRICE13";
	public static final String				GOODS_PRICE14		= "PRICE14";
	public static final String				GOODS_PRICE15		= "PRICE15";
	public static final String				GOODS_PRICE16		= "PRICE16";
	public static final String				GOODS_PRICE17		= "PRICE17";
	public static final String				GOODS_PRICE18		= "PRICE18";
	public static final String				GOODS_PRICE19		= "PRICE19";
	public static final String				GOODS_PRICE20		= "PRICE20";
	public static final String				GOODS_DISCOUNT		= "DISCOUNT";
	public static final String				GOODS_PRICEWNDS		= "PRICEWNDS";
	public static final String				GOODS_PRICEWONDS	= "PRICEWONDS";
	public static final String				GOODS_UN			= "UN";
	public static final String				GOODS_COEFF			= "COEFF";
	public static final String				GOODS_SUMWONDS		= "SUMWONDS";
	public static final String				GOODS_SUMWNDS		= "SUMWNDS";
	public static final String				GOODS_WEIGHT1		= "WEIGHT1";
	public static final String				GOODS_WEIGHT		= "WEIGHT";
	public static final String				GOODS_VOLUME1		= "VOLUME1";
	public static final String				GOODS_VOLUME		= "VOLUME";
	public static final String				GOODS_NDS			= "NDS";
	public static final String				GOODS_DATE			= "DATE";
	public static final String				GOODS_FLASH			= "FLASH";
	public static final String			TABLE_SOLD				= "_sold";
	public static final String				SOLD_BRAND_ID		= "BRAND_ID";
	public static final String				SOLD_GROUP_ID		= "GROUP_ID";
	public static final String				SOLD_CUST_ID		= "CUST_ID";
	public static final String				SOLD_SHOP_ID		= "SHOP_ID";
	public static final String				SOLD_COMMENT		= "COMMENT";
	public static final String			TABLE_SALOUT			= "_salout";
	public static final String				SALOUT_INVOICE_NO	= "INVOICE_NO";
	public static final String				SALOUT_DATETIME		= "DATETIME";
	public static final String				SALOUT_CUST_ID		= "CUST_ID";
	public static final String				SALOUT_SHOP_ID		= "SHOP_ID";
	public static final String				SALOUT_WH_ID		= "WH_ID";
	public static final String				SALOUT_CUST_NAME	= "CUST_NAME";
	public static final String				SALOUT_SHOP_NAME	= "SHOP_NAME";
	public static final String				SALOUT_WH_NAME		= "WH_NAME";
	public static final String				SALOUT_GOODS_ID		= "GOODS_ID";
	public static final String				SALOUT_GOODS_NAME	= "GOODS_NAME";
	public static final String				SALOUT_QTY			= "QTY";
	public static final String				SALOUT_PRICE		= "PRICE";
	public static final String				SALOUT_SUM			= "SUM";
	public static final String			TABLE_MATRIX			= "_matrix";
	public static final String				MATRIX_CUST_ID		= "CUST_ID";
	public static final String				MATRIX_SHOP_ID		= "SHOP_ID";
	public static final String				MATRIX_GOODS_ID		= "GOODS_ID";
	public static final String				MATRIX_MATRIX		= "MATRIX";
	public static final String				MATRIX_AVG			= "AVG";
	public static final String				MATRIX_COEF			= "COEF";
	public static final String				MATRIX_DELIVERY		= "DELIVERY";
	public static final String				MATRIX_SHARE		= "SHARE";
	public static final String				MATRIX_VPERCENT		= "VPERCENT";
	public static final String				MATRIX_DATE1		= "DATE1";
	public static final String				MATRIX_REST1		= "REST1";
	public static final String				MATRIX_RETURN1		= "RETURN1";
	public static final String				MATRIX_ORDER1		= "ORDER1";
	public static final String				MATRIX_DATE2		= "DATE2";
	public static final String				MATRIX_REST2		= "REST2";
	public static final String				MATRIX_RETURN2		= "RETURN2";
	public static final String				MATRIX_ORDER2		= "ORDER2";
	public static final String				MATRIX_DATE3		= "DATE3";
	public static final String				MATRIX_REST3		= "REST3";
	public static final String				MATRIX_RETURN3		= "RETURN3";
	public static final String				MATRIX_ORDER3		= "ORDER3";
	public static final String				MATRIX_DATE4		= "DATE4";
	public static final String				MATRIX_REST4		= "REST4";
	public static final String				MATRIX_RETURN4		= "RETURN4";
	public static final String				MATRIX_ORDER4		= "ORDER4";
	public static final String				MATRIX_DATE5		= "DATE5";
	public static final String				MATRIX_REST5		= "REST5";
	public static final String				MATRIX_RETURN5		= "RETURN5";
	public static final String				MATRIX_ORDER5		= "ORDER5";
	public static final String				MATRIX_DATE6		= "DATE6";
	public static final String				MATRIX_REST6		= "REST6";
	public static final String				MATRIX_RETURN6		= "RETURN6";
	public static final String				MATRIX_ORDER6		= "ORDER6";
	public static final String				MATRIX_DATE7		= "DATE7";
	public static final String				MATRIX_REST7		= "REST7";
	public static final String				MATRIX_RETURN7		= "RETURN7";
	public static final String				MATRIX_ORDER7		= "ORDER7";
	public static final String				MATRIX_DATE8		= "DATE8";
	public static final String				MATRIX_REST8		= "REST8";
	public static final String				MATRIX_RETURN8		= "RETURN8";
	public static final String				MATRIX_ORDER8		= "ORDER8";
	public static final String				MATRIX_DATE9		= "DATE9";
	public static final String				MATRIX_REST9		= "REST9";
	public static final String				MATRIX_RETURN9		= "RETURN9";
	public static final String				MATRIX_ORDER9		= "ORDER9";
	public static final String				MATRIX_CUSTOM1		= "CUSTOM1";
	public static final String			TABLE_HIST				= "_hist";
	public static final String				HIST_CUST_ID		= "CUST_ID";
	public static final String				HIST_SHOP_ID		= "SHOP_ID";
	public static final String				HIST_GOODS_ID		= "GOODS_ID";
	public static final String				HIST_QTY			= "QTY";
	public static final String				HIST_PRICE			= "PRICE";
	public static final String				HIST_DATE			= "DATE";
	public static final String				HIST_FLASH			= "FLASH";
	public static final String				HIST_COMMENT		= "COMMENT";
	public static final String				HIST_DISCOUNT		= "DISCOUNT";
	public static final String			TABLE_PRODLOCK			= "_prodlock";
	public static final String				PRODLOCK_CUST_ID	= "CUST_ID";
	public static final String				PRODLOCK_SHOP_ID	= "SHOP_ID";
	public static final String				PRODLOCK_GOODS_ID	= "GOODS_ID";
	public static final String				PRODLOCK_DATE		= "DATE";
	public static final String			TABLE_STATIC_PLAN		= "_statplan";
	public static final String				STATIC_DATETIME		= "DATETIME";
	public static final String				STATIC_CUST_ID		= "CUST_ID";
	public static final String				STATIC_SHOP_ID		= "SHOP_ID";
	public static final String				STATIC_BRAND_ID		= "BRAND_ID";
	public static final String				STATIC_PLAN_SUM		= "PLAN_SUM";
	public static final String				STATIC_PLAN_TOP_QTY	= "PLAN_TOP_QTY";
	public static final String				STATIC_PLAN_QTY		= "PLAN_QTY";
	public static final String				STATIC_ACT_SUM		= "ACT_SUM";
	public static final String				STATIC_ACT_TOP_QTY	= "ACT_TOP_QTY";
	public static final String				STATIC_ACT_QTY		= "ACT_QTY";
	public static final String				STATIC_REST_SUM		= "REST_SUM";
	public static final String				STATIC_REST_TOP_QTY	= "REST_TOP_QTY";
	public static final String				STATIC_REST_QTY		= "REST_QTY";
	public static final String				STATIC_TOTALS		= "TOTALS";
	public static final String			TABLE_HEAD				= "_head";
	public static final String				HEAD_ID				= "ID";
	public static final String				HEAD_ZAKAZ_ID		= "ZAKAZ_ID";
	public static final String				HEAD_CUST_ID		= "CUST_ID";
	public static final String				HEAD_SHOP_ID		= "SHOP_ID";
	public static final String				HEAD_SKLAD_ID		= "SKLAD_ID";
	public static final String				HEAD_BLOCK			= "BLOCK";       ///////+1
	public static final String				HEAD_SENDED			= "SENDED";      ///////+2
	public static final String				HEAD_CUST_TEXT		= "CUST_TEXT";   
	public static final String				HEAD_SHOP_TEXT		= "SHOP_TEXT";
	public static final String				HEAD_SKLAD_TEXT		= "SKLAD_TEXT";
	public static final String				HEAD_DELAY			= "DELAY";
	public static final String				HEAD_PAYTYPE		= "PAYTYPE";
	public static final String				HEAD_HSUMO			= "HSUMO";
	public static final String				HEAD_HWEIGHT		= "HWEIGHT";
	public static final String				HEAD_HVOLUME		= "HVOLUME";
	public static final String				HEAD_DATE			= "DATE";         ///////////+4
	public static final String				HEAD_TIME			= "TIME";         //////////+6
	public static final String				HEAD_HNDS			= "HNDS";
	public static final String				HEAD_HNDSRATE		= "HNDSRATE";
	public static final String				HEAD_SUMWONDS		= "SUMWONDS";
	public static final String				HEAD_NUMFULL		= "NUMFULL";      ////////////+7
	public static final String				HEAD_NUM1C			= "NUM1C";        /////////// +8
	public static final String				HEAD_GPSCOORD		= "GPSCOORD";     /////////// +9
	public static final String				HEAD_REMARK			= "REMARK";
	public static final String				HEAD_ROUTECODE		= "ROUTECODE";
	public static final String				HEAD_VISITID		= "VISITID";
	public static final String				HEAD_SDATE			= "SDATE";        ///////////+5
	public static final String				HEAD_MONITORED		= "MONITORED";    ///////////+3
	public static final String				HEAD_DELIVERY		= "DELIVERY";
	public static final String			TABLE_LINES				= "_lines";
	public static final String				LINES_ID			= "ID";
	public static final String				LINES_ZAKAZ_ID		= "ZAKAZ_ID";
	public static final String				LINES_GOODS_ID		= "GOODS_ID";
	public static final String				LINES_TEXT_GOODS	= "TEXT_GOODS";
	public static final String				LINES_RESTCUST		= "RESTCUST";
	public static final String				LINES_QTY			= "QTY";
	public static final String				LINES_UN			= "UN";
	public static final String				LINES_COEFF			= "COEFF";
	public static final String				LINES_DISCOUNT		= "DISCOUNT";
	public static final String				LINES_PRICEWNDS		= "PRICEWNDS";
	public static final String				LINES_SUMWNDS		= "SUMWNDS";
	public static final String				LINES_PRICEWONDS	= "PRICEWONDS";
	public static final String				LINES_SUMWONDS		= "SUMWONDS";
	public static final String				LINES_NDS			= "NDS";
	public static final String				LINES_DELAY			= "DELAY";
	public static final String				LINES_WEIGHT		= "WEIGHT";
	public static final String				LINES_COMMENT		= "COMMENT";
	public static final String				LINES_BRAND_NAME	= "BRAND_NAME";
	public static final String			TABLE_RESTS				= "_rests";
	public static final String				RESTS_ID			= "ID";
	public static final String				RESTS_ZAKAZ_ID		= "ZAKAZ_ID";
	public static final String				RESTS_GOODS_ID		= "GOODS_ID";
	public static final String				RESTS_RESTQTY		= "RESTQTY";
	public static final String				RESTS_RECQTY		= "RECQTY";
	public static final String				RESTS_QTY			= "QTY";
	public static final String			TABLE_GEOPHOTO			= "_geophoto";
	public static final String				GEOPHOTO_ZAKAZ_ID	= "ZAKAZ_ID";
	public static final String				GEOPHOTO_GPS		= "GPS";
	public static final String				GEOPHOTO_PHOTO		= "PHOTO";
	public static final String				GEOPHOTO_TIMESTAMP	= "TIMESTAMP";
	public static final String				GEOPHOTO_CUST_ID	= "CUST_ID";
	public static final String				GEOPHOTO_SHOP_ID	= "SHOP_ID";
	public static final String			TABLE_KASSA				= "_kassa";
	public static final String				KASSA_ZAKAZ_ID		= "ZAKAZ_ID";
	public static final String				KASSA_CUST_ID		= "CUST_ID";
	public static final String				KASSA_CUST_TEXT 	= "CUST_TEXT";
	public static final String				KASSA_DATE			= "DATE";
	public static final String				KASSA_HSUMO			= "HSUMO";
	public static final String				KASSA_TIME			= "TIME";
	public static final String			TABLE_KASSADET			= "_kassadet";
	public static final String				KASSADET_CUST_ID	= "CUST_ID";
	public static final String				KASSADET_RN 		= "RN";
	public static final String				KASSADET_SUM		= "SUM";
	public static final String				KASSADET_DATE		= "DATE";
	public static final String				KASSADET_FULL		= "FULL";
	public static final String				KASSADET_CUSTNAME	= "CUSTNAME";
	public static final String				KASSADET_TIME			= "TIME";
	public static final String			TABLE_QUEST				= "_quest";
	public static final String				QUEST_ZAKAZ_ID		= "ZAKAZ_ID";
	public static final String				QUEST_QUESTION_ID	= "QUESTION_ID";
	public static final String				QUEST_QUESTION_TEXT	= "QUESTION_TEXT";
	public static final String				QUEST_CORRECT		= "CORRECT";
	public static final String				QUEST_ANSWER		= "ANSWER";
	public static final String				QUEST_COMMENT		= "COMMENT";
	
	private String currentDbName;
	
	
	/**
	 * Database constructor, creates database rsa.db on device
	 * @param context Activity context that binds database on it
	 */
	public RsaDbHelper(Context context, String db_name)
	{
		// Ticket 28: check for DBORDERS version if less the 5 then make upgrade
		super(context, getPrefix(context) + db_name, null, (db_name.equals(DB_ORDERS)?16:24));  // первая - заказы, вторая цифра - основная бд
		currentDbName = db_name;
	}
	
	private static String getPrefix(Context c) {
		SharedPreferences prefs = c.getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE);
		int i = prefs.getInt(RsaDb.DBPREFIXKEY, 0);
		return DB_PREFIX[i];
	}
	/**
	 *  If database is not created then creates it
	 *  @param db database
	 */
	@Override
	public void onCreate(SQLiteDatabase db)
	{
		if (currentDbName.equals(DB_ORDERS)) {
			createHeadDBTable(db);
			createLinesDBTable(db);
			createRestsDBTable(db);
			createKassaDBTable(db);
			createKassadetDBTable(db);
			createQuestDBTable(db);
			createGeophotoDBTable(db);
		} else {
			createCustDBTable(db);
			createBrandDBTable(db);
			createGroupDBTable(db);
			createSkladDBTable(db);
			createShopDBTable(db);
			createShoptypeDBTable(db);
			createDebitDBTable(db);
			createCharDBTable(db);
			createPlanDBTable(db);
			createPriceTypeTable(db);
			createGoodsDBTable(db);
			createSoldDBTable(db);
			createMatrixDBTable(db);
			createProdlockDBTable(db);
			createHistDBTable(db);
			createSkladdetDBTable(db);
			createSaloutDBTable(db);
			createStaticPlanTable(db);
		}
	}
	
	/**
	 * Creates table TABLE_CUST in database 
	 * @param db Database where we have to create table
	 */
	public static void createCustDBTable(SQLiteDatabase db)
	{
		db.execSQL("CREATE TABLE " 		+ TABLE_CUST + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ CUST_ID			+ " TEXT UNIQUE, "
				+ CUST_NAME			+ " TEXT, "
				+ CUST_TEL			+ " TEXT, "
				+ CUST_ADDRESS		+ " TEXT, "
				+ CUST_OKPO			+ " TEXT, "
				+ CUST_INN			+ " TEXT, "
				+ CUST_CONTACT		+ " TEXT, "
				+ CUST_DOGOVOR		+ " TEXT, "
		        + CUST_COMMENT		+ " TEXT)"
		);
	}
	
	/**
	 * Creates table TABLE_BRAND in database 
	 * @param db Database where we have to create table
	 */
	public static void createBrandDBTable(SQLiteDatabase db)
	{
		db.execSQL("CREATE TABLE " 		+ TABLE_BRAND + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ BRAND_ID			+ " TEXT UNIQUE, "
				+ BRAND_NAME		+ " TEXT, "
				+ BRAND_COMMENT		+ " TEXT, "
				+ BRAND_CPRICE		+ " TEXT)"
		);
	}
	
	/**
	 * Creates table TABLE_GROUP in database 
	 * @param db Database where we have to create table
	 */
	public static void createGroupDBTable(SQLiteDatabase db)
	{
		db.execSQL("CREATE TABLE " 		+ TABLE_GROUP + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ GROUP_ID			+ " TEXT UNIQUE, "
				+ GROUP_NAME		+ " TEXT, "
				+ GROUP_COMMENT		+ " TEXT, "
				+ GROUP_BRAND_ID	+ " TEXT, "
				+ GROUP_PARENT_NAME	+ " TEXT);"
		);
	}
	
	/**
	 * Creates table TABLE_SKLAD in database 
	 * @param db Database where we have to create table
	 */
	public static void createSkladDBTable(SQLiteDatabase db)
	{
		db.execSQL("CREATE TABLE " 		+ TABLE_SKLAD + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ SKLAD_ID			+ " TEXT UNIQUE, "
				+ SKLAD_NAME		+ " TEXT, "
				+ SKLAD_DEFAULT		+ " TEXT);"
		);
	}
	
	public static void createSaloutDBTable(SQLiteDatabase db)
	{
		db.execSQL("CREATE TABLE " 		+ TABLE_SALOUT + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ SALOUT_INVOICE_NO		+ " TEXT, "
				+ SALOUT_DATETIME		+ " TEXT, "	
				+ SALOUT_CUST_ID		+ " TEXT, "
				+ SALOUT_SHOP_ID		+ " TEXT, "		
				+ SALOUT_WH_ID			+ " TEXT, "	
				+ SALOUT_CUST_NAME		+ " TEXT, "	
				+ SALOUT_SHOP_NAME		+ " TEXT, "
				+ SALOUT_WH_NAME		+ " TEXT, "		
				+ SALOUT_GOODS_ID		+ " TEXT, "	
				+ SALOUT_GOODS_NAME		+ " TEXT, "	
				+ SALOUT_QTY			+ " TEXT, "		
				+ SALOUT_PRICE			+ " TEXT, "		
				+ SALOUT_SUM			+ " TEXT);"	
		);
	}

	public static void createStaticPlanTable(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " 		+ TABLE_STATIC_PLAN + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ STATIC_DATETIME		+ " TEXT, "
				+ STATIC_CUST_ID		+ " TEXT, "
				+ STATIC_SHOP_ID		+ " TEXT, "
				+ STATIC_BRAND_ID		+ " TEXT, "
				+ STATIC_PLAN_SUM		+ " TEXT, "
				+ STATIC_PLAN_TOP_QTY	+ " TEXT, "
				+ STATIC_PLAN_QTY		+ " TEXT, "
				+ STATIC_ACT_SUM		+ " TEXT, "
				+ STATIC_ACT_TOP_QTY	+ " TEXT, "
				+ STATIC_ACT_QTY		+ " TEXT, "
				+ STATIC_REST_SUM		+ " TEXT, "
				+ STATIC_REST_TOP_QTY	+ " TEXT, "
				+ STATIC_REST_QTY		+ " TEXT, "
				+ STATIC_TOTALS			+ " TEXT);"
		);
	}

	/**
	 * Creates table TABLE_SHOP in database 
	 * @param db Database where we have to create table
	 */
	public static void createShopDBTable(SQLiteDatabase db)
	{
		db.execSQL("CREATE TABLE " 		+ TABLE_SHOP + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ SHOP_ID			+ " TEXT, "
				+ SHOP_CUST_ID		+ " TEXT, "												   	
				+ SHOP_NAME			+ " TEXT, "
				+ SHOP_ADDRESS		+ " TEXT, " 
				+ SHOP_TYPE			+ " TEXT, " 
				+ SHOP_CONTACT		+ " TEXT, " 
				+ SHOP_TEL			+ " TEXT, " 
				+ SHOP_GPS			+ " TEXT, " 
				+ SHOP_PHOTO		+ " TEXT, " 
				+ "FOREIGN KEY(" + SHOP_CUST_ID	+ ") REFERENCES " + TABLE_CUST + "(" + CUST_ID + "));"						
		);
	}
	
	public static void createShoptypeDBTable(SQLiteDatabase db)
	{
		db.execSQL("CREATE TABLE " 		+ TABLE_SHOPTYPE + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ SHOPTYPE_NAME			+ " TEXT);" 					
		);
	}
	
	/**
	 * Creates table TABLE_DEBIT in database 
	 * @param db Database where we have to create table
	 */
	public static void createDebitDBTable(SQLiteDatabase db)
	{
		db.execSQL("CREATE TABLE " 		+ TABLE_DEBIT + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ DEBIT_ID			+ " TEXT UNIQUE, "
				+ DEBIT_CUST_ID		+ " TEXT, "
				+ DEBIT_RN			+ " TEXT, "
				+ DEBIT_DATEDOC		+ " TEXT, "
				+ DEBIT_SUM			+ " TEXT, "
				+ DEBIT_DATEPP		+ " TEXT, "
				+ DEBIT_CLOSED		+ " TEXT, "
				+ DEBIT_COMMENT		+ " TEXT, "
				+ DEBIT_SHOP_ID		+ " TEXT, "
				+ DEBIT_PAYMENT		+ " TEXT, "
				+ "FOREIGN KEY(" + DEBIT_CUST_ID	+ ") REFERENCES " + TABLE_CUST + "(" + CUST_ID + "));"
		);
	}	
	
	/**
	 * Creates table TABLE_CHAR in database 
	 * @param db Database where we have to create table
	 */
	public static void createCharDBTable(SQLiteDatabase db)
	{
		db.execSQL("CREATE TABLE " 		+ TABLE_CHAR + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ CHAR_ID			+ " TEXT, "
				+ CHAR_CUST_ID		+ " TEXT, "
				+ CHAR_BRAND_ID		+ " TEXT, "
				+ CHAR_DISCOUNT		+ " TEXT, "
				+ CHAR_DELAY		+ " TEXT, "
				+ CHAR_PRICE		+ " TEXT, "
				+ "FOREIGN KEY(" + CHAR_CUST_ID	+ ") REFERENCES " + TABLE_CUST + "(" + CUST_ID + "), "
				+ "FOREIGN KEY(" + CHAR_BRAND_ID+ ") REFERENCES " + TABLE_BRAND + "(" + BRAND_ID + "));"
		);
	}	
	
	
	public static void createSkladdetDBTable(SQLiteDatabase db)
	{
		db.execSQL("CREATE TABLE " 		+ TABLE_SKLADDET + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ SKLAD_SKLAD_ID	+ " TEXT, "
				+ SKLAD_GOODS_ID	+ " TEXT, "
				+ SKLAD_FUNC_1		+ " TEXT, "
				+ SKLAD_FUNC_2		+ " TEXT, "
				+ "FOREIGN KEY(" + SKLAD_SKLAD_ID	+ ") REFERENCES " + TABLE_SKLAD + "(" + SKLAD_ID + "), "
				+ "FOREIGN KEY(" + SKLAD_GOODS_ID+ ") REFERENCES " + TABLE_GOODS + "(" + GOODS_ID + "));"
		);
	}
	
	
	/**
	 * Creates table TABLE_CHAR in database 
	 * @param db Database where we have to create table
	 */
	public static void createPlanDBTable(SQLiteDatabase db)
	{
		db.execSQL("CREATE TABLE " 		+ TABLE_PLAN + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ PLAN_ID			+ " TEXT, "
				+ PLAN_CUST_ID		+ " TEXT, "
				+ PLAN_SHOP_ID		+ " TEXT, "
				+ PLAN_CUST_TEXT	+ " TEXT, "
				+ PLAN_SHOP_TEXT	+ " TEXT, "
				+ PLAN_DATEV		+ " TEXT, "
				+ PLAN_STATE		+ " TEXT, "
				+ "FOREIGN KEY(" + PLAN_CUST_ID	+ ") REFERENCES " + TABLE_CUST + "(" + CUST_ID + "), "
				+ "FOREIGN KEY(" + PLAN_SHOP_ID + ") REFERENCES " + TABLE_SHOP + "(" + SHOP_ID + "));"
		);
	}
	
	/**
	 * Creates table TABLE_GOODS in database 
	 * @param db Database where we have to create table
	 */
	public static void createGoodsDBTable(SQLiteDatabase db)
	{
		db.execSQL("CREATE TABLE " 		+ TABLE_GOODS + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ GOODS_ID			+ " TEXT UNIQUE, " 
				+ GOODS_NPP			+ " TEXT, "
				+ GOODS_NAME		+ " TEXT, " 
				+ GOODS_BRAND_ID	+ " TEXT, "
				+ GOODS_QTY			+ " TEXT, " 
				+ GOODS_RESTCUST	+ " TEXT, "
				+ GOODS_REST		+ " TEXT, " 
				+ GOODS_HIST1		+ " TEXT, "
				+ GOODS_RESTCUST1	+ " TEXT, " 
				+ GOODS_HIST2		+ " TEXT, "
				+ GOODS_RESTCUST2	+ " TEXT, " 
				+ GOODS_HIST3		+ " TEXT, "
				+ GOODS_GROUP_ID	+ " TEXT, " 
				+ GOODS_PRICE1		+ " TEXT, "
				+ GOODS_PRICE2		+ " TEXT, "
				+ GOODS_PRICE3		+ " TEXT, "
				+ GOODS_PRICE4		+ " TEXT, "
				+ GOODS_PRICE5		+ " TEXT, "
				+ GOODS_PRICE6		+ " TEXT, "
				+ GOODS_PRICE7		+ " TEXT, "
				+ GOODS_PRICE8		+ " TEXT, "
				+ GOODS_PRICE9		+ " TEXT, "
				+ GOODS_PRICE10		+ " TEXT, "
				+ GOODS_PRICE11		+ " TEXT, "
				+ GOODS_PRICE12		+ " TEXT, "
				+ GOODS_PRICE13		+ " TEXT, "
				+ GOODS_PRICE14		+ " TEXT, "
				+ GOODS_PRICE15		+ " TEXT, "
				+ GOODS_PRICE16		+ " TEXT, "
				+ GOODS_PRICE17		+ " TEXT, "
				+ GOODS_PRICE18		+ " TEXT, "
				+ GOODS_PRICE19		+ " TEXT, "
				+ GOODS_PRICE20		+ " TEXT, " 
				+ GOODS_DISCOUNT	+ " TEXT, "
				+ GOODS_PRICEWNDS	+ " TEXT, "
				+ GOODS_PRICEWONDS	+ " TEXT, "
				+ GOODS_UN			+ " TEXT, "
				+ GOODS_COEFF		+ " TEXT, "
				+ GOODS_SUMWONDS	+ " TEXT, "
				+ GOODS_SUMWNDS		+ " TEXT, "
				+ GOODS_WEIGHT1		+ " TEXT, "
				+ GOODS_WEIGHT		+ " TEXT, "
				+ GOODS_VOLUME1		+ " TEXT, "
				+ GOODS_VOLUME		+ " TEXT, "
				+ GOODS_NDS			+ " TEXT, "
				+ GOODS_DATE		+ " TEXT, "
				+ GOODS_FLASH		+ " TEXT, "
				+ "FOREIGN KEY(" + GOODS_BRAND_ID + ") REFERENCES " + TABLE_BRAND + "(" + BRAND_ID + "), "
				+ "FOREIGN KEY(" + GOODS_GROUP_ID + ") REFERENCES " + TABLE_GROUP + "(" + GROUP_ID + "));"
		);	
	}
	
	public static void createSoldDBTable(SQLiteDatabase db)
	{
		db.execSQL("CREATE TABLE " 		+ TABLE_SOLD + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ SOLD_BRAND_ID			+ " TEXT, "
				+ SOLD_GROUP_ID			+ " TEXT, "
				+ SOLD_CUST_ID			+ " TEXT, "
				+ SOLD_SHOP_ID			+ " TEXT, "
				+ SOLD_COMMENT			+ " TEXT, "
				+ "FOREIGN KEY(" + SOLD_BRAND_ID	+ ") REFERENCES " + TABLE_BRAND + "(" + BRAND_ID + "), "
				+ "FOREIGN KEY(" + SOLD_GROUP_ID	+ ") REFERENCES " + TABLE_GROUP + "(" + GROUP_ID + "), "
				+ "FOREIGN KEY(" + SOLD_CUST_ID		+ ") REFERENCES " + TABLE_CUST 	+ "(" + CUST_ID + "), "
				+ "FOREIGN KEY(" + SOLD_SHOP_ID		+ ") REFERENCES " + TABLE_SHOP 	+ "(" + SHOP_ID + "));"
		);
	}
	
	public static void createProdlockDBTable(SQLiteDatabase db)
	{
		db.execSQL("CREATE TABLE " 		+ TABLE_PRODLOCK + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ PRODLOCK_CUST_ID		+ " TEXT, "
				+ PRODLOCK_SHOP_ID		+ " TEXT, "
				+ PRODLOCK_GOODS_ID		+ " TEXT, "
				+ PRODLOCK_DATE			+ " TEXT, "
				+ "FOREIGN KEY(" + PRODLOCK_CUST_ID	+ ") REFERENCES " + TABLE_CUST + "(" + CUST_ID + "), "
				+ "FOREIGN KEY(" + PRODLOCK_SHOP_ID	+ ") REFERENCES " + TABLE_SHOP 	+ "(" + SHOP_ID + "));"
		);
	}
	
	public static void createHistDBTable(SQLiteDatabase db)
	{
		db.execSQL("CREATE TABLE " 		+ TABLE_HIST + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ HIST_CUST_ID		+ " TEXT, "
				+ HIST_SHOP_ID		+ " TEXT, "
				+ HIST_GOODS_ID		+ " TEXT, "
				+ HIST_QTY			+ " TEXT, "
				+ HIST_PRICE		+ " TEXT, "
				+ HIST_DATE			+ " TEXT, "
				+ HIST_FLASH		+ " TEXT, "
				+ HIST_COMMENT		+ " TEXT, "
				+ HIST_DISCOUNT		+ " TEXT, "
				+ "FOREIGN KEY(" + HIST_CUST_ID	+ ") REFERENCES " + TABLE_CUST + "(" + CUST_ID + "), "
				+ "FOREIGN KEY(" + HIST_SHOP_ID	+ ") REFERENCES " + TABLE_SHOP 	+ "(" + SHOP_ID + "));"
		);
	}
	
	public static void createMatrixDBTable(SQLiteDatabase db)
	{
		db.execSQL("CREATE TABLE " 		+ TABLE_MATRIX + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ MATRIX_CUST_ID			+ " TEXT, "
				+ MATRIX_SHOP_ID			+ " TEXT, "
				+ MATRIX_GOODS_ID			+ " TEXT, "
				+ MATRIX_MATRIX				+ " TEXT, "
				+ MATRIX_AVG				+ " TEXT, "
				+ MATRIX_COEF				+ " TEXT, "
				+ MATRIX_DELIVERY			+ " TEXT, "
				+ MATRIX_SHARE				+ " TEXT, "
				
				+ MATRIX_VPERCENT			+ " TEXT, "
				+ MATRIX_DATE1				+ " TEXT, "
				+ MATRIX_REST1				+ " TEXT, "
				+ MATRIX_RETURN1			+ " TEXT, "
				+ MATRIX_ORDER1				+ " TEXT, "
				+ MATRIX_DATE2				+ " TEXT, "
				+ MATRIX_REST2				+ " TEXT, "
				+ MATRIX_RETURN2			+ " TEXT, "
				+ MATRIX_ORDER2				+ " TEXT, "
				+ MATRIX_DATE3				+ " TEXT, "
				+ MATRIX_REST3				+ " TEXT, "
				+ MATRIX_RETURN3			+ " TEXT, "
				+ MATRIX_ORDER3				+ " TEXT, "
				+ MATRIX_DATE4				+ " TEXT, "
				+ MATRIX_REST4				+ " TEXT, "
				+ MATRIX_RETURN4			+ " TEXT, "
				+ MATRIX_ORDER4 			+ " TEXT, "
				+ MATRIX_DATE5				+ " TEXT, "
				+ MATRIX_REST5				+ " TEXT, "
				+ MATRIX_RETURN5			+ " TEXT, "
				+ MATRIX_ORDER5				+ " TEXT, "
				+ MATRIX_DATE6				+ " TEXT, "
				+ MATRIX_REST6				+ " TEXT, "
				+ MATRIX_RETURN6			+ " TEXT, "
				+ MATRIX_ORDER6				+ " TEXT, "
				+ MATRIX_DATE7				+ " TEXT, "
				+ MATRIX_REST7				+ " TEXT, "
				+ MATRIX_RETURN7			+ " TEXT, "
				+ MATRIX_ORDER7				+ " TEXT, "
				+ MATRIX_DATE8				+ " TEXT, "
				+ MATRIX_REST8				+ " TEXT, "
				+ MATRIX_RETURN8			+ " TEXT, "
				+ MATRIX_ORDER8				+ " TEXT, "
				+ MATRIX_DATE9				+ " TEXT, "
				+ MATRIX_REST9				+ " TEXT, "
				+ MATRIX_RETURN9			+ " TEXT, "
				+ MATRIX_ORDER9				+ " TEXT, "
				+ MATRIX_CUSTOM1			+ " TEXT, "
				+ "FOREIGN KEY(" + MATRIX_CUST_ID	+ ") REFERENCES " + TABLE_CUST + "(" + CUST_ID + "), "
				+ "FOREIGN KEY(" + MATRIX_SHOP_ID	+ ") REFERENCES " + TABLE_SHOP + "(" + SHOP_ID + "), "
				+ "FOREIGN KEY(" + MATRIX_GOODS_ID	+ ") REFERENCES " + TABLE_GOODS	+ "(" + GOODS_ID + "));"
		);
	}
	
	/**
	 * Creates table TABLE_HEAD in database 
	 * @param db Database where we have to create table
	 */
	public static void createHeadDBTable(SQLiteDatabase db)
	{
		db.execSQL("CREATE TABLE " 	+ TABLE_HEAD + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ HEAD_ID			+ " TEXT, "
				+ HEAD_ZAKAZ_ID		+ " TEXT, "
				+ HEAD_CUST_ID		+ " TEXT, "
				+ HEAD_SHOP_ID		+ " TEXT, "
				+ HEAD_SKLAD_ID 	+ " TEXT, "
				+ HEAD_BLOCK		+ " INTEGER, "
				+ HEAD_SENDED		+ " TEXT, "
				+ HEAD_CUST_TEXT	+ " TEXT, "
				+ HEAD_SHOP_TEXT	+ " TEXT, "
				+ HEAD_SKLAD_TEXT	+ " TEXT, "
				+ HEAD_DELAY		+ " TEXT, "
				+ HEAD_PAYTYPE		+ " TEXT, "
				+ HEAD_HSUMO		+ " TEXT, "
				+ HEAD_HWEIGHT		+ " TEXT, "
				+ HEAD_HVOLUME		+ " TEXT, "				
				+ HEAD_DATE			+ " TEXT, "
				+ HEAD_TIME			+ " TEXT, "
				+ HEAD_HNDS			+ " TEXT, "				
				+ HEAD_HNDSRATE		+ " TEXT, "
				+ HEAD_SUMWONDS		+ " TEXT, "
				+ HEAD_NUMFULL		+ " TEXT, "
				+ HEAD_NUM1C		+ " TEXT, "
				+ HEAD_GPSCOORD		+ " TEXT, "
				+ HEAD_REMARK		+ " TEXT, "
				+ HEAD_ROUTECODE	+ " TEXT, "
				+ HEAD_VISITID		+ " TEXT, "
				+ HEAD_SDATE		+ " TEXT, "
				+ HEAD_MONITORED	+ " INTEGER DEFAULT 0, " 
				+ HEAD_DELIVERY		+ " TEXT);" 
			//	+ "FOREIGN KEY(" + HEAD_CUST_ID	+ ") REFERENCES " + TABLE_CUST + "(" + CUST_ID + "), "
			//	+ "FOREIGN KEY(" + HEAD_SKLAD_ID+ ") REFERENCES " + TABLE_SKLAD + "(" + SKLAD_ID + "));"
		);
	}		
	
	/**
	 * Creates table TABLE_LINES in database 
	 * @param db Database where we have to create table
	 */
	public static void createLinesDBTable(SQLiteDatabase db)
	{
		db.execSQL("CREATE TABLE " 	+ TABLE_LINES + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ LINES_ID			+ " TEXT, "
				+ LINES_ZAKAZ_ID	+ " TEXT, "
				+ LINES_GOODS_ID	+ " TEXT, "
				+ LINES_TEXT_GOODS	+ " TEXT, "
				+ LINES_RESTCUST	+ " TEXT, "
				+ LINES_QTY			+ " TEXT, "
				+ LINES_UN			+ " TEXT, "
				+ LINES_COEFF		+ " TEXT, "
				+ LINES_DISCOUNT	+ " TEXT, "
				+ LINES_PRICEWNDS	+ " TEXT, "
				+ LINES_SUMWNDS		+ " TEXT, "
				+ LINES_PRICEWONDS	+ " TEXT, "
				+ LINES_SUMWONDS	+ " TEXT, "
				+ LINES_NDS			+ " TEXT, "
				+ LINES_DELAY		+ " TEXT, "
				+ LINES_WEIGHT		+ " TEXT, "
				+ LINES_COMMENT		+ " TEXT, "
				+ LINES_BRAND_NAME	+ " TEXT);"
			//	+ "FOREIGN KEY(" + LINES_GOODS_ID 	+ ") REFERENCES " + TABLE_GOODS + "(" + GOODS_ID + "));"
		);
	}	
	
	public static void createQuestDBTable(SQLiteDatabase db)
	{
		db.execSQL("CREATE TABLE " 	+ TABLE_QUEST + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ QUEST_ZAKAZ_ID		+ " TEXT, "
				+ QUEST_QUESTION_ID		+ " TEXT, "
				+ QUEST_QUESTION_TEXT	+ " TEXT, "
				+ QUEST_CORRECT			+ " TEXT, "
				+ QUEST_ANSWER			+ " TEXT, "
				+ QUEST_COMMENT			+ " TEXT);"
			//	+ "FOREIGN KEY(" + LINES_GOODS_ID 	+ ") REFERENCES " + TABLE_GOODS + "(" + GOODS_ID + "));"
		);
	}
	
	/**
	 * Creates table TABLE_RESTS in database 
	 * @param db Database where we have to create table
	 */
	public static void createRestsDBTable(SQLiteDatabase db)
	{
		db.execSQL("CREATE TABLE IF NOT EXISTS " 	+ TABLE_RESTS + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ RESTS_ID			+ " TEXT, "
				+ RESTS_ZAKAZ_ID	+ " TEXT, "
				+ RESTS_GOODS_ID	+ " TEXT, "
				+ RESTS_RESTQTY		+ " TEXT, "
				+ RESTS_RECQTY		+ " TEXT, "
				+ RESTS_QTY			+ " TEXT);"
			//	+ "FOREIGN KEY(" + RESTS_GOODS_ID 	+ ") REFERENCES " + TABLE_GOODS + "(" + GOODS_ID + "));"
		);
	}
	
	public static void createGeophotoDBTable(SQLiteDatabase db)
	{
		db.execSQL("CREATE TABLE IF NOT EXISTS " 	+ TABLE_GEOPHOTO + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ GEOPHOTO_ZAKAZ_ID	+ " TEXT, "
				+ GEOPHOTO_GPS		+ " TEXT, "
				+ GEOPHOTO_PHOTO	+ " TEXT, "
				+ GEOPHOTO_TIMESTAMP+ " TEXT, "
				+ GEOPHOTO_CUST_ID	+ " TEXT, "
				+ GEOPHOTO_SHOP_ID	+ " TEXT);"
			//	+ "FOREIGN KEY(" + RESTS_GOODS_ID 	+ ") REFERENCES " + TABLE_GOODS + "(" + GOODS_ID + "));"
		);
	}
	
	/**
	 * Creates table TABLE_KASSA in database 
	 * @param db Database where we have to create table
	 */
	public static void createKassaDBTable(SQLiteDatabase db)
	{
		db.execSQL("CREATE TABLE IF NOT EXISTS " 		+ TABLE_KASSA + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ KASSA_ZAKAZ_ID		+ " TEXT UNIQUE, "
				+ KASSA_DATE			+ " TEXT, "
				+ KASSA_CUST_ID			+ " TEXT, "
				+ KASSA_CUST_TEXT		+ " TEXT, "
				+ KASSA_HSUMO			+ " TEXT, "
				+ KASSA_TIME			+ " TEXT);"
		);
	}
	
	/**
	 * Creates table TABLE_KASSA in database 
	 * @param db Database where we have to create table
	 */
	public static void createKassadetDBTable(SQLiteDatabase db)
	{
		db.execSQL("CREATE TABLE IF NOT EXISTS " 		+ TABLE_KASSADET + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ KASSADET_CUST_ID		+ " TEXT, "
				+ KASSADET_RN			+ " TEXT, "
				+ KASSADET_SUM			+ " TEXT, "
				+ KASSADET_DATE			+ " TEXT, "
				+ KASSADET_FULL			+ " TEXT, "
				+ KASSADET_CUSTNAME		+ " TEXT, "
				+ KASSADET_TIME			+ " TEXT);"
		);
	}
	
	/**
	 * Creates table TABLE_PRICETYPE in database 
	 * @param db Database where we have to create table
	 */
	public static void createPriceTypeTable(SQLiteDatabase db)
	{
		db.execSQL("CREATE TABLE " 	+ TABLE_PRICETYPE + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ PRICETYPE_ID		+ " TEXT UNIQUE, "						
				+ PRICETYPE_NAME	+ " TEXT)"
		);
		// Ticket 28: Fill pricetype table, because it is constant values table
		fillPriceTypeTable(db);		
	}
	
	/**
	 *  Ticket 28: Fill table TABLE_PRICETYPE in database 
	 * @param db Database where we have to fill table
	 */
	private static void fillPriceTypeTable(SQLiteDatabase db)
	{
		for (int i = 1; i < 21; i++)
		{
			db.execSQL("INSERT INTO " + TABLE_PRICETYPE + " (" + PRICETYPE_ID + ", " + PRICETYPE_NAME + ") " 
					+ "VALUES ('" + Integer.toString(i) + "', 'PRICE" + Integer.toString(i) + "')"
			);	
		}
	}
	
	/**
	 * Runs if structure of database has been changed
	 * destroy all tables and then creates them with 
	 * new architecture by constructor onCreate()
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{

		if (currentDbName.equals(DB_ORDERS))
		{
			if (oldVersion<4)
			{
				createKassaDBTable(db);
			}
			if (oldVersion<5)
			{
				createRestsDBTable(db);
			}
			if (oldVersion<6)
			{
				createKassadetDBTable(db);
			}
			if (oldVersion<7)
			{
				db.execSQL("ALTER TABLE " + TABLE_HEAD + " ADD COLUMN " + HEAD_MONITORED + " INTEGER DEFAULT 0");
			}
			if (oldVersion<8)
			{
				db.execSQL("ALTER TABLE " + TABLE_LINES + " ADD COLUMN " + LINES_WEIGHT + " TEXT");
			}
			if (oldVersion<10)
			{
				db.execSQL("ALTER TABLE " + TABLE_LINES + " ADD COLUMN " + LINES_COMMENT + " TEXT");
			}
			if (oldVersion<11)
			{
				db.execSQL("ALTER TABLE " + TABLE_HEAD + " ADD COLUMN " + HEAD_DELIVERY + " TEXT");
			}
			if (oldVersion<12)
			{
				db.execSQL("ALTER TABLE " + TABLE_LINES + " ADD COLUMN " + LINES_BRAND_NAME + " TEXT");
			}
			if (oldVersion<13)
			{
				createQuestDBTable(db);
			}
			if (oldVersion<14)
			{
				try {
					db.execSQL("ALTER TABLE " + TABLE_KASSA    + " ADD COLUMN " + KASSA_TIME + " TEXT");
					db.execSQL("ALTER TABLE " + TABLE_KASSADET + " ADD COLUMN " + KASSADET_TIME + " TEXT");
				} catch (Exception e) {System.out.println("onUpgrade error");}
			}
			if (oldVersion<15)
			{
				createGeophotoDBTable(db);
			}
			if (oldVersion<16)
			{
				try {
					db.execSQL("ALTER TABLE " + TABLE_GEOPHOTO + " ADD COLUMN " + GEOPHOTO_CUST_ID + " TEXT");
					db.execSQL("ALTER TABLE " + TABLE_GEOPHOTO + " ADD COLUMN " + GEOPHOTO_SHOP_ID + " TEXT");
				} catch (Exception e) {System.out.println("onUpgrade error");}
			}
		} else {
			if (oldVersion<4)
			{
				try {
					db.execSQL("ALTER TABLE " + TABLE_BRAND + " ADD COLUMN " + BRAND_COMMENT + " TEXT");
					db.execSQL("ALTER TABLE " + TABLE_GROUP + " ADD COLUMN " + GROUP_COMMENT + " TEXT");
				} catch (Exception e) {System.out.println("onUpgrade error");}
			}
			if (oldVersion<5)
			{
				try {
					createSoldDBTable(db);
				} catch (Exception e) {System.out.println("onUpgrade error");}
			}
			if (oldVersion<6)
			{
				try {
					db.execSQL("ALTER TABLE " + TABLE_DEBIT + " ADD COLUMN " + DEBIT_COMMENT + " TEXT");
				} catch (Exception e) {System.out.println("onUpgrade error");}
			}
			if (oldVersion<7)
			{
				try {
					db.execSQL("ALTER TABLE " + TABLE_GROUP + " ADD COLUMN " + GROUP_BRAND_ID + " TEXT");
				} catch (Exception e) {System.out.println("onUpgrade error");}
			}
			if (oldVersion<8)
			{
				try {
					createProdlockDBTable(db);
				} catch (Exception e) {System.out.println("onUpgrade error");}
			}
			if (oldVersion<10)
			{
				try {
					db.execSQL("ALTER TABLE " + TABLE_SKLAD + " ADD COLUMN " + SKLAD_DEFAULT + " TEXT");
				} catch (Exception e) {System.out.println("onUpgrade error");}
			}
			if (oldVersion<11)
			{
				try {
					createHistDBTable(db);
				} catch (Exception e) {System.out.println("onUpgrade error");}
			}
			if (oldVersion<12)
			{
				try {
					createSkladdetDBTable(db);
				} catch (Exception e) {System.out.println("onUpgrade error");}
			}
			if (oldVersion<13)
			{
				try {
					db.execSQL("ALTER TABLE " + TABLE_BRAND + " ADD COLUMN " + BRAND_CPRICE + " TEXT");
				} catch (Exception e) {System.out.println("onUpgrade error");}
			}
			if (oldVersion<14)
			{
				try {
					db.execSQL("ALTER TABLE " + TABLE_GROUP + " ADD COLUMN " + GROUP_PARENT_NAME + " TEXT");
				} catch (Exception e) {System.out.println("onUpgrade error");}
			}
			if (oldVersion<15)
			{
				try {
					db.execSQL("ALTER TABLE " + TABLE_SHOP + " ADD COLUMN " + SHOP_TYPE + " TEXT");
					db.execSQL("CREATE TABLE " 		+ TABLE_SHOPTYPE + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
							+ SHOPTYPE_NAME			+ " TEXT);" 					
					);
				} catch (Exception e) {System.out.println("onUpgrade error");}
			}
			if (oldVersion<16)
			{
				try {
					db.execSQL("ALTER TABLE " + TABLE_DEBIT + " ADD COLUMN " + DEBIT_SHOP_ID + " TEXT");
				} catch (Exception e) {System.out.println("onUpgrade error");}
			}
			if (oldVersion<17)
			{
				try {
					db.execSQL("ALTER TABLE " + TABLE_DEBIT + " ADD COLUMN " + DEBIT_PAYMENT + " TEXT");
				} catch (Exception e) {System.out.println("onUpgrade error");}
			}
			if (oldVersion<18)
			{
				try {
					db.execSQL("ALTER TABLE " + TABLE_SHOP + " ADD COLUMN " + SHOP_CONTACT + " TEXT");
					db.execSQL("ALTER TABLE " + TABLE_SHOP + " ADD COLUMN " + SHOP_TEL + " TEXT");
				} catch (Exception e) {System.out.println("onUpgrade error");}
			}
			if (oldVersion<20)
			{
				try {
					createSaloutDBTable(db);
				} catch (Exception e) {System.out.println("onUpgrade error");}
			}
			if (oldVersion<21)
			{
				try {
					db.execSQL("ALTER TABLE " + TABLE_SHOP + " ADD COLUMN " + SHOP_GPS + " TEXT");
					db.execSQL("ALTER TABLE " + TABLE_SHOP + " ADD COLUMN " + SHOP_PHOTO + " TEXT");
				} catch (Exception e) {System.out.println("onUpgrade error");}
			}
			if (oldVersion<22)
			{
				try {
					db.execSQL("ALTER TABLE " + TABLE_CUST + " ADD COLUMN " + CUST_COMMENT + " TEXT");
				} catch (Exception e) {System.out.println("onUpgrade error");}
			}
			if (oldVersion<23)
			{
				try {
					db.execSQL("ALTER TABLE " + TABLE_MATRIX + " ADD COLUMN " + MATRIX_CUSTOM1 + " TEXT");
				} catch (Exception e) {System.out.println("onUpgrade error");}
			}
			if (oldVersion<24)
			{
				try {
					createStaticPlanTable(db);
				} catch (Exception e) {System.out.println("onUpgrade error create table");}
			}
			
		}
		System.out.println("onUpgrade = " + oldVersion);
		
	
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
		if (!db.isReadOnly())
		{
			// SQLite Integrity monitoring turning ON
			// after that incoming data have no 
			// chances to break database architecture
			db.execSQL("PRAGMA foreign_keys=ON;");
		}
	}

}
