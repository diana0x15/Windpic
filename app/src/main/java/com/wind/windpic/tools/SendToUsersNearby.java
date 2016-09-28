package com.wind.windpic.tools;

import android.app.Activity;
import android.app.ProgressDialog;
import android.util.Log;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.wind.windpic.MainActivity;
import com.wind.windpic.WindpicApplication;

import java.util.List;

/**
 * Created by dianapislaru on 10/10/15.
 */
public class SendToUsersNearby {

    public static final String TAG = "SendToUsersNearby";

    private Activity mContext;
    private ParseFile mFile;
    private String mAuthorId;
    private ProgressDialog mProgressDialog;
    private List<ParseUser> mFriends;

    private String mGender;
    private int mDistance;
    private int mMinAge;
    private int mMaxAge;

    public SendToUsersNearby(Activity context, ParseFile file, ProgressDialog progress) {
        mContext = context;
        mFile = file;
        mAuthorId = MainActivity.CURRENT_USER.getObjectId();
        mProgressDialog = progress;
        getFriends();
        getPreferences();
    }

    public void send() {
        mProgressDialog.show();
        final ParseObject photo = getPhotoObject();
        photo.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                String id = photo.getObjectId();
                sendPhoto(id);
            }
        });
    }

    private ParseObject getPhotoObject() {
        ParseObject objectFile = new ParseObject("Snap");
        objectFile.put("authorId", mAuthorId);
        objectFile.put("file", mFile);
        objectFile.put("first_name", MainActivity.CURRENT_USER.getString("first_name"));
        objectFile.put("last_name", MainActivity.CURRENT_USER.getString("last_name"));
        objectFile.put("age", MainActivity.CURRENT_USER.getInt("age"));
        objectFile.put("gender", MainActivity.CURRENT_USER.getString("gender"));
        objectFile.put("currentLocation", MainActivity.CURRENT_USER.getParseGeoPoint("currentLocation"));

        return objectFile;
    }

    private void sendPhoto(final String id) {
        ParseUser currentUser = ParseUser.getCurrentUser();
        ParseGeoPoint userLocation = (ParseGeoPoint) currentUser.get("currentLocation");
        int userAge = currentUser.getInt("age");

        if (userLocation != null) {
            ParseQuery<ParseUser> query = ParseUser.getQuery();

            // Check matching with user's preferences
            Log.i(TAG, userAge + " " + mDistance + " " + mGender);
            query.whereEqualTo("snapSearching", "true");
            query.whereWithinMiles("currentLocation", userLocation, mDistance);
            query.whereLessThanOrEqualTo("minAge", userAge);
            query.whereGreaterThanOrEqualTo("maxAge", userAge);
            if (!mGender.equals("FM"))  query.whereEqualTo("gender", mGender);

            query.findInBackground(new FindCallback<ParseUser>() {
                @Override
                public void done(List<ParseUser> list, ParseException e) {
                    mProgressDialog.dismiss();
                    if (e == null) {
                        for (final ParseUser user : list) {
                            //if (!isFriend(user) && matchesMyPreferences(user)) {
                            if (!isMe(user) && matchesMyPreferences(user)) {
                                Log.i(TAG, user.getString("first_name") + " " + user.getString("last_name"));
                                WindpicApplication.FIREBASE.child("NEW_PHOTOS").child(user.getObjectId()).push().setValue(id);
                            }
                        }
                    } else {
                        Toast.makeText(mContext, "Could not access your location.", Toast.LENGTH_LONG).show();
                        Log.e(TAG, e.getMessage());
                    }
                }
            });
        } else {
            Toast.makeText(mContext, "Could not access your location.", Toast.LENGTH_LONG).show();
            Log.i(TAG, "User location is null");
            mProgressDialog.dismiss();
        }
    }

    private boolean isMe(ParseUser user) {
        String id = user.getObjectId();
        if (id.equals(MainActivity.CURRENT_USER.getObjectId())) {
            return true;
        }
        return false;
    }

    private boolean matchesMyPreferences(ParseUser user) {
        if (user.getInt("age") < MainActivity.CURRENT_USER.getInt("minAge")) {
            return false;
        }
        if (user.getInt("age") > MainActivity.CURRENT_USER.getInt("maxAge")) {
            return false;
        }
        if (user.getParseGeoPoint("currentLocation").distanceInMilesTo(MainActivity.CURRENT_USER.getParseGeoPoint("currentLocation")) > user.getInt("distance")) {
            return false;
        }
        return true;
    }

    private void getFriends() {
        mFriends = MainActivity.CURRENT_USER.getList("friends");
    }

    private void getPreferences() {
        // Get preferences from Parse Current User
        mGender = MainActivity.CURRENT_USER.getString("searchGender");
        mMaxAge = MainActivity.CURRENT_USER.getInt("maxAge");
        mMinAge = MainActivity.CURRENT_USER.getInt("minAge");
        mDistance = MainActivity.CURRENT_USER.getInt("distance");
    }
}
