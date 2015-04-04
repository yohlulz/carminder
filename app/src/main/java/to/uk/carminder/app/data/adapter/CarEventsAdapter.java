package to.uk.carminder.app.data.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import to.uk.carminder.app.R;
import to.uk.carminder.app.Utility;
import to.uk.carminder.app.data.StatusEvent;

public class CarEventsAdapter extends CursorAdapter{
    private static final String LOG_TAG = CarEventsAdapter.class.getSimpleName();

    private View view;
    private EditText carNumberView;

    public CarEventsAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(final Context context, Cursor cursor, ViewGroup parent) {
        view = LayoutInflater.from(context).inflate(R.layout.car_event_item, parent, false);
        view.setTag(new ViewHolder(view));

        view.findViewById(R.id.item_picker).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utility.notifyUser(context, "Pick date TODO");

            }
        });
        view.findViewById(R.id.item_name_container).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utility.notifyUser(context, "Modify event");
            }
        });

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        final ViewHolder holder = (ViewHolder) view.getTag();
        final StatusEvent event = StatusEvent.fromCursor(cursor);

        holder.itemName.setText(event.getAsString(StatusEvent.FIELD_NAME));
        holder.itemDescription.setText(event.getAsString(StatusEvent.FIELD_DESCRIPTION));
        holder.itemPickerMonth.setText(event.getExpireMonth());
        holder.itemPickerDay.setText(event.getExpireDay());
        holder.itemPickerYear.setText(event.getExpireYear());

        if (carNumberView != null) {
            carNumberView.setText(event.getAsString(StatusEvent.FIELD_CAR_NUMBER));
        }
    }

    public void setCarNumberView(EditText carNumberView) {
        this.carNumberView = carNumberView;
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
