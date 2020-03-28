/*
 * Copyright (c) 2019 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.event

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import net.mm2d.orientation.event.Event.EVENT_UPDATE

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
object EventRouter {
    private val subject: Subject<Event> = PublishSubject.create()

    fun notifyUpdate() {
        subject.onNext(EVENT_UPDATE)
    }

    fun createUpdateObserver(): EventObserver =
        RxEventObserver(EVENT_UPDATE)

    internal class RxEventObserver(
        private val event: Event
    ) : EventObserver {
        private var disposable: Disposable? = null

        override fun subscribe(callback: () -> Unit) {
            disposable?.dispose()
            disposable = subject
                .observeOn(AndroidSchedulers.mainThread())
                .filter { it == event }
                .subscribe { callback.invoke() }
        }

        override fun unsubscribe() {
            disposable?.dispose()
        }
    }
}
