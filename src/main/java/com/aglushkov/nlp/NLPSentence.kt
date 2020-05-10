package com.aglushkov.nlp

class NLPSentence(val text: String,
                  private val core: NLPCore
) {
    lateinit var tokens: Array<out String>
    lateinit var tags: Array<out String>
    lateinit var lemmas: Array<out String>
    lateinit var chunks: Array<out String>

    init {
        load()
    }

    private fun load() {
        tokens = core.tokenize(text)
        tags = core.tag(tokens)
        lemmas = core.lemmatize(tokens, tags)
        chunks = core.chunk(tokens, tags)
    }

    override fun toString(): String {
        return java.lang.String.join(" ", *tokens)
    }
}