package com.aglushkov.nlp

class NLPSentence(val text: String,
                  private val core: NLPCore
) {
    var tokens: Array<out String> = emptyArray()
    var tags: Array<out String> = emptyArray()
    var lemmas: Array<out String> = emptyArray()
    var chunks: Array<out String> = emptyArray()

    fun load() {
        tokens = core.tokenize(text)
        tags = core.tag(tokens)
        lemmas = core.lemmatize(tokens, tags)
        chunks = core.chunk(tokens, tags)
    }

    override fun toString(): String {
        return java.lang.String.join(" ", *tokens)
    }
}