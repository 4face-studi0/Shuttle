package shuttle.apps.domain

import arrow.core.Either
import kotlinx.coroutines.flow.Flow
import shuttle.apps.domain.error.GenericError
import shuttle.apps.domain.model.AppModel

interface AppsRepository {

    /**
     * Observe all the installed apps
     */
    fun observeAllInstalledApps(): Flow<Either<GenericError, List<AppModel>>>

    /**
     * Observe all the installed apps that are not blacklisted
     */
    fun observeNotBlacklistedApps(): Flow<Either<GenericError, List<AppModel>>>
}
