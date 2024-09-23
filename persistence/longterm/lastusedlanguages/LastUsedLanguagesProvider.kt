package com.chakulafasta.pom.persistence.longterm.lastusedlanguages

import com.chakulafasta.pom.Database
import com.chakulafasta.pom.persistence.DbKeys
import com.chakulafasta.pom.persistence.localeAdapter
import com.chakulafasta.pom.presentation.common.LongTermStateProvider
import com.chakulafasta.pom.presentation.common.SpeakerImpl.LastUsedLanguages
import java.util.*

class LastUsedLanguagesProvider(
    private val database: Database
) : LongTermStateProvider<LastUsedLanguages> {
    override fun load(): LastUsedLanguages {
        val keyValues: Map<Long, String?> = database.keyValueQueries
            .selectValues(
                keys = listOf(
                    DbKeys.LAST_USED_LANGUAGE_1,
                    DbKeys.LAST_USED_LANGUAGE_2
                )
            )
            .executeAsList()
            .associate { (key, value) -> key to value }
        val language1: Locale? = keyValues[DbKeys.LAST_USED_LANGUAGE_1]?.let(localeAdapter::decode)
        val language2: Locale? = keyValues[DbKeys.LAST_USED_LANGUAGE_2]?.let(localeAdapter::decode)
        return LastUsedLanguages(language1, language2)
    }
}