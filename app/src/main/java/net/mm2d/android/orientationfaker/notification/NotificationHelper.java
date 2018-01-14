/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.orientationfaker.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import net.mm2d.android.orientationfaker.MainActivity;
import net.mm2d.android.orientationfaker.R;
import net.mm2d.android.orientationfaker.orientation.OrientationIdManager;
import net.mm2d.android.orientationfaker.orientation.OrientationIdManager.OrientationId;
import net.mm2d.android.orientationfaker.orientation.OrientationReceiver;
import net.mm2d.android.orientationfaker.settings.Settings;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class NotificationHelper {
    private static final String CHANNEL_ID = "CHANNEL_ID";
    private static final int NOTIFICATION_ID = 10;

    private static void createChannel(@NonNull final Context context) {
        if (VERSION.SDK_INT >= VERSION_CODES.O) {
            final String name = context.getString(R.string.notification_channel_name);
            final NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name,
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.enableLights(false);
            channel.enableVibration(false);
            final NotificationManager manager = (NotificationManager) context
                    .getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    public static void startForeground(@NonNull final Service service) {
        createChannel(service);
        service.startForeground(NOTIFICATION_ID, makeNotification(service));
    }

    public static void stopForeground(@NonNull final Service service) {
        service.stopForeground(true);
    }

    private static Notification makeNotification(@NonNull final Context context) {
        final int orientation = new Settings(context).getOrientation();
        final RemoteViews remoteViews = createRemoteViews(context, orientation);
        return new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(context.getText(R.string.app_name))
                .setSmallIcon(OrientationIdManager.getIconIdFromOrientation(orientation))
                .setOngoing(true)
                .setCustomContentView(remoteViews)
                .build();
    }

    private static RemoteViews createRemoteViews(@NonNull final Context context, final int orientation) {
        final RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.notification);
        for (final OrientationId id : OrientationIdManager.getList()) {
            remoteViews.setOnClickPendingIntent(id.getViewId(), createOrientationIntent(context, id.getOrientation()));
            final int drawableId = orientation == id.getOrientation() ? R.drawable.bg_icon_selected : R.drawable.bg_icon;
            remoteViews.setInt(id.getViewId(), "setBackgroundResource", drawableId);
        }
        remoteViews.setOnClickPendingIntent(R.id.button_settings, createActivityIntent(context));
        return remoteViews;
    }

    private static PendingIntent createOrientationIntent(@NonNull final Context context, final int orientation) {
        final Intent intent = new Intent(OrientationReceiver.ACTION_ORIENTATION);
        intent.putExtra(OrientationReceiver.EXTRA_ORIENTATION, orientation);
        intent.setClass(context, OrientationReceiver.class);
        return PendingIntent.getBroadcast(context, orientation, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static PendingIntent createActivityIntent(@NonNull final Context context) {
        final Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return PendingIntent.getActivity(context, 100, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
