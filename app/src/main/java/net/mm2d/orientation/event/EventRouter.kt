/*
 * Copyright (c) 2019 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.event

import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import net.mm2d.orientation.event.Event.EVENT_UPDATE

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
@FlowPreview
@ExperimentalCoroutinesApi
object EventRouter {
    private val channel: BroadcastChannel<Event> = BroadcastChannel(1)

    fun notifyUpdate() {
        GlobalScope.launch {
            channel.send(EVENT_UPDATE)
        }
    }

    fun createUpdateObserver(): EventObserver =
        ChannelEventObserver(EVENT_UPDATE)

    internal class ChannelEventObserver(
        private val event: Event
    ) : EventObserver {
        override fun subscribe(owner: LifecycleOwner, callback: () -> Unit) {
            GlobalScope.launch {
                channel.asFlow()
                    .filter { it == event }
                    .collect {
                        withContext(Dispatchers.Main) {
                            callback.invoke()
                        }
                    }
            }.cancelOnDestroy(owner)
        }
    }
}
