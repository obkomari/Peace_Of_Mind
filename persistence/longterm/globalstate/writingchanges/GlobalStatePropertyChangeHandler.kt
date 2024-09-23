package com.chakulafasta.pom.persistence.longterm.globalstate.writingchanges

import com.chakulafasta.pom.Database
import com.chakulafasta.pom.domain.architecturecomponents.PropertyChangeRegistry
import com.chakulafasta.pom.domain.architecturecomponents.PropertyChangeRegistry.Change.CollectionChange
import com.chakulafasta.pom.domain.architecturecomponents.PropertyChangeRegistry.Change.PropertyValueChange
import com.chakulafasta.pom.domain.entity.*
import com.chakulafasta.pom.persistence.DbKeys
import com.chakulafasta.pom.persistence.globalstate.DeckListDb
import com.chakulafasta.pom.persistence.longterm.PropertyChangeHandler
import com.chakulafasta.pom.persistence.toCardDb
import com.chakulafasta.pom.persistence.toDeckDb
import com.chakulafasta.pom.persistence.toDeckListDb

class GlobalStatePropertyChangeHandler(
    private val database: Database,
    private val deckPropertyChangeHandler: DeckPropertyChangeHandler
) : PropertyChangeHandler {
    override fun handle(change: PropertyChangeRegistry.Change) {
        when (change.property) {
            GlobalState::decks -> {
                if (change !is CollectionChange) return

                val removedDecks = change.removedItems as Collection<Deck>
                removedDecks.forEach { deck: Deck ->
                    database.deckQueries.delete(deck.id)
                }

                val addedDecks = change.addedItems as Collection<Deck>
                addedDecks.forEach { deck ->
                    val deckDb = deck.toDeckDb()
                    database.deckQueries.insert(deckDb)
                    deck.cards.mapIndexed { index, card -> card.toCardDb(deck.id, ordinal = index) }
                        .forEach(database.cardQueries::insert)
                    deckPropertyChangeHandler.insertExercisePreferenceIfNotExists(
                        deck.exercisePreference
                    )
                }
            }
            GlobalState::deckLists -> {
                if (change !is CollectionChange) return

                val removedDeckLists = change.removedItems as Collection<DeckList>
                removedDeckLists.forEach { deckList: DeckList ->
                    database.deckListQueries.delete(deckList.id)
                }

                val addedDeckLists = change.addedItems as Collection<DeckList>
                addedDeckLists.forEach { deckList: DeckList ->
                    val deckListDb: DeckListDb = deckList.toDeckListDb()
                    database.deckListQueries.insert(deckListDb)
                }
            }
            GlobalState::sharedExercisePreferences -> {
                if (change !is CollectionChange) return

                val removedSharedExercisePreferences =
                    change.removedItems as Collection<ExercisePreference>
                removedSharedExercisePreferences.forEach { exercisePreference: ExercisePreference ->
                    database.sharedExercisePreferenceQueries.delete(exercisePreference.id)
                }

                val addedSharedExercisePreferences =
                    change.addedItems as Collection<ExercisePreference>
                addedSharedExercisePreferences.forEach { exercisePreference: ExercisePreference ->
                    database.sharedExercisePreferenceQueries.insert(exercisePreference.id)
                    deckPropertyChangeHandler.insertExercisePreferenceIfNotExists(exercisePreference)
                }
            }
            GlobalState::isWalkingModeEnabled -> {
                if (change !is PropertyValueChange) return
                val isWalkingModeEnabled = change.newValue as Boolean
                database.keyValueQueries.replace(
                    key = DbKeys.IS_WALKING_MODE_ENABLED,
                    value = isWalkingModeEnabled.toString()
                )
            }
            GlobalState::numberOfLapsInPlayer -> {
                if (change !is PropertyValueChange) return
                val numberOfLapsInPlayer = change.newValue as Int
                database.keyValueQueries.replace(
                    key = DbKeys.NUMBER_OF_LAPS_IN_PLAYER,
                    value = numberOfLapsInPlayer.toString()
                )
            }
        }
    }
}