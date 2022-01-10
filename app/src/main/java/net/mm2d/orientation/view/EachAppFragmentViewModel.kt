package net.mm2d.orientation.view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import net.mm2d.orientation.settings.PreferenceRepository
import javax.inject.Inject

@HiltViewModel
class EachAppFragmentViewModel @Inject constructor(
    preferenceRepository: PreferenceRepository
) : ViewModel() {
    private val orientationPreferenceRepository =
        preferenceRepository.orientationPreferenceRepository
    private val menuPreferenceRepository =
        preferenceRepository.menuPreferenceRepository

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
