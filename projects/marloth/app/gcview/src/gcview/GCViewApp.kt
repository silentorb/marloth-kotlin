package gcview

import configuration.loadConfig
import configuration.saveConfig
import javafx.application.Application
import javafx.collections.FXCollections
import javafx.event.EventHandler
import javafx.scene.Scene
import javafx.scene.chart.AreaChart
import javafx.scene.chart.NumberAxis
import javafx.scene.chart.XYChart
import javafx.scene.control.*
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.stage.Stage
import java.io.File
import kotlin.concurrent.thread
import kotlin.reflect.full.memberProperties


data class AppConfig(
    val logPath: String,
    val javaPath: String,
    val exeArgs: String
)

const val configFilePath = "gcViewConfig.yaml"

fun defaultAppConfig() =
    AppConfig(
        logPath = "gctestlog.txt",
        javaPath = "Not-configured",
        exeArgs = "Not-configured"
    )

fun onNewConfigFile(): AppConfig {
  val config = defaultAppConfig()
  saveConfig(configFilePath, config)
  return config
}

fun createChart(data: LogData, prop: Map.Entry<String, String>): AreaChart<Number, Number> {
  val xAxis = NumberAxis()
  val yAxis = NumberAxis()
  xAxis.label = "Start Time"
  yAxis.label = prop.value

  val chart = AreaChart(xAxis, yAxis)
  chart.title = "GC Times"
  chart.isLegendVisible = false

  val property = GarbageCollection::class.memberProperties.first { it.name == prop.key }

  data.collections.forEachIndexed { index, collection ->
    val series = XYChart.Series<Number, Number>()
    val duration = property.get(collection) as Float
    series.data.add(XYChart.Data(collection.startTime, duration))
//    series.data.add(XYChart.Data(collection.startTime + duration / 1000, duration))
    chart.getData().add(series)
  }
  return chart
}

typealias GarbageCollectionTable = TableView<GarbageCollection>

val collectionProperties = mapOf(
    "startTime" to "Start",
    "evacuateCollectionSetDuration" to "Duration",
    "youngPauseDuration" to "Young Pause",
    "updateRSMax" to "Update RS",
    "objectCopyMax" to "Object Copy"
)

fun createTable(data: LogData): GarbageCollectionTable {
  val table = GarbageCollectionTable()

  fun createColumn(title: String, property: String): TableColumn<GarbageCollection, Number> {
    val column = TableColumn<GarbageCollection, Number>(title)
    column.minWidth = 100.0
    column.isSortable = false
    column.cellValueFactory = PropertyValueFactory<GarbageCollection, Number>(property)
    return column
  }

  for (collectionProperty in collectionProperties) {
    table.columns.add(createColumn(collectionProperty.value, collectionProperty.key))
  }

  table.items = FXCollections.observableArrayList(data.collections)
  return table
}

fun createView(data: LogData, prop: Map.Entry<String, String>): Pair<VBox, VBox> {
  val root = VBox()
  val chartContainer = VBox()
  chartContainer.children.add(createChart(data, prop))
  root.children.addAll(
      chartContainer,
      createTable(data)
  )

  return Pair(root, chartContainer)
}

data class Actions(
    val record: () -> Unit,
    val propertyChanged: (Map.Entry<String, String>) -> Unit
)

const val second = 1000L
const val minute = 60L * second

fun createActionBar(actions: Actions, prop: Map.Entry<String, String>): HBox {
  val hbox = HBox()

  val propertySelector = ComboBox<String>(
      FXCollections.observableArrayList(collectionProperties.values.drop(1))
  )

  propertySelector.value = prop.value
  propertySelector.valueProperty().addListener { _, _, t1 ->
    actions.propertyChanged(collectionProperties.entries.first { it.value == t1 })
  }
  val recordButton = Button("Rec")
  recordButton.onAction = EventHandler {
    actions.record()
  }
  hbox.children.addAll(propertySelector, recordButton)
  return hbox
}

data class TabInfo(
    val data: LogData,
    val chartContainer: VBox
)

class GCViewApp : Application() {

  var config: AppConfig = loadConfig<AppConfig>(configFilePath) ?: onNewConfigFile()
  val tabMap: MutableMap<Tab, TabInfo> = mutableMapOf()
  val tabPane = TabPane()
  var prop = collectionProperties.entries.drop(1).first()

  override fun start(primaryStage: Stage) {
    primaryStage.title = "GC View"

    val root = VBox()
    tabPane.tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE
    createTabs()

    val actions = Actions(
        record = { record(config) },
        propertyChanged = { entry: Map.Entry<String, String> -> recreateViews(entry) }
    )

    root.children.addAll(
        createActionBar(actions, prop),
        tabPane
    )

    val scene = Scene(root, 1400.0, 900.0)
    primaryStage.scene = scene
    primaryStage.show()
  }

  fun createTabs() {
    println("loading")
    val logFiles = File("logs").listFiles().filter { it.isFile }
//    val logFiles = listOf(File(config.logPath))
    for (file in logFiles) {
      val tab = Tab(file.name)
      val data = loadLogFile(file)
      val (view, chartContainer) = createView(data, prop)
      tabMap[tab] = TabInfo(data, chartContainer)
      tab.content = view
      tabPane.tabs.add(tab)
    }
    println("done")
  }

  fun record(config: AppConfig) {
    val command = config.javaPath + " -Xlog:gc*=debug:file=logs/test.txt " + config.exeArgs
    val run = Runtime.getRuntime()
    val proc = run.exec(command)
    val pid = proc.toHandle().pid()
    thread {
      Thread.sleep(30 * second)
      Runtime.getRuntime().exec("taskkill /F /pid " + pid)
    }
//    Thread.sleep(30 * second)
//    Runtime.getRuntime().exec("taskkill /F /pid " + pid)
//    tabPane.tabs.clear()
//    createTabs()
  }

  fun recreateViews(prop: Map.Entry<String, String>) {
    this.prop = prop
    for (entry in tabMap) {
      val info = entry.value
      val chartContainer = info.chartContainer
      chartContainer.children.clear()
      chartContainer.children.add(createChart(info.data, prop))
    }
  }

  companion object {
    @JvmStatic
    fun main(args: Array<String>) {
      Application.launch(GCViewApp::class.java)
    }
  }
}