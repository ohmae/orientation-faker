/*
 * Copyright (c) 2014 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.view

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import net.mm2d.android.orientationfaker.BuildConfig
import net.mm2d.android.orientationfaker.R
import net.mm2d.android.orientationfaker.databinding.ActivityMainBinding
import net.mm2d.orientation.settings.PreferenceRepository
import net.mm2d.orientation.util.DeviceOrientationChecker
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var preferenceRepository: PreferenceRepository
    private lateinit var binding: ActivityMainBinding
    private val appBarElevation: Float by lazy {
        resources.getDimension(R.dimen.appbar_elevation)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navController = binding.navHost.getFragment<NavHostFragment>().navController
        binding.toolbar.setupWithNavController(navController)
        binding.version.text = getString(R.string.app_version_format, BuildConfig.VERSION_NAME)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.version.isVisible = destination.id == R.id.MainFragment
            binding.appBar.elevation = if (destination.id == R.id.EachAppFragment) 0f else appBarElevation
        }

        setSupportActionBar(binding.toolbar)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                DeviceOrientationChecker.check(this@MainActivity, preferenceRepository)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val navController = binding.navHost.getFragment<NavHostFragment>().navController
        return item.onNavDestinationSelected(navController) || when (item.itemId) {
            android.R.id.home -> navController.popBackStack()
            else -> super.onOptionsItemSelected(item)
        }
    }
}
