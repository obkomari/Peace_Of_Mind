package com.chakulafasta.pom.domain.interactor.cardeditor

import com.chakulafasta.pom.domain.architecturecomponents.toCopyableList
import com.chakulafasta.pom.domain.entity.AbstractDeck
import com.chakulafasta.pom.domain.entity.Card
import com.chakulafasta.pom.domain.entity.Deck
import com.chakulafasta.pom.domain.entity.GlobalState
import com.chakulafasta.pom.domain.generateId

class CardsEditorForEditingDeck(
    val deck: Deck,
    val isNewDeck: Boolean,
    state: State,
    globalState: GlobalState
) : CardsEditor(state, globalState) {
    init {
        ensureLastEmptyCard()
    }

    override fun setQuestion(question: String) {
        super.setQuestion(question)
        ensureLastEmptyCard()
    }

    override fun setAnswer(answer: String) {
        super.setAnswer(answer)
        ensureLastEmptyCard()
    }

    private fun ensureLastEmptyCard() {
        with(state) {
            if (editableCards.last().isFullyBlank()) {
                var redundantCardCount = 0
                for (i in editableCards.lastIndex - 1 downTo currentPosition) {
                    if (editableCards[i].isFullyBlank()) {
                        redundantCardCount++
                    } else {
                        break
                    }
                }
                if (redundantCardCount > 0) {
                    editableCards = editableCards.dropLast(redundantCardCount)
                }
            } else {
                val newEditableCard = EditableCard(
                    Card(id = generateId(), question = "", answer = ""),
                    deck
                )
                editableCards = editableCards + newEditableCard
            }
        }
    }

    override fun isCurrentCardRemovable(): Boolean =
        isPositionValid() && state.currentPosition != state.editableCards.lastIndex

    override fun moveTo(abstractDeck: AbstractDeck): Boolean {
        if (!isPositionValid()) return false
        if (!isCurrentCardMovable()) return false
        val deck: Deck = getOrCreateDeckFrom(abstractDeck)
        with(state) {
            editableCards = editableCards.toMutableList().apply {
                val movedCard = removeAt(currentPosition)
                movements.add(CardMoving(movedCard, currentPosition, deck))
            }
            currentPosition = when {
                editableCards.isEmpty() -> -1
                currentPosition > editableCards.lastIndex -> editableCards.lastIndex
                else -> currentPosition
            }
        }
        return true
    }

    override fun cancelLastMovement() {
        with(state) {
            if (movements.isEmpty()) return
            val lastCardMoving: CardMoving = movements.removeLast()
            val lastMovedCard: EditableCard = lastCardMoving.editableCard
            val insertPosition: Int = minOf(lastCardMoving.positionInSource, editableCards.size)
            editableCards = editableCards.toMutableList().apply {
                add(insertPosition, lastMovedCard)
            }
            currentPosition = insertPosition
        }
    }

    override fun areCardsEdited(): Boolean {
        if (state.removals.isNotEmpty()) return true
        if (state.movements.isNotEmpty()) return true
        if (state.copyOperations.isNotEmpty()) return true
        with(state) {
            val originalCards = deck.cards
            if (originalCards.size != editableCards.size - 1) return true
            repeat(originalCards.size) { i: Int ->
                val originalCard: Card = originalCards[i]
                val editableCard: EditableCard = editableCards[i]
                if (isEdited(originalCard, editableCard)) return true
            }
        }
        return false
    }

    private fun isEdited(originalCard: Card, editableCard: EditableCard): Boolean {
        return originalCard.id != editableCard.card.id
                || originalCard.question != editableCard.question
                || originalCard.answer != editableCard.answer
                || originalCard.isLearned != editableCard.isLearned
                || originalCard.grade != editableCard.grade
    }

    override fun save(): SavingResult {
        checkDeck()?.let { failure -> return failure }
        applyCopying()
        applyChangesInThisDeck()
        applyMovements()
        return SavingResult.Success
    }

    private fun checkDeck(): SavingResult.Failure? {
        val underfilledPositions: List<Int> = state.editableCards
            .mapIndexedNotNull { index, editableCard ->
                if (editableCard.isHalfFilled()) index else null
            }
        return if (underfilledPositions.isEmpty()) null
        else SavingResult.Failure(underfilledPositions)
    }

    private fun applyChangesInThisDeck() {
        deck.cards = state.editableCards
            .filterNot(EditableCard::isFullyBlank)
            .map { editableCard ->
                editableCard.card.apply {
                    question = editableCard.question
                    answer = editableCard.answer
                    isLearned = editableCard.isLearned
                    grade = editableCard.grade
                }
            }
            .toCopyableList()
    }

    private fun applyMovements() {
        state.movements.groupBy(
            keySelector = { cardMoving: CardMoving -> cardMoving.targetDeck },
            valueTransform = { cardMoving: CardMoving ->
                cardMoving.editableCard.card.apply {
                    question = cardMoving.editableCard.question
                    answer = cardMoving.editableCard.answer
                    isLearned = cardMoving.editableCard.isLearned
                    grade = cardMoving.editableCard.grade
                }
            }
        ).forEach { (deckToMoveTo: Deck, movingCards: List<Card>) ->
            deckToMoveTo.cards = (deckToMoveTo.cards + movingCards).toCopyableList()
        }
    }
}