package to.uk.carminder.app.data.adapter;

import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Calendar;
import java.util.List;

import to.uk.carminder.app.R;
import to.uk.carminder.app.Utility;
import to.uk.carminder.app.data.StatusEvent;

public class CarEventsAdapter extends ArrayAdapter<StatusEvent> {
    private static final String LOG_TAG = CarEventsAdapter.class.getSimpleName();

    private final boolean showCarPlate;

    public CarEventsAdapter(Context context, List<StatusEvent> events) {
        this(context, events, false);
    }

    public CarEventsAdapter(Context context, List<StatusEvent> events, boolean showCarPlate) {
        super(context, 0, events);
        this.showCarPlate = showCarPlate;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final StatusEvent event = getItem(position);
        final ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.car_event_item, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (event.requiresAttention(getContext())) {
            final View container = convertView.findViewById(R.id.car_event_item_container);
            container.setBackgroundColor(getContext().getResources().getColor(R.color.red_holo));
            container.getBackground().setAlpha(100);
        }
        holder.itemName.setText(event.getAsString(StatusEvent.FIELD_NAME));
        final String description = showCarPlate ? event.getAsString(StatusEvent.FIELD_CAR_NUMBER) : event.getAsString(StatusEvent.FIELD_DESCRIPTION);
        if (Utility.isStringNullOrEmpty(description)) {
            holder.itemDescription.setVisibility(View.GONE);
        } else {
            holder.itemDescription.setText(description);
            holder.itemDescription.setVisibility(View.VISIBLE);
        }
        holder.itemPickerMonth.setText(event.getExpireMonth());
        holder.itemPickerDay.setText(event.getExpireDay());
        holder.itemPickerYear.setText(event.getExpireYear());
        boolean prefAddCalendarEvents = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(getContext().getString(R.string.pref_key_calendar_events), Boolean.valueOf(getContext().getString(R.string.pref_default_calendar_events)));
        if (showCarPlate && prefAddCalendarEvents) {
            holder.btnAddToCalendar.setClickable(true);
            holder.btnAddToCalendar.setVisibility(View.VISIBLE);
            holder.btnAddToCalendar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addEventToCalendar(event);
                }
            });
        } else {
            holder.btnAddToCalendar.setClickable(false);
            holder.btnAddToCalendar.setVisibility(View.GONE);
            holder.btnAddToCalendar.setOnClickListener(null);
        }


        return convertView;
    }

    private void addEventToCalendar(StatusEvent event) {
        final Intent intent = new Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, event.getAsLong(StatusEvent.FIELD_END_DATE))
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, event.getAsLong(StatusEvent.FIELD_END_DATE))
                .putExtra(CalendarContract.Events.TITLE, String.format("Expires %s for %s", event.getAsString(StatusEvent.FIELD_NAME), event.getAsString(StatusEvent.FIELD_CAR_NUMBER)))
                .putExtra(CalendarContract.Events.DESCRIPTION, event.getAsString(StatusEvent.FIELD_DESCRIPTION))
                .putExtra(CalendarContract.Events.ALL_DAY, true);

        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            getContext().startActivity(Intent.createChooser(intent, "Choose app"));
        } else {
            Utility.notifyUser(getContext(), "There is no activity found for adding calendar events.");
        }
    }

    private static class ViewHolder {
        private TextView itemName;
        private TextView itemDescription;
        private TextView itemPickerMonth;
        private TextView itemPickerDay;
        private TextView itemPickerYear;
        private ImageView btnAddToCalendar;

        public ViewHolder(View view) {
            itemName = (TextView) view.findViewById(R.id.item_name);
            itemDescription = (TextView) view.findViewById(R.id.item_description);
            itemPickerDay = (TextView) view.findViewById(R.id.item_picker_day);
            itemPickerMonth = (TextView) view.findViewById(R.id.item_picker_month);
            itemPickerYear = (TextView) view.findViewById(R.id.item_picker_year);
            btnAddToCalendar = (ImageView) view.findViewById(R.id.btn_add_to_calendar);
        }
    }
}
