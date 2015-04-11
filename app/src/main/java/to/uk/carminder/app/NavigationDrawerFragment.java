package to.uk.carminder.app;

import android.content.Intent;
import android.database.Cursor;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.app.Activity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

import to.uk.carminder.app.data.EventContract;
import to.uk.carminder.app.data.StatusEvent;
import to.uk.carminder.app.data.adapter.CarSummaryAdapter;
import to.uk.carminder.app.service.EventsManagementService;

public class NavigationDrawerFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String LOG_TAG = NavigationDrawerFragment.class.getSimpleName();

    private static final int POSITION_VIEW_ALL_EVENTS = -1;
    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";
    private static final String STATE_SELECTED_VALUE = "selected_navigation_drawer_value";
    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";
    private static final int CAR_SUMMARY_LOADER = 1;

    private NavigationDrawerCallbacks mCallbacks;

    private ActionBarDrawerToggle mDrawerToggle;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerListView;
    private View mFragmentContainerView;

    private int mCurrentSelectedPosition = POSITION_VIEW_ALL_EVENTS;
    private String mCurrentSelectdValue;
    private boolean mFromSavedInstanceState;
    private boolean mUserLearnedDrawer;

    private CarSummaryAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);

        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            mCurrentSelectdValue = savedInstanceState.getString(STATE_SELECTED_VALUE);
            mFromSavedInstanceState = true;
        }
        selectItem(mCurrentSelectedPosition, true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        getLoaderManager().restartLoader(CAR_SUMMARY_LOADER, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_navigation_drawer, container, false);
        adapter = new CarSummaryAdapter(getActivity(), new ArrayList<StatusEvent>());
        rootView.findViewById(R.id.list_item_car_view_all).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectItem(POSITION_VIEW_ALL_EVENTS, true);
            }
        });
        rootView.findViewById(R.id.btn_list_item_add_car).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), CarEventsActivity.class));
            }
        });
        mDrawerListView = (ListView) rootView.findViewById(R.id.list_car_entries);
        mDrawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectItem(position, true);
            }
        });
        registerForContextMenu(mDrawerListView);
        mDrawerListView.setAdapter(adapter);
        if (mCurrentSelectedPosition >= 0) {
            mDrawerListView.setItemChecked(mCurrentSelectedPosition, true);
        }
        return rootView;
    }

    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
    }

    public void setUp(int fragmentId, DrawerLayout drawerLayout) {
        mFragmentContainerView = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the navigation drawer and the action bar app icon.
        mDrawerToggle = new ActionBarDrawerToggle(
                getActivity(),                    /* host Activity */
                mDrawerLayout,                    /* DrawerLayout object */
                R.drawable.ic_drawer,             /* nav drawer image to replace 'Up' caret */
                R.string.navigation_drawer_open,  /* "open drawer" description for accessibility */
                R.string.navigation_drawer_close  /* "close drawer" description for accessibility */
        ) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (!isAdded()) {
                    return;
                }

                getActivity().supportInvalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (!isAdded()) {
                    return;
                }

                if (!mUserLearnedDrawer) {
                    // The user manually opened the drawer; store this flag to prevent auto-showing
                    // the navigation drawer automatically in the future.
                    mUserLearnedDrawer = true;
                    PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).apply();
                }

                getActivity().supportInvalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }
        };

        // If the user hasn't 'learned' about the drawer, open it to introduce them to the drawer,
        // per the navigation drawer design guidelines.
        if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
            mDrawerLayout.openDrawer(mFragmentContainerView);
        }

        // Defer code dependent on restoration of previous instance state.
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });

        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    private void selectItem(int position, boolean notifyCallback) {
        mCurrentSelectedPosition = position;
        if (mDrawerListView != null) {
            if (position >= 0) {
                mDrawerListView.setItemChecked(position, true);
            } else {
                mDrawerListView.clearChoices();
                mDrawerListView.requestLayout();
            }
        }
        closeDrawer();
        if (mCallbacks != null && notifyCallback) {
            mCurrentSelectdValue = (position >= 0) ?
                                        (adapter != null && adapter.getCount() > position) ? adapter.getItem(position).getAsString(StatusEvent.FIELD_CAR_NUMBER)
                                                                                                : mCurrentSelectdValue
                                            : getString(R.string.action_view_all_car_events);
            mCallbacks.onNavigationDrawerItemSelected(mCurrentSelectdValue);
        }
    }

    void closeDrawer() {
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mFragmentContainerView);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (NavigationDrawerCallbacks) activity;
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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
        outState.putString(STATE_SELECTED_VALUE, mCurrentSelectdValue);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (mDrawerLayout != null && isDrawerOpen()) {
            inflater.inflate(R.menu.global, menu);
            showGlobalContextActionBar();
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getActivity().getMenuInflater().inflate(R.menu.context_status_manage_item, menu);
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        final StatusEvent selectedEvent = adapter.getItem(info.position);

        switch (item.getItemId()) {
            case R.id.status_edit_event:
                final Intent editIntent = new Intent(getActivity(), CarEventsActivity.class);
                editIntent.putExtra(Utility.FIELD_DATA, selectedEvent.getAsString(StatusEvent.FIELD_CAR_NUMBER));
                startActivity(editIntent);
                break;

            case R.id.status_delete_event:
                adapter.remove(selectedEvent);
                adapter.notifyDataSetChanged();
                //TODO add reply listener and handle status code
                getActivity().startService(EventsManagementService.IntentBuilder.newInstance()
                                                                              .command(EventsManagementService.COMMAND_DELETE_CAR)
                                                                              .data(selectedEvent)
                                                                              .build(getActivity()));
                break;

            default:
                return super.onContextItemSelected(item);
        }
        return true;
    }

    private void showGlobalContextActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setTitle(R.string.app_name);
    }

    private ActionBar getActionBar() {
        return ((ActionBarActivity) getActivity()).getSupportActionBar();
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

    public static interface NavigationDrawerCallbacks {
        void onNavigationDrawerItemSelected(String carPlate);
    }
}
