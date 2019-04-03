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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.Date;
import java.util.GregorianCalendar;

import de.mfpl.api.lib.DataRequest;
import de.mfpl.api.lib.base.Delivery;
import de.mfpl.api.lib.base.Request;
import de.mfpl.api.lib.data.Calendar;
import de.mfpl.api.lib.data.Day;
import de.mfpl.api.lib.data.Trip;
import de.mfpl.app.R;
import de.mfpl.app.adapters.CalendarAdapter;
import de.mfpl.app.adapters.SkeletonAdapter;
import de.mfpl.app.common.DateTimeFormat;
import de.mfpl.app.common.SettingsManager;
import de.mfpl.app.databinding.FragmentSearchCalendarBinding;
import de.mfpl.app.dialogs.ErrorDialog;
import de.mfpl.app.listeners.OnCalendarItemClickListener;
import de.mfpl.app.listeners.OnFragmentInteractionListener;

public class SearchCalendarFragment extends Fragment implements OnCalendarItemClickListener {

    public final static String TAG = "SearchCalendarFragment";

    public final static String KEY_FRAGMENT_ACTION = "KEY_FRAGMENT_ACTION";
    public final static String KEY_SEARCH_ROUTE_ID = "KEY_SEARCH_ROUTE_ID";
    public final static String KEY_SEARCH_ROUTE_NAME = "KEY_SEARCH_ROUTE_NAME";
    public final static String KEY_SEARCH_DATE = "KEY_ROUTE_DATE";
    public final static String KEY_SEARCH_TIME = "KEY_SEARCH_TIME";

    public final static int ACTION_SELECT_ROUTE = 0;
    public final static int ACTION_SHOW_SETTINGS = 1;

    private FragmentSearchCalendarBinding components;
    private RecyclerView.ItemDecoration itemDecoration;
    private OnFragmentInteractionListener fragmentInteractionListener;

    // needed for retain behaviour when returning from back stack to this fragment
    private Calendar resultCalendar = null;

    public SearchCalendarFragment() {
        // Required empty public constructor
    }

    public static SearchCalendarFragment newInstance() {
        SearchCalendarFragment fragment = new SearchCalendarFragment();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.components = DataBindingUtil.inflate(inflater, R.layout.fragment_search_calendar, container, false);
        this.components.setFragment(this);

        // set activity title
        AppCompatActivity activity = (AppCompatActivity) this.getActivity();
        if(activity != null) {
            activity.getSupportActionBar().setTitle(this.getString(R.string.str_search_calendar_title));
        }

        // item decor
        this.itemDecoration = new DividerItemDecoration(this.getContext(), DividerItemDecoration.VERTICAL);

        // load calendar from server
        if(this.resultCalendar != null && resultCalendar.getDays().size() > 0) {
            this.setCalendarAdapter(this.resultCalendar);
        } else {
            this.loadCalendar();
        }

        LinearLayoutManager layoutLocationManager = new LinearLayoutManager(getContext());
        layoutLocationManager.setOrientation(LinearLayoutManager.VERTICAL);
        this.components.rcvCalendarView.setLayoutManager(layoutLocationManager);

        return this.components.getRoot();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_search_calendar_fragment, menu);
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
    public void onCalendarItemClick(Day day) {
        if(this.fragmentInteractionListener != null) {
            Bundle arguments = new Bundle();
            arguments.putInt(KEY_FRAGMENT_ACTION, ACTION_SELECT_ROUTE);
            arguments.putString(KEY_SEARCH_ROUTE_ID, day.getRoute().getRouteId());
            arguments.putString(KEY_SEARCH_ROUTE_NAME, day.getRoute().getRouteLongName());
            arguments.putString(KEY_SEARCH_DATE, DateTimeFormat.from(day.getDate(), DateTimeFormat.YYYYMMDD).to(DateTimeFormat.DDMMYYYY));
            arguments.putString(KEY_SEARCH_TIME, "00:00:01");

            this.fragmentInteractionListener.onFragmentInteraction(this, arguments);
        }
    }

    public void setCalendarAdapter(Calendar resultCalendar) {
        CalendarAdapter calendarAdapter = new CalendarAdapter(this.getContext(), this.resultCalendar);
        calendarAdapter.setOnCalendarItemClickListener(this);

        // divider setup
        this.components.rcvCalendarView.addItemDecoration(this.itemDecoration);

        this.components.rcvCalendarView.setAdapter(calendarAdapter);
    }

    private void setSkeletonAdapter() {
        SkeletonAdapter skeletonAdapter = new SkeletonAdapter(this.getContext(), 5);
        skeletonAdapter.setViewType(SkeletonAdapter.TYPE_DATA_ONLY);

        // divider setup
        this.components.rcvCalendarView.removeItemDecoration(this.itemDecoration);

        this.components.rcvCalendarView.setAdapter(skeletonAdapter);
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

    private void loadCalendar() {
        this.setSkeletonAdapter();

        SettingsManager settingsManager = new SettingsManager(this.getContext());

        DataRequest dataRequest = new DataRequest();
        dataRequest.setAppId(settingsManager.getAppId());
        dataRequest.setApiKey(settingsManager.getApiKey());

        Request.Filter filter = new Request.Filter();
        filter.setWheelchairAccessible(settingsManager.getPreferenceWheelchairAccessible() ? Trip.WheelchairAccessible.YES : Trip.WheelchairAccessible.NO);
        filter.setBikesAllowed(settingsManager.getPreferenceBikesAllowed() ? Trip.BikesAllowed.YES : Trip.BikesAllowed.NO);

        // calculate start and end date for calendar request
        String startDate = DateTimeFormat.from(new Date()).to(DateTimeFormat.YYYYMMDD);
        String endDate = null;

        GregorianCalendar calendar = (GregorianCalendar) GregorianCalendar.getInstance();
        calendar.add(java.util.Calendar.MONTH, 6);
        endDate = DateTimeFormat.from(calendar.getTime()).to(DateTimeFormat.YYYYMMDD);

        dataRequest.setListener(new DataRequest.Listener() {
            @Override
            public void onSuccess(Delivery delivery, double duration) {
                // check for api errors
                if(delivery.getError() != null) {
                    showNetworkErrorDialog(null);
                    return;
                }

                // select complete calendar
                resultCalendar = delivery.getCalendar();
                if(resultCalendar.getDays().size() == 0) {
                    components.rcvCalendarView.setVisibility(View.GONE);
                    components.layoutSearchCalendarEmpty.setVisibility(View.VISIBLE);
                    return;
                } else {
                    components.rcvCalendarView.setVisibility(View.VISIBLE);
                    components.layoutSearchCalendarEmpty.setVisibility(View.GONE);
                }

                // set list adapter
                setCalendarAdapter(resultCalendar);
            }

            @Override
            public void onError(Throwable throwable, double duration) {
                showNetworkErrorDialog(() -> loadCalendar());
            }
        }).loadCalendar(startDate, endDate, filter);
    }
}
