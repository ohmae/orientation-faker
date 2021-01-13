/*
 * Copyright (c) 2020 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.util

import android.app.Activity
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.clientVersionStalenessDays
import com.google.android.play.core.ktx.isImmediateUpdateAllowed

object Updater {
    private const val DAYS_FOR_UPDATE: Int = 2

    fun startUpdateIfAvailable(activity: Activity) {
        val manager = AppUpdateManagerFactory.create(activity.applicationContext)
        manager.appUpdateInfo
            .addOnSuccessListener {
                if (it.isAvailable()) {
                    runCatching {
                        manager.startUpdateFlow(it, activity, AppUpdateOptions.defaultOptions(AppUpdateType.IMMEDIATE))
                    }
                }
            }
    }

    private fun AppUpdateInfo.isAvailable(): Boolean =
        updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
            clientVersionStalenessDays.let { it != null && it >= DAYS_FOR_UPDATE } &&
            isImmediateUpdateAllowed
}
