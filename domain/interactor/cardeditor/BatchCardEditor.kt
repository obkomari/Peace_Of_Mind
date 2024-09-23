package com.chakulafasta.pom.domain.interactor.cardeditor

import com.chakulafasta.pom.domain.architecturecomponents.CopyableList
import com.chakulafasta.pom.domain.architecturecomponents.FlowMaker
import com.chakulafasta.pom.domain.architecturecomponents.copyableListOf
import com.chakulafasta.pom.domain.architecturecomponents.toCopyableList
import com.chakulafasta.pom.domain.entity.*
import com.chakulafasta.pom.domain.generateId
import com.chakulafasta.pom.domain.interactor.autoplay.Player
import com.chakulafasta.pom.domain.interactor.exercise.Exercise

class BatchCardEditor(
    private val globalState: GlobalState,
    val state: State = State(),
    val exercise: Exercise? = null,
    val player: Player? = null
) {
    class State(
        selectedCards: Collection<EditableCard> = emptyList()
    ) : FlowMaker<State>() {
        var selectedCards: Collection<EditableCard> by flowMaker(selectedCards)
    }

    private var cancelLastAction: (() -> Unit)? = null

    fun toggleSelected(editableCard: EditableCard) {
        val selectedCardIds: List<Long> = state.selectedCards.map { it.card.id }
        if (editableCard.card.id in selectedCardIds) {
            state.selectedCards = state.selectedCards.filter { selectedEditableCard: EditableCard ->
                selectedEditableCard.card.id != editableCard.card.id
            }
        } else {
            state.selectedCards += editableCard
        }
    }

    fun addCardsToSelection(editableCards: Collection<EditableCard>) {
        val addedAssociatedEditableCards = editableCards.associateBy { it.card.id }
        state.selectedCards = state.selectedCards.associateBy { it.card.id }
            .plus(addedAssociatedEditableCards)
            .values
    }

    fun invert() {
        val backup: Collection<Card> = state.selectedCards.map { editableCard -> editableCard.card }
        cancelLastAction = {
            backup.forEach(::invert)
        }
        backup.forEach(::invert)
        clearSelection()
    }

    private fun invert(card: Card) {
        val newAnswer = card.question
        card.question = card.answer
        card.answer = newAnswer
        notifyCardChanged(card.id, isQuestionChanged = true, isAnswerChanged = true)
    }

    fun changeGrade(grade: Int) {
        if (grade < 0) return
        val backup: Collection<Pair<Card, Int>> = state.selectedCards
            .map { editableCard: EditableCard -> editableCard.card to editableCard.card.grade }
        cancelLastAction = {
            backup.forEach { (card: Card, oldGrade: Int) ->
                card.grade = oldGrade
                notifyCardChanged(card.id, isGradeChanged = true)
            }
        }
        state.selectedCards.forEach { editableCard: EditableCard ->
            editableCard.card.grade = grade
            notifyCardChanged(editableCard.card.id, isGradeChanged = true)
        }
        clearSelection()
    }

    fun markAsLearned() = markAs(learned = true)
    fun markAsUnlearned() = markAs(learned = false)

    private fun markAs(learned: Boolean) {
        val backup: Collection<Pair<Card, Boolean>> = state.selectedCards
            .map { editableCard: EditableCard -> editableCard.card to editableCard.card.isLearned }
        cancelLastAction = {
            backup.forEach { (card: Card, oldIsLearned: Boolean) ->
                card.isLearned = oldIsLearned
                notifyCardChanged(card.id, isIsLearnedChanged = true)
            }
        }
        state.selectedCards.forEach { editableCard: EditableCard ->
            editableCard.card.isLearned = learned
            notifyCardChanged(editableCard.card.id, isIsLearnedChanged = true)
        }
        clearSelection()
    }

    private fun notifyCardChanged(
        cardId: Long,
        isQuestionChanged: Boolean = false,
        isAnswerChanged: Boolean = false,
        isGradeChanged: Boolean = false,
        isIsLearnedChanged: Boolean = false
    ) {
        exercise?.notifyCardChanged(
            cardId, isQuestionChanged,
            isAnswerChanged, isGradeChanged, isIsLearnedChanged
        )
    }

    fun remove() {
        val removingData: Map<Deck, List<Long>> = state.selectedCards.groupBy(
            keySelector = { editableCard: EditableCard -> editableCard.deck },
            valueTransform = { editableCard: EditableCard -> editableCard.card.id }
        )
        val backup: Map<Deck, CopyableList<Card>> =
            removingData.mapValues { (deck: Deck, _) -> deck.cards }
        cancelLastAction = {
            backup.forEach { (deck: Deck, backupCards: CopyableList<Card>) ->
                deck.cards = backupCards
            }
        }
        removingData.forEach { (deck: Deck, removingCardIds: List<Long>) ->
            deck.cards = deck.cards
                .filter { card: Card -> card.id !in removingCardIds }
                .toCopyableList()
        }
        if (exercise != null || player != null) {
            val removedCardIds = state.selectedCards.map { selectedCard -> selectedCard.card.id }
            exercise?.notifyCardsRemoved(removedCardIds)
            player?.notifyCardsRemoved(removedCardIds)
        }
        clearSelection()
    }

    fun moveTo(abstractDeck: AbstractDeck) {
        val deckToMoveTo = getOrCreateDeckFrom(abstractDeck)
        val removingData: Map<Deck, List<Long>> = state.selectedCards.groupBy(
            keySelector = { editableCard: EditableCard -> editableCard.deck },
            valueTransform = { editableCard: EditableCard -> editableCard.card.id }
        )
        val backup: Map<Deck, CopyableList<Card>> =
            removingData.mapValues { (deck: Deck, _) -> deck.cards }
        val numberOfCardsAddedToExistingDeck =
            if (abstractDeck is ExistingDeck) state.selectedCards.size else 0
        cancelLastAction = {
            if (abstractDeck is NewDeck) {
                globalState.decks = globalState.decks
                    .filter { deck: Deck -> deck.id != deckToMoveTo.id }
                    .toCopyableList()
            } else {
                deckToMoveTo.cards = deckToMoveTo.cards
                    .dropLast(numberOfCardsAddedToExistingDeck)
                    .toCopyableList()
            }
            backup.forEach { (deck: Deck, backupCards: CopyableList<Card>) ->
                deck.cards = backupCards
            }
            if (exercise != null || player != null) {
                val cardMovement = removingData.flatMap { (deck: Deck, movedCardIds: List<Long>) ->
                    movedCardIds.map { cardId: Long -> Exercise.CardMoving(cardId, deck) }
                }
                exercise?.notifyCardsMoved(cardMovement)
                player?.notifyCardsMoved(cardMovement)
            }
        }
        removingData.forEach { (deck: Deck, removingCardIds: List<Long>) ->
            deck.cards = deck.cards
                .filter { card: Card -> card.id !in removingCardIds }
                .toCopyableList()
        }
        val movingCards: List<Card> =
            state.selectedCards.map { editableCard: EditableCard -> editableCard.card }
        deckToMoveTo.cards = (deckToMoveTo.cards + movingCards).toCopyableList()
        if (exercise != null || player != null) {
            val cardMovement: List<Exercise.CardMoving> =
                state.selectedCards.map { selectedCard: EditableCard ->
                    Exercise.CardMoving(selectedCard.card.id, deckToMoveTo)
                }
            exercise?.notifyCardsMoved(cardMovement)
            player?.notifyCardsMoved(cardMovement)
        }
        clearSelection()
    }

    fun copyTo(abstractDeck: AbstractDeck) {
        val deckToCopyTo = getOrCreateDeckFrom(abstractDeck)
        val numberOfCardsAddedToExistingDeck =
            if (abstractDeck is ExistingDeck) state.selectedCards.size else 0
        cancelLastAction = {
            if (abstractDeck is NewDeck) {
                globalState.decks = globalState.decks
                    .filter { deck: Deck -> deck.id != deckToCopyTo.id }
                    .toCopyableList()
            } else {
                deckToCopyTo.cards = deckToCopyTo.cards
                    .dropLast(numberOfCardsAddedToExistingDeck)
                    .toCopyableList()
            }
        }
        val copyingCards: List<Card> =
            state.selectedCards.map { editableCard: EditableCard ->
                Card(
                    id = generateId(),
                    question = editableCard.card.question,
                    answer = editableCard.card.answer
                )
            }
        deckToCopyTo.cards = (deckToCopyTo.cards + copyingCards).toCopyableList()
        clearSelection()
    }

    private fun getOrCreateDeckFrom(abstractDeck: AbstractDeck): Deck {
        return when (abstractDeck) {
            is ExistingDeck -> abstractDeck.deck
            is NewDeck -> {
                val exercisePreferences: List<ExercisePreference> = state.selectedCards
                    .map { editableCard: EditableCard -> editableCard.deck.exercisePreference }
                    .distinctBy { exercisePreference -> exercisePreference.id }
                val decksHaveTheSameExercisePreference: Boolean = exercisePreferences.size == 1
                val exercisePreferenceForNewDeck: ExercisePreference =
                    if (decksHaveTheSameExercisePreference
                        && exercisePreferences.first().isShared()
                    ) {
                        exercisePreferences.first()
                    } else {
                        ExercisePreference.Default
                    }
                val newDeck = Deck(
                    id = generateId(),
                    name = abstractDeck.deckName,
                    cards = copyableListOf(),
                    exercisePreference = exercisePreferenceForNewDeck
                )
                globalState.decks = (globalState.decks + newDeck).toCopyableList()
                newDeck
            }
            else -> error(ERROR_MESSAGE_UNKNOWN_IMPLEMENTATION_OF_ABSTRACT_DECK)
        }
    }

    fun cancelLastAction() {
        cancelLastAction?.invoke()
        cancelLastAction = null
        clearSelection()
    }

    fun clearSelection() {
        state.selectedCards = emptyList()
    }
}