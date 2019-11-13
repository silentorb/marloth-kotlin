package generation.architecture.building

import generation.architecture.cellLength
import mythic.spatial.Vector3
import scenery.MeshName

fun newHalfStepFloorMesh(mesh: MeshName) =
    floorMesh(mesh, Vector3(0f, 0f, cellLength / 3f))
