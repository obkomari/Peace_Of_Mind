package com.chakulafasta.pom.domain.architecturecomponents

import kotlinx.coroutines.flow.Flow

interface Flowable<out T> {
    fun asFlow(): Flow<T>
}