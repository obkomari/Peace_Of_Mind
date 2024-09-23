package com.chakulafasta.pom.persistence.longterm.globalstate.writingchanges

import android.util.Log
import com.chakulafasta.pom.BuildConfig
import com.chakulafasta.pom.Database
import com.chakulafasta.pom.domain.architecturecomponents.PropertyChangeRegistry
import com.chakulafasta.pom.domain.architecturecomponents.PropertyChangeRegistry.Change.ListChange
import com.chakulafasta.pom.domain.architecturecomponents.PropertyChangeRegistry.Change.PropertyValueChange
import com.chakulafasta.pom.domain.entity.Card
import com.chakulafasta.pom.domain.entity.Deck
import com.chakulafasta.pom.domain.entity.ExercisePreference
import com.chakulafasta.pom.domain.entity.isDefault
import com.chakulafasta.pom.persistence.longterm.PropertyChangeHandler
import com.chakulafasta.pom.persistence.toCardDb
import com.chakulafasta.pom.persistence.toExercisePreferenceDb
import com.soywiz.klock.DateTime

class DeckPropertyChangeHandler(
    private val database: Database,
    private val exercisePreferencePropertyChangeHandler: ExercisePreferencePropertyChangeHandler
) : PropertyChangeHandler {
    override fun handle(change: PropertyChangeRegistry.Change) {
        val deckId: Long = change.propertyOwnerId
        val exists: Boolean = database.deckQueries.exists(deckId).executeAsOne()
        if (!exists) return
        when (change.property) {
            Deck::name -> {
                if (change !is PropertyValueChange) return
                val name = change.newValue as String
                database.deckQueries.updateName(name, deckId)
            }
            Deck::lastTestedAt -> {
                if (change !is PropertyValueChange) return
                val lastTestedAt = change.newValue as DateTime?
                database.deckQueries.updateLastTestedAt(lastTestedAt, deckId)
            }
            Deck::cards -> {
                if (change !is ListChange) return
                change.removedItemsAt.forEach { ordinal: Int ->
                    database.cardQueries.delete(deckId, ordinal)
                }
                change.movedItemsAt
                    .mapNotNull { (oldOrdinal: Int, newOrdinal: Int) ->
                        val cardId: Long = try {
                            database.cardQueries
                                .selectIdByDeckIdAndOrdinal(deckId, oldOrdinal)
                                .executeAsOne()
                        } catch (e: Exception) {
                            e.printStackTrace()
                            if (BuildConfig.DEBUG) {
                                Log.w("db", "CANNOT UPDATE ordinal: ${e.message}")
                            }
                            return@mapNotNull null
                        }
                        cardId to newOrdinal
                    }
                    .forEach { (cardId: Long, newOrdinal: Int) ->
                        database.cardQueries.updateOrdinal(newOrdinal, cardId)
                    }
                (change.addedItems as Map<Int, Card>).forEach { (ordinal, card) ->
                    val cardDb = card.toCardDb(deckId, ordinal)
                    database.cardQueries.insert(cardDb)
                }
            }
            Deck::exercisePreference -> {
                if (change !is PropertyValueChange) return
                val linkedExercisePreference = change.newValue as ExercisePreference
                insertExercisePreferenceIfNotExists(linkedExercisePreference)
                database.deckQueries.updateExercisePreferenceId(linkedExercisePreference.id, deckId)
            }
            Deck::isPinned -> {
                if (change !is PropertyValueChange) return
                val isPinned = change.newValue as Boolean
                database.deckQueries.updateIsPinned(isPinned, deckId)
            }
        }
    }

    fun insertExercisePreferenceIfNotExists(exercisePreference: ExercisePreference) {
        val exists = exercisePreference.isDefault()
                || database.exercisePreferenceQueries.exists(exercisePreference.id).executeAsOne()
        if (!exists) {
            exercisePreferencePropertyChangeHandler.insertPronunciationIfNotExists(
                exercisePreference.pronunciation
            )
            exercisePreference.intervalScheme
                ?.let(exercisePreferencePropertyChangeHandler::insertIntervalSchemeIfNotExists)
            exercisePreferencePropertyChangeHandler.insertGradingIfNotExists(
                exercisePreference.grading
            )
            exercisePreferencePropertyChangeHandler.insertPronunciationPlanIfNotExists(
                exercisePreference.pronunciationPlan
            )
            val exercisePreferenceDb = exercisePreference.toExercisePreferenceDb()
            database.exercisePreferenceQueries.insert(exercisePreferenceDb)
        }
    }
}