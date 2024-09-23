package com.chakulafasta.pom.persistence.longterm.deckreviewpreference

import com.chakulafasta.pom.Database
import com.chakulafasta.pom.domain.entity.DeckList
import com.chakulafasta.pom.domain.entity.GlobalState
import com.chakulafasta.pom.persistence.DeckReviewPreferenceDb
import com.chakulafasta.pom.presentation.common.LongTermStateProvider
import com.chakulafasta.pom.presentation.screen.home.DeckReviewPreference
import com.chakulafasta.pom.presentation.screen.home.DeckSorting

class DeckReviewPreferenceProvider(
    private val id: Long,
    private val database: Database,
    private val globalState: GlobalState
) : LongTermStateProvider<DeckReviewPreference> {
    override fun load(): DeckReviewPreference {
        val deckReviewPreferenceDb: DeckReviewPreferenceDb =
            database.deckReviewPreferenceQueries.select(id).executeAsOne()
        val deckList: DeckList? = deckReviewPreferenceDb.deckListId?.let { deckListId: Long ->
            globalState.deckLists.find { deckList: DeckList -> deckList.id == deckListId }
        }
        val deckSorting = DeckSorting(
            deckReviewPreferenceDb.deckSortingCriterion,
            deckReviewPreferenceDb.deckSortingDirection,
            deckReviewPreferenceDb.newDecksFirst
        )
        return DeckReviewPreference(
            deckReviewPreferenceDb.id,
            deckList,
            deckSorting,
            deckReviewPreferenceDb.displayOnlyDecksAvailableForExercise
        )
    }
}