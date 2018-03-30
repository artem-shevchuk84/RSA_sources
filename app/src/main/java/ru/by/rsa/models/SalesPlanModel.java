package ru.by.rsa.models;

import android.text.TextUtils;

public class SalesPlanModel {

    private static final String NOT_TOTALS = "0";

    private String mDatetime;
    private String mCustName;
    private String mShopName;
    private String mBrandName;
    private String mPlanSum;
    private String mPlanTopQty;
    private String mPlanQty;
    private String mActSum;
    private String mActTopQty;
    private String mActQty;
    private String mRestSum;
    private String mRestTopQty;
    private String mRestQty;
    private String mTotals;

    public SalesPlanModel(String datetime, String custName, String shopName, String brandName, String planSum,
            String planTopQty, String planQty, String actSum, String actTopQty, String actQty,
            String restSum, String restTopQty, String restQty, String totals) {
        mDatetime = datetime;
        mCustName = custName;
        mShopName = shopName;
        mBrandName = brandName;
        mPlanSum = planSum;
        mPlanTopQty = planTopQty;
        mPlanQty = planQty;
        mActSum = actSum;
        mActTopQty = actTopQty;
        mActQty = actQty;
        mRestSum = restSum;
        mRestTopQty = restTopQty;
        mRestQty = restQty;
        mTotals = totals;
    }

    public String getRestSum() {
        return mRestSum;
    }

    public void setRestSum(String restSum) {
        mRestSum = restSum;
    }

    public String getRestTopQty() {
        return mRestTopQty;
    }

    public void setRestTopQty(String restTopQty) {
        mRestTopQty = restTopQty;
    }

    public String getRestQty() {
        return mRestQty;
    }

    public void setRestQty(String restQty) {
        mRestQty = restQty;
    }

    public String getTotals() {
        return mTotals;
    }

    public void setTotals(String totals) {
        mTotals = totals;
    }

    public boolean isTotals() {
        return TextUtils.equals(mTotals, NOT_TOTALS);
    }

    public String getDatetime() {
        return mDatetime;
    }

    public void setDatetime(String datetime) {
        mDatetime = datetime;
    }

    public String getCustName() {
        return mCustName;
    }

    public void setCustName(String custName) {
        mCustName = custName;
    }

    public String getShopName() {
        return mShopName;
    }

    public void setShopName(String shopName) {
        mShopName = shopName;
    }

    public String getBrandName() {
        return mBrandName;
    }

    public void setBrandName(String brandName) {
        mBrandName = brandName;
    }

    public String getPlanSum() {
        return mPlanSum;
    }

    public void setPlanSum(String planSum) {
        mPlanSum = planSum;
    }

    public String getPlanTopQty() {
        return mPlanTopQty;
    }

    public void setPlanTopQty(String planTopQty) {
        mPlanTopQty = planTopQty;
    }

    public String getPlanQty() {
        return mPlanQty;
    }

    public void setPlanQty(String planQty) {
        mPlanQty = planQty;
    }

    public String getActSum() {
        return mActSum;
    }

    public void setActSum(String actSum) {
        mActSum = actSum;
    }

    public String getActTopQty() {
        return mActTopQty;
    }

    public void setActTopQty(String actTopQty) {
        mActTopQty = actTopQty;
    }

    public String getActQty() {
        return mActQty;
    }

    public void setActQty(String actQty) {
        mActQty = actQty;
    }
}
