package com.chakulafasta.pom.persistence.longterm.tipstate

import com.chakulafasta.pom.Database
import com.chakulafasta.pom.presentation.screen.deckeditor.decksettings.Tip
import com.chakulafasta.pom.presentation.screen.deckeditor.decksettings.TipState

class TipStateProvider(
    private val database: Database
) {
    fun load() {
        database.tipStateQueries.selectAll(::TipState).executeAsList()
            .forEach { loadedTipState: TipState ->
                enumValues<Tip>().find { it.state.id == loadedTipState.id }
                    ?.state?.run {
                        needToShow = loadedTipState.needToShow
                        lastShowedAt = loadedTipState.lastShowedAt
                    }
            }
    }
}