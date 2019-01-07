package de.mpfl.app.controllers;

import android.content.Context;
import android.support.v7.widget.DividerItemDecoration;
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
import de.mpfl.app.listeners.OnTripItemClickListener;

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
        StaticRequest staticRequest = new StaticRequest();
        staticRequest.setAppId(this.context.getString(R.string.MFPL_APP_ID));
        staticRequest.setApiKey(this.context.getString(R.string.MFPL_API_KEY));

        Request.Filter filter = new Request.Filter();
        filter.setDate(Request.Filter.Date.fromJavaDate(new Date()));
        filter.setTime(new SimpleDateFormat("HH:mm:ss").format(new Date()));

        this.components.rcvTripList.setVisibility(View.GONE);
        this.components.layErrorView.setVisibility(View.GONE);
        this.components.layProgressView.setVisibility(View.VISIBLE);
        staticRequest.setListener(new StaticRequest.Listener() {
            @Override
            public void onSuccess(Delivery delivery) {
                // check api errors here
                if(delivery.getError() != null) {
                    components.progressBar.setVisibility(View.GONE);
                    components.layErrorView.setVisibility(View.VISIBLE);

                    components.lblErrorText.setText(delivery.getError().getErrorMessage());
                    return;
                }

                currentTripList = delivery.getTrips();
                if(currentTripList.size() == 0) {
                    components.layProgressView.setVisibility(View.GONE);
                    components.layErrorView.setVisibility(View.VISIBLE);
                    components.lblErrorText.setText(context.getString(R.string.str_no_departures_found));
                    return;
                }

                TripListAdapter tripListAdapter = new TripListAdapter(context, currentTripList);
                tripListAdapter.setOnTripItemClickListener(new OnTripItemClickListener() {
                    @Override
                    public void onTripItemClick(Trip tripItem) {
                        // pass through the clicked trip item
                        OnTripItemClickListener parentListener = components.getOnTripItemClickListener();
                        if(parentListener != null) {
                            parentListener.onTripItemClick(tripItem);
                        }
                    }
                });

                components.layProgressView.setVisibility(View.GONE);
                components.rcvTripList.setVisibility(View.VISIBLE);

                LinearLayoutManager layoutManager = new LinearLayoutManager(context);
                layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                components.rcvTripList.setLayoutManager(layoutManager);
                components.rcvTripList.setAdapter(tripListAdapter);

                components.layProgressView.setVisibility(View.GONE);
                components.rcvTripList.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError(Throwable throwable) {
                components.progressBar.setVisibility(View.GONE);
                components.layErrorView.setVisibility(View.VISIBLE);

                components.lblErrorText.setText(R.string.str_request_error);
            }
        }).loadDepartures(stopId, filter);
    }
}
