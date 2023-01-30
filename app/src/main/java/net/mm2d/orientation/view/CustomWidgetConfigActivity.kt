/*
 * Copyright (c) 2023 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.view

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import net.mm2d.android.orientationfaker.R
import net.mm2d.android.orientationfaker.databinding.ActivityCustomWidgetConfigBinding

@AndroidEntryPoint
class CustomWidgetConfigActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCustomWidgetConfigBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomWidgetConfigBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        supportFragmentManager.beginTransaction()
            .add(R.id.fragment, CustomWidgetConfigFragment.create(this))
            .commit()
    }

    override fun finish() {
        setResult(Activity.RESULT_OK)
        super.finish()
    }
}
