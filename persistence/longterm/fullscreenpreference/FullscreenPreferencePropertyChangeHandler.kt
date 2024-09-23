package com.chakulafasta.pom.persistence.longterm.fullscreenpreference

import com.chakulafasta.pom.Database
import com.chakulafasta.pom.domain.architecturecomponents.PropertyChangeRegistry.Change
import com.chakulafasta.pom.domain.architecturecomponents.PropertyChangeRegistry.Change.PropertyValueChange
import com.chakulafasta.pom.persistence.DbKeys
import com.chakulafasta.pom.persistence.longterm.PropertyChangeHandler
import com.chakulafasta.pom.presentation.common.entity.FullscreenPreference

class FullscreenPreferencePropertyChangeHandler(
    database: Database
) : PropertyChangeHandler {
    private val queries = database.keyValueQueries

    override fun handle(change: Change) {
        if (change !is PropertyValueChange) return
        when (change.property) {
            FullscreenPreference::isEnabledInExercise -> {
                val isEnabledInExercise = change.newValue as Boolean
                queries.replace(
                    key = DbKeys.IS_FULLSCREEN_ENABLED_IN_EXERCISE,
                    value = isEnabledInExercise.toString()
                )
            }
            FullscreenPreference::isEnabledInCardPlayer -> {
                val isEnabledInCardPlayer = change.newValue as Boolean
                queries.replace(
                    key = DbKeys.IS_FULLSCREEN_ENABLED_IN_CARD_PLAYER,
                    value = isEnabledInCardPlayer.toString()
                )
            }
            FullscreenPreference::isEnabledInOtherPlaces -> {
                val isEnabledInOtherPlaces = change.newValue as Boolean
                queries.replace(
                    key = DbKeys.IS_FULLSCREEN_ENABLED_IN_OTHER_PLACES,
                    value = isEnabledInOtherPlaces.toString()
                )
            }
        }
    }
}