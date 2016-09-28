package com.wind.windpic;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.parse.Parse;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;
import com.wind.windpic.objects.Database;
import com.wind.windpic.objects.Friend;
import com.wind.windpic.objects.Photo;

import java.util.ArrayList;

/**
 * Created by dianapislaru on 09/10/15.
 */
public class WindpicApplication extends Application {

    public static final String TAG = "WindpicApplication";

    public static Firebase FIREBASE;
    public static Database DATABASE;
    public static boolean MESSAGES = false;
    public static boolean PHOTOS = false;
    public static boolean REQUESTS = false;
    public static ArrayList<ParseUser> REQUESTS_LIST;
    public static ArrayList<Photo> PHOTOS_LIST;
    public static ArrayList<Friend> FRIENDS_LIST;
    public static DataSnapshot REQUESTS_SNAPSHOT;

    @Override
    public void onCreate() {
        super.onCreate();

        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "l5klrUmFwmvWEPI5ivYi3EcxQD08MmFRccs61MXK", "0xbPvIDynKB7cIchXjGbjEFTs81ZOz5m1AOzjoB8");

        ParseFacebookUtils.initialize(this);
        Firebase.setAndroidContext(this);
        FIREBASE = new Firebase("https://vivid-heat-7688.firebaseio.com");
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
