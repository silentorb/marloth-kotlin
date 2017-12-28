package gdxy

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Files
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3NativesLoader

fun initializeGdx() {
  Lwjgl3NativesLoader.load()
  Gdx.files = Lwjgl3Files()
}