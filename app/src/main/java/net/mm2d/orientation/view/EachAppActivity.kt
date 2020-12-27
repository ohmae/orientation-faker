package net.mm2d.orientation.view

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.isGone
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import net.mm2d.android.orientationfaker.R
import net.mm2d.android.orientationfaker.databinding.ActivityEachAppBinding
import net.mm2d.android.orientationfaker.databinding.ItemEachAppBinding
import net.mm2d.orientation.control.ForegroundPackageSettings
import net.mm2d.orientation.control.Orientation
import net.mm2d.orientation.service.MainController
import net.mm2d.orientation.service.MainService
import net.mm2d.orientation.settings.Settings
import net.mm2d.orientation.util.SystemSettings
import net.mm2d.orientation.view.dialog.EachAppOrientationDialog
import net.mm2d.orientation.view.dialog.UsageAppPermissionDialog
import java.util.*

class EachAppActivity : AppCompatActivity(), EachAppOrientationDialog.Callback {
    private val settings by lazy {
        Settings.get()
    }
    private var adapter: EachAppAdapter? = null
    private val job = Job()
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + job)
    private lateinit var binding: ActivityEachAppBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEachAppBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        setUpSearch()
        setUpBottom()
        scope.launch {
            val list = makeAppList()
            withContext(Dispatchers.Main) {
                setAdapter(list)
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
        val adapter = adapter ?: return
        adapter.search(word)
        binding.noAppCaution.isGone = adapter.itemCount != 0
        binding.recyclerView.scrollToPosition(0)
    }

    private fun setUpBottom() {
        binding.showAllCheck.isChecked = settings.showAllApps
        binding.showAllCheck.setOnCheckedChangeListener { _, isChecked ->
            settings.showAllApps = isChecked
            adapter?.update()
        }
        binding.resetButton.setOnClickListener {
            ForegroundPackageSettings.reset()
            adapter?.notifyDataSetChanged()
        }
    }

    private fun setAdapter(list: List<AppInfo>) {
        ForegroundPackageSettings.updateInstalledPackages(list.map { it.packageName })
        val adapter = EachAppAdapter(this, list) { position, packageName ->
            hideKeyboard()
            EachAppOrientationDialog.show(this, position, packageName)
        }
        this.adapter = adapter
        binding.recyclerView.adapter = adapter
        binding.progressBar.visibility = View.GONE
    }

    private fun hideKeyboard() {
        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            .hideSoftInputFromWindow(binding.searchWindow.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    override fun onResume() {
        super.onResume()
        binding.packageCheckDisabled.isGone = settings.foregroundPackageCheckEnabled
    }

    override fun onPostResume() {
        super.onPostResume()
        if (!SystemSettings.hasUsageAccessPermission(this)) {
            UsageAppPermissionDialog.show(this)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.each_app, menu)
        (menu.findItem(R.id.package_check).actionView as SwitchCompat).also {
            it.isChecked = settings.foregroundPackageCheckEnabled
            it.setOnCheckedChangeListener { _, isChecked ->
                hideKeyboard()
                settings.foregroundPackageCheckEnabled = isChecked
                binding.packageCheckDisabled.isGone = isChecked
                MainController.update()
            }
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onChangeSettings(position: Int) {
        adapter?.notifyItemChanged(position)
        if (MainService.isStarted) {
            MainController.update()
        }
    }

    private fun makeAppList(): List<AppInfo> {
        val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PackageManager.MATCH_ALL else 0
        val pm = packageManager
        val allApps = pm.getInstalledPackages(PackageManager.GET_ACTIVITIES)
            .mapNotNull { it.activities?.getOrNull(0) }
            .map { it to false }
        val launcherApps = pm.queryIntentActivities(categoryIntent(Intent.CATEGORY_LAUNCHER), flag)
            .mapNotNull { it.activityInfo }
            .map { it to true }
        val launcher = pm.queryIntentActivities(categoryIntent(Intent.CATEGORY_HOME), flag)
            .mapNotNull { it.activityInfo }
            .map { it to true }
        return (launcherApps + launcher + allApps)
            .filter { it.first.packageName != packageName }
            .distinctBy { it.first.packageName }
            .map { appInfo(pm, it.first, it.second) }
            .sortedBy { it.label }
    }

    private fun categoryIntent(category: String): Intent =
        Intent(Intent.ACTION_MAIN).also { it.addCategory(category) }

    private fun appInfo(pm: PackageManager, activityInfo: ActivityInfo, launcher: Boolean): AppInfo =
        AppInfo(activityInfo, activityInfo.loadLabel(pm).toString(), activityInfo.packageName, launcher)

    data class AppInfo(
        val activityInfo: ActivityInfo,
        val label: String,
        val packageName: String,
        val launcher: Boolean
    ) {
        var icon: Drawable? = null
    }

    private class DiffCallback(
        private val oldList: List<AppInfo>,
        private val newList: List<AppInfo>
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            oldList[oldItemPosition].packageName == newList[newItemPosition].packageName

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            oldList[oldItemPosition].packageName == newList[newItemPosition].packageName
    }

    private class EachAppAdapter(
        private val context: Context,
        private val initialList: List<AppInfo>,
        private val listener: (position: Int, packageName: String) -> Unit
    ) : RecyclerView.Adapter<ViewHolder>() {
        private val inflater: LayoutInflater = LayoutInflater.from(context)
        private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
        private val settings: Settings = Settings.get()
        private val defaultIcon: Drawable by lazy {
            AppCompatResources.getDrawable(context, R.drawable.ic_launcher_default)!!
        }
        private var searchWord: String = ""
        private var list: List<AppInfo> =
            if (settings.showAllApps) initialList else initialList.filter { it.launcher }

        fun search(word: String) {
            val w = word.toLowerCase(Locale.ENGLISH)
            if (w == searchWord) return
            searchWord = w
            update()
        }

        fun update() {
            val oldList = list
            list = filter()
            val result = DiffUtil.calculateDiff(DiffCallback(oldList, list))
            result.dispatchUpdatesTo(this)
        }

        private fun filter(): List<AppInfo> {
            val word = searchWord
            return if (word.isEmpty()) {
                if (settings.showAllApps) initialList else initialList.filter { it.launcher }
            } else {
                if (settings.showAllApps) {
                    initialList.filter { it.contains(word) }
                } else {
                    initialList.filter { it.launcher && it.contains(word) }
                }
            }
        }

        private fun AppInfo.contains(word: String): Boolean =
            label.toLowerCase(Locale.ENGLISH).contains(word) ||
                packageName.toLowerCase(Locale.ENGLISH).contains(word)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(ItemEachAppBinding.inflate(inflater, parent, false))

        override fun getItemCount(): Int = list.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val binding = holder.binding
            val info = list[position]
            binding.root.tag = position
            if (info.icon != null) {
                binding.appIcon.setImageDrawable(info.icon)
            } else {
                scope.launch {
                    info.icon = info.activityInfo.loadIcon(context.packageManager) ?: defaultIcon
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
                Orientation.values.find { it.orientation == orientation }?.let {
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
        fun start(context: Context) {
            context.startActivity(Intent(context, EachAppActivity::class.java))
        }
    }
}
