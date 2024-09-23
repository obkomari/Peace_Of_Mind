package com.chakulafasta.pom.persistence.longterm.cardappearance

import com.chakulafasta.pom.Database
import com.chakulafasta.pom.domain.architecturecomponents.PropertyChangeRegistry
import com.chakulafasta.pom.domain.architecturecomponents.PropertyChangeRegistry.Change.PropertyValueChange
import com.chakulafasta.pom.persistence.DbKeys
import com.chakulafasta.pom.persistence.longterm.PropertyChangeHandler
import com.chakulafasta.pom.presentation.screen.cardappearance.CardAppearance
import com.chakulafasta.pom.presentation.screen.cardappearance.CardTextAlignment

class CardAppearancePropertyChangeHandler(
    database: Database
) : PropertyChangeHandler {
    private val queries = database.keyValueQueries

    override fun handle(change: PropertyChangeRegistry.Change) {
        if (change !is PropertyValueChange) return
        when (change.property) {
            CardAppearance::questionTextAlignment -> {
                val questionTextAlignment = change.newValue as CardTextAlignment
                queries.replace(
                    key = DbKeys.QUESTION_TEXT_ALIGNMENT,
                    value = questionTextAlignment.name
                )
            }
            CardAppearance::questionTextSize -> {
                val questionTextSize = change.newValue as Int
                queries.replace(
                    key = DbKeys.QUESTION_TEXT_SIZE,
                    value = questionTextSize.toString()
                )
            }
            CardAppearance::answerTextAlignment -> {
                val answerTextAlignment = change.newValue as CardTextAlignment
                queries.replace(
                    key = DbKeys.ANSWER_TEXT_ALIGNMENT,
                    value = answerTextAlignment.name
                )
            }
            CardAppearance::answerTextSize -> {
                val answerTextSize = change.newValue as Int
                queries.replace(
                    key = DbKeys.ANSWER_TEXT_SIZE,
                    value = answerTextSize.toString()
                )
            }
            CardAppearance::textOpacityInLightTheme -> {
                val textOpacityInLightTheme = change.newValue as Float
                queries.replace(
                    key = DbKeys.CARD_TEXT_OPACITY_IN_LIGHT_THEME,
                    value = textOpacityInLightTheme.toString()
                )
            }
            CardAppearance::textOpacityInDarkTheme -> {
                val textOpacityInDarkTheme = change.newValue as Float
                queries.replace(
                    key = DbKeys.CARD_TEXT_OPACITY_IN_DARK_THEME,
                    value = textOpacityInDarkTheme.toString()
                )
            }
        }
    }
}