package com.chakulafasta.pom.persistence.shortterm

import com.chakulafasta.pom.Database
import com.chakulafasta.pom.persistence.shortterm.HelpArticleScreenStateProvider.SerializableState
import com.chakulafasta.pom.presentation.screen.helparticle.HelpArticle
import com.chakulafasta.pom.presentation.screen.helparticle.HelpArticleScreenState
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class HelpArticleScreenStateProvider(
    json: Json,
    database: Database,
    override val key: String = HelpArticleScreenState::class.qualifiedName!!
) : BaseSerializableStateProvider<HelpArticleScreenState, SerializableState>(
    json,
    database
) {
    @Serializable
    data class SerializableState(
        val currentArticle: HelpArticle
    )

    override val serializer = SerializableState.serializer()

    override fun toSerializable(state: HelpArticleScreenState) = SerializableState(
        state.currentArticle
    )

    override fun toOriginal(serializableState: SerializableState) = HelpArticleScreenState(
        serializableState.currentArticle
    )
}