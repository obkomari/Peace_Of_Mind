package com.chakulafasta.pom.persistence.longterm.globalstate.writingchanges

import com.chakulafasta.pom.Database
import com.chakulafasta.pom.domain.architecturecomponents.PropertyChangeRegistry.Change
import com.chakulafasta.pom.domain.architecturecomponents.PropertyChangeRegistry.Change.PropertyValueChange
import com.chakulafasta.pom.domain.entity.PronunciationEvent
import com.chakulafasta.pom.domain.entity.PronunciationPlan
import com.chakulafasta.pom.persistence.longterm.PropertyChangeHandler

class PronunciationPlanPropertyChangeHandler(
    private val database: Database
) : PropertyChangeHandler {
    override fun handle(change: Change) {
        val pronunciationPlanId: Long = change.propertyOwnerId
        val exists: Boolean = database.pronunciationPlanQueries.exists(pronunciationPlanId)
            .executeAsOne()
        if (!exists) return
        when (change.property) {
            PronunciationPlan::pronunciationEvents -> {
                if (change !is PropertyValueChange) return
                val newPronunciationEvents = change.newValue as List<PronunciationEvent>
                database.pronunciationPlanQueries
                    .updatePronunciationEvents(newPronunciationEvents, pronunciationPlanId)
            }
        }
    }
}