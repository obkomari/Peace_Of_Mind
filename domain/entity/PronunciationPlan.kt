package com.chakulafasta.pom.domain.entity

import com.chakulafasta.pom.domain.architecturecomponents.FlowMakerWithRegistry
import com.chakulafasta.pom.domain.entity.PronunciationEvent.*
import com.soywiz.klock.seconds

class PronunciationPlan(
    override val id: Long,
    pronunciationEvents: List<PronunciationEvent>
) : FlowMakerWithRegistry<PronunciationPlan>() {
    var pronunciationEvents: List<PronunciationEvent> by flowMaker(pronunciationEvents)

    override fun copy() = PronunciationPlan(id, pronunciationEvents)

    companion object {
        val Default = PronunciationPlan(
            id = 0L,
            pronunciationEvents = listOf(
                SpeakQuestion,
                Delay(2.seconds),
                SpeakAnswer,
                Delay(1.seconds)
            )
        )
    }
}

fun PronunciationPlan.isDefault(): Boolean = id == PronunciationPlan.Default.id