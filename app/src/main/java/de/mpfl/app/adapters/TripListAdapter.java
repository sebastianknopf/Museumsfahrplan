package de.mpfl.app.adapters;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
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
import de.mpfl.app.listeners.OnRecyclerViewItemClickListener;
import de.mpfl.app.listeners.OnTripItemClickListener;
import de.mpfl.app.utils.DateTimeFormat;

public final class TripListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements OnRecyclerViewItemClickListener {

    private final static int TYPE_TRIP = 0;
    private final static int TYPE_ROUTE = 1;

    private Context context;
    private OnTripItemClickListener tripItemClickListener;
    private List<Object> items;

    public TripListAdapter(Context context, List<Trip> tripList) {
        this.context = context;

        // sort input trips by their route, add each route
        // once as header and every trip sorted by it's departure
        // time below the route item

        // create list of all routes
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

        // iterate over all routes and find corresponding
        // trips for this route
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

    public void setOnTripItemClickListener(OnTripItemClickListener tripItemClickListener) {
        this.tripItemClickListener = tripItemClickListener;
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
            return new TripItemViewHolder(components, this);
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
                // display route icon depending on route type
                Route.RouteType routeType = tripItem.getRoute().getRouteType();
                if(routeType == Route.RouteType.RAIL) {
                    tripItemViewHolder.components.imgTripType.setImageDrawable(context.getDrawable(R.drawable.ic_route_railway));
                } else if(routeType == Route.RouteType.TRAM || routeType == Route.RouteType.SUBWAY) {
                    tripItemViewHolder.components.imgTripType.setImageDrawable(context.getDrawable(R.drawable.ic_route_tram));
                } else if(routeType == Route.RouteType.BUS) {
                    tripItemViewHolder.components.imgTripType.setImageDrawable(context.getDrawable(R.drawable.ic_route_bus));
                } else if(routeType == Route.RouteType.FERRY) {
                    tripItemViewHolder.components.imgTripType.setImageDrawable(context.getDrawable(R.drawable.ic_route_ferry));
                } else if(routeType == Route.RouteType.FUNICULAR) {
                    tripItemViewHolder.components.imgTripType.setImageDrawable(context.getDrawable(R.drawable.ic_route_funicular));
                }

                String headsignString = tripItem.getTripHeadsign();
                tripItemViewHolder.components.lblTripName.setText(headsignString);

                String departureString = DateTimeFormat.from(tripItem.getStopTimes().get(0).getDepartureTime(), DateTimeFormat.HHMMSS).to(DateTimeFormat.HHMM);
                tripItemViewHolder.components.lblDepartureText.setText(departureString);

                // display exceptional icon
                if(tripItem.getRealtime().hasAlerts()) {
                    tripItemViewHolder.components.imgExceptional.setVisibility(View.VISIBLE);
                } else {
                    tripItemViewHolder.components.imgExceptional.setVisibility(View.GONE);
                }

                // trip short name / trip info
                String shortNameString = tripItem.getTripShortName();
                if(!shortNameString.equals("")) {
                    tripItemViewHolder.components.lblTripAdditionalInfo.setVisibility(View.VISIBLE);
                    tripItemViewHolder.components.lblTripAdditionalInfo.setText(shortNameString);
                } else if(tripItem.getRealtime().hasAlerts()) {
                    tripItemViewHolder.components.lblTripAdditionalInfo.setVisibility(View.VISIBLE);
                    tripItemViewHolder.components.lblTripAdditionalInfo.setText(R.string.str_exceptional);
                } else {
                    tripItemViewHolder.components.lblTripAdditionalInfo.setVisibility(View.GONE);
                }

                // frequency info text - only if this is not the last trip in this frequency period
                if(tripItem.getFrequency() != null && !tripItem.getFrequency().getEndTime().equals(tripItem.getFrequency().getTripTime())) {
                    tripItemViewHolder.components.lblTripFrequencyInfo.setVisibility(View.VISIBLE);
                    if(tripItem.getFrequency().getExactTimes() == Frequency.ExactTimes.YES) {
                        tripItemViewHolder.components.lblTripFrequencyInfo.setText(this.context.getString(R.string.str_frequency_exact, String.valueOf(tripItem.getFrequency().getHeadway())));
                    } else {
                        tripItemViewHolder.components.lblTripFrequencyInfo.setText(this.context.getString(R.string.str_frequency_demand, String.valueOf(tripItem.getFrequency().getHeadway())));
                    }
                } else {
                    tripItemViewHolder.components.lblTripFrequencyInfo.setVisibility(View.GONE);
                }
            }
        } else {
            Route routeItem = (Route) this.items.get(i);
            RouteItemViewHolder routeItemViewHolder = (RouteItemViewHolder) viewHolder;

            if(routeItem != null) {
                // override route item background
                routeItemViewHolder.components.getRoot().setBackgroundColor(ContextCompat.getColor(context, R.color.colorBackgroundLightGray));

                // display realtime alerts assigned to this route
                if(routeItem.getRealtime().hasAlerts() || routeItem.getAgency().getRealtime().hasAlerts()) {
                    AlertListAdapter adapter = new AlertListAdapter(this.context, routeItem.getRealtime().getAlerts());
                    routeItemViewHolder.components.lstRouteAlerts.setVisibility(View.VISIBLE);
                    routeItemViewHolder.components.lstRouteAlerts.setAdapter(adapter);
                } else {
                    routeItemViewHolder.components.lstRouteAlerts.setVisibility(View.GONE);
                }

                String stringRouteName = routeItem.getRouteLongName();
                routeItemViewHolder.components.lblRouteName.setText(stringRouteName);

                // display distance in km's if there's a distance available
                if(routeItem.getPosition() != null && routeItem.getPosition().getDistance() != 0) {
                    routeItemViewHolder.components.lblRouteDistance.setVisibility(View.VISIBLE);
                    routeItemViewHolder.components.lblRouteDistance.setText(context.getString(R.string.str_distance_km, routeItem.getPosition().getDistance()/1000));
                }

                // agency name
                if(routeItem.getAgency() != null) {
                    String stringAgencyName = routeItem.getAgency().getAgencyName();
                    routeItemViewHolder.components.lblAgencyName.setVisibility(View.VISIBLE);
                    routeItemViewHolder.components.lblAgencyName.setText(stringAgencyName);
                }

                // route description below
                if(!routeItem.getRouteDescription().equals("")) {
                    String stringRouteDescription = routeItem.getRouteDescription();
                    routeItemViewHolder.components.lblRouteDescription.setVisibility(View.VISIBLE);
                    routeItemViewHolder.components.lblRouteDescription.setText(stringRouteDescription);
                }

                // display information url - peferred url is route url if available
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

                // information phone number from agency object
                if(routeItem.getAgency() != null) {
                    if (!routeItem.getAgency().getAgencyPhone().equals("")) {
                        String routeInfoPhone = routeItem.getAgency().getAgencyPhone();
                        routeItemViewHolder.components.lblInfoPhone.setVisibility(View.VISIBLE);
                        routeItemViewHolder.components.lblInfoPhone.setText(routeInfoPhone);
                    }
                }

                // override route items background
                routeItemViewHolder.components.getRoot().setBackgroundColor(ContextCompat.getColor(context, R.color.colorBackgroundLightGray));
            }
        }
    }

    @Override
    public int getItemCount() {
        return this.items.size();
    }

    @Override
    public void onItemClick(RecyclerView.ViewHolder viewHolder, int position) {
        if(this.tripItemClickListener != null) {
            Trip tripItem = (Trip) this.items.get(position);
            this.tripItemClickListener.onTripItemClick(tripItem);
        }
    }

    public static class TripItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public LayoutListTripItemBinding components;
        private OnRecyclerViewItemClickListener recyclerViewItemClickListener;

        public TripItemViewHolder(LayoutListTripItemBinding components, OnRecyclerViewItemClickListener recyclerViewItemClickListener) {
            super(components.getRoot());
            this.components = components;
            this.recyclerViewItemClickListener = recyclerViewItemClickListener;

            this.components.getRoot().setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if(this.recyclerViewItemClickListener != null) {
                this.recyclerViewItemClickListener.onItemClick(this, this.getLayoutPosition());
            }
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
