package to.uk.carminder.app.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import to.uk.carminder.app.MainActivity;
import to.uk.carminder.app.R;
import to.uk.carminder.app.data.StatusEvent;

public class NotificationManager {
    public void showNotification(Context context, StatusEvent notificationEvent) {
        final int requestCode = notificationEvent.getAsLong(StatusEvent.FIELD_ID).intValue();
        final android.app.NotificationManager notificationManager = (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        final NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                                                                                .setSmallIcon(R.mipmap.ic_launcher)
                                                                                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_alarm))
                                                                                .setContentTitle(context.getString(R.string.notification_title))
                                                                                .setContentText(notificationEvent.getSummary(context));
        notificationBuilder.setContentIntent(TaskStackBuilder.create(context)
                                                             .addNextIntent(new Intent(context, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP))
                                                             .getPendingIntent(requestCode, PendingIntent.FLAG_UPDATE_CURRENT));

        final Notification notification = notificationBuilder.build();
        notification.defaults |= Notification.DEFAULT_SOUND;
        notification.defaults |= Notification.DEFAULT_VIBRATE;

        notificationManager.notify(requestCode, notification);
    }

    public void addAlarm(Context context, StatusEvent data) {
        final int requestCode = data.getAsLong(StatusEvent.FIELD_ID).intValue();
        final AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        final Intent intent = EventsManagementService.IntentBuilder.newInstance()
                                                                   .command(EventsManagementService.ACTION_NOTIFICATION)
                                                                   .data(data)
                                                                   .build(context);
        am.set(AlarmManager.RTC_WAKEUP, data.getNotificationDate(context).getTimeInMillis(), PendingIntent.getService(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT));
    }
}
