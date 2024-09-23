package com.chakulafasta.pom.persistence.longterm.globalstate.provision

import com.chakulafasta.pom.Database
import com.chakulafasta.pom.domain.entity.GlobalState
import com.chakulafasta.pom.presentation.common.LongTermStateProvider

class GlobalStateProvider(private val database: Database) : LongTermStateProvider<GlobalState> {
    override fun load(): GlobalState {
        lateinit var tables: TablesForGlobalState
        database.transaction {
            tables = TablesForGlobalState.load(database)
        }
        return GlobalStateBuilder.build(tables)
    }
}