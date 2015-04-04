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
import to.uk.carminder.app.data.StatusEvent;
import to.uk.carminder.app.data.adapter.StatusEventAdapter;
import to.uk.carminder.app.service.CheckStatusService;
import to.uk.carminder.app.service.EventsModifierService;


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
                        //TODO refactor activity -> service -> activity behaviour
                        final Intent eventsIntent = new Intent(getActivity(), CarEventsActivity.class);
                        eventsIntent.putExtra(EventsModifierService.FIELD_DATA, event.getAsString(StatusEvent.FIELD_CAR_NUMBER));
                        startActivity(eventsIntent);

//                        getActivity().startService(EventsModifierService.IntentBuilder.newInstance()
//                                                                                      .command(EventsModifierService.COMMAND_ADD_EVENT)
//                                                                                      .event(event)
//                                                                                      .replySubject(EventsModifierService.ACTION_MODIFY_STATUS)
//                                                                                      .build(getActivity()));

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

            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver, new IntentFilter(CheckStatusService.ACTION_ON_DEMAND));
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver, new IntentFilter(EventsModifierService.ACTION_MODIFY_STATUS));
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
                        final StatusEvent event = (StatusEvent) intent.getParcelableExtra(CheckStatusService.FIELD_DATA);
                        if (suggestions != null && event != null && event.isValid()) {
                            suggestions.saveRecentQuery(event.getAsString(StatusEvent.FIELD_CAR_NUMBER), null);
                        }
                        adapter.clear();
                        adapter.add(event);
                        break;

                    case EventsModifierService.ACTION_MODIFY_STATUS:
                        final Intent eventsIntent = new Intent(context, CarEventsActivity.class);
                        eventsIntent.putExtra(EventsModifierService.FIELD_DATA, intent.getStringExtra(EventsModifierService.FIELD_DATA));
                        startActivity(eventsIntent);
                        break;

                    default:
                        Log.e(LOG_TAG, "Unknown action " + intent.getAction());
                }
            }
        }
    }
}
