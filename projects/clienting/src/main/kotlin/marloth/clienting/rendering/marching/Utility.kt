package marloth.clienting.rendering.marching

import silentorb.mythic.fathom.mergeDistanceFunctions
import silentorb.mythic.fathom.mergeDistanceFunctionsTrackingIds
import silentorb.mythic.fathom.misc.DistanceFunction
import silentorb.mythic.fathom.misc.ModelFunction
import silentorb.mythic.fathom.misc.ModelFunctionMap
import silentorb.mythic.fathom.transform
import silentorb.mythic.lookinglass.ElementGroups
import silentorb.mythic.lookinglass.MeshElement
import silentorb.mythic.spatial.Vector3
import silentorb.mythic.spatial.Vector3i
import silentorb.mythic.spatial.toVector3
import kotlin.math.floor

fun filterModels(models: ModelFunctionMap, elements: ElementGroups): List<MeshElement> =
    elements
        .flatMap { element ->
          element.meshes.filter { mesh ->
            models.containsKey(mesh.mesh)
          }
        }

fun mapElementTransforms(models: ModelFunctionMap, elements: List<MeshElement>): List<ModelFunction> =
    elements
        .map { mesh ->
          val model = models[mesh.mesh]!!
          model.copy(
              form = transform(mesh.transform, model.form)
          )
        }

fun elementsToDistanceFunction(models: ModelFunctionMap, elements: List<MeshElement>): DistanceFunction =
    mergeDistanceFunctionsTrackingIds(mapElementTransforms(models, elements))

fun toCellVector3i(offset: Vector3): Vector3i =
    Vector3i(
        floor(offset.x).toInt(),
        floor(offset.y).toInt(),
        floor(offset.z).toInt()
    )
