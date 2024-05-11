package airline.service

import airline.AirlineMessage
import airline.api.*
import airline.find
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.Instant

class BookingServiceImpl(
    private val config: AirlineConfig,
    private val flights: MutableStateFlow<List<Flight>>,
    private val airlineManagementMessagesFlow: MutableSharedFlow<AirlineMessage>,
) : BookingService {
    override val flightSchedule: List<FlightInfo>
        get() = flights.value
            .filter {
                it.hasFreeSeats() &&
                    !it.isCancelled &&
                    !it.isDeparted() &&
                    !it.isTicketSaleOver(config.ticketSaleEndTime)
            }
            .map(Flight::toFlightInfo)

    override fun freeSeats(flightId: String, departureTime: Instant): Set<String> {
        return flights.value.find(flightId, departureTime)?.let {
            it.plane.seats - it.tickets.keys
        } ?: return emptySet()
    }

    override suspend fun buyTicket(
        flightId: String,
        departureTime: Instant,
        seatNo: String,
        passengerId: String,
        passengerName: String,
        passengerEmail: String,
    ) {
        val ticket = Ticket(
            flightId = flightId,
            departureTime = departureTime,
            seatNo = seatNo,
            passengerId = passengerId,
            passengerName = passengerName,
            passengerEmail = passengerEmail,
        )

        airlineManagementMessagesFlow.emit(BuyTicket(flightId, departureTime, ticket))
    }
}
