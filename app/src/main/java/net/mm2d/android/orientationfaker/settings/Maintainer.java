/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.orientationfaker.settings;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
class Maintainer {
    private static final int SETTINGS_VERSION = 1;

    static void maintain(@NonNull final SettingsStorage storage) {
        final int currentVersion = getSettingsVersion(storage);
        if (currentVersion == SETTINGS_VERSION) {
            return;
        }
        if (currentVersion == 0) {
            migrateFrom0(storage);
        }
        storage.writeInt(Key.SETTINGS_VERSION, SETTINGS_VERSION);
    }

    private static int getSettingsVersion(@NonNull final SettingsStorage storage) {
        //noinspection deprecation
        if (storage.getPreferences().contains("mode")) {
            // バージョン番号を割り振る前の設定値が含まれている
            return 0;
        }
        return storage.readInt(Key.SETTINGS_VERSION, -1);
    }

    private static void migrateFrom0(@NonNull final SettingsStorage storage) {
        @SuppressWarnings("deprecation")
        final SharedPreferences pref = storage.getPreferences();
        final boolean resident = pref.getBoolean("startup", false);
        final int orientation = convertToOrientationInt(pref.getString("mode", null));
        storage.clear();
        storage.writeBoolean(Key.RESIDENT, resident);
        storage.writeInt(Key.ORIENTATION, orientation);
    }

    private static int convertToOrientationInt(@Nullable final String orientation) {
        if (TextUtils.isEmpty(orientation)) {
            return ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
        }
        switch (orientation) {
            case "portrait":
                return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
            case "landscape":
                return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
            case "r_portrait":
                return ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
            case "r_landscape":
                return ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
        }
        return ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
    }
}
