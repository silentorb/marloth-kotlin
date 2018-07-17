package gcview

import configuration.loadConfig
import configuration.saveConfig
import javafx.application.Application
import javafx.collections.FXCollections
import javafx.event.EventHandler
import javafx.scene.Scene
import javafx.scene.chart.AreaChart
import javafx.scene.chart.LineChart
import javafx.scene.chart.NumberAxis
import javafx.scene.chart.XYChart
import javafx.scene.control.*
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.stage.Stage
import java.io.File
import kotlin.concurrent.thread

data class AppConfig(
    val logPath: String,
    val exePath: String
)

const val configFilePath = "gcViewConfig.yaml"

fun defaultAppConfig() =
    AppConfig(
        logPath = "gctestlog.txt",
        exePath = "Not-configured"
    )

fun onNewConfigFile(): AppConfig {
  val config = defaultAppConfig()
  saveConfig(configFilePath, config)
  return config
}

fun createChart(data: LogData): AreaChart<Number, Number> {
  val xAxis = NumberAxis()
  val yAxis = NumberAxis()
  xAxis.label = "Start Time"
  yAxis.label = "Duration"

  val chart = AreaChart(xAxis, yAxis)
  chart.title = "GC Times"
  chart.isLegendVisible = false

  data.collections.forEachIndexed { index, collection ->
    val series = XYChart.Series<Number, Number>()
    val duration = collection.evacuateCollectionSet.duration
    series.data.add(XYChart.Data(collection.startTime, duration))
//    series.data.add(XYChart.Data(collection.startTime + duration / 1000, duration))
    chart.getData().add(series)
  }
  return chart
}

typealias GarbageCollectionTable = TableView<GarbageCollection>

fun createTable(data: LogData): GarbageCollectionTable {
  val table = GarbageCollectionTable()

  fun createColumn(title: String, property: String): TableColumn<GarbageCollection, Number> {
    val column = TableColumn<GarbageCollection, Number>(title)
    column.minWidth = 100.0
    column.cellValueFactory = PropertyValueFactory<GarbageCollection, Number>(property)
    return column
  }

  table.columns.addAll(
      createColumn("start", "startTime"),
      createColumn("duration", "evacuateCollectionSetDuration")
  )

  table.items = FXCollections.observableArrayList(data.collections)
  return table
}

fun createView(file: File): VBox {
  val data = loadLogFile(file)
  val root = VBox()
  root.children.addAll(
      createChart(data),
      createTable(data)
  )
  return root
}

data class Actions(
    val record: () -> Unit
)

const val second = 1000L
const val minute = 60L * second

fun record(exePath: String) {
//  val command = System.getProperty("user.dir") + "\\"

  val run = Runtime.getRuntime()
  val proc = run.exec(exePath)
  val pid = proc.toHandle().pid()
  thread {
    Thread.sleep(minute)
    Runtime.getRuntime().exec("taskkill /F /pid " + pid)
  }
}

fun createActionBar(actions: Actions): HBox {
  val hbox = HBox()
  val recordButton = Button("Rec")
  recordButton.onAction = EventHandler {
    actions.record()
  }
  hbox.children.addAll(recordButton)
  return hbox
}

class GCViewApp : Application() {

  var config: AppConfig = loadConfig<AppConfig>(configFilePath) ?: onNewConfigFile()

  override fun start(primaryStage: Stage) {
    primaryStage.title = "GC View"

    val root = VBox()
    val tabPane = TabPane()
    tabPane.tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE

    val logFiles = File("logs").listFiles()
    for (file in logFiles) {
      val tab = Tab(file.name)
      tab.content = createView(file)
      tabPane.tabs.add(tab)
    }

    val actions = Actions(
        record = { record(config.exePath)}
    )

    root.children.addAll(
        createActionBar(actions),
        tabPane
    )

    val scene = Scene(root, 1400.0, 900.0)
    primaryStage.scene = scene
    primaryStage.show()
  }

  companion object {
    @JvmStatic
    fun main(args: Array<String>) {
      Application.launch(GCViewApp::class.java)
    }
  }
}