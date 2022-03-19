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
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle.State
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.transition.TransitionManager
import dagger.hilt.android.AndroidEntryPoint
import net.mm2d.android.orientationfaker.BuildConfig
import net.mm2d.android.orientationfaker.R
import net.mm2d.android.orientationfaker.databinding.FragmentMainBinding
import net.mm2d.orientation.review.ReviewRequest
import net.mm2d.orientation.service.MainService
import net.mm2d.orientation.settings.NightModes
import net.mm2d.orientation.settings.PreferenceRepository
import net.mm2d.orientation.util.Launcher
import net.mm2d.orientation.util.SystemSettings
import net.mm2d.orientation.util.Updater
import net.mm2d.orientation.util.autoCleared
import net.mm2d.orientation.view.dialog.NightModeDialog
import net.mm2d.orientation.view.dialog.OverlayPermissionDialog
import javax.inject.Inject

@AndroidEntryPoint
class MainFragment : Fragment(R.layout.fragment_main) {
    @Inject
    lateinit var preferenceRepository: PreferenceRepository
    private val handler = Handler(Looper.getMainLooper())
    private val checkSystemSettingsTask = Runnable { checkSystemSettings() }
    private var notificationSample: NotificationSample by autoCleared()
    private var binding: FragmentMainBinding by autoCleared()
    private val viewModel: MainFragmentViewModel by viewModels()
    private var warnSystemRotate: Boolean = false
    private var enable: Boolean = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentMainBinding.bind(view)
        setHasOptionsMenu(true)
        setUpViews()
        if (!SystemSettings.canDrawOverlays(requireContext())) {
            viewModel.updateEnabled(false)
        } else {
            Updater.startUpdateIfAvailable(requireActivity())
        }
        NightModeDialog.registerListener(this, REQUEST_KEY_NIGHT_MODE) {
            viewModel.updateNightMode(it)
        }
        viewModel.menu.observe(viewLifecycleOwner) { menu ->
            binding.content.nightModeDescription.setText(NightModes.getTextId(menu.nightMode))
            binding.content.nightMode.setOnClickListener {
                NightModeDialog.show(this, REQUEST_KEY_NIGHT_MODE, menu.nightMode)
            }
            warnSystemRotate = menu.warnSystemRotate
        }
        viewModel.sample.observe(viewLifecycleOwner) { (orientation, design) ->
            notificationSample.update(orientation, design)
            enable = orientation.enabled
            if (orientation.enabled) {
                binding.content.statusButton.setText(R.string.button_status_stop)
                binding.content.statusButton.setBackgroundResource(R.drawable.bg_stop_button)
                binding.content.statusDescription.setText(R.string.menu_description_status_running)
            } else {
                binding.content.statusButton.setText(R.string.button_status_start)
                binding.content.statusButton.setBackgroundResource(R.drawable.bg_start_button)
                binding.content.statusDescription.setText(R.string.menu_description_status_waiting)
            }
        }
    }

    @SuppressLint("NewApi")
    override fun onResume() {
        super.onResume()
        handler.removeCallbacks(checkSystemSettingsTask)
        handler.post(checkSystemSettingsTask)
        ReviewRequest.requestReviewIfNeed(this, preferenceRepository)
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
        if (!warnSystemRotate) {
            binding.content.caution.visibility = View.GONE
            return
        }
        val shouldWarning = SystemSettings.rotationIsFixed(requireContext())
        if (binding.content.caution.isVisible != shouldWarning) {
            TransitionManager.beginDelayedTransition(binding.content.contentsContainer)
            binding.content.caution.isVisible = shouldWarning
        }
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
        notificationSample = NotificationSample(binding.notificationSample)
        binding.content.status.setOnClickListener { toggleStatus() }
        binding.content.detailedSetting.setOnClickListener {
            navigate(MainFragmentDirections.actionMainFragmentToDetailedSettingsFragment())
        }
        binding.content.versionDescription.text = BuildConfig.VERSION_NAME
        setUpOrientationIcons()
        binding.content.eachApp.setOnClickListener {
            navigate(MainFragmentDirections.actionMainFragmentToEachAppFragment())
        }
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
                viewModel.updateOrientation(view.orientation)
            }
        }
    }

    @SuppressLint("NewApi")
    private fun toggleStatus() {
        if (enable) {
            viewModel.updateEnabled(false)
        } else {
            if (SystemSettings.canDrawOverlays(requireContext())) {
                viewModel.updateEnabled(true)
            } else {
                OverlayPermissionDialog.show(this)
            }
        }
    }

    companion object {
        private const val CHECK_INTERVAL: Long = 5000L
        private const val PREFIX = "MainFragment."
        private const val REQUEST_KEY_NIGHT_MODE = PREFIX + "REQUEST_KEY_NIGHT_MODE"
    }
}
