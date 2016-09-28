package com.wind.windpic.tools;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;

import com.wind.windpic.MainActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.parse.ParseGeoPoint;

/**
 * Created by dianapislaru on 27/09/15.
 */
public class UserLocation implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    public static final String TAG = "UserLocation";

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Double mCurrentLongitude = 0.0;
    private Double mCurrentLatitude = 0.0;

    public UserLocation(Context context) {
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    public void connect() {
        mGoogleApiClient.connect();
    }

    public void disconnect() {
        mGoogleApiClient.disconnect();
    }

    public Double getLongitude() {
        return mCurrentLongitude;
    }

    public Double getLatitude() {
        return mCurrentLatitude;
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        updateLocationToParse();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLatitude = location.getLatitude();
        mCurrentLongitude = location.getLongitude();
        updateLocationToParse();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    private void updateLocationToParse() {
        if (mCurrentLatitude != 0.0 && mCurrentLongitude != 0.0) {
            ParseGeoPoint point = new ParseGeoPoint(mCurrentLatitude, mCurrentLongitude);
            if (MainActivity.CURRENT_USER != null) {
                MainActivity.CURRENT_USER.put("currentLocation", point);
                MainActivity.CURRENT_USER.saveInBackground();
            }
        }
        else {
            mCurrentLatitude = 47.292575;
            mCurrentLongitude = 24.385941;
            ParseGeoPoint point = new ParseGeoPoint(mCurrentLatitude, mCurrentLongitude);
            if (MainActivity.CURRENT_USER != null) {
                MainActivity.CURRENT_USER.put("currentLocation", point);
                MainActivity.CURRENT_USER.saveInBackground();
            }
        }
    }
}
