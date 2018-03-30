package ua.rsa.gd;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ua.rsa.gd.AvantaErarh.Item;
import ua.rsa.gd.AvantaErarh.ListAdapter;
import ua.rsa.gd.AvantaErarh.ListItem;
import ru.by.rsa.R;
import ua.rsa.gd.utils.DeviceUtils;

public class GroupActivityAvanta extends Activity implements ListAdapter.OnProductClickListener {

    private static final String TAG = "GroupActivityAvanta";

    public static final int IDM_ZERO_STOCKS = 100;
    public static final int IDM_ZERO_STOCKS_SHOW = 101;
    public static final int IDM_PLAN_REPORT = 102;

    private static final String DEFAULT_COUNT = "1";

    private static final String ONE = "1";

    private static final String PRICE = "Цена";
    private static final String PRICE_SPACE = "Цена ";
    private static final String FORMAT_DISCOUNT = "СКИДКА: %d%%";

    private static final String QUERY = "select _goods.ID, _goods.NAME, REST, HIST1, HIST2, "
            + "PRICE1, PRICE2, PRICE3, PRICE4, PRICE5, PRICE6, PRICE7, PRICE8, PRICE9, PRICE10, "
            + "PRICE11, PRICE12, PRICE13, PRICE14, PRICE15, PRICE16,  PRICE17, PRICE18, PRICE19, "
            + "PRICE20, _goods.FLASH, NDS, _brand.NAME, chist.QTY from _goods "
            + "left join _brand on _brand.ID = _goods.BRAND_ID ";

    @Bind(R.id.listView)
    ListView mListView;
    @Bind(R.id.group_txtTotalSum_text)
    TextView mSum;
    @Bind(R.id.prices)
    TextView mPrices;
    @Bind(R.id.eye)
    View mEye;

    private ArrayList<Item> mItems;
    private ListAdapter mAdapter;
    private SharedPreferences mPrefs;
    private SharedPreferences mPrefsDb;
    private OrderHead mOrderH;
    private boolean mLightTheme;
    private boolean mAllowDiscount;
    private int mPriceIndex = 1;

    private boolean mShowZeroStocks = true;

    private int ttop;
    private int iidx;
    private int groupPos;

    public void onCreate(Bundle savedInstanceState) {
        initDefaults();
        setTheme(mLightTheme ? R.style.Theme_Custom : R.style.Theme_CustomBlack2);
        super.onCreate(savedInstanceState);
        setContentView(mLightTheme ? R.layout.l_group_avanta : R.layout.group_avanta);
        ButterKnife.bind(this);

        Bundle extras = getIntent().getExtras();
        mOrderH = extras.getParcelable("ORDERH");
        if (mOrderH == null) {
            Toast.makeText(this, "Не найден заказник", Toast.LENGTH_LONG).show();
            finish();
        }
        ttop = extras.getInt("TOP", 0);
        iidx = extras.getInt("INDEX", 0);
        groupPos = extras.getInt("GPOSITION", 0);

        String currentPrice = mPrefs.getString(RsaDb.PRICESELECTED, "xxx");
        if (!TextUtils.equals(currentPrice, "xxx")) {
            for (int i = 1; i <= 20; i++) {
                if (TextUtils.equals(PRICE_SPACE + Integer.toString(i), currentPrice)) {
                    mPriceIndex = i;
                    break;
                }
            }
        }

        initList();
        mergeListWithExistingOrder();
        updateSum();
        updatePricesButton();
    }

    private void updateEyeIcon() {
        mEye.setVisibility(mShowZeroStocks ? View.GONE : View.VISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, IDM_ZERO_STOCKS_SHOW, Menu.NONE, "ПОКАЗАТЬ НУЛЕВЫЕ ОСТАТКИ")
                .setAlphabeticShortcut('s');
        menu.add(Menu.NONE, IDM_ZERO_STOCKS, Menu.NONE, "СКРЫТЬ НУЛЕВЫЕ ОСТАТКИ")
                .setAlphabeticShortcut('h');
        menu.add(Menu.NONE, IDM_PLAN_REPORT, Menu.NONE, "ОТЧЕТ: ПЛАН ПРОДАЖ")
                .setAlphabeticShortcut('p');
        return (super.onCreateOptionsMenu(menu));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case IDM_ZERO_STOCKS:
                mShowZeroStocks = false;
                updateEyeIcon();
                initList();
                mergeListWithExistingOrder();
                return true;
            case IDM_ZERO_STOCKS_SHOW:
                mShowZeroStocks = true;
                updateEyeIcon();
                initList();
                mergeListWithExistingOrder();
                return true;
            case IDM_PLAN_REPORT:
                ReportSalesPlanActivity.start(this,
                        mOrderH.cust_id.toString(), mOrderH.cust_text.toString(),
                        mOrderH.shop_id.toString(), mOrderH.shop_text.toString());
                return true;
        }
        return true;
    }

    private void updatePricesButton() {
        mPrices.setText(PRICE + Integer.toString(mPriceIndex));
    }

    private void initDefaults() {
        mPrefs = getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE);
        mPrefsDb = getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);
        mLightTheme = mPrefs.getBoolean(RsaDb.LIGHTTHEMEKEY, false);
        mAllowDiscount = mPrefs.getBoolean("dscperline", false);
    }

    private void initList() {
        mItems = generateHierarchy();
        mAdapter = new ListAdapter(this, mItems, mPriceIndex, this, mLightTheme);

        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mAdapter.clickOnItem(position);
            }
        });
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                openOptionsMenu();
                return true;
            }
        });
    }

    private ArrayList<Item> generateHierarchy() {
        mItems = new ArrayList<Item>();
        RsaDbHelper mDb = new RsaDbHelper(this,
                mPrefsDb.getString(RsaDb.ACTUALDBKEY, RsaDbHelper.DB_NAME1));
        SQLiteDatabase db = mDb.getReadableDatabase();
        String q = QUERY + "left join (select _hist.GOODS_ID, _hist.QTY from _hist where _hist.CUST_ID = '"
                + mOrderH.cust_id + "' and _hist.SHOP_ID = '" + mOrderH.shop_id
                + "' group by _hist.GOODS_ID) as chist on chist.GOODS_ID = _goods.ID";
        if (!mShowZeroStocks) {
            q += " where HIST2=1 or REST>0";
        }
        Cursor cursor = db.rawQuery(q, null);

        String parentId;
        boolean isParent;
        ListItem listItem;
        ListItem answer = null;

        final ListItem topSkuItem =
                new ListItem("z77", "TOP SKU", new float[20], 0, 0, true, null, true, "0", null);
        topSkuItem.setTopLevel(true);
        mItems.add(topSkuItem);

        while (answer != null || cursor.moveToNext()) {
            if (answer != null) {
                listItem = answer;
                parentId = listItem.getParentId();
                isParent = listItem.isParent();
                answer = null;
            } else {
                parentId = cursor.getString(3);
                isParent = !TextUtils.isEmpty(cursor.getString(4));
                float[] prices = new float[20];
                for (int i = 0; i < 20; i++) {
                    prices[i] = cursor.getFloat(i + 5);
                }
                boolean isTopSku = TextUtils.equals(cursor.getString(25), ONE);
                listItem = new ListItem(cursor.getString(0), cursor.getString(1), prices,
                        cursor.getFloat(2), 0, isParent, parentId, isTopSku,
                        cursor.getString(26), cursor.getString(27));
                listItem.setInHistory(!TextUtils.isEmpty(cursor.getString(28)));
            }
            if (!isParent && TextUtils.isEmpty(parentId) && correctNds(
                    listItem)) { // if not parent and no parents
                mItems.add(listItem);
                if (listItem.isTopSku()) {
                    topSkuItem.addChild(listItem);
                }
            } else if (isParent) { // if parent
                answer = generateBranch(cursor, listItem, topSkuItem);
                listItem.setTopLevel(true);
                mItems.add(listItem);
            } // else do nothing because "not parent and have parents
        }

        cursor.close();
        db.close();
        return mItems;
    }

    private ListItem generateBranch(Cursor cursor, ListItem root, ListItem topSkuItem) {
        String parentId;
        boolean isParent;
        ListItem listItem;
        ListItem answer = null;

        while (answer != null || cursor.moveToNext()) {
            if (answer != null) {
                listItem = answer;
                parentId = listItem.getParentId();
                isParent = listItem.isParent();
                answer = null;
            } else {
                parentId = cursor.getString(3);
                isParent = !TextUtils.isEmpty(cursor.getString(4));
                float[] prices = new float[20];
                for (int i = 0; i < 20; i++) {
                    prices[i] = cursor.getFloat(i + 5);
                }
                boolean isTopSku = TextUtils.equals(cursor.getString(25), ONE);
                listItem = new ListItem(cursor.getString(0), cursor.getString(1), prices,
                        cursor.getFloat(2), 0, isParent, parentId, isTopSku,
                        cursor.getString(26), cursor.getString(27));
                listItem.setInHistory(!TextUtils.isEmpty(cursor.getString(28)));
            }
            if (TextUtils.equals(parentId, root.getId()) && !isParent && correctNds(listItem)) {
                if (listItem.isTopSku()) {
                    topSkuItem.addChild(listItem);
                }
                root.addChild(listItem);
            } else if (TextUtils.equals(parentId, root.getId()) && isParent) {
                answer = generateBranch(cursor, listItem, topSkuItem);
                root.addChild(listItem);
            } else {
                return listItem;
            }
        }
        return null;
    }

    private boolean correctNds(Item listItem) {
        if (mOrderH.hndsrate.equals("0") || mOrderH.hndsrate.equals("")) {
            return !TextUtils.equals(listItem.getNds(), "0")
                    && !TextUtils.equals(listItem.getNds(), "0.07")
                    && !TextUtils.equals(listItem.getNds(), "");
        } else if (mOrderH.hndsrate.equals("2") && TextUtils.equals(listItem.getNds(), "0.07")) {
            return true;
        } else {
            return TextUtils.equals(listItem.getNds(), "0") ||
                    TextUtils.equals(listItem.getNds(), "");
        }
    }

    private void mergeListWithExistingOrder() {
        String goodsId;
        Item item;
        for (OrderLines curLine : mOrderH.lines) {
            goodsId = curLine.get(OrderLines.GOODS_ID);
            for (int i = 0, size = mItems.size(); i < size; i++) {
                item = mItems.get(i);
                if (!item.isParent() && TextUtils.equals(goodsId, mItems.get(i).getId())) {
                    int count = Integer.parseInt(curLine.get(OrderLines.QTY));
                    setItemOrder(mItems, item.getId(), count, curLine.get(OrderLines.DISCOUNT));
                    break;
                } else if (item.isParent() && item.getChilds() != null
                        && item.getChilds().size() > 0) {
                    mergeChilds(item.getChilds(), curLine, goodsId);
                }
            }
        }
    }

    private void mergeChilds(ArrayList<Item> items, OrderLines line, String goodsId) {
        Item item;
        for (int i = 0, size = items.size(); i < size; i++) {
            item = items.get(i);
            if (!item.isParent() && TextUtils.equals(goodsId, items.get(i).getId())) {
                int count = Integer.parseInt(line.get(OrderLines.QTY));
                setItemOrder(mItems, item.getId(), count, line.get(OrderLines.DISCOUNT));
                break;
            } else if (item.isParent() && item.getChilds() != null
                    && item.getChilds().size() > 0) {
                mergeChilds(item.getChilds(), line, goodsId);
            }
        }
    }

    private void showDiscountDialog(final Item item, final TextView discount, final TextView sum,
            final TextView price, final String order) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dlg_head, null);
        dialogBuilder.setView(dialogView);

        final EditText edt = (EditText) dialogView.findViewById(R.id.edtRemark_head);
        edt.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
        edt.setText(Integer.toString(item.getDiscount()));
        edt.setSelectAllOnFocus(true);
        dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                item.setDiscount(Integer.parseInt(edt.length() == 0 ? "0" : edt.getText().toString()));
                discount.setText(String.format(ListItem.LOCALE, FORMAT_DISCOUNT, item.getDiscount()));
                float fPrice = item.getPrice(mPriceIndex) * (1f - item.getDiscount() / 100f);
                price.setText(String.format(ListItem.LOCALE, ListItem.FORMAT_PRICE_SHORT, fPrice));
                sum.setText(String.format(ListItem.LOCALE, ListItem.FORMAT_SUM_SHORT,
                        fPrice * (TextUtils.isEmpty(order) ? 0f : Float.parseFloat(order))));
            }
        });
        dialogBuilder.setNegativeButton("ОТМЕНА", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();

    }

    private void updatePriceSum(TextView sum, Item item, EditText edt) {
        sum.setText(String.format(ListItem.LOCALE, ListItem.FORMAT_SUM_SHORT,
                item.getPrice(mPriceIndex) * (1f - item.getDiscount() / 100f) * (edt.length() == 0 ? 0f
                        : Float.parseFloat(edt.getText().toString()))));
    }

    @Override
    public void onProductClicked(final Item item) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dlg_goods_avanta, null);
        dialogBuilder.setView(dialogView);

        final EditText edt = (EditText) dialogView.findViewById(R.id.order);
        final TextView name = (TextView) dialogView.findViewById(R.id.name);
        final TextView stock = (TextView) dialogView.findViewById(R.id.stock);
        final TextView price = (TextView) dialogView.findViewById(R.id.price);
        final TextView sum = (TextView) dialogView.findViewById(R.id.sum);
        final TextView discount = (TextView) dialogView.findViewById(R.id.discount);
        discount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAllowDiscount) {
                    showDiscountDialog(item, discount, sum, price, edt.getText().toString());
                }
            }
        });
        edt.setText(item.getOrder() == 0 ? DEFAULT_COUNT : Integer.toString(item.getOrder()));
        name.setText(item.getTitle());
        stock.setText(String.format(ListItem.LOCALE, ListItem.FORMAT_STOCK_SHORT, item.getStock()));
        float fPrice = item.getPrice(mPriceIndex) * (1f - item.getDiscount() / 100f);
        price.setText(String.format(ListItem.LOCALE, ListItem.FORMAT_PRICE_SHORT, fPrice));
        sum.setText(String.format(ListItem.LOCALE, ListItem.FORMAT_SUM_SHORT, fPrice * (float) item.getOrder()));
        discount.setText(String.format(ListItem.LOCALE, FORMAT_DISCOUNT, item.getDiscount()));
        updatePriceSum(sum, item, edt);
        edt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                updatePriceSum(sum, item, edt);
            }
        });
        DeviceUtils.showIME(this);
        dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                DeviceUtils.hideIME(edt.getContext(), edt);
                int order = edt.length() == 0 ? 0 : Integer.parseInt(edt.getText().toString());
                setItemOrder(mItems, item.getId(), order, Integer.toString(item.getDiscount()));
                mAdapter.notifyDataSetChanged();
                updateSum();
            }
        });
        dialogBuilder.setNeutralButton("УДАЛИТЬ", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                DeviceUtils.hideIME(edt.getContext(), edt);
                setItemOrder(mItems, item.getId(), 0, null);
                mAdapter.notifyDataSetChanged();
                updateSum();
            }
        });
        dialogBuilder.setNegativeButton("ОТМЕНА", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                DeviceUtils.hideIME(edt.getContext(), edt);
            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }

    private void setItemOrder(List<Item> list, String id, int count, String discount) {
        Item item;
        for (int i = 0, size = list.size(); i < size; i++) {
            item = list.get(i);
            if (!item.isParent() && TextUtils.equals(item.getId(), id)) {
                item.setOrder(count);
                item.setDiscount(discount);
            } else if (item.isParent()) {
                setItemOrder(item.getChilds(), id, count, discount);
            }
        }
    }

    private void updateSum() {
        float total = 0;
        Item item;
        for (int i = 1, size = mItems.size(); i < size; i++) {
            item = mItems.get(i);
            if (!item.isParent() && item.getOrder() > 0) {
                total += item.getOrder() * item.getPrice(mPriceIndex) * (1 - item.getDiscount() / 100f);
            } else if (item.isParent() && item.getChilds() != null && item.getChilds().size() > 0) {
                total += calculateChildSum(item.getChilds());
            }
        }
        mSum.setText(String.format(ListItem.LOCALE, ListItem.FORMAT_NUMBER_ONLY, total));
    }

    private float calculateChildSum(ArrayList<Item> items) {
        float total = 0;
        Item item;
        for (int i = 0, size = items.size(); i < size; i++) {
            item = items.get(i);
            if (!item.isParent() && item.getOrder() > 0) {
                total += item.getOrder() * item.getPrice(mPriceIndex) * (1 - item.getDiscount() / 100f);
            } else if (item.isParent() && item.getChilds() != null && item.getChilds().size() > 0) {
                total += calculateChildSum(item.getChilds());
            }
        }
        return total;
    }

    @OnClick(R.id.group_pbtn_prev)
    void onButtonBackClicked() {
        onBackPressed();
    }

    @OnClick(R.id.prices)
    void onSelectPriceClicked() {
        final CharSequence[] items = new CharSequence[20];
        for (int i = 1; i <= 20; i++) {
            items[i - 1] = PRICE + Integer.toString(i);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Тип цен");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                mPriceIndex = item + 1;
                mPrefs.edit().putString(RsaDb.PRICESELECTED, "Цена " + Integer.toString(mPriceIndex)).commit();
                updatePricesButton();
                mAdapter.setPriceIndex(mPriceIndex);
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onBackPressed() {
        saveOrder();
        moveBack();
    }

    private void moveBack() {
        Intent intent = new Intent();
        intent.putExtra("ORDERH", mOrderH);
        intent.putExtra("TOP", ttop);
        intent.putExtra("INDEX", iidx);
        intent.putExtra("GPOSITION", groupPos);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void saveOrder() {
        Item item;
        ArrayList<OrderLines> orderLines = new ArrayList<OrderLines>();
        for (int i = 1, size = mItems.size(); i < size; i++) {
            item = mItems.get(i);
            if (!item.isParent() && item.getOrder() > 0) {
                orderLines.add(composeLine(item));
            } else if (item.isParent() && item.getChilds() != null && item.getChilds().size() > 0) {
                saveChilds(orderLines, item.getChilds());
            }
        }
        mOrderH.lines = orderLines;
    }

    private void saveChilds(ArrayList<OrderLines> orderLines, ArrayList<Item> items) {
        Item item;
        for (int i = 0, size = items.size(); i < size; i++) {
            item = items.get(i);
            if (!item.isParent() && item.getOrder() > 0) {
                orderLines.add(composeLine(item));
            } else if (item.isParent() && item.getChilds() != null && item.getChilds().size() > 0) {
                saveChilds(orderLines, item.getChilds());
            }
        }
    }

    private OrderLines composeLine(Item item) {
        OrderLines line;
        float price = item.getPrice(mPriceIndex);
        float tmpPricewonds = price / 1.2f;
        float tmpSumwnds = price * item.getOrder();
        float tmpSumwonds = tmpSumwnds / 1.2f;

        line = new OrderLines("0",      // *id used for REST QTY in Shop (Merchandising)
                "",                     // *zakaz_id used in headactivity
                item.getId(),           // goods_id
                item.getTitle(),        // text_goods
                "Price" + Integer.toString(mPriceIndex), // PRICETYPE (Price1,Price2...20)
                Integer.toString(item.getOrder()),  // qty
                "шт",                               // un
                "1",                                // coeff
                Integer.toString(item.getDiscount()), // *discount !not used
                String.format(ListItem.LOCALE, "%.8f", price).replace(',', '.'),  // pricewnds
                String.format(ListItem.LOCALE, "%.8f", tmpSumwnds).replace(',', '.'), // sumwnds
                String.format(ListItem.LOCALE, "%.8f", tmpPricewonds).replace(',', '.'),
                // pricewonds=pricewnds/1.2
                String.format(ListItem.LOCALE, "%.8f", tmpSumwonds).replace(',', '.'),
                // sumwonds=sumwnds/1.2
                String.format(ListItem.LOCALE, "%.8f", tmpSumwnds - tmpSumwonds).replace(',', '.'),
                // nds=sumwnds-sumwonds
                "2",  // must be delay, but overloaded to NDS flag
                "1",                    //weight
                "",                     // comment
                item.getBrand());       // current brandname

        return line;
    }

}