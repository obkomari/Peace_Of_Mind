package com.chakulafasta.pom.persistence.longterm.globalstate.writingchanges

import com.chakulafasta.pom.Database
import com.chakulafasta.pom.domain.architecturecomponents.PropertyChangeRegistry
import com.chakulafasta.pom.domain.architecturecomponents.PropertyChangeRegistry.Change.PropertyValueChange
import com.chakulafasta.pom.domain.entity.DeckList
import com.chakulafasta.pom.persistence.longterm.PropertyChangeHandler

class DeckListPropertyChangeHandler(
    database: Database
): PropertyChangeHandler {
    private val queries = database.deckListQueries

    override fun handle(change: PropertyChangeRegistry.Change) {
        if (change !is PropertyValueChange) return
        val deckListId: Long = change.propertyOwnerId
        val exists: Boolean = queries.exists(deckListId).executeAsOne()
        if (!exists) return
        when (change.property) {
            DeckList::name -> {
                val name = change.newValue as String
                queries.updateName(name, deckListId)
            }
            DeckList::color -> {
                val color = change.newValue as Int
                queries.updateColor(color, deckListId)
            }
            DeckList::deckIds -> {
                val deckIds = change.newValue as Set<Long>
                queries.updateDeckIds(deckIds, deckListId)
            }
        }
    }
}