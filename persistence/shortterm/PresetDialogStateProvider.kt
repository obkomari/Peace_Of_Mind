package com.chakulafasta.pom.persistence.shortterm

import com.chakulafasta.pom.Database
import com.chakulafasta.pom.persistence.shortterm.PresetDialogStateProvider.SerializableState
import com.chakulafasta.pom.presentation.screen.deckeditor.decksettings.preset.DialogPurpose
import com.chakulafasta.pom.presentation.screen.deckeditor.decksettings.preset.PresetDialogState
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class PresetDialogStateProvider(
    json: Json,
    database: Database,
    override val key: String
): BaseSerializableStateProvider<PresetDialogState, SerializableState>(
    json,
    database
) {
    @Serializable
    data class SerializableState(
        val purpose: DialogPurpose?,
        val typedPresetName: String,
        val idToDelete: Long?
    )

    override val serializer = SerializableState.serializer()

    override fun toSerializable(state: PresetDialogState) = SerializableState(
        state.purpose,
        state.typedPresetName,
        state.idToDelete
    )

    override fun toOriginal(serializableState: SerializableState) = PresetDialogState().apply {
        purpose = serializableState.purpose
        typedPresetName = serializableState.typedPresetName
        idToDelete = serializableState.idToDelete
    }
}