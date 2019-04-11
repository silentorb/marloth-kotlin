package lab.views.game

import com.badlogic.gdx.physics.bullet.linearmath.btIDebugDraw
import com.badlogic.gdx.utils.Disposable
import marloth.front.GameApp
import marloth.front.RenderHook
import mythic.spatial.Vector3
import mythic.spatial.Vector4
import mythic.spatial.manhattanDistance
import physics.toVector3
import rendering.SceneRenderer
import com.badlogic.gdx.math.Vector3 as GdxVector3

class BulletDebugDrawer : btIDebugDraw(), Disposable {
  var _debugMode = DebugDrawModes.DBG_NoDebug
  var sceneRenderer: SceneRenderer? = null
  val lineData = mutableMapOf<Vector3, MutableList<Float>>()
  var origin: Vector3 = Vector3()

  override fun setDebugMode(debugMode: Int) {
    this._debugMode = debugMode
  }

  override fun getDebugMode(): Int {
    return _debugMode
  }

  override fun drawLine(from: GdxVector3, to: GdxVector3, color: GdxVector3) {
    if (manhattanDistance(origin, toVector3(from)) > 40f)
      return

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

fun drawBulletDebug(gameApp: GameApp, origin: Vector3): RenderHook = { sceneRenderer: SceneRenderer ->
  val dynamicsWorld = gameApp.bulletState.dynamicsWorld
  val drawer = if (debugDrawer == null) {
    debugDrawer = BulletDebugDrawer()
    debugDrawer!!.debugMode = btIDebugDraw.DebugDrawModes.DBG_DrawWireframe
    debugDrawer!!
  } else
    debugDrawer!!

  dynamicsWorld.setDebugDrawer(drawer)

  drawer.sceneRenderer = sceneRenderer
  drawer.origin = origin
  dynamicsWorld.debugDrawWorld()
  for ((color, data) in drawer.lineData) {
    sceneRenderer.drawLines(data, Vector4(color.x, color.y, color.z, 1f))
  }
  drawer.lineData.forEach { it.value.clear() }
  drawer.sceneRenderer = null
}