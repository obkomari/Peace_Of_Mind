package com.chakulafasta.pom.persistence.shortterm

import com.chakulafasta.pom.Database
import com.chakulafasta.pom.persistence.shortterm.CardInversionScreenStateProvider.SerializableState
import com.chakulafasta.pom.presentation.screen.cardinversion.CardInversionScreenState
import com.chakulafasta.pom.presentation.screen.deckeditor.decksettings.Tip
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class CardInversionScreenStateProvider(
    json: Json,
    database: Database,
    override val key: String = CardInversionScreenState::class.qualifiedName!!
) : BaseSerializableStateProvider<CardInversionScreenState, SerializableState>(
    json,
    database
) {
    @Serializable
    data class SerializableState(
        val tipId: Long?
    )

    override val serializer = SerializableState.serializer()

    override fun toSerializable(state: CardInversionScreenState) = SerializableState(
        state.tip?.state?.id
    )

    override fun toOriginal(serializableState: SerializableState): CardInversionScreenState {
        val tip = Tip.values().find { it.state.id == serializableState.tipId }
        return CardInversionScreenState(tip)
    }
}