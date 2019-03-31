package de.mfpl.app.fragments;

import android.Manifest;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.PointF;
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
import android.support.v7.app.AppCompatActivity;
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
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import de.mfpl.api.lib.DataRequest;
import de.mfpl.api.lib.base.Delivery;
import de.mfpl.api.lib.base.Request;
import de.mfpl.api.lib.data.Position;
import de.mfpl.api.lib.data.Stop;
import de.mfpl.app.R;
import de.mfpl.app.adapters.AlertListAdapter;
import de.mfpl.app.common.DateTimeFormat;
import de.mfpl.app.common.SettingsManager;
import de.mfpl.app.common.VectorIconFactory;
import de.mfpl.app.controllers.BottomSheetActionController;
import de.mfpl.app.databinding.FragmentMapOverviewBinding;
import de.mfpl.app.dialogs.ErrorDialog;
import de.mfpl.app.listeners.OnFragmentInteractionListener;

public class MapOverviewFragment extends Fragment implements MapboxMap.OnCameraIdleListener, MapboxMap.OnMapClickListener {

    public final static String TAG ="MapOverviewFragment";
    public final static String KEY_FRAGMENT_ACTION = "KEY_FRAGMENT_ACTION";
    public final static String KEY_SEARCH_LAT = "KEY_SEARCH_LAT";
    public final static String KEY_SEARCH_LON = "KEY_SEARCH_LON";
    public final static String KEY_ROUTE_ID = "KEY_ROUTE_ID";
    public final static String KEY_ROUTE_NAME = "KEY_ROUTE_NAME";
    public final static String KEY_ROUTE_DATE = "KEY_ROUTE_DATE";
    public final static String KEY_ROUTE_TIME = "KEY_ROUTE_TIME";

    public final static int ACTION_OPEN_SEARCH = 1;
    public final static int ACTION_SELECT_ROUTE = 2;

    private final static int PERMISSION_ACCESS_LOCATION = 0;

    private FragmentMapOverviewBinding components;
    private BottomSheetBehavior bottomSheetBehavior;
    private FusedLocationProviderClient locationProviderClient;
    private LocationCallback locationCallback;
    private OnFragmentInteractionListener fragmentInteractionListener;

    private MapboxMap currentMap = null;
    private Bitmap markerBitmap = null;
    private Snackbar snackbar = null;

    private List<Stop> currentStopList;
    private boolean markerSelected = false;

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

        this.markerBitmap = VectorIconFactory.fromVectorDrawable(this.getContext(), R.drawable.ic_marker);
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
        this.components.mapView.onCreate(savedInstanceState);
        this.components.mapView.getMapAsync(resultMap -> {
            this.currentMap = resultMap;
            this.currentMap.addOnCameraIdleListener(MapOverviewFragment.this);
            this.currentMap.addOnMapClickListener(this);
            this.currentMap.addImage("icon.marker", this.markerBitmap);

            SettingsManager settingsManager = new SettingsManager(getContext());
            Location lastMapLocation = settingsManager.getLastMapPosition();
            if(lastMapLocation != null) {
                this.moveMapToPosition(lastMapLocation, settingsManager.getLastMapZoomlevel(), false);
            }

            // add layer for marker symbols
            FeatureCollection markerCollection = FeatureCollection.fromFeatures(new Feature[]{});
            GeoJsonSource markerSource = new GeoJsonSource("source.marker", markerCollection);
            this.currentMap.addSource(markerSource);
            SymbolLayer markerLayer = new SymbolLayer("layer.marker", "source.marker").withProperties(PropertyFactory.iconImage("icon.marker"));
            markerLayer.setProperties(PropertyFactory.iconAllowOverlap(true)); // this call is very important - otherwise some stop icons may be swallowed by the layer!
            this.currentMap.addLayer(markerLayer);

            // add layer for selected marker level
            FeatureCollection selectedMarkerCollection = FeatureCollection.fromFeatures(new Feature[]{});
            GeoJsonSource selectedMarkerSource = new GeoJsonSource("source.selected.marker", selectedMarkerCollection);
            this.currentMap.addSource(selectedMarkerSource);
            SymbolLayer selectedMarkerLayer = new SymbolLayer("layer.selected.marker", "source.selected.marker").withProperties(PropertyFactory.iconImage("icon.marker"));
            this.currentMap.addLayer(selectedMarkerLayer);
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
                    components.fabLocation.hide();
                } else {
                    toggleButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_arrow_up));
                    components.fabLocation.show();
                }
            }

            @Override
            public void onSlide(@NonNull View view, float v) {
            }
        });

        // expand / hide bottom sheet on button click
        this.components.bottomSheetHolder.btnToggle.setOnClickListener(view -> {
            int currentState = bottomSheetBehavior.getState();
            if(currentState == BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            } else {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });

        // set action controller and for bottomsheet and item click fragmentInteractionListener on trip items
        this.components.bottomSheetHolder.setActionController(new BottomSheetActionController(this.getContext(), this.components.bottomSheetHolder));
        this.components.bottomSheetHolder.setOnCalendarItemClickListener(object -> {
            Bundle arguments = new Bundle();
            arguments.putInt(KEY_FRAGMENT_ACTION, ACTION_SELECT_ROUTE);
            arguments.putString(KEY_ROUTE_ID, object.getRoute().getRouteId());
            arguments.putString(KEY_ROUTE_NAME, object.getRoute().getRouteLongName());
            arguments.putString(KEY_ROUTE_DATE, DateTimeFormat.from(object.getDate(), DateTimeFormat.YYYYMMDD).to(DateTimeFormat.DDMMYYYY));

            String thisDate = DateTimeFormat.from(new Date()).to(DateTimeFormat.DDMMYYYY);
            if(arguments.getString(KEY_ROUTE_DATE).equals(thisDate)) {
                arguments.putString(KEY_ROUTE_TIME, DateTimeFormat.from(new Date()).to(DateTimeFormat.HHMMSS));
            } else {
                arguments.putString(KEY_ROUTE_TIME, "00:00:01");
            }

            fragmentInteractionListener.onFragmentInteraction(this, arguments);
        });

        // set activity title from itels
        AppCompatActivity activity = (AppCompatActivity) this.getActivity();
        if(activity != null) {
            activity.getSupportActionBar().setTitle(R.string.str_map_overview_title);
        }

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

        this.components.mapView.onStart();

        // check permissions and try to achieve them
        // do this only here (and not in onResume!) cause it will display a dialog
        this.checkEnvironmentConditions();
    }

    @Override
    public void onResume() {
        super.onResume();

        this.components.mapView.onResume();

        if(this.bottomSheetBehavior != null) {
            this.bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        this.dismissSnackbar();
        this.components.mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        this.components.mapView.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        this.components.mapView.onDestroy();
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

        if(cameraPosition.zoom > 9.5) {
            this.loadStationData(cameraPosition);
            this.components.layZoomlevelHint.animate().setDuration(200).alpha(0.0f).start();
        } else {
            // remove all markers
            GeoJsonSource markerSource = currentMap.getSourceAs("source.marker");
            markerSource.setGeoJson(FeatureCollection.fromFeatures(new Feature[]{}));

            GeoJsonSource selectedMarkerSource = currentMap.getSourceAs("source.selected.marker");
            selectedMarkerSource.setGeoJson(FeatureCollection.fromFeatures(new Feature[]{}));

            this.components.layZoomlevelHint.animate().setDuration(200).alpha(1.0f).start();
        }
    }

    @Override
    public void onMapClick(@NonNull LatLng point) {
        final SymbolLayer marker = (SymbolLayer) this.currentMap.getLayer("layer.selected.marker");

        final PointF pixel = this.currentMap.getProjection().toScreenLocation(point);
        List<Feature> features = this.currentMap.queryRenderedFeatures(pixel, "layer.marker");
        List<Feature> selectedFeature = this.currentMap.queryRenderedFeatures(pixel, "layer.selected.marker");

        // load departures of selected stop
        if(features.size() > 0) {
            Feature stopFeature = features.get(0);
            Stop selectedStop = this.currentStopList.get(stopFeature.getNumberProperty("stop.index").intValue());

            this.bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            this.components.bottomSheetHolder.lblTitle.setText(selectedStop.getStopName());
            if(selectedStop.getRealtime() != null && selectedStop.getRealtime().hasAlerts()) {
                AlertListAdapter adapter = new AlertListAdapter(this.getContext(), selectedStop.getRealtime().getAlerts());
                this.components.bottomSheetHolder.lstStopAlerts.setVisibility(View.VISIBLE);
                this.components.bottomSheetHolder.lstStopAlerts.setAdapter(adapter);
            } else {
                this.components.bottomSheetHolder.lstStopAlerts.setVisibility(View.GONE);
            }

            this.components.bottomSheetHolder.getActionController().loadDepartures(selectedStop.getStopId());
        } else {
            if(this.markerSelected) {
                this.deselectMarker(marker);
            }

            this.bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            return;
        }

        // handle marker animation here
        if (selectedFeature.size() > 0 && this.markerSelected) {
            return;
        }

        FeatureCollection featureCollection = FeatureCollection.fromFeatures(new Feature[]{Feature.fromGeometry(features.get(0).geometry())});
        GeoJsonSource source = this.currentMap.getSourceAs("source.selected.marker");
        if (source != null) {
            source.setGeoJson(featureCollection);
        }

        if (this.markerSelected) {
            this.deselectMarker(marker);
        }

        if (features.size() > 0) {
            this.selectMarker(marker);
        }
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

            this.displaySnackbar(this.components.fragmentLayout, stringResId, Snackbar.LENGTH_INDEFINITE, R.string.str_enable, v -> requestPermission(requestCode, permissions[0]));
        } else {
            this.checkEnvironmentConditions();
        }
    }

    @SuppressLint("MissingPermission")
    public void fabLocationClick(View view) {
        LocationManager locationManager = (LocationManager) this.getContext().getSystemService(Context.LOCATION_SERVICE);
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            this.displaySnackbar(this.components.fragmentLayout, R.string.str_location_provider_error, Snackbar.LENGTH_INDEFINITE, R.string.str_activate, v -> {
                Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(settingsIntent);
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

        this.bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    public void fabSearchClick(View view) {
        Bundle arguments = new Bundle();
        arguments.putInt(KEY_FRAGMENT_ACTION, ACTION_OPEN_SEARCH);
        arguments.putDouble(KEY_SEARCH_LAT, this.currentMap.getCameraPosition().target.getLatitude());
        arguments.putDouble(KEY_SEARCH_LON, this.currentMap.getCameraPosition().target.getLongitude());
        this.fragmentInteractionListener.onFragmentInteraction(this, arguments);
    }

    private void loadStationData(CameraPosition cameraPosition) {
        SettingsManager settingsManager = new SettingsManager(this.getContext());

        DataRequest dataRequest = new DataRequest();
        dataRequest.setAppId(settingsManager.getAppId());
        dataRequest.setApiKey(settingsManager.getApiKey());

        GregorianCalendar calendar = (GregorianCalendar) GregorianCalendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(GregorianCalendar.MONTH, 6);

        Request.Filter filter = new Request.Filter();
        filter.setDate(Request.Filter.Date.fromJavaDateRange(new Date(), calendar.getTime()));
        filter.setTime(new SimpleDateFormat("HH:mm:ss").format(new Date()));

        dataRequest.setListener(new DataRequest.Listener() {
            @Override
            public void onSuccess(Delivery delivery) {
                currentStopList = delivery.getStops();

                // create marker feature list
                List<Feature> markerList = new ArrayList<Feature>();
                for(int i = 0; i < currentStopList.size(); i++) {
                    Feature marker = Feature.fromGeometry(Point.fromLngLat(currentStopList.get(i).getPosition().getLongitude(), currentStopList.get(i).getPosition().getLatitude()));
                    marker.addNumberProperty("stop.index", i);
                    markerList.add(marker);
                }

                // display markers in source.markers data source
                GeoJsonSource markerSource = currentMap.getSourceAs("source.marker");
                if(markerSource != null) {
                    markerSource.setGeoJson(FeatureCollection.fromFeatures(markerList));
                }
            }

            @Override
            public void onError(Throwable throwable) {
                showNetworkErrorDialog(() -> loadStationData(cameraPosition));
            }
        }).loadStops(new Position().setLatitude(cameraPosition.target.getLatitude()).setLongitude(cameraPosition.target.getLongitude()), 35000, filter);
    }

    private void showNetworkErrorDialog(ErrorDialog.OnRetryClickListener retryListener) {
        this.showNetworkErrorDialog(R.string.str_default_network_error_title, R.string.str_default_network_error_text, retryListener);
    }

    private void showNetworkErrorDialog(@StringRes int titleStringRes, @StringRes int textStringRes, ErrorDialog.OnRetryClickListener retryListener) {
        ErrorDialog errorDialog = new ErrorDialog(this.getContext());
        errorDialog.setDialogImage(R.drawable.img_error_basic);
        errorDialog.setDialogTitle(titleStringRes);
        errorDialog.setDialogText(textStringRes);
        errorDialog.setOnRetryClickListener(retryListener);
        errorDialog.show();
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

    private void selectMarker(SymbolLayer marker) {
        ValueAnimator valueAnimator = new ValueAnimator();
        valueAnimator.setObjectValues(1.0f, 1.35f);
        valueAnimator.setDuration(250);
        valueAnimator.addUpdateListener(animation -> {
            marker.setProperties(PropertyFactory.iconSize((float) animation.getAnimatedValue()));
        });

        valueAnimator.start();
        this.markerSelected = true;
    }

    private void deselectMarker(SymbolLayer marker) {
        ValueAnimator valueAnimator = new ValueAnimator();
        valueAnimator.setObjectValues(1.35f, 1.0f);
        valueAnimator.setDuration(250);
        valueAnimator.addUpdateListener(animation -> {
            marker.setProperties(PropertyFactory.iconSize((float) animation.getAnimatedValue()));
        });

        valueAnimator.start();
        this.markerSelected = false;
    }

    private void moveMapToPosition(Location location, double zoomlevel) {
        this.moveMapToPosition(location, zoomlevel, true);
    }

    private void moveMapToPosition(Location location, double zoomLevel, boolean animate) {
        if(currentMap != null && location != null) {
            if(zoomLevel == 0) {
                zoomLevel = 13.0;
            }

            CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(location.getLatitude(), location.getLongitude())).zoom(zoomLevel).build();
            if(animate) {
                currentMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 2000);
            } else {
                currentMap.setCameraPosition(cameraPosition);
            }
        }
    }
}
