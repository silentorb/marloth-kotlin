package marloth.clienting.rendering

import marloth.scenery.enums.Textures
import silentorb.mythic.ent.Id
import silentorb.mythic.lookinglass.Material
import silentorb.mythic.lookinglass.MeshElement
import silentorb.mythic.scenery.MeshName
import simulation.accessorize.getFirstAccessory
import simulation.entities.Depiction
import simulation.main.Deck

fun berryBushDepiction(deck: Deck, id: Id, mesh: MeshName, depiction: Depiction): MeshElement? {
  val accessory = getFirstAccessory(deck.accessories, id)
  val texture = when (accessory?.quantity) {
    0 -> Textures.leafFloor
    1 -> Textures.leafBerriesLess
    else -> Textures.leafBerries
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
