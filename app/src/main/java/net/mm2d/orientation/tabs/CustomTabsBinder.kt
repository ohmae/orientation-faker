/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.tabs

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class CustomTabsBinder : ActivityLifecycleCallbacks {
    private var count: Int = 0
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        count++
        CustomTabsHelper.bind()
    }

    override fun onActivityDestroyed(activity: Activity) {
        if (--count == 0 && activity.isFinishing) {
            CustomTabsHelper.unbind()
        }
    }

    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityResumed(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle?) {}
}
