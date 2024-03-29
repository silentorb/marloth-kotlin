package simulation.intellect.navigation

import org.recast4j.detour.crowd.CrowdAgentParams
import org.recast4j.detour.crowd.debug.CrowdAgentDebugInfo
import silentorb.mythic.ent.Id
import silentorb.mythic.ent.Table
import silentorb.mythic.intellect.navigation.fromRecastVector3
import silentorb.mythic.intellect.navigation.toRecastVector3
import silentorb.mythic.spatial.Vector3
import simulation.characters.MoveSpeedTable
import simulation.main.Deck

fun newCrowdAgentParams(actor: Id, maxSpeed: Float): CrowdAgentParams {
  val params = CrowdAgentParams()

  params.radius = agentRadius
  params.height = agentHeight
  params.maxAcceleration = 8f
  params.maxSpeed = maxSpeed
  params.userData = actor
  params.collisionQueryRange = params.radius * 12f
  params.pathOptimizationRange = params.radius * 30f
  params.separationWeight = 2f
  params.obstacleAvoidanceType = 3 // Don't know what this represents but got it from a working demo.  I assume it's two bitwise flags.
  params.updateFlags = CrowdAgentParams.DT_CROWD_ANTICIPATE_TURNS or CrowdAgentParams.DT_CROWD_OPTIMIZE_VIS or CrowdAgentParams.DT_CROWD_OPTIMIZE_TOPO or CrowdAgentParams.DT_CROWD_OBSTACLE_AVOIDANCE

  return params
}

fun mythicToDetour(deck: Deck, moveSpeedTable: MoveSpeedTable, navigation: NavigationState): NavigationState {
  val crowd = navigation.crowd
  val spirits = deck.spirits
  val agents = navigation.agents
  val missing = spirits - agents.keys
  val removed = agents - spirits.keys

  val newAgents = missing.mapValues { (actor, _) ->
    val body = deck.bodies[actor]!!
    val agent = crowd.addAgent(toRecastVector3(body.position), newCrowdAgentParams(actor, moveSpeedTable[actor]!!))
    assert(agent != -1)
    agent
  }

  val updated = (agents - removed.keys)

  for ((actor, agent) in updated) {
    val editable = crowd.getEditableAgent(agent)!!
    val body = deck.bodies[actor]!!
    editable.npos = toRecastVector3(body.position)
    val targetPosition = spirits[actor]!!.pursuit?.targetPosition
    if (targetPosition != null) {
      val nearest = nearestPolygon(navigation, targetPosition)
      if (nearest != null) {
        crowd.requestMoveTarget(agent, nearest.result.nearestRef, nearest.result.nearestPos)
      }
    }
  }

  for (agent in removed.values) {
    crowd.removeAgent(agent)
  }

  return navigation.copy(
      agents = updated + newAgents
  )
}

private val debugInfo = CrowdAgentDebugInfo()

fun updateNavigation(deck: Deck, moveSpeedTable: MoveSpeedTable, delta: Float, navigation: NavigationState): NavigationState {
  val nextNavigation = mythicToDetour(deck, moveSpeedTable, navigation)
  val crowd = nextNavigation.crowd
  crowd.update(delta, debugInfo)
  return nextNavigation
}

fun updateNavigationDirections(navigation: NavigationState): Table<NavigationDirection> =
    navigation.agents.mapValues { (_, agent) ->
      val crowd = navigation.crowd
      val record = crowd.getAgent(agent)!!
      val velocity = fromRecastVector3(record.vel)
      if (velocity == Vector3.zero)
        Vector3.zero
      else
        velocity.normalize()
    }
