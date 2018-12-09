package de.mpfl.app.adapters;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import de.mfpl.staticnet.lib.data.Frequency;
import de.mfpl.staticnet.lib.data.Route;
import de.mfpl.staticnet.lib.data.Trip;
import de.mpfl.app.R;
import de.mpfl.app.databinding.LayoutListRouteItemBinding;
import de.mpfl.app.databinding.LayoutListTripItemBinding;
import de.mpfl.app.utils.DateTimeFormat;

public final class TripListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final static int TYPE_TRIP = 0;
    private final static int TYPE_ROUTE = 1;

    private Context context;
    private List<Object> items;

    public TripListAdapter(Context context, List<Trip> tripList) {
        this.context = context;

        List<String> routeList = new ArrayList<>();
        for(Trip trip : tripList) {
            if(trip.getRoute() == null) {
                this.items = new ArrayList<>(tripList);
                return;
            }

            if(!routeList.contains(trip.getRoute().getRouteId())) {
                routeList.add(trip.getRoute().getRouteId());
            }
        }

        this.items = new ArrayList<>();
        for(String route : routeList) {
            boolean routeIncluded = false;
            for(Trip trip : tripList) {
                if(trip.getRoute().getRouteId().equals(route)) {
                    if(!routeIncluded) {
                        this.items.add(trip.getRoute());
                        routeIncluded = true;
                    }

                    this.items.add(trip);
                }
            }
        }
    }

    @Override
    public  int getItemViewType(int index) {
        if(this.items.get(index) instanceof Trip) {
            return TYPE_TRIP;
        } else {
            return TYPE_ROUTE;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        if(viewType == TYPE_TRIP) {
            LayoutListTripItemBinding components = DataBindingUtil.inflate(LayoutInflater.from(viewGroup.getContext()), R.layout.layout_list_trip_item, viewGroup, false);
            return new TripItemViewHolder(components);
        } else {
            LayoutListRouteItemBinding components = DataBindingUtil.inflate(LayoutInflater.from(viewGroup.getContext()), R.layout.layout_list_route_item, viewGroup, false);
            return new RouteItemViewHolder(components);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        if(viewHolder.getItemViewType() == TYPE_TRIP) {
            Trip tripItem = (Trip) this.items.get(i);
            TripItemViewHolder tripItemViewHolder = (TripItemViewHolder) viewHolder;

            if(tripItem != null) {
                String headsignString = tripItem.getTripHeadsign();
                tripItemViewHolder.components.lblTripName.setText(headsignString);

                String departureString = DateTimeFormat.from(tripItem.getStopTimes().get(0).getDepartureTime(), DateTimeFormat.HHMMSS).to(DateTimeFormat.HHMM);
                tripItemViewHolder.components.lblDepartureText.setText(departureString);

                if(tripItem.getFrequency() != null) {
                    String stringTripInfo;
                    if(tripItem.getFrequency().getExactTimes() == Frequency.ExactTimes.YES) {
                        stringTripInfo = this.context.getString(R.string.str_frequency_exact, String.valueOf(tripItem.getFrequency().getHeadway()));
                    } else {
                        stringTripInfo = this.context.getString(R.string.str_frequency_demand, String.valueOf(tripItem.getFrequency().getHeadway()));
                    }

                    tripItemViewHolder.components.lblTripInfo.setVisibility(View.VISIBLE);
                    tripItemViewHolder.components.lblTripInfo.setText(stringTripInfo);
                } else if(!tripItem.getTripShortName().equals(""))
                if(!tripItem.getTripShortName().equals("")) {
                    String tripInfoString = tripItem.getTripShortName();
                    tripItemViewHolder.components.lblTripInfo.setVisibility(View.VISIBLE);
                    tripItemViewHolder.components.lblTripInfo.setText(tripInfoString);
                }
            }
        } else {
            Route routeItem = (Route) this.items.get(i);
            RouteItemViewHolder routeItemViewHolder = (RouteItemViewHolder) viewHolder;

            if(routeItem != null) {
                String stringRouteName = routeItem.getRouteLongName();
                routeItemViewHolder.components.lblRouteName.setText(stringRouteName);

                if(routeItem.getAgency() != null) {
                    String stringAgencyName = routeItem.getAgency().getAgencyName();
                    routeItemViewHolder.components.lblAgencyName.setVisibility(View.VISIBLE);
                    routeItemViewHolder.components.lblAgencyName.setText(stringAgencyName);
                }

                if(!routeItem.getRouteDescription().equals("")) {
                    String stringRouteDescription = routeItem.getRouteDescription();
                    routeItemViewHolder.components.lblRouteDescription.setVisibility(View.VISIBLE);
                    routeItemViewHolder.components.lblRouteDescription.setText(stringRouteDescription);
                }

                String stringRouteInfoURL = null;
                if(routeItem.getAgency() != null) {
                    if(!routeItem.getAgency().getAgencyUrl().equals("")) {
                        stringRouteInfoURL = routeItem.getAgency().getAgencyUrl();
                    }
                }

                if(!routeItem.getRouteUrl().equals("")) {
                    stringRouteInfoURL = routeItem.getRouteUrl();
                }

                routeItemViewHolder.components.lblInfoURL.setVisibility(View.VISIBLE);
                routeItemViewHolder.components.lblInfoURL.setText(stringRouteInfoURL.replace("http://", "").replace("https://", ""));

                if(routeItem.getAgency() != null) {
                    if(!routeItem.getAgency().getAgencyPhone().equals("")) {
                        String routeInfoPhone = routeItem.getAgency().getAgencyPhone();
                        routeItemViewHolder.components.lblInfoPhone.setVisibility(View.VISIBLE);
                        routeItemViewHolder.components.lblInfoPhone.setText(routeInfoPhone);
                    }
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return this.items.size();
    }

    public static class TripItemViewHolder extends RecyclerView.ViewHolder {

        public LayoutListTripItemBinding components;

        public TripItemViewHolder(LayoutListTripItemBinding components) {
            super(components.getRoot());
            this.components = components;
        }
    }

    public static class RouteItemViewHolder extends RecyclerView.ViewHolder {

        public LayoutListRouteItemBinding components;

        public RouteItemViewHolder(LayoutListRouteItemBinding components) {
            super(components.getRoot());
            this.components = components;
        }
    }
}
