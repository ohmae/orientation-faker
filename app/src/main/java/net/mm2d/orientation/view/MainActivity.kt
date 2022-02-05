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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import net.mm2d.android.orientationfaker.R
import net.mm2d.android.orientationfaker.databinding.ActivityMainBinding
import net.mm2d.orientation.settings.PreferenceRepository
import net.mm2d.orientation.util.DeviceOrientationChecker
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var preferenceRepository: PreferenceRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        binding.toolbar.setupWithNavController(navHostFragment.navController)
        navHostFragment.navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.EachAppFragment) {
                binding.appBar.elevation = 0f
            } else {
                binding.appBar.elevation = resources.getDimension(R.dimen.design_appbar_elevation)
            }
        }
        setSupportActionBar(binding.toolbar)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                DeviceOrientationChecker.check(this@MainActivity, preferenceRepository)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return item.onNavDestinationSelected(navController) || when (item.itemId) {
            android.R.id.home -> navController.popBackStack()
            else -> super.onOptionsItemSelected(item)
        }
    }
}
