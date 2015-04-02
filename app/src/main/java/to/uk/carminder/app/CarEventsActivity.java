package to.uk.carminder.app;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;

import to.uk.carminder.app.data.adapter.CarEventsAdapter;
import to.uk.carminder.app.data.StatusEvent;
import to.uk.carminder.app.service.EventsModifierService;


public class CarEventsActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_events);
        if (savedInstanceState == null) {
            final CarEventsFragment fragment = new CarEventsFragment();
            //TODO extract field_data to utility
            final String carPlate = (getIntent() != null) ? getIntent().getStringExtra(EventsModifierService.FIELD_DATA) : null;
            if (!Utility.isStringNullOrEmpty(carPlate)) {
                final Bundle bundle = new Bundle();
                bundle.putString(EventsModifierService.FIELD_DATA, carPlate);
                fragment.setArguments(bundle);
            }

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.car_events, menu);
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

    public static class CarEventsFragment extends Fragment {
        private CarEventsAdapter adapter;

        public CarEventsFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_car_events, container, false);
            adapter = new CarEventsAdapter(getActivity(), null, 0);

            final ListView view = (ListView) rootView.findViewById(R.id.list_item_car_event);
            view.setAdapter(adapter);

            final EditText carNumberView = (EditText) rootView.findViewById(R.id.list_item_car_name);
            final String carPlate = getArguments() != null ? getArguments().getString(EventsModifierService.FIELD_DATA) : null;
            if (!Utility.isStringNullOrEmpty(carPlate)) {
                carNumberView.setText(carPlate);
            }
            //TODO add carNUmberView to adapter to be able to set it from DB

            return rootView;
        }
    }
}
