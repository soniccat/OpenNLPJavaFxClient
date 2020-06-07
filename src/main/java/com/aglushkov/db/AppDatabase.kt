package com.aglushkov.db

import com.aglushkov.db.models.Sentence
import com.aglushkov.db.models.TextGroup
import com.aglushkov.extensions.firstLong
import com.aglushkov.model.Resource
import com.aglushkov.model.isLoaded
import com.aglushkov.nlphelper.Database
import com.squareup.sqldelight.db.use
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import javax.inject.Inject

class AppDatabase @Inject constructor(
    private val scope: CoroutineScope
) {
    private val driver = JdbcSqliteDriver("jdbc:sqlite:mydb.db")
    private var db = Database(driver)

    val context = scope.coroutineContext
    val sentences = Sentences()
    val textGroups = TextGroups()

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
        fun insert(sentence: Sentence) = db.sentencesQueries.insertSentence(sentence)
        fun insertedSentenceId() = db.sentencesQueries.lastInsertedRowId().firstLong()

        fun search(text: String) = db.sentencesQueries.search("%${text}%")

        fun selectAll() = db.sentencesQueries.selectAll()

        fun removeAll() = db.sentencesQueries.removeAll()
    }

    inner class TextGroups {
        fun insert(textGroup: TextGroup) = db.textGroupQueries.insertTextGroup(textGroup)
        fun insertedTextGroupId() = db.textGroupQueries.lastInsertedRowId().firstLong()

        fun removeAll() = db.textGroupQueries.removeAll()
    }
}