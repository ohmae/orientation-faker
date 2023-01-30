/*
 * Copyright (c) 2023 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import net.mm2d.orientation.control.FunctionButton
import net.mm2d.orientation.room.WidgetSettingEntity
import net.mm2d.orientation.settings.Default
import net.mm2d.orientation.settings.IconShape
import net.mm2d.orientation.settings.WidgetSettingsRepository
import net.mm2d.orientation.util.doOnNext
import javax.inject.Inject

@HiltViewModel
class CustomWidgetConfigViewModel @Inject constructor(
    private val widgetSettingsRepository: WidgetSettingsRepository,
    private val defaults: Default,
) : ViewModel() {
    private var widgetSetting: WidgetSettingEntity? = null

    fun getParam(id: Int): LiveData<WidgetSettingEntity> =
        widgetSettingsRepository.dao()
            .getFlow(id)
            .filterNotNull()
            .asLiveData()
            .doOnNext {
                widgetSetting = it
            }

    fun updateForeground(color: Int) {
        val widgetSetting = widgetSetting ?: return
        viewModelScope.launch {
            widgetSettingsRepository.dao()
                .insert(widgetSetting.copy(foreground = color))
        }
    }

    fun updateBackground(color: Int) {
        val widgetSetting = widgetSetting ?: return
        viewModelScope.launch {
            widgetSettingsRepository.dao()
                .insert(widgetSetting.copy(background = color))
        }
    }

    fun updateForegroundSelected(color: Int) {
        val widgetSetting = widgetSetting ?: return
        viewModelScope.launch {
            widgetSettingsRepository.dao()
                .insert(widgetSetting.copy(foregroundSelected = color))
        }
    }

    fun updateBackgroundSelected(color: Int) {
        val widgetSetting = widgetSetting ?: return
        viewModelScope.launch {
            widgetSettingsRepository.dao()
                .insert(widgetSetting.copy(backgroundSelected = color))
        }
    }

    fun updateBase(color: Int) {
        val widgetSetting = widgetSetting ?: return
        viewModelScope.launch {
            widgetSettingsRepository.dao()
                .insert(widgetSetting.copy(base = color))
        }
    }

    fun updateFunctions(functions: List<FunctionButton>) {
        val widgetSetting = widgetSetting ?: return
        viewModelScope.launch {
            widgetSettingsRepository.dao()
                .insert(widgetSetting.copy(functions = functions))
        }
    }

    fun resetTheme() {
        val widgetSetting = widgetSetting ?: return
        viewModelScope.launch {
            widgetSettingsRepository.dao()
                .insert(
                    widgetSetting.copy(
                        foreground = defaults.color.foreground,
                        background = defaults.color.background,
                        foregroundSelected = defaults.color.foregroundSelected,
                        backgroundSelected = defaults.color.backgroundSelected,
                        base = defaults.color.base,
                    )
                )
        }
    }

    fun updateShape(shape: IconShape) {
        val widgetSetting = widgetSetting ?: return
        viewModelScope.launch {
            widgetSettingsRepository.dao()
                .insert(widgetSetting.copy(shape = shape))
        }
    }
}
