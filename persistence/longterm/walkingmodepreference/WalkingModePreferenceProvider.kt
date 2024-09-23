package com.chakulafasta.pom.persistence.longterm.walkingmodepreference

import com.chakulafasta.pom.Database
import com.chakulafasta.pom.presentation.common.LongTermStateProvider
import com.chakulafasta.pom.presentation.screen.walkingmodesettings.KeyGesture
import com.chakulafasta.pom.presentation.screen.walkingmodesettings.KeyGestureAction
import com.chakulafasta.pom.presentation.screen.walkingmodesettings.WalkingModePreference

class WalkingModePreferenceProvider(
    private val database: Database
) : LongTermStateProvider<WalkingModePreference> {
    override fun load(): WalkingModePreference {
        lateinit var keyGestureMap: Map<KeyGesture, KeyGestureAction>
        database.transaction {
            keyGestureMap = database.keyGestureMapQueries
                .selectAll()
                .executeAsList()
                .associate { it.keyGesture to it.keyGestureAction }
            }
        return if (keyGestureMap.isEmpty()) {
            WalkingModePreference()
        } else {
            WalkingModePreference(keyGestureMap)
        }
    }
}