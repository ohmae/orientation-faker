/*
 * Copyright (c) 2023 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.view.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.os.Bundle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import net.mm2d.orientation.control.Orientation
import net.mm2d.orientation.settings.DesignPreference
import net.mm2d.orientation.settings.OrientationPreference
import net.mm2d.orientation.settings.PreferenceRepository
import net.mm2d.orientation.settings.WidgetSettingsRepository
import javax.inject.Inject

@AndroidEntryPoint
class CustomWidgetProvider : AppWidgetProvider() {
    @Inject
    lateinit var preference: PreferenceRepository

    @Inject
    lateinit var widgetSettings: WidgetSettingsRepository

    private val exceptionHandler = CoroutineExceptionHandler { _, _ -> }
    private val job = SupervisorJob()
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main + job + exceptionHandler)

    private val orientationFlow: Flow<OrientationPreference> by lazy {
        preference.orientationPreferenceFlow
            .map {
                val o = if (it.enabled) it.orientation else Orientation.INVALID
                it.copy(orientation = o)
            }
            .shareIn(scope, SharingStarted.Eagerly, 1)
    }
    private val designFlow: Flow<DesignPreference> by lazy {
        preference.designPreferenceFlow.shareIn(scope, SharingStarted.Eagerly, 1)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == Intent.ACTION_CONFIGURATION_CHANGED) {
            scope.launch {
                delay(500)
                val manager: AppWidgetManager = AppWidgetManager.getInstance(context)
                val orientation = orientationFlow.first()
                manager.getAppWidgetIds(ComponentName(context, CustomWidgetProvider::class.java)).forEach {
                    launch { update(context, manager, it, orientation) }
                }
            }
        }
    }

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        scope.launch {
            val (orientation, design) = combine(orientationFlow, designFlow, ::Pair).first()
            ids.forEach { launch { update(context, manager, it, orientation, design) } }
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        manager: AppWidgetManager,
        id: Int,
        newOptions: Bundle?
    ) {
        scope.launch {
            val (orientation, design) = combine(orientationFlow, designFlow, ::Pair).first()
            update(context, manager, id, orientation, design, newOptions)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        scope.launch {
            appWidgetIds.forEach {
                widgetSettings.dao().delete(it)
            }
        }
    }

    override fun onRestored(context: Context, oldWidgetIds: IntArray, newWidgetIds: IntArray) {
        scope.launch {
            val newSettings = oldWidgetIds.indices.mapNotNull {
                val oldId = oldWidgetIds[it]
                val newId = newWidgetIds[it]
                if (oldId == newId) return@mapNotNull null
                widgetSettings.dao().get(oldId)?.copy(id = newId)
            }
            oldWidgetIds.forEach { widgetSettings.dao().delete(it) }
            newSettings.forEach { widgetSettings.dao().insert(it) }
        }
    }

    companion object {
        private lateinit var preferenceRepository: PreferenceRepository
        private lateinit var widgetSettingsRepository: WidgetSettingsRepository
        private lateinit var orientationFlow: Flow<OrientationPreference>

        fun initialize(
            context: Context,
            preference: PreferenceRepository,
            widgetSettings: WidgetSettingsRepository,
        ) {
            preferenceRepository = preference
            widgetSettingsRepository = widgetSettings
            orientationFlow = preference.orientationPreferenceFlow
                .map {
                    val o = if (it.enabled) it.orientation else Orientation.INVALID
                    it.copy(orientation = o)
                }
                .shareIn(preference.scope, SharingStarted.Eagerly, 1)

            val manager: AppWidgetManager = AppWidgetManager.getInstance(context)
            preference.scope.launch {
                orientationFlow.collect { orientation ->
                    manager.getAppWidgetIds(ComponentName(context, CustomWidgetProvider::class.java)).forEach {
                        launch { update(context, manager, it, orientation) }
                    }
                }
            }
            context.registerReceiver(CustomWidgetProvider(), IntentFilter(Intent.ACTION_CONFIGURATION_CHANGED))
        }

        private suspend fun update(
            context: Context,
            manager: AppWidgetManager,
            id: Int,
            orientation: OrientationPreference,
            design: DesignPreference,
            options: Bundle? = null
        ) {
            val setting = widgetSettingsRepository.getOrDefault(id, design)
            val (width, height) = extractSize(context, manager, id, options)
            val views = CustomWidgetRemoteViewsCreator(context, width, height, setting, orientation).create()
            manager.updateAppWidget(id, views)
        }

        private suspend fun update(
            context: Context,
            manager: AppWidgetManager,
            id: Int,
            orientation: OrientationPreference,
        ) {
            val setting = widgetSettingsRepository.dao().get(id) ?: return
            val (width, height) = extractSize(context, manager, id)
            val views = CustomWidgetRemoteViewsCreator(context, width, height, setting, orientation).create()
            manager.updateAppWidget(id, views)
        }

        fun update(
            context: Context,
            id: Int,
        ) {
            preferenceRepository.scope.launch {
                val setting = widgetSettingsRepository.dao().get(id) ?: return@launch
                val orientation = orientationFlow.first()
                val manager: AppWidgetManager = AppWidgetManager.getInstance(context)
                val (width, height) = extractSize(context, manager, id)
                val views = CustomWidgetRemoteViewsCreator(context, width, height, setting, orientation).create()
                manager.updateAppWidget(id, views)
            }
        }

        private fun extractSize(
            context: Context,
            manager: AppWidgetManager,
            id: Int,
            options: Bundle? = null
        ): Pair<Int, Int> = (options ?: manager.getAppWidgetOptions(id)).let {
            if (context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                it.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH) to
                    it.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)
            } else {
                it.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH) to
                    it.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT)
            }
        }
    }
}
