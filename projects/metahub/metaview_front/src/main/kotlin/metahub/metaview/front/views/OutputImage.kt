package metahub.metaview.front.views

import javafx.scene.Node
import javafx.scene.canvas.Canvas
import javafx.scene.image.Image
import javafx.scene.image.PixelFormat
import javafx.scene.image.WritableImage
import metahub.metaview.textureLength
import java.nio.ByteBuffer

fun newImage(buffer: ByteBuffer): Image {
  buffer.rewind()
  val byteArray = ByteArray(buffer.capacity())
  buffer.get(byteArray)
  val image = WritableImage(textureLength, textureLength)
  image.pixelWriter.setPixels(0, 0, textureLength, textureLength,
      PixelFormat.getByteRgbInstance(),
      byteArray, 0, textureLength * 3)
  return image
}

fun outputImage(image: Image, length: Double): Node {
  val canvas = Canvas(length, length)
  canvas.isMouseTransparent = true
  canvas.graphicsContext2D.drawImage(image, 0.0, 0.0, length, length)
  return canvas
}
