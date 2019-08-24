package generation.architecture

import generation.misc.MeshAttribute
import generation.misc.QueryMode
import generation.misc.queryMeshes
import mythic.spatial.Pi
import mythic.spatial.Vector3
import simulation.physics.old.getLookAtAngle

val placeTunnelWalls: Architect = { config, realm, dice ->
  tunnelNodes(realm.graph)
      .flatMap { node ->
        val segmentLength = 4f
        val info = getTunnelInfo(realm.graph, node.id)
        val lookAtAngle = getLookAtAngle(info.vector)
        val halfWidth = standardTunnelWidth / 2f
        val biome = config.biomes[node.biome]!!
        createSeries(info.length, segmentLength, -0f) { step, stepOffset ->
          val minorOffset = 0.001f
          val minorMod = if (step % 2 == 0) -minorOffset else minorOffset
          val minor = Vector3(0f, 0f, minorMod)
          listOf(-1f, 1f)
              .filter { wallPlacementFilter(dice, biome) }
              .map { sideMod ->
                val randomFlip = if (dice.getBoolean()) 1 else -1
                val sideOffset = Vector3(info.vector.y, -info.vector.x, 0f) * (sideMod + minorMod) * halfWidth
                val wallPosition = info.start + info.vector * stepOffset + sideOffset
                val wallAngle = lookAtAngle + sideMod * randomFlip * Pi / 2f
                val mesh = dice.takeOne(
                    queryMeshes(config.meshes, biome, setOf(MeshAttribute.placementWall))
                )
                newWall(config, mesh, dice, node, wallPosition, wallAngle)
              }
        }.flatten()
      }
}
