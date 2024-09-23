package com.chakulafasta.pom.domain.interactor.exercise

import com.chakulafasta.pom.domain.architecturecomponents.FlowMaker
import com.chakulafasta.pom.domain.entity.Card
import com.chakulafasta.pom.domain.entity.Deck

interface ExerciseCard {
    val base: Base

    class Base(
        id: Long,
        card: Card,
        deck: Deck,
        isInverted: Boolean,
        isQuestionDisplayed: Boolean,
        isAnswerCorrect: Boolean? = null,
        hint: String? = null,
        timeLeft: Int,
        isExpired: Boolean = false,
        initialGrade: Int,
        isGradeEditedManually: Boolean
    ) : FlowMaker<Base>() {
        val id: Long by flowMaker(id)
        val card: Card by flowMaker(card)
        var deck: Deck by flowMaker(deck)
        var isInverted: Boolean by flowMaker(isInverted)
        var isQuestionDisplayed: Boolean by flowMaker(isQuestionDisplayed)
        var isAnswerCorrect: Boolean? by flowMaker(isAnswerCorrect)
        var hint: String? by flowMaker(hint)
        var timeLeft: Int by flowMaker(timeLeft)
        var isExpired: Boolean by flowMaker(isExpired)
        val initialGrade: Int by flowMaker(initialGrade)
        var isGradeEditedManually: Boolean by flowMaker(isGradeEditedManually)
    }
}

val ExerciseCard.isAnswered get() = base.isAnswerCorrect != null