package com.chakulafasta.pom.domain.interactor.searcher

import com.chakulafasta.pom.domain.entity.Card
import com.chakulafasta.pom.domain.entity.Deck

data class FoundCard(
    val card: Card,
    val deck: Deck,
    val searchText: String
)