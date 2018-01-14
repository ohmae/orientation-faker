/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.orientationfaker;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

public class MainService extends Service {
    private static final String ACTION_START = "ACTION_START";
    private static final String ACTION_STOP = "ACTION_STOP";

    public static void start(@NonNull final Context context) {
        final Intent intent = new Intent(context, MainService.class);
        intent.setAction(ACTION_START);
        if (VERSION.SDK_INT >= VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    public static void stop(@NonNull final Context context) {
        if (!OrientationHelper.getInstance(context).isEnabled()) {
            return;
        }
        final Intent intent = new Intent(context, MainService.class);
        intent.setAction(ACTION_STOP);
        if (VERSION.SDK_INT >= VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(@Nullable final Intent intent, final int flags, final int startId) {
        if (!OverlayPermissionHelper.canDrawOverlays(this)) {
            stop();
            return START_NOT_STICKY;
        }
        if (intent != null && TextUtils.equals(intent.getAction(), ACTION_STOP)) {
            stop();
            return START_NOT_STICKY;
        }
        start();
        return START_STICKY;
    }

    private void start() {
        OrientationHelper.getInstance(this)
                .updateOrientation();
        NotificationHelper.startForeground(this);
        MainActivity.notifyUpdate(this);
    }

    private void stop() {
        OrientationHelper.getInstance(this).cancel();
        NotificationHelper.stopForeground(this);
        MainActivity.notifyUpdate(this);
        stopSelf();
    }
}
