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
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import to.uk.carminder.app.data.EventSuggestionProvider;
import to.uk.carminder.app.service.StatusEvent;
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
            startService(CheckStatusService.IntentBuilder.newInstance()
                                                         .carPlate(intent.getStringExtra(SearchManager.QUERY))
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
                //TODO start from alarm
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


        public StatusFragment() {
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            getActivity().getMenuInflater().inflate(R.menu.context_status_item, menu);
            super.onCreateContextMenu(menu, v, menuInfo);
        }

        @Override
        public boolean onContextItemSelected(MenuItem item) {
            final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

            switch (item.getItemId()) {
                case R.id.status_add_event:
                    final StatusEvent event = adapter.getItem(info.position);
                    if (event != null && event.isValid()) {
                        final Intent addEvent = new Intent(getActivity(), CarEventsActivity.class);
                        event.populateIntent(addEvent);

                        startActivity(addEvent);
                    } else {
                        Toast.makeText(getActivity(), getString(R.string.message_connect_to_internet), Toast.LENGTH_LONG).show();
                    }
                    break;

                default:
                    return super.onContextItemSelected(item);
            }

            return true;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_status, container, false);

            suggestions = new SearchRecentSuggestions(getActivity(), EventSuggestionProvider.AUTHORITY, EventSuggestionProvider.MODE);
            adapter = new StatusEventAdapter(getActivity(), new ArrayList<StatusEvent>());
            final ListView eventsView = (ListView) view.findViewById(R.id.listView_event);
            eventsView.setAdapter(adapter);
            registerForContextMenu(eventsView);
            return view;
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
                final StatusEvent event = StatusEvent.fromIntent(intent);
                if (suggestions != null && event.isValid()) {
                    suggestions.saveRecentQuery(event.getName(), null);
                }
                adapter.add(event);
            }
        }
    }
}
