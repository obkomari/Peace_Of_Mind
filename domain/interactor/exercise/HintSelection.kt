package com.chakulafasta.pom.domain.interactor.exercise

import kotlinx.serialization.Serializable

@Serializable
data class HintSelection(
    val startIndex: Int,
    val endIndex: Int
)