package com.chakulafasta.pom.persistence.shortterm

import com.chakulafasta.pom.Database
import com.chakulafasta.pom.persistence.shortterm.CardLimitDialogStateProvider.SerializableState
import com.chakulafasta.pom.presentation.screen.cardfilterforexercise.cardlimit.CardLimitDialogState
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class CardLimitDialogStateProvider(
    json: Json,
    database: Database,
    override val key: String = CardLimitDialogState::class.qualifiedName!!
) : BaseSerializableStateProvider<CardLimitDialogState, SerializableState>(
    json,
    database
) {
    @Serializable
    data class SerializableState(
        val isNoLimit: Boolean,
        val dialogText: String
    )

    override val serializer = SerializableState.serializer()

    override fun toSerializable(state: CardLimitDialogState): SerializableState {
        return SerializableState(
            state.isNoLimit,
            state.dialogText
        )
    }

    override fun toOriginal(serializableState: SerializableState): CardLimitDialogState {
        return CardLimitDialogState(
            serializableState.isNoLimit,
            serializableState.dialogText
        )
    }
}