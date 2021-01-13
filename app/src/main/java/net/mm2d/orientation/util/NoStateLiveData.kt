/*
 * Copyright (c) 2021 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.util

import androidx.lifecycle.MutableLiveData

class NoStateLiveData<T> : MutableLiveData<T>() {
    override fun setValue(value: T) {
        super.setValue(value)
        if (value != null) {
            super.setValue(null)
        }
    }
}
