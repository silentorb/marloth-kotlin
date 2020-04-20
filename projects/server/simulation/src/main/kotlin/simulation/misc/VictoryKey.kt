package simulation.misc

import marloth.scenery.enums.AccessoryId
import marloth.scenery.enums.MeshId
import silentorb.mythic.accessorize.Accessory
import silentorb.mythic.accessorize.ChangeItemOwnerEvent
import silentorb.mythic.ent.Id
import silentorb.mythic.ent.IdSource
import silentorb.mythic.ent.Table
import silentorb.mythic.happenings.Events
import silentorb.mythic.happenings.GameEvent
import silentorb.mythic.physics.Body
import silentorb.mythic.physics.CollisionObject
import silentorb.mythic.scenery.Light
import silentorb.mythic.scenery.LightType
import silentorb.mythic.scenery.Sphere
import silentorb.mythic.spatial.Pi
import silentorb.mythic.spatial.Vector3
import silentorb.mythic.spatial.Vector3i
import silentorb.mythic.spatial.Vector4
import simulation.accessorize.ItemPickup
import simulation.entities.Depiction
import simulation.entities.DepictionType
import simulation.entities.Spinner
import simulation.happenings.Trigger
import simulation.main.Deck
import simulation.main.Hand
import simulation.main.IdHand
import simulation.main.World

fun getAllVictoryKeys(accessories: Table<Accessory>): Table<Accessory> =
    accessories
        .filterValues { it.type == AccessoryId.victoryKey.name }

// Needs body
fun newVictoryKey(owner: Id = 0L) =
    Hand(
        accessory = Accessory(
            type = AccessoryId.victoryKey.name,
            owner = owner
        ),
        spinner = Spinner(Pi)
    )

fun newSpatialVictoryKey(location: Vector3): Hand {
  val mesh = MeshId.key.name
  return Hand(
      depiction = Depiction(
          type = DepictionType.staticMesh,
          mesh = mesh
      ),
      body = Body(
          position = location + Vector3(0f, 0f, 1.2f)
      )
  )
}

fun newVictoryKeyLight(offset: Vector3) =
    Light(
        type = LightType.point,
        color = Vector4(1f, 0.7f, 0f, 1f),
        offset = offset,
        range = 7f
    )

fun newVictoryKeyPickup(nextId: IdSource): (Vector3) -> List<IdHand> = { location ->
  val id = nextId()
  listOf(
      IdHand(
          id = id,
          hand = newVictoryKey()
      ),
      IdHand(
          id = id,
          hand = newSpatialVictoryKey(location)
              .copy(
                  collisionShape = CollisionObject(
                      shape = Sphere(1.2f),
                      isSolid = false
                  ),
                  trigger = Trigger(),
                  light = newVictoryKeyLight(Vector3(0f, 0f, 1f)),
                  itemPickup = ItemPickup()
              )
      )
  )
}

data class PlaceVictoryKeyEvent(
    val item: Id,
    val cell: Vector3i
) : GameEvent

fun eventsFromVictoryKeys(world: World): Events {
  val deck = world.deck
  val grid = world.realm.grid

  val keys = getAllVictoryKeys(deck.accessories)
  val carriedKeys = keys.filterValues { deck.characters.containsKey(it.owner) }
  return carriedKeys.mapNotNull { (item, accessory) ->
    if (isAtHome(grid, deck)(accessory.owner)) {
      listOf(
          PlaceVictoryKeyEvent(
              item = item,
              cell = getPointCell(deck.bodies[accessory.owner]!!.position)
          ),
          ChangeItemOwnerEvent(
              item = item,
              newOwner = 0L
          )
      )
    } else
      null
  }
      .flatten()
}

fun placeVictoryKeys(grid: MapGrid, deck: Deck, events: Events): List<IdHand> {
  val placementEvents = events.filterIsInstance<PlaceVictoryKeyEvent>()
  return placementEvents.map { event ->
    val victoryKeyStats = getVictoryKeyStats(grid, deck)
    val spacing = 1f
    val startOffset = -(victoryKeyStats.total.toFloat() * spacing) / 2f
    val offset = Vector3(0f, startOffset + 1f * victoryKeyStats.collected.toFloat(), 0f) + floorOffset
    val location = getCellPoint(event.cell) + offset
    IdHand(
        id = event.item,
        hand = newSpatialVictoryKey(location)
    )
  }
}
