package ua.rsa.gd.adapter;

import java.util.HashMap;

/**
 * Created by Ромка on 17.01.2016.
 */
public class DebitExtItem extends HashMap<String, String> {

    private static final long serialVersionUID = 1L;
    public static final String CUST_ID = "cust_id";
    public static final String CUST_NAME = "cust_name";
    public static final String DATE = "date";
    public static final String TIME = "time";
    public static final String RN = "rn";
    public static final String SUM = "sum";

    public DebitExtItem(String custId, String custName, String date, String time,
            String rn, String sum) {
        super();
        super.put(CUST_ID, custId);
        super.put(CUST_NAME, custName);
        super.put(DATE, date);
        super.put(TIME, time);
        super.put(RN, rn);
        super.put(SUM, sum);
    }
}
