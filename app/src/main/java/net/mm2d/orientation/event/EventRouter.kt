/*
 * Copyright (c) 2019 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.event

import net.mm2d.orientation.event.Event.EVENT_UPDATE

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
object EventRouter {
    fun notifyUpdate() {
        EventBus.notify(EVENT_UPDATE)
    }

    fun createUpdateObserver(): EventObserver =
        RxEventObserver(EVENT_UPDATE)
}
