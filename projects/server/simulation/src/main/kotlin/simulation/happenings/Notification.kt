package simulation.happenings

import marloth.scenery.enums.ClientCommand
import silentorb.mythic.happenings.Commands

data class Notification(
    val message: String,
    val from: String? = null,
    val elapsedTime: Float = 0f,
)

const val notificationDuration = 5f

fun updateNotifications(delta: Float, commands: Commands, notifications: List<Notification>): List<Notification> =
    notifications.filter { it.elapsedTime < notificationDuration }
        .map { notification ->
          notification.copy(
              elapsedTime = notification.elapsedTime + delta
          )
        } + commands
        .filter { it.type == ClientCommand.notification }
        .mapNotNull { command -> command.value as? Notification }
