package mythic.bloom.arranging

import mythic.bloom.*

fun fixed(bounds: FlexBounds): Arranger = { (_, _, c) -> Pair(bounds, c.map { bounds }) }

fun fixed(bounds: Bounds): Arranger = fixed(convertBounds(bounds))

val inherit: Arranger = { (_, s, c) -> Pair(s, c) }

fun hlistOld(lengths: List<Int?>): Arranger {
  return { (p, s, c) ->
    if (isResolved(p))
      Pair(p,
          applyLengths(p[FlexProperty.left]!!, resolveLengths(p[FlexProperty.width]!!, lengths), 0, c)
      )
    else
      Pair(s, c)
  }
}