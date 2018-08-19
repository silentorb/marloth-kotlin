package intellect

import colliding.rayCanHitPoint
import simulation.Character

const val viewingRange = 30f

fun isInAngleOfView(viewer: Character, target: Character): Boolean =
    viewer.facingVector.dot((target.body.position - viewer.body.position).normalize()) > 0.5f

fun isInVisibleRange(viewer: Character, target: Character): Boolean =
    viewer.body.position.distance(target.body.position) <= viewingRange

fun canSee(viewer: Character, target: Character): Boolean = false
//    isInVisibleRange(viewer, target)
//        && isInAngleOfView(viewer, target)
//        && rayCanHitPoint(viewer.body.node, viewer.body.position, target.body.position)
