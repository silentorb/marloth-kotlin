package simulation.happenings

import marloth.scenery.enums.ClientCommand
import silentorb.mythic.happenings.Command
import silentorb.mythic.happenings.Commands

data class Notification(
    val message: String,
    val from: String? = null,
    val elapsedTime: Float = 0f,
)

const val notificationDuration = 5f

const val notificationCommandType = "marloth.notification"

fun notify(to: Any, message: String, from: String? = null) =
    Command(
        type = notificationCommandType,
        target = to,
        value = Notification(
            message = message,
            from = from,
        )
    )

fun updateNotifications(delta: Float, commands: Commands, notifications: List<Notification>): List<Notification> =
    notifications.filter { it.elapsedTime < notificationDuration }
        .map { notification ->
          notification.copy(
              elapsedTime = notification.elapsedTime + delta
          )
        } + commands
        .filter { it.type == notificationCommandType }
        .mapNotNull { command -> command.value as? Notification }
