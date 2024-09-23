package com.chakulafasta.pom.persistence.shortterm

import com.chakulafasta.pom.Database
import com.chakulafasta.pom.domain.entity.*
import com.chakulafasta.pom.domain.interactor.autoplay.Player
import com.chakulafasta.pom.domain.interactor.autoplay.PlayingCard
import com.chakulafasta.pom.persistence.shortterm.PlayerStateProvider.SerializableState
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class PlayerStateProvider(
    json: Json,
    database: Database,
    private val globalState: GlobalState,
    override val key: String
) : BaseSerializableStateProvider<Player.State, SerializableState>(
    json,
    database
) {
    @Serializable
    data class SerializableState(
        val serializablePlayingCards: List<SerializablePlayingCard>,
        val currentPosition: Int,
        val pronunciationEventPosition: Int,
        val currentLap: Int,
        val isPlaying: Boolean
    )

    @Serializable
    data class SerializablePlayingCard(
        val id: Long,
        val cardId: Long,
        val deckId: Long,
        val isQuestionDisplayed: Boolean,
        val isReverse: Boolean,
        val isAnswerDisplayed: Boolean
    )

    override val serializer = SerializableState.serializer()

    override fun toSerializable(state: Player.State): SerializableState {
        val serializablePlayingCards: List<SerializablePlayingCard> = state.playingCards
            .map { playingCard: PlayingCard ->
                with(playingCard) {
                    SerializablePlayingCard(
                        id,
                        card.id,
                        deck.id,
                        isQuestionDisplayed,
                        isInverted,
                        isAnswerDisplayed
                    )
                }
            }
        return SerializableState(
            serializablePlayingCards,
            state.currentPosition,
            state.pronunciationEventPosition,
            state.currentLap,
            state.isPlaying
        )
    }

    override fun toOriginal(serializableState: SerializableState): Player.State {
        val deckIdDeckMap: Map<Long, Deck> = globalState.decks.associateBy { deck -> deck.id }
        val cardIdCardMap: Map<Long, Card> = globalState.decks
            .flatMap { deck -> deck.cards }
            .associateBy { card -> card.id }
        val playingCards: List<PlayingCard> = serializableState.serializablePlayingCards
            .map { serializablePlayingCard: SerializablePlayingCard ->
                with(serializablePlayingCard) {
                    PlayingCard(
                        id = id,
                        card = cardIdCardMap.getValue(cardId),
                        deck = deckIdDeckMap.getValue(deckId),
                        isQuestionDisplayed = isQuestionDisplayed,
                        isAnswerDisplayed = isAnswerDisplayed,
                        isInverted = isReverse
                    )
                }
            }
        return Player.State(
            playingCards,
            serializableState.currentPosition,
            serializableState.pronunciationEventPosition,
            serializableState.currentLap,
            serializableState.isPlaying
        )
    }
}