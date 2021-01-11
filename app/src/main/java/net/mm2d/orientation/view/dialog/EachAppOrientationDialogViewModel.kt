package net.mm2d.orientation.view.dialog

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import net.mm2d.orientation.util.NoStateLiveData

class EachAppOrientationDialogViewModel : ViewModel() {
    private val liveData: MutableLiveData<Int> = NoStateLiveData()
    fun postChangedPosition(mode: Int) {
        liveData.postValue(mode)
    }

    fun changedPositionLiveData(): LiveData<Int> = liveData
}
