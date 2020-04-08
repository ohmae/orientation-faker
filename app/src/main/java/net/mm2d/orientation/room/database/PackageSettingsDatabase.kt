package net.mm2d.orientation.room.database

import androidx.room.Database
import androidx.room.RoomDatabase
import net.mm2d.orientation.room.dao.PackageSettingsDao
import net.mm2d.orientation.room.entity.PackageSettingEntity

@Database(entities = [PackageSettingEntity::class], version = 1)
abstract class PackageSettingsDatabase : RoomDatabase() {
    abstract fun packageSettingsDao(): PackageSettingsDao
}
