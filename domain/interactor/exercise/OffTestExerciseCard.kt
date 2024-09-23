package com.chakulafasta.pom.domain.interactor.exercise

import com.chakulafasta.pom.domain.architecturecomponents.FlowMaker

class OffTestExerciseCard(
    base: ExerciseCard.Base
) : FlowMaker<OffTestExerciseCard>(), ExerciseCard {
    override val base: ExerciseCard.Base by flowMaker(base)
}