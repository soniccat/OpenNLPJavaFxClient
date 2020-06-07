package com.aglushkov.nlphelper.sentences

import com.aglushkov.nlphelper.BaseView
import com.aglushkov.nlphelper.di.AppOwner
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.stage.DirectoryChooser
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.io.File
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
    @FXML lateinit var infoLabel: Label

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

        vm.onReady()
    }

    private fun observeVM() {
        mainScope.launch {
            vm.sentences.collect {
                val sentences = FXCollections.observableArrayList(it.map { it.text })
                listView.items = sentences
                infoLabel.text = "${sentences.size} sentences"
            }
        }
    }

    private fun subscribeOnViewEvents() {
        searchField.textProperty().addListener { observable, oldValue, newValue ->
            vm.onSearchChanged(newValue)
        }
    }

    fun importText() {
        if (importTextArea.text.isEmpty()) {
            val chooser = DirectoryChooser().apply {
                title = "Choose Book folder"
                initialDirectory = File(System.getProperty("user.dir"))
            }
            val selectedDirectory = chooser.showDialog(stage)
            if (selectedDirectory != null) {
                vm.onImportDirectory(selectedDirectory)
            }
        } else {
            vm.onImportPressed(importNameTextField.text, importTextArea.text)
        }
    }

    @FXML
    fun removeAll() {
        vm.onRemoveAllPressed()
    }
}