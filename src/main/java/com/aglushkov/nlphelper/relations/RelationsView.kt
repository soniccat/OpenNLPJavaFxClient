package com.aglushkov.nlphelper.relations

import com.aglushkov.nlphelper.BaseView
import com.aglushkov.nlphelper.di.AppOwner
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.input.KeyCode
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.awt.event.KeyEvent
import java.net.URL
import java.util.*
import javax.inject.Inject


class RelationsView : BaseView(), Initializable {
    lateinit var appOwner: AppOwner

    @FXML lateinit var wordTextField: TextField
    @FXML lateinit var relationTypeComboBox: ComboBox<RelationsVM.RelationOption>
    @FXML lateinit var listView: ListView<String>

    @Inject lateinit var vm: RelationsVM

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        appOwner = resources!!.getObject(AppOwner.Key) as AppOwner
        DaggerRelationsViewComponent.factory().create(appOwner as RelationsViewComponent.Dependencies)
                .inject(this)
    }

    override fun onReady() {
        super.onReady()

        observeVM()
        subscribeOnViewEvents()

        stage.title = "Relations"
        stage.scene = Scene(parent, 400.0, 600.0)

        relationTypeComboBox.items.setAll(vm.relationOptions())
        relationTypeComboBox.selectionModel.selectFirst()
    }

    private fun observeVM() {
        mainScope.launch {
            vm.relations.collect {
                val relations = FXCollections.observableArrayList(it.map { "${it.word1} - ${it.word2}" })
                listView.items = relations
            }
        }
    }

    private fun subscribeOnViewEvents() {
        relationTypeComboBox.selectionModel.selectedItemProperty().addListener { observable, oldValue, newValue ->
            vm.onOptionSelected(newValue)
        }

        wordTextField.textProperty().addListener { observable, oldValue, newValue ->
            vm.onWordChanged(newValue)
        }

        wordTextField.setOnKeyPressed {
            if (it.getCode().equals(KeyCode.ENTER)) {
                vm.onStartSearchRequested()
            }
        }
    }
}