/*
 * Copyright (c) 2020 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.control

import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.mm2d.orientation.room.database.PackageSettingsDatabase
import net.mm2d.orientation.room.entity.PackageSettingEntity

object ForegroundPackageSettings {
    private const val DB_NAME = "package_settings.db"
    private lateinit var database: PackageSettingsDatabase
    private val map: MutableMap<String, Orientation> = mutableMapOf()
    private val exceptionHandler = CoroutineExceptionHandler { _, _ -> }
    private val job = SupervisorJob()
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + job + exceptionHandler)
    private val emptyFlow: MutableStateFlow<Boolean> = MutableStateFlow(true)

    fun initialize(context: Context) {
        database = Room.databaseBuilder(context, PackageSettingsDatabase::class.java, DB_NAME).build()
        scope.launch {
            val all = database.packageSettingsDao().getAll()
            withContext(Dispatchers.Main) {
                all.forEach {
                    map[it.packageName] = it.orientation.toOrientation()
                }
                emptyFlow.emit(map.isEmpty())
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

    fun emptyFlow(): Flow<Boolean> = emptyFlow

    fun get(packageName: String): Orientation = map.getOrElse(packageName) { Orientation.INVALID }

    fun put(packageName: String, orientation: Orientation) {
        if (orientation == Orientation.INVALID) {
            map.remove(packageName)
            scope.launch {
                database.packageSettingsDao().delete(packageName)
            }
        } else {
            map[packageName] = orientation
            scope.launch {
                val entity = PackageSettingEntity(packageName, orientation.value)
                database.packageSettingsDao().insert(entity)
            }
        }
        scope.launch {
            emptyFlow.emit(map.isEmpty())
        }
    }

    fun reset() {
        map.clear()
        scope.launch {
            database.packageSettingsDao().deleteAll()
        }
        scope.launch {
            emptyFlow.emit(map.isEmpty())
        }
    }
}
