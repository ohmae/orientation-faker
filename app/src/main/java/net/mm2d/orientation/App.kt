/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation

import android.app.Application
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.os.StrictMode.VmPolicy
import io.reactivex.exceptions.OnErrorNotImplementedException
import io.reactivex.exceptions.UndeliverableException
import io.reactivex.plugins.RxJavaPlugins
import net.mm2d.android.orientationfaker.BuildConfig
import net.mm2d.log.Logger
import net.mm2d.log.android.AndroidSenders
import net.mm2d.orientation.control.OrientationHelper
import net.mm2d.orientation.service.KeepAlive
import net.mm2d.orientation.settings.Settings
import net.mm2d.orientation.tabs.CustomTabsBinder
import net.mm2d.orientation.tabs.CustomTabsHelper

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
@Suppress("unused")
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        setUpLogger()
        setStrictMode()
        RxJavaPlugins.setErrorHandler(::logError)
        Settings.initialize(this)
        CustomTabsHelper.initialize(this)
        registerActivityLifecycleCallbacks(CustomTabsBinder())
        KeepAlive.ensureResident(this)
        OrientationHelper.initialize(this)
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
