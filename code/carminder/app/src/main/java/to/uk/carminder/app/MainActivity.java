package to.uk.carminder.app;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.PersistableBundle;
import android.provider.CalendarContract;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;

import java.util.Calendar;

import to.uk.carminder.app.data.EventsContainer;
import to.uk.carminder.app.data.StatusEvent;
import to.uk.carminder.app.data.adapter.CarEventsAdapter;
import to.uk.carminder.app.service.EventsManagementService;


public class MainActivity extends ActionBarActivity implements UserActionsFragment.NavigationDrawerCallbacks, CarEventsAdapter.FragmentCallbacks {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String STATE_LATEST_VALUE = "STATE_LATEST_VALUE";
    private static final String PREF_EVENT_STATUS = "pref_modify_event_status";
    private static final String PREF_EVENT_STATE = "pref_event_state";

    private UserActionsFragment mUserActionsFragment;
    private CharSequence mTitle;
    private boolean isDrawerLocked;

    private String latestValue;

    private StatusEvent event;
    private EventsContainer.EventState state;
    private AlertDialog editDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mUserActionsFragment = (UserActionsFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();
        isDrawerLocked = getResources().getBoolean(R.bool.lockDrawer);

        mUserActionsFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout), isDrawerLocked);
        if (savedInstanceState != null) {
            latestValue = savedInstanceState.getString(STATE_LATEST_VALUE);
            event = savedInstanceState.getParcelable(PREF_EVENT_STATUS);
            state = (EventsContainer.EventState) savedInstanceState.getSerializable(PREF_EVENT_STATE);

        }

        onNavigationDrawerItemSelected(latestValue);
        if (event != null) {
            showEventDialog(event);
        }
    }

    @Override
    public void onNavigationDrawerItemSelected(String value) {
        latestValue = value;
        final Fragment fragment;
        if (getString(R.string.action_show_car_plates).equals(value)) {
            fragment = new CarPlatesFragment();
        } else {
            fragment = CarEventsFragment.newInstance(value);
        }
        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();

            final FragmentTransaction transaction = fragmentManager.beginTransaction().replace(R.id.container, fragment, value);
            if (!Utility.isStringNullOrEmpty(value)) {
                transaction.addToBackStack(value);
            }
            transaction.commit();
        }
    }

    public void onSectionAttached(String carPlate) {
        /* update latest value to update the UI with the correct value at back button press */
        latestValue = carPlate;

        mTitle = Utility.isStringNullOrEmpty(carPlate) ? getString(R.string.action_show_all_car_events) : carPlate;
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mUserActionsFragment.isDrawerOpen()) {
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
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

            case R.id.action_add_event:
                if (getString(R.string.action_show_car_plates).equals(latestValue)) {
                    mUserActionsFragment.showValidateDialog(null, null);
                } else {
                    state = EventsContainer.EventState.ADDED;
                    final StatusEvent event = new StatusEvent();
                    if (isCustomPlate(latestValue)) {
                        event.put(StatusEvent.FIELD_CAR_NUMBER, latestValue);
                    }
                    showEventDialog(event);
                }
                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private boolean isCustomPlate(String value) {
        return !getString(R.string.action_show_car_plates).equals(value) && !getString(R.string.action_show_all_car_events).equals(value);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_LATEST_VALUE, latestValue);
        outState.putParcelable(PREF_EVENT_STATUS, event);
        outState.putSerializable(PREF_EVENT_STATE, state);
    }

    @Override
    public void onBackPressed() {
        /* close drawer if opened */
        if (mUserActionsFragment.isDrawerOpen() && !isDrawerLocked) {
            mUserActionsFragment.closeDrawer();
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (editDialog != null && event != null) {
            editDialog.show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (editDialog != null) {
            editDialog.dismiss();
        }
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

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(Intent.createChooser(intent, getString(R.string.message_choose_app)));

        } else {
            Utility.notifyUser(this, getString(R.string.message_no_activity_found));
        }

    }

    @Override
    public void editEvent(StatusEvent event) {
        state = EventsContainer.EventState.MODIFIED;
        showEventDialog(event);

    }

    @Override
    public void deleteEvent(final StatusEvent event) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_action)
                .setPositiveButton(R.string.action_delete_event, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final EventsContainer container = new EventsContainer();
                        container.add(event, EventsContainer.EventState.DELETED);
                        startService(EventsManagementService.IntentBuilder.newInstance()
                                .command(EventsManagementService.COMMAND_APPLY_FROM_CONTAINER)
                                .data(container)
                                .build(MainActivity.this));
                        dialog.dismiss();
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

    private void saveModifiedEvent(EventsContainer container) {
        startService(EventsManagementService.IntentBuilder.newInstance()
                .command(EventsManagementService.COMMAND_APPLY_FROM_CONTAINER)
                .data(container)
                .build(this));
    }

    private void showEventDialog(final StatusEvent editEvent) {
        if (editDialog != null) {
            editDialog.dismiss();
        }
        this.event = editEvent;
        final View dialogView = getLayoutInflater().inflate(R.layout.manage_event_item, null);
        final EditText carNumberView = (EditText) dialogView.findViewById(R.id.edit_event_car_number);
        final EditText eventNameView = (EditText) dialogView.findViewById(R.id.edit_event_item_name);
        final EditText eventDescriptionView = (EditText) dialogView.findViewById(R.id.edit_event_item_description);
        final DatePicker datePicker = (DatePicker) dialogView.findViewById(R.id.edit_event_date_picker);
        final Calendar today = Utility.getCurrentDate();

        final Calendar expireDate = Utility.getCurrentDate();
        if (event != null && event.getAsLong(StatusEvent.FIELD_END_DATE) != null) {
            expireDate.setTimeInMillis(event.getAsLong(StatusEvent.FIELD_END_DATE));
        }
        datePicker.setMinDate(today.getTimeInMillis());
        datePicker.init(expireDate.get(Calendar.YEAR), expireDate.get(Calendar.MONTH), expireDate.get(Calendar.DAY_OF_MONTH), new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                event.put(StatusEvent.FIELD_END_DATE, Utility.parse(view.getYear(), view.getMonth(), view.getDayOfMonth()));
            }
        });

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

        carNumberView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                event.put(StatusEvent.FIELD_CAR_NUMBER, s.toString());
            }
        });
        eventNameView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                event.put(StatusEvent.FIELD_NAME, s.toString());

            }
        });
        eventDescriptionView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                event.put(StatusEvent.FIELD_DESCRIPTION, s.toString());
            }
        });

        editDialog = new AlertDialog.Builder(this)
                .setTitle(EventsContainer.EventState.MODIFIED != state ? R.string.action_add_event : R.string.action_modify_event)
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
                        final Long expireDate = Utility.parse(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
                        final String carPlate = String.valueOf(carNumberView.getText());
                        final String eventName = String.valueOf(eventNameView.getText());
                        final String eventDescription = String.valueOf(eventDescriptionView.getText());
                        if (Utility.isStringNullOrEmpty(carPlate)) {
                            Utility.notifyUser(MainActivity.this, getString(R.string.message_invalid_car_plate_retry));
                            return;
                        }
                        if (Utility.isStringNullOrEmpty(eventName)) {
                            Utility.notifyUser(MainActivity.this, getString(R.string.message_invalid_event_retry));
                            return;
                        }
                        if (expireDate < today.getTimeInMillis()) {
                            Utility.notifyUser(MainActivity.this, getString(R.string.message_invalid_expire_date_retry));
                            return;
                        }
                        event.put(StatusEvent.FIELD_CAR_NUMBER, carPlate);
                        event.put(StatusEvent.FIELD_NAME, eventName);
                        event.put(StatusEvent.FIELD_DESCRIPTION, eventDescription);
                        event.put(StatusEvent.FIELD_END_DATE, expireDate);
                        if (state == EventsContainer.EventState.ADDED) {
                            event.put(StatusEvent.FIELD_START_DATE, expireDate);
                        }

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
