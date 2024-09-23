package com.chakulafasta.pom.domain.entity

import com.chakulafasta.pom.domain.architecturecomponents.FlowMakerWithRegistry

class DeckList(
    override val id: Long,
    name: String,
    color: Int,
    deckIds: Set<Long>
) : FlowMakerWithRegistry<DeckList>() {
    var name: String by flowMaker(name)
    var color: Int by flowMaker(color)
    var deckIds: Set<Long> by flowMaker(deckIds)

    override fun copy() = DeckList(id, name, color, deckIds)
}