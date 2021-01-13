/*
 * Copyright (c) 2020 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import net.mm2d.orientation.room.entity.PackageSettingEntity

@Dao
interface PackageSettingsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: PackageSettingEntity)

    @Query("SELECT * FROM package_settings")
    suspend fun getAll(): List<PackageSettingEntity>

    @Query("DELETE FROM package_settings WHERE package_name = :packageName")
    suspend fun delete(packageName: String)

    @Query("DELETE FROM package_settings")
    suspend fun deleteAll()
}
