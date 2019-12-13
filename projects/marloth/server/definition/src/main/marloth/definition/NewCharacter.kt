package marloth.definition

import simulation.intellect.Spirit
import silentorb.mythic.ent.Id
import silentorb.mythic.spatial.Pi
import silentorb.mythic.spatial.Quaternion
import silentorb.mythic.spatial.Vector2
import silentorb.mythic.spatial.Vector3
import marloth.scenery.AnimationId
import silentorb.mythic.physics.Body
import silentorb.mythic.physics.DynamicBody
import silentorb.mythic.scenery.Capsule
import simulation.entities.*
import simulation.main.Hand
import simulation.main.HandAttachment
import simulation.misc.Definitions
import simulation.misc.ResourceContainer
import simulation.physics.*

fun newCharacter(definitions: Definitions, definition: CharacterDefinition, faction: Id, position: Vector3,
                 angle: Float = Pi / 2f,
                 spirit: Spirit? = null): Hand {
  return Hand(
      ambientAudioEmitter = if (definition.ambientSounds.any())
        AmbientAudioEmitter(
            delay = position.length() % 2.0
        )
      else
        null,
      animation = CharacterAnimation(
          animations = listOf(
              SingleAnimation(
                  animationId = AnimationId.stand.name,
                  animationOffset = 0f
              )
          )
      ),
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
//          abilities = abilities,
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
          type = definition.depictionType
      ),
      dynamicBody = DynamicBody(
          gravity = true,
          mass = 45f,
          resistance = 4f
      ),
      spirit = spirit,
      attachments = definition.accessories
          .mapIndexed { index, type ->
            HandAttachment(
                category = AttachmentCategory.ability,
                index = index,
                hand = Hand(
                    accessory = Accessory(
                        type = type
                    ),
                    action = newPossibleAction(definitions, type)
                )
            )
          }
  )
}
