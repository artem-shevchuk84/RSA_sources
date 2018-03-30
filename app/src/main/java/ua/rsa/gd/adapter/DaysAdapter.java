package ua.rsa.gd.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import ru.by.rsa.R;
import ua.rsa.gd.adapter.viewholders.DayViewHolder;
import ua.rsa.gd.models.DayModel;

/**
 * Created on 27.03.2016 by Roman Komarov (neo33da@gmail.com)
 */
public class DaysAdapter extends RecyclerView.Adapter<DayViewHolder> implements View.OnClickListener {

    private static final String TAG = "DaysAdapter";
    private static final String OUTLETS_COUNT = " торговых точек";

    private List<DayModel> mItems;
    private LayoutInflater mInflater;
    private OnItemClickListener mListener;
    private boolean mLightTheme;

    public interface OnItemClickListener {
        void onItemClicked(DayModel item, int position);
    }

    public DaysAdapter(@NonNull Context context, @NonNull List<DayModel> list,
                       @Nullable OnItemClickListener listener, boolean lightTheme) {
        mInflater = LayoutInflater.from(context);
        mItems = list;
        mListener = listener;
        mLightTheme = lightTheme;
    }

    public void itemRemoved(int position) {
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, mItems.size()); // fixing positioning tags
    }

    @Override
    public DayViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new DayViewHolder(mInflater.inflate(mLightTheme
                ? R.layout.l_item_day
                : R.layout.item_day
                , parent, false));
    }

    @Override
    public int getItemCount() {
        return (mItems == null) ? 0 : mItems.size();
    }

    @Override
    public void onClick(View v) {
        if (mListener != null) {
            int position = (Integer) v.getTag();
            switch (v.getId()) {
                case R.id.layout:
                    v.setBackgroundColor(Color.parseColor("#d3d3d3"));
                    mListener.onItemClicked(mItems.get(position), position);
                    break;
            }
        }
    }

    @Override
    public void onBindViewHolder(DayViewHolder holder, int position) {
        DayModel item = mItems.get(position);

        holder.setTag(position);
        holder.setOnClickListener(this);
        holder.setTitle(item.getName());
        holder.setSubtitle(item.getOutletCount() + OUTLETS_COUNT);
        holder.setHighlighted(item.isHighlited());
    }
}
