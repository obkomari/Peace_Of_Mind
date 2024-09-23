package com.chakulafasta.pom.persistence.shortterm

import com.chakulafasta.pom.Database
import com.chakulafasta.pom.domain.interactor.decklistseditor.EditableDeckList
import com.chakulafasta.pom.persistence.shortterm.DeckListEditorScreenStateProvider.SerializableState
import com.chakulafasta.pom.presentation.screen.decklistseditor.DeckListEditorScreenState
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class DeckListEditorScreenStateProvider(
    json: Json,
    database: Database,
    private val editingDeckLists: List<EditableDeckList>,
    override val key: String = DeckListEditorScreenState::class.qualifiedName!!
) : BaseSerializableStateProvider<DeckListEditorScreenState, SerializableState>(
    json,
    database
) {
    @Serializable
    data class SerializableState(
        val isForCreation: Boolean,
        val deckListIdForColorChooser: Long?
    )

    override val serializer = SerializableState.serializer()

    override fun toSerializable(state: DeckListEditorScreenState): SerializableState {
        return SerializableState(
            state.isForCreation,
            state.editableDeckListForColorChooser?.deckList?.id
        )
    }

    override fun toOriginal(serializableState: SerializableState): DeckListEditorScreenState {
        val editableDeckListForColorChooser: EditableDeckList? =
            serializableState.deckListIdForColorChooser?.let { deckListId: Long ->
                editingDeckLists.find { it.deckList.id == deckListId }
            }
        return DeckListEditorScreenState(
            serializableState.isForCreation,
            editableDeckListForColorChooser
        )
    }
}