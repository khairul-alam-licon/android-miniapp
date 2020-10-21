package com.rakuten.tech.mobile.miniapp.js

import androidx.annotation.VisibleForTesting
import com.google.gson.Gson
import com.rakuten.tech.mobile.miniapp.MiniAppSdkException
import com.rakuten.tech.mobile.miniapp.permission.*
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionCache
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionManager

/**
 * A class to provide the interface for requesting custom permissions.
 */
abstract class CustomPermissionBridgeDispatcher {

    private lateinit var bridgeExecutor: MiniAppBridgeExecutor
    private lateinit var customPermissionCache: MiniAppCustomPermissionCache
    private lateinit var miniAppId: String

    internal fun init(
        bridgeExecutor: MiniAppBridgeExecutor,
        miniAppCustomPermissionCache: MiniAppCustomPermissionCache,
        miniAppId: String
    ) {
        this.bridgeExecutor = bridgeExecutor
        this.customPermissionCache = miniAppCustomPermissionCache
        this.miniAppId = miniAppId
    }

    /**
     * Post custom permissions request.
     * @param permissionsWithDescription list of name and descriptions of custom permissions sent from external.
     * @param callback to invoke a list of name and grant results of custom permissions sent from hostapp.
     */
    open fun requestCustomPermissions(
        permissionsWithDescription: List<Pair<MiniAppCustomPermissionType, String>>,
        callback: (List<Pair<MiniAppCustomPermissionType, MiniAppCustomPermissionResult>>) -> Unit
    ) {
        throw MiniAppSdkException(
            "The `MiniAppMessageBridge.requestCustomPermissions`" +
                    " method has not been implemented by the Host App."
        )
    }

    @Suppress("LongMethod")
    internal fun onRequestCustomPermissions(jsonStr: String) {
        var callbackObj: CustomPermissionCallbackObj? = null

        try {
            callbackObj = Gson().fromJson(jsonStr, CustomPermissionCallbackObj::class.java)
            val permissionObjList = arrayListOf<CustomPermissionObj>()
            callbackObj.param?.permissions?.forEach {
                permissionObjList.add(CustomPermissionObj(it.name, it.description))
            }

            val permissionsWithDescription = MiniAppCustomPermissionManager()
                .preparePermissionsWithDescription(permissionObjList)

            requestCustomPermissions(
                permissionsWithDescription
            ) { permissionsWithResult ->
                // store values in SDK cache
                val miniAppCustomPermission = MiniAppCustomPermission(
                    miniAppId = miniAppId,
                    pairValues = permissionsWithResult
                )
                customPermissionCache.storePermissions(miniAppCustomPermission)

                // send JSON response to miniapp
                onRequestCustomPermissionsResult(
                    callbackId = callbackObj.id,
                    jsonResult = MiniAppCustomPermissionManager().createJsonResponse(
                        customPermissionCache,
                        miniAppCustomPermission.miniAppId,
                        permissionsWithDescription
                    )
                )
            }
        } catch (e: Exception) {
            callbackObj?.id?.let {
                bridgeExecutor.postError(
                    it,
                    "${ErrorBridgeMessage.ERR_REQ_CUSTOM_PERMISSION} ${e.message}"
                )
            }
        }
    }

    /** Inform the custom permission request result to MiniApp. **/
    @Suppress("FunctionMaxLength")
    @VisibleForTesting
    private fun onRequestCustomPermissionsResult(callbackId: String, jsonResult: String) {
        bridgeExecutor.postValue(callbackId, jsonResult)
    }
}
