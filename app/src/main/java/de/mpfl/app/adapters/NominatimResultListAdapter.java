package de.mpfl.app.adapters;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import de.mpfl.app.R;
import de.mpfl.app.databinding.LayoutListNominatimResultItemBinding;
import de.mpfl.app.listeners.OnNominatimResultClickListener;
import de.mpfl.app.listeners.OnRecyclerViewItemClickListener;
import de.mpfl.app.network.NominatimResult;

public final class NominatimResultListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements OnRecyclerViewItemClickListener {

    private Context context;
    private OnNominatimResultClickListener nominatimItemClickListener;
    private List<NominatimResult> resultList;

    public NominatimResultListAdapter(Context context, List<NominatimResult> resultList) {
        this.context = context;
        this.resultList = resultList;
    }

    public void setOnNominatimResultClickListener(OnNominatimResultClickListener nominatimResultClickListener) {
        this.nominatimItemClickListener = nominatimResultClickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        LayoutListNominatimResultItemBinding components = DataBindingUtil.inflate(LayoutInflater.from(viewGroup.getContext()), R.layout.layout_list_nominatim_result_item, viewGroup, false);
        return new NominatimResultItemViewHolder(components, this);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        NominatimResult nominatimResult = this.resultList.get(position);
        NominatimResultItemViewHolder nominatimResultItemViewHolder = (NominatimResultItemViewHolder) viewHolder;

        if(nominatimResult != null) {
            String nominatimResultTitle = nominatimResult.getName();
            nominatimResultItemViewHolder.components.lblNominatimResultTitle.setText(nominatimResultTitle);

            String nominatimResultSubtitle = nominatimResult.getState();
            nominatimResultItemViewHolder.components.lblNominatimResultSubtitle.setText(nominatimResultSubtitle);
        }
    }

    @Override
    public int getItemCount() {
        return this.resultList.size();
    }

    @Override
    public void onItemClick(RecyclerView.ViewHolder viewHolder, int position) {
        if(this.nominatimItemClickListener != null) {
            NominatimResult nominatimResult = this.resultList.get(position);
            this.nominatimItemClickListener.onNominatimResultClick(nominatimResult);
        }
    }

    static class NominatimResultItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public LayoutListNominatimResultItemBinding components;
        private OnRecyclerViewItemClickListener recyclerViewItemClickListener;

        public NominatimResultItemViewHolder(LayoutListNominatimResultItemBinding components, OnRecyclerViewItemClickListener recyclerViewItemClickListener) {
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
