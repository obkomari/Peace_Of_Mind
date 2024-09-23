package com.chakulafasta.pom.persistence.longterm

import com.chakulafasta.pom.domain.architecturecomponents.PropertyChangeRegistry.Change

interface PropertyChangeHandler {
    fun handle(change: Change)
}