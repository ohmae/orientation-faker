/*
 * Copyright (c) 2023 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.view

import android.app.Application
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import net.mm2d.orientation.room.WidgetSettingEntity
import net.mm2d.orientation.settings.PreferenceRepository
import net.mm2d.orientation.settings.WidgetSettingsRepository
import net.mm2d.orientation.view.widget.CustomWidgetProvider
import javax.inject.Inject

@HiltViewModel
class CustomWidgetListViewModel @Inject constructor(
    application: Application,
    private val preferenceRepository: PreferenceRepository,
    private val widgetSettings: WidgetSettingsRepository,
) : AndroidViewModel(application) {
    private val widgetComponentName = ComponentName(application, CustomWidgetProvider::class.java)
    private val manager: AppWidgetManager = AppWidgetManager.getInstance(application)

    init {
        viewModelScope.launch {
            preferenceRepository.designPreferenceFlow.map { design ->
                manager.getAppWidgetIds(widgetComponentName)
                    .map { widgetSettings.getOrDefault(it, design) }
            }.collect()
        }
    }

    fun getAll(): LiveData<List<WidgetSettingEntity>> =
        widgetSettings.dao().getAllFlow().asLiveData()
}
