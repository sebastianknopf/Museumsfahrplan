package de.mfpl.app.fragments;

import android.animation.Animator;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.mfpl.api.lib.DataRequest;
import de.mfpl.api.lib.base.Delivery;
import de.mfpl.api.lib.base.Request;
import de.mfpl.api.lib.data.Alert;
import de.mfpl.api.lib.data.Position;
import de.mfpl.api.lib.data.Shape;
import de.mfpl.api.lib.data.StopTime;
import de.mfpl.api.lib.data.Trip;
import de.mfpl.api.lib.data.TripUpdate;
import de.mfpl.app.R;
import de.mfpl.app.adapters.AlertListAdapter;
import de.mfpl.app.adapters.StopTimesAdapter;
import de.mfpl.app.common.DateTimeFormat;
import de.mfpl.app.common.SettingsManager;
import de.mfpl.app.common.VectorIconFactory;
import de.mfpl.app.database.ApplicationDatabase;
import de.mfpl.app.database.Favorite;
import de.mfpl.app.databinding.FragmentTripDetailsBinding;
import de.mfpl.app.dialogs.ErrorDialog;
import de.mfpl.app.listeners.OnFragmentInteractionListener;

public class TripDetailsFragment extends Fragment {

    public final static String TAG = "TripDetailsFragment";

    public final static String KEY_FRAGMENT_ACTION = "KEY_FRAGMENT_ACTION";
    public final static String KEY_TRIP_ID = "KEY_TRIP_ID";
    public final static String KEY_TRIP_DATE = "KEY_TRIP_DATE";
    public final static String KEY_TRIP_TIME = "KEY_TRIP_TIME";

    public final static int ACTION_VIEW_FAREINFO = 0;
    public final static int ACTION_VIEW_MAP = 1;
    public final static int ACTION_VIEW_FAVORITES = 2;

    private FragmentTripDetailsBinding components;
    private OnFragmentInteractionListener fragmentInteractionListener;

    private String currentTripId;
    private Date currentTripDate;
    private String currentTripTime;

    private boolean isFabMenuOpen = false;

    private Trip resultTrip = null;

    public TripDetailsFragment() {
        // Required empty public constructor
    }

    public static TripDetailsFragment newInstance(String tripId, String tripDate, String tripTime) {
        TripDetailsFragment fragment = new TripDetailsFragment();

        Bundle args = new Bundle();
        args.putString(KEY_TRIP_ID, tripId);
        args.putString(KEY_TRIP_DATE, tripDate);
        args.putString(KEY_TRIP_TIME, tripTime);

        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (this.getArguments() != null) {
            this.currentTripId = this.getArguments().getString(KEY_TRIP_ID);
            this.currentTripDate = DateTimeFormat.from(this.getArguments().getString(KEY_TRIP_DATE), DateTimeFormat.YYYYMMDD).toDate();
            this.currentTripTime = this.getArguments().getString(KEY_TRIP_TIME);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.components = DataBindingUtil.inflate(inflater, R.layout.fragment_trip_details, container, false);
        this.components.setFragment(this);

        // init map view
        this.components.mapView.onCreate(savedInstanceState);

        // determine if we've double pane or single pane layout
        // "hack around" - there seems to be no default value for databinding variables
        DisplayMetrics dm = this.getContext().getResources().getDisplayMetrics();
        int dpWidth = (int) (dm.widthPixels / dm.density);
        if(dpWidth > 600) {
            this.components.setDoublePane(true);
            this.components.setMapVisible(true);
        }

        // set action controller for trip details view
        this.components.layoutSwipeRefresh.setOnRefreshListener(() -> loadTripDetails(currentTripId, currentTripDate));

        // initiate loading of trip times the first time
        if(this.resultTrip != null) {
            this.setTripDetails(this.resultTrip, this.resultTrip.getRoute().getRouteColor());
            this.setTripDetailsAdapter(this.resultTrip, this.resultTrip.getRoute().getRouteColor());

            if(this.resultTrip.getRealtime() != null && this.resultTrip.getRealtime().hasAlerts()) {
                this.setAlertAdapter(this.resultTrip.getRealtime().getAlerts());
            }
        } else {
            this.loadTripDetails(this.currentTripId, this.currentTripDate);
        }

        // set action bar title from itself
        AppCompatActivity activity = (AppCompatActivity) this.getActivity();
        if(activity != null) {
            activity.getSupportActionBar().setTitle(R.string.str_trip_details_title);
            activity.getSupportActionBar().setSubtitle(this.getString(R.string.str_trip_details_date, DateTimeFormat.from(this.currentTripDate).to(DateTimeFormat.DDMMYYYY)));
        }

        return this.components.getRoot();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            this.fragmentInteractionListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
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
    }

    @Override
    public void onResume() {
        super.onResume();
        this.components.mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
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

        // restore default subtitle
        AppCompatActivity activity = (AppCompatActivity) this.getActivity();
        if(activity != null) {
            activity.getSupportActionBar().setSubtitle(null);
        }
    }

    public void viewFabBackgroundClick(View view) {
        this.hideFabMenu();
    }

    public void fabMenuClick(View view) {
        if(!this.isFabMenuOpen) {
            this.showFabMenu();
        } else {
            this.hideFabMenu();
        }
    }

    public void fabFareInfoClick(View view) {
        this.hideFabMenu();

        if(this.fragmentInteractionListener != null) {
            Bundle arguments = new Bundle();
            arguments.putInt(KEY_FRAGMENT_ACTION, ACTION_VIEW_FAREINFO);

            this.fragmentInteractionListener.onFragmentInteraction(this, arguments);
        }
    }

    public void fabMapViewClick(View view) {
        this.hideFabMenu();

        if(!this.components.getDoublePane()) {
            if(!this.components.getMapVisible()) {
                this.showTripDetailsMap();

                Drawable icList = this.getResources().getDrawable(R.drawable.ic_menu_list, null);
                this.components.fabMapView.setImageDrawable(icList);
            } else {
                this.showTripDetailsList();

                Drawable icMap = this.getResources().getDrawable(R.drawable.ic_menu_map, null);
                this.components.fabMapView.setImageDrawable(icMap);
            }
        }
    }

    public void fabAddFavoriteClick(View view) {
        this.hideFabMenu();

        // add favorite to internal database
        ApplicationDatabase applicationDatabase = ApplicationDatabase.getInstance(this.getContext());

        Favorite favorite = new Favorite();
        favorite.setTripId(this.resultTrip.getTripId());
        favorite.setTripType(this.resultTrip.getRoute().getRouteType().toString());
        favorite.setTripName((!this.resultTrip.getTripShortName().equals("") ? this.resultTrip.getTripShortName() + " " : "") + this.resultTrip.getTripHeadsign());
        favorite.setTripDesc(this.resultTrip.getRoute().getRouteLongName());
        favorite.setTripDate(DateTimeFormat.from(this.currentTripDate).to(DateTimeFormat.YYYYMMDD));
        favorite.setTripTime(this.resultTrip.getStopTimes().get(0).getDepartureTime());

        applicationDatabase.addFavorite(favorite);

        // inform user
        Snackbar.make(this.components.getRoot(), R.string.str_trip_details_favorite_added, Snackbar.LENGTH_LONG).setAction(R.string.str_view, v -> {
            if(this.fragmentInteractionListener != null) {
                Bundle arguments = new Bundle();
                arguments.putInt(KEY_FRAGMENT_ACTION, ACTION_VIEW_FAVORITES);

                this.fragmentInteractionListener.onFragmentInteraction(this, arguments);
            }
        }).show();
    }

    private void setTripDetails(Trip trip, String colorString) {
        // parse route color
        int routeColor = ContextCompat.getColor(this.getContext(), R.color.colorAccentDAY);
        try {
            if(!colorString.startsWith("#")) {
                routeColor = Color.parseColor("#" + colorString);
            } else {
                routeColor = Color.parseColor(colorString);
            }
        } catch(Exception ignored) {
        }

        String tripNameString = trip.getTripHeadsign();
        if (trip.getTripShortName() != null && !trip.getTripShortName().equals("")) {
            tripNameString = trip.getTripShortName() + " " + tripNameString;
        }
        this.components.lblTripName.setText(tripNameString);
        this.components.lblTripName.setTextColor(routeColor);

        // trip information text
        if(trip.hasTripUpdate() && trip.getTripUpdate().getScheduleRelationship() == TripUpdate.ScheduleRelationship.CANCELED) {
            this.components.layoutTripInformation.setVisibility(View.VISIBLE);
            this.components.lblTripInformation.setText(R.string.str_trip_cancelled);
        } else if(trip.hasTripUpdate() && trip.getTripUpdate().getScheduleRelationship() == TripUpdate.ScheduleRelationship.ADDED) {
            this.components.layoutTripInformation.setVisibility(View.VISIBLE);
            this.components.lblTripInformation.setText(R.string.str_trip_added);
        } else {
            int currentDateInt = Integer.parseInt(DateTimeFormat.from(new Date()).to(DateTimeFormat.YYYYMMDD));
            if(currentDateInt >= Integer.parseInt(DateTimeFormat.from(this.currentTripDate).to(DateTimeFormat.YYYYMMDD))) {
                Date lastArrivalTime = DateTimeFormat.from(trip.getStopTimes().get(trip.getStopTimes().size() - 1).getArrivalTime(), DateTimeFormat.HHMMSS).toDate();
                if(lastArrivalTime.before(new Date())) {
                    this.components.lblTripInformation.setVisibility(View.VISIBLE);
                    this.components.lblTripInformation.setText(R.string.str_trip_already_departed);
                } else {
                    this.components.layoutTripInformation.setVisibility(View.GONE);
                }
            }
        }

        // wheelchair and bike information
        this.components.layoutTripAccessibility.setVisibility(View.VISIBLE);
        String wheelchairString = this.getString(R.string.str_trip_details_na);
        String bikeString = this.getString(R.string.str_trip_details_na);

        if(trip.getWheelchairAccessible() == Trip.WheelchairAccessible.NO) {
            wheelchairString = this.getString(R.string.str_trip_details_wheelchair_no);
            this.components.lblWheelchairAccess.setTextColor(Color.RED);
        } else if(trip.getWheelchairAccessible() == Trip.WheelchairAccessible.YES) {
            wheelchairString = this.getString(R.string.str_trip_details_wheelchair_yes);
        }

        if(trip.getBikesAllowed() == Trip.BikesAllowed.NO) {
            bikeString = this.getString(R.string.str_trip_details_bikes_no);
            this.components.lblBikesAllowed.setTextColor(Color.RED);
        } else if(trip.getBikesAllowed() == Trip.BikesAllowed.YES) {
            bikeString = this.getString(R.string.str_trip_details_bikes_yes);
        }

        this.components.lblWheelchairAccess.setText(wheelchairString);
        this.components.lblBikesAllowed.setText(bikeString);
    }

    private void setTripMapView(List<StopTime> stopTimeList, Shape tripShape, final String tripShapeColor) {
        this.components.mapView.getMapAsync(map -> {
            // clear map at first
            map.clear();

            // lat lng bound builder to zoom the map to desied views
            LatLngBounds.Builder latLngBuilder = new LatLngBounds.Builder();

            // calculare route color of current trip
            int routeColor = Color.RED;
            try {
                if(!tripShapeColor.startsWith("#")) {
                    routeColor = Color.parseColor("#" + tripShapeColor);
                } else {
                    routeColor = Color.parseColor(tripShapeColor);
                }
            } catch(Exception ignored) {
            }

            // get stop
            Bitmap stopBitmap = VectorIconFactory.fromVectorDrawable(this.getContext(), R.drawable.ic_stop);

            // hacky way to change the bitmap color to the route color
            int [] stopBitmapPixels = new int [stopBitmap.getHeight() * stopBitmap.getWidth()];
            stopBitmap.getPixels(stopBitmapPixels, 0, stopBitmap.getWidth(), 0, 0, stopBitmap.getWidth(), stopBitmap.getHeight());
            for(int i = 0; i < stopBitmapPixels.length; i++) {
                if(stopBitmapPixels[i] == Color.BLACK)
                {
                    stopBitmapPixels[i] = routeColor;
                }
            }
            stopBitmap.setPixels(stopBitmapPixels,0,stopBitmap.getWidth(),0, 0, stopBitmap.getWidth(),stopBitmap.getHeight());

            // set bitmap as stop icon
            Icon stopIcon = IconFactory.getInstance(this.getContext()).fromBitmap(stopBitmap);

            // display all stops on the map
            for(StopTime stopTime : stopTimeList) {
                if(stopTime.getStop() != null) {
                    LatLng position = new LatLng(stopTime.getStop().getPosition().getLatitude(), stopTime.getStop().getPosition().getLongitude());

                    latLngBuilder.include(position);

                    map.addMarker(new MarkerOptions()
                            .position(position)
                            .setTitle(stopTime.getStop().getStopName())
                            .setIcon(stopIcon)
                    );
                }
            }

            // display shape polyline on map
            if(tripShape != null && tripShape.getPoints().size() > 0) {
                List<LatLng> pointList = new ArrayList<LatLng>();
                for(Position pos : tripShape.getPoints()) {
                    LatLng position = new LatLng(pos.getLatitude(), pos.getLongitude());
                    latLngBuilder.include(position);
                    pointList.add(position);
                }

                map.addPolyline(new PolylineOptions()
                        .addAll(pointList)
                        .color(routeColor)
                        .width(this.getResources().getDimension(R.dimen.map_polyline_width))
                );
            }

            map.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBuilder.build(), (int) this.getResources().getDimension(R.dimen.map_boundary_padding)));
        });
    }

    private void setTripDetailsAdapter(Trip trip, String colorString) {
        StopTimesAdapter stopTimesAdapter = new StopTimesAdapter(getContext(), trip);

        // parse route color
        int tripColor = ContextCompat.getColor(this.getContext(), R.color.colorAccentDAY);
        try {
            tripColor = Color.parseColor((!colorString.startsWith("#") ? "#" : "") + colorString);
        } catch(Exception ignored) {
        }

        // display trip progress with colored line only when the trip is departing today
        String currentDateString = DateTimeFormat.from(this.currentTripDate).to(DateTimeFormat.YYYYMMDD);
        if(currentDateString.equals(DateTimeFormat.from(new Date()).to(DateTimeFormat.YYYYMMDD))) {
            stopTimesAdapter.setLineActiveColor(tripColor);
        } else {
            stopTimesAdapter.setLineActiveColor(ContextCompat.getColor(this.getContext(), R.color.colorBackgroundDarkGray));
        }

        stopTimesAdapter.setPointActiveColor(tripColor);
        stopTimesAdapter.setPointInactiveColor(tripColor);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this.getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        this.components.rcvTripDetails.setLayoutManager(layoutManager);
        this.components.rcvTripDetails.setHasFixedSize(true);
        this.components.rcvTripDetails.setAdapter(stopTimesAdapter);
    }

    private void setAlertAdapter(List<Alert> alertList) {
        AlertListAdapter alertListAdapter = new AlertListAdapter(getContext(), alertList);
        this.components.lstTripAlerts.setAdapter(alertListAdapter);
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

    public void showTripDetailsList() {
        if(!this.components.getDoublePane()) {
            this.components.setMapVisible(false);
            this.components.layoutTripDetailsList.setVisibility(View.VISIBLE);
            this.components.layoutTripDetailsMap.setVisibility(View.GONE);
        }
    }

    private void showTripDetailsMap() {
        if(!this.components.getDoublePane()) {
            this.components.setMapVisible(true);
            this.components.layoutTripDetailsList.setVisibility(View.GONE);
            this.components.layoutTripDetailsMap.setVisibility(View.VISIBLE);

            this.setTripMapView(this.resultTrip.getStopTimes(), this.resultTrip.getShape(), this.resultTrip.getRoute().getRouteColor());
        }
    }

    private void showFabMenu() {
        this.isFabMenuOpen = true;

        if(this.resultTrip != null) {
            this.components.viewFabBackground.setVisibility(View.VISIBLE);
            this.components.viewFabBackground.animate().alpha(1f);

            // todo: enable fare information here if available
            /*if(resultTrip.getFareInfo() != null) {
                this.components.fabFareInfo.show();
                this.components.fabFareInfo.animate().translationY(-this.getResources().getDimension(R.dimen.fab_menu_level_3)).rotation(0f);
            }*/

            this.components.fabMapView.show();
            this.components.fabMapView.animate().translationY(-this.getResources().getDimension(R.dimen.fab_menu_level_2)).rotation(0f);

            this.components.fabAddFavorite.show();
            this.components.fabAddFavorite.animate().translationY(-this.getResources().getDimension(R.dimen.fab_menu_level_1)).rotation(0f);
        }
    }

    private void hideFabMenu() {
        this.isFabMenuOpen = false;

        this.components.viewFabBackground.animate().alpha(0f);

        this.components.fabAddFavorite.animate().translationY(0f).rotation(90f);
        this.components.fabMapView.animate().translationY(0f).rotation(90f);
        this.components.fabFareInfo.animate().translationY(0f).rotation(90f).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                components.viewFabBackground.setVisibility(View.GONE);
                components.fabFareInfo.setVisibility(View.GONE);
                components.fabMapView.setVisibility(View.GONE);
                components.fabAddFavorite.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
    }

    private void loadTripDetails(String tripId, Date tripDate) {
        SettingsManager settingsManager = new SettingsManager(this.getContext());

        DataRequest dataRequest = new DataRequest();
        dataRequest.setAppId(settingsManager.getAppId());
        dataRequest.setApiKey(settingsManager.getApiKey());

        Request.Filter filter = new Request.Filter();
        filter.setDate(new Request.Filter.Date().fromJavaDate(tripDate));
        filter.setTime(this.currentTripTime);
        
        this.components.layoutSwipeRefresh.setRefreshing(true);
        dataRequest.setListener(new DataRequest.Listener() {
            @Override
            public void onSuccess(Delivery delivery, double duration) {
                // stop displaying refresh progress bar
                components.layoutSwipeRefresh.setRefreshing(false);

                // check for api errors
                if (delivery.getError() != null) {
                    showNetworkErrorDialog(null);
                    return;
                }

                List<Trip> tripList = delivery.getTrips();
                if (tripList.size() != 1) {
                    showNetworkErrorDialog(null);
                    return;
                }

                resultTrip = tripList.get(0);

                // display trip details view
                components.layoutTripDetailsView.setVisibility(View.VISIBLE);
                components.layoutTripDetailsError.setVisibility(View.GONE);

                // display basic trip information
                setTripDetails(resultTrip, resultTrip.getRoute().getRouteColor());

                // create trip timeline here...
                setTripDetailsAdapter(resultTrip, resultTrip.getRoute().getRouteColor());

                // display trip assigned alerts
                if (resultTrip.getRealtime().hasAlerts()) {
                    setAlertAdapter(resultTrip.getRealtime().getAlerts());
                }

                // display trip map view if we're in double pane mode
                if(components.getDoublePane()) {
                    setTripMapView(resultTrip.getStopTimes(), resultTrip.getShape(), resultTrip.getRoute().getRouteColor());
                }
            }

            @Override
            public void onError(Throwable throwable, double duration) {
                // stop displaying refresh progress bar and display trip error view
                components.layoutSwipeRefresh.setRefreshing(false);

                showNetworkErrorDialog(() -> loadTripDetails(currentTripId, currentTripDate));
            }
        }).loadTripDetails(tripId, filter, true);
    }
}