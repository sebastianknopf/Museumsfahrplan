package de.mfpl.app.adapters;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Paint;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

import de.mfpl.app.R;
import de.mfpl.app.databinding.LayoutListAlertItemBinding;
import de.mfpl.staticnet.lib.data.Alert;

public final class AlertListAdapter extends BaseAdapter {

    private Context context;
    private List<Alert> alertList;

    public AlertListAdapter(Context context, List<Alert> alertList) {
        this.context = context;
        this.alertList = alertList;
    }

    @Override
    public int getCount() {
        return this.alertList.size();
    }

    @Override
    public Alert getItem(int position) {
        return this.alertList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(this.context).inflate(R.layout.layout_list_alert_item, parent, false);
        }

        Alert alert = this.getItem(position);
        LayoutListAlertItemBinding components = DataBindingUtil.bind(convertView);

        components.lblAlertTitle.setText(alert.getAlertHeader());

        if(!alert.getAlertDescription().equals("")) {
            components.lblAlertDescription.setVisibility(View.VISIBLE);
            components.lblAlertDescription.setText(alert.getAlertDescription());
        } else {
            components.lblAlertDescription.setVisibility(View.GONE);
        }

        if(!alert.getAlertUrl().equals("")) {
            String urlString = alert.getAlertUrl();
            if(!urlString.startsWith("http://") && !urlString.startsWith("https://")) {
                urlString = "http://" + urlString;
            }

            final String urlComponent = urlString;

            components.lblAlertUrl.setVisibility(View.VISIBLE);
            components.lblAlertUrl.setPaintFlags(components.lblAlertUrl.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            components.lblAlertUrl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Intent viewIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlComponent));
                        context.startActivity(viewIntent);
                    } catch (Exception e) {
                    }

                }
            });
        } else {
            components.lblAlertUrl.setVisibility(View.GONE);
        }

        return convertView;
    }
}
