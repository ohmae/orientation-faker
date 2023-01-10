/*
 * Copyright (c) 2023 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.settings

import android.content.Context
import androidx.room.Room
import dagger.hilt.android.qualifiers.ApplicationContext
import net.mm2d.orientation.control.FunctionButton
import net.mm2d.orientation.room.WidgetSettingEntity
import net.mm2d.orientation.room.WidgetSettingsDao
import net.mm2d.orientation.room.WidgetSettingsDatabase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WidgetSettingsRepository @Inject constructor(
    @ApplicationContext context: Context
) {
    private val database: WidgetSettingsDatabase =
        Room.databaseBuilder(context, WidgetSettingsDatabase::class.java, DB_NAME).build()
    private val dao: WidgetSettingsDao by lazy {
        database.widgetSettingsDao()
    }

    fun dao(): WidgetSettingsDao = dao

    suspend fun getOrDefault(id: Int, designPreference: DesignPreference): WidgetSettingEntity =
        dao.get(id) ?: widgetEntityFromDesignPreference(id, designPreference)

    private fun widgetEntityFromDesignPreference(id: Int, designPreference: DesignPreference): WidgetSettingEntity =
        WidgetSettingEntity(
            id = id,
            foreground = designPreference.foreground,
            background = designPreference.background,
            foregroundSelected = designPreference.foregroundSelected,
            backgroundSelected = designPreference.backgroundSelected,
            base = designPreference.base,
            shape = designPreference.shape,
            functions = FunctionButton.all()
        )

    companion object {
        private const val DB_NAME = "widget_settings.db"
    }
}
