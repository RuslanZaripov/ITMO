package airline.service

import airline.api.Flight
import airline.api.Ticket
import kotlin.coroutines.coroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

data class PassengerNotification(
    val flightId: String,
    val message: String,
)

class PassengerNotificationService(
    private val flights: MutableStateFlow<List<Flight>>,
    private val bufferedEmailService: BufferedEmailService,
) {
    private val notificationMessages = Channel<PassengerNotification>()

    suspend fun notifyPassengers(flightId: String, message: String) {
        notificationMessages.send(PassengerNotification(flightId, message))
    }

    suspend fun run() {
        with(CoroutineScope(coroutineContext)) {
            launch {
                for (notification in notificationMessages) {
                    flights.value
                        .find { it.flightId == notification.flightId }
                        ?.let { flight ->
                            flight.tickets.values
                                .map(Ticket::passengerEmail)
                                .forEach { passengerEmail ->
                                    bufferedEmailService.send(passengerEmail, notification.message)
                                }
                        }
                }
            }
        }
    }
}
