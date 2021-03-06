package shuttle.database.datasource

import arrow.core.Option
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import shuttle.database.Stat
import shuttle.database.StatQueries
import shuttle.database.model.DatabaseAppId
import shuttle.database.model.DatabaseDate
import shuttle.database.model.DatabaseGeoHash
import shuttle.database.model.DatabaseTime
import shuttle.database.util.suspendTransaction

interface StatDataSource {

    suspend fun clearAllStatsOlderThan(date: DatabaseDate)

    suspend fun deleteAllCountersFor(appId: DatabaseAppId)

    fun findAllStats(
        geoHash: Option<DatabaseGeoHash>,
        startTime: DatabaseTime,
        endTime: DatabaseTime
    ): Flow<List<Stat>>

    suspend fun insertOpenStats(
        appId: DatabaseAppId,
        date: DatabaseDate,
        geoHash: Option<DatabaseGeoHash>,
        time: DatabaseTime
    )
}

internal class StatDataSourceImpl(
    private val statQueries: StatQueries,
    private val ioDispatcher: CoroutineDispatcher
): StatDataSource {

    override suspend fun clearAllStatsOlderThan(date: DatabaseDate) {
        statQueries.clearAllStatsOlderThan(date)
    }

    override suspend fun deleteAllCountersFor(appId: DatabaseAppId) {
        statQueries.suspendTransaction(ioDispatcher) {
            deleteStatsForApp(appId)
        }
    }

    override fun findAllStats(
        geoHash: Option<DatabaseGeoHash>,
        startTime: DatabaseTime,
        endTime: DatabaseTime
    ): Flow<List<Stat>> {
        val geoHashValue = geoHash.orNull()
        val query =
            if (geoHashValue == null) {
                statQueries.findAllStats(
                    startTime = startTime,
                    endTime = endTime
                )
            }else {
                statQueries.findAllStatsByGeoHash(
                    geoHash = geoHashValue,
                    startTime = startTime,
                    endTime = endTime
                )
            }
        return query.asFlow().mapToList(ioDispatcher)
    }

    override suspend fun insertOpenStats(
        appId: DatabaseAppId,
        date: DatabaseDate,
        geoHash: Option<DatabaseGeoHash>,
        time: DatabaseTime
    ) {
        statQueries.insertStat(
            appId = appId,
            date = date,
            geoHash = geoHash.orNull(),
            time = time
        )
    }
}
