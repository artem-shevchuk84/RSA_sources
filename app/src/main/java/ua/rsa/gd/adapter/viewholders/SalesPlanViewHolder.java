package ua.rsa.gd.adapter.viewholders;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import ru.by.rsa.R;

/**
 * Created on 27.03.2016 by Roman Komarov (neo33da@gmail.com)
 */
public class SalesPlanViewHolder extends RecyclerView.ViewHolder {

    public static final int STATE_NOTHING_SELECTED = 0;
    public static final int STATE_BRAND_ONLY = 1;
    public static final int STATE_CUST_ONLY = 2;
    public static final int STATE_CUST_AND_BRAND = 3;
    public static final int STATE_ALL_SELECTED = 4;
    public static final int STATE_CUST_AND_SHOP = 5;

    @Bind(R.id.layout)
    ViewGroup mLayout;
    @Bind(R.id.cust)
    TextView mCust;
    @Bind(R.id.shop)
    TextView mShop;
    @Bind(R.id.brand)
    TextView mBrand;
    @Bind(R.id.planSum)
    TextView mPlanSum;
    @Bind(R.id.planTopQty)
    TextView mPlanTopQty;
    @Bind(R.id.planQty)
    TextView mPlanQty;

    int mHighlightColor;
    int mDefaultColor;

    public SalesPlanViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        mHighlightColor = Color.parseColor("#333333");
        mDefaultColor = Color.parseColor("#000000");
    }

    public void setCust(String text) {
        mCust.setText(text);
    }

    public void setShop(String text) {
        mShop.setText(text);
    }

    public void setBrand(String text) {
        mBrand.setText(text);
    }

    public void setPlanSum(String text) {
        mPlanSum.setText(text);
    }

    public void setPlanTopQty(String text) {
        mPlanTopQty.setText(text);
    }

    public void setPlanQty(String text) {
        mPlanQty.setText(text);
    }

    public void setHighlight(boolean isHighlight) {
        mLayout.setBackgroundColor(isHighlight ? mHighlightColor : mDefaultColor);
    }

    public void setPlanQtyVisibility(boolean isVisible) {
        mPlanQty.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    public void setState(int newState) {
        mCust.setVisibility(View.VISIBLE);
        mShop.setVisibility(View.VISIBLE);
        mBrand.setVisibility(View.VISIBLE);
        mPlanSum.setVisibility(View.VISIBLE);
        mPlanTopQty.setVisibility(View.VISIBLE);
        mPlanQty.setVisibility(View.VISIBLE);
        switch (newState) {
            case STATE_ALL_SELECTED:
                mCust.setVisibility(View.GONE);
                mShop.setVisibility(View.GONE);
                mBrand.setVisibility(View.GONE);
                break;
            case STATE_CUST_ONLY:
                mCust.setVisibility(View.GONE);
                break;
            case STATE_BRAND_ONLY:
                mBrand.setVisibility(View.GONE);
                break;
            case STATE_CUST_AND_BRAND:
                mCust.setVisibility(View.GONE);
                mBrand.setVisibility(View.GONE);
                break;
            case STATE_CUST_AND_SHOP:
                mCust.setVisibility(View.GONE);
                mShop.setVisibility(View.GONE);
                break;
        }
    }
}
