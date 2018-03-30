package ua.rsa.gd.AvantaErarh;

import android.graphics.Color;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created on 8/13/16 by Roman Komarov (0503412392)
 */
public class ListItem implements Item {

    private static final int COLOR_FOLDER = Color.parseColor("#220000FF");
    private static final int COLOR_TOP_FOLDER = Color.parseColor("#3300FF00");
    private static final int COLOR_ITEM = Color.parseColor("#00FFFFFF");
    public static final String FORMAT_PRICE = "ЦЕНА: %.2f грн.";
    private static final String FORMAT_ORDER = "ЗАК: %d шт.";
    private static final String FORMAT_STOCK = "ОСТ: %.0f шт.";
    public static final String FORMAT_STOCK_SHORT = "ОСТ: %.0f";
    public static final String FORMAT_PRICE_SHORT = "ЦЕНА: %.2f";
    public static final String FORMAT_SUM_SHORT = "%.2f грн.";
    public static final String FORMAT_NUMBER_ONLY = "%.2f";
    public static final Locale LOCALE = Locale.getDefault();

    private String mId;
    private String mTitle;
    private float[] mPrices = new float[20];
    private int mOrder;
    private float mStock;
    private String mOrderText;
    private String mStockText;
    private boolean mParent;
    private String mParentId;
    private ArrayList<Item> mChilds;
    private boolean mTopSku;
    private String mNds;
    private float mFloatNds;
    private String mBrand;
    private boolean mTopLevel;
    private int mDiscount;
    private boolean mInHistory;

    public ListItem(String id, String title, float[] prices, float stock, int order,
            boolean isParent, String parentId, boolean topSku, String nds, String brand) {
        mId = id;
        mParent = isParent;
        mTitle = title;
        mParentId = parentId;
        setPrices(prices);
        setStock(stock);
        setOrder(order);
        mChilds = new ArrayList<Item>();
        mTopSku = topSku;
        mBrand = brand;
        mNds = nds;
        if (!TextUtils.isEmpty(nds)) {
            mFloatNds = Float.parseFloat(nds);
        }
    }

    @Override
    public int getDiscount() {
        return mDiscount;
    }

    @Override
    public void setDiscount(int discount) {
        mDiscount = discount;
    }

    @Override
    public void setDiscount(String discount) {
        mDiscount = TextUtils.isEmpty(discount) ? 0 : Integer.parseInt(discount);
    }

    @Override
    public boolean isTopSku() {
        return mTopSku;
    }

    public String getNds() {
        return mNds;
    }

    @Override
    public String getBrand() {
        return mBrand;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    @Override
    public String getTitle() {
        return mTitle;
    }

    public boolean isParent() {
        return mParent;
    }

    public boolean isTopLevel() {
        return mTopLevel;
    }

    public void setTopLevel(boolean topLevel) {
        mTopLevel = topLevel;
    }

    public void setParent(boolean parent) {
        mParent = parent;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public void setPrices(float[] prices) {
        mPrices = prices;
    }

    public void setPrice(int index, float price) {
        mPrices[index] = price;
    }

    public void setOrder(int order) {
        mOrder = order;
        mOrderText = String.format(LOCALE, FORMAT_ORDER, order);
    }

    public void setStock(float stock) {
        mStock = stock;
        mStockText = String.format(LOCALE, FORMAT_STOCK, stock);
    }

    @Override
    public float getPrice(int index) {
        float price = mPrices[index <= 1 ? 0 : index - 1];
        return price + price * mFloatNds;
    }

    @Override
    public int getOrder() {
        return mOrder;
    }

    @Override
    public float getStock() {
        return mStock;
    }

    public String getOrderText() {
        return mOrderText;
    }

    public String getStockText() {
        return mStockText;
    }

    public String getParentId() {
        return mParentId;
    }

    public boolean inHistory() {
        return mInHistory;
    }

    public void setInHistory(boolean inHistory) {
        mInHistory = inHistory;
    }

    @Override
    public ArrayList<Item> getChilds() {
        return mChilds;
    }

    @Override
    public int getBackgroundColor() {
        if (mTopLevel && mChilds.size() > 0) {
            return COLOR_TOP_FOLDER;
        }
        return mChilds.size() > 0 ? COLOR_FOLDER : COLOR_ITEM;
    }

    public void addChild(Item item) {
        mChilds.add(item);
    }
}
