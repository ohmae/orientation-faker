/*
 * Copyright (c) 2020 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "package_settings")
data class PackageSettingEntity(
    @PrimaryKey
    @ColumnInfo(name = "package_name")
    val packageName: String,
    @ColumnInfo(name = "orientation")
    val orientation: Int
)
