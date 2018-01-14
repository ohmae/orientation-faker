/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.orientationfaker.settings;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.support.annotation.NonNull;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class Settings {
    /**
     * アプリ起動時に一度だけコールされ、初期化を行う。
     *
     * @param context コンテキスト
     */
    public static void initialize(@NonNull final Context context) {
        SettingsStorage.initialize(context);
    }

    @NonNull
    private final SettingsStorage mStorage;

    /**
     * インスタンス作成。
     *
     * @param context コンテキスト
     */
    public Settings(@NonNull final Context context) {
        mStorage = new SettingsStorage(context);
    }

    public void setOrientation(final int orientation) {
        mStorage.writeInt(Key.ORIENTATION, verifyOrientation(orientation));
    }

    public int getOrientation() {
        final int orientation = mStorage.readInt(Key.ORIENTATION, ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        return verifyOrientation(orientation);
    }

    private static int verifyOrientation(final int orientation) {
        switch (orientation) {
            case ActivityInfo.SCREEN_ORIENTATION_PORTRAIT:
            case ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE:
            case ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT:
            case ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE:
            case ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED:
                return orientation;
        }
        return ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
    }

    public void setResident(final boolean resident) {
        mStorage.writeBoolean(Key.RESIDENT, resident);
    }

    public boolean shouldResident() {
        return mStorage.readBoolean(Key.RESIDENT, false);
    }
}
