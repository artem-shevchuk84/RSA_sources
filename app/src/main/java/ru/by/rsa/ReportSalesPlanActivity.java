package ru.by.rsa;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ru.by.rsa.adapter.SalesPlanAdapter;
import ru.by.rsa.adapter.viewholders.SalesPlanViewHolder;
import ru.by.rsa.models.ItemModel;
import ru.by.rsa.models.SalesPlanModel;

public class ReportSalesPlanActivity extends Activity {

    private static final String EXTRA_CUST_ID = "extra_cust_id";
    private static final String EXTRA_SHOP_ID = "extra_shop_id";
    private static final String EXTRA_CUST_NAME = "extra_cust_name";
    private static final String EXTRA_SHOP_NAME = "extra_shop_name";

    @Bind(R.id.cust)
    Button mCustButton;
    @Bind(R.id.shop)
    Button mShopButton;
    @Bind(R.id.brand)
    Button mBrandButton;
    @Bind(R.id.recyclerView)
    RecyclerView mRecyclerView;

    @Bind(R.id.label_cust)
    View mCustLabel;
    @Bind(R.id.label_shop)
    View mShopLabel;
    @Bind(R.id.label_brand)
    View mBrandLabel;

    private SalesPlanAdapter mAdapter;
    private List<SalesPlanModel> mPlanList = new ArrayList<SalesPlanModel>();
    private int mState = SalesPlanViewHolder.STATE_NOTHING_SELECTED;

    private String mDBKey;
    private ItemModel mSelectedCust = new ItemModel();
    private ItemModel mSelectedShop = new ItemModel();
    private ItemModel mSelectedBrand = new ItemModel();
    private List<ItemModel> mCustList = new ArrayList<ItemModel>();
    private List<ItemModel> mShopList = new ArrayList<ItemModel>();
    private List<ItemModel> mBrandList = new ArrayList<ItemModel>();

    private boolean isPlanShowQty = false;

    public static void start(Context context) {
        Intent intent = new Intent(context, ReportSalesPlanActivity.class);
        context.startActivity(intent);
    }

    public static void start(Context context, String custId, String custName, String shopId, String shopName) {
        Intent intent = new Intent(context, ReportSalesPlanActivity.class);
        intent.putExtra(EXTRA_CUST_ID, custId);
        intent.putExtra(EXTRA_SHOP_ID, shopId);
        intent.putExtra(EXTRA_CUST_NAME, custName);
        intent.putExtra(EXTRA_SHOP_NAME, shopName);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.salesplan);
        ButterKnife.bind(this);

        SharedPreferences def_prefs = PreferenceManager.getDefaultSharedPreferences(this);
        isPlanShowQty = def_prefs.getBoolean("prefStatPlanShowQty", false);

        SharedPreferences prefs = getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);
        mDBKey = prefs.getString(RsaDb.ACTUALDBKEY, RsaDbHelper.DB_NAME1);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mAdapter = new SalesPlanAdapter(this, mPlanList, isPlanShowQty);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setHasFixedSize(true);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String cust_id = extras.getString(EXTRA_CUST_ID);
            if (!TextUtils.isEmpty(cust_id)) {
                mSelectedCust.setId(cust_id);
                mSelectedCust.setName(extras.getString(EXTRA_CUST_NAME));
                mSelectedShop.setId(extras.getString(EXTRA_SHOP_ID));
                mSelectedShop.setName(extras.getString(EXTRA_SHOP_NAME));
            }
        }

        initCustFilter();
        initShopFilter();
        initBrandFilter();
        updateButtonsAndState();
        updateList();
    }

    @Override
    protected void onDestroy() {
        ButterKnife.unbind(this);
        super.onDestroy();
    }

    private void updateList() {
        RsaDbHelper mDb = new RsaDbHelper(this, mDBKey);
        SQLiteDatabase db = mDb.getReadableDatabase();
        String where = "";
        if (mState == SalesPlanViewHolder.STATE_CUST_ONLY) {
            where = "where _statplan.CUST_ID='" + mSelectedCust.getId() + "' ";
        } else if (mState == SalesPlanViewHolder.STATE_CUST_AND_SHOP) {
            where = "where _statplan.CUST_ID='" + mSelectedCust.getId() + "' and "
                    + "_statplan.SHOP_ID='" + mSelectedShop.getId() + "' ";
        } else if (mState == SalesPlanViewHolder.STATE_CUST_AND_BRAND) {
            where = "where _statplan.CUST_ID='" + mSelectedCust.getId() + "' and "
                    + "_statplan.BRAND_ID='" + mSelectedBrand.getId() + "' ";
        } else if (mState == SalesPlanViewHolder.STATE_BRAND_ONLY) {
            where = "where _statplan.BRAND_ID='" + mSelectedBrand.getId() + "'";
        } else if (mState == SalesPlanViewHolder.STATE_ALL_SELECTED) {
            where = "where _statplan.CUST_ID='" + mSelectedCust.getId() + "' and "
                    + "_statplan.SHOP_ID='" + mSelectedShop.getId() + "' and "
                    + "_statplan.BRAND_ID='" + mSelectedBrand.getId() + "' ";
        }
        String query =
                "select DATETIME, _cust.NAME, _shop.NAME, _brand.NAME, PLAN_SUM, PLAN_TOP_QTY, PLAN_QTY, ACT_SUM, "
                        + "ACT_TOP_QTY, ACT_QTY, REST_SUM, REST_TOP_QTY, REST_QTY, ACT_QTY from _statplan "
                        + "left join _cust on _statplan.CUST_ID = _cust.ID "
                        + "left join _shop on _statplan.SHOP_ID = _shop.ID "
                        + "left join _brand on _statplan.BRAND_ID = _brand.ID "
                        + where;

        Cursor cursor = db.rawQuery(query, null);
        mPlanList.clear();
        while (cursor.moveToNext()) {
            mPlanList.add(new SalesPlanModel(cursor.getString(0), cursor.getString(1), cursor.getString(2),
                    cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getString(6),
                    cursor.getString(7), cursor.getString(8), cursor.getString(9), cursor.getString(10),
                    cursor.getString(11), cursor.getString(12), cursor.getString(13)));
        }
        mAdapter.setItems(mPlanList, mState);
        if (!cursor.isClosed()) {
            cursor.close();
        }
        if (db.isOpen()) {
            db.close();
        }
    }

    private void initCustFilter() {
        RsaDbHelper mDb = new RsaDbHelper(this, mDBKey);
        SQLiteDatabase db = mDb.getReadableDatabase();
        String query = "select ID, NAME from _cust where ID in (select CUST_ID from _statplan) order by NAME";
        Cursor cursor = db.rawQuery(query, null);
        mCustList.add(new ItemModel("", "Все"));
        while (cursor.moveToNext()) {
            mCustList.add(new ItemModel(cursor.getString(0), cursor.getString(1)));
        }
        if (!cursor.isClosed()) {
            cursor.close();
        }
        if (db.isOpen()) {
            db.close();
        }
    }

    private void initShopFilter() {
        if (mSelectedCust.isEmpty()) {
            mShopList.clear();
            mShopList.add(new ItemModel("", "Все"));
            return;
        }

        RsaDbHelper mDb = new RsaDbHelper(this, mDBKey);
        SQLiteDatabase db = mDb.getReadableDatabase();
        String query = "select ID, NAME from _shop where CUST_ID='" + mSelectedCust.getId() + "' "
                + "and ID in (select SHOP_ID from _statplan where CUST_ID='" + mSelectedCust.getId() + "')";
        Cursor cursor = db.rawQuery(query, null);
        mShopList.clear();
        mShopList.add(new ItemModel("", "Все"));
        while (cursor.moveToNext()) {
            mShopList.add(new ItemModel(cursor.getString(0), cursor.getString(1)));
        }
        if (!cursor.isClosed()) {
            cursor.close();
        }
        if (db.isOpen()) {
            db.close();
        }
    }

    private void initBrandFilter() {
        String where = "";
        if (!mSelectedCust.isEmpty()) {
            where = " where CUST_ID='" + mSelectedCust.getId() + "'";
        }
        if (!mSelectedShop.isEmpty()) {
            where += " and SHOP_ID='" + mSelectedShop.getId() + "'";
        }
        RsaDbHelper mDb = new RsaDbHelper(this, mDBKey);
        SQLiteDatabase db = mDb.getReadableDatabase();
        String query = "select ID, NAME from _brand where ID in "
                + "(select BRAND_ID from _statplan" + where + ")";
        Cursor cursor = db.rawQuery(query, null);
        mBrandList.clear();
        mBrandList.add(new ItemModel("", "Все"));
        while (cursor.moveToNext()) {
            mBrandList.add(new ItemModel(cursor.getString(0), cursor.getString(1)));
        }
        if (!cursor.isClosed()) {
            cursor.close();
        }
        if (db.isOpen()) {
            db.close();
        }
    }

    private void updateButtonsAndState() {
        mCustButton.setText(mSelectedCust.isEmpty() ? "Клиент: все" : mSelectedCust.getName());
        mShopButton.setText(mSelectedShop.isEmpty() ? "ТТ: все" : mSelectedShop.getName());
        mBrandButton.setText(mSelectedBrand.isEmpty() ? "Бренд: все" : mSelectedBrand.getName());
        if (mSelectedCust.isEmpty() && mSelectedShop.isEmpty() && mSelectedBrand.isEmpty()) {
            mState = SalesPlanViewHolder.STATE_NOTHING_SELECTED;
            mCustLabel.setVisibility(View.VISIBLE);
            mShopLabel.setVisibility(View.VISIBLE);
            mBrandLabel.setVisibility(View.VISIBLE);
        } else if (!mSelectedCust.isEmpty() && mSelectedShop.isEmpty() && mSelectedBrand.isEmpty()) {
            mState = SalesPlanViewHolder.STATE_CUST_ONLY;
            mCustLabel.setVisibility(View.GONE);
            mShopLabel.setVisibility(View.VISIBLE);
            mBrandLabel.setVisibility(View.VISIBLE);
        } else if (!mSelectedCust.isEmpty() && !mSelectedShop.isEmpty() && mSelectedBrand.isEmpty()) {
            mState = SalesPlanViewHolder.STATE_CUST_AND_SHOP;
            mCustLabel.setVisibility(View.GONE);
            mShopLabel.setVisibility(View.GONE);
            mBrandLabel.setVisibility(View.VISIBLE);
        } else if (!mSelectedCust.isEmpty() && mSelectedShop.isEmpty() && !mSelectedBrand.isEmpty()) {
            mState = SalesPlanViewHolder.STATE_CUST_AND_BRAND;
            mCustLabel.setVisibility(View.GONE);
            mShopLabel.setVisibility(View.VISIBLE);
            mBrandLabel.setVisibility(View.GONE);
        } else if (!mSelectedCust.isEmpty() && !mSelectedShop.isEmpty() && !mSelectedBrand.isEmpty()) {
            mState = SalesPlanViewHolder.STATE_ALL_SELECTED;
            mCustLabel.setVisibility(View.GONE);
            mShopLabel.setVisibility(View.GONE);
            mBrandLabel.setVisibility(View.GONE);
        } else if (mSelectedCust.isEmpty() && mSelectedShop.isEmpty() && !mSelectedBrand.isEmpty()) {
            mState = SalesPlanViewHolder.STATE_BRAND_ONLY;
            mCustLabel.setVisibility(View.VISIBLE);
            mShopLabel.setVisibility(View.VISIBLE);
            mBrandLabel.setVisibility(View.GONE);
        }
    }

    private static String[] listToArrayOfNames(List<ItemModel> list) {
        int size = list.size();
        String[] array = new String[size];
        for (int i = 0; i < size; i++) {
            array[i] = list.get(i).getName();
        }
        return array;
    }

    @OnClick(R.id.cust)
    void onCustFilterClick() {
        String[] items = listToArrayOfNames(mCustList);
        new AlertDialog.Builder(this)
                .setTitle("Клиент")
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mSelectedCust.clear();
                        mSelectedShop.clear();
                        mSelectedBrand.clear();
                        if (which > 0) {
                            mSelectedCust.setId(mCustList.get(which).getId());
                            mSelectedCust.setName(mCustList.get(which).getName());
                        }
                        initShopFilter();
                        initBrandFilter();
                        updateButtonsAndState();
                        updateList();
                    }
                })
                .create()
                .show();
    }

    @OnClick(R.id.shop)
    void onShopFilterClick() {
        String[] items = listToArrayOfNames(mShopList);
        new AlertDialog.Builder(this)
                .setTitle("Торг точка")
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mSelectedShop.clear();
                        mSelectedBrand.clear();
                        if (which > 0) {
                            mSelectedShop.setId(mShopList.get(which).getId());
                            mSelectedShop.setName(mShopList.get(which).getName());
                        }
                        initBrandFilter();
                        updateButtonsAndState();
                        updateList();
                    }
                })
                .create()
                .show();
    }

    @OnClick(R.id.brand)
    void onBrandFilterClick() {
        String[] items = listToArrayOfNames(mBrandList);
        new AlertDialog.Builder(this)
                .setTitle("Бренд")
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mSelectedBrand.clear();
                        if (which > 0) {
                            mSelectedBrand.setId(mBrandList.get(which).getId());
                            mSelectedBrand.setName(mBrandList.get(which).getName());
                        }
                        updateButtonsAndState();
                        updateList();
                    }
                })
                .create()
                .show();
    }
}
