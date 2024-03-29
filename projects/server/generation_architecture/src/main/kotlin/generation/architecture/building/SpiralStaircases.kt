package generation.architecture.building

//val curvedStaircases = blockBuilder() { input ->
//  val biome = input.biome
//  val config = input.general.config
//  val mesh = MeshId.curvingStairStep
//  val meshInfo = input.general.config.meshes[mesh]!!
//  val stepWidth = meshInfo.shape!!.x
//  val sweepLength = Pi * 2f
//
//  val baseAngle = applyTurnsOld(input.turns)
//
//  val stairHeight = cellLength
//  val heightStep = 0.3f
//  val stepCount = (stairHeight / heightStep).toInt()
//  val angleStep = sweepLength / stepCount.toFloat()
//  val baseOffset = input.position + floorOffset + align(config.meshes, alignWithCeiling)(mesh)
//  val columnRadius = 0.5f
//  val roationVector = Vector3(columnRadius + stepWidth / 2f - 0.2f, 0f, 0f)
//  (0 until stepCount).map { step ->
//    val angle = baseAngle + step * angleStep
//    val heightPosition = Vector3(0f, 0f, heightStep + step * heightStep)
//    val rotationPosition = roationVector.transform(Matrix.identity.rotateZ(angle))
//    val position = baseOffset + heightPosition + rotationPosition
//    newArchitectureMesh(
//        meshes = config.meshes,
//        mesh = mesh,
//        position = position,
//        orientation = Quaternion().rotateZ(angle),
//        texture = biomeTexture(biome, TextureGroup.floor)
//    )
//  }
//      .plus(newArchitectureMesh(
//          meshes = config.meshes,
//          mesh = MeshId.spiralStaircaseColumn,
//          position = input.position + cellCenterOffset,
//          texture = biomeTexture(biome, TextureGroup.wall)
//      ))
//}

//fun spiralStairBlocks(): Map<String, BlockBuilder> = mapOf(
//    "stairBottom" to compose(
//        setOf(CellAttribute.lockedRotation, CellAttribute.traversable),
//        blockBuilder(
//            up = spiralStaircaseTopOrBottom,
//            east = impassableCylinder,
//            north = openOrSolidCylinder,
//            south = impassableCylinder,
//            west = openOrSolidCylinder
//        ),
//        floorMesh(MeshId.squareFloor),
//        cylinderWalls(),
//        curvedStaircases
//    ),
//
//    "stairMiddle" to compose(
//        setOf(CellAttribute.lockedRotation, CellAttribute.traversable),
//        blockBuilder(
//            up = spiralStaircaseTop,
//            down = spiralStaircaseBottom,
//            east = impassableCylinder,
//            north = impassableCylinder,
//            south = impassableCylinder,
//            west = impassableCylinder
//        ),
//        cylinderWalls(),
//        curvedStaircases
//    ),
//
//    "stairTop" to compose(
//        setOf(CellAttribute.lockedRotation, CellAttribute.traversable),
//        blockBuilder(
//            up = impassableVertical,
//            down = spiralStaircaseTopOrBottom,
//            east = openOrSolidCylinder,
//            north = impassableCylinder,
//            south = openOrSolidCylinder,
//            west = impassableCylinder
//        ),
//        cylinderWalls(),
//        halfFloorMesh(MeshId.halfSquareFloor)
//    )
//)
//    .mapValues(mapEntryValue(withCellAttributes(
//        setOf(
//            CellAttribute.lockedRotation,
//            CellAttribute.spiralStaircase,
//            CellAttribute.traversable
//        )
//    )))
