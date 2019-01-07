package de.mpfl.app.fragments;

import android.animation.Animator;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import de.mfpl.staticnet.lib.StaticRequest;
import de.mfpl.staticnet.lib.base.Delivery;
import de.mfpl.staticnet.lib.base.Request;
import de.mfpl.staticnet.lib.data.Alert;
import de.mfpl.staticnet.lib.data.StopTime;
import de.mfpl.staticnet.lib.data.Trip;
import de.mpfl.app.R;
import de.mpfl.app.adapters.AlertListAdapter;
import de.mpfl.app.adapters.StopTimesAdapter;
import de.mpfl.app.databinding.FragmentTripDetailsBinding;
import de.mpfl.app.dialogs.ErrorDialog;
import de.mpfl.app.listeners.OnFragmentInteractionListener;
import de.mpfl.app.utils.DateTimeFormat;

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
    private String currentTripDate;
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
        if (getArguments() != null) {
            this.currentTripId = getArguments().getString(KEY_TRIP_ID);
            this.currentTripDate = getArguments().getString(KEY_TRIP_DATE);
            this.currentTripTime = getArguments().getString(KEY_TRIP_TIME);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.components = DataBindingUtil.inflate(inflater, R.layout.fragment_trip_details, container, false);
        this.components.setFragment(this);

        // set action controller for trip details view
        //this.components.tripDetailsHolder.setActionController(new TripDetailsActionController(this.getContext(), this.components.tripDetailsHolder));
        this.components.layoutSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadTripDetails(currentTripId, currentTripDate, currentTripTime);
            }
        });

        // initiate loading of trip times the first time
        if(this.resultTrip != null) {
            this.setTripDetails(this.resultTrip.getTripShortName(), this.resultTrip.getTripHeadsign(), this.currentTripDate);
            this.setStopTimesAdapter(this.resultTrip.getStopTimes());

            if(this.resultTrip.getRealtime() != null && this.resultTrip.getRealtime().hasAlerts()) {
                this.setAlertAdapter(this.resultTrip.getRealtime().getAlerts());
            }
        } else {
            this.components.layoutSwipeRefresh.setRefreshing(true);
            this.loadTripDetails(this.currentTripId, this.currentTripDate, this.currentTripTime);
        }

        // set action bar title from itself
        AppCompatActivity activity = (AppCompatActivity) this.getActivity();
        if(activity != null) {
            activity.getSupportActionBar().setTitle(R.string.str_trip_details_title);
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

        // todo: add complete action here
        if(this.fragmentInteractionListener != null) {
            Bundle arguments = new Bundle();
            arguments.putInt(KEY_FRAGMENT_ACTION, ACTION_VIEW_FAREINFO);

            this.fragmentInteractionListener.onFragmentInteraction(this, arguments);
        }
    }

    public void fabMapViewClick(View view) {
        this.hideFabMenu();

        // todo: add complete action here
        if(this.fragmentInteractionListener != null) {
            Bundle arguments = new Bundle();
            arguments.putInt(KEY_FRAGMENT_ACTION, ACTION_VIEW_MAP);

            this.fragmentInteractionListener.onFragmentInteraction(this, arguments);
        }
    }

    public void fabAddFavoriteClick(View view) {
        this.hideFabMenu();

        // todo: add complete action here
        Snackbar.make(this.components.getRoot(), R.string.str_favorite_added, Snackbar.LENGTH_LONG).setAction(R.string.str_view, v -> {
            if(this.fragmentInteractionListener != null) {
                Bundle arguments = new Bundle();
                arguments.putInt(KEY_FRAGMENT_ACTION, ACTION_VIEW_FAVORITES);

                this.fragmentInteractionListener.onFragmentInteraction(this, arguments);
            }
        }).show();
    }

    private void setTripDetails(String tripShortName, String tripHeadsign, String tripDate) {
        String tripNameString = tripHeadsign;
        if (tripShortName != null && !tripShortName.equals("")) {
            tripNameString = tripShortName + " " + tripNameString;
        }
        this.components.lblTripName.setText(tripNameString);

        // trip date of selected trip
        String tripDateString = getContext().getString(R.string.str_trip_date, DateTimeFormat.from(tripDate, DateTimeFormat.YYYYMMDD).to(DateTimeFormat.DDMMYYYY));
        this.components.lblTripDate.setText(tripDateString);
    }

    private void setStopTimesAdapter(List<StopTime> stopTimesList) {
        StopTimesAdapter stopTimesAdapter = new StopTimesAdapter(getContext(), stopTimesList);
        stopTimesAdapter.setLineActiveColor(ContextCompat.getColor(getContext(), R.color.colorAccentDAY));
        stopTimesAdapter.setPointActiveColor(ContextCompat.getColor(getContext(), R.color.colorAccentDAY));
        stopTimesAdapter.setPointInactiveColor(ContextCompat.getColor(getContext(), R.color.colorAccentDAY));

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
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

            if(this.resultTrip.getShape() != null) {
                this.components.fabMapView.show();
                this.components.fabMapView.animate().translationY(-this.getResources().getDimension(R.dimen.fab_menu_level_2)).rotation(0f);
            }

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

    public void loadTripDetails(String tripId, String tripDate, String tripTime) {
        StaticRequest staticRequest = new StaticRequest();
        staticRequest.setAppId(this.getContext().getString(R.string.MFPL_APP_ID));
        staticRequest.setApiKey(this.getContext().getString(R.string.MFPL_API_KEY));

        Request.Filter filter = new Request.Filter();
        filter.setDate(new Request.Filter.Date().setSingle(tripDate));
        filter.setTime(tripTime);

        staticRequest.setListener(new StaticRequest.Listener() {
            @Override
            public void onSuccess(Delivery delivery) {
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
                setTripDetails(resultTrip.getTripShortName(), resultTrip.getTripHeadsign(), tripDate);

                // create trip timeline here...
                setStopTimesAdapter(resultTrip.getStopTimes());

                // display trip assigned alerts
                if (resultTrip.getRealtime().hasAlerts()) {
                    setAlertAdapter(resultTrip.getRealtime().getAlerts());
                }
            }

            @Override
            public void onError(Throwable throwable) {
                // stop displaying refresh progress bar and display trip error view
                components.layoutSwipeRefresh.setRefreshing(false);

                showNetworkErrorDialog(() -> loadTripDetails(currentTripId, currentTripDate, currentTripTime));
            }
        }).loadTripDetails(tripId, filter, true);
    }
}
