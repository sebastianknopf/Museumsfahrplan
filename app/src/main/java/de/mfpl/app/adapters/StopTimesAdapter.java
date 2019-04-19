package de.mfpl.app.adapters;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Date;
import java.util.List;

import de.mfpl.api.lib.data.StopTime;
import de.mfpl.api.lib.data.StopTimeUpdate;
import de.mfpl.api.lib.data.Trip;
import de.mfpl.app.R;
import de.mfpl.app.common.DateTimeFormat;
import de.mfpl.app.databinding.LayoutListStopTimeItemBinding;

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

    private boolean isRealtimeTimesAvailable = false;

    public StopTimesAdapter(Context context, Trip trip) {
        this.context = context;

        this.stopTimeList = trip.getStopTimes();
        for(StopTime st : this.stopTimeList) {
            if(st.hasStopTimeUpdate()) {
                this.isRealtimeTimesAvailable = true;
                break;
            }
        }
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
            // display realtime area
            if(this.isRealtimeTimesAvailable) {
                viewHolder.components.layoutStopTimeRealtime.setVisibility(View.VISIBLE);
            }

            // arrival time - only if not the same like departure time!
            if(!stopTimeItem.getArrivalTime().equals(stopTimeItem.getDepartureTime())) {
                String arrivalString = DateTimeFormat.from(stopTimeItem.getArrivalTime(), DateTimeFormat.HHMMSS).to(DateTimeFormat.HHMM);
                viewHolder.components.lblStopArrival.setVisibility(View.VISIBLE);
                viewHolder.components.lblStopArrival.setText(arrivalString);

                if(stopTimeItem.hasStopTimeUpdate()) {
                    int arrivalDelay = stopTimeItem.getRealtime().getStopTimeUpdate().getArrivalDelay();
                    if(arrivalDelay < 4) {
                        viewHolder.components.lblStopArrivalDelay.setTextColor(ContextCompat.getColor(this.context, R.color.colorAccentDAY));
                    } else {
                        viewHolder.components.lblStopArrivalDelay.setTextColor(Color.RED);
                    }

                    viewHolder.components.lblStopArrivalDelay.setVisibility(View.VISIBLE);
                    viewHolder.components.lblStopArrivalDelay.setText(String.format("+ %d", arrivalDelay));
                } else {
                    viewHolder.components.lblStopArrivalDelay.setVisibility(View.GONE);
                }
            } else {
                viewHolder.components.lblStopArrival.setVisibility(View.GONE);
                viewHolder.components.lblStopArrivalDelay.setVisibility(View.GONE);
            }

            // departure time - always
            String departureString = DateTimeFormat.from(stopTimeItem.getDepartureTime(), DateTimeFormat.HHMMSS).to(DateTimeFormat.HHMM);
            viewHolder.components.lblStopDeparture.setText(departureString);

            if(stopTimeItem.hasStopTimeUpdate()) {
                int departureDelay = stopTimeItem.getRealtime().getStopTimeUpdate().getDepartureDelay();
                if(departureDelay < 4) {
                    viewHolder.components.lblStopDepartureDelay.setTextColor(ContextCompat.getColor(this.context, R.color.colorAccentDAY));
                } else {
                    viewHolder.components.lblStopDepartureDelay.setTextColor(Color.RED);
                }

                viewHolder.components.lblStopDepartureDelay.setText(String.format("+ %d", departureDelay));
            } else {
                viewHolder.components.lblStopDepartureDelay.setVisibility(View.GONE);
            }

            // stop name
            String stopName = stopTimeItem.getStop().getStopName();
            viewHolder.components.lblStopName.setText(stopName);

            // stop description
            String stopDesc = stopTimeItem.getStop().getStopDesc();
            if(!stopDesc.equals("")) {
                viewHolder.components.lblStopDescription.setVisibility(View.VISIBLE);
                viewHolder.components.lblStopDescription.setText(stopDesc);
            } else {
                viewHolder.components.lblStopDescription.setVisibility(View.GONE);
            }

            // stop schedule relationship type
            String scheduleInfoString = null;
            if(stopTimeItem.getRealtime() != null) {
                if(stopTimeItem.getRealtime().getStopTimeUpdate() != null && stopTimeItem.getRealtime().getStopTimeUpdate().getScheduleRelationship() == StopTimeUpdate.ScheduleRelationship.SKIPPED) {
                    scheduleInfoString = this.context.getString(R.string.str_stoptime_item_skipped);
                }
            }

            if(scheduleInfoString == null && (stopTimeItem.getPickupType() == StopTime.ChangeType.DEMAND || stopTimeItem.getDropOffType() == StopTime.ChangeType.DEMAND)) {
                scheduleInfoString = this.context.getString(R.string.str_stoptime_item_change_demand);
            }

            if(scheduleInfoString != null) {
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
