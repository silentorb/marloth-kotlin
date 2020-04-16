package lab.views.map

import silentorb.mythic.glowing.DrawMethod
import silentorb.mythic.lookinglass.SceneRenderer
import simulation.misc.Node

fun drawAbstractNodes(renderer: SceneRenderer, nodes: Collection<Node>) {
  for (node in nodes) {
    renderer.drawCircle(node.position, node.radius, DrawMethod.triangleFan)
  }
}
