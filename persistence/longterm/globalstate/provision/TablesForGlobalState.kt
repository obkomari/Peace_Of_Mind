package com.chakulafasta.pom.persistence.longterm.globalstate.provision

import com.chakulafasta.pom.Database
import com.chakulafasta.pom.persistence.globalstate.*

class TablesForGlobalState private constructor(
    val deckTable: List<DeckDb>,
    val cardTable: List<CardDb>,
    val exercisePreferenceTable: List<ExercisePreferenceDb>,
    val pronunciationTable: List<PronunciationDb>,
    val intervalSchemeTable: List<Long>,
    val intervalTable: List<IntervalDb>,
    val gradingTable: List<GradingDb>,
    val pronunciationPlanTable: List<PronunciationPlanDb>,
    val sharedExercisePreferenceTable: List<Long>,
    val deckListTable: List<DeckListDb>,
    val keyValueTable: Map<Long, String?>
) {
    companion object {
        fun load(database: Database): TablesForGlobalState {
            return with(database) {
                TablesForGlobalState(
                    deckTable = deckQueries.selectAll().executeAsList(),
                    cardTable = cardQueries.selectAll().executeAsList(),
                    exercisePreferenceTable = exercisePreferenceQueries.selectAll().executeAsList(),
                    pronunciationTable = pronunciationQueries.selectAll().executeAsList(),
                    intervalSchemeTable = intervalSchemeQueries.selectAll().executeAsList(),
                    intervalTable = intervalQueries.selectAll().executeAsList(),
                    gradingTable = gradingQueries.selectAll().executeAsList(),
                    pronunciationPlanTable = pronunciationPlanQueries.selectAll().executeAsList(),
                    sharedExercisePreferenceTable = sharedExercisePreferenceQueries.selectAll().executeAsList(),
                    keyValueTable = keyValueQueries.selectAll().executeAsList().associate { it.key to it.value },
                    deckListTable = deckListQueries.selectAll().executeAsList()
                )
            }
        }
    }
}