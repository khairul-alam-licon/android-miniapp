package com.rakuten.tech.mobile.testapp.ui.permission

import android.app.Activity
import android.content.DialogInterface
import android.view.LayoutInflater
import androidx.recyclerview.widget.LinearLayoutManager
import com.rakuten.tech.mobile.miniapp.MiniApp
import com.rakuten.tech.mobile.miniapp.testapp.databinding.ListCustomPermissionBinding

fun promptCustomPermission(activity: Activity, permissions: List<String>) {
    val layoutInflater = LayoutInflater.from(activity)
    val permissionLayout = ListCustomPermissionBinding.inflate(layoutInflater, null, false)
    permissionLayout.listCustomPermission.layoutManager = LinearLayoutManager(activity)
    val adapter = CustomPermissionAdapter()

    val results: HashMap<String, String> = hashMapOf()
    permissions.forEachIndexed { index, _ ->
        results[permissions[index]] =
            MiniApp.selfReadCustomPermissions(activity, permissions)[index]
    }

    adapter.addPermissionList(permissions, results)
    permissionLayout.listCustomPermission.adapter = adapter

    val listener = DialogInterface.OnClickListener { _, _ ->
        MiniApp.setCustomPermissions(
            activity,
            permissions,
            adapter.permissionToggles
        )
    }

    // TODO: Brushup dialog title
    val permissionDialogBuilder =
        CustomPermissionDialog.Builder().build(activity, "miniApp name", permissions).apply {
            setView(permissionLayout.root)
            setListener(listener)
        }

    if (isAnyDeniedCustomPermission(activity, permissions)) {
        permissionDialogBuilder.show()
    } else {
        MiniApp.getCustomPermissions(activity, permissions)
    }
}

private fun isAnyDeniedCustomPermission(activity: Activity, permissions: List<String>): Boolean {
    val storedGrandResults = MiniApp.selfReadCustomPermissions(activity, permissions)
    return storedGrandResults.contains(MiniApp.CUSTOM_PERMISSION_DENIED)
}
