package com.chakulafasta.pom.persistence.shortterm

import com.chakulafasta.pom.Database
import com.chakulafasta.pom.persistence.shortterm.CardsThresholdDialogStateProvider.SerializableState
import com.chakulafasta.pom.presentation.screen.exercisesettings.CardsThresholdDialogState
import com.chakulafasta.pom.presentation.screen.exercisesettings.CardsThresholdDialogState.Purpose
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class CardsThresholdDialogStateProvider(
    json: Json,
    database: Database,
    override val key: String = CardsThresholdDialogState::class.qualifiedName!!
) : BaseSerializableStateProvider<CardsThresholdDialogState, SerializableState>(
    json,
    database
) {
    @Serializable
    data class SerializableState(
        val text: String,
        val purpose: Purpose? = null
    )

    override val serializer = SerializableState.serializer()

    override fun toSerializable(state: CardsThresholdDialogState): SerializableState {
        return SerializableState(
            state.text,
            state.purpose
        )
    }

    override fun toOriginal(serializableState: SerializableState): CardsThresholdDialogState {
        return CardsThresholdDialogState(
            serializableState.text,
            serializableState.purpose
        )
    }
}