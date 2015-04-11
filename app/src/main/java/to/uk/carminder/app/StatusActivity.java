package to.uk.carminder.app;

import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.SearchRecentSuggestions;
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
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Date;

import to.uk.carminder.app.data.EventSuggestionProvider;
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
                    .add(R.id.container, new StatusFragment())
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
            //TODO start from alarm
            startService(CheckStatusService.IntentBuilder.newInstance()
                                                         .carPlate(intent.getStringExtra(SearchManager.QUERY))
                                                         .replySubject(CheckStatusService.ACTION_ON_DEMAND)
                                                         .verificationDate(new Date())
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

            case R.id.action_clear_history:
                Utility.clearSearchHistory(this);
                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    public static class StatusFragment extends Fragment {
        private StatusEventAdapter adapter;
        private StatusReceiver receiver;
        private SearchRecentSuggestions suggestions;
        private StatusEvent receivedEvent;
        private Button addEventButton;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_status, container, false);

            suggestions = new SearchRecentSuggestions(getActivity(), EventSuggestionProvider.AUTHORITY, EventSuggestionProvider.MODE);
            adapter = new StatusEventAdapter(getActivity(), new ArrayList<StatusEvent>());
            final ListView eventsView = (ListView) view.findViewById(R.id.listView_event);
            eventsView.setAdapter(adapter);
            addEventButton = (Button) view.findViewById(R.id.button_add_event_from_status);
            addEventButton.setVisibility(View.GONE);
            addEventButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Intent eventsIntent = new Intent(getActivity(), CarEventsActivity.class);
                    eventsIntent.putExtra(Utility.FIELD_DATA, receivedEvent.getAsString(StatusEvent.FIELD_CAR_NUMBER));
                    startActivity(eventsIntent);
                }
            });

            return view;
        }

        @Override
        public void onResume() {
            super.onResume();
            unregisterReceiver();
            receiver = new StatusReceiver();

            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver, new IntentFilter(CheckStatusService.ACTION_ON_DEMAND));
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
                switch (intent.getAction()) {
                    case CheckStatusService.ACTION_ON_DEMAND:
                        final StatusEvent event = intent.getParcelableExtra(Utility.FIELD_DATA);
                        if (suggestions != null && event != null && event.isValid()) {
                            suggestions.saveRecentQuery(event.getAsString(StatusEvent.FIELD_CAR_NUMBER), null);
                            receivedEvent = event;
                            addEventButton.setVisibility(View.VISIBLE);
                        }
                        adapter.clear();
                        adapter.add(event);
                        break;

                    default:
                        Log.e(LOG_TAG, "Unknown action " + intent.getAction());
                }
            }
        }
    }
}
