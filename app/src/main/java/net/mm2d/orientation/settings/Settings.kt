/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.settings

import android.content.Context
import android.content.pm.ActivityInfo
import net.mm2d.orientation.settings.Key.Main

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class Settings private constructor(
    private val preferences: Preferences<Main>
) {
    var orientation: Int
        get() = preferences.readInt(Main.ORIENTATION_INT, ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
        set(value) = preferences.writeInt(Main.ORIENTATION_INT, value)

    var foregroundColor: Int
        get() = preferences.readInt(Main.COLOR_FOREGROUND_INT, Default.color.foreground)
        set(value) = preferences.writeInt(Main.COLOR_FOREGROUND_INT, value)

    var backgroundColor: Int
        get() = preferences.readInt(Main.COLOR_BACKGROUND_INT, Default.color.background)
        set(value) = preferences.writeInt(Main.COLOR_BACKGROUND_INT, value)

    var foregroundColorSelected: Int
        get() = preferences.readInt(Main.COLOR_FOREGROUND_SELECTED_INT, Default.color.foregroundSelected)
        set(value) = preferences.writeInt(Main.COLOR_FOREGROUND_SELECTED_INT, value)

    var backgroundColorSelected: Int
        get() = preferences.readInt(Main.COLOR_BACKGROUND_SELECTED_INT, Default.color.backgroundSelected)
        set(value) = preferences.writeInt(Main.COLOR_BACKGROUND_SELECTED_INT, value)

    var notifySecret: Boolean
        get() = preferences.readBoolean(Main.NOTIFY_SECRET_BOOLEAN, false)
        set(value) = preferences.writeBoolean(Main.NOTIFY_SECRET_BOOLEAN, value)

    var autoRotateWarning: Boolean
        get() = preferences.readBoolean(Main.AUTO_ROTATE_WARNING_BOOLEAN, true)
        set(value) = preferences.writeBoolean(Main.AUTO_ROTATE_WARNING_BOOLEAN, value)

    var reviewIntervalRandomFactor: Long
        get() = preferences.readLong(Main.REVIEW_INTERVAL_RANDOM_FACTOR_LONG, 0L)
        set(value) = preferences.writeLong(Main.REVIEW_INTERVAL_RANDOM_FACTOR_LONG, value)

    var firstUseTime: Long
        get() = preferences.readLong(Main.TIME_FIRST_USE_LONG, 0L)
        set(value) = preferences.writeLong(Main.TIME_FIRST_USE_LONG, value)

    var firstReviewTime: Long
        get() = preferences.readLong(Main.TIME_FIRST_REVIEW_LONG, 0L)
        set(value) = preferences.writeLong(Main.TIME_FIRST_REVIEW_LONG, value)

    var orientationChangeCount: Int
        get() = preferences.readInt(Main.COUNT_ORIENTATION_CHANGED_INT, 0)
        set(value) = preferences.writeInt(Main.COUNT_ORIENTATION_CHANGED_INT, value)

    var reviewCancelCount: Int
        get() = preferences.readInt(Main.COUNT_REVIEW_DIALOG_CANCELED_INT, 0)
        set(value) = preferences.writeInt(Main.COUNT_REVIEW_DIALOG_CANCELED_INT, value)

    var reviewed: Boolean
        get() = preferences.readBoolean(Main.REVIEW_REVIEWED_BOOLEAN, false)
        set(value) = preferences.writeBoolean(Main.REVIEW_REVIEWED_BOOLEAN, value)

    var reported: Boolean
        get() = preferences.readBoolean(Main.REVIEW_REPORTED_BOOLEAN, false)
        set(value) = preferences.writeBoolean(Main.REVIEW_REPORTED_BOOLEAN, value)

    var shouldUseBlankIconForNotification: Boolean
        get() = preferences.readBoolean(Main.USE_BLANK_ICON_FOR_NOTIFICATION_BOOLEAN, false)
        set(value) = preferences.writeBoolean(Main.USE_BLANK_ICON_FOR_NOTIFICATION_BOOLEAN, value)

    var orientationList: List<Int>
        get() = OrientationList.toList(preferences.readString(Main.ORIENTATION_LIST_STRING, "")).let {
            if (it.isEmpty()) Default.orientationList else it
        }
        set(value) = preferences.writeString(Main.ORIENTATION_LIST_STRING, OrientationList.toString(value))

    fun resetTheme() {
        foregroundColor = Default.color.foreground
        backgroundColor = Default.color.background
        foregroundColorSelected = Default.color.foregroundSelected
        backgroundColorSelected = Default.color.backgroundSelected
    }

    fun setAutoStart(autoStart: Boolean) {
        preferences.writeBoolean(Main.RESIDENT_BOOLEAN, autoStart)
    }

    fun shouldAutoStart(): Boolean {
        return preferences.readBoolean(Main.RESIDENT_BOOLEAN, false)
    }

    companion object {
        private lateinit var settings: Settings

        /**
         * アプリ起動時に一度だけコールされ、初期化を行う。
         *
         * @param context コンテキスト
         */
        fun initialize(context: Context) {
            Default.init(context)
            Preferences(context, Main::class).also {
                Maintainer.maintain(context, it)
                settings = Settings(it)
            }
        }

        /**
         * Settingsのインスタンスを返す。
         *
         * 初期化が完了していなければブロックされる。
         */
        fun get(): Settings = settings
    }
}
