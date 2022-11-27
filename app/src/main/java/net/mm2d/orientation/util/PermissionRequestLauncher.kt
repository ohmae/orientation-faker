/*
 * Copyright (c) 2022 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.util

import android.app.Activity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment

interface PermissionRequestLauncher {
    fun launch(activity: Activity)

    companion object {
        val EMPTY = object : PermissionRequestLauncher {
            override fun launch(activity: Activity) = Unit
        }
    }
}

fun Fragment.registerForPermissionRequest(
    permission: String,
    callback: (granted: Boolean, failedToShowDialog: Boolean) -> Unit
): PermissionRequestLauncher = PermissionRequestLauncherImpl(permission).also { launcher ->
    launcher.launcher = registerForActivityResult(RequestPermission()) {
        callback(it, launcher.failedToShowDialog(requireActivity()))
    }
}

private class PermissionRequestLauncherImpl(
    private val permission: String
) : PermissionRequestLauncher {
    lateinit var launcher: ActivityResultLauncher<String>
    private var shouldShowRationaleBefore: Boolean = false
    private var start: Long = 0

    fun failedToShowDialog(activity: Activity): Boolean {
        val elapsedEnoughTime = System.currentTimeMillis() - start > ENOUGH_DURATION
        val shouldShowRationaleAfter = ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
        return !(shouldShowRationaleBefore || shouldShowRationaleAfter || elapsedEnoughTime)
    }

    override fun launch(activity: Activity) {
        start = System.currentTimeMillis()
        shouldShowRationaleBefore =
            ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
        launcher.launch(permission)
    }

    companion object {
        private const val ENOUGH_DURATION = 300L
    }
}
