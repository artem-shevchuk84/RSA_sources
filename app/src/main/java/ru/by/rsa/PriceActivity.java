package ru.by.rsa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleExpandableListAdapter;
import android.widget.Toast;

import ru.by.rsa.utils.DataUtils;

public class PriceActivity extends Activity {
	
	public static final int SHOW_ALL				= 0;
	public static final int SHOW_ONLY_GROUPS		= 1;
	public static final int SHOW_ONLY_BRANDS		= 2;
	
	ArrayList<Map<String, String>> groupData;
	ArrayList<ArrayList<Map<String, String>>> childData;
	ExpandableListView elvPriceList;
	ListView lvPrices;
	SQLiteDatabase db;
	
	private boolean lightTheme;
	  
	  
	@Override
	public void onCreate(Bundle savedInstanceState) {
		lightTheme = getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE).getBoolean(RsaDb.LIGHTTHEMEKEY, false);
		if (lightTheme) 
			setTheme(R.style.Theme_Custom);
		else
			setTheme(R.style.Theme_CustomBlackNoTitleBar);
		
		super.onCreate(savedInstanceState);
		setContentView(lightTheme?R.layout.l_price:R.layout.price);
		
		updateList();
	}
	
	@Override
	protected void onStart() {
		super.onStart();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}
	
	@Override
	public void onBackPressed() {
		if (db.isOpen()) db.close();
    	finish();
	}
	
	@Override
    protected void onDestroy() {    	
    	super.onDestroy();
    	if (db.isOpen()) db.close();
    }
	
	@Override
    public void onCreateContextMenu(ContextMenu menu, View v,ContextMenu.ContextMenuInfo menuInfo) 
    {
    	menu.add(Menu.NONE, SHOW_ALL,  		   Menu.NONE, R.string.group_showall);
    	menu.add(Menu.NONE, SHOW_ONLY_GROUPS,  Menu.NONE, R.string.group_showgroups);
    	menu.add(Menu.NONE, SHOW_ONLY_BRANDS,  Menu.NONE, R.string.group_showbrands);
    }
	
	@Override
    public boolean onContextItemSelected(MenuItem item) {
		SharedPreferences prefs = getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);
		
    	switch (item.getItemId()) {
       		case SHOW_ALL: {
       			prefs.edit().putInt(RsaDb.BRANDGROUPSHOW, SHOW_ALL).commit();
       			updateList();
       			return(true);
       		}
       		case SHOW_ONLY_GROUPS: {
       			prefs.edit().putInt(RsaDb.BRANDGROUPSHOW, SHOW_ONLY_GROUPS).commit();
       			updateList();
       			return(true);
       		}
       		case SHOW_ONLY_BRANDS: {
       			prefs.edit().putInt(RsaDb.BRANDGROUPSHOW, SHOW_ONLY_BRANDS).commit();
       			updateList();
       			return(true);
       		}
    	}
    	return(super.onOptionsItemSelected(item));
   }
	
	private void updateList() {
		if (getDataFromDatabase() == false) {
			Toast.makeText(getApplicationContext(), "Нет данных в БД!", Toast.LENGTH_LONG).show();
			return;
		}
		if (showPrice() == false) {
			Toast.makeText(getApplicationContext(), "Невозможно отобразить!", Toast.LENGTH_LONG).show();
			return;
		}
	}
	
	private boolean getDataFromDatabase() {
		Map<String, String> m;
		ArrayList<Map<String, String>> childDataItem;
		//// fill cursors
			Cursor cBrands;
			Cursor cGroups;
			Cursor cGoods = null;
			String qBrands	= "select ID, NAME from _brand order by NAME";
			String qGroups	= "select ID, NAME from _group order by NAME";
			String qGoods	= "select NPP, ID, NAME, REST from _goods where ";
			int countBrands	= 0;
			int countGroups = 0;
			int countGoods = 0;
			
			SharedPreferences prefs = getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);
			RsaDbHelper mDb = new RsaDbHelper(this, prefs.getString(RsaDb.ACTUALDBKEY, RsaDbHelper.DB_NAME1));
			db = mDb.getReadableDatabase();
			
			cBrands = db.rawQuery(qBrands, null);
			cGroups = db.rawQuery(qGroups, null);
			
			countBrands = cBrands.getCount();
			countGroups = cGroups.getCount();
		//// end
		
			int whatShow = prefs.getInt(RsaDb.BRANDGROUPSHOW, SHOW_ALL);
		// load groups
		try {
			groupData = new ArrayList<Map<String, String>>();
			childData = new ArrayList<ArrayList<Map<String, String>>>();
			if ((countBrands>0)&&(whatShow!=SHOW_ONLY_GROUPS)) {
				cBrands.moveToFirst();
		        for (int i=0;i<countBrands;i++) {
		        	cGoods = db.rawQuery(qGoods + "BRAND_ID='"+ cBrands.getString(0) +"'", null);
		        	countGoods = cGoods.getCount();
		        	if (countGoods>0) {
		        		// add group
			        	m = new HashMap<String, String>();
			            m.put("groupName", cBrands.getString(1)); 
			            groupData.add(m);
			            
			            // add items
			            childDataItem = new ArrayList<Map<String, String>>();
			            cGoods.moveToFirst();
			            for (int j=0;j<countGoods;j++) {
				        	m = new HashMap<String, String>();
				        	m.put("childName", cGoods.getString(2));
				        	m.put("childId", cGoods.getString(1));
				        	m.put("childRest", cGoods.getString(3));
				        	childDataItem.add(m);
				        	cGoods.moveToNext();
				        }
				        childData.add(childDataItem);
		        	}
		            cBrands.moveToNext();
		        }
		    }
			if ((countGroups>0)&&(whatShow!=SHOW_ONLY_BRANDS)) {
				cGroups.moveToFirst();
		        for (int i=0;i<countGroups;i++) {
		        	cGoods = db.rawQuery(qGoods + "GROUP_ID='"+ cGroups.getString(0) +"'", null);
		        	countGoods = cGoods.getCount();
		        	if (countGoods>0) {
		        		// add group
			        	m = new HashMap<String, String>();
			            m.put("groupName", cGroups.getString(1)); 
			            groupData.add(m);
			            
			            // add items
			            childDataItem = new ArrayList<Map<String, String>>();
			            cGoods.moveToFirst();
			            for (int j=0;j<countGoods;j++) {
				        	m = new HashMap<String, String>();
				        	m.put("childName", cGoods.getString(2)); 
				        	m.put("childId", cGoods.getString(1));
				        	m.put("childRest", cGoods.getString(3));
				        	childDataItem.add(m);
				        	cGoods.moveToNext();
				        }
				        childData.add(childDataItem);
		        	}
		            cGroups.moveToNext();
		        }
			}
		} catch (Exception e) {return false;}

		if (groupData.isEmpty()) return false;
		
		try {
			if (cBrands != null) cBrands.close();
			if (cGroups != null) cGroups.close();
			if (cGoods != null) cGoods.close();
		} catch (Exception e) {}
		return true;
	}
	
	private boolean showPrice() {
		
		String groupFrom[] = new String[] {"groupName"};
        int groupTo[] = new int[] {android.R.id.text1};
        String childFrom[] = new String[] {"childName", "childId", "childRest"};
        int childTo[] = new int[] {R.id.txt_price_name, 0, R.id.txt_price_rest};
        
		SimpleExpandableListAdapter adapter = new SimpleExpandableListAdapter(
	            this,
	            groupData,
	            lightTheme?R.layout.l_expandable_list_item:R.layout.expandable_list_item,
	            groupFrom,
	            groupTo,
	            childData,
	            R.layout.simple_list_item,
	            childFrom,
	            childTo);
	            
	    elvPriceList = (ExpandableListView) findViewById(R.id.price_list);
	    elvPriceList.setAdapter(adapter);
	    elvPriceList.setOnChildClickListener(new OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				
				String cId = childData.get(groupPosition).get(childPosition).get("childId");
				if (showList(cId)==false) 
					Toast.makeText(getApplicationContext(), "Нет цен", Toast.LENGTH_SHORT).show();
				return false;
			}
		});
	    registerForContextMenu(elvPriceList);
		
		return true;
	}
	
	private boolean showList(String cId) {
		Map<String, String> m;
		ArrayList<Map<String, String>> listData;
		
		String qPrices = "select PRICE1, PRICE2, PRICE3, PRICE4, PRICE5, " +
								"PRICE6, PRICE7, PRICE8, PRICE9, PRICE10, " +
								"PRICE11, PRICE12, PRICE13, PRICE14, PRICE15, " +
								"PRICE16, PRICE17, PRICE18, PRICE19, PRICE20, NDS " +
						 "from _goods " +
						 "where ID='" + cId + "'";
		Cursor cPrices = db.rawQuery(qPrices, null);
		if (cPrices.getCount()<1) 
			return false;
		
		String[] arrayPrices = getResources().getStringArray(R.array.Prices);
		cPrices.moveToFirst();
		
		listData = new ArrayList<Map<String, String>>();
		for (int i=0;i<20;i++) {
			m = new HashMap<String, String>();
        	m.put("priceName", arrayPrices[i]);
        	float price = 0;
        	float nds = 0;
        	try {
        		price = Float.parseFloat(cPrices.getString(i));
	        	nds = Float.parseFloat(cPrices.getString(20));
        	} catch (Exception e) {}
        	price = price + price*nds;
        	m.put("priceFloat", DataUtils.Float.format("%.2f", price));
        	listData.add(m);
		}
		
        String listFrom[] = new String[] {"priceName", "priceFloat"};
        int listTo[] = new int[] {R.id.price_text1, R.id.price_text2};
        
        try {
			SimpleAdapter adapter = new SimpleAdapter(
		            this,
		            listData,
		            R.layout.list_prices,
		            listFrom,
		            listTo);
		            
		    lvPrices = (ListView) findViewById(R.id.price_listView);
		    lvPrices.setAdapter(adapter);
        } catch (Exception e) { return false;}
		
        try {
			if (cPrices != null) cPrices.close();
		} catch (Exception e) {}
		return true;
	}
}
