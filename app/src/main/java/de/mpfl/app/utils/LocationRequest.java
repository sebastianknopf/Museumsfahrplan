package de.mpfl.app.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public final class LocationRequest implements LocationListener {

    private LocationManager locationManager;
    private OnLocationReceivedListener listener;

    public LocationRequest(Activity activity) {
        if(activity instanceof OnLocationReceivedListener) {
            this.listener = (OnLocationReceivedListener) activity;
        } else {
            throw new RuntimeException(activity.toString() + " must implement OnLocationReceivedListener!");
        }

        this.locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
    }

    public LocationRequest(Context context, OnLocationReceivedListener listener) {
        this.listener = listener;
        this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    @SuppressLint("MissingPermission")
    public void getLocationOnce(String provider) {
        this.locationManager.requestLocationUpdates(provider, 0, 0, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        this.listener.onLocationReceived(location);
        this.locationManager.removeUpdates(this);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    public interface OnLocationReceivedListener {
        void onLocationReceived(Location location);
    }

}
