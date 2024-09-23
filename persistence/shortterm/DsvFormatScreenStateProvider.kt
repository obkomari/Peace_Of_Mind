package com.chakulafasta.pom.persistence.shortterm

import com.chakulafasta.pom.Database
import com.chakulafasta.pom.persistence.shortterm.DsvFormatScreenStateProvider.SerializableState
import com.chakulafasta.pom.presentation.screen.dsvformat.DsvFormatScreenState
import com.chakulafasta.pom.presentation.screen.dsvformat.DsvFormatScreenState.Purpose
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class DsvFormatScreenStateProvider(
    json: Json,
    database: Database,
    override val key: String = DsvFormatScreenState::class.qualifiedName!!
) : BaseSerializableStateProvider<DsvFormatScreenState, SerializableState>(
    json,
    database
) {
    @Serializable
    data class SerializableState(
        val purpose: Purpose,
        val isTipVisible: Boolean
    )

    override val serializer = SerializableState.serializer()

    override fun toSerializable(state: DsvFormatScreenState): SerializableState {
        return SerializableState(
            state.purpose,
            state.isTipVisible
        )
    }

    override fun toOriginal(serializableState: SerializableState): DsvFormatScreenState {
        return DsvFormatScreenState(
            serializableState.purpose,
            serializableState.isTipVisible
        )
    }
}