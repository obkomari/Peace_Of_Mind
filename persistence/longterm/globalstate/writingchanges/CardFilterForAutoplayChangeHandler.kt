package com.chakulafasta.pom.persistence.longterm.globalstate.writingchanges

import com.chakulafasta.pom.Database
import com.chakulafasta.pom.domain.architecturecomponents.PropertyChangeRegistry
import com.chakulafasta.pom.domain.architecturecomponents.PropertyChangeRegistry.Change.PropertyValueChange
import com.chakulafasta.pom.domain.interactor.autoplay.CardFilterForAutoplay
import com.chakulafasta.pom.persistence.DbKeys
import com.chakulafasta.pom.persistence.dateTimeSpanAdapter
import com.chakulafasta.pom.persistence.longterm.PropertyChangeHandler
import com.soywiz.klock.DateTimeSpan

class CardFilterForAutoplayChangeHandler(
    database: Database
) : PropertyChangeHandler {
    private val queries = database.keyValueQueries

    override fun handle(change: PropertyChangeRegistry.Change) {
        if (change !is PropertyValueChange) return
        when (change.property) {
            CardFilterForAutoplay::areCardsAvailableForExerciseIncluded -> {
                val areCardsAvailableForExerciseIncluded = change.newValue as Boolean
                queries.replace(
                    key = DbKeys.CARD_FILTER_FOR_AUTOPLAY_ARE_CARDS_AVAILABLE_FOR_EXERCISE_INCLUDED,
                    value = areCardsAvailableForExerciseIncluded.toString()
                )
            }
            CardFilterForAutoplay::areAwaitingCardsIncluded -> {
                val areAwaitingCardsIncluded = change.newValue as Boolean
                queries.replace(
                    key = DbKeys.CARD_FILTER_FOR_AUTOPLAY_ARE_AWAITING_CARDS_INCLUDED,
                    value = areAwaitingCardsIncluded.toString()
                )
            }
            CardFilterForAutoplay::areLearnedCardsIncluded -> {
                val areLearnedCardsIncluded = change.newValue as Boolean
                queries.replace(
                    key = DbKeys.CARD_FILTER_FOR_AUTOPLAY_ARE_LEARNED_CARDS_INCLUDED,
                    value = areLearnedCardsIncluded.toString()
                )
            }
            CardFilterForAutoplay::gradeRange -> {
                val gradeRange = change.newValue as IntRange
                queries.replace(
                    key = DbKeys.CARD_FILTER_FOR_AUTOPLAY_GRADE_MIN,
                    value = gradeRange.first.toString()
                )
                queries.replace(
                    key = DbKeys.CARD_FILTER_FOR_AUTOPLAY_GRADE_MAX,
                    value = gradeRange.last.toString()
                )
            }
            CardFilterForAutoplay::lastTestedFromTimeAgo -> {
                val lastTestedFromTimeAgo = change.newValue as DateTimeSpan?
                queries.replace(
                    key = DbKeys.CARD_FILTER_FOR_AUTOPLAY_LAST_TESTED_FROM_TIME_AGO,
                    value = lastTestedFromTimeAgo?.let(dateTimeSpanAdapter::encode)
                )
            }
            CardFilterForAutoplay::lastTestedToTimeAgo -> {
                val lastAnswerToTimeAgo = change.newValue as DateTimeSpan?
                queries.replace(
                    key = DbKeys.CARD_FILTER_FOR_AUTOPLAY_LAST_TESTED_TO_TIME_AGO,
                    value = lastAnswerToTimeAgo?.let(dateTimeSpanAdapter::encode)
                )
            }
        }
    }
}