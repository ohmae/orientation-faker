/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.orientationfaker

import android.app.Application
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.os.StrictMode.VmPolicy
import io.reactivex.exceptions.OnErrorNotImplementedException
import io.reactivex.exceptions.UndeliverableException
import io.reactivex.plugins.RxJavaPlugins
import net.mm2d.android.orientationfaker.settings.Settings
import net.mm2d.log.Log
import net.mm2d.log.android.AndroidLogInitializer

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
@Suppress("unused")
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.setInitializer(AndroidLogInitializer.getSingleThread())
        Log.initialize(BuildConfig.DEBUG, true)
        setStrictMode()
        RxJavaPlugins.setErrorHandler { logError(it) }
        Settings.initialize(this)
    }

    private fun logError(e: Throwable) {
        when (e) {
            is UndeliverableException
            -> Log.w(null, "UndeliverableException:", e.cause)
            is OnErrorNotImplementedException
            -> Log.w(null, "OnErrorNotImplementedException:", e.cause)
            else
            -> Log.w(e)
        }
    }

    private fun setStrictMode() {
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .penaltyDropBox()
                    .penaltyDialog()
                    .build())
            StrictMode.setVmPolicy(VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .penaltyDropBox()
                    .build())
        } else {
            StrictMode.setThreadPolicy(ThreadPolicy.LAX)
            StrictMode.setVmPolicy(VmPolicy.LAX)
        }
    }
}
