package simulation.combat.spatial

import silentorb.mythic.accessorize.Accessory
import silentorb.mythic.accessorize.Modifier
import silentorb.mythic.characters.rigs.CharacterRig
import silentorb.mythic.ent.Id
import simulation.combat.general.Destructible
import silentorb.mythic.ent.Table
import silentorb.mythic.performing.Action
import silentorb.mythic.physics.Body
import silentorb.mythic.physics.CollisionObject

data class SpatialCombatDeck(
    val accessories: Table<Accessory>,
    val actions: Table<Action>,
    val bodies: Table<Body>,
    val characterRigs: Table<CharacterRig>,
    val collisionShapes: Table<CollisionObject>,
    val destructibles: Table<Destructible>,
    val missiles: Table<Missile>,
    val targets: Table<Id>
)
