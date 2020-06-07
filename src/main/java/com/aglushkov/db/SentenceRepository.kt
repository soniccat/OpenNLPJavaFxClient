package com.aglushkov.db

import com.aglushkov.db.models.Sentence
import com.aglushkov.db.models.TextGroup
import com.aglushkov.extensions.readAsFlow
import com.aglushkov.nlp.NLPCore
import kotlinx.coroutines.flow.Flow
import java.lang.Exception
import java.util.*

class SentenceRepository(
    private val database: AppDatabase,
    private val nlpCore: NLPCore
) {
    private var nlpCoreCopy: NLPCore? = null

    suspend fun importText(name: String, text: String) {
        waitUntilInitialized()

        val textGroupId = database.textGroups.run {
            insert(TextGroup.Impl(0, name, Date().time ))
            insertedTextGroupId()
        } ?: 0

        val resultText = text.replace(13.toChar(), ' ')
                .replace(10.toChar(), ' ')
                .replace(9.toChar(), ' ')
                .replace(11.toChar(), ' ')
                .replace(13.toChar(), ' ')
                .replace(3.toChar(), ' ')
                .replace("``", "\"")
                .replace("''", "\"")
                .trim()
        try {
            nlpCoreCopy!!.sentences(resultText).forEachIndexed { index, s ->
                database.sentences.insert(Sentence.Impl(0, textGroupId, index.toLong(), s))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            println(resultText)
        }
    }

    suspend fun searchSentences(word: String): Flow<Sentence> {
        waitUntilInitialized()
        return database.sentences.search(word).readAsFlow()
    }

    suspend fun loadSentences(): Flow<Sentence> {
        waitUntilInitialized()
        return database.sentences.selectAll().readAsFlow()
    }

    suspend fun deleteAll() {
        waitUntilInitialized()
        database.sentences.removeAll()
        database.textGroups.removeAll()
    }

    private suspend fun waitUntilInitialized() {
        database.waitUntilInitialized()
        nlpCore.waitUntilInitialized()
        nlpCoreCopy = nlpCore.clone()
    }
}