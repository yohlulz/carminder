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

public class CarEventsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String ARG_CAR_PLATE = "car_plate";
    private static final String PREF_ADD_EVENTS_TO_CALENDAR = "pref_add_events_to_calendar";
    private static final int CAR_DETAILS_LOADER = 2;

    private CarEventsAdapter adapter;
    private boolean prefAddEventsToCalendar;

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

        } else {
            prefAddEventsToCalendar = addEventsToCalendar();
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
            final MainActivity mainActivity = (MainActivity) activity;
            mainActivity.onSectionAttached(getArguments().getString(ARG_CAR_PLATE));
            adapter.setFragmentListener(mainActivity);
        }
        super.onResume();
    }

    private boolean addEventsToCalendar() {
        return PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean(getActivity().getString(R.string.pref_key_calendar_events), Boolean.valueOf(getActivity().getString(R.string.pref_default_calendar_events)));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(PREF_ADD_EVENTS_TO_CALENDAR, prefAddEventsToCalendar);
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

}
