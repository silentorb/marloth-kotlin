package metaview.views

import javafx.scene.Node
import javafx.scene.canvas.Canvas
import javafx.scene.image.PixelFormat
import javafx.scene.image.WritableImage
import metaview.textureLength
import java.nio.ByteBuffer

fun outputImage(buffer: ByteBuffer, length: Double): Node {
  buffer.rewind()
  val byteArray = ByteArray(buffer.capacity())
  buffer.get(byteArray)
  val canvas = Canvas()
  val image = WritableImage(textureLength, textureLength)
  image.pixelWriter.setPixels(0, 0, textureLength, textureLength,
      PixelFormat.getByteRgbInstance(),
      byteArray, 0, textureLength * 3);
  canvas.width = length
  canvas.height = length
  canvas.graphicsContext2D.drawImage(image, 0.0, 0.0, length, length)
  return canvas
}
