package com.chakulafasta.pom.persistence.shortterm

import com.chakulafasta.pom.Database
import com.chakulafasta.pom.persistence.shortterm.MotivationalTimerScreenStateProvider.SerializableState
import com.chakulafasta.pom.presentation.screen.deckeditor.decksettings.Tip
import com.chakulafasta.pom.presentation.screen.motivationaltimer.MotivationalTimerScreenState
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class MotivationalTimerScreenStateProvider(
    json: Json,
    database: Database,
    override val key: String = MotivationalTimerScreenState::class.qualifiedName!!
) : BaseSerializableStateProvider<MotivationalTimerScreenState, SerializableState>(
    json,
    database
) {
    @Serializable
    data class SerializableState(
        val tipId: Long?,
        val isTimerEnabled: Boolean,
        val timeInput: String
    )

    override val serializer = SerializableState.serializer()

    override fun toSerializable(state: MotivationalTimerScreenState) = SerializableState(
        state.tip?.state?.id,
        state.isTimerEnabled,
        state.timeInput
    )

    override fun toOriginal(serializableState: SerializableState): MotivationalTimerScreenState {
        val tip = Tip.values().find { it.state.id == serializableState.tipId }
        return MotivationalTimerScreenState(
            tip,
            serializableState.isTimerEnabled,
            serializableState.timeInput
        )
    }
}