/*
 * Copyright (c) 2020 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.settings

import android.content.Context
import net.mm2d.android.orientationfaker.BuildConfig

class SettingsPreferences<K>(
    context: Context,
    name: String
) where K : Enum<*>,
        K : Key {
    private val dataStore: SharedPreferenceDataStore =
        SharedPreferenceDataStore(
            context.getSharedPreferences(BuildConfig.APPLICATION_ID + "." + name, Context.MODE_PRIVATE)
        )

    fun remove(key: K): Unit =
        dataStore.remove(key.name)

    operator fun contains(key: K): Boolean =
        dataStore.contains(key.name)

    fun readBoolean(key: K, default: Boolean): Boolean {
        return dataStore.getBoolean(key.name, default)
    }

    fun writeBoolean(key: K, value: Boolean) {
        dataStore.putBoolean(key.name, value)
    }

    fun readInt(key: K, default: Int): Int {
        return dataStore.getInt(key.name, default)
    }

    fun writeInt(key: K, value: Int) {
        dataStore.putInt(key.name, value)
    }

    fun readLong(key: K, default: Long): Long {
        return dataStore.getLong(key.name, default)
    }

    fun writeLong(key: K, value: Long) {
        dataStore.putLong(key.name, value)
    }

    fun readString(key: K, default: String): String {
        return dataStore.getString(key.name, default)!!
    }

    fun writeString(key: K, value: String) {
        dataStore.putString(key.name, value)
    }
}
