package com.chakulafasta.pom.domain.interactor.decklistseditor

import com.chakulafasta.pom.domain.architecturecomponents.FlowMaker
import com.chakulafasta.pom.domain.entity.DeckList

class EditableDeckList(
    val deckList: DeckList,
    name: String = deckList.name,
    color: Int = deckList.color
) : FlowMaker<EditableDeckList>() {
    var name: String by flowMaker(name)
    var color: Int by flowMaker(color)
}