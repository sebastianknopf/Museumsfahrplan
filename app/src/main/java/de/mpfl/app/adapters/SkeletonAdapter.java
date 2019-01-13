package de.mpfl.app.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.shimmer.ShimmerFrameLayout;

import de.mpfl.app.R;

public final class SkeletonAdapter extends RecyclerView.Adapter<SkeletonAdapter.SkeletonItemViewHolder> {

    public final static int TYPE_DATA_ONLY = 0;
    public final static int TYPE_DATA_ICON = 1;

    private Context context;
    private int itemsCount;

    private int viewType = TYPE_DATA_ONLY;
    private boolean firstItemDifferent = false;

    public SkeletonAdapter(Context context, int itemsCount) {
        this.context = context;
        this.itemsCount = itemsCount;
    }

    public void setFirstItemDifferent(boolean firstItemDifferent) {
        this.firstItemDifferent = firstItemDifferent;
    }

    public void setViewType(int viewType) {
        this.viewType = viewType;
    }

    @Override
    public int getItemViewType(int position) {
        int viewItemType = -1;

        if(this.viewType == TYPE_DATA_ONLY) {
            if(position == 0 && this.firstItemDifferent) {
                viewItemType = TYPE_DATA_ICON;
            } else {
                viewItemType = TYPE_DATA_ONLY;
            }
        } else if(this.viewType == TYPE_DATA_ICON) {
            if(position == 0 && this.firstItemDifferent) {
                viewItemType = TYPE_DATA_ONLY;
            } else {
                viewItemType = TYPE_DATA_ICON;
            }
        }

        return viewItemType;
    }

    @Override
    public SkeletonItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = null;
        if(viewType == TYPE_DATA_ONLY) {
            itemView = LayoutInflater.from(this.context).inflate(R.layout.layout_list_skeleton_data_only_item, parent, false);
        } else if(viewType == TYPE_DATA_ICON) {
            itemView = LayoutInflater.from(this.context).inflate(R.layout.layout_list_skeleton_data_icon_item, parent, false);
        }

        return new SkeletonItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(SkeletonItemViewHolder holder, int position) {
        ShimmerFrameLayout shimmerFrameLayout;
        if(holder.getItemViewType() == TYPE_DATA_ONLY) {
            shimmerFrameLayout = holder.getItemView().findViewById(R.id.shimmerLayoutDataOnly);
        } else {
            shimmerFrameLayout = holder.getItemView().findViewById(R.id.shimmerLayoutDataIcon);
        }

        if(shimmerFrameLayout != null) {
            shimmerFrameLayout.startShimmerAnimation();
        }
    }

    @Override
    public int getItemCount() {
        return this.itemsCount;
    }

    static class SkeletonItemViewHolder extends RecyclerView.ViewHolder {

        private View itemView;

        public SkeletonItemViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
        }

        public View getItemView() {
            return this.itemView;
        }
    }
}
