package gdxy

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.backends.lwjgl.LwjglFiles

fun initializeGdx() {
  Gdx.files = LwjglFiles()
}