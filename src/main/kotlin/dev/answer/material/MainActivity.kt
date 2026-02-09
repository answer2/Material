package dev.answer.material

import dev.answer.material.view.Activity
import dev.answer.material.view.Gravity
import dev.answer.material.view.LinearLayoutParams
import dev.answer.material.view.LinearView
import dev.answer.material.view.Orientation
import dev.answer.material.view.TextView
import javafx.geometry.Insets

/**
 *
 * @author AnswerDev
 * @date 2026/2/9 20:52
 * @description MainActivity
 */
class MainActivity : Activity() {

    override fun onCreate() {
        super.onCreate()

        setTitle("This an activity")
        setWindowSize(300.0, 300.0)

        val layout = LinearView(Orientation.VERTICAL)
        layout.setPadding(16.0)

        val title = TextView("Hello Material").apply {
            setTextSize(18.0)
            setBold(true)
            setGravity(Gravity.CENTER)
            setPadding(16.0)
        }

        layout.addView(
            title,
            LinearLayoutParams(
                margin = Insets(8.0)
            )
        )

        setContentView(layout)


    }


    override fun onStart() {
        super.onStart()
    }

}