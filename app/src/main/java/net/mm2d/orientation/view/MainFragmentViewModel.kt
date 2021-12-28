package net.mm2d.orientation.view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import net.mm2d.orientation.settings.PreferenceRepository

class MainFragmentViewModel : ViewModel() {
    private val menuPreferenceRepository =
        PreferenceRepository.get().menuPreferenceRepository

    val menu = menuPreferenceRepository.flow
        .asLiveData()

    fun updateNightMode(nightMode: Int) {
        viewModelScope.launch {
            menuPreferenceRepository.updateNightMode(nightMode)
        }
    }
}
