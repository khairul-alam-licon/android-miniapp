package com.rakuten.tech.mobile.miniapp

import androidx.annotation.VisibleForTesting
import com.rakuten.tech.mobile.miniapp.api.ApiClient
import com.rakuten.tech.mobile.miniapp.api.ApiClientRepository
import com.rakuten.tech.mobile.miniapp.display.Displayer
import com.rakuten.tech.mobile.miniapp.js.MiniAppMessageBridge
import com.rakuten.tech.mobile.miniapp.navigator.MiniAppNavigator
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermission
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionCache
import com.rakuten.tech.mobile.miniapp.storage.DownloadedManifestCache

@Suppress("TooManyFunctions", "LongMethod", "ExpressionBodySyntax")
internal class RealMiniApp(
    private val apiClientRepository: ApiClientRepository,
    private val miniAppDownloader: MiniAppDownloader,
    private val displayer: Displayer,
    private val miniAppInfoFetcher: MiniAppInfoFetcher,
    initCustomPermissionCache: () -> MiniAppCustomPermissionCache,
    initDownloadedManifestCache: () -> DownloadedManifestCache
) : MiniApp() {

    private val miniAppCustomPermissionCache: MiniAppCustomPermissionCache by lazy { initCustomPermissionCache() }
    private val downloadedManifestCache: DownloadedManifestCache by lazy { initDownloadedManifestCache() }

    override suspend fun listMiniApp(): List<MiniAppInfo> = miniAppInfoFetcher.fetchMiniAppList()

    override suspend fun fetchInfo(appId: String): MiniAppInfo = when {
        appId.isBlank() -> throw sdkExceptionForInvalidArguments()
        else -> miniAppInfoFetcher.getInfo(appId)
    }

    override fun getCustomPermissions(
        miniAppId: String
    ): MiniAppCustomPermission {
        // return only the permissions listed in the Mini App's manifest.
        val manifestPermissions = downloadedManifestCache.getAllPermissions(miniAppId)
        return MiniAppCustomPermission(miniAppId, manifestPermissions)
    }

    override fun setCustomPermissions(miniAppCustomPermission: MiniAppCustomPermission) =
        miniAppCustomPermissionCache.storePermissions(miniAppCustomPermission)

    @Suppress("FunctionMaxLength")
    override fun listDownloadedWithCustomPermissions(): List<Pair<MiniAppInfo, MiniAppCustomPermission>> {
        return miniAppDownloader.getDownloadedMiniAppList().map {
            Pair(it, miniAppCustomPermissionCache.readPermissions(it.id))
        }
    }

    override suspend fun create(
        appId: String,
        miniAppMessageBridge: MiniAppMessageBridge,
        miniAppNavigator: MiniAppNavigator?,
        queryParams: String
    ): MiniAppDisplay = when {
        appId.isBlank() -> throw sdkExceptionForInvalidArguments()
        else -> {
            val (basePath, miniAppInfo) = miniAppDownloader.getMiniApp(appId)
            val manifest = getMiniAppManifest(appId, fetchInfo(appId).version.versionId)
            downloadedManifestCache.storeDownloadedManifest(appId, manifest) // store per miniapp id
            if (downloadedManifestCache.isRequiredPermissionDenied(appId))
                throw MiniAppSdkException(ERR_REQUIRED_PERMISSION_DENIED)
            else displayer.createMiniAppDisplay(
                basePath,
                miniAppInfo,
                miniAppMessageBridge,
                miniAppNavigator,
                miniAppCustomPermissionCache,
                queryParams
            )
        }
    }

    override suspend fun create(
        appInfo: MiniAppInfo,
        miniAppMessageBridge: MiniAppMessageBridge,
        miniAppNavigator: MiniAppNavigator?,
        queryParams: String
    ): MiniAppDisplay = when {
        appInfo.id.isBlank() -> throw sdkExceptionForInvalidArguments()
        else -> {
            val (basePath, miniAppInfo) = miniAppDownloader.getMiniApp(appInfo)
            val manifest = getMiniAppManifest(appInfo.id, appInfo.version.versionId)
            downloadedManifestCache.storeDownloadedManifest(appInfo.id, manifest) // store per miniapp id
            if (downloadedManifestCache.isRequiredPermissionDenied(appInfo.id))
                throw MiniAppSdkException(ERR_REQUIRED_PERMISSION_DENIED)
            else displayer.createMiniAppDisplay(
                basePath,
                miniAppInfo,
                miniAppMessageBridge,
                miniAppNavigator,
                miniAppCustomPermissionCache,
                queryParams
            )
        }
    }

    override suspend fun createWithUrl(
        appUrl: String,
        miniAppMessageBridge: MiniAppMessageBridge,
        miniAppNavigator: MiniAppNavigator?,
        queryParams: String
    ): MiniAppDisplay = when {
        appUrl.isBlank() -> throw sdkExceptionForInvalidArguments()
        else -> {
            miniAppDownloader.validateHttpAppUrl(appUrl)
            displayer.createMiniAppDisplay(
                appUrl,
                miniAppMessageBridge,
                miniAppNavigator,
                miniAppCustomPermissionCache,
                queryParams
            )
        }
    }

    override suspend fun getMiniAppManifest(appId: String, versionId: String): MiniAppManifest =
        miniAppDownloader.fetchMiniAppManifest(appId, versionId)

    override fun getDownloadedManifest(appId: String): MiniAppManifest? {
        return downloadedManifestCache.readDownloadedManifest(appId)
    }

    override fun updateConfiguration(newConfig: MiniAppSdkConfig) {
        var nextApiClient = apiClientRepository.getApiClientFor(newConfig.key)
        if (nextApiClient == null) {
            nextApiClient = createApiClient(newConfig)
            apiClientRepository.registerApiClient(newConfig.key, nextApiClient)
        }

        nextApiClient.also {
            miniAppDownloader.updateApiClient(it)
            miniAppInfoFetcher.updateApiClient(it)
        }
    }

    @VisibleForTesting
    internal fun createApiClient(newConfig: MiniAppSdkConfig) = ApiClient(
        baseUrl = newConfig.baseUrl,
        rasProjectId = newConfig.rasProjectId,
        subscriptionKey = newConfig.subscriptionKey,
        isPreviewMode = newConfig.isPreviewMode
    )

    private companion object {
        const val ERR_REQUIRED_PERMISSION_DENIED = "Required permissions are not granted yet for this miniapp."
    }
}
