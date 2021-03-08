package simulation.happenings

import silentorb.mythic.happenings.Command

const val requestCommandType = "requestCommand"

data class Request(
    val type: Any,
    val from: Any,
    val to: Any,
    val value: Any? = null,
)

fun requestCommand(type: Any, from: Any, to: Any,  value: Any? = null) =
    Command(
        type = requestCommandType,
        value = Request(type, to, from, value),
        target = to,
    )

typealias Requests = List<Request>

fun addRequest(request: Request, requests: Requests): Requests =
    requests + request

fun removeRequest(request: Request, requests: Requests): Requests =
    requests - request
