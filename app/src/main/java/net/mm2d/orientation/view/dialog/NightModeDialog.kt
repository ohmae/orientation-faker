package net.mm2d.orientation.view.dialog

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.NightMode
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import net.mm2d.android.orientationfaker.R
import net.mm2d.orientation.settings.NightModes
import net.mm2d.orientation.settings.Settings

class NightModeDialog : DialogFragment() {
    private val modes = listOf(
        AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
        AppCompatDelegate.MODE_NIGHT_NO,
        AppCompatDelegate.MODE_NIGHT_YES,
    )
    private var mode: Int = Settings.get().nightMode

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.menu_title_app_theme)
            .setSingleChoiceItems(
                modes.map { getText(NightModes.getTextId(it)) }.toTypedArray(),
                modes.indexOf(mode)
            ) { _, which ->
                mode = modes[which]
            }
            .setPositiveButton(R.string.ok) { dialog, _ ->
                dialog.cancel()
                (activity as? Callback)?.onSelectNightMode(mode)
            }
            .setNegativeButton(R.string.cancel, null)
            .create()

    interface Callback {
        fun onSelectNightMode(@NightMode mode: Int)
    }

    companion object {
        private const val TAG = "NightModeDialog"

        fun show(activity: FragmentActivity) {
            val manager = activity.supportFragmentManager
            if (manager.isStateSaved || manager.findFragmentByTag(TAG) != null) return
            NightModeDialog().show(manager, TAG)
        }
    }
}
