package com.aglushkov.nlphelper.sentences

import com.aglushkov.db.AppDatabase
import com.aglushkov.db.SentenceRepository
import com.aglushkov.db.models.Sentence
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Named

interface SentencesVM {
    val search: MutableStateFlow<String>
    val sentences: MutableStateFlow<List<Sentence>>

    fun onImportPressed(name: String, text: String)
    fun onSearchChanged(string: String)
}

class SentencesVMImp @Inject constructor(
        private val database: AppDatabase,
        private val sentenceRepository: SentenceRepository,
        private @Named("main") val mainScope: CoroutineScope
): SentencesVM {
    private var query: String = ""

    override val search = MutableStateFlow("")
    override val sentences = MutableStateFlow<List<Sentence>>(emptyList())

    init {
        load("")
    }

    override fun onImportPressed(name: String, text: String) {
        mainScope.launch {
            withContext(database.context) {
                sentenceRepository.importText(name, text)
            }
        }
    }

    override fun onSearchChanged(query: String) {
        this.query = query
        load(query)
    }

    private fun load(query: String) {
        mainScope.launch {
            val list = withContext(database.context) {
                database.sentences.search(query).executeAsList()
            }

            sentences.value = list
        }
    }
}