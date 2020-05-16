package com.aglushkov.db

import com.aglushkov.db.models.Sentence
import com.aglushkov.nlp.NLPCore
import java.io.File

class SentenceRepository(
        private val database: AppDatabase,
        private val nlpCore: NLPCore
) {
    suspend fun importBook(file: File) {
        database.waitUntilInitialized()
        nlpCore.waitUntilInitialized()

        val text = file.readText(Charsets.UTF_8)
        nlpCore.sentences(text).forEachIndexed { index, s ->
            database.insert(Sentence.Impl(0, index.toLong(), s))
        }
    }
}