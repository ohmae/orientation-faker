/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.settings

import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Looper
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import net.mm2d.android.orientationfaker.BuildConfig
import net.mm2d.log.Logger
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class Settings private constructor(
    private val storage: SettingsStorage
) {
    var orientation: Int
        get() = verifyOrientation(
            storage.readInt(
                Key.ORIENTATION,
                ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            )
        )
        set(orientation) = storage.writeInt(Key.ORIENTATION, verifyOrientation(orientation))

    var foregroundColor: Int
        get() = storage.readInt(Key.COLOR_FOREGROUND, Default.color.foreground)
        set(value) = storage.writeInt(Key.COLOR_FOREGROUND, value)

    var backgroundColor: Int
        get() = storage.readInt(Key.COLOR_BACKGROUND, Default.color.background)
        set(value) = storage.writeInt(Key.COLOR_BACKGROUND, value)

    var foregroundColorSelected: Int
        get() = storage.readInt(Key.COLOR_FOREGROUND_SELECTED, Default.color.foregroundSelected)
        set(value) = storage.writeInt(Key.COLOR_FOREGROUND_SELECTED, value)

    var backgroundColorSelected: Int
        get() = storage.readInt(Key.COLOR_BACKGROUND_SELECTED, Default.color.backgroundSelected)
        set(value) = storage.writeInt(Key.COLOR_BACKGROUND_SELECTED, value)

    var notifySecret: Boolean
        get() = storage.readBoolean(Key.NOTIFY_SECRET, false)
        set(value) = storage.writeBoolean(Key.NOTIFY_SECRET, value)

    var useFullSensor: Boolean
        get() = storage.readBoolean(Key.USE_FULL_SENSOR, false)
        set(value) = storage.writeBoolean(Key.USE_FULL_SENSOR, value)

    var firstUseTime: Long
        get() = storage.readLong(Key.TIME_FIRST_USE, 0L)
        set(value) = storage.writeLong(Key.TIME_FIRST_USE, value)

    var firstReviewTime: Long
        get() = storage.readLong(Key.TIME_FIRST_USE, 0L)
        set(value) = storage.writeLong(Key.TIME_FIRST_USE, value)

    var orientationChangeCount: Int
        get() = storage.readInt(Key.COUNT_ORIENTATION_CHANGED, 0)
        set(value) = storage.writeInt(Key.COUNT_ORIENTATION_CHANGED, value)

    var reviewCancelCount: Int
        get() = storage.readInt(Key.COUNT_REVIEW_DIALOG_CANCELED, 0)
        set(value) = storage.writeInt(Key.COUNT_REVIEW_DIALOG_CANCELED, value)

    var reviewed: Boolean
        get() = storage.readBoolean(Key.REVIEW_REVIEWED, false)
        set(value) = storage.writeBoolean(Key.REVIEW_REVIEWED, value)

    var reported: Boolean
        get() = storage.readBoolean(Key.REVIEW_REPORTED, false)
        set(value) = storage.writeBoolean(Key.REVIEW_REPORTED, value)

    fun resetTheme() {
        foregroundColor = Default.color.foreground
        backgroundColor = Default.color.background
        foregroundColorSelected = Default.color.foregroundSelected
        backgroundColorSelected = Default.color.backgroundSelected
    }

    fun setAutoStart(autoStart: Boolean) {
        storage.writeBoolean(Key.RESIDENT, autoStart)
    }

    fun shouldAutoStart(): Boolean {
        return storage.readBoolean(Key.RESIDENT, false)
    }

    companion object {
        private var settings: Settings? = null
        private val lock: Lock = ReentrantLock()
        private val condition: Condition = lock.newCondition()!!

        /**
         * アプリ起動時に一度だけコールされ、初期化を行う。
         *
         * @param context コンテキスト
         */
        fun initialize(context: Context) {
            Completable.fromAction { initializeInner(context) }
                .subscribeOn(Schedulers.io())
                .subscribe()
        }

        private fun initializeInner(context: Context) {
            Default.init(context)
            val storage = SettingsStorage(context)
            Maintainer.maintain(storage)
            lock.withLock {
                settings = Settings(storage)
                condition.signalAll()
            }
        }

        fun doOnGet(task: (Settings) -> Unit): Disposable {
            return Single.fromCallable { get() }
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(task) {
                    Thread.currentThread()
                        .uncaughtExceptionHandler
                        ?.uncaughtException(Thread.currentThread(), it)
                }
        }

        /**
         * Settingsのインスタンスを返す。
         *
         * 初期化が完了していなければブロックされる。
         */
        fun get(): Settings {
            settings?.let {
                return it
            }
            lock.withLock {
                while (settings == null) {
                    if (BuildConfig.DEBUG) {
                        Logger.e("!!!!!!!!!! BLOCK !!!!!!!!!!")
                    }
                    val timeout = if (isMainThread()) 4L else 40L
                    if (!condition.await(timeout, TimeUnit.SECONDS)) {
                        throw IllegalStateException("Settings initialization timeout")
                    }
                }
                return settings!!
            }
        }

        private fun isMainThread() = Looper.getMainLooper().thread == Thread.currentThread()

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
