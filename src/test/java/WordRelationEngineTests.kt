import com.aglushkov.nlp.NLPCore
import com.aglushkov.nlp.NLPSentence
import com.aglushkov.word_relation.WordRelationEngine
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.jetbrains.annotations.TestOnly

import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class WordRelationEngineTests {

    val scope = TestCoroutineScope()
    val nlpCore = NLPCore(scope)
    val engine = WordRelationEngine(nlpCore)

    init {
        nlpCore.load()
    }

    @Test
    fun test1() = testForNounAfterVerb(
        "Oh, I thought they'd taken care of them,' said Jenny.",
        "take",
        "care"
    )

    @Test
    fun test2() = testForNounAfterVerb(
        "Trevor takes care of the rest of the garden",
        "take",
        "care"
    )

    @Test
    fun test3() = testForNounAfterVerb(
        "Take care to avoid needless misunderstandings",
        "take",
        "care"
    )

    @Test
    fun test4() = testForNounAfterVerb(
        "We are taking every care to provide the latest technology and a quality environment.",
        "take",
        "care"
    )

    @Test
    fun test5() = testForNounAfterVerb(
        "you should take particular care not to catch flu",
        "take",
        "care"
    )

    @Test
    fun test6() = testForNounAfterVerb(
        "The partner of the speechreader will take special care that the conversation remains `one-to-one' and not three people talking together",
        "take",
        "care"
    )

    private fun testForNounAfterVerb(str: String, verb: String, noun: String) {
        // Assume
        val nlpSentence = NLPSentence(str, nlpCore)

        // Act
        val result = engine.findNounAfterVerb(nlpSentence, verb)

        // Assert
        assertEquals(1, result.size)
        assertEquals(noun, result.first().word2)
    }
}