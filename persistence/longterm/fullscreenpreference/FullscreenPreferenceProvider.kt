package com.chakulafasta.pom.persistence.longterm.fullscreenpreference

import com.chakulafasta.pom.Database
import com.chakulafasta.pom.persistence.DbKeys
import com.chakulafasta.pom.presentation.common.LongTermStateProvider
import com.chakulafasta.pom.presentation.common.entity.FullscreenPreference

class FullscreenPreferenceProvider(
    private val database: Database
) : LongTermStateProvider<FullscreenPreference> {
    override fun load(): FullscreenPreference {
        val keyValues: Map<Long, String?> = database.keyValueQueries
            .selectValues(
                keys = listOf(
                    DbKeys.IS_FULLSCREEN_ENABLED_IN_EXERCISE,
                    DbKeys.IS_FULLSCREEN_ENABLED_IN_CARD_PLAYER,
                    DbKeys.IS_FULLSCREEN_ENABLED_IN_OTHER_PLACES
                )
            )
            .executeAsList()
            .associate { (key, value) -> key to value }
        val isEnabledInExercise: Boolean = keyValues[DbKeys.IS_FULLSCREEN_ENABLED_IN_EXERCISE]
            ?.toBoolean()
            ?: FullscreenPreference.DEFAULT_IS_ENABLED_IN_EXERCISE
        val isEnabledInCardPlayer: Boolean = keyValues[DbKeys.IS_FULLSCREEN_ENABLED_IN_CARD_PLAYER]
            ?.toBoolean()
            ?: FullscreenPreference.DEFAULT_IS_ENABLED_IN_CARD_PLAYER
        val isEnabledInOtherPlaces: Boolean = keyValues[DbKeys.IS_FULLSCREEN_ENABLED_IN_OTHER_PLACES]
            ?.toBoolean()
            ?: FullscreenPreference.DEFAULT_IS_ENABLED_IN_OTHER_PLACES
        return FullscreenPreference(
            isEnabledInExercise,
            isEnabledInCardPlayer,
            isEnabledInOtherPlaces
        )
    }
}