package com.wind.windpic.tools;

import android.view.View;

import com.wind.windpic.MainActivity;
import com.wind.windpic.WindpicApplication;
import com.wind.windpic.fragments.FriendsFragment;
import com.wind.windpic.objects.Friend;
import com.wind.windpic.objects.Message;
import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by dianapislaru on 15/11/15.
 */
public class FriendsListener {

    public static void getFriends() {
        // prepare the friends
        WindpicApplication.FRIENDS_LIST = new ArrayList<>();
        final List<ParseUser> list = MainActivity.CURRENT_USER.getList("friends");
        if (list != null && list.size() > 0) {
            if (FriendsFragment.mEmptyTextView != null) {
                FriendsFragment.mEmptyTextView.setVisibility(View.INVISIBLE);
            }
            for (final ParseUser friend : list) {
                friend.fetchFromLocalDatastoreInBackground(new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject parseObject, ParseException e) {
                        if (parseObject == null) {
                            fetchFriendFromCloud(friend);
                        } else {
                            final Friend friend = new Friend((ParseUser) parseObject);
                            WindpicApplication.FRIENDS_LIST.add(friend);
                            setLastMessageListener(friend);
                            Collections.sort(WindpicApplication.FRIENDS_LIST, new FriendsListComparator());
                        }
                    }
                });
            }
        } else {
            if (FriendsFragment.mEmptyTextView != null) {
                FriendsFragment.mEmptyTextView.setVisibility(View.VISIBLE);
            }
        }
    }

    public static void setLastMessageListener(final Friend friend) {
        WindpicApplication.FIREBASE.child("LAST_MESSAGES").child(getChatId(friend.getId())).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, String> last_message = new HashMap<>();

                if (dataSnapshot == null || dataSnapshot.getValue() == null) {
                    Message message = WindpicApplication.DATABASE.getLastMessage(friend.getId());
                    if (message != null) {
                        last_message.put("text", message.getText());
                        last_message.put("date", message.getDate());
                        last_message.put(MainActivity.CURRENT_USER.getObjectId(), "seen");
                        last_message.put(friend.getId(), "seen");
                    }
                } else {
                    last_message = dataSnapshot.getValue(Map.class);
                    if (last_message.get("date") != null)
                    last_message.put("date", Methods.changeTimeZoneFromGMT(last_message.get("date")));
                }
                friend.setLastMessage(last_message);

                if (FriendsFragment.mAdapter != null) {
                    FriendsFragment.mAdapter.notifyDataSetChanged();
                    Collections.sort(WindpicApplication.FRIENDS_LIST, new FriendsListComparator());

                    if (last_message != null) {
                        if (last_message.get(MainActivity.CURRENT_USER.getObjectId())!= null) {
                            if (last_message.get(MainActivity.CURRENT_USER.getObjectId()).equals("unseen")) {
                                WindpicApplication.MESSAGES = true;
                                FriendsFragment.showMessageNotificationIcon(friend);
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });

    }

    private static void fetchFriendFromCloud(ParseUser friend) {
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo("objectId", friend.getObjectId());
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> list, ParseException e) {
                if (list != null && list.size() > 0) {
                    Friend f = new Friend(list.get(0));
                    WindpicApplication.FRIENDS_LIST.add(f);
                    list.get(0).pinInBackground();
                    Collections.sort(WindpicApplication.FRIENDS_LIST, new FriendsListComparator());
                    setLastMessageListener(f);
                }
            }
        });
    }

    private static String getChatId(String id) {
        String id1 = MainActivity.CURRENT_USER.getObjectId();
        if (id1.compareTo(id) < 0) {
            return id1 + id;
        } else {
            return id + id1;
        }
    }

}
