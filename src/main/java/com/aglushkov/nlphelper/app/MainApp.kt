package com.aglushkov.nlphelper.app

import com.aglushkov.db.AppDatabase
import com.aglushkov.model.Resource
import com.aglushkov.nlp.NLPCore
import com.aglushkov.nlphelper.BaseView
import com.aglushkov.nlphelper.di.AppOwner
import com.aglushkov.nlphelper.di.AppOwnerResourceBundle
import com.aglushkov.nlphelper.main.MainViewComponent
import com.aglushkov.nlphelper.sentences.SentencesViewComponent
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

    @Inject lateinit var core: NLPCore
    @Inject lateinit var database: AppDatabase

    override fun nlpCore() = core
    override fun database() = database

    @Throws(Exception::class)
    override fun start(primaryStage: Stage) {
        appComponent = DaggerAppComponent.builder().build()
        appComponent.inject(this)

        mainScope.launch {
            core.state.first {
                it is Resource.Error
            }.run {
                showError("Model Loading", this as Resource.Error)
            }
        }

        openWindow("main.fxml", this, primaryStage)
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