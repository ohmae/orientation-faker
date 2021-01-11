package net.mm2d.orientation.view.dialog

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import net.mm2d.orientation.settings.IconShape
import net.mm2d.orientation.util.NoStateLiveData

class IconShapeDialogViewModel : ViewModel() {
    private val liveData: MutableLiveData<IconShape> = NoStateLiveData()
    fun postIconShape(mode: IconShape) {
        liveData.postValue(mode)
    }

    fun iconShapeLiveData(): LiveData<IconShape> = liveData
}
