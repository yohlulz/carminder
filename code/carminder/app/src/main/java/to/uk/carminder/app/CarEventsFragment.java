package to.uk.carminder.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Calendar;

import to.uk.carminder.app.data.EventContract;
import to.uk.carminder.app.data.EventsContainer;
import to.uk.carminder.app.data.StatusEvent;
import to.uk.carminder.app.data.adapter.CarEventsAdapter;
import to.uk.carminder.app.service.EventsManagementService;

public class CarEventsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, CarEventsAdapter.FragmentCallbacks {
    private static final String ARG_CAR_PLATE = "car_plate";
    private static final String PREF_ADD_EVENTS_TO_CALENDAR = "pref_add_events_to_calendar";
    private static final String PREF_EVENT_STATUS = "pref_modify_event_status";
    private static final String PREF_EVENT_STATE = "pref_event_state";
    private static final int CAR_DETAILS_LOADER = 2;

    private CarEventsAdapter adapter;
    private boolean prefAddEventsToCalendar;
    private StatusEvent event;
    private EventsContainer.EventState state;
    private AlertDialog editDialog;

    public static CarEventsFragment newInstance(String carPlate) {
        CarEventsFragment fragment = new CarEventsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CAR_PLATE, carPlate);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            prefAddEventsToCalendar = savedInstanceState.getBoolean(PREF_ADD_EVENTS_TO_CALENDAR);
            event = savedInstanceState.getParcelable(PREF_EVENT_STATUS);
            state = (EventsContainer.EventState) savedInstanceState.getSerializable(PREF_EVENT_STATE);

        } else {
            prefAddEventsToCalendar = addEventsToCalendar();
        }

        if (event != null) {
            showEventDialog(savedInstanceState, event);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().restartLoader(CAR_DETAILS_LOADER, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        final ListView view = (ListView) rootView.findViewById(R.id.list_car_details);
        adapter = new CarEventsAdapter(getActivity(), new ArrayList<StatusEvent>(), true);
        adapter.setFragmentListener(this);
        view.setAdapter(adapter);

        return rootView;
    }

    @Override
    public void onResume() {
        boolean prefAddCalendarEvents = addEventsToCalendar();
        if (prefAddCalendarEvents != this.prefAddEventsToCalendar) {
            if (prefAddCalendarEvents) {
                Utility.notifyUser(getActivity(), "Press on event's date to add it to calendar");
            }
            this.prefAddEventsToCalendar = prefAddCalendarEvents;
            getLoaderManager().restartLoader(CAR_DETAILS_LOADER, null, this);
        }

        final Activity activity = getActivity();
        if (activity instanceof  MainActivity) {
            ((MainActivity) activity).onSectionAttached(getArguments().getString(ARG_CAR_PLATE));
        }
        super.onResume();
        if (editDialog != null && event != null) {
            editDialog.show();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (editDialog != null) {
            editDialog.dismiss();
        }
    }

    private boolean addEventsToCalendar() {
        return PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean(getActivity().getString(R.string.pref_key_calendar_events), Boolean.valueOf(getActivity().getString(R.string.pref_default_calendar_events)));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(PREF_ADD_EVENTS_TO_CALENDAR, prefAddEventsToCalendar);
        outState.putParcelable(PREF_EVENT_STATUS, event);
        outState.putSerializable(PREF_EVENT_STATE, state);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String carPlate = getArguments().getString(ARG_CAR_PLATE);
        carPlate = getString(R.string.action_show_all_car_events).equals(carPlate) ? null : carPlate;
        return new CursorLoader(getActivity(),
                (carPlate != null) ? EventContract.StatusEntry.buildStatusByCarPlateUri(carPlate) : EventContract.StatusEntry.CONTENT_URI,
                StatusEvent.COLUMNS_STATUS_ENTRY,
                null,
                null,
                EventContract.StatusEntry.COLUMN_END_DATE + " ASC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.clear();
        adapter.addAll(StatusEvent.fromCursor(data));
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.clear();
    }

    @Override
    public void addEventToCalendar(StatusEvent event) {
        final Intent intent = new Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, event.getAsLong(StatusEvent.FIELD_END_DATE))
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, event.getAsLong(StatusEvent.FIELD_END_DATE))
                .putExtra(CalendarContract.Events.TITLE, String.format("Expires %s for %s", event.getAsString(StatusEvent.FIELD_NAME), event.getAsString(StatusEvent.FIELD_CAR_NUMBER)))
                .putExtra(CalendarContract.Events.DESCRIPTION, event.getAsString(StatusEvent.FIELD_DESCRIPTION))
                .putExtra(CalendarContract.Events.ALL_DAY, true);

        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            getActivity().startActivity(Intent.createChooser(intent, "Choose app"));

        } else {
            Utility.notifyUser(getActivity(), "There is no activity found for adding calendar events.");
        }

    }

    @Override
    public void editEvent(StatusEvent event) {
        state = EventsContainer.EventState.MODIFIED;
        showEventDialog(null, event);

    }

    @Override
    public void deleteEvent(StatusEvent event) {
        final EventsContainer container = new EventsContainer();
        container.add(event, EventsContainer.EventState.DELETED);
        getActivity().startService(EventsManagementService.IntentBuilder.newInstance()
                .command(EventsManagementService.COMMAND_APPLY_FROM_CONTAINER)
                .data(container)
                .build(getActivity()));
    }

    private void saveModifiedEvent(EventsContainer container) {
        getActivity().startService(EventsManagementService.IntentBuilder.newInstance()
                .command(EventsManagementService.COMMAND_APPLY_FROM_CONTAINER)
                .data(container)
                .build(getActivity()));
    }

    private void showEventDialog(final Bundle savedInstanceState, final StatusEvent editEvent) {
        if (editDialog != null) {
            editDialog.dismiss();
        }
        this.event = editEvent;
        final View dialogView = getLayoutInflater(savedInstanceState).inflate(R.layout.manage_event_item, null);
        final EditText carNumberView = (EditText) dialogView.findViewById(R.id.edit_event_car_number);
        final EditText eventNameView = (EditText) dialogView.findViewById(R.id.edit_event_item_name);
        final EditText eventDescriptionView = (EditText) dialogView.findViewById(R.id.edit_event_item_description);
        final DatePicker datePicker = (DatePicker) dialogView.findViewById(R.id.edit_event_date_picker);
        final long today = Utility.getCurrentDate().getTime().getTime();
        datePicker.setMinDate(today);

        String field;
        if (event != null && !Utility.isStringNullOrEmpty(field = event.getAsString(StatusEvent.FIELD_CAR_NUMBER))) {
            carNumberView.setText(field);
        }
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

        editDialog = new AlertDialog.Builder(getActivity())
                                            .setTitle(EventsContainer.EventState.MODIFIED == state ? R.string.action_add_event : R.string.action_modify_event)
                                            .setIcon(R.drawable.car_launcher)
                                            .setView(dialogView)
                                            .setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    event = null;
                                                    state = null;
                                                    editDialog.dismiss();
                                                }
                                            })
                                            .setPositiveButton(R.string.action_save, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    if (!isAdded()) {
                                                        showEventDialog(savedInstanceState, editEvent);
                                                    }

                                                    final Long expireDate = Utility.parse(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
                                                    final String carPlate = String.valueOf(carNumberView.getText());
                                                    final String eventName = String.valueOf(eventNameView.getText());
                                                    final String eventDescription = String.valueOf(eventDescriptionView.getText());
                                                    if (Utility.isStringNullOrEmpty(carPlate)) {
                                                        Utility.notifyUser(getActivity(), "Invalid car plate, please retry");
                                                        return;
                                                    }
                                                    if (Utility.isStringNullOrEmpty(eventName)) {
                                                        Utility.notifyUser(getActivity(), "Invalid event name, please retry");
                                                        return;
                                                    }
                                                    if (expireDate < today) {
                                                        Utility.notifyUser(getActivity(), "Invalid expire date, please retry");
                                                        return;
                                                    }
                                                    event.put(StatusEvent.FIELD_CAR_NUMBER, carPlate);
                                                    event.put(StatusEvent.FIELD_NAME, eventName);
                                                    event.put(StatusEvent.FIELD_DESCRIPTION, eventDescription);
                                                    event.put(StatusEvent.FIELD_END_DATE, expireDate);

                                                    final EventsContainer container = new EventsContainer();
                                                    container.add(event, state);
                                                    saveModifiedEvent(container);

                                                    event = null;
                                                    state = null;
                                                    dialog.dismiss();
                                                }
                                            })
                                            .create();
        editDialog.show();

    }
}
