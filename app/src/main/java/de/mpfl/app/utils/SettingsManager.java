package de.mpfl.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.support.v7.preference.PreferenceManager;

import de.mpfl.app.R;
import de.mpfl.app.fragments.SettingsFragment;

public final class SettingsManager {

    private final static String SETTINGS_MANAGER = "SETTINGS_MANAGER";

    public final static String MAP_LAST_LATITUDE = "MAP_LAST_LATITUDE";
    public final static String MAP_LAST_LONGITUDE = "MAP_LAST_LONGITUDE";
    public final static String MAP_LAST_ZOOMLEVEL = "MAP_LAST_ZOOMLEVEL";
    public final static String MFPL_APP_ID = "APP_ID";
    public final static String MFPL_API_KEY = "API_KEY";

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

    public boolean getPreferenceWheelchairAccessible() {
        SharedPreferences defaultPreference = PreferenceManager.getDefaultSharedPreferences(this.context);
        return defaultPreference.getBoolean(SettingsFragment.KEY_WHEELCHAIR_ACCESSIBLE, false);
    }

    public boolean getPreferenceBikesAllowed() {
        SharedPreferences defaultPreference = PreferenceManager.getDefaultSharedPreferences(this.context);
        return defaultPreference.getBoolean(SettingsFragment.KEY_BIKES_ALLOWED, false);
    }

    public int getPreferencesNumResults() {
        SharedPreferences defaultPreference = PreferenceManager.getDefaultSharedPreferences(this.context);
        int result = 10;

        try {
            result = Integer.parseInt(defaultPreference.getString(SettingsFragment.KEY_NUM_RESULTS, "10"));
        } catch (Exception ignored) {
        }

        return result;
    }

    public String getAppId() {
        SharedPreferences defaultPreference = this.context.getSharedPreferences(SETTINGS_MANAGER, Context.MODE_PRIVATE);
        return defaultPreference.getString(MFPL_APP_ID, this.context.getString(R.string.MFPL_APP_ID));
    }

    public String getApiKey() {
        SharedPreferences defaultPreference = this.context.getSharedPreferences(SETTINGS_MANAGER, Context.MODE_PRIVATE);
        return defaultPreference.getString(MFPL_API_KEY, this.context.getString(R.string.MFPL_API_KEY));
    }

    public void setApiCredentials(String appId, String apiKey) {
        SharedPreferences.Editor sp = this.context.getSharedPreferences(SETTINGS_MANAGER, Context.MODE_PRIVATE).edit();
        sp.putString(MFPL_APP_ID, appId);
        sp.putString(MFPL_API_KEY, apiKey);
        sp.commit();
    }

    public void resetApiCredentials() {
        SharedPreferences.Editor sp = this.context.getSharedPreferences(SETTINGS_MANAGER, Context.MODE_PRIVATE).edit();
        sp.putString(MFPL_APP_ID, this.context.getString(R.string.MFPL_APP_ID));
        sp.putString(MFPL_API_KEY, this.context.getString(R.string.MFPL_API_KEY));
        sp.commit();
    }

    public boolean isSecondaryAuthentification() {
        return !this.getAppId().equals(this.context.getString(R.string.MFPL_APP_ID));
    }

}
