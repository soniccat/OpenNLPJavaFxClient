package com.aglushkov.nlphelper.main

import com.aglushkov.model.Resource
import com.aglushkov.nlp.NLPCore
import com.aglushkov.nlp.NLPSentence
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

interface MainVM {
    val text: MutableStateFlow<String>
    val tokens: MutableStateFlow<String>
    val tags: MutableStateFlow<String>
    val lemmas: MutableStateFlow<String>
    val chunks: MutableStateFlow<String>

    fun onTextChanged(string: String)
}

class MainVMImp @Inject constructor(private val core: NLPCore): MainVM {
    private var sentence: NLPSentence? = null

    override val text = MutableStateFlow<String>("")
    override val tokens = MutableStateFlow("")
    override val tags = MutableStateFlow<String>("")
    override val lemmas = MutableStateFlow<String>("")
    override val chunks = MutableStateFlow<String>("")

    init {
        // Test data
//
//        mainScope.launch {
//            delay(1500)
//            text.text = "Many people use the models directly in their Java code by creating SentenceDetector and Tokenizer objects and calling their methods as appropriate."
//        }
    }

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