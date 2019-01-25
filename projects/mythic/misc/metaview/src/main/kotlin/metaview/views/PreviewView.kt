package metaview.views

import javafx.scene.Node
import javafx.scene.control.CheckBox
import javafx.scene.image.ImageView
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import metahub.OutputValues
import metahub.getGraphOutput
import metaview.*
import mythic.imaging.textureOutputTypes

fun previewView(emit: Emitter, values: OutputValues) =
    ifSelectedNode { state, id ->
      val buffer = if (state.gui.previewFinal) {
        val output = getGraphOutput(textureOutputTypes, state.graph!!, values)
        getNodePreviewBuffer(bitmapType, output["diffuse"]!!)
      } else
        getNodePreviewBuffer(state.graph!!, id, values[id]!!)

      val image = newImage(buffer)
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
      val toggleFinal = CheckBox()
      toggleFinal.text = "Final"
      toggleFinal.isSelected = state.gui.previewFinal
      toggleFinal.selectedProperty().addListener { _ ->
        emit(Event(EventType.setPreviewFinal, toggleFinal.isSelected))
      }
      val panel = VBox()
      panel.children.addAll(preview, HBox(5.0, toggleTiling, toggleFinal))
      panel
    }
