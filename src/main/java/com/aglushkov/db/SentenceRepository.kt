package com.aglushkov.db

import com.aglushkov.db.models.Sentence
import com.aglushkov.db.models.TextGroup
import com.aglushkov.extensions.asFlow
import com.aglushkov.extensions.readAsFlow
import com.aglushkov.nlp.NLPCore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.collect
import java.util.*

class SentenceRepository(
    private val database: AppDatabase,
    private val nlpCore: NLPCore
) {
    suspend fun importText(name: String, text: String) {
        waitUntilinitialized()

        val textGroupId = database.textGroups.run {
            insert(TextGroup.Impl(0, name, Date().time ))
            insertedTextGroupId()
        } ?: 0

        nlpCore.sentences(text).forEachIndexed { index, s ->
            val withoutNewLine = s.replace(13.toChar(), ' ')
                    .replace(10.toChar(), ' ')
                    .replace(9.toChar(), ' ')
                    .replace(11.toChar(), ' ')
                    .replace(3.toChar(), ' ')
                    .replace("``", "\"")
                    .replace("''", "\"")
                    .trim()
            database.sentences.insert(Sentence.Impl(0, textGroupId, index.toLong(), withoutNewLine))
        }
    }

    suspend fun searchSentences(word: String): Flow<Sentence> {
        waitUntilinitialized()
        return database.sentences.search(word).readAsFlow()
    }

    suspend fun loadSentences(): Flow<Sentence> {
        waitUntilinitialized()
        return database.sentences.selectAll().readAsFlow()
    }

    private suspend fun waitUntilinitialized() {
        database.waitUntilInitialized()
        nlpCore.waitUntilInitialized()
    }
}