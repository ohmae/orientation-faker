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
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import net.mm2d.orientation.control.FunctionButton
import net.mm2d.orientation.control.Orientation
import net.mm2d.orientation.control.mapOrientation
import net.mm2d.orientation.settings.IconShape
import net.mm2d.orientation.settings.PreferenceRepository
import javax.inject.Inject

@HiltViewModel
class DetailedSettingsFragmentViewModel @Inject constructor(
    private val preferenceRepository: PreferenceRepository
) : ViewModel() {
    private val orientationPreferenceRepository =
        preferenceRepository.orientationPreferenceRepository
    private val controlPreferenceRepository =
        preferenceRepository.controlPreferenceRepository
    private val designPreferenceRepository =
        preferenceRepository.designPreferenceRepository
    private val menuPreferenceRepository =
        preferenceRepository.menuPreferenceRepository

    val menu = preferenceRepository.menuPreferenceFlow
        .asLiveData()
    val sample = combine(
        preferenceRepository.actualOrientationPreferenceFlow,
        preferenceRepository.designPreferenceFlow,
        ::Pair
    ).asLiveData()
    val orientation = preferenceRepository.orientationPreferenceFlow.asLiveData()
    val control = preferenceRepository.controlPreferenceFlow.asLiveData()
    val design = preferenceRepository.designPreferenceFlow.asLiveData()

    fun updateOrientation(orientation: Orientation) {
        viewModelScope.launch {
            orientationPreferenceRepository.updateOrientationManually(orientation)
        }
    }

    fun updateNotifySecret(secret: Boolean) {
        viewModelScope.launch {
            controlPreferenceRepository.updateNotifySecret(secret)
        }
    }

    fun updateUseBlankIcon(use: Boolean) {
        viewModelScope.launch {
            controlPreferenceRepository.updateUseBlankIcon(use)
        }
    }

    fun updateForeground(color: Int) {
        viewModelScope.launch {
            designPreferenceRepository.updateForeground(color)
        }
    }

    fun updateBackground(color: Int) {
        viewModelScope.launch {
            designPreferenceRepository.updateBackground(color)
        }
    }

    fun updateForegroundSelected(color: Int) {
        viewModelScope.launch {
            designPreferenceRepository.updateForegroundSelected(color)
        }
    }

    fun updateBackgroundSelected(color: Int) {
        viewModelScope.launch {
            designPreferenceRepository.updateBackgroundSelected(color)
        }
    }

    fun updateBase(color: Int) {
        viewModelScope.launch {
            designPreferenceRepository.updateBase(color)
        }
    }

    fun resetTheme() {
        viewModelScope.launch {
            designPreferenceRepository.resetTheme()
        }
    }

    fun updateShape(shape: IconShape) {
        viewModelScope.launch {
            designPreferenceRepository.updateShape(shape)
        }
    }

    fun updateShowSettings(show: Boolean) {
        viewModelScope.launch {
            designPreferenceRepository.updateShowSettings(show)
        }
    }

    fun updateFunctions(functions: List<FunctionButton>) {
        viewModelScope.launch {
            designPreferenceRepository.updateFunctions(functions)
        }
    }

    fun updateWarnSystemRotate(warn: Boolean) {
        viewModelScope.launch {
            menuPreferenceRepository.updateWarnSystemRotate(warn)
        }
    }

    fun updateOrientationWhenPowerIsConnected(orientation: Orientation) {
        viewModelScope.launch {
            orientationPreferenceRepository.updateOrientationWhenPowerIsConnected(orientation)
        }
    }

    fun adjustOrientation() {
        preferenceRepository.scope.launch {
            combine(
                preferenceRepository.orientationPreferenceFlow,
                preferenceRepository.designPreferenceFlow,
                ::Pair
            ).take(1).collect { (orientation, design) ->
                if (!design.functions.mapOrientation().contains(orientation.orientation)) {
                    orientationPreferenceRepository.updateOrientation(design.functions.mapOrientation().first())
                }
            }
        }
    }
}
