package to.uk.carminder.app.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class RebootReceiver extends BroadcastReceiver {
    public static final String LOG_TAG = RebootReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case Intent.ACTION_BOOT_COMPLETED:
                context.startService(EventsManagementService.IntentBuilder.newInstance()
                        .command(EventsManagementService.ACTION_RESCHEDULE_ALARMS)
                        .build(context));
                break;

            default:
                Log.w(LOG_TAG, String.format("Unknown command %s, skipping intent %s", intent.getAction(), intent));

        }

    }
}
