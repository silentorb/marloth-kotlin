package metahub.metaview.front.views

import javafx.scene.Node
import javafx.scene.control.ComboBox
import javafx.scene.control.ToolBar
import metahub.metaview.common.Emitter
import metahub.metaview.common.Event
import metahub.metaview.common.CommonEvent


fun toolBarView(state: CommonState, emit: Emitter): Node {
  val domainSelect = ComboBox<Domain>()
  domainSelect.items.addAll(Domain.modeling, Domain.texturing)
  domainSelect.value = state.domain
  domainSelect.valueProperty().addListener { event ->
    emit(Event(CommonEvent.switchDomain, domainSelect.value))
  }
  val toolbar = ToolBar(
      domainSelect
  )
  return toolbar
}