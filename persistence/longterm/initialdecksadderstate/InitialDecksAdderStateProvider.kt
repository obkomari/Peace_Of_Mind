package com.chakulafasta.pom.persistence.longterm.initialdecksadderstate

import com.chakulafasta.pom.Database
import com.chakulafasta.pom.persistence.DbKeys
import com.chakulafasta.pom.presentation.common.LongTermStateProvider
import com.chakulafasta.pom.presentation.common.mainactivity.InitialDecksAdder

class InitialDecksAdderStateProvider(
    private val database: Database
) : LongTermStateProvider<InitialDecksAdder.State> {
    override fun load(): InitialDecksAdder.State {
        return database.keyValueQueries
            .selectValue(DbKeys.ARE_INITIAL_DECKS_ADDED)
            .executeAsOneOrNull()
            ?.value?.toBoolean()?.let(InitialDecksAdder::State)
            ?: InitialDecksAdder.State()
    }
}