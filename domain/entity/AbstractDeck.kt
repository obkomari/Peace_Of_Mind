package com.chakulafasta.pom.domain.entity

import com.chakulafasta.pom.domain.architecturecomponents.FlowMaker

interface AbstractDeck

class NewDeck(
    deckName: String
) : FlowMaker<NewDeck>(), AbstractDeck {
    var deckName: String by flowMaker(deckName)
}

class ExistingDeck(val deck: Deck) : AbstractDeck

const val ERROR_MESSAGE_UNKNOWN_IMPLEMENTATION_OF_ABSTRACT_DECK =
    "Unknown implementation of AbstractDeck"

val AbstractDeck.name: String
    get() = when (this) {
        is NewDeck -> deckName
        is ExistingDeck -> deck.name
        else -> error(ERROR_MESSAGE_UNKNOWN_IMPLEMENTATION_OF_ABSTRACT_DECK)
    }