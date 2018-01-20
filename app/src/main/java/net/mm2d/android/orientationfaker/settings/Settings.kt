/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.orientationfaker.settings

import android.content.Context
import android.content.pm.ActivityInfo

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class Settings(context: Context) {
    private val storage: SettingsStorage = SettingsStorage(context)

    var orientation: Int
        get() = verifyOrientation(storage.readInt(Key.ORIENTATION, ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED))
        set(orientation) = storage.writeInt(Key.ORIENTATION, verifyOrientation(orientation))

    fun setResident(resident: Boolean) {
        storage.writeBoolean(Key.RESIDENT, resident)
    }

    fun shouldResident(): Boolean {
        return storage.readBoolean(Key.RESIDENT, false)
    }

    companion object {
        /**
         * アプリ起動時に一度だけコールされ、初期化を行う。
         *
         * @param context コンテキスト
         */
        fun initialize(context: Context) {
            SettingsStorage.initialize(context)
        }

        private fun verifyOrientation(orientation: Int): Int {
            return when (orientation) {
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT,
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE,
                ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT,
                ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE,
                ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED -> orientation
                else -> ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
        }
    }
}
