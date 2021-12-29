package net.mm2d.orientation.view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import net.mm2d.orientation.settings.PreferenceRepository

class DetailedSettingsFragmentViewModel : ViewModel() {
    private val menuPreferenceRepository =
        PreferenceRepository.get().menuPreferenceRepository

    val menu = menuPreferenceRepository.flow
        .asLiveData()

    fun updateWarnSystemRotate(warn: Boolean) {
        viewModelScope.launch {
            menuPreferenceRepository.updateWarnSystemRotate(warn)
        }
    }
}
