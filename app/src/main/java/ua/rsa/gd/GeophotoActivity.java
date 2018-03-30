package ua.rsa.gd;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import ru.by.rsa.R;

public class GeophotoActivity extends Activity {

    static final int PICK_PHOTO_REQUEST = 11;

    private static int verOS = 0;
    private boolean lightTheme;
    private Bundle extras;

    private String currentCoordinates = "";
    private String currentPhotoName = "";

    TextView txtCust;
    TextView txtShop;
    TextView txtCoords;
    TextView txtCoordsStatus;
    TextView txtShopStatus;

    boolean isNewCoords;
    boolean isNewPhoto;

    Button btnCoords;
    Button btnPhoto;
    Button btnSave;
    Button btnCancel;

    String SD_CARD_PATH;

    boolean fromSavedState;

    OrderHead mOrderH;

    LocationManager locationManager;

    private String mPhotoFilePath;

    LocationListener MyLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location arg0) {
            try {
                long[] gps = new long[2];
                gps[0] = (long) (arg0.getLatitude() * 1E6);
                gps[1] = (long) (arg0.getLongitude() * 1E6);
                currentCoordinates = Long.toString(gps[0]) + " " + Long.toString(gps[1]);
                isNewCoords = true;
                updateInfo();
                locationManager.removeUpdates(MyLocationListener);
            } catch (Exception e) {
            }
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        verOS = 2;
        isNewCoords = false;
        isNewPhoto = false;
        SD_CARD_PATH = Environment.getExternalStorageDirectory().toString() + File.separator + "rsa" + File.separator + "outbox";
        try {
            verOS = Integer.parseInt(Build.VERSION.RELEASE.substring(0, 1));
        } catch (Exception e) {
        }
        ;

        lightTheme = getSharedPreferences(RsaDb.PREFS_NAME_MAIN, Context.MODE_PRIVATE).getBoolean(RsaDb.LIGHTTHEMEKEY, false);
        if (lightTheme) {
            //setTheme(R.style.Theme_Custom);
            super.onCreate(savedInstanceState);
            setContentView(R.layout.geophoto);
        } else {
            //setTheme(R.style.Theme_CustomBlackNoTitleBar);
            super.onCreate(savedInstanceState);
            setContentView(R.layout.geophoto);
        }

        if (savedInstanceState != null) {
            extras = savedInstanceState;
            fromSavedState = true;
        } else {
            fromSavedState = false;
            extras = getIntent().getExtras();
        }
        mOrderH = (OrderHead) extras.getParcelable("ORDERH");

        txtCust = (TextView) findViewById(R.id.txtGeophoto_cust);
        txtShop = (TextView) findViewById(R.id.txtGeophoto_shop);
        txtCoords = (TextView) findViewById(R.id.txtGeophoto_coords);
        txtCoordsStatus = (TextView) findViewById(R.id.txtGeophoto_coordstatus);
        txtShopStatus = (TextView) findViewById(R.id.txtGeophoto_shopstatus);


        btnCoords = (Button) findViewById(R.id.btnGeo);
        btnPhoto = (Button) findViewById(R.id.btnMakePhoto);
        btnSave = (Button) findViewById(R.id.btnSaveExit);
        btnCancel = (Button) findViewById(R.id.btnCancelExit);

        bindButtonsAction();

        updateInfo();
    }

    private void updateInfo() {
        String gpsInfo = getShopGPSInfo();
        String photoInfo = getShopPhotoInfo();

        txtCust.setText(mOrderH.cust_text.toString());
        txtShop.setText(mOrderH.shop_text.toString());
        txtShopStatus.setText(photoInfo);
        txtCoordsStatus.setText(gpsInfo);
        txtCoords.setText(currentCoordinates);

        if (isNewCoords) {
            txtCoordsStatus.setTextColor(Color.parseColor("#ffff00"));
            txtCoords.setTextColor(Color.parseColor("#ffff00"));
            txtCoordsStatus.setText("Были зафиксированы новые координаты");
        } else if (gpsInfo.contains("нет")) {
            txtCoordsStatus.setTextColor(Color.parseColor("#ff0000"));
            txtCoords.setTextColor(Color.parseColor("#ff0000"));
        } else {
            txtCoordsStatus.setTextColor(Color.parseColor("#00ff00"));
            txtCoords.setTextColor(Color.parseColor("#00ff00"));
        }

        if (isNewPhoto) {
            txtShopStatus.setTextColor(Color.parseColor("#ffff00"));
            txtShopStatus.setText("Было сделано новое фото");
        } else if (photoInfo.contains("нет")) {
            txtShopStatus.setTextColor(Color.parseColor("#ff0000"));
        } else {
            txtShopStatus.setTextColor(Color.parseColor("#00ff00"));
        }
    }

    private void bindButtonsAction() {
        btnSave.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isNewCoords)
                    saveCoords();
                if (isNewPhoto)
                    savePhoto();

                performExit();
            }
        });
        btnCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                performExit();
            }
        });
        btnCoords.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

//                long[] gps = new long[2];
//                gps[0] = 646546546l;
//                gps[1] = 944343234l;
//                currentCoordinates = Long.toString(gps[0]) + " " + Long.toString(gps[1]);
//                isNewCoords = true;
//                updateInfo();

                if (locationManager != null) {
                    locationManager.removeUpdates(MyLocationListener);
                }

                locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                try {
                    String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
                    if (!provider.contains("gps")) {
                        Toast.makeText(getApplicationContext(), "Включите датчик GPS!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } catch (Exception e) {
                }
                Toast.makeText(getApplicationContext(), "Подождите определяется местоположение!", Toast.LENGTH_LONG).show();
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, MyLocationListener);
            }
        });
        btnPhoto.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, createNewPhoto());
                try {
                    startActivityForResult(takePictureIntent, PICK_PHOTO_REQUEST);
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Не могу вызвать фотоаппарат", Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    private Uri createNewPhoto() {
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = timeStamp + mOrderH.cust_id + mOrderH.shop_id;
        mPhotoFilePath = SD_CARD_PATH + File.separator + imageFileName + ".jpg";
        File newFile = new File(mPhotoFilePath);
        return Uri.fromFile(newFile);
    }


    private void saveCoords() {
        RsaDbHelper mDb_ord = new RsaDbHelper(this, RsaDbHelper.DB_ORDERS);
        SQLiteDatabase db_orders = mDb_ord.getReadableDatabase();
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sfm = new SimpleDateFormat("yyyy-MM-dd");
        String currentDay = sfm.format(c.getTime());
        c.add(Calendar.DATE, 1);
        String nextDay = sfm.format(c.getTime());
        String query = "select GPS from _geophoto where TIMESTAMP BETWEEN '" + currentDay + "' " +
                "AND '" + nextDay + "' " +
                "AND CUST_ID='" + mOrderH.cust_id + "' " +
                "AND SHOP_ID='" + mOrderH.shop_id + "' limit 1";

        Cursor cursor = db_orders.rawQuery(query, null);

        SimpleDateFormat sfm2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        c = Calendar.getInstance();

        if (cursor.getCount() > 0) {
            db_orders.execSQL("update _geophoto set GPS='" + currentCoordinates + "', TIMESTAMP='" + sfm2.format(c.getTime()) + "' where " +
                    "TIMESTAMP BETWEEN '" + currentDay + "' " +
                    "AND '" + nextDay + "' " +
                    "AND CUST_ID='" + mOrderH.cust_id + "' " +
                    "AND SHOP_ID='" + mOrderH.shop_id + "'");
        } else {
            db_orders.execSQL("insert into _geophoto values (null, 'new', " +
                    "'" + currentCoordinates + "', " +
                    "'0', " +
                    "'" + sfm2.format(c.getTime()) + "', " +
                    "'" + mOrderH.cust_id + "', " +
                    "'" + mOrderH.shop_id + "')");
        }
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        if (db_orders != null && db_orders.isOpen()) {
            db_orders.close();
        }
    }

    private void savePhoto() {
        RsaDbHelper mDb_ord = new RsaDbHelper(this, RsaDbHelper.DB_ORDERS);
        SQLiteDatabase db_orders = mDb_ord.getReadableDatabase();
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sfm = new SimpleDateFormat("yyyy-MM-dd");
        String currentDay = sfm.format(c.getTime());
        c.add(Calendar.DATE, 1);
        String nextDay = sfm.format(c.getTime());
        String query = "select PHOTO from _geophoto where TIMESTAMP BETWEEN '" + currentDay + "' " +
                "AND '" + nextDay + "' " +
                "AND CUST_ID='" + mOrderH.cust_id + "' " +
                "AND SHOP_ID='" + mOrderH.shop_id + "' limit 1";

        Cursor cursor = db_orders.rawQuery(query, null);

        SimpleDateFormat sfm2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        c = Calendar.getInstance();

        if (cursor.getCount() > 0) {
            db_orders.execSQL("update _geophoto set PHOTO='" + currentPhotoName + "', TIMESTAMP='" + sfm2.format(c.getTime()) + "' where " +
                    "TIMESTAMP BETWEEN '" + currentDay + "' " +
                    "AND '" + nextDay + "' " +
                    "AND CUST_ID='" + mOrderH.cust_id + "' " +
                    "AND SHOP_ID='" + mOrderH.shop_id + "'");
        } else {
            db_orders.execSQL("insert into _geophoto values (null, 'new', " +
                    "'0', " +
                    "'" + currentPhotoName + "', " +
                    "'" + sfm2.format(c.getTime()) + "', " +
                    "'" + mOrderH.cust_id + "', " +
                    "'" + mOrderH.shop_id + "')");
        }
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        if (db_orders != null && db_orders.isOpen()) {
            db_orders.close();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();


    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable("ORDERH", extras.getParcelable("ORDERH"));
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

    @Override
    public void onBackPressed() {
        if (locationManager != null && MyLocationListener != null) {
            locationManager.removeUpdates(MyLocationListener);
        }
        performExit();
    }

    private String getShopPhotoInfo() {
        String result = "У текущей ТТ нет фотографии в 1С!";
        SQLiteDatabase db = null;
        SharedPreferences prefs = getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);
        RsaDbHelper mDb = new RsaDbHelper(this, prefs.getString(RsaDb.ACTUALDBKEY, RsaDbHelper.DB_NAME1));
        db = mDb.getReadableDatabase();
        Cursor cursor = null;
        String query = "select PHOTO from _shop where CUST_ID='" + mOrderH.cust_id
                + "' AND ID='" + mOrderH.shop_id + "' limit 1";
        RsaDbHelper mDb_orders = new RsaDbHelper(this, RsaDbHelper.DB_ORDERS);
        SQLiteDatabase db_orders = mDb_orders.getReadableDatabase();
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sfm = new SimpleDateFormat("yyyy-MM-dd");
        String currentDay = sfm.format(c.getTime());
        c.add(Calendar.DATE, 1);
        String nextDay = sfm.format(c.getTime());
        String query2 = "select PHOTO from _geophoto where TIMESTAMP BETWEEN '" + currentDay + "' " +
                "AND '" + nextDay + "' " +
                "AND CUST_ID='" + mOrderH.cust_id + "' " +
                "AND SHOP_ID='" + mOrderH.shop_id + "' limit 1";
        String value;
        try {
            cursor = db.rawQuery(query, null);
            if (cursor.moveToFirst()) {
                value = cursor.getString(0);
                if (value != null && value.equals("1")) {
                    result = "У текущей ТТ есть фотография в 1С";
                }
            }

            cursor = db_orders.rawQuery(query2, null);
            if (cursor.moveToFirst()) {
                value = cursor.getString(0);
                if (value != null && value.length() > 3) {
                    result = "Была сделана новая фотография";
                }
            }
        } catch (Exception e) {
            result = "";
        }


        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        if (db != null && db.isOpen()) {
            db.close();
        }
        if (db_orders != null && db_orders.isOpen()) {
            db_orders.close();
        }

        return result;
    }

    private String getShopGPSInfo() {
        String result = "В 1С координат ТТ нет!";
        SQLiteDatabase db = null;
        SharedPreferences prefs = getSharedPreferences(RsaDb.PREFS_NAME, Context.MODE_PRIVATE);
        RsaDbHelper mDb = new RsaDbHelper(this, prefs.getString(RsaDb.ACTUALDBKEY, RsaDbHelper.DB_NAME1));
        db = mDb.getReadableDatabase();
        Cursor cursor = null;
        String query = "select GPS from _shop where CUST_ID='" + mOrderH.cust_id
                + "' AND ID='" + mOrderH.shop_id + "' limit 1";
        RsaDbHelper mDb_orders = new RsaDbHelper(this, RsaDbHelper.DB_ORDERS);
        SQLiteDatabase db_orders = mDb_orders.getReadableDatabase();
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sfm = new SimpleDateFormat("yyyy-MM-dd");
        String currentDay = sfm.format(c.getTime());
        c.add(Calendar.DATE, 1);
        String nextDay = sfm.format(c.getTime());
        String query2 = "select GPS from _geophoto where TIMESTAMP BETWEEN '" + currentDay + "' " +
                "AND '" + nextDay + "' " +
                "AND CUST_ID='" + mOrderH.cust_id + "' " +
                "AND SHOP_ID='" + mOrderH.shop_id + "' limit 1";
        String value;
        try {
            cursor = db.rawQuery(query, null);
            if (cursor.moveToFirst()) {
                value = cursor.getString(0);
                if (value != null && value.length() > 3) {
                    result = "В 1С есть координаты ТТ";
                }
            }

            cursor = db_orders.rawQuery(query2, null);
            if (cursor.moveToFirst()) {
                value = cursor.getString(0);
                if (value != null && value.length() > 3) {
                    currentCoordinates = value;
                    result = "Были зафиксированы новые координаты";
                }
            }
        } catch (Exception e) {
            result = "";
        }

        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        if (db != null && db.isOpen()) {
            db.close();
        }
        if (db_orders != null && db_orders.isOpen()) {
            db_orders.close();
        }
        return result;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            if (requestCode == PICK_PHOTO_REQUEST && (resultCode == RESULT_OK)) {
                if (TextUtils.isEmpty(mPhotoFilePath)) {
                    Toast.makeText(this, "Планшет не сохраняет фото!", Toast.LENGTH_LONG).show();
                    return;
                }
                File attachedFile = new File(mPhotoFilePath);
                currentPhotoName = attachedFile.getName();
                isNewPhoto = true;
            }
        } catch (Exception sEx) {
            sEx.printStackTrace();
            Toast.makeText(this, "77 Планшет не отдает фото!", Toast.LENGTH_LONG).show();
        }
        updateInfo();
    }
}
