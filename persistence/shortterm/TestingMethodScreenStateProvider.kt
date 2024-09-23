package com.chakulafasta.pom.persistence.shortterm

import com.chakulafasta.pom.Database
import com.chakulafasta.pom.persistence.shortterm.TestingMethodScreenStateProvider.SerializableState
import com.chakulafasta.pom.presentation.screen.deckeditor.decksettings.Tip
import com.chakulafasta.pom.presentation.screen.testingmethod.TestingMethodScreenState
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class TestingMethodScreenStateProvider(
    json: Json,
    database: Database,
    override val key: String = TestingMethodScreenState::class.qualifiedName!!
) : BaseSerializableStateProvider<TestingMethodScreenState, SerializableState>(
    json,
    database
) {
    @Serializable
    data class SerializableState(
        val tipId: Long?
    )

    override val serializer = SerializableState.serializer()

    override fun toSerializable(state: TestingMethodScreenState) = SerializableState(
        state.tip?.state?.id
    )

    override fun toOriginal(serializableState: SerializableState): TestingMethodScreenState {
        val tip = Tip.values().find { it.state.id == serializableState.tipId }
        return TestingMethodScreenState(tip)
    }
}