package to.uk.carminder.app.data.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.media.Image;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import to.uk.carminder.app.R;
import to.uk.carminder.app.data.EventsContainer;
import to.uk.carminder.app.data.StatusEvent;
import to.uk.carminder.app.service.EventsManagementService;

public class CarSummaryAdapter extends ArrayAdapter<StatusEvent> {

    public CarSummaryAdapter(Context context, List<StatusEvent> events) {
        super(context, 0, events);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final StatusEvent event = getItem(position);
        final ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_car, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.carNumber.setText(event.getAsString(StatusEvent.FIELD_CAR_NUMBER));
        if (event.requiresAttention(getContext())) {
            holder.attentionView.setVisibility(View.VISIBLE);
            holder.carNumber.setTextColor(getContext().getResources().getColor(R.color.red_holo));
        } else {
            /* keep it in layout to preserve alignment */
            holder.attentionView.setVisibility(View.INVISIBLE);
        }
        holder.deleteView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getContext().startService(EventsManagementService.IntentBuilder.newInstance()
                                                    .command(EventsManagementService.COMMAND_DELETE_CAR)
                                                    .data(event)
                                                    .build(getContext()));
            }
        });

        return convertView;
    }


    private static class ViewHolder {
        private TextView carNumber;
        private ImageView attentionView;
        private ImageView deleteView;

        public ViewHolder(View view) {
            carNumber = (TextView) view.findViewById(R.id.list_item_view_car_number);
            attentionView = (ImageView) view.findViewById(R.id.list_item_attention);
            deleteView = (ImageView) view.findViewById(R.id.list_item_car_delete);
        }
    }
}
