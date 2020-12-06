package marloth.definition.data.persistence

import silentorb.mythic.ent.PropertyInfo
import silentorb.mythic.ent.PropertySchema

object PersistenceProperties {
  const val leader = "leader"
}

val persistencePropertiesInfo: PropertySchema = mapOf(
    PersistenceProperties.leader to PropertyInfo(
        type = String,
    )
)
