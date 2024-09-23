package com.chakulafasta.pom.persistence.shortterm

import com.chakulafasta.pom.Database
import com.chakulafasta.pom.domain.interactor.autoplay.CardFilterForAutoplay
import com.chakulafasta.pom.domain.entity.Deck
import com.chakulafasta.pom.domain.entity.GlobalState
import com.chakulafasta.pom.domain.interactor.autoplay.PlayerStateCreator
import com.chakulafasta.pom.persistence.shortterm.PlayerCreatorStateProvider.SerializableState
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class PlayerCreatorStateProvider(
    json: Json,
    database: Database,
    private val globalState: GlobalState,
    override val key: String = PlayerStateCreator.State::class.qualifiedName!!
) : BaseSerializableStateProvider<PlayerStateCreator.State, SerializableState>(
    json,
    database
) {
    @Serializable
    data class SerializableState(
        val deckIds: List<Long>,
        val cardFilterId: Long
    )

    override val serializer = SerializableState.serializer()

    override fun toSerializable(state: PlayerStateCreator.State) = SerializableState(
        deckIds = state.decks.map { it.id },
        cardFilterId = state.cardFilter.id
    )

    override fun toOriginal(serializableState: SerializableState): PlayerStateCreator.State {
        val cardFilter: CardFilterForAutoplay =
            if (serializableState.cardFilterId == CardFilterForAutoplay.IncludeAll.id) {
                CardFilterForAutoplay.IncludeAll
            } else {
                globalState.cardFilterForAutoplay
            }
        val decks: List<Deck> = globalState.decks.filter { it.id in serializableState.deckIds }
        return PlayerStateCreator.State(decks, cardFilter)
    }
}