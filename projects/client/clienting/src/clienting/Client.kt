package clienting

import rendering.Renderer

class Client {
  private val renderer: Renderer = Renderer()

  fun update() {
    renderer.render()
  }

  fun free() {
    renderer.free()
  }
}