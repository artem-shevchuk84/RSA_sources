package ru.by.rsa.ui.activity;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.StringBuilderPrinter;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import ru.by.rsa.R;
import ru.by.rsa.RsaDb;
import ru.by.rsa.RsaDbHelper;
import ru.by.rsa.adapter.item.ComboCustomerItem;
import ru.by.rsa.adapter.item.DebitItem;

/**
 * @author Komarev Roman
 *         Kharkov, neo33da@gmail.com, +380503412392
 */
public class AddDebitActivity extends Activity implements AdapterView.OnItemSelectedListener {

    private static final String TAG = "AddDebitActivity";

    private static String KEY_CUST_ID = "CUST_ID";
    private static String KEY_RN = "RN";
    private static String KEY_CASH = "CASH";

    private boolean mIsLightTheme;
    private SimpleAdapter mAdapterCustomers;
    private SimpleAdapter mAdapterDebits;
    private ArrayList<ComboCustomerItem> mListCustomers;
    private ArrayList<DebitItem> mListDebit;
    private Spinner mCustomerSpinner;
    private Spinner mDebitSpinner;
    private TextView mRemark;
    private boolean isFirstTimeShowed = true;

    private String mCustId;
    private String mRn;
    private String mCash;
    private boolean mIsNew;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mIsLightTheme = getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE)
                .getBoolean(RsaDb.LIGHTTHEMEKEY, false);

        if (mIsLightTheme) {
            setTheme(R.style.Theme_Custom);
            super.onCreate(savedInstanceState);
            setContentView(R.layout.l_debit_ext_add);
        } else {
            setTheme(R.style.Theme_CustomBlack2);
            super.onCreate(savedInstanceState);
            setContentView(R.layout.debit_ext_add);
        }
        Bundle extras = getIntent().getExtras();
        mCustId = extras.getString(KEY_CUST_ID);
        mRn = extras.getString(KEY_RN);
        Log.d("FFFF sended", TextUtils.isEmpty(mRn) ? "empy" : mRn);
        mCash = extras.getString(KEY_CASH);
        mIsNew = TextUtils.isEmpty(mCustId) && TextUtils.isEmpty(mRn);
        Button btnCancel = (Button) findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        Button btnSave = (Button) findViewById(R.id.debit_pbtn_prev);
        btnSave.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                saveCurrentDebt();
                finish();
            }
        });
        mRemark = (TextView) findViewById(R.id.textRemark);
        mCustomerSpinner = (Spinner) findViewById(R.id.spinCustomers);
        mCustomerSpinner.setOnItemSelectedListener(this);

        mDebitSpinner = (Spinner) findViewById(R.id.spinDebts);
        mDebitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                EditText editText = (EditText) findViewById(R.id.editText);
                mRn = mListDebit.get(i).get(DebitItem.DOCNUMBER);
                String sum = mListDebit.get(i).get(DebitItem.SUM);
                SharedPreferences def_prefs = PreferenceManager.getDefaultSharedPreferences(
                        getApplicationContext());
                def_prefs.edit().putString("prevSDebit", mRn).commit();
                if (isFirstTimeShowed && !mIsNew) {
                    editText.setText(TextUtils.isEmpty(mCash) ? "0" : mCash);
                } else {
                    editText.setText(TextUtils.isEmpty(sum) ? "0" : sum);
                }
                editText.setSelection(0, editText.getText().toString().length());
                if (isFirstTimeShowed) {
                    isFirstTimeShowed = false;
                    InputMethodManager imm = (InputMethodManager) getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(editText, InputMethodManager.SHOW_FORCED);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        updateInfo();
    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        SharedPreferences def_prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mCustId = mListCustomers.get(i).get(ComboCustomerItem.ID);
        def_prefs.edit().putString("prevSCust", mCustId).commit();
        populateRemarkField(mCustId);
        populateDebtList(mCustId);
        initDebitAdapter();
        if (!TextUtils.isEmpty(mRn)) {
            selectDebitByRn(mRn);
            return;
        }

        String previousSelectedDebit = def_prefs.getString("prevSDebit", "-XXX");
        if (!previousSelectedDebit.equals("-XXX")) {
            selectDebitByRn(previousSelectedDebit);
        } else {
            selectDebit(0);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    private void updateInfo() {
        mCustomerSpinner = (Spinner) findViewById(R.id.spinCustomers);
        mDebitSpinner = (Spinner) findViewById(R.id.spinDebts);
        populateCustomerList();
        initCustomerAdapter();

        if (!TextUtils.isEmpty(mCustId)) {
            Log.d(TAG, "updateInfo: cust_id=" + mCustId);
            selectCustomerById(mCustId);
            return;
        }

        SharedPreferences def_prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String previousSelectedCustomerId = def_prefs.getString("prevSCust", "-XXX");
        if (previousSelectedCustomerId.equals("-XXX")) {
            selectCustomer(0);
        } else {
            selectCustomerById(previousSelectedCustomerId);
        }
    }

    private void selectCustomer(int position) {
        if (mListCustomers.size() == 0) {
            return;
        } else {
            mCustomerSpinner.setSelection(position);
        }
    }

    private void selectCustomerById(String custId) {
        if (mListCustomers.size() == 0) {
            return;
        }
        int pos = 0;
        for (int i = 0; i < mListCustomers.size(); i++) {
            if (mListCustomers.get(i).get(ComboCustomerItem.ID).equals(custId)) {
                mCustomerSpinner.setSelection(i);
                return;
            }
        }
        mCustomerSpinner.setSelection(pos);
    }

    private void selectDebitByRn(String rn) {
        if (mListDebit.size() == 0) {
            return;
        }
        int pos = 0;
        for (int i = 0; i < mListDebit.size(); i++) {
            Log.d(TAG, "selectDebitByRn: comparing etalonrn=" + rn + " RN=" + mListDebit.get(i)
                    .get(DebitItem.DOCNUMBER)
                    .equals(rn));
            if (mListDebit.get(i).get(DebitItem.DOCNUMBER).equals(rn)) {
                mDebitSpinner.setSelection(i);
                return;
            }
        }
        mDebitSpinner.setSelection(pos);
    }

    private void selectDebit(int position) {
        if (mListDebit.size() == 0) {
            return;
        } else {
            mDebitSpinner.setSelection(position);
            mRn = mListDebit.get(position).get(DebitItem.DOCNUMBER);
        }
    }

    private void initCustomerAdapter() {
        mAdapterCustomers = new SimpleAdapter(this, mListCustomers, R.layout.spinner_shop_item,
                new String[] {ComboCustomerItem.NAME, ComboCustomerItem.ADRES},
                new int[] {R.id.spinner_shop_text0, R.id.spinner_shop_text1});
        mCustomerSpinner.setAdapter(mAdapterCustomers);
    }

    private void initDebitAdapter() {
        mAdapterDebits = new SimpleAdapter(this, mListDebit, R.layout.spinner_debit_item,
                new String[] {DebitItem.DATEDOC, DebitItem.EXPDATE, DebitItem.DOCNUMBER,
                        DebitItem.SUM, DebitItem.COMMENT},
                new int[] {R.id.textDate, R.id.textExp, R.id.textRn, R.id.textSum,
                        R.id.textComment});
        mDebitSpinner.setAdapter(mAdapterDebits);
    }

    private void populateCustomerList() {
        SharedPreferences def_prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean usePlan = def_prefs.getBoolean(RsaDb.USINGPLAN, false);
        SharedPreferences prefs = getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);
        RsaDbHelper mDb = new RsaDbHelper(this,
                prefs.getString(RsaDb.ACTUALDBKEY, RsaDbHelper.DB_NAME1));
        //RsaDbHelper mDb_orders = new RsaDbHelper(this, RsaDbHelper.DB_ORDERS);
        SQLiteDatabase db = mDb.getReadableDatabase();
        //SQLiteDatabase db_orders = mDb_orders.getReadableDatabase();
        mListCustomers = new ArrayList<ComboCustomerItem>();
        String query
                = "select ID, NAME, ADDRESS from _cust where ID <> '' group by ID order by NAME";

        if (usePlan) {
            Calendar c = Calendar.getInstance();
            int d = c.get(Calendar.DAY_OF_MONTH);
            int m = c.get(Calendar.MONTH) + 1;
            int y = c.get(Calendar.YEAR);
            String curDate = String.format("%02d%02d%04d", d, m, y);
            query = "select c.ID, c.NAME, c.ADDRESS from _cust as c"
                    + " left join _plan as p on c.ID=p.CUST_ID"
                    + " where c.ID <> '' and p.DATEV='" + curDate + "'"
                    + " group by c.ID"
                    + " order by c.NAME";
        }
        Log.d("FFFF", "populateCust=" + query);
        Cursor cursor = db.rawQuery(query, null);
        mListCustomers.clear();
        while (cursor.moveToNext()) {
            mListCustomers.add(new ComboCustomerItem(cursor.getString(0), cursor.getString(1),
                    "", cursor.getString(2), "", "", "", "", "")
            );
        }
        if (cursor != null) {
            cursor.close();
        }
        if (db != null) {
            db.close();
        }
    }

    private void populateDebtList(String id) {
        SharedPreferences prefs = getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);
        RsaDbHelper mDb = new RsaDbHelper(this,
                prefs.getString(RsaDb.ACTUALDBKEY, RsaDbHelper.DB_NAME1));
        //RsaDbHelper mDb_orders = new RsaDbHelper(this, RsaDbHelper.DB_ORDERS);
        SQLiteDatabase db = mDb.getReadableDatabase();
        //SQLiteDatabase db_orders = mDb_orders.getReadableDatabase();
        mListDebit = new ArrayList<DebitItem>();
        String query = "select CUST_ID, RN, DATEDOC, SUM, DATEPP, CLOSED, COMMENT, "
                + "SHOP_ID, PAYMENT from _debit "
                + "where CUST_ID = '" + id + "' order by DATEDOC";

        Cursor cursor = db.rawQuery(query, null);
        mListDebit.clear();

        while (cursor.moveToNext()) {
            Log.d("FFFFF cursor", cursor.getString(1) == null ? "none!" : cursor.getString(1));
            mListDebit.add(new DebitItem(
                            cursor.getString(2),
                            cursor.getString(3),
                            cursor.getString(4),
                            cursor.getString(1),
                            "", // expired
                            "", // cash
                            "", // curency
                            cursor.getString(6),
                            "", // adress
                            "" // startdebt
                    )
            );
        }

        if (mListDebit.size() == 0) {
            mListDebit.add(new DebitItem(
                            "",
                            "",
                            "",
                            "Долгов нет",
                            "", // expired
                            "", // cash
                            "", // curency
                            "",
                            "", // adress
                            "" // startdebt
                    )
            );
        }

        if (cursor != null) {
            cursor.close();
        }
        if (db != null) {
            db.close();
        }
    }

    private void populateRemarkField(String custId) {
        RsaDbHelper mDb_orders = new RsaDbHelper(this, RsaDbHelper.DB_ORDERS);
        SQLiteDatabase db = mDb_orders.getReadableDatabase();
        String query = "select DATE, TIME, RN, SUM from _kassadet where CUST_ID='" + custId
                + "' order by DATE, TIME";

        Cursor cursor = db.rawQuery(query, null);
        float sum = 0;
        StringBuilder builder = new StringBuilder();
        while (cursor.moveToNext()) {
            try {
                sum += Float.parseFloat(cursor.getString(3));
                builder.append(cursor.getString(0));
                builder.append("  ");
                builder.append(cursor.getString(1));
                builder.append(" - ");
                builder.append(String.format("%.2f грн.", Float.parseFloat(cursor.getString(3))));
                builder.append("(");
                builder.append(cursor.getString(2));
                builder.append(")");
                builder.append("\n");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        builder.append("ИТОГО: " + String.format("%.2f грн.", sum));
        mRemark.setText(builder);

        if (cursor != null) {
            cursor.close();
        }
        if (db != null) {
            db.close();
        }
    }

    private void saveCurrentDebt() {
        EditText editText = (EditText) findViewById(R.id.editText);
        Calendar c = Calendar.getInstance();
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat fmtTime = new SimpleDateFormat("HH:mm:ss");
        String time = fmtTime.format(c.getTime());
        String date = fmt.format(c.getTime());
        String sum = editText.getText().toString();
        sum = TextUtils.isEmpty(sum) ? "0" : sum;
        String full = "0";
        String cust_name = mListCustomers
                .get(mCustomerSpinner.getSelectedItemPosition())
                .get(ComboCustomerItem.NAME);
        SharedPreferences def_prefs = PreferenceManager.getDefaultSharedPreferences(
                getApplicationContext());
        boolean isAutocorrect = def_prefs.getBoolean("prefDebitAutocorrection", false);

        try {
            float f_sum = Float.parseFloat(sum);
            float prev_sum = Float.parseFloat(mListDebit
                    .get(mDebitSpinner.getSelectedItemPosition())
                    .get(DebitItem.SUM));
            if (f_sum > prev_sum) {
                if (isAutocorrect == true && prev_sum > 0) {
                    sum = mListDebit
                            .get(mDebitSpinner.getSelectedItemPosition())
                            .get(DebitItem.SUM);
                    Toast.makeText(getApplicationContext(),
                            "Автокоррекция! Денег больше чем долга!!",
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Денег больше чем долга!!",
                            Toast.LENGTH_LONG).show();
                }
            }
        } catch (Exception e) {
        }

        RsaDbHelper mDb_orders = new RsaDbHelper(this, RsaDbHelper.DB_ORDERS);
        SQLiteDatabase db_orders = mDb_orders.getReadableDatabase();
        ContentValues values = new ContentValues();
        values.put(RsaDbHelper.KASSADET_SUM, sum);
        values.put(RsaDbHelper.KASSADET_DATE, date);
        // if (alreadyTaken(date, mCustId, mRn)) {
        //     db_orders.update(RsaDbHelper.TABLE_KASSADET, values,
        //             "(DATE='" + date + "') and (CUST_ID='" + mCustId + "') and (RN='" + mRn +
        // "')",
        //             null);
        // } else {
        values.put(RsaDbHelper.KASSADET_CUST_ID, mCustId);
        values.put(RsaDbHelper.KASSADET_RN, mRn);
        values.put(RsaDbHelper.KASSADET_CUSTNAME, cust_name);
        values.put(RsaDbHelper.KASSADET_FULL, full);
        values.put(RsaDbHelper.KASSADET_TIME, time);
        db_orders.insert(RsaDbHelper.TABLE_KASSADET, RsaDbHelper.KASSADET_CUST_ID, values);
        //}

        if (db_orders != null) {
            db_orders.close();
        }

        InputMethodManager imm = (InputMethodManager) getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }

    private String normalizeSum(String sum) {
        if (TextUtils.isEmpty(sum)) {
            return "0.00";
        } else {
            float fSum = 0;
            try {
                fSum = Float.parseFloat(sum);
            } catch (NumberFormatException e) {
            }
            return String.format("%.2f", fSum).replace(',', '.');
        }
    }

    private boolean alreadyTaken(String date, String cust_id, String rn) {
        boolean result = false;
        Cursor cashCursor = null;
        RsaDbHelper mDb_orders = new RsaDbHelper(this, RsaDbHelper.DB_ORDERS);
        SQLiteDatabase db_orders = mDb_orders.getReadableDatabase();
        try {
            String q = "select SUM from _kassadet where (DATE='" + date + "') and "
                    + "(CUST_ID='" + cust_id + "') and (RN='" + rn + "') limit 1";
            Log.d("FFFF query", q);
            cashCursor = db_orders.rawQuery(q, null);
            if (cashCursor.getCount() > 0) {
                result = true;
            }
        } catch (Exception e3) {
        }

        try {
            if (cashCursor != null) {
                cashCursor.close();
            }
        } catch (Exception e4) {
        }

        if (db_orders != null) {
            db_orders.close();
        }
        Log.d("FFFF result", result ? "true" : "false");
        return result;
    }

    public static void startAddDebitActivity(Context context,
            String custId,
            String rn,
            String cash) {
        Intent intent = new Intent();
        intent.setClass(context, AddDebitActivity.class);
        intent.putExtra(KEY_CUST_ID, custId);
        intent.putExtra(KEY_RN, rn);
        intent.putExtra(KEY_CASH, cash);
        context.startActivity(intent);
    }

}