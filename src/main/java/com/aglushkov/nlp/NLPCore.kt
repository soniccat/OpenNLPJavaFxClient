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
        VBZ,     //	Verb, 3rd person singular present

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
    }

//    enum class ChunkPart {
//        BEGIN,
//        INCLUDE,
//        UNKNOWN
//    }

    enum class ChunkType {
        NP,
        VP,
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
    }

    data class Span(val start: Int, val end: Int, val type: ChunkType) {
        companion object {
            fun fromNLPSpan(span: opennlp.tools.util.Span): Span {
                return Span(span.start, span.end, ChunkType.parse(span.type))
            }
        }
    }

//    data class Chunk(val part: ChunkPart, val type: ChunkType) {
//        companion object {
//            fun parse(str: String): Chunk? {
//                val parts = str.split('-')
//                return if (parts.size == 2) {
//                    try {
//                        val part = ChunkPart.valueOf(parts[0])
//                        val type = ChunkType.valueOf(parts[1])
//                        Chunk(part, type)
//                    } catch (e: java.lang.Exception) {
//                        null
//                    }
//                } else {
//                    null
//                }
//            }
//        }
//    }

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