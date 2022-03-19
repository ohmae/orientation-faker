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
    private val preferenceRepository: PreferenceRepository
) : ViewModel() {
    val orientation = preferenceRepository.orientationPreferenceFlow
        .asLiveData()

    val menu = preferenceRepository.menuPreferenceFlow
        .asLiveData()

    fun updateControlByForegroundApp(enable: Boolean) {
        viewModelScope.launch {
            preferenceRepository.orientationPreferenceRepository
                .updateControlByForegroundApp(enable)
        }
    }

    fun updateShowAllApps(show: Boolean) {
        viewModelScope.launch {
            preferenceRepository.menuPreferenceRepository
                .updateShowAllApps(show)
        }
    }
}
