package to.uk.carminder.app;

import android.app.Activity;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.FrameLayout;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Date;

import to.uk.carminder.app.data.EventContract;
import to.uk.carminder.app.data.StatusEvent;
import to.uk.carminder.app.data.adapter.CarEventsAdapter;


public class MainActivity extends ActionBarActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private NavigationDrawerFragment mNavigationDrawerFragment;
    private CharSequence mTitle;
    private boolean isDrawerLocked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();
        final DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        isDrawerLocked = getResources().getBoolean(R.bool.lockDrawer);

        mNavigationDrawerFragment.setUp(R.id.navigation_drawer, drawerLayout, isDrawerLocked);
    }

    @Override
    public void onNavigationDrawerItemSelected(String carPlate) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(carPlate))
                .commit();
    }

    public void onSectionAttached(String carPlate) {
        mTitle = Utility.isStringNullOrEmpty(carPlate) ? getString(R.string.action_view_all_car_events) : carPlate;
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();

            SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_status));
            searchView.setSearchableInfo(searchManager.getSearchableInfo(new ComponentName(this, StatusActivity.class)));
            searchView.setIconifiedByDefault(true);

            return true;
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;

            case R.id.action_status:
                onSearchRequested();
                break;

            case R.id.action_clear_history:
                Utility.clearSearchHistory(this);
                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        /* close drawer if opened */
        if (mNavigationDrawerFragment.isDrawerOpen() && !isDrawerLocked) {
            mNavigationDrawerFragment.closeDrawer();
            return;
        }
        super.onBackPressed();
    }

    public static class PlaceholderFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
        private static final String ARG_CAR_PLATE = "car_plate";
        private static final String PREF_ADD_EVENTS_TO_CALENDAR = "pref_add_events_to_calendar";
        private static final int CAR_DETAILS_LOADER = 2;

        private CarEventsAdapter adapter;
        private boolean prefAddEventsToCalendar;

        public static PlaceholderFragment newInstance(String carPlate) {
            PlaceholderFragment fragment = new PlaceholderFragment();
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
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(getArguments().getString(ARG_CAR_PLATE));
        }

        @Override
        public void onResume() {
            boolean prefAddCalendarEvents = PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean(getActivity().getString(R.string.pref_key_calendar_events), Boolean.valueOf(getActivity().getString(R.string.pref_default_calendar_events)));
            if (prefAddCalendarEvents != this.prefAddEventsToCalendar) {
                getLoaderManager().restartLoader(CAR_DETAILS_LOADER, null, this);
                this.prefAddEventsToCalendar = prefAddCalendarEvents;
            }

            super.onResume();
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putBoolean(PREF_ADD_EVENTS_TO_CALENDAR, prefAddEventsToCalendar);
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            String carPlate = getArguments().getString(ARG_CAR_PLATE);
            carPlate = getString(R.string.action_view_all_car_events).equals(carPlate) ? null : carPlate;
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

}
