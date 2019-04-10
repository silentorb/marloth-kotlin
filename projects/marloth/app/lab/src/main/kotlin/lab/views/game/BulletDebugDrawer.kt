package lab.views.game

import com.badlogic.gdx.physics.bullet.linearmath.btIDebugDraw
import com.badlogic.gdx.utils.Disposable
import marloth.front.GameApp
import marloth.front.RenderHook
import mythic.spatial.Vector3
import mythic.spatial.Vector4
import physics.toVector3
import rendering.SceneRenderer
import com.badlogic.gdx.math.Vector3 as GdxVector3

class BulletDebugDrawer() : btIDebugDraw(), Disposable {
  var _debugMode = btIDebugDraw.DebugDrawModes.DBG_NoDebug
  var sceneRenderer: SceneRenderer? = null
  val lineData = mutableMapOf<Vector3, MutableList<Float>>()

  override fun setDebugMode(debugMode: Int) {
    this._debugMode = debugMode
  }

  override fun getDebugMode(): Int {
    return _debugMode
  }

  override fun drawLine(from: GdxVector3, to: GdxVector3, color: GdxVector3) {
//    sceneRenderer!!.drawLine(toVector3(from), toVector3(to), Vector4(1f, 0f, 0f, 1f))
    val c = toVector3(color)
    val data = if (lineData.containsKey(c))
      lineData[c]!!
    else {
      lineData[c] = mutableListOf()
      lineData[c]!!
    }

    data.add(from.x)
    data.add(from.y)
    data.add(from.z)
    data.add(to.x)
    data.add(to.y)
    data.add(to.z)
  }
}

private var debugDrawer: BulletDebugDrawer? = null

fun drawBulletDebug(gameApp: GameApp): RenderHook = { sceneRenderer: SceneRenderer ->
  val dynamicsWorld = gameApp.bulletState.dynamicsWorld
  val drawer = if (debugDrawer == null) {
    debugDrawer = BulletDebugDrawer()
//          dynamicsWorld.debugDrawer = debugDrawer
    dynamicsWorld.setDebugDrawer(debugDrawer)
    dynamicsWorld.debugDrawer.debugMode = btIDebugDraw.DebugDrawModes.DBG_DrawWireframe
    debugDrawer!!
  } else
    debugDrawer!!

  drawer.sceneRenderer = sceneRenderer
  dynamicsWorld.debugDrawWorld()
  for ((color, data) in drawer.lineData) {
    sceneRenderer.drawLines(data, Vector4(color.x, color.y, color.z, 1f))
  }
  drawer.lineData.forEach { it.value.clear() }
  drawer.sceneRenderer = null
}