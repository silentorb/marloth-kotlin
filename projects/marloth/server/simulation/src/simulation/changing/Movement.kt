package simulation.changing

import commanding.CommandType
import haft.Commands
import mythic.sculpting.FlexibleEdge
import mythic.sculpting.FlexibleFace
import mythic.spatial.*
import org.joml.minus
import org.joml.plus
import org.joml.times
import org.joml.xy
import physics.Body
import physics.Force
import simulation.*

fun hitsWall(edge: FlexibleEdge, position: Vector3, radius: Float) =
    lineIntersectsCircle(edge.first.xy, edge.second.xy, position.xy, radius)

fun getEdgeNormal(first: Vector2, second: Vector2): Vector2 {
  val combination = first - second
  return Vector2(combination.y, -combination.x).normalize()
}

fun getCollisionWalls(world: AbstractWorld, node: Node): Sequence<FlexibleFace> {
  return node.neighbors
      .plus(node)
      .flatMap { it.walls.asSequence() }
      .filter(isWall)
      .distinct()
}

fun findCollisionWalls(source: Vector3, originalOffset: Vector3, world: AbstractWorld, node: Node): List<Triple<FlexibleFace, Vector2, Float>> {
  val offset = originalOffset + 0f
  val maxLength = offset.length()
  val newPosition = source + offset
  val radius = 0.8f
  val broadRadius = radius + maxLength
  val walls = getCollisionWalls(world, node)
//  val walls = world.walls
//      .filter(isWall)
  return walls
      .filter { wall -> hitsWall(wall.edges[0].edge, newPosition, broadRadius) && offset.dot(wall.normal) < 0f }
      .map {
        val edge = it.edges[0]
        val hitPoint = projectPointOntoLine(source.xy, edge.first.xy, edge.second.xy)
        if (hitPoint != null) {
          val gap = hitPoint.distance(source.xy) - radius
          Triple(it, hitPoint, gap)
        } else {
          val nearestEnd = listOf(edge.first.xy, edge.second.xy).sortedBy { it.distance(source.xy) }.first()
          val gap = nearestEnd.distance(source.xy) - radius
          Triple(it, nearestEnd, gap)
        }
      }
      .sortedBy { it.first.normal.dot(offset) }
      .toList()
}

data class WallCollision(
    val walls: List<FlexibleFace>,
    val offset: Vector3
)

fun getWallCollisionMovementOffset(walls: List<Triple<FlexibleFace, Vector2, Float>>, offset: Vector3): WallCollision {
  if (walls.size > 3) {
//    throw Error("Not supported.")
    // TODO Uncomment above line
  }
  val slideVectors = walls.map { (face, _, _) ->
    face.normal * face.normal.dot(offset)
  }

  val gapVectors = walls.map { (_, _, gap) ->
    ((offset + 0f).normalize() * gap)
  }
  if (walls.size == 2) {
//      val gapVector = gapVectors[0] + gapVectors[1]
//      offset = gapVector
    val firstEdge = walls[0].first.edges[0]
    val secondEdge = walls[1].first.edges[0]

    // Get the points of either edge ordered by the shared point and then the unshared point
    val rightEdge = if (firstEdge.first === secondEdge.second)
      secondEdge
    else
      firstEdge

    val knee = walls[0].first.normal + walls[1].first.normal
    val angle = getAngle(
        (rightEdge.first - rightEdge.second).xy.normalize(),
        knee.xy.normalize()
    )
//    println("" + angle + " | " + knee.xy + " | " + slideVectors[0].dot(walls[1].first.normal) + " | " + slideVectors[1].dot(walls[0].first.normal))
//    if (Math.abs(angle) <= Pi) {
    if (Math.abs(slideVectors[1].dot(walls[0].first.normal)) > 0.05f) {
      val dot2 = offset.dot(walls[0].first.normal + walls[0].first.normal)
//      println(dot2)
//        if (dot2 < 0f) {
      return WallCollision(walls.map { it.first }, gapVectors[0] + gapVectors[1])
//        println(offset)
//        }
    }
  }
//    else {
//      return offset + gapVectors[0] - slideVectors[0]
//    }
////      println("2 " + walls[0].first.normal + " | " + offset + " | " + gapVectors[0] + " | " + slideVectors[0])
//  } else {
////      println("1 " + walls[0].first.normal + " | " + offset + " | " + gapVectors[0] + " | " + slideVectors[0])
////      println("1 " + walls[0].first.normal + " | " + offset)
//  }
  return WallCollision(walls.map { it.first }, offset + gapVectors[0] - slideVectors[0])
}

fun checkWallCollision(source: Vector3, originalOffset: Vector3, world: AbstractWorld, node: Node): WallCollision {
  var offset = originalOffset + 0f
  val maxLength = offset.length()
  val walls = findCollisionWalls(source, originalOffset, world, node)

  if (walls.size > 0) {
    val collision = getWallCollisionMovementOffset(walls, offset)
    offset = collision.offset
    val offsetLength = offset.length()
    if (offsetLength > maxLength) {
      offset.normalize().mul(maxLength)
    }
    return WallCollision(collision.walls, source + offset)
  } else {
    return WallCollision(listOf(), source + offset)
  }
}

val playerMoveMap = mapOf(
    CommandType.moveLeft to Vector3(-1f, 0f, 0f),
    CommandType.moveRight to Vector3(1f, 0f, 0f),
    CommandType.moveUp to Vector3(0f, 1f, 0f),
    CommandType.moveDown to Vector3(0f, -1f, 0f)
)

val playerLookMapFP = mapOf(
    CommandType.lookLeft to Vector3(0f, 0f, 1f),
    CommandType.lookRight to Vector3(0f, 0f, -1f),
    CommandType.lookUp to Vector3(0f, -1f, 0f),
    CommandType.lookDown to Vector3(0f, 1f, 0f)
)

val playerLookMapTP = mapOf(
    CommandType.lookLeft to Vector3(0f, 0f, 1f),
    CommandType.lookRight to Vector3(0f, 0f, -1f),
    CommandType.lookUp to Vector3(0f, 1f, 0f),
    CommandType.lookDown to Vector3(0f, -1f, 0f)
)

val playerAttackMap = mapOf(
    CommandType.lookLeft to Vector3(-1f, 0f, 0f),
    CommandType.lookRight to Vector3(1f, 0f, 0f),
    CommandType.lookUp to Vector3(0f, 1f, 0f),
    CommandType.lookDown to Vector3(0f, -1f, 0f)
)

fun joinInputVector(commands: Commands<CommandType>, commandMap: Map<CommandType, Vector3>): Vector3? {
  val forces = commands.mapNotNull {
    val vector = commandMap[it.type]
    if (vector != null && it.value > 0)
      vector * it.value
    else
      null
  }
  if (forces.isEmpty())
    return null

  val offset = forces.reduce { a, b -> a + b }
  offset.normalize()
  assert(!offset.x.isNaN() && !offset.y.isNaN())
  return offset
}

fun getLookAtAngle(lookAt: Vector3) =
    getAngle(Vector2(1f, 0f), lookAt.xy)

fun setCharacterFacing(character: Character, lookAt: Vector3) {
  val angle = getLookAtAngle(lookAt)
  character.facingRotation.z = angle
}

fun playerMove(player: Player, commands: Commands<CommandType>): Force? {
  var offset = joinInputVector(commands, playerMoveMap)

  if (offset != null) {
    if (player.viewMode == ViewMode.firstPerson) {
      offset = (Quaternion().rotateZ(player.character.facingRotation.z - Pi / 2) * offset)!!
    } else if (player.viewMode == ViewMode.thirdPerson) {
      offset = (Quaternion().rotateZ(player.hoverCamera.yaw + Pi / 2) * offset)!!
      setCharacterFacing(player.character, offset)
    } else {
      setCharacterFacing(player.character, offset)
    }
    return Force(player.character.body, offset, maximum = 6f)
  } else {
    return null
  }
}

fun playerRotateFP(player: Player, commands: Commands<CommandType>, delta: Float) {
  val speed = Vector3(1f, 1.8f, 5.4f)
  val offset = joinInputVector(commands, playerLookMapFP)

  if (offset != null) {
    player.lookVelocity += offset * speed * delta
  }
}

fun playerRotateTP(player: Player, commands: Commands<CommandType>, delta: Float) {
  val speed = Vector3(1f, 3.4f, 6.4f)
//  val speed = Vector3(1f, 3.4f, 1f)
  val offset = joinInputVector(commands, playerLookMapTP)

  if (offset != null) {
    player.lookVelocity += offset * speed * delta
  }
}

data class MomentumConfig(
    val max: Float,
    val drag: Float
)

data class MomentumConfig2(
    val yaw: MomentumConfig,
    val pitch: MomentumConfig
)

private val thirdPersonLookMomentum = MomentumConfig2(
    MomentumConfig(1.7f, 4f),
    MomentumConfig(1f, 4f)
)

private val firstPersonLookMomentum = MomentumConfig2(
    MomentumConfig(3f, 4f),
    MomentumConfig(1f, 4f)
)


fun updatePlayerRotation(player: Player, delta: Float) {
//  if (player.viewMode == ViewMode.topDown)
//    return

  val velocity = player.lookVelocity
  val deltaVelocity = velocity * delta
  if (velocity.y != 0f || velocity.z != 0f) {
    val m = if (player.viewMode == ViewMode.firstPerson)
      firstPersonLookMomentum
    else
      thirdPersonLookMomentum

    if (player.viewMode == ViewMode.firstPerson)
      player.character.facingRotation += deltaVelocity
    else {
      val hoverCamera = player.hoverCamera
      hoverCamera.pitch += deltaVelocity.y
      hoverCamera.yaw += deltaVelocity.z
      val hoverPitchMin = -1.0f // Up
      val hoverPitchMax = 0.0f // Down

      if (hoverCamera.pitch > hoverPitchMax)
        hoverCamera.pitch = hoverPitchMax

      if (hoverCamera.pitch < hoverPitchMin)
        hoverCamera.pitch = hoverPitchMin

//      println("p " + hoverCamera.pitch + ", y" + hoverCamera.yaw + " |  vp " + player.lookVelocity.y + ",vy " + player.lookVelocity.z)
    }
    player.lookVelocity.y = Math.min(m.pitch.max, velocity.y * (1 - m.pitch.drag * delta))
    player.lookVelocity.z = Math.min(m.yaw.max, velocity.z * (1 - m.yaw.drag * delta))
    if (Vector2(velocity.y, velocity.z).length() < 0.01f) {
      player.lookVelocity.zero()
    }

  }
}

fun isInsideNode(node: Node, position: Vector3) =
    node.floors.any { isInsidePolygon(position, it.vertices) }

fun updateBodyNode(body: Body) {
  val position = body.position
  val node = body.node

  if (isInsideNode(node, position))
    return

  val newNode = node.neighbors.firstOrNull { isInsideNode(it, position) }
  if (newNode == null) {
    isInsideNode(node, position)
//    throw Error("Not supported")
//    assert(false)
  } else {
    body.node = newNode
  }
}