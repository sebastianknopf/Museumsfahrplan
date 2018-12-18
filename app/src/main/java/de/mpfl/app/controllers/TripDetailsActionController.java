package de.mpfl.app.controllers;

import android.content.Context;
import android.widget.Toast;

import java.util.List;

import de.mfpl.staticnet.lib.StaticRequest;
import de.mfpl.staticnet.lib.base.Delivery;
import de.mfpl.staticnet.lib.base.Request;
import de.mfpl.staticnet.lib.data.Trip;
import de.mpfl.app.R;
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
                    return;
                }

                Trip tripItem = tripList.get(0);

                // stop displaying refresh progress bar
                components.layoutSwipeRefresh.setRefreshing(false);

                // display basic trip information
                components.lblTripName.setText(tripItem.getTripHeadsign());
                String tripDateString = context.getString(R.string.str_trip_date, DateTimeFormat.from(tripDate, DateTimeFormat.YYYYMMDD).to(DateTimeFormat.DDMMYYYY));
                components.lblTripDate.setText(tripDateString);

                // create trip timeline here...

                Toast.makeText(context, "Trip Info received", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Throwable throwable) {
            }
        }).loadTripDetails(tripId, true);
    }

}
