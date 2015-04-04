package to.uk.carminder.app.service;


import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import to.uk.carminder.app.Utility;
import to.uk.carminder.app.data.StatusEvent;

public class EventsModifierService extends IntentService {
    private static final String WORKER_NAME = "EventModifier Worker";
    private static final String LOG_TAG = EventsModifierService.class.getSimpleName();
    public static final String ACTION_MODIFY_STATUS = "uk.to.carminder.app.MODIFY_STATUS";

    public static final String COMMAND_ADD_EVENT = "add_event";
    public static final String COMMAND_MODIFY_EVENT = "modify_event";
    public static final String COMMAND_DELETE_EVENT = "delete_event";

    public static final String COMMAND_DELETE_CAR = "delete_car";

    public static final String FIELD_COMMAND = "FIELD_COMMAND";
    public static final String FIELD_DATA = "FIELD_DATA";
    public static final String FIELD_REPLY_SUBJECT = "REPLY_SUBJECT";

    public EventsModifierService() {
        super(WORKER_NAME);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        TaskBuilder.newInstance()
                   .command(intent.getStringExtra(FIELD_COMMAND))
                   .event((StatusEvent) intent.getParcelableExtra(FIELD_DATA))
                   .replySubject(intent.getStringExtra(FIELD_REPLY_SUBJECT))
                   .execute(this);
    }

    private static class TaskBuilder {
        private StatusEvent event;
        private String command;
        private String subject;

        private TaskBuilder() {
        }

        public TaskBuilder event(StatusEvent event) {
            this.event = event;
            return this;
        }

        public TaskBuilder command(String command) {
            this.command = command;
            return this;
        }

        public TaskBuilder replySubject(String subject) {
            this.subject = subject;
            return this;
        }

        public void execute(Context context) {
            if (!Utility.isStringNullOrEmpty(subject)) {
                //TODO manage event, CRUD operations in DB

                final Intent replyIntent = buildReplyIntent(context, subject, event);

                LocalBroadcastManager.getInstance(context).sendBroadcast(replyIntent);

            } else {
                Log.w(LOG_TAG, "Skipping request due to empty reply subject.");
            }
        }

        private Intent buildReplyIntent(Context context, String action, StatusEvent data) {
            final Intent intent = new Intent();
            intent.setAction(action);
            intent.putExtra(FIELD_DATA, data.getAsString(StatusEvent.FIELD_CAR_NUMBER));

            return intent;
        }

        public static TaskBuilder newInstance() {
            return new TaskBuilder();
        }
    }

    public static class IntentBuilder {
        private String command;
        private StatusEvent event;
        private String replySubject;

        private IntentBuilder() {
        }

        public IntentBuilder command(String command) {
            this.command = command;
            return this;
        }

        public IntentBuilder event(StatusEvent event) {
            this.event = event;
            return this;
        }

        public IntentBuilder replySubject(String replySubject) {
            this.replySubject = replySubject;
            return this;
        }

        public Intent build(Context context) {
           final Intent intent = new Intent(context, EventsModifierService.class);
            intent.putExtra(FIELD_COMMAND, command);
            intent.putExtra(FIELD_DATA, event);
            intent.putExtra(FIELD_REPLY_SUBJECT, replySubject);

            return intent;
        }

        public static IntentBuilder newInstance() {
            return new IntentBuilder();
        }
    }
}
