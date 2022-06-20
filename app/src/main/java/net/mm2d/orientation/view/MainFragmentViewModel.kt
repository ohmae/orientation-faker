/*
 * Copyright (c) 2021 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import net.mm2d.orientation.control.Orientation
import net.mm2d.orientation.settings.PreferenceRepository
import javax.inject.Inject

@HiltViewModel
class MainFragmentViewModel @Inject constructor(
    preferenceRepository: PreferenceRepository
) : ViewModel() {
    private val orientationPreferenceRepository =
        preferenceRepository.orientationPreferenceRepository
    private val menuPreferenceRepository =
        preferenceRepository.menuPreferenceRepository
    var notificationPermissionRequested: Boolean = false
        private set

    val menu = preferenceRepository.menuPreferenceFlow
        .onEach { notificationPermissionRequested = it.notificationPermissionRequested }
        .asLiveData()

    val sample = combine(
        preferenceRepository.actualOrientationPreferenceFlow,
        preferenceRepository.designPreferenceFlow,
        ::Pair
    ).asLiveData()

    fun updateEnabled(enabled: Boolean) {
        viewModelScope.launch {
            orientationPreferenceRepository.updateEnabled(enabled)
        }
    }

    fun updateOrientation(orientation: Orientation) {
        viewModelScope.launch {
            orientationPreferenceRepository.updateOrientationManually(orientation)
        }
    }

    fun updateNightMode(nightMode: Int) {
        viewModelScope.launch {
            menuPreferenceRepository.updateNightMode(nightMode)
        }
    }

    fun updateNotificationPermissionRequested() {
        viewModelScope.launch {
            menuPreferenceRepository.updateNotificationPermissionRequested(true)
        }
    }
}
