package com.aglushkov.word_relation

import com.aglushkov.db.models.WordRelation
import com.aglushkov.nlp.NLPCore
import com.aglushkov.nlp.NLPSentence
import javax.inject.Inject

class WordRelationEngine @Inject constructor(
    val nlpCore: NLPCore
) {
    /*
    * ""
    **/
    fun findNounAfterVerb(sentence: String, verb: String): List<WordRelation.Impl> {
        val nlpSentence = NLPSentence(sentence, nlpCore)
        return findNounAfterVerb(nlpSentence, verb)
    }

    fun findNounAfterVerb(sentence: NLPSentence, filterVerb: String): List<WordRelation.Impl> {
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
                        verb = sentence.lemmaOrToken(verbIndex)
                        verbTag = tags[verbIndex]
                    }
                }

                if (verb != null && verb.contains(filterVerb)) {
                    val nounSpan = spanList[i + 1]
                    for (nounIndex in nounSpan.start until nounSpan.end) {
                        if (tags[nounIndex].isNoun()) {
                            val relation = WordRelation.Impl(
                                    0,
                                    verb,
                                    verbTag.toString(),
                                    sentence.lemmaOrToken(nounIndex),
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

    // to be able to work with WordRelationEngine in a separate thread
    fun clone(): WordRelationEngine {
        return WordRelationEngine(nlpCore.clone())
    }

    suspend fun waitUntilInitialized() = nlpCore.waitUntilInitialized()
}