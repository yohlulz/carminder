package to.uk.carminder.app;

import android.content.Intent;
import android.database.Cursor;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import to.uk.carminder.app.data.EventContract;
import to.uk.carminder.app.data.adapter.CarEventsAdapter;
import to.uk.carminder.app.data.StatusEvent;
import to.uk.carminder.app.service.EventsModifierService;


public class CarEventsActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_events);
        if (savedInstanceState == null) {
            final CarEventsFragment fragment = new CarEventsFragment();
            //TODO extract field_data to utility
            final String carPlate = (getIntent() != null) ? getIntent().getStringExtra(EventsModifierService.FIELD_DATA) : null;
            if (!Utility.isStringNullOrEmpty(carPlate)) {
                final Bundle bundle = new Bundle();
                bundle.putString(EventsModifierService.FIELD_DATA, carPlate);
                fragment.setArguments(bundle);
            }

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.car_events, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    public static class CarEventsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
        private static final int STATUS_LOADER = 0;
        private CarEventsAdapter adapter;
        private EditText carNumberView;
        private String carPlate;

        public CarEventsFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_car_events, container, false);
            adapter = new CarEventsAdapter(getActivity(), null, 0);

            final ListView view = (ListView) rootView.findViewById(R.id.list_item_car_event);
            view.setAdapter(adapter);

            carNumberView = (EditText) rootView.findViewById(R.id.list_item_car_name);
            carPlate = getArguments() != null ? getArguments().getString(EventsModifierService.FIELD_DATA) : null;
            if (!Utility.isStringNullOrEmpty(carPlate)) {
                carNumberView.setText(carPlate);
            }
            adapter.setCarNumberView(carNumberView);

            rootView.findViewById(R.id.btn_events_cancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().onBackPressed();
                }
            });
            rootView.findViewById(R.id.btn_events_save).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Utility.notifyUser(getActivity(), "Eh");
                }
            });
            rootView.findViewById(R.id.list_item_add_new).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Utility.notifyUser(getActivity(), "Add new event");
                }
            });



            return rootView;
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            getLoaderManager().initLoader(STATUS_LOADER, null, this);
            super.onActivityCreated(savedInstanceState);
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return new CursorLoader(getActivity(),
                    EventContract.StatusEntry.buildStatusByCarPlateUri(carPlate),
                    StatusEvent.COLUMNS_STATUS_ENTRY,
                    null,
                    null,
                    null);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            //TODO merge data cursor with matrix cursor to be able to add data manually
            adapter.swapCursor(data);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            adapter.swapCursor(null);
        }

        @Override
        public void onDestroy() {
            final Cursor cursor = adapter.getCursor();
            if (cursor != null) {
                cursor.close();
            }
            super.onDestroy();
        }
    }
}
