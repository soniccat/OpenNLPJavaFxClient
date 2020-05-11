package com.aglushkov.nlphelper.main

import com.aglushkov.nlphelper.BaseView
import com.aglushkov.nlphelper.di.AppOwner
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.Scene
import javafx.scene.control.TextArea
import javafx.scene.control.skin.TextAreaSkin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.net.URL
import java.util.*
import javax.inject.Inject


class MainView : BaseView(), Initializable {
    @FXML lateinit var text: TextArea
    @FXML lateinit var tokens: TextArea
    @FXML lateinit var tags: TextArea
    @FXML lateinit var lemmas: TextArea
    @FXML lateinit var chunks: TextArea

    @Inject
    lateinit var vm: MainVM

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        val appOwner = resources!!.getObject(AppOwner.Key)
        DaggerMainViewComponent.factory().create(appOwner as MainViewComponent.Dependencies)
                .inject(this)
    }

    override fun onReady() {
        super.onReady()

        observeVM()
        subscribeOnViewEvents()

        stage.title = "Hello World"
        stage.scene = Scene(parent, 600.0, 600.0)

        // Test data
        mainScope.launch {
            delay(1500)
            text.text = "Many people use the models directly in their Java code by creating SentenceDetector and Tokenizer objects and calling their methods as appropriate."
        }
    }

    private fun subscribeOnViewEvents() {
        text.textProperty().addListener { observable, oldValue, newValue ->
            vm.onTextChanged(newValue)
        }

        chunks.setOnMouseMoved {
            val skin = chunks.skin as TextAreaSkin
            val mouseHit = skin.getIndex(it.x, it.y)
            selectChunk(mouseHit.charIndex)
        }
    }

    private fun selectChunk(charIndex: Int) {
        if (charIndex < 0 || charIndex >= chunks.text.length) {
            chunks.selectRange(0,0)
            onChunkSelectionCleared()
            return
        }

        var pos = charIndex
        var ch = chunks.text[pos]
        while (pos > 0 && ch != 'B' && ch != 'O') {
            --pos
            ch = chunks.text[pos]
        }

        while (pos < chunks.text.length - 1 && chunks.text[pos] == ' ') ++pos
        val startPos = pos

        pos = charIndex
        ch = chunks.text[pos]
        while (pos < chunks.text.length - 1 && ch != 'B' && ch != 'O') {
            ++pos
            ch = chunks.text[pos]
        }

        --pos
        while (pos > 0 && chunks.text[pos] == ' ') --pos
        val endPos = pos

        if (startPos < endPos) {
            chunks.selectRange(startPos, endPos + 1)
            onChunkSelected(startPos, endPos + 1)
        } else {
            chunks.selectRange(0,0)
            onChunkSelectionCleared()
        }
    }

    private fun onChunkSelected(start: Int, end: Int) {
        val selectedWordCount = chunks.text.subSequence(start, end).count {
            it == ' '
        } + 1

        val beforeWordCount = chunks.text.subSequence(0, start).count {
            it == ' '
        } + 1

        selectTokens(beforeWordCount, beforeWordCount + selectedWordCount)
        selectTags(beforeWordCount, beforeWordCount + selectedWordCount)
        selectLemmas(beforeWordCount, beforeWordCount + selectedWordCount)
    }

    private fun selectTokens(startToken: Int, endToken: Int) {
        selectWords(startToken, endToken, tokens)
    }

    private fun selectTags(startTag: Int, endTag: Int) {
        selectWords(startTag, endTag, tags)
    }

    private fun selectLemmas(startLemma: Int, endLemma: Int) {
        selectWords(startLemma, endLemma, lemmas)
    }

    private fun selectWords(startWord: Int, endWord: Int, textArea: TextArea) {
        var wordIndex = 0
        var startWordIndex = 0
        var endWordIndex = 0
        textArea.text.forEachIndexed { index, c ->
            if (c == ' ') {
                ++wordIndex
                if (wordIndex == startWord - 1) {
                    startWordIndex = index + 1
                } else if (wordIndex == endWord - 1) {
                    endWordIndex = index
                }
            }
        }

        textArea.selectRange(startWordIndex, endWordIndex)
    }

    private fun onChunkSelectionCleared() {
        selectTokens(0, 0)
        selectTags(0, 0)
        selectLemmas(0, 0)
    }

    private fun observeVM() {
        mainScope.launch {
            vm.tokens.collect {
                tokens.text = it
            }
        }

        mainScope.launch {
            vm.tags.collect {
                tags.text = it
            }
        }

        mainScope.launch {
            vm.chunks.collect {
                chunks.text = it
            }
        }

        mainScope.launch {
            vm.lemmas.collect {
                lemmas.text = it
            }
        }
    }
}