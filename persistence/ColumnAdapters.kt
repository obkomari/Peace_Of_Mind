package com.chakulafasta.pom.persistence

import com.chakulafasta.pom.domain.entity.PronunciationEvent
import com.chakulafasta.pom.domain.entity.PronunciationEvent.*
import com.soywiz.klock.*
import com.squareup.sqldelight.ColumnAdapter
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.util.*

val localeAdapter = object : ColumnAdapter<Locale, String> {
    override fun encode(value: Locale): String {
        return value.toLanguageTag()
    }

    override fun decode(databaseValue: String): Locale {
        return Locale.forLanguageTag(databaseValue)
    }
}

val setOfLocalesAdapter = object : ColumnAdapter<Set<Locale>, String> {
    private val SEPARATOR = ";"

    override fun encode(value: Set<Locale>): String {
        return value.joinToString(SEPARATOR) { locale: Locale -> locale.toLanguageTag() }
    }

    override fun decode(databaseValue: String): Set<Locale> {
        return databaseValue.split(SEPARATOR).mapNotNull { chunk: String ->
            try {
                Locale.forLanguageTag(chunk)
            } catch (e: Exception) {
                null
            }
        }
            .toSet()
    }
}

val dateTimeAdapter = object : ColumnAdapter<DateTime, Long> {
    override fun encode(value: DateTime): Long = value.unixMillisLong
    override fun decode(databaseValue: Long): DateTime = DateTime.fromUnix(databaseValue)
}

val dateTimeSpanAdapter = object : ColumnAdapter<DateTimeSpan, String> {
    override fun encode(value: DateTimeSpan): String {
        return "${value.monthSpan.totalMonths}|${value.timeSpan.millisecondsLong}"
    }

    override fun decode(databaseValue: String): DateTimeSpan {
        val chunks = databaseValue.split("|")
        val totalMonths: Int = chunks[0].toInt()
        val monthSpan = MonthSpan(totalMonths)
        val milliseconds: Double = chunks[1].toDouble()
        val timeSpan = TimeSpan(milliseconds)
        return DateTimeSpan(monthSpan, timeSpan)
    }
}

val pronunciationEventsAdapter = object : ColumnAdapter<List<PronunciationEvent>, String> {
    override fun encode(value: List<PronunciationEvent>): String {
        return value.joinToString { pronunciationEvent: PronunciationEvent ->
            when (pronunciationEvent) {
                SpeakQuestion -> "SPEAK_QUESTION"
                SpeakAnswer -> "SPEAK_ANSWER"
                is Delay -> pronunciationEvent.timeSpan.seconds.toInt().toString()
            }
        }
    }

    override fun decode(databaseValue: String): List<PronunciationEvent> {
        return databaseValue.split(", ")
            .map { chunk: String ->
                when (chunk) {
                    "SPEAK_QUESTION" -> SpeakQuestion
                    "SPEAK_ANSWER" -> SpeakAnswer
                    else -> {
                        val timeSpan: TimeSpan = chunk.toInt().seconds
                        Delay(timeSpan)
                    }
                }
            }
    }
}

@Serializable
data class SerializableStringArray(
    val stringArray: Array<String?>
)

val stringArrayAdapter = object : ColumnAdapter<Array<String?>, String> {
    override fun encode(value: Array<String?>): String {
        val serializable = SerializableStringArray(value)
        return Json.encodeToString(SerializableStringArray.serializer(), serializable)
    }

    override fun decode(databaseValue: String): Array<String?> {
        val serializable = try {
            Json.decodeFromString(SerializableStringArray.serializer(), databaseValue)
        } catch (e: SerializationException) {
            return arrayOf()
        }
        return serializable.stringArray
    }
}

val setOfLongAdapter = object : ColumnAdapter<Set<Long>, String> {
    private val SEPARATOR = ";"

    override fun encode(value: Set<Long>): String {
        return value.joinToString(SEPARATOR, transform = Long::toString)
    }

    override fun decode(databaseValue: String): Set<Long> {
        return databaseValue.split(SEPARATOR)
            .mapNotNull { chunk: String -> if (chunk.isEmpty()) null else chunk.toLong() }
            .toSet()
    }
}