/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.orientationfaker;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import net.mm2d.android.orientationfaker.settings.Settings;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class NotificationHelper {
    private static final String CHANNEL_ID = "CHANNEL_ID";
    private static final int NOTIFICATION_ID = 0;

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

    public static void update(@NonNull final Context context) {
        final Context applicationContext = context.getApplicationContext();
        createChannel(applicationContext);
        notify(applicationContext);
    }

    public static void cancel(@NonNull final Context context) {
        final Context applicationContext = context.getApplicationContext();
        final NotificationManager manager = (NotificationManager) applicationContext
                .getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.cancel(NOTIFICATION_ID);
        }
    }

    private static void notify(@NonNull final Context context) {
        final RemoteViews remoteViews = createRemoteViews(context);
        final Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(getActiveIconResource(context))
                .setOngoing(true)
                .setCustomContentView(remoteViews)
                .build();
        final NotificationManager manager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, notification);
        }
    }

    private static RemoteViews createRemoteViews(@NonNull final Context context) {
        final RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.notification);
        remoteViews.setOnClickPendingIntent(R.id.button_unspecified,
                createOrientationIntent(context, ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED));
        remoteViews.setOnClickPendingIntent(R.id.button_portrait,
                createOrientationIntent(context, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT));
        remoteViews.setOnClickPendingIntent(R.id.button_landscape,
                createOrientationIntent(context, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE));
        remoteViews.setOnClickPendingIntent(R.id.button_reverse_portrait,
                createOrientationIntent(context, ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT));
        remoteViews.setOnClickPendingIntent(R.id.button_reverse_landscape,
                createOrientationIntent(context, ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE));
        remoteViews.setOnClickPendingIntent(R.id.button_settings, createActivityIntent(context));
        remoteViews.setInt(R.id.button_unspecified, "setBackgroundResource", R.drawable.bg_icon);
        remoteViews.setInt(R.id.button_portrait, "setBackgroundResource", R.drawable.bg_icon);
        remoteViews.setInt(R.id.button_landscape, "setBackgroundResource", R.drawable.bg_icon);
        remoteViews.setInt(R.id.button_reverse_portrait, "setBackgroundResource", R.drawable.bg_icon);
        remoteViews.setInt(R.id.button_reverse_landscape, "setBackgroundResource", R.drawable.bg_icon);
        remoteViews.setInt(getActiveId(context), "setBackgroundResource", R.drawable.bg_icon_selected);
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

    private static int getActiveId(@NonNull final Context context) {
        final Settings settings = new Settings(context);
        switch (settings.getOrientation()) {
            case ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED:
                return R.id.button_unspecified;
            case ActivityInfo.SCREEN_ORIENTATION_PORTRAIT:
                return R.id.button_portrait;
            case ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE:
                return R.id.button_landscape;
            case ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT:
                return R.id.button_reverse_portrait;
            case ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE:
                return R.id.button_reverse_landscape;
        }
        return R.id.button_unspecified;
    }

    @DrawableRes
    private static int getActiveIconResource(@NonNull final Context context) {
        final Settings settings = new Settings(context);
        switch (settings.getOrientation()) {
            case ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED:
                return R.drawable.ic_unspecified;
            case ActivityInfo.SCREEN_ORIENTATION_PORTRAIT:
                return R.drawable.ic_portrait;
            case ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE:
                return R.drawable.ic_landscape;
            case ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT:
                return R.drawable.ic_reverse_portrait;
            case ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE:
                return R.drawable.ic_reverse_landscape;
        }
        return R.drawable.ic_unspecified;
    }
}
