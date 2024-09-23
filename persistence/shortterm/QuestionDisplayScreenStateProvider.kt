package com.chakulafasta.pom.persistence.shortterm

import com.chakulafasta.pom.Database
import com.chakulafasta.pom.persistence.shortterm.QuestionDisplayScreenStateProvider.SerializableState
import com.chakulafasta.pom.presentation.screen.deckeditor.decksettings.Tip
import com.chakulafasta.pom.presentation.screen.questiondisplay.QuestionDisplayScreenState
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class QuestionDisplayScreenStateProvider(
    json: Json,
    database: Database,
    override val key: String = QuestionDisplayScreenState::class.qualifiedName!!
) : BaseSerializableStateProvider<QuestionDisplayScreenState, SerializableState>(
    json,
    database
) {
    @Serializable
    data class SerializableState(
        val tipId: Long?
    )

    override val serializer = SerializableState.serializer()

    override fun toSerializable(state: QuestionDisplayScreenState) = SerializableState(
        state.tip?.state?.id
    )

    override fun toOriginal(serializableState: SerializableState): QuestionDisplayScreenState {
        val tip = Tip.values().find { it.state.id == serializableState.tipId }
        return QuestionDisplayScreenState(tip)
    }
}