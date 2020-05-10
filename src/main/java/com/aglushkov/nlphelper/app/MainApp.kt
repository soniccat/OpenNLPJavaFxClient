package com.aglushkov.nlphelper.app

import com.aglushkov.nlp.NLPCore
import com.aglushkov.model.*
import com.aglushkov.nlphelper.di.*
import com.aglushkov.nlphelper.main.MainViewComponent
import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
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
//        primaryStage.apply {
//            val oldDispatcher = eventDispatcher
//            setEventDispatcher { event, tail ->
//                oldDispatcher.dispatchEvent(event, tail)
//            }
//        }

        appComponent = DaggerAppComponent.builder().build()
        appComponent.inject(this)

        mainScope.launch {
            core.state.first {
                it is Resource.Error
            }.run {
                showError("Model Loading", this as Resource.Error)
            }
        }

        val root = FXMLLoader.load<Parent>(Resources.layout("main.fxml"),
                AppOwnerResourceBundle(this))
        primaryStage.title = "Hello World"
        primaryStage.scene = Scene(root, 600.0, 600.0)
        primaryStage.show()
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