package com.aglushkov.nlphelper.sentences

import com.aglushkov.db.models.Sentence
import com.aglushkov.nlphelper.BaseView
import com.aglushkov.nlphelper.di.AppOwner
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.ListView
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL
import java.util.*
import javax.inject.Inject


class SentencesView : BaseView(), Initializable {
    lateinit var appOwner: AppOwner

    @FXML lateinit var importNameTextField: TextField
    @FXML lateinit var importTextArea: TextArea
    @FXML lateinit var importButton: Button

    @FXML lateinit var searchField: TextField
    @FXML lateinit var listView: ListView<String>

    @Inject lateinit var vm: SentencesVM

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        appOwner = resources!!.getObject(AppOwner.Key) as AppOwner
        DaggerSentencesViewComponent.factory().create(appOwner as SentencesViewComponent.Dependencies)
                .inject(this)
    }

    override fun onReady() {
        super.onReady()

        observeVM()
        subscribeOnViewEvents()

        stage.title = "Sentences"
        stage.scene = Scene(parent, 400.0, 600.0)
    }

    private fun observeVM() {
        mainScope.launch {
            vm.sentences.collect {
                val sentences = FXCollections.observableArrayList(it.map { it.text })
                listView.items = sentences
            }
        }
    }

    private fun subscribeOnViewEvents() {
        searchField.textProperty().addListener { observable, oldValue, newValue ->
            vm.onSearchChanged(newValue)
        }
    }

    fun importText() {
        vm.onImportPressed(importNameTextField.text, importTextArea.text)
    }
}