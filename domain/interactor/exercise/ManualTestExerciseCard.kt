package com.chakulafasta.pom.domain.interactor.exercise

import com.chakulafasta.pom.domain.architecturecomponents.FlowMaker

class ManualTestExerciseCard(
    base: ExerciseCard.Base
) : FlowMaker<ManualTestExerciseCard>(), ExerciseCard {
    override val base: ExerciseCard.Base by flowMaker(base)
}