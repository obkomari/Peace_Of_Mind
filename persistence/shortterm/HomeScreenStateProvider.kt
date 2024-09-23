package com.chakulafasta.pom.persistence.shortterm

import com.chakulafasta.pom.Database
import com.chakulafasta.pom.domain.entity.Deck
import com.chakulafasta.pom.domain.entity.GlobalState
import com.chakulafasta.pom.domain.interactor.cardsimport.CardsFileFormat
import com.chakulafasta.pom.domain.interactor.cardsimport.CardsImportStorage
import com.chakulafasta.pom.persistence.shortterm.HomeScreenStateProvider.SerializableHomeScreenState
import com.chakulafasta.pom.presentation.screen.home.ChooseDeckListDialogPurpose
import com.chakulafasta.pom.presentation.screen.home.DeckSelection
import com.chakulafasta.pom.presentation.screen.home.HomeScreenState
import com.soywiz.klock.DateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class HomeScreenStateProvider(
    json: Json,
    database: Database,
    private val globalState: GlobalState,
    private val cardsImportStorage: CardsImportStorage,
    override val key: String = HomeScreenState::class.qualifiedName!!
) : BaseSerializableStateProvider<HomeScreenState, SerializableHomeScreenState>(
    json,
    database
) {
    @Serializable
    data class SerializableHomeScreenState(
        val searchText: String,
        val deckSelection: DeckSelection?,
        val deckIdForDeckOptionMenu: Long?,
        val fileFormatId: Long?,
        val chooseDeckListDialogPurpose: ChooseDeckListDialogPurpose?,
        val deckIdRelatedToNoExerciseCardDialog: Long?,
        val timeWhenTheFirstCardWillBeAvailable: Double?
    )

    override val serializer = SerializableHomeScreenState.serializer()

    override fun toSerializable(state: HomeScreenState) = SerializableHomeScreenState(
        state.searchText,
        state.deckSelection,
        state.deckForDeckOptionMenu?.id,
        state.fileFormatForExport?.id,
        state.chooseDeckListDialogPurpose,
        state.deckRelatedToNoExerciseCardDialog?.id,
        state.timeWhenTheFirstCardWillBeAvailable?.unixMillis
    )

    override fun toOriginal(serializableState: SerializableHomeScreenState): HomeScreenState {
        val deckForDeckOptionMenu: Deck? =
            serializableState.deckIdForDeckOptionMenu?.let { deckId: Long ->
                globalState.decks.first { deck -> deck.id == deckId }
            }
        val fileFormat: CardsFileFormat? =
            if (serializableState.fileFormatId != null) {
                CardsFileFormat.predefinedFormats.find { predefinedFileFormat: CardsFileFormat ->
                    predefinedFileFormat.id == serializableState.fileFormatId
                } ?: cardsImportStorage.customFileFormats.find { customFileFormat: CardsFileFormat ->
                    customFileFormat.id == serializableState.fileFormatId
                }
            } else {
                null
            }
        val deckRelatedToNoExerciseCardDialog: Deck? =
            serializableState.deckIdRelatedToNoExerciseCardDialog?.let { deckId: Long ->
                globalState.decks.first { deck -> deck.id == deckId }
            }
        val timeWhenTheFirstCardWillBeAvailable: DateTime? =
            serializableState.timeWhenTheFirstCardWillBeAvailable?.let(::DateTime)
        return HomeScreenState().apply {
            searchText = serializableState.searchText
            deckSelection = serializableState.deckSelection
            this.deckForDeckOptionMenu = deckForDeckOptionMenu
            fileFormatForExport = fileFormat
            chooseDeckListDialogPurpose = serializableState.chooseDeckListDialogPurpose
            this.deckRelatedToNoExerciseCardDialog = deckRelatedToNoExerciseCardDialog
            this.timeWhenTheFirstCardWillBeAvailable = timeWhenTheFirstCardWillBeAvailable
        }
    }
}