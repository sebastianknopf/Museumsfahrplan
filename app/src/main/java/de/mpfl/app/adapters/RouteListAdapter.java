package de.mpfl.app.adapters;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import de.mfpl.staticnet.lib.data.Route;
import de.mpfl.app.R;
import de.mpfl.app.databinding.LayoutListRouteItemBinding;
import de.mpfl.app.listeners.OnRecyclerViewItemClickListener;
import de.mpfl.app.listeners.OnRouteItemClickListener;

public final class RouteListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements OnRecyclerViewItemClickListener {

    private Context context;
    private OnRouteItemClickListener routeItemClickListener;
    private List<Route> routeList;

    public RouteListAdapter(Context context, List<Route> routeList) {
        this.context = context;
        this.routeList = routeList;
    }

    public void setOnRouteItemClickListener(OnRouteItemClickListener routeItemClickListener) {
        this.routeItemClickListener = routeItemClickListener;
    }

    @Override
    public RouteItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        LayoutListRouteItemBinding components = DataBindingUtil.inflate(LayoutInflater.from(viewGroup.getContext()), R.layout.layout_list_route_item, viewGroup, false);
        return new RouteItemViewHolder(components, this);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        Route routeItem = (Route) this.routeList.get(position);
        RouteItemViewHolder routeItemViewHolder = (RouteItemViewHolder) viewHolder;

        if(routeItem != null) {
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
                routeItemViewHolder.components.lblRouteDistance.setText(context.getString(R.string.str_distance_km, Math.ceil((double)routeItem.getPosition().getDistance()/1000.0)));
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
                if(!routeItem.getAgency().getAgencyPhone().equals("")) {
                    String routeInfoPhone = routeItem.getAgency().getAgencyPhone();
                    routeItemViewHolder.components.lblInfoPhone.setVisibility(View.VISIBLE);
                    routeItemViewHolder.components.lblInfoPhone.setText(routeInfoPhone);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return this.routeList.size();
    }

    @Override
    public void onItemClick(RecyclerView.ViewHolder viewHolder, int position) {
        if(this.routeItemClickListener != null) {
            Route routeItem = this.routeList.get(position);
            this.routeItemClickListener.onRouteItemClick(routeItem);
        }
    }

    static class RouteItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public LayoutListRouteItemBinding components;
        private OnRecyclerViewItemClickListener recyclerViewItemClickListener;

        public RouteItemViewHolder(LayoutListRouteItemBinding components, OnRecyclerViewItemClickListener recyclerViewItemClickListener) {
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
}
