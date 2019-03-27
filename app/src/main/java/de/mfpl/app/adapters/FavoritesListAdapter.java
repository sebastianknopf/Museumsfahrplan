package de.mfpl.app.adapters;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import de.mfpl.app.R;
import de.mfpl.app.common.DateTimeFormat;
import de.mfpl.app.database.Favorite;
import de.mfpl.app.databinding.LayoutListFavoriteItemBinding;
import de.mfpl.app.listeners.OnFavoriteItemClickListener;
import de.mfpl.app.listeners.OnRecyclerViewItemClickListener;

public final class FavoritesListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements OnRecyclerViewItemClickListener {

    private Context context;
    private OnFavoriteItemClickListener favoriteItemClickListener;
    private List<Favorite> favoritesList;

    public FavoritesListAdapter(Context context, List<Favorite> favoritesList) {
        this.context = context;
        this.favoritesList = favoritesList;
    }

    public void setOnFavoriteItemClickListener(OnFavoriteItemClickListener favoriteItemClickListener) {
        this.favoriteItemClickListener = favoriteItemClickListener;
    }

    @Override
    public FavoriteItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutListFavoriteItemBinding components = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.layout_list_favorite_item, parent, false);
        return new FavoriteItemViewHolder(components, this);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Favorite favoriteItem = this.favoritesList.get(position);
        FavoriteItemViewHolder favoriteItemViewHolder = (FavoriteItemViewHolder) holder;

        if(favoriteItem != null) {
            // trip icon
            Drawable tripIcon = null;
            switch(favoriteItem.getTripType()) {
                case "RAIL":
                    tripIcon = this.context.getDrawable(R.drawable.ic_route_railway);
                    break;

                case "TRAM":
                    tripIcon = this.context.getDrawable(R.drawable.ic_route_tram);
                    break;

                case "BUS":
                    tripIcon = this.context.getDrawable(R.drawable.ic_route_bus);
                    break;

                case "FERRY":
                    tripIcon = this.context.getDrawable(R.drawable.ic_route_ferry);
                    break;

                case "FUNICULAR":
                    tripIcon = this.context.getDrawable(R.drawable.ic_route_funicular);
                    break;

                default:
                    tripIcon = this.context.getDrawable(R.drawable.ic_route_railway);
                    break;
            }

            favoriteItemViewHolder.components.imgTripType.setImageDrawable(tripIcon);

            // set trip name headsign
            String tripName = favoriteItem.getTripName();
            favoriteItemViewHolder.components.lblTripName.setText(tripName);

            // set trip route info
            String tripRouteName = favoriteItem.getTripDesc();
            favoriteItemViewHolder.components.lblTripDesc.setText(tripRouteName);

            // set trip date / time as text
            String tripDateTime = DateTimeFormat.from(favoriteItem.getTripDate(), DateTimeFormat.YYYYMMDD).to(DateTimeFormat.DDMMYYYY);
            if(favoriteItem.getTripTime() != null && !favoriteItem.getTripTime().equals("")) {
                tripDateTime += " " + DateTimeFormat.from(favoriteItem.getTripTime(), DateTimeFormat.HHMMSS).to(DateTimeFormat.HHMM);
            }

            favoriteItemViewHolder.components.lblTripDateTime.setText(tripDateTime);
        }
    }

    @Override
    public int getItemCount() {
        return this.favoritesList.size();
    }

    @Override
    public void onItemClick(RecyclerView.ViewHolder viewHolder, int position) {
        if(this.favoriteItemClickListener != null) {
            Favorite favoriteItem = this.favoritesList.get(position);
            this.favoriteItemClickListener.onFavoriteItemClick(favoriteItem);
        }
    }

    public List<Favorite> getFavoritesList() {
        return this.favoritesList;
    }

    public void removeItem(int position) {
        try {
            this.favoritesList.remove(position);
            this.notifyItemRemoved(position);
        } catch (Exception e) {
        }
    }

    public void insertItem(int position, Favorite favorite) {
        try {
            this.favoritesList.add(position, favorite);
            this.notifyItemInserted(position);
        } catch (Exception e) {
        }
    }

    static class FavoriteItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public LayoutListFavoriteItemBinding components;
        private OnRecyclerViewItemClickListener recyclerViewItemClickListener;

        public FavoriteItemViewHolder(LayoutListFavoriteItemBinding components, OnRecyclerViewItemClickListener recyclerViewItemClickListener) {
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
