package com.aglushkov.db

import com.aglushkov.db.models.Sentence
import com.aglushkov.db.models.TextGroup
import com.aglushkov.nlp.NLPCore
import java.util.*

class SentenceRepository(
    private val database: AppDatabase,
    private val nlpCore: NLPCore
) {
    suspend fun importText(name: String, text: String) {
        database.waitUntilInitialized()
        nlpCore.waitUntilInitialized()

        val textGroupId = database.textGroups.run {
            insert(TextGroup.Impl(0, name, Date().time ))
            insertedTextGroupId()
        } ?: 0

        nlpCore.sentences(text).forEachIndexed { index, s ->
            val withoutNewLine = s.replace('\n', ' ')
            database.sentences.insert(Sentence.Impl(0, textGroupId, index.toLong(), withoutNewLine))
        }
    }
}