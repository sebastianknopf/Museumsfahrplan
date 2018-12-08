package de.mpfl.app.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import java.text.SimpleDateFormat;
import java.util.Date;

import de.mfpl.staticnet.lib.StaticRequest;
import de.mfpl.staticnet.lib.base.Delivery;
import de.mfpl.staticnet.lib.base.Request;
import de.mfpl.staticnet.lib.data.Position;
import de.mfpl.staticnet.lib.data.Stop;
import de.mpfl.app.R;
import de.mpfl.app.databinding.FragmentMapOverviewBinding;
import de.mpfl.app.layout.BottomSheetActionController;
import de.mpfl.app.utils.LocationRequest;
import de.mpfl.app.utils.SettingsManager;
import de.mpfl.app.utils.VectorIconFactory;


public class MapOverviewFragment extends Fragment implements LocationListener, MapboxMap.OnCameraIdleListener, MapboxMap.OnMarkerClickListener {

    public final static String TAG ="MapOverviewFragment";

    private final static int PERMISSION_ACCESS_LOCATION = 0;

    private FragmentMapOverviewBinding components;
    private BottomSheetBehavior bottomSheetBehavior;
    private LocationManager locationManager;
    private MapboxMap currentMap = null;
    private Icon markerIcon = null;
    private Snackbar snackbar = null;

    public MapOverviewFragment() {
    }

    public static MapOverviewFragment newInstance() {
        MapOverviewFragment fragment = new MapOverviewFragment();
        return fragment;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.markerIcon = VectorIconFactory.fromVectorDrawable(this.getContext(), R.drawable.ic_marker);
        this.locationManager = (LocationManager) this.getContext().getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Mapbox.getInstance(this.getActivity().getApplicationContext(), this.getString(R.string.MAPBOX_PUBLIC_KEY));

        this.components = DataBindingUtil.inflate(inflater, R.layout.fragment_map_overview, container, false);
        this.components.setFragment(this);
        this.components.mapViewHolder.mapView.onCreate(savedInstanceState);
        this.components.mapViewHolder.mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap resultMap) {
                currentMap = resultMap;
                currentMap.addOnCameraIdleListener(MapOverviewFragment.this);
                currentMap.setOnMarkerClickListener(MapOverviewFragment.this);

                moveMapToLastPosition();
            }
        });

        // setup bottom sheet behaviour
        this.bottomSheetBehavior = BottomSheetBehavior.from(this.components.bottomSheetHolder.bottomSheet);
        this.bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        this.bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View view, int newState) {
                ImageButton toggleButton = components.bottomSheetHolder.btnToggle;
                if(newState == BottomSheetBehavior.STATE_EXPANDED) {
                    toggleButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_arrow_down));
                } else {
                    toggleButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_arrow_up));
                }
            }

            @Override
            public void onSlide(@NonNull View view, float v) {

            }
        });

        // expand / hide bottom sheet on button click
        this.components.bottomSheetHolder.btnToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int currentState = bottomSheetBehavior.getState();
                if(currentState == BottomSheetBehavior.STATE_EXPANDED) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                } else {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            }
        });

        // set action controller and for bottomsheet
        this.components.bottomSheetHolder.setActionController(new BottomSheetActionController(this.getContext(), this.components.bottomSheetHolder));

        return this.components.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();

        // check permissions and try to achieve them
        // do this only here (and not in onResume!) cause it will display a dialog
        this.checkRequiredPermissions();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onResume() {
        super.onResume();

        this.checkGPSStatus();
        this.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1000, this);

        // check network status
        if(!this.checkNetworkStatus()) {
            return;
        }

        // check gps status
        if(!this.checkGPSStatus()) {
            return;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        this.dismissSnackbar();

        this.locationManager.removeUpdates(this);
    }

    @Override
    public void onStop() {
        super.onStop();

        CameraPosition cameraPosition = this.currentMap.getCameraPosition();
        Location currentMapLocation = new Location(LocationManager.GPS_PROVIDER);
        currentMapLocation.setLatitude(cameraPosition.target.getLatitude());
        currentMapLocation.setLongitude(cameraPosition.target.getLongitude());

        SettingsManager settingsManager = new SettingsManager(this.getContext());
        settingsManager.setLastMapLocation(currentMapLocation);
        settingsManager.setLastMapZoomlevel(cameraPosition.zoom);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        this.components.mapViewHolder.mapView.onDestroy();
    }

    @Override
    public void onLocationChanged(Location location){
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
        this.dismissSnackbar();
    }

    @Override
    public void onProviderDisabled(String provider) {
        this.checkGPSStatus();
    }

    @Override
    public void onCameraIdle() {
        CameraPosition cameraPosition = this.currentMap.getCameraPosition();

        if(cameraPosition.zoom > 11.0) {
            this.dismissSnackbar();

            StaticRequest staticRequest = new StaticRequest();
            staticRequest.setAppId(this.getString(R.string.MFPL_APP_ID));
            staticRequest.setApiKey(this.getString(R.string.MFPL_API_KEY));

            Request.Filter filter = new Request.Filter();
            filter.setDate(Request.Filter.Date.fromJavaDate(new Date()));
            filter.setTime(new SimpleDateFormat("HH:mm:ss").format(new Date()));

            staticRequest.setListener(new StaticRequest.Listener() {
                @Override
                public void onSuccess(Delivery delivery) {
                    currentMap.clear();
                    for(Stop stop : delivery.getStops()) {
                        currentMap.addMarker(new MarkerOptions()
                                .position(new LatLng(stop.getPosition().getLatitude(), stop.getPosition().getLongitude()))
                                .setTitle(stop.getStopName())
                                .setSnippet(stop.getStopId())
                                .icon(markerIcon));
                    }
                }

                @Override
                public void onError(Throwable throwable) {
                    Log.d(TAG, "some error in request...!");
                }
            }).loadStops(new Position().setLatitude(cameraPosition.target.getLatitude()).setLongitude(cameraPosition.target.getLongitude()), 10000, filter);
        } else {
            this.currentMap.clear();

            if(this.snackbar == null || !this.snackbar.isShown()) {
                this.displaySnackbar(this.components.fragmentLayout, R.string.str_zoom_level_hint, Snackbar.LENGTH_LONG);
            }
        }
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        this.bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        this.components.bottomSheetHolder.lblTitle.setText(marker.getTitle());
        this.components.bottomSheetHolder.getActionController().loadDepartures(marker.getSnippet());

        return true;
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, final @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(grantResults.length < 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            int stringResId = 0;
            switch(requestCode) {
                case PERMISSION_ACCESS_LOCATION:
                    stringResId = R.string.str_location_access_error;
                    break;
            }

            this.displaySnackbar(this.components.fragmentLayout, stringResId, Snackbar.LENGTH_INDEFINITE, R.string.str_enable, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    requestPermission(requestCode, permissions[0]);
                }
            });
        } else {
            this.checkRequiredPermissions();
        }
    }

    @SuppressLint("MissingPermission")
    public void fabLocationClick(View view) {
        this.checkGPSStatus();
        this.moveMapToCurrentPosition();
    }

    private boolean checkPermission(String permissionName) {
        return ContextCompat.checkSelfPermission(this.getActivity(), permissionName) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission(int requestCode, String permissionName) {
        requestPermissions(new String[] {permissionName}, requestCode);
    }

    private void checkRequiredPermissions() {
        // check location permission
        if(!this.checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            // eventually hide fab
            this.components.fabLocation.hide();

            // try to request the missing permission
            this.requestPermission(PERMISSION_ACCESS_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION);
            return;
        }

        // this is only executed if the required permissions are granted
        this.components.fabLocation.show();

        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) this.components.fabLocation.getLayoutParams();
        FloatingActionButton.Behavior behavior = (FloatingActionButton.Behavior) params.getBehavior();
        if(behavior != null) {
            behavior.setAutoHideEnabled(true);
        }
    }

    private boolean checkNetworkStatus() {
        return true;
    }

    private boolean checkGPSStatus() {
        LocationManager locationManager = (LocationManager) this.getContext().getSystemService(Context.LOCATION_SERVICE);
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            this.displaySnackbar(this.components.fragmentLayout, R.string.str_location_provider_error, Snackbar.LENGTH_INDEFINITE, R.string.str_activate, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(settingsIntent);
                }
            });

            return false;
        } else {
            return true;
        }
    }

    private void displaySnackbar(View parentView, @StringRes int stringResId, int snackbarLength) {
        this.displaySnackbar(parentView, stringResId, snackbarLength, 0, null);
    }

    private void displaySnackbar(View parentView, @StringRes int messageStringResId, int snackbarLength, @StringRes int buttonStringResId, View.OnClickListener listener) {
        this.dismissSnackbar();

        this.snackbar = Snackbar.make(parentView, messageStringResId, snackbarLength);
        if(buttonStringResId != 0 && listener != null) {
            this.snackbar.setAction(buttonStringResId, listener).setActionTextColor(this.getResources().getColor(R.color.colorAccentDAY));
        }

        this.snackbar.show();
    }

    private void dismissSnackbar() {
        if(this.snackbar != null && this.snackbar.isShown()) {
            this.snackbar.dismiss();
        }
    }

    private void moveMapToLastPosition() {
        SettingsManager settingsManager = new SettingsManager(this.getContext());
        Location lastMapLocation = settingsManager.getLastMapPosition();
        if(lastMapLocation != null) {
            this.moveMapToPosition(lastMapLocation, settingsManager.getLastMapZoomlevel());
        } else {
            this.moveMapToCurrentPosition();
        }
    }

    private void moveMapToCurrentPosition() {
        Criteria criteria = new Criteria();
        criteria.setPowerRequirement(Criteria.POWER_MEDIUM);
        criteria.setAccuracy(Criteria.ACCURACY_MEDIUM);
        criteria.setSpeedRequired(false);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(false);

        String locationProvider = this.locationManager.getBestProvider(criteria, true);
        new LocationRequest(this.getActivity(), new LocationRequest.OnLocationReceivedListener() {
            @Override
            public void onLocationReceived(Location location) {
                moveMapToPosition(location, 13.0);
            }
        }).getLocationOnce(locationProvider);
    }

    private void moveMapToPosition(Location location, double zoomLevel) {
        if(currentMap != null) {
            CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(location.getLatitude(), location.getLongitude())).zoom(zoomLevel).build();
            currentMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 2000);
        }
    }
}
