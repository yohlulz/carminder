package to.uk.carminder.app.data.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

import to.uk.carminder.app.R;

public class CarEventsAdapter extends CursorAdapter{
    private static final String LOG_TAG = CarEventsAdapter.class.getSimpleName();

    private View view;

    public CarEventsAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        view = LayoutInflater.from(context).inflate(R.layout.car_event_item, parent, false);
        view.setTag(new ViewHolder(view));

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        final ViewHolder holder = (ViewHolder) view.getTag();
    }

    private static class ViewHolder {

        public ViewHolder(View view) {
        }
    }
}
