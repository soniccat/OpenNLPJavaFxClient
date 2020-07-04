package com.aglushkov.word_relation

import com.aglushkov.db.models.WordRelation
import com.aglushkov.nlp.NLPCore
import com.aglushkov.nlp.NLPSentence
import javax.inject.Inject

class WordRelationEngine @Inject constructor(
    val nlpCore: NLPCore
) {
    fun findNounAfterVerb(sentence: String, verb: String): List<WordRelation.Impl> {
        val nlpSentence = NLPSentence(sentence, nlpCore)
        return findNounAfterVerb(nlpSentence, verb)
    }

    fun findNounAfterVerb(sentence: NLPSentence, filterVerb: String): List<WordRelation.Impl> {
        val result = mutableListOf<WordRelation.Impl>()
        val tags = sentence.tagEnums()
        val spanList = sentence.spanList()

        var i = 0
        while (i < spanList.size) {
            val span = spanList[i]
            val verbIndex = IntRange(span.start, span.end - 1).firstOrNull {
                sentence.lemmaOrToken(it).contains(filterVerb)
            }

            if (verbIndex == null) { ++i; continue }

            // Ex: Trevor takes care of the rest of the garden
            if (span.type.isVerbPhrase() && tags[verbIndex].isVerb() &&
                i + 1 < spanList.size && spanList[i + 1].type.isNounPhrase()) {

                val nounSpan = spanList[i + 1]
                IntRange(nounSpan.start, nounSpan.end - 1).firstOrNull {
                    tags[it].isNoun()
                }?.let { nounIndex ->
                    val relation = WordRelation.Impl(
                        0,
                        sentence.lemmaOrToken(verbIndex),
                        tags[verbIndex].toString(),
                        sentence.lemmaOrToken(nounIndex),
                        tags[nounIndex].toString()
                    )
                    result.add(relation)
                    ++i
                }

            // Ex: Take care to avoid needless misunderstandings
            } else if (span.type.isNounPhrase()) {
                IntRange(verbIndex + 1, span.end - 1).firstOrNull {
                    tags[it].isNoun()
                }?.let { nounIndex ->
                    val relation = WordRelation.Impl(
                        0,
                        sentence.lemmaOrToken(verbIndex),
                        tags[verbIndex].toString(),
                        sentence.lemmaOrToken(nounIndex),
                        tags[nounIndex].toString()
                    )
                    result.add(relation)
                }
            }

            ++i
        }

        return result
    }

    fun findPrepAfterVerb(sentence: NLPSentence, filterVerb: String): List<WordRelation.Impl> {
        val result = mutableListOf<WordRelation.Impl>()
        val tags = sentence.tagEnums()
        val spanList = sentence.spanList()

        var i = 0
        while (i < spanList.size) {
            val span = spanList[i]
            val verbIndex = IntRange(span.start, span.end - 1).firstOrNull {
                sentence.lemmaOrToken(it).contains(filterVerb)
            }

            if (verbIndex == null) { ++i; continue }

            if (span.type.isVerbPhrase() && tags[verbIndex].isVerb()) {
                var prepSpan: NLPCore.Span? = null

                // Ex: after being taken into custody by members
                if (i + 1 < spanList.size && spanList[i + 1].type.isPrepositionalPhrase()) {
                    prepSpan = spanList[i + 1]

                // Ex: His explorations took him into deserts and marshes
                } else if (i + 2 < spanList.size && spanList[i + 2].type.isPrepositionalPhrase()) {
                    prepSpan = spanList[i + 2]
                }

                if (prepSpan != null) {
                    IntRange(prepSpan.start, prepSpan.end - 1).firstOrNull {
                        tags[it].isPrep()
                    }?.let { nounIndex ->
                        val relation = WordRelation.Impl(
                            0,
                            sentence.lemmaOrToken(verbIndex),
                            tags[verbIndex].toString(),
                            sentence.lemmaOrToken(nounIndex),
                            tags[nounIndex].toString()
                        )
                        result.add(relation)
                        ++i
                    }

                    ++i
                }
            }

            ++i
        }

        return result
    }

    // to be able to work with WordRelationEngine in a separate thread
    fun clone(): WordRelationEngine {
        return WordRelationEngine(nlpCore.clone())
    }

    suspend fun waitUntilInitialized() = nlpCore.waitUntilInitialized()
}