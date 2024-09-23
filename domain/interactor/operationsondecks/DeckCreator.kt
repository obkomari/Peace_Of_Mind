package com.chakulafasta.pom.domain.interactor.deckcreator

import com.chakulafasta.pom.domain.architecturecomponents.copyableListOf
import com.chakulafasta.pom.domain.architecturecomponents.toCopyableList
import com.chakulafasta.pom.domain.entity.Card
import com.chakulafasta.pom.domain.entity.Deck
import com.chakulafasta.pom.domain.entity.GlobalState
import com.chakulafasta.pom.domain.entity.NameCheckResult
import com.chakulafasta.pom.domain.generateId
import com.chakulafasta.pom.domain.interactor.cardeditor.CardsEditor
import com.chakulafasta.pom.domain.interactor.cardeditor.CardsEditorForEditingDeck
import com.chakulafasta.pom.domain.interactor.cardeditor.EditableCard
import com.chakulafasta.pom.domain.interactor.deckeditor.checkDeckName

fun createDeck(deckName: String, globalState: GlobalState): CardsEditorForEditingDeck? {
    return when (checkDeckName(deckName, globalState)) {
        NameCheckResult.Ok -> {
            val newDeck = Deck(id = generateId(), name = deckName, cards = copyableListOf())
            globalState.decks = (globalState.decks + newDeck).toCopyableList()
            val initialEditableCard = EditableCard(
                Card(id = generateId(), question = "", answer = ""),
                newDeck
            )
            val cardsEditorState = CardsEditor.State(editableCards = listOf(initialEditableCard))
            CardsEditorForEditingDeck(
                newDeck,
                isNewDeck = true,
                cardsEditorState,
                globalState
            )
        }
        else -> null
    }
}