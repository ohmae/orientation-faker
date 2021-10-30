/*
 * Copyright (c) 2020 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.settings

import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.preference.PreferenceDataStore

class SharedPreferenceDataStore(
    private val sharedPreferences: SharedPreferences
) : PreferenceDataStore() {
    fun clear(): Unit =
        sharedPreferences.edit { clear() }

    fun remove(key: String): Unit =
        sharedPreferences.edit { remove(key) }

    fun contains(key: String): Boolean =
        sharedPreferences.contains(key)

    override fun getBoolean(key: String, defValue: Boolean): Boolean =
        sharedPreferences.getBoolean(key, defValue)

    override fun putBoolean(key: String, value: Boolean) =
        sharedPreferences.edit { putBoolean(key, value) }

    override fun getInt(key: String, defValue: Int): Int =
        sharedPreferences.getInt(key, defValue)

    override fun putInt(key: String, value: Int) =
        sharedPreferences.edit { putInt(key, value) }

    override fun getLong(key: String, defValue: Long): Long =
        sharedPreferences.getLong(key, defValue)

    override fun putLong(key: String, value: Long) =
        sharedPreferences.edit { putLong(key, value) }

    override fun getFloat(key: String, defValue: Float): Float =
        sharedPreferences.getFloat(key, defValue)

    override fun putFloat(key: String, value: Float) =
        sharedPreferences.edit { putFloat(key, value) }

    override fun getString(key: String, defValue: String?): String? =
        sharedPreferences.getString(key, defValue)

    override fun putString(key: String, value: String?) =
        sharedPreferences.edit { putString(key, value) }

    override fun getStringSet(key: String, defValues: Set<String>?): Set<String>? =
        sharedPreferences.getStringSet(key, defValues)

    override fun putStringSet(key: String, values: Set<String>?) =
        sharedPreferences.edit { putStringSet(key, values) }
}
