package com.chakulafasta.pom.persistence.shortterm

import com.chakulafasta.pom.Database
import com.chakulafasta.pom.persistence.shortterm.IntervalsScreenStateProvider.SerializableState
import com.chakulafasta.pom.presentation.screen.deckeditor.decksettings.Tip
import com.chakulafasta.pom.presentation.screen.intervals.IntervalsScreenState
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class IntervalsScreenStateProvider(
    json: Json,
    database: Database,
    override val key: String = IntervalsScreenState::class.qualifiedName!!
) : BaseSerializableStateProvider<IntervalsScreenState, SerializableState>(
    json,
    database
) {
    @Serializable
    data class SerializableState(
        val tipId: Long?
    )

    override val serializer = SerializableState.serializer()

    override fun toSerializable(state: IntervalsScreenState) = SerializableState(
        state.tip?.state?.id
    )

    override fun toOriginal(serializableState: SerializableState): IntervalsScreenState {
        val tip = Tip.values().find { it.state.id == serializableState.tipId }
        return IntervalsScreenState(tip)
    }
}