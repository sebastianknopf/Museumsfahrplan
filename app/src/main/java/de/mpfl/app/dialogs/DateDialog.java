package de.mpfl.app.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.view.LayoutInflater;

import java.util.Date;
import java.util.GregorianCalendar;

import de.mpfl.app.R;
import de.mpfl.app.databinding.LayoutDialogDateBinding;

public final class DateDialog {

    private Context context;
    private LayoutDialogDateBinding components;
    private OnDateChangedListener onDateChangedListener;

    public DateDialog(Context context) {
        this.context = context;
        this.components = DataBindingUtil.inflate(LayoutInflater.from(this.context), R.layout.layout_dialog_date, null, false);
        this.components.clvSearchDate.setMinDate(new Date().getTime());
    }

    public void setOnDateChangedListener(OnDateChangedListener onDateChangedListener) {
        this.onDateChangedListener = onDateChangedListener;
    }

    public void setSearchDate(Date searchDate) {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(searchDate);

        this.components.clvSearchDate.setDate(searchDate.getTime());
    }

    public void show() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this.context);
        dialogBuilder.setView(this.components.getRoot());
        dialogBuilder.setNegativeButton(R.string.str_cancel, null);
        dialogBuilder.setPositiveButton(R.string.str_ok, (dialog, which) -> {
            if(this.onDateChangedListener != null) {
                Date targetDate = new Date();
                targetDate.setTime(this.components.clvSearchDate.getDate());

                this.onDateChangedListener.onDateChanged(targetDate);
            }
        });

        AlertDialog dialog = dialogBuilder.create();
        dialog.show();
    }

    public interface OnDateChangedListener {

        void onDateChanged(Date date);

    }

}
