package to.uk.carminder.app.data.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import java.util.List;

import to.uk.carminder.app.R;
import to.uk.carminder.app.Utility;
import to.uk.carminder.app.service.StatusEvent;

public class StatusEventAdapter extends ArrayAdapter<StatusEvent> {
    private static final String LOG_TAG = StatusEventAdapter.class.getSimpleName();

    public StatusEventAdapter(Context context, List<StatusEvent> events) {
        super(context, 0, events);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final StatusEvent existentItem = getItem(position);

        final ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.status_event_item, parent, false);
            holder.event_name = (TextView) convertView.findViewById(R.id.list_item_event_name);
            holder.event_start_day = (TextView) convertView.findViewById(R.id.list_item_start_day);
            holder.event_start_month = (TextView) convertView.findViewById(R.id.list_item_start_month);
            holder.event_end_day = (TextView) convertView.findViewById(R.id.list_item_end_day);
            holder.event_end_month = (TextView) convertView.findViewById(R.id.list_item_end_month);
            holder.event_description = (TextView) convertView.findViewById(R.id.list_item_event_description);

            holder.event_separator = (View) convertView.findViewById(R.id.list_item_status_separator);
            holder.event_dates = (LinearLayout) convertView.findViewById(R.id.list_item_date_status);

            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.event_name.setText(existentItem.getName());
        holder.event_description.setText(existentItem.getDescription());
        if (Utility.isStringNullOrEmpty(existentItem.getStartDate()) || Utility.isStringNullOrEmpty(existentItem.getExpireDate())) {
            holder.event_separator.setVisibility(View.GONE);
            holder.event_dates.setVisibility(View.GONE);

        } else {
            holder.event_start_day.setText(existentItem.getStartDay());
            holder.event_start_month.setText(existentItem.getStartMonth());
            holder.event_end_day.setText(existentItem.getExpireDay());
            holder.event_end_month.setText(existentItem.getExpireMonth());
        }

        return convertView;
    }

    private static class ViewHolder {
        TextView event_name;
        TextView event_start_day;
        TextView event_start_month;
        TextView event_end_day;
        TextView event_end_month;
        TextView event_description;

        LinearLayout event_dates;
        View event_separator;
    }
}
