package shuttle.apps.data

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import shuttle.apps.domain.AppsRepository
import shuttle.apps.domain.model.AppId
import shuttle.apps.domain.model.AppModel
import shuttle.apps.domain.model.AppName
import shuttle.database.App
import shuttle.database.datasource.AppDataSource
import shuttle.database.model.DatabaseAppId
import shuttle.settings.domain.usecase.IsBlacklisted
import kotlin.coroutines.coroutineContext
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class AppsRepositoryImpl(
    private val dataSource: AppDataSource,
    private val ioDispatcher: CoroutineDispatcher,
    private val isBlacklisted: IsBlacklisted,
    private val packageManager: PackageManager,
) : AppsRepository {

    override fun observeAllInstalledApps(): Flow<List<AppModel>> =
        merge(observeAllInstalledAppsFromCache(), observeAndRefreshAppsFromDevice())

    override fun observeInstalledIconPacks(): Flow<List<AppModel>> =
        flow {
            while (coroutineContext.isActive) {
                emit(getIconPacksFromDevice())
                delay(RefreshDelay)
            }
        }

    override fun observeNotBlacklistedApps(): Flow<List<AppModel>> =
        observeAllInstalledApps().map { list ->
            list.filterNot { isBlacklisted(it.id) }
        }

    private fun observeAllInstalledAppsFromCache(): Flow<List<AppModel>> =
        dataSource.findAllApps()
            .filterNot { it.isEmpty() }
            .map { list ->
                list.map { AppModel(AppId(it.id.value), AppName(it.name)) }
            }

    private fun observeAndRefreshAppsFromDevice(): Flow<List<AppModel>> =
        flow {
            while (coroutineContext.isActive) {
                coroutineScope {
                    val installedAppsFromDeviceDeferred = async { getAllInstalledAppsFromDevice() }
                    val installedAppsFromCacheDeferred = async { dataSource.findAllApps().first() }

                    val installedAppsFromDevice = installedAppsFromDeviceDeferred.await()
                    val installedAppsFromCache = installedAppsFromCacheDeferred.await()

                    emit(installedAppsFromDevice)
                    val databaseApps =
                        installedAppsFromDevice.map { app -> App(DatabaseAppId(app.id.value), app.name.value) }
                    dataSource.insert(databaseApps)

                    val appsToRemove = installedAppsFromCache.filterNot { app ->
                        app.id.value in installedAppsFromDevice.map { it.id.value }
                    }
                    dataSource.delete(appsToRemove)

                    delay(RefreshDelay)
                }
            }
        }

    @SuppressLint("QueryPermissionsNeeded")
    private suspend fun getAllInstalledAppsFromDevice(): List<AppModel> =
        withContext(ioDispatcher) {
            packageManager.queryIntentActivities(buildLauncherCategoryIntent(), PackageManager.GET_META_DATA)
                .map(::toAppModel)
                .sortedBy { it.name.value.uppercase() }
        }


    @SuppressLint("QueryPermissionsNeeded")
    private suspend fun getIconPacksFromDevice(): List<AppModel> =
        withContext(ioDispatcher) {
            packageManager.queryIntentActivities(Intent(IconPackThemesId), PackageManager.GET_META_DATA)
                .map(::toAppModel)
                .sortedBy { it.name.value.uppercase() }
        }

    private fun buildLauncherCategoryIntent() =
        Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)

    private fun toAppModel(resolveInfo: ResolveInfo) = AppModel(
        AppId(resolveInfo.activityInfo.packageName),
        AppName(resolveInfo.loadLabel(packageManager).toString())
    )

    companion object {

        private const val IconPackThemesId = "com.novalauncher.THEME"
        private val RefreshDelay = 1.toDuration(DurationUnit.MINUTES)
    }
}
