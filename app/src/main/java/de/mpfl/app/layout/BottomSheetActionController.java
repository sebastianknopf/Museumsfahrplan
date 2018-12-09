package de.mpfl.app.layout;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import de.mfpl.staticnet.lib.StaticRequest;
import de.mfpl.staticnet.lib.base.Delivery;
import de.mfpl.staticnet.lib.base.Request;
import de.mfpl.staticnet.lib.data.Trip;
import de.mpfl.app.R;
import de.mpfl.app.adapters.TripListAdapter;
import de.mpfl.app.databinding.LayoutMapBottomSheetBinding;

public final class BottomSheetActionController {

    private Context context;
    private LayoutMapBottomSheetBinding components;

    public BottomSheetActionController(Context context, LayoutMapBottomSheetBinding components) {
        this.context = context;
        this.components = components;
    }

    public void loadDepartures(String stopId) {
        StaticRequest staticRequest = new StaticRequest();
        staticRequest.setAppId(this.context.getString(R.string.MFPL_APP_ID));
        staticRequest.setApiKey(this.context.getString(R.string.MFPL_API_KEY));

        Request.Filter filter = new Request.Filter();
        filter.setDate(Request.Filter.Date.fromJavaDate(new Date()));
        filter.setTime(new SimpleDateFormat("HH:mm:ss").format(new Date()));

        this.components.tripListHolder.rcvTripList.setVisibility(View.GONE);
        this.components.tripListHolder.layErrorView.setVisibility(View.GONE);
        this.components.tripListHolder.layProgressView.setVisibility(View.VISIBLE);
        staticRequest.setListener(new StaticRequest.Listener() {
            @Override
            public void onSuccess(Delivery delivery) {
                List<Trip> tripList = delivery.getTrips();
                if(tripList.size() == 0) {
                    components.tripListHolder.layProgressView.setVisibility(View.GONE);
                    components.tripListHolder.layErrorView.setVisibility(View.VISIBLE);
                    components.tripListHolder.lblErrorText.setText(context.getString(R.string.str_no_departures_found));
                    return;
                }

                TripListAdapter tripListAdapter = new TripListAdapter(context, tripList);
                components.tripListHolder.layProgressView.setVisibility(View.GONE);
                components.tripListHolder.rcvTripList.setVisibility(View.VISIBLE);

                LinearLayoutManager layoutManager = new LinearLayoutManager(context);
                layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                components.tripListHolder.rcvTripList.setLayoutManager(layoutManager);
                components.tripListHolder.rcvTripList.setAdapter(tripListAdapter);

                components.tripListHolder.layProgressView.setVisibility(View.GONE);
                components.tripListHolder.rcvTripList.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError(Throwable throwable) {
                components.tripListHolder.progressBar.setVisibility(View.GONE);
                components.tripListHolder.layErrorView.setVisibility(View.VISIBLE);
            }
        }).loadDepartures(stopId, filter);
    }
}
