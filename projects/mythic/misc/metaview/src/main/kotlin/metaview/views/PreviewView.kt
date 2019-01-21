package metaview.views

import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.CheckBox
import javafx.scene.image.ImageView
import javafx.scene.layout.FlowPane
import javafx.scene.layout.GridPane
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox
import metahub.OutputValues
import metaview.Emitter
import metaview.Event
import metaview.EventType

fun previewView(emit: Emitter, values: OutputValues) =
    ifSelectedNode { state, id ->
      val length = 40.0
      val image = newImage(getNodePreviewBuffer(state.graph!!, id, values[id]!!))
      val newImageView = { len: Double ->
        val imageView = ImageView(image)
        imageView.fitWidth = len
        imageView.fitHeight = len
        imageView
      }

      val preview = if (state.gui.tilePreview) {
        val panel = GridPane()
        fun addCell(x: Int, y: Int, node: Node) {
          GridPane.setColumnIndex(node, x)
          GridPane.setRowIndex(node, y)
          panel.children.add(node)
        }

        val imageViews = (1..4).map { newImageView(200.0) }
        addCell(1, 1, imageViews[0])
        addCell(2, 1, imageViews[1])
        addCell(1, 2, imageViews[2])
        addCell(2, 2, imageViews[3])
        panel
      } else {
        newImageView(400.0)
      }

      val toggleTiling = CheckBox()
      toggleTiling.text = "Tile"
      toggleTiling.isSelected = state.gui.tilePreview
      toggleTiling.selectedProperty().addListener { _ ->
        emit(Event(EventType.setTilePreview, toggleTiling.isSelected))
      }
      val panel = VBox()
       panel.children.addAll(preview, toggleTiling)
      panel
    }
