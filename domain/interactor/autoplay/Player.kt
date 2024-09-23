package com.chakulafasta.pom.domain.interactor.autoplay

import com.chakulafasta.pom.domain.architecturecomponents.FlowMaker
import com.chakulafasta.pom.domain.entity.*
import com.chakulafasta.pom.domain.entity.CardInversion.*
import com.chakulafasta.pom.domain.entity.PronunciationEvent.*
import com.chakulafasta.pom.domain.entity.PronunciationEvent.Delay
import com.chakulafasta.pom.domain.interactor.exercise.Exercise
import com.chakulafasta.pom.domain.interactor.exercise.TextInBracketsRemover
import kotlinx.coroutines.*
import java.util.*
import kotlin.coroutines.CoroutineContext
import kotlin.random.Random

class Player(
    val state: State,
    private val globalState: GlobalState,
    private val speaker: Speaker,
    coroutineContext: CoroutineContext
) {
    class State(
        playingCards: List<PlayingCard>,
        currentPosition: Int = 0,
        pronunciationEventPosition: Int = 0,
        currentLap: Int = 0,
        isPlaying: Boolean = true,
        isCompleted: Boolean = false,
        questionSelection: String = "",
        answerSelection: String = ""
    ) : FlowMaker<State>() {
        var playingCards: List<PlayingCard> by flowMaker(playingCards)
        var currentPosition: Int by flowMaker(currentPosition)
        var pronunciationEventPosition: Int by flowMaker(pronunciationEventPosition)
        var currentLap: Int by flowMaker(currentLap)
        var isPlaying: Boolean by flowMaker(isPlaying)
        var isCompleted: Boolean by flowMaker(isCompleted)
        var questionSelection: String by flowMaker(questionSelection)
        var answerSelection: String by flowMaker(answerSelection)
    }

    private val coroutineScope = CoroutineScope(coroutineContext)

    private val currentPlayingCard: PlayingCard
        get() = with(state) { playingCards[currentPosition] }

    private val currentPronunciation
        get() = currentPlayingCard.deck.exercisePreference.pronunciation

    private val questionLanguage: Locale?
        get() = if (currentPlayingCard.isInverted)
            currentPronunciation.answerLanguage else
            currentPronunciation.questionLanguage

    private val answerLanguage: Locale?
        get() = if (currentPlayingCard.isInverted)
            currentPronunciation.questionLanguage else
            currentPronunciation.answerLanguage

    private val currentPronunciationPlan: PronunciationPlan
        get() = currentPlayingCard.deck.exercisePreference.pronunciationPlan

    private val currentPronunciationEvent: PronunciationEvent
        get() = with(currentPronunciationPlan.pronunciationEvents) {
            getOrElse(state.pronunciationEventPosition) { last() }
        }

    private val textInBracketsRemover by lazy(::TextInBracketsRemover)
    private var delayJob: Job? = null
    private var skipDelay = true
    private val onSpeakingFinished = ::tryToExecuteNextPronunciationEvent

    init {
        if (isPositionValid()) {
            speaker.addOnSpeakingFinishedListener(onSpeakingFinished)
            if (state.isPlaying) {
                executePronunciationEvent()
            }
        }
    }

    private fun isPositionValid(): Boolean =
        state.currentPosition in 0..state.playingCards.lastIndex

    fun setNumberOfLaps(numberOfLaps: Int) {
        globalState.numberOfLapsInPlayer = numberOfLaps
    }

    fun setCurrentPosition(position: Int) {
        if (position < 0
            || position >= state.playingCards.size
            || position == state.currentPosition
        ) {
            return
        }
        pause()
        state.currentPosition = position
        state.pronunciationEventPosition = 0
    }

    fun showQuestion() {
        if (!isPositionValid()) return
        currentPlayingCard.isQuestionDisplayed = true
    }

    fun showAnswer() {
        if (!isPositionValid()) return
        currentPlayingCard.isQuestionDisplayed = true
        currentPlayingCard.isAnswerDisplayed = true
    }

    fun setQuestionSelection(selection: String) {
        state.questionSelection = selection
        state.answerSelection = ""
    }

    fun setAnswerSelection(selection: String) {
        state.answerSelection = selection
        state.questionSelection = ""
    }

    fun setIsCardLearned(isLearned: Boolean) {
        if (!isPositionValid()) return
        currentPlayingCard.card.isLearned = isLearned
    }

    fun speak() {
        if (!isPositionValid()) return
        pause()
        when {
            hasQuestionSelection() -> speakQuestionSelection()
            hasAnswerSelection() -> speakAnswerSelection()
            currentPlayingCard.isAnswerDisplayed -> speakAnswer()
            else -> speakQuestion()
        }
    }

    private fun hasAnswerSelection(): Boolean = state.answerSelection.isNotEmpty()
    private fun hasQuestionSelection(): Boolean = state.questionSelection.isNotEmpty()

    private fun speakQuestionSelection() {
        speak(
            state.questionSelection,
            questionLanguage
        )
    }

    private fun speakAnswerSelection() {
        speak(
            state.answerSelection,
            answerLanguage
        )
    }

    private fun speakQuestion() {
        with(currentPlayingCard) {
            val question = if (isInverted) card.answer else card.question
            speak(question, questionLanguage)
        }
    }

    private fun speakAnswer() {
        with(currentPlayingCard) {
            val answer = if (isInverted) card.question else card.answer
            speak(answer, answerLanguage)
        }
    }

    private fun speak(text: String, language: Locale?) {
        val textToSpeak =
            if (currentPronunciation.speakTextInBrackets) text
            else textInBracketsRemover.process(text)
        speaker.speak(textToSpeak, language)
    }

    fun stopSpeaking() {
        delayJob?.cancel()
        speaker.stop()
        state.isPlaying = false
    }

    fun setGrade(grade: Int) {
        if (!isPositionValid()) return
        currentPlayingCard.card.grade = grade
    }

    fun pause() {
        if (!state.isPlaying) return
        delayJob?.cancel()
        speaker.stop()
        state.isPlaying = false
    }

    fun resume() {
        if (!isPositionValid()) return
        if (state.isPlaying) return
        if (!hasOneMorePronunciationEventForCurrentPlayingCard() && !hasOneMorePlayingCard()) {
            nextLap()
        }
        skipDelay = true
        state.isPlaying = true
        state.isCompleted = false
        executePronunciationEvent()
    }

    fun playOneMoreLap() {
        if (!isPositionValid()) return
        nextLap()
        resume()
    }

    private fun executePronunciationEvent() {
        when (val pronunciationEvent = currentPronunciationEvent) {
            SpeakQuestion -> {
                skipDelay = false
                speakQuestion()
            }
            SpeakAnswer -> {
                skipDelay = false
                showAnswer()
                speakAnswer()
            }
            is Delay -> {
                if (skipDelay) {
                    tryToExecuteNextPronunciationEvent()
                } else {
                    delayJob = coroutineScope.launch {
                        delay(pronunciationEvent.timeSpan.millisecondsLong)
                        if (isActive) {
                            tryToExecuteNextPronunciationEvent()
                        }
                    }
                }
            }
        }
    }

    private fun tryToExecuteNextPronunciationEvent() {
        if (!state.isPlaying) return
        val success: Boolean = switchToNextPronunciationEvent()
        if (success) {
            executePronunciationEvent()
        } else {
            state.isCompleted = true
            state.isPlaying = false
        }
    }

    private fun switchToNextPronunciationEvent(): Boolean {
        return when {
            hasOneMorePronunciationEventForCurrentPlayingCard() -> {
                state.pronunciationEventPosition++
                true
            }
            hasOneMorePlayingCard() -> {
                state.currentPosition++
                state.pronunciationEventPosition = 0
                true
            }
            hasOneMoreLap() -> {
                nextLap()
                true
            }
            else -> false
        }
    }

    private fun hasOneMorePronunciationEventForCurrentPlayingCard(): Boolean =
        state.pronunciationEventPosition + 1 < currentPronunciationPlan.pronunciationEvents.size

    private fun hasOneMorePlayingCard(): Boolean =
        state.currentPosition + 1 < state.playingCards.size

    private fun nextLap() {
        state.playingCards.forEach { playingCard: PlayingCard ->
            with(playingCard) {
                isQuestionDisplayed = deck.exercisePreference.isQuestionDisplayed
                isAnswerDisplayed = false
            }
        }
        state.currentPosition = 0
        state.pronunciationEventPosition = 0
        state.currentLap++
    }

    private fun hasOneMoreLap(): Boolean = state.currentLap + 1 < globalState.numberOfLapsInPlayer

    fun notifyCardsRemoved(removedCardIds: List<Long>) {
        state.playingCards = state.playingCards
            .filter { playingCard: PlayingCard -> playingCard.card.id !in removedCardIds }
    }

    fun notifyCardsMoved(cardMovement: List<Exercise.CardMoving>) {
        for (cardMoving: Exercise.CardMoving in cardMovement) {
            for (playingCard: PlayingCard in state.playingCards) {
                if (playingCard.card.id != cardMoving.cardId) continue
                val isExercisePreferenceChanged: Boolean =
                    playingCard.deck.exercisePreference.id != cardMoving.deckMovedTo.exercisePreference.id
                playingCard.deck = cardMoving.deckMovedTo
                if (isExercisePreferenceChanged) {
                    playingCard.conformToNewExercisePreference()
                }
            }
        }
    }

    fun notifyExercisePreferenceChanged() {
        state.playingCards.forEach { playingCard: PlayingCard ->
            playingCard.conformToNewExercisePreference()
        }
    }

    private fun PlayingCard.conformToNewExercisePreference() {
        isInverted = when (deck.exercisePreference.cardInversion) {
            Off -> false
            On -> true
            EveryOtherLap -> card.lap % 2 == 1
            Randomly -> Random.nextBoolean()
        }
        isQuestionDisplayed = isAnswerDisplayed || deck.exercisePreference.isQuestionDisplayed
    }

    fun dispose() {
        speaker.removeOnSpeakingFinishedListener(onSpeakingFinished)
        speaker.stop()
        coroutineScope.cancel()
    }
}