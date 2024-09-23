package com.chakulafasta.pom.domain.interactor.exercise

import com.chakulafasta.pom.domain.architecturecomponents.FlowMaker

class EntryTestExerciseCard(
    base: ExerciseCard.Base,
    userInput: String? = null
) : FlowMaker<EntryTestExerciseCard>(), ExerciseCard {
    override val base: ExerciseCard.Base by flowMaker(base)
    var userInput: String? by flowMaker(userInput)
}