package com.chakulafasta.pom.persistence.longterm.walkingmodepreference

import com.chakulafasta.pom.Database
import com.chakulafasta.pom.domain.architecturecomponents.PropertyChangeRegistry
import com.chakulafasta.pom.domain.architecturecomponents.PropertyChangeRegistry.Change.PropertyValueChange
import com.chakulafasta.pom.persistence.KeyGestureMapDb
import com.chakulafasta.pom.persistence.longterm.PropertyChangeHandler
import com.chakulafasta.pom.presentation.screen.walkingmodesettings.KeyGesture
import com.chakulafasta.pom.presentation.screen.walkingmodesettings.KeyGestureAction
import com.chakulafasta.pom.presentation.screen.walkingmodesettings.WalkingModePreference

class WalkingModePreferencePropertyChangeHandler(
    database: Database
) : PropertyChangeHandler {
    private val queries = database.keyGestureMapQueries

    override fun handle(change: PropertyChangeRegistry.Change) {
        if (change !is PropertyValueChange) return
        when (change.property) {
            WalkingModePreference::keyGestureMap -> {
                val keyGestureMap = change.newValue as Map<KeyGesture, KeyGestureAction>
                keyGestureMap.forEach { (keyGesture: KeyGesture, keyGestureAction: KeyGestureAction) ->
                    val keyGestureMapDb = KeyGestureMapDb(keyGesture, keyGestureAction)
                    queries.replace(keyGestureMapDb)
                }
            }
        }
    }
}