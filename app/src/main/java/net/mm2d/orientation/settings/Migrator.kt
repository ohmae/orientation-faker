/*
 * Copyright (c) 2021 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.settings

import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences

class Migrator(
    private val old: OldPreference,
    private val preference: MutablePreferences
) {
    fun int(oldKey: Key.Main, newKey: Preferences.Key<Int>, default: Int? = null) {
        (old.getInt(oldKey) ?: default)
            ?.let { preference[newKey] = it }
    }

    fun string(oldKey: Key.Main, newKey: Preferences.Key<String>, default: String? = null) {
        (old.getString(oldKey) ?: default)
            ?.let { preference[newKey] = it }
    }

    fun boolean(oldKey: Key.Main, newKey: Preferences.Key<Boolean>, default: Boolean? = null) {
        (old.getBoolean(oldKey) ?: default)
            ?.let { preference[newKey] = it }
    }

    fun long(oldKey: Key.Main, newKey: Preferences.Key<Long>, default: Long? = null) {
        (old.getLong(oldKey) ?: default)
            ?.let { preference[newKey] = it }
    }
}
