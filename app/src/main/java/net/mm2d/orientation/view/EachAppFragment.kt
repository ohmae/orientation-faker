/*
 * Copyright (c) 2020 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageItemInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.MenuProvider
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.mm2d.android.orientationfaker.R
import net.mm2d.android.orientationfaker.databinding.FragmentEachAppBinding
import net.mm2d.android.orientationfaker.databinding.ItemEachAppBinding
import net.mm2d.orientation.control.ForegroundPackageSettings
import net.mm2d.orientation.control.Orientation
import net.mm2d.orientation.control.Orientations
import net.mm2d.orientation.util.SystemSettings
import net.mm2d.orientation.util.autoCleared
import net.mm2d.orientation.view.dialog.EachAppOrientationDialog
import net.mm2d.orientation.view.dialog.UsageAppPermissionDialog
import java.util.*

@AndroidEntryPoint
class EachAppFragment : Fragment(R.layout.fragment_each_app) {
    private var adapter: EachAppAdapter by autoCleared()
    private var binding: FragmentEachAppBinding by autoCleared()
    private val viewModel: EachAppFragmentViewModel by viewModels()
    private var shouldControlByForegroundApp: Boolean = false
    private var menuSwitch: SwitchCompat? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentEachAppBinding.bind(view)
        binding.recyclerView.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        val adapter = EachAppAdapter(requireContext()) { position, packageName ->
            hideKeyboard()
            EachAppOrientationDialog.show(this, REQUEST_KEY_ORIENTATION, position, packageName)
        }
        this.adapter = adapter
        binding.recyclerView.adapter = adapter

        setUpSearch()
        setUpBottom()
        setUpMenu()
        EachAppOrientationDialog.registerListener(this, REQUEST_KEY_ORIENTATION)
        { position, packageName, orientation ->
            ForegroundPackageSettings.put(packageName, orientation)
            adapter.notifyItemChanged(position)
        }
        viewModel.menu.observe(viewLifecycleOwner) {
            binding.showAllCheck.isChecked = it.shouldShowAllApp
            adapter.setShowAllApps(it.shouldShowAllApp)
        }
        viewModel.orientation.observe(viewLifecycleOwner) {
            shouldControlByForegroundApp = it.shouldControlByForegroundApp
            binding.packageCheckDisabled.isGone = it.shouldControlByForegroundApp
            menuSwitch?.let { menuSwitch ->
                if (menuSwitch.isChecked != it.shouldControlByForegroundApp) {
                    menuSwitch.isChecked = it.shouldControlByForegroundApp
                }
            }
        }

        val context = requireContext()
        lifecycleScope.launch(Dispatchers.IO) {
            runCatching {
                val list = makeAppList(context)
                withContext(Dispatchers.Main) {
                    setAppList(list)
                }
            }
        }
    }

    private fun setUpSearch() {
        binding.searchWindow.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = Unit
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                search(s.toString())
            }
        })
        binding.searchWindow.setOnEditorActionListener { _, _, _ ->
            hideKeyboard()
            true
        }
    }

    private fun search(word: String) {
        if (!lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)) {
            return
        }
        adapter.search(word)
        binding.noAppCaution.isGone = adapter.isNotEmpty()
        binding.recyclerView.scrollToPosition(0)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setUpBottom() {
        binding.showAllCheck.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateShowAllApps(isChecked)
            binding.noAppCaution.isGone = adapter.isNotEmpty()
            binding.recyclerView.scrollToPosition(0)
        }
        binding.resetButton.setOnClickListener {
            ForegroundPackageSettings.reset()
            adapter.notifyDataSetChanged()
        }
    }

    private fun setAppList(list: List<AppInfo>) {
        ForegroundPackageSettings.updateInstalledPackages(list.mapTo(LinkedHashSet(list.size)) { it.packageName })
        adapter.updateList(list)
        binding.noAppCaution.isGone = adapter.isNotEmpty()
        binding.progressBar.visibility = View.GONE
    }

    private fun hideKeyboard() {
        WindowInsetsControllerCompat(requireActivity().window, binding.searchWindow)
            .hide(WindowInsetsCompat.Type.ime())
    }

    override fun onResume() {
        super.onResume()
        if (!SystemSettings.hasUsageAccessPermission(requireContext())) {
            UsageAppPermissionDialog.show(this)
        }
    }

    private fun setUpMenu() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.each_app, menu)
                (menu.findItem(R.id.package_check).actionView as SwitchCompat).also {
                    menuSwitch = it
                    it.isChecked = shouldControlByForegroundApp
                    it.setOnCheckedChangeListener { _, isChecked ->
                        hideKeyboard()
                        viewModel.updateControlByForegroundApp(isChecked)
                    }
                }
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean = true
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun makeAppList(context: Context): List<AppInfo> {
        val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PackageManager.MATCH_ALL else 0
        val pm = context.packageManager
        val allApps = pm.getInstalledPackages(PackageManager.GET_ACTIVITIES)
            .mapNotNull { it.applicationInfo }
            .map { it to false }
        val launcherApps = pm.queryIntentActivities(categoryIntent(Intent.CATEGORY_LAUNCHER), flag)
            .mapNotNull { it.activityInfo }
            .map { it to true }
        val launcher = pm.queryIntentActivities(categoryIntent(Intent.CATEGORY_HOME), flag)
            .mapNotNull { it.activityInfo }
            .map { it to true }
        return (launcherApps + launcher + allApps)
            .filter { it.first.packageName != context.packageName }
            .distinctBy { it.first.packageName }
            .map { appInfo(pm, it.first, it.second) }
            .sortedBy { it.label }
    }

    private fun categoryIntent(category: String): Intent =
        Intent(Intent.ACTION_MAIN).also { it.addCategory(category) }

    private fun appInfo(pm: PackageManager, info: PackageItemInfo, launcher: Boolean): AppInfo =
        AppInfo(info, info.loadLabel(pm).toString(), info.packageName, launcher)

    data class AppInfo(
        val info: PackageItemInfo,
        val label: String,
        val packageName: String,
        val launcher: Boolean,
        var icon: Drawable? = null
    )

    private class EachAppAdapter(
        private val context: Context,
        private val listener: (position: Int, packageName: String) -> Unit
    ) : ListAdapter<AppInfo, ViewHolder>(object : DiffUtil.ItemCallback<AppInfo>() {
        override fun areItemsTheSame(oldItem: AppInfo, newItem: AppInfo): Boolean =
            oldItem.packageName == newItem.packageName

        override fun areContentsTheSame(oldItem: AppInfo, newItem: AppInfo): Boolean =
            oldItem == newItem
    }) {
        private val inflater: LayoutInflater = LayoutInflater.from(context)
        private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        private val defaultIcon: Drawable by lazy {
            AppCompatResources.getDrawable(context, R.drawable.ic_launcher_default)!!
        }
        private var searchWord: String = ""
        private var shouldShowAllApps: Boolean = false
        private var allList: List<AppInfo> = emptyList()
        private var filteredList: List<AppInfo> = emptyList()

        fun search(word: String) {
            val w = word.lowercase(Locale.ENGLISH)
            if (w == searchWord) return
            searchWord = w
            update()
        }

        fun setShowAllApps(show: Boolean) {
            shouldShowAllApps = show
            update()
        }

        fun updateList(list: List<AppInfo>) {
            allList = list
            update()
        }

        private fun update() {
            filteredList = filter()
            submitList(filteredList)
        }

        fun isNotEmpty(): Boolean = filteredList.isNotEmpty()

        private fun filter(): List<AppInfo> {
            val word = searchWord
            return if (word.isEmpty()) {
                if (shouldShowAllApps) allList else allList.filter { it.launcher }
            } else {
                if (shouldShowAllApps) {
                    allList.filter { it.contains(word) }
                } else {
                    allList.filter { it.launcher && it.contains(word) }
                }
            }
        }

        private fun AppInfo.contains(word: String): Boolean =
            label.lowercase(Locale.ENGLISH).contains(word) ||
                packageName.lowercase(Locale.ENGLISH).contains(word)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(ItemEachAppBinding.inflate(inflater, parent, false))

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val binding = holder.binding
            val info = getItem(position)
            binding.root.tag = position
            if (info.icon != null) {
                binding.appIcon.setImageDrawable(info.icon)
            } else {
                scope.launch {
                    info.icon = info.info.loadIcon(context.packageManager) ?: defaultIcon
                    withContext(Dispatchers.Main) {
                        if (binding.root.tag == position) {
                            binding.appIcon.setImageDrawable(info.icon)
                        }
                    }
                }
            }
            binding.appName.text = info.label
            binding.appPackage.text = info.packageName
            val orientation = ForegroundPackageSettings.get(info.packageName)
            if (orientation != Orientation.INVALID) {
                Orientations.find(orientation)?.let {
                    binding.orientationIcon.setImageResource(it.icon)
                    binding.orientationName.setText(it.label)
                }
            } else {
                binding.orientationIcon.setImageResource(0)
                binding.orientationName.text = ""
            }
            binding.root.setOnClickListener {
                listener(holder.adapterPosition, info.packageName)
            }
        }
    }

    class ViewHolder(val binding: ItemEachAppBinding) : RecyclerView.ViewHolder(binding.root)

    companion object {
        private const val PREFIX = "EachAppFragment."
        private const val REQUEST_KEY_ORIENTATION = PREFIX + "REQUEST_KEY_ORIENTATION"
    }
}
