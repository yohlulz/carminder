package to.uk.carminder.app.data.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;

import java.util.List;

import to.uk.carminder.app.R;
import to.uk.carminder.app.data.StatusEvent;

public class CarEventsAdapter extends ArrayAdapter<StatusEvent> {
    private static final String LOG_TAG = CarEventsAdapter.class.getSimpleName();

    private EditText carNumberView;
    private final boolean showCarPlate;

    public CarEventsAdapter(Context context, List<StatusEvent> events) {
        this(context, events, false);
    }

    public CarEventsAdapter(Context context, List<StatusEvent> events, boolean showCarPlate) {
        super(context, 0, events);
        this.showCarPlate = showCarPlate;
    }

    public void setCarNumberView(EditText carNumberView) {
        this.carNumberView = carNumberView;
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

        holder.itemName.setText(event.getAsString(StatusEvent.FIELD_NAME));
        holder.itemDescription.setText(showCarPlate ? event.getAsString(StatusEvent.FIELD_CAR_NUMBER) : event.getAsString(StatusEvent.FIELD_DESCRIPTION));
        holder.itemPickerMonth.setText(event.getExpireMonth());
        holder.itemPickerDay.setText(event.getExpireDay());
        holder.itemPickerYear.setText(event.getExpireYear());

        if (carNumberView != null) {
            carNumberView.setText(event.getAsString(StatusEvent.FIELD_CAR_NUMBER));
        }

        return convertView;
    }

    private static class ViewHolder {
        private TextView itemName;
        private TextView itemDescription;
        private TextView itemPickerMonth;
        private TextView itemPickerDay;
        private TextView itemPickerYear;

        public ViewHolder(View view) {
            itemName = (TextView) view.findViewById(R.id.item_name);
            itemDescription = (TextView) view.findViewById(R.id.item_description);
            itemPickerDay = (TextView) view.findViewById(R.id.item_picker_day);
            itemPickerMonth = (TextView) view.findViewById(R.id.item_picker_month);
            itemPickerYear = (TextView) view.findViewById(R.id.item_picker_year);
        }
    }
}
