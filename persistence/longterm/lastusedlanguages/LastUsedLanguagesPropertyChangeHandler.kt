package com.chakulafasta.pom.persistence.longterm.lastusedlanguages

import com.chakulafasta.pom.Database
import com.chakulafasta.pom.domain.architecturecomponents.PropertyChangeRegistry.Change
import com.chakulafasta.pom.domain.architecturecomponents.PropertyChangeRegistry.Change.PropertyValueChange
import com.chakulafasta.pom.persistence.DbKeys
import com.chakulafasta.pom.persistence.localeAdapter
import com.chakulafasta.pom.persistence.longterm.PropertyChangeHandler
import com.chakulafasta.pom.presentation.common.SpeakerImpl.LastUsedLanguages
import java.util.*

class LastUsedLanguagesPropertyChangeHandler(
    database: Database
) : PropertyChangeHandler {
    private val queries = database.keyValueQueries

    override fun handle(change: Change) {
        if (change !is PropertyValueChange) return
        when (change.property) {
            LastUsedLanguages::language1 -> {
                val language1 = change.newValue as Locale?
                language1 ?: return
                queries.replace(
                    key = DbKeys.LAST_USED_LANGUAGE_1,
                    value = localeAdapter.encode(language1)
                )
            }
            LastUsedLanguages::language2 -> {
                val language2 = change.newValue as Locale?
                language2 ?: return
                queries.replace(
                    key = DbKeys.LAST_USED_LANGUAGE_2,
                    value = localeAdapter.encode(language2)
                )
            }
        }
    }
}