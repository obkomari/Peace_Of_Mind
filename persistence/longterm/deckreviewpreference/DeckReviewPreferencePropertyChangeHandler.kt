package com.chakulafasta.pom.persistence.longterm.deckreviewpreference

import com.chakulafasta.pom.Database
import com.chakulafasta.pom.domain.architecturecomponents.PropertyChangeRegistry
import com.chakulafasta.pom.domain.architecturecomponents.PropertyChangeRegistry.Change.PropertyValueChange
import com.chakulafasta.pom.domain.entity.DeckList
import com.chakulafasta.pom.persistence.longterm.PropertyChangeHandler
import com.chakulafasta.pom.presentation.screen.home.DeckReviewPreference
import com.chakulafasta.pom.presentation.screen.home.DeckSorting

class DeckReviewPreferencePropertyChangeHandler(
    database: Database
) : PropertyChangeHandler {
    private val queries = database.deckReviewPreferenceQueries

    override fun handle(change: PropertyChangeRegistry.Change) {
        if (change !is PropertyValueChange) return
        val deckReviewPreferenceId = change.propertyOwnerId
        when (change.property) {
            DeckReviewPreference::deckList -> {
                val deckList = change.newValue as DeckList?
                queries.updateDeckListId(deckList?.id, deckReviewPreferenceId)
            }
            DeckReviewPreference::deckSorting -> {
                val deckSorting = change.newValue as DeckSorting
                queries.updateDeckSorting(
                    deckSorting.criterion,
                    deckSorting.direction,
                    deckSorting.newDecksFirst,
                    deckReviewPreferenceId
                )
            }
            DeckReviewPreference::displayOnlyDecksAvailableForExercise -> {
                val displayOnlyDecksAvailableForExercise = change.newValue as Boolean
                queries.updateDisplayOnlyDecksAvailableForExercise(
                    displayOnlyDecksAvailableForExercise,
                    deckReviewPreferenceId
                )
            }
        }
    }
}