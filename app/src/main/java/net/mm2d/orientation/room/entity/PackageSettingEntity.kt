package net.mm2d.orientation.room.entity

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
