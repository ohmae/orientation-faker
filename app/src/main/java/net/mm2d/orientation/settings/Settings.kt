/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.settings

import android.content.Context
import android.content.pm.ActivityInfo
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import net.mm2d.android.orientationfaker.BuildConfig
import net.mm2d.log.Log
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class Settings private constructor(context: Context) {
    private val storage: SettingsStorage = SettingsStorage(context)

    var orientation: Int
        get() = verifyOrientation(
            storage.readInt(
                Key.ORIENTATION,
                ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            )
        )
        set(orientation) = storage.writeInt(Key.ORIENTATION, verifyOrientation(orientation))

    fun setResident(resident: Boolean) {
        storage.writeBoolean(Key.RESIDENT, resident)
    }

    fun shouldResident(): Boolean {
        return storage.readBoolean(Key.RESIDENT, false)
    }

    companion object {
        private var settings: Settings? = null
        private val lock: Lock = ReentrantLock()
        private val condition: Condition = lock.newCondition()!!

        /**
         * Settingsのインスタンスを返す。
         *
         * 初期化が完了していなければブロックされる。
         */
        fun get(): Settings {
            lock.withLock {
                while (settings == null) {
                    if (BuildConfig.DEBUG) {
                        Log.e("!!!!!!!!!! BLOCK !!!!!!!!!!")
                    }
                    if (!condition.await(4, TimeUnit.SECONDS)) {
                        throw IllegalStateException("Settings initialization timeout")
                    }
                }
                return settings!!
            }
        }

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
            val storage = SettingsStorage(context)
            Maintainer.maintain(storage)
            lock.withLock {
                settings = Settings(context)
                condition.signalAll()
            }
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
