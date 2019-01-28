package metahub.metaview.texturing

import metahub.metaview.common.StateTransformListener
import metahub.metaview.common.eventTypeSwitch
import mythic.ent.pass

typealias TexturingTransform = (TexturingState) -> TexturingState

fun setTilePreview(value: Boolean): TexturingTransform = { state ->
  state.copy(
      tilePreview = value
  )
}

fun updateTexturingState(event: TexturingEvent, data: Any): TexturingTransform =
    when (event) {
      TexturingEvent.setTilePreview -> setTilePreview(data as Boolean)
      else -> ::pass
    }

val texturingListener: StateTransformListener<TexturingState> = eventTypeSwitch(::updateTexturingState)