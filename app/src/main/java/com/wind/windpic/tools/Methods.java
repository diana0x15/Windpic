package com.wind.windpic.tools;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.TypedValue;
import android.view.Display;
import android.view.WindowManager;

import com.wind.windpic.MainActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by dianapislaru on 09/10/15.
 */
public class Methods {

    public static final int FRAGMENT_LOGIN = 0;
    public static final int FRAGMENT_CAMERA = 1;
    public static final int FRAGMENT_FRIENDS = 2;
    public static final int FRAGMENT_CHAT = 3;
    public static final int FRAGMENT_PROFILE = 4;
    public static final int FRAGMENT_REQUESTS = 5;
    public static final int FRAGMENT_PREVIEW = 6;
    public static final int FRAGMENT_PHOTOS = 7;
    public static final int FRAGMENT_PREFERENCES = 8;

    public static Point getScreenSize(Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }

    public static void setFragment(int fragment) {
        switch (fragment) {
            case FRAGMENT_LOGIN:
                MainActivity.FRAGMENT_LOGIN = true;
                MainActivity.FRAGMENT_CAMERA = false;
                MainActivity.FRAGMENT_FRIENDS = false;
                MainActivity.FRAGMENT_CHAT = false;
                MainActivity.FRAGMENT_PROFILE = false;
                MainActivity.FRAGMENT_REQUESTS = false;
                MainActivity.FRAGMENT_PREVIEW = false;
                MainActivity.FRAGMENT_PHOTOS = false;
                MainActivity.FRAGMENT_PREFERENCES = false;
                break;
            case FRAGMENT_CAMERA:
                MainActivity.FRAGMENT_LOGIN = false;
                MainActivity.FRAGMENT_CAMERA = true;
                MainActivity.FRAGMENT_FRIENDS = false;
                MainActivity.FRAGMENT_CHAT = false;
                MainActivity.FRAGMENT_PROFILE = false;
                MainActivity.FRAGMENT_REQUESTS = false;
                MainActivity.FRAGMENT_PREVIEW = false;
                MainActivity.FRAGMENT_PHOTOS = false;
                MainActivity.FRAGMENT_PREFERENCES = false;
                break;
            case FRAGMENT_FRIENDS:
                MainActivity.FRAGMENT_LOGIN = false;
                MainActivity.FRAGMENT_CAMERA = false;
                MainActivity.FRAGMENT_FRIENDS = true;
                MainActivity.FRAGMENT_CHAT = false;
                MainActivity.FRAGMENT_PROFILE = false;
                MainActivity.FRAGMENT_REQUESTS = false;
                MainActivity.FRAGMENT_PREVIEW = false;
                MainActivity.FRAGMENT_PHOTOS = false;
                MainActivity.FRAGMENT_PREFERENCES = false;
                break;
            case FRAGMENT_CHAT:
                MainActivity.FRAGMENT_LOGIN = false;
                MainActivity.FRAGMENT_CAMERA = false;
                MainActivity.FRAGMENT_FRIENDS = false;
                MainActivity.FRAGMENT_CHAT = true;
                MainActivity.FRAGMENT_PROFILE = false;
                MainActivity.FRAGMENT_REQUESTS = false;
                MainActivity.FRAGMENT_PREVIEW = false;
                MainActivity.FRAGMENT_PHOTOS = false;
                MainActivity.FRAGMENT_PREFERENCES = false;
                break;
            case FRAGMENT_PROFILE:
                MainActivity.FRAGMENT_LOGIN = false;
                MainActivity.FRAGMENT_CAMERA = false;
                MainActivity.FRAGMENT_FRIENDS = false;
                MainActivity.FRAGMENT_CHAT = false;
                MainActivity.FRAGMENT_PROFILE = true;
                MainActivity.FRAGMENT_REQUESTS = false;
                MainActivity.FRAGMENT_PREVIEW = false;
                MainActivity.FRAGMENT_PHOTOS = false;
                MainActivity.FRAGMENT_PREFERENCES = false;
                break;
            case FRAGMENT_REQUESTS:
                MainActivity.FRAGMENT_LOGIN = false;
                MainActivity.FRAGMENT_CAMERA = false;
                MainActivity.FRAGMENT_FRIENDS = false;
                MainActivity.FRAGMENT_CHAT = false;
                MainActivity.FRAGMENT_PROFILE = false;
                MainActivity.FRAGMENT_REQUESTS = true;
                MainActivity.FRAGMENT_PREVIEW = false;
                MainActivity.FRAGMENT_PHOTOS = false;
                MainActivity.FRAGMENT_PREFERENCES = false;
                break;
            case FRAGMENT_PREVIEW:
                MainActivity.FRAGMENT_LOGIN = false;
                MainActivity.FRAGMENT_CAMERA = false;
                MainActivity.FRAGMENT_FRIENDS = false;
                MainActivity.FRAGMENT_CHAT = false;
                MainActivity.FRAGMENT_PROFILE = false;
                MainActivity.FRAGMENT_REQUESTS = false;
                MainActivity.FRAGMENT_PREVIEW = true;
                MainActivity.FRAGMENT_PHOTOS = false;
                MainActivity.FRAGMENT_PREFERENCES = false;
                break;
            case FRAGMENT_PHOTOS:
                MainActivity.FRAGMENT_LOGIN = false;
                MainActivity.FRAGMENT_CAMERA = false;
                MainActivity.FRAGMENT_FRIENDS = false;
                MainActivity.FRAGMENT_CHAT = false;
                MainActivity.FRAGMENT_PROFILE = false;
                MainActivity.FRAGMENT_REQUESTS = false;
                MainActivity.FRAGMENT_PREVIEW = false;
                MainActivity.FRAGMENT_PHOTOS = true;
                MainActivity.FRAGMENT_PREFERENCES = false;
                break;
            case FRAGMENT_PREFERENCES:
                MainActivity.FRAGMENT_LOGIN = false;
                MainActivity.FRAGMENT_CAMERA = false;
                MainActivity.FRAGMENT_FRIENDS = false;
                MainActivity.FRAGMENT_CHAT = false;
                MainActivity.FRAGMENT_PROFILE = false;
                MainActivity.FRAGMENT_REQUESTS = false;
                MainActivity.FRAGMENT_PREVIEW = false;
                MainActivity.FRAGMENT_PHOTOS = false;
                MainActivity.FRAGMENT_PREFERENCES = true;
                break;

        }
    }


    public static void enableFullScreen(Activity activity) {
        activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    public static void disableFullScreen(Activity activity) {
        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    public static Point getSmallDisplaySize(Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        int statusBarHeight = 0;
        int resourceId = activity.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = activity.getResources().getDimensionPixelSize(resourceId);
        }

        size.y -= statusBarHeight;
        return size;
    }

    public static Point getFullDisplaySize(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }

    public static int getActionBarHeight(Activity activity) {
        TypedValue tv = new TypedValue();
        if (activity.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            return TypedValue.complexToDimensionPixelSize(tv.data, activity.getResources().getDisplayMetrics());
        }
        return 0;
    }


    public static String getCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        return dateFormat.format(calendar.getTime());
    }

    public static Date getCurrentDateDate() {
        Calendar calendar = Calendar.getInstance();
        return new Date(calendar.getTimeInMillis());
    }

    public static String getDay(String messageDate) {
        String inputPattern = "MM/dd/yyyy HH:mm:ss";
        String outputPattern = "MM/dd/yyyy";
        SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
        SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);

        Date date = null;
        String str = null;

        try {
            date = inputFormat.parse(messageDate);
            str = outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return str;
    }

    public static String getTime(String messageDate) {
        String inputPattern = "MM/dd/yyyy HH:mm:ss";
        String outputPattern = "hh:mm aa";

        SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
        SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);

        Date date = null;
        String str = null;

        try {
            date = inputFormat.parse(messageDate);
            str = outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return str;
    }

    public static String getStringDate(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        return dateFormat.format(date);
    }

    public static boolean areEqual(float x, float y) {
        float epsilon = 0.0000001f;
        return ( Math.abs(x - y) < epsilon );
    }

    public static boolean isInternetAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId, Point size) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, size.x, size.y);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    public static String changeTimeZoneToGMT(String string) {
        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

        Date date = null;
        try {
            date = format.parse(string);
            System.out.println(date);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (date == null)
            return null;
        //getting GMT timezone
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        return format.format(date);
    }

    public static String changeTimeZoneFromGMT(String string) {
        if (string == null)
            return "";

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = null;
        try {
            date = simpleDateFormat.parse(string);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (date == null)
            return null;

        return getStringDate(date);
    }

}
