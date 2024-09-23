package com.chakulafasta.pom.persistence.longterm

import android.util.Log
import com.chakulafasta.pom.BuildConfig
import com.chakulafasta.pom.Database
import com.chakulafasta.pom.domain.architecturecomponents.PropertyChangeRegistry
import com.chakulafasta.pom.domain.entity.*
import com.chakulafasta.pom.domain.interactor.autoplay.CardFilterForAutoplay
import com.chakulafasta.pom.domain.interactor.exercise.CardFilterForExercise
import com.chakulafasta.pom.domain.interactor.cardsimport.CardsFileFormat
import com.chakulafasta.pom.domain.interactor.cardsimport.CardsImportStorage
import com.chakulafasta.pom.persistence.longterm.cardappearance.CardAppearancePropertyChangeHandler
import com.chakulafasta.pom.persistence.longterm.deckreviewpreference.DeckReviewPreferencePropertyChangeHandler
import com.chakulafasta.pom.persistence.longterm.exercisesettings.ExerciseSettingsPropertyChangeHandler
import com.chakulafasta.pom.persistence.longterm.fileimportstorage.CardsFormatPropertyChangeHandler
import com.chakulafasta.pom.persistence.longterm.fileimportstorage.CardsImportStoragePropertyChangeHandler
import com.chakulafasta.pom.persistence.longterm.fullscreenpreference.FullscreenPreferencePropertyChangeHandler
import com.chakulafasta.pom.persistence.longterm.globalstate.writingchanges.*
import com.chakulafasta.pom.persistence.longterm.initialdecksadderstate.InitialDecksAdderStatePropertyChangeHandler
import com.chakulafasta.pom.persistence.longterm.lastusedlanguages.LastUsedLanguagesPropertyChangeHandler
import com.chakulafasta.pom.persistence.longterm.pronunciationpreference.PronunciationPreferencePropertyChangeHandler
import com.chakulafasta.pom.persistence.longterm.tipstate.TipStatePropertyChangeHandler
import com.chakulafasta.pom.persistence.longterm.walkingmodepreference.WalkingModePreferencePropertyChangeHandler
import com.chakulafasta.pom.presentation.common.LongTermStateSaver
import com.chakulafasta.pom.presentation.common.SpeakerImpl.LastUsedLanguages
import com.chakulafasta.pom.presentation.common.entity.FullscreenPreference
import com.chakulafasta.pom.presentation.common.mainactivity.InitialDecksAdder
import com.chakulafasta.pom.presentation.screen.cardappearance.CardAppearance
import com.chakulafasta.pom.presentation.screen.deckeditor.decksettings.TipState
import com.chakulafasta.pom.presentation.screen.exercisesettings.ExerciseSettings
import com.chakulafasta.pom.presentation.screen.home.DeckReviewPreference
import com.chakulafasta.pom.presentation.screen.pronunciation.PronunciationPreference
import com.chakulafasta.pom.presentation.screen.walkingmodesettings.WalkingModePreference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlin.reflect.KClass

class LongTermStateSaverImpl(
    private val database: Database,
    private val json: Json
) : LongTermStateSaver {
    private val propertyChangeHandlers: Map<KClass<*>, PropertyChangeHandler> =
        HashMap<KClass<*>, PropertyChangeHandler>().apply {
            val intervalSchemePropertyChangeHandler = IntervalSchemePropertyChangeHandler(database)
            val exercisePreferencePropertyChangeHandler = ExercisePreferencePropertyChangeHandler(
                database,
                intervalSchemePropertyChangeHandler
            )
            val deckPropertyChangeHandler = DeckPropertyChangeHandler(
                database,
                exercisePreferencePropertyChangeHandler
            )
            val globalStatePropertyChangeHandler = GlobalStatePropertyChangeHandler(
                database,
                deckPropertyChangeHandler
            )

            put(GlobalState::class, globalStatePropertyChangeHandler)
            put(Deck::class, deckPropertyChangeHandler)
            put(Card::class, CardPropertyChangeHandler(database))
            put(ExercisePreference::class, exercisePreferencePropertyChangeHandler)
            put(Pronunciation::class, PronunciationPropertyChangeHandler(database))
            put(IntervalScheme::class, IntervalSchemePropertyChangeHandler(database))
            put(Interval::class, IntervalPropertyChangeHandler(database))
            put(Grading::class, GradingPropertyChangeHandler(database))
            put(PronunciationPlan::class, PronunciationPlanPropertyChangeHandler(database))
            put(CardFilterForExercise::class, CardFilterForExerciseChangeHandler(database))
            put(CardFilterForAutoplay::class, CardFilterForAutoplayChangeHandler(database))
            put(DeckReviewPreference::class, DeckReviewPreferencePropertyChangeHandler(database))
            put(WalkingModePreference::class, WalkingModePreferencePropertyChangeHandler(database))
            put(FullscreenPreference::class, FullscreenPreferencePropertyChangeHandler(database))
            put(InitialDecksAdder.State::class, InitialDecksAdderStatePropertyChangeHandler(database))
            put(TipState::class, TipStatePropertyChangeHandler(database))
            put(CardsImportStorage::class, CardsImportStoragePropertyChangeHandler(database))
            put(CardsFileFormat::class, CardsFormatPropertyChangeHandler(database))
            put(PronunciationPreference::class, PronunciationPreferencePropertyChangeHandler(database))
            put(LastUsedLanguages::class, LastUsedLanguagesPropertyChangeHandler(database))
            put(CardAppearance::class, CardAppearancePropertyChangeHandler(database))
            put(DeckList::class, DeckListPropertyChangeHandler(database))
            put(ExerciseSettings::class, ExerciseSettingsPropertyChangeHandler(database, json))
        }

    override fun saveStateByRegistry() {
        val changes: List<PropertyChangeRegistry.Change> = PropertyChangeRegistry.removeAll()
        if (changes.isEmpty()) return
        GlobalScope.launch(Dispatchers.IO) {
            database.transaction {
                changes.forEach(::save)
            }
        }
    }

    private fun save(change: PropertyChangeRegistry.Change) {
        if (BuildConfig.DEBUG) {
            Log.d("db", change.toString())
        }
        val handler: PropertyChangeHandler? = propertyChangeHandlers[change.propertyOwnerClass]
        if (handler != null) {
            handler.handle(change)
        } else if (BuildConfig.DEBUG) {
            Log.w("db", "UNHANDLED CHANGE: $change")
        }
    }
}