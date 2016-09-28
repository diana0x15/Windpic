package com.wind.windpic.objects;

import android.graphics.Bitmap;
import android.util.Log;

import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.wind.windpic.MainActivity;

/**
 * Created by dianapislaru on 07/10/15.
 */
public class Photo {
    private static final String TAG = "Photo";

    private ParseObject mObject;
    private Bitmap mPhoto;
    private String mAuthor;
    private String mFirstName = "";
    private String mLastName = "";
    private int mAge;
    private String mGender;
    private int mDistance;

    public Photo(ParseObject object, String id, String first, String last, int age, String gender, ParseGeoPoint location, Bitmap bitmap) {
        mObject = object;
        mPhoto = bitmap;
        mAuthor = id;
        mFirstName = first;
        mLastName = last;
        mAge = age;
        mGender = gender;

        Log.i(TAG, "LAT:" + location.getLatitude());
        Log.i(TAG, "LONG:" + location.getLongitude());
        Log.i(TAG, "LONGITUDINEA MEA" + MainActivity.CURRENT_USER.getParseGeoPoint("currentLocation").getLongitude());
        Log.i(TAG, "LATITUDINEA MEA" + MainActivity.CURRENT_USER.getParseGeoPoint("currentLocation").getLatitude());
//        Log.i(TAG, "DISTANCE: " + location.distanceInMilesTo(MainActivity.CURRENT_USER.getParseGeoPoint("location")));

        mDistance = (int) (location.distanceInMilesTo(MainActivity.CURRENT_USER.getParseGeoPoint("currentLocation")));
    }

    public ParseObject getObject() {
        return mObject;
    }

    public Bitmap getPhoto() {
        return mPhoto;
    }

    public String getAuthor() {
        return mAuthor;
    }

    public String getFirstName() {
        return mFirstName;
    }

    public String getLastName() {
        return mLastName;
    }

    public int getDistance() {
        return mDistance;
    }

    public int getAge() {
        return mAge;
    }

    public String getGender() {
        return mGender;
    }
}
