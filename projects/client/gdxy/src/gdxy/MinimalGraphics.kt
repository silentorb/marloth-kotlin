package gdxy

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.glutils.GLVersion
import org.lwjgl.BufferUtils
import org.lwjgl.glfw.GLFW

import com.badlogic.gdx.Graphics
import com.badlogic.gdx.graphics.Cursor
import com.badlogic.gdx.graphics.Cursor.SystemCursor
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.GL30
import com.badlogic.gdx.graphics.Pixmap

fun createLwjgl3GL30(): GL30 {
  val clazz = Class.forName("com.badlogic.gdx.backends.lwjgl3.Lwjgl3GL30")
  val constructors = clazz.getDeclaredConstructors()
  constructors[0].setAccessible(true)
  return constructors[0].newInstance() as GL30
}

class MinimalGraphics(private val window: Long) : Graphics {
  private val gl: GL30
  private var backBufferWidth: Int = 0
  private var backBufferHeight: Int = 0
  var logicalWidth: Int = 0
  var logicalHeight: Int = 0
  private var bufferFormat: Graphics.BufferFormat
  private var frameId: Long = 0

  private var buffer1 = BufferUtils.createIntBuffer(1)
  private var buffer2 = BufferUtils.createIntBuffer(1)

  init {
    this.gl = createLwjgl3GL30()
    Gdx.gl = gl
    Gdx.gl20 = gl
    Gdx.gl30 = gl
    GLFW.glfwGetFramebufferSize(window, buffer1, buffer2)
    this.backBufferWidth = buffer1.get(0)
    this.backBufferHeight = buffer2.get(0)
    GLFW.glfwGetWindowSize(window, buffer1, buffer2)
    logicalWidth = buffer1.get(0)
    logicalHeight = buffer2.get(0)
    bufferFormat = Graphics.BufferFormat(8, 8, 8, 8, 16, 0,
        0, false)
    Gdx.graphics = this
  }

  override fun isGL30Available(): Boolean {
    return true
  }

  override fun getGL20(): GL20? {
    return gl
  }

  override fun getGL30(): GL30? {
    return gl
  }

  override fun setGL20(gl20: GL20) {
    throw Error("Not supported")
  }

  override fun setGL30(gl30: GL30) {
    throw Error("Not supported")
  }

  override fun getWidth(): Int {
    return backBufferWidth
  }

  override fun getHeight(): Int {
    return backBufferHeight
  }

  override fun getBackBufferWidth(): Int {
    return backBufferWidth
  }

  override fun getBackBufferHeight(): Int {
    return backBufferHeight
  }

  override fun getFrameId(): Long {
    return frameId
  }

  override fun getDeltaTime(): Float {
    throw Error("Not supported")
  }

  override fun getRawDeltaTime(): Float {
    throw Error("Not supported")
  }

  override fun getFramesPerSecond(): Int {
    throw Error("Not supported")
  }

  override fun getType(): Graphics.GraphicsType {
    return Graphics.GraphicsType.LWJGL3
  }

  override fun getGLVersion(): GLVersion? {
    return glVersion
  }

  override fun getPpiX(): Float {
    throw Error("Not supported")
  }

  override fun getPpiY(): Float {
    throw Error("Not supported")
  }

  override fun getPpcX(): Float {
    throw Error("Not supported")
  }

  override fun getPpcY(): Float {
    throw Error("Not supported")
  }

  override fun getDensity(): Float {
    throw Error("Not supported")
  }

  override fun supportsDisplayModeChange(): Boolean {
    throw Error("Not supported")
  }

  override fun getPrimaryMonitor(): Graphics.Monitor {
    throw Error("Not supported")
  }

  override fun getMonitor(): Graphics.Monitor {
    throw Error("Not supported")
  }

  override fun getMonitors(): Array<Graphics.Monitor> {
    throw Error("Not supported")
  }

  override fun getDisplayModes(): Array<Graphics.DisplayMode> {
    throw Error("Not supported")
  }

  override fun getDisplayModes(monitor: Graphics.Monitor): Array<Graphics.DisplayMode> {
    throw Error("Not supported")
  }

  override fun getDisplayMode(): Graphics.DisplayMode {
    throw Error("Not supported")
  }

  override fun getDisplayMode(monitor: Graphics.Monitor): Graphics.DisplayMode {
    throw Error("Not supported")
  }

  override fun setFullscreenMode(displayMode: Graphics.DisplayMode): Boolean {
    throw Error("Not supported")
  }


  override fun setWindowedMode(width: Int, height: Int): Boolean {
    throw Error("Not supported")
  }

  override fun setTitle(title: String?) {
    throw Error("Not supported")
  }

  override fun setUndecorated(undecorated: Boolean) {
    throw Error("Not supported")
  }

  override fun setResizable(resizable: Boolean) {
    throw Error("Not supported")
  }

  override fun setVSync(vsync: Boolean) {
    throw Error("Not supported")
  }

  override fun getBufferFormat(): Graphics.BufferFormat? {
    return bufferFormat
  }

  override fun supportsExtension(extension: String): Boolean {
    return GLFW.glfwExtensionSupported(extension)
  }

  override fun setContinuousRendering(isContinuous: Boolean) {
    throw Error("Not supported")
  }

  override fun isContinuousRendering(): Boolean {
    throw Error("Not supported")
  }

  override fun requestRendering() {
    throw Error("Not supported")
  }

  override fun isFullscreen(): Boolean {
    throw Error("Not supported")
  }

  override fun newCursor(pixmap: Pixmap, xHotspot: Int, yHotspot: Int): Cursor {
    throw Error("Not supported")
  }

  override fun setCursor(cursor: Cursor) {
    throw Error("Not supported")
  }

  override fun setSystemCursor(systemCursor: SystemCursor) {
    throw Error("Not supported")
  }
}
