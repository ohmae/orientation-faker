/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation

import android.app.ActivityManager
import android.app.Application
import android.os.Build
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.os.StrictMode.VmPolicy
import androidx.core.content.getSystemService
import dagger.hilt.android.HiltAndroidApp
import net.mm2d.orientation.control.ControlStatusReceiver
import net.mm2d.orientation.control.ForegroundPackageReceiver
import net.mm2d.orientation.control.ForegroundPackageSettings
import net.mm2d.orientation.service.MainService
import net.mm2d.orientation.service.PowerConnectionReceiver
import net.mm2d.orientation.settings.PreferenceRepository
import net.mm2d.orientation.tabs.CustomTabsHelper
import net.mm2d.orientation.view.notification.NotificationHelper
import net.mm2d.orientation.view.widget.WidgetProvider
import javax.inject.Inject

@HiltAndroidApp
@Suppress("unused")
open class App : Application() {
    @Inject
    lateinit var preferenceRepository: PreferenceRepository

    override fun onCreate() {
        super.onCreate()
        if (!isMainProcess()) return
        initializeOverrideWhenDebug()
        MainService.initialize(this, preferenceRepository)
        NotificationHelper.createChannel(this)
        ForegroundPackageSettings.initialize(this)
        CustomTabsHelper.initialize(this)
        WidgetProvider.initialize(this, preferenceRepository)
        PowerConnectionReceiver.initialize(this, preferenceRepository)
        ControlStatusReceiver.register(this)
        ForegroundPackageReceiver.register(this)
    }

    protected open fun initializeOverrideWhenDebug() {
        setUpStrictMode()
    }

    private fun setUpStrictMode() {
        StrictMode.setThreadPolicy(ThreadPolicy.LAX)
        StrictMode.setVmPolicy(VmPolicy.LAX)
    }

    private fun isMainProcess(): Boolean = getCurrentProcessName() == packageName

    private fun getCurrentProcessName(): String =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getProcessName()
        } else {
            getSystemService<ActivityManager>()?.let {
                it.runningAppProcesses.find { processInfo ->
                    processInfo.pid == android.os.Process.myPid()
                }
            }?.processName ?: ""
        }
}
