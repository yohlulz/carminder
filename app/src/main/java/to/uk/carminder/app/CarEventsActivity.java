package to.uk.carminder.app;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import to.uk.carminder.app.data.EventContract;
import to.uk.carminder.app.data.adapter.CarEventsAdapter;
import to.uk.carminder.app.data.StatusEvent;
import to.uk.carminder.app.service.EventsModifierService;


public class CarEventsActivity extends ActionBarActivity {
    private static final String LOG_TAG = CarEventsActivity.class.getSimpleName();

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
        private final ConcurrentMap<StatusEvent, EventState> eventToState = new ConcurrentHashMap<>();

        public CarEventsFragment() {
        }

        @Override
        public View onCreateView(final LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_car_events, container, false);
            adapter = new CarEventsAdapter(getActivity(), new ArrayList<StatusEvent>());

            final ListView view = (ListView) rootView.findViewById(R.id.list_item_car_event);
            view.setAdapter(adapter);
            registerForContextMenu(view);

            carNumberView = (EditText) rootView.findViewById(R.id.list_item_car_name);
            carPlate = getArguments() != null ? getArguments().getString(EventsModifierService.FIELD_DATA) : null;
            if (!Utility.isStringNullOrEmpty(carPlate)) {
                carNumberView.setText(carPlate);
            }
            adapter.setCarNumberView(carNumberView);

            rootView.findViewById(R.id.btn_events_cancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /* discard any changes */
                    eventToState.clear();
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
                    new AlertDialog.Builder(getActivity()).setTitle(R.string.title_dialog_manage_car_event)
                            .setIcon(R.drawable.car_launcher)
                            .setView(getLayoutInflater(savedInstanceState).inflate(R.layout.manage_event_item, null))
                            .setPositiveButton(R.string.action_save, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Utility.notifyUser(getActivity(), "Save");
                                }
                            })
                            .setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .create()
                            .show();
                }
            });



            return rootView;
        }

        //TODO show dialog for new/ edit
        //TODO manage calendar events

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            getActivity().getMenuInflater().inflate(R.menu.context_status_manage_item, menu);
            super.onCreateContextMenu(menu, v, menuInfo);
        }

        @Override
        public boolean onContextItemSelected(MenuItem item) {
            final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

            switch (item.getItemId()) {
                case R.id.status_edit_event:
                    final StatusEvent editEvent = adapter.getItem(info.position);
                    eventToState.put(editEvent, EventState.MODIFIED);
                    //TODO add functionality
                    Utility.notifyUser(getActivity(), "Edit " + editEvent.getContentValues());
                    break;

                case R.id.status_delete_event:
                    final StatusEvent deleteEvent = adapter.getItem(info.position);
                    final EventState state = eventToState.get(deleteEvent);
                    /* event not yet saved to DB, just remove it from structures */
                    if (state == EventState.ADDED) {
                        eventToState.remove(deleteEvent);
                    } else { // mark it for deletion
                        eventToState.put(deleteEvent, EventState.DELETED);
                    }
                    adapter.remove(deleteEvent);
                    adapter.notifyDataSetChanged();
                    break;

                default:
                    Log.w(LOG_TAG, "Unknown context id " + item.getItemId());
            }
            return super.onContextItemSelected(item);
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
            final Set<StatusEvent> events = StatusEvent.fromCursor(data);
            data.close();
            adapter.addAll(events);
            for (StatusEvent event : events) {
                eventToState.put(event, EventState.UNCHANGED);
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            adapter.clear();
        }
    }

    private static enum EventState {
        ADDED, MODIFIED, DELETED, UNCHANGED;
    }
}
