package metaview.views

import com.sun.javafx.scene.control.CustomColorDialog
import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.scene.Node
import javafx.scene.control.TextField
import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundFill
import javafx.scene.layout.CornerRadii
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.util.Duration
import metaview.*
import mythic.spatial.Vector3

typealias OnChange = (Any, Boolean) -> Unit
typealias ValueView = (value: Any, OnChange) -> Node

typealias ValueViewSource = (InputDefinition) -> ValueView

fun convertColor(color: Color) =
    Vector3(color.red.toFloat(), color.green.toFloat(), color.blue.toFloat())

//fun valueViewSource(valueView: ValueView) = { }

val colorView: ValueView = { value, changed ->
  val color = value as Vector3
  val image = Pane()
  var fxColor = Color(color.x.toDouble(), color.y.toDouble(), color.z.toDouble(), 1.0)
  fun updateColorSample() {
    image.background = Background(BackgroundFill(fxColor, CornerRadii.EMPTY, Insets.EMPTY))
  }
  updateColorSample()
  image.prefWidth = nodeLength.toDouble()
  image.prefHeight = nodeLength.toDouble()
  image.setOnMouseClicked {
    val dialog = CustomColorDialog(globalWindow())
    dialog.currentColor = fxColor
    dialog.show()
    val updater = Timeline(KeyFrame(Duration.seconds(0.5), EventHandler {
      fxColor = dialog.customColor
      updateColorSample()
      val newColor = convertColor(dialog.customColor)
      changed(newColor, true)

    }))
    updater.cycleCount = Timeline.INDEFINITE
    updater.play()

    val save = {
      updater.stop()
      fxColor = dialog.customColor
      updateColorSample()
      val newColor = convertColor(dialog.customColor)
      changed(newColor, false)
    }

    dialog.setOnCancel {
      updater.stop()
      fxColor = Color(color.x.toDouble(), color.y.toDouble(), color.z.toDouble(), 1.0)
      updateColorSample()
      changed(color, true)
    }
    dialog.setOnSave(save)
    dialog.setOnUse(save)
  }
  image
}

val numericFloatView: ValueViewSource = { definition ->
  { value, changed ->
    val field = TextField()
    field.text = (value as Float).toString()
    field.textProperty().addListener { event ->
      val newValue = field.text.toFloatOrNull()
      if (newValue != null)
        changed(newValue, false)
    }
    field.setOnInputMethodTextChanged { event ->
    }
    field
  }
}

val numericIntView: ValueViewSource = { definition ->
  { value, changed ->
    val field = TextField()
    field.text = (value as Int).toString()
    field.textProperty().addListener { event ->
      val newValue = field.text.toIntOrNull()
      if (newValue != null)
        changed(newValue, false)
    }
    field.setOnInputMethodTextChanged { event ->
    }
    field
  }
}

val valueViews: Map<String, ValueViewSource> = mapOf(
    colorType to { _ -> colorView },
    floatType to numericFloatView,
    intType to numericIntView
)