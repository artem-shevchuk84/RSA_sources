package ua.rsa.gd.models;

import android.support.annotation.Nullable;

/**
 * Created on 27.03.2016 by Roman Komarov (neo33da@gmail.com)
 */
public class DayModel {

    private String mName;
    private String mOutletCount;
    private boolean mHighlited;
    private int mDayOfWeek;

    public DayModel(String name, int outletCount, boolean highlited, int dayOfWeek) {
        mName = name;
        mOutletCount = Integer.toString(outletCount);
        mHighlited = highlited;
        mDayOfWeek = dayOfWeek;
    }

    @Nullable
    public String getName() {
        return mName;
    }

    public void setName(@Nullable String name) {
        mName = name;
    }

    @Nullable
    public String getOutletCount() {
        return mOutletCount;
    }

    public void setOutletCount(@Nullable String outletCount) {
        mOutletCount = outletCount;
    }

    public void setOutletCount(int outletCount) {
        mOutletCount = Integer.toString(outletCount);
    }

    public void setHighlited(boolean highlited) {
        mHighlited = highlited;
    }

    public boolean isHighlited() {
        return mHighlited;
    }

    public int getDayOfWeek() {
        return mDayOfWeek;
    }
}
