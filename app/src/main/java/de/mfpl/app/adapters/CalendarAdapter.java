package de.mfpl.app.adapters;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import de.mfpl.app.R;
import de.mfpl.app.common.DateTimeFormat;
import de.mfpl.app.databinding.LayoutListCalendarItemBinding;
import de.mfpl.app.databinding.LayoutListDateItemBinding;
import de.mfpl.app.listeners.OnCalendarItemClickListener;
import de.mfpl.app.listeners.OnRecyclerViewItemClickListener;
import de.mfpl.staticnet.lib.data.Calendar;
import de.mfpl.staticnet.lib.data.Day;

public final class CalendarAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements OnRecyclerViewItemClickListener {

    private final static int TYPE_DATE = 0;
    private final static int TYPE_ROUTE = 1;

    private Context context;
    private OnCalendarItemClickListener calendarItemClickListener;
    private List<Object> items;

    public CalendarAdapter(Context context, Calendar calendar) {
        this.context = context;

        // create internal calendar list
        this.items = new ArrayList<Object>();
        for(Day currentDay : calendar.getDays()) {
            if(!this.items.contains(currentDay.getDate().substring(0, 6))) {
                this.items.add(currentDay.getDate().substring(0, 6));
            }

            this.items.add(currentDay);
        }
    }

    public void setOnCalendarItemClickListener(OnCalendarItemClickListener calendarItemClickListener) {
        this.calendarItemClickListener = calendarItemClickListener;
    }

    @Override
    public  int getItemViewType(int index) {
        if(this.items.get(index) instanceof String) {
            return TYPE_DATE;
        } else {
            return TYPE_ROUTE;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        if(viewType == TYPE_DATE) {
            LayoutListDateItemBinding components = DataBindingUtil.inflate(LayoutInflater.from(viewGroup.getContext()), R.layout.layout_list_date_item, viewGroup, false);
            return new DateItemViewHolder(components);
        } else {
            LayoutListCalendarItemBinding components = DataBindingUtil.inflate(LayoutInflater.from(viewGroup.getContext()), R.layout.layout_list_calendar_item, viewGroup, false);
            return new CalendarItemViewHolder(components, this);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        if(viewHolder.getItemViewType() == TYPE_DATE) {
            String dateString = (String) this.items.get(i);
            DateItemViewHolder dateItemViewHolder = (DateItemViewHolder) viewHolder;

            if(dateString != null) {
                String dateFromatted = DateTimeFormat.from(dateString, DateTimeFormat.YYYYMM).to(DateTimeFormat.MONTH_YYYY);
                dateItemViewHolder.components.lblDateFormatted.setText(dateFromatted);
            }
        } else {
            Day dayItem = (Day) this.items.get(i);
            CalendarItemViewHolder calendarItemViewHolder = (CalendarItemViewHolder) viewHolder;

            if(dayItem != null && dayItem.getRoute() != null) {
                // display realtime alerts assigned to this route
                if(dayItem.getRoute().getRealtime().hasAlerts() || dayItem.getRoute().getAgency().getRealtime().hasAlerts()) {
                    AlertListAdapter adapter = new AlertListAdapter(this.context, dayItem.getRoute().getRealtime().getAlerts());
                    calendarItemViewHolder.components.lstCalendarAlerts.setVisibility(View.VISIBLE);
                    calendarItemViewHolder.components.lstCalendarAlerts.setAdapter(adapter);
                } else {
                    calendarItemViewHolder.components.lstCalendarAlerts.setVisibility(View.GONE);
                }

                // display route name as calendar title
                String stringCalendarTitle = dayItem.getRoute().getRouteLongName();
                calendarItemViewHolder.components.lblCalendarTitle.setText(stringCalendarTitle);

                // display calendar date
                String stringCalendarDate = DateTimeFormat.from(dayItem.getDate(), DateTimeFormat.YYYYMMDD).to(DateTimeFormat.DDMMYYYY);
                calendarItemViewHolder.components.lblCalendarDate.setText(stringCalendarDate);

                // agency name
                if(dayItem.getRoute().getAgency() != null) {
                    String stringAgencyName = dayItem.getRoute().getAgency().getAgencyName();
                    calendarItemViewHolder.components.lblCalendarSubtitle.setVisibility(View.VISIBLE);
                    calendarItemViewHolder.components.lblCalendarSubtitle.setText(stringAgencyName);
                } else {
                    calendarItemViewHolder.components.lblCalendarSubtitle.setVisibility(View.GONE);
                }

                // route description below
                if(!dayItem.getRoute().getRouteDescription().equals("")) {
                    String stringRouteDescription = dayItem.getRoute().getRouteDescription();
                    calendarItemViewHolder.components.lblCalendarDescription.setVisibility(View.VISIBLE);
                    calendarItemViewHolder.components.lblCalendarDescription.setText(stringRouteDescription);
                } else {
                    calendarItemViewHolder.components.lblCalendarDescription.setVisibility(View.GONE);
                }

                // display information url - peferred url is route url if available
                String stringRouteInfoURL = null;
                if(dayItem.getRoute().getAgency() != null) {
                    if(!dayItem.getRoute().getAgency().getAgencyUrl().equals("")) {
                        stringRouteInfoURL = dayItem.getRoute().getAgency().getAgencyUrl();
                    }
                }

                if(!dayItem.getRoute().getRouteUrl().equals("")) {
                    stringRouteInfoURL = dayItem.getRoute().getRouteUrl();
                }

                if(stringRouteInfoURL != null) {
                    calendarItemViewHolder.components.lblInfoURL.setVisibility(View.VISIBLE);
                    calendarItemViewHolder.components.lblInfoURL.setText(stringRouteInfoURL.replace("http://", "").replace("https://", ""));
                } else {
                    calendarItemViewHolder.components.lblInfoURL.setVisibility(View.GONE);
                }

                // information phone number from agency object
                if(dayItem.getRoute().getAgency() != null) {
                    if (!dayItem.getRoute().getAgency().getAgencyPhone().equals("")) {
                        String routeInfoPhone = dayItem.getRoute().getAgency().getAgencyPhone();
                        calendarItemViewHolder.components.lblInfoPhone.setVisibility(View.VISIBLE);
                        calendarItemViewHolder.components.lblInfoPhone.setText(routeInfoPhone);
                    }
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return this.items.size();
    }

    @Override
    public void onItemClick(RecyclerView.ViewHolder viewHolder, int position) {
        if(this.calendarItemClickListener != null) {
            Day day = (Day) this.items.get(position);
            if(day != null) {
                this.calendarItemClickListener.onCalendarItemClick(day);
            }
        }
    }

    static class DateItemViewHolder extends RecyclerView.ViewHolder {

        public LayoutListDateItemBinding components;

        public DateItemViewHolder(LayoutListDateItemBinding components) {
            super(components.getRoot());
            this.components = components;
        }
    }

    static class CalendarItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public LayoutListCalendarItemBinding components;
        private OnRecyclerViewItemClickListener recyclerViewItemClickListener;

        public CalendarItemViewHolder(LayoutListCalendarItemBinding components, OnRecyclerViewItemClickListener recyclerViewItemClickListener) {
            super(components.getRoot());
            this.components = components;
            this.recyclerViewItemClickListener = recyclerViewItemClickListener;

            this.components.getRoot().setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(this.recyclerViewItemClickListener != null) {
                this.recyclerViewItemClickListener.onItemClick(this, this.getLayoutPosition());
            }
        }
    }
}
