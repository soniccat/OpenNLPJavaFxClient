package com.aglushkov.nlphelper.app

import com.aglushkov.db.AppDatabase
import com.aglushkov.db.SentenceRepository
import com.aglushkov.model.Resource
import com.aglushkov.model.isError
import com.aglushkov.nlp.NLPCore
import com.aglushkov.nlphelper.BaseView
import com.aglushkov.nlphelper.di.AppOwner
import com.aglushkov.nlphelper.di.AppOwnerResourceBundle
import com.aglushkov.nlphelper.main.MainViewComponent
import com.aglushkov.nlphelper.sentences.SentencesViewComponent
import com.aglushkov.word_relation.WordRelationEngine
import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType
import javafx.stage.Stage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import resources.Resources
import javax.inject.Inject
import javax.inject.Named


class MainApp : Application(),
        AppOwner,
        MainViewComponent.Dependencies,
        SentencesViewComponent.Dependencies
{
    private lateinit var appComponent: AppComponent

    @Inject
    @Named("main")
    lateinit var mainScope: CoroutineScope

    @Inject lateinit var nlpCore: NLPCore
    @Inject lateinit var database: AppDatabase
    @Inject lateinit var sentenceRepository: SentenceRepository
    @Inject lateinit var wordRelationEngine: WordRelationEngine

    override fun nlpCore() = nlpCore
    override fun database() = database
    override fun sentenceRepository() = sentenceRepository

    @Throws(Exception::class)
    override fun start(primaryStage: Stage) {
        appComponent = DaggerAppComponent.builder().build()
        appComponent.inject(this)

        observeCriticalErrors()

        openWindow("main.fxml", this, primaryStage)

        mainScope.launch {
            nlpCore.waitUntilInitialized()
            wordRelationEngine.findNounAfterVerb("I can drive a car, but Tom can't.")
        }
    }

    private fun observeCriticalErrors() {
        mainScope.launch {
            nlpCore.state.first { it.isError() }.run {
                showError("NLP Core Loading Error", this as Resource.Error)
            }
        }

        mainScope.launch {
            database.state.first { it.isError() }.run {
                showError("Database Loading Error", this as Resource.Error)
            }
        }
    }

    private fun showError(title: String, error: Resource.Error<*>) {
        val alert = Alert(AlertType.ERROR)
        alert.title = title
        alert.contentText = error.throwable.message
        alert.showAndWait()
    }

    override fun getApplication(): Application {
        return this
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(MainApp::class.java, *args)
        }

        @JvmStatic
        fun openWindow(name: String, appOwner: AppOwner, stage: Stage) {
            val loader = FXMLLoader(Resources.layout(name), AppOwnerResourceBundle(appOwner))
            val root = loader.load<Parent>()
            val view: BaseView = loader.getController()

            view.stage = stage
            view.parent = root
            view.show()
        }
    }
}