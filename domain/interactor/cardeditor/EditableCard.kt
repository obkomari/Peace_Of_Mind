package com.chakulafasta.pom.domain.interactor.cardeditor

import com.chakulafasta.pom.domain.architecturecomponents.FlowMaker
import com.chakulafasta.pom.domain.entity.Card
import com.chakulafasta.pom.domain.entity.Deck

class EditableCard(
    val card: Card,
    val deck: Deck,
    question: String = card.question,
    answer: String = card.answer,
    isLearned: Boolean = card.isLearned,
    grade: Int = card.grade
) : FlowMaker<EditableCard>() {
    var question: String by flowMaker(question)
    var answer: String by flowMaker(answer)
    var isLearned: Boolean by flowMaker(isLearned)
    var grade: Int by flowMaker(grade)

    fun isFullyBlank(): Boolean = question.isBlank() && answer.isBlank()
    fun hasBlankField(): Boolean = question.isBlank() || answer.isBlank()
    fun isHalfFilled(): Boolean = question.isBlank() xor answer.isBlank()
}