package com.aglushkov.nlphelper

import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.TextArea
import java.net.URL
import java.util.*

class MainView : Initializable {
    @FXML lateinit var text: TextArea

    @FXML
    fun handleSubmitButtonAction(event: ActionEvent?) {
        text.text = "Sign in button pressed"
    }

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        System.out.println("a")

        text.textProperty().addListener { observable, oldValue, newValue ->
            println("omg: " + newValue)
        }
    }
}