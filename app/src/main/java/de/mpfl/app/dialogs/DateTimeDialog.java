package de.mpfl.app.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import de.mpfl.app.R;
import de.mpfl.app.databinding.LayoutDialogDateTimeBinding;

public final class DateTimeDialog {

    private Context context;
    private LayoutDialogDateTimeBinding components;
    private OnDateTimeChangedListener onDateTimeChangedListener;

    public DateTimeDialog(Context context) {
        this.context = context;
        this.components = DataBindingUtil.inflate(LayoutInflater.from(this.context), R.layout.layout_dialog_date_time, null, false);
        this.components.clvSearchDate.setMinDate(new Date().getTime());

        // display time picker in 24h format
        this.components.tmpSearchTime.setIs24HourView(true);

        // add functionality for tab layout
        this.components.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            int selectedTabColor = ContextCompat.getColor(context, R.color.colorAccentDAY);
            int unselectedTabColor = Color.BLACK;

            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if(tab.getPosition() == 0) {
                    components.layoutSearchDate.setVisibility(View.VISIBLE);
                    components.layoutSearchTime.setVisibility(View.GONE);

                    components.tabLayout.getTabAt(0).getIcon().setColorFilter(selectedTabColor, PorterDuff.Mode.SRC_IN);
                    components.tabLayout.getTabAt(1).getIcon().setColorFilter(unselectedTabColor, PorterDuff.Mode.SRC_IN);
                } else {
                    components.layoutSearchTime.setVisibility(View.VISIBLE);
                    components.layoutSearchDate.setVisibility(View.GONE);

                    components.tabLayout.getTabAt(0).getIcon().setColorFilter(unselectedTabColor, PorterDuff.Mode.SRC_IN);
                    components.tabLayout.getTabAt(1).getIcon().setColorFilter(selectedTabColor, PorterDuff.Mode.SRC_IN);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        this.components.tabLayout.getTabAt(0).getIcon().setColorFilter(ContextCompat.getColor(context, R.color.colorAccentDAY), PorterDuff.Mode.SRC_IN);
    }

    public void setOnDateTimeChangedListener(OnDateTimeChangedListener onDateTimeChangedListener) {
        this.onDateTimeChangedListener = onDateTimeChangedListener;
    }

    public void setSearchDateTime(Date searchDateTime) {
        this.components.clvSearchDate.setDate(searchDateTime.getTime());

        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(searchDateTime);

        this.components.tmpSearchTime.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));
        this.components.tmpSearchTime.setCurrentMinute(calendar.get(Calendar.MINUTE));
    }

    public void show() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this.context);
        dialogBuilder.setView(this.components.getRoot());
        dialogBuilder.setNegativeButton(R.string.str_cancel, null);
        dialogBuilder.setPositiveButton(R.string.str_ok, (dialog, which) -> {
            if(this.onDateTimeChangedListener != null) {
                Date targetDate = new Date();
                targetDate.setTime(this.components.clvSearchDate.getDate());

                GregorianCalendar gregorianCalendar = new GregorianCalendar();
                gregorianCalendar.setTime(targetDate);
                gregorianCalendar.set(Calendar.HOUR_OF_DAY, this.components.tmpSearchTime.getCurrentHour());
                gregorianCalendar.set(Calendar.MINUTE, this.components.tmpSearchTime.getCurrentMinute());

                targetDate = gregorianCalendar.getTime();

                this.onDateTimeChangedListener.onDateTimeChanged(targetDate);
            }
        });

        AlertDialog dialog = dialogBuilder.create();
        dialog.show();
    }

    public interface OnDateTimeChangedListener {

        void onDateTimeChanged(Date date);

    }

}
