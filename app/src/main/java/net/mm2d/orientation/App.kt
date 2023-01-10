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
import net.mm2d.orientation.control.ControlStatusReceiver
import net.mm2d.orientation.control.ForegroundPackageReceiver
import net.mm2d.orientation.control.ForegroundPackageSettings
import net.mm2d.orientation.service.MainService
import net.mm2d.orientation.service.PowerConnectionReceiver
import net.mm2d.orientation.settings.PreferenceRepository
import net.mm2d.orientation.settings.WidgetSettingsRepository
import net.mm2d.orientation.tabs.CustomTabsHelper
import net.mm2d.orientation.view.notification.NotificationHelper
import net.mm2d.orientation.view.widget.CustomWidgetProvider
import net.mm2d.orientation.view.widget.WidgetProvider
import javax.inject.Inject

@HiltAndroidApp
@Suppress("unused")
open class App : Application() {
    @Inject
    lateinit var preferenceRepository: PreferenceRepository

    @Inject
    lateinit var foregroundPackageSettings: ForegroundPackageSettings

    @Inject
    lateinit var widgetSettingsRepository: WidgetSettingsRepository

    override fun onCreate() {
        super.onCreate()
        initializeOverrideWhenDebug()
        MainService.initialize(this, preferenceRepository, foregroundPackageSettings)
        NotificationHelper.createChannel(this)
        CustomTabsHelper.initialize(this)
        WidgetProvider.initialize(this, preferenceRepository)
        CustomWidgetProvider.initialize(this, preferenceRepository, widgetSettingsRepository)
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
}
