package de.mfpl.app.controllers;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;

import java.util.Date;

import de.mfpl.api.lib.data.Trip;
import de.mfpl.api.lib.data.TripUpdate;
import de.mfpl.app.R;
import de.mfpl.app.adapters.AlertListAdapter;
import de.mfpl.app.adapters.StopTimesAdapter;
import de.mfpl.app.common.DateTimeFormat;
import de.mfpl.app.databinding.LayoutTripDetailsBinding;

public final class TripDetailsActionController {

    private Context context;
    private LayoutTripDetailsBinding components;

    private Date currentTripDate = null;

    public TripDetailsActionController(Context context, LayoutTripDetailsBinding components) {
        this.context = context;
        this.components = components;
    }

    public void setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener listener) {
        this.components.layoutSwipeRefresh.setOnRefreshListener(listener);
    }

    public void setRefreshing(boolean refreshing) {
        this.components.layoutSwipeRefresh.setRefreshing(refreshing);
    }

    public void displayTripDetails(Trip trip, Date currentTripDate, int routeColor) {
        // set current trip date
        this.currentTripDate = currentTripDate;

        // display trip information
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
        String wheelchairString = this.context.getString(R.string.str_trip_details_na);
        String bikeString = this.context.getString(R.string.str_trip_details_na);

        if(trip.getWheelchairAccessible() == Trip.WheelchairAccessible.NO) {
            wheelchairString = this.context.getString(R.string.str_trip_details_wheelchair_no);
            this.components.lblWheelchairAccess.setTextColor(Color.RED);
        } else if(trip.getWheelchairAccessible() == Trip.WheelchairAccessible.YES) {
            wheelchairString = this.context.getString(R.string.str_trip_details_wheelchair_yes);
        }

        if(trip.getBikesAllowed() == Trip.BikesAllowed.NO) {
            bikeString = this.context.getString(R.string.str_trip_details_bikes_no);
            this.components.lblBikesAllowed.setTextColor(Color.RED);
        } else if(trip.getBikesAllowed() == Trip.BikesAllowed.YES) {
            bikeString = this.context.getString(R.string.str_trip_details_bikes_yes);
        }

        this.components.lblWheelchairAccess.setText(wheelchairString);
        this.components.lblBikesAllowed.setText(bikeString);

        // display trip departure times
        StopTimesAdapter stopTimesAdapter = new StopTimesAdapter(this.context, trip);

        // display trip progress with colored line only when the trip is departing today
        String currentDateString = DateTimeFormat.from(this.currentTripDate).to(DateTimeFormat.YYYYMMDD);
        if(currentDateString.equals(DateTimeFormat.from(new Date()).to(DateTimeFormat.YYYYMMDD))) {
            stopTimesAdapter.setLineActiveColor(routeColor);
        } else {
            stopTimesAdapter.setLineActiveColor(ContextCompat.getColor(this.context, R.color.colorBackgroundDarkGray));
        }

        stopTimesAdapter.setPointActiveColor(routeColor);
        stopTimesAdapter.setPointInactiveColor(routeColor);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this.context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        this.components.rcvTripDetails.setLayoutManager(layoutManager);
        this.components.rcvTripDetails.setHasFixedSize(true);
        this.components.rcvTripDetails.setAdapter(stopTimesAdapter);

        // display trip realtime alerts
        if(trip.getRealtime() != null && trip.getRealtime().hasAlerts()) {
            AlertListAdapter alertListAdapter = new AlertListAdapter(this.context, trip.getRealtime().getAlerts());
            this.components.lstTripAlerts.setAdapter(alertListAdapter);
        }
    }
}