package marloth.clienting.rendering

import marloth.clienting.Client
import marloth.clienting.gui.menus.imageDepiction
import marloth.scenery.enums.Textures
import silentorb.mythic.bloom.Box
import silentorb.mythic.bloom.OffsetBox
import silentorb.mythic.bloom.renderLayout
import silentorb.mythic.debugging.getDebugBoolean
import silentorb.mythic.lookinglass.createCanvas
import silentorb.mythic.lookinglass.pipeline.applyRenderedBuffers
import silentorb.mythic.platforming.WindowInfo
import silentorb.mythic.spatial.Matrix
import silentorb.mythic.spatial.Pi
import silentorb.mythic.spatial.Vector2i
import simulation.misc.simplifyRotation
import simulation.updating.simulationDelta
import kotlin.math.min

private var shortHandRotation = 0f
private var longHandRotation = Pi / 2f

fun renderLoadingScreen(client: Client, windowInfo: WindowInfo) {
  val dimensions = windowInfo.dimensions
  val clockBaseLength = 1024
  val length = min(clockBaseLength, min(dimensions.x, dimensions.y))
  val scale = { value: Int -> value * length / clockBaseLength }
  val clockFaceCenter = Vector2i(scale(283), scale(315))
  shortHandRotation -= simplifyRotation(1.5f * simulationDelta)
  longHandRotation += simplifyRotation(5.2f * simulationDelta)
  val box = Box(
      dimensions = Vector2i.zero,
      boxes = listOf(
          OffsetBox(
              child = Box(
                  dimensions = Vector2i(length, length),
                  depiction = imageDepiction(Textures.loadingClock),
              )
          ),
          OffsetBox(
              child = Box(
                  dimensions = Vector2i(scale(16), scale(128)),
                  depiction = imageDepiction(Textures.longHand) { b, c ->
                    Matrix.identity
                        .mul(c.pixelsToScalar)
                        .translate(b.position.x.toFloat(), b.position.y.toFloat(), 0f)
                        .rotateZ(longHandRotation)
                        .scale(b.dimensions.x.toFloat(), b.dimensions.y.toFloat(), 1f)
                        .translate(-0.5f, -0.94f, 0f)
                  },
              ),
              offset = clockFaceCenter,
          ),
          OffsetBox(
              child = Box(
                  dimensions = Vector2i(scale(32), scale(64)),
                  depiction = imageDepiction(Textures.shortHand) { b, c ->
                    Matrix.identity
                        .mul(c.pixelsToScalar)
                        .translate(b.position.x.toFloat(), b.position.y.toFloat(), 0f)
                        .rotateZ(shortHandRotation)
                        .scale(b.dimensions.x.toFloat(), b.dimensions.y.toFloat(), 1f)
                        .translate(-0.5f, -0.86f, 0f)
                  },
              ),
              offset = clockFaceCenter,
          ),
      )
  )
  val canvas = createCanvas(client.renderer, client.customBloomResources, dimensions)
  applyRenderedBuffers(client.renderer, windowInfo)
  renderLayout(box, canvas, getDebugBoolean("MARK_BLOOM_GUI_PASS"))
}
