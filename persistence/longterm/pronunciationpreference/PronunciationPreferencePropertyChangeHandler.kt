package com.chakulafasta.pom.persistence.longterm.pronunciationpreference

import com.chakulafasta.pom.Database
import com.chakulafasta.pom.domain.architecturecomponents.PropertyChangeRegistry.Change
import com.chakulafasta.pom.domain.architecturecomponents.PropertyChangeRegistry.Change.PropertyValueChange
import com.chakulafasta.pom.persistence.DbKeys
import com.chakulafasta.pom.persistence.longterm.PropertyChangeHandler
import com.chakulafasta.pom.persistence.setOfLocalesAdapter
import com.chakulafasta.pom.presentation.screen.pronunciation.PronunciationPreference
import java.util.*

class PronunciationPreferencePropertyChangeHandler(
    database: Database
) : PropertyChangeHandler {
    private val queries = database.keyValueQueries

    override fun handle(change: Change) {
        if (change !is PropertyValueChange) return
        when (change.property) {
            PronunciationPreference::favoriteLanguages -> {
                val favoriteLanguages = change.newValue as Set<Locale>
                queries.replace(
                    key = DbKeys.FAVORITE_LANGUAGES,
                    value = setOfLocalesAdapter.encode(favoriteLanguages)
                )
            }
        }
    }
}