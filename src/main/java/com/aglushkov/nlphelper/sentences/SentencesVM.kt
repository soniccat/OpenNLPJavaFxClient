package com.aglushkov.nlphelper.sentences

import com.aglushkov.db.AppDatabase
import com.aglushkov.db.SentenceRepository
import com.aglushkov.db.models.Sentence
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.charset.Charset
import javax.inject.Inject
import javax.inject.Named

interface SentencesVM {
    val sentences: MutableStateFlow<List<Sentence>>

    fun onImportDirectory(file: File)
    fun onImportPressed(name: String, text: String)
    fun onSearchChanged(string: String)
}

class SentencesVMImp @Inject constructor(
        private val database: AppDatabase,
        private val sentenceRepository: SentenceRepository,
        private @Named("main") val mainScope: CoroutineScope
): SentencesVM {
    private var query: String = ""
    override val sentences = MutableStateFlow<List<Sentence>>(emptyList())

    init {
        load("")
    }

    override fun onImportDirectory(file: File) {
        importDirectory(file)
    }

    private fun importDirectory(file: File) {
        val files = file.listFiles { dir, name ->
            true
        }

        for (f in files) {
            if (f.isDirectory) {
                importDirectory(f)
            } else if (f.length() < 1024 * 1024 * 5) {
                val text = f.readText(Charsets.UTF_8)
                importText(f.name, text)
            }
        }
    }

    override fun onImportPressed(name: String, text: String) {
        importText(name, text)
    }

    private fun importText(name: String, text: String) {
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