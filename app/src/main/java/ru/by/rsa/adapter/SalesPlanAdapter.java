package ru.by.rsa.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.List;
import java.util.Locale;

import ru.by.rsa.R;
import ru.by.rsa.adapter.viewholders.SalesPlanViewHolder;
import ru.by.rsa.models.SalesPlanModel;

public class SalesPlanAdapter extends RecyclerView.Adapter<SalesPlanViewHolder> {

    private static final String PLAN_FORMAT = "%s/%s\nD=%s";

    private List<SalesPlanModel> mItems;
    private LayoutInflater mInflater;
    private int mState = SalesPlanViewHolder.STATE_NOTHING_SELECTED;
    private boolean mIsShowPlanQty = false;

    public SalesPlanAdapter(@NonNull Context context, @NonNull List<SalesPlanModel> list, boolean isShowPlanQty) {
        mInflater = LayoutInflater.from(context);
        mItems = list;
        mIsShowPlanQty = isShowPlanQty;
    }

    public void setItems(List<SalesPlanModel> items, int state ) {
        mItems = items;
        mState = state;
        notifyDataSetChanged();
    }

    public void setState(int state) {
        mState = state;
        notifyDataSetChanged();
    }

    @Override
    public SalesPlanViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SalesPlanViewHolder(mInflater.inflate(R.layout.item_sales_plan, parent, false));
    }

    @Override
    public int getItemCount() {
        return (mItems == null) ? 0 : mItems.size();
    }

    @Override
    public void onBindViewHolder(SalesPlanViewHolder holder, int position) {
        SalesPlanModel item = mItems.get(position);

        holder.setCust(item.getCustName());
        holder.setShop(item.getShopName());
        holder.setBrand(item.getBrandName());

        holder.setPlanSum(formatPlan(item.getPlanSum(), item.getActSum(), item.getRestSum()));
        holder.setPlanTopQty(formatPlan(item.getPlanTopQty(), item.getActTopQty(), item.getRestTopQty()));
        holder.setPlanQty(formatPlan(item.getPlanQty(), item.getActQty(), item.getRestQty()));

        holder.setHighlight(item.isTotals());
        holder.setState(mState);
        holder.setPlanQtyVisibility(mIsShowPlanQty);
    }

    private static String formatPlan(String plan, String actual, String rest) {
        if (TextUtils.isEmpty(actual)) {
            return plan;
        }
        return String.format(Locale.getDefault(), PLAN_FORMAT, plan, actual, rest);
    }
}
