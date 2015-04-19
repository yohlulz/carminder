package to.uk.carminder.app.data.adapter;

import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceFragment;
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

import to.uk.carminder.app.CarEventsFragment;
import to.uk.carminder.app.R;
import to.uk.carminder.app.Utility;
import to.uk.carminder.app.data.EventsContainer;
import to.uk.carminder.app.data.StatusEvent;
import to.uk.carminder.app.service.EventsManagementService;

public class CarEventsAdapter extends ArrayAdapter<StatusEvent> {
    private static final String LOG_TAG = CarEventsAdapter.class.getSimpleName();

    private FragmentCallbacks listener;

    public CarEventsAdapter(Context context, List<StatusEvent> events) {
        super(context, 0, events);
    }

    public void setFragmentListener(FragmentCallbacks listener) {
        this.listener = listener;
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
        holder.itemCarPlate.setText(event.getAsString(StatusEvent.FIELD_CAR_NUMBER));
        final String description = event.getAsString(StatusEvent.FIELD_DESCRIPTION);
        if (Utility.isStringNullOrEmpty(description)) {
            holder.itemDescription.setVisibility(View.GONE);
        } else {
            holder.itemDescription.setText(description);
            holder.itemDescription.setVisibility(View.VISIBLE);
        }
        holder.itemPickerMonth.setText(event.getExpireMonth());
        holder.itemPickerDay.setText(event.getExpireDay());
        holder.itemPickerYear.setText(event.getExpireYear());
        //TODO refactor this section to reuse the actual click listener and not add new ones at each call
        final boolean addToCalendar = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(getContext().getString(R.string.pref_key_calendar_events), Boolean.valueOf(getContext().getString(R.string.pref_default_calendar_events)));
        if (addToCalendar) {
            holder.dateView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.addEventToCalendar(event);
                    }
                }
            });

        } else {
            holder.dateView.setOnClickListener(null);
        }
        holder.btnEditEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.editEvent(event);
                }
            }
        });
        holder.btnDeleteEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.deleteEvent(event);
                }

            }
        });

        return convertView;
    }

    private static class ViewHolder {
        private TextView itemName;
        private TextView itemCarPlate;
        private TextView itemDescription;
        private TextView itemPickerMonth;
        private TextView itemPickerDay;
        private TextView itemPickerYear;
        private ImageView btnEditEvent;
        private ImageView btnDeleteEvent;
        private View dateView;


        public ViewHolder(View view) {
            itemName = (TextView) view.findViewById(R.id.item_name);
            itemCarPlate = (TextView) view.findViewById(R.id.item_car_plate);
            itemDescription = (TextView) view.findViewById(R.id.item_description);
            itemPickerDay = (TextView) view.findViewById(R.id.item_picker_day);
            itemPickerMonth = (TextView) view.findViewById(R.id.item_picker_month);
            itemPickerYear = (TextView) view.findViewById(R.id.item_picker_year);
            btnEditEvent = (ImageView) view.findViewById(R.id.btn_event_edit);
            btnDeleteEvent = (ImageView) view.findViewById(R.id.btn_event_delete);
            dateView = view.findViewById(R.id.item_picker);
        }
    }

    public static interface FragmentCallbacks {
        void addEventToCalendar(StatusEvent event);
        void editEvent(StatusEvent event);
        void deleteEvent(StatusEvent event);
    }
}
