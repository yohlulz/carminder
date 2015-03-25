package to.uk.carminder.app;

import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import to.uk.carminder.app.data.CarEvent;
import to.uk.carminder.app.data.adapter.EventStatusAdapter;
import to.uk.carminder.app.service.CheckStatusService;


public class StatusActivity extends ActionBarActivity {
    public static final String LOG_TAG = StatusActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            final String query = intent.getStringExtra(SearchManager.QUERY);
            Log.i(LOG_TAG, "Search " + query);
            Toast.makeText(this, query, Toast.LENGTH_LONG).show();
            //TODO perform search and display results
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.global, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        private EventStatusAdapter adapter;

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_status, container, false);

            adapter = new EventStatusAdapter(getActivity(), new ArrayList<>(Arrays.asList(
                    new CarEvent(new Date(), "test", "test"),
                    new CarEvent(new Date(), "test1", "test"))));
            final ListView eventsView = (ListView) rootView.findViewById(R.id.listView_event);
            eventsView.setAdapter(adapter);
            return rootView;
        }

        private class StatusReceiver extends BroadcastReceiver {

            @Override
            public void onReceive(Context context, Intent intent) {
                //TODO review this and implement fromString for CarEvent and Car
                final CarEvent event = CarEvent.fromString(intent.getStringExtra(CheckStatusService.FIELD_DATA));
                if (event != null) {
                    adapter.add(event);

                }
            }
        }
    }
}
