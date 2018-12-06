package simulation

import mythic.ent.Entity
import mythic.ent.Id
import mythic.spatial.Vector4

data class Light(
    override val id: Id,
    val color: Vector4, // w is brightness
    val range: Float
) : Entity