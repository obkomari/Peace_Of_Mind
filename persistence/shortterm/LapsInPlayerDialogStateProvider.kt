package com.chakulafasta.pom.persistence.shortterm

import com.chakulafasta.pom.Database
import com.chakulafasta.pom.persistence.shortterm.LapsInPlayerDialogStateProvider.SerializableState
import com.chakulafasta.pom.presentation.screen.player.view.laps.LapsInPlayerDialogState
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class LapsInPlayerDialogStateProvider(
    json: Json,
    database: Database,
    override val key: String = LapsInPlayerDialogState::class.qualifiedName!!
) : BaseSerializableStateProvider<LapsInPlayerDialogState, SerializableState>(
    json,
    database
) {
    @Serializable
    data class SerializableState(
        val isInfinitely: Boolean,
        val numberOfLapsInput: String
    )

    override val serializer = SerializableState.serializer()

    override fun toSerializable(state: LapsInPlayerDialogState) = SerializableState(
        state.isInfinitely,
        state.numberOfLapsInput
    )

    override fun toOriginal(serializableState: SerializableState) = LapsInPlayerDialogState(
        serializableState.isInfinitely,
        serializableState.numberOfLapsInput
    )
}