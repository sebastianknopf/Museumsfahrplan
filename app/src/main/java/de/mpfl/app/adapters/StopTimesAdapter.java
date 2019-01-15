package de.mpfl.app.adapters;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Date;
import java.util.List;

import de.mfpl.staticnet.lib.data.StopTime;
import de.mpfl.app.R;
import de.mpfl.app.databinding.LayoutListStopTimeItemBinding;
import de.mpfl.app.utils.DateTimeFormat;

public final class StopTimesAdapter extends RecyclerView.Adapter<StopTimesAdapter.StopTimeItemViewHolder> {

    private final static int TYPE_TOP = 0;
    private final static int TYPE_MIDDLE = 1;
    private final static int TYPE_BOTTOM = 2;

    private Context context;
    private List<StopTime> stopTimeList;

    private int lineActiveColor = 0xFFFF0000;
    private int lineInactiveColor = 0xFFDDDDDD;

    private int pointActiveColor = 0xFFFF0000;
    private int pointInactiveColor = 0xFFDDDDDD;

    public StopTimesAdapter(Context context, List<StopTime> stopTimeList) {
        this.context = context;
        this.stopTimeList = stopTimeList;
    }

    @NonNull
    @Override
    public StopTimeItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        LayoutListStopTimeItemBinding components = DataBindingUtil.inflate(LayoutInflater.from(viewGroup.getContext()), R.layout.layout_list_stop_time_item, viewGroup, false);
        return new StopTimeItemViewHolder(components);
    }

    @Override
    public int getItemViewType(int position) {
        if(position == 0) {
            return TYPE_TOP;
        } else if(position == this.stopTimeList.size() - 1) {
            return TYPE_BOTTOM;
        } else {
            return TYPE_MIDDLE;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull StopTimeItemViewHolder viewHolder, int position) {
        StopTime stopTimeItem = this.stopTimeList.get(position);

        // set background for point
        GradientDrawable pointIndicator = this.createPointIndicator(position);
        viewHolder.components.viewPoint.setBackground(pointIndicator);

        // set background line for top, middle or bottom item
        GradientDrawable lineTopIndicator = this.createLineIndicator(position > 0 ? position - 1 : position);
        GradientDrawable lineBottomIndicator = this.createLineIndicator(position);
        switch(viewHolder.getItemViewType()) {
            case TYPE_TOP:
                viewHolder.components.viewLineTop.setBackground(null);
                viewHolder.components.viewLineBottom.setBackground(lineBottomIndicator);
                break;

            case TYPE_BOTTOM:
                viewHolder.components.viewLineTop.setBackground(lineTopIndicator);
                viewHolder.components.viewLineBottom.setBackground(null);
                break;

            default:
                viewHolder.components.viewLineTop.setBackground(lineTopIndicator);
                viewHolder.components.viewLineBottom.setBackground(lineBottomIndicator);;
                break;
        }

        // display stop time information
        if(stopTimeItem != null) {
            // arrival time - only if not the same like departure time!
            if(!stopTimeItem.getArrivalTime().equals(stopTimeItem.getDepartureTime())) {
                String arrivalString = DateTimeFormat.from(stopTimeItem.getArrivalTime(), DateTimeFormat.HHMMSS).to(DateTimeFormat.HHMM);
                viewHolder.components.lblStopArrival.setText(arrivalString);
            } else {
                viewHolder.components.lblStopArrival.setVisibility(View.GONE);
            }

            // departure time - always
            String departureString = DateTimeFormat.from(stopTimeItem.getDepartureTime(), DateTimeFormat.HHMMSS).to(DateTimeFormat.HHMM);
            viewHolder.components.lblStopDeparture.setText(departureString);

            // stop name
            String stopName = stopTimeItem.getStop().getStopName();
            viewHolder.components.lblStopName.setText(stopName);

            // stop schedule relationship type
            // todo: add realtime check here when relatime level II is starting, then execute this only, when schedule relationship is not 'cancelled'
            if(stopTimeItem.getPickupType() == StopTime.ChangeType.DEMAND || stopTimeItem.getDropOffType() == StopTime.ChangeType.DEMAND) {
                String scheduleInfoString = context.getString(R.string.str_stoptime_item_change_demand);

                viewHolder.components.layoutStopTimeScheduleInfo.setVisibility(View.VISIBLE);
                viewHolder.components.lblStopScheduleInfoText.setText(scheduleInfoString);
            } else {
                viewHolder.components.layoutStopTimeScheduleInfo.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return this.stopTimeList.size();
    }

    public void setLineActiveColor(int lineActiveColor) {
        this.lineActiveColor = lineActiveColor;
    }

    public void setLineInactiveColor(int lineInactiveColor) {
        this.lineInactiveColor = lineInactiveColor;
    }

    public void setPointActiveColor(int pointActiveColor) {
        this.pointActiveColor = pointActiveColor;
    }

    public void setPointInactiveColor(int pointInactiveColor) {
        this.pointInactiveColor = pointInactiveColor;
    }

    private GradientDrawable createLineIndicator(int position) {
        GradientDrawable resultDrawable = (GradientDrawable) context.getDrawable(R.drawable.shape_trip_details_line);
        StopTime stopTimeItem = this.stopTimeList.get(position);

        if(stopTimeItem != null) {
            Date stopDepartureDateTime = DateTimeFormat.from(stopTimeItem.getDepartureTime(), DateTimeFormat.HHMMSS).toDate();

            if(stopDepartureDateTime.before(new Date())) {
                resultDrawable.setColor(this.lineActiveColor);
            } else {
                resultDrawable.setColor(this.lineInactiveColor);
            }
        }

        return resultDrawable;
    }

    private GradientDrawable createPointIndicator(int position) {
        GradientDrawable resultDrawable = (GradientDrawable) context.getDrawable(R.drawable.shape_trip_details_point);

        StopTime stopTimeItem = this.stopTimeList.get(position);
        if(stopTimeItem != null) {
            Date stopDepartureDateTime = DateTimeFormat.from(stopTimeItem.getDepartureTime(), DateTimeFormat.HHMMSS).toDate();

            if(stopDepartureDateTime.before(new Date())) {
                resultDrawable.setColor(this.pointActiveColor);
            } else {
                resultDrawable.setColor(this.pointInactiveColor);
            }
        }

        return resultDrawable;
    }

    static class StopTimeItemViewHolder extends RecyclerView.ViewHolder {

        public LayoutListStopTimeItemBinding components;

        public StopTimeItemViewHolder(LayoutListStopTimeItemBinding components) {
            super(components.getRoot());
            this.components = components;
        }

    }
}
