package de.mfpl.app.adapters;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import de.mfpl.app.R;
import de.mfpl.app.databinding.LayoutListOpencageResultItemBinding;
import de.mfpl.app.listeners.OnOpenCageResultClickListener;
import de.mfpl.app.listeners.OnRecyclerViewItemClickListener;
import de.mfpl.app.network.OpenCageResult;

public final class OpenCageResultListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements OnRecyclerViewItemClickListener {

    private Context context;
    private OnOpenCageResultClickListener openCageItemClickListener;
    private List<OpenCageResult> resultList;

    public OpenCageResultListAdapter(Context context, List<OpenCageResult> resultList) {
        this.context = context;
        this.resultList = resultList;
    }

    public void setOnOpenCageResultClickListener(OnOpenCageResultClickListener openCageResultClickListener) {
        this.openCageItemClickListener = openCageResultClickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        LayoutListOpencageResultItemBinding components = DataBindingUtil.inflate(LayoutInflater.from(viewGroup.getContext()), R.layout.layout_list_opencage_result_item, viewGroup, false);
        return new OpenCageResultItemViewHolder(components, this);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        OpenCageResult nominatimResult = this.resultList.get(position);
        OpenCageResultItemViewHolder nominatimResultItemViewHolder = (OpenCageResultItemViewHolder) viewHolder;

        if(nominatimResult != null) {
            String nominatimResultTitle = nominatimResult.toString();
            nominatimResultItemViewHolder.components.lblNominatimResultTitle.setText(nominatimResultTitle);

            if(nominatimResult.getComponents() != null) {
                String nominatimResultSubtitle = nominatimResult.getComponents().getState();
                nominatimResultItemViewHolder.components.lblNominatimResultSubtitle.setText(nominatimResultSubtitle);
            }
        }
    }

    @Override
    public int getItemCount() {
        return this.resultList.size();
    }

    @Override
    public void onItemClick(RecyclerView.ViewHolder viewHolder, int position) {
        if(this.openCageItemClickListener != null) {
            OpenCageResult openCageResult = this.resultList.get(position);
            this.openCageItemClickListener.onOpenCageResultClick(openCageResult);
        }
    }

    static class OpenCageResultItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public LayoutListOpencageResultItemBinding components;
        private OnRecyclerViewItemClickListener recyclerViewItemClickListener;

        public OpenCageResultItemViewHolder(LayoutListOpencageResultItemBinding components, OnRecyclerViewItemClickListener recyclerViewItemClickListener) {
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
