package com.chakulafasta.pom.persistence.shortterm

import com.chakulafasta.pom.Database
import com.chakulafasta.pom.persistence.shortterm.ModifyIntervalDialogStateProvider.SerializableState
import com.chakulafasta.pom.presentation.screen.intervals.DisplayedInterval
import com.chakulafasta.pom.presentation.screen.intervals.DisplayedInterval.IntervalUnit
import com.chakulafasta.pom.presentation.screen.intervals.modifyinterval.ModifyIntervalDialogState
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class ModifyIntervalDialogStateProvider(
    json: Json,
    database: Database,
    override val key: String = ModifyIntervalDialogState::class.qualifiedName!!
) : BaseSerializableStateProvider<ModifyIntervalDialogState, SerializableState>(
    json,
    database
) {
    @Serializable
    data class SerializableState(
        val purpose: ModifyIntervalDialogState.Purpose,
        val grade: Int,
        val intervalInputValue: Int?,
        val intervalUnit: IntervalUnit
    )

    override val serializer = SerializableState.serializer()

    override fun toSerializable(state: ModifyIntervalDialogState) =
        SerializableState(
            purpose = state.purpose,
            grade = state.grade,
            intervalInputValue = state.displayedInterval.value,
            intervalUnit = state.displayedInterval.intervalUnit
        )

    override fun toOriginal(
        serializableState: SerializableState
    ): ModifyIntervalDialogState {
        val intervalInputData =
            DisplayedInterval(
                serializableState.intervalInputValue,
                serializableState.intervalUnit
            )
        return ModifyIntervalDialogState(
            serializableState.purpose,
            serializableState.grade,
            intervalInputData
        )
    }
}