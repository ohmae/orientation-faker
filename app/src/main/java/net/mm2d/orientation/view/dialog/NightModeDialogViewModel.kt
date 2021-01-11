package net.mm2d.orientation.view.dialog

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import net.mm2d.orientation.util.NoStateLiveData

class NightModeDialogViewModel : ViewModel() {
    private val liveData: MutableLiveData<Int> = NoStateLiveData()
    fun postMode(mode: Int) {
        liveData.postValue(mode)
    }

    fun modeLiveData(): LiveData<Int> = liveData
}
