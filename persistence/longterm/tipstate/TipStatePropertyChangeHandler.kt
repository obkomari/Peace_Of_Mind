package com.chakulafasta.pom.persistence.longterm.tipstate

import com.chakulafasta.pom.Database
import com.chakulafasta.pom.domain.architecturecomponents.PropertyChangeRegistry.Change
import com.chakulafasta.pom.domain.architecturecomponents.PropertyChangeRegistry.Change.PropertyValueChange
import com.chakulafasta.pom.persistence.longterm.PropertyChangeHandler
import com.chakulafasta.pom.presentation.screen.deckeditor.decksettings.TipState
import com.soywiz.klock.DateTime

class TipStatePropertyChangeHandler(
    database: Database
) : PropertyChangeHandler {
    private val queries = database.tipStateQueries

    override fun handle(change: Change) {
        if (change !is PropertyValueChange) return
        when (change.property) {
            TipState::needToShow -> {
                val needToShow = change.newValue as Boolean
                queries.upsert(
                    id = change.propertyOwnerId,
                    needToShow = needToShow,
                    lastShowedAt = TipState.DEFAULT_LAST_SHOWED_AT
                )
            }
            TipState::lastShowedAt -> {
                val lastShowedAt = change.newValue as DateTime?
                queries.upsert(
                    id = change.propertyOwnerId,
                    needToShow = TipState.DEFAULT_NEED_TO_SHOW,
                    lastShowedAt = lastShowedAt
                )
            }
        }
    }
}