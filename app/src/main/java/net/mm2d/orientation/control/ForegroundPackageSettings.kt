/*
 * Copyright (c) 2020 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.control

import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.mm2d.orientation.room.database.PackageSettingsDatabase
import net.mm2d.orientation.room.entity.PackageSettingEntity
import net.mm2d.orientation.settings.Settings

object ForegroundPackageSettings {
    private const val DB_NAME = "package_settings.db"
    private lateinit var database: PackageSettingsDatabase
    private val map: MutableMap<String, Int> = mutableMapOf()
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private val settings: Settings by lazy { Settings.get() }

    fun initialize(context: Context) {
        database = Room.databaseBuilder(context, PackageSettingsDatabase::class.java, DB_NAME).build()
        scope.launch {
            val all = database.packageSettingsDao().getAll()
            withContext(Dispatchers.Main) {
                all.forEach {
                    map[it.packageName] = it.orientation
                }
            }
        }
    }

    fun updateInstalledPackages(packages: List<String>) {
        val list = map.keys.toMutableList()
        scope.launch {
            list.removeAll(packages)
            withContext(Dispatchers.IO) {
                list.forEach { map.remove(it) }
            }
            list.forEach { database.packageSettingsDao().delete(it) }
        }
    }

    fun disabled(): Boolean = map.isEmpty() || !settings.foregroundPackageCheckEnabled

    fun get(packageName: String): Int = map.getOrElse(packageName) { Orientation.INVALID }

    fun put(packageName: String, orientation: Int) {
        if (orientation == Orientation.INVALID) {
            map.remove(packageName)
            scope.launch {
                database.packageSettingsDao().delete(packageName)
            }
        } else {
            map[packageName] = orientation
            scope.launch {
                val entity = PackageSettingEntity(packageName, orientation)
                database.packageSettingsDao().insert(entity)
            }
        }
    }

    fun reset() {
        map.clear()
        scope.launch {
            database.packageSettingsDao().deleteAll()
        }
    }
}
