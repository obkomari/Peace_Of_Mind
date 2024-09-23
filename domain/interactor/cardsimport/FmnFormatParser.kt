package com.chakulafasta.pom.domain.interactor.cardsimport

class FmnFormatParser : Parser() {
    private val cardBlockSeparatorRegex = Regex("""\n+(?=[[:blank:]]*Q:[[:blank:]]*\n)""")
    private val cardRegex = Regex(
        """\s*Q:[[:blank:]]*\n(((?!^[[:blank:]]*[QA]:[[:blank:]]*${'$'})[\s\S])+)\n[[:blank:]]*A:[[:blank:]]*\n(((?!^[[:blank:]]*[QA]:[[:blank:]]*${'$'})[\s\S])+)""",
        RegexOption.MULTILINE
    )
    private val cardContentRegex = Regex("""[[:blank:]]*[\S]([\s\S]*[\S]|)""")

    private lateinit var text: String
    private val cardMarkups: MutableList<CardMarkup> = ArrayList()
    private val errors: MutableList<Error> = ArrayList()
    private val newLineCharLocations: MutableList<Int> = ArrayList()

    override fun parse(text: String): ParserResult {
        if (text.isEmpty()) return ParserResult(emptyList(), emptyList())
        this.text = text
        cardMarkups.clear()
        errors.clear()
        newLineCharLocations.clear()
        text.forEachIndexed { index, ch ->
            if (ch == '\n') {
                newLineCharLocations.add(index)
            }
        }
        val separatorMatches: List<MatchResult> = cardBlockSeparatorRegex.findAll(text).toList()
        var cardBlockStartIndex = 0
        var cardBlockEndIndex: Int
        repeat(separatorMatches.size) { i: Int ->
            if (i != 0) {
                cardBlockStartIndex = separatorMatches[i - 1].range.last + 1
            }
            cardBlockEndIndex = separatorMatches[i].range.first - 1
            parseCardBlock(cardBlockStartIndex, cardBlockEndIndex)
        }
        cardBlockStartIndex =
            if (separatorMatches.isNotEmpty()) {
                minOf(
                    separatorMatches.last().range.last + 1,
                    text.lastIndex
                )
            } else {
                0
            }
        cardBlockEndIndex = text.lastIndex
        parseCardBlock(cardBlockStartIndex, cardBlockEndIndex)
        return ParserResult(cardMarkups, errors)
    }

    private fun parseCardBlock(cardBlockStartIndex: Int, cardBlockEndIndex: Int) {
        val cardBlock: String = text.substring(cardBlockStartIndex..cardBlockEndIndex)
        if (cardBlock.isBlank()) return
        if (!cardBlock.matches(cardRegex)) {
            val errorMessage = "Invalid record"
            val errorRange = cardBlockStartIndex..cardBlockEndIndex
            val error = Error(errorMessage, errorRange)
            errors.add(error)
            return
        }
        val cardMatchResult = cardRegex.find(cardBlock)!!

        val questionGroup: MatchGroup = cardMatchResult.groups[1]!!
        val questionMatchResult = cardContentRegex.find(questionGroup.value)
        if (questionMatchResult == null) {
            val errorMessage = "Cannot find question"
            val errorRange = cardBlockStartIndex..cardBlockEndIndex
            val error = Error(errorMessage, errorRange)
            errors.add(error)
            return
        }

        val answerGroup: MatchGroup = cardMatchResult.groups[3]!!
        val answerMatchResult = cardContentRegex.find(answerGroup.value)
        if (answerMatchResult == null) {
            val errorMessage = "Cannot find answer"
            val errorRange = cardBlockStartIndex..cardBlockEndIndex
            val error = Error(errorMessage, errorRange)
            errors.add(error)
            return
        }

        val questionStart: Int =
            cardBlockStartIndex + questionGroup.range.first + questionMatchResult.range.first
        val questionEnd: Int =
            cardBlockStartIndex + questionGroup.range.first + questionMatchResult.range.last
        val questionRange = questionStart..questionEnd
        val questionText = text.substring(questionRange)

        val answerStart: Int =
            cardBlockStartIndex + answerGroup.range.first + answerMatchResult.range.first
        val answerEnd: Int =
            cardBlockStartIndex + answerGroup.range.first + answerMatchResult.range.last
        val answerRange = answerStart..answerEnd
        val answerText = text.substring(answerRange)

        val cardPrototype = CardMarkup(
            questionText,
            questionRange,
            answerText,
            answerRange
        )
        cardMarkups.add(cardPrototype)
    }
}