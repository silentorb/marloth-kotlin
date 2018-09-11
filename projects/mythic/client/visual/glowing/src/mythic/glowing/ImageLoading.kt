package mythic.glowing

import org.joml.Vector2i
import org.lwjgl.BufferUtils
import java.awt.Color
import java.net.URL
import java.nio.FloatBuffer
import javax.imageio.ImageIO

fun loadImageBuffer(url: URL): Pair<FloatBuffer, Vector2i> {
  val image = ImageIO.read(url)
  val width = image.getWidth()
  val height = image.getHeight()

  val buffer = BufferUtils.createFloatBuffer(width * height * 3)

  for (y in 0 until width) {
    for (x in 0 until height) {
      val pixel = image.getRGB(x, y)
      val color = Color(pixel)
      buffer.put(color.red.toFloat() / 255)
      buffer.put(color.green.toFloat() / 255)
      buffer.put(color.blue.toFloat() / 255)
    }
  }
  buffer.flip()

  return Pair(buffer, Vector2i(width, height))
}