package to.uk.carminder.app.data.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import java.util.List;

import to.uk.carminder.app.R;
import to.uk.carminder.app.data.CarEvent;

/**
 * Created by ovidiu on 3/21/15.
 */
public class EventStatusAdapter extends ArrayAdapter<CarEvent> {

    public EventStatusAdapter(Context context, List<CarEvent> events) {
        super(context, 0, events);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final CarEvent existentItem = getItem(position);

        final ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_event, parent, false);
            holder.event_name = (TextView) convertView.findViewById(R.id.list_item_event_name);
            holder.event_date = (TextView) convertView.findViewById(R.id.list_item_event_date);
            holder.event_month = (TextView) convertView.findViewById(R.id.list_item_event_month);
            holder.event_type = (TextView) convertView.findViewById(R.id.list_item_event_type);
            holder.event_alarm = (ImageView) convertView.findViewById(R.id.list_item_event_alarm);
            holder.event_calendar = (ImageView) convertView.findViewById(R.id.list_item_event_calendar);
            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.event_name.setText(existentItem.getName());
        holder.event_type.setText(existentItem.getDescription());
        holder.event_date.setText(existentItem.getExpireDate());
        holder.event_month.setText(existentItem.getExpireMonth());
        holder.event_calendar.setImageResource(R.drawable.ic_calendar);
        holder.event_alarm.setImageResource(R.drawable.ic_alarm);

        return convertView;
    }

    private static class ViewHolder {
        TextView event_name;
        TextView event_date;
        TextView event_month;
        TextView event_type;
        ImageView event_alarm;
        ImageView event_calendar;
    }
}
