package metahub.metaview.front.views

import javafx.scene.Node
import javafx.scene.control.ComboBox
import javafx.scene.control.ToolBar
import metahub.metaview.common.Emitter
import metahub.metaview.common.Event
import metahub.metaview.front.AppState
import metahub.metaview.front.Domain
import metahub.metaview.front.DomainEvent


fun toolBarView(state: AppState, emit: Emitter): Node {
  val domainSelect = ComboBox<Domain>()
  domainSelect.items.addAll(Domain.modeling, Domain.texturing)
  domainSelect.value = state.domain
  domainSelect.valueProperty().addListener { event ->
    emit(Event(DomainEvent.switchDomain, domainSelect.value))
  }
  val toolbar = ToolBar(
      domainSelect
  )
  return toolbar
}