/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.settings

import android.content.Context
import net.mm2d.android.orientationfaker.BuildConfig
import net.mm2d.orientation.control.Orientation
import net.mm2d.orientation.settings.Key.Main
import java.io.File

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class Settings private constructor(
    private val preferences: Preferences<Main>
) {
    var orientation: Int
        get() = preferences.readInt(Main.ORIENTATION_INT, Orientation.UNSPECIFIED)
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

    var foregroundPackage: String
        get() = preferences.readString(Main.FOREGROUND_PACKAGE_STRING, "")
        set(value) = preferences.writeString(Main.FOREGROUND_PACKAGE_STRING, value)

    var foregroundPackageCheckTime: Long
        get() = preferences.readLong(Main.FOREGROUND_PACKAGE_CHECK_TIME_LONG, 0L)
        set(value) = preferences.writeLong(Main.FOREGROUND_PACKAGE_CHECK_TIME_LONG, value)

    var foregroundPackageCheckEnabled: Boolean
        get() = preferences.readBoolean(Main.FOREGROUND_PACKAGE_ENABLED_BOOLEAN, true)
        set(value) = preferences.writeBoolean(Main.FOREGROUND_PACKAGE_ENABLED_BOOLEAN, value)

    var shouldUseRoundBackground: Boolean
        get() = preferences.readBoolean(Main.USE_ROUND_BACKGROUND_BOOLEAN, false)
        set(value) = preferences.writeBoolean(Main.USE_ROUND_BACKGROUND_BOOLEAN, value)

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
            tryMigrate(context)
            Preferences<Main>(context, Main.FILE_NAME).also {
                Maintainer.maintain(context, it)
                settings = Settings(it)
            }
        }

        private fun tryMigrate(context: Context) {
            // 2020/11/2 4.6.0
            runCatching {
                val prefsDir = File(context.filesDir.parent, "shared_prefs")
                val obfuscatedFile = File(prefsDir, BuildConfig.APPLICATION_ID + ".b.xml")
                val targetFile = File(prefsDir, BuildConfig.APPLICATION_ID + "." + Main.FILE_NAME + ".xml")
                if (targetFile.exists() || !obfuscatedFile.exists()) return
                obfuscatedFile.renameTo(targetFile)
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
