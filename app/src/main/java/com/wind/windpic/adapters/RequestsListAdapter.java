package com.wind.windpic.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.parse.ParseUser;
import com.wind.windpic.MainActivity;
import com.wind.windpic.R;
import com.wind.windpic.WindpicApplication;
import com.wind.windpic.fragments.CameraFragment;
import com.wind.windpic.fragments.FriendsFragment;
import com.wind.windpic.fragments.PhotosFragment;
import com.wind.windpic.fragments.profiles.RequestProfileFragment;
import com.wind.windpic.objects.Friend;
import com.wind.windpic.tools.Listeners;

/**
 * Created by dianapislaru on 10/10/15.
 */
public class RequestsListAdapter extends ArrayAdapter<ParseUser> {

    public static final String TAG = "RequestsImageAdapter";
    private static final String USER_PHOTO = "photo";
    private static final String USER_NAME = "name";
    private static final String USER_AGE = "age";
    private static final String USER_GENDER = "gender";
    private static final String USER_DISTANCE = "distance";

    private Activity mActivity;
    private ListView mListView;

    public RequestsListAdapter(Activity context, ListView listView) {
        super(context, -1, WindpicApplication.REQUESTS_LIST);
        mActivity = context;
        mListView = listView;
    }

    @Override
    public int getCount() {
        return WindpicApplication.REQUESTS_LIST.size();
    }

    @Override
    public ParseUser getItem(int index) {
        return WindpicApplication.REQUESTS_LIST.get(index);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup container) {

        LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.request_item, container, false);

        final ImageView imageView = (ImageView) view.findViewById(R.id.request_item_imageView);
        final TextView nameTextView = (TextView) view.findViewById(R.id.request_item_name_textView);
        final TextView infoTextView = (TextView) view.findViewById(R.id.request_item_info_textView);
        Button acceptButton = (Button) view.findViewById(R.id.request_item_accept_button);
        Button declineButton = (Button) view.findViewById(R.id.request_item_decline_button);

        final Friend friend = new Friend(WindpicApplication.REQUESTS_LIST.get(position));


        byte[] data = friend.getPicture();
        if (data != null) {
            imageView.setImageBitmap(BitmapFactory.decodeByteArray(data, 0, data.length));
        }
        nameTextView.setText(friend.getName());

        int distance = friend.getDistanceInMiles();
        String info = "";
        if (distance <= 1) {
            info = friend.getAge() + ", " +
                    friend.getGender() + ", less than a mile away";
        } else {
            info = friend.getAge() + ", " +
                    friend.getGender() + ", " + distance + " miles away";
        }
        infoTextView.setText(info);

        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListView.setAdapter(null);
                WindpicApplication.REQUESTS_LIST.remove(position);
                mListView.setAdapter(RequestsListAdapter.this);
                addUserToFriends(friend.getId());

                if (WindpicApplication.REQUESTS_LIST.size() == 0) {
                    WindpicApplication.REQUESTS = false;
                    hideRequestsNotificationIcon();
                    MainActivity.hideRequestNotifications();
                }
            }
        });

        declineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListView.setAdapter(null);
                removeUser(WindpicApplication.REQUESTS_LIST.get(position));
                mListView.setAdapter(RequestsListAdapter.this);
                if (WindpicApplication.REQUESTS_LIST.size() == 0) {
                    WindpicApplication.REQUESTS = false;
                    hideRequestsNotificationIcon();
                    MainActivity.hideRequestNotifications();
                }
            }
        });
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RequestProfileFragment fragment = new RequestProfileFragment();
                if (friend == null) {
                    return;
                }
                Bundle bundle = new Bundle();
                bundle.putByteArray(USER_PHOTO, friend.getPicture());
                bundle.putString(USER_NAME, friend.getName());
                bundle.putInt(USER_AGE, friend.getAge());
                bundle.putString(USER_GENDER, friend.getGender());
                bundle.putString(USER_DISTANCE, friend.getDistanceInMiles() + " miles away");
                fragment.setArguments(bundle);
                openUserProfileFragment(fragment);
            }
        });

        return view;
    }

    private void openUserProfileFragment(Fragment fragment) {
        FragmentManager manager = ((AppCompatActivity) mActivity).getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(R.id.fragment_container, fragment);
        transaction.addToBackStack("requests");
        transaction.commit();
    }


    private void addUserToFriends(String id) {
        Listeners.addUserToFriends(id);
        removeRequestChild(id);
        WindpicApplication.FIREBASE.child("ACCEPTED_FRIEND_REQUESTS")
                .child(id).push().setValue(MainActivity.CURRENT_USER.getObjectId());
    }

    private void removeUser(ParseUser user) {
        removeRequestChild(user.getObjectId());
        WindpicApplication.REQUESTS_LIST.remove(user);
        if (WindpicApplication.REQUESTS_LIST.size() == 0) {
            WindpicApplication.REQUESTS = false;
            hideRequestsNotificationIcon();
            MainActivity.hideRequestNotifications();

        }
    }

    private void removeRequestChild(String id) {
        for (DataSnapshot snapshot : WindpicApplication.REQUESTS_SNAPSHOT.getChildren()) {
            if (snapshot.getValue().toString().equals(id)) {
                WindpicApplication.FIREBASE.child("PENDING_FRIEND_REQUESTS")
                        .child(MainActivity.CURRENT_USER.getObjectId())
                        .child(snapshot.getKey()).removeValue();
            }
        }
    }

    private void hideRequestsNotificationIcon() {
        CameraFragment.updateRequestsNotifications(false);
        PhotosFragment.updateRequestsNotifications(false);
        FriendsFragment.updateRequestsNotifications(false);
    }

}
