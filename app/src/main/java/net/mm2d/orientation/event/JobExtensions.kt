/*
 * Copyright (c) 2020 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.event

import androidx.lifecycle.Lifecycle.Event
import androidx.lifecycle.Lifecycle.State.DESTROYED
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.Job

fun Job.cancelOnDestroy(owner: LifecycleOwner) {
    if (owner.lifecycle.currentState == DESTROYED) {
        cancel()
        return
    }
    CancelOnDestroy(this, owner).observe()
}

private class CancelOnDestroy(
    private val job: Job,
    private val owner: LifecycleOwner
) : LifecycleEventObserver {
    fun observe() {
        owner.lifecycle.addObserver(this)
    }

    override fun onStateChanged(source: LifecycleOwner, event: Event) {
        if (event != Event.ON_DESTROY) return
        job.cancel()
        owner.lifecycle.removeObserver(this)
    }
}
