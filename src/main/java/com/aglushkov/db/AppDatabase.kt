package com.aglushkov.db

import com.aglushkov.db.models.Sentence
import com.aglushkov.extensions.asFlow
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

class AppDatabase(
        private val scope: CoroutineScope
) {
    private val driver = JdbcSqliteDriver("jdbc:sqlite:mydb.db")
    private var db = Database(driver)

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

    fun insert(sentence: Sentence) = db.sentencesQueries.insertSentence(sentence)

    fun selectQueryFlow() = db.sentencesQueries.selectAll()

    fun selectFlow() = flow {
        val query = db.sentencesQueries.selectAll()
        query.execute().use {
            while (it.next()) {
                emit(query.mapper(it))
            }
        }
    }
}