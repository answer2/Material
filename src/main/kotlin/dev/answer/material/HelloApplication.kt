package dev.answer.material

import dev.answer.material.manager.ActivityManager
import dev.answer.material.view.Activity
import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.stage.Stage

class HelloApplication : Application() {
    override fun start(stage: Stage) {
       val fxmlLoader = FXMLLoader(HelloApplication::class.java.getResource("hello-view.fxml"))
        val scene = Scene(fxmlLoader.load(), 320.0, 240.0)
        stage.title = "Hello!"
        stage.scene = scene
        stage.show()

        val activity = MainActivity()
        ActivityManager.startActivity(activity)
    }
}

fun main() {
    Application.launch(HelloApplication::class.java)


}