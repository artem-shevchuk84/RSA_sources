package ru.by.rsa;



import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnDismissListener;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnShowListener;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import ru.by.rsa.adapter.item.BrandItem;
import ru.by.rsa.adapter.item.ComboCustomerItem;
import ru.by.rsa.adapter.item.ComboShopItem;
import ru.by.rsa.adapter.item.ComboSkladItem;
import ru.by.rsa.adapter.item.GoodItem;
import ru.by.rsa.utils.DataUtils;
import ru.by.rsa.utils.GpsUtils;

/**
 * Allows user to choose ClientName, ShopAdress, Remark and other
 * parameters of current order
 * @author Komarev Roman
 * Odessa, neo3da@mail.ru, +380503412392
 */
public class NewHeadActivity extends Activity {
	private static int verOS = 2;
	boolean		restored;
	boolean		lightTheme;
	boolean		isEkvator;
	boolean		usePlan;
	boolean		useGPS;
	boolean		allowEmptyOrders;
	boolean		isPad;
	boolean 	usingVozvrat;
	boolean		usingQuest;
	boolean 	usingMatrix;
	boolean 	usingDelivery;
	boolean		usingAgreement;
	boolean		usingGeoPhoto;
	boolean		isActivityFullyInitialized;
	static boolean 	saving_in_progress;
	String		routecode;
	String 		currency;
	String		SD_CARD_PATH;
	String 		mCurrentPhotoPath;

	String 		deliveryDate;

	int			lastImageID;

	Bundle		extras;

	OrderHead	mOrderH;
	ArrayList<ComboCustomerItem> listCustomers;
	ArrayList<ComboShopItem> 	 listShops;
	ArrayList<ComboSkladItem> 	 listSklad;
	ArrayList<OrdersListItem> 	 listOrders;


	Spinner 		spnrCustomers;
	Spinner			spnrShops;
	ToggleButton 	tbtnCash;
	ToggleButton 	tbtnVAT;
	ToggleButton 	tbtnDiscount;
	ToggleButton 	tbtnSklad;
	ToggleButton 	tbtnDelay;
	ToggleButton 	tbtnDelivery;
	ToggleButton	tbtnPhoto;
	ToggleButton	tbtnCb1;
	ToggleButton	tbtnCb2;
	ToggleButton	tbtnCb3;
	ToggleButton	tbtnCb4;
	TextView 		txtRemark;
	Button			btnRemark;
	Button			btnQuest;
	Button			btnCancel;
	Button			btnLines;
	Button			btnOK;
	CheckBox		chkReturn;

	public static final int IDM_TOPSKU				= 500;
	public static final int IDM_TAKEPHOTO			= 501;
	public static final int IDM_CUSTINFO			= 502;
	public static final int IDM_STATISTIC			= 503;
	public static final int IDM_SETTYPE				= 504;
	public static final int IDM_AGREED				= 505;
	public static final int IDM_GEOPHOTO_ONSAVE		= 506;
	public static final int IDM_GEOPHOTO_REWRITE	= 507;

	private final static int IDD_DISCOUNT	= 1;
	private final static int IDD_DELAY		= 3;
	private final static int IDD_SKLAD		= 4;
	private final static int IDD_REMARK		= 5;
	private final static int IDD_DELIVERY	= 6;
	private final static int IDD_RETURN		= 7;
	private final static int IDD_SETTYPE	= 8;


	static final int PICK_HEAD_REQUEST 		= 2;
	static final int PICK_PHOTO_REQUEST 	= 11;

	private static final String JPEG_FILE_SUFFIX = ".jpg";

	SharedPreferences def_prefs;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		lightTheme 		= getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE).getBoolean(RsaDb.LIGHTTHEMEKEY, false);
		isPad	   		= RsaDb.checkScreenSize(this, 5);

		if (lightTheme) {
			setTheme(R.style.Theme_Custom);
			super.onCreate(savedInstanceState);
			setContentView(isPad?R.layout.pad_l_head:R.layout.l_head);
		} else {
			setTheme(R.style.Theme_CustomBlack2);
			super.onCreate(savedInstanceState);
			setContentView(isPad?R.layout.pad_head:R.layout.head);
		}

		deliveryDate= "";
		lastImageID = 0;
		try {
			isActivityFullyInitialized = false;
			saving_in_progress = false;
			def_prefs 		= PreferenceManager.getDefaultSharedPreferences(this);
			usingMatrix 	= getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE).getBoolean(RsaDb.MATRIXKEY, false);
			usingDelivery 	= def_prefs.getBoolean("prefUsedelivery", false);
			usingAgreement  = def_prefs.getBoolean("prefUseAgreed", false);
			usingGeoPhoto 	= def_prefs.getBoolean("prefGeoPhoto", false);
			isEkvator 		= getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE).getBoolean(RsaDb.EKVATOR, false);
			usePlan 		= def_prefs.getBoolean(RsaDb.USINGPLAN, false);
			useGPS			= getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE).getBoolean(RsaDb.GPSKEY, false);
			routecode		= getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE).getString(RsaDb.CODEKEY, "0");
			allowEmptyOrders= getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE).getBoolean(RsaDb.SENDLINES, false);
			SD_CARD_PATH 	= Environment.getExternalStorageDirectory().toString() + File.separator + "rsa" + File.separator + "outbox";
			usingVozvrat 	= def_prefs.getBoolean("prefUsingVozvrat", false);
			usingQuest	 	= def_prefs.getBoolean("prefUsingQuest", false);
			if (usingMatrix==true || usingQuest == true) {
				allowEmptyOrders= true;
			}
			currency 		= " " + def_prefs.getString("prefCurrency", getResources().getString(R.string.preferences_currency_summary));
			verOS 			= Integer.parseInt(Build.VERSION.RELEASE.substring(0,1));
		} catch(Exception e) {
			writeErrLog("HeadActivity: Error while detecting screensize, theme, etc...", e.getMessage());
		}

		try {
			((View)findViewById(R.id.lnrReturn)).setVisibility(usingVozvrat?View.VISIBLE:View.GONE);
		} catch(Exception e) {}
		try {
			((View)findViewById(R.id.btnQuest_head)).setVisibility(usingQuest?View.VISIBLE:View.GONE);
		} catch(Exception e) {}


		try {
			if (usingMatrix == true || usingDelivery == true) {
				//SimpleDateFormat sfm = new SimpleDateFormat("yyyy-MM-dd");
				//Calendar c = Calendar.getInstance();
				((View)findViewById(R.id.tbtn_Delivery_head)).setVisibility(View.VISIBLE);
				//c.add(Calendar.DATE, 1);
				//deliveryDate = sfm.format(c.getTime());
			} else {
				((View)findViewById(R.id.tbtn_Delivery_head)).setVisibility(View.GONE);
				deliveryDate = "";
			}
		} catch(Exception e) {}

		if (savedInstanceState != null) {
        	extras	= savedInstanceState;
        	restored = true;
        	mOrderH	= extras.getParcelable("ORDERH");
        } else {
        	extras = getIntent().getExtras();
        	restored = false;
        }

		removeQuestionnaire();
		if (usingGeoPhoto) removeGeophoto();
		//writeErrLog("HeadActivity: onCreate()", "OK");
	}

	@Override
	public void onStart() {
		super.onStart();
		//writeErrLog("HeadActivity: onStart()", "OK");
	}

	@Override
	public void onResume() {
		super.onResume();
		//writeErrLog("HeadActivity: onResume()", "OK");
		isActivityFullyInitialized = false;
		try {
			updateAll();
		} catch (Exception sEx) {
			writeErrLog("HeadActivity: updateAll()", sEx.getMessage());
		}
		isActivityFullyInitialized = true;
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putInt("MODE", mOrderH.mode);
		savedInstanceState.putString("_id", mOrderH._id);
		savedInstanceState.putParcelable("ORDERH", mOrderH);
		savedInstanceState.putString("SKLADID", mOrderH.sklad_id.toString());
		savedInstanceState.putString("REMARK", mOrderH.remark.toString());
		savedInstanceState.putString("DELAY", mOrderH.delay.toString());
		savedInstanceState.putString("DISCOUNT", mOrderH.id.toString());
	}

	private void updateAll() {
		isPad	   		= RsaDb.checkScreenSize(this, 5);
		try {
			spnrCustomers	= (Spinner)findViewById(R.id.cmbCust_head);
			spnrShops		= (Spinner)findViewById(R.id.spnShop_head);
			tbtnCb1		= (ToggleButton)findViewById(R.id.tbtn_cb1);
			tbtnCb2		= (ToggleButton)findViewById(R.id.tbtn_cb2);
			tbtnCb3		= (ToggleButton)findViewById(R.id.tbtn_cb3);
			tbtnCb4		= (ToggleButton)findViewById(R.id.tbtn_cb4);
			tbtnCash		= (ToggleButton)findViewById(R.id.tbtn_Cash_head);
	        tbtnVAT			= (ToggleButton)findViewById(R.id.tbtn_VAT_head);
	        tbtnSklad		= (ToggleButton)findViewById(R.id.tbtn_Sklad_head);
	        tbtnDelay		= (ToggleButton)findViewById(R.id.tbtn_Delay_head);
	        tbtnDiscount	= (ToggleButton)findViewById(R.id.tbtn_Discount_head);
	        tbtnDelivery	= (ToggleButton)findViewById(R.id.tbtn_Delivery_head);
	        txtRemark 		= (TextView)findViewById(R.id.txtRemark_head);
	        btnRemark 		= (Button)findViewById(R.id.btnRemark_head);
	        btnQuest 		= (Button)findViewById(R.id.btnQuest_head);
	        btnCancel 		= (Button)findViewById(R.id.btnCancel_head);
	        btnLines 		= (Button)findViewById(R.id.head_pbtn_next);
	        btnOK 			= (Button)findViewById(R.id.head_pbtn_prev);
	        chkReturn 		= (CheckBox)findViewById(R.id.isReturn);
	        if (isPad)	{
	        	tbtnPhoto	= (ToggleButton)findViewById(R.id.tbtn_Photo_head);
	        	tbtnPhoto.setVisibility(View.VISIBLE);
	        }

		} catch(Exception e) {writeErrLog("HeadActivity: bindViews()", 	e.getMessage());}

        try{ bindCustomersSpinner();} catch(Exception e) {writeErrLog("HeadActivity: bindCustomerSpinner()", 	e.getMessage());}
        try{ bindShopsSpinner();	} catch(Exception e) {writeErrLog("HeadActivity: bindShopsSpinner()", 		e.getMessage());}
        try{ bindCustomToggle();	} catch(Exception e) {writeErrLog("HeadActivity: bindCustomToggle()", 		e.getMessage());}
        try{ bindSkladToggle();		} catch(Exception e) {writeErrLog("HeadActivity: bindSkladToggle()", 		e.getMessage());}
        try{ bindPaytypeToggle();	} catch(Exception e) {writeErrLog("HeadActivity: bindPaytypeToggle()", 		e.getMessage());}
        try{ bindVATToggle();		} catch(Exception e) {writeErrLog("HeadActivity: bindVATToggle()", 			e.getMessage());}
        try{ bindDelayToggle();		} catch(Exception e) {writeErrLog("HeadActivity: bindDelayToggle()", 		e.getMessage());}
        try{ bindDiscountToggle();	} catch(Exception e) {writeErrLog("HeadActivity: bindDiscountToggle()", 	e.getMessage());}
        try{ bindDeliveryToggle();	} catch(Exception e) {writeErrLog("HeadActivity: bindDeliveryToggle()", 	e.getMessage());}
        try{ bindRemarkButton();	} catch(Exception e) {writeErrLog("HeadActivity: bindRemarkButton()", 		e.getMessage());}
        try{ bindQuestButton();		} catch(Exception e) {writeErrLog("HeadActivity: bindQuestButton()", 		e.getMessage());}
        try{ bindCancelButton();	} catch(Exception e) {writeErrLog("HeadActivity: bindCancelButton()", 		e.getMessage());}
        try{ bindLinesButton();		} catch(Exception e) {writeErrLog("HeadActivity: bindLinesButton()", 		e.getMessage());}
        try{ bindOKButton();		} catch(Exception e) {writeErrLog("HeadActivity: bindOKButton()", 			e.getMessage());}
        try{ bindReturnCheckbox();	} catch(Exception e) {writeErrLog("HeadActivity: bindReturnCheckbox()", 	e.getMessage());}
        if (isPad)
        	try{ bindPhotoToggle();	} catch(Exception e) {writeErrLog("HeadActivity: bindPhotoToggle()", 		e.getMessage());}

        if (RsaDb.checkScreenSize(this, 3.62)==false) {
        	btnRemark.setText("Примеч");
        	//btnQuest.setText("Анк");
        	btnCancel.setText("Отмена");
        }

        if (restored == true)
        	try{ updateFromSavedState();} catch(Exception e) {writeErrLog("HeadActivity: updateFromSavedState()", 	e.getMessage());}
		else
			try{ updateFromExtras();	} catch(Exception e) {writeErrLog("HeadActivity: updateFromExtras()", 		e.getMessage());}
		RsaApplication app = (RsaApplication) getApplicationContext();
		app.orderingId = mOrderH==null?null:mOrderH._id;
	}

	private void updateFromSavedState() {
		mOrderH			= extras.getParcelable("ORDERH");
		mOrderH.mode	= extras.getInt("MODE");
		mOrderH._id		= extras.getString("_id");

		if (removeDublicatesFromLines()) writeErrLog("HeadActivity: removeDublicatesFromLines()", "There are were dublicates");
		updateRestored();
	}

	private void updateFromExtras() {
		mOrderH = new OrderHead();
		mOrderH.clear();
		mOrderH.restored 	= false;
		mOrderH.mode 		= extras.getInt("MODE");
		if (usePlan) {
			mOrderH.cust_id = extras.getString("_CUSTID");
			mOrderH.shop_id	= extras.getString("_SHOPID");
		}

		if (mOrderH.mode == RSAActivity.IDM_MODIFY) updateModified();
		else if (mOrderH.mode == RSAActivity.IDM_ADD) updateAdded();
		else writeErrLog("HeadActivity: Undefined MODE", ""); return;
	}

	private void updateFromResult(Bundle data) {
		extras		= data;
		restored	= true;
	}

	private void updateRestored() {
		fillCustomersFromDB(mOrderH.cust_id.toString());
		fillShopsFromDB(mOrderH.cust_id.toString(), mOrderH.shop_id.toString());
		fillSkladsListFromDB(mOrderH.sklad_id.toString());
		fillButtonsFromDB();
		if (usingMatrix == true || usingDelivery == true) {
			deliveryDate = mOrderH.delivery.toString();
		}
		fillComment();
	}

	private void updateAdded() {
		SharedPreferences def_prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		Outlet o = null;

		boolean useExtdebit = def_prefs.getBoolean("useExtdebit", false);
		if (useExtdebit) {
			o = getLastOutletFromPrefs();
		} else {
			o = getLastOutlet();
		}

		if (usePlan) {
			o = new Outlet(mOrderH.cust_id.toString(), mOrderH.shop_id.toString());
		} else if (o!=null) {
			// do nothing, because o has value that we need
		} else {
			o = new Outlet(null, null);
		}

		fillCustomersFromDB(o.getCust_id());
		fillShopsFromDB(	o.getCust_id()!=null?o.getCust_id():listCustomers.get(0).get(ComboShopItem.ID),
							o.getShop_id());
		try {
			fillSkladsListFromDB(null);
		} catch(Exception e) {
			Log.d("RRRR",e.getMessage());
		}
		fillButtonsFromDefaults();

		if (usingMatrix == true) {
			SimpleDateFormat sfm = new SimpleDateFormat("yyyy-MM-dd");
			Calendar c = Calendar.getInstance();
			c.add(Calendar.DATE, 1);
			deliveryDate = sfm.format(c.getTime());
			mOrderH.delivery = deliveryDate;
			mOrderH.paytype = "Без";
			mOrderH.hndsrate = "0";
		}

		if (usingDelivery == true) {
			SimpleDateFormat sfm = new SimpleDateFormat("yyyy-MM-dd");
			Calendar c = Calendar.getInstance();
			c.add(Calendar.DATE, 1);
			if (c.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
				c.add(Calendar.DATE, 1);
			deliveryDate = sfm.format(c.getTime());
			mOrderH.delivery = deliveryDate;
		}

		fillComment();
	}

	private void updateModified() {
		fillmOrderHFromDB(extras.getString("_id"));
		fillCustomersFromDB(mOrderH.cust_id.toString());
		fillShopsFromDB(mOrderH.cust_id.toString(), mOrderH.shop_id.toString());
		fillSkladsListFromDB(mOrderH.sklad_id.toString());
		fillButtonsFromDB();
		if (usingMatrix == true || usingDelivery == true) {
			deliveryDate = mOrderH.delivery.toString();
		}
		fillComment();
	}

	private void fillCustomersFromDB(String selectedID) {
		String 				query 	= "select ID, NAME, TEL, ADDRESS, OKPO, INN, CONTACT, DOGOVOR, COMMENT from _cust where ID <> '' group by ID order by NAME";

		SharedPreferences def_prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		boolean prefFixCustomer = def_prefs.getBoolean("prefFixCustomer", false);

		if (usePlan || (!mOrderH.lines.isEmpty() && prefFixCustomer)  ) {
			query 	= "select ID, NAME, TEL, ADDRESS, OKPO, INN, CONTACT, DOGOVOR, COMMENT " +
					  "from _cust " +
					  "where ID ='"+selectedID+"' " +
					  "group by ID order by NAME";
		}

		SharedPreferences 	prefs	= getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);
		RsaDbHelper 		mDb		= new RsaDbHelper(this, prefs.getString(RsaDb.ACTUALDBKEY, RsaDbHelper.DB_NAME1));
		SQLiteDatabase 		db		= mDb.getReadableDatabase();
		Cursor				cursor	= db.rawQuery(query, null);

		listCustomers = new ArrayList<ComboCustomerItem>();
		if (cursor.getCount()>0) {
			while (cursor.moveToNext()) {
				listCustomers.add(new ComboCustomerItem(cursor.getString(0),	cursor.getString(1),	cursor.getString(2),	cursor.getString(3),
														cursor.getString(4),	cursor.getString(5),	cursor.getString(6), 	cursor.getString(7),
														cursor.getString(8)));
			}
		} else {
			listCustomers.add(new ComboCustomerItem("000", "Нет клиентов", "", "", "", "", "", "", ""));
		}

		SimpleAdapter adapter = new SimpleAdapter(this, listCustomers,
														isPad?R.layout.pad_simple_spinner_item:android.R.layout.simple_spinner_item,
														new String[] {null, null, ComboCustomerItem.NAME},
														new int[] {0, 0, android.R.id.text1});
		adapter.setDropDownViewResource(isPad?R.layout.pad_simple_spinner_dropdown_item:android.R.layout.simple_spinner_dropdown_item);
		spnrCustomers.setAdapter(adapter);

		int position = selectedID==null?0:findCustIndexByID(selectedID);
		mOrderH.cust_id 	= listCustomers.get(position).get(ComboCustomerItem.ID);
		mOrderH.cust_text	= listCustomers.get(position).get(ComboCustomerItem.NAME);
		spnrCustomers.setSelection(position);

		if (cursor!=null && !cursor.isClosed()) {
			cursor.close();
		}
		if (db!=null && db.isOpen()) {
			db.close();
		}
	}

	private void fillShopsFromDB(String cust_id, String selectedID) {
		String 				query 	= "select ID, CUST_ID, NAME, ADDRESS, TYPE from _shop " +
									  "where CUST_ID = '"+ cust_id +
									  "' group by ID order by NAME";

		SharedPreferences def_prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		boolean prefFixCustomer = def_prefs.getBoolean("prefFixCustomer", false);

		if (usePlan || (!mOrderH.lines.isEmpty() && prefFixCustomer) ) {
			query 	= "select ID, CUST_ID, NAME, ADDRESS, TYPE from _shop " +
					  "where CUST_ID = '"+ cust_id +"' and ID = '"+selectedID+"' group by ID order by NAME";

		}



		SharedPreferences 	prefs	= getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);
		RsaDbHelper 		mDb		= new RsaDbHelper(this, prefs.getString(RsaDb.ACTUALDBKEY, RsaDbHelper.DB_NAME1));
		SQLiteDatabase 		db		= mDb.getReadableDatabase();
		Cursor				cursor	= db.rawQuery(query, null);

		listShops = new ArrayList<ComboShopItem>();
		if (cursor.getCount()>0) {
			while (cursor.moveToNext()) {
				listShops.add(new ComboShopItem(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4)));
			}
		} else {
			listShops.add(new ComboShopItem("000", "(Нет точки)", "(нет точки)", "", ""));
		}

		SimpleAdapter adapter = new SimpleAdapter(this, listShops,
														isPad?R.layout.pad_spinner_shop_item:R.layout.spinner_shop_item,
														new String[] {null, null, ComboShopItem.NAME, ComboShopItem.ADRES},
														new int[] {0, 0, R.id.spinner_shop_text0, R.id.spinner_shop_text1});
		adapter.setDropDownViewResource(isPad?R.layout.pad_spinner_shop_dropdown_item:R.layout.spinner_shop_dropdown_item);
		spnrShops.setAdapter(adapter);

		int position = selectedID==null?0:findShopIndexByID(selectedID);
		mOrderH.shop_id 	= listShops.get(position).get(ComboShopItem.ID);
		mOrderH.shop_text	= listShops.get(position).get(ComboShopItem.NAME);
		spnrShops.setSelection(position);

		if (cursor!=null && !cursor.isClosed()) {
			cursor.close();
		}
		if (db!=null && db.isOpen()) {
			db.close();
		}
	}

	private void fillSkladsListFromDB(String selectedID) {
		SharedPreferences 	screen_prefs = getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE);
		SQLiteDatabase 		db			 = null;
		Cursor				cursor		 = null;

		String				defID		 = "r1r";
		String				defName		 = "r1r";

		if (listSklad==null) {
			String 				query 	= "select ID, NAME, DFLT from _sklad where ID <> '' group by ID order by NAME";
			SharedPreferences 	prefs	= getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);
			RsaDbHelper 		mDb		= new RsaDbHelper(this, prefs.getString(RsaDb.ACTUALDBKEY, RsaDbHelper.DB_NAME1));
								db		= mDb.getReadableDatabase();
								cursor	= db.rawQuery(query, null);

			listSklad = new ArrayList<ComboSkladItem>();
			if (cursor.getCount()>0) {
				while (cursor.moveToNext()) {
					listSklad.add(new ComboSkladItem(cursor.getString(0), cursor.getString(1)));
					try {
						if (cursor.getString(2).equals("1")) {
							defID = cursor.getString(0);
							defName = cursor.getString(1);
						}
					} catch (Exception e) {};
				}
			} else {
				listSklad.add(new ComboSkladItem("000", "(Нет склада)"));
			}
		}

		String lastID 		= screen_prefs.getString(RsaDb.LASTSKLADID, 	"x");
		String lastName 	= screen_prefs.getString(RsaDb.LASTSKLADNAME, 	"x");

		String s = "";
		if (mOrderH != null
				&& mOrderH.sklad_text!=null
				&& !mOrderH.equals("")
				&& usingAgreement
				&& mOrderH.sklad_text.toString().contains("&&^^")) {
			s = "^^&&";
		}

		if (selectedID==null && lastID.equals("x")) {
			if (defID.equals("r1r") == false) {
				mOrderH.sklad_id = defID;
				mOrderH.sklad_text = defName+s;
			} else {
				mOrderH.sklad_id 	= listSklad.get(0).get(ComboSkladItem.ID);
				mOrderH.sklad_text	= listSklad.get(0).get(ComboSkladItem.NAME)+s;
			}
		} else if (selectedID==null) {
			if (defID.equals("r1r") == false) {
				mOrderH.sklad_id = defID;
				mOrderH.sklad_text = defName+s;
			} else {
				mOrderH.sklad_id	= lastID;
				mOrderH.sklad_text	= lastName+s;
			}
		}

		if (cursor!=null && !cursor.isClosed()) {
			cursor.close();
		}
		if (db!=null && db.isOpen()) {
			db.close();
		}
	}

	private List<HashMap<String, String>> fillShopTypesListFromDB() {

		List<HashMap<String, String>> fillMaps = new ArrayList<HashMap<String, String>>();

		SQLiteDatabase 		db			 = null;
		Cursor				cursor		 = null;
		String 				query 	= "select NAME from _stype";
		SharedPreferences 	prefs	= getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);
		RsaDbHelper 		mDb		= new RsaDbHelper(this, prefs.getString(RsaDb.ACTUALDBKEY, RsaDbHelper.DB_NAME1));
							db		= mDb.getReadableDatabase();
							cursor	= db.rawQuery(query, null);

		HashMap<String, String> map = new HashMap<String, String>();

		if (cursor.getCount()>0) {
			while (cursor.moveToNext()) {
				map.put("NAME", cursor.getString(0));
				fillMaps.add(map);
				map = new HashMap<String, String>();
			}
		} else {
			map.put("NAME", "Undef");
			fillMaps.add(map);
		}

		if (cursor!=null && !cursor.isClosed()) {
			cursor.close();
		}
		if (db!=null && db.isOpen()) {
			db.close();
		}

		return fillMaps;
	}

	private void updateShopType (String cid, String sid, String newType) {
		SQLiteDatabase 		db			 = null;
		SharedPreferences 	prefs	= getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);
		RsaDbHelper 		mDb		= new RsaDbHelper(this, prefs.getString(RsaDb.ACTUALDBKEY, RsaDbHelper.DB_NAME1));
							db		= mDb.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(RsaDbHelper.SHOP_TYPE, newType);

		db.update(RsaDbHelper.TABLE_SHOP, values, "CUST_ID='"+cid+"' and ID='"+sid+"'", null);

		if (db!=null && db.isOpen()) {
			db.close();
		}
	}

	private void fillOrdersList() {
		SQLiteDatabase 		db			 = null;
		Cursor				cursor		 = null;

		if (listOrders==null) {
			String 			query = "select DATE, _id, HSUMO from _head where CUST_ID = '" + mOrderH.cust_id + "' " +
																   " AND SHOP_ID = '" + mOrderH.shop_id +"' " +
																   " AND (NUM1C = '' OR NUM1C = '0') " +
																   "order by _id desc limit 10";
			RsaDbHelper 	mDb_ord	  = new RsaDbHelper(this, RsaDbHelper.DB_ORDERS);
							db		= mDb_ord.getReadableDatabase();
							cursor	= db.rawQuery(query, null);

			listOrders = new ArrayList<OrdersListItem>();
			listOrders.add(new OrdersListItem("Не нашел", "-", "-"));
				if (cursor.getCount()>0) {
					while (cursor.moveToNext()) {
						listOrders.add(new OrdersListItem(cursor.getString(0),
															cursor.getString(1),
															  cursor.getString(2)));
					}
				}
		}

		if (cursor!=null && !cursor.isClosed()) {
			cursor.close();
		}
		if (db!=null && db.isOpen()) {
			db.close();
		}
	}

	private boolean fillmOrderHFromDB(String selectedID) {
		//// fill HEAD //////////////////////////////////////////////////////////////////////////
		String 	query 	= 	"select _id, ID, ZAKAZ_ID, CUST_ID, SHOP_ID, SKLAD_ID, BLOCK, SENDED, CUST_TEXT, SHOP_TEXT, " +
							"SKLAD_TEXT, DELAY, PAYTYPE, HSUMO, HWEIGHT, HVOLUME, DATE, TIME, HNDS, HNDSRATE, SUMWONDS, " +
							"NUMFULL, NUM1C, GPSCOORD, REMARK, ROUTECODE, VISITID, SDATE, MONITORED, DELIVERY from _head " +
							"where _id = '"+selectedID+"' group by _id LIMIT 1";
		RsaDbHelper 		mDb		= new RsaDbHelper(this, RsaDbHelper.DB_ORDERS);
		SQLiteDatabase 		db		= mDb.getReadableDatabase();
		Cursor				cursor	= db.rawQuery(query, null);
		if (cursor.getCount()>0) cursor.moveToFirst();
		else return false;
		mOrderH._id 		= cursor.getString(0);  mOrderH.id			= cursor.getString(1).equals("")?"0":cursor.getString(1);
		mOrderH.zakaz_id	= cursor.getString(2);  mOrderH.cust_id		= cursor.getString(3);
		mOrderH.shop_id		= cursor.getString(4);  mOrderH.sklad_id	= cursor.getString(5);
		mOrderH.block		= cursor.getString(6);  mOrderH.sended		= cursor.getString(7);
		mOrderH.cust_text	= cursor.getString(8);  mOrderH.shop_text	= cursor.getString(9);
		mOrderH.sklad_text	= cursor.getString(10); mOrderH.delay		= cursor.getString(11);
		mOrderH.paytype		= cursor.getString(12); mOrderH.hsumo		= cursor.getString(13);
		mOrderH.hweight		= cursor.getString(14); mOrderH.hvolume		= cursor.getString(15);
		mOrderH.date		= cursor.getString(16); mOrderH.time		= cursor.getString(17);
		mOrderH.hnds		= cursor.getString(18); mOrderH.hndsrate	= cursor.getString(19);
		mOrderH.sumwonds	= cursor.getString(20); mOrderH.numfull		= cursor.getString(21);
		mOrderH.num1c		= cursor.getString(22); mOrderH.gpscoord	= cursor.getString(23);
		mOrderH.remark		= cursor.getString(24); mOrderH.routecode	= cursor.getString(25);
		mOrderH.visitid		= cursor.getString(26); mOrderH.delivery	= cursor.getString(29);
		if (cursor!=null && !cursor.isClosed()) {
			cursor.close();
		}

		//// fill LINES /////////////////////////////////////////////////////////////////////////
		query 	= "select _id, ID, ZAKAZ_ID, GOODS_ID, TEXT_GOODS, RESTCUST, QTY, UN, COEFF, DISCOUNT, PRICEWNDS, SUMWNDS, PRICEWONDS, " +
				  "SUMWONDS, NDS, DELAY, WEIGHT, COMMENT, BRAND_NAME from _lines where (ZAKAZ_ID='"+selectedID+"') and (GOODS_ID<>'') group by GOODS_ID";
		cursor	= db.rawQuery(query, null);
		if (cursor.getCount()>0) {
			while (cursor.moveToNext()) {
				mOrderH.lines.add(new OrderLines( "",								// *id !not used
										selectedID,									// *zakaz_id foreign to HEAD _id
										cursor.getString(3),						// goods_id
										cursor.getString(4),						// text_goods
										cursor.getString(5),						// PRICETYPE (Price1,Price2...20)
										cursor.getString(6),						// qty
										cursor.getString(7),						// un
										cursor.getString(8),						// coeff
										cursor.getString(9),						// discount
										cursor.getString(10), 						// pricewnds
										cursor.getString(11), 						// sumwnds
										cursor.getString(12),						// pricewonds=pricewnds/1.2
										cursor.getString(13),	 					// sumwonds=sumwnds/1.2
										cursor.getString(14),						// nds=sumwnds-sumwonds
										cursor.getString(15),						// must be delay but overloaded for nds flag
										cursor.getString(16),						// weight
										cursor.getString(17),						// comment
										cursor.getString(18)));						// brand_name
			}
		}
		if (cursor!=null && !cursor.isClosed()) {
			cursor.close();
		}

		//// fill RESTS /////////////////////////////////////////////////////////////////////////
		query 	= "select _id, ID, ZAKAZ_ID, GOODS_ID, RESTQTY, RECQTY, QTY from _rests where (ZAKAZ_ID='"+selectedID+"') and (GOODS_ID<>'') group by GOODS_ID";
		cursor	= db.rawQuery(query, null);
		if (cursor.getCount()>0) {
			while (cursor.moveToNext()) {
				mOrderH.restslines.add(new OrderRests( "", 							// *id !not used
											selectedID,								// *zakaz_id foreign to HEAD _id
											cursor.getString(3),					// goods_id
											cursor.getString(4),					// restqty
											cursor.getString(5),					// recqty
											cursor.getString(6)));					// qty
			}
		}
		if (cursor!=null && !cursor.isClosed()) {
			cursor.close();
		}

		if (db!=null && db.isOpen()) {
			db.close();
		}
		return true;
	}

	private void fillButtonsFromDefaults() {
		SharedPreferences prefs = getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE);
		String cVAT = prefs.getString(RsaDb.LASTVATKEY, "x");

		if (cVAT.equals("0")) {
			tbtnVAT.setChecked(true);
			tbtnVAT.setTextOn("НДС20");
			tbtnVAT.setText("НДС20");
			mOrderH.hndsrate = "0";
		} else if (cVAT.equals("1")) {
			tbtnVAT.setChecked(false);
			tbtnVAT.setTextOff("бНДС");
			tbtnVAT.setText("бНДС");
			mOrderH.hndsrate = "1";
		} else if (cVAT.equals("2")) {
			tbtnVAT.setChecked(true);
			tbtnVAT.setTextOn("НДС7");
			tbtnVAT.setText("НДС7");
			mOrderH.hndsrate = "2";
		} else {
			tbtnVAT.setChecked(true);
			tbtnVAT.setTextOn("НДС20");
			tbtnVAT.setText("НДС20");
			mOrderH.hndsrate = "0";
		}

		//tbtnVAT.setChecked(!prefs.getString(RsaDb.LASTVATKEY, "x").equals("1")); // if "1" NDS=off overwise NDS=on

		tbtnDelay.setChecked(false);
		tbtnSklad.setChecked(true);
		tbtnDiscount.setChecked(false);
		tbtnDelivery.setChecked(true);
		tbtnCash.setChecked(false);
		if (usingMatrix) {
			tbtnVAT.setChecked(true);
			tbtnVAT.setTextOn("НДС20");
			tbtnVAT.setText("НДС20");
			mOrderH.hndsrate = "0";
			tbtnCash.setChecked(true);
		}

		mOrderH.paytype  = "Нал";
	}

	private void fillButtonsFromDB() {
		//tbtnVAT.setChecked(mOrderH.hndsrate.toString().equals("0")); // if "1" NDS=off overwise NDS=on



			int customButtonsState = 0;

			try {
				customButtonsState = Integer.parseInt(mOrderH.visitid.toString().equals("")?"0":mOrderH.visitid.toString());
			} catch (Exception e) {}

			tbtnCb1.setChecked((customButtonsState&1)==1);
			tbtnCb2.setChecked((customButtonsState&2)==2);
			tbtnCb3.setChecked((customButtonsState&4)==4);
			tbtnCb4.setChecked((customButtonsState&8)==8);
			mOrderH.visitid = Integer.toString(customButtonsState);

		if (mOrderH.hndsrate.equals("0")) {
			tbtnVAT.setChecked(true);
			tbtnVAT.setTextOn("НДС20");
			tbtnVAT.setText("НДС20");
		} else if (mOrderH.hndsrate.equals("1")) {
			tbtnVAT.setChecked(false);
			tbtnVAT.setTextOff("бНДС");
			tbtnVAT.setText("бНДС");
		} else if (mOrderH.hndsrate.equals("2")) {
			tbtnVAT.setChecked(true);
			tbtnVAT.setTextOn("НДС7");
			tbtnVAT.setText("НДС7");
		}

		tbtnCash.setChecked(mOrderH.paytype.toString().equals("Без"));
		tbtnSklad.setChecked(true);
		String s = mOrderH.delay.toString();
		tbtnDelay.setChecked(!s.equals("") && !s.equals("0"));
		s = mOrderH.id.toString();
		tbtnDiscount.setChecked(!s.equals("") && !s.equals("0"));
		chkReturn.setChecked(mOrderH.num1c.toString().length()>0&&(!mOrderH.num1c.toString().equals("0")));
	}

	private void fillComment() {
		SharedPreferences def_prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		boolean isAutocorrect = def_prefs.getBoolean("prefDebitAutocorrection", false);
		SharedPreferences prefs = getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE);
		String vatRate	= prefs.getString(RsaDb.VATRATE, "20");

		String strCust	= getResources().getString(R.string.head_client) + mOrderH.cust_text  + "\n";
		String strShop	= getResources().getString(R.string.head_adress) + mOrderH.shop_text  + "\n";
		String strSklad	= getResources().getString(R.string.head_warehouse)
								+ mOrderH.sklad_text.toString().replace("^^&&", "") + "\n";
		String strDelay =  getResources().getString(R.string.head_paydelay) + mOrderH.delay
																	+ getResources().getString(R.string.head_days);
		String cNDS = "";
		if (mOrderH.hndsrate.toString().equals("0")) {
			cNDS = " (С НДС = " + vatRate + "%)\n";
		} else if (mOrderH.hndsrate.toString().equals("1")) {
			cNDS = " (БЕЗ НДС)\n";
		} else if (mOrderH.hndsrate.toString().equals("2")) {
			cNDS = " (С НДС = 7%)\n";
		}

		String strPay = getResources().getString(R.string.head_cashpaytype) + mOrderH.paytype + cNDS;
		String strRem = getResources().getString(R.string.head_writeremark) + mOrderH.remark + "\n";
		String strReadOnly = "";
		if (mOrderH.mode==RSAActivity.IDM_MODIFY
				&& Integer.parseInt(mOrderH.block.toString())==R.drawable.ic_locked_) {
			strReadOnly = getResources().getString(R.string.head_readonly);
		}
		String strPhoto = "";
		try {
			strPhoto = getResources().getString(R.string.head_photo) + getPhotoCount() + " шт.  ";
		} catch (Exception e){}

		String strDebit = "Deb. error";
		try {
				strDebit = getResources().getString(R.string.head_debit) + String.format("%.2f",mOrderH.debit_actual).replace(',', '.')
						+ " (" + String.format("%.2f",mOrderH.debit_total).replace(',', '.') + currency + ")\n";
		} catch(Exception e) {}

		String strDsc = "0";

		float hsumo = 0;
		float disc = 0;
		for (OrderLines curLine : mOrderH.lines) {
			strDsc = curLine.get(OrderLines.DISCOUNT).equals("") ? "0" : curLine.get(OrderLines.DISCOUNT);
			hsumo += Float.parseFloat(curLine.get(OrderLines.SUMWNDS));
			disc = hsumo*(Float.parseFloat(strDsc)/100);
			hsumo = hsumo-disc;
		}



		String strType = " Тип ТТ: ";
		try {
			int s = spnrShops.getSelectedItemPosition();
			strType += listShops.get(s).get(ComboShopItem.TYPE) + "\n";
		} catch (Exception e) {}

		try {
			if (def_prefs.getBoolean("usingFactCash", false)) {
				strDsc = " Факт оплата: "+ strDsc;
				strType = "\n";
			} else {
				strDsc = getResources().getString(R.string.head_writedisc) + strDsc
						+ "% (" + DataUtils.Float.format("%.2f",disc)
						+ currency + ") ";
			}
		} catch (Exception e) {}

		String strSum = "Sum err.";
		try {
			strSum = getResources().getString(R.string.head_sum) + DataUtils.Float.format("%.2f",hsumo)
				+ currency + "\n";
		} catch (Exception e) {}

		String strDelivery = "";
		if ((usingMatrix || usingDelivery) && deliveryDate.length()>0) {
			strDelivery = " Дата отгрузки: " + deliveryDate + "\n";
		}
		String strVzrt = "";
		if (mOrderH.num1c.toString().length()>0&&(!mOrderH.num1c.toString().equals("0")))
			strVzrt=" Возврат к заказу: " + mOrderH.num1c.toString()+"\n";

		String strShopPhoto = "";
		String strShopGPS = "";

		if (usingGeoPhoto) {
			strShopPhoto = getShopPhotoInfo()+"\n";
			strShopGPS 	 = getShopGPSInfo();
		}

		if (isAutocorrect) {
			try {
				strType="";
				strSklad = "";
				strDsc = "";
				strDebit = getResources().getString(R.string.head_debit)
						+ " " + String.format("%.2f",mOrderH.debit_total).replace(',', '.') + currency + "\n";
				} catch(Exception e) {}
		}

		txtRemark.setText(strVzrt+strCust+strShop+strSklad+strDelay+strPay
				+strSum+strDsc+strType+strDebit+strPhoto
				+strRem+strReadOnly+strDelivery+strShopPhoto+strShopGPS);
	}

	private String getShopPhotoInfo() {
		String result = " Фотография ТТ: ОТСУТСТВУЕТ В 1С!";
		SQLiteDatabase 		db	    = null;
		SharedPreferences 	prefs	= getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);
		RsaDbHelper 		mDb		= new RsaDbHelper(this, prefs.getString(RsaDb.ACTUALDBKEY, RsaDbHelper.DB_NAME1));
							db		= mDb.getReadableDatabase();
		Cursor 				cursor	= null;
		String 				query	= "select PHOTO from _shop where CUST_ID='"+mOrderH.cust_id
																  +"' AND ID='"+mOrderH.shop_id+"' limit 1";
		RsaDbHelper 		mDb_orders	= new RsaDbHelper(this, RsaDbHelper.DB_ORDERS);
		SQLiteDatabase 		db_orders	= mDb_orders.getReadableDatabase();
		Calendar 			c = Calendar.getInstance();
		SimpleDateFormat	sfm = new SimpleDateFormat("yyyy-MM-dd");
		String 				currentDay = sfm.format(c.getTime());
		c.add(Calendar.DATE, 1);
		String				nextDay	   = sfm.format(c.getTime());
		String				query2  = "select PHOTO from _geophoto where TIMESTAMP BETWEEN '"+ currentDay +"' " +
																  "AND '" + nextDay + "' " +
																  "AND CUST_ID='"+mOrderH.cust_id+"' " +
																  "AND SHOP_ID='"+mOrderH.shop_id+"' limit 1";
		String 				value;
		try {
			cursor = db.rawQuery(query,  null);
			if (cursor.moveToFirst()) {
				value = cursor.getString(0);
				if (value!=null && value.equals("1")) {
					result = " Фотография ТТ: В наличии (1С)";
				}
			}

			cursor = db_orders.rawQuery(query2, null);
			if (cursor.moveToFirst()) {
				value = cursor.getString(0);
				if (value!=null && value.length()>3) {
					result = " Фотография ТТ: Новая "+ currentDay;
				}
			}
		} catch (Exception e) {
			result = "";
		}


		if (cursor!=null && !cursor.isClosed()) {
			cursor.close();
		}
		if (db!=null && db.isOpen()) {
			db.close();
		}
		if (db_orders!=null && db_orders.isOpen()) {
			db_orders.close();
		}

		return result;
	}

	private String getShopGPSInfo() {
		String result = " Координаты ТТ: ОТСУТСТВУЮТ В 1С!";
		SQLiteDatabase 		db	    = null;
		SharedPreferences 	prefs	= getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);
		RsaDbHelper 		mDb		= new RsaDbHelper(this, prefs.getString(RsaDb.ACTUALDBKEY, RsaDbHelper.DB_NAME1));
							db		= mDb.getReadableDatabase();
		Cursor 				cursor	= null;
		String 				query	= "select GPS from _shop where CUST_ID='"+mOrderH.cust_id
																+"' AND ID='"+mOrderH.shop_id+"' limit 1";
		RsaDbHelper 		mDb_orders	= new RsaDbHelper(this, RsaDbHelper.DB_ORDERS);
		SQLiteDatabase 		db_orders	= mDb_orders.getReadableDatabase();
		Calendar 			c = Calendar.getInstance();
		SimpleDateFormat	sfm = new SimpleDateFormat("yyyy-MM-dd");
		String 				currentDay = sfm.format(c.getTime());
		c.add(Calendar.DATE, 1);
		String				nextDay	   = sfm.format(c.getTime());
		String				query2  = "select GPS from _geophoto where TIMESTAMP BETWEEN '"+ currentDay +"' " +
																  "AND '" + nextDay + "' " +
																  "AND CUST_ID='"+mOrderH.cust_id+"' " +
																  "AND SHOP_ID='"+mOrderH.shop_id+"' limit 1";
		String 				value;
		try {
			cursor = db.rawQuery(query,  null);
			if (cursor.moveToFirst()) {
				value = cursor.getString(0);
				if (value!=null && value.length()>3) {
					result = " Координаты ТТ: В наличии (1С)";
				}
			}

			cursor = db_orders.rawQuery(query2, null);
			if (cursor.moveToFirst()) {
				value = cursor.getString(0);
				if (value!=null && value.length()>3) {
					result = " Координаты ТТ: Новые "+ currentDay;
				}
			}
		} catch (Exception e) {
			result = "";
		}



		if (cursor!=null && !cursor.isClosed()) {
			cursor.close();
		}
		if (db!=null && db.isOpen()) {
			db.close();
		}
		if (db_orders!=null && db_orders.isOpen()) {
			db_orders.close();
		}
		return result;
	}


	private void bindCustomersSpinner() {
		//spnrCustomers.setEnabled(!usePlan);
		spnrCustomers.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

				String prevCustID	= mOrderH.cust_id.toString();
				mOrderH.cust_id		= listCustomers.get(arg2).get(ComboCustomerItem.ID);
				mOrderH.cust_text	= listCustomers.get(arg2).get(ComboCustomerItem.NAME);

                SharedPreferences def_prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                def_prefs.edit().putString("presavedCust", mOrderH.cust_id.toString()).commit();

				fillShopsFromDB(mOrderH.cust_id.toString(), mOrderH.cust_id.toString().equals(prevCustID)?mOrderH.shop_id.toString():null);

				if (usingQuest)
					calculateDebitByShop();
				else
					calculateDebit();

				fillComment();

			}
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {}
		});
		spnrCustomers.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				Intent intent = new Intent();
			    intent.setClass(getApplicationContext(), DebitActivity.class);
				intent.putExtra("ORDERH", mOrderH);
				startActivityForResult(intent, PICK_HEAD_REQUEST);
				return true;
			}
		});


	}

	private void bindShopsSpinner() {

		spnrShops.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

				mOrderH.shop_id		= listShops.get(arg2).get(ComboShopItem.ID);
				mOrderH.shop_text	= listShops.get(arg2).get(ComboShopItem.NAME);
                SharedPreferences def_prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                def_prefs.edit().putString("presavedShop", mOrderH.shop_id.toString()).commit();

				if (usingQuest)
					calculateDebitByShop();
				else
					calculateDebit();

				fillComment();
			}



			@Override
			public void onNothingSelected(AdapterView<?> arg0) {}
		});

		//spnrShops.setEnabled(!usePlan);
	}

	private void bindCustomToggle() {
		String[] cb_name = new String[4];
		cb_name[0] = def_prefs.getString("cb1", "");
		cb_name[1] = def_prefs.getString("cb2", "");
		cb_name[2] = def_prefs.getString("cb3", "");
		cb_name[3] = def_prefs.getString("cb4", "");

		if (!cb_name[0].equals("")) {
			tbtnCb1.setVisibility(View.VISIBLE);
			tbtnCb1.setTextOff(cb_name[0]);
			tbtnCb1.setTextOn(cb_name[0]);
			tbtnCb1.setText(cb_name[0]);
		}
		if (!cb_name[1].equals("")) {
			tbtnCb2.setVisibility(View.VISIBLE);
			tbtnCb2.setTextOff(cb_name[1]);
			tbtnCb2.setTextOn(cb_name[1]);
			tbtnCb2.setText(cb_name[1]);
		}
		if (!cb_name[2].equals("")) {
			tbtnCb3.setVisibility(View.VISIBLE);
			tbtnCb3.setTextOff(cb_name[2]);
			tbtnCb3.setTextOn(cb_name[2]);
			tbtnCb3.setText(cb_name[2]);
		}
		if (!cb_name[3].equals("")) {
			tbtnCb4.setVisibility(View.VISIBLE);
			tbtnCb4.setTextOff(cb_name[3]);
			tbtnCb4.setTextOn(cb_name[3]);
			tbtnCb4.setText(cb_name[3]);
		}

		tbtnCb1.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				int state = Integer.parseInt(mOrderH.visitid.toString().equals("")?"0":mOrderH.visitid.toString());
				if (isChecked) {
					state |= (state | 1);
				} else {
					state &= state | 14;
				}
				mOrderH.visitid = Integer.toString(state);
			}
		});
		tbtnCb2.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				int state = Integer.parseInt(mOrderH.visitid.toString().equals("")?"0":mOrderH.visitid.toString());
				if (isChecked) {
					state |= (state | 2);
				} else {
					state &= state | 13;
				}
				mOrderH.visitid = Integer.toString(state);
			}
		});
		tbtnCb3.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				int state = Integer.parseInt(mOrderH.visitid.toString().equals("")?"0":mOrderH.visitid.toString());
				if (isChecked) {
					state |= (state | 4);
				} else {
					state &= state | 11;
				}
				mOrderH.visitid = Integer.toString(state);
			}
		});
		tbtnCb4.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				int state = Integer.parseInt(mOrderH.visitid.toString().equals("")?"0":mOrderH.visitid.toString());
				if (isChecked) {
					state |= (state | 8);
				} else {
					state &= state | 7;
				}
				mOrderH.visitid = Integer.toString(state);
			}
		});
	}

	private void bindSkladToggle() {
		if (def_prefs.getBoolean("prefButton1", true)==false)
			tbtnSklad.setVisibility(View.GONE);
		String name = def_prefs.getString("prefButton1_name", "zxc");
		if (name.equals("zxc") == false) {
			tbtnSklad.setTextOn(name);
			tbtnSklad.setTextOff(name);
		}
		tbtnSklad.setOnClickListener(new OnClickListener(){
  			@Override
  			public void onClick(View arg0) {
  				tbtnSklad.setChecked(true);
  				if (isActivityFullyInitialized)
  					showDialog(IDD_SKLAD);
  		}});
	}

	private void bindPaytypeToggle() {
		if (def_prefs.getBoolean("prefButton2", true)==false)
			tbtnCash.setVisibility(View.GONE);
		String name = def_prefs.getString("prefButton2_name", "zxc");
		if (name.equals("zxc") == false) {
			tbtnCash.setTextOn(name);
			tbtnCash.setTextOff(name);
		}
		tbtnCash.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				mOrderH.paytype = isChecked?"Без":"Нал";
				fillComment();
			}
		});
	}

	private void bindVATToggle() {
		if (def_prefs.getBoolean("prefButton3", true)==false)
			tbtnVAT.setVisibility(View.GONE);
		String name = def_prefs.getString("prefButton3_name", "zxc");
		if (name.equals("zxc") == false) {
			tbtnVAT.setTextOn(name);
			tbtnVAT.setTextOff(name);
		}
		tbtnVAT.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				if (mOrderH.hndsrate.equals("1")) {
					mOrderH.hndsrate = "0";
					tbtnVAT.setChecked(true);
					tbtnVAT.setTextOn("НДС20");
					tbtnVAT.setText("НДС20");
				} else if (mOrderH.hndsrate.equals("0")) {
					mOrderH.hndsrate = "2";
					tbtnVAT.setChecked(true);
					tbtnVAT.setTextOn("НДС7");
					tbtnVAT.setText("НДС7");
				} else if (mOrderH.hndsrate.equals("2")) {
					mOrderH.hndsrate = "1";
					tbtnVAT.setChecked(false);
					tbtnVAT.setTextOff("бНДС");
					tbtnVAT.setText("бНДС");
				}
				fillComment();
			}
		});
	}

	private void bindDelayToggle() {
		if (def_prefs.getBoolean("prefButton4", true)==false)
			tbtnDelay.setVisibility(View.GONE);
		String name = def_prefs.getString("prefButton4_name", "zxc");
		if (name.equals("zxc") == false) {
			tbtnDelay.setTextOn(name);
			tbtnDelay.setTextOff(name);
		}
		tbtnDelay.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isActivityFullyInitialized)
					showDialog(IDD_DELAY);
			}
		});
	}

	private void bindDiscountToggle() {
		if (def_prefs.getBoolean("prefButton5", true)==false)
			tbtnDiscount.setVisibility(View.GONE);
		String name = def_prefs.getString("prefButton5_name", "zxc");
		if (name.equals("zxc") == false) {
			tbtnDiscount.setTextOn(name);
			tbtnDiscount.setTextOff(name);
		}
		if (def_prefs.getBoolean("usingFactCash", false)) {
			tbtnDiscount.setTextOn("ФАКТ");
			tbtnDiscount.setTextOff("ФАКТ");
		}
		tbtnDiscount.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isActivityFullyInitialized)
					showDialog(IDD_DISCOUNT);
			}
		});
	}

	private void bindDeliveryToggle() {
		if (def_prefs.getBoolean("prefButton6", true)==false)
			tbtnDelivery.setVisibility(View.GONE);
		String name = def_prefs.getString("prefButton6_name", "zxc");
		if (name.equals("zxc") == false) {
			tbtnDelivery.setTextOn(name);
			tbtnDelivery.setTextOff(name);
		}
		tbtnDelivery.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isActivityFullyInitialized)
					showDialog(IDD_DELIVERY);
					buttonView.setChecked(true);
			}
		});
	}

	private void bindPhotoToggle() {
		tbtnPhoto.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isActivityFullyInitialized) {
					buttonView.setChecked(true);
					openOptionsMenu();
				}
			}
		});
	}

	private void bindRemarkButton() {
		btnRemark.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isActivityFullyInitialized)
					showDialog(IDD_REMARK);
			}
		});
	}

	private void bindQuestButton() {
		btnQuest.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isActivityFullyInitialized) {
					Intent intent = new Intent();

					intent.setClass(getApplicationContext(), QuestActivity.class);
					intent.putExtra("ORDERH", mOrderH);
					startActivityForResult(intent, PICK_HEAD_REQUEST);
				}
			}
		});
	}

	private void bindCancelButton() {
		btnCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//removeQuestionnaire();
				saveQuests();
				if (usingGeoPhoto) saveGeophoto();
				Intent intent = new Intent();
				setResult(RESULT_CANCELED, intent);
				finish();
			}
		});
	}

	private boolean removeQuestionnaire() {
		boolean res = false;

		RsaDbHelper 	mDb_ord	  = new RsaDbHelper(getApplicationContext(), RsaDbHelper.DB_ORDERS);
		SQLiteDatabase  db	= mDb_ord.getReadableDatabase();

		Log.d("RRRR","deleting");

		//db.execSQL("delete from _quest where ZAKAZ_ID='new'");
		if (db.delete(RsaDbHelper.TABLE_QUEST, "ZAKAZ_ID='new'", null)>0)
			res = true;

		if (db!=null && db.isOpen()) {
			db.close();
		}

		return res;
	}

	private boolean removeGeophoto() {
		boolean res = false;

		RsaDbHelper 	mDb_ord	  = new RsaDbHelper(getApplicationContext(), RsaDbHelper.DB_ORDERS);
		SQLiteDatabase  db	= mDb_ord.getReadableDatabase();

		try {
		//db.execSQL("delete from _geophoto where ZAKAZ_ID='new'");
		if (db.delete(RsaDbHelper.TABLE_GEOPHOTO, "ZAKAZ_ID='new'", null)>0)
			res = true;
		} catch (Exception e) {}
		if (db!=null && db.isOpen()) {
			db.close();
		}

		return res;
	}

	private void bindLinesButton() {
		final Intent intent = new Intent();
		intent.setClass(this, LinesActivity.class);
		btnLines.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				intent.putExtra("ORDERH", mOrderH);
				intent.putExtra("FROMHEAD", true);
				startActivityForResult(intent, PICK_HEAD_REQUEST);
			}
		});
	}

	private void bindReturnCheckbox() {
		chkReturn.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (!chkReturn.isChecked()) {
					showDialog(IDD_RETURN);
				} else {
					mOrderH.num1c = "";
					fillComment();
				}
				return false;
			}
		});/*
		chkReturn.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				//mOrderH.num1c = isChecked?"1":"";
				if (isChecked) {
					showDialog(IDD_RETURN);
				} else {
					mOrderH.num1c = "";
					fillComment();
				}
			}
		});*/
	}

	private void bindOKButton() {
		btnOK.setOnClickListener(new DebouncedOnClickListener(2000) {
			@Override
			public void onDebouncedClick(View v) {
				if (saving_in_progress==true)
					return;

				saving_in_progress = true;

				if (usingGeoPhoto &&
						(getShopPhotoInfo().contains("ОТСУТ")
						 || getShopGPSInfo().contains("ОТСУТ"))) {
					saving_in_progress = false;
					showDialog(IDM_GEOPHOTO_ONSAVE);
					return;
				}

				Log.d("ROMKA", "OK Clisked, MODE="+ Integer.toString(mOrderH.mode));
				Log.d("ROMKA", saving_in_progress?"Saving in progress":"false");

				String block = mOrderH.block.toString();
				if (block.length()>0 && Integer.parseInt(block)==R.drawable.ic_locked_) {
					Toast.makeText(getApplicationContext(),R.string.head_notsaved,Toast.LENGTH_LONG).show();
					saving_in_progress = false;
					return;
				}
				if (allowEmptyOrders==false && mOrderH.lines.isEmpty()) {
					Toast.makeText(getApplicationContext(),R.string.head_linesisempty,Toast.LENGTH_SHORT).show();
					saving_in_progress = false;
					return;
				}
				try {
					fillOrderAdditionalData();
				} catch(Exception e) {
					writeErrLog("HeadActivity: fillOrderAdditionalData()", e.getMessage());
					Toast.makeText(getApplicationContext(),"Ощибка#1",Toast.LENGTH_SHORT).show();
					saving_in_progress = false;
					return;
				}
				if (mOrderH.mode==RSAActivity.IDM_MODIFY) {
					try {
						saveModifiedOrder();
					} catch (Exception e) {
						writeErrLog("HeadActivity: saveModifiedOrder()", e.getMessage());
						Toast.makeText(getApplicationContext(),"Ощибка#2",Toast.LENGTH_SHORT).show();
						saving_in_progress = false;
						return;
					}
				}
				else if (mOrderH.mode==RSAActivity.IDM_ADD) {
					try {
						saveAddedOrder();
					} catch (Exception e) {
						writeErrLog("HeadActivity: saveAddedOrder()", e.getMessage());
						Toast.makeText(getApplicationContext(),"Ощибка#3",Toast.LENGTH_SHORT).show();
						saving_in_progress = false;
						return;
					}
				}
				else {
					Toast.makeText(getApplicationContext(),"Не могу сохранить",Toast.LENGTH_LONG).show();
					saving_in_progress = false;
					return;
				}

				SharedPreferences prefs = getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE);
				prefs.edit().putString(RsaDb.LASTSKLADID,   mOrderH.sklad_id.toString()).commit();
				prefs.edit().putString(RsaDb.LASTSKLADNAME, mOrderH.sklad_text.toString()).commit();
				prefs.edit().putString(RsaDb.LASTVATKEY,   	mOrderH.hndsrate.toString()).commit();

				Intent intent = new Intent();
				setResult(RESULT_OK, intent);
				saving_in_progress = false;
				finish();
			}
		});
	}

	private DatePickerDialog.OnDateSetListener mDateSetListener =
            new DatePickerDialog.OnDateSetListener() {
				@Override
				public void onDateSet(DatePicker view, int year,
										int monthOfYear, int dayOfMonth) {
					SimpleDateFormat sfmt = new SimpleDateFormat("yyyy-MM-dd");
					Calendar c = Calendar.getInstance();
					c.set(year, monthOfYear, dayOfMonth);
			        deliveryDate =  sfmt.format(c.getTime());
			        mOrderH.delivery = deliveryDate;
			        fillComment();
				}
	};

	@Override
	protected Dialog onCreateDialog(int id) {
		switch(id) {
			case IDM_GEOPHOTO_ONSAVE:{
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("Не могу сохранить заказ!")
						.setMessage("В 1С не хватает информации по текущей ТТ (фотография и/или координаты).")
						.setCancelable(true)
						.setPositiveButton("Добавить информацию",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int id) {
										Intent intent = new Intent();

										intent.setClass(getApplicationContext(), GeophotoActivity.class);
										intent.putExtra("ORDERH", mOrderH);
										startActivityForResult(intent, PICK_HEAD_REQUEST);

										dialog.cancel();
									}
								})
						.setNegativeButton("Вернуться к заказу",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int id) {
										dialog.cancel();
									}
								});
				return builder.create();
			}
			case IDD_DELIVERY:{
				SimpleDateFormat sfmt = new SimpleDateFormat("yyyy-MM-dd");
				Calendar c = Calendar.getInstance();
				try {
					c.setTime(sfmt.parse(deliveryDate));
				} catch(Exception pp) {}
				return new DatePickerDialog(this, mDateSetListener,
											c.get(Calendar.YEAR),
											c.get(Calendar.MONTH),
											c.get(Calendar.DAY_OF_MONTH));
			}
			case IDD_SKLAD: {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(R.string.head_selectWH);
				if (listSklad==null)
					fillSkladsListFromDB(extras.getString("SKLADID"));
				builder.setAdapter(new SimpleAdapter(this, listSklad, lightTheme?android.R.layout.simple_list_item_1:R.layout.simple_list_item_1_black,
														              new String[] {ComboSkladItem.NAME},
														              new int[] {android.R.id.text1}),
								   new DialogInterface.OnClickListener(){
										@Override
										public void onClick(DialogInterface dialog, int which){
											String s = "";
											if (mOrderH != null
													&& mOrderH.sklad_text!=null
													&& !mOrderH.equals("")
													&& usingAgreement
													&& mOrderH.sklad_text.toString().contains("&&^^")) {
												s = "^^&&";
											}

							  			    mOrderH.sklad_id	= listSklad.get(which).get(ComboSkladItem.ID);
							  			    mOrderH.sklad_text 	= listSklad.get(which).get(ComboSkladItem.NAME)+s;
											fillComment();
										}
								   }
				);
				builder.setCancelable(true);
				return builder.create();
			}
			case IDD_SETTYPE: {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("Укажите тип ТТ");

				final List<HashMap<String, String>> listShopTypes = fillShopTypesListFromDB();

				builder.setAdapter(new SimpleAdapter(this, listShopTypes, lightTheme?android.R.layout.simple_list_item_1:R.layout.simple_list_item_1_black,
														              new String[] {"NAME"},
														              new int[] {android.R.id.text1}),
								   new DialogInterface.OnClickListener(){
										@Override
										public void onClick(DialogInterface dialog, int which){
							  			   Toast.makeText(getApplicationContext(),
							  					   "Установлен тип "+listShopTypes.get(which).get("NAME"), Toast.LENGTH_LONG).show();

							  			   int i = spnrCustomers.getSelectedItemPosition();
										   int s = spnrShops.getSelectedItemPosition();

							  			   String cid = listCustomers.get(i).get(ComboCustomerItem.ID);
							  			   String sid = listShops.get(s).get(ComboShopItem.ID);

							  			   updateShopType(cid, sid, listShopTypes.get(which).get("NAME"));
							  			   updateModified();
										}
								   }
				);
				builder.setCancelable(true);
				return builder.create();
			}
			case IDD_RETURN: {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("Возврат к заказу:");
				if (listOrders==null) fillOrdersList();

				builder.setAdapter(new SimpleAdapter(this, listOrders, R.layout.orderlist_item,
												              new String[] {OrdersListItem.DATE, OrdersListItem.NUM, OrdersListItem.SUM},
												              new int[] {R.id.orderlist_item1, R.id.orderlist_item3, R.id.orderlist_item2}),
								   new DialogInterface.OnClickListener(){
										@Override
										public void onClick(DialogInterface dialog, int which){
											mOrderH.num1c = listOrders.get(which).get(OrdersListItem.NUM);
											fillComment();
											chkReturn.setChecked(true);
										}
									}
				);
				builder.setCancelable(false);
				return builder.create();
			}
			case IDD_REMARK: {
				LayoutInflater		inflater	= getLayoutInflater();
				View 				layout		= inflater.inflate(R.layout.dlg_head, (ViewGroup)findViewById(R.id.linear_dlg_head));
				AlertDialog.Builder builder		= new AlertDialog.Builder(this);
				final EditText 		edtRemark	= (EditText)layout.findViewById(R.id.edtRemark_head);
				builder.setView(layout);

				builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						mOrderH.remark = edtRemark.getText();
						fillComment();
						InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				        imm.hideSoftInputFromWindow(edtRemark.getWindowToken(), 0);
					}
				});

				builder.setNeutralButton(R.string.head_clear, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						mOrderH.remark = "";
						fillComment();
						InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				        imm.hideSoftInputFromWindow(edtRemark.getWindowToken(), 0);
					}
				});

				builder.setNegativeButton(R.string.head_dlg_cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				        imm.hideSoftInputFromWindow(edtRemark.getWindowToken(), 0);
					}
				});

				builder.setCancelable(false);
				Dialog md = builder.create();
				md.requestWindowFeature(Window.FEATURE_NO_TITLE);
				return md;
			}
			case IDD_DELAY: {
				LayoutInflater		inflater	= getLayoutInflater();
				View 				layout		= inflater.inflate(R.layout.dlg_head, (ViewGroup)findViewById(R.id.linear_dlg_head));
				AlertDialog.Builder builder		= new AlertDialog.Builder(this);
				final EditText 		edtDelay	= (EditText)layout.findViewById(R.id.edtRemark_head);
				builder.setView(layout);

				builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						mOrderH.delay	= edtDelay.getText();
						if (mOrderH.delay.toString().length()==0 || mOrderH.delay.toString().equals("0")) {
							mOrderH.delay = "0";
							tbtnDelay.setChecked(false);
						} else {
							tbtnDelay.setChecked(true);
						}
						fillComment();
						InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				        imm.hideSoftInputFromWindow(edtDelay.getWindowToken(), 0);
					}
				});

				builder.setNeutralButton(R.string.head_clear, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						mOrderH.delay	= "0";
						tbtnDelay.setChecked(false);
						fillComment();
						InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				        imm.hideSoftInputFromWindow(edtDelay.getWindowToken(), 0);
					}
				});

				builder.setNegativeButton(R.string.head_dlg_cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (mOrderH.delay.toString().length()==0 || mOrderH.delay.toString().equals("0")) {
							mOrderH.delay = "0";
							tbtnDelay.setChecked(false);
						} else {
							tbtnDelay.setChecked(true);
						}
						InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				        imm.hideSoftInputFromWindow(edtDelay.getWindowToken(), 0);
					}
				});

				builder.setCancelable(false);
				Dialog md = builder.create();
				md.requestWindowFeature(Window.FEATURE_NO_TITLE);
				return md;
			}
			case IDD_DISCOUNT: {
				LayoutInflater		inflater	= getLayoutInflater();
				View 				layout		= inflater.inflate(R.layout.dlg_head, (ViewGroup)findViewById(R.id.linear_dlg_head));
				AlertDialog.Builder builder		= new AlertDialog.Builder(this);
				final EditText 		edtDiscount	= (EditText)layout.findViewById(R.id.edtRemark_head);
				builder.setView(layout);

				builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						mOrderH.id	= edtDiscount.getText();
						if (mOrderH.id.toString().length()==0 || mOrderH.id.toString().equals("0")) {
							mOrderH.id = "0";
							tbtnDiscount.setChecked(false);
						} else {
							tbtnDiscount.setChecked(true);
						}
						fillComment();
						InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				        imm.hideSoftInputFromWindow(edtDiscount.getWindowToken(), 0);
					}
				});

				builder.setNeutralButton(R.string.head_clear, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						mOrderH.id	= "0";
						tbtnDiscount.setChecked(false);
						fillComment();
						InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				        imm.hideSoftInputFromWindow(edtDiscount.getWindowToken(), 0);
					}
				});

				builder.setNegativeButton(R.string.head_dlg_cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (mOrderH.id.toString().length()==0 || mOrderH.id.toString().equals("0")) {
							mOrderH.id = "0";
							tbtnDiscount.setChecked(false);
						} else {
							tbtnDiscount.setChecked(true);
						}
						InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				        imm.hideSoftInputFromWindow(edtDiscount.getWindowToken(), 0);
					}
				});

				builder.setCancelable(false);
				Dialog md = builder.create();
				md.requestWindowFeature(Window.FEATURE_NO_TITLE);
				return md;
			}
			default: return null;
		}
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		switch(id) {
			case IDD_SKLAD: {
			} break;
			case IDD_RETURN: {
			} break;
			case IDD_REMARK: {
				final EditText	edtRemark 		= (EditText)dialog.findViewById(R.id.edtRemark_head);
				int 			maxLength 		= 40;

				SharedPreferences scr_prefs = getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE);
				String iFace = scr_prefs.getString(RsaDb.INTERFACEKEY, "DBF");
				SharedPreferences def_prefs = PreferenceManager.getDefaultSharedPreferences(this);
				boolean isLongComment = def_prefs.getBoolean("longComment", false);
				if (iFace.equals("XML")) {
					maxLength = 250;
				} else if (isLongComment) {
					maxLength = 200;
				}

				InputFilter[]	FilterArray		= new InputFilter[1];
								FilterArray[0]	= new InputFilter.LengthFilter(maxLength);
				edtRemark.setFilters(FilterArray);
			    edtRemark.setText(mOrderH==null?extras.getString("REMARK"):mOrderH.remark.toString());
			    edtRemark.setOnLongClickListener(new OnLongClickListener() {
					@Override
					public boolean onLongClick(View arg0) { return false; }});
				edtRemark.setOnTouchListener(new OnTouchListener(){
					@Override
					public boolean onTouch(View arg0, MotionEvent arg1) { return false; }});
				dialog.setOnShowListener(new OnShowListener() {
				    @Override
				    public void onShow(DialogInterface dialog) {
				        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				        imm.showSoftInput(edtRemark, InputMethodManager.SHOW_FORCED);
				        edtRemark.setSelection(0, edtRemark.getText().toString().length());
				    }});
			    dialog.setOnDismissListener(new OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						try {
							InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
							imm.hideSoftInputFromWindow(edtRemark.getWindowToken(), 0);
						} catch (Exception e) {}
					}});
			} break;
			case IDD_DELAY: {
				final EditText	edtDelay 		= (EditText)dialog.findViewById(R.id.edtRemark_head);
				edtDelay.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
				int 			maxLength 		= 3;
				InputFilter[]	FilterArray		= new InputFilter[1];
								FilterArray[0]	= new InputFilter.LengthFilter(maxLength);
				edtDelay.setFilters(FilterArray);

			    edtDelay.setText(mOrderH==null?extras.getString("DELAY"):mOrderH.delay.toString());
			    edtDelay.setOnLongClickListener(new OnLongClickListener() {
					@Override
					public boolean onLongClick(View arg0) { return false; }});
				edtDelay.setOnTouchListener(new OnTouchListener(){
					@Override
					public boolean onTouch(View arg0, MotionEvent arg1) { return false; }});
				dialog.setOnShowListener(new OnShowListener() {
				    @Override
				    public void onShow(DialogInterface dialog) {
				        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				        imm.showSoftInput(edtDelay, InputMethodManager.SHOW_FORCED);
				        edtDelay.setSelection(0, edtDelay.getText().toString().length());
				    }});
			    dialog.setOnDismissListener(new OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						try {
							InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
							imm.hideSoftInputFromWindow(edtDelay.getWindowToken(), 0);
						} catch (Exception e) {}
					}});
			} break;
			case IDD_DISCOUNT: {
				final EditText	edtDiscount		= (EditText)dialog.findViewById(R.id.edtRemark_head);

				if (def_prefs.getBoolean("usingFactCash", false)) {
					edtDiscount.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
				} else {
					edtDiscount.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
					int 			maxLength 		= 2;
					InputFilter[]	FilterArray		= new InputFilter[1];
									FilterArray[0]	= new InputFilter.LengthFilter(maxLength);
					edtDiscount.setFilters(FilterArray);
				}

			    edtDiscount.setText(mOrderH==null?extras.getString("DISCOUNT"):mOrderH.id.toString());
			    edtDiscount.setOnLongClickListener(new OnLongClickListener() {
					@Override
					public boolean onLongClick(View arg0) { return false; }});
				edtDiscount.setOnTouchListener(new OnTouchListener(){
					@Override
					public boolean onTouch(View arg0, MotionEvent arg1) { return false; }});
				dialog.setOnShowListener(new OnShowListener() {
				    @Override
				    public void onShow(DialogInterface dialog) {
				        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				        imm.showSoftInput(edtDiscount, InputMethodManager.SHOW_FORCED);
				        edtDiscount.setSelection(0, edtDiscount.getText().toString().length());
				    }});
			    dialog.setOnDismissListener(new OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						try {
							InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
							imm.hideSoftInputFromWindow(edtDiscount.getWindowToken(), 0);
						} catch (Exception e) {}
					}});
			} break;
			default:
		}
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	if (usingGeoPhoto) {
	    	menu.add(Menu.NONE, IDM_GEOPHOTO_REWRITE, Menu.NONE, "ТТ: Снять фото/GPS")
			.setIcon(R.drawable.ic_menu_info_details)
			.setAlphabeticShortcut('g');
    	}
	    menu.add(Menu.NONE, IDM_TOPSKU, Menu.NONE, R.string.head_topsku)
			.setIcon(R.drawable.ic_menu_mark)
			.setAlphabeticShortcut('t');
	    if (!usingGeoPhoto) {
	    	menu.add(Menu.NONE, IDM_TAKEPHOTO, Menu.NONE, R.string.head_takephoto)
				.setIcon(R.drawable.ic_menu_camera)
	    		.setAlphabeticShortcut('p');
    	}
    	menu.add(Menu.NONE, IDM_CUSTINFO, Menu.NONE, R.string.head_custinfo)
			.setIcon(R.drawable.ic_menu_info_details)
			.setAlphabeticShortcut('i');
    	if (!usingGeoPhoto) {
	    	menu.add(Menu.NONE, IDM_STATISTIC, Menu.NONE, R.string.head_statistics)
			.setIcon(R.drawable.ic_menu_info_details)
			.setAlphabeticShortcut('s');
    	}
    	menu.add(Menu.NONE, IDM_SETTYPE, Menu.NONE, "Указать тип ТТ")
		.setIcon(R.drawable.ic_menu_info_details)
		.setAlphabeticShortcut('o');
    	if (usingAgreement) {
	    	menu.add(Menu.NONE, IDM_AGREED, Menu.NONE, "Адрес согласован")
			.setIcon(R.drawable.ic_menu_info_details)
			.setAlphabeticShortcut('a');
    	}

    	return (super.onCreateOptionsMenu(menu));
    }

    @SuppressLint("DefaultLocale")
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
	    	case IDM_GEOPHOTO_REWRITE: {
	    		Intent intent = new Intent();

				intent.setClass(getApplicationContext(), GeophotoActivity.class);
				intent.putExtra("ORDERH", mOrderH);
				startActivityForResult(intent, PICK_HEAD_REQUEST);
			} break;
			case IDM_AGREED: {
				String s = mOrderH.sklad_text.toString();
				if (s.contains("^^&&")) {
					Toast.makeText(this, "Теперь адрес НЕ согласован!", Toast.LENGTH_LONG).show();
					mOrderH.sklad_text = s.replace("^^&&", "");
				} else {
					Toast.makeText(this, "Адрес согласован!", Toast.LENGTH_LONG).show();
					mOrderH.sklad_text = s + "^^&&";
				}
			} break;
			case IDM_TAKEPHOTO: {
				Intent takePictureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
				try {
					startActivityForResult(takePictureIntent, PICK_PHOTO_REQUEST);
				} catch (Exception e) {
					Toast.makeText(this, "Не могу вызвать фотоаппарат", Toast.LENGTH_LONG).show();
				}
			} break;
			case IDM_CUSTINFO: {
				Intent intent = new Intent();
				String[] arrayReports = getResources().getStringArray(R.array.Reports);
				ArrayList<String> elements = new ArrayList<String>();
					elements.add("Поле:");
					elements.add("Значение:");
					int i = spnrCustomers.getSelectedItemPosition();
					int s = spnrShops.getSelectedItemPosition();
					elements.add("Код 1С");
					elements.add(listCustomers.get(i).get(ComboCustomerItem.ID));
					elements.add("Наименование");
					elements.add(listCustomers.get(i).get(ComboCustomerItem.NAME));
					elements.add("Конт. лицо");
					elements.add(listCustomers.get(i).get(ComboCustomerItem.CONTACT));
					elements.add("Договор");
					elements.add(listCustomers.get(i).get(ComboCustomerItem.DOGOVOR));
					elements.add("Телефон");
					elements.add(listCustomers.get(i).get(ComboCustomerItem.TEL));
					elements.add("Адрес");
					elements.add(listCustomers.get(i).get(ComboCustomerItem.ADRES));
					elements.add("ОКПО");
					elements.add(listCustomers.get(i).get(ComboCustomerItem.OKPO));
					elements.add("ИНН");
					elements.add(listCustomers.get(i).get(ComboCustomerItem.INN));
					elements.add("Тип ТТ");
					elements.add(listShops.get(s).get(ComboShopItem.TYPE));
					elements.add("Комментарий");
					elements.add(listCustomers.get(i).get(ComboCustomerItem.COMMENT));

					String[] array_elements = new String[elements.size()];
					elements.toArray(array_elements);

					intent.setClass(this, ShowreportActivity.class);
					intent.putExtra("NAME", arrayReports[4]);
					intent.putExtra("ELEMENTS", array_elements);
					intent.putExtra("COLUMNS", 2);
				intent.putExtra("ORDERH", mOrderH);
				startActivityForResult(intent, PICK_HEAD_REQUEST);
			} break;
			case IDM_SETTYPE: {
				showDialog(IDD_SETTYPE);
			} break;
			case IDM_TOPSKU: {
				SharedPreferences 	prefs	= getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);
				RsaDbHelper 		mDb		= new RsaDbHelper(this, prefs.getString(RsaDb.ACTUALDBKEY, RsaDbHelper.DB_NAME1));
				SQLiteDatabase 		db		= mDb.getReadableDatabase();
				Intent intent = new Intent();
				String[] arrayReports = getResources().getStringArray(R.array.Reports);
				ArrayList<String> elements = new ArrayList<String>();
					elements.add("ТМЦ:");
					elements.add("Кол-во:");
				String q = "select ID, NAME from _goods where FLASH='1' order by NAME";
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
				for (OrderLines CurLine : mOrderH.lines) {
					int index = ReportActivity.isTop(arrayTopSku, CurLine.get(OrderLines.GOODS_ID), countTopSku);
					if (index>=0) {
						arrayTopSku[index][2] = CurLine.get(OrderLines.QTY);
					}
				}
				for (int i=0;i<countTopSku;i++) {
					elements.add(arrayTopSku[i][1]);
					elements.add("   " + arrayTopSku[i][2]);
				}
				String[] array_elements = new String[elements.size()];
				elements.toArray(array_elements);

				intent.setClass(this, ShowreportActivity.class);
				intent.putExtra("NAME", arrayReports[5]);
				intent.putExtra("ELEMENTS", array_elements);
				intent.putExtra("COLUMNS", 2);
				intent.putExtra("REP", 5);
				try {
					if (cTopSku!=null && !cTopSku.isClosed()) {
						cTopSku.close();
					}
					if (db!=null && db.isOpen()) {
		    	      	db.close();
		    	    }
				} catch (Exception e) {}

				intent.putExtra("ORDERH", mOrderH);
				startActivityForResult(intent, PICK_HEAD_REQUEST);
			} break;
			case IDM_STATISTIC: {
				SharedPreferences 	prefs	  = getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);
				RsaDbHelper 		mDb		  = new RsaDbHelper(this, prefs.getString(RsaDb.ACTUALDBKEY, RsaDbHelper.DB_NAME1));
				SQLiteDatabase 		db		  = mDb.getReadableDatabase();
				RsaDbHelper 		mDb_ord	  = new RsaDbHelper(this, RsaDbHelper.DB_ORDERS);
				SQLiteDatabase 		db_orders = mDb_ord.getReadableDatabase();
				Calendar c = Calendar.getInstance();
				String  cc = mOrderH.cust_id.toString();
				String  ss = mOrderH.shop_id.toString();
				String  dd = String.format( "%04d-%02d-01", c.get(Calendar.YEAR), c.get(Calendar.MONTH)+1 );
				BrandItem[] brandlist = new BrandItem[1];
				GoodItem[] goodslist = new GoodItem[1];
				float total = 0;
				try {
					String q = "select ID, NAME from _brand group by ID order by NAME";
					Cursor cursorB = db.rawQuery(q, null);
					int countOfBrands = cursorB.getCount();
					if (countOfBrands>0) {
						cursorB.moveToFirst();
						brandlist = new BrandItem[countOfBrands];
						for (int i=0;i<countOfBrands;i++) {
							brandlist[i] = new BrandItem(cursorB.getString(0), cursorB.getString(1));
							cursorB.moveToNext();
						}
					}
					if (cursorB != null) {
						cursorB.close();
					}
					//-----------------------------------------------
					q = "select ID, BRAND_ID from _goods group by ID";
					Cursor cursorG = db.rawQuery(q, null);
					int countOfGoods = cursorG.getCount();
					if (countOfGoods>0) {
						cursorG.moveToFirst();
						goodslist = new GoodItem[countOfGoods];
						for (int i=0;i<countOfGoods;i++) {
							goodslist[i] = new GoodItem(cursorG.getString(0), cursorG.getString(1));
							cursorG.moveToNext();
						}
					}
					if (cursorG != null) {
						cursorG.close();
					}
					// ----------------------------------------------
					q = 		"select GOODS_ID, SUM(SUMWNDS) from _lines " +
								"where ZAKAZ_ID in (select _id from _head " +
								"                   where SDATE > '"+ dd + "' " +
								"                   and CUST_ID='"+ cc + "' " +
								"                   and SHOP_ID='"+ ss + "' " +
								"					and (NUM1C = '' OR NUM1C = '0') ) " +
								"group by GOODS_ID";
					Cursor cursorL = db_orders.rawQuery(q, null);
					int countOfLines = cursorL.getCount();
					if (countOfLines>0) {
						cursorL.moveToFirst();
						for (int i=0;i<countOfLines;i++) {
							total += cursorL.getFloat(1);
							incrementBrand(	brandlist,
											findBrandId(goodslist, cursorL.getString(0)),
											cursorL.getFloat(1));
							cursorL.moveToNext();
						}
					}
					if (cursorL != null) {
						cursorL.close();
					}
					Intent intent = new Intent();
					ArrayList<String> elements = new ArrayList<String>();
					elements.add("Бренд:");
					elements.add("Сумма:");
					for (int i=0;i<countOfBrands;i++) {
						elements.add(brandlist[i].getName());
						elements.add(brandlist[i].getValue());
					}
					elements.add("Итого:");
					elements.add(String.format("%.2f",total).replace(',','.'));
					String[] array_elements = new String[elements.size()];
					elements.toArray(array_elements);
					intent.setClass(this, ShowreportActivity.class);
					intent.putExtra("NAME", "Отгрузки за тек. месяц");
					intent.putExtra("ELEMENTS", array_elements);
					intent.putExtra("COLUMNS", 2);
					if (cursorB!=null && !cursorB.isClosed()) {
						cursorB.close();
					}
					if (cursorG!=null && !cursorG.isClosed()) {
						cursorG.close();
					}
					if (cursorL!=null && !cursorL.isClosed()) {
						cursorL.close();
					}
					if (db!=null && db.isOpen()) {
		    	      	db.close();
		    	    }
					if (db_orders!=null && db_orders.isOpen()) {
		    	      	db_orders.close();
		    	    }

					intent.putExtra("ORDERH", mOrderH);
					startActivityForResult(intent, PICK_HEAD_REQUEST);
				} catch (Exception e) {}
			} break;
			default:
				return false;
		}

    	return true;
    }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		try {
			if (requestCode == PICK_HEAD_REQUEST) {
	        	if (resultCode == RESULT_OK) {
	        		updateFromResult(data.getExtras());
	        	}
	        }
		} catch (Exception sEx) {
			writeErrLog("HeadActivity: ActResult() 1", sEx.getMessage());
		}

		try {
	        if (requestCode==PICK_PHOTO_REQUEST && (resultCode == RESULT_OK)) {
	        	// - new start
	        	Uri newuri = null;
	        	int orient = 0;
	        	final ContentResolver cr = getContentResolver();
	        	final String[] p1 = new String[] {
	        	    MediaStore.Images.ImageColumns._ID,
	        	    MediaStore.Images.ImageColumns.DATE_TAKEN,
	        	    MediaStore.Images.ImageColumns.ORIENTATION
	        	};
	        	Cursor c1 = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, p1, null, null, p1[1] + " DESC");
	        	int imageID = 0;
	        	if ( c1.moveToFirst() ) {
	        	    String uristringpic = "content://media/external/images/media/" +c1.getInt(0);
	        	    imageID = c1.getInt(0);
	        	    newuri = Uri.parse(uristringpic);
	        	    orient = c1.getInt(2);
	        	}
	        	c1.close();

	        	// - end
	        	Uri imageUri = newuri;//data.getData();
	        	if (imageUri == null) {
	        		Toast.makeText(this, "Планшет не отдает фото!", Toast.LENGTH_LONG).show();
	        		return;
	        	}
	        	Bitmap photo = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
	        	String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
	        	String cust_name = "";
	        	if (getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE).getBoolean(RsaDb.SENDLINES, false)) {
	        		cust_name = "#" + mOrderH.cust_text.toString();
	        	}
	 	        String imageFileName = timeStamp + cust_name + "#" + mOrderH.cust_id + "@" + mOrderH.shop_id + "#";

	 	        Log.d("RRR","Orientation = "+Integer.toString(orient));
	 	        Matrix matrix = new Matrix();
	 	        matrix.postRotate(orient);
	 	        Bitmap rotatedBMP = Bitmap.createBitmap(photo, 0, 0, photo.getWidth(), photo.getHeight(), matrix, true);

	 	        OutputStream fOut = new FileOutputStream(SD_CARD_PATH+File.separator+imageFileName+".jpg");
	 	        rotatedBMP.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
        		fOut.flush();
        		fOut.close();

        		removeImage(imageID);
	        }
		} catch (Exception sEx) {
		}
	}

	@Override
	public void onBackPressed() {
	}

	@SuppressLint("DefaultLocale")
	private void calculateDebit() {
		SharedPreferences def_prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		boolean isAutocorrect = def_prefs.getBoolean("prefDebitAutocorrection", false);
		mOrderH.debit_actual	  		= 0;
		mOrderH.debit_total		  		= 0;
		Calendar 				c 		= Calendar.getInstance();
		String 			  		curDate = "2013-12-01";
		try {
		 			  			curDate = String.format( "%04d-%02d-%02d", c.get(Calendar.YEAR), c.get(Calendar.MONTH)+1, c.get(Calendar.DAY_OF_MONTH) );
		} catch (Exception e) {}

		String 				query 	= "select CUST_ID, SUM(SUM) from _debit where CUST_ID='"+mOrderH.cust_id+"' and CLOSED!='2' group by CUST_ID";
		SharedPreferences 	prefs	= getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);
		RsaDbHelper 		mDb		= new RsaDbHelper(this, prefs.getString(RsaDb.ACTUALDBKEY, RsaDbHelper.DB_NAME1));
		SQLiteDatabase 		db		= mDb.getReadableDatabase();
		Cursor				cursor	= db.rawQuery(query, null);
		float kassadet_total = 0;
		float kassadet_total_expired = 0;

		try {
			if (cursor.getCount()>0) {
				cursor.moveToFirst();



				if (isAutocorrect) {
					kassadet_total = getKassaDet(mOrderH.cust_id.toString());
					//kassadet_total_expired = getKassaDetExpired(mOrderH.cust_id.toString());
				}

				mOrderH.debit_total = Double.parseDouble((cursor.getString(1).replace(',', '.'))) - kassadet_total;
			}
			if (cursor!=null && !cursor.isClosed()) {
				cursor.close();
			}

			query = "select CUST_ID, SUM(SUM) from _debit where CLOSED!='2' and (CUST_ID='"+mOrderH.cust_id+"') and (strftime('%Y-%m-%d',DATEPP)<strftime('%Y-%m-%d','"+curDate+"')) " +
					"group by CUST_ID";
			cursor = db.rawQuery(query, null);
			if (cursor.getCount()>0) {
				cursor.moveToFirst();
				mOrderH.debit_actual = Double.parseDouble((cursor.getString(1).replace(',', '.'))) - kassadet_total_expired;
			}
		} catch (Exception e) {
			Log.d("zaga",e.getMessage());
		}

		if (cursor!=null && !cursor.isClosed()) {
			cursor.close();
		}
		if (db!=null && db.isOpen()) {
			db.close();
		}
	}

	private float getKassaDet(String c) {
		float result = 0;

		RsaDbHelper		mDb = new RsaDbHelper(getApplicationContext(), RsaDbHelper.DB_ORDERS);
        SQLiteDatabase	db 	= mDb.getReadableDatabase();
        Cursor			cur = null;

        try {
        	cur = db.rawQuery("select SUM(SUM) from _kassadet where CUST_ID='"+c+"'", null);
        } catch (Exception e) {}

        if (cur!=null && cur.moveToFirst()) {
        	result = Float.parseFloat( cur.getString(0)==null?"0":cur.getString(0));
        }

		if(cur!=null && !cur.isClosed()) {
			cur.close();
		}
		if (db!=null && db.isOpen()) {
			db.close();
		}
		return result;
	}




	@SuppressLint("DefaultLocale")
	private void calculateDebitByShop() {
		mOrderH.debit_actual	  		= 0;
		mOrderH.debit_total		  		= 0;
		Calendar 				c 		= Calendar.getInstance();
		String 			  		curDate = "2013-12-01";
		try {
		 			  			curDate = String.format( "%04d-%02d-%02d", c.get(Calendar.YEAR), c.get(Calendar.MONTH)+1, c.get(Calendar.DAY_OF_MONTH) );
		} catch (Exception e) {}

		String 				query 	= "select CUST_ID, SUM(SUM) from _debit where CUST_ID='"+mOrderH.cust_id+"' and CLOSED='1' group by CUST_ID";
		SharedPreferences 	prefs	= getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);
		RsaDbHelper 		mDb		= new RsaDbHelper(this, prefs.getString(RsaDb.ACTUALDBKEY, RsaDbHelper.DB_NAME1));
		SQLiteDatabase 		db		= mDb.getReadableDatabase();
		Cursor				cursor	= db.rawQuery(query, null);

		try {
			if (cursor.getCount()>0) {
				cursor.moveToFirst();
				mOrderH.debit_total = Double.parseDouble((cursor.getString(1).replace(',', '.')));
			}
			if (cursor!=null && !cursor.isClosed()) {
				cursor.close();
			}

			query = "select CUST_ID, SUM(SUM) from _debit where (CUST_ID='"+mOrderH.cust_id+"') and CLOSED='1' and (SHOP_ID='"+mOrderH.shop_id+"') group by SHOP_ID";
			cursor = db.rawQuery(query, null);
			if (cursor.getCount()>0) {
				cursor.moveToFirst();
				mOrderH.debit_actual = Double.parseDouble((cursor.getString(1).replace(',', '.')));
			}
		} catch (Exception e) {}

		if (cursor!=null && !cursor.isClosed()) {
			cursor.close();
		}
		if (db!=null && db.isOpen()) {
			db.close();
		}
	}

	@SuppressLint("DefaultLocale")
	private void fillOrderAdditionalData() {
		// mOrderH.date, mOrderH.sdate, mOrderH.time
		Calendar c		= Calendar.getInstance();
		String	 date	= "01.12.2013";
		String	 time	= "00:00";
		try {
			 	date	= String.format( "%02d.%02d.%02d", c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.MONTH)+1, c.get(Calendar.YEAR) );
			 	time	= String.format( "%02d:%02d", c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE) );
		} catch(Exception e) {}
		mOrderH.date 	= date;
		mOrderH.time	= time;

		// mOrderH.gpscoord
		if (useGPS==true) {
			LocationManager lm 		= (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
			long[]			l		= GpsUtils.getGPS(lm);
			mOrderH.gpscoord 		= l==null?"1":Long.toString(l[0])+" "+Long.toString(l[1]);
		} else {
			mOrderH.gpscoord = "0";
		}

		// mOrderH.num1c
		// mOrderH.num1c = chkReturn.isChecked()?"1":"0";

		// mOrderH.numful
		String gpsStatus = "";
		try{
			String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
		    if((!provider.contains("gps"))&&(useGPS)) gpsStatus = "_G";
		} catch (Exception e) {}
		mOrderH.numfull = "_" + date + "_" + time + gpsStatus;

		// mOrderH.routecode
		mOrderH.routecode = routecode;

		// mOrderH.sended
		mOrderH.sended="0";

		// mOrderH.zakaz_id
		mOrderH.zakaz_id = "";

		// mOrderH.hvolume for CSV export
		int sec = 10 + (int)(Math.random() * ((59 - 10) + 1));
		mOrderH.hvolume = Integer.toString(sec);


		// mOrderH.visitid
		// mOrderH.visitid = "";

		// mOrderH.id
		if (mOrderH.id.equals(""))
			mOrderH.id = "0";



		// mOrderH.hsumo, mOrderH.disc, mOrderH.hwght, mOrderH.hweight
		float	hsumo  = 0;
		float	disc   = 0;
		float	hwght  = 0;
		float	fDsc   = 0;
		String	strDsc = mOrderH.id.toString();
		try {
			fDsc = Float.parseFloat(strDsc);
		} catch (Exception e) {};

		for (OrderLines CurLine : mOrderH.lines) {
			try {
				float __w = Float.parseFloat(CurLine.get(OrderLines.WEIGHT));
				float __q = Float.parseFloat(CurLine.get(OrderLines.QTY));
				hwght += __w*__q;
			} catch (Exception e) {}
			try {
				hsumo += Float.parseFloat(CurLine.get(OrderLines.SUMWNDS));
			} catch (Exception e) {}
		}
		disc = hsumo*(fDsc/100);
		hsumo = hsumo-disc;
		try {
			mOrderH.hsumo		= String.format("%.2f", hsumo).replace(',', '.');
			mOrderH.sumwonds	= String.format("%.2f", (hsumo/1.2F)).replace(',', '.');
			mOrderH.hnds		= String.format("%.2f", (hsumo - (hsumo/1.2F))).replace(',', '.');
			mOrderH.hweight		= String.format("%.3f", hwght).replace(',', '.');
		} catch (Exception e) {
			mOrderH.hsumo		= "0.00";
			mOrderH.sumwonds	= "0.00";
			mOrderH.hnds		= "0.00";
			mOrderH.hweight		= "0.000";
		}
	}

	private void saveAddedOrder() {
		saveHead();
		saveLines();
		saveRests();
		saveQuests();
		if (usingGeoPhoto) saveGeophoto();
		try {
			updateStocksOrderAdded();
		} catch (Exception e) {
			writeErrLog("HeadActivity: updateStocksOrderAdded()", e.getMessage());
		}
	}

	private void saveModifiedOrder() {
		saveHead();
		// update stock moved to saveHead() and called only if MODE=MODIFY
		saveLines();
		saveRests();
		saveQuests();
		if (usingGeoPhoto) saveGeophoto();
	}

	private void saveHead() {
		Log.d("ROMKA", "saveHead, MODE="+ Integer.toString(mOrderH.mode));
		Calendar 	c	  = Calendar.getInstance();
		String 		sdate = "2013-12-01";
		int 		block = 0;
		long		res	  = -1;
		try {
			sdate = String.format( "%04d-%02d-%02d", c.get(Calendar.YEAR), c.get(Calendar.MONTH)+1, c.get(Calendar.DAY_OF_MONTH) );
		} catch(Exception e) {}
		try {
			block = Integer.parseInt(mOrderH.block.toString());
		} catch (Exception e) {}

		int customButtonsState = 0; // all off

		customButtonsState |= tbtnCb1.isChecked()?1:0;
		customButtonsState |= tbtnCb2.isChecked()?2:0;
		customButtonsState |= tbtnCb3.isChecked()?4:0;
		customButtonsState |= tbtnCb4.isChecked()?8:0;
		mOrderH.visitid = Integer.toString(customButtonsState);

		ContentValues values = new ContentValues();
		values.put(RsaDbHelper.HEAD_ID,			mOrderH.id.toString());
		values.put(RsaDbHelper.HEAD_ZAKAZ_ID,	mOrderH.zakaz_id.toString());
		values.put(RsaDbHelper.HEAD_CUST_ID,	mOrderH.cust_id.toString());
		values.put(RsaDbHelper.HEAD_SHOP_ID,	mOrderH.shop_id.toString());
		values.put(RsaDbHelper.HEAD_SKLAD_ID,	mOrderH.sklad_id.toString());
		values.put(RsaDbHelper.HEAD_BLOCK,	    block);
		values.put(RsaDbHelper.HEAD_SENDED,		mOrderH.sended.toString());
		values.put(RsaDbHelper.HEAD_CUST_TEXT,	mOrderH.cust_text.toString());
		values.put(RsaDbHelper.HEAD_SHOP_TEXT,	mOrderH.shop_text.toString());
		values.put(RsaDbHelper.HEAD_SKLAD_TEXT,	mOrderH.sklad_text.toString());
		values.put(RsaDbHelper.HEAD_DELAY,		mOrderH.delay.toString());
		values.put(RsaDbHelper.HEAD_PAYTYPE,	mOrderH.paytype.toString());
		values.put(RsaDbHelper.HEAD_HSUMO,		mOrderH.hsumo.toString());
		values.put(RsaDbHelper.HEAD_HWEIGHT,	mOrderH.hweight.toString());
		values.put(RsaDbHelper.HEAD_HVOLUME,	mOrderH.hvolume.toString());
		if (mOrderH.mode==RSAActivity.IDM_ADD) {
			values.put(RsaDbHelper.HEAD_DATE,		mOrderH.date.toString());
			values.put(RsaDbHelper.HEAD_TIME,		mOrderH.time.toString());
			values.put(RsaDbHelper.HEAD_SDATE,		sdate);
			values.put(RsaDbHelper.HEAD_NUMFULL,	mOrderH.numfull.toString());
			values.put(RsaDbHelper.HEAD_GPSCOORD,	mOrderH.gpscoord.toString());
		}
		values.put(RsaDbHelper.HEAD_HNDS,		mOrderH.hnds.toString());
		values.put(RsaDbHelper.HEAD_HNDSRATE,	mOrderH.hndsrate.toString());
		values.put(RsaDbHelper.HEAD_SUMWONDS,	mOrderH.sumwonds.toString());
		values.put(RsaDbHelper.HEAD_NUM1C,		mOrderH.num1c.toString());
		values.put(RsaDbHelper.HEAD_REMARK, 	mOrderH.remark.toString());
		values.put(RsaDbHelper.HEAD_ROUTECODE,	mOrderH.routecode.toString());
		values.put(RsaDbHelper.HEAD_VISITID,	mOrderH.visitid.toString());
		values.put(RsaDbHelper.HEAD_DELIVERY,	mOrderH.delivery.toString());

		RsaDbHelper		mDb = new RsaDbHelper(getApplicationContext(), RsaDbHelper.DB_ORDERS);
        SQLiteDatabase	db 	= mDb.getWritableDatabase();

        if (mOrderH.mode==RSAActivity.IDM_ADD) {
        	Log.d("ROMKA", "insert, MODE="+ Integer.toString(mOrderH.mode));
        	res	= db.insert(RsaDbHelper.TABLE_HEAD, RsaDbHelper.HEAD_CUST_TEXT, values);
        	mOrderH._id = Long.toString(res);
        	Intent myIntent = new Intent(RsaGpsService.NEW_ORDER);
    		myIntent.putExtra(RsaDb.MAKESTARTKEY, true);
    		sendBroadcast(myIntent);
        } else if (mOrderH.mode==RSAActivity.IDM_MODIFY) {
        	db.update(RsaDbHelper.TABLE_HEAD, values, "_id='"+ mOrderH._id +"'", null);
        	try {updateStocksOrderModified();} catch (Exception e) {writeErrLog("HeadActivity: updateStocksOrderModified()", e.getMessage());}
        	db.delete(RsaDbHelper.TABLE_LINES, RsaDbHelper.LINES_ZAKAZ_ID + "='"+ mOrderH._id +"'", null);
			db.delete(RsaDbHelper.TABLE_RESTS, RsaDbHelper.RESTS_ZAKAZ_ID + "='"+ mOrderH._id +"'", null);
        } else {
        	writeErrLog("HeadActivity: saveHead()", "Undefined mode");
        }
        if (db!=null && db.isOpen()) {
			db.close();
		}
	}

	private void saveLines() {
		ContentValues values = new ContentValues();
		RsaDbHelper		mDb = new RsaDbHelper(getApplicationContext(), RsaDbHelper.DB_ORDERS);
        SQLiteDatabase	db 	= mDb.getWritableDatabase();
        db.beginTransaction();
        try {
			for (OrderLines CurLine : mOrderH.lines) {
				values.clear();
				values.put(RsaDbHelper.LINES_ID,			CurLine.get(OrderLines.ID) );
				values.put(RsaDbHelper.LINES_ZAKAZ_ID,		mOrderH._id );
				values.put(RsaDbHelper.LINES_GOODS_ID,		CurLine.get(OrderLines.GOODS_ID) );
				values.put(RsaDbHelper.LINES_TEXT_GOODS,	CurLine.get(OrderLines.TEXT_GOODS) );
				values.put(RsaDbHelper.LINES_RESTCUST,		CurLine.get(OrderLines.RESTCUST) );
				values.put(RsaDbHelper.LINES_QTY,			CurLine.get(OrderLines.QTY) );
				values.put(RsaDbHelper.LINES_UN,			CurLine.get(OrderLines.UN) );
				values.put(RsaDbHelper.LINES_COEFF,			CurLine.get(OrderLines.COEFF) );
				values.put(RsaDbHelper.LINES_DISCOUNT,		CurLine.get(OrderLines.DISCOUNT) );
				values.put(RsaDbHelper.LINES_PRICEWNDS,		CurLine.get(OrderLines.PRICEWNDS) );
				values.put(RsaDbHelper.LINES_SUMWNDS,		CurLine.get(OrderLines.SUMWNDS) );
				values.put(RsaDbHelper.LINES_PRICEWONDS,	CurLine.get(OrderLines.PRICEWONDS) );
				values.put(RsaDbHelper.LINES_SUMWONDS,		CurLine.get(OrderLines.SUMWONDS) );
				values.put(RsaDbHelper.LINES_NDS,			CurLine.get(OrderLines.NDS) );
				values.put(RsaDbHelper.LINES_DELAY,			CurLine.get(OrderLines.DELAY) );
				values.put(RsaDbHelper.LINES_WEIGHT,		CurLine.get(OrderLines.WEIGHT) );
				values.put(RsaDbHelper.LINES_COMMENT,		CurLine.get(OrderLines.COMMENT) );
				values.put(RsaDbHelper.LINES_BRAND_NAME,	CurLine.get(OrderLines.BRAND_NAME) );
				db.insert(RsaDbHelper.TABLE_LINES, RsaDbHelper.LINES_TEXT_GOODS, values);
			}
			db.setTransactionSuccessful();
        } catch (Exception e) {
        	writeErrLog("HeadActivity: saveLines()", e.getMessage());
        } finally {
        	db.endTransaction();
        }
		if (db!=null && db.isOpen()) {
			db.close();
		}
	}

	private void saveRests() {
		ContentValues values = new ContentValues();
		RsaDbHelper		mDb = new RsaDbHelper(getApplicationContext(), RsaDbHelper.DB_ORDERS);
        SQLiteDatabase	db 	= mDb.getWritableDatabase();
        db.beginTransaction();
        try {
			for (OrderRests CurRestLine : mOrderH.restslines) {
				values.clear();
				values.put(RsaDbHelper.RESTS_ID,			CurRestLine.get(OrderRests.ID) );
				values.put(RsaDbHelper.RESTS_ZAKAZ_ID,		mOrderH._id );
				values.put(RsaDbHelper.RESTS_GOODS_ID,		CurRestLine.get(OrderRests.GOODS_ID) );
				values.put(RsaDbHelper.RESTS_RESTQTY,		CurRestLine.get(OrderRests.RESTQTY) );
				values.put(RsaDbHelper.RESTS_RECQTY,		CurRestLine.get(OrderRests.RECQTY) );
				values.put(RsaDbHelper.RESTS_QTY,			mOrderH.getQTYbyGoodsIDfromLines(CurRestLine.get(OrderRests.GOODS_ID)) );
				db.insert(RsaDbHelper.TABLE_RESTS, RsaDbHelper.RESTS_ID, values);
			}
			db.setTransactionSuccessful();
        } catch (Exception e) {
        	writeErrLog("HeadActivity: saveRests()", e.getMessage());
        } finally {
        	db.endTransaction();
        }
		if (db!=null && db.isOpen()) {
			db.close();
		}
	}

	private void saveQuests() {
		ContentValues values = new ContentValues();
		RsaDbHelper		mDb = new RsaDbHelper(getApplicationContext(), RsaDbHelper.DB_ORDERS);
        SQLiteDatabase	db 	= mDb.getWritableDatabase();
        try {
				//values.put(RsaDbHelper.RESTS_ZAKAZ_ID,		mOrderH._id );
        		db.execSQL("update _quest set ZAKAZ_ID='"+mOrderH._id+"' where ZAKAZ_ID='new'");
		} catch (Exception e) {}

		if (db!=null && db.isOpen()) {
			db.close();
		}
	}

	private void saveGeophoto() {

		ContentValues values = new ContentValues();
		RsaDbHelper		mDb = new RsaDbHelper(getApplicationContext(), RsaDbHelper.DB_ORDERS);
        SQLiteDatabase	db 	= mDb.getWritableDatabase();
        try {
				//values.put(RsaDbHelper.RESTS_ZAKAZ_ID,		mOrderH._id );
        		db.execSQL("update _geophoto set ZAKAZ_ID='"+mOrderH._id+"' where ZAKAZ_ID='new'");
		} catch (Exception e) {}

		if (db!=null && db.isOpen()) {
			db.close();
		}
	}

	private void updateStocksOrderAdded() {
		SharedPreferences 	prefs	= getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);
		RsaDbHelper			mDb 	= new RsaDbHelper(this, prefs.getString(RsaDb.ACTUALDBKEY, RsaDbHelper.DB_NAME1));
        SQLiteDatabase		db 		= mDb.getWritableDatabase();
        Cursor				cursor	= null;
        StringBuilder		query	= new StringBuilder();

        if (mOrderH.lines.size()<1) return;

        query.append("select ID, REST from _goods where ID in ('"+mOrderH.lines.get(0).get(OrderLines.GOODS_ID)+"'");
        for (int i=1;i<mOrderH.lines.size();i++)
        	query.append(", '"+mOrderH.lines.get(i).get(OrderLines.GOODS_ID)+"'");
        query.append(") group by ID");

        cursor = db.rawQuery(query.toString(), null);
        if (cursor.getCount()>0) {
	        db.beginTransaction();
	        try {
	        	ContentValues values = new ContentValues();
		        while (cursor.moveToNext()) {
		        	float 	old_rest = 0;
		        	float	new_rest = 0;
		        	String	id		 = cursor.getString(0);
		        	try {old_rest = cursor.getFloat(1);} catch (Exception e) {}
		        	new_rest = old_rest - getRestByID(id);
		        	values.clear();
		        	values.put(RsaDbHelper.GOODS_REST,	Float.toString(new_rest));
		        	db.update(RsaDbHelper.TABLE_GOODS, values, RsaDbHelper.GOODS_ID+"='"+id+"'", null);
		        }
		        db.setTransactionSuccessful();
	        } catch (Exception e) {}
	        finally {
	        	db.endTransaction();
	        }
        }

        if (cursor!=null && !cursor.isClosed()) {
			cursor.close();
		}
		if (db!=null && db.isOpen()) {
			db.close();
		}
	}

	private void updateStocksOrderModified() {
		SharedPreferences 	prefs	= getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);
		RsaDbHelper			mDb 	= new RsaDbHelper(this, prefs.getString(RsaDb.ACTUALDBKEY, RsaDbHelper.DB_NAME1));
        SQLiteDatabase		db 		= mDb.getWritableDatabase();
        Cursor				cursor	= null;
        StringBuilder		query	= new StringBuilder();

        if (mOrderH.lines.size()<1) return;

        query.append("select ID, REST from _goods where ID in ('"+mOrderH.lines.get(0).get(OrderLines.GOODS_ID)+"'");
        for (int i=1;i<mOrderH.lines.size();i++)
        	query.append(", '"+mOrderH.lines.get(i).get(OrderLines.GOODS_ID)+"'");
        query.append(") group by ID");

        cursor = db.rawQuery(query.toString(), null);
        if (cursor.getCount()>0) {
	        db.beginTransaction();
	        try {
	        	ContentValues values = new ContentValues();
		        while (cursor.moveToNext()) {
		        	float 	old_rest = 0;
		        	float	new_rest = 0;
		        	String	id		 = cursor.getString(0);
		        	try {old_rest = cursor.getFloat(1);} catch (Exception e) {}
		        	new_rest = old_rest - getRestByID(id) + getRestByIDFromPreviuos(id);
		        	values.clear();
		        	values.put(RsaDbHelper.GOODS_REST,	Float.toString(new_rest));
		        	db.update(RsaDbHelper.TABLE_GOODS, values, RsaDbHelper.GOODS_ID+"='"+id+"'", null);
		        }
		        db.setTransactionSuccessful();
	        } catch (Exception e) {}
	        finally {
	        	db.endTransaction();
	        }
        }

        if (cursor!=null && !cursor.isClosed()) {
			cursor.close();
		}
		if (db!=null && db.isOpen()) {
			db.close();
		}
	}

	private int findCustIndexByID(String item_id) {
		for (int i=0;i<listCustomers.size();i++) {
			if (listCustomers.get(i).get(ComboCustomerItem.ID).equals(item_id))
				return i;
		}
		return 0;
	}

	private int findShopIndexByID(String item_id) {
		for (int i=0;i<listShops.size();i++) {
			if (listShops.get(i).get(ComboShopItem.ID).equals(item_id))
				return i;
		}
		return 0;
	}

	private float getRestByID(String id) {
		float res = 0;
		for (OrderLines CurLine : mOrderH.lines) {
			if (CurLine.get(OrderLines.GOODS_ID).equals(id)) {
				float coeff = 1;
				try {
					coeff = Float.parseFloat(CurLine.get(OrderLines.COEFF));
				} catch (Exception e) {}
				return coeff*Float.parseFloat(CurLine.get(OrderLines.QTY));
			}
		}
		return res;
	}

	private float getRestByIDFromPreviuos(String id) {
		float res = 0;
		String				query	= "select GOODS_ID, QTY from _lines where ZAKAZ_ID='"+mOrderH._id+"' " +
									  "and GOODS_ID='"+id+"' group by GOODS_ID LIMIT 1";
		RsaDbHelper 		mDb		= new RsaDbHelper(this, RsaDbHelper.DB_ORDERS);
		SQLiteDatabase 		db		= mDb.getReadableDatabase();
		Cursor				cursor	= db.rawQuery(query, null);

		try {
			if (cursor.getCount()>0) {
				cursor.moveToFirst();
				return cursor.getFloat(1);
			}
		} catch (Exception e) {}

		if (cursor!=null && !cursor.isClosed()) {
			cursor.close();
		}
		if (db!=null && db.isOpen()) {
			db.close();
		}
		return res;
	}

	public static String findBrandId(GoodItem[] gItem, String gId) {
    	for (int i=0;i<gItem.length;i++) {
    		if (gItem[i].getId().equals(gId)) {
    			return gItem[i].getBrandId();
    		}
    	}
    	return "";
    }

	private boolean removeDublicatesFromLines() {
    	boolean res = false;
    	String	id;
    	for (int i=0;i<mOrderH.lines.size();i++) {
    		id = mOrderH.lines.get(i).get(OrderLines.GOODS_ID);
    		if (!id.equals("")) {
    			for (int k=i+1;k<mOrderH.lines.size();k++) {
    				if (mOrderH.lines.get(k).get(OrderLines.GOODS_ID).equals(id)) {
    					res = true;
    					mOrderH.lines.remove(k);
    				}
    			}
    		} else {
    			mOrderH.lines.remove(i);
    		}
    	}
    	return res;
    }

	public static void incrementBrand(BrandItem[] bItem, String bId, float value) {
    	for (int i=0;i<bItem.length;i++) {
    		if (bItem[i].getId().equals(bId)) {
    			bItem[i].incValue(value);
    			return;
    		}
    	}
    }

	@SuppressLint("SimpleDateFormat")
	private void writeErrLog(String mess, String ex) {
		try {
	    	SD_CARD_PATH = Environment.getExternalStorageDirectory().toString() + File.separator + "rsa";
	    	SD_CARD_PATH = SD_CARD_PATH + File.separator + "error_log.txt";
	    	// get current time
	    	Calendar c = Calendar.getInstance();
	    	SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	    	// init error msg
	    	StringBuilder msgError = new StringBuilder("\r\n---"+ fmt.format(c.getTime()) +"----------------------\r\n");
	    	msgError.append(mess);
	    	msgError.append("\r\nException text:\r\n");
	    	msgError.append(ex + "\r\n");

	    	BufferedWriter out = new BufferedWriter(new FileWriter(SD_CARD_PATH, true));
	    	out.write(msgError.toString());
	    	out.close();
    	} catch (Exception e) {}
    }

	@SuppressLint("SimpleDateFormat")
	private File createImageFile() throws IOException {
	       File path = new File(SD_CARD_PATH);

	       String timeStamp =
	            new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
	        String imageFileName = timeStamp + "#" + mOrderH.cust_id + "@" + mOrderH.shop_id + "#";

	        // if VIM-SERVICE then use another filename
	        if (getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE).getBoolean(RsaDb.SENDLINES, false)) {
	        	imageFileName = timeStamp + "#" + mOrderH.cust_text + "#" + mOrderH.cust_id + "@" + mOrderH.shop_id + "#";
	        }

	        File image = File.createTempFile(
	            imageFileName,
	            JPEG_FILE_SUFFIX,
	            path //Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
	        );
	        mCurrentPhotoPath = image.getAbsolutePath();

	        return image;
	}

	private int getLastImageId2(){
        final String[] imageColumns = { MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA };
        final String imageOrderBy = MediaStore.Images.Media._ID+" DESC";
        Cursor imageCursor = managedQuery(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageColumns, null, null, imageOrderBy);

        if(imageCursor.moveToFirst()){
            int id = imageCursor.getInt(imageCursor.getColumnIndex(MediaStore.Images.Media._ID));
            //String fullPath = imageCursor.getString(imageCursor.getColumnIndex(MediaStore.Images.Media.DATA));
            if (verOS < 4) {
            	imageCursor.close();
            }
            return id;
        }else{
            return 0;
        }
    }

    private void removeImage(int id) {
 	   ContentResolver cr = getContentResolver();
 	   cr.delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, MediaStore.Images.Media._ID + "=?", new String[]{ Long.toString(id) } );
    }

	private Bitmap getBitmap(String path) {

	 	File mFile = new File(path);

	 	InputStream in = null;
	 	try {
	 	    final int IMAGE_MAX_SIZE = 480000; // 1.2MP
	 	    in = new FileInputStream(mFile);

	 	    // Decode image size
	 	    BitmapFactory.Options o = new BitmapFactory.Options();
	 	    o.inJustDecodeBounds = true;
	 	    BitmapFactory.decodeStream(in, null, o);
	 	    in.close();

	 	    int scale = 1;
	 	    while ((o.outWidth * o.outHeight) * (1 / Math.pow(scale, 2)) >
	 	          IMAGE_MAX_SIZE) {
	 	       scale++;
	 	    }

	 	    Bitmap b = null;
	 	    in = new FileInputStream(mFile);
	 	    if (scale > 1) {
	 	        scale--;
	 	        // scale to max possible inSampleSize that still yields an image
	 	        // larger than target
	 	        o = new BitmapFactory.Options();
	 	        o.inSampleSize = scale;
	 	        b = BitmapFactory.decodeStream(in, null, o);

	 	        // resize to desired dimensions
	 	        int height = b.getHeight();
	 	        int width = b.getWidth();

	 	        double y = Math.sqrt(IMAGE_MAX_SIZE
	 	                / (((double) width) / height));
	 	        double x = (y / height) * width;

	 	        Bitmap scaledBitmap = Bitmap.createScaledBitmap(b, (int) x,
	 	           (int) y, true);
	 	        b.recycle();
	 	        b = scaledBitmap;

	 	        System.gc();
	 	    } else {
	 	        b = BitmapFactory.decodeStream(in);
	 	    }
	 	    in.close();

	 	    return b;
	 	} catch (IOException e) {
	 	    return null;
	 	}
	 }

    private String getPhotoCount() {
    	File dir = new File(SD_CARD_PATH);
    	FilenameFilter imFilter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				return filename.endsWith(".jpg");
			}
		};
    	File[] fileList = dir.listFiles(imFilter);
    	MerchImages imgList = new MerchImages(fileList);

    	return Integer.toString( imgList.getCount(Calendar.getInstance(),
    												mOrderH.cust_id.toString(),
    												mOrderH.shop_id.toString())		);
    }

	synchronized public Outlet getLastOutletFromPrefs() {
		Outlet o = null;
		SharedPreferences def_prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		String custId = def_prefs.getString("presavedCust", "-XXX");
		String shopId = def_prefs.getString("presavedShop", "-XXX");
		if (!custId.equals("-XXX")) {
			o = new Outlet(custId, shopId);
		}
		return o;
	}

    synchronized public Outlet getLastOutlet() {
		Outlet o = null;

		RsaDbHelper 	mDb_ord	  = new RsaDbHelper(this, RsaDbHelper.DB_ORDERS);
		SQLiteDatabase db_orders  = mDb_ord.getReadableDatabase();
		Cursor			cursor = null;
		Calendar		c = Calendar.getInstance();
		SimpleDateFormat	fmt	= new SimpleDateFormat("yyyy-MM-dd");
		String 			today = fmt.format(c.getTime());

		try {
			cursor = db_orders.rawQuery("select CUST_ID, SHOP_ID from _head where SDATE='"+today+"' order by _id desc limit 1", null);
			if (cursor.moveToFirst()) {
				o = new Outlet(cursor.getString(0), cursor.getString(1));
			}
		} catch (Exception e) {}

		if (cursor!=null && !cursor.isClosed()) {
			cursor.close();
		}
		if (db_orders!=null && db_orders.isOpen()) {
			db_orders.close();
		}

		return o;
	}

}
