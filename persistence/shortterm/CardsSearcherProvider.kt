package com.chakulafasta.pom.persistence.shortterm

import com.chakulafasta.pom.Database
import com.chakulafasta.pom.domain.entity.Deck
import com.chakulafasta.pom.domain.entity.GlobalState
import com.chakulafasta.pom.domain.interactor.searcher.CardsSearcher
import com.chakulafasta.pom.domain.interactor.searcher.CardsSearcher.SearchArea
import com.chakulafasta.pom.persistence.shortterm.CardsSearcherProvider.SerializableState
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class CardsSearcherProvider(
    private val globalState: GlobalState,
    json: Json,
    database: Database,
    override val key: String = CardsSearcher::class.qualifiedName!!
) : BaseSerializableStateProvider<CardsSearcher, SerializableState>(
    json,
    database
) {
    @Serializable
    data class SerializableState(
        val deckId: Long?
    )

    override val serializer = SerializableState.serializer()

    override fun toSerializable(state: CardsSearcher): SerializableState {
        val searchArea = state.state.searchArea
        val deckId: Long? =
            if (searchArea is SearchArea.SpecificDeck)
                searchArea.deck.id
            else
                null
        return SerializableState(deckId)
    }

    override fun toOriginal(serializableState: SerializableState): CardsSearcher {
        val deckId = serializableState.deckId
        return if (deckId != null) {
            val deck = globalState.decks.first { deck: Deck -> deck.id == deckId }
            CardsSearcher(deck)
        } else {
            CardsSearcher(globalState)
        }
    }
}