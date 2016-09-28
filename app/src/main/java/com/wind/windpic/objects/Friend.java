package com.wind.windpic.objects;

import android.os.Parcel;
import android.os.Parcelable;

import com.wind.windpic.MainActivity;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;

import java.util.Map;

/**
 * Created by dianapislaru on 12/10/15.
 */
public class Friend implements Parcelable {

    public static final String TAG = "Friend";

    private String mId;
    private String mName;
    private String mFirstName;
    private String mLastName;
    private int mAge;
    private String mGender;
    private int mDistanceInKm;
    private int mDistanceInMiles;
    private byte[] mPicture;
    private Map<String,String> mLastMessage;

    public Friend(ParseUser user) {
        mId = user.getObjectId();
        mFirstName = user.getString("first_name");
        mLastName = user.getString("last_name");
        mName = mFirstName + " " + mLastName;
        mAge = user.getInt("age");
        mGender = user.getString("gender");

        ParseGeoPoint location = user.getParseGeoPoint("currentLocation");
        if (location != null) {
            mDistanceInKm = (int) location.distanceInKilometersTo(MainActivity.CURRENT_USER.getParseGeoPoint("currentLocation"));
            mDistanceInMiles = (int) location.distanceInMilesTo(MainActivity.CURRENT_USER.getParseGeoPoint("currentLocation"));
        }

        // Get the user's avatar
        try {
            mPicture = user.getParseFile("avatar").getData();
        } catch (ParseException e) { }
    }

    public Friend(String id, String name, int age, String gender) {
        mId = id;
        mName = name;
        mAge = age;
        mGender = gender;
    }

    public String getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }
    public String getFirstName() {
        return mFirstName;
    }

    public String getLastName() {
        return mLastName;
    }

    public byte[] getPicture() {
        return mPicture;
    }

    public int getAge() {
        return mAge;
    }

    public String getGender() {
        return mGender;
    }

    public int getDistanceInKm() {
        return mDistanceInKm;
    }

    public int getDistanceInMiles() {
        return mDistanceInMiles;
    }

    public void setId(String id) {
        mId = id;
    }

    public void setName(String name) {
        mName = name;
    }

    public void setPicture(byte[] picture) {
        mPicture = picture;
    }

    protected Friend(Parcel in) {
        mId = in.readString();
        mName = in.readString();
        mAge = in.readInt();
        mGender = in.readString();
        int size = in.readInt();
        mPicture = new byte[size];
        in.readByteArray(mPicture);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mId);
        parcel.writeString(mName);
        parcel.writeInt(mAge);
        parcel.writeString(mGender);
        parcel.writeInt(mPicture.length);
        parcel.writeByteArray(mPicture);
    }

    public static final Creator<Friend> CREATOR = new Creator<Friend>() {
        @Override
        public Friend createFromParcel(Parcel in) {
            return new Friend(in);
        }

        @Override
        public Friend[] newArray(int size) {
            return new Friend[size];
        }
    };

    public Map<String,String> getLastMessage() {
        return mLastMessage;
    }

    public void setLastMessage(Map<String,String> lastMessage) {
        mLastMessage = lastMessage;
    }
}
