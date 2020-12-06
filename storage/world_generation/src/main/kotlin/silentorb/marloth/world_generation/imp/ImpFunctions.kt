package silentorb.marloth.world_generation.imp

import marloth.scenery.enums.MeshInfoMap
import silentorb.imp.core.CompleteParameter
import silentorb.imp.core.CompleteSignature
import silentorb.imp.core.PathKey
import silentorb.imp.core.stringType
import silentorb.imp.execution.CompleteFunction
import silentorb.marloth.world_generation.GetSpatialNode
import silentorb.marloth.world_generation.SpatialNode
import silentorb.mythic.fathom.spatial.quaternionType
import silentorb.mythic.fathom.spatial.translation3Type
import silentorb.mythic.fathom.spatial.vector3Type
import silentorb.mythic.physics.CollisionObject
import silentorb.mythic.scenery.MeshName
import silentorb.mythic.spatial.Quaternion
import silentorb.mythic.spatial.Vector3
import simulation.entities.Depiction
import simulation.main.Hand
import simulation.physics.CollisionGroups

fun newMeshHand(meshes: MeshInfoMap, meshName: MeshName): Hand {
  val meshInfo = meshes[meshName]
  val shape = meshInfo?.shape
  return Hand(
      depiction = Depiction(mesh = meshName),
      collisionShape = if (shape != null)
        CollisionObject(
            shape = shape,
            groups = CollisionGroups.solidStatic,
            mask = CollisionGroups.staticMask
        )
      else
        null
  )
}

fun worldGenerationFunctions() = listOf(

    CompleteFunction(
        path = PathKey(worldGenerationPath, "mesh"),
        signature = CompleteSignature(
            parameters = listOf(
                CompleteParameter("name", stringType),
            ),
            output = spatialNodeType
        ),
        implementation = { arguments ->
          val meshName = arguments["name"] as String
          val result: GetSpatialNode = { input ->
            SpatialNode(
                hand = newMeshHand(input.meshes, meshName)
            )
          }
          result
        }
    ),

    CompleteFunction(
        path = PathKey(worldGenerationPath, "node"),
        signature = CompleteSignature(
            parameters = listOf(
            ),
            output = spatialNodeType
        ),
        implementation = { arguments ->
          val result: GetSpatialNode = {
            SpatialNode()
          }
          result
        }
    ),

    CompleteFunction(
        path = PathKey(worldGenerationPath, "attribute"),
        signature = CompleteSignature(
            parameters = listOf(
                CompleteParameter("node", spatialNodeType),
                CompleteParameter("value", stringType),
            ),
            output = spatialNodeType
        ),
        implementation = { arguments ->
          val getNode = arguments["node"] as GetSpatialNode
          val value = arguments["value"] as String
          val result: GetSpatialNode = {
            val node = getNode(it)
            val hand = node.hand ?: Hand()
            node.copy(
                hand = hand.copy(
                    attributes = (hand.attributes ?: setOf()) + value
                )
            )
          }
          result
        }
    ),

    CompleteFunction(
        path = PathKey(worldGenerationPath, "node"),
        signature = CompleteSignature(
            parameters = listOf(
                CompleteParameter("children", spatialNodeListType),
            ),
            output = spatialNodeType
        ),
        implementation = { arguments ->
          val children = arguments["children"] as List<GetSpatialNode>
          val result: GetSpatialNode = { input ->
            SpatialNode(
                children = children.map { it(input) }
            )
          }
          result
        }
    ),

    CompleteFunction(
        path = PathKey(worldGenerationPath, "listOf"),
        signature = CompleteSignature(
            isVariadic = true,
            parameters = listOf(
                CompleteParameter("values", spatialNodeType)
            ),
            output = spatialNodeListType
        ),
        implementation = { arguments ->
          arguments["values"] as List<GetSpatialNode>
        }
    ),


    CompleteFunction(
        path = PathKey(worldGenerationPath, "translate"),
        signature = CompleteSignature(
            parameters = listOf(
                CompleteParameter("offset", translation3Type),
                CompleteParameter("source", spatialNodeType)
            ),
            output = spatialNodeType
        ),
        implementation = { arguments ->
          val offset = arguments["offset"] as Vector3
          transformSpatialNode(arguments) { it.translate(offset) }
        }
    ),

    CompleteFunction(
        path = PathKey(worldGenerationPath, "rotate"),
        signature = CompleteSignature(
            parameters = listOf(
                CompleteParameter("rotation", quaternionType),
                CompleteParameter("source", spatialNodeType)
            ),
            output = spatialNodeType
        ),
        implementation = { arguments ->
          val orientation = arguments["rotation"] as Quaternion
          val opposite = -orientation
          transformSpatialNode(arguments) { it.rotate(opposite) }
        }
    ),

    CompleteFunction(
        path = PathKey(worldGenerationPath, "scale"),
        signature = CompleteSignature(
            parameters = listOf(
                CompleteParameter("offset", vector3Type),
                CompleteParameter("source", spatialNodeType)
            ),
            output = spatialNodeType
        ),
        implementation = { arguments ->
          val offset = arguments["offset"] as Vector3
          transformSpatialNode(arguments) { it.scale(offset) }
        }
    ),
)
