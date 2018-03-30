package ru.by.rsa.utils;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.os.Environment;
import android.os.Looper;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import ru.by.rsa.CoordDbHelper;
import ru.by.rsa.CoordProvider;
import ru.by.rsa.R;

/**
 * Created by Ромка on 16.04.2016.
 */
public class GpsUtils {

    public static long[] getGPS(LocationManager mActLm) {
        LocationManager lm = mActLm;
        List<String> providers = lm.getProviders(true);
        Location l = null;
        for (int i = providers.size() - 1; i >= 0; i--) {
            l = lm.getLastKnownLocation(providers.get(i));
            if (l != null) break;
        }
        long[] gps = new long[2];
        if (l != null) {
            gps[0] = (long) (l.getLatitude() * 1E6);
            gps[1] = (long) (l.getLongitude() * 1E6);
            return gps;
        }
        return null;
    }

    public static void exportGPSTrack(Activity activity, boolean fromPrefs, String date) {
        long countRec;
        Cursor mCursor;
        final String[] mContent = new String[] {CoordDbHelper._ID, CoordDbHelper.DATE,
                CoordDbHelper.TIME,
                CoordDbHelper.COORD, CoordDbHelper.SENT, CoordDbHelper.AVER,
                CoordDbHelper.AFLG, CoordDbHelper.ADATE, CoordDbHelper.AUTC,
                CoordDbHelper.ALAT, CoordDbHelper.ASIND, CoordDbHelper.ALONG,
                CoordDbHelper.AWIND, CoordDbHelper.AALT, CoordDbHelper.ASPEED,
                CoordDbHelper.ACOURSE, CoordDbHelper.ABAT, CoordDbHelper.ASTART,
                CoordDbHelper.AFIX, CoordDbHelper.FNMEA}; //[19]

        if (!fromPrefs) {
            Looper.prepare();
        }
        String where = date == null ? null : CoordDbHelper.DATE + "='" + date+"'";
        mCursor = activity.managedQuery(CoordProvider.CONTENT_URI, mContent, where, null, null);
        // Get count of orders
        countRec = mCursor.getCount();

        if (countRec > 0) {
            String SD_CARD_PATH = Environment.getExternalStorageDirectory().toString()
                    + File.separator + "rsa" + File.separator + "track.log";

            BufferedWriter out = null;

            try {
                out = new BufferedWriter(new FileWriter(SD_CARD_PATH));
            } catch (IOException e) {
                Toast.makeText(activity,
                        activity.getResources().getString(R.string.preferences_Coord_errCr),
                        Toast.LENGTH_LONG).show();
            }

            // Parsing all orders in HEAD table
            mCursor.moveToFirst();
            for (int i = 0; i < countRec; i++) {
                try {
                    for (int j = 1; j < mContent.length; j++) {
                        out.write(mCursor.getString(j) + ";");
                    }
                    out.write("\n");
                } catch (IOException e) {
                    Toast.makeText(activity,
                            activity.getResources().getString(R.string.preferences_Coord_errWr),
                            Toast.LENGTH_LONG).show();
                }
                mCursor.moveToNext();
            }

            try {
                out.close();
            } catch (IOException e) {
                Toast.makeText(activity,
                        activity.getResources().getString(R.string.preferences_Coord_errCl),
                        Toast.LENGTH_LONG).show();
            }
        }

        Toast.makeText(activity,
                activity.getResources().getString(R.string.preferences_Coord_done),
                Toast.LENGTH_SHORT).show();
    }
}
