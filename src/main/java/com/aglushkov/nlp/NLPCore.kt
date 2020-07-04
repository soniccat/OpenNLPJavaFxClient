package com.aglushkov.nlp

import com.aglushkov.model.Resource
import com.aglushkov.model.isLoaded
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import opennlp.tools.chunker.ChunkSample
import opennlp.tools.chunker.ChunkerME
import opennlp.tools.chunker.ChunkerModel
import opennlp.tools.lemmatizer.DictionaryLemmatizer
import opennlp.tools.postag.POSModel
import opennlp.tools.postag.POSTaggerME
import opennlp.tools.sentdetect.SentenceDetectorME
import opennlp.tools.sentdetect.SentenceModel
import opennlp.tools.tokenize.TokenizerME
import opennlp.tools.tokenize.TokenizerModel
import opennlp.tools.util.Span
import resources.Resources

class NLPCore(
    private val scope: CoroutineScope
) {
    companion object {
        val UNKNOWN_LEMMA = "O"
    }

    enum class Tag {
        NN,     //	Noun, singular or mass
        NNS,    //	Noun, plural
        NNP,    //	Proper noun, singular
        NNPS,   //	Proper noun, plural

        VB,     //	Verb, base form
        VBD,    //	Verb, past tense
        VBG,    //	Verb, gerund or present participle
        VBN,    //	Verb, past participle
        VBP,    //	Verb, non-3rd person singular present
        VBZ,    //	Verb, 3rd person singular present

        IN,     // Preposition or subordinating conjunction

        UNKNOWN
        ;

        fun isNoun() = when (this) {
            NN, NNS, NNP, NNPS -> true
            else -> false
        }

        fun isVerb() = when (this) {
            VB, VBD, VBG, VBN, VBP, VBZ -> true
            else -> false
        }

        fun isPrep() = when (this) {
            NN, NNS, NNP, NNPS -> true
            else -> false
        }
    }

    enum class ChunkType {
        NP,     // Noun Phrase
        VP,     // Verb phrase
        PP,     // Prepositional phrase
        ADJP,   // Adjective phrase
        ADVP,   // Adverb phrase
        X;

        companion object {
            fun parse(str: String): ChunkType {
                return try {
                    valueOf(str)
                } catch (e: java.lang.Exception) {
                    X
                }
            }
        }

        fun isNounPhrase() = this == NP
        fun isVerbPhrase() = this == VP
        fun isPrepositionalPhrase() = this == PP
    }

    data class Span(val start: Int, val end: Int, val type: ChunkType) {
        companion object {
            fun fromNLPSpan(span: opennlp.tools.util.Span): Span {
                return Span(span.start, span.end, ChunkType.parse(span.type))
            }
        }
    }

    val state = MutableStateFlow<Resource<NLPCore>>(Resource.Uninitialized())

    private var sentenceModel: SentenceModel? = null
    private var tokenModel: TokenizerModel? = null
    private var posModel: POSModel? = null
    private var chunkerModel: ChunkerModel? = null

    private var sentenceDetector: SentenceDetectorME? = null
    private var tokenizer: TokenizerME? = null
    private var tagger: POSTaggerME? = null
    private var lemmatizer: DictionaryLemmatizer? = null
    private var chunker: ChunkerME? = null

    init {
    }

    suspend fun waitUntilInitialized() = state.first { it.isLoaded() }

    fun sentences(text: String): Array<out String> = sentenceDetector?.sentDetect(text).orEmpty()
    fun tokenize(sentence: String): Array<out String> = tokenizer?.tokenize(sentence).orEmpty()
    fun tag(tokens: Array<out String>): Array<out String> = tagger?.tag(tokens).orEmpty()
    fun tagEnums(tags: Array<out String>): List<Tag> = tags.map {
        try {
            Tag.valueOf(it)
        } catch (e: java.lang.Exception) {
            Tag.UNKNOWN
        }
    }
    fun lemmatize(tokens: Array<out String>, tags: Array<out String>) = lemmatizer?.lemmatize(tokens, tags).orEmpty()
    fun chunk(tokens: Array<out String>, tags: Array<out String>) = chunker?.chunk(tokens, tags).orEmpty()
    fun spanList(tokens: Array<out String>, tags: Array<out String>, chunks: Array<out String>) =
            ChunkSample.phrasesAsSpanList(tokens, tags, chunks).map {
                Span.fromNLPSpan(it)
            }

    fun load() {
        state.value = Resource.Loading(this@NLPCore)
        scope.launch {
            try {
                loadModels()
                createMEObjects()
                state.value = Resource.Loaded(this@NLPCore)
            } catch (e: Exception) {
                state.value = Resource.Error(e, true)
            }
        }
    }

    private fun loadModels() {
        Resources.modelAsStream("en_sent.bin").use { modelIn ->
            sentenceModel = SentenceModel(modelIn)
        }

        Resources.modelAsStream("en_token.bin").use { stream ->
            tokenModel = TokenizerModel(stream)
        }

        Resources.modelAsStream("en_pos_maxent.bin").use { stream ->
            posModel = POSModel(stream)
        }

        Resources.modelAsStream("en_lemmatizer.dict.bin").use { stream ->
            lemmatizer = DictionaryLemmatizer(stream)
        }

        Resources.modelAsStream("en_chunker.bin").use { stream ->
            chunkerModel = ChunkerModel(stream)
        }
    }

    private fun createMEObjects() {
        sentenceDetector = SentenceDetectorME(sentenceModel)
        tokenizer = TokenizerME(tokenModel)
        tagger = POSTaggerME(posModel)
        chunker = ChunkerME(chunkerModel)
    }

    // to be able to work with NLPCore in a separate thread
    fun clone(): NLPCore {
        assert(state.value.isLoaded()) { "Can't copy NLPCore when it isn't loaded" }

        return NLPCore(scope).apply {
            sentenceDetector = SentenceDetectorME(this@NLPCore.sentenceModel)
            tokenizer = TokenizerME(this@NLPCore.tokenModel)
            tagger = POSTaggerME(this@NLPCore.posModel)
            chunker = ChunkerME(this@NLPCore.chunkerModel)
            lemmatizer = this@NLPCore.lemmatizer
            state.value = this@NLPCore.state.value
        }
    }
}