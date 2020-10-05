package silentorb.marloth.world_generation.imp

import silentorb.imp.core.CompleteParameter
import silentorb.imp.core.CompleteSignature
import silentorb.imp.core.PathKey
import silentorb.imp.core.stringType
import silentorb.imp.execution.CompleteFunction
import silentorb.marloth.world_generation.GetHand
import silentorb.marloth.world_generation.GetSpatialNode
import silentorb.marloth.world_generation.SpatialNode
import silentorb.mythic.fathom.spatial.quaternionType
import silentorb.mythic.fathom.spatial.translation3Type
import silentorb.mythic.fathom.spatial.vector3Type
import silentorb.mythic.physics.CollisionObject
import silentorb.mythic.spatial.Quaternion
import silentorb.mythic.spatial.Vector3
import simulation.entities.Depiction
import simulation.main.Hand
import simulation.physics.CollisionGroups

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
          val result: GetHand = { input ->
            val meshInfo = input.meshes[meshName]
            val shape = meshInfo?.shape
            Hand(
                depiction = Depiction(mesh = meshName),
                collisionShape = if (shape != null)
                  CollisionObject(
                      shape = shape,
                      groups = CollisionGroups.static or CollisionGroups.affectsCamera or CollisionGroups.walkable,
                      mask = CollisionGroups.staticMask
                  )
                else
                  null
            )
          }
          result
        }
    ),

    CompleteFunction(
        path = PathKey(worldGenerationPath, "endpoint"),
        signature = CompleteSignature(
            parameters = listOf(
                CompleteParameter("hand", handType),
            ),
            output = spatialNodeType
        ),
        implementation = { arguments ->
          val getHand = arguments["hand"] as GetHand
          val result: GetSpatialNode = {
            SpatialNode(
                getHand = getHand
            )
          }
          result
        }
    ),

    CompleteFunction(
        path = PathKey(worldGenerationPath, "parentNode"),
        signature = CompleteSignature(
            parameters = listOf(
                CompleteParameter("children", spatialNodeListType),
            ),
            output = spatialNodeType
        ),
        implementation = { arguments ->
          val children = arguments["hand"] as List<GetSpatialNode>
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
