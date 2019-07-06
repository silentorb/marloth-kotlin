package lab.views.map

import mythic.glowing.DrawMethod
import rendering.SceneRenderer
import simulation.misc.Node

fun drawAbstractNodes(renderer: SceneRenderer, nodes: Collection<Node>) {
  for (node in nodes) {
    renderer.drawCircle(node.position, node.radius, DrawMethod.triangleFan)
  }
}
