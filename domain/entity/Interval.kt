package com.chakulafasta.pom.domain.entity

import com.chakulafasta.pom.domain.architecturecomponents.FlowMakerWithRegistry
import com.soywiz.klock.DateTimeSpan

class Interval(
    override val id: Long,
    grade: Int,
    value: DateTimeSpan
) : FlowMakerWithRegistry<Interval>() {
    var grade: Int by flowMaker(grade)
    var value: DateTimeSpan by flowMaker(value)

    override fun copy() = Interval(id, grade, value)
}