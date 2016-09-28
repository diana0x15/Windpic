package com.wind.windpic.tools;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.wind.windpic.MainActivity;
import com.wind.windpic.WindpicApplication;
import com.wind.windpic.fragments.CameraFragment;
import com.wind.windpic.fragments.FriendsFragment;
import com.wind.windpic.fragments.PhotosFragment;
import com.wind.windpic.objects.Friend;
import com.wind.windpic.objects.Photo;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dianapislaru on 10/10/15.
 */
public class Listeners {

    public static final String TAG = "Listeners";

    public static ChildEventListener mPhotosListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot snapshot, String s) {

            getPhotoFromParse(snapshot);
            WindpicApplication.FIREBASE.child("NEW_PHOTOS")
                    .child(MainActivity.CURRENT_USER.getObjectId())
                    .child(snapshot.getKey()).removeValue();

            WindpicApplication.PHOTOS = true;
            CameraFragment.updatePhotosNotifications(true);
            FriendsFragment.updatePhotosNotifications(true);

            Log.i(TAG, "new photo");
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
        }

        @Override
        public void onCancelled(FirebaseError firebaseError) {
        }
    };

    public static ChildEventListener mAcceptedRequestsListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot snapshot, String s) {
            String id = snapshot.getValue().toString();
            addUserToFriends(id);
            for (ParseUser user : WindpicApplication.REQUESTS_LIST) {
                if (user.getObjectId().equals(id)) {
                    WindpicApplication.REQUESTS_LIST.remove(user);
                    if (WindpicApplication.REQUESTS_LIST.size() == 0) {
                        CameraFragment.updateRequestsNotifications(false);
                        FriendsFragment.updateRequestsNotifications(false);
                        PhotosFragment.updateRequestsNotifications(false);
                    }
                }
            }
            WindpicApplication.FIREBASE.child("ACCEPTED_FRIEND_REQUESTS")
                    .child(MainActivity.CURRENT_USER.getObjectId())
                    .child(snapshot.getKey()).removeValue();

        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
        }

        @Override
        public void onCancelled(FirebaseError firebaseError) {
        }
    };

    public static ChildEventListener mRemovedFriendListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot snapshot, String s) {
            removeUserFromFriends(snapshot.getValue().toString());
            WindpicApplication.FIREBASE.child("UNFRIEND")
                    .child(MainActivity.CURRENT_USER.getObjectId())
                    .child(snapshot.getKey()).removeValue();
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
        }

        @Override
        public void onCancelled(FirebaseError firebaseError) {
        }
    };

    public static ChildEventListener mUpdateListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            String id = dataSnapshot.getValue().toString();
            List<ParseUser> friends = MainActivity.CURRENT_USER.getList("friends");
            for (ParseUser f : friends) {
                if (f.getObjectId().equals(id)) {
                    f.fetchInBackground();
                    f.pinInBackground();
                }
            }
            WindpicApplication.FIREBASE.child("UPDATE")
                    .child(MainActivity.CURRENT_USER.getObjectId())
                    .child(id).removeValue();
            if (FriendsFragment.mAdapter != null) {
                FriendsFragment.mAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(FirebaseError firebaseError) {

        }
    };

    private static void getPhotoFromParse(DataSnapshot snapshot) {
        String id = snapshot.getValue().toString();
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Snap");
        query.getInBackground(id, new GetCallback<ParseObject>() {
            public void done(final ParseObject object, ParseException e) {
                if (e == null) {
                    Photo photo = convertPhoto(object);
                    if (!matchesGenderPreferences(photo)) {
                        return;
                    }
                    object.pinInBackground(MainActivity.CURRENT_USER.getObjectId());
                    WindpicApplication.PHOTOS_LIST.add(photo);

                    if (MainActivity.FRAGMENT_PHOTOS && PhotosFragment.mSwipe != null && PhotosFragment.mFrontPhoto == null) {
                        PhotosFragment.mFrontPhoto = photo;
                        PhotosFragment.mSwipe.show();
                    }
                }
            }
        });
    }

    private static boolean matchesGenderPreferences(Photo photo) {
        Log.i(TAG, photo.getGender());
        if (MainActivity.CURRENT_USER.getString("searchGender").equals("FM"))
            return true;
        if (!photo.getGender().equals(MainActivity.CURRENT_USER.getString("gender")))
            return false;
        return true;
    }

    public static void addUserToFriends(String id) {
        // check if is already a friend
        List<ParseUser> friends = MainActivity.CURRENT_USER.getList("friends");
        for (ParseUser friend : friends) {
            if (friend.getObjectId().equals(id))
                return;
        }

        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo("objectId", id);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> list, ParseException e) {
                if (list != null && list.size() > 0) {
                    ParseUser user = list.get(0);
                    if (user != null) {
                        List<ParseUser> friends = MainActivity.CURRENT_USER.getList("friends");
                        if (friends == null) {
                            friends = new ArrayList<>();
                            friends.add(user);
                            MainActivity.CURRENT_USER.put("friends", friends);
                        } else {
                            MainActivity.CURRENT_USER.getList("friends").add(user);
                        }
                        MainActivity.CURRENT_USER.saveEventually();
                        user.pinInBackground();

                        if (WindpicApplication.FRIENDS_LIST != null) {
                            Friend f = new Friend(user);
                            WindpicApplication.FRIENDS_LIST.add(f);
                            FriendsListener.setLastMessageListener(f);
                        }
                        if (FriendsFragment.mAdapter != null) {
                            FriendsFragment.mAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }
        });
    }

    public static void removeUserFromFriends(String id) {
        List<ParseUser> friends = MainActivity.CURRENT_USER.getList("friends");
        for (int i = 0; i < friends.size(); ++i) {
            ParseUser friend = friends.get(i);
            if (friend.getObjectId().equals(id)) {
                MainActivity.CURRENT_USER.getList("friends").remove(i);
                MainActivity.CURRENT_USER.saveEventually();

                if (WindpicApplication.FRIENDS_LIST != null) {
                    WindpicApplication.FRIENDS_LIST.remove(new Friend(friend));
                }
                if (FriendsFragment.mAdapter != null) {
                    FriendsFragment.mAdapter.notifyDataSetChanged();
                }
                return;
            }
        }
    }

    private static Photo convertPhoto(ParseObject object) {
        byte[] data = null;
        try {
            data = object.getParseFile("file").getData();
        } catch (ParseException e) {
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
