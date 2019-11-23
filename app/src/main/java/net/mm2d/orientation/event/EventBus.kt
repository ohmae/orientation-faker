/*
 * Copyright (c) 2019 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.event

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject

internal object EventBus {
    private val subject: Subject<Event> = PublishSubject.create<Event>()

    fun notify(event: Event): Unit = subject.onNext(event)

    fun observable(): Observable<Event> = subject
}
