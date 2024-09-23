package com.chakulafasta.pom.persistence.shortterm

import com.chakulafasta.pom.Database
import com.chakulafasta.pom.persistence.shortterm.PronunciationScreenStateProvider.SerializableState
import com.chakulafasta.pom.presentation.screen.deckeditor.decksettings.Tip
import com.chakulafasta.pom.presentation.screen.pronunciation.PronunciationScreenState
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class PronunciationScreenStateProvider(
    json: Json,
    database: Database,
    override val key: String = PronunciationScreenState::class.qualifiedName!!
) : BaseSerializableStateProvider<PronunciationScreenState, SerializableState>(
    json,
    database
) {
    @Serializable
    data class SerializableState(
        val tipId: Long?
    )

    override val serializer = SerializableState.serializer()

    override fun toSerializable(state: PronunciationScreenState) = SerializableState(
        state.tip?.state?.id
    )

    override fun toOriginal(serializableState: SerializableState): PronunciationScreenState {
        val tip = Tip.values().find { it.state.id == serializableState.tipId }
        return PronunciationScreenState(tip)
    }
}