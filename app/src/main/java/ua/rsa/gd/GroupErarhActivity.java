package ua.rsa.gd;

import java.util.ArrayList;
import java.util.List;


import ru.by.rsa.R;
import ua.rsa.gd.erarh.NLevelAdapter;
import ua.rsa.gd.erarh.NLevelItem;
import ua.rsa.gd.erarh.NLevelView;



import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

/**
 * Activity that allows user to view list goods groups
 * and user can select one them to add some products in
 * next activity (GoodsActivity) 
 * @author Komarev Roman
 * Odessa, neo3da@mail.ru, +380503412392
 */
public class GroupErarhActivity extends Activity {

	List<NLevelItem> list;
	ListView listView;
	
	static final int PICK_GOODS_REQUEST = 0;
	
	//ArrayList<Map<String, String>> groupData;
	//ArrayList<ArrayList<Map<String, String>>> childData;
	//ExpandableListView elvPriceList;
	
	/** Current position in ListView */
	int iidx;
	int ttop;
	int groupPos;
	
	private OrderHead mOrderH;
	private ArrayList<String> arrayPricetypes;
	private boolean lightTheme;
	private static int verOS = 0;
	
	/**
	 * Creates the activity
	 * @param savedInstanceState previous saved state of activity
	 */
	public void onCreate(Bundle savedInstanceState) {
		Bundle extras;

		verOS = 2;
		try {
			verOS = Integer.parseInt(Build.VERSION.RELEASE.substring(0,1));
		} catch (Exception e) {};
		lightTheme = getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE).getBoolean(RsaDb.LIGHTTHEMEKEY, false);
		
		if (lightTheme) {
			setTheme(R.style.Theme_Custom);
			super.onCreate(savedInstanceState);
			setContentView(R.layout.l_igroup);
		} else {
			setTheme(R.style.Theme_CustomBlack2);
			super.onCreate(savedInstanceState);
			setContentView(R.layout.igroup);
		}
		
		iidx = 0;
		ttop = 0;
		groupPos= -1;
		
		if (savedInstanceState != null) {
        	extras = savedInstanceState;
        } else {
    		extras = getIntent().getExtras();
        }
		

		
		// Init class to store data of current order before save it to database
        mOrderH = new OrderHead();
		// Get data of current order from back activity(LinesActivity.class) to this */
		mOrderH = extras.getParcelable("ORDERH");
		
		updateList();
	}
	
	
	
	/**
	 * Metgod that starts every time when Activity is shown on display
	 */	
	@Override
	public void onStart()
	{
		super.onStart();
		
	    /** Get Shared Preferences */
		SharedPreferences prefs = getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);
        
        arrayPricetypes = new ArrayList<String>();
		if (getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE).getBoolean(RsaDb.PRICETYPEKEY, false)) {
			try {
				RsaDbHelper mDb = new RsaDbHelper(this, prefs.getString(RsaDb.ACTUALDBKEY, RsaDbHelper.DB_NAME1));
		        SQLiteDatabase db = mDb.getReadableDatabase();
				loadPriceTypes(db, arrayPricetypes, mOrderH.cust_id.toString());
				db.close();
			}catch (Exception e) {
				Toast.makeText(getApplicationContext(), "Не заполнены типы цен!", Toast.LENGTH_LONG).show();
			}
		}
		
        Button btnBack = (Button)findViewById(R.id.group_pbtn_prev);
        btnBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				Bundle b = new Bundle();
				b.putBoolean("ISFROMGROUP",true);
		    	intent.putExtra("ORDERH", mOrderH);
		    	intent.putExtras(b);
		    	setResult(RESULT_OK, intent);
		    	finish();
			}				
		});
        
        LayoutInflater inflater = null;
        
        if (listView == null) {
        
	        listView = (ListView) findViewById(R.id.igroup_list);
			list = new ArrayList<NLevelItem>();
			inflater = LayoutInflater.from(this);
			
			if (getDataFromDatabase(inflater) == false) {
				Toast.makeText(getApplicationContext(), "Нет данных в БД!", Toast.LENGTH_LONG).show();
				return;
			}
			
			if (showList(inflater) == false) {
				Toast.makeText(getApplicationContext(), "Невозможно отобразить!", Toast.LENGTH_LONG).show();
				return;
			}
        }
        updateList();
	}
	
	private void updateList() {
		
		
		try {
        	/** Ticket 33: Used to get Sum from lines in special class holder OrderH */
			ArrayList<OrderLines> mOrder = mOrderH.lines;
			/** Ticket 33: Used to calculate "Sum with NDS" of all goods in current order */
			float hsumo = 0;
			// Ticket 33: FOR statement for every item in ArrayList
			for (OrderLines CurLine : mOrder) 
			{
				//  Ticket 33: SUM all "sum with nds" from ordered goods one by one
				hsumo += Float.parseFloat(CurLine.get(OrderLines.SUMWNDS));
			} // Ticket 33: At the end of cycle hsumo stores total sum with nds of current order
			/** Ticket 33: Binding xml element of textview to variable */
			TextView txtTotalSum = (TextView)findViewById(R.id.group_txtTotalSum_text);
			// Ticket 33: Set xml text view of current order sum with calculated sum
			txtTotalSum.setText(String.format("%.2f",hsumo).replace(',', '.') + " $");
        } catch (Exception e) {
        	Toast.makeText(getApplicationContext(),  "Некритичный сбой",Toast.LENGTH_LONG).show();
        }
		
	}
	
	private boolean showList(LayoutInflater inflater) {
		
		NLevelAdapter adapter = new NLevelAdapter(list);
		listView.setAdapter(adapter);
		
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				NLevelAdapter adapter = (NLevelAdapter)listView.getAdapter();
				adapter.toggle(arg2);
				adapter.getFilter().filter();
				NLevelItem item = (NLevelItem) adapter.getItem(arg2);
				SomeObject group = (SomeObject) item.getWrappedObject();
				if (group.id.equals("$$") == false) {
					String cId = group.id;
					String cName = group.name;
					Intent intent = new Intent();
					intent.putExtra("isTOPSKU", false);
					intent.putExtra(RsaDbHelper.GROUP_ID, cId);
					intent.putExtra(RsaDbHelper.GROUP_NAME, cName.toUpperCase());
					intent.putExtra("isGroup", true);
					
					try {
						intent.putStringArrayListExtra("PR_TYPES", arrayPricetypes);
						
						intent.putExtra("ORDERH", mOrderH);		
						intent.putExtra("INDEX", 0);
						intent.putExtra("TOP", 0);
						intent.putExtra("GPOSITION", 0);
					} catch (Exception e) {}



					SharedPreferences def_prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

					boolean useModalCalc = def_prefs.getBoolean("useModalCalc", false);
					
					if (useModalCalc) {
						intent.setClass(getApplicationContext(), ModalGoodsActivity.class);
					} else {
						intent.setClass(getApplicationContext(), GoodsActivity.class);
					}
					
					startActivityForResult(intent, PICK_GOODS_REQUEST);
				}
			}
		});
		
		return true;
	}
	
	private boolean getDataFromDatabase(final LayoutInflater inflater) {
		Cursor cGroups = null;
		String qGroups = "select _group.ID, _group.NAME, _group.BRAND_ID, _brand.NAME, _group.PARENT_NAME from _group " +
						 "inner join _brand on _group.BRAND_ID = _brand.ID " +
						 "order by _group.PARENT_NAME, _brand.NAME, _group.NAME";
		int countGroups = 0;
		
		SharedPreferences prefs = getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);
		RsaDbHelper mDb = new RsaDbHelper(this, prefs.getString(RsaDb.ACTUALDBKEY, RsaDbHelper.DB_NAME1));
		SQLiteDatabase db = mDb.getReadableDatabase();
		
		cGroups = db.rawQuery(qGroups, null);
		countGroups = cGroups.getCount();
		
		if (countGroups>0) {
			String currentBrandName  = "s1s";
			String currentParentName = "s1s";

				NLevelItem parent = null;
				NLevelItem grandParent = null;
			
				while (cGroups.moveToNext()) {
					
					if (currentParentName.equals("s1s") && cGroups.getString(4)==null) {
						currentParentName = "КАТАЛОГ ПРОДУКЦИИ";
						grandParent = new NLevelItem(new SomeObject(currentParentName, "$$"), null, new NLevelView() {
							@Override
							public View getView(NLevelItem item) {
								View view = inflater.inflate(lightTheme?R.layout.custom_list_item:R.layout.custom_list_item_black, null);
								TextView tv = (TextView) view.findViewById(R.id.textView);
								LinearLayout ll = (LinearLayout) view.findViewById(R.id.linearLayout);
								ImageView iv = (ImageView) view.findViewById(R.id.imageView1);
								
								Drawable currentImageState = getResources().getDrawable(R.drawable.expander_ic_minimized);
								if (item.isExpanded())
									currentImageState = getResources().getDrawable(R.drawable.expander_ic_maximized);
								iv.setImageDrawable(currentImageState);
								
								
								ll.setPadding(getPXfromDP(6), 0, 0, 0);
								tv.setTypeface(null, Typeface.BOLD);
								String name = (String) ((SomeObject) item.getWrappedObject()).getName();
								tv.setText(name);
								return view;
							}
						});
						list.add(grandParent);
					}
					
					if (cGroups.getString(4)!=null && currentParentName.equals(cGroups.getString(4))==false ) {
						currentParentName = cGroups.getString(4);
						grandParent = new NLevelItem(new SomeObject(currentParentName, "$$"), null, new NLevelView() {
							@Override
							public View getView(NLevelItem item) {
								View view = inflater.inflate(lightTheme?R.layout.custom_list_item:R.layout.custom_list_item_black, null);
								TextView tv = (TextView) view.findViewById(R.id.textView);
								LinearLayout ll = (LinearLayout) view.findViewById(R.id.linearLayout);
								ImageView iv = (ImageView) view.findViewById(R.id.imageView1);
								
								Drawable currentImageState = getResources().getDrawable(R.drawable.expander_ic_minimized);
								if (item.isExpanded())
									currentImageState = getResources().getDrawable(R.drawable.expander_ic_maximized);
								iv.setImageDrawable(currentImageState);
								
								
								ll.setPadding(getPXfromDP(6), 0, 0, 0);
								tv.setTypeface(null, Typeface.BOLD);
								String name = (String) ((SomeObject) item.getWrappedObject()).getName();
								tv.setText(name);
								return view;
							}
						});
						list.add(grandParent);
					}
					
					if (currentBrandName.equals(cGroups.getString(3)) == false) { // if new brand detected						
						currentBrandName = cGroups.getString(3);				        
				        parent = new NLevelItem(new SomeObject(currentBrandName, "$$"), grandParent, new NLevelView() {
							@Override
						    public View getView(NLevelItem item) {
						    	View view = inflater.inflate(lightTheme?R.layout.custom_list_item:R.layout.custom_list_item_black, null);
						    	TextView tv = (TextView) view.findViewById(R.id.textView);
						    	LinearLayout ll = (LinearLayout) view.findViewById(R.id.linearLayout);
						    	ImageView iv = (ImageView) view.findViewById(R.id.imageView1);
						    	Drawable currentImageState = getResources().getDrawable(R.drawable.expander_ic_minimized);
								if (item.isExpanded())
									currentImageState = getResources().getDrawable(R.drawable.expander_ic_maximized);
								iv.setImageDrawable(currentImageState);
								
								ll.setPadding(getPXfromDP(18), 0, 0, 0);
						    	String name = (String) ((SomeObject) item.getWrappedObject()).getName();
						    	tv.setText(name);
						    	return view;
							}
						});
					    list.add(parent);	  
				        
				    	NLevelItem child = new NLevelItem(new SomeObject(cGroups.getString(1),cGroups.getString(0)),parent, new NLevelView() {      
					    	@Override
					    	public View getView(NLevelItem item) {
						       View view = inflater.inflate(lightTheme?R.layout.custom_list_item:R.layout.custom_list_item_black, null);
						       TextView tv = (TextView) view.findViewById(R.id.textView);
						       LinearLayout ll = (LinearLayout) view.findViewById(R.id.linearLayout);
						       ImageView iv = (ImageView) view.findViewById(R.id.imageView1);
						       iv.setVisibility(View.GONE);
						       ll.setPadding(getPXfromDP(6), 0, 0, 0);
						       String name = (String) ((SomeObject) item.getWrappedObject()).getName();
						       tv.setText(name);
						       
						       if (lightTheme) {
							       LinearLayout bg = (LinearLayout) view.findViewById(R.id.linearBack);
							       bg.setBackgroundColor(Color.parseColor("#ffffffff"));
						       }
						       return view;
					    	}
				    	});
				    	list.add(child);
					} else {
						NLevelItem child = new NLevelItem(new SomeObject(cGroups.getString(1),cGroups.getString(0)),parent, new NLevelView() {      
					    	@Override
					    	public View getView(NLevelItem item) {
						       View view = inflater.inflate(lightTheme?R.layout.custom_list_item:R.layout.custom_list_item_black, null);
						       TextView tv = (TextView) view.findViewById(R.id.textView);
						       LinearLayout ll = (LinearLayout) view.findViewById(R.id.linearLayout);
						       ImageView iv = (ImageView) view.findViewById(R.id.imageView1);
						       iv.setVisibility(View.GONE);
						       ll.setPadding(getPXfromDP(6), 0, 0, 0);
						       String name = (String) ((SomeObject) item.getWrappedObject()).getName();
						       tv.setText(name);
						       
						       if (lightTheme) {
							       LinearLayout bg = (LinearLayout) view.findViewById(R.id.linearBack);
							       bg.setBackgroundColor(Color.parseColor("#ffffffff"));
						       }
						       return view;
					    	}
				    	});
				    	list.add(child);
					}
				}
		} 
		
		if (cGroups!=null && !cGroups.isClosed()) {
			cGroups.close();
		}
		if (db!=null && db.isOpen()) {
			db.close();
		}
		
		return countGroups>0;
	}
	/**
     * Metohd that starts when another activity becomes on display
     */	
	@Override
	public void onStop() {
		super.onStop();
	}
	
	
    /**
     * Method that starts if system trying to destroy activity
     */	
	@Override
	protected void onDestroy() {
	   super.onDestroy();
	}
	
	@Override
	protected void onSaveInstanceState (Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putParcelable("ORDERH", mOrderH);
	}
	
	/**
	 * If Back-button on device pressed then do...
	 */
	@Override
	public void onBackPressed()
	{
		Intent intent = new Intent();
		Bundle b = new Bundle();
		b.putBoolean("ISFROMGROUP",true);
    	intent.putExtra("ORDERH", mOrderH);
    	intent.putExtras(b);
    	
    	setResult(RESULT_OK, intent);
    	
    	finish();
	}
	
	/** 
	 * When user select some goods and go to this activity, he gets data of current order
	 *  by this method
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		/** Data from previous activity (GoodsActivity.class) to extras variable */
		Bundle extras;
		
		// if arrived data from GoodsActivity then...
        if (requestCode == PICK_GOODS_REQUEST)  {
        	if (resultCode == RESULT_OK)
        	{
        		extras = data.getExtras();
                mOrderH = new OrderHead();
        		mOrderH = extras.getParcelable("ORDERH");
        		
        		iidx = extras.getInt("INDEX");
        		ttop = extras.getInt("TOP");
        		groupPos = extras.getInt("GPOSITION");
        	}
        }
        updateList();
    }
	
	private void loadPriceTypes(SQLiteDatabase __db, ArrayList<String> __arr, String __id) {
		Cursor __cur;
		String __q = "select PRICE from _char where CUST_ID='" + __id + "' group by PRICE";
		int __count = 0;
		__cur = __db.rawQuery(__q, null);
		__count = __cur.getCount();
		
		if (__count>0) {
			__cur.moveToFirst();
			for (int i=0; i<__count; i++) {
				__arr.add(__cur.getString(0));
				__cur.moveToNext();
			}
		}
		if (__cur != null) {
			__cur.close();
		}
	}
	
	class SomeObject {
		public String name;
		public String id;

		public SomeObject(String name, String id) {
			this.name = name;
			this.id = id;
		}
		
		public String getName() {
			return name;
		}
		public String getId() {
			return id;
		}
	}
	
	public int getPXfromDP(int padding_in_dp) {
	    final float scale = getResources().getDisplayMetrics().density;
	    return (int) (padding_in_dp * scale + 0.5f);
	}
	
}
