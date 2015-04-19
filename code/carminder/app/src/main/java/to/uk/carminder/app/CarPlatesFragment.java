package to.uk.carminder.app;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

import to.uk.carminder.app.data.EventContract;
import to.uk.carminder.app.data.StatusEvent;
import to.uk.carminder.app.data.adapter.CarSummaryAdapter;

public class CarPlatesFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int CAR_PLATES_LOADER = 0;

    private UserActionsFragment.NavigationDrawerCallbacks mCallbacks;
    private CarSummaryAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        final ListView view = (ListView) rootView.findViewById(R.id.list_car_details);
        view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mCallbacks.onNavigationDrawerItemSelected(adapter.getItem(position).getAsString(StatusEvent.FIELD_CAR_NUMBER));
            }
        });
        adapter = new CarSummaryAdapter(getActivity(), new ArrayList<StatusEvent>());
        view.setAdapter(adapter);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().restartLoader(CAR_PLATES_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (UserActionsFragment.NavigationDrawerCallbacks) activity;

        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onResume() {
        final Activity activity = getActivity();
        if (activity instanceof  MainActivity) {
            ((MainActivity) activity).onSectionAttached(getString(R.string.action_show_car_plates));
        }
        super.onResume();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(),
                                EventContract.StatusEntry.buildGroupByUri(EventContract.StatusEntry.COLUMN_CAR_NUMBER),
                                StatusEvent.COLUMNS_STATUS_ENTRY,
                                null,
                                null,
                                EventContract.StatusEntry.COLUMN_END_DATE + " ASC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.clear();
        adapter.addAll(StatusEvent.fromCursor(data, true));

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.clear();
    }
}
