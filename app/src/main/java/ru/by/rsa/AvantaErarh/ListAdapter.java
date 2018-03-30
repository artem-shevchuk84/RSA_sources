package ru.by.rsa.AvantaErarh;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Locale;

import ru.by.rsa.R;

/**
 * Created on 8/13/16 by Roman Komarov (0503412392)
 */
public class ListAdapter extends BaseAdapter {

    public static final Locale LOCALE = Locale.getDefault();

    public interface OnProductClickListener {

        void onProductClicked(Item item);
    }

    private class Pair {

        Item item;
        int level;

        Pair(Item item, int level) {
            this.item = item;
            this.level = level;
        }
    }

    private OnProductClickListener mCallback;
    private LayoutInflater mLayoutInflater;
    private ArrayList<Pair> mHierarchyArray;
    private ArrayList<Item> mOriginalItems;
    private LinkedList<Item> mOpenItems;
    private int mPriceIndex;
    private boolean mLightTheme;
    private int mColorOrdered;

    public ListAdapter(Context ctx, ArrayList<Item> items, int priceIndex,
            OnProductClickListener callback, boolean lightTheme) {
        mCallback = callback;
        mLayoutInflater = LayoutInflater.from(ctx);
        mOriginalItems = items;
        mHierarchyArray = new ArrayList<Pair>();
        mOpenItems = new LinkedList<Item>();
        mLightTheme = lightTheme;
        mColorOrdered = Color.parseColor(mLightTheme ? "#30000000" : "#30FFFFFF");
        mPriceIndex = priceIndex;
        generateHierarchy();
    }

    public void setPriceIndex(int priceIndex) {
        mPriceIndex = priceIndex;
        notifyDataSetChanged();
    }

    private void generateHierarchy() {
        mHierarchyArray.clear();
        generateList(mOriginalItems, 0);
    }

    private void generateList(ArrayList<Item> items, int level) {
        for (Item i : items) {
            mHierarchyArray.add(new Pair(i, level));
            if (mOpenItems.contains(i))
                generateList(i.getChilds(), level + 1);
        }
    }

    public void clickOnItem(int position) {
        Item i = mHierarchyArray.get(position).item;
        if (!i.isParent() && mCallback != null) {
            mCallback.onProductClicked(i);
        } else {
            if (!closeItem(i)) {
                mOpenItems.add(i);
            }
            generateHierarchy();
            notifyDataSetChanged();
        }
    }

    private boolean closeItem(Item i) {
        if (mOpenItems.remove(i)) {
            for (Item c : i.getChilds()) {
                closeItem(c);
            }
            return true;
        }
        return false;
    }

    @Override
    public int getCount() {
        return mHierarchyArray.size();
    }

    @Override
    public Object getItem(int position) {
        return mHierarchyArray.get(position).item;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = mLayoutInflater.inflate(mLightTheme ? R.layout.l_row : R.layout.row, null);

        View layout = convertView.findViewById(R.id.layout);
        TextView title = (TextView) convertView.findViewById(R.id.title);
        TextView price = (TextView) convertView.findViewById(R.id.price);
        TextView stock = (TextView) convertView.findViewById(R.id.stock);
        TextView order = (TextView) convertView.findViewById(R.id.order);

        Pair pair = mHierarchyArray.get(position);
        boolean isParent = pair.item.isParent();
        if (pair.item.getOrder() == 0 || pair.item.isParent()) {
            layout.setBackgroundColor(pair.item.getBackgroundColor());
        } else {
            layout.setBackgroundColor(mColorOrdered);
        }
        title.setText(pair.item.getTitle());
        float fDiscount = pair.item.getDiscount() / 100f;
        float fPrice = pair.item.getPrice(mPriceIndex) * (1 - fDiscount);
        price.setText(isParent ? null : String.format(LOCALE, ListItem.FORMAT_PRICE, fPrice));
        order.setText(isParent ? null : pair.item.getOrderText());
        stock.setText(isParent ? null : pair.item.getStockText());
        title.setPadding(pair.level * 8, 0, 0, 0);
        price.setPadding(pair.level * 8, 0, 0, 0);
        if (pair.item.isTopSku()) {
            title.setTypeface(Typeface.DEFAULT_BOLD);
            title.setTextColor(pair.item.inHistory() ? Color.BLUE : Color.RED);
        } else {
            title.setTypeface(Typeface.DEFAULT);
            title.setTextColor(mLightTheme ? Color.BLACK : Color.LTGRAY);
        }
        return convertView;
    }
}