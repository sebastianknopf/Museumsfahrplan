package de.mfpl.app.fragments;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Date;
import java.util.List;

import de.mfpl.api.lib.DataRequest;
import de.mfpl.api.lib.base.Delivery;
import de.mfpl.api.lib.base.Request;
import de.mfpl.api.lib.data.Trip;
import de.mfpl.app.R;
import de.mfpl.app.adapters.SkeletonAdapter;
import de.mfpl.app.adapters.TripListAdapter;
import de.mfpl.app.common.DateTimeFormat;
import de.mfpl.app.common.SettingsManager;
import de.mfpl.app.databinding.FragmentSearchDetailsBinding;
import de.mfpl.app.dialogs.ErrorDialog;
import de.mfpl.app.listeners.OnFragmentInteractionListener;
import de.mfpl.app.listeners.OnTripItemClickListener;

public class SearchDetailsFragment extends Fragment implements OnTripItemClickListener {

    public final static String TAG = "SearchDetailsFragment";

    public final static String KEY_FRAGMENT_ACTION = "KEY_FRAGMENT_ACTION";
    public final static String KEY_SEARCH_ROUTE_ID = "KEY_SEARCH_ROUTE_ID";
    public final static String KEY_SEARCH_ROUTE_NAME = "KEY_SEARCH_ROUTE_NAME";
    public final static String KEY_SEARCH_DATE = "KEY_SEARCH_DATE";
    public final static String KEY_SEARCH_TIME = "KEY_SEARCH_TIME";
    public final static String KEY_TRIP_ID = "KEY_TRIP_ID";
    public final static String KEY_TRIP_TIME = "KEY_TRIP_TIME";
    public final static String KEY_TRIP_DATE = "KEY_TRIP_DATE";

    public final static int ACTION_SELECT_TRIP = 0;

    private FragmentSearchDetailsBinding components;
    private RecyclerView.ItemDecoration itemDecoration;
    private OnFragmentInteractionListener fragmentInteractionListener;

    private String currentSearchRouteId;
    private String currentSearchRouteName;
    private Date currentSearchDate = new Date();
    private String currentSearchTime;
    private int overloadCount = 0;

    // needed for retain behaviour when returning from back stack to this fragment
    private List<Trip> resultList = null;

    public SearchDetailsFragment() {
        // Required empty public constructor
    }

    public static SearchDetailsFragment newInstance(String routeId, String routeName, String searchDate, String searchTime) {
        SearchDetailsFragment fragment = new SearchDetailsFragment();

        Bundle arguments = new Bundle();
        arguments.putString(KEY_SEARCH_ROUTE_ID, routeId);
        arguments.putString(KEY_SEARCH_ROUTE_NAME, routeName);
        arguments.putString(KEY_SEARCH_DATE, searchDate);
        arguments.putString(KEY_SEARCH_TIME, searchTime);
        fragment.setArguments(arguments);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(this.getArguments() != null) {
            this.currentSearchRouteId = this.getArguments().getString(KEY_SEARCH_ROUTE_ID);
            this.currentSearchRouteName = this.getArguments().getString(KEY_SEARCH_ROUTE_NAME);
            this.currentSearchDate = DateTimeFormat.from(this.getArguments().getString(KEY_SEARCH_DATE), DateTimeFormat.DDMMYYYY).toDate();
            this.currentSearchTime = this.getArguments().getString(KEY_SEARCH_TIME);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.components = DataBindingUtil.inflate(inflater, R.layout.fragment_search_details, container, false);
        this.components.setFragment(this);

        // set activity title
        AppCompatActivity activity = (AppCompatActivity) this.getActivity();
        if(activity != null) {
            activity.getSupportActionBar().setTitle(this.currentSearchRouteName);
            activity.getSupportActionBar().setSubtitle(this.getString(R.string.str_trip_details_date, DateTimeFormat.from(this.currentSearchDate).to(DateTimeFormat.DDMMYYYY)));
        }

        // item decor
        this.itemDecoration = new DividerItemDecoration(this.getContext(), DividerItemDecoration.VERTICAL);

        // load route related trips at startup if there's a route id
        if(this.resultList != null && this.resultList.size() > 0) {
            this.setListAdapter(this.resultList);
        } else if(this.currentSearchRouteId != null) {
            this.loadRouteTrips();
        }

        // layout manager for recycler view
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        this.components.rcvSearchDetailsResults.setLayoutManager(layoutManager);

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
    public void onDestroyView() {
        super.onDestroyView();

        // restore default subtitle
        AppCompatActivity activity = (AppCompatActivity) this.getActivity();
        if(activity != null) {
            activity.getSupportActionBar().setSubtitle(null);
        }
    }

    @Override
    public void onTripItemClick(Trip tripItem) {
        if(this.fragmentInteractionListener != null) {
            Bundle arguments = new Bundle();
            arguments.putInt(KEY_FRAGMENT_ACTION, ACTION_SELECT_TRIP);
            arguments.putString(KEY_TRIP_ID, tripItem.getTripId());
            arguments.putString(KEY_TRIP_DATE, DateTimeFormat.from(this.currentSearchDate).to(DateTimeFormat.YYYYMMDD));

            if(tripItem.getFrequency() != null) {
                arguments.putString(KEY_TRIP_TIME, tripItem.getFrequency().getTripTime());
            }

            this.fragmentInteractionListener.onFragmentInteraction(this, arguments);
        }
    }

    public void setListAdapter(List<Trip> resultList) {
        TripListAdapter listAdapter = new TripListAdapter(getContext(), resultList);
        listAdapter.setOnTripItemClickListener(SearchDetailsFragment.this);

        // divider setup
        this.components.rcvSearchDetailsResults.addItemDecoration(this.itemDecoration);

        this.components.rcvSearchDetailsResults.setAdapter(listAdapter);
    }

    private void setSkeletonAdapter() {
        SkeletonAdapter skeletonAdapter = new SkeletonAdapter(this.getContext(), 5);
        skeletonAdapter.setViewType(SkeletonAdapter.TYPE_DATA_ICON);
        skeletonAdapter.setFirstItemDifferent(true);

        // divider setup
        this.components.rcvSearchDetailsResults.removeItemDecoration(this.itemDecoration);

        this.components.rcvSearchDetailsResults.setAdapter(skeletonAdapter);
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

    private void loadRouteTrips() {
        this.setSkeletonAdapter();

        SettingsManager settingsManager = new SettingsManager(this.getContext());

        DataRequest dataRequest = new DataRequest();
        dataRequest.setAppId(settingsManager.getAppId());
        dataRequest.setApiKey(settingsManager.getApiKey());
        dataRequest.setDefaultLimit(settingsManager.getPreferencesNumResults());

        Request.Filter filter = new Request.Filter();
        filter.setDate(Request.Filter.Date.fromJavaDate(this.currentSearchDate));
        filter.setTime(this.currentSearchTime);
        filter.setWheelchairAccessible(settingsManager.getPreferenceWheelchairAccessible() ? Trip.WheelchairAccessible.YES : Trip.WheelchairAccessible.NO);
        filter.setBikesAllowed(settingsManager.getPreferenceBikesAllowed() ? Trip.BikesAllowed.YES : Trip.BikesAllowed.NO);

        dataRequest.setListener(new DataRequest.Listener() {
            @Override
            public void onSuccess(Delivery delivery, double duration) {
                // check for api errors
                if(delivery.getError() != null) {
                    showNetworkErrorDialog(null);
                    return;
                }

                // no need for check result count here (only for error purposes)
                // api returns this route id only if there's at least one scheduled trip
                resultList = delivery.getTrips();
                if(resultList.size() == 0) {
                    if(overloadCount == 0) {
                        currentSearchTime = "00:00:01";
                        overloadCount++;
                        loadRouteTrips();
                    } else {
                        components.rcvSearchDetailsResults.setVisibility(View.GONE);
                        components.layoutSearchDetailsEmpty.setVisibility(View.VISIBLE);
                    }

                    return;
                } else {
                    components.rcvSearchDetailsResults.setVisibility(View.VISIBLE);
                    components.layoutSearchDetailsEmpty.setVisibility(View.GONE);
                }

                // set list adapter
                setListAdapter(resultList);
            }

            @Override
            public void onError(Throwable throwable, double duration) {
                showNetworkErrorDialog(() -> loadRouteTrips());
            }
        }).loadTrips(this.currentSearchRouteId, filter);
    }
}
