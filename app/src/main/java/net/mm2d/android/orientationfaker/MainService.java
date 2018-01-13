/**
 * Copyright(C) 2014 大前良介(OHMAE Ryosuke)
 */

package net.mm2d.android.orientationfaker;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

public class MainService extends Service {
    private static boolean running = false;

    public static boolean isRunning() {
        return running;
    }

    private View view;
    private WindowManager windowManager;
    private WindowManager.LayoutParams layoutParam;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        running = true;
        view = new View(this);
        windowManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        layoutParam = new WindowManager.LayoutParams(0, 0, 0, 0,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
        layoutParam.gravity = Gravity.TOP;
        layoutParam.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
        final Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_FROM_BACKGROUND);
        final PendingIntent p = PendingIntent.getActivity(this, 0, intent, 0);
        final NotificationManager notificationManager = (NotificationManager) this
                .getSystemService(NOTIFICATION_SERVICE);
        final Notification notification = new NotificationCompat.Builder(getApplicationContext())
        .setContentTitle(getText(R.string.app_name))
        .setSmallIcon(R.drawable.ic_launcher)
        .setOngoing(true)
        .setContentIntent(p)
        .build();
        notificationManager.notify(R.string.app_name, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        running = false;
        if (view.getParent() != null) {
            windowManager.removeView(view);
        }
        final NotificationManager notificationManager = (NotificationManager) this
                .getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(R.string.app_name);
    }

    @SuppressLint("InlinedApi")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        final String temp = pref.getString("mode", "portrait");
        if ("portrait".equals(temp)) {
            layoutParam.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        } else if ("landscape".equals(temp)) {
            layoutParam.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        } else if ("r_portrait".equals(temp)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                layoutParam.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
            } else {
                layoutParam.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
            }
        } else if ("r_landscape".equals(temp)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                layoutParam.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
            } else {
                layoutParam.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
            }
        } else {
            layoutParam.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        }
        if (view.getParent() != null) {
            windowManager.updateViewLayout(view, layoutParam);
        } else {
            windowManager.addView(view, layoutParam);
        }
        return super.onStartCommand(intent, flags, startId);
    }
}
