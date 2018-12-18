package de.mpfl.app.fragments;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.mpfl.app.R;
import de.mpfl.app.controllers.TripDetailsActionController;
import de.mpfl.app.databinding.FragmentTripDetailsBinding;
import de.mpfl.app.listeners.OnFragmentInteractionListener;

public class TripDetailsFragment extends Fragment {

    public final static String KEY_TRIP_ID = "KEY_TRIP_ID";
    public final static String KEY_TRIP_DATE = "KEY_TRIP_DATE";
    public final static String KEY_TRIP_TIME = "KEY_TRIP_TIME";

    private FragmentTripDetailsBinding components;
    private OnFragmentInteractionListener fragmentInteractionListener;

    private String currentTripId;
    private String currentTripDate;
    private String currentTripTime;

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
        this.components.tripDetailsHolder.setActionController(new TripDetailsActionController(this.getContext(), this.components.tripDetailsHolder));
        this.components.tripDetailsHolder.layoutSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                components.tripDetailsHolder.getActionController().loadTripDetails(currentTripId, currentTripDate, currentTripTime);
            }
        });

        // initiate loading of trip times the first time
        this.components.tripDetailsHolder.layoutSwipeRefresh.setRefreshing(true);
        this.components.tripDetailsHolder.getActionController().loadTripDetails(currentTripId, currentTripDate, currentTripTime);

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

}
