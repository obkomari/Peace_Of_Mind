package com.chakulafasta.pom.persistence.shortterm

import com.chakulafasta.pom.Database
import com.chakulafasta.pom.persistence.shortterm.PronunciationEventDialogStateProvider.SerializableState
import com.chakulafasta.pom.presentation.screen.pronunciationplan.DialogPurpose
import com.chakulafasta.pom.presentation.screen.pronunciationplan.PronunciationEventDialogState
import com.chakulafasta.pom.presentation.screen.pronunciationplan.PronunciationEventType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class PronunciationEventDialogStateProvider(
    json: Json,
    database: Database,
    override val key: String = PronunciationEventDialogState::class.qualifiedName!!
) : BaseSerializableStateProvider<PronunciationEventDialogState, SerializableState>(
    json,
    database
) {
    @Serializable
    data class SerializableState(
        val dialogPurpose: DialogPurpose?,
        val selectedRadioButton: PronunciationEventType?,
        val delayInput: String
    )

    override val serializer = SerializableState.serializer()

    override fun toSerializable(state: PronunciationEventDialogState) = SerializableState(
        state.dialogPurpose,
        state.selectedRadioButton,
        state.delayInput
    )

    override fun toOriginal(serializableState: SerializableState) =
        PronunciationEventDialogState().apply {
            dialogPurpose = serializableState.dialogPurpose
            selectedRadioButton = serializableState.selectedRadioButton
            delayInput = serializableState.delayInput
        }
}