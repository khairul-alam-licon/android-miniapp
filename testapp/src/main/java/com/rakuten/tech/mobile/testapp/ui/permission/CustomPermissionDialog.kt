package com.rakuten.tech.mobile.testapp.ui.permission

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.view.View

class CustomPermissionDialog {

    class Builder {
        var alert: AlertDialog.Builder? = null

        fun build(context: Context?, miniappName: String, permissions: List<String>): Builder {
            alert = AlertDialog.Builder(context)

            val permissionSetName = permissions.toString()
            alert?.setMessage("Allow $miniappName to access custom permissions of $permissionSetName")
            return this
        }

        fun setView(view: View): Builder {
            alert?.setView(view)
            return this
        }

        fun setListener(listener: DialogInterface.OnClickListener): Builder {
            alert?.setPositiveButton("Close", listener)
            return this
        }

        fun show() {
            alert?.create()?.show()
        }
    }
}
