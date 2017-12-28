package gdxy

import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch

class CanvasManager {
  val font = BitmapFont()
  val batch = SpriteBatch()

  fun drawText(text: String, x: Float, y: Float) {
    batch.begin()
    font.draw(batch, text, x, y)
    batch.end()
  }
}