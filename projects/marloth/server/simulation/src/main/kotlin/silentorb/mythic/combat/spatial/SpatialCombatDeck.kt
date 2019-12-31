package silentorb.mythic.combat.spatial

import silentorb.mythic.accessorize.Accessory
import silentorb.mythic.accessorize.Modifier
import silentorb.mythic.characters.CharacterRig
import silentorb.mythic.combat.general.Destructible
import silentorb.mythic.ent.Table
import silentorb.mythic.physics.Body

data class SpatialCombatDeck(
    val accessories: Table<Accessory>,
    val bodies: Table<Body>,
    val characterRigs: Table<CharacterRig>,
    val destructibles: Table<Destructible>,
    val modifiers: Table<Modifier>
)
