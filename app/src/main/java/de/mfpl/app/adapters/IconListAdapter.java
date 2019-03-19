package de.mfpl.app.adapters;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.DrawableRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

import de.mfpl.app.R;
import de.mfpl.app.databinding.LayoutListIconItemBinding;

public final class IconListAdapter extends BaseAdapter {

    private Context context;
    private List<IconListItem> itemList;

    public IconListAdapter(Context context, List<IconListItem> items) {
        this.context = context;
        this.itemList = items;
    }

    @Override
    public int getCount() {
        return this.itemList.size();
    }

    @Override
    public IconListItem getItem(int position) {
        return this.itemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(this.context).inflate(R.layout.layout_list_icon_item, parent, false);
        }

        IconListItem item = this.getItem(position);
        LayoutListIconItemBinding components = DataBindingUtil.bind(convertView);

        components.imgItemIcon.setImageDrawable(this.context.getDrawable(item.getIconDrawable()));
        components.lblItemTitle.setText(item.getTitle());

        if(item.getSubTitle() != null) {
            components.lblItemSubtitle.setVisibility(View.VISIBLE);
            components.lblItemSubtitle.setText(item.getSubTitle());
        } else {
            components.lblItemSubtitle.setVisibility(View.GONE);
        }

        return convertView;
    }

    public static class IconListItem {

        private String title;
        private String subTitle;
        private int iconDrawable;

        public IconListItem(String title) {
            this.title = title;
            this.subTitle = null;
            this.iconDrawable = 0;
        }

        public IconListItem(String title, String subTitle) {
            this.title = title;
            this.subTitle = subTitle;
            this.iconDrawable = 0;
        }

        public IconListItem(String title, String subTitle, @DrawableRes int iconDrawable) {
            this.title = title;
            this.subTitle = subTitle;
            this.iconDrawable = iconDrawable;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getSubTitle() {
            return subTitle;
        }

        public void setSubTitle(String subTitle) {
            this.subTitle = subTitle;
        }

        public int getIconDrawable() {
            return iconDrawable;
        }

        public void setIconDrawable(@DrawableRes int iconDrawable) {
            this.iconDrawable = iconDrawable;
        }

    }
}
