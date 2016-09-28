package com.wind.windpic.tools;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.wind.windpic.MainActivity;
import com.wind.windpic.WindpicApplication;
import com.wind.windpic.fragments.CameraFragment;
import com.wind.windpic.fragments.FriendsFragment;
import com.wind.windpic.objects.Photo;
import com.parse.FindCallback;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dianapislaru on 09/11/15.
 */
public class PhotosListener {
    public static void getPhotos() {
        WindpicApplication.PHOTOS_LIST = new ArrayList<>();

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Snap");
        query.fromLocalDatastore();
        query.fromPin(MainActivity.CURRENT_USER.getObjectId());
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, com.parse.ParseException e) {
                if (list != null && list.size() > 0) {
                    for (ParseObject photo : list) {
                        WindpicApplication.PHOTOS_LIST.add(convertPhoto(photo));
                    }
                    WindpicApplication.PHOTOS = true;
                    CameraFragment.updatePhotosNotifications(true);
                    FriendsFragment.updatePhotosNotifications(true);
                } else {
                    WindpicApplication.PHOTOS = false;
                    CameraFragment.updatePhotosNotifications(false);
                    FriendsFragment.updatePhotosNotifications(false);
                }
            }
        });
    }

    private static Photo convertPhoto(ParseObject object) {
        byte[] data = null;
        try {
            data = object.getParseFile("file").getData();
        } catch (com.parse.ParseException e) {
        }
        if (data == null) {
            return null;
        }
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

        String author = object.getString("authorId");
        String first = object.getString("first_name");
        String last = object.getString("last_name");
        int age = object.getInt("age");
        String gender = object.getString("gender");

        ParseGeoPoint location = object.getParseGeoPoint("currentLocation");
        return new Photo(object, author, first, last, age, gender, location, bitmap);
    }
}
