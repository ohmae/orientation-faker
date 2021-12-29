/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.IBinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import net.mm2d.android.orientationfaker.R
import net.mm2d.orientation.control.ForegroundPackageChecker
import net.mm2d.orientation.control.ForegroundPackageSettings
import net.mm2d.orientation.control.OrientationHelper
import net.mm2d.orientation.settings.OrientationPreference
import net.mm2d.orientation.settings.PreferenceRepository
import net.mm2d.orientation.util.SystemSettings
import net.mm2d.orientation.util.Toaster
import net.mm2d.orientation.view.notification.NotificationHelper

class MainService : Service() {
    private var checker: ForegroundPackageChecker? = null
    private val job = SupervisorJob()
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main + job)
    private val controlPreferenceFlow = PreferenceRepository.get().controlPreferenceRepository.flow
    private val designPreferenceFlow = PreferenceRepository.get().designPreferenceRepository.flow
    private val orientationPreferenceFlow = PreferenceRepository.get().orientationPreferenceFlow
    private val packageNameFlow: MutableStateFlow<String> = MutableStateFlow("")

    override fun onBind(intent: Intent): IBinder? {
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        NotificationHelper.startForegroundEmpty(this)
        if (shouldStop(intent)) {
            isStarted = false
            stop()
            return START_NOT_STICKY
        }
        isStarted = true
        start()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        isStarted = false
        job.cancel()
    }

    private fun shouldStop(intent: Intent?): Boolean {
        if (!SystemSettings.canDrawOverlays(this)) {
            return true
        }
        return intent != null && intent.action == ACTION_STOP
    }

    private fun start() {
        scope.launch {
            packageNameFlow
                .map { ForegroundPackageSettings.get(it) }
                .collect {
                    PreferenceRepository.get().preferredOrientationFlow.emit(it)
                }
        }
        scope.launch {
            orientationPreferenceFlow.collect { preferences ->
                if (preferences.enabled) {
                    OrientationHelper.update(preferences.orientation, preferences.isLandscapeDevice)
                } else {
                    stop()
                }
                startForegroundChecker(preferences)
            }
        }
        scope.launch {
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
        NotificationHelper.stopForeground(this)
        OrientationHelper.cancel()
        stopForegroundChecker()
        stopSelf()
    }

    private fun startForegroundChecker(preference: OrientationPreference) {
        val disable = ForegroundPackageSettings.isEmpty() || !preference.shouldControlByForegroundApp
        if (checker != null) {
            if (disable) {
                stopForegroundChecker()
            }
            return
        }
        if (disable) return
        if (!SystemSettings.hasUsageAccessPermission(this)) {
            Toaster.showLong(this, R.string.toast_no_permission_to_usage_access)
            return
        }
        checker = ForegroundPackageChecker(this) {
            scope.launch { packageNameFlow.emit(it) }
        }.also { checker ->
            checker.start()
        }
    }

    private fun stopForegroundChecker() {
        checker?.destroy()
        checker = null
    }

    companion object {
        private const val ACTION_START = "ACTION_START"
        private const val ACTION_STOP = "ACTION_STOP"
        var isStarted: Boolean = false
            private set

        fun start(context: Context) {
            startService(
                context,
                ACTION_START
            )
        }

        fun stop(context: Context) {
            if (!isStarted) {
                return
            }
            startService(
                context,
                ACTION_STOP
            )
        }

        private fun startService(context: Context, action: String) {
            val intent =
                makeIntent(context, action)
            if (VERSION.SDK_INT >= VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        private fun makeIntent(context: Context, action: String) =
            Intent(context, MainService::class.java).also {
                it.action = action
            }
    }
}
