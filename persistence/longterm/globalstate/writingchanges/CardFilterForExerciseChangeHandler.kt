package com.chakulafasta.pom.persistence.longterm.globalstate.writingchanges

import com.chakulafasta.pom.Database
import com.chakulafasta.pom.domain.architecturecomponents.PropertyChangeRegistry
import com.chakulafasta.pom.domain.architecturecomponents.PropertyChangeRegistry.Change.PropertyValueChange
import com.chakulafasta.pom.domain.interactor.exercise.CardFilterForExercise
import com.chakulafasta.pom.persistence.DbKeys
import com.chakulafasta.pom.persistence.dateTimeSpanAdapter
import com.chakulafasta.pom.persistence.longterm.PropertyChangeHandler
import com.soywiz.klock.DateTimeSpan

class CardFilterForExerciseChangeHandler(
    database: Database
) : PropertyChangeHandler {
    private val queries = database.keyValueQueries

    override fun handle(change: PropertyChangeRegistry.Change) {
        if (change !is PropertyValueChange) return
        when (change.property) {
            CardFilterForExercise::limit -> {
                val limit = change.newValue as Int
                queries.replace(
                    key = DbKeys.CARD_FILTER_FOR_EXERCISE_LIMIT,
                    value = limit.toString()
                )
            }
            CardFilterForExercise::gradeRange -> {
                val gradeRange = change.newValue as IntRange
                queries.replace(
                    key = DbKeys.CARD_FILTER_FOR_EXERCISE_GRADE_MIN,
                    value = gradeRange.first.toString()
                )
                queries.replace(
                    key = DbKeys.CARD_FILTER_FOR_EXERCISE_GRADE_MAX,
                    value = gradeRange.last.toString()
                )
            }
            CardFilterForExercise::lastTestedFromTimeAgo -> {
                val lastTestedFromTimeAgo = change.newValue as DateTimeSpan?
                queries.replace(
                    key = DbKeys.CARD_FILTER_FOR_EXERCISE_LAST_TESTED_FROM_TIME_AGO,
                    value = lastTestedFromTimeAgo?.let(dateTimeSpanAdapter::encode)
                )
            }
            CardFilterForExercise::lastTestedToTimeAgo -> {
                val lastAnswerToTimeAgo = change.newValue as DateTimeSpan?
                queries.replace(
                    key = DbKeys.CARD_FILTER_FOR_EXERCISE_LAST_TESTED_TO_TIME_AGO,
                    value = lastAnswerToTimeAgo?.let(dateTimeSpanAdapter::encode)
                )
            }
        }
    }
}