package gdxy

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch

//fun createLwjglGraphics():Lwjgl3Graphics {
//  val constructors = Lwjgl3Graphics::class.java.getDeclaredConstructors()
//  constructors[0].setAccessible(true)
//  return constructors[0].newInstance(Lwjgl3ApplicationConfiguration()) as Lwjgl3Graphics
//}

class CanvasManager(window: Long) {
  //  private val graphics: Lwjgl3Graphics = createLwjglGraphics()
  private val graphics = MinimalGraphics(window)
  private val font = BitmapFont()
  private val batch = SpriteBatch()

  fun drawText(text: String, x: Float, y: Float) {
    batch.begin()
    font.draw(batch, text, x, y)
    batch.end()
  }
}