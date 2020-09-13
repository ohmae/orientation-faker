/*
 * Copyright (c) 2019 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.event

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
object EventRouter {
    private val updateChannel: BroadcastChannel<Unit> = BroadcastChannel(1)

    fun notifyUpdate() {
        GlobalScope.launch {
            updateChannel.send(Unit)
        }
    }

    fun observeUpdate(owner: LifecycleOwner, callback: () -> Unit) {
        owner.lifecycleScope.launch {
            updateChannel.asFlow().collect { callback() }
        }
    }
}
