package gcview

import javafx.application.Application
import javafx.stage.Stage

class GCViewApp : Application() {

  override fun start(primaryStage: Stage) {
    primaryStage.title = "Editor"
    primaryStage.show()
  }

  companion object {
    @JvmStatic fun main(args: Array<String>) {
//      Application.launch(GCViewApp::class.java)
      launch()
    }
  }
}