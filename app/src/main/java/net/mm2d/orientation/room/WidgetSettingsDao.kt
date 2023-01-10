/*
 * Copyright (c) 2023 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface WidgetSettingsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: WidgetSettingEntity)

    @Query("SELECT * FROM widget_settings")
    suspend fun getAll(): List<WidgetSettingEntity>

    @Query("SELECT * FROM widget_settings WHERE id=:id LIMIT 1")
    suspend fun get(id: Int): WidgetSettingEntity?

    @Query("DELETE FROM widget_settings WHERE id=:id")
    suspend fun delete(id: Int)

    @Query("DELETE FROM widget_settings")
    suspend fun deleteAll()
}
