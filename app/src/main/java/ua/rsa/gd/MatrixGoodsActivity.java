package ua.rsa.gd;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import ua.rsa.gd.R;
import ua.rsa.gd.adapter.item.MatrixItem;


public class MatrixGoodsActivity extends ListActivity {
	SharedPreferences def_prefs;
	SharedPreferences prefs_name;
	SharedPreferences prefs_name_main;
	Bundle extras;
	boolean lightTheme;
	boolean restored;
	boolean isGroup;
	boolean isTOPSKU;
	
	private final static int IDD_COUNT = 0;
	
	private final static int ID_REST  = 100;
	private final static int ID_ORDER = 200;
	
	String curGroupID;
	String curGroupName;
	String curBrandID;
	String curBrandName;
	
	String curGoodsID;
	int curRecomendation;
	int lastSelectedPosition;
	
	int ttop;
	int iidx;
	int goods_iidx;
	int goods_ttop;
	
	EditText edtOrderedQty 	= null;
	EditText edtRestQty 	= null;
	
	TextView txtRecomended 	= null;
	TextView txtSum 		= null;
	TextView txtCode1C 		= null;
	TextView txtPrice 		= null;
	TextView txtWeight 		= null;
	TextView txtRest 		= null;
	TextView txtCustId 		= null;
	TextView txtShopId 		= null;
	TextView txtMatrix 		= null;
	TextView txtAVG 		= null;
	TextView txtCoef 		= null;
	TextView txtDelivery 	= null;
	TextView txtShare 		= null;
	TextView txtName 		= null;
	TextView txtVPerc		= null;

	TextView txtCustomField1= null;
	
	TextView txtDate1		= null;
	TextView txtRest1		= null;
	TextView txtReturn1		= null;
	TextView txtOrder1		= null;
	TextView txtDate2		= null;
	TextView txtRest2		= null;
	TextView txtReturn2		= null;
	TextView txtOrder2		= null;
	TextView txtDate3		= null;
	TextView txtRest3		= null;
	TextView txtReturn3		= null;
	TextView txtOrder3		= null;
	TextView txtDate4		= null;
	TextView txtRest4		= null;
	TextView txtReturn4		= null;
	TextView txtOrder4		= null;
	TextView txtDate5		= null;
	TextView txtRest5		= null;
	TextView txtReturn5		= null;
	TextView txtOrder5		= null;
	TextView txtDate6		= null;
	TextView txtRest6		= null;
	TextView txtReturn6		= null;
	TextView txtOrder6		= null;
	TextView txtDate7		= null;
	TextView txtRest7		= null;
	TextView txtReturn7		= null;
	TextView txtOrder7		= null;
	TextView txtDate8		= null;
	TextView txtRest8		= null;
	TextView txtReturn8		= null;
	TextView txtOrder8		= null;
	TextView txtDate9		= null;
	TextView txtRest9		= null;
	TextView txtReturn9		= null;
	TextView txtOrder9		= null;
	
	ArrayList<MatrixItem> list;
	SimpleAdapter mAdapterList;	
	ListView lv;
	OrderHead mOrderH;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		lightTheme = getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE).getBoolean(RsaDb.LIGHTTHEMEKEY, false);
		lastSelectedPosition = 0;
		ttop = 0;
		iidx = 0;
		goods_iidx = 0;
		goods_ttop = 0;
		
		if (lightTheme) {
			setTheme(R.style.Theme_Custom);
			super.onCreate(savedInstanceState);
			setContentView(R.layout.l_goods);
		} else {
			setTheme(R.style.Theme_CustomBlack2);
			super.onCreate(savedInstanceState);
			setContentView(R.layout.goods);
		}
		
		def_prefs		= PreferenceManager.getDefaultSharedPreferences(this);
		prefs_name		= getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);
		prefs_name_main = getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE);
		
		if (savedInstanceState != null) {
        	extras	= savedInstanceState;
        	goods_ttop = extras.getInt("GTOP");
        	goods_iidx = extras.getInt("GINDEX");
        	restored = true;
        } else {
        	extras = getIntent().getExtras();
        	restored = false;
        }
		
		mOrderH	 = extras.getParcelable("ORDERH");
		isGroup  = extras.getBoolean("isGroup");
		isTOPSKU = extras.getBoolean("isTOPSKU");
		
		curGoodsID = extras.getString("curGoods");
		curRecomendation = extras.getInt("curRecomend");
		lastSelectedPosition = extras.getInt("lastSelected");
		
		ttop = extras.getInt("TOP");
    	iidx = extras.getInt("INDEX");
		
		if (isGroup == true) {
			// take group id from selected group
			curGroupID = extras.getString(RsaDbHelper.GROUP_ID);
			curGroupName = extras.getString(RsaDbHelper.GROUP_NAME);
		} else {
			// take brand id from selected brand
			curBrandID = extras.getString(RsaDbHelper.BRAND_ID);
			curBrandName = extras.getString(RsaDbHelper.BRAND_NAME);
		}
	}
	
	@Override
	public void onStart() {
		super.onStart();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		try {
			updateAll();
		} catch (Exception sEx) {sEx.printStackTrace();}
	}
	
	@Override
	public void onPause() {
		super.onPause();
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putParcelable("ORDERH", mOrderH);
		savedInstanceState.putString(RsaDbHelper.GROUP_ID, curGroupID);
		savedInstanceState.putString(RsaDbHelper.GROUP_NAME, curGroupName);
		savedInstanceState.putString(RsaDbHelper.BRAND_ID, curBrandID);
		savedInstanceState.putString(RsaDbHelper.BRAND_NAME, curBrandName);
		savedInstanceState.putBoolean("isGroup", isGroup);
		savedInstanceState.putBoolean("isTOPSKU", isTOPSKU);
		
		savedInstanceState.putString("curGoods", curGoodsID);
		savedInstanceState.putInt("curRecomend", curRecomendation);
		savedInstanceState.putInt("lastSelected", lastSelectedPosition);
		
		savedInstanceState.putInt("TOP", ttop);
		savedInstanceState.putInt("INDEX", iidx);
	}
	
	private void updateAll() {
		Button btnBack = (Button)findViewById(R.id.goods_pbtn_prev);
		btnBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
		    	intent.putExtra("ORDERH", mOrderH);
		    	intent.putExtra("TOP", ttop);
		    	intent.putExtra("INDEX", iidx);
		    	setResult(RESULT_OK, intent);
		    	finish();
			}				
		});
		
		updateTop();
		updateList();
	}
	
	private void updateTop() {
		TextView txtTopText = (TextView)findViewById(R.id.goods_txtGroup);
		txtTopText.setText(isGroup?curGroupName:curBrandName);
		if (isTOPSKU) 
			txtTopText.setText("TOP SKU");
		TextView txtTopSum = (TextView)findViewById(R.id.goods_txtTotalSum_text);
		float hsumo = 0;
		for (OrderLines CurLine : mOrderH.lines)
			hsumo += Float.parseFloat(CurLine.get(OrderLines.SUMWNDS));
		txtTopSum.setText(String.format("%.2f",hsumo).replace(',', '.') + " $");
	}
	
	private static String getParam(Cursor c, int i){
		return c.isNull(i)?"0":c.getString(i);
	}
	
	private void updateList() {
		String stmtWhere = "";
		/*
		if (isTOPSKU) {
			stmtWhere = "where _goods.FLASH = '1'";
		} else if (isGroup) {
			stmtWhere = "where _goods.GROUP_ID = '" + curGroupID + "'";
		} else {
			stmtWhere = "where _goods.BRAND_ID = '" + curBrandID + "'";
		}*/
		String query = "select _goods.ID, _goods.NAME, _goods.UN, _goods.COEFF, _goods.NDS, _goods.QTY, " +
							  "_goods.REST, _goods.WEIGHT1, _goods.PRICE1, _matrix.MATRIX, _matrix.AVG, " +
							  "_matrix.COEF, _matrix.DELIVERY, _matrix.SHARE, _matrix.VPERCENT, " +
							  "_matrix.DATE1, _matrix.REST1, _matrix.RETURN1, _matrix.ORDER1, " +
							  "_matrix.DATE2, _matrix.REST2, _matrix.RETURN2, _matrix.ORDER2, " +
							  "_matrix.DATE3, _matrix.REST3, _matrix.RETURN3, _matrix.ORDER3, " +
							  "_matrix.DATE4, _matrix.REST4, _matrix.RETURN4, _matrix.ORDER4, " +
							  "_matrix.DATE5, _matrix.REST5, _matrix.RETURN5, _matrix.ORDER5, " +
							  "_matrix.DATE6, _matrix.REST6, _matrix.RETURN6, _matrix.ORDER6, " +
							  "_matrix.DATE7, _matrix.REST7, _matrix.RETURN7, _matrix.ORDER7, " +
							  "_matrix.DATE8, _matrix.REST8, _matrix.RETURN8, _matrix.ORDER8, " +
							  "_matrix.DATE9, _matrix.REST9, _matrix.RETURN9, _matrix.ORDER9, _matrix.CUSTOM1 " +
					   "from _goods " +
					         "left outer join _matrix " +
					         "on _matrix.CUST_ID = '" + mOrderH.cust_id + "' " +
					         "and _matrix.SHOP_ID = '" + mOrderH.shop_id + "' " +
					         "and _goods.ID = _matrix.GOODS_ID " +
					         "order by _goods.BRAND_ID, _goods.NAME " +
					   stmtWhere;
		
		RsaDbHelper 		mDb		= new RsaDbHelper(this, prefs_name.getString(RsaDb.ACTUALDBKEY, RsaDbHelper.DB_NAME1));
		SQLiteDatabase 		db		= mDb.getReadableDatabase();
		Cursor				cursor	= db.rawQuery(query, null);
		
		list = new ArrayList<MatrixItem>();
		if (cursor.getCount()>0) {
			cursor.moveToFirst();
			for (int i=0;i<cursor.getCount();i++) {
				float flPrice = 0;
				float flVAT = 0;
				try {
					flPrice= Float.parseFloat(getParam(cursor, 8));
					flVAT= Float.parseFloat(getParam(cursor, 4));
					flPrice+= flPrice*flVAT;
				} catch (Exception e) {}
				list.add(new MatrixItem(getParam(cursor, 0), getParam(cursor, 1), getParam(cursor, 2), getParam(cursor, 3),
										getParam(cursor, 4), getParam(cursor, 5), getParam(cursor, 6), getParam(cursor, 7), 
										String.format("%.2f", flPrice).replace(',', '.'), 
										getParam(cursor, 9), getParam(cursor, 10), getParam(cursor, 11), 
										getParam(cursor, 12), getParam(cursor, 13),
										getParam(cursor, 14),
										getParam(cursor, 15),
										getParam(cursor, 16),
										getParam(cursor, 17),
										getParam(cursor, 18),
										getParam(cursor, 19),
										getParam(cursor, 20),
										getParam(cursor, 21),
										getParam(cursor, 22),
										getParam(cursor, 23),
										getParam(cursor, 24),
										getParam(cursor, 25),
										getParam(cursor, 26), 
										
										getParam(cursor, 27),
										getParam(cursor, 28),
										getParam(cursor, 29),
										getParam(cursor, 30),
										getParam(cursor, 31),
										getParam(cursor, 32),
										getParam(cursor, 33),
										getParam(cursor, 34),
										getParam(cursor, 35),
										getParam(cursor, 36),
										getParam(cursor, 37),
										getParam(cursor, 38),
										getParam(cursor, 39),
										getParam(cursor, 40),
										getParam(cursor, 41),
										getParam(cursor, 42),
										getParam(cursor, 43),
										getParam(cursor, 44),
										getParam(cursor, 45),
										getParam(cursor, 46),
										getParam(cursor, 47),
										getParam(cursor, 48),
										getParam(cursor, 49),
										getParam(cursor, 50),
										"brand_mtrx",
										getParam(cursor, 51)));
				cursor.moveToNext();
			}
		}
		if (cursor!=null && !cursor.isClosed()) {
			cursor.close();
		}
		if (db!=null && db.isOpen()) {
			db.close();
		}
		mAdapterList = new SimpleAdapter(	this, list, lightTheme?R.layout.l_list_goods_matrix:R.layout.list_goods_matrix, 
											new String[] {MatrixItem.MATRIX, MatrixItem.ID, MatrixItem.NAME, MatrixItem.UN, MatrixItem.COEFF,
														  MatrixItem.NDS, MatrixItem.QTY, MatrixItem.REST, MatrixItem.WEIGHT1,
														  MatrixItem.PRICE1, MatrixItem.AVG, MatrixItem.COEF,
														  MatrixItem.DELIVERY, MatrixItem.SHARE}, 
											new int[] {R.id.txt_rec_goods, R.id.txtOrderedCount_goods,R.id.txtName_goods,R.id.txtUn_goods,0,0,0,R.id.txtRest_goods,
														R.id.txtWeight_goods,R.id.txtPrice_goods,0,0,0,R.id.txt_rest_goods});
		SimpleAdapter.ViewBinder viewBinder = new SimpleAdapter.ViewBinder() {
			@Override
			public boolean setViewValue(View view, Object data,
					String textRepresentation) {
				switch (view.getId()) {
				case R.id.txtOrderedCount_goods:
					TextView txtView = (TextView) view;
                    View prnt = (View)(view.getParent().getParent());
                    myInt mi = new myInt();
                    String qt = findOrderedGoods(textRepresentation, mi);
                    txtView.setText(qt);
                    prnt.setBackgroundColor(mi.get());
					return true;
				case R.id.txt_rec_goods:
					View top = (View)(view.getParent().getParent().getParent());
					View relat = ((ViewGroup)top).getChildAt(0);
					TextView txtName = (TextView)((ViewGroup)relat).getChildAt(1);
					myDraw draw = new myDraw();
					draw.calc(textRepresentation);
					//txtName.setTextColor(draw.get());
					txtName.setTextAppearance(getApplicationContext(), draw.getAppearance());
					return true;
				case R.id.txt_rest_goods:
					TextView txtRest = (TextView)view;
					View parent = (View)view.getParent();
					View v1 = ((ViewGroup)parent).getChildAt(0);
					View v2 = ((ViewGroup)parent).getChildAt(1);
					myCrit crit = new myCrit();
					crit.calc(textRepresentation);
					v1.setVisibility(View.GONE);
					v2.setVisibility(View.GONE);
					txtRest.setVisibility(crit.get());
					parent.setVisibility(crit.get());
					txtRest.setText("КРИТ!");
					return true;
				}
				return false;
			}
		};
		
		mAdapterList.setViewBinder(viewBinder);
		
		lv = getListView();
		lv.setAdapter(mAdapterList);
		lv.setSelectionFromTop(goods_iidx, goods_ttop);
	}
	
	private class myInt {
		int color;
		public myInt() {
			color = Color.TRANSPARENT;
		}
		public int get() {
			return color;
		}
		public void set(int c) {
			color = c;
		}
	}
	
	private class myDraw {
		int color;
		int appear;
		public myDraw() {
			color = lightTheme?Color.BLACK:Color.GRAY;
			appear = lightTheme?R.style.matrixFontLight:R.style.matrixFont;
		}
		public int get() { return color; }
		public int getAppearance() { return appear; }
		public void calc(String s) {
			if (s.equals("1")) {
				set(Color.parseColor(lightTheme?"#ffbe00ff":"#ffe89ffa"));
				setAppear(lightTheme?R.style.matrixBoldFontLight:R.style.matrixBoldFont);
			}
		}
		public void set(int c) { color = c; }
		public void setAppear(int a) { appear = a; }
	}
	
	private class myCrit {
		int visibility;
		public myCrit() {
			visibility = View.GONE;
		}
		public int get() { return visibility; }
		public void calc(String s) {
			if (s!=null && s.equals("-1")) {
				set(View.VISIBLE);
			}
		}
		public void set(int v) { visibility = v; }
	}
	
	private String findOrderedGoods(String goodsId, myInt ii) {
		if ((goodsId==null) || (goodsId.equals(""))) 
			return "";
		for (OrderLines CurLine : mOrderH.lines) {
			if (CurLine.get(OrderLines.GOODS_ID).equals(goodsId)) {
				ii.set(Color.parseColor(lightTheme?"#3000FF00":"#3000FF00"));
				return CurLine.get(OrderLines.QTY); 
			}
		}
		return "";
	}
	
	
	@Override
	public void onBackPressed() {
		Intent intent = new Intent();
    	intent.putExtra("ORDERH", mOrderH);
    	intent.putExtra("TOP", ttop);
    	intent.putExtra("INDEX", iidx);
    	setResult(RESULT_OK, intent);
    	finish();
	}
	
	public void onListItemClick(ListView parent, View v, int position, long id) {			
		lastSelectedPosition = position;
		
		try {
			ListView lv = this.getListView();
			goods_iidx = lv.getFirstVisiblePosition(); 
			View mv = lv.getChildAt(0);
			goods_ttop = (mv == null) ? 0 : mv.getTop();
		} catch(Exception e) {
			Toast.makeText(getApplicationContext(), "1. Ошибка сохранения позиции", Toast.LENGTH_LONG).show();
		}
    	
		showDialog(IDD_COUNT);
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch(id) {	
			case IDD_COUNT: {
				View layout = null;
				AlertDialog.Builder builder = null;
				try {
					LayoutInflater inflater = getLayoutInflater();
					layout = inflater.inflate(lightTheme?R.layout.l_dlg_matrix:R.layout.dlg_matrix, 
							(ViewGroup)findViewById(lightTheme?R.layout.l_dlg_matrix:R.id.dlg_matrix));
					builder = new AlertDialog.Builder(this);
					builder.setView(layout);
				} catch(Exception e) {
					Toast.makeText(getApplicationContext(), "2. Ошибка. Создание диалога", Toast.LENGTH_LONG).show();
				}
				
				try {
					edtOrderedQty	= (EditText)layout.findViewById(R.id.matrixEditOrd);
					edtRestQty		= (EditText)layout.findViewById(R.id.matrixEditRest);
				} catch (Exception e) {
					Toast.makeText(getApplicationContext(), "3. Ошибка поиска лайаутов", Toast.LENGTH_LONG).show();
				}
				
				edtOrderedQty.setOnTouchListener(new OnTouchListener() {
					@Override
					public boolean onTouch(View v, MotionEvent event) {
						edtOrderedQty.requestFocus();
						edtOrderedQty.selectAll();
						InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					    imm.showSoftInput(edtOrderedQty, InputMethodManager.SHOW_FORCED);
						return true;
					}
				});
				edtRestQty.setOnTouchListener(new OnTouchListener() {
					@Override
					public boolean onTouch(View v, MotionEvent event) {
						edtRestQty.requestFocus();
						edtRestQty.selectAll();
						InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					    imm.showSoftInput(edtRestQty, InputMethodManager.SHOW_FORCED);
						return true;
					}
				});
				
				builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						final MatrixItem item = list.get(lastSelectedPosition);
						
						int idx_line = mOrderH.isInLines(curGoodsID);
						int idx_rest = mOrderH.isInRests(curGoodsID);
						
						float pr = Float.parseFloat(item.get(MatrixItem.PRICE1));
						float nds = 0;//Float.parseFloat(item.get(MatrixItem.NDS));
						float qty = Float.parseFloat(edtOrderedQty.getText().toString());
						float sum = pr*qty;
						float tpr = pr+ pr*nds;
						float hsumo = sum+sum*nds;
						
						if (idx_line>=0) { // Уже есть, надо просто отредактировать
							OrderLines line = mOrderH.lines.get(idx_line);
							line.put(OrderLines.QTY, 	 edtOrderedQty.getText().toString());
							line.put(OrderLines.SUMWNDS, String.format("%.2f",hsumo).replace(',', '.'));
							line.put(OrderLines.SUMWONDS, String.format("%.2f",sum).replace(',', '.'));
						} else { // Не заказано надо добавить
							mOrderH.lines.add(new OrderLines(	"0", 											// *id used for REST QTY in Shop (Merchandising)
																"",												// *zakaz_id used in headactivity
																curGoodsID,										// goods_id
																item.get(MatrixItem.NAME),						// text_goods
																"PRICE1",	    								// PRICETYPE (Price1,Price2...20)
																edtOrderedQty.getText().toString(),				// qty
																item.get(MatrixItem.UN),						// un
																item.get(MatrixItem.COEFF),						// coeff
																"0",											// *discount !not used
																String.format("%.2f",tpr).replace(',', '.'),	// pricewnds 
																String.format("%.2f",hsumo).replace(',', '.'),	// sumwnds
																String.format("%.2f",pr).replace(',', '.'), 	// pricewonds=pricewnds/1.2	
																String.format("%.2f",sum).replace(',', '.'),	// sumwonds=sumwnds/1.2
																String.format("%.2f",hsumo-sum).replace(',', '.'),	// nds=sumwnds-sumwonds
																item.get(MatrixItem.NDS),						// must be delay, but overloaded to NDS flag
																item.get(MatrixItem.WEIGHT1),					//weight
																"", 											// comment
																item.get(MatrixItem.BRAND_NAME)));				// brand
						}
						
						if (idx_rest>=0) { // Уже есть, надо просто отредактировать
							OrderRests restline = mOrderH.restslines.get(idx_rest);
							restline.put(OrderRests.RECQTY, Integer.toString(curRecomendation));
							restline.put(OrderRests.RESTQTY, edtRestQty.getText().toString());
							restline.put(OrderRests.QTY, edtOrderedQty.getText().toString());
						} else { // Не заказано надо добавить
							mOrderH.restslines.add(new OrderRests(	"0", 											// *id not used
																	"",												// *zakaz_id used in headactivity
																	curGoodsID,										// goods_id
																	edtRestQty.getText().toString(),				// restqty
																	Integer.toString(curRecomendation),				// recqty
																	edtOrderedQty.getText().toString()));			// qty
						}
						updateList();
						updateTop();
						InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				        imm.hideSoftInputFromWindow(edtOrderedQty.getWindowToken(), 0);
					}
				});
				builder.setNegativeButton(R.string.goods_cancel, new DialogInterface.OnClickListener() {					
					@Override
					public void onClick(DialogInterface dialog, int which) {
				        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				        imm.hideSoftInputFromWindow(edtOrderedQty.getWindowToken(), 0);
					}
				});
				
				builder.setNeutralButton(R.string.goods_delete, new DialogInterface.OnClickListener() {					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						int idx_line = mOrderH.isInLines(curGoodsID);
						int idx_rest = mOrderH.isInRests(curGoodsID);
						
						if (idx_line>=0) mOrderH.lines.remove(idx_line);
						if (idx_rest>=0) mOrderH.restslines.remove(idx_rest);
						
						updateList();
						updateTop();
				        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				        imm.hideSoftInputFromWindow(edtOrderedQty.getWindowToken(), 0);
					}
				});
				
				builder.setCancelable(false);
				
				Dialog md = builder.create();
				
				try {
					md.requestWindowFeature(Window.FEATURE_NO_TITLE);
				} catch(Exception e) {
					Toast.makeText(getApplicationContext(), "4. Ошибка убрать заголовок", Toast.LENGTH_LONG).show();
				}
				return md;
			}
			default:
				return null;
		}
	}
	
	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		MatrixItem item = null;
		try {
			item = list.get(lastSelectedPosition);
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), "5. Ошибка перехода", Toast.LENGTH_LONG).show();
			return;
		}
		
		try {
			curGoodsID = item.get(MatrixItem.ID);
		} catch(Exception e) {
			Toast.makeText(getApplicationContext(), "6. pos="+Integer.toString(lastSelectedPosition),Toast.LENGTH_LONG).show();
		}
		
		switch(id) {	
		case IDD_COUNT: {
			edtOrderedQty	= (EditText)dialog.findViewById(R.id.matrixEditOrd);
			edtRestQty		= (EditText)dialog.findViewById(R.id.matrixEditRest);
			txtRecomended	= (TextView)dialog.findViewById(R.id.matrixRecomended);
			txtSum			= (TextView)dialog.findViewById(R.id.matrixSum);
			txtCode1C		= (TextView)dialog.findViewById(R.id.matrix1C);
			txtPrice		= (TextView)dialog.findViewById(R.id.matrixPrice);
			txtWeight		= (TextView)dialog.findViewById(R.id.matrixWeight);
			txtRest			= (TextView)dialog.findViewById(R.id.matrixRest);
			txtCustId		= (TextView)dialog.findViewById(R.id.matrixCust);
			txtShopId		= (TextView)dialog.findViewById(R.id.matrixShop);
			txtMatrix		= (TextView)dialog.findViewById(R.id.matrixM);
			txtAVG			= (TextView)dialog.findViewById(R.id.matrixA);
			txtCoef			= (TextView)dialog.findViewById(R.id.matrixC);
			txtDelivery		= (TextView)dialog.findViewById(R.id.matrixD);
			txtShare		= (TextView)dialog.findViewById(R.id.matrixS);
			txtName			= (TextView)dialog.findViewById(R.id.matrixName);
			txtVPerc		= (TextView)dialog.findViewById(R.id.matrixVproc);

			txtCustomField1= (TextView)dialog.findViewById(R.id.matrixCustomField1);
			
			txtDate1		= (TextView)dialog.findViewById(R.id.matrix_date1);
			txtRest1		= (TextView)dialog.findViewById(R.id.matrix_rest1);
			txtReturn1		= (TextView)dialog.findViewById(R.id.matrix_return1);
			txtOrder1		= (TextView)dialog.findViewById(R.id.matrix_order1);
			txtDate2		= (TextView)dialog.findViewById(R.id.matrix_date2);
			txtRest2		= (TextView)dialog.findViewById(R.id.matrix_rest2);
			txtReturn2		= (TextView)dialog.findViewById(R.id.matrix_return2);
			txtOrder2		= (TextView)dialog.findViewById(R.id.matrix_order2);
			txtDate3		= (TextView)dialog.findViewById(R.id.matrix_date3);
			txtRest3		= (TextView)dialog.findViewById(R.id.matrix_rest3);
			txtReturn3		= (TextView)dialog.findViewById(R.id.matrix_return3);
			txtOrder3		= (TextView)dialog.findViewById(R.id.matrix_order3);
			txtDate4		= (TextView)dialog.findViewById(R.id.matrix_date4);
			txtRest4		= (TextView)dialog.findViewById(R.id.matrix_rest4);
			txtReturn4		= (TextView)dialog.findViewById(R.id.matrix_return4);
			txtOrder4		= (TextView)dialog.findViewById(R.id.matrix_order4);
			txtDate5		= (TextView)dialog.findViewById(R.id.matrix_date5);
			txtRest5		= (TextView)dialog.findViewById(R.id.matrix_rest5);
			txtReturn5		= (TextView)dialog.findViewById(R.id.matrix_return5);
			txtOrder5		= (TextView)dialog.findViewById(R.id.matrix_order5);
			txtDate6		= (TextView)dialog.findViewById(R.id.matrix_date6);
			txtRest6		= (TextView)dialog.findViewById(R.id.matrix_rest6);
			txtReturn6		= (TextView)dialog.findViewById(R.id.matrix_return6);
			txtOrder6		= (TextView)dialog.findViewById(R.id.matrix_order6);
			txtDate7		= (TextView)dialog.findViewById(R.id.matrix_date7);
			txtRest7		= (TextView)dialog.findViewById(R.id.matrix_rest7);
			txtReturn7		= (TextView)dialog.findViewById(R.id.matrix_return7);
			txtOrder7		= (TextView)dialog.findViewById(R.id.matrix_order7);
			txtDate8		= (TextView)dialog.findViewById(R.id.matrix_date8);
			txtRest8		= (TextView)dialog.findViewById(R.id.matrix_rest8);
			txtReturn8		= (TextView)dialog.findViewById(R.id.matrix_return8);
			txtOrder8		= (TextView)dialog.findViewById(R.id.matrix_order8);
			txtDate9		= (TextView)dialog.findViewById(R.id.matrix_date9);
			txtRest9		= (TextView)dialog.findViewById(R.id.matrix_rest9);
			txtReturn9		= (TextView)dialog.findViewById(R.id.matrix_return9);
			txtOrder9		= (TextView)dialog.findViewById(R.id.matrix_order9);
			
			dialog.setOnShowListener(new OnShowListener() {
				@Override
				public void onShow(DialogInterface dialog) {
					edtRestQty.requestFocus();
					edtRestQty.selectAll();
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				    imm.showSoftInput(edtRestQty, InputMethodManager.SHOW_FORCED);
				}
			});
			
			try {
				txtCustId.setText("K: " + mOrderH.cust_id);
				txtShopId.setText("Sh: " + mOrderH.shop_id);
				txtMatrix.setText("M: " + item.get(MatrixItem.MATRIX));
				txtAVG.setText("A: " + item.get(MatrixItem.AVG));
				txtCoef.setText("C: " + item.get(MatrixItem.COEF));
				txtDelivery.setText("D: " + item.get(MatrixItem.DELIVERY));
				txtShare.setText("S: " + item.get(MatrixItem.SHARE));
				txtRest.setText("Остаток: " + item.get(MatrixItem.REST));
				txtWeight.setText("Вес: " + item.get(MatrixItem.WEIGHT1));
				txtPrice.setText("Цена: " + item.get(MatrixItem.PRICE1));
				txtCode1C.setText("Kод 1С: " + item.get(MatrixItem.ID));
				txtName.setText(item.get(MatrixItem.NAME));
				edtRestQty.setText("0");
				edtOrderedQty.setText("0");
			} catch(Exception e) {
				Toast.makeText(getApplicationContext(), "7. Ошибка заполения", Toast.LENGTH_LONG).show();
			}
			try {
				txtVPerc.setText("Процент возврата: "+item.get(MatrixItem.VPERCENT)+"%");

				if (!TextUtils.isEmpty(item.get(MatrixItem.CUSTOM1))) {
					txtCustomField1.setVisibility(View.VISIBLE);
					txtCustomField1.setText(item.get(MatrixItem.CUSTOM1));
				} else {
					txtCustomField1.setVisibility(View.GONE);
				}
			} catch(Exception e) {
				Toast.makeText(getApplicationContext(), "Процент возврата", Toast.LENGTH_LONG).show();
			}
			try {	
				txtDate1.setText(item.get(MatrixItem.DATE1));
				txtRest1.setText("ОСТ: " + item.get(MatrixItem.REST1));
				txtReturn1.setText("ВЗРТ: " + item.get(MatrixItem.RETURN1));
				txtOrder1.setText("ЗАКАЗ: " + item.get(MatrixItem.ORDER1));
				txtDate2.setText(item.get(MatrixItem.DATE2));
				txtRest2.setText("ОСТ: " + item.get(MatrixItem.REST2));
				txtReturn2.setText("ВЗРТ: " + item.get(MatrixItem.RETURN2));
				txtOrder2.setText("ЗАКАЗ: " + item.get(MatrixItem.ORDER2));
				txtDate3.setText(item.get(MatrixItem.DATE3));
				txtRest3.setText("ОСТ: " + item.get(MatrixItem.REST3));
				txtReturn3.setText("ВЗРТ: " + item.get(MatrixItem.RETURN3));
				txtOrder3.setText("ЗАКАЗ: " + item.get(MatrixItem.ORDER3));
				txtDate4.setText(item.get(MatrixItem.DATE4));
				txtRest4.setText("ОСТ: " + item.get(MatrixItem.REST4));
				txtReturn4.setText("ВЗРТ: " + item.get(MatrixItem.RETURN4));
				txtOrder4.setText("ЗАКАЗ: " + item.get(MatrixItem.ORDER4));
				txtDate5.setText(item.get(MatrixItem.DATE5));
				txtRest5.setText("ОСТ: " + item.get(MatrixItem.REST5));
				txtReturn5.setText("ВЗРТ: " + item.get(MatrixItem.RETURN5));
				txtOrder5.setText("ЗАКАЗ: " + item.get(MatrixItem.ORDER5));
				txtDate6.setText(item.get(MatrixItem.DATE6));
				txtRest6.setText("ОСТ: " + item.get(MatrixItem.REST6));
				txtReturn6.setText("ВЗРТ: " + item.get(MatrixItem.RETURN6));
				txtOrder6.setText("ЗАКАЗ: " + item.get(MatrixItem.ORDER6));
				txtDate7.setText(item.get(MatrixItem.DATE7));
				txtRest7.setText("ОСТ: " + item.get(MatrixItem.REST7));
				txtReturn7.setText("ВЗРТ: " + item.get(MatrixItem.RETURN7));
				txtOrder7.setText("ЗАКАЗ: " + item.get(MatrixItem.ORDER7));
				txtDate8.setText(item.get(MatrixItem.DATE8));
				txtRest8.setText("ОСТ: " + item.get(MatrixItem.REST8));
				txtReturn8.setText("ВЗРТ: " + item.get(MatrixItem.RETURN8));
				txtOrder8.setText("ЗАКАЗ: " + item.get(MatrixItem.ORDER8));
				txtDate9.setText(item.get(MatrixItem.DATE9));
				txtRest9.setText("ОСТ: " + item.get(MatrixItem.REST9));
				txtReturn9.setText("ВЗРТ: " + item.get(MatrixItem.RETURN9));
				txtOrder9.setText("ЗАКАЗ: " + item.get(MatrixItem.ORDER9));
				
			} catch(Exception e) {
				Toast.makeText(getApplicationContext(), "ЖЖЖЖ", Toast.LENGTH_LONG).show();
			}
			
			try {
				edtRestQty.setText(inOrderAlreadyRest(edtRestQty.getText().toString(),mOrderH.restslines, item.get(MatrixItem.ID)));
				edtOrderedQty.setText(inOrderAlreadyOrder(edtOrderedQty.getText().toString(),mOrderH.lines, item.get(MatrixItem.ID)));
			} catch (Exception e) {
				Toast.makeText(getApplicationContext(), "8. Ошибка inOrderAlready...", Toast.LENGTH_LONG).show();
			}
			
			int rec_count = 0;
			try {
				rec_count = calculateRecomend(	mOrderH.delivery.toString(), 
												item.get(MatrixItem.DELIVERY),
												item.get(MatrixItem.AVG),
												item.get(MatrixItem.COEF),
												"1",//item.get(MatrixItem.SHARE),
												edtRestQty.getText().toString());
			} catch (Exception e) {
				Toast.makeText(getApplicationContext(), "9. Ошибка расчета рекомендов...", Toast.LENGTH_LONG).show();
			}
			curRecomendation = rec_count;
			
			try {
				txtRecomended.setText("Рекомендованный заказ: \n" + Integer.toString(rec_count) + " ед.");
				txtSum.setText(calculateSum(item.get(MatrixItem.PRICE1), 
								item.get(MatrixItem.NDS), 
								edtOrderedQty.getText().toString()));
			} catch (Exception e) {
				Toast.makeText(getApplicationContext(), "10. Ошибка преобразования", Toast.LENGTH_LONG).show();
			}
			
			final MatrixItem mItem = item;
			edtOrderedQty.addTextChangedListener(new TextWatcher(){
				@Override
				public void afterTextChanged(Editable arg0) {
					if (edtOrderedQty.getText().toString().isEmpty()) {
						edtOrderedQty.setText("0");
						edtOrderedQty.selectAll();
						txtSum.setText("Сумма: 0.00 грн.");
					}
					if (mItem.get(MatrixItem.SHARE)!=null 
							&& mItem.get(MatrixItem.SHARE).equals("-1") 
							&& !edtOrderedQty.getText().toString().equals("1")
							&& !edtOrderedQty.getText().toString().equals("0")
																				){
						// edtOrderedQty.setText("1");
						// edtOrderedQty.selectAll();
						
						int curOrder = curRecomendation;
						int df = 0;
						try {
							curOrder = Integer.parseInt(edtOrderedQty.getText().toString());
							df = Math.abs(curRecomendation-curOrder);
						} catch (Exception e) { }
						if (df>3) {
							Toast.makeText(getApplicationContext(), "Критический процент возврата!", Toast.LENGTH_SHORT).show();
						}
					}
					txtSum.setText(calculateSum(mItem.get(MatrixItem.PRICE1), 
									mItem.get(MatrixItem.NDS), 
									edtOrderedQty.getText().toString()));
				}
				@Override
				public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) { }
				@Override
				public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) { }
			});
			
			edtRestQty.addTextChangedListener(new TextWatcher(){
				@Override
				public void afterTextChanged(Editable arg0) {
					if (edtRestQty.getText().toString().isEmpty()) {
						edtRestQty.setText("0");
						edtRestQty.selectAll();
					}
					int res = calculateRecomend(	mOrderH.delivery.toString(), 
													mItem.get(MatrixItem.DELIVERY),
													mItem.get(MatrixItem.AVG),
													mItem.get(MatrixItem.COEF),
													"1",//mItem.get(MatrixItem.SHARE),
													edtRestQty.getText().toString());
					curRecomendation = res;
					txtRecomended.setText("Рекомендованный заказ: " + Integer.toString(res) + " ед.");
				}
				@Override
				public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) { }
				@Override
				public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) { }
			});
		}
		default:
		}
	}
	
	private static String calculateSum(String p, String n, String q) {
		float price = Float.parseFloat(p);
		float nds	= 0;//Float.parseFloat(n);
		float qty	= Float.parseFloat(q);
		float hsumo = (price+(price*nds))*qty;
		
		return "Сумма: "+String.format("%.2f",hsumo)+" грн.";
	}
	
	private static int calculateRecomend(String strCurDelivery, String strNextDelivery, String AVG, 
		String COEF, String SHARE, String REST) {
		if (SHARE!=null && SHARE.equals("-1"))
			return 1;
		SimpleDateFormat fmt  = new SimpleDateFormat("yyyy-MM-dd");
		Calendar calNext = Calendar.getInstance();
		Calendar calCurrent = Calendar.getInstance();
		try {
			calNext.setTime(fmt.parse(strNextDelivery));
			calCurrent.setTime(fmt.parse(strCurDelivery));
		} catch (Exception e) {
			Log.d("RRR","DATE ERRRRORRRRR!!!!!");
			return 1;
		}
		
		float diffTime = calNext.getTimeInMillis() - calCurrent.getTimeInMillis();
		int srok_otgruzki = (int)(diffTime / 1000 / 60 / 60 / 24);
		if (srok_otgruzki==0) srok_otgruzki = 1; 
		float srednednev = 0;
		if (AVG!=null) srednednev = Float.parseFloat(AVG);
		else srednednev = 0;
		float bazoviy = srednednev*srok_otgruzki;
		float coef = 0;
		if (COEF!=null && !COEF.equals("0")) {
			coef = Float.parseFloat(COEF);
		} else {
			coef = detectCoef(calCurrent);
		}
		float bazoviyWithCoef = coef * bazoviy;
		
		if (SHARE!=null) {
			if (SHARE.equals("-1")) {
				return 1;
			} else {
				float share = Float.parseFloat(SHARE)/100;
				bazoviyWithCoef = bazoviyWithCoef + (bazoviyWithCoef*share);
			}
		}
		
		float tmp = Float.parseFloat(REST) - srednednev;
		if (tmp > 0) {
			bazoviyWithCoef = bazoviyWithCoef - tmp;
		}
		
		int celaya = (int)bazoviyWithCoef; 
		float drobnaya = bazoviyWithCoef - celaya; 
		
		if (drobnaya > 0.25) {
			celaya++;
		} 
		int rec = celaya;	
		
		if (rec<0)
			rec = 0;
		
		return rec;
	}
	
	private static float detectCoef(Calendar c) {
		float result = 0;
		int d = c.get(Calendar.DAY_OF_WEEK);
		switch (d) {
			case 1:  return 2; //Воскр
			case 2:  return 1; //Пн
			case 3:  return 1; //Вт
			case 4:  return (float)0.7; //Ср
			case 5:  return 1; //Чт
			case 6:  return 2; //Пт
			case 7:  return 2; //Сб
		}
		return result;
	}
	
	private static String inOrderAlreadyRest(String cur, ArrayList<OrderRests> rests, String goodsId) {
		for (OrderRests curRests : rests) {
			if (curRests.get(OrderRests.GOODS_ID).equals(goodsId))
				return curRests.get(OrderRests.RESTQTY);
		}
		return cur;
	}
	private static String inOrderAlreadyOrder(String cur, ArrayList<OrderLines> lines, String goodsId) {
		for (OrderLines curLine : lines) {
			if (curLine.get(OrderLines.GOODS_ID).equals(goodsId))
				return curLine.get(OrderLines.QTY);
		}
		return cur;
	}
			
}

