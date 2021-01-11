package net.mm2d.orientation.util

import androidx.lifecycle.MutableLiveData

class NoStateLiveData<T> : MutableLiveData<T>() {
    override fun setValue(value: T) {
        super.setValue(value)
        super.setValue(null)
    }
}
