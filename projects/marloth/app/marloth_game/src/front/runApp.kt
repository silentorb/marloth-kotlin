package front

import generation.generateDefaultWorld
import marloth.clienting.Client
import mythic.glowing.SimpleMesh
import mythic.platforming.Platform
import mythic.quartz.DeltaTimer
import mythic.spatial.Vector4
import rendering.Renderer
import rendering.convertMesh
import rendering.temporaryVertexSerializer
import simulation.*
import visualizing.createScene

fun runApp(platform: Platform) {
  val display = platform.display
  val timer = DeltaTimer()
  val world = generateDefaultWorld()
  val client = Client(platform)
  setWorldMesh(world.meta.structureWorld, client)

  while (!platform.process.isClosing()) {
    display.swapBuffers()
    val scene = createScene(world, client.screens[0])
    val commands = client.update(scene)
    val delta = timer.update().toFloat()
    updateWorld(world, commands, delta)
    platform.process.pollEvents()
  }
}

fun convertWorldMesh(structureWorld: StructureWorld, renderer: Renderer): SimpleMesh {
  return convertMesh(structureWorld.mesh, renderer.vertexSchemas.standard, temporaryVertexSerializer)
}

fun setWorldMesh(structureWorld: StructureWorld, client: Client) {
  client.renderer.worldMesh = convertWorldMesh(structureWorld, client.renderer)
}