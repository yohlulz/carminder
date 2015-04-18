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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Set;

import to.uk.carminder.app.data.EventContract;
import to.uk.carminder.app.data.EventsContainer;
import to.uk.carminder.app.data.adapter.CarEventsAdapter;
import to.uk.carminder.app.data.StatusEvent;
import to.uk.carminder.app.service.EventsManagementService;


public class CarEventsActivity extends ActionBarActivity {
    private static final String LOG_TAG = CarEventsActivity.class.getSimpleName();
    private static final String FIELD_DIALOG_SHOWN = "FIELD_DETAILS_DIALOG_SHOWN";
    private static final String FIELD_STATUS_EVENT_DETAILS = "FIELD_DETAILS_STATUS_EVENT";
    private static final String FIELD_CONTEXT_MENU_LEARNT = "FIELD_CONTEXT_MENU_LEARNT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_events);
        if (savedInstanceState == null) {
            final CarEventsFragment fragment = new CarEventsFragment();
            final String carPlate = (getIntent() != null) ? getIntent().getStringExtra(Utility.FIELD_DATA) : null;
            if (!Utility.isStringNullOrEmpty(carPlate)) {
                final Bundle bundle = new Bundle();
                bundle.putString(Utility.FIELD_DATA, carPlate);
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
        private final EventsContainer eventsContainer = new EventsContainer();
        private AlertDialog detailsDialog;
        private StatusEvent detailsEvent;
        private boolean detailsDialogShown;
        private boolean contextMenuLearnt;
        private ListView itemsView;

        @Override
        public View onCreateView(final LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_car_events, container, false);
            adapter = new CarEventsAdapter(getActivity(), new ArrayList<StatusEvent>());

            itemsView = (ListView) rootView.findViewById(R.id.list_item_car_event);
            itemsView.setAdapter(adapter);
            registerForContextMenu(itemsView);
            if (!contextMenuLearnt) {
                itemsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Utility.notifyUser(getActivity(), "Long click for options menu");
                    }
                });
            }

            carNumberView = (EditText) rootView.findViewById(R.id.list_item_car_name);
            carNumberView.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    carPlate = s.toString();
                }
            });
            carPlate = getArguments() != null ? getArguments().getString(Utility.FIELD_DATA) : null;
            if (!Utility.isStringNullOrEmpty(carPlate)) {
                carNumberView.setText(carPlate);
            }

            rootView.findViewById(R.id.btn_events_cancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /* discard any changes */
                    eventsContainer.clear();
                    getActivity().onNavigateUp();
                }
            });
            rootView.findViewById(R.id.btn_events_save).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    eventsContainer.ensureCarPlate(carPlate);
                    //TODO add listener and handle reply status code
                    getActivity().startService(EventsManagementService.IntentBuilder.newInstance()
                            .command(EventsManagementService.COMMAND_APPLY_FROM_CONTAINER)
                            .data(eventsContainer)
                            .build(getActivity()));
                    getActivity().onNavigateUp();

                }
            });
            rootView.findViewById(R.id.list_item_add_new).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDetailsView(savedInstanceState, null);
                }
            });

            return rootView;
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putParcelable(Utility.FIELD_CAR_EVENTS, eventsContainer);
            outState.putBoolean(FIELD_DIALOG_SHOWN, detailsDialogShown);
            outState.putParcelable(FIELD_STATUS_EVENT_DETAILS, detailsEvent);
            outState.putBoolean(FIELD_CONTEXT_MENU_LEARNT, contextMenuLearnt);
        }

        private void showDetailsView(final Bundle savedInstanceState, final StatusEvent event) {
            final View dialogView = getLayoutInflater(savedInstanceState).inflate(R.layout.manage_event_item, null);
            final EditText eventNameView = (EditText) dialogView.findViewById(R.id.edit_event_item_name);
            final EditText eventDescriptionView = (EditText) dialogView.findViewById(R.id.edit_event_item_description);
            final DatePicker datePicker = (DatePicker) dialogView.findViewById(R.id.edit_event_date_picker);
            final long today = Utility.getCurrentDate().getTime().getTime();
            datePicker.setMinDate(today);

            String field;
            if (event != null && !Utility.isStringNullOrEmpty(field = event.getAsString(StatusEvent.FIELD_NAME))) {
                eventNameView.setText(field);
            }
            if (event != null && !Utility.isStringNullOrEmpty(field = event.getAsString(StatusEvent.FIELD_DESCRIPTION))) {
                eventDescriptionView.setText(field);
            }
            if (event != null && event.getAsLong(StatusEvent.FIELD_END_DATE) != null) {
                final Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(event.getAsLong(StatusEvent.FIELD_END_DATE));
                datePicker.updateDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
            }

            detailsDialog = new AlertDialog.Builder(getActivity()).setTitle(R.string.title_dialog_manage_car_event)
                    .setIcon(R.drawable.car_launcher)
                    .setView(dialogView)
                    .setPositiveButton(R.string.action_add, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            final Long expireDate = Utility.parse(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
                            final String eventName = String.valueOf(eventNameView.getText());
                            final String eventDescription = String.valueOf(eventDescriptionView.getText());
                            if (Utility.isStringNullOrEmpty(eventName)) {
                                Utility.notifyUser(getActivity(), "Invalid event name, please retry");
                                return;
                            }
                            if (expireDate < today) {
                                Utility.notifyUser(getActivity(), "Invalid expire date, please retry");
                                return;
                            }
                            if (event != null) { // modify existing
                                eventsContainer.remove(event, false);

                                event.put(StatusEvent.FIELD_NAME, eventName);
                                event.put(StatusEvent.FIELD_DESCRIPTION, eventDescription);
                                event.put(StatusEvent.FIELD_END_DATE, expireDate);
                                eventsContainer.add(event, EventsContainer.EventState.MODIFIED);
                                adapter.notifyDataSetChanged();

                            } else { // add new
                                final StatusEvent addedEvent = new StatusEvent(eventName,
                                        expireDate, expireDate,
                                        null, /* car_number, null for now, will be populated at save request */
                                        eventDescription);
                                if (eventsContainer.add(addedEvent, EventsContainer.EventState.ADDED)) {
                                    adapter.add(addedEvent);
                                }
                            }
                            detailsDialogShown = false;
                            detailsEvent = null;
                        }
                    })
                    .setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            detailsDialog.dismiss();
                            detailsDialog = null;
                            detailsDialogShown = false;
                            detailsEvent = null;

                        }
                    })
                    .create();
            detailsDialogShown = true;
            detailsEvent = event;
            detailsDialog.show();
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            getActivity().getMenuInflater().inflate(R.menu.context_status_manage_item, menu);
            super.onCreateContextMenu(menu, v, menuInfo);
        }

        @Override
        public boolean onContextItemSelected(MenuItem item) {
            contextMenuLearnt = true;
            itemsView.setOnItemClickListener(null);
            final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

            switch (item.getItemId()) {
                case R.id.status_edit_event:
                    showDetailsView(null, adapter.getItem(info.position));
                    break;

                case R.id.status_delete_event:
                    final StatusEvent deleteEvent = adapter.getItem(info.position);
                    eventsContainer.remove(deleteEvent, true);
                    adapter.remove(deleteEvent);
                    adapter.notifyDataSetChanged();
                    break;

                default:
                    return super.onContextItemSelected(item);
            }
            return true;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (savedInstanceState != null) {
                contextMenuLearnt = savedInstanceState.getBoolean(FIELD_CONTEXT_MENU_LEARNT, false);
                this.eventsContainer.update((EventsContainer) savedInstanceState.getParcelable(Utility.FIELD_CAR_EVENTS));
                if (savedInstanceState.getBoolean(FIELD_DIALOG_SHOWN, false)) {
                    showDetailsView(savedInstanceState, (StatusEvent) savedInstanceState.getParcelable(FIELD_STATUS_EVENT_DETAILS));
                }
            }
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            getLoaderManager().initLoader(STATUS_LOADER, null, this);
            super.onActivityCreated(savedInstanceState);
        }

        @Override
        public void onResume() {
            if (detailsDialog != null) {
                detailsDialog.show();
            }
            super.onResume();
        }

        @Override
        public void onPause() {
            if (detailsDialog != null) {
                detailsDialog.dismiss();
            }
            super.onPause();
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
            for (StatusEvent event : events) {
                eventsContainer.add(event, EventsContainer.EventState.UNCHANGED);
            }

            adapter.clear();
            adapter.addAll(eventsContainer.getAllEvents());

        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            adapter.clear();
        }
    }
}


