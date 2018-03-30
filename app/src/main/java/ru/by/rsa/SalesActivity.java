package ru.by.rsa;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ExpandableListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleExpandableListAdapter;
import android.widget.Toast;

import ru.by.rsa.adapter.item.ComboSkladItem;

public class SalesActivity extends Activity {
	
	private boolean lightTheme;
	ArrayList<Map<String, String>> groupData;
	ArrayList<ArrayList<Map<String, String>>> childData;
	ExpandableListView elvSaloutList;
	SQLiteDatabase db;
	
	String dateFrom;
	String dateTo;
	String custID;
	String shopID;
	String custName;
	String shopName;
	
	ArrayList<ComboSkladItem> 	 listCust;
	ArrayList<ComboSkladItem> 	 listShop;
	
	private final static int IDD_DATEFROM	= 1;
	private final static int IDD_DATETO		= 2;
	private final static int IDD_CUST		= 3;
	private final static int IDD_SHOP		= 4;
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		lightTheme = getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE).getBoolean(RsaDb.LIGHTTHEMEKEY, false);
		setTheme(R.style.Theme_Custom);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.l_salout);	
		
		Calendar c = Calendar.getInstance();
		SimpleDateFormat sfm = new SimpleDateFormat("yyyy-MM-dd");
		dateTo = sfm.format(c.getTime());
		dateFrom = "2015-01-01";
		custID="";
		shopID="";
		
		Button btnDateFrom = (Button)findViewById(R.id.btnSalesDateFrom);
		Button btnDateTo = (Button)findViewById(R.id.btnSalesDateTo);
		Button btnCust = (Button)findViewById(R.id.btnSalesCust);
		Button btnShop = (Button)findViewById(R.id.btnSalesShop);
		
		btnDateFrom.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showDialog(IDD_DATEFROM);
			}
		});
		btnDateTo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showDialog(IDD_DATETO);
			}
		});
		btnCust.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showDialog(IDD_CUST);
			}
		});
		btnShop.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showDialog(IDD_SHOP);
			}
		});
		
		fillCustList();
		fillShopList();
		
		updateList();
	}
	
	void fillCustList() {
		SharedPreferences prefs = getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);
		RsaDbHelper mDb = new RsaDbHelper(this, prefs.getString(RsaDb.ACTUALDBKEY, RsaDbHelper.DB_NAME1));
		db = mDb.getReadableDatabase();
		String query = "select distinct CUST_ID, CUST_NAME from _salout order by CUST_NAME";
		Cursor cursor = db.rawQuery(query, null);
		listCust = new ArrayList<ComboSkladItem>();
		listCust.add(new ComboSkladItem("","Все"));
		while (cursor.moveToNext()) {
			listCust.add(new ComboSkladItem(cursor.getString(0), cursor.getString(1)));
		}
		
		if (cursor!=null && !cursor.isClosed()) 
			cursor.close();
		if (db!=null && db.isOpen()) 
			db.close();
	}
	
	void fillShopList() {
		SharedPreferences prefs = getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);
		RsaDbHelper mDb = new RsaDbHelper(this, prefs.getString(RsaDb.ACTUALDBKEY, RsaDbHelper.DB_NAME1));
		db = mDb.getReadableDatabase();
		String query = "select distinct SHOP_ID, SHOP_NAME from _salout where CUST_ID = '" + custID +"'";
		Cursor cursor = db.rawQuery(query, null);
		listShop = new ArrayList<ComboSkladItem>();
		listShop.add(new ComboSkladItem("","Все"));
		while (cursor.moveToNext()) {
			listShop.add(new ComboSkladItem(cursor.getString(0), cursor.getString(1)));
		}
		
		if (cursor!=null && !cursor.isClosed()) 
			cursor.close();
		if (db!=null && db.isOpen()) 
			db.close();
	}
	
	void updateList() {
		Button btnDateFrom = (Button)findViewById(R.id.btnSalesDateFrom);
		Button btnDateTo = (Button)findViewById(R.id.btnSalesDateTo);
		Button btnCust = (Button)findViewById(R.id.btnSalesCust);
		Button btnShop = (Button)findViewById(R.id.btnSalesShop);
			
		btnCust.setText(custID.equals("")?"Все":custName);
		
		
		btnShop.setText(shopID.equals("")?"Все":shopName);	
		
		btnDateFrom.setText("с "+dateFrom);
		btnDateTo.setText("по "+dateTo);
		
		if (getDataFromDatabase() == false) {
			Toast.makeText(getApplicationContext(), "Нет данных в БД!", Toast.LENGTH_LONG).show();
			return;
		}
		if (showSales() == false) {
			Toast.makeText(getApplicationContext(), "Невозможно отобразить!", Toast.LENGTH_LONG).show();
			return;
		}
	}
	
	private boolean getDataFromDatabase() {
		Map<String, String> m;
		Map<String, String> currentInvoiceData;
		ArrayList<Map<String, String>> childDataItem;
			
		SharedPreferences prefs = getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);
		RsaDbHelper mDb = new RsaDbHelper(this, prefs.getString(RsaDb.ACTUALDBKEY, RsaDbHelper.DB_NAME1));
		db = mDb.getReadableDatabase();
		
		SimpleDateFormat sfm = new SimpleDateFormat("yyyy-MM-dd");
		Calendar c = Calendar.getInstance();
		try {
			c.setTime(sfm.parse(dateTo));
		} catch (Exception e) {}
		c.add(Calendar.DATE, 1);
		String tillDate = sfm.format(c.getTime());
		
		String query =  "select INVOICE_NO, DATETIME, WH_NAME, GOODS_NAME, PRICE, QTY, SUM " +
						"from _salout where " +
						"DATETIME BETWEEN '" + dateFrom + "' AND '" + tillDate + "' " +
						(custID.equals("")?"":"AND CUST_ID = '" + custID + "' ") +
						(shopID.equals("")?"":"AND SHOP_ID = '" + shopID + "' ") +
 						"order by DATETIME";
		Cursor cursor = db.rawQuery(query, null);
		try {
			groupData = new ArrayList<Map<String, String>>();
			childData = new ArrayList<ArrayList<Map<String, String>>>();
			if (cursor.moveToFirst()) {
				while (!cursor.isAfterLast()) {
					   String invoice_no = cursor.getString(0);
			        // add group
				       m = new HashMap<String, String>();
				       currentInvoiceData = m;
				       m.put("groupName", invoice_no); 
				       m.put("groupDate", cursor.getString(1)); 
				       m.put("groupWh",   cursor.getString(2)); 
				       m.put("groupSum", "0"); 
				       groupData.add(m);
				           
				       float invoiceSum = 0;
				    // add items
				       childDataItem = new ArrayList<Map<String, String>>();
				       do {
					        	m = new HashMap<String, String>();
					        	m.put("childName", cursor.getString(3));
					        	m.put("childPrice", cursor.getString(4));
					        	m.put("childQty", cursor.getString(5));
					        	m.put("childSum", cursor.getString(6));
					        	childDataItem.add(m);
					        	try {
							    	invoiceSum += Float.parseFloat(cursor.getString(6));
							    } catch (Exception e) {}
					   } while (cursor.moveToNext() && invoice_no.equals(cursor.getString(0)));
					   childData.add(childDataItem);
					   
					   String sum = "0.00";
					   try {
						   sum = String.format("%.2f",invoiceSum).replace(",", ".");
					   } catch (Exception e) {}
					   
					   currentInvoiceData.put("groupSum", sum);
					   groupData.set(groupData.size()-1, currentInvoiceData);
				}
			}
			
			if (cursor!=null && !cursor.isClosed()) 
				cursor.close();
			if (db!=null && db.isOpen()) 
				db.close();
			
		} catch (Exception e) {Log.d ("SSSS", e.getMessage());return false;}

		
		if (groupData.isEmpty()) return false;
		
		return true;
	}
	
	private boolean showSales() {
		
		String groupFrom[] = new String[] {"groupDate", "groupName", "groupWh", "groupSum"};
        int groupTo[] = new int[] {R.id.txtSalout_date, R.id.txtSalout_invoice, R.id.txtSalout_wh, R.id.txtTotalSalout_sum};
        String childFrom[] = new String[] {"childName", "childPrice", "childQty", "childSum"};
        int childTo[] = new int[] {R.id.txtSalout_item_name, R.id.txtSalout_item_price, R.id.txtSalout_item_qty, R.id.txtSalout_item_sum};
        
		SimpleExpandableListAdapter adapter = new SimpleExpandableListAdapter(
	            this,
	            groupData,
	            R.layout.l_salout_list,
	            groupFrom,
	            groupTo,
	            childData,
	            R.layout.list_item_salout,
	            childFrom,
	            childTo);
	            
	    elvSaloutList = (ExpandableListView) findViewById(R.id.salout_list);
	    elvSaloutList.setAdapter(adapter);
		
		return true;
	}

	 private DatePickerDialog.OnDateSetListener mDateSetFromListener =
	            new DatePickerDialog.OnDateSetListener() {
			@Override
			public void onDateSet(DatePicker view, int year,
									int monthOfYear, int dayOfMonth) {
				Calendar c = Calendar.getInstance();
				c.set(year, monthOfYear, dayOfMonth);
				SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
				dateFrom = fmt.format(c.getTime());
				updateList();
			}
	 };
	 
	 private DatePickerDialog.OnDateSetListener mDateSetToListener =
	            new DatePickerDialog.OnDateSetListener() {
			@Override
			public void onDateSet(DatePicker view, int year,
									int monthOfYear, int dayOfMonth) {
				Calendar c = Calendar.getInstance();
				c.set(year, monthOfYear, dayOfMonth);
				SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
				dateTo = fmt.format(c.getTime());
				updateList();
			}
	 };
		
	@Override
	protected Dialog onCreateDialog(int id) {
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
		Calendar calendarFrom = Calendar.getInstance();
		Calendar calendarTo = Calendar.getInstance();
		try {
			calendarFrom.setTime(fmt.parse(dateFrom));
			calendarTo.setTime(fmt.parse(dateTo));
		} catch (Exception e) {}
		
		switch(id) {
			case IDD_DATEFROM: {
				return new DatePickerDialog(this, mDateSetFromListener,
						calendarFrom.get(Calendar.YEAR), calendarFrom.get(Calendar.MONTH), calendarFrom.get(Calendar.DAY_OF_MONTH));
			}
			case IDD_DATETO: {
				return new DatePickerDialog(this, mDateSetToListener,
						calendarTo.get(Calendar.YEAR), calendarTo.get(Calendar.MONTH), calendarTo.get(Calendar.DAY_OF_MONTH));
			}
			case IDD_CUST: {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("Выберите клиента");
				builder.setAdapter(new SimpleAdapter(this, listCust, android.R.layout.simple_list_item_1, 
														              new String[] {ComboSkladItem.NAME}, 
														              new int[] {android.R.id.text1}), 
								   new DialogInterface.OnClickListener(){
										@Override
										public void onClick(DialogInterface dialog, int which){
											custName = listCust.get(which).get(ComboSkladItem.NAME);
											if (custName.equals("Все") == false) {
												custID 	 = listCust.get(which).get(ComboSkladItem.ID);
											} else {
												custID = "";
											}
											shopID = "";
											fillShopList();
											updateList();
										}
								   }
				);
				builder.setCancelable(true);
				return builder.create();
			} 
			case IDD_SHOP: {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("Выберите торг. точку");
				builder.setAdapter(new SimpleAdapter(this, listShop, android.R.layout.simple_list_item_1, 
														              new String[] {ComboSkladItem.NAME}, 
														              new int[] {android.R.id.text1}), 
								   new DialogInterface.OnClickListener(){
										@Override
										public void onClick(DialogInterface dialog, int which){
											shopName = listShop.get(which).get(ComboSkladItem.NAME);
											if (shopName.equals("Все") == false) {
												shopID 	 = listShop.get(which).get(ComboSkladItem.ID);
											} else {
												shopID = "";
											}
											updateList();
										}
								   }
				);
				builder.setCancelable(true);
				return builder.create();
			}
			default: return null;
		}
	}
	
	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {	
		switch(id) {	
			case IDD_DATEFROM: {
			} break;
			case IDD_DATETO: {
			} break;
			case IDD_CUST: {
			} break;
			case IDD_SHOP: {
				 AlertDialog ad = (AlertDialog) dialog;
				 ad.getListView().setAdapter(new SimpleAdapter(this, listShop, android.R.layout.simple_list_item_1, 
								              new String[] {ComboSkladItem.NAME}, 
								              new int[] {android.R.id.text1})
				 );
			} break;
			default:
		}
	}
}
