package com.wind.windpic.tools;

import com.wind.windpic.MainActivity;
import com.wind.windpic.WindpicApplication;
import com.wind.windpic.fragments.CameraFragment;
import com.wind.windpic.fragments.FriendsFragment;
import com.wind.windpic.fragments.PhotosFragment;
import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dianapislaru on 08/11/15.
 */
public class RequestsListener {

    public static void getRequests(final ParseUser currentUser) {
        WindpicApplication.REQUESTS_LIST = new ArrayList<>();
        WindpicApplication.FIREBASE.child("PENDING_FRIEND_REQUESTS")
                .child(currentUser.getObjectId())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        WindpicApplication.REQUESTS_SNAPSHOT = dataSnapshot;
                        if (dataSnapshot == null || dataSnapshot.getValue() == null || dataSnapshot.getChildrenCount() == 0) {
                            return;
                        }
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            getRequestUser(snapshot, currentUser);
                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                    }
                });
    }

    public static void getRequestUser(final DataSnapshot snapshot, final ParseUser currentUser) {
        final String id = snapshot.getValue().toString();

        // Check if is already a friend
        List<ParseUser> friends = currentUser.getList("friends");
        for (ParseUser friend : friends) {
            if (friend.getObjectId().equals(id)) {
                return;
            }
        }

        // Get the user from Parse
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo("objectId", id);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> list, ParseException e) {
                if (e == null && list.size() > 0) {
                    ParseUser user = list.get(0);
                    if (user != null && !isInList(id)) {
                        WindpicApplication.REQUESTS_LIST.add(user);
                    }
                }
            }
        });

        WindpicApplication.REQUESTS = true;
        showRequestsNotificationIcon();
        MainActivity.showRequestNotifications();
    }


    public static boolean isInList(String id) {
        for (ParseUser user : WindpicApplication.REQUESTS_LIST) {
            if (user.getObjectId().equals(id)) {
                return true;
            }
        }
        return false;
    }

    public static void showRequestsNotificationIcon() {
        CameraFragment.updateRequestsNotifications(true);
        PhotosFragment.updateRequestsNotifications(true);
        FriendsFragment.updateRequestsNotifications(true);

    }
}
