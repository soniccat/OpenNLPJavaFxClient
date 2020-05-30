package com.aglushkov.word_relation

import com.aglushkov.db.models.WordRelation
import com.aglushkov.nlp.NLPCore
import com.aglushkov.nlp.NLPSentence
import javax.inject.Inject

interface WordRelationEngine {
    fun findNounAfterVerb(sentence: String): List<WordRelation.Impl>
}

class WordRelationEngineImpl @Inject constructor(
    val nlpCore: NLPCore
): WordRelationEngine {
    /*
    * ""
    **/
    override fun findNounAfterVerb(sentence: String): List<WordRelation.Impl> {
        val nlpSentence = NLPSentence(sentence, nlpCore).apply {
            load()
        }

        return findNounAfterVerb(nlpSentence)
    }

    fun findNounAfterVerb(sentence: NLPSentence): List<WordRelation.Impl> {
        val result = mutableListOf<WordRelation.Impl>()
        val tags = sentence.tagEnums()
        val spanList = sentence.spanList()

        var i = 0
        while(i < spanList.size) {
            val span = spanList[i]
            if (span.type.isVerbPhrase() && i + 1 < spanList.size && spanList[i + 1].type.isNounPhrase()) {
                var verb: String? = null
                var verbTag: NLPCore.Tag = NLPCore.Tag.UNKNOWN
                for (verbIndex in span.start until span.end) {
                    if (tags[verbIndex].isVerb()) {
                        verb = sentence.lemmas[verbIndex]
                        verbTag = tags[verbIndex]
                    }
                }

                if (verb != null) {
                    val nounSpan = spanList[i + 1]
                    for (nounIndex in nounSpan.start until nounSpan.end) {
                        if (tags[nounIndex].isNoun()) {
                            val relation = WordRelation.Impl(
                                    0,
                                    verb,
                                    verbTag.toString(),
                                    sentence.lemmas[nounIndex],
                                    tags[nounIndex].toString()
                            )
                            result.add(relation)
                        }
                    }
                }

                ++i
            }

            ++i
        }

        return result
    }

    fun findNounPhrase(startIndex: Int, sentence: NLPSentence): IntRange {
        return IntRange.EMPTY
    }
}