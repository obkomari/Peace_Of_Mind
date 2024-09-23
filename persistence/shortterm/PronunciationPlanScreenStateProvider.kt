package com.chakulafasta.pom.persistence.shortterm

import com.chakulafasta.pom.Database
import com.chakulafasta.pom.persistence.shortterm.PronunciationPlanScreenStateProvider.SerializableState
import com.chakulafasta.pom.presentation.screen.deckeditor.decksettings.Tip
import com.chakulafasta.pom.presentation.screen.pronunciationplan.PronunciationPlanScreenState
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class PronunciationPlanScreenStateProvider(
    json: Json,
    database: Database,
    override val key: String = PronunciationPlanScreenState::class.qualifiedName!!
) : BaseSerializableStateProvider<PronunciationPlanScreenState, SerializableState>(
    json,
    database
) {
    @Serializable
    data class SerializableState(
        val tipId: Long?
    )

    override val serializer = SerializableState.serializer()

    override fun toSerializable(state: PronunciationPlanScreenState) = SerializableState(
        state.tip?.state?.id
    )

    override fun toOriginal(serializableState: SerializableState): PronunciationPlanScreenState {
        val tip = Tip.values().find { it.state.id == serializableState.tipId }
        return PronunciationPlanScreenState(tip)
    }
}