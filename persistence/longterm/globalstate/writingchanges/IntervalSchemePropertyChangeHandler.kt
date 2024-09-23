package com.chakulafasta.pom.persistence.longterm.globalstate.writingchanges

import com.chakulafasta.pom.Database
import com.chakulafasta.pom.domain.architecturecomponents.PropertyChangeRegistry
import com.chakulafasta.pom.domain.architecturecomponents.PropertyChangeRegistry.Change.*
import com.chakulafasta.pom.domain.entity.Interval
import com.chakulafasta.pom.domain.entity.IntervalScheme
import com.chakulafasta.pom.persistence.longterm.PropertyChangeHandler
import com.chakulafasta.pom.persistence.toIntervalDb

class IntervalSchemePropertyChangeHandler(
    private val database: Database
) : PropertyChangeHandler {
    private val queries = database.intervalSchemeQueries

    override fun handle(change: PropertyChangeRegistry.Change) {
        val intervalSchemeId: Long = change.propertyOwnerId
        val exists: Boolean = queries.exists(intervalSchemeId).executeAsOne()
        if (!exists) return
        when (change.property) {
            IntervalScheme::intervals -> {
                if (change !is CollectionChange) return
                val removedIntervals = change.removedItems as Collection<Interval>
                removedIntervals.forEach { interval -> database.intervalQueries.delete(interval.id) }
                val addedIntervals = change.addedItems as Collection<Interval>
                insertIntervals(addedIntervals, intervalSchemeId)
            }
        }
    }

    fun insertIntervals(intervals: Collection<Interval>, intervalSchemeId: Long) {
        intervals.forEach { interval ->
            val intervalDb = interval.toIntervalDb(intervalSchemeId)
            database.intervalQueries.insert(intervalDb)
        }
    }
}