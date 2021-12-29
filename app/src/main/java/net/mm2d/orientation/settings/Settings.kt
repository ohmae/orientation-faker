/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.settings

import android.content.Context
import net.mm2d.android.orientationfaker.BuildConfig
import net.mm2d.orientation.settings.Key.Main
import java.io.File

class Settings private constructor(
    private val preferences: SettingsPreferences<Main>
) {
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
            SettingsPreferences<Main>(context, Main.FILE_NAME).also {
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
