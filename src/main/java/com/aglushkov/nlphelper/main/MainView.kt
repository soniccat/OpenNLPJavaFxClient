package com.aglushkov.nlphelper.main

import com.aglushkov.nlphelper.di.*
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.TextArea
import java.net.URL
import java.util.*
import javax.inject.Inject

class MainView : Initializable {
    @FXML lateinit var text: TextArea
    @FXML lateinit var tokens: TextArea
    @FXML lateinit var tags: TextArea
    @FXML lateinit var chunks: TextArea

    @Inject
    lateinit var vm: MainViewModel

    @FXML
    fun handleSubmitButtonAction(event: ActionEvent?) {
        text.text = "Sign in button pressed"
    }

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        val appOwner = resources!!.getObject(AppOwner.Key)
        DaggerMainViewComponent.factory().create(appOwner as MainViewComponent.Dependencies)
                .inject(this)

        System.out.println("a")

        text.textProperty().addListener { observable, oldValue, newValue ->
            println("omg: " + newValue)
        }
    }
}