package com.chakulafasta.pom.persistence

import com.chakulafasta.pom.Database
import com.chakulafasta.pom.presentation.common.businessLogicThread
import com.chakulafasta.pom.presentation.common.di.AppDiScope
import com.squareup.sqldelight.db.SqlDriver
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class DbCleaner private constructor(
    private val sqliteDriver: SqlDriver,
    private val database: Database
) {
    private fun cleanupDatabase() {
        database.transaction {
            database.serializableQueries.deleteAll()
            database.exercisePreferenceQueries.deleteUnused()
            database.intervalSchemeQueries.deleteUnused()
            database.pronunciationQueries.deleteUnused()
            database.pronunciationPlanQueries.deleteUnused()
        }
        sqliteDriver.executeQuery(null, "VACUUM", 0)
    }

    companion object {
        fun cleanupDatabase() {
            GlobalScope.launch(businessLogicThread) {
                val dbCleaner = with(AppDiScope.get()) {
                    DbCleaner(sqlDriver, database)
                }
                dbCleaner.cleanupDatabase()
            }
        }
    }
}