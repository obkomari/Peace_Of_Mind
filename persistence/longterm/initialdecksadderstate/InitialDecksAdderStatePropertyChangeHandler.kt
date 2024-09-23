package com.chakulafasta.pom.persistence.longterm.initialdecksadderstate

import com.chakulafasta.pom.Database
import com.chakulafasta.pom.domain.architecturecomponents.PropertyChangeRegistry.Change
import com.chakulafasta.pom.domain.architecturecomponents.PropertyChangeRegistry.Change.PropertyValueChange
import com.chakulafasta.pom.persistence.DbKeys
import com.chakulafasta.pom.persistence.longterm.PropertyChangeHandler
import com.chakulafasta.pom.presentation.common.mainactivity.InitialDecksAdder

class InitialDecksAdderStatePropertyChangeHandler(
    database: Database
) : PropertyChangeHandler {
    private val queries = database.keyValueQueries

    override fun handle(change: Change) {
        if (change !is PropertyValueChange) return
        when (change.property) {
            InitialDecksAdder.State::areInitialDecksAdded -> {
                val areInitialDecksAdded = change.newValue as Boolean
                queries.replace(
                    key = DbKeys.ARE_INITIAL_DECKS_ADDED,
                    value = areInitialDecksAdded.toString()
                )
            }
        }
    }
}