package net.mm2d.orientation.view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import net.mm2d.orientation.control.Orientation
import net.mm2d.orientation.settings.PreferenceRepository

class MainFragmentViewModel : ViewModel() {
    private val orientationPreferenceRepository =
        PreferenceRepository.get().orientationPreferenceRepository
    private val menuPreferenceRepository =
        PreferenceRepository.get().menuPreferenceRepository

    val menu = menuPreferenceRepository.flow
        .asLiveData()

    val sample = combine(
        PreferenceRepository.get().orientationPreferenceFlow,
        PreferenceRepository.get().designPreferenceRepository.flow,
        ::Pair
    ).asLiveData()

    fun updateEnabled(enabled: Boolean) {
        viewModelScope.launch {
            orientationPreferenceRepository.updateEnabled(enabled)
        }
    }

    fun updateOrientation(orientation: Orientation) {
        viewModelScope.launch {
            orientationPreferenceRepository.updateOrientation(orientation)
        }
    }

    fun updateNightMode(nightMode: Int) {
        viewModelScope.launch {
            menuPreferenceRepository.updateNightMode(nightMode)
        }
    }
}
