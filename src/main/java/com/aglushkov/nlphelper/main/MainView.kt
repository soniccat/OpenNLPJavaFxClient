package com.aglushkov.nlphelper.main

import com.aglushkov.nlphelper.BaseView
import com.aglushkov.nlphelper.di.*
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.Scene
import javafx.scene.control.TextArea
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.net.URL
import java.util.*
import javax.inject.Inject
import javax.inject.Named

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
            
        }
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