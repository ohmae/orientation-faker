/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.orientationfaker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

import net.mm2d.android.orientationfaker.settings.Settings;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class OrientationHelper {
    @SuppressLint("StaticFieldLeak")
    private static OrientationHelper sInstance;

    public static OrientationHelper getInstance(@NonNull final Context context) {
        if (sInstance == null) {
            sInstance = new OrientationHelper(context);
        }
        return sInstance;
    }

    private Context mContext;
    private Settings mSettings;
    private View mView;
    private WindowManager mWindowManager;
    private LayoutParams mLayoutParam;

    private OrientationHelper(@NonNull final Context context) {
        mContext = context.getApplicationContext();
        mSettings = new Settings(mContext);
        mView = new View(mContext);
        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        mLayoutParam = new WindowManager.LayoutParams(0, 0, 0, 0,
                getType(),
                LayoutParams.FLAG_NOT_FOCUSABLE
                        | LayoutParams.FLAG_NOT_TOUCHABLE
                        | LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
        mLayoutParam.gravity = Gravity.TOP;
        mLayoutParam.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
    }

    public boolean isEnabled() {
        return mView.getParent() != null;
    }

    private int getType() {
        return VERSION.SDK_INT >= VERSION_CODES.O ? LayoutParams.TYPE_APPLICATION_OVERLAY : LayoutParams.TYPE_SYSTEM_ALERT;
    }

    public void updateOrientation() {
        setOrientation(mSettings.getOrientation());
    }

    public void setOrientation(final int orientation) {
        mLayoutParam.screenOrientation = orientation;
        if (isEnabled()) {
            mWindowManager.updateViewLayout(mView, mLayoutParam);
        } else {
            mWindowManager.addView(mView, mLayoutParam);
        }
    }

    public void cancel() {
        if (isEnabled()) {
            mWindowManager.removeView(mView);
        }
    }
}
