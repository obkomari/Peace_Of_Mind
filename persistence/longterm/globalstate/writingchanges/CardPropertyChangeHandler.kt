package com.chakulafasta.pom.persistence.longterm.globalstate.writingchanges

import com.chakulafasta.pom.Database
import com.chakulafasta.pom.domain.architecturecomponents.PropertyChangeRegistry
import com.chakulafasta.pom.domain.architecturecomponents.PropertyChangeRegistry.Change.PropertyValueChange
import com.chakulafasta.pom.domain.entity.Card
import com.chakulafasta.pom.persistence.longterm.PropertyChangeHandler
import com.soywiz.klock.DateTime

class CardPropertyChangeHandler(
    database: Database
) : PropertyChangeHandler {
    private val queries = database.cardQueries

    override fun handle(change: PropertyChangeRegistry.Change) {
        if (change !is PropertyValueChange) return
        val cardId: Long = change.propertyOwnerId
        val exists: Boolean = queries.exists(cardId).executeAsOne()
        if (!exists) return
        when (change.property) {
            Card::question -> {
                val question = change.newValue as String
                queries.updateQuestion(question, cardId)
            }
            Card::answer -> {
                val answer = change.newValue as String
                queries.updateAnswer(answer, cardId)
            }
            Card::lap -> {
                val lap = change.newValue as Int
                queries.updateLap(lap, cardId)
            }
            Card::isLearned -> {
                val isLearned = change.newValue as Boolean
                queries.updateIsLearned(isLearned, cardId)
            }
            Card::grade -> {
                val grade = change.newValue as Int
                queries.updateGrade(grade, cardId)
            }
            Card::lastTestedAt -> {
                val lastTestedAt = change.newValue as DateTime?
                queries.updateLastTestedAt(lastTestedAt, cardId)
            }
        }
    }
}