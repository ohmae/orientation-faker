/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation

import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.os.StrictMode.VmPolicy
import androidx.multidex.MultiDexApplication
import com.squareup.leakcanary.LeakCanary
import io.reactivex.exceptions.OnErrorNotImplementedException
import io.reactivex.exceptions.UndeliverableException
import io.reactivex.plugins.RxJavaPlugins
import net.mm2d.android.orientationfaker.BuildConfig
import net.mm2d.log.Logger
import net.mm2d.log.android.AndroidSenders
import net.mm2d.orientation.settings.Settings
import net.mm2d.orientation.tabs.CustomTabsBinder
import net.mm2d.orientation.tabs.CustomTabsHelper

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
@Suppress("unused")
class App : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return
        }
        LeakCanary.install(this)
        setUpLogger()
        setStrictMode()
        RxJavaPlugins.setErrorHandler(::logError)
        Settings.initialize(this)
        UpdateRouter.initialize(this)
        CustomTabsHelper.init(this)
        registerActivityLifecycleCallbacks(CustomTabsBinder())
    }

    private fun logError(e: Throwable) {
        when (e) {
            is UndeliverableException
            -> Logger.w(e.cause, "UndeliverableException:")
            is OnErrorNotImplementedException
            -> Logger.w(e.cause, "OnErrorNotImplementedException:")
            else
            -> Logger.w(e)
        }
    }

    private fun setUpLogger() {
        if (!BuildConfig.DEBUG) {
            return
        }
        Logger.setSender(AndroidSenders.create())
        Logger.setLogLevel(Logger.VERBOSE)
        AndroidSenders.appendCaller(true)
        AndroidSenders.appendThread(true)
    }

    private fun setStrictMode() {
        if (BuildConfig.DEBUG) {
            StrictMode.enableDefaults()
        } else {
            StrictMode.setThreadPolicy(ThreadPolicy.LAX)
            StrictMode.setVmPolicy(VmPolicy.LAX)
        }
    }
}
