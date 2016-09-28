package com.wind.windpic.tools;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by dianapislaru on 23/09/15.
 */
public class NetworkStateReceiver extends BroadcastReceiver {

    public static final String TAG = "NetworkStateReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        // Network connectivity change
        if (intent.getExtras() != null) {
            NetworkInfo info = (NetworkInfo) intent.getExtras().get(ConnectivityManager.EXTRA_NETWORK_INFO);
            if (info != null && info.getState() == NetworkInfo.State.CONNECTED) {
                // Network connected

            } else if (intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, Boolean.FALSE)) {
                // Network disconnected

            }
        }
    }
}
