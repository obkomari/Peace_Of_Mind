package com.chakulafasta.pom.domain.interactor.operationsondecks

import com.chakulafasta.pom.domain.architecturecomponents.toCopyableList
import com.chakulafasta.pom.domain.entity.Deck
import com.chakulafasta.pom.domain.entity.DeckList
import com.chakulafasta.pom.domain.entity.GlobalState
import com.chakulafasta.pom.domain.interactor.decklistseditor.recheckDeckIdsInDeckLists

class DeckRemover(
    private val globalState: GlobalState
) {
    private var restore: (() -> Unit)? = null

    fun removeDeck(deckId: Long): Int = removeDecks(listOf(deckId))

    fun removeDecks(deckIds: List<Long>): Int {
        val (removingDecks: List<Deck>, remainingDecks: List<Deck>) =
            globalState.decks.partition { deck: Deck -> deck.id in deckIds }
        val deckListsBackup: Map<DeckList, Set<Long>> =
            globalState.deckLists.associateWith { deckList: DeckList -> deckList.deckIds }
        restore = {
            globalState.decks = (globalState.decks + removingDecks).toCopyableList()
            deckListsBackup.forEach { (deckList: DeckList, deckIds: Set<Long>) ->
                deckList.deckIds = deckIds
            }
            recheckDeckIdsInDeckLists(globalState)
        }
        globalState.decks = remainingDecks.toCopyableList()
        recheckDeckIdsInDeckLists(globalState)
        return removingDecks.size
    }

    fun cancelRemoving() {
        restore?.invoke()
        restore = null
    }
}