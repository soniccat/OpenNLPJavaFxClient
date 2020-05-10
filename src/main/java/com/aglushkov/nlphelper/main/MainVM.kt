package com.aglushkov.nlphelper.main

import com.aglushkov.model.Resource
import com.aglushkov.nlp.NLPCore
import com.aglushkov.nlp.NLPSentence
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

interface MainVM {
    val tokens: MutableStateFlow<String>
    val tags: MutableStateFlow<String>
    val lemmas: MutableStateFlow<String>
    val chunks: MutableStateFlow<String>

    fun onTextChanged(string: String)
}

class MainVMImp @Inject constructor(val core: NLPCore): MainVM {
    private var sentence: NLPSentence? = null

    override val tokens = MutableStateFlow("")
    override val tags = MutableStateFlow<String>("")
    override val lemmas = MutableStateFlow<String>("")
    override val chunks = MutableStateFlow<String>("")

    override fun onTextChanged(string: String) {
        sentence = NLPSentence(string, core)
        processSentence()
    }

    private fun processSentence() {
        sentence?.let {
            it.load()
            tokens.value = it.tokens.joinToString(" ")
            tags.value = it.tags.joinToString(" ")
            lemmas.value = it.lemmas.joinToString(" ")
            chunks.value = it.chunks.joinToString(" ")
        }
    }
}