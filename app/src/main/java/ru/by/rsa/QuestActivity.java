package ru.by.rsa;

import android.app.Activity;
import android.app.ListActivity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.OnHierarchyChangeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class QuestActivity extends Activity {

    private static int verOS = 0;
    private boolean lightTheme;
    private Bundle extras;

    boolean answersBool[];
    String newAnswers[];
    String comments[];
    String data[];

    boolean fromSavedState;
    private boolean mDebtListFilled;

    LinearLayout line1;
    LinearLayout line2;
    LinearLayout line3;
    LinearLayout line4;
    LinearLayout line5;
    LinearLayout line6;

    LinearLayout debtList;

    CheckBox chk1;
    CheckBox chk2;
    CheckBox chk3;
    CheckBox chk4;
    CheckBox chk5;
    CheckBox chk6;

    TextView txt_q1;
    TextView txt_q2;
    TextView txt_q3;
    TextView txt_q4;
    TextView txt_q5;
    TextView txt_q6;

    EditText edt1;
    EditText edt10;
    EditText edt2;
    EditText edt20;
    EditText edt30;
    EditText edt4;
    EditText edt40;
    EditText edt5;
    EditText edt50;
    EditText edt6;
    EditText edt60;

    Button btnSave;

    Spinner comboTypes;

    OrderHead mOrderH;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        verOS = 2;
        try {
            verOS = Integer.parseInt(Build.VERSION.RELEASE.substring(0, 1));
        } catch (Exception e) {
        }
        ;

        lightTheme = getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE).getBoolean(
                RsaDb.LIGHTTHEMEKEY, false);
        if (lightTheme) {
            //setTheme(R.style.Theme_Custom);
            super.onCreate(savedInstanceState);
            setContentView(R.layout.quest);
        } else {
            //setTheme(R.style.Theme_CustomBlackNoTitleBar);
            super.onCreate(savedInstanceState);
            setContentView(R.layout.quest);
        }

        if (savedInstanceState != null) {
            extras = savedInstanceState;
            answersBool = extras.getBooleanArray("answersBool");
            newAnswers = extras.getStringArray("newAnswers");
            comments = extras.getStringArray("comments");
            Log.i("QuestActivity", "import from saved instance");
            fromSavedState = true;
        } else {
            fromSavedState = false;
            extras = getIntent().getExtras();
            answersBool = new boolean[6];
            newAnswers = new String[6];
            comments = new String[6];
            for (int i = 0; i < 6; i++) {
                newAnswers[i] = "";
                comments[i] = "";
            }
        }
        mOrderH = (OrderHead) extras.getParcelable("ORDERH");

        btnSave = (Button) findViewById(R.id.quest_btn_confirm);
        btnSave.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                performAddQuestionnaire();
                performExit();

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void updateData() {
        String type = "";
        if (fromSavedState == true) {
            chk1.setChecked(answersBool[0]);
            line1.setVisibility(answersBool[0] ? View.GONE : View.VISIBLE);
            chk2.setChecked(answersBool[1]);
            line2.setVisibility(answersBool[1] ? View.GONE : View.VISIBLE);
            chk3.setChecked(answersBool[2]);
            line3.setVisibility(answersBool[2] ? View.GONE : View.VISIBLE);
            chk4.setChecked(answersBool[3]);
            line4.setVisibility(answersBool[3] ? View.GONE : View.VISIBLE);
            chk5.setChecked(answersBool[4]);
            line5.setVisibility(answersBool[4] ? View.GONE : View.VISIBLE);
            chk6.setChecked(answersBool[5]);
            line6.setVisibility(answersBool[5] ? View.GONE : View.VISIBLE);

            edt1.setText(newAnswers[0]);
            edt10.setText(comments[0]);
            edt2.setText(newAnswers[1]);
            edt20.setText(comments[1]);
            edt30.setText(comments[2]);
            edt4.setText(newAnswers[3]);
            edt40.setText(comments[3]);
            edt5.setText(newAnswers[4]);
            edt50.setText(comments[4]);
            edt6.setText(newAnswers[5]);
            edt60.setText(comments[5]);
        } else {
            RsaDbHelper mDb_ord = new RsaDbHelper(getApplicationContext(), RsaDbHelper.DB_ORDERS);
            SQLiteDatabase db_ord = mDb_ord.getWritableDatabase();
            String q = "select QUESTION_ID, CORRECT, ANSWER, COMMENT from _quest where ZAKAZ_ID='"
                    + mOrderH._id + "' or ZAKAZ_ID='new'";
            Cursor cursorQuest = db_ord.rawQuery(q, null);

            if (cursorQuest.getCount() > 0) {
                while (cursorQuest.moveToNext()) {
                    if (cursorQuest.getString(0).equals("1")) {
                        chk1.setChecked(cursorQuest.getString(1).equals("1"));
                        line1.setVisibility(chk1.isChecked() ? View.GONE : View.VISIBLE);
                        edt1.setText(cursorQuest.getString(2));
                        edt10.setText(cursorQuest.getString(3));
                    } else if (cursorQuest.getString(0).equals("2")) {
                        chk2.setChecked(cursorQuest.getString(1).equals("1"));
                        line2.setVisibility(chk2.isChecked() ? View.GONE : View.VISIBLE);
                        edt2.setText(cursorQuest.getString(2));
                        edt20.setText(cursorQuest.getString(3));
                    } else if (cursorQuest.getString(0).equals("3")) {
                        chk3.setChecked(cursorQuest.getString(1).equals("1"));
                        line3.setVisibility(chk3.isChecked() ? View.GONE : View.VISIBLE);
                        if (chk3.isChecked() == false) {
                            type = cursorQuest.getString(2);
                        }
                        edt30.setText(cursorQuest.getString(3));
                    } else if (cursorQuest.getString(0).equals("4")) {
                        chk4.setChecked(cursorQuest.getString(1).equals("1"));
                        line4.setVisibility(chk4.isChecked() ? View.GONE : View.VISIBLE);
                        edt4.setText(cursorQuest.getString(2));
                        edt40.setText(cursorQuest.getString(3));
                    } else if (cursorQuest.getString(0).equals("5")) {
                        chk5.setChecked(cursorQuest.getString(1).equals("1"));
                        line5.setVisibility(chk5.isChecked() ? View.GONE : View.VISIBLE);
                        edt5.setText(cursorQuest.getString(2));
                        edt50.setText(cursorQuest.getString(3));
                    } else if (cursorQuest.getString(0).equals("6")) {
                        chk6.setChecked(cursorQuest.getString(1).equals("1"));
                        line6.setVisibility(chk6.isChecked() ? View.GONE : View.VISIBLE);
                        edt6.setText(cursorQuest.getString(2));
                        edt60.setText(cursorQuest.getString(3));
                    }
                }
            }

            if (cursorQuest != null && !cursorQuest.isClosed()) {
                cursorQuest.close();
            }

            if (db_ord != null && db_ord.isOpen()) {
                db_ord.close();
            }
        }

        fillCurrentDataFromDB(type);
    }

    private void fillCurrentDataFromDB(String t) {
        SharedPreferences prefs = getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);
        RsaDbHelper mDb = new RsaDbHelper(this,
                prefs.getString(RsaDb.ACTUALDBKEY, RsaDbHelper.DB_NAME1));
        SQLiteDatabase db = mDb.getReadableDatabase();
        String queryshop = "select ID, CUST_ID, NAME, ADDRESS, TYPE, CONTACT, TEL " +
                "from _shop " +
                "where ID='" + mOrderH.shop_id + "' " +
                "and CUST_ID='" + mOrderH.cust_id + "' " +
                "limit 1";
        Cursor cursorshop = db.rawQuery(queryshop, null);

        if (cursorshop.getCount() > 0) {
            cursorshop.moveToFirst();
            txt_q1.setText("1. ФИО ЧП(ТТ): " + mOrderH.cust_text);
            txt_q2.setText("2. Адрес ЧП(ТТ): " + mOrderH.shop_text);
            txt_q3.setText("3. Категория ТТ: " + cursorshop.getString(4));
            txt_q4.setText("4. Сумма долга: " + String.format("%.2f грн.", mOrderH.debit_actual)
                    .replace(',', '.'));
            txt_q5.setText("5. Контактное лицо: " + cursorshop.getString(5));
            txt_q6.setText("6. Контактный телефон: " + cursorshop.getString(6));

        }
        if (cursorshop != null && !cursorshop.isClosed()) {
            cursorshop.close();
        }
        //////////////////////////start debitlist
        if (!mDebtListFilled) {
            mDebtListFilled = true;
            String querydebit = "select rn, sum from _debit " +
                    "where CUST_ID='" + mOrderH.cust_id + "' " +
                    "and SHOP_ID='" + mOrderH.shop_id + "' " +
                    "and CLOSED='2'";
            Cursor cursordebit = db.rawQuery(querydebit, null);
            TextView valueView;
            while (cursordebit.moveToNext()) {
                valueView = new TextView(this);
                valueView.setText(cursordebit.getString(0) + " = " + cursordebit.getString(1));
                valueView.setLayoutParams(
                        new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT));
                debtList.addView(valueView);
            }

            if (cursordebit != null && !cursordebit.isClosed()) {
                cursordebit.close();
            }
        }
        //////////////////////////end debitlist
        String querytype = "select NAME from _stype";
        Cursor cursortype = db.rawQuery(querytype, null);
        int count = cursortype.getCount();
        data = new String[count + 1];
        data[0] = "";
        if (count > 0) {
            int i = 1;
            while (cursortype.moveToNext()) {
                data[i] = cursortype.getString(0);
                i++;
            }
        }
        if (cursortype != null && !cursortype.isClosed()) {
            cursortype.close();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, data);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        comboTypes.setAdapter(adapter);
        comboTypes.setPrompt("Выберите тип ТТ");
        int ix = 0;
        for (int i = 0; i < data.length; i++) {
            if (data[i].equals(t)) {
                ix = i;
                break;
            }
        }
        comboTypes.setSelection(ix);
        comboTypes.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                newAnswers[2] = data[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        if (db != null && db.isOpen()) {
            db.close();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        comboTypes = (Spinner) findViewById(R.id.quest_spr3);

        line1 = (LinearLayout) findViewById(R.id.quest_line1);
        line2 = (LinearLayout) findViewById(R.id.quest_line2);
        line3 = (LinearLayout) findViewById(R.id.quest_line3);
        line4 = (LinearLayout) findViewById(R.id.quest_line4);
        line5 = (LinearLayout) findViewById(R.id.quest_line5);
        line6 = (LinearLayout) findViewById(R.id.quest_line6);

        debtList = (LinearLayout) findViewById(R.id.debtList);

        edt1 = (EditText) findViewById(R.id.editText1);
        edt10 = (EditText) findViewById(R.id.editText10);
        edt2 = (EditText) findViewById(R.id.editText2);
        edt20 = (EditText) findViewById(R.id.editText20);
        edt30 = (EditText) findViewById(R.id.editText30);
        edt4 = (EditText) findViewById(R.id.editText4);
        edt40 = (EditText) findViewById(R.id.editText40);
        edt5 = (EditText) findViewById(R.id.editText5);
        edt50 = (EditText) findViewById(R.id.editText50);
        edt6 = (EditText) findViewById(R.id.editText6);
        edt60 = (EditText) findViewById(R.id.editText60);

        txt_q1 = (TextView) findViewById(R.id.quest_q1);
        txt_q2 = (TextView) findViewById(R.id.quest_q2);
        txt_q3 = (TextView) findViewById(R.id.quest_q3);
        txt_q4 = (TextView) findViewById(R.id.quest_q4);
        txt_q5 = (TextView) findViewById(R.id.quest_q5);
        txt_q6 = (TextView) findViewById(R.id.quest_q6);

        chk1 = (CheckBox) findViewById(R.id.checkBox1);
        chk2 = (CheckBox) findViewById(R.id.checkBox2);
        chk3 = (CheckBox) findViewById(R.id.checkBox3);
        chk4 = (CheckBox) findViewById(R.id.checkBox4);
        chk5 = (CheckBox) findViewById(R.id.checkBox5);
        chk6 = (CheckBox) findViewById(R.id.checkBox6);

        chk1.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                line1.setVisibility(isChecked ? View.GONE : View.VISIBLE);
                answersBool[0] = isChecked;
            }
        });
        chk2.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                line2.setVisibility(isChecked ? View.GONE : View.VISIBLE);
                answersBool[1] = isChecked;
            }
        });
        chk3.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                line3.setVisibility(isChecked ? View.GONE : View.VISIBLE);
                answersBool[2] = isChecked;
            }
        });
        chk4.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                line4.setVisibility(isChecked ? View.GONE : View.VISIBLE);
                answersBool[3] = isChecked;
            }
        });
        chk5.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                line5.setVisibility(isChecked ? View.GONE : View.VISIBLE);
                answersBool[4] = isChecked;
            }
        });
        chk6.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                line6.setVisibility(isChecked ? View.GONE : View.VISIBLE);
                answersBool[5] = isChecked;
            }
        });

        updateData();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable("ORDERH", extras.getParcelable("ORDERH"));
        outState.putBooleanArray("answersBool", answersBool);
        outState.putStringArray("newAnswers", newAnswers);
        outState.putStringArray("comments", comments);
    }

    private void performExit() {
        OrderHead mOrderH = extras.getParcelable("ORDERH");

        Intent intent = new Intent();

        if (mOrderH != null) {
            Bundle b = new Bundle();
            b.putInt("MODE", mOrderH.mode);
            b.putString("_id", mOrderH._id);
            b.putString("SKLADID", mOrderH.sklad_id.toString());
            b.putString("REMARK", mOrderH.remark.toString());
            b.putString("DELAY", mOrderH.delay.toString());
            b.putString("DISCOUNT", mOrderH.id.toString());
            intent.putExtra("ORDERH", mOrderH);
            intent.putExtras(b);
        }
        setResult(RESULT_OK, intent);

        finish();
    }

    private void performAddQuestionnaire() {
        RsaDbHelper mDb_ord = new RsaDbHelper(getApplicationContext(), RsaDbHelper.DB_ORDERS);
        SQLiteDatabase db = mDb_ord.getWritableDatabase();

        db.execSQL("delete from _quest where ZAKAZ_ID='new' or ZAKAZ_ID='" + mOrderH._id + "'");

        ContentValues values = new ContentValues();
        values.put(RsaDbHelper.QUEST_ZAKAZ_ID, "new");
        values.put(RsaDbHelper.QUEST_QUESTION_ID, "1");
        values.put(RsaDbHelper.QUEST_QUESTION_TEXT, "ФИО ЧП(ТТ)");
        values.put(RsaDbHelper.QUEST_CORRECT, chk1.isChecked() ? "1" : "0");
        values.put(RsaDbHelper.QUEST_ANSWER, edt1.getText().toString());
        values.put(RsaDbHelper.QUEST_COMMENT, edt10.getText().toString());
        db.insert(RsaDbHelper.TABLE_QUEST, RsaDbHelper.QUEST_COMMENT, values);

        values.clear();
        values.put(RsaDbHelper.QUEST_ZAKAZ_ID, "new");
        values.put(RsaDbHelper.QUEST_QUESTION_ID, "2");
        values.put(RsaDbHelper.QUEST_QUESTION_TEXT, "Адрес ЧП(ТТ)");
        values.put(RsaDbHelper.QUEST_CORRECT, chk2.isChecked() ? "1" : "0");
        values.put(RsaDbHelper.QUEST_ANSWER, edt2.getText().toString());
        values.put(RsaDbHelper.QUEST_COMMENT, edt20.getText().toString());
        db.insert(RsaDbHelper.TABLE_QUEST, RsaDbHelper.QUEST_COMMENT, values);

        values.clear();
        values.put(RsaDbHelper.QUEST_ZAKAZ_ID, "new");
        values.put(RsaDbHelper.QUEST_QUESTION_ID, "3");
        values.put(RsaDbHelper.QUEST_QUESTION_TEXT, "Категория ТТ");
        values.put(RsaDbHelper.QUEST_CORRECT, chk3.isChecked() ? "1" : "0");
        values.put(RsaDbHelper.QUEST_ANSWER, newAnswers[2]);
        values.put(RsaDbHelper.QUEST_COMMENT, edt30.getText().toString());
        db.insert(RsaDbHelper.TABLE_QUEST, RsaDbHelper.QUEST_COMMENT, values);

        values.clear();
        values.put(RsaDbHelper.QUEST_ZAKAZ_ID, "new");
        values.put(RsaDbHelper.QUEST_QUESTION_ID, "4");
        values.put(RsaDbHelper.QUEST_QUESTION_TEXT, "Сумма долга");
        values.put(RsaDbHelper.QUEST_CORRECT, chk4.isChecked() ? "1" : "0");
        values.put(RsaDbHelper.QUEST_ANSWER, edt4.getText().toString());
        values.put(RsaDbHelper.QUEST_COMMENT, edt40.getText().toString());
        db.insert(RsaDbHelper.TABLE_QUEST, RsaDbHelper.QUEST_COMMENT, values);

        values.clear();
        values.put(RsaDbHelper.QUEST_ZAKAZ_ID, "new");
        values.put(RsaDbHelper.QUEST_QUESTION_ID, "5");
        values.put(RsaDbHelper.QUEST_QUESTION_TEXT, "Контактное лицо");
        values.put(RsaDbHelper.QUEST_CORRECT, chk5.isChecked() ? "1" : "0");
        values.put(RsaDbHelper.QUEST_ANSWER, edt5.getText().toString());
        values.put(RsaDbHelper.QUEST_COMMENT, edt50.getText().toString());
        db.insert(RsaDbHelper.TABLE_QUEST, RsaDbHelper.QUEST_COMMENT, values);

        values.clear();
        values.put(RsaDbHelper.QUEST_ZAKAZ_ID, "new");
        values.put(RsaDbHelper.QUEST_QUESTION_ID, "6");
        values.put(RsaDbHelper.QUEST_QUESTION_TEXT, "Контактный телефон");
        values.put(RsaDbHelper.QUEST_CORRECT, chk6.isChecked() ? "1" : "0");
        values.put(RsaDbHelper.QUEST_ANSWER, edt6.getText().toString());
        values.put(RsaDbHelper.QUEST_COMMENT, edt60.getText().toString());
        db.insert(RsaDbHelper.TABLE_QUEST, RsaDbHelper.QUEST_COMMENT, values);

        Log.d("RRRR", "inserting");

        if (db != null && db.isOpen()) {
            db.close();
        }
    }

    @Override
    public void onBackPressed() {
        performExit();
    }
}
