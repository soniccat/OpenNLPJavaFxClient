package com.aglushkov.nlp

import com.aglushkov.model.Resource
import com.aglushkov.model.isLoaded
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
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

class NLPCore(
        private val scope: CoroutineScope
) {
    val state = MutableStateFlow<Resource<NLPCore>>(Resource.Uninitialized())

    private var sentenceDetector: SentenceDetectorME? = null
    private var tokenizer: TokenizerME? = null
    private var tagger: POSTaggerME? = null
    private var lemmatizer: DictionaryLemmatizer? = null
    private var chunker: ChunkerME? = null

    init {
        load()
    }

    suspend fun waitUntilInitialized() = state.first { it.isLoaded() }

    fun sentences(text: String): Array<out String> = sentenceDetector?.sentDetect(text).orEmpty()
    fun tokenize(sentence: String): Array<out String> = tokenizer?.tokenize(sentence).orEmpty()
    fun tag(tokens: Array<out String>): Array<out String> = tagger?.tag(tokens).orEmpty()
    fun lemmatize(tokens: Array<out String>, tags: Array<out String>) = lemmatizer?.lemmatize(tokens, tags).orEmpty()
    fun chunk(tokens: Array<out String>, tags: Array<out String>) = chunker?.chunk(tokens, tags).orEmpty()

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
            lemmatizer= DictionaryLemmatizer(stream) }

        Resources.modelAsStream("en_chunker.bin").use { stream ->
            val chunkerModel = ChunkerModel(stream)
            chunker = ChunkerME(chunkerModel)
        }
    }
}