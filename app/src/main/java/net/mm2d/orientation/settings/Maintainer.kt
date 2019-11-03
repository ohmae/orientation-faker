/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.settings

import android.content.pm.ActivityInfo
import net.mm2d.android.orientationfaker.BuildConfig

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
internal object Maintainer {
    // 0 : 1.0.0
    // 1 : 2.0.0 - 2.1.2
    // 2 : 2.2.0 -
    private const val SETTINGS_VERSION = 2

    fun maintain(storage: SettingsStorage) {
        if (storage.readInt(Key.APP_VERSION_AT_LAST_LAUNCHED, 0) != BuildConfig.VERSION_CODE) {
            storage.writeInt(Key.APP_VERSION_AT_LAST_LAUNCHED, BuildConfig.VERSION_CODE)
        }

        storage.readInt(Key.SETTINGS_VERSION, -1).let {
            when {
                it < 1 -> {
                    storage.clear()
                    storage.writeInt(Key.APP_VERSION_AT_INSTALL, BuildConfig.VERSION_CODE, false)
                }
                it == 1 ->
                    // 2.0.0-2.1.2の間が不明のため、2.1.2として書き込む
                    storage.writeInt(Key.APP_VERSION_AT_INSTALL, 20102, false)
                it == SETTINGS_VERSION ->
                    return
            }
        }
        storage.writeInt(Key.SETTINGS_VERSION, SETTINGS_VERSION)
        storage.writeInt(Key.APP_VERSION_AT_LAST_LAUNCHED, BuildConfig.VERSION_CODE)

        writeDefaultValue(storage, false)
    }

    /**
     * デフォルト値の書き込みを行う
     *
     * @param storage SettingsStorage
     */
    private fun writeDefaultValue(storage: SettingsStorage, overwrite: Boolean) {
        storage.writeInt(Key.ORIENTATION, ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED, overwrite)
        storage.writeBoolean(Key.RESIDENT, false, overwrite)
        storage.writeInt(Key.COLOR_FOREGROUND, Default.color.foreground)
        storage.writeInt(Key.COLOR_BACKGROUND, Default.color.background)
        storage.writeInt(Key.COLOR_FOREGROUND_SELECTED, Default.color.foregroundSelected)
        storage.writeInt(Key.COLOR_BACKGROUND_SELECTED, Default.color.backgroundSelected)
        storage.writeBoolean(Key.NOTIFY_SECRET, false)
        storage.writeLong(Key.TIME_FIRST_USE, 0L)
        storage.writeLong(Key.TIME_FIRST_REVIEW, 0L)
        storage.writeInt(Key.COUNT_ORIENTATION_CHANGED, 0)
        storage.writeInt(Key.COUNT_REVIEW_DIALOG_CANCELED, 0)
        storage.writeBoolean(Key.REVIEW_REPORTED, false)
        storage.writeBoolean(Key.REVIEW_REVIEWED, false)
        storage.writeBoolean(Key.AUTO_ROTATE_WARNING, true)
    }
}
