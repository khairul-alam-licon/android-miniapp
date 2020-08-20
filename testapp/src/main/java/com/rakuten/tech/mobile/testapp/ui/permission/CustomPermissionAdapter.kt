package com.rakuten.tech.mobile.testapp.ui.permission

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView
import com.rakuten.tech.mobile.miniapp.MiniApp
import com.rakuten.tech.mobile.miniapp.testapp.databinding.ItemListCustomPermissionBinding
import kotlinx.android.synthetic.main.item_list_custom_permission.view.*

class CustomPermissionAdapter : RecyclerView.Adapter<CustomPermissionAdapter.ViewHolder?>() {
    private var permissionNames: List<String> = emptyList()
    var storedPermissionToggle: HashMap<String, String> = hashMapOf()
    var permissionToggles = mutableListOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemListCustomPermissionBinding.inflate(layoutInflater, parent, false)

        return ViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.permissionName.text = permissionNames[position].camelCaseToReadable()
        if (storedPermissionToggle.containsKey(permissionNames[position]))
            holder.permissionSwitch.isChecked =
                storedPermissionToggle[permissionNames[position]]?.let {
                    permissionResultToChecked(
                        it
                    )
                }!!

        permissionToggles.add(position, permissionResultToText(holder.permissionSwitch.isChecked))

        holder.permissionSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            permissionToggles.removeAt(position)
            permissionToggles.add(
                position,
                permissionResultToText(holder.permissionSwitch.isChecked)
            )
        }
    }

    override fun getItemCount(): Int = permissionNames.size

    fun addPermissionList(names: List<String>, toggles: HashMap<String, String>) {
        permissionNames = names
        storedPermissionToggle = toggles
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val permissionName: TextView = itemView.permissionText
        val permissionSwitch: SwitchCompat = itemView.permissionSwitch
    }

    private fun String.camelCaseToReadable(): String {
        return this[0].toUpperCase() + this.replace(
            String.format(
                "%s|%s|%s",
                "(?<=[A-Z])(?=[A-Z][a-z])",
                "(?<=[^A-Z])(?=[A-Z])",
                "(?<=[A-Za-z])(?=[^A-Za-z])"
            ).toRegex(), " "
        ).substring(1)
    }

    private fun permissionResultToText(isChecked: Boolean): String {
        if (isChecked)
            return MiniApp.CUSTOM_PERMISSION_ALLOWED

        return MiniApp.CUSTOM_PERMISSION_DENIED
    }

    private fun permissionResultToChecked(result: String): Boolean {
        if (result == MiniApp.CUSTOM_PERMISSION_ALLOWED)
            return true

        return false
    }
}
