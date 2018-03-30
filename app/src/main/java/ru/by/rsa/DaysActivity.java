package ru.by.rsa;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ru.by.rsa.adapter.DaysAdapter;
import ru.by.rsa.models.DayModel;

/**
 * Created on 26.03.2016 by Roman Komarov (neo33da@gmail.com)
 */
public class DaysActivity extends Activity implements DaysAdapter.OnItemClickListener {

    private static final String TAG = "DaysActivity";

    @Bind(R.id.recyclerView)
    RecyclerView mRecyclerView;

    private SharedPreferences mPrefs;
    private DaysAdapter mAdapter;
    private boolean mLightTheme;


    public static void start(Context context) {
        Intent intent = new Intent(context, DaysActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPrefs = getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE);
        mLightTheme = mPrefs.getBoolean(RsaDb.LIGHTTHEMEKEY, false);
        setTheme(mLightTheme ? R.style.Theme_Custom : R.style.Theme_CustomBlack2);
        setContentView(mLightTheme ? R.layout.l_activity_days : R.layout.activity_days);
        ButterKnife.bind(this);
        initList();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }

    private void initList() {
        mAdapter = new DaysAdapter(this, populateDays(), this, mLightTheme);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mAdapter);
    }

    @NonNull
    private List<DayModel> populateDays() {
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
        List<DayModel> list = new ArrayList<DayModel>(7);
        int[] outlets = countOutletsByDay();
        Calendar calendar = Calendar.getInstance();
        int currentDay = calendar.get(Calendar.DAY_OF_WEEK);
        calendar.set(2016, 2, 21); // monday
        for (int i = 0; i < 7; i++) {
            list.add(new DayModel(
                    dayFormat.format(calendar.getTime()),
                    outlets[i],
                    calendar.get(Calendar.DAY_OF_WEEK) == currentDay,
                    calendar.get(Calendar.DAY_OF_WEEK)
            ));
            calendar.add(Calendar.DATE, 1);
        }
        return list;
    }

    private int[] countOutletsByDay() {
        Calendar c = Calendar.getInstance();
        int[] result = new int[]{0, 0, 0, 0, 0, 0, 0};
        SimpleDateFormat fmt = new SimpleDateFormat("ddMMyyyy", Locale.getDefault());
        RsaDbHelper mDb = new RsaDbHelper(this,
                mPrefs.getString(RsaDb.ACTUALDBKEY, RsaDbHelper.DB_NAME1));
        SQLiteDatabase db = mDb.getReadableDatabase();
        String query = "select DATEV from _plan group by DATEV, CUST_ID, SHOP_ID";
        Cursor cursor = db.rawQuery(query, null);
        try {
            while (cursor.moveToNext()) {
                c.setTime(fmt.parse(cursor.getString(0)));
                switch (c.get(Calendar.DAY_OF_WEEK)) {
                    case Calendar.MONDAY:
                        result[0]++;
                        break;
                    case Calendar.TUESDAY:
                        result[1]++;
                        break;
                    case Calendar.WEDNESDAY:
                        result[2]++;
                        break;
                    case Calendar.THURSDAY:
                        result[3]++;
                        break;
                    case Calendar.FRIDAY:
                        result[4]++;
                        break;
                    case Calendar.SATURDAY:
                        result[5]++;
                        break;
                    case Calendar.SUNDAY:
                        result[6]++;
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        cursor.close();
        db.close();
        return result;
    }

    @OnClick(R.id.back)
    void onBack() {
        onBackPressed();
    }

    @Override
    public void onItemClicked(DayModel item, int position) {
        Intent intent = new Intent();
        intent.setClass(getApplicationContext(), PlanActivity.class);
        intent.putExtra(PlanActivity.EXTRA_CALL_FROM_DAYS_ACTIVITY, item.getDayOfWeek());
        startActivityForResult(intent, 0);
    }
}
