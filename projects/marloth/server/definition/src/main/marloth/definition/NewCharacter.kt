package marloth.definition

import marloth.scenery.AnimationId
import silentorb.mythic.ent.Id
import silentorb.mythic.physics.Body
import silentorb.mythic.physics.CollisionObject
import silentorb.mythic.physics.DynamicBody
import silentorb.mythic.rigging.characters.CharacterRig
import silentorb.mythic.rigging.characters.defaultCharacterHeight
import silentorb.mythic.rigging.characters.defaultCharacterRadius
import silentorb.mythic.scenery.Capsule
import silentorb.mythic.spatial.Pi
import silentorb.mythic.spatial.Quaternion
import silentorb.mythic.spatial.Vector2
import silentorb.mythic.spatial.Vector3
import simulation.entities.*
import simulation.intellect.Spirit
import simulation.main.Hand
import simulation.main.HandAttachment
import simulation.misc.Definitions
import simulation.misc.ResourceContainer

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
          faction = faction,
          sanity = ResourceContainer(100),
          money = 30
      ),
      characterRig = CharacterRig(
          facingRotation = Vector3(0f, 0f, angle),
          isActive = true,
          maxSpeed = definition.maxSpeed,
          turnSpeed = Vector2(3f, 1f)
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
