package com.aglushkov.nlphelper.sentences

import com.aglushkov.nlphelper.BaseView
import com.aglushkov.nlphelper.di.AppOwner
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.Scene
import javafx.scene.control.ListView
import javafx.scene.control.TextField
import java.net.URL
import java.util.*

class SentencesView : BaseView(), Initializable {
    lateinit var appOwner: AppOwner

    @FXML lateinit var searchField: TextField
    @FXML lateinit var listView: ListView<String>

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
        stage.scene = Scene(parent, 300.0, 300.0)
    }

    private fun observeVM() {
    }

    private fun subscribeOnViewEvents() {
    }
}