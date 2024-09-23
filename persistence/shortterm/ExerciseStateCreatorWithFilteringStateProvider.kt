package com.chakulafasta.pom.persistence.shortterm

import com.chakulafasta.pom.Database
import com.chakulafasta.pom.domain.entity.Deck
import com.chakulafasta.pom.domain.entity.GlobalState
import com.chakulafasta.pom.domain.interactor.exercise.ExerciseStateCreatorWithFiltering
import com.chakulafasta.pom.persistence.shortterm.ExerciseStateCreatorWithFilteringStateProvider.SerializableState
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class ExerciseStateCreatorWithFilteringStateProvider(
    json: Json,
    database: Database,
    private val globalState: GlobalState,
    override val key: String = ExerciseStateCreatorWithFiltering.State::class.qualifiedName!!
) : BaseSerializableStateProvider<ExerciseStateCreatorWithFiltering.State, SerializableState>(
    json,
    database
) {
    @Serializable
    data class SerializableState(
        val deckIds: List<Long>
    )

    override val serializer = SerializableState.serializer()

    override fun toSerializable(state: ExerciseStateCreatorWithFiltering.State): SerializableState {
        val deckIds: List<Long> = state.decks.map { it.id }
        return SerializableState(deckIds)
    }

    override fun toOriginal(serializableState: SerializableState): ExerciseStateCreatorWithFiltering.State {
        val decks: List<Deck> = globalState.decks.filter { it.id in serializableState.deckIds }
        return ExerciseStateCreatorWithFiltering.State(
            decks,
            globalState.cardFilterForExercise
        )
    }
}