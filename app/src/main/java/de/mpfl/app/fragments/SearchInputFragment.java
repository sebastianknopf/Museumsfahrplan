package de.mpfl.app.fragments;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.mfpl.staticnet.lib.StaticRequest;
import de.mfpl.staticnet.lib.base.Delivery;
import de.mfpl.staticnet.lib.base.Request;
import de.mfpl.staticnet.lib.data.Position;
import de.mfpl.staticnet.lib.data.Route;
import de.mfpl.staticnet.lib.data.Trip;
import de.mpfl.app.R;
import de.mpfl.app.adapters.NominatimResultListAdapter;
import de.mpfl.app.adapters.RouteListAdapter;
import de.mpfl.app.adapters.SkeletonAdapter;
import de.mpfl.app.databinding.FragmentSearchInputBinding;
import de.mpfl.app.dialogs.DateTimeDialog;
import de.mpfl.app.dialogs.ErrorDialog;
import de.mpfl.app.listeners.OnFragmentInteractionListener;
import de.mpfl.app.listeners.OnNominatimResultClickListener;
import de.mpfl.app.listeners.OnRouteItemClickListener;
import de.mpfl.app.network.NominatimRequest;
import de.mpfl.app.network.NominatimResult;
import de.mpfl.app.utils.DateTimeFormat;
import de.mpfl.app.utils.SettingsManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchInputFragment extends Fragment implements OnRouteItemClickListener, OnNominatimResultClickListener {

    public final static String TAG ="SearchInputFragment";

    public final static String KEY_FRAGMENT_ACTION = "KEY_FRAGMENT_ACTION";
    public final static String KEY_SEARCH_LAT = "KEY_SEARCH_LAT";
    public final static String KEY_SEARCH_LON = "KEY_SEARCH_LON";
    public final static String KEY_SEARCH_ROUTE_ID = "KEY_SEARCH_ROUTE_ID";
    public final static String KEY_SEARCH_ROUTE_NAME = "KEY_SEARCH_ROUTE_NAME";
    public final static String KEY_SEARCH_DATE = "KEY_SEARCH_DATE";
    public final static String KEY_SEARCH_TIME = "KEY_SEARCH_TIME";

    public final static int ACTION_SELECT_ROUTE = 0;
    public final static int ACTION_SHOW_SETTINGS = 1;

    private FragmentSearchInputBinding components;
    private RecyclerView.ItemDecoration itemDecoration;
    private OnFragmentInteractionListener fragmentInteractionListener;

    private double currentSearchLatitude;
    private double currentSearchLongitude;
    private int currentSearchRadius = 25000;            // 25km
    private Date currentSearchDate = new Date();        // let's look for results of today

    // needed for retain behaviour when returning from back stack to this fragment
    private List<Route> resultList = null;

    private boolean textInputBlocked = false;

    public SearchInputFragment() {
        // Required empty public constructor
    }

    public static SearchInputFragment newInstance(double searchLatitude, double searchLongitude) {
        SearchInputFragment fragment = new SearchInputFragment();

        Bundle arguments = new Bundle();
        arguments.putDouble(KEY_SEARCH_LAT, searchLatitude);
        arguments.putDouble(KEY_SEARCH_LON, searchLongitude);
        fragment.setArguments(arguments);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setHasOptionsMenu(true);

        if(this.getArguments() != null) {
            this.currentSearchLatitude = this.getArguments().getDouble(KEY_SEARCH_LAT);
            this.currentSearchLongitude = this.getArguments().getDouble(KEY_SEARCH_LON);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.components = DataBindingUtil.inflate(inflater, R.layout.fragment_search_input, container, false);
        this.components.setFragment(this);

        // set activity title
        AppCompatActivity activity = (AppCompatActivity) this.getActivity();
        if(activity != null) {
            activity.getSupportActionBar().setTitle(R.string.str_search_input_title);
        }

        // divider setup
        this.itemDecoration = new DividerItemDecoration(this.getContext(), DividerItemDecoration.VERTICAL);

        // layout manager for recycler views
        LinearLayoutManager layoutRouteManager = new LinearLayoutManager(getContext());
        layoutRouteManager.setOrientation(LinearLayoutManager.VERTICAL);
        this.components.rcvSearchInputRouteResults.setLayoutManager(layoutRouteManager);

        LinearLayoutManager layoutLocationManager = new LinearLayoutManager(getContext());
        layoutLocationManager.setOrientation(LinearLayoutManager.VERTICAL);
        this.components.rcvSearchInputLocationResults.setLayoutManager(layoutLocationManager);

        // display current search parameters
        this.components.lblSearchParamDate.setText(DateTimeFormat.from(this.currentSearchDate).to(DateTimeFormat.DDMMYYYY_HHMM));
        this.components.skbSearchParamRadius.setProgress(this.currentSearchRadius / 1000);

        // add seek bar listener for reloading data when radius has changed
        this.components.skbSearchParamRadius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                currentSearchRadius = components.skbSearchParamRadius.getProgress() * 1000;
                loadRouteResults();
            }
        });

        // enable geocoding service for user location input
        this.components.edtUserLocationInput.addTextChangedListener(new TextWatcher() {
            final Handler handler = new Handler();
            Runnable operation = null;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                this.handler.removeCallbacks(operation);

                if(!textInputBlocked) {
                    // display progress
                    components.pgbUserLocation.setVisibility(View.VISIBLE);
                    showLocationResultList();
                    setLocationSkeletonAdapter();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!textInputBlocked && !s.toString().equals("")) {
                    this.operation = () -> {
                        loadGeocodingResults(s.toString());
                    };

                    this.handler.postDelayed(this.operation, 1250);
                }

            }
        });

        // load data depending on current location
        if(this.currentSearchLatitude != 0.0 && this.currentSearchLongitude != 0.0) {
            // request geo information on current location
            this.textInputBlocked = true;
            this.components.pgbUserLocation.setVisibility(View.VISIBLE);
            Call<NominatimResult> reverseCall = NominatimRequest.createReverseCall(this.currentSearchLatitude, this.currentSearchLongitude);
            reverseCall.enqueue(new Callback<NominatimResult>() {
                @Override
                public void onResponse(Call<NominatimResult> call, Response<NominatimResult> response) {
                    components.pgbUserLocation.setVisibility(View.GONE);

                    NominatimResult result = response.body();
                    if(response.isSuccessful() && !result.getAddress().toString().equals("")) {
                        components.edtUserLocationInput.setText(result.getAddress().toString());
                    } else {
                        components.edtUserLocationInput.setText(R.string.str_search_input_current_location);
                    }

                    textInputBlocked = false;
                }

                @Override
                public void onFailure(Call<NominatimResult> call, Throwable t) {
                    components.pgbUserLocation.setVisibility(View.GONE);
                    components.edtUserLocationInput.setText(R.string.str_search_input_current_location);

                    textInputBlocked = false;
                }
            });

            // load routes depending on current location and search radius
            if(this.resultList != null && this.resultList.size() > 0) {
                this.setRouteListAdapter(this.resultList);
                this.showRouteResultList();
            } else {
                this.loadRouteResults();
            }
        }

        return this.components.getRoot();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_settings_shortcut, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.optionsMenuSettings && this.fragmentInteractionListener != null) {
            Bundle arguments = new Bundle();
            arguments.putInt(KEY_FRAGMENT_ACTION, ACTION_SHOW_SETTINGS);

            this.fragmentInteractionListener.onFragmentInteraction(this, arguments);
        }

        return super.onOptionsItemSelected(item);
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
    public void onRouteItemClick(Route routeItem) {
        if(this.fragmentInteractionListener != null) {
            Bundle arguments = new Bundle();
            arguments.putInt(KEY_FRAGMENT_ACTION, ACTION_SELECT_ROUTE);
            arguments.putString(KEY_SEARCH_ROUTE_ID, routeItem.getRouteId());
            arguments.putString(KEY_SEARCH_ROUTE_NAME, routeItem.getRouteLongName());
            arguments.putString(KEY_SEARCH_DATE, DateTimeFormat.from(this.currentSearchDate).to(DateTimeFormat.DDMMYYYY));
            arguments.putString(KEY_SEARCH_TIME, DateTimeFormat.from(this.currentSearchDate).to(DateTimeFormat.HHMMSS));

            this.fragmentInteractionListener.onFragmentInteraction(this, arguments);
        }
    }

    @Override
    public void onNominatimResultClick(NominatimResult nominatimResult) {
        this.textInputBlocked = true;
        this.components.edtUserLocationInput.setText(nominatimResult.getName());
        this.textInputBlocked = false;

        this.currentSearchLatitude = nominatimResult.getLatitude();
        this.currentSearchLongitude = nominatimResult.getLongitude();
        this.loadRouteResults();
    }

    public void lblSearchParamDateClick(View view) {
        DateTimeDialog dateDialog = new DateTimeDialog(this.getContext());
        dateDialog.setSearchDateTime(this.currentSearchDate);
        dateDialog.setOnDateTimeChangedListener(date -> {
            this.currentSearchDate = date;
            this.components.lblSearchParamDate.setText(DateTimeFormat.from(date).to(DateTimeFormat.DDMMYYYY_HHMM));
            this.loadRouteResults();
        });
        dateDialog.show();
    }

    private void setLocationListAdapter(List<NominatimResult> resultList) {
        NominatimResultListAdapter nominatimResultListAdapter = new NominatimResultListAdapter(this.getContext(), resultList);
        nominatimResultListAdapter.setOnNominatimResultClickListener(this);

        // divider setup
        components.rcvSearchInputLocationResults.addItemDecoration(this.itemDecoration);

        components.rcvSearchInputLocationResults.setAdapter(nominatimResultListAdapter);
    }

    private void setLocationSkeletonAdapter() {
        SkeletonAdapter skeletonAdapter = new SkeletonAdapter(this.getContext(), 5);
        skeletonAdapter.setViewType(SkeletonAdapter.TYPE_DATA_ONLY);

        // divider setup
        this.components.rcvSearchInputLocationResults.removeItemDecoration(this.itemDecoration);

        this.components.rcvSearchInputLocationResults.setAdapter(skeletonAdapter);
    }

    private void setRouteListAdapter(List<Route> resultList) {
        RouteListAdapter routeListAdapter = new RouteListAdapter(this.getContext(), resultList);
        routeListAdapter.setOnRouteItemClickListener(this);

        // divider setup
        this.components.rcvSearchInputRouteResults.addItemDecoration(this.itemDecoration);

        this.components.rcvSearchInputRouteResults.setAdapter(routeListAdapter);
    }

    private void setRouteSkeletonAdapter() {
        SkeletonAdapter skeletonAdapter = new SkeletonAdapter(this.getContext(), 5);
        skeletonAdapter.setViewType(SkeletonAdapter.TYPE_DATA_ONLY);

        // divider setup
        this.components.rcvSearchInputRouteResults.removeItemDecoration(this.itemDecoration);

        this.components.rcvSearchInputRouteResults.setAdapter(skeletonAdapter);
    }

    private void showRouteResultList() {
        this.components.layoutSearchInputRouteResults.setVisibility(View.VISIBLE);
        this.components.layoutSearchInputLocationResults.setVisibility(View.GONE);
    }

    private void showLocationResultList() {
        this.components.layoutSearchInputLocationResults.setVisibility(View.VISIBLE);
        this.components.layoutSearchInputRouteResults.setVisibility(View.GONE);

        // display list instead of error screen
        this.components.rcvSearchInputLocationResults.setVisibility(View.VISIBLE);
        this.components.lblSearchInputLocationAttribution.setVisibility(View.VISIBLE);
        this.components.layoutSearchInputLocationEmpty.setVisibility(View.GONE);
    }

    private void showEmptyScreen() {
        this.components.layoutSearchInputLocationResults.setVisibility(View.GONE);
        this.components.layoutSearchInputRouteResults.setVisibility(View.GONE);
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

        this.showEmptyScreen();
    }

    private void loadGeocodingResults(String queryString) {
        this.components.pgbUserLocation.setVisibility(View.VISIBLE);

        this.showLocationResultList();
        Call<List<NominatimResult>> forwardCall = NominatimRequest.createForwardCall(queryString);
        forwardCall.enqueue(new Callback<List<NominatimResult>>() {
            @Override
            public void onResponse(Call<List<NominatimResult>> call, Response<List<NominatimResult>> response) {
                components.pgbUserLocation.setVisibility(View.GONE);

                if(response.isSuccessful()) {
                    List<NominatimResult> resultList = new ArrayList<NominatimResult>();
                    for(NominatimResult currentResult : response.body()) {
                        if(currentResult.getType().matches("city|administrative")) {
                            resultList.add(currentResult);
                        }
                    }

                    // check whether we have results
                    if(resultList.size() == 0) {
                        components.rcvSearchInputLocationResults.setVisibility(View.GONE);
                        components.lblSearchInputLocationAttribution.setVisibility(View.GONE);
                        components.layoutSearchInputLocationEmpty.setVisibility(View.VISIBLE);
                        return;
                    } else {
                        components.rcvSearchInputLocationResults.setVisibility(View.VISIBLE);
                        components.layoutSearchInputLocationEmpty.setVisibility(View.GONE);
                    }

                    setLocationListAdapter(resultList);
                }
            }

            @Override
            public void onFailure(Call<List<NominatimResult>> call, Throwable t) {
                components.pgbUserLocation.setVisibility(View.GONE);
                showNetworkErrorDialog(() -> loadGeocodingResults(queryString));
            }
        });
    }

    private void loadRouteResults() {
        SettingsManager settingsManager = new SettingsManager(this.getContext());

        StaticRequest staticRequest = new StaticRequest();
        staticRequest.setAppId(this.getString(R.string.MFPL_APP_ID));
        staticRequest.setApiKey(this.getString(R.string.MFPL_API_KEY));
        staticRequest.setDefaultLimit(settingsManager.getPreferencesNumResults());

        Request.Filter filter = new Request.Filter();
        filter.setDate(Request.Filter.Date.fromJavaDate(this.currentSearchDate));
        filter.setWheelchairAccessible(settingsManager.getPreferenceWheelchairAccessible() ? Trip.WheelchairAccessible.YES : Trip.WheelchairAccessible.NO);
        filter.setBikesAllowed(settingsManager.getPreferenceBikesAllowed() ? Trip.BikesAllowed.YES : Trip.BikesAllowed.NO);

        this.showRouteResultList();
        this.setRouteSkeletonAdapter();
        staticRequest.setListener(new StaticRequest.Listener() {
            @Override
            public void onSuccess(Delivery delivery) {
                // check for api errors
                if(delivery.getError() != null) {
                    showNetworkErrorDialog(null);
                    return;
                }

                // check whether we have some results
                resultList = delivery.getRoutes();
                if(resultList.size() == 0) {
                    components.rcvSearchInputRouteResults.setVisibility(View.GONE);
                    components.layoutSearchInputRouteEmpty.setVisibility(View.VISIBLE);
                    return;
                } else {
                    components.rcvSearchInputRouteResults.setVisibility(View.VISIBLE);
                    components.layoutSearchInputRouteEmpty.setVisibility(View.GONE);
                }

                setRouteListAdapter(resultList);
            }

            @Override
            public void onError(Throwable throwable) {
                showNetworkErrorDialog(() -> loadRouteResults());
            }
        }).loadRoutes(new Position().setLatitude(this.currentSearchLatitude).setLongitude(this.currentSearchLongitude), this.currentSearchRadius, filter);
    }
}
