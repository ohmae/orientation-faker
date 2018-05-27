/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.orientationfaker.settings

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
internal class SettingsStorage(context: Context) {
    private val sharedPreferences: SharedPreferences = PreferencesHolder.get(context)

    /**
     * SharedPreferencesのインスタンスを返す。
     *
     * Maintainerで必要な場合のみ利用する。
     * それ以外では使用しないこと。
     *
     * @return SharedPreferences
     */
    val preferences: SharedPreferences
        @Deprecated("")
        get() = sharedPreferences

    /**
     * 書き込まれている内容を消去する。
     */
    fun clear() {
        sharedPreferences.edit()
                .clear()
                .apply()
    }

    /**
     * keyの値が書き込まれているかを返す。
     *
     * @param key Key
     * @return 含まれている場合true
     */
    operator fun contains(key: Key): Boolean {
        return sharedPreferences.contains(key.name)
    }

    /**
     * boolean値を書き込む。
     *
     * @param key   Key
     * @param value 書き込む値
     */
    fun writeBoolean(key: Key, value: Boolean) {
        sharedPreferences.edit()
                .putBoolean(key.name, value)
                .apply()
    }

    /**
     * boolean値を書き込む。
     *
     * @param key       Key
     * @param value     書き込む値
     * @param overwrite true:値を上書きする、false:値がない場合のみ書き込む
     */
    fun writeBoolean(key: Key, value: Boolean, overwrite: Boolean) {
        if (!overwrite && contains(key)) {
            return
        }
        writeBoolean(key, value)
    }

    /**
     * boolean値を読み出す。
     *
     * @param key          Key
     * @param defaultValue デフォルト値
     * @return 読み出したboolean値
     */
    fun readBoolean(key: Key, defaultValue: Boolean): Boolean {
        return sharedPreferences.getBoolean(key.name, defaultValue)
    }

    /**
     * int値を書き込む。
     *
     * @param key   Key
     * @param value 書き込む値
     */
    fun writeInt(key: Key, value: Int) {
        sharedPreferences.edit()
                .putInt(key.name, value)
                .apply()
    }

    /**
     * int値を書き込む。
     *
     * @param key       Key
     * @param value     書き込む値
     * @param overwrite true:値を上書きする、false:値がない場合のみ書き込む
     */
    fun writeInt(key: Key, value: Int, overwrite: Boolean) {
        if (!overwrite && contains(key)) {
            return
        }
        writeInt(key, value)
    }

    /**
     * int値を読み出す。
     *
     * @param key          Key
     * @param defaultValue デフォルト値
     * @return 読み出したint値
     */
    fun readInt(key: Key, defaultValue: Int): Int {
        return sharedPreferences.getInt(key.name, defaultValue)
    }

    /**
     * long値を書き込む。
     *
     * @param key   Key
     * @param value 書き込む値
     */
    fun writeLong(key: Key, value: Long) {
        sharedPreferences.edit()
                .putLong(key.name, value)
                .apply()
    }

    /**
     * long値を書き込む。
     *
     * @param key       Key
     * @param value     書き込む値
     * @param overwrite true:値を上書きする、false:値がない場合のみ書き込む
     */
    fun writeLong(key: Key, value: Long, overwrite: Boolean) {
        if (!overwrite && contains(key)) {
            return
        }
        writeLong(key, value)
    }

    /**
     * long値を読み出す。
     *
     * @param key          Key
     * @param defaultValue デフォルト値
     * @return 読み出したlong値
     */
    fun readLong(key: Key, defaultValue: Long): Long {
        return sharedPreferences.getLong(key.name, defaultValue)
    }

    /**
     * String値を書き込む。
     *
     * @param key   Key
     * @param value 書き込む値
     */
    fun writeString(key: Key, value: String) {
        sharedPreferences.edit()
                .putString(key.name, value)
                .apply()
    }

    /**
     * String値を書き込む。
     *
     * @param key       Key
     * @param value     書き込む値
     * @param overwrite true:値を上書きする、false:値がない場合のみ書き込む
     */
    fun writeString(key: Key, value: String, overwrite: Boolean) {
        if (!overwrite && contains(key)) {
            return
        }
        writeString(key, value)
    }

    /**
     * String値を読み出す。
     *
     * @param key          Key
     * @param defaultValue デフォルト値
     * @return 読み出したString値
     */
    fun readString(key: Key, defaultValue: String?): String? {
        return sharedPreferences.getString(key.name, defaultValue)
    }

    private object PreferencesHolder {
        private var sPreferences: SharedPreferences? = null

        @Synchronized
        internal operator fun get(context: Context): SharedPreferences {
            if (sPreferences == null) {
                sPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            }
            return sPreferences!!
        }
    }
}
