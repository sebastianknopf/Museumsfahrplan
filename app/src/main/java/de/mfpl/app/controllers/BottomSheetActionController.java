package de.mfpl.app.controllers;

import android.content.Context;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import de.mfpl.app.R;
import de.mfpl.app.adapters.CalendarAdapter;
import de.mfpl.app.common.DateTimeFormat;
import de.mfpl.app.common.SettingsManager;
import de.mfpl.app.databinding.LayoutMapBottomSheetBinding;
import de.mfpl.app.listeners.OnCalendarItemClickListener;
import de.mfpl.staticnet.lib.DataRequest;
import de.mfpl.staticnet.lib.base.Delivery;
import de.mfpl.staticnet.lib.base.Request;
import de.mfpl.staticnet.lib.data.Calendar;
import de.mfpl.staticnet.lib.data.Trip;

public final class BottomSheetActionController {

    private List<Trip> currentTripList;

    private Context context;
    private LayoutMapBottomSheetBinding components;

    public BottomSheetActionController(Context context, LayoutMapBottomSheetBinding components) {
        this.context = context;
        this.components = components;

        DividerItemDecoration itemDecor = new DividerItemDecoration(context, DividerItemDecoration.VERTICAL);
        this.components.rcvTripList.addItemDecoration(itemDecor);
    }

    public void loadDepartures(String stopId) {
        SettingsManager settingsManager = new SettingsManager(this.context);

        DataRequest dataRequest = new DataRequest();
        dataRequest.setAppId(settingsManager.getAppId());
        dataRequest.setApiKey(settingsManager.getApiKey());

        Request.Filter filter = new Request.Filter();
        filter.setWheelchairAccessible(settingsManager.getPreferenceWheelchairAccessible() ? Trip.WheelchairAccessible.YES : Trip.WheelchairAccessible.NO);
        filter.setBikesAllowed(settingsManager.getPreferenceBikesAllowed() ? Trip.BikesAllowed.YES : Trip.BikesAllowed.NO);

        // set selection time and date
        String startDate = DateTimeFormat.from(new Date()).to(DateTimeFormat.YYYYMMDD);

        GregorianCalendar gregorianCalendar = (GregorianCalendar) GregorianCalendar.getInstance();
        gregorianCalendar.setTime(new Date());
        gregorianCalendar.add(GregorianCalendar.MONTH, 6);
        String endDate = DateTimeFormat.from(gregorianCalendar.getTime()).to(DateTimeFormat.YYYYMMDD);

        // load calendar results
        this.components.rcvTripList.setVisibility(View.GONE);
        this.components.layErrorView.setVisibility(View.GONE);
        this.components.layProgressView.setVisibility(View.VISIBLE);
        dataRequest.setListener(new DataRequest.Listener() {
            @Override
            public void onSuccess(Delivery delivery) {
                // check api errors here
                if(delivery.getError() != null) {
                    components.progressBar.setVisibility(View.GONE);
                    components.layErrorView.setVisibility(View.VISIBLE);

                    components.lblErrorText.setText(delivery.getError().getErrorMessage());
                    return;
                }

                Calendar calendar = delivery.getCalendar();
                CalendarAdapter calendarAdapter = new CalendarAdapter(context, calendar, CalendarAdapter.DISPLAY_MODE_DAYS);
                calendarAdapter.setOnCalendarItemClickListener(object -> {
                    OnCalendarItemClickListener parentListener = components.getOnCalendarItemClickListener();
                    if(parentListener != null) {
                        parentListener.onCalendarItemClick(object);
                    }
                });

                components.layProgressView.setVisibility(View.GONE);
                components.rcvTripList.setVisibility(View.VISIBLE);

                LinearLayoutManager layoutManager = new LinearLayoutManager(context);
                layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                components.rcvTripList.setLayoutManager(layoutManager);
                components.rcvTripList.setAdapter(calendarAdapter);

                components.layProgressView.setVisibility(View.GONE);
                components.rcvTripList.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError(Throwable throwable) {
                components.progressBar.setVisibility(View.GONE);
                components.layErrorView.setVisibility(View.VISIBLE);

                components.lblErrorText.setText(R.string.str_default_request_error);
            }
        }).loadCalendar(stopId, startDate, endDate, filter);
    }
}
