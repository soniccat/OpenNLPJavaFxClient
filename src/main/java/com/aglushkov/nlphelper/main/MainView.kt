package com.aglushkov.nlphelper.main

import com.aglushkov.nlphelper.di.*
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.TextArea
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.net.URL
import java.util.*
import javax.inject.Inject
import javax.inject.Named

class MainView : Initializable {
    @FXML lateinit var text: TextArea
    @FXML lateinit var tokens: TextArea
    @FXML lateinit var tags: TextArea
    @FXML lateinit var lemmas: TextArea
    @FXML lateinit var chunks: TextArea

    @Inject
    @Named("main")
    lateinit var mainScope: CoroutineScope

    @Inject
    lateinit var vm: MainVM

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        val appOwner = resources!!.getObject(AppOwner.Key)
        DaggerMainViewComponent.factory().create(appOwner as MainViewComponent.Dependencies)
                .inject(this)

        text.textProperty().addListener { observable, oldValue, newValue ->
            vm.onTextChanged(newValue)
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