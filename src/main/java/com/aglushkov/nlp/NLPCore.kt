package com.aglushkov.nlp

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import opennlp.tools.chunker.ChunkerME
import opennlp.tools.chunker.ChunkerModel
import opennlp.tools.lemmatizer.DictionaryLemmatizer
import opennlp.tools.postag.POSModel
import opennlp.tools.postag.POSTaggerME
import opennlp.tools.sentdetect.SentenceDetectorME
import opennlp.tools.sentdetect.SentenceModel
import opennlp.tools.tokenize.TokenizerME
import opennlp.tools.tokenize.TokenizerModel
import resources.Resources
import java.io.FileInputStream

class NLPCore(val scope: CoroutineScope) {
    val state = MutableStateFlow<Resource<NLPCore>>(Resource.Uninitialized())

    private var sentenceDetector: SentenceDetectorME? = null
    private var tokenizer: TokenizerME? = null
    private var tagger: POSTaggerME? = null
    private var lemmatizerModel: DictionaryLemmatizer? = null
    private var chunker: ChunkerME? = null

    init {
        load()
    }

    private fun load() {
        state.value = Resource.Loading(this@NLPCore)
        scope.launch {
            try {
                loadModels()
                state.value = Resource.Loaded(this@NLPCore)
            } catch (e: Exception) {
                state.value = Resource.Error(e, true)
            }
        }
    }

    private fun loadModels() {
        Resources.modelAsStream("en_sent.bin").use { modelIn ->
            val sentenceModel = SentenceModel(modelIn)
            sentenceDetector = SentenceDetectorME(sentenceModel)
        }

        Resources.modelAsStream("en_token.bin").use { stream ->
            val tokenModel = TokenizerModel(stream)
            tokenizer = TokenizerME(tokenModel)
        }

        Resources.modelAsStream("en_pos_maxent.bin").use { stream ->
            val model = POSModel(stream)
            tagger = POSTaggerME(model)
        }

        Resources.modelAsStream("en_lemmatizer.dict.bin").use { stream ->
            lemmatizerModel = DictionaryLemmatizer(stream) }

        Resources.modelAsStream("en_chunker.bin").use { stream ->
            val chunkerModel = ChunkerModel(stream)
            chunker = ChunkerME(chunkerModel)
        }
    }
}