package com.chakulafasta.pom.domain.interactor.cardsimport

import com.chakulafasta.pom.domain.entity.Card
import kotlinx.serialization.Serializable

@Serializable
data class CardPrototype(
    val id: Long,
    val question: String,
    val answer: String,
    val isSelected: Boolean
) {
    fun toCard() = Card(
        id,
        question,
        answer
    )
}