/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.settings

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
internal enum class OldKey {
    SETTINGS_VERSION,
    APP_VERSION_AT_INSTALL,

    // APP_VERSION_AT_LAST_LAUNCHED,
    REVIEW_INTERVAL_RANDOM_FACTOR,
    TIME_FIRST_USE,
    TIME_FIRST_REVIEW,
    COUNT_ORIENTATION_CHANGED,
    COUNT_REVIEW_DIALOG_CANCELED,
    REVIEW_REPORTED,
    REVIEW_REVIEWED,
    ORIENTATION,
    RESIDENT,
    COLOR_FOREGROUND,
    COLOR_BACKGROUND,
    COLOR_FOREGROUND_SELECTED,
    COLOR_BACKGROUND_SELECTED,
    NOTIFY_SECRET,
    USE_FULL_SENSOR,
    AUTO_ROTATE_WARNING,
    USE_BLANK_ICON_FOR_NOTIFICATION,
}
