package com.wind.windpic.tools;

import android.util.Log;

import com.facebook.Profile;

/**
 * Created by dianapislaru on 18/11/15.
 */
public class ProfileTracker {

    private com.facebook.ProfileTracker mProfileTracker;

    public ProfileTracker() {
        mProfileTracker = new com.facebook.ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                if (oldProfile == null)
                    Log.i("New profile", "null");
                else
                    Log.i("New profile", "not null");
            }
        };
    }

    public void startTracking() {
        mProfileTracker.startTracking();
    }

    public void stopTracking() {
        mProfileTracker.stopTracking();
    }

    public boolean isTracking() {
        return mProfileTracker.isTracking();
    }
}
