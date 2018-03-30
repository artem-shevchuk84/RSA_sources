package ua.rsa.gd;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import ru.by.rsa.R;
import ua.rsa.gd.adapter.item.ReportItem;

/**
 * About Activity gives info about Application and
 * License
 * @author Komarev Roman
 * Odessa, neo3da@mail.ru, +380503412392
 */
public class ReportActivity extends ListActivity
{
	private SimpleAdapter mAdapterList;
	public static final int IDM_ROUTE = 0;
	public static final int IDM_INCOME = 1;
	public static final int IDM_CUST_DETAILS = 2;
	public static final int IDM_DEBTS = 3;
	public static final int IDM_PRICE = 4;
	public static final int IDM_TOPSKU = 5;
	public static final int IDM_TOTALSALES = 6;
	public static final int IDM_BRANDSALES = 7;
	public static final int IDM_MUTUALDEBTS = 8;
	public static final int IDM_SALOUT = 9;
	public static final int IDM_SALOUT_SKU = 10;
	public static final int IDM_SALES_PLAN = 11;


	public static final String NAME = "NAME";
	public static final String ELEMENTS = "ELEMENTS";
	public static final String COLUMNS = "COLUMNS";
	private boolean lightTheme;
	private static int verOS = 0;
	private Calendar selectedDate;
	private int currentReport;
	private String currency = " грн.";

	String[] custContent = {"_id",  RsaDbHelper.CUST_ID, RsaDbHelper.CUST_NAME, RsaDbHelper.CUST_TEL,
									RsaDbHelper.CUST_ADDRESS, RsaDbHelper.CUST_OKPO, RsaDbHelper.CUST_INN,
									RsaDbHelper.CUST_CONTACT, RsaDbHelper.CUST_DOGOVOR};


	@Override
	public void onCreate(Bundle savedInstanceState) {
		verOS = 2;
		try {
			verOS = Integer.parseInt(Build.VERSION.RELEASE.substring(0,1));
		}
		catch (Exception e) {};

		selectedDate = Calendar.getInstance();
		currentReport = IDM_ROUTE;


		SharedPreferences ddef_prefs = PreferenceManager.getDefaultSharedPreferences(this);
		currency = " "+ddef_prefs.getString("prefCurrency", getResources().getString(R.string.preferences_currency_summary));

		lightTheme = getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE).getBoolean(RsaDb.LIGHTTHEMEKEY, false);
		if (lightTheme) {
			setTheme(R.style.Theme_Custom);
			super.onCreate(savedInstanceState);
			setContentView(R.layout.l_report);
		} else {
			setTheme(R.style.Theme_CustomBlack2);
			super.onCreate(savedInstanceState);
			setContentView(R.layout.report);
		}
	}

	@Override
	public void onStart() {
		super.onStart();

		Button btnBack = (Button)findViewById(R.id.report_pbtn_prev);
        btnBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
		    	finish();
			}
		});

        String[] arrayReports = getResources().getStringArray(R.array.Reports);
        ArrayList<ReportItem> list = new ArrayList<ReportItem>();
        for(int i=0;i<arrayReports.length;i+=2) {
        	list.add(new ReportItem(		arrayReports[i],
        									arrayReports[i+1]));
        }
        mAdapterList = new SimpleAdapter(	this, list, lightTheme?R.layout.l_list_report:R.layout.list_report,
				new String[] {ReportItem.NAME, ReportItem.DETAILS},
				new int[] {R.id.txtName_report, R.id.txtDetails_report});
        setListAdapter(mAdapterList);
	}

	public void onListItemClick(ListView parent, View v, int position, long id) {
		currentReport = position;
		switch (currentReport) {
			case IDM_INCOME: {
				SharedPreferences def_prefs = PreferenceManager.getDefaultSharedPreferences(this);
				if (def_prefs.getBoolean("prefDeepIncome", false)) {
					showDialog(position);
				} else {
					startReport(currentReport, Calendar.getInstance(), null, null);
				}
			} break;
			case IDM_PRICE: {
				Intent intent = new Intent();
				intent.setClass(this, PriceActivity.class);
				startActivity(intent);
			} break;
			case IDM_SALOUT: {
				Intent intent = new Intent();
				intent.setClass(this, SalesActivity.class);
				startActivity(intent);
			} break;
			case IDM_SALES_PLAN: {
				ReportSalesPlanActivity.start(this);
			} break;
			default: {
				showDialog(position);
			} break;
		}
	}

	@Override
    protected Dialog onCreateDialog(int id, Bundle args) {
		switch (id) {
			case IDM_CUST_DETAILS: {
				SharedPreferences prefs = getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);
				final SQLiteDatabase cust_db = openMainDB(prefs.getString(RsaDb.ACTUALDBKEY, RsaDbHelper.DB_NAME1));
				final Cursor custCursor = cust_db.query(RsaDbHelper.TABLE_CUST, custContent, null, null, null, null, RsaDbHelper.CUST_NAME);
				if (custCursor.getCount()<1) {
					Toast.makeText(getApplicationContext(),"No customers found in DB",Toast.LENGTH_SHORT).show();
					return null;
				}
				custCursor.moveToFirst();
				final SimpleCursorAdapter custAdapter = new SimpleCursorAdapter(this, R.layout.list_report_cust,
																				custCursor, custContent,
																				new int[] {0,0,android.R.id.text1,0,0,0,0,0,0});
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
			    builder.setTitle(R.string.report_select_client)
			    	   .setAdapter(custAdapter, new DialogInterface.OnClickListener() {
			               public void onClick(DialogInterface dialog, int which) {
			               custCursor.moveToPosition(which);
			               startReport(IDM_CUST_DETAILS, null, custCursor.getString(1), null);
			           }
			    });
			    return builder.create();
			}
			case IDM_MUTUALDEBTS: {
				SharedPreferences prefs = getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);
				final SQLiteDatabase cust_db = openMainDB(prefs.getString(RsaDb.ACTUALDBKEY, RsaDbHelper.DB_NAME1));
				final Cursor custCursor = cust_db.query(RsaDbHelper.TABLE_CUST, custContent, null, null, null, null, RsaDbHelper.CUST_NAME);
				if (custCursor.getCount()<1) {
					Toast.makeText(getApplicationContext(),"No customers found in DB",Toast.LENGTH_SHORT).show();
					return null;
				}
				custCursor.moveToFirst();
				final SimpleCursorAdapter custAdapter = new SimpleCursorAdapter(this, R.layout.list_report_cust,
																				custCursor, custContent,
																				new int[] {0,0,android.R.id.text1,0,0,0,0,0,0});
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
			    builder.setTitle(R.string.report_select_client)
			    	   .setAdapter(custAdapter, new DialogInterface.OnClickListener() {
			               public void onClick(DialogInterface dialog, int which) {
			               custCursor.moveToPosition(which);
			               startReport(IDM_MUTUALDEBTS, null, custCursor.getString(1), custCursor.getString(2));
			           }
			    });
			    return builder.create();
			}
			default: {
				return new DatePickerDialog( this, mDateSetListener,
						   selectedDate.get(Calendar.YEAR),
						   selectedDate.get(Calendar.MONTH),
						   selectedDate.get(Calendar.DAY_OF_MONTH) );
			}
		}
    }

	private DatePickerDialog.OnDateSetListener mDateSetListener =
            new DatePickerDialog.OnDateSetListener()
    {
		@Override
		public void onDateSet(DatePicker view, int year,
								int month, int day)
		{
        	selectedDate.set(year, month, day);
        	startReport(currentReport, selectedDate, null, null);
		}
	};

	private SQLiteDatabase openMainDB(String db_name) {
		RsaDbHelper mDb = new RsaDbHelper(this, db_name);
		return mDb.getReadableDatabase();
	}

	private void closeMainDB(SQLiteDatabase base, Cursor cur) {
		if (cur != null) {
			cur.close();
		}
		if (base != null) {
			base.close();
		}
	}

	public static int isTop(String[][] array, String id, int _count) {

		for (int i=0;i<_count;i++){
			if (array[i][0].equals(id))
				return i;
		}

		return -1;
	}

	private void startReport(int rep, Calendar date, String id, String id2) {
		Intent intent = new Intent();
		SharedPreferences prefs = getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);

		switch (rep) {
			case IDM_TOTALSALES: {
				SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
				String d = fmt.format(date.getTime());
				int count = 0;
				int countv = 0;
				float sum = 0;
				float sumv = 0;
				float weight = 0;
				float weightv = 0;

				SQLiteDatabase db = openMainDB(RsaDbHelper.DB_ORDERS);
					String[] arrayReports = getResources().getStringArray(R.array.Reports);
					ArrayList<String> elements = new ArrayList<String>();
						elements.add("Параметр:");
						elements.add("Значение:");
					String q =  "select CUST_ID, SHOP_ID from _head " +
								"where SDATE='" + d + "' AND (NUM1C = '' OR NUM1C = '0' ) " +
								"group by CUST_ID, SHOP_ID";
					boolean cursorError = true;
					Cursor mCursorCount = null;
					Cursor mCursorSales = null;
					try {
						mCursorCount = db.rawQuery(q, null);
						cursorError=false;
					} catch (Exception e) {
						cursorError=true;
					}

					if ((cursorError==false) && (mCursorCount.getCount()>0)) {
						count = mCursorCount.getCount();
						q = "select SUM(HSUMO), SUM(HWEIGHT) from _head " +
							"where SDATE='" + d + "' AND (NUM1C = '' OR NUM1C = '0' ) ";
						cursorError = true;
						try {
							mCursorSales = db.rawQuery(q, null);
							cursorError=false;
						} catch (Exception e) {
							e.printStackTrace();
							cursorError=true;
						}

						if ((cursorError==false) && (mCursorSales.getCount()>0)) {
							mCursorSales.moveToFirst();
							sum = mCursorSales.getFloat(0);
							weight = mCursorSales.getFloat(1);
						}
					}

					SharedPreferences def_prefs = PreferenceManager.getDefaultSharedPreferences(this);
					currency = " "+def_prefs.getString("prefCurrency", getResources().getString(R.string.preferences_currency_summary));
					elements.add("ТТ с продажами:");
					elements.add(Integer.toString(count) + " шт.");
					elements.add("Продажи:");
					elements.add(String.format("%.2f"+ currency, sum));
					elements.add("Вес продаж:");
					elements.add(String.format("%.2f кг.", weight/1000));

					/// Кол-во и сумма возвратов
						q = "select CUST_ID, SHOP_ID from _head " +
							"where SDATE='" + d + "' AND NUM1C <> '' AND NUM1C <> '0' " +
							"group by CUST_ID, SHOP_ID";
						cursorError = true;
						Cursor mCursorCountVZT = null;
						Cursor mCursorSalesVZT = null;
						try {
							mCursorCountVZT = db.rawQuery(q, null);
							cursorError=false;
						} catch (Exception e) {
							cursorError=true;
						}

						if ((cursorError==false) && (mCursorCountVZT.getCount()>0)) {
							countv = mCursorCountVZT.getCount();
							q = "select SUM(HSUMO), SUM(HWEIGHT) from _head " +
								"where SDATE='" + d + "' AND NUM1C <> '' AND NUM1C <> '0' ";
							cursorError = true;
							try {
								mCursorSalesVZT = db.rawQuery(q, null);
								cursorError=false;
							} catch (Exception e) {
								e.printStackTrace();
								cursorError=true;
							}

							if ((cursorError==false) && (mCursorSalesVZT.getCount()>0)) {
								mCursorSalesVZT.moveToFirst();
								sumv = mCursorSalesVZT.getFloat(0);
								weightv = mCursorSalesVZT.getFloat(1);
							}


							int eff		= sum==0?100:(int)(100*sumv/sum);
							int eff_w	= weight==0?100:(int)(100*weightv/weight);
							int eff_tt	= count==0?100:(int)(100*countv/count);

							elements.add("ТТ с возвратами:");
							elements.add(Integer.toString(countv) + " шт.");
							elements.add("Сумма возвратов:");
							elements.add(String.format("%.2f"+ currency, sumv));
							elements.add("Вес возвратов:");
							elements.add(String.format("%.2f кг.", weightv/1000));
							elements.add("ВЗРТ/ПРОД"+currency+":");
							elements.add(Integer.toString(eff)+" %");
							elements.add("ВЗРТ/ПРОД кг.:");
							elements.add(Integer.toString(eff_w)+" %");
							elements.add("ТТ ВЗРТ/ТТ ПРОД:");
							elements.add(Integer.toString(eff_tt)+" %");
						}
					/// end

					String[] array_elements = new String[elements.size()];
					elements.toArray(array_elements);
					if (mCursorSales != null)
						mCursorSales.close();
					if (mCursorSalesVZT != null)
						mCursorSalesVZT.close();
					if (mCursorCountVZT != null)
						mCursorCountVZT.close();
				closeMainDB(db, mCursorCount);

				fmt = new SimpleDateFormat("dd.MM.yyyy");
				d = fmt.format(date.getTime());
				intent.setClass(this, ShowreportActivity.class);
				intent.putExtra(NAME, arrayReports[rep+rep] + " (" + d + ")");
				intent.putExtra(ELEMENTS, array_elements);
				intent.putExtra(COLUMNS, 2);
				startActivity(intent);
			} break;
			case IDM_ROUTE: {
				SimpleDateFormat fmt = new SimpleDateFormat("ddMMyyyy");
				String d = fmt.format(date.getTime());

				SQLiteDatabase db = openMainDB(prefs.getString(RsaDb.ACTUALDBKEY, RsaDbHelper.DB_NAME1));
					String[] arrayReports = getResources().getStringArray(R.array.Reports);
					ArrayList<String> elements = new ArrayList<String>();
						elements.add("Клиент:");
						elements.add("Адрес:");
					String q = "select _id, cast(ID as number) as sid, CUST_TEXT, SHOP_TEXT " +
				        		   "from _plan " +
				        		   "where DATEV='" + d + "' " +
				        		   "order by sid";
					boolean cursorError = true;
					Cursor mCursorRoute = null;
					try {
						mCursorRoute = db.rawQuery(q, null);
						cursorError=false;
					} catch (Exception e) {
						cursorError=true;
					}

					String strName = "name";
					if ((cursorError==false)&&(mCursorRoute.getCount()>0)) {
						mCursorRoute.moveToFirst();
						for(int i=0;i<mCursorRoute.getCount();i++) {
							strName = mCursorRoute.getString(2);
							if (strName.length()>20) strName = strName.substring(0, 20);
							elements.add(strName);
							elements.add(mCursorRoute.getString(3));
							mCursorRoute.moveToNext();
						}
					}
					String[] array_elements = new String[elements.size()];
					elements.toArray(array_elements);
				closeMainDB(db, mCursorRoute);

				fmt = new SimpleDateFormat("dd.MM.yyyy");
				d = fmt.format(date.getTime());
				intent.setClass(this, ShowreportActivity.class);
				intent.putExtra(NAME, arrayReports[rep+rep] + " (" + d + ")");
				intent.putExtra(ELEMENTS, array_elements);
				intent.putExtra(COLUMNS, 2);
				startActivity(intent);
			} break;
			case IDM_INCOME: {
				SharedPreferences def_prefs = PreferenceManager.getDefaultSharedPreferences(this);
				SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
				String d = fmt.format(date.getTime());

				SQLiteDatabase db = openMainDB(RsaDbHelper.DB_ORDERS);
					String[] arrayReports = getResources().getStringArray(R.array.Reports);
					ArrayList<String> elements = new ArrayList<String>();
						elements.add("Клиент:");
						elements.add("Деньги:");
				//	String q = "select CUST_ID, SUM(SUM), CUSTNAME " +
				 //       	   "from _kassadet " +
				  //      	   "where DATE='" + d + "' " +
				   //     	   "group by CUST_ID";

					String q =	"select CUST_ID, SUM(SUM), CUSTNAME " +
								"from (	select CUST_ID, SUM, CUSTNAME " +
								"		from _kassadet " +
				        	    "		where DATE='" + d + "' " +
				        	    "		union all " +
				        	    "		select CUST_ID, HSUMO as SUM, CUST_TEXT as CUSTNAME " +
				        	    "		from _kassa " +
				        	    "		where DATE='" + d + "' " +
				        	    "	  )" +
				        	   "group by CUST_ID";

					if (def_prefs.getBoolean("prefDeepIncome", false)) {
						q = "select CUST_ID, SUM(SUM), CUSTNAME " +
					        	   "from _kassadet " +
					        	   "where DATE>='" + d + "' " +
					        	   "group by CUST_ID";
						d = "С " + d;
					}

					boolean cursorError = true;
					Cursor mCursorIncome = null;
					try {
						mCursorIncome = db.rawQuery(q, null);
						cursorError=false;
					} catch (Exception e) {
						e.printStackTrace();
						cursorError=true;
					}
					String strCash = "0.00";
					String strName = "name";
					float flCash = 0;
					float flSum = 0;
					if ((cursorError==false)&&(mCursorIncome.getCount()>0)) {
						mCursorIncome.moveToFirst();
						for(int i=0;i<mCursorIncome.getCount();i++) {
							strName = mCursorIncome.getString(2);

							if (strName.length()>20) strName = strName.substring(0, 20);
							elements.add(strName);
							flCash = Float.parseFloat(mCursorIncome.getString(1));
							strCash = String.format("%.2f", flCash).replace(',','.');
							elements.add(strCash);
							flSum+= flCash;
							mCursorIncome.moveToNext();
						}
					}
					elements.add("Сумма:");
					elements.add(String.format("%.2f", flSum).replace(',','.'));
					String[] array_elements = new String[elements.size()];
					elements.toArray(array_elements);
				closeMainDB(db, mCursorIncome);

				fmt = new SimpleDateFormat("dd.MM.yyyy");
				//d = fmt.format(date.getTime());
				intent.setClass(this, ShowreportActivity.class);
				intent.putExtra(NAME, arrayReports[rep+rep] + " (" + d + ")");
				intent.putExtra(ELEMENTS, array_elements);
				intent.putExtra(COLUMNS, 2);
				startActivity(intent);
			} break;
			case IDM_CUST_DETAILS: {
				SQLiteDatabase db = openMainDB(prefs.getString(RsaDb.ACTUALDBKEY, RsaDbHelper.DB_NAME1));
					String[] arrayReports = getResources().getStringArray(R.array.Reports);
					ArrayList<String> elements = new ArrayList<String>();
						elements.add("Поле:");
						elements.add("Значение:");

					boolean cursorError = true;
					Cursor mCursorCustomers = null;
					Cursor mCursorShops = null;
					try {
						mCursorCustomers = db.query(RsaDbHelper.TABLE_CUST, custContent, RsaDbHelper.CUST_ID + "='" + id + "'", null, null, null, null);
						cursorError=false;
					} catch (Exception e) {
						cursorError=true;
					}

					if (id2 != null) {
						try {
							mCursorShops = db.rawQuery("select TYPE from _shop where CUST_ID='"+ id +"' and ID='" +id2+ "'", null);
						} catch (Exception e) {}
					}

					String strName = "name";
					if ((cursorError==false)&&(mCursorCustomers.getCount()>0)) {
						mCursorCustomers.moveToFirst();

						elements.add("Код 1С");
						elements.add(mCursorCustomers.getString(1));
						elements.add("Наименование");
						elements.add(mCursorCustomers.getString(2));
						elements.add("Конт. лицо");
						elements.add(mCursorCustomers.getString(7));
						elements.add("Договор");
						elements.add(mCursorCustomers.getString(8));
						elements.add("Телефон");
						elements.add(mCursorCustomers.getString(3));
						elements.add("Адрес");
						elements.add(mCursorCustomers.getString(4));
						elements.add("ОКПО");
						elements.add(mCursorCustomers.getString(5));
						elements.add("ИНН");
						elements.add(mCursorCustomers.getString(6));
						if (id2!=null && mCursorShops != null && mCursorShops.getCount()>0) {
							mCursorShops.moveToFirst();
							elements.add("Тип ТТ");
							elements.add(mCursorShops.getString(0));
						}
					}
					String[] array_elements = new String[elements.size()];
					elements.toArray(array_elements);
				closeMainDB(db, mCursorCustomers);

				intent.setClass(this, ShowreportActivity.class);
				intent.putExtra(NAME, arrayReports[rep+rep]);
				intent.putExtra(ELEMENTS, array_elements);
				intent.putExtra(COLUMNS, 2);
				startActivity(intent);
			} break;
			case IDM_DEBTS: {
				SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
				String d = fmt.format(date.getTime());

				SQLiteDatabase db = openMainDB(prefs.getString(RsaDb.ACTUALDBKEY, RsaDbHelper.DB_NAME1));
					String[] arrayReports = getResources().getStringArray(R.array.Reports);
					ArrayList<String> elements = new ArrayList<String>();
						elements.add("Клиент:");
						elements.add("Долг:");
					String q = "select SUM(_debit.SUM), _cust.NAME " +
				        		   "from _debit " +
				        		      "inner join _cust " +
				        		      "on _debit.CUST_ID = _cust.ID " +
				        		   "where CLOSED!='2' " +
				        		   "group by _cust.NAME " +
				        		   "order by _cust.NAME";
					boolean cursorError = true;
					Cursor mCursorDebts = null;
					try {
						mCursorDebts = db.rawQuery(q, null);
						cursorError=false;
					} catch (Exception e) {
						cursorError=true;
					}

					String strName = "name";
					if ((cursorError==false)&&(mCursorDebts.getCount()>0)) {
						mCursorDebts.moveToFirst();
						for(int i=0;i<mCursorDebts.getCount();i++) {
							strName = mCursorDebts.getString(1);
							//if (strName.length()>20) strName = strName.substring(0, 20);
							String fmtCash = "0.00";
							try {
								double c_value = Double.parseDouble(mCursorDebts.getString(0));
								DecimalFormat myFormatter = new DecimalFormat("###,###.###");
							    fmtCash = myFormatter.format(c_value);
							} catch (Exception ee) {
								fmtCash = mCursorDebts.getString(0);
							}
							elements.add(strName);
							elements.add(fmtCash);
							mCursorDebts.moveToNext();
						}
					}
					String[] array_elements = new String[elements.size()];
					elements.toArray(array_elements);
				closeMainDB(db, mCursorDebts);

				fmt = new SimpleDateFormat("dd.MM.yyyy");
				d = fmt.format(date.getTime());
				intent.setClass(this, ShowreportActivity.class);
				intent.putExtra(NAME, arrayReports[rep+rep] + " (" + d + ")");
				intent.putExtra(ELEMENTS, array_elements);
				intent.putExtra(COLUMNS, 2);
				intent.putExtra("REP", IDM_DEBTS);
				startActivity(intent);
			} break;
			case IDM_MUTUALDEBTS: {
				SQLiteDatabase db = openMainDB(prefs.getString(RsaDb.ACTUALDBKEY, RsaDbHelper.DB_NAME1));
					String[] arrayReports = getResources().getStringArray(R.array.Reports);
					ArrayList<String> elements = new ArrayList<String>();
						elements.add(" ");
						elements.add(" ");
						elements.add(" ");
					String q =    "select s.ID, s.NAME, d.RN, d.SUM, d.CLOSED from _debit as d "
								+ "left join _shop as s on s.CUST_ID=d.CUST_ID and s.ID = d.SHOP_ID "
								+ "where CLOSED!='2' and d.CUST_ID='"+id+"' order by s.NAME";
					boolean cursorError = true;
					Cursor mCursorDebts = null;
					try {
						mCursorDebts = db.rawQuery(q, null);
						cursorError=false;
					} catch (Exception e) {
						cursorError=true;
					}

					DecimalFormat myFormatter = new DecimalFormat("###,###.###");
					double totalSale = 0;
					boolean isInitDebt = false;

					if ((cursorError==false)&&(mCursorDebts.getCount()>0)) {
						mCursorDebts.moveToFirst();
						int indexOfOutletTotals = 3;
						int indexOfStartDebt = 0;
						elements.add("тут будет ТТ");
						elements.add("тут будет отгр");
						elements.add("тут будет оплата");

						double sumOfCurrentOutlet = 0;
						double sumOfCurrentOutletCash = 0;
						double endDebt = 0;

						String currentOutletId = mCursorDebts.getString(0)==null?"Undef":mCursorDebts.getString(0);
						String currentOutletName = mCursorDebts.getString(1)==null?"Undef":mCursorDebts.getString(1);


						for(int i=0;i<mCursorDebts.getCount();i++) {
							String fmtCash = "0.00";
							String fmtCashEnd = "0.00";
							double c_value = 0;


							try {
								isInitDebt = mCursorDebts.getString(4).equals("1");
								c_value = Double.parseDouble(mCursorDebts.getString(3).replaceAll(" ","").replace(",", "."));

								if (currentOutletId.equals(mCursorDebts.getString(0)==null?"Undef":mCursorDebts.getString(0))) {

									if (isInitDebt==true) {
											totalSale += c_value;
									} else {
										if (c_value>0)
											sumOfCurrentOutlet += c_value;
										else
											sumOfCurrentOutletCash += (-1)*c_value;
									}

								} else {
									elements.set(indexOfOutletTotals, "##"+currentOutletName);
									elements.set(indexOfOutletTotals+1, myFormatter.format(sumOfCurrentOutlet));
									elements.set(indexOfOutletTotals+2, myFormatter.format(sumOfCurrentOutletCash));
									elements.set(indexOfStartDebt, myFormatter.format(endDebt-sumOfCurrentOutlet+sumOfCurrentOutletCash));
									elements.add("");elements.add("");elements.add("");
									elements.add("тут будет след ТТ");
									elements.add("тут будет след отгр");
									elements.add("тут будет след оплата");

									if (isInitDebt==true) {
										totalSale += c_value;
										sumOfCurrentOutlet = 0;
										sumOfCurrentOutletCash = 0;
									} else {
										if (c_value>0) {
											sumOfCurrentOutlet = c_value;
										} else {
											sumOfCurrentOutletCash = (-1)*c_value;
										}
									}
									currentOutletName = mCursorDebts.getString(1)==null?"Undef":mCursorDebts.getString(1);
									currentOutletId = mCursorDebts.getString(0)==null?"Undef":mCursorDebts.getString(0);
									indexOfOutletTotals = elements.size()-3;
								}

								//DecimalFormat myFormatter = new DecimalFormat("###,###.###");
							    fmtCash = myFormatter.format(c_value>0?c_value:(-1)*c_value);
								fmtCashEnd = myFormatter.format(c_value);
							} catch (Exception ee) {
								fmtCash = mCursorDebts.getString(3);
								fmtCashEnd = fmtCash;
								//fmtCash="zuzu";
							}

							String rn = mCursorDebts.getString(2);
							if (rn.equals("")) {
								indexOfStartDebt = elements.size()+1;
								endDebt = c_value;
								elements.add("##Начальный остаток");
								elements.add("0");
								elements.add("");
								elements.add("##Конечный остаток");
								elements.add(fmtCashEnd);
								elements.add("    ");
							} else {
								elements.add(rn);
								elements.add(c_value>0?fmtCash:"    ");
								elements.add(c_value<0?fmtCash:"    ");
							}


							mCursorDebts.moveToNext();
						}

						elements.set(indexOfOutletTotals, "##"+currentOutletName);
						elements.set(indexOfOutletTotals+1, myFormatter.format(sumOfCurrentOutlet));
						elements.set(indexOfOutletTotals+2, myFormatter.format(sumOfCurrentOutletCash));
						elements.set(indexOfStartDebt, myFormatter.format(endDebt-sumOfCurrentOutlet+sumOfCurrentOutletCash));
					}
					String[] array_elements = new String[elements.size()];
					elements.toArray(array_elements);
				closeMainDB(db, mCursorDebts);

				intent.setClass(this, ShowmutualreportActivity.class);
				intent.putExtra(NAME, arrayReports[rep+rep]+".\n"+id2+" ("+myFormatter.format(totalSale)+")");
				intent.putExtra(ELEMENTS, array_elements);
				intent.putExtra(COLUMNS, 3);
				intent.putExtra("REP", IDM_MUTUALDEBTS);
				startActivity(intent);
			} break;
			case IDM_TOPSKU: {
				SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
				String d = fmt.format(date.getTime());

				SQLiteDatabase db = openMainDB(prefs.getString(RsaDb.ACTUALDBKEY, RsaDbHelper.DB_NAME1));
					String[] arrayReports = getResources().getStringArray(R.array.Reports);
					ArrayList<String> elements = new ArrayList<String>();
						elements.add("ТМЦ:");
						elements.add("Кол-во:");
					String q = "select ID, NAME " +
				        		   "from _goods " +
				        		   "where FLASH='1' " +
				        		   "order by NAME";
					boolean cursorError = true;
					Cursor cTopSku = null;
					try {
						cTopSku = db.rawQuery(q, null);
						cursorError=false;
					} catch (Exception e) {
						cursorError=true;
					}

					String strName = "name";
					String[][] arrayTopSku = null;
					int countTopSku = cTopSku.getCount();
					if ((cursorError==false)&&(countTopSku>0)) {
						arrayTopSku = new String[countTopSku][3];
						cTopSku.moveToFirst();
						for(int i=0;i<countTopSku;i++) {
							strName = cTopSku.getString(1);
							//if (strName.length()>20) strName = strName.substring(0, 20);
							arrayTopSku[i][0] = cTopSku.getString(0);
							arrayTopSku[i][1] = strName;
							arrayTopSku[i][2] = "0";
							cTopSku.moveToNext();
						}
					}
				closeMainDB(db, cTopSku);
				SQLiteDatabase db_orders = openMainDB(RsaDbHelper.DB_ORDERS);
					q = "select GOODS_ID, SUM(QTY) from _lines " +
						"where ZAKAZ_ID in (select _id from _head where SDATE='" + d + "') " +
						"group by GOODS_ID";

					Cursor cLines = null;
					try{
						cLines = db_orders.rawQuery(q, null);
						cursorError=false;
					} catch(Exception e) {
						cursorError=true;
					}
					int countLines = cLines.getCount();
					if ((cursorError==false)&&(countLines>0)) {
						cLines.moveToFirst();
						for (int i=0;i<countLines;i++) {
							int index = isTop(arrayTopSku, cLines.getString(0), countTopSku);
							if (index>=0) {
								arrayTopSku[index][2] = cLines.getString(1);
							}
							cLines.moveToNext();
						}
					}
				closeMainDB(db_orders, cLines);

				for (int i=0;i<countTopSku;i++) {
					elements.add(arrayTopSku[i][1]);
					elements.add("   " + arrayTopSku[i][2]);
				}

				String[] array_elements = new String[elements.size()];
				elements.toArray(array_elements);

				fmt = new SimpleDateFormat("dd.MM.yyyy");
				d = fmt.format(date.getTime());
				intent.setClass(this, ShowreportActivity.class);
				intent.putExtra(NAME, arrayReports[rep+rep] + " (" + d + ")");
				intent.putExtra(ELEMENTS, array_elements);
				intent.putExtra(COLUMNS, 2);
				intent.putExtra("REP", IDM_TOPSKU);
				startActivity(intent);
			} break;
			case IDM_SALOUT_SKU: {
				SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
				String d = fmt.format(date.getTime());
				String[] arrayReports = getResources().getStringArray(R.array.Reports);
				ArrayList<String> elements = new ArrayList<String>();
				elements.add("ТМЦ:");
				elements.add("Кво/Сумма:");
				SQLiteDatabase db_orders = openMainDB(RsaDbHelper.DB_ORDERS);
				String q = "select TEXT_GOODS, SUM(QTY), SUM(SUMWNDS) from _lines " +
						"where ZAKAZ_ID in (select _id from _head where SDATE>='" + d + "') " +
						"group by GOODS_ID order by TEXT_GOODS";
				Cursor cLines = null;
				try{
					cLines = db_orders.rawQuery(q, null);
				} catch(Exception e) {
				}
				int totalCount = 0;
				float totalSum = 0;
                while(cLines.moveToNext()) {
					totalCount+= Integer.parseInt(cLines.getString(1));
					totalSum += Float.parseFloat(cLines.getString(2));
                    elements.add(cLines.getString(0));
                    elements.add(cLines.getString(1) + " шт. / " + cLines.getString(2) + " грн.");
				}
				elements.add("-----------");
				elements.add("-----------");
                elements.add("Итого кол-во:");
                elements.add(Integer.toString(totalCount) + " шт.");
                elements.add("Итого сумма:");
                elements.add(String.format(Locale.getDefault(), "%.2f", totalSum));

				closeMainDB(db_orders, cLines);
				String[] array_elements = new String[elements.size()];
				elements.toArray(array_elements);
				fmt = new SimpleDateFormat("dd.MM.yyyy");
				d = fmt.format(date.getTime());
				intent.setClass(this, ShowreportActivity.class);
				intent.putExtra(NAME, arrayReports[rep+rep] + " (с " + d + ")");
				intent.putExtra(ELEMENTS, array_elements);
				intent.putExtra(COLUMNS, 2);
				intent.putExtra("REP", IDM_SALOUT_SKU);
				startActivity(intent);
			} break;
			case IDM_BRANDSALES: {
				SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
				String d = fmt.format(date.getTime());
				String[] arrayReports = getResources().getStringArray(R.array.Reports);
				ArrayList<String> elements = new ArrayList<String>();
					elements.add("Бренд:");
					elements.add("Сумма:");

				SQLiteDatabase db_orders = openMainDB(RsaDbHelper.DB_ORDERS);
					String q = "select BRAND_NAME, SUM(SUMWNDS) from _lines " +
						"where ZAKAZ_ID in (select _id from _head where (NUM1C='' OR NUM1C = '0') and SDATE='" + d + "') " +
						"group by BRAND_NAME";
					Cursor cLines = null;
					boolean cursorError = true;

					try{
						cLines = db_orders.rawQuery(q, null);
						cursorError=false;
					} catch(Exception e) {
						cursorError=true;
					}
					int countLines = cLines.getCount();
					if ((cursorError==false)&&(countLines>0)) {
						cLines.moveToFirst();
						for (int i=0;i<countLines;i++) {
							elements.add(cLines.getString(0));
							elements.add(cLines.getString(1));
							cLines.moveToNext();
						}
					}
				closeMainDB(db_orders, cLines);

				String[] array_elements = new String[elements.size()];
				elements.toArray(array_elements);

				fmt = new SimpleDateFormat("dd.MM.yyyy");
				d = fmt.format(date.getTime());
				intent.setClass(this, ShowreportActivity.class);
				intent.putExtra(NAME, arrayReports[rep+rep] + " (" + d + ")");
				intent.putExtra(ELEMENTS, array_elements);
				intent.putExtra(COLUMNS, 2);
				intent.putExtra("REP", IDM_BRANDSALES);
				startActivity(intent);
			} break;
			default:
			  break;
		}
	}


}
