package com.chakulafasta.pom.domain.interactor.autoplay

import com.chakulafasta.pom.domain.architecturecomponents.FlowMaker
import com.chakulafasta.pom.domain.entity.Card
import com.chakulafasta.pom.domain.entity.Deck

class PlayingCard(
    id: Long,
    card: Card,
    deck: Deck,
    isQuestionDisplayed: Boolean,
    isInverted: Boolean,
    isAnswerDisplayed: Boolean = false
) : FlowMaker<PlayingCard>() {
    val id: Long by flowMaker(id)
    val card: Card by flowMaker(card)
    var deck: Deck by flowMaker(deck)
    var isQuestionDisplayed: Boolean by flowMaker(isQuestionDisplayed)
    var isInverted: Boolean by flowMaker(isInverted)
    var isAnswerDisplayed: Boolean by flowMaker(isAnswerDisplayed)
}