package com.chakulafasta.pom.persistence.longterm.globalstate.writingchanges

import com.chakulafasta.pom.Database
import com.chakulafasta.pom.domain.architecturecomponents.PropertyChangeRegistry
import com.chakulafasta.pom.domain.architecturecomponents.PropertyChangeRegistry.Change.PropertyValueChange
import com.chakulafasta.pom.domain.entity.Interval
import com.chakulafasta.pom.persistence.longterm.PropertyChangeHandler
import com.soywiz.klock.DateTimeSpan

class IntervalPropertyChangeHandler(
    database: Database
) : PropertyChangeHandler {
    private val queries = database.intervalQueries

    override fun handle(change: PropertyChangeRegistry.Change) {
        if (change !is PropertyValueChange) return
        val intervalId: Long = change.propertyOwnerId
        val exists: Boolean = queries.exists(intervalId).executeAsOne()
        if (!exists) return
        when (change.property) {
            Interval::grade -> {
                val grade = change.newValue as Int
                queries.updateGrade(grade, intervalId)
            }
            Interval::value -> {
                val value = change.newValue as DateTimeSpan
                queries.updateValue(value, intervalId)
            }
        }
    }
}