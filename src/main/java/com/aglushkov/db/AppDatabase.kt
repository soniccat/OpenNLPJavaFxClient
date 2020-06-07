package com.aglushkov.db

import com.aglushkov.db.models.Sentence
import com.aglushkov.db.models.SentenceNLP
import com.aglushkov.db.models.TextGroup
import com.aglushkov.extensions.firstLong
import com.aglushkov.model.Resource
import com.aglushkov.model.isLoaded
import com.aglushkov.nlp.NLPCore
import com.aglushkov.nlp.NLPSentence
import com.aglushkov.nlphelper.Database
import com.squareup.sqldelight.db.use
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

class AppDatabase @Inject constructor(
    private val scope: CoroutineScope
) {
    companion object {
        const val nlpSeparator = "&&"

        fun splitNLPString(str: String) = str.split(nlpSeparator)

        fun createNLPSentence(sentence: SentenceNLP, nlpCore: NLPCore): NLPSentence {
            val tokens = sentence.tokens.split(nlpSeparator)
            val tags = sentence.tags.split(nlpSeparator)
            val lemmas = sentence.lemmas.split(nlpSeparator)
            val chunks = sentence.chunks.split(nlpSeparator)

            return NLPSentence(
                tokens.toTypedArray(),
                tags.toTypedArray(),
                lemmas.toTypedArray(),
                chunks.toTypedArray(),
                nlpCore
            )
        }
    }

    private val driver = JdbcSqliteDriver("jdbc:sqlite:mydb.db")
    private var db = Database(driver)

    val context = scope.coroutineContext
    val sentences = Sentences()
    val textGroups = TextGroups()
    val sentencesNLP = SentencesNLP()

    val state = MutableStateFlow<Resource<AppDatabase>>(Resource.Uninitialized())

    init {
        create()
    }

    suspend fun waitUntilInitialized() = state.first { it.isLoaded() }

    fun create() {
        state.value = Resource.Loading(this@AppDatabase)
        scope.launch {
            try {
                createDb()
                state.value = Resource.Loaded(this@AppDatabase)
            } catch (e: Exception) {
                state.value = Resource.Error(e, true)
            }
        }
    }

    private fun createDb() {
        Database.Schema.create(driver)
    }

    inner class Sentences {
        fun insert(sentence: Sentence) = db.sentenceQueries.insertSentence(sentence)
        fun insertedSentenceId() = db.sentenceQueries.lastInsertedRowId().firstLong()

        fun search(text: String) = db.sentenceQueries.search("%${text}%")
        fun selectAll() = db.sentenceQueries.selectAll()
        fun selectAllWithNLP() = db.sentenceQueries.selectAllWithNLP()

        fun removeAll() = db.sentenceQueries.removeAll()
    }

    inner class SentencesNLP {
        fun insert(sentenceId: Long, nlpSentence: NLPSentence) = db.sentenceNLPQueries.insert(
            sentenceId,
            nlpSentence.tokens.joinToString(nlpSeparator),
            nlpSentence.tags.joinToString(nlpSeparator),
            nlpSentence.lemmas.joinToString(nlpSeparator),
            nlpSentence.chunks.joinToString(nlpSeparator)
        )

        fun selectAll() = db.sentenceNLPQueries.selectAll()

        fun removeAll() = db.sentenceNLPQueries.removeAll()
    }

    inner class TextGroups {
        fun insert(textGroup: TextGroup) = db.textGroupQueries.insertTextGroup(textGroup)
        fun insertedTextGroupId() = db.textGroupQueries.lastInsertedRowId().firstLong()

        fun removeAll() = db.textGroupQueries.removeAll()
    }
}