package de.mpfl.app.listeners;

import android.support.v7.widget.RecyclerView;

public interface OnRecyclerViewItemClickListener {

    void onItemClick(RecyclerView.ViewHolder viewHolder, int position);

}
