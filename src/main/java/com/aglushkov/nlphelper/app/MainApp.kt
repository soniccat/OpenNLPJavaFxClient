package com.aglushkov.nlphelper.app

import com.aglushkov.model.Resource
import com.aglushkov.nlp.NLPCore
import com.aglushkov.nlphelper.di.AppOwner
import com.aglushkov.nlphelper.di.AppOwnerResourceBundle
import com.aglushkov.nlphelper.main.MainView
import com.aglushkov.nlphelper.main.MainViewComponent
import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType
import javafx.stage.Stage
import javafx.stage.WindowEvent
import javafx.stage.WindowEvent.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import resources.Resources
import javax.inject.Inject
import javax.inject.Named

class MainApp : Application(),
        AppOwner,
        MainViewComponent.Dependencies {
    private lateinit var appComponent: AppComponent

    @Inject
    @Named("main")
    lateinit var mainScope: CoroutineScope

    @Inject
    lateinit var core: NLPCore

    override fun nlpCore(): NLPCore {
        return core
    }

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

        val loader = FXMLLoader(Resources.layout("main.fxml"),
                AppOwnerResourceBundle(this))
        val root = loader.load<Parent>()
        val view: MainView = loader.getController<MainView>()

        view.stage = primaryStage
        view.parent = root
        view.show()
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
    }
}