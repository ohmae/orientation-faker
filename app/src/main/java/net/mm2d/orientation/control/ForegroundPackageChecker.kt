/*
 * Copyright (c) 2020 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.control

import android.annotation.SuppressLint
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.Looper
import net.mm2d.orientation.util.Powers
import net.mm2d.orientation.util.SystemSettings
import java.util.concurrent.TimeUnit

class ForegroundPackageChecker(
    private val context: Context,
    private val onChangeForegroundPackage: (String) -> Unit
) {
    @SuppressLint("InlinedApi")
    private val usageStatsManager: UsageStatsManager =
        context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    private val handler: Handler = Handler(Looper.getMainLooper())
    private val checkTask: Runnable = object : Runnable {
        override fun run() {
            if (!enabled) return
            check()
            handler.postDelayed(this, CHECK_INTERVAL)
        }
    }
    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (!enabled) return
            handler.removeCallbacks(checkTask)
            if (intent.action == Intent.ACTION_SCREEN_ON) {
                handler.postDelayed(checkTask, CHECK_INTERVAL)
            }
        }
    }
    private var enabled: Boolean = true

    fun start() {
        context.registerReceiver(broadcastReceiver, IntentFilter().also {
            it.addAction(Intent.ACTION_SCREEN_ON)
            it.addAction(Intent.ACTION_SCREEN_OFF)
        })
        check()
        onChangeForegroundPackage(foregroundPackage)
        if (Powers.isInteractive(context)) {
            handler.postDelayed(checkTask, CHECK_INTERVAL)
        }
    }

    fun destroy() {
        enabled = false
        context.unregisterReceiver(broadcastReceiver)
        onChangeForegroundPackage("")
    }

    private fun check() {
        if (!SystemSettings.hasUsageAccessPermission(context)) {
            return
        }
        val lastCheckTime = foregroundPackageCheckTime - CHECK_MARGIN
        val lastPackage = foregroundPackage
        val now = System.currentTimeMillis()
        foregroundPackageCheckTime = now
        val packageName = searchLatestForegroundPackage(lastCheckTime, now) ?: return
        if (packageName == lastPackage) return
        foregroundPackage = packageName
        onChangeForegroundPackage(packageName)
    }

    private fun searchLatestForegroundPackage(before: Long, after: Long): String? =
        if (after - before < FIRST_DURATION) {
            getLatestForegroundPackage(before, after)
        } else {
            val firstPeriod = after - FIRST_DURATION
            val secondPeriod = maxOf(before, after - SECOND_DURATION)
            getLatestForegroundPackage(firstPeriod, after)
                ?: getLatestForegroundPackage(secondPeriod, firstPeriod)
        }

    private fun getLatestForegroundPackage(before: Long, after: Long): String? =
        usageStatsManager.queryEvents(before, after)
            .asResumedPackageSequence()
            .lastOrNull()

    @SuppressLint("InlinedApi")
    private fun UsageEvents.asResumedPackageSequence(): Sequence<String> =
        sequence {
            val event = UsageEvents.Event()
            while (getNextEvent(event)) {
                if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED && event.packageName != "android") {
                    yield(event.packageName)
                }
            }
        }

    companion object {
        private val CHECK_INTERVAL: Long = TimeUnit.SECONDS.toMillis(1)
        private val FIRST_DURATION: Long = TimeUnit.MINUTES.toMillis(5)
        private val SECOND_DURATION: Long = TimeUnit.HOURS.toMillis(1)
        private const val CHECK_MARGIN = 2000

        private var foregroundPackageCheckTime: Long = 0L
        private var foregroundPackage: String = ""
    }
}
