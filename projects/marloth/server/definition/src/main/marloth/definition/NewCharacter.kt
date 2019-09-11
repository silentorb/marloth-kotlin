package marloth.definition

import simulation.intellect.Spirit
import mythic.ent.Id
import mythic.ent.newIdSource
import mythic.spatial.Pi
import mythic.spatial.Quaternion
import mythic.spatial.Vector2
import mythic.spatial.Vector3
import scenery.AnimationId
import scenery.Capsule
import simulation.entities.*
import simulation.main.Hand
import simulation.misc.ResourceContainer
import simulation.physics.*

fun newCharacter(definition: CharacterDefinition, faction: Id, position: Vector3, angle: Float = Pi / 2f,
                 spirit: Spirit? = null): Hand {
  val nextId = newIdSource(1)
  val abilities = definition.abilities.map {
    Ability(
        id = nextId(),
        definition = it
    )
  }
  return Hand(
      ambientAudioEmitter = if (definition.ambientSounds.any())
        AmbientAudioEmitter(
            delay = position.length() % 2.0
        )
      else
        null,
      body = Body(
          position = position,
          orientation = Quaternion(),
          velocity = Vector3()
      ),
      character = Character(
          definition = definition,
          turnSpeed = Vector2(3f, 1f),
          facingRotation = Vector3(0f, 0f, angle),
          faction = faction,
          sanity = ResourceContainer(100),
          abilities = abilities,
          money = 30
      ),
      destructible = Destructible(
          base = DestructibleBaseStats(
              health = definition.health,
              damageMultipliers = definition.damageMultipliers
          ),
          health = ResourceContainer(definition.health),
          damageMultipliers = definition.damageMultipliers
      ),
      collisionShape = CollisionObject(
          shape = Capsule(defaultCharacterRadius, defaultCharacterHeight)
      ),
      depiction = Depiction(
          type = definition.depictionType,
          animations = listOf(
              DepictionAnimation(
                  animationId = AnimationId.stand,
                  animationOffset = 0f
              )
          )
      ),
      dynamicBody = DynamicBody(
          gravity = true,
          mass = 45f,
          resistance = 4f
      ),
      spirit = spirit
  )
}
