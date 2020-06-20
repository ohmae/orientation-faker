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
import kotlinx.android.synthetic.main.activity_each_app.*
import kotlinx.android.synthetic.main.li_each_app.view.*
import kotlinx.coroutines.*
import net.mm2d.android.orientationfaker.R
import net.mm2d.orientation.control.ForegroundPackageSettings
import net.mm2d.orientation.control.Orientation
import net.mm2d.orientation.control.OrientationHelper
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
    private var adapter: Adapter? = null
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_each_app)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        recycler_view.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        setUpSearch()
        scope.launch {
            val list = makeAppList()
            withContext(Dispatchers.Main) {
                setAdapter(list)
            }
        }
    }

    private fun setUpSearch() {
        search_window.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = Unit
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                adapter?.search(s.toString())
            }
        })
        search_window.setOnEditorActionListener { _, _, _ ->
            hideKeyboard()
            true
        }
    }

    private fun setAdapter(list: List<AppInfo>) {
        ForegroundPackageSettings.updateInstalledPackages(list.map { it.packageName })
        Adapter(this, list) { position, packageName ->
            hideKeyboard()
            EachAppOrientationDialog.show(this, position, packageName)
        }.let {
            adapter = it
            recycler_view.adapter = it
            it.search(search_window.text.toString())
        }
        progress_bar.visibility = View.GONE
    }

    private fun hideKeyboard() {
        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            .hideSoftInputFromWindow(search_window.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    override fun onResume() {
        super.onResume()
        package_check_disabled.isGone = settings.foregroundPackageCheckEnabled
    }

    override fun onPostResume() {
        super.onPostResume()
        if (!SystemSettings.hasUsageAccessPermission(this)) {
            UsageAppPermissionDialog.showDialog(this)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.each_app, menu)
        (menu.findItem(R.id.package_check).actionView as SwitchCompat).also {
            it.isChecked = settings.foregroundPackageCheckEnabled
            it.setOnCheckedChangeListener { _, isChecked ->
                hideKeyboard()
                settings.foregroundPackageCheckEnabled = isChecked
                package_check_disabled.isGone = isChecked
                MainService.update(this)
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
        if (OrientationHelper.isEnabled) {
            MainService.start(this)
        }
    }

    private fun makeAppList(): List<AppInfo> {
        val intent = Intent(Intent.ACTION_MAIN).also {
            it.addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PackageManager.MATCH_ALL else 0
        val pm = packageManager
        return pm.queryIntentActivities(intent, flag)
            .asSequence()
            .mapNotNull { it.activityInfo }
            .filter { it.packageName != packageName }
            .map {
                AppInfo(
                    it,
                    it.loadLabel(pm).toString(),
                    it.packageName
                )
            }
            .sortedBy { it.label }
            .toList()
    }

    data class AppInfo(
        val activityInfo: ActivityInfo,
        val label: String,
        val packageName: String
    ) {
        var icon: Drawable? = null
    }

    private class DiffCallback(
        private val old: List<AppInfo>,
        private val new: List<AppInfo>
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int = old.size
        override fun getNewListSize(): Int = new.size
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            old[oldItemPosition] == new[newItemPosition]

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            old[oldItemPosition] == new[newItemPosition]
    }

    class Adapter(
        private val context: Context,
        private val initialList: List<AppInfo>,
        private val listener: (position: Int, packageName: String) -> Unit
    ) : RecyclerView.Adapter<ViewHolder>() {
        private val inflater: LayoutInflater = LayoutInflater.from(context)
        private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
        private var list: List<AppInfo> = initialList
        private var defaultIcon: Drawable? = null
        private var searchWord: String = ""

        fun search(word: String) {
            val w = word.toLowerCase(Locale.ENGLISH)
            if (w == searchWord) return
            searchWord = w
            val newList = initialList.filter { it.contains(w) }
            val diff = DiffUtil.calculateDiff(DiffCallback(list, newList))
            list = newList
            diff.dispatchUpdatesTo(this)
        }

        private fun AppInfo.contains(word: String): Boolean =
            label.toLowerCase(Locale.ENGLISH).contains(word) ||
                packageName.toLowerCase(Locale.ENGLISH).contains(word)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(inflater.inflate(R.layout.li_each_app, parent, false))

        override fun getItemCount(): Int = list.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            bind(holder.itemView, position, list[position])
        }

        private fun getDefaultIcon(): Drawable =
            defaultIcon ?: AppCompatResources.getDrawable(context, R.drawable.ic_launcher_default)!!.also {
                defaultIcon = it
            }

        private fun bind(itemView: View, position: Int, info: AppInfo) {
            itemView.tag = position
            if (info.icon != null) {
                itemView.app_icon.setImageDrawable(info.icon)
            } else {
                scope.launch {
                    info.icon = info.activityInfo.loadIcon(context.packageManager)
                        ?: getDefaultIcon()
                    withContext(Dispatchers.Main) {
                        if (itemView.tag == position) {
                            itemView.app_icon.setImageDrawable(info.icon)
                        }
                    }
                }
            }
            itemView.app_name.text = info.label
            itemView.app_package.text = info.packageName
            val orientation = ForegroundPackageSettings.get(info.packageName)
            if (orientation != Orientation.INVALID) {
                Orientation.values.find { it.orientation == orientation }?.let {
                    itemView.orientation_icon.setImageResource(it.icon)
                    itemView.orientation_name.setText(it.label)
                }
            } else {
                itemView.orientation_icon.setImageResource(0)
                itemView.orientation_name.text = ""
            }
            itemView.setOnClickListener {
                listener(position, info.packageName)
            }
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, EachAppActivity::class.java))
        }
    }
}
