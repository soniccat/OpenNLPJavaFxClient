package com.aglushkov.nlphelper.sentences

import com.aglushkov.db.models.Sentence
import com.aglushkov.model.isLoaded
import com.aglushkov.nlp.NLPCore
import com.aglushkov.nlp.NLPSentence
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

interface SentencesVM {
    val search: MutableStateFlow<String>
    val sentences: MutableStateFlow<List<Sentence>>

    fun onTextChanged(string: String)
}

class SentencesVMImp @Inject constructor(
        private val core: NLPCore,
        private @Named("main") val mainScope: CoroutineScope
): SentencesVM {
    private var sentence: NLPSentence? = null

    override val search = MutableStateFlow("")
    override val sentences = MutableStateFlow<List<Sentence>>(emptyList())

    init {
    }

    override fun onTextChanged(string: String) {
        sentence = NLPSentence(string, core)
    }
}