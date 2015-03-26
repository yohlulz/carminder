package to.uk.carminder.app;

import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import to.uk.carminder.app.data.CarEvent;
import to.uk.carminder.app.data.StatusEvent;
import to.uk.carminder.app.data.adapter.StatusEventAdapter;
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

            startService(CheckStatusService.IntentBuilder.newInstance()
                                                         .carPlate(query)
                                                         .replySubject(CheckStatusService.ACTION_ON_DEMAND)
                                                         .build(this));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.global, menu);
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

    public static class PlaceholderFragment extends Fragment {
        private StatusEventAdapter adapter;
        private StatusReceiver receiver;

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_status, container, false);

            adapter = new StatusEventAdapter(getActivity(), new ArrayList<StatusEvent>());
            final ListView eventsView = (ListView) rootView.findViewById(R.id.listView_event);
            eventsView.setAdapter(adapter);
            return rootView;
        }

        @Override
        public void onResume() {
            super.onResume();
            unregisterReceiver();
            receiver = new StatusReceiver();
            final IntentFilter filter = new IntentFilter(CheckStatusService.ACTION_ON_DEMAND);
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver, filter);
        }

        @Override
        public void onPause() {
            super.onPause();
            unregisterReceiver();
        }

        private void unregisterReceiver() {
            if (receiver != null) {
                LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(receiver);
            }
        }

        private class StatusReceiver extends BroadcastReceiver {

            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i(LOG_TAG, "Received intent " + intent);
                adapter.addAll(StatusEvent.fromJSON(intent.getStringExtra(CheckStatusService.FIELD_DATA)));
            }
        }
    }
}
