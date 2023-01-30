/*
 * Copyright (c) 2023 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.util

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData

fun <T> LiveData<T>.doOnNext(block: (T?) -> Unit): LiveData<T> =
    MediatorLiveData<T>().also { mediator ->
        mediator.addSource(this) {
            block(it)
            mediator.value = it
        }
    }
