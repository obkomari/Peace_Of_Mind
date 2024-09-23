package com.chakulafasta.pom.domain.interactor.cardsimport

data class ImportedCardsFile(
    val fileName: String,
    val content: ByteArray
)