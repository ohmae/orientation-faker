/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.orientationfaker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.text.TextUtils;

import net.mm2d.android.orientationfaker.settings.Settings;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class OrientationReceiver extends BroadcastReceiver {
    static final String ACTION_ORIENTATION = "net.mm2d.android.orientationfaker.ACTION_ORIENTATION";
    static final String EXTRA_ORIENTATION = "EXTRA_ORIENTATION";

    @Override
    public void onReceive(final Context context, final Intent intent) {
        if (!TextUtils.equals(intent.getAction(), ACTION_ORIENTATION)) {
            return;
        }
        final int orientation = intent.getIntExtra(EXTRA_ORIENTATION, ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        final Settings settings = new Settings(context);
        settings.setOrientation(orientation);
        OrientationHelper.getInstance(context)
                .setOrientation(settings.getOrientation());
    }
}
