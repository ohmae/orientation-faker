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
import dagger.hilt.android.HiltAndroidApp
import net.mm2d.orientation.control.ForegroundPackageSettings
import net.mm2d.orientation.control.OrientationHelper
import net.mm2d.orientation.service.MainService
import net.mm2d.orientation.service.PowerConnectionReceiver
import net.mm2d.orientation.settings.Default
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
        initializeOverrideWhenDebug()
        Default.initialize(this)
        MainService.initialize(this, preferenceRepository)
        NotificationHelper.createChannel(this)
        ForegroundPackageSettings.initialize(this)
        CustomTabsHelper.initialize(this)
        OrientationHelper.initialize(this, preferenceRepository)
        WidgetProvider.initialize(this, preferenceRepository)
        PowerConnectionReceiver.initialize(this, preferenceRepository)
    }

    protected open fun initializeOverrideWhenDebug() {
        setUpStrictMode()
    }

    private fun setUpStrictMode() {
        StrictMode.setThreadPolicy(ThreadPolicy.LAX)
        StrictMode.setVmPolicy(VmPolicy.LAX)
    }
}
