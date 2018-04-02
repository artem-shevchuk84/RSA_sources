package ua.rsa.gd;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import ua.rsa.gd.R;
import ua.rsa.gd.adapter.DebitExtItem;

/**
 * @author Komarev Roman
 *         Kharkov, neo33da@gmail.com, +380503412392
 */
public class DebitExtActivity extends ListActivity
        implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    public static final int IDM_DEBIT			= 107;

    private boolean mIsLightTheme;
    private ListView mListView;
    private SimpleAdapter mAdapter;
    private ArrayList<DebitExtItem> mList;
    private Context mContext;

    private String selectedCustId;
    private String selectedSum;
    private String selectedRn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mContext = this;
        mIsLightTheme = getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE)
                .getBoolean(RsaDb.LIGHTTHEMEKEY, false);

        if (mIsLightTheme) {
            setTheme(R.style.Theme_Custom);
            super.onCreate(savedInstanceState);
            setContentView(R.layout.l_debit_ext);
        } else {
            setTheme(R.style.Theme_CustomBlack2);
            super.onCreate(savedInstanceState);
            setContentView(R.layout.debit_ext);
        }
        Button btnBack = (Button) findViewById(R.id.debit_pbtn_prev);
        btnBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        Button btnNext = (Button) findViewById(R.id.debit_pbtn_next);
        btnNext.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                OrderHead mOrderH = new OrderHead();
                SharedPreferences def_prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                mOrderH.shop_id = "ZZZ";
                mOrderH.cust_id = def_prefs.getString("prevSCust", "-XXX");

                final Intent intent = new Intent();
                intent.setClass(getApplicationContext(), DebitActivity.class);
                intent.putExtra("MODE", IDM_DEBIT);
                if (!mOrderH.cust_id.equals("-XXX")) {
                    intent.putExtra("ORDERH", mOrderH);
                }
                startActivity(intent);
                //Intent intent = new Intent();
                //intent.setClass(getApplicationContext(), AddDebitActivity.class);
                //AddDebitActivity.startAddDebitActivity(mContext, "", "", "");
            }
        });
        mListView = getListView();
        mListView.setOnItemClickListener(this);
        mListView.setOnItemLongClickListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateList();
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
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        //AddDebitActivity.startAddDebitActivity(mContext,
        //        mList.get(i).get(DebitExtItem.CUST_ID),
        //        mList.get(i).get(DebitExtItem.RN),
        //        mList.get(i).get(DebitExtItem.SUM));
        OrderHead mOrderH = new OrderHead();
        SharedPreferences def_prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mOrderH.shop_id = "ZZZ";
        mOrderH.cust_id = mList.get(i).get(DebitExtItem.CUST_ID);

        final Intent intent = new Intent();
        intent.setClass(getApplicationContext(), DebitActivity.class);
        intent.putExtra("MODE", IDM_DEBIT);
        intent.putExtra("ORDERH", mOrderH);
        startActivity(intent);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        selectedCustId = mList.get(i).get(DebitExtItem.CUST_ID);
        selectedSum = mList.get(i).get(DebitExtItem.SUM);
        selectedRn = mList.get(i).get(DebitExtItem.RN);
        showDialog(0);
        return true;
    }

    @Override
    protected Dialog onCreateDialog(int id, Bundle args) {
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setTitle("Удалить ПКО");
        adb.setMessage("Вы уверены?");
        adb.setPositiveButton("Удалить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                RsaDbHelper mDb_orders = new RsaDbHelper(getApplicationContext(),
                        RsaDbHelper.DB_ORDERS);
                SQLiteDatabase db_orders = mDb_orders.getWritableDatabase();
                db_orders.execSQL(
                        "delete from _kassadet where CUST_ID='"
                                + selectedCustId + "' and RN='"
                                + selectedRn
                                + "' and SUM='" + selectedSum + "'");
                if (db_orders != null) {
                    db_orders.close();
                }
                updateList();
            }
        });
        adb.setNegativeButton("Отмена", null);
        return adb.create();
    }

    private void updateList() {
        populateList();
        initAdapter();
        setHeader();
    }

    private void initAdapter() {
        mAdapter = new SimpleAdapter(this, mList,
                mIsLightTheme ? R.layout.l_list_debit_ext : R.layout.list_debit_ext,
                new String[] {DebitExtItem.CUST_NAME, DebitExtItem.DATE,
                        DebitExtItem.TIME, DebitExtItem.RN, DebitExtItem.SUM},
                new int[] {R.id.textName, R.id.textDate, R.id.textTime,
                        R.id.textRn, R.id.textSum});
        setListAdapter(mAdapter);
    }

    private void setHeader() {
        TextView textTotal = (TextView) findViewById(R.id.textTotal);
        float fSum = 0;
        for (DebitExtItem item : mList) {
            try {
                fSum += Float.parseFloat(item.get(DebitExtItem.SUM));
            } catch (NumberFormatException e) {
            }
        }
        textTotal.setText("Итого: " + String.format("%.2f", fSum).replace(',', '.') + " грн.");
    }

    private void populateList() {
        SharedPreferences prefs = getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);
        //RsaDbHelper mDb = new RsaDbHelper(this,
        //        prefs.getString(RsaDb.ACTUALDBKEY, RsaDbHelper.DB_NAME1));
        RsaDbHelper mDb_orders = new RsaDbHelper(this, RsaDbHelper.DB_ORDERS);
        //SQLiteDatabase db = mDb.getReadableDatabase();
        SQLiteDatabase db_orders = mDb_orders.getReadableDatabase();
        mList = new ArrayList<DebitExtItem>();
        String query = "select CUST_ID, CUSTNAME, DATE, TIME, RN, SUM from _kassadet "
                + "order by DATE, TIME";
        Cursor cursor = db_orders.rawQuery(query, null);

        while (cursor.moveToNext()) {
            mList.add(new DebitExtItem(
                            cursor.getString(0),
                            cursor.getString(1),
                            cursor.getString(2),
                            cursor.getString(3),
                            cursor.getString(4),
                            cursor.getString(5)
                    )
            );
        }
        if (cursor != null) {
            cursor.close();
        }
        if (db_orders != null) {
            db_orders.close();
        }
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

}