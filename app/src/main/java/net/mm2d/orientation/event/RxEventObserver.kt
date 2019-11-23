/*
 * Copyright (c) 2019 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.event

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
internal class RxEventObserver(
    private val event: Event
) : EventObserver {
    private var disposable: Disposable? = null

    override fun subscribe(callback: () -> Unit) {
        disposable?.dispose()
        disposable = EventBus.observable()
            .observeOn(AndroidSchedulers.mainThread())
            .filter { it == event }
            .subscribe { callback.invoke() }
    }

    override fun unsubscribe() {
        disposable?.dispose()
    }
}
