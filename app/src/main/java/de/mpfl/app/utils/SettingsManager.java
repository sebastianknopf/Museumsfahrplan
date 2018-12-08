package de.mpfl.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;

public final class SettingsManager {

    private final static String SETTINGS_MANAGER = "SETTINGS_MANAGER";

    public final static String MAP_LAST_LATITUDE = "MAP_LAST_LATITUDE";
    public final static String MAP_LAST_LONGITUDE = "MAP_LAST_LONGITUDE";
    public final static String MAP_LAST_ZOOMLEVEL = "MAP_LAST_ZOOMLEVEL";

    private Context context;

    public SettingsManager(Context context) {
        this.context = context;
    }

    public Location getLastMapPosition() {
        SharedPreferences sp = this.context.getSharedPreferences(SETTINGS_MANAGER, Context.MODE_PRIVATE);
        double lastLat = sp.getFloat(MAP_LAST_LATITUDE, 0.0f);
        double lastLon = sp.getFloat(MAP_LAST_LONGITUDE, 0.0f);

        if(lastLat != 0.0f && lastLon != 0.0f) {
            Location location = new Location(LocationManager.GPS_PROVIDER);
            location.setLatitude(lastLat);
            location.setLongitude(lastLon);
            return location;
        } else {
            return null;
        }
    }

    public void setLastMapLocation(Location location) {
        SharedPreferences.Editor sp = this.context.getSharedPreferences(SETTINGS_MANAGER, Context.MODE_PRIVATE).edit();
        sp.putFloat(MAP_LAST_LATITUDE, (float) location.getLatitude());
        sp.putFloat(MAP_LAST_LONGITUDE, (float) location.getLongitude());
        sp.commit();
    }

    public double getLastMapZoomlevel() {
        SharedPreferences sp = this.context.getSharedPreferences(SETTINGS_MANAGER, Context.MODE_PRIVATE);
        return sp.getFloat(MAP_LAST_ZOOMLEVEL, 10.0f);
    }

    public void setLastMapZoomlevel(double lastZoomlevel) {
        SharedPreferences.Editor sp = this.context.getSharedPreferences(SETTINGS_MANAGER, Context.MODE_PRIVATE).edit();
        sp.putFloat(MAP_LAST_ZOOMLEVEL, (float) lastZoomlevel);
        sp.commit();
    }
}
