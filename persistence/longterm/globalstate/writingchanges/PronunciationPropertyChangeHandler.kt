package com.chakulafasta.pom.persistence.longterm.globalstate.writingchanges

import com.chakulafasta.pom.Database
import com.chakulafasta.pom.domain.architecturecomponents.PropertyChangeRegistry
import com.chakulafasta.pom.domain.architecturecomponents.PropertyChangeRegistry.Change.PropertyValueChange
import com.chakulafasta.pom.domain.entity.Pronunciation
import com.chakulafasta.pom.persistence.longterm.PropertyChangeHandler
import java.util.*

class PronunciationPropertyChangeHandler(
    database: Database
) : PropertyChangeHandler {
    private val queries = database.pronunciationQueries

    override fun handle(change: PropertyChangeRegistry.Change) {
        if (change !is PropertyValueChange) return
        val pronunciationId: Long = change.propertyOwnerId
        val exists: Boolean = queries.exists(pronunciationId).executeAsOne()
        if (!exists) return
        when (change.property) {
            Pronunciation::questionLanguage -> {
                val questionLanguage = change.newValue as Locale?
                queries.updateQuestionLanguage(questionLanguage, pronunciationId)
            }
            Pronunciation::questionAutoSpeaking -> {
                val questionAutoSpeak = change.newValue as Boolean
                queries.updateQuestionAutoSpeaking(questionAutoSpeak, pronunciationId)
            }
            Pronunciation::answerLanguage -> {
                val answerLanguage = change.newValue as Locale?
                queries.updateAnswerLanguage(answerLanguage, pronunciationId)
            }
            Pronunciation::answerAutoSpeaking -> {
                val answerAutoSpeak = change.newValue as Boolean
                queries.updateAnswerAutoSpeaking(answerAutoSpeak, pronunciationId)
            }
            Pronunciation::speakTextInBrackets -> {
                val speakTextInBrackets = change.newValue as Boolean
                queries.updateSpeakTextInBrackets(speakTextInBrackets, pronunciationId)
            }
        }
    }
}