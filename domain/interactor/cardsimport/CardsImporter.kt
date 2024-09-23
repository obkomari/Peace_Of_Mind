package com.chakulafasta.pom.domain.interactor.cardsimport

import com.chakulafasta.pom.domain.architecturecomponents.CopyableList
import com.chakulafasta.pom.domain.architecturecomponents.FlowMaker
import com.chakulafasta.pom.domain.architecturecomponents.toCopyableList
import com.chakulafasta.pom.domain.entity.*
import com.chakulafasta.pom.domain.entity.NameCheckResult.Ok
import com.chakulafasta.pom.domain.generateId
import com.chakulafasta.pom.domain.interactor.deckeditor.checkDeckName
import com.chakulafasta.pom.domain.interactor.cardsimport.CardsFileFormat.Companion.EXTENSION_CSV
import com.chakulafasta.pom.domain.interactor.cardsimport.CardsFileFormat.Companion.EXTENSION_TSV
import com.chakulafasta.pom.domain.interactor.cardsimport.CardsFileFormat.Companion.EXTENSION_TXT
import com.chakulafasta.pom.domain.interactor.cardsimport.CardsImporter.ImportResult.Failure
import com.chakulafasta.pom.domain.interactor.cardsimport.CardsImporter.ImportResult.Failure.Cause
import com.chakulafasta.pom.domain.interactor.cardsimport.CardsImporter.ImportResult.Success
import com.chakulafasta.pom.domain.interactor.cardsimport.Parser.CardMarkup
import com.chakulafasta.pom.domain.interactor.cardsimport.Parser.Error
import com.chakulafasta.pom.domain.removeFirst
import java.nio.charset.Charset

class CardsImporter(
    val state: State,
    private val globalState: GlobalState,
    private val cardsImportStorage: CardsImportStorage
) {
    class State(
        files: List<CardsFile>,
        currentPosition: Int = 0,
        maxVisitedPosition: Int = 0
    ) : FlowMaker<State>() {
        var files: List<CardsFile> by flowMaker(files)
        var currentPosition: Int by flowMaker(currentPosition)
        var maxVisitedPosition: Int by flowMaker(maxVisitedPosition)

        companion object {
            fun fromFiles(files: List<ImportedCardsFile>, cardsImportStorage: CardsImportStorage): State {
                val cardsFile: List<CardsFile> =
                    files.map { (fileName: String, content: ByteArray) ->
                        val charset: Charset = Charset.defaultCharset()
                        val extension = fileName.substringAfterLast('.', "")
                        val format = when (extension) {
                            EXTENSION_TXT -> cardsImportStorage.lastUsedFormatForTxt
                            EXTENSION_CSV -> cardsImportStorage.lastUsedFormatForCsv
                            EXTENSION_TSV -> cardsImportStorage.lastUsedFormatForTsv
                            else -> CardsFileFormat.FMN_FORMAT
                        }
                        val text: String = content.toString(charset)
                            .normalizeForParser(format.parser)
                        val parseResult = format.parser.parse(text)
                        val errors: List<Error> = parseResult.errors
                        val cardPrototypes: List<CardPrototype> =
                            parseResult.cardMarkups.map { cardMarkup: CardMarkup ->
                                val question: String = cardMarkup.questionText
                                val answer: String = cardMarkup.answerText
                                CardPrototype(
                                    id = generateId(),
                                    question,
                                    answer,
                                    isSelected = true
                                )
                            }
                        val deckName = fileName.substringBeforeLast(".")
                        val deckWhereToAdd: AbstractDeck = NewDeck(deckName)
                        CardsFile(
                            id = generateId(),
                            extension,
                            sourceBytes = content,
                            charset,
                            text,
                            format,
                            errors,
                            cardPrototypes,
                            deckWhereToAdd
                        )
                    }
                return State(cardsFile)
            }
        }
    }

    private val currentFile: CardsFile get() = with(state) { files[currentPosition] }

    fun setCurrentPosition(position: Int) {
        with(state) {
            if (position !in 0..files.lastIndex || position == currentPosition) return
            currentPosition = position
            if (position > maxVisitedPosition) {
                maxVisitedPosition = position
            }
        }
    }

    fun skip() {
        with(state) {
            if (files.size <= 1) return
            val position = currentPosition
            if (currentPosition == files.lastIndex) {
                currentPosition--
            }
            files = files.toMutableList().apply {
                removeAt(position)
            }
        }
    }

    fun setDeckWhereToAdd(deckWhereToAdd: AbstractDeck) {
        currentFile.deckWhereToAdd = deckWhereToAdd
    }

    fun setCharset(newCharset: Charset) {
        setCharsetForPosition(newCharset, state.currentPosition)
        for (position in state.files.indices) {
            if (position > state.maxVisitedPosition) {
                setCharsetForPosition(newCharset, position)
            }
        }
        cardsImportStorage.lastUsedEncodingName = newCharset.name()
    }

    private fun setCharsetForPosition(newCharset: Charset, position: Int) {
        val file: CardsFile = state.files[position]
        if (file.charset == newCharset) return
        val reencodedText: String = file.sourceBytes.toString(newCharset)
            .normalizeForParser(file.format.parser)
        updateTextForPosition(reencodedText, position)
        file.charset = newCharset
    }

    fun setFormat(format: CardsFileFormat) {
        setFormatForPosition(format, state.currentPosition)
        state.files.forEachIndexed { index, cardsFile ->
            if (index > state.maxVisitedPosition && cardsFile.extension == currentFile.extension) {
                setFormatForPosition(format, index)
            }
        }
        when (currentFile.extension) {
            EXTENSION_TXT -> cardsImportStorage.lastUsedFormatForTxt = format
            EXTENSION_CSV -> cardsImportStorage.lastUsedFormatForCsv = format
            EXTENSION_TSV -> cardsImportStorage.lastUsedFormatForTsv = format
        }
    }

    private fun setFormatForPosition(format: CardsFileFormat, position: Int) {
        val file: CardsFile = state.files[position]
        file.format = format
        updateTextForPosition(file.text, position)
    }

    fun updateText(text: String): Parser.ParserResult =
        updateTextForPosition(text, state.currentPosition)

    private fun updateTextForPosition(text: String, position: Int): Parser.ParserResult {
        val file: CardsFile = state.files[position]
        val parseResult: Parser.ParserResult = file.format.parser.parse(text)
        file.text = text
        file.errors = parseResult.errors
        val oldCardPrototypes: MutableList<CardPrototype> = file.cardPrototypes.toMutableList()
        file.cardPrototypes = parseResult.cardMarkups.map { cardMarkup: CardMarkup ->
            val question: String = cardMarkup.questionText
            val answer: String = cardMarkup.answerText
            oldCardPrototypes.removeFirst { cardPrototype: CardPrototype ->
                cardPrototype.question == question && cardPrototype.answer == answer
            } ?: CardPrototype(
                id = generateId(),
                question,
                answer,
                isSelected = true
            )
        }
        return parseResult
    }

    fun invertSelection(cardPrototypeId: Long) {
        with(currentFile) {
            cardPrototypes = cardPrototypes.map { cardPrototype: CardPrototype ->
                if (cardPrototypeId == cardPrototype.id)
                    cardPrototype.copy(isSelected = !cardPrototype.isSelected) else
                    cardPrototype
            }
        }
    }

    fun selectAll() {
        with(currentFile) {
            cardPrototypes = cardPrototypes.map { cardPrototype: CardPrototype ->
                if (cardPrototype.isSelected) {
                    cardPrototype
                } else {
                    cardPrototype.copy(isSelected = true)
                }
            }
        }
    }

    fun unselectAll() {
        with(currentFile) {
            cardPrototypes = cardPrototypes.map { cardPrototype: CardPrototype ->
                if (cardPrototype.isSelected) {
                    cardPrototype.copy(isSelected = false)
                } else {
                    cardPrototype
                }
            }
        }
    }

    fun selectOnlyNew() {
        val existingCards: List<Card> =
            (currentFile.deckWhereToAdd as? ExistingDeck)?.deck?.cards ?: return
        with(currentFile) {
            cardPrototypes = cardPrototypes.map { cardPrototype: CardPrototype ->
                val doesImportedCardExist = existingCards.any { existingCard: Card ->
                    existingCard.question == cardPrototype.question
                            && existingCard.answer == cardPrototype.answer
                }
                val shouldBeSelected = !doesImportedCardExist
                if (cardPrototype.isSelected != shouldBeSelected) {
                    cardPrototype.copy(isSelected = shouldBeSelected)
                } else {
                    cardPrototype
                }
            }
        }
    }

    fun useCurrentDeckForNextFiles() {
        val first = state.currentPosition + 1
        val last = state.files.lastIndex
        for (i in first..last) {
            val file = state.files[i]
            file.deckWhereToAdd = currentFile.deckWhereToAdd
        }
    }

    fun import(): ImportResult {
        for ((position: Int, cardsFile: CardsFile) in state.files.withIndex()) {
            val deckWhereToAdd = cardsFile.deckWhereToAdd
            if (deckWhereToAdd is NewDeck) {
                val nameCheckResult: NameCheckResult =
                    checkDeckName(deckWhereToAdd.deckName, globalState)
                if (nameCheckResult != Ok) {
                    return Failure(Cause.InvalidName(position))
                }
            }
        }
        val importedDecks: MutableList<Deck> = ArrayList(state.files.size)
        var numberOfImportedCards = 0
        for (cardsFile: CardsFile in state.files) {
            val newCards: CopyableList<Card> = cardsFile.cardPrototypes
                .filter { cardPrototype -> cardPrototype.isSelected }
                .map { cardPrototype -> cardPrototype.toCard() }
                .toCopyableList()
            if (newCards.isEmpty()) continue
            when (val deckWhereToAdd = cardsFile.deckWhereToAdd) {
                is NewDeck -> {
                    val deck = Deck(
                        id = generateId(),
                        name = deckWhereToAdd.deckName,
                        cards = newCards
                    )
                    globalState.decks = (globalState.decks + deck).toCopyableList()
                    importedDecks.add(deck)
                }
                is ExistingDeck -> {
                    deckWhereToAdd.deck.cards =
                        (deckWhereToAdd.deck.cards + newCards).toCopyableList()
                    importedDecks.add(deckWhereToAdd.deck)
                }
                else -> error(ERROR_MESSAGE_UNKNOWN_IMPLEMENTATION_OF_ABSTRACT_DECK)
            }
            numberOfImportedCards += newCards.size
        }
        return if (numberOfImportedCards == 0) {
            Failure(Cause.NoCards)
        } else {
            Success(importedDecks, numberOfImportedCards)
        }
    }

    sealed class ImportResult {
        class Success(val decks: List<Deck>, val numberOfImportedCards: Int) : ImportResult()
        class Failure(val cause: Cause) : ImportResult() {
            sealed class Cause {
                class InvalidName(val position: Int) : Cause()
                object NoCards : Cause()
            }
        }
    }

    private companion object {
        val unwantedEOL by lazy { Regex("""\r\n?""") }

        fun String.normalizeForParser(parser: Parser): String {
            return when (parser) {
                is FmnFormatParser -> {
                    removePrefix("\uFEFF").replace(unwantedEOL, "\n")
                }
                else -> {
                    removePrefix("\uFEFF")
                }
            }
        }
    }
}