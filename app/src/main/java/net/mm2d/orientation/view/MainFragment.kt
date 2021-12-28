/*
 * Copyright (c) 2014 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle.State
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import net.mm2d.android.orientationfaker.BuildConfig
import net.mm2d.android.orientationfaker.R
import net.mm2d.android.orientationfaker.databinding.FragmentMainBinding
import net.mm2d.orientation.control.Orientation
import net.mm2d.orientation.event.EventRouter
import net.mm2d.orientation.review.ReviewRequest
import net.mm2d.orientation.service.MainController
import net.mm2d.orientation.service.MainService
import net.mm2d.orientation.settings.NightModes
import net.mm2d.orientation.settings.Settings
import net.mm2d.orientation.util.Launcher
import net.mm2d.orientation.util.SystemSettings
import net.mm2d.orientation.util.Updater
import net.mm2d.orientation.util.autoCleared
import net.mm2d.orientation.view.dialog.NightModeDialog
import net.mm2d.orientation.view.dialog.OverlayPermissionDialog

class MainFragment : Fragment(R.layout.fragment_main) {
    private val settings by lazy {
        Settings.get()
    }
    private val handler = Handler(Looper.getMainLooper())
    private val checkSystemSettingsTask = Runnable { checkSystemSettings() }
    private var notificationSample: NotificationSample by autoCleared()
    private var binding: FragmentMainBinding by autoCleared()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentMainBinding.bind(view)
        setHasOptionsMenu(true)
        setUpViews()
        EventRouter.observeUpdate(viewLifecycleOwner) {
            applyStatus()
            notificationSample.update()
        }
        if (!SystemSettings.canDrawOverlays(requireContext())) {
            MainController.stop()
        } else {
            if (Settings.get().shouldAutoStart()) {
                MainController.start()
            }
            Updater.startUpdateIfAvailable(requireActivity())
        }
        NightModeDialog.registerListener(this, REQUEST_KEY_NIGHT_MODE) {
            onSelectNightMode(it)
        }
    }

    @SuppressLint("NewApi")
    override fun onResume() {
        super.onResume()
        notificationSample.update()
        handler.removeCallbacks(checkSystemSettingsTask)
        handler.post(checkSystemSettingsTask)
        applyStatus()
        if (!SystemSettings.canDrawOverlays(requireContext())) {
            OverlayPermissionDialog.show(this)
        }
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(checkSystemSettingsTask)
    }

    private fun checkSystemSettings() {
        if (lifecycle.currentState != State.RESUMED) {
            return
        }
        if (!settings.autoRotateWarning) {
            binding.content.caution.visibility = View.GONE
            return
        }
        binding.content.caution.visibility =
            if (SystemSettings.rotationIsFixed(requireContext())) View.VISIBLE else View.GONE
        handler.postDelayed(checkSystemSettingsTask, CHECK_INTERVAL)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val context = requireContext()
        when (item.itemId) {
            R.id.license -> LicenseActivity.start(context)
            R.id.source_code -> Launcher.openSourceCode(context)
            R.id.privacy_policy -> Launcher.openPrivacyPolicy(context)
            R.id.mail_to_developer -> Launcher.sendMailToDeveloper(context)
            R.id.share_this_app -> Launcher.shareThisApp(requireActivity())
            R.id.play_store -> Launcher.openGooglePlay(context)
        }
        return true
    }

    private fun setUpViews() {
        notificationSample = NotificationSample(binding.content.notificationSample)
        binding.content.status.setOnClickListener { toggleStatus() }
        binding.content.detailedSetting.setOnClickListener {
            navigate(MainFragmentDirections.actionMainFragmentToDetailedSettingsFragment())
        }
        binding.content.versionDescription.text = BuildConfig.VERSION_NAME
        setUpOrientationIcons()
        binding.content.eachApp.setOnClickListener {
            navigate(MainFragmentDirections.actionMainFragmentToEachAppFragment())
        }
        setUpNightMode()
    }

    private fun navigate(directions: NavDirections) {
        val controller = findNavController()
        if (controller.currentDestination?.id == R.id.MainFragment) {
            controller.navigate(directions)
        }
    }

    private fun setUpOrientationIcons() {
        notificationSample.buttonList.forEach { view ->
            view.button.setOnClickListener {
                updateOrientation(view.orientation)
                if (!MainService.isStarted && SystemSettings.canDrawOverlays(requireContext())) {
                    MainController.start()
                    settings.setAutoStart(true)
                }
            }
        }
    }

    private fun setUpNightMode() {
        binding.content.nightMode.setOnClickListener {
            NightModeDialog.show(this, REQUEST_KEY_NIGHT_MODE, settings.nightMode)
        }
        applyNightMode()
    }

    private fun applyNightMode() {
        binding.content.nightModeDescription.setText(NightModes.getTextId(settings.nightMode))
    }

    private fun onSelectNightMode(mode: Int?) {
        if (mode == null) return
        if (settings.nightMode == mode) return
        settings.nightMode = mode
        applyNightMode()
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    @SuppressLint("NewApi")
    private fun toggleStatus() {
        if (MainService.isStarted) {
            MainController.stop()
            settings.setAutoStart(false)
        } else {
            if (SystemSettings.canDrawOverlays(requireContext())) {
                MainController.start()
                settings.setAutoStart(true)
            } else {
                OverlayPermissionDialog.show(this)
            }
        }
    }

    private fun applyStatus() {
        if (MainService.isStarted) {
            binding.content.statusButton.setText(R.string.button_status_stop)
            binding.content.statusButton.setBackgroundResource(R.drawable.bg_stop_button)
            binding.content.statusDescription.setText(R.string.menu_description_status_running)
        } else {
            binding.content.statusButton.setText(R.string.button_status_start)
            binding.content.statusButton.setBackgroundResource(R.drawable.bg_start_button)
            binding.content.statusDescription.setText(R.string.menu_description_status_waiting)
        }
        ReviewRequest.requestReviewIfNeed(this)
    }

    private fun updateOrientation(orientation: Orientation) {
        settings.orientation = orientation
        notificationSample.update()
        MainController.update()
    }

    companion object {
        private const val CHECK_INTERVAL: Long = 5000L
        private const val PREFIX = "MainFragment."
        private const val REQUEST_KEY_NIGHT_MODE = PREFIX + "REQUEST_KEY_NIGHT_MODE"
    }
}
