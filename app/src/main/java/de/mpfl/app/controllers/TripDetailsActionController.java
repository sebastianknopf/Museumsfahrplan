package de.mpfl.app.controllers;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;

import java.util.List;

import de.mfpl.staticnet.lib.StaticRequest;
import de.mfpl.staticnet.lib.base.Delivery;
import de.mfpl.staticnet.lib.base.Request;
import de.mfpl.staticnet.lib.data.Trip;
import de.mpfl.app.R;
import de.mpfl.app.adapters.AlertListAdapter;
import de.mpfl.app.adapters.StopTimesAdapter;
import de.mpfl.app.databinding.LayoutTripDetailsBinding;
import de.mpfl.app.utils.DateTimeFormat;

public final class TripDetailsActionController {

    private Context context;
    private LayoutTripDetailsBinding components;

    public TripDetailsActionController(Context context, LayoutTripDetailsBinding components) {
        this.context = context;
        this.components = components;
    }

    public void loadTripDetails(String tripId, String tripDate, String tripTime) {
        StaticRequest staticRequest = new StaticRequest();
        staticRequest.setAppId(this.context.getString(R.string.MFPL_APP_ID));
        staticRequest.setApiKey(this.context.getString(R.string.MFPL_API_KEY));

        Request.Filter filter = new Request.Filter();
        filter.setDate(new Request.Filter.Date().setSingle(tripDate));
        filter.setTime(tripTime);

        staticRequest.setListener(new StaticRequest.Listener() {
            @Override
            public void onSuccess(Delivery delivery) {
                List<Trip> tripList = delivery.getTrips();
                if(tripList.size() != 1) {
                    // todo: display unambigous trip error here...
                    return;
                }

                Trip tripItem = tripList.get(0);

                // stop displaying refresh progress bar and display trip details view
                components.layoutSwipeRefresh.setRefreshing(false);
                components.layoutTripDetailsView.setVisibility(View.VISIBLE);
                components.layoutTripDetailsError.setVisibility(View.GONE);

                // display basic trip information
                String tripNameString = tripItem.getTripHeadsign();
                if(!tripItem.getTripShortName().equals("")) {
                    tripNameString = tripItem.getTripShortName() + " " + tripNameString;
                }
                components.lblTripName.setText(tripNameString);

                // trip date of selected trip
                String tripDateString = context.getString(R.string.str_trip_date, DateTimeFormat.from(tripDate, DateTimeFormat.YYYYMMDD).to(DateTimeFormat.DDMMYYYY));
                components.lblTripDate.setText(tripDateString);

                // create trip timeline here...
                StopTimesAdapter stopTimesAdapter = new StopTimesAdapter(context, tripItem.getStopTimes());
                stopTimesAdapter.setLineActiveColor(ContextCompat.getColor(context, R.color.colorAccentDAY));
                stopTimesAdapter.setPointActiveColor(ContextCompat.getColor(context, R.color.colorAccentDAY));
                stopTimesAdapter.setPointInactiveColor(ContextCompat.getColor(context, R.color.colorAccentDAY));

                LinearLayoutManager layoutManager = new LinearLayoutManager(context);
                layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                components.rcvTripDetails.setLayoutManager(layoutManager);
                components.rcvTripDetails.setHasFixedSize(true);
                components.rcvTripDetails.setAdapter(stopTimesAdapter);

                // display trip assigned alerts
                if(tripItem.getRealtime().hasAlerts()) {
                    AlertListAdapter alertListAdapter = new AlertListAdapter(context, tripItem.getRealtime().getAlerts());
                    components.lstTripAlerts.setAdapter(alertListAdapter);
                }
            }

            @Override
            public void onError(Throwable throwable) {
                // todo: error handling!
                // stop displaying refresh progress bar and display trip error view
                components.layoutSwipeRefresh.setRefreshing(false);
                components.layoutTripDetailsView.setVisibility(View.GONE);
                components.layoutTripDetailsError.setVisibility(View.VISIBLE);
            }
        }).loadTripDetails(tripId, filter, true);
    }

}
