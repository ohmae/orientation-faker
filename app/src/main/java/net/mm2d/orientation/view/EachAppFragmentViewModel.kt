package net.mm2d.orientation.view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import net.mm2d.orientation.settings.PreferenceRepository

class EachAppFragmentViewModel : ViewModel() {
    private val orientationPreferenceRepository =
        PreferenceRepository.get().orientationPreferenceRepository
    private val menuPreferenceRepository =
        PreferenceRepository.get().menuPreferenceRepository

    val orientation = orientationPreferenceRepository.flow
        .asLiveData()

    val menu = menuPreferenceRepository.flow
        .asLiveData()

    fun updateControlByForegroundApp(enable: Boolean) {
        viewModelScope.launch {
            orientationPreferenceRepository.updateControlByForegroundApp(enable)
        }
    }

    fun updateShowAllApps(show: Boolean) {
        viewModelScope.launch {
            menuPreferenceRepository.updateShowAllApps(show)
        }
    }
}
