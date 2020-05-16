package com.aglushkov.nlphelper.main

import com.aglushkov.nlphelper.BaseView
import com.aglushkov.nlphelper.di.AppOwner
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.Scene
import javafx.scene.control.Menu
import javafx.scene.control.MenuBar
import javafx.scene.control.MenuItem
import javafx.scene.control.TextArea
import javafx.scene.control.skin.TextAreaSkin
import javafx.scene.layout.BorderPane
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.net.URL
import java.util.*
import javax.inject.Inject


class MainView : BaseView(), Initializable {
    @FXML lateinit var mainMenu: MenuBar
    @FXML lateinit var showSentences: MenuItem

    @FXML lateinit var text: TextArea
    @FXML lateinit var tokens: TextArea
    @FXML lateinit var tags: TextArea
    @FXML lateinit var lemmas: TextArea
    @FXML lateinit var chunks: TextArea

    @Inject
    lateinit var vm: MainVM

    @FXML
    fun showSentences() {

    }

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
    }

    private fun subscribeOnViewEvents() {
        text.textProperty().addListener { observable, oldValue, newValue ->
            vm.onTextChanged(newValue)
        }

        tokens.setOnMouseMoved {
            val skin = tokens.skin as TextAreaSkin
            val mouseHit = skin.getIndex(it.x, it.y)
            selectWord(mouseHit.charIndex, tokens)
        }

        tags.setOnMouseMoved {
            val skin = tags.skin as TextAreaSkin
            val mouseHit = skin.getIndex(it.x, it.y)
            selectWord(mouseHit.charIndex, tags)
        }

        chunks.setOnMouseMoved {
            val skin = chunks.skin as TextAreaSkin
            val mouseHit = skin.getIndex(it.x, it.y)
            selectChunk(mouseHit.charIndex)
        }

        lemmas.setOnMouseMoved {
            val skin = lemmas.skin as TextAreaSkin
            val mouseHit = skin.getIndex(it.x, it.y)
            selectWord(mouseHit.charIndex, lemmas)
        }
    }

    private fun selectWord(charIndex: Int, textArea: TextArea) {
        val number = findWordNumber(charIndex, textArea)
        if (number == -1) {
            clearSelections()
        } else {
            selectWord(number)
        }
    }

    private fun findWordNumber(charIndex: Int, textArea: TextArea): Int {
        if (charIndex < 0 || charIndex >= textArea.text.length) {
            return -1
        }

        var pos = charIndex
        while (pos > 0 && textArea.text[pos] != ' ') {
            --pos
        }

        val wordCountBefore = if (pos != 0) {
            textArea.text.subSequence(0, pos).count {
                it == ' '
            } + 1
        } else {
            0
        }

        return wordCountBefore + 1
    }

    private fun selectChunk(charIndex: Int) {
        val range = getChunkRange(charIndex)
        if (range.isEmpty()) {
            clearSelections()
            return
        }

        chunks.selectRange(range.first, range.last + 1)
        onChunkSelected(range.first, range.last + 1)
    }

    private fun getChunkRange(charIndex: Int): IntRange {
        if (charIndex < 0 || charIndex >= chunks.text.length) {
            return IntRange.EMPTY
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

        return IntRange(startPos, endPos + 1)
    }

    private fun onChunkSelected(start: Int, end: Int) {
        val selectedWordCount = chunks.text.subSequence(start, end).count {
            it == ' '
        } + 1

        val beforeWordCount = chunks.text.subSequence(0, start).count {
            it == ' '
        } + 1

        selectWords(beforeWordCount, beforeWordCount + selectedWordCount, tokens)
        selectWords(beforeWordCount, beforeWordCount + selectedWordCount, tags)
        selectWords(beforeWordCount, beforeWordCount + selectedWordCount, lemmas)
    }

    private fun selectWords(startWord: Int, endWord: Int, textArea: TextArea) {
        if (startWord == 0 || endWord == 0) {
            textArea.selectRange(0, 0)
            return
        }

        var wordIndex = 0
        var startWordIndex = 0
        var endWordIndex = textArea.text.length
        textArea.text.forEachIndexed { index, c ->
            if (c == ' ') {
                ++wordIndex
                if (wordIndex == startWord - 1) {
                    startWordIndex = index + 1
                }
                if (wordIndex == endWord - 1) {
                    endWordIndex = index
                }
            }
        }

        textArea.selectRange(startWordIndex, endWordIndex)
    }

    private fun selectWord(word: Int) {
        selectWords(word, word + 1, tokens)
        selectWords(word, word + 1, tags)
        selectWords(word, word + 1, lemmas)
        selectWords(word, word + 1, chunks)
    }

    private fun clearSelections() {
        chunks.deselect()
        tokens.deselect()
        tags.deselect()
        lemmas.deselect()
    }

    private fun observeVM() {
        mainScope.launch {
            vm.text.collect {
                text.text = it
            }
        }

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