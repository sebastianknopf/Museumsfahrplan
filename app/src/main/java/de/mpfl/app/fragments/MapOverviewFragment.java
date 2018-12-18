package de.mpfl.app.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.location.Location;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
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
import java.util.List;

import de.mfpl.staticnet.lib.StaticRequest;
import de.mfpl.staticnet.lib.base.Delivery;
import de.mfpl.staticnet.lib.base.Request;
import de.mfpl.staticnet.lib.data.Position;
import de.mfpl.staticnet.lib.data.Stop;
import de.mfpl.staticnet.lib.data.Trip;
import de.mpfl.app.R;
import de.mpfl.app.adapters.AlertListAdapter;
import de.mpfl.app.controllers.BottomSheetActionController;
import de.mpfl.app.databinding.FragmentMapOverviewBinding;
import de.mpfl.app.listeners.OnFragmentInteractionListener;
import de.mpfl.app.listeners.OnTripItemClickListener;
import de.mpfl.app.utils.DateTimeFormat;
import de.mpfl.app.utils.SettingsManager;
import de.mpfl.app.utils.VectorIconFactory;


public class MapOverviewFragment extends Fragment implements MapboxMap.OnCameraIdleListener, MapboxMap.OnMarkerClickListener {

    public final static String TAG ="MapOverviewFragment";
    public final static String KEY_FRAGMENT_ACTION = "KEY_FRAGMENT_ACTION";
    public final static String KEY_TRIP_ID = "KEY_TRIP_ID";
    public final static String KEY_TRIP_TIME = "KEY_TRIP_TIME";
    public final static String KEY_TRIP_DATE = "KEY_TRIP_DATE";

    public final int ACTION_SELECT_TRIP = 0;

    private final static int PERMISSION_ACCESS_LOCATION = 0;

    private FragmentMapOverviewBinding components;
    private BottomSheetBehavior bottomSheetBehavior;
    private FusedLocationProviderClient locationProviderClient;
    private LocationCallback locationCallback;
    private OnFragmentInteractionListener fragmentInteractionListener;

    private MapboxMap currentMap = null;
    private Icon markerIcon = null;
    private Snackbar snackbar = null;

    private List<Stop> currentStopList;

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
        this.locationProviderClient = LocationServices.getFusedLocationProviderClient(this.getContext());
        this.locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null || locationResult.getLocations().size() == 0) {
                    return;
                }

                moveMapToPosition(locationResult.getLocations().get(0), 0);
            }
        };
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

                SettingsManager settingsManager = new SettingsManager(getContext());
                Location lastMapLocation = settingsManager.getLastMapPosition();
                if(lastMapLocation != null) {
                    moveMapToPosition(lastMapLocation, settingsManager.getLastMapZoomlevel());
                }
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

        // set action controller and for bottomsheet and item click fragmentInteractionListener on trip items
        this.components.bottomSheetHolder.setActionController(new BottomSheetActionController(this.getContext(), this.components.bottomSheetHolder));
        this.components.bottomSheetHolder.setOnTripItemClickListener(new OnTripItemClickListener() {
            @Override
            public void onTripItemClick(Trip tripItem) {
                Bundle arguments = new Bundle();
                arguments.putInt(KEY_FRAGMENT_ACTION, ACTION_SELECT_TRIP);
                arguments.putString(KEY_TRIP_ID, tripItem.getTripId());
                arguments.putString(KEY_TRIP_DATE, DateTimeFormat.from(new Date()).to(DateTimeFormat.YYYYMMDD));

                if(tripItem.getFrequency() != null) {
                    arguments.putString(KEY_TRIP_TIME, tripItem.getFrequency().getTripTime());
                }

                fragmentInteractionListener.onFragmentInteraction(MapOverviewFragment.this, arguments);
            }
        });

        return this.components.getRoot();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if(context instanceof  OnFragmentInteractionListener) {
            this.fragmentInteractionListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context + " must implement OnFragmentInteractionListener!");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        this.fragmentInteractionListener = null;
    }

    @Override
    public void onStart() {
        super.onStart();

        // check permissions and try to achieve them
        // do this only here (and not in onResume!) cause it will display a dialog
        this.checkEnvironmentConditions();
    }

    @Override
    public void onResume() {
        super.onResume();

        this.components.mapViewHolder.mapView.onResume();

        if(this.bottomSheetBehavior != null) {
            this.bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        this.dismissSnackbar();
        this.components.mapViewHolder.mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();

        // don't call this here... it sucks
        //this.components.mapViewHolder.mapView.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        this.components.mapViewHolder.mapView.onDestroy();
    }

    @Override
    public void onCameraIdle() {
        CameraPosition cameraPosition = this.currentMap.getCameraPosition();

        // store last location every time when camera idle is active
        Location currentMapLocation = new Location(LocationManager.GPS_PROVIDER);
        currentMapLocation.setLatitude(cameraPosition.target.getLatitude());
        currentMapLocation.setLongitude(cameraPosition.target.getLongitude());

        SettingsManager settingsManager = new SettingsManager(this.getContext());
        settingsManager.setLastMapLocation(currentMapLocation);
        settingsManager.setLastMapZoomlevel(cameraPosition.zoom);

        if(cameraPosition.zoom > 11.0) {
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
                    currentStopList = delivery.getStops();

                    for(int i = 0; i < currentStopList.size(); i++) {
                        currentMap.addMarker(new MarkerOptions()
                                .position(new LatLng(currentStopList.get(i).getPosition().getLatitude(), currentStopList.get(i).getPosition().getLongitude()))
                                .setTitle(currentStopList.get(i).getStopName())
                                .setSnippet(String.valueOf(i))
                                .icon(markerIcon));
                    }
                }

                @Override
                public void onError(Throwable throwable) {
                }
            }).loadStops(new Position().setLatitude(cameraPosition.target.getLatitude()).setLongitude(cameraPosition.target.getLongitude()), 15000, filter);
        } else {
            this.currentMap.clear();
        }
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        this.bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        Stop selectedStop = this.currentStopList.get(Integer.parseInt(marker.getSnippet()));

        this.components.bottomSheetHolder.lblTitle.setText(marker.getTitle());
        if(selectedStop.getRealtime() != null && selectedStop.getRealtime().hasAlerts()) {
            AlertListAdapter adapter = new AlertListAdapter(this.getContext(), selectedStop.getRealtime().getAlerts());
            this.components.bottomSheetHolder.lstStopAlerts.setVisibility(View.VISIBLE);
            this.components.bottomSheetHolder.lstStopAlerts.setAdapter(adapter);
        } else {
            this.components.bottomSheetHolder.lstStopAlerts.setVisibility(View.GONE);
        }

        this.components.bottomSheetHolder.getActionController().loadDepartures(selectedStop.getStopId());

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
            this.checkEnvironmentConditions();
        }
    }

    @SuppressLint("MissingPermission")
    public void fabLocationClick(View view) {
        LocationManager locationManager = (LocationManager) this.getContext().getSystemService(Context.LOCATION_SERVICE);
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            this.displaySnackbar(this.components.fragmentLayout, R.string.str_location_provider_error, Snackbar.LENGTH_INDEFINITE, R.string.str_activate, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(settingsIntent);
                }
            });

            return;
        }

        this.dismissSnackbar();

        try {
            LocationRequest locationRequest = new LocationRequest();
            locationRequest.setNumUpdates(1);
            locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

            this.locationProviderClient.requestLocationUpdates(locationRequest, this.locationCallback, null);
        } catch(SecurityException e) {
        }
    }

    private boolean checkPermission(String permissionName) {
        return ContextCompat.checkSelfPermission(this.getActivity(), permissionName) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission(int requestCode, String permissionName) {
        requestPermissions(new String[] {permissionName}, requestCode);
    }

    private void checkEnvironmentConditions() {
        // check location permission
        if(!this.checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            // eventually hide fab
            this.components.fabLocation.hide();

            // try to request the missing permission
            this.requestPermission(PERMISSION_ACCESS_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION);
            return;
        }

        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        if(googleApiAvailability.isGooglePlayServicesAvailable(this.getContext()) != ConnectionResult.SUCCESS) {
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

    private void moveMapToPosition(Location location, double zoomLevel) {
        if(currentMap != null && location != null) {
            if(zoomLevel == 0) {
                zoomLevel = 13.0;
            }

            CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(location.getLatitude(), location.getLongitude())).zoom(zoomLevel).build();

            currentMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 2000);
        }
    }
}
