package to.uk.carminder.app;

import android.content.Intent;
import android.os.PersistableBundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.widget.DrawerLayout;


public class MainActivity extends ActionBarActivity implements UserActionsFragment.NavigationDrawerCallbacks {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String STATE_LATEST_VALUE = "STATE_LATEST_VALUE";

    private UserActionsFragment mUserActionsFragment;
    private CharSequence mTitle;
    private boolean isDrawerLocked;

    private String latestValue;

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
        }
        onNavigationDrawerItemSelected(latestValue);
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

            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_LATEST_VALUE, latestValue);
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

}
