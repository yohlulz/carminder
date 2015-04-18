package to.uk.carminder.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Date;

import to.uk.carminder.app.data.EventsContainer;
import to.uk.carminder.app.data.StatusEvent;
import to.uk.carminder.app.service.CheckStatusService;
import to.uk.carminder.app.service.EventsManagementService;

public class UserActionsFragment extends Fragment {
    private static final String LOG_TAG = UserActionsFragment.class.getSimpleName();

    private static final String STATE_SELECTED_VALUE = "selected_navigation_drawer_value";
    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";
    private static final String STATE_STATUS_EVENT = "status_event_shown_in_dialog";
    private static final String STATE_VALIDATE_CAR_PLATE = "validate_dialog_car_plate";
    private static final String STATE_VALIDATE_SHOW_DIALOG = "validate_show_dialog";

    private NavigationDrawerCallbacks mCallbacks;

    private ActionBarDrawerToggle mDrawerToggle;

    private DrawerLayout mDrawerLayout;
    private View mFragmentContainerView;

    private String mCurrentSelectedValue;

    private boolean mFromSavedInstanceState;
    private boolean mUserLearnedDrawer;

    private boolean isDrawerLocked;

    private AlertDialog statusDialog;
    private StatusEvent statusEvent;
    private AlertDialog validateDialog;
    private String validateCarPlate;
    private boolean showValidateDialog;
    private StatusReceiver receiver;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);

        if (savedInstanceState != null) {
            mCurrentSelectedValue = savedInstanceState.getString(STATE_SELECTED_VALUE);
            statusEvent = savedInstanceState.getParcelable(STATE_STATUS_EVENT);
            validateCarPlate = savedInstanceState.getString(STATE_VALIDATE_CAR_PLATE);
            showValidateDialog = savedInstanceState.getBoolean(STATE_VALIDATE_SHOW_DIALOG);
            mFromSavedInstanceState = true;
        }
        selectAction(mCurrentSelectedValue);
        if (statusEvent != null) {
            showStatusDialog(savedInstanceState, statusEvent);
        }
        if (showValidateDialog) {
            showValidateDialog(savedInstanceState, validateCarPlate);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_navigation_drawer, container, false);
        rootView.findViewById(R.id.btn_show_events).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectAction(getString(R.string.action_show_all_car_events));
            }
        });
        rootView.findViewById(R.id.btn_show_car_plates).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectAction(getString(R.string.action_show_car_plates));
            }
        });
        rootView.findViewById(R.id.btn_validate_status).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showValidateDialog(savedInstanceState, null);
            }
        });
        rootView.findViewById(R.id.btn_show_settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                //TODO check if select action is required (tablet mode)
            }
        });

//        rootView.findViewById(R.id.list_item_car_view_all).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                selectItem(-1, true);
//            }
//        });
//        rootView.findViewById(R.id.btn_list_item_add_car).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startActivity(new Intent(getActivity(), CarEventsActivity.class));
//            }
//        });
//        mDrawerListView = (ListView) rootView.findViewById(R.id.list_car_entries);
//        mDrawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                selectItem(position, true);
//            }
//        });
//        mDrawerListView.setAdapter(adapter);
//        if (mCurrentSelectedPosition >= 0) {
//            mDrawerListView.setItemChecked(mCurrentSelectedPosition, true);
//        }
        return rootView;
    }

    private void showStatusDialog(Bundle savedInstanceState, final StatusEvent event) {
        final View dialogView = getLayoutInflater(savedInstanceState).inflate(R.layout.item_event_status, null);
        final View statusView = dialogView.findViewById(R.id.car_event_item_status);
        ((TextView) statusView.findViewById(R.id.item_name)).setText(event.getAsString(StatusEvent.FIELD_CAR_NUMBER));
        ((TextView) statusView.findViewById(R.id.item_description)).setText(event.getAsString(StatusEvent.FIELD_DESCRIPTION));

        final View statusDateView = statusView.findViewById(R.id.list_item_date_status);
        if (Utility.isStringNullOrEmpty(event.getStartDate()) || Utility.isStringNullOrEmpty(event.getExpireDate())) {
            statusDateView.setVisibility(View.GONE);
            statusView.findViewById(R.id.list_item_status_separator).setVisibility(View.GONE);

        } else {
            //start date
            ((TextView)statusDateView.findViewById(R.id.item_picker_month_start)).setText(event.getStartMonth());
            ((TextView)statusDateView.findViewById(R.id.item_picker_day_start)).setText(event.getStartDay());
            ((TextView)statusDateView.findViewById(R.id.item_picker_year_start)).setText(event.getStartYear());

            //end date
            ((TextView)statusDateView.findViewById(R.id.item_picker_month_end)).setText(event.getExpireMonth());
            ((TextView)statusDateView.findViewById(R.id.item_picker_day_end)).setText(event.getExpireDay());
            ((TextView)statusDateView.findViewById(R.id.item_picker_year_end)).setText(event.getExpireYear());
        }

        final DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                statusEvent = null;
                dialog.dismiss();
            }
        };
        statusEvent = event;
        statusDialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.title_activity_status)
                .setIcon(R.drawable.car_launcher)
                .setView(dialogView)
                .setPositiveButton(R.string.action_ok, listener)
                .create();

        if (event.isValid()) {
            if (event.getAsLong(StatusEvent.FIELD_ID) == null) {
                statusDialog.setButton(DialogInterface.BUTTON_NEUTRAL, getString(R.string.action_save_results), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        statusEvent = null;
                        final EventsContainer container = new EventsContainer();
                        container.add(event, EventsContainer.EventState.ADDED);
                        getActivity().startService(EventsManagementService.IntentBuilder.newInstance()
                                .command(EventsManagementService.COMMAND_APPLY_FROM_CONTAINER)
                                .data(container)
                                .build(getActivity()));
                        dialog.dismiss();
                    }
                });
            }
        }
        statusDialog.show();
    }

    private void showValidateDialog(Bundle savedInstanceState, String carPlate) {
        final View dialogView = getLayoutInflater(savedInstanceState).inflate(R.layout.item_event_validate, null);
        final View inputView = dialogView.findViewById(R.id.car_event_item_container);
        final EditText editText = (EditText) inputView.findViewById(R.id.input_text_car_plate);
        if (carPlate != null) {
            editText.setText(carPlate);
        }
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                validateCarPlate = s.toString();
            }
        });

        validateDialog = new AlertDialog.Builder(getActivity())
                .setIcon(R.drawable.car_launcher)
                .setTitle(R.string.title_activity_status)
                .setView(dialogView)
                .setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showValidateDialog = false;
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(R.string.action_validate, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showValidateDialog = false;
                        dialog.dismiss();
                        if (Utility.isStringNullOrEmpty(validateCarPlate)) {
                            Utility.notifyUser(getActivity(), "Will not validate an empty car plate, please retry");

                        } else {
                            getActivity().startService(CheckStatusService.IntentBuilder.newInstance()
                                    .carPlate(validateCarPlate.trim())
                                    .replySubject(CheckStatusService.ACTION_ON_DEMAND)
                                    .verificationDate(new Date())
                                    .build(getActivity()));

                        }
                    }
                })
                .create();
        showValidateDialog = true;
        validateDialog.show();

    }

    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
    }

    public void setUp(int fragmentId, DrawerLayout drawerLayout, boolean isDrawerLocked) {
        this.isDrawerLocked = isDrawerLocked;
        mFragmentContainerView = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;
        if (isDrawerLocked && mDrawerLayout != null) {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN);
            mDrawerLayout.setScrimColor(Color.TRANSPARENT);
        } else {
            // set a custom shadow that overlays the main content when the drawer opens
            mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
            // set up the drawer's list view with items and click listener

            ActionBar actionBar = getActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

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
                    mUserLearnedDrawer = true;
                    PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).apply();
                }

                getActivity().supportInvalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }
        };

        // If the user hasn't 'learned' about the drawer, open it to introduce them to the drawer,
        // per the navigation drawer design guidelines.
        if (!isDrawerLocked && !mUserLearnedDrawer && !mFromSavedInstanceState) {
            mDrawerLayout.openDrawer(mFragmentContainerView);
        }

        // Defer code dependent on restoration of previous instance state.
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });

        if (!isDrawerLocked) {
            mDrawerLayout.setDrawerListener(mDrawerToggle);
        }
    }

    private void selectAction(String value) {
        mCurrentSelectedValue = value;
        closeDrawer();

        if (mCallbacks != null) {
            mCallbacks.onNavigationDrawerItemSelected(value);
            if (isDrawerLocked) {
                showGlobalContextActionBar(mCurrentSelectedValue);
            }
        }
    }

    void closeDrawer() {
        if (mDrawerLayout != null && !isDrawerLocked) {
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
    public void onResume() {
        if (validateDialog != null && showValidateDialog) {
            validateDialog.show();
        }
        if (statusDialog != null && statusEvent != null) {
            statusDialog.show();
        }

        super.onResume();
        unregisterReceiver();
        receiver = new StatusReceiver();

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver, new IntentFilter(CheckStatusService.ACTION_ON_DEMAND));
    }

    @Override
    public void onPause() {
        if (validateDialog != null) {
            validateDialog.dismiss();
        }
        if (statusDialog != null) {
            statusDialog.dismiss();
        }
        super.onPause();
        unregisterReceiver();
    }

    private void unregisterReceiver() {
        if (receiver != null) {
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(receiver);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_SELECTED_VALUE, mCurrentSelectedValue);
        outState.putBoolean(STATE_VALIDATE_SHOW_DIALOG, showValidateDialog);
        outState.putString(STATE_VALIDATE_CAR_PLATE, validateCarPlate);
        outState.putParcelable(STATE_STATUS_EVENT, statusEvent);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (mDrawerLayout != null && isDrawerOpen()) {
            inflater.inflate(R.menu.main, menu);
            showGlobalContextActionBar(getString(R.string.app_name));
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

//    @Override
//    public boolean onContextItemSelected(MenuItem item) {
//        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
//        final StatusEvent selectedEvent = adapter.getItem(info.position);
//
//        switch (item.getItemId()) {
//            case R.id.status_edit_event:
//                final Intent editIntent = new Intent(getActivity(), CarEventsActivity.class);
//                editIntent.putExtra(Utility.FIELD_DATA, selectedEvent.getAsString(StatusEvent.FIELD_CAR_NUMBER));
//                startActivity(editIntent);
//                break;
//
//            case R.id.status_delete_event:
//                adapter.remove(selectedEvent);
//                adapter.notifyDataSetChanged();
//                //TODO add reply listener and handle status code
//                getActivity().startService(EventsManagementService.IntentBuilder.newInstance()
//                                                                              .command(EventsManagementService.COMMAND_DELETE_CAR)
//                                                                              .data(selectedEvent)
//                                                                              .build(getActivity()));
//                break;
//
//            default:
//                return super.onContextItemSelected(item);
//        }
//        return true;
//    }

    private void showGlobalContextActionBar(String title) {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setTitle(title);
    }

    private ActionBar getActionBar() {
        return ((ActionBarActivity) getActivity()).getSupportActionBar();
    }

    public static interface NavigationDrawerCallbacks {
        void onNavigationDrawerItemSelected(String value);
    }

    private class StatusReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case CheckStatusService.ACTION_ON_DEMAND:
                    showStatusDialog(null, (StatusEvent) intent.getParcelableExtra(Utility.FIELD_DATA));
                    break;

                default:
                    Log.e(LOG_TAG, "Unknown action " + intent.getAction());
            }
        }
    }
}
