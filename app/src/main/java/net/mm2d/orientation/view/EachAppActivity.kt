package net.mm2d.orientation.view

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_each_app.*
import kotlinx.android.synthetic.main.li_each_app.view.*
import net.mm2d.android.orientationfaker.R
import net.mm2d.orientation.control.ForegroundPackageSettings
import net.mm2d.orientation.control.Orientation
import net.mm2d.orientation.control.OrientationHelper
import net.mm2d.orientation.service.MainService
import net.mm2d.orientation.util.SystemSettings
import net.mm2d.orientation.view.dialog.EachAppOrientationDialog
import net.mm2d.orientation.view.dialog.UsageAppPermissionDialog

class EachAppActivity : AppCompatActivity(), EachAppOrientationDialog.Callback {
    private var defaultIcon: Drawable? = null
    private lateinit var adapter: Adapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_each_app)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        recycler_view.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        val list = makeAppList()
        adapter = Adapter(this, list) { position, packageName ->
            EachAppOrientationDialog.show(this, position, packageName)
        }
        recycler_view.adapter = adapter
        ForegroundPackageSettings.updateInstalledPackages(list.map { it.packageName })
    }

    override fun onPostResume() {
        super.onPostResume()
        if (!SystemSettings.hasUsageAccessPermission(this)) {
            UsageAppPermissionDialog.showDialog(this)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onChangeSettings(position: Int) {
        adapter.notifyItemChanged(position)
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
                    it.loadIcon(pm) ?: getDefaultIcon(),
                    it.loadLabel(pm).toString(),
                    it.packageName
                )
            }
            .sortedBy { it.label }
            .toList()
    }

    private fun getDefaultIcon(): Drawable =
        defaultIcon ?: AppCompatResources.getDrawable(this, R.drawable.ic_launcher_default)!!.also {
            defaultIcon = it
        }

    data class AppInfo(
        val icon: Drawable,
        val label: String,
        val packageName: String
    )

    class Adapter(
        context: Context,
        private val list: List<AppInfo>,
        private val listener: (position: Int, packageName: String) -> Unit
    ) : RecyclerView.Adapter<ViewHolder>() {
        private val inflater: LayoutInflater = LayoutInflater.from(context)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(inflater.inflate(R.layout.li_each_app, parent, false))

        override fun getItemCount(): Int = list.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val info = list[position]
            holder.bind(info)
            holder.itemView.setOnClickListener {
                listener(position, info.packageName)
            }
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(info: AppInfo) {
            itemView.app_icon.setImageDrawable(info.icon)
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
        }
    }

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, EachAppActivity::class.java))
        }
    }
}
