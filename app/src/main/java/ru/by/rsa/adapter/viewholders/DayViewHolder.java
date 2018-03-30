package ru.by.rsa.adapter.viewholders;

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
public class DayViewHolder extends RecyclerView.ViewHolder {

    @Bind(R.id.layout)
    ViewGroup mLayout;
    @Bind(R.id.title)
    TextView mTitle;
    @Bind(R.id.subtitle)
    TextView mSubtitle;

    public DayViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void setTag(int position) {
        mLayout.setTag(position);
    }

    public void setOnClickListener(View.OnClickListener listener) {
        mLayout.setOnClickListener(listener);
    }

    public void setTitle(String text) {
        mTitle.setText(text);
    }

    public void setSubtitle(String text) {
        mSubtitle.setText(text);
    }

    public void setHighlighted(boolean isHighlited) {
        if (isHighlited) {
            mLayout.setBackgroundColor(Color.parseColor("#55FFFF00"));
        } else {
            mLayout.setBackgroundDrawable(null);
        }
    }

}
