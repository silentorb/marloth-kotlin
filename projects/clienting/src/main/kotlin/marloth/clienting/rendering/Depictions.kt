package marloth.clienting.rendering

import marloth.scenery.enums.Textures
import silentorb.mythic.ent.Id
import silentorb.mythic.lookinglass.Material
import silentorb.mythic.lookinglass.MeshElement
import silentorb.mythic.scenery.MeshName
import simulation.entities.Depiction
import simulation.main.Deck

fun berryBushDepiction(deck: Deck, id: Id, mesh: MeshName, depiction: Depiction): MeshElement? {
  val accessory = deck.accessories[id]
  val texture = when (accessory?.quantity) {
    1 -> Textures.leafBerriesLess
    2 -> Textures.leafBerries
    else -> Textures.leafFloor
  }
  val material = depiction.material?.copy(
      texture = texture,
  )
      ?: Material(
          texture = texture,
          shading = true,
      )
  return convertSimpleDepiction(deck, id, mesh, material)
}
