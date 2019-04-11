package simulation.data

import colliding.ShapeOffset
import colliding.Sphere
import mythic.spatial.Matrix
import mythic.spatial.Vector4
import physics.DynamicBody
import simulation.Depiction
import simulation.DepictionType
import simulation.Hand
import simulation.Light

enum class EntityTemplate {
  character,
  wallLamp,
  missile
}

val entityTemplates: Map<EntityTemplate, Hand> = mapOf(
    EntityTemplate.wallLamp to Hand(
        id = 0,
        collisionShape = ShapeOffset(Matrix().translate(1f, 0f, 0.8f), Sphere(1f)),
        depiction = Depiction(
            type = DepictionType.wallLamp
        ),
        dynamicBody = DynamicBody(
            gravity = false,
            mass = 0f,
            resistance = 0f
        ),
        light = Light(
            color = Vector4(1f),
            range = 15f
        )
    )
)