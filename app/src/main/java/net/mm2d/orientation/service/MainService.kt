/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.service

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import net.mm2d.android.orientationfaker.R
import net.mm2d.orientation.control.ForegroundPackageChecker
import net.mm2d.orientation.control.ForegroundPackageSettings
import net.mm2d.orientation.control.OrientationHelper
import net.mm2d.orientation.settings.ControlPreference
import net.mm2d.orientation.settings.DesignPreference
import net.mm2d.orientation.settings.OrientationPreference
import net.mm2d.orientation.settings.PreferenceRepository
import net.mm2d.orientation.util.SystemSettings
import net.mm2d.orientation.util.Toaster
import net.mm2d.orientation.view.notification.NotificationHelper
import javax.inject.Inject

@AndroidEntryPoint
class MainService : LifecycleService() {
    @Inject
    lateinit var preferenceRepository: PreferenceRepository
    private val controlPreferenceFlow: Flow<ControlPreference> by lazy {
        preferenceRepository.controlPreferenceRepository.flow
    }
    private val designPreferenceFlow: Flow<DesignPreference> by lazy {
        preferenceRepository.designPreferenceRepository.flow
    }
    private val orientationPreferenceFlow: Flow<OrientationPreference> by lazy {
        preferenceRepository.orientationPreferenceFlow
    }
    private val packageNameFlow: MutableStateFlow<String> = MutableStateFlow("")
    private var checker: ForegroundPackageChecker? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        NotificationHelper.startForegroundEmpty(this)
        if (!SystemSettings.canDrawOverlays(this)) {
            stop()
            return START_NOT_STICKY
        }
        start()
        return START_STICKY
    }

    override fun onDestroy() {
        startedFlow.value = false
        super.onDestroy()
    }

    private fun start() {
        startedFlow.value = true
        lifecycleScope.launch {
            packageNameFlow
                .map { ForegroundPackageSettings.get(it) }
                .collect {
                    preferenceRepository.updatePackageOrientation(it)
                }
        }
        lifecycleScope.launch {
            orientationPreferenceFlow.collect { preferences ->
                if (preferences.enabled) {
                    OrientationHelper.update(preferences.orientation, preferences.isLandscapeDevice)
                } else {
                    stop()
                }
            }
        }
        lifecycleScope.launch {
            combine(
                preferenceRepository.orientationPreferenceRepository.flow,
                ForegroundPackageSettings.emptyFlow(),
            ) { orientation: OrientationPreference, empty: Boolean ->
                orientation.enabled && orientation.shouldControlByForegroundApp && !empty
            }.collect {
                startForegroundChecker(it)
            }
        }
        lifecycleScope.launch {
            combine(
                orientationPreferenceFlow,
                controlPreferenceFlow,
                designPreferenceFlow,
                ::Triple
            ).collect { (orientation, control, design) ->
                NotificationHelper.startForeground(this@MainService, orientation, control, design)
            }
        }
    }

    private fun stop() {
        startedFlow.value = false
        NotificationHelper.stopForeground(this)
        OrientationHelper.cancel()
        stopForegroundChecker()
        stopSelf()
    }

    private fun startForegroundChecker(enable: Boolean) {
        if (checker != null) {
            if (!enable) {
                stopForegroundChecker()
            }
            return
        }
        if (!enable) return
        if (!SystemSettings.hasUsageAccessPermission(this)) {
            Toaster.showLong(this, R.string.toast_no_permission_to_usage_access)
            return
        }
        checker = ForegroundPackageChecker(this) {
            packageNameFlow.value = it
        }.also { checker ->
            checker.start()
        }
    }

    private fun stopForegroundChecker() {
        checker?.destroy()
        checker = null
    }

    companion object {
        val startedFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
        val isStarted: Boolean
            get() = startedFlow.value

        fun initialize(c: Context, preferenceRepository: PreferenceRepository) {
            val context = c.applicationContext
            preferenceRepository.scope.launch {
                preferenceRepository
                    .orientationPreferenceRepository
                    .flow
                    .collect {
                        if (it.enabled && !isStarted) {
                            runCatching { start(context) }
                        }
                    }
            }
        }

        private fun start(context: Context) {
            val intent = Intent(context, MainService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }
}
