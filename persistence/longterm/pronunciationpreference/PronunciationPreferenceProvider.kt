package com.chakulafasta.pom.persistence.longterm.pronunciationpreference

import com.chakulafasta.pom.Database
import com.chakulafasta.pom.persistence.DbKeys
import com.chakulafasta.pom.persistence.setOfLocalesAdapter
import com.chakulafasta.pom.presentation.common.LongTermStateProvider
import com.chakulafasta.pom.presentation.screen.pronunciation.PronunciationPreference
import java.util.*

class PronunciationPreferenceProvider(
    private val database: Database
) : LongTermStateProvider<PronunciationPreference> {
    override fun load(): PronunciationPreference {
        val favoriteLanguages: Set<Locale> = database.keyValueQueries
            .selectValue(DbKeys.FAVORITE_LANGUAGES)
            .executeAsOneOrNull()
            ?.value
            ?.let(setOfLocalesAdapter::decode)
            ?: emptySet()
        return PronunciationPreference(favoriteLanguages)
    }
}