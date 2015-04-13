package to.uk.carminder.app.service;


import android.app.IntentService;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.os.Parcelable;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import to.uk.carminder.app.R;
import to.uk.carminder.app.Utility;
import to.uk.carminder.app.data.EventContract;
import to.uk.carminder.app.data.EventProvider;
import to.uk.carminder.app.data.EventsContainer;
import to.uk.carminder.app.data.StatusEvent;

public class EventsManagementService extends IntentService {
    private static final String WORKER_NAME = "EventModifier Worker";
    private static final String LOG_TAG = EventsManagementService.class.getSimpleName();

    public static final String ACTION_MODIFY_STATUS = "uk.to.carminder.app.MODIFY_STATUS";

    public static final String COMMAND_APPLY_FROM_CONTAINER = "apply_from_container";
    public static final String COMMAND_DELETE_CAR = "delete_car";
    public static final String ACTION_NOTIFICATION = "uk.to.carminder.app.NOTIFICATION";
    public static final String ACTION_RESCHEDULE_ALARMS = "uk.to.carminder.app.RESCHEDULE_ALARMS";
    public static final String ACTION_ADD_ALARM = "uk.to.carminder.app.SCHEDULE_ALARM";

    public static final int STATUS_OK = 0;
    public static final int STATUS_ERROR = 1;

    public static final String FIELD_RESULT_STATUS = "FIELD_STATUS";
    public static final String FIELD_RESULT_STATUS_MESSAGE = "FIELD_STATUS_MESSAGE";
    public static final String FIELD_COMMAND = "FIELD_COMMAND";
    public static final String FIELD_REPLY_SUBJECT = "REPLY_SUBJECT";

    public EventsManagementService() {
        super(WORKER_NAME);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        TaskBuilder.newInstance()
                   .command(intent.getStringExtra(FIELD_COMMAND))
                   .data(intent.getParcelableExtra(Utility.FIELD_DATA))
                   .replySubject(intent.getStringExtra(FIELD_REPLY_SUBJECT))
                   .execute(this);
    }

    private static class TaskBuilder {
        private static final Map<EventsContainer.EventState, Callback> operationByState = new HashMap<>();

        static {
            operationByState.put(EventsContainer.EventState.ADDED, new Callback() {
                @Override
                public ArrayList<ContentProviderOperation> buildOperations(Collection<StatusEvent> events) {
                    final ArrayList<ContentProviderOperation> operations = new ArrayList<>();
                    for (StatusEvent event : events) {
                        operations.add(ContentProviderOperation.newInsert(EventContract.StatusEntry.CONTENT_URI)
                                .withValues(event.getContentValues())
                                .build());
                    }
                    return operations;
                }
            });
            operationByState.put(EventsContainer.EventState.MODIFIED, new Callback() {
                @Override
                public ArrayList<ContentProviderOperation> buildOperations(Collection<StatusEvent> events) {
                    final ArrayList<ContentProviderOperation> operations = new ArrayList<>();
                    for (StatusEvent event : events) {
                        operations.add(ContentProviderOperation.newUpdate(EventContract.StatusEntry.CONTENT_URI)
                                .withSelection(EventContract.StatusEntry._ID + " = ?", new String[] {event.getAsString(StatusEvent.FIELD_ID)})
                                .withValues(event.getContentValues())
                                .build());
                    }
                    return operations;
                }
            });
            operationByState.put(EventsContainer.EventState.DELETED, new Callback() {
                @Override
                public ArrayList<ContentProviderOperation> buildOperations(Collection<StatusEvent> events) {
                    final ArrayList<ContentProviderOperation> operations = new ArrayList<>();
                    for (StatusEvent event : events) {
                        operations.add(ContentProviderOperation.newDelete(EventContract.StatusEntry.CONTENT_URI)
                                .withSelection(EventContract.StatusEntry._ID + " = ?", new String[] {event.getAsString(StatusEvent.FIELD_ID)})
                                .build());
                    }
                    return operations;
                }
            });
        }

        private Parcelable data;
        private String command;
        private String subject;

        private TaskBuilder() {
        }

        public TaskBuilder data(Parcelable data) {
            this.data = data;
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
                int status = STATUS_OK;
                String message = Utility.EMPTY_STRING;

                switch (command) {
                    case COMMAND_DELETE_CAR:
                        if (data instanceof StatusEvent) {
                            final StatusEvent event = (StatusEvent) data;
                            final int deletedRows = context.getContentResolver().delete(EventContract.StatusEntry.CONTENT_URI,
                                                                                        EventContract.StatusEntry.COLUMN_CAR_NUMBER + " LIKE ?",
                                                                                        new String[] {event.getAsString(StatusEvent.FIELD_CAR_NUMBER)});
                            Log.i(LOG_TAG, String.format("Deleted %d rows for command %s having data set to %s", deletedRows, command, event.getAsString(StatusEvent.FIELD_CAR_NUMBER)));

                        } else {
                            status = STATUS_ERROR;
                            message = context.getString(R.string.message_invalid_data);
                        }
                        break;

                    case COMMAND_APPLY_FROM_CONTAINER:
                        if (data instanceof EventsContainer) {
                            final EventsContainer container = (EventsContainer) data;
                            /* apply operations for each modified data (added, modified and deleted) */
                            for (EventsContainer.EventState state : EventsContainer.EventState.values()) {
                                if (state == EventsContainer.EventState.UNCHANGED) {
                                    /* skip unchanged data */
                                    continue;
                                }
                                final Collection<StatusEvent> events = container.getByState(state);
                                if (!Utility.isCollectionNullOrEmpty(events)) {
                                    try {
                                        context.getContentResolver().applyBatch(EventContract.CONTENT_AUTHORITY, operationByState.get(state).buildOperations(events));

                                    } catch (RemoteException | OperationApplicationException ex) {
                                        Log.w(LOG_TAG, ex.getMessage(), ex);
                                        status = STATUS_ERROR;
                                        message = ex.getMessage();
                                    }
                                }
                            }
                            /* because there can be newly added events which don't have yet an id, reschedule all alarms */
                            context.startService(EventsManagementService.IntentBuilder.newInstance()
                                                                                      .command(EventsManagementService.ACTION_RESCHEDULE_ALARMS)
                                                                                      .build(context));

                        } else {
                            status = STATUS_ERROR;
                            message = context.getString(R.string.message_invalid_data);
                        }
                        break;

                    case ACTION_ADD_ALARM:
                        if (data instanceof StatusEvent) {
                            new NotificationManager().addAlarm(context, (StatusEvent) data);

                        } else {
                            status = STATUS_ERROR;
                            message = context.getString(R.string.message_invalid_data);
                        }
                        break;

                    case ACTION_RESCHEDULE_ALARMS:
                        final NotificationManager notificationManager = new NotificationManager();
                        for (StatusEvent event : StatusEvent.fromCursor(context.getContentResolver().query(EventContract.StatusEntry.CONTENT_URI,
                                                                                                            StatusEvent.COLUMNS_STATUS_ENTRY,
                                                                                                            null, null, null))) {
                            notificationManager.addAlarm(context, event);
                            Log.i(LOG_TAG, "Rescheduled alarm for event " + event.getContentValues());
                        }
                        break;

                    case ACTION_NOTIFICATION:
                        if (data instanceof StatusEvent) {
                            final StatusEvent notificationEvent = (StatusEvent) data;
                            final Collection<StatusEvent> existingEvents = StatusEvent.fromCursor(context.getContentResolver().query(EventContract.StatusEntry.CONTENT_URI,
                                                                                                                                     StatusEvent.COLUMNS_STATUS_ENTRY,
                                                                                                                                     EventProvider.SELECTION_CAR_PLATE + " AND " + EventContract.StatusEntry.COLUMN_EVENT_NAME + " LIKE ?",
                                                                                                                                     new String[] {notificationEvent.getAsString(StatusEvent.FIELD_CAR_NUMBER), notificationEvent.getAsString(StatusEvent.FIELD_NAME)},
                                                                                                                                     null));
                            if (existingEvents.contains(notificationEvent)) {
                                /* trigger notification only if the data has not changed, changed data means deleted data or improper alarm management */
                                new NotificationManager().showNotification(context, notificationEvent);

                            } else {
                                Log.w(LOG_TAG, "Will not show a notification for a modified event without proper alarm. Event: " + notificationEvent.getContentValues());
                            }

                        } else {
                            status = STATUS_ERROR;
                            message = context.getString(R.string.message_invalid_data);
                        }
                        break;

                    default:
                        status = STATUS_ERROR;
                        message = context.getString(R.string.message_unknown_command);
                }
                LocalBroadcastManager.getInstance(context).sendBroadcast(buildReplyIntent(subject, status, message));

            } else {
                Log.w(LOG_TAG, "Skipping request due to empty reply subject.");
            }
        }

        private Intent buildReplyIntent(String action, int statusCode, String statusMessage) {
            final Intent intent = new Intent();
            intent.setAction(action);
            intent.putExtra(FIELD_RESULT_STATUS, statusCode);
            intent.putExtra(FIELD_RESULT_STATUS_MESSAGE, statusMessage);

            return intent;
        }

        public static TaskBuilder newInstance() {
            return new TaskBuilder();
        }

        private static interface Callback {
            ArrayList<ContentProviderOperation> buildOperations(Collection<StatusEvent> events);
        }
    }

    /**
     * Helper class that builds a command with its data for this service to execute
     */
    public static class IntentBuilder {
        private String command;
        private Parcelable data;
        private String replySubject = ACTION_MODIFY_STATUS;

        private IntentBuilder() {
        }

        public IntentBuilder command(String command) {
            this.command = command;
            return this;
        }

        public IntentBuilder data(Parcelable data) {
            this.data = data;
            return this;
        }

        public IntentBuilder replySubject(String replySubject) {
            this.replySubject = replySubject;
            return this;
        }

        public Intent build(Context context) {
           final Intent intent = new Intent(context, EventsManagementService.class);
            intent.putExtra(FIELD_COMMAND, command);
            intent.putExtra(Utility.FIELD_DATA, data);
            intent.putExtra(FIELD_REPLY_SUBJECT, replySubject);

            return intent;
        }

        public static IntentBuilder newInstance() {
            return new IntentBuilder();
        }
    }

}
